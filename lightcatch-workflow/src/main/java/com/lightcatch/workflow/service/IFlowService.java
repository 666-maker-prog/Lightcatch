package com.lightcatch.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lightcatch.workflow.entity.AiFlow;

public interface IFlowService extends IService<AiFlow> {
    String executeFlow(String flowId, String input) throws Exception;
}
