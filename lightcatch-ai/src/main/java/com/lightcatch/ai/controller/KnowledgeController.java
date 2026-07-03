package com.lightcatch.ai.controller;

import com.lightcatch.ai.entity.AiKnowledge;
import com.lightcatch.ai.entity.AiKnowledgeDoc;
import com.lightcatch.ai.service.knowledge.IKnowledgeService;
import com.lightcatch.common.model.Result;
import com.lightcatch.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ai/knowledge")
public class KnowledgeController {

    @Autowired
    private IKnowledgeService knowledgeService;
    @Autowired
    private com.lightcatch.ai.service.knowledge.IEmbeddingService embeddingService;

    @GetMapping("/list")
    public Result<List<AiKnowledge>> list() {
        return Result.ok(knowledgeService.list());
    }

    @PostMapping("/create")
    public Result<?> create(@RequestBody AiKnowledge knowledge) {
        knowledge.setDocCount(0);
        knowledge.setStatus(1);
        knowledgeService.save(knowledge);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable String id, @RequestBody AiKnowledge knowledge) {
        knowledge.setId(id);
        knowledgeService.updateById(knowledge);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id) {
        knowledgeService.deleteKnowledge(id);
        return Result.ok();
    }

    @GetMapping("/{id}/docs")
    public Result<List<AiKnowledgeDoc>> getDocs(@PathVariable String id) {
        return Result.ok(knowledgeService.getDocuments(id));
    }

    @PostMapping("/{id}/upload")
    public Result<?> upload(@PathVariable String id,
                             @RequestParam("file") MultipartFile file,
                             HttpServletRequest request) {
        String token = request.getHeader("X-Access-Token");
        String userId = JwtUtil.getUserId(token);
        knowledgeService.uploadDocument(id, file, userId);
        return Result.ok("上传成功，正在处理中");
    }

    @DeleteMapping("/doc/{docId}")
    public Result<?> deleteDoc(@PathVariable String docId) {
        knowledgeService.deleteDocument(docId);
        return Result.ok();
    }

    @PostMapping("/doc/{docId}/re-embed")
    public Result<?> reEmbed(@PathVariable String docId,
                              @RequestParam(required = false) String knowledgeId) {
        if (knowledgeId == null) {
            com.lightcatch.ai.entity.AiKnowledgeDoc doc = knowledgeService.getDocument(docId);
            if (doc == null) return Result.error("文档不存在");
            knowledgeId = doc.getKnowledgeId();
        }
        embeddingService.embedDocument(knowledgeId, docId);
        return Result.ok("重新向量化完成");
    }

    @PostMapping("/{id}/search")
    public Result<List<String>> search(@PathVariable String id,
                                        @RequestParam String query) {
        // Test search - returns matching chunk content
        return Result.ok(List.of("搜索结果功能需要先配置向量模型"));
    }
}
