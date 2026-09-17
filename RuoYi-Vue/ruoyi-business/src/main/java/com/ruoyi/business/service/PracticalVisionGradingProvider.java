package com.ruoyi.business.service;

import com.ruoyi.business.domain.TeacherAiConfig;

public interface PracticalVisionGradingProvider
{
    PracticalAiGradingOutput grade(TeacherAiConfig config, String apiKey, PracticalAiGradingInput input);
}
