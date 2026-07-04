package com.lightcatch.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.lightcatch.ai.config.ModelFactory;
import com.lightcatch.workflow.entity.AiFlow;
import com.lightcatch.workflow.mapper.AiFlowMapper;
import com.lightcatch.workflow.service.IFlowService;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class FlowServiceImpl extends ServiceImpl<AiFlowMapper, AiFlow> implements IFlowService {

    @Autowired
    private FlowExecutor flowExecutor;
    @Autowired
    private ModelFactory modelFactory;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private com.lightcatch.workflow.node.LLMNodeComponent llmNode;
    @Autowired
    private com.lightcatch.workflow.node.KnowledgeNodeComponent knowledgeNode;
    @Autowired
    private com.lightcatch.workflow.mapper.AiFlowOutputMapper flowOutputMapper;

    @Override
    public String executeFlow(String flowId, String input) throws Exception {
        AiFlow flow = getById(flowId);
        if (flow == null) throw new Exception("流程不存在: " + flowId);
        log.info("Executing flow: {} with input: {}", flow.getName(), input);

        // 解析 design JSON 获取节点列表
        String designStr = flow.getDesign();
        if (designStr == null || designStr.isEmpty()) {
            throw new Exception("流程「" + flow.getName() + "」缺少设计数据");
        }

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> design = mapper.readValue(designStr, java.util.Map.class);
        @SuppressWarnings("unchecked")
        java.util.List<java.util.Map<String, Object>> nodes = (java.util.List<java.util.Map<String, Object>>) design.getOrDefault("nodes", new java.util.ArrayList<>());

        if (nodes.isEmpty()) {
            throw new Exception("流程「" + flow.getName() + "」未配置任何节点");
        }

        // 按顺序执行每个节点（跳过 trigger/manual 等逻辑节点）
        String currentInput = input;
        for (java.util.Map<String, Object> node : nodes) {
            String type = (String) node.get("type");
            log.info("Executing node: type={}, name={}", type, node.get("name"));

            switch (type) {
                case "knowledge":
                    currentInput = knowledgeNode.execute(currentInput);
                    break;
                case "llm":
                    currentInput = llmNode.execute(currentInput);
                    break;
                case "trigger":
                case "manual":
                case "image":
                case "web_search":
                case "condition":
                default:
                    log.warn("Node type '{}' not supported yet, skipping", type);
                    break;
            }
        }

        // 保存执行结果到草稿箱
        try {
            com.lightcatch.workflow.entity.AiFlowOutput output = new com.lightcatch.workflow.entity.AiFlowOutput();
            output.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
            output.setFlowId(flowId);
            output.setUserId(input); // placeholder, will be overridden if caller sets userId
            output.setTitle("来自「" + flow.getName() + "」的创作");
            output.setContent(currentInput);
            output.setMedia("[]");
            output.setPlatforms("[]");
            output.setStatus(0);
            flowOutputMapper.insert(output);
            log.info("Saved output: {} for flow: {}", output.getId(), flowId);
        } catch (Exception e) {
            log.warn("Failed to save output: {}", e.getMessage());
        }

        return "流程执行完成";
    }

    @Override
    public AiFlow generateFromText(String description) throws Exception {
        ChatModel llm = modelFactory.createChatModel((String) null);

        String systemPrompt = "你是一个工作流解析器。将用户的创作需求解析为结构化的工作流步骤。\n\n"
            + "可用节点类型：\n"
            + "- trigger: 定时触发（用户提到每天、定时、自动时使用）\n"
            + "- manual: 手动触发（用户没说定时，或说手动运行、点击触发时使用）\n"
            + "- knowledge: 从素材库检索内容\n"
            + "- llm: 调用大模型生成文案、分析、总结\n"
            + "- image: 生成图片（用户提到配图、生成图片时使用）\n"
            + "- web_search: 联网搜索信息\n"
            + "- condition: 条件判断（根据结果决定走哪条路）\n\n"
            + "输出纯 JSON（不要加任何 markdown 格式）：\n"
            + "{\n"
            + "  \"name\": \"简短的工作流名称\",\n"
            + "  \"nodes\": [\n"
            + "    {\"id\":1,\"type\":\"节点类型\",\"name\":\"步骤名\",\"depends_on\":[],\"parallel\":false}\n"
            + "  ]\n"
            + "}\n\n"
            + "规则：\n"
            + "1. depends_on 表示依赖的上一步 id，空数组表示不依赖\n"
            + "2. 如果两个步骤互不依赖，后面那个 steps.parallel 设为 true\n"
            + "3. 遇到不支持的步骤直接跳过加注释即可";

        String prompt = systemPrompt + "\n用户描述：\n" + description;
        String response = llm.chat(prompt);
        log.info("LLM parse result: {}", response);

        // 提取 JSON
        String jsonStr = response;
        if (jsonStr.contains("```json")) {
            jsonStr = jsonStr.substring(jsonStr.indexOf("```json") + 7, jsonStr.lastIndexOf("```"));
        } else if (jsonStr.contains("```")) {
            jsonStr = jsonStr.substring(jsonStr.indexOf("```") + 3, jsonStr.lastIndexOf("```"));
        }
        jsonStr = jsonStr.trim();

        @SuppressWarnings("unchecked")
        Map<String, Object> parsed = mapper.readValue(jsonStr, Map.class);
        String name = (String) parsed.getOrDefault("name", "未命名工作流");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) parsed.getOrDefault("nodes", new ArrayList<>());

        // 生成可读的步骤链
        StringBuilder chain = new StringBuilder();
        for (Map<String, Object> node : nodes) {
            if (chain.length() > 0) chain.append(" -> ");
            String type = (String) node.getOrDefault("type", "llm");
            String icon = switch (type) {
                case "trigger" -> "⏰";
                case "manual" -> "▶️";
                case "knowledge" -> "📚";
                case "llm" -> "🤖";
                case "image" -> "🎨";
                case "web_search" -> "🌐";
                case "condition" -> "🔀";
                default -> "📦";
            };
            chain.append(icon).append(" ").append(node.getOrDefault("name", type));
        }

        AiFlow flow = new AiFlow();
        flow.setId(UUID.randomUUID().toString().replace("-", ""));
        flow.setName(name);
        flow.setDescription(description);
        flow.setChain(chain.toString());
        flow.setDesign(mapper.writeValueAsString(parsed));
        flow.setType("generated");
        flow.setStatus(1);
        save(flow);

        log.info("Generated workflow: {} with {} steps", flow.getName(), nodes.size());
        return flow;
    }
}
