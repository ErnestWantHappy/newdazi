package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import java.util.Collections;
import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;
import com.ruoyi.common.exception.ServiceException;

class QwenPracticalVisionGradingProviderTest
{
    private final QwenPracticalVisionGradingProvider provider = new QwenPracticalVisionGradingProvider();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void threeProvidersUseVisionJsonPayloadWithoutLeakingQwenOptions() throws Exception
    {
        File image = File.createTempFile("anonymous-vision-", ".jpg");
        try
        {
            Files.write(image.toPath(), new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xd9});
            for (String[] selected : new String[][] {
                    {"QWEN", "qwen3.8-max"}, {"DEEPSEEK", "deepseek-flash"},
                    {"DOUBAO", "doubao-seed-2.1-pro"}})
            {
                TeacherAiConfig config = new TeacherAiConfig();
                config.setProviderCode(selected[0]);
                config.setModelName(selected[1]);
                config.setEndpointUrl(PracticalVisionModelCatalog.endpoint(selected[0], selected[1]));
                ObjectNode body = provider.buildRequestBody(config, "匿名评分样本",
                        Collections.singletonList(image), Collections.singletonList("学生作品"), 256, "只返回 JSON");
                assertEquals(selected[1], body.path("model").asText());
                assertEquals("json_object", body.path("response_format").path("type").asText());
                assertEquals(true, body.path("messages").get(1).path("content").get(2)
                        .path("image_url").path("url").asText().startsWith("data:image/jpeg;base64,"));
                assertEquals("QWEN".equals(selected[0]), body.has("enable_thinking"));
            }
        }
        finally { Files.deleteIfExists(image.toPath()); }
    }

    @Test
    void paperBlankAndUncertainCannotReceiveFullMarks() throws Exception
    {
        for (String status : Arrays.asList("blank", "uncertain")) {
            PracticalAiGradingInput input = paperInput(status);
            assertThrows(ServiceException.class, () -> provider.parse(paperResponse(10, 30), input));
        }
        assertThrows(ServiceException.class, () -> provider.parse(paperResponse(1, 30), paperInput("blank")));
    }

    @Test
    void paperPartialBlankPreservesEvidenceAndForcesReview() throws Exception
    {
        PracticalAiGradingInput input = paperInput("blank");
        ObjectNode recognition = (ObjectNode) mapper.readTree(input.getPaperRecognitionJson());
        ((ArrayNode) recognition.path("items").get(0).path("parts")).addObject()
            .put("label", "2").put("page", 1).put("status", "written").put("text", "A");
        input.setPaperRecognitionJson(recognition.toString());
        PracticalAiGradingOutput output = provider.parse(paperResponse(5, 30), input);
        assertEquals(35, output.getSuggestedScore());
        assertEquals(true, mapper.readTree(output.getEvidenceJson()).path("needsHumanReview").asBoolean());
        assertEquals(recognition, mapper.readTree(output.getEvidenceJson()).path("paperRecognition"));
        assertEquals("request-test", output.getRequestId());
        assertEquals(100, output.getPromptTokens());
    }

    @Test
    void paperRecognitionRejectsMissingItemsDuplicatePartsAndReferencePage() throws Exception
    {
        PracticalAiGradingInput input = paperInput("blank");
        for (String evidence : Arrays.asList("{\"items\":[]}", input.getPaperRecognitionJson().replace("\"page\":1", "\"page\":2"),
                input.getPaperRecognitionJson().replace("\"text\":\"\"", "\"text\":\"invented\""))) {
            input.setPaperRecognitionJson(evidence);
            final PracticalAiGradingInput invalid = input;
            assertThrows(ServiceException.class, () -> provider.validateRecognition(invalid));
        }
        input = paperInput("written");
        ObjectNode recognition = (ObjectNode) mapper.readTree(input.getPaperRecognitionJson());
        ArrayNode parts = (ArrayNode) recognition.path("items").get(0).path("parts");
        parts.add(parts.get(0).deepCopy());
        input.setPaperRecognitionJson(recognition.toString());
        final PracticalAiGradingInput duplicate = input;
        assertThrows(ServiceException.class, () -> provider.validateRecognition(duplicate));
    }

    private PracticalAiGradingInput paperInput(String status) throws Exception
    {
        PracticalAiGradingInput input = input(); input.setPaperSubmission(true); input.setStudentPageCount(1);
        ObjectNode recognition = mapper.createObjectNode();
        ArrayNode items = recognition.putArray("items");
        for (long id : new long[] {1, 2}) {
            String state = id == 1 ? status : "written";
            items.addObject().put("rubricItemId", id).putArray("parts").addObject().put("label", "1")
                .put("page", 1).put("status", state).put("text", "blank".equals(state) ? "" : "A");
        }
        input.setPaperRecognitionJson(recognition.toString()); return input;
    }

    private String paperResponse(int first, int second) throws Exception
    {
        ObjectNode root = (ObjectNode) mapper.readTree(response(first, second, first + second));
        ObjectNode content = (ObjectNode) mapper.readTree(root.path("choices").get(0).path("message").path("content").asText());
        content.put("needsHumanReview", false);
        for (com.fasterxml.jackson.databind.JsonNode item : content.path("rubricResults"))
            ((ObjectNode) item).putArray("evidence").addObject().put("page", 1).put("description", "逐空核对结果");
        ((ObjectNode) root.path("choices").get(0).path("message")).put("content", content.toString());
        return root.toString();
    }

    @Test
    void shouldAcceptCompleteBoundedJsonAndRecalculateTotal() throws Exception
    {
        PracticalAiGradingOutput output = provider.parse(response(8, 24, 32), input());
        assertEquals(32, output.getSuggestedScore());
        assertEquals(2, mapper.readTree(output.getScoringDetailsJson()).size());
    }

    @Test
    void shouldRejectOverLimitOrInconsistentScore()
    {
        assertThrows(ServiceException.class, () -> provider.parse(response(11, 24, 35), input()));
        assertThrows(ServiceException.class, () -> provider.parse(response(8, 24, 31), input()));
    }

    @Test
    void promptContainsOnlyRubricContractWithoutStudentIdentity() throws Exception
    {
        String prompt = provider.gradingPrompt(input());
        assertFalse(prompt.contains("studentName"));
        assertFalse(prompt.contains("studentNo"));
        assertEquals(true, prompt.contains("总分必须等于逐项分数之和"));
    }

    @Test
    void shouldAllowWholeQuestionSuggestionWhenNoChildRubricExists() throws Exception
    {
        PracticalAiGradingInput input = input();
        input.setScoringItems(Collections.emptyList());
        ObjectNode content = mapper.createObjectNode();
        content.putArray("rubricResults");
        content.put("totalScore", 28).put("maxScore", 40).put("confidence", 0.6D);
        ObjectNode root = mapper.createObjectNode();
        root.putArray("choices").addObject().putObject("message").put("content", mapper.writeValueAsString(content));
        assertEquals(28, provider.parse(mapper.writeValueAsString(root), input).getSuggestedScore());
    }

    @Test
    void shouldIgnoreInventedItemsWithoutRubricAndPreserveOverallComment() throws Exception
    {
        PracticalAiGradingInput input = input();
        input.setScoringItems(Collections.emptyList());
        PracticalAiGradingOutput output = provider.parse(response(99, -1, 28), input);
        assertEquals(28, output.getSuggestedScore());
        assertEquals("[]", output.getScoringDetailsJson());
        assertEquals("建议教师复核", mapper.readTree(output.getEvidenceJson()).path("overallComment").asText());
        assertEquals(0.8D, output.getConfidence().doubleValue());
        assertThrows(ServiceException.class, () -> provider.parse(response(8, 24, 41), input));
        assertThrows(ServiceException.class, () -> provider.parse(response(8, 24, -1), input));
        assertEquals(0, provider.parse(response(8, 24, 0), input).getSuggestedScore());
        assertEquals(40, provider.parse(response(8, 24, 40), input).getSuggestedScore());
    }

    @Test
    void shouldRejectFractionalMissingAndOverflowTotal() throws Exception
    {
        PracticalAiGradingInput input = input(); input.setScoringItems(Collections.emptyList());
        for (String total : Arrays.asList("28.5", "null", "2147483648", "\"28\"")) {
            ObjectNode root = (ObjectNode) mapper.readTree(response(8, 24, 28));
            ObjectNode content = (ObjectNode) mapper.readTree(root.path("choices").get(0).path("message").path("content").asText());
            content.set("totalScore", mapper.readTree(total));
            ((ObjectNode) root.path("choices").get(0).path("message")).put("content", mapper.writeValueAsString(content));
            assertThrows(ServiceException.class, () -> provider.parse(mapper.writeValueAsString(root), input));
        }
    }

    @Test
    void shouldRejectFractionalItemScoreInsteadOfTruncating()
    {
        // 3.5 + 24 = 27.5：旧 asInt 会静默截成 3 + 24 = 27 并通过整题校验；现在必须直接失败。
        assertThrows(ServiceException.class, () -> provider.parse(responseDecimal(3.5D, 24, 27), input()));
        assertThrows(ServiceException.class, () -> provider.parse(responseDecimal(8, 24.5D, 32), input()));
    }

    @Test
    void shouldMapProviderBillingKeyAndThrottleErrors()
    {
        assertEquals("模型厂商账户可能余额不足或已欠费，请核实后重试",
                provider.friendlyHttpError(error("Arrearage", "Account balance is insufficient")));
        assertEquals("AI Key 无效或已过期，请重新配置",
                provider.friendlyHttpError(error("InvalidApiKey.NotFound", "bad key")));
        assertEquals("请求过于频繁，已触发限流，请稍后重试",
                provider.friendlyHttpError(error("Throttling.RateQuota", "slow down")));
    }

    @Test
    void shouldKeepSafeServerMessageForUnknownErrors()
    {
        assertEquals("模型接口调用失败（HTTP 400）：模型参数无效",
                provider.friendlyHttpError(error("InvalidParameter", "模型参数无效")));
        assertEquals("模型接口调用失败（HTTP 400）：API Key: ***，Bearer ***",
                provider.friendlyHttpError(error("Unknown", "API Key: sk-secret123456，Bearer abc.def.ghi")));
    }

    private PracticalAiGradingInput input()
    {
        PracticalRubricSnapshot rubric = new PracticalRubricSnapshot();
        rubric.setQuestionContent("制作一张主题海报"); rubric.setQuestionScore(40);
        PracticalAiGradingInput input = new PracticalAiGradingInput();
        input.setRubric(rubric); input.setScoringItems(Arrays.asList(item(1L, "内容", 10), item(2L, "版式", 30)));
        return input;
    }

    private PracticalScoringItemVo item(Long id, String name, int max)
    {
        PracticalScoringItemVo item = new PracticalScoringItemVo();
        item.setItemId(id); item.setItemName(name); item.setMaxScore(max); return item;
    }

    private String response(int first, int second, int total) throws Exception
    {
        ObjectNode content = mapper.createObjectNode();
        ArrayNode results = content.putArray("rubricResults");
        results.addObject().put("rubricItemId", 1).put("score", first).put("maxScore", 10);
        results.addObject().put("rubricItemId", 2).put("score", second).put("maxScore", 30);
        content.put("totalScore", total).put("maxScore", 40).put("confidence", 0.8D)
                .put("overallComment", "建议教师复核").put("needsHumanReview", true);
        ObjectNode root = mapper.createObjectNode();
        root.put("id", "request-test");
        root.putArray("choices").addObject().putObject("message").put("content", mapper.writeValueAsString(content));
        root.putObject("usage").put("prompt_tokens", 100).put("completion_tokens", 50);
        return mapper.writeValueAsString(root);
    }
    private String responseDecimal(double first, double second, int total) throws Exception
    {
        ObjectNode content = mapper.createObjectNode();
        ArrayNode results = content.putArray("rubricResults");
        results.addObject().put("rubricItemId", 1).put("score", first).put("maxScore", 10);
        results.addObject().put("rubricItemId", 2).put("score", second).put("maxScore", 30);
        content.put("totalScore", total).put("maxScore", 40).put("confidence", 0.8D)
                .put("overallComment", "建议教师复核").put("needsHumanReview", true);
        ObjectNode root = mapper.createObjectNode();
        root.put("id", "request-test");
        root.putArray("choices").addObject().putObject("message").put("content", mapper.writeValueAsString(content));
        root.putObject("usage").put("prompt_tokens", 100).put("completion_tokens", 50);
        return mapper.writeValueAsString(root);
    }


    private HttpClientErrorException error(String code, String message)
    {
        String body = "{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}";
        return HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request",
                HttpHeaders.EMPTY, body.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                java.nio.charset.StandardCharsets.UTF_8);
    }
}
