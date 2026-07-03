package com.lightcatch.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lightcatch.workflow.entity.AiFlow;
import com.lightcatch.workflow.mapper.AiFlowMapper;
import com.lightcatch.workflow.service.IFlowService;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FlowServiceImpl extends ServiceImpl<AiFlowMapper, AiFlow> implements IFlowService {

    @Autowired
    private FlowExecutor flowExecutor;

    @Override
    public String executeFlow(String flowId, String input) throws Exception {
        AiFlow flow = getById(flowId);
        if (flow == null) throw new Exception("流程不存在: " + flowId);
        log.info("Executing flow: {} with input: {}", flow.getName(), input);

        String chain = flow.getChain();
        if (chain == null || chain.isEmpty()) {
            throw new Exception("流程「" + flow.getName() + "」未配置任何节点");
        }

        String[] nodeIds = chain.split("\\s*->\\s*");
        StringBuilder elBuilder = new StringBuilder("THEN(");
        for (int i = 0; i < nodeIds.length; i++) {
            if (i > 0) elBuilder.append(",");
            elBuilder.append(nodeIds[i].trim());
        }
        elBuilder.append(")");
        String el = elBuilder.toString();
        log.info("LiteFlow EL: {}", el);

        LiteflowResponse response = flowExecutor.execute2Resp(el, null, input);
        if (response.isSuccess()) {
            log.info("Flow execution completed successfully");
            return "流程执行完成";
        } else {
            log.error("Flow execution failed: {}", response.getMessage());
            throw new Exception("流程执行失败: " + response.getMessage());
        }
    }
}
