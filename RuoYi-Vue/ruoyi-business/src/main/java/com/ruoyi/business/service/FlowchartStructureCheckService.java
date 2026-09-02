package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.common.exception.ServiceException;

/** 按图结构生成可解释的检查结果，坐标和颜色不参与评分。 */
@Service
public class FlowchartStructureCheckService {
    private final ObjectMapper objectMapper;
    private final FlowchartDocumentService documentService;

    public FlowchartStructureCheckService(ObjectMapper objectMapper, FlowchartDocumentService documentService) {
        this.objectMapper = objectMapper;
        this.documentService = documentService;
    }

    public String generateRules(String answerJson) {
        JsonNode answer = documentService.readDocument(answerJson);
        List<ObjectNode> rules = new ArrayList<ObjectNode>();
        for (JsonNode node : answer.path("nodes")) {
            ObjectNode rule = objectMapper.createObjectNode();
            rule.put("id", "node:" + node.path("id").asText());
            rule.put("kind", "NODE");
            rule.put("expectedNodeId", node.path("id").asText());
            rule.put("nodeType", node.path("type").asText());
            rule.put("expectedText", node.path("text").asText(""));
            rule.putArray("aliases");
            rule.put("enabled", true);
            rule.put("scoring", true);
            rules.add(rule);
        }
        for (JsonNode edge : answer.path("edges")) {
            ObjectNode rule = objectMapper.createObjectNode();
            rule.put("id", "edge:" + edge.path("id").asText());
            rule.put("kind", "EDGE");
            rule.put("sourceExpectedNodeId", edge.path("sourceNodeId").asText());
            rule.put("targetExpectedNodeId", edge.path("targetNodeId").asText());
            rule.put("expectedText", edge.path("text").asText(""));
            rule.putArray("aliases");
            rule.put("enabled", true);
            rule.put("scoring", true);
            rules.add(rule);
        }
        int count = rules.size();
        int assigned = 0;
        ArrayNode result = objectMapper.createArrayNode();
        for (int i = 0; i < count; i++) {
            int weight = count == 0 ? 0 : (i == count - 1 ? 100 - assigned : 100 / count);
            assigned += weight;
            rules.get(i).put("weight", weight);
            result.add(rules.get(i));
        }
        try { return objectMapper.writeValueAsString(result); }
        catch (Exception e) { throw new ServiceException("生成结构检查规则失败"); }
    }

    public CheckOutcome check(String answerJson, String studentJson, String rulesJson, int maxScore) {
        JsonNode answer = documentService.readDocument(answerJson);
        JsonNode student = documentService.readDocument(studentJson);
        ArrayNode rules = readRules(rulesJson == null || rulesJson.trim().isEmpty() ? generateRules(answerJson) : rulesJson);

        Map<String, JsonNode> answerNodes = nodesById(answer.path("nodes"));
        validateRules(rules, answerNodes);
        Map<String, JsonNode> studentNodes = nodesById(student.path("nodes"));
        Map<String, String> matched = new HashMap<String, String>();
        Set<String> usedStudentIds = new HashSet<String>();
        ArrayNode itemResults = objectMapper.createArrayNode();
        BigDecimal earned = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (JsonNode rule : rules) {
            if (!rule.path("enabled").asBoolean(true) || !"NODE".equals(rule.path("kind").asText())) continue;
            String expectedId = rule.path("expectedNodeId").asText();
            String studentId = matchNode(rule, studentNodes, usedStudentIds);
            boolean pass = studentId != null;
            if (pass) { matched.put(expectedId, studentId); usedStudentIds.add(studentId); }
            BigDecimal weight = weight(rule);
            if (rule.path("scoring").asBoolean(true)) {
                total = total.add(weight);
                if (pass) earned = earned.add(weight);
            }
            itemResults.add(resultItem(rule, pass ? "PASS" : "MISSING",
                    pass ? "节点类型和文字符合要求" : "缺少必需节点或节点文字不符合要求",
                    pass ? studentId : null));
        }

        for (JsonNode rule : rules) {
            if (!rule.path("enabled").asBoolean(true) || !"EDGE".equals(rule.path("kind").asText())) continue;
            String source = matched.get(rule.path("sourceExpectedNodeId").asText());
            String target = matched.get(rule.path("targetExpectedNodeId").asText());
            String edgeId = source == null || target == null ? null
                    : matchEdge(rule, student.path("edges"), source, target);
            boolean pass = edgeId != null;
            BigDecimal weight = weight(rule);
            if (rule.path("scoring").asBoolean(true)) {
                total = total.add(weight);
                if (pass) earned = earned.add(weight);
            }
            itemResults.add(resultItem(rule, pass ? "PASS" : "WRONG",
                    pass ? "连接方向和分支文字符合要求" : "缺少连接、方向错误或分支文字不符合要求",
                    edgeId));
        }

        BigDecimal ratio = total.signum() == 0 ? BigDecimal.ZERO
                : earned.divide(total, 6, RoundingMode.HALF_UP);
        BigDecimal suggested = ratio.multiply(BigDecimal.valueOf(maxScore)).setScale(2, RoundingMode.HALF_UP);
        ObjectNode output = objectMapper.createObjectNode();
        output.put("schemaVersion", "1.0");
        output.set("items", itemResults);
        output.put("earnedWeight", earned);
        output.put("totalWeight", total);
        output.put("scoreRatio", ratio);
        output.put("suggestedScore", suggested);
        try { return new CheckOutcome(objectMapper.writeValueAsString(output), suggested); }
        catch (Exception e) { throw new ServiceException("结构检查结果保存失败"); }
    }

    private ArrayNode readRules(String json) {
        try {
            if (json == null || json.getBytes(StandardCharsets.UTF_8).length > 512 * 1024) {
                throw new ServiceException("结构检查规则不能超过 512KB");
            }
            JsonNode node = objectMapper.readTree(json);
            if (node == null || !node.isArray() || node.size() > 600) throw new ServiceException("结构检查规则格式不正确");
            return (ArrayNode) node;
        } catch (ServiceException e) { throw e; }
        catch (Exception e) { throw new ServiceException("结构检查规则 JSON 无法解析"); }
    }

    private Map<String, JsonNode> nodesById(JsonNode nodes) {
        Map<String, JsonNode> result = new HashMap<String, JsonNode>();
        for (JsonNode node : nodes) result.put(node.path("id").asText(), node);
        return result;
    }

    /**
     * 规则会进入课程快照，因此必须在题库保存阶段拒绝悬空引用和被篡改的节点类型。
     */
    private void validateRules(ArrayNode rules, Map<String, JsonNode> answerNodes) {
        if (rules.isEmpty()) throw new ServiceException("结构检查规则不能为空");
        Set<String> ruleIds = new HashSet<String>();
        for (JsonNode rule : rules) {
            String id = rule.path("id").asText().trim();
            String kind = rule.path("kind").asText();
            if (id.isEmpty() || id.length() > 128 || !ruleIds.add(id)) {
                throw new ServiceException("结构检查规则编号不能为空、重复或过长");
            }
            if (!rule.path("weight").isNumber() || rule.path("weight").decimalValue().signum() < 0
                    || rule.path("weight").decimalValue().compareTo(BigDecimal.valueOf(1000)) > 0) {
                throw new ServiceException("结构检查规则权重必须在 0 到 1000 之间");
            }
            if (!rule.path("aliases").isArray() || rule.path("aliases").size() > 20) {
                throw new ServiceException("结构检查规则别名格式不正确");
            }
            if (rule.path("expectedText").asText("").length() > 200) {
                throw new ServiceException("结构检查规则文字不能超过 200 个字符");
            }
            for (JsonNode alias : rule.path("aliases")) {
                if (!alias.isTextual() || alias.asText().length() > 200) {
                    throw new ServiceException("结构检查规则别名不能超过 200 个字符");
                }
            }
            if ("NODE".equals(kind)) {
                String expectedId = rule.path("expectedNodeId").asText();
                JsonNode expected = answerNodes.get(expectedId);
                if (expected == null) throw new ServiceException("节点检查规则引用了不存在的标准答案节点");
                if (!expected.path("type").asText().equals(rule.path("nodeType").asText())) {
                    throw new ServiceException("节点检查规则类型与标准答案不一致");
                }
            } else if ("EDGE".equals(kind)) {
                if (!answerNodes.containsKey(rule.path("sourceExpectedNodeId").asText())
                        || !answerNodes.containsKey(rule.path("targetExpectedNodeId").asText())) {
                    throw new ServiceException("连线检查规则引用了不存在的标准答案节点");
                }
            } else {
                throw new ServiceException("结构检查规则类型只能是 NODE 或 EDGE");
            }
        }
    }

    private String matchNode(JsonNode rule, Map<String, JsonNode> students, Set<String> used) {
        String expectedId = rule.path("expectedNodeId").asText();
        JsonNode sameId = students.get(expectedId);
        if (sameId != null && matchesNode(rule, sameId)) return expectedId;
        for (Map.Entry<String, JsonNode> entry : students.entrySet()) {
            if (!used.contains(entry.getKey()) && matchesNode(rule, entry.getValue())) return entry.getKey();
        }
        return null;
    }

    private boolean matchesNode(JsonNode rule, JsonNode node) {
        if (!rule.path("nodeType").asText().equals(node.path("type").asText())) return false;
        return matchesText(rule, node.path("text").asText(""));
    }

    private String matchEdge(JsonNode rule, JsonNode edges, String source, String target) {
        for (JsonNode edge : edges) {
            if (source.equals(edge.path("sourceNodeId").asText())
                    && target.equals(edge.path("targetNodeId").asText())
                    && matchesText(rule, edge.path("text").asText(""))) return edge.path("id").asText();
        }
        return null;
    }

    private boolean matchesText(JsonNode rule, String actual) {
        String normalized = documentService.normalizeText(actual);
        if (normalized.equals(documentService.normalizeText(rule.path("expectedText").asText("")))) return true;
        for (JsonNode alias : rule.path("aliases")) {
            if (normalized.equals(documentService.normalizeText(alias.asText("")))) return true;
        }
        return false;
    }

    private BigDecimal weight(JsonNode rule) {
        BigDecimal value = rule.path("weight").decimalValue();
        return value.signum() < 0 ? BigDecimal.ZERO : value;
    }

    private ObjectNode resultItem(JsonNode rule, String status, String message, String actualId) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("ruleId", rule.path("id").asText());
        result.put("kind", rule.path("kind").asText());
        result.put("status", status);
        result.put("message", message);
        result.put("weight", weight(rule));
        result.put("scoring", rule.path("scoring").asBoolean(true));
        if (actualId != null) result.put("actualId", actualId);
        return result;
    }

    public static class CheckOutcome {
        private final String resultJson;
        private final BigDecimal suggestedScore;
        public CheckOutcome(String resultJson, BigDecimal suggestedScore) {
            this.resultJson = resultJson;
            this.suggestedScore = suggestedScore;
        }
        public String getResultJson() { return resultJson; }
        public BigDecimal getSuggestedScore() { return suggestedScore; }
    }
}
