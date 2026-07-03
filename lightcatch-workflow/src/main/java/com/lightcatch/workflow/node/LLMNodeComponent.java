package com.lightcatch.workflow.node;

import com.lightcatch.ai.config.ModelFactory;
import com.lightcatch.common.exception.BusinessException;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
            String response = llm.chat(input);
            log.info("LLM node completed: response length={}", response.length());
            return response;
        } catch (BusinessException e) {
            log.warn("LLM node skipped: {}", e.getMessage());
            return input;
        }
    }
}
