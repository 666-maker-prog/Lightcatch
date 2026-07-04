package com.lightcatch.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lightcatch.workflow.entity.AiFlow;
import com.lightcatch.workflow.entity.AiFlowOutput;
import com.lightcatch.workflow.mapper.AiFlowOutputMapper;
import com.lightcatch.workflow.service.IFlowOutputService;
import com.lightcatch.workflow.service.IFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FlowOutputServiceImpl extends ServiceImpl<AiFlowOutputMapper, AiFlowOutput> implements IFlowOutputService {

    @Autowired
    private IFlowService flowService;

    @Override
    public List<AiFlowOutput> getByUserId(String userId) {
        return baseMapper.selectList(new LambdaQueryWrapper<AiFlowOutput>()
                .eq(AiFlowOutput::getUserId, userId)
                .orderByDesc(AiFlowOutput::getCreateTime));
    }

    @Override
    public AiFlowOutput saveOutput(String flowId, String userId, String content) {
        AiFlow flow = flowService.getById(flowId);
        AiFlowOutput output = new AiFlowOutput();
        output.setId(UUID.randomUUID().toString().replace("-", ""));
        output.setFlowId(flowId);
        output.setUserId(userId);
        output.setTitle(flow != null ? flow.getName() : "未命名");
        output.setContent(content);
        output.setPlatforms("[]");
        output.setStatus(0);
        save(output);
        log.info("Saved output: {} for flow: {}", output.getId(), flowId);
        return output;
    }
}
