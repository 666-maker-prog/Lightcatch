package com.lightcatch.workflow.node;

import com.lightcatch.ai.config.ModelFactory;
import com.lightcatch.common.exception.BusinessException;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * LLM 调用节点
 * 以文本为输入，调大模型生成后返回文本
 */
@Slf4j
@LiteflowComponent("LLM_Node")
public class LLMNodeComponent extends NodeComponent {

    @Autowired
    private ModelFactory modelFactory;

    @Override
    public void process() throws Exception {
        log.info("LLM node executing...");
    }

    /** 供 FlowServiceImpl 直接调用 */
    public String execute(String input) {
        try {
            log.info("LLM node executing with input: length={}", input.length());
            ChatModel llm = modelFactory.createChatModel((String) null);
            // 系统提示词：直接生成内容，不要废话
            String systemPrompt = "你是「拾光LightCatch」AI 创作助手。直接根据要求创作内容，不要解释你在做什么，"
                    + "不要使用'好的！''我来为你''以下是我为你'等开头语，不要出现'希望这篇...''如果你需要...'等结尾语。"
                    + "直接输出内容本身。";
            List<ChatMessage> messages = java.util.List.of(new SystemMessage(systemPrompt), new UserMessage(input));
            dev.langchain4j.model.chat.response.ChatResponse resp = llm.chat(messages);
            String text = resp.aiMessage().text();
            log.info("LLM node completed: response length={}", text.length());
            return text;
        } catch (BusinessException e) {
            log.warn("LLM node skipped: {}", e.getMessage());
            return input;
        }
    }
}
