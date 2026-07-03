package com.lightcatch.ai.service.knowledge;

public interface IEmbeddingService {
    void embedDocument(String knowledgeId, String docId);
    String getEmbeddingModelId(String knowledgeId);
}
