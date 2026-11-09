package com.ruoyi.business.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import com.ruoyi.business.domain.BizQuestion;

class QuestionReferenceAnswerPolicyTest
{
    private static final Long OWNER_ID = 1920L;
    private static final Long OTHER_TEACHER_ID = 1885L;

    @Test
    void ownerKeepsPythonReferenceAnswer()
    {
        BizQuestion question = pythonQuestion();

        QuestionReferenceAnswerPolicy.apply(question, OWNER_ID, false);

        assertEquals("reference", question.getAnswer());
        assertEquals("analysis", question.getAnalysis());
    }

    @Test
    void nonOwnerTeacherCannotReadPythonReferenceAnswer()
    {
        BizQuestion question = pythonQuestion();

        QuestionReferenceAnswerPolicy.apply(question, OTHER_TEACHER_ID, false);

        assertNull(question.getAnswer());
        assertNull(question.getAnalysis());
    }

    @Test
    void adminKeepsPythonReferenceAnswer()
    {
        BizQuestion question = pythonQuestion();

        QuestionReferenceAnswerPolicy.apply(question, OTHER_TEACHER_ID, true);

        assertEquals("reference", question.getAnswer());
        assertEquals("analysis", question.getAnalysis());
    }

    @Test
    void choiceAnswerRemainsAvailableForExistingCourseFlows()
    {
        BizQuestion question = new BizQuestion();
        question.setQuestionType("choice");
        question.setAnswer("A");
        question.setAnalysis("选择题解析");

        QuestionReferenceAnswerPolicy.apply(question, OTHER_TEACHER_ID, false);

        assertEquals("A", question.getAnswer());
        assertEquals("选择题解析", question.getAnalysis());
    }

    private BizQuestion pythonQuestion()
    {
        BizQuestion question = new BizQuestion();
        question.setQuestionType("practical");
        question.setPracticalMode("PYTHON");
        question.setCreatorId(OWNER_ID);
        question.setAnswer("reference");
        question.setAnalysis("analysis");
        return question;
    }
}
