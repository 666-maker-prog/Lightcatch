package com.lightcatch.ai.service.knowledge.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lightcatch.ai.entity.AiDocChunk;
import com.lightcatch.ai.entity.AiKnowledge;
import com.lightcatch.ai.entity.AiKnowledgeDoc;
import com.lightcatch.ai.mapper.AiDocChunkMapper;
import com.lightcatch.ai.mapper.AiKnowledgeDocMapper;
import com.lightcatch.ai.mapper.AiKnowledgeMapper;
import com.lightcatch.ai.service.knowledge.IEmbeddingService;
import com.lightcatch.ai.service.knowledge.IKnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class KnowledgeServiceImpl extends ServiceImpl<AiKnowledgeMapper, AiKnowledge> implements IKnowledgeService {

    @Autowired
    private AiKnowledgeDocMapper docMapper;
    @Autowired
    private AiDocChunkMapper chunkMapper;
    @Autowired
    private IEmbeddingService embeddingService;

    private final Tika tika = new Tika();

    @Override
    public void uploadDocument(String knowledgeId, MultipartFile file, String userId) {
        try {
            // Extract text content
            String content;
            try (InputStream is = file.getInputStream()) {
                content = tika.parseToString(is);
            }

            // Create document record
            AiKnowledgeDoc doc = new AiKnowledgeDoc();
            doc.setId(UUID.randomUUID().toString().replace("-", ""));
            doc.setKnowledgeId(knowledgeId);
            doc.setTitle(file.getOriginalFilename());
            doc.setFileType(file.getContentType());
            doc.setContent(content);
            doc.setWordCount(content.length());
            doc.setStatus(0); // Processing
            doc.setUserId(userId);
            doc.setCreateBy(userId);
            docMapper.insert(doc);

            // Start embedding async (simplified - sync for now)
            try {
                embeddingService.embedDocument(knowledgeId, doc.getId());
            } catch (Exception e) {
                log.error("Embedding failed for doc: {}", doc.getId(), e);
                doc.setStatus(2);
                doc.setErrorMsg(e.getMessage());
                docMapper.updateById(doc);
            }

            // Update knowledge doc count
            AiKnowledge knowledge = getById(knowledgeId);
            if (knowledge != null) {
                Long count = docMapper.selectCount(
                        new LambdaQueryWrapper<AiKnowledgeDoc>()
                                .eq(AiKnowledgeDoc::getKnowledgeId, knowledgeId));
                knowledge.setDocCount(count != null ? count.intValue() : 0);
                updateById(knowledge);
            }

            log.info("Document uploaded: {} ({})", file.getOriginalFilename(), content.length());
        } catch (Exception e) {
            log.error("Upload failed", e);
            throw new RuntimeException("文档上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteDocument(String docId) {
        chunkMapper.delete(new LambdaQueryWrapper<AiDocChunk>().eq(AiDocChunk::getDocId, docId));
        docMapper.deleteById(docId);
    }

    @Override
    @Transactional
    public void deleteKnowledge(String knowledgeId) {
        // Delete all docs and their chunks
        List<AiKnowledgeDoc> docs = getDocuments(knowledgeId);
        for (AiKnowledgeDoc doc : docs) {
            chunkMapper.delete(new LambdaQueryWrapper<AiDocChunk>().eq(AiDocChunk::getDocId, doc.getId()));
        }
        docMapper.delete(new LambdaQueryWrapper<AiKnowledgeDoc>().eq(AiKnowledgeDoc::getKnowledgeId, knowledgeId));
        removeById(knowledgeId);
    }

    @Override
    public AiKnowledgeDoc getDocument(String docId) {
        return docMapper.selectById(docId);
    }

    @Override
    public List<AiKnowledgeDoc> getDocuments(String knowledgeId) {
        return docMapper.selectList(
                new LambdaQueryWrapper<AiKnowledgeDoc>()
                        .eq(AiKnowledgeDoc::getKnowledgeId, knowledgeId)
                        .orderByDesc(AiKnowledgeDoc::getCreateTime));
    }
}
