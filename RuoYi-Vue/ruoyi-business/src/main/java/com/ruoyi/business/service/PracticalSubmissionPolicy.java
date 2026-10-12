package com.ruoyi.business.service;

import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.common.exception.ServiceException;

/** 文件和画程共用正式成绩边界，零分不能被误认为尚未批改。 */
public final class PracticalSubmissionPolicy {
    private PracticalSubmissionPolicy() { }

    public static boolean canUpdate(BizStudentAnswer answer, boolean returned) {
        return answer == null || answer.getScore() == null || returned;
    }

    public static void assertCanUpdate(BizStudentAnswer answer, boolean returned) {
        if (!canUpdate(answer, returned))
            throw new ServiceException("作品已批改，如需修改请联系老师退回重交");
    }
}
