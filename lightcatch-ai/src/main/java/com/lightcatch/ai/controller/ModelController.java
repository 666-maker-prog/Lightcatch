package com.lightcatch.ai.controller;

import com.lightcatch.ai.entity.AiModel;
import com.lightcatch.ai.service.model.IModelService;
import com.lightcatch.common.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/model")
public class ModelController {

    @Autowired
    private IModelService modelService;

    @GetMapping("/list")
    public Result<List<AiModel>> list(@RequestParam(required = false) String type) {
        if (type != null) {
            return Result.ok(modelService.findByType(type));
        }
        return Result.ok(modelService.list());
    }

    @GetMapping("/{id}")
    public Result<AiModel> getById(@PathVariable String id) {
        return Result.ok(modelService.getById(id));
    }

    @PostMapping("/create")
    public Result<?> create(@RequestBody AiModel model) {
        if (model.getStatus() == null) model.setStatus(1);
        modelService.save(model);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable String id, @RequestBody AiModel model) {
        model.setId(id);
        modelService.updateById(model);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id) {
        modelService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/{id}/test")
    public Result<Boolean> testConnection(@PathVariable String id) {
        boolean ok = modelService.testConnection(id);
        return Result.ok(ok, ok ? "连接成功" : "连接失败");
    }

    @PutMapping("/{id}/default")
    public Result<?> setDefault(@PathVariable String id) {
        AiModel model = modelService.getById(id);
        if (model != null) {
            model.setIsDefault(true);
            modelService.updateById(model);
        }
        return Result.ok();
    }
}
