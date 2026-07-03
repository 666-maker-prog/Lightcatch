package com.lightcatch.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "lightcatch.ai")
public class AiConfig {
    private double defaultChatTemp = 0.7;
    private int defaultMaxTokens = 4096;
    private int chunkSize = 500;
    private int chunkOverlap = 50;
    private int retrievalTopK = 5;
    private double embeddingDimension = 1536;
    private String uploadPath = "./upload";
}
