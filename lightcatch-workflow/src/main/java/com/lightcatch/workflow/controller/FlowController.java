package com.lightcatch.workflow.controller;

import com.lightcatch.common.model.Result;
import com.lightcatch.common.util.JwtUtil;
import com.lightcatch.workflow.entity.AiFlow;
import com.lightcatch.workflow.service.IFlowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流控制器
 * 负责工作流的 CRUD、自然语言生成、手动执行
 */
@RestController
@RequestMapping("/api/ai/flow")
public class FlowController {

    @Autowired
    private IFlowService flowService;

    /** 获取当前用户的工作流列表 */
    @GetMapping("/list")
    public Result<List<AiFlow>> list(HttpServletRequest request) {
        String token = request.getHeader("X-Access-Token");
        String userId = JwtUtil.getUserId(token);
        return Result.ok(flowService.lambdaQuery().eq(AiFlow::getUserId, userId).orderByDesc(AiFlow::getCreateTime).list());
    }

    /** 手动创建一个空工作流（不常用，主要用 gen 接口） */
    @PostMapping("/create")
    public Result<?> create(@RequestBody AiFlow flow) {
        flow.setStatus(1);
        flowService.save(flow);
        return Result.ok();
    }

    /**
     * 自然语言生成工作流
     * 用户输入一段文字描述（如"每天8点从素材库找爆款，写一篇小红书"），
     * LLM 自动解析为结构化工作流并存入数据库
     */
    @PostMapping("/gen")
    public Result<?> generate(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String description = body.get("description");
        if (description == null || description.trim().isEmpty()) {
            return Result.error("请描述你的创作流程");
        }
        String token = request.getHeader("X-Access-Token");
        String userId = JwtUtil.getUserId(token);
        try {
            AiFlow flow = flowService.generateFromText(description, userId);
            return Result.ok(flow);
        } catch (Exception e) {
            return Result.error("解析失败: " + e.getMessage());
        }
    }

    /** 更新工作流名称/描述 */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable String id, @RequestBody AiFlow flow) {
        flow.setId(id);
        flowService.updateById(flow);
        return Result.ok();
    }

    /** 删除工作流 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id) {
        flowService.removeById(id);
        return Result.ok();
    }

    /**
     * 执行工作流
     * 解析 design 字段中的节点列表，按依赖顺序执行，结果自动保存到草稿箱
     */
    @PostMapping("/run")
    public Result<?> runFlow(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String flowId = (String) params.get("flowId");
        String input = (String) params.get("input");
        String token = request.getHeader("X-Access-Token");
        String userId = JwtUtil.getUserId(token);
        try {
            String result = flowService.executeFlow(flowId, input, userId);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.error("流程执行失败: " + e.getMessage());
        }
    }
}
