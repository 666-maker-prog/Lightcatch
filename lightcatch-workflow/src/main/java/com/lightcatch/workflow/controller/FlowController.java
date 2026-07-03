package com.lightcatch.workflow.controller;

import com.lightcatch.common.model.Result;
import com.lightcatch.workflow.entity.AiFlow;
import com.lightcatch.workflow.service.IFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/flow")
public class FlowController {

    @Autowired
    private IFlowService flowService;

    @GetMapping("/list")
    public Result<List<AiFlow>> list() {
        return Result.ok(flowService.list());
    }

    @PostMapping("/create")
    public Result<?> create(@RequestBody AiFlow flow) {
        flow.setStatus(1);
        flowService.save(flow);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable String id, @RequestBody AiFlow flow) {
        flow.setId(id);
        flowService.updateById(flow);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id) {
        flowService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/run")
    public Result<?> runFlow(@RequestBody Map<String, Object> params) {
        String flowId = (String) params.get("flowId");
        String input = (String) params.get("input");
        try {
            String result = flowService.executeFlow(flowId, input);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.error("流程执行失败: " + e.getMessage());
        }
    }
}
