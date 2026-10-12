package com.ruoyi.business.service;

import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PracticalSubmissionPolicyTest {
    @Test void ungradedAndFirstSubmissionRemainEditable() {
        assertTrue(PracticalSubmissionPolicy.canUpdate(null, false));
        assertTrue(PracticalSubmissionPolicy.canUpdate(new BizStudentAnswer(), false));
    }
    @Test void zeroIsAFormalGradeAndLocksTheWork() {
        BizStudentAnswer answer = new BizStudentAnswer(); answer.setScore(0);
        assertThrows(ServiceException.class, () -> PracticalSubmissionPolicy.assertCanUpdate(answer, false));
    }
    @Test void returnedGradeAllowsUpdateWithoutClearingTheOldScore() {
        BizStudentAnswer answer = new BizStudentAnswer(); answer.setScore(12);
        assertDoesNotThrow(() -> PracticalSubmissionPolicy.assertCanUpdate(answer, true));
        assertEquals(12, answer.getScore());
    }
}
