package com.lightcatch.ai.service.model;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lightcatch.ai.entity.AiModel;
import java.util.List;

public interface IModelService extends IService<AiModel> {
    List<AiModel> findByType(String type);
    AiModel getDefaultChatModel();
    AiModel getDefaultEmbeddingModel();
    boolean testConnection(String modelId);
}
