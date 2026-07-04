package com.lightcatch.ai.config;

import com.lightcatch.ai.entity.AiModel;
import com.lightcatch.ai.service.model.IModelService;
import com.lightcatch.common.constant.AiConstants;
import com.lightcatch.common.exception.BusinessException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ModelFactory {

    @Autowired
    @Lazy
    private IModelService modelService;

    public ChatModel createChatModel(String modelId) {
        AiModel model = null;
        if (modelId != null && !modelId.isEmpty()) {
            model = modelService.getById(modelId);
        }
        if (model == null) {
            model = modelService.getDefaultChatModel();
        }
        if (model == null) {
            throw new BusinessException("未配置对话模型，请先在「模型管理」中添加并激活一个对话模型");
        }
        if (model.getApiKey() == null || model.getApiKey().isEmpty() || model.getApiKey().contains("placeholder")) {
            throw new BusinessException("模型「" + model.getName() + "」的 API Key 未配置或无效");
        }
        return buildChatModel(model);
    }

    public ChatModel createChatModel(AiModel model) {
        if (model == null) return createChatModel((String) null);
        if (model.getApiKey() == null || model.getApiKey().isEmpty()) {
            return createChatModel((String) null);
        }
        return buildChatModel(model);
    }

    public EmbeddingModel createEmbeddingModel(String modelId) {
        AiModel model = null;
        if (modelId != null && !modelId.isEmpty()) {
            model = modelService.getById(modelId);
        }
        if (model == null) {
            model = modelService.getDefaultEmbeddingModel();
        }
        if (model == null) {
            // 没有默认模型时，取第一个可用的向量模型
            java.util.List<com.lightcatch.ai.entity.AiModel> models = modelService.findByType(com.lightcatch.common.constant.AiConstants.MODEL_TYPE_EMBED);
            if (!models.isEmpty()) {
                model = models.get(0);
            }
        }
        if (model == null) {
            throw new BusinessException("未配置向量模型，请先在「模型管理」中添加并激活一个向量模型");
        }
        if (model.getApiKey() == null || model.getApiKey().isEmpty()) {
            throw new BusinessException("向量模型「" + model.getName() + "」的 API Key 未配置");
        }
        String baseUrl = model.getBaseUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://api.openai.com/v1";
        }
        return OpenAiEmbeddingModel.builder()
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .baseUrl(baseUrl)
                .build();
    }

    public int getEmbeddingDimension(String modelId) {
        try {
            AiModel model = modelId != null ? modelService.getById(modelId) : modelService.getDefaultEmbeddingModel();
            if (model == null) return AiConstants.DEFAULT_EMBEDDING_DIM;
            String name = model.getModelName() != null ? model.getModelName().toLowerCase() : "";
            if (name.contains("3-small")) return 1536;
            if (name.contains("3-large")) return 3072;
            if (name.contains("v3") || name.contains("bge-m3")) return 1024;
            return 1536;
        } catch (Exception e) {
            return AiConstants.DEFAULT_EMBEDDING_DIM;
        }
    }

    private ChatModel buildChatModel(AiModel model) {
        String baseUrl = model.getBaseUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            if ("DeepSeek".equalsIgnoreCase(model.getProvider())) {
                baseUrl = "https://api.deepseek.com/v1";
            } else if ("Qwen".equalsIgnoreCase(model.getProvider())) {
                baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
            } else {
                baseUrl = "https://api.openai.com/v1";
            }
        }
        log.info("Creating chat model: provider={}, model={}, baseUrl={}", model.getProvider(), model.getModelName(), baseUrl);
        return OpenAiChatModel.builder()
                .apiKey(model.getApiKey())
                .modelName(model.getModelName())
                .baseUrl(baseUrl)
                .temperature(0.7)
                .maxTokens(4096)
                .build();
    }
}
