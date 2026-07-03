package com.lightcatch.ai.rag;

import com.lightcatch.ai.entity.AiDocChunk;
import com.lightcatch.ai.mapper.AiDocChunkMapper;
import com.lightcatch.common.constant.AiConstants;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Retriever {

    @Autowired
    private AiDocChunkMapper docChunkMapper;

    public List<String> retrieve(EmbeddingModel embeddingModel, String knowledgeId,
                                  String query, int topK, double minScore) {
        dev.langchain4j.data.embedding.Embedding queryEmbed = embeddingModel.embed(query).content();
        float[] vector = queryEmbed.vector();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");

        List<Map<String, Object>> results = docChunkMapper.vectorSearch(
                knowledgeId, sb.toString(), topK, minScore);

        return results.stream()
                .map(r -> r.get("content").toString())
                .collect(Collectors.toList());
    }
}
