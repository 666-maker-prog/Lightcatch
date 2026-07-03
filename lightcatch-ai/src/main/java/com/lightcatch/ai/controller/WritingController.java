package com.lightcatch.ai.controller;

import com.lightcatch.ai.service.writing.IWritingService;
import com.lightcatch.common.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/writing")
public class WritingController {

    @Autowired
    private IWritingService writingService;

    @PostMapping("/generate")
    public Result<String> generate(@RequestBody Map<String, String> body) {
        String topic = body.get("topic");
        String style = body.get("style");
        String knowledgeId = body.get("knowledgeId");
        String modelId = body.get("modelId");
        return Result.ok(writingService.generate(topic, style, knowledgeId, modelId));
    }

    @PostMapping("/rewrite")
    public Result<String> rewrite(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        String style = body.get("style");
        String modelId = body.get("modelId");
        return Result.ok(writingService.rewrite(text, style, modelId));
    }

    @PostMapping("/titles")
    public Result<String> generateTitles(@RequestBody Map<String, String> body) {
        String topic = body.get("topic");
        String modelId = body.get("modelId");
        return Result.ok(writingService.generateTitles(topic, modelId));
    }

    @PostMapping("/outline")
    public Result<String> generateOutline(@RequestBody Map<String, String> body) {
        String topic = body.get("topic");
        String modelId = body.get("modelId");
        return Result.ok(writingService.generateOutline(topic, modelId));
    }

    @PostMapping("/optimize")
    public Result<String> optimize(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        String modelId = body.get("modelId");
        return Result.ok(writingService.optimize(text, modelId));
    }

    @PostMapping("/expand")
    public Result<String> expand(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        String modelId = body.get("modelId");
        return Result.ok(writingService.expand(text, modelId));
    }

    @PostMapping("/shorten")
    public Result<String> shorten(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        String modelId = body.get("modelId");
        return Result.ok(writingService.shorten(text, modelId));
    }
}
