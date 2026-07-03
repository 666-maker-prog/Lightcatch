package com.lightcatch.workflow.node;

import com.lightcatch.ai.config.ModelFactory;
import com.lightcatch.ai.mapper.AiDocChunkMapper;
import com.lightcatch.common.exception.BusinessException;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@LiteflowComponent("Knowledge_Node")
public class KnowledgeNodeComponent extends NodeComponent {

    @Autowired private ModelFactory modelFactory;
    @Autowired private AiDocChunkMapper docChunkMapper;

    @Override
    public void process() throws Exception {
        log.info("Knowledge node executing...");
        try {
            log.info("Knowledge node: no knowledgeId configured, skipping");
        } catch (BusinessException e) {
            log.warn("Knowledge node skipped: {}", e.getMessage());
        }
    }
}
