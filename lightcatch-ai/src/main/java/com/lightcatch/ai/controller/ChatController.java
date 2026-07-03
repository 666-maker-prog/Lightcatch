package com.lightcatch.ai.controller;

import com.lightcatch.ai.entity.AiChatMessage;
import com.lightcatch.ai.entity.AiConversation;
import com.lightcatch.ai.service.chat.IChatService;
import com.lightcatch.common.annotation.IgnoreAuth;
import com.lightcatch.common.model.Result;
import com.lightcatch.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/chat")
public class ChatController {

    @Autowired
    private IChatService chatService;
    @Autowired
    private com.lightcatch.ai.mapper.AiConversationMapper conversationMapper;

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("X-Access-Token");
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        return token;
    }

    @GetMapping("/stream")
    public SseEmitter stream(
            @RequestParam String message,
            @RequestParam(required = false) String conversationId,
            @RequestParam(required = false) String modelId,
            @RequestParam(required = false) String knowledgeId,
            HttpServletRequest request) {
        String token = extractToken(request);
        String userId = JwtUtil.getUserId(token);
        String tenantId = "0";
        return chatService.streamChat(conversationId, message, modelId, knowledgeId, userId, tenantId);
    }

    @PostMapping("/stream")
    public SseEmitter streamPost(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String message = body.get("message");
        String conversationId = body.get("conversationId");
        String modelId = body.get("modelId");
        String knowledgeId = body.get("knowledgeId");
        String token = request.getHeader("X-Access-Token");
        String userId = JwtUtil.getUserId(token);
        String tenantId = "0";
        try {
            tenantId = JwtUtil.parseToken(token).get("tenantId", String.class);
        } catch (Exception ignored) {}
        if (tenantId == null) tenantId = "0";
        return chatService.streamChat(conversationId, message, modelId, knowledgeId, userId, tenantId);
    }

    @GetMapping("/receive/{requestId}")
    public SseEmitter receive(@PathVariable String requestId) {
        return chatService.receiveByRequestId(requestId);
    }

    @GetMapping("/stop/{requestId}")
    public Result<?> stop(@PathVariable String requestId) {
        chatService.stopRequest(requestId);
        return Result.ok("已停止");
    }

    @GetMapping("/conversations")
    public Result<List<AiConversation>> getConversations(HttpServletRequest request) {
        String token = request.getHeader("X-Access-Token");
        String userId = JwtUtil.getUserId(token);
        return Result.ok(chatService.getConversations(userId));
    }

    @GetMapping("/conversation/{id}/messages")
    public Result<List<AiChatMessage>> getMessages(@PathVariable String id) {
        return Result.ok(chatService.getMessages(id));
    }

    @PutMapping("/conversation/{id}/update/title")
    public Result<?> updateTitle(@PathVariable String id, @RequestBody Map<String, String> body) {
        String title = body.get("title");
        if (title == null || title.isEmpty()) return Result.error("标题不能为空");
        AiConversation conv = new AiConversation();
        conv.setId(id);
        conv.setTitle(title);
        conv.setUpdateTime(new Date());
        conversationMapper.updateById(conv);
        return Result.ok();
    }

    @DeleteMapping("/conversation/{id}")
    public Result<?> deleteConversation(@PathVariable String id) {
        chatService.deleteConversation(id);
        return Result.ok();
    }
}
