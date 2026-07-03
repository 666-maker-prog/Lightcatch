package com.lightcatch.ai.service.chat;

import com.lightcatch.ai.entity.AiChatMessage;
import com.lightcatch.ai.entity.AiConversation;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface IChatService {
    SseEmitter streamChat(String conversationId, String message, String modelId,
                           String knowledgeId, String userId, String tenantId);
    SseEmitter receiveByRequestId(String requestId);
    void stopRequest(String requestId);
    List<AiConversation> getConversations(String userId);
    List<AiChatMessage> getMessages(String conversationId);
    void deleteConversation(String conversationId);
}
