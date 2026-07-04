package com.lightcatch.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lightcatch.workflow.entity.AiFlowOutput;

import java.util.List;

public interface IFlowOutputService extends IService<AiFlowOutput> {
    List<AiFlowOutput> getByUserId(String userId);
    AiFlowOutput saveOutput(String flowId, String userId, String content);
}
