package com.lightcatch.api.config;

import com.lightcatch.common.model.Result;
import com.lightcatch.workflow.entity.AiFlow;
import com.lightcatch.workflow.service.IFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wf")
public class WorkflowGenController {

    @Autowired
    private IFlowService flowService;

    @PostMapping("/gen")
    public Result<?> generate(@RequestBody Map<String, String> body) {
        String description = body.get("description");
        if (description == null || description.trim().isEmpty()) {
            return Result.error("请描述你的创作流程");
        }
        try {
            AiFlow flow = flowService.generateFromText(description);
            return Result.ok(flow);
        } catch (Exception e) {
            return Result.error("解析失败: " + e.getMessage());
        }
    }
}
