package com.lightcatch.workflow.node;

import com.lightcatch.ai.config.ModelFactory;
import com.lightcatch.common.exception.BusinessException;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@LiteflowComponent("LLM_Node")
public class LLMNodeComponent extends NodeComponent {

    @Autowired
    private ModelFactory modelFactory;

    @Override
    public void process() throws Exception {
        log.info("LLM node executing...");
        try {
            String input = this.getNodeId();
            ChatModel llm = modelFactory.createChatModel((String) null);
            String response = llm.chat("Hello");
            log.info("LLM node completed: response length={}", response.length());
        } catch (BusinessException e) {
            log.warn("LLM node skipped: {}", e.getMessage());
        }
    }
}
