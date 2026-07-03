package com.lightcatch.ai.rag;

import org.springframework.stereotype.Component;

@Component
public class PromptTemplate {

    public String buildRagPrompt(String userMessage, String context, String systemPrompt) {
        StringBuilder sb = new StringBuilder();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            sb.append(systemPrompt).append("\n\n");
        }
        sb.append("你是一个专业的 AI 写作助手。请根据以下参考资料来回答用户的问题。\n");
        sb.append("如果参考资料中有相关内容，请基于资料回答；如果资料中没有，请如实说不知道。\n");
        sb.append("回答时请保持与参考资料一致的风格和语气。\n\n");
        sb.append("=== 参考资料 ===\n");
        sb.append(context).append("\n\n");
        sb.append("=== 用户问题 ===\n");
        sb.append(userMessage);
        return sb.toString();
    }

    public String buildWriterPrompt(String topic, String style, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业的自媒体内容创作者。\n");
        sb.append("请根据以下要求生成内容。\n\n");
        if (context != null && !context.isEmpty()) {
            sb.append("=== 风格参考 ===\n");
            sb.append("请参考以下内容的风格、语气和表达方式：\n");
            sb.append(context).append("\n\n");
        }
        sb.append("=== 写作要求 ===\n");
        sb.append("风格：").append(style != null ? style : "通用").append("\n");
        sb.append("主题：").append(topic).append("\n");
        sb.append("要求：语言自然流畅，段落分明，适合自媒体平台阅读。\n");
        return sb.toString();
    }
}
