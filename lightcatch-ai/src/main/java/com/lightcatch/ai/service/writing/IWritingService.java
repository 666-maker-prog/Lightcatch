package com.lightcatch.ai.service.writing;

public interface IWritingService {
    String generate(String topic, String style, String knowledgeId, String modelId);
    String rewrite(String text, String style, String modelId);
    String generateTitles(String topic, String modelId);
    String generateOutline(String topic, String modelId);
    String optimize(String text, String modelId);
    String expand(String text, String modelId);
    String shorten(String text, String modelId);
}
