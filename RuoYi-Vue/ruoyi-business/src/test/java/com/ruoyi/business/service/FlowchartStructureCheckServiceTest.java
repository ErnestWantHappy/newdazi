package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.common.exception.ServiceException;

class FlowchartStructureCheckServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FlowchartDocumentService documentService = new FlowchartDocumentService(objectMapper);
    private final FlowchartStructureCheckService service =
            new FlowchartStructureCheckService(objectMapper, documentService);

    @Test
    void shouldIgnoreLayoutAndAcceptTeacherAlias() throws Exception {
        String answer = document("开始", "处理数据", "是");
        JsonNode rules = objectMapper.readTree(service.generateRules(answer));
        ((ArrayNode) rules.get(1).path("aliases")).add("计算数据");
        String student = document("开始", "计算数据", "是").replace("\"x\":240", "\"x\":900");

        FlowchartStructureCheckService.CheckOutcome outcome = service.check(
                answer, student, objectMapper.writeValueAsString(rules), 20);
        JsonNode result = objectMapper.readTree(outcome.getResultJson());

        assertEquals(0, new BigDecimal("20.00").compareTo(outcome.getSuggestedScore()));
        assertEquals(3, result.path("items").size());
        assertTrue(result.path("items").findValuesAsText("status").stream().allMatch("PASS"::equals));
    }

    @Test
    void shouldExplainWrongDirectionAndDeductItsWeight() throws Exception {
        String answer = document("开始", "处理数据", "");
        String reversed = answer.replace("\"sourceNodeId\":\"n1\",\"targetNodeId\":\"n2\"",
                "\"sourceNodeId\":\"n2\",\"targetNodeId\":\"n1\"");

        FlowchartStructureCheckService.CheckOutcome outcome = service.check(
                answer, reversed, service.generateRules(answer), 30);
        JsonNode result = objectMapper.readTree(outcome.getResultJson());

        assertTrue(outcome.getSuggestedScore().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(outcome.getSuggestedScore().compareTo(new BigDecimal("30")) < 0);
        assertEquals("WRONG", result.path("items").get(2).path("status").asText());
    }

    @Test
    void shouldRejectRuleThatReferencesMissingAnswerNode() throws Exception {
        String answer = document("开始", "处理数据", "");
        JsonNode rules = objectMapper.readTree(service.generateRules(answer));
        ((ObjectNode) ((ArrayNode) rules).get(0)).put("expectedNodeId", "missing-node");

        assertThrows(ServiceException.class, () -> service.check(
                answer, answer, objectMapper.writeValueAsString(rules), 20));
    }

    private String document(String firstText, String secondText, String edgeText) {
        return "{\"schemaVersion\":\"1.0\",\"nodes\":["
                + "{\"id\":\"n1\",\"type\":\"terminal\",\"x\":100,\"y\":100,\"text\":\"" + firstText + "\"},"
                + "{\"id\":\"n2\",\"type\":\"process\",\"x\":240,\"y\":100,\"text\":\"" + secondText + "\"}],"
                + "\"edges\":[{\"id\":\"e1\",\"type\":\"polyline\",\"sourceNodeId\":\"n1\","
                + "\"targetNodeId\":\"n2\",\"text\":\"" + edgeText + "\"}]}";
    }
}
