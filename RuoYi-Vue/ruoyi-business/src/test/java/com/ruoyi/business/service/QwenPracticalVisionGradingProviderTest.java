package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;
import com.ruoyi.common.exception.ServiceException;

class QwenPracticalVisionGradingProviderTest
{
    private final QwenPracticalVisionGradingProvider provider = new QwenPracticalVisionGradingProvider();
    private final ObjectMapper mapper = new ObjectMapper();

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
}
