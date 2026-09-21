package com.ruoyi.business.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.business.domain.BizScoringItem;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.TeacherAiConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 显式启用的真实样卷回归，只调用模型并写本地证据，不连接业务写接口。 */
@EnabledIfEnvironmentVariable(named = "PAPER_LIVE_FIXTURE", matches = ".+")
class PaperGradingLiveTest
{
    @Test
    void fullPaperMustPreserveBlankAndRejectFullMarks() throws Exception
    {
        // 禁止 HTTP DEBUG 把 base64 图片及业务原文写入构建日志。
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME))
            .setLevel(ch.qos.logback.classic.Level.WARN);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode fixture = mapper.readTree(new File(System.getenv("PAPER_LIVE_FIXTURE")));
        for (String model : Arrays.asList("qwen3.7-plus", "qwen3.8-max"))
        {
            PracticalAiGradingInput input = new PracticalAiGradingInput();
            PracticalRubricSnapshot rubric = new PracticalRubricSnapshot();
            rubric.setQuestionContent(fixture.path("questionContent").asText());
            rubric.setQuestionScore(fixture.path("questionScore").asInt());
            input.setRubric(rubric);
            input.setScoringItems(new PracticalScoringPolicyService().buildScoringItems(
                mapper.convertValue(fixture.path("scoringItems"), new TypeReference<java.util.List<BizScoringItem>>() {}), rubric.getQuestionScore()));
            for (JsonNode page : fixture.path("pages")) {
                input.getPageImages().add(new File(page.path("file").asText()));
                input.getPageLabels().add(page.path("label").asText());
            }
            input.setPaperSubmission(true); input.setStudentPageCount(fixture.path("studentPageCount").asInt());
            TeacherAiConfig config = new TeacherAiConfig();
            config.setModelName(model); config.setEndpointUrl(TeacherAiConfigService.DEFAULT_ENDPOINT);
            PracticalAiGradingOutput output = new QwenPracticalVisionGradingProvider().grade(config, System.getenv("PAPER_LIVE_API_KEY"), input);
            ObjectNode evidence = (ObjectNode) mapper.readTree(output.getEvidenceJson());
            evidence.put("model", model).put("promptTokens", output.getPromptTokens()).put("completionTokens", output.getCompletionTokens());
            evidence.put("requestId", output.getRequestId());
            File destination = new File(fixture.path("outputDirectory").asText(), "full-paper-" + model + ".json");
            Files.write(destination.toPath(), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(evidence).getBytes(StandardCharsets.UTF_8));
            JsonNode q8 = null, recognition = null;
            for (JsonNode item : evidence.path("rubricResults")) if (item.path("rubricItemId").asLong() == 289) q8 = item;
            for (JsonNode item : evidence.path("paperRecognition").path("items")) if (item.path("rubricItemId").asLong() == 289) recognition = item;
            assertTrue(q8 != null && q8.path("score").asInt() < 14);
            assertTrue(recognition != null);
            assertEquals(3, recognition.path("parts").size());
            assertEquals("blank", recognition.path("parts").get(1).path("status").asText());
            assertTrue(evidence.path("needsHumanReview").asBoolean());
            System.out.println("LIVE_PAPER model=" + model + " q8=" + q8.path("score") + "/14 total=" + output.getSuggestedScore() + " review=true");
        }
    }
}
