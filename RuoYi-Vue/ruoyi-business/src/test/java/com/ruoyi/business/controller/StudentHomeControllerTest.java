package com.ruoyi.business.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 无效提交必须被识别：请求题目无一属于当前课程时不得误报成功。 */
class StudentHomeControllerTest
{
    @Test
    void matchedQuestionDetected()
    {
        Map<Long, String> answers = new HashMap<Long, String>();
        answers.put(1218L, "A");
        Map<Long, Object> questionMap = new HashMap<Long, Object>();
        questionMap.put(1218L, new Object());
        assertTrue(StudentHomeController.hasMatchedQuestion(answers, questionMap));
    }

    @Test
    void allInvalidQuestionsRejected()
    {
        Map<Long, String> answers = new HashMap<Long, String>();
        answers.put(999999L, "X");
        Map<Long, Object> questionMap = new HashMap<Long, Object>();
        questionMap.put(1218L, new Object());
        assertFalse(StudentHomeController.hasMatchedQuestion(answers, questionMap));
    }

    @Test
    void nullAndEmptyInputsRejected()
    {
        Map<Long, Object> questionMap = new HashMap<Long, Object>();
        questionMap.put(1218L, new Object());
        assertFalse(StudentHomeController.hasMatchedQuestion(null, questionMap));
        assertFalse(StudentHomeController.hasMatchedQuestion(Collections.<Long, String>emptyMap(), questionMap));
        assertFalse(StudentHomeController.hasMatchedQuestion(Collections.singletonMap(1218L, "A"), null));
    }
}
