package com.lightcatch.ai.service.model.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lightcatch.ai.config.ModelFactory;
import com.lightcatch.ai.entity.AiModel;
import com.lightcatch.ai.mapper.AiModelMapper;
import com.lightcatch.ai.service.model.IModelService;
import com.lightcatch.common.constant.AiConstants;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel> implements IModelService {

    @Autowired
    @Lazy
    private ModelFactory modelFactory;

    @Override
    public List<AiModel> findByType(String type) {
        return baseMapper.findByType(type);
    }

    @Override
    public AiModel getDefaultChatModel() {
        return baseMapper.findDefaultByType(AiConstants.MODEL_TYPE_LLM);
    }

    @Override
    public AiModel getDefaultEmbeddingModel() {
        return baseMapper.findDefaultByType(AiConstants.MODEL_TYPE_EMBED);
    }

    @Override
    public boolean testConnection(String modelId) {
        try {
            AiModel model = getById(modelId);
            if (model == null) return false;
            if (AiConstants.MODEL_TYPE_LLM.equals(model.getModelType())) {
                ChatModel llm = modelFactory.createChatModel(model);
                String resp = llm.chat("ping");
                return resp != null && !resp.isEmpty();
            }
            return true;
        } catch (Exception e) {
            log.error("Model test failed: {}", e.getMessage());
            return false;
        }
    }
}
