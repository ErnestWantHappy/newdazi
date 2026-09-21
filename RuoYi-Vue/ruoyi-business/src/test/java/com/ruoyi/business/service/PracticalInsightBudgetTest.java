package com.ruoyi.business.service;

import java.math.BigDecimal;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PracticalInsightBudgetTest
{
    @Test void cumulativeTokensIncludePreviousAttemptsAndStopAtLimit()
    {
        Job job = new Job(); job.promptTokens = 1000; job.completionTokens = 500;
        Input in = new Input(); in.inputPrice = new BigDecimal(".012"); in.outputPrice = new BigDecimal(".036");
        assertDoesNotThrow(() -> PracticalInsightAiService.requireBudget(job, in, new BigDecimal(".031")));
        assertThrows(PracticalInsightAiService.BudgetExceeded.class,
                () -> PracticalInsightAiService.requireBudget(job, in, new BigDecimal(".030")));
        assertThrows(PracticalInsightAiService.BudgetExceeded.class,
                () -> PracticalInsightAiService.requireBudget(job, in, BigDecimal.ZERO));
    }
}
