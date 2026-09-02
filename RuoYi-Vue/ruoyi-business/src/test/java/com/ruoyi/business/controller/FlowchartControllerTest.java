package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import com.ruoyi.business.service.FlowchartService;
import com.ruoyi.common.core.domain.AjaxResult;

class FlowchartControllerTest {
    @Test
    void generateRulesShouldReturnJsonInDataInsteadOfMessage() {
        FlowchartService service = mock(FlowchartService.class);
        when(service.generateRules("answer-json")).thenReturn("[{\"id\":\"node:n1\"}]");
        FlowchartController controller = new FlowchartController(service);

        AjaxResult result = controller.generateRules(
                Collections.<String, Object>singletonMap("answerJson", "answer-json"));

        assertEquals("操作成功", result.get("msg"));
        assertEquals("[{\"id\":\"node:n1\"}]", result.get("data"));
    }
}
