package com.lightcatch.ai.service.knowledge.impl;

import com.lightcatch.ai.config.ModelFactory;
import com.lightcatch.ai.entity.AiDocChunk;
import com.lightcatch.ai.entity.AiKnowledge;
import com.lightcatch.ai.entity.AiKnowledgeDoc;
import com.lightcatch.ai.mapper.AiDocChunkMapper;
import com.lightcatch.ai.mapper.AiKnowledgeDocMapper;
import com.lightcatch.ai.mapper.AiKnowledgeMapper;
import com.lightcatch.ai.rag.ContentChunker;
import com.lightcatch.ai.rag.VectorStore;
import com.lightcatch.ai.service.knowledge.IEmbeddingService;
import com.lightcatch.common.constant.AiConstants;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class EmbeddingServiceImpl implements IEmbeddingService {

    @Autowired private AiKnowledgeMapper knowledgeMapper;
    @Autowired private AiKnowledgeDocMapper docMapper;
    @Autowired private AiDocChunkMapper chunkMapper;
    @Autowired private ModelFactory modelFactory;
    @Autowired private ContentChunker contentChunker;
    @Autowired private VectorStore vectorStore;

    @Override
    public void embedDocument(String knowledgeId, String docId) {
        AiKnowledgeDoc doc = docMapper.selectById(docId);
        if (doc == null) { log.warn("Document not found: {}", docId); return; }

        String content = doc.getContent();
        if (content == null || content.trim().isEmpty()) {
            doc.setStatus(2); doc.setErrorMsg("文档内容为空");
            docMapper.updateById(doc); return;
        }

        // Get embedding model
        AiKnowledge knowledge = knowledgeMapper.selectById(knowledgeId);
        String embedModelId = knowledge != null ? knowledge.getEmbedModelId() : null;

        try {
            EmbeddingModel embeddingModel = modelFactory.createEmbeddingModel(embedModelId);

            // Chunk
            List<String> chunks = contentChunker.chunk(content,
                    AiConstants.DEFAULT_CHUNK_SIZE, AiConstants.DEFAULT_CHUNK_OVERLAP);

            // Delete old chunks
            chunkMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiDocChunk>()
                    .eq(AiDocChunk::getDocId, docId));
            vectorStore.deleteByDocId(docId);

            // Create new chunks
            int index = 0;
            for (String chunkText : chunks) {
                if (chunkText.trim().isEmpty()) continue;

                Embedding embedding = embeddingModel.embed(chunkText).content();
                float[] vector = embedding.vector();

                AiDocChunk chunk = new AiDocChunk();
                chunk.setId(UUID.randomUUID().toString().replace("-", ""));
                chunk.setDocId(docId);
                chunk.setKnowledgeId(knowledgeId);
                chunk.setContent(chunkText);
                chunk.setChunkIndex(index);
                chunk.setTenantId(doc.getTenantId());
                chunk.setCreateTime(new java.util.Date());
                chunkMapper.insert(chunk);

                // Store vector in memory + file
                vectorStore.insert(chunk.getId(), docId, knowledgeId, chunkText,
                        index, doc.getCreateBy(), vector);
                index++;
            }

            doc.setChunkCount(index);
            doc.setStatus(1);
            doc.setErrorMsg(null);
            docMapper.updateById(doc);
            log.info("Document embedded: {} with {} chunks", docId, index);

        } catch (Exception e) {
            log.error("Embedding failed for doc: {}", docId, e);
            doc.setStatus(2);
            doc.setErrorMsg(e.getMessage());
            docMapper.updateById(doc);
        }
    }

    @Override
    public String getEmbeddingModelId(String knowledgeId) {
        AiKnowledge knowledge = knowledgeMapper.selectById(knowledgeId);
        return knowledge != null ? knowledge.getEmbedModelId() : null;
    }
}
