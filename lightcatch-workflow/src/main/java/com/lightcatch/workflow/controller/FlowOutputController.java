package com.lightcatch.workflow.controller;

import com.lightcatch.common.model.Result;
import com.lightcatch.common.util.JwtUtil;
import com.lightcatch.workflow.entity.AiFlowOutput;
import com.lightcatch.workflow.service.IFlowOutputService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/flow/output")
public class FlowOutputController {

    @Autowired
    private IFlowOutputService outputService;

    /** 草稿箱列表 */
    @GetMapping("/list")
    public Result<List<AiFlowOutput>> list(HttpServletRequest request) {
        String token = request.getHeader("X-Access-Token");
        String userId = JwtUtil.getUserId(token);
        return Result.ok(outputService.getByUserId(userId));
    }

    /** 更新草稿 */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable String id, @RequestBody AiFlowOutput output) {
        output.setId(id);
        outputService.updateById(output);
        return Result.ok();
    }

    /** 删除草稿 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id) {
        outputService.removeById(id);
        return Result.ok();
    }
}
