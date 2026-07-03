package com.lightcatch.ai.controller;

import com.lightcatch.ai.entity.AiMcp;
import com.lightcatch.common.model.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lightcatch.ai.mapper.AiMcpMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/mcp")
public class McpController {

    @Autowired
    private AiMcpMapper mcpMapper;

    @GetMapping("/list")
    public Result<List<AiMcp>> list() {
        return Result.ok(mcpMapper.selectList(
                new LambdaQueryWrapper<AiMcp>().orderByDesc(AiMcp::getCreateTime)));
    }

    @PostMapping("/create")
    public Result<?> create(@RequestBody AiMcp mcp) {
        mcp.setStatus(1);
        mcpMapper.insert(mcp);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable String id, @RequestBody AiMcp mcp) {
        mcp.setId(id);
        mcpMapper.updateById(mcp);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id) {
        mcpMapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/{id}/test")
    public Result<Boolean> testConnection(@PathVariable String id) {
        AiMcp mcp = mcpMapper.selectById(id);
        if (mcp == null) return Result.error("插件不存在");
        try {
            java.net.URL url = new java.net.URL(mcp.getEndpoint());
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            return Result.ok(code == 200, code == 200 ? "连接成功" : "连接失败(" + code + ")");
        } catch (Exception e) {
            return Result.ok(false, "连接失败: " + e.getMessage());
        }
    }
}
