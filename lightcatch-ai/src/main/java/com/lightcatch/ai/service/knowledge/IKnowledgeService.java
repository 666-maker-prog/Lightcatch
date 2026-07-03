package com.lightcatch.ai.service.knowledge;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lightcatch.ai.entity.AiKnowledge;
import com.lightcatch.ai.entity.AiKnowledgeDoc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IKnowledgeService extends IService<AiKnowledge> {
    void uploadDocument(String knowledgeId, MultipartFile file, String userId);
    void deleteDocument(String docId);
    void deleteKnowledge(String knowledgeId);
    List<AiKnowledgeDoc> getDocuments(String knowledgeId);
    AiKnowledgeDoc getDocument(String docId);
}
