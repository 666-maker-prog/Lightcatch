package com.lightcatch.ai.service.chat.impl;

import com.lightcatch.ai.config.ModelFactory;
import com.lightcatch.ai.entity.AiChatMessage;
import com.lightcatch.ai.entity.AiConversation;
import com.lightcatch.ai.entity.AiModel;
import com.lightcatch.ai.entity.AiKnowledgeDoc;
import com.lightcatch.ai.mapper.AiChatMessageMapper;
import com.lightcatch.ai.mapper.AiConversationMapper;
import com.lightcatch.ai.rag.PromptTemplate;
import com.lightcatch.ai.rag.Retriever;
import com.lightcatch.ai.service.chat.IChatService;
import com.lightcatch.ai.service.knowledge.IKnowledgeService;
import com.lightcatch.ai.service.model.IModelService;
import com.lightcatch.common.constant.AiConstants;
import com.lightcatch.common.exception.BusinessException;
import com.lightcatch.common.config.MyBatisPlusConfig;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl implements IChatService {

    @Autowired
    private AiConversationMapper conversationMapper;
    @Autowired
    private AiChatMessageMapper chatMessageMapper;
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private IModelService modelService;
    @Autowired
    private IKnowledgeService knowledgeService;
    @Autowired
    private Retriever retriever;
    @Autowired
    private PromptTemplate promptTemplate;
    @Autowired
    private ExecutorService sseExecutorService;

    // In-memory SSE history cache
    private static final Map<String, List<String>> SSE_HISTORY = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, SseEmitter> SSE_EMITTERS = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public SseEmitter streamChat(String conversationId, String message, String modelId,
                                  String knowledgeId, String userId, String tenantId) {
        MyBatisPlusConfig.TenantContext.setCurrentTenantId(tenantId);

        String requestId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(300_000L);
        SSE_EMITTERS.put(requestId, emitter);
        SSE_HISTORY.put(requestId, new ArrayList<>());

        // Get or create conversation
        AiConversation conversation = getOrCreateConversation(conversationId, modelId, userId, tenantId);

        // Save user message
        saveMessage(conversation.getId(), AiConstants.MESSAGE_ROLE_USER, message, null, modelId);

        final String finalTenantId = tenantId;

        sseExecutorService.execute(() -> {
            try {
                // Send request ID first
                Map<String, String> initEvent = new HashMap<>();
                initEvent.put("type", "init");
                initEvent.put("conversationId", conversation.getId());
                sendEvent(emitter, requestId, initEvent);

                // Load conversation history
                List<AiChatMessage> history = chatMessageMapper.findByConversationId(conversation.getId());
                List<ChatMessage> langchainMessages = new ArrayList<>();

                // Build system prompt
                String systemPrompt = "你是「拾光LightCatch」AI 创作助手，一个擅长创作、写作和内容生成的小助手。"
                        + "你的核心能力是帮助用户撰写文案、创作内容、提供灵感。"
                        + "注意：不要透露你的模型名称或技术细节，不要说你是一个AI大模型。"
                        + "请始终用中文回答，语气亲切友好。";
                String knowledgeContext = "";

                // RAG retrieval
                if (knowledgeId != null && !knowledgeId.isEmpty()) {
                    try {
                        String embedModelId = knowledgeService.getById(knowledgeId).getEmbedModelId();
                        EmbeddingModel embedModel = modelFactory.createEmbeddingModel(embedModelId);
                        List<String> chunks = retriever.retrieve(embedModel, knowledgeId, message, 5, 0.75);
                        if (!chunks.isEmpty()) {
                            knowledgeContext = String.join("\n---\n", chunks);
                        }
                    } catch (Exception e) {
                        log.warn("RAG retrieval failed: {}", e.getMessage());
                    }
                }

                String ragPrompt = promptTemplate.buildRagPrompt(message, knowledgeContext, systemPrompt);
                langchainMessages.add(new SystemMessage(ragPrompt));

                // Add history (last N turns)
                int maxHistoryMsgs = 10;
                List<AiChatMessage> recentHistory = history.size() > maxHistoryMsgs
                        ? history.subList(history.size() - maxHistoryMsgs, history.size())
                        : history;

                for (AiChatMessage msg : recentHistory) {
                    if (AiConstants.MESSAGE_ROLE_USER.equals(msg.getRole())) {
                        langchainMessages.add(new UserMessage(msg.getContent()));
                    } else if (AiConstants.MESSAGE_ROLE_AI.equals(msg.getRole())) {
                        langchainMessages.add(new AiMessage(msg.getContent()));
                    }
                }

                // Get LLM
                ChatModel llm = modelFactory.createChatModel(modelId);

                // Call LLM
                dev.langchain4j.model.chat.response.ChatResponse chatResp = llm.chat(langchainMessages);
                String response = chatResp.aiMessage().text();

                // Stream response in chunks (simulate token-by-token)
                StringBuilder fullResponse = new StringBuilder();
                int chunkSize = 5;
                for (int i = 0; i < response.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, response.length());
                    String chunk = response.substring(i, end);
                    fullResponse.append(chunk);

                    Map<String, String> tokenEvent = new HashMap<>();
                    tokenEvent.put("type", "token");
                    tokenEvent.put("content", chunk);
                    sendEvent(emitter, requestId, tokenEvent);

                    Thread.sleep(20); // simulate typing speed
                }

                // Save assistant message
                saveMessage(conversation.getId(), AiConstants.MESSAGE_ROLE_AI,
                        fullResponse.toString(), null, modelId);

                // Send done event
                Map<String, String> doneEvent = new HashMap<>();
                doneEvent.put("type", "done");
                doneEvent.put("conversationId", conversation.getId());
                sendEvent(emitter, requestId, doneEvent);
                emitter.complete();

            } catch (Exception e) {
                log.error("Chat stream error", e);
                try {
                    Map<String, String> errEvent = new HashMap<>();
                    errEvent.put("type", "error");
                    errEvent.put("content", e.getMessage());
                    sendEvent(emitter, requestId, errEvent);
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            } finally {
                SSE_EMITTERS.remove(requestId);
                SSE_HISTORY.remove(requestId);
                MyBatisPlusConfig.TenantContext.clear();
            }
        });

        return emitter;
    }

    @Override
    public SseEmitter receiveByRequestId(String requestId) {
        SseEmitter oldEmitter = SSE_EMITTERS.get(requestId);
        if (oldEmitter == null) return null;

        List<String> history = SSE_HISTORY.get(requestId);
        SseEmitter emitter = new SseEmitter(300_000L);

        sseExecutorService.execute(() -> {
            try {
                if (history != null) {
                    for (String data : history) {
                        Map<String, String> event = new HashMap<>();
                        event.put("type", "replay");
                        event.put("content", data);
                        sendEvent(emitter, requestId, event);
                        Thread.sleep(5);
                    }
                }
                Map<String, String> doneEvent = new HashMap<>();
                doneEvent.put("type", "done");
                doneEvent.put("conversationId", requestId);
                sendEvent(emitter, requestId, doneEvent);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @Override
    public void stopRequest(String requestId) {
        SseEmitter emitter = SSE_EMITTERS.remove(requestId);
        if (emitter != null) {
            try { emitter.complete(); } catch (Exception ignored) {}
        }
    }

    @Override
    public List<AiConversation> getConversations(String userId) {
        return conversationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .orderByDesc(AiConversation::getUpdateTime));
    }

    @Override
    public List<AiChatMessage> getMessages(String conversationId) {
        return chatMessageMapper.findByConversationId(conversationId);
    }

    @Override
    public void deleteConversation(String conversationId) {
        chatMessageMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getConversationId, conversationId));
        conversationMapper.deleteById(conversationId);
    }

    private AiConversation getOrCreateConversation(String conversationId, String modelId,
                                                     String userId, String tenantId) {
        if (conversationId != null && !conversationId.isEmpty()) {
            AiConversation existing = conversationMapper.selectById(conversationId);
            if (existing != null) return existing;
        }

        AiConversation conv = new AiConversation();
        conv.setUserId(userId);
        conv.setTitle("新对话");
        conv.setModelId(modelId);
        conv.setType(AiConstants.CONV_TYPE_CHAT);
        conv.setTenantId(tenantId);
        conv.setStatus(1);
        conversationMapper.insert(conv);
        return conv;
    }

    private void saveMessage(String conversationId, String role, String content,
                              String toolResult, String modelId) {
        AiChatMessage msg = new AiChatMessage();
        msg.setConversationId(conversationId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setToolExecutionResult(toolResult);
        msg.setModelName(modelId);
        chatMessageMapper.insert(msg);

        // Update conversation time
        AiConversation conv = new AiConversation();
        conv.setId(conversationId);
        conv.setUpdateTime(new Date());
        conversationMapper.updateById(conv);
    }

    private void sendEvent(SseEmitter emitter, String requestId, Map<String, String> data) throws IOException {
        String json = toJson(data);
        emitter.send(json);
        SSE_HISTORY.computeIfAbsent(requestId, k -> new ArrayList<>()).add(json);
    }

    private String toJson(Map<String, String> data) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : data.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":\"")
                    .append(e.getValue().replace("\"", "\\\"")).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
