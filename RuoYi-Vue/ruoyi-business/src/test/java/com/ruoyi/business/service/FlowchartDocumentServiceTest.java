package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;

class FlowchartDocumentServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FlowchartDocumentService service = new FlowchartDocumentService(objectMapper);

    @Test
    void shouldPreserveEdgeGeometryAndRejectInvalidPoints() throws Exception {
        String source = "{\"nodes\":[{\"id\":\"a\",\"type\":\"process\",\"x\":100,\"y\":100}],\"edges\":[{"
                + "\"id\":\"e\",\"sourceNodeId\":\"a\",\"targetNodeId\":\"a\",\"sourceAnchorId\":\"a_right\","
                + "\"startPoint\":{\"x\":174,\"y\":100},\"endPoint\":{\"x\":100,\"y\":68},"
                + "\"pointsList\":[{\"x\":174,\"y\":100},{\"x\":250,\"y\":100},{\"x\":250,\"y\":20},{\"x\":100,\"y\":68}],"
                + "\"text\":{\"value\":\"循环\",\"x\":250,\"y\":50}}]}";
        String saved = service.normalizeDocument(source);
        JsonNode edge = objectMapper.readTree(saved).path("edges").get(0);
        assertEquals(4, edge.path("pointsList").size());
        assertEquals(174, edge.path("startPoint").path("x").asInt());
        assertEquals("a_right", edge.path("sourceAnchorId").asText());
        assertEquals("循环", edge.path("text").asText());
        assertEquals(250, edge.path("textPosition").path("x").asInt());
        assertEquals(saved, service.normalizeDocument(saved));
        assertThrows(ServiceException.class, () -> service.normalizeDocument(source.replace("250", "10001")));
    }

    @Test
    void shouldKeepOnlyCanonicalFieldsAndElementLocks() throws Exception {
        String source = "{\"schemaVersion\":\"future\",\"nodes\":[{\"id\":\"n1\",\"type\":\"terminal\","
                + "\"x\":100,\"y\":80,\"text\":\" 开始 \" ,\"evil\":\"drop\"," 
                + "\"properties\":{\"locked\":true,\"textEditable\":false,\"url\":\"https://bad\"}}],"
                + "\"edges\":[]}";

        JsonNode normalized = objectMapper.readTree(service.normalizeDocument(source));

        assertEquals("1.0", normalized.path("schemaVersion").asText());
        assertEquals("开始", normalized.path("nodes").get(0).path("text").asText());
        assertTrue(normalized.path("nodes").get(0).path("properties").path("locked").asBoolean());
        assertFalse(normalized.path("nodes").get(0).has("evil"));
        assertFalse(normalized.path("nodes").get(0).path("properties").has("url"));
    }

    @Test
    void shouldRoundTripOptionalNodeSizeAndRejectOversize() throws Exception {
        String source = "{\"nodes\":[{\"id\":\"p\",\"type\":\"inputOutput\",\"x\":100,\"y\":100,"
                + "\"properties\":{\"width\":320,\"height\":110}}],\"edges\":[]}";
        String normalized = service.normalizeDocument(source);
        assertEquals(normalized, service.normalizeDocument(normalized));
        assertEquals(320, objectMapper.readTree(normalized).path("nodes").get(0).path("properties").path("width").asInt());
        assertThrows(ServiceException.class, () -> service.normalizeDocument(source.replace("320", "5000")));
    }

    @Test
    void shouldRejectHtmlAndDanglingEdges() {
        ServiceException html = assertThrows(ServiceException.class, () -> service.normalizeDocument(
                "{\"nodes\":[{\"id\":\"n1\",\"type\":\"process\",\"x\":1,\"y\":1,\"text\":\"<script>\"}],\"edges\":[]}", "标准答案"));
        assertTrue(html.getMessage().contains("标准答案第 1 个节点"));
        assertTrue(html.getMessage().contains("HTML 标签"));
        assertThrows(ServiceException.class, () -> service.normalizeDocument(
                "{\"nodes\":[{\"id\":\"n1\",\"type\":\"process\",\"x\":1,\"y\":1,\"text\":\"<!-- comment -->\"}],\"edges\":[]}"));
        assertThrows(ServiceException.class, () -> service.normalizeDocument(
                "{\"nodes\":[{\"id\":\"n1\",\"type\":\"process\",\"x\":1,\"y\":1,\"text\":\"javascript:alert(1)\"}],\"edges\":[]}"));
        assertThrows(ServiceException.class, () -> service.normalizeDocument(
                "{\"nodes\":[],\"edges\":[{\"id\":\"e1\",\"sourceNodeId\":\"n1\",\"targetNodeId\":\"n2\"}]}"));
    }

    @Test
    void shouldAllowComparisonOperatorsInText() throws Exception {
        String normalized = service.normalizeDocument(
                "{\"nodes\":[{\"id\":\"n1\",\"type\":\"decision\",\"x\":1,\"y\":1,\"text\":\"tu < 36?\"}],\"edges\":[]}");

        assertEquals("tu < 36?", objectMapper.readTree(normalized).path("nodes").get(0).path("text").asText());
    }

    @Test
    void shouldAllowCompactComparisonButRejectCompleteHtmlTag() throws Exception {
        String normalized = service.normalizeDocument(
                "{\"nodes\":[{\"id\":\"n1\",\"type\":\"decision\",\"x\":1,\"y\":1,\"text\":\"a<b && c>d\"}],\"edges\":[]}");

        assertEquals("a<b && c>d", objectMapper.readTree(normalized).path("nodes").get(0).path("text").asText());
        assertThrows(ServiceException.class, () -> service.normalizeDocument(
                "{\"nodes\":[{\"id\":\"n1\",\"type\":\"decision\",\"x\":1,\"y\":1,\"text\":\"<b>文字</b>\"}],\"edges\":[]}"));
    }

    @Test
    void shouldNormalizeChinesePunctuationAndWidth() {
        assertEquals("开始流程", service.normalizeText("  开始， 流程！"));
        assertEquals("abc123", service.normalizeText("ＡＢＣ-１２３"));
    }
}
