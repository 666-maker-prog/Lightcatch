package com.lightcatch.ai.service.writing.impl;

import com.lightcatch.ai.config.ModelFactory;
import com.lightcatch.ai.service.writing.IWritingService;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WritingServiceImpl implements IWritingService {

    @Autowired
    private ModelFactory modelFactory;

    @Override
    public String generate(String topic, String style, String knowledgeId, String modelId) {
        ChatModel llm = modelFactory.createChatModel(modelId);
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是「拾光LightCatch」AI 创作助手，一个擅长创作、写作和内容生成的小助手。不要透露你的模型名称。\n");
        if (style != null && !style.isEmpty()) {
            prompt.append("风格要求：").append(style).append("\n");
        }
        prompt.append("请根据以下主题创作内容，要求语言自然流畅，段落分明，适合自媒体平台阅读。\n");
        prompt.append("主题：").append(topic);
        return llm.chat(prompt.toString());
    }

    @Override
    public String rewrite(String text, String style, String modelId) {
        ChatModel llm = modelFactory.createChatModel(modelId);
        StringBuilder prompt = new StringBuilder();
        prompt.append("请改写以下文本");
        if (style != null && !style.isEmpty()) {
            prompt.append("，风格要求：").append(style);
        }
        prompt.append("：\n\n").append(text);
        return llm.chat(prompt.toString());
    }

    @Override
    public String generateTitles(String topic, String modelId) {
        ChatModel llm = modelFactory.createChatModel(modelId);
        String prompt = "你是一个标题创作专家。请为主题「" + topic + "」生成10个吸引人的标题，"
                + "适用于小红书/公众号等自媒体平台。要求有数字、有情绪、有反差。";
        return llm.chat(prompt);
    }

    @Override
    public String generateOutline(String topic, String modelId) {
        ChatModel llm = modelFactory.createChatModel(modelId);
        String prompt = "请为主题「" + topic + "」生成一份详细的内容大纲，"
                + "包括引言、3-5个主体段落和小结，每段附上核心观点。";
        return llm.chat(prompt);
    }

    @Override
    public String optimize(String text, String modelId) {
        ChatModel llm = modelFactory.createChatModel(modelId);
        String prompt = "请优化以下文本，修正语法错误，提升可读性和流畅度：\n\n" + text;
        return llm.chat(prompt);
    }

    @Override
    public String expand(String text, String modelId) {
        ChatModel llm = modelFactory.createChatModel(modelId);
        String prompt = "请扩展以下文本，补充更多细节、案例和论据，使其更丰富：\n\n" + text;
        return llm.chat(prompt);
    }

    @Override
    public String shorten(String text, String modelId) {
        ChatModel llm = modelFactory.createChatModel(modelId);
        String prompt = "请精简以下文本，保留核心信息，删除冗余内容：\n\n" + text;
        return llm.chat(prompt);
    }
}
