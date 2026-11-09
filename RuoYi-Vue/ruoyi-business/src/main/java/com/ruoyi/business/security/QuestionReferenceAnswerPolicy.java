package com.ruoyi.business.security;

import com.ruoyi.business.domain.BizQuestion;

/**
 * Python 操作题参考答案的字段级可见性。
 *
 * 题库通用详情和列表接口仍要供工作台读取题干、测试点等公开信息，
 * 但参考答案只能由创建者或管理员查看。裁剪放在 Controller 响应层，
 * 避免改变 Mapper 在课程、考试和成绩分析中的既有查询语义。
 */
public final class QuestionReferenceAnswerPolicy
{
    private QuestionReferenceAnswerPolicy()
    {
    }

    public static void apply(BizQuestion question, Long viewerUserId, boolean admin)
    {
        if (question == null || admin || !isPythonPractical(question))
        {
            return;
        }
        if (viewerUserId != null && viewerUserId.equals(question.getCreatorId()))
        {
            return;
        }
        question.setAnswer(null);
        question.setAnalysis(null);
    }

    private static boolean isPythonPractical(BizQuestion question)
    {
        return "practical".equalsIgnoreCase(question.getQuestionType())
                && "PYTHON".equalsIgnoreCase(question.getPracticalMode());
    }
}
