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
    void shouldRejectHtmlAndDanglingEdges() {
        assertThrows(ServiceException.class, () -> service.normalizeDocument(
                "{\"nodes\":[{\"id\":\"n1\",\"type\":\"process\",\"x\":1,\"y\":1,\"text\":\"<script>\"}],\"edges\":[]}"));
        assertThrows(ServiceException.class, () -> service.normalizeDocument(
                "{\"nodes\":[],\"edges\":[{\"id\":\"e1\",\"sourceNodeId\":\"n1\",\"targetNodeId\":\"n2\"}]}"));
    }

    @Test
    void shouldNormalizeChinesePunctuationAndWidth() {
        assertEquals("开始流程", service.normalizeText("  开始， 流程！"));
        assertEquals("abc123", service.normalizeText("ＡＢＣ-１２３"));
    }
}
