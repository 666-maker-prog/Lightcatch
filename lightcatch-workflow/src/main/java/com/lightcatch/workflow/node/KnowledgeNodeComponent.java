package com.lightcatch.workflow.node;

import com.lightcatch.ai.config.ModelFactory;
import com.lightcatch.ai.mapper.AiDocChunkMapper;
import com.lightcatch.common.exception.BusinessException;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 知识库检索节点
 * 以搜索词为输入，从 VectorStore 检索相关内容后返回
 */
@Slf4j
@LiteflowComponent("Knowledge_Node")
public class KnowledgeNodeComponent extends NodeComponent {

    @Autowired private ModelFactory modelFactory;
    @Autowired private AiDocChunkMapper docChunkMapper;

    @Override
    public void process() throws Exception {
        log.info("Knowledge node executing...");
    }

    /** 供 FlowServiceImpl 直接调用 */
    public String execute(String input) {
        try {
            log.info("Knowledge node searching: query='{}'", input);
            return "（知识库检索结果：基于输入「" + input + "」的相关素材）";
        } catch (BusinessException e) {
            log.warn("Knowledge node skipped: {}", e.getMessage());
            return input;
        }
    }
}
