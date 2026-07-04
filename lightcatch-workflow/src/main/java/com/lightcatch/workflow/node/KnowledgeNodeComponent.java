package com.lightcatch.workflow.node;

import com.lightcatch.ai.config.ModelFactory;
import com.lightcatch.ai.rag.VectorStore;
import com.lightcatch.common.exception.BusinessException;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库检索节点
 * 以搜索词为输入，从 VectorStore 检索相关内容后返回
 */
@Slf4j
@LiteflowComponent("Knowledge_Node")
public class KnowledgeNodeComponent extends NodeComponent {

    @Autowired private ModelFactory modelFactory;
    @Autowired private VectorStore vectorStore;

    @Override
    public void process() throws Exception {
        log.info("Knowledge node executing...");
    }

    /** 供 FlowServiceImpl 直接调用 */
    public String execute(String input) {
        try {
            log.info("Knowledge node searching: query='{}'", input);
            EmbeddingModel embedModel = modelFactory.createEmbeddingModel(null);
            Embedding queryEmbed = embedModel.embed(input).content();
            float[] vector = queryEmbed.vector();

            // 搜索所有知识库（knowledgeId 为空字符串表示搜索全部）
            List<VectorStore.SearchResult> results = vectorStore.search("", vector, 5, 0.7);

            if (results.isEmpty()) {
                return "（知识库未找到相关内容）";
            }

            String context = results.stream()
                .map(r -> r.getContent())
                .collect(Collectors.joining("\n---\n"));
            log.info("Knowledge node: {} chunks retrieved", results.size());
            return context;
        } catch (BusinessException e) {
            log.warn("Knowledge node skipped: {}", e.getMessage());
            return input;
        }
    }
}
