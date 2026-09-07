package com.ruoyi.business.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** 学生端视图必须脱敏：标准答案、评分规则与检查证据不得下发。 */
class FlowchartStudentViewTest
{
    @Test
    void snapshotViewStripsAnswerAndRules()
    {
        FlowchartLessonSnapshot snapshot = new FlowchartLessonSnapshot();
        snapshot.setSnapshotId(7L);
        snapshot.setLessonId(362L);
        snapshot.setQuestionId(2028L);
        snapshot.setStarterJson("{\"starter\":true}");
        snapshot.setAnswerJson("{\"answer\":true}");
        snapshot.setPermissionsJson("{\"perm\":true}");
        snapshot.setRulesJson("[{\"rule\":1}]");

        FlowchartLessonSnapshot view = snapshot.toStudentView();

        assertNull(view.getAnswerJson());
        assertNull(view.getRulesJson());
        assertEquals("{\"starter\":true}", view.getStarterJson());
        assertEquals("{\"perm\":true}", view.getPermissionsJson());
        assertEquals(362L, view.getLessonId());
        // 原对象不受影响（教师链路仍可用全量口径）。
        assertEquals("{\"answer\":true}", snapshot.getAnswerJson());
    }

    @Test
    void submissionViewStripsRuleSnapshotAndEvidence()
    {
        FlowchartSubmission submission = new FlowchartSubmission();
        submission.setVersionNo(3);
        submission.setDocumentJson("{\"mine\":true}");
        submission.setRulesSnapshotJson("[{\"rule\":1}]");
        submission.setCheckResultJson("{\"checks\":[]}");

        FlowchartSubmission view = submission.toStudentView();

        assertNull(view.getRulesSnapshotJson());
        assertNull(view.getCheckResultJson());
        assertEquals(3, view.getVersionNo());
        assertEquals("{\"mine\":true}", view.getDocumentJson());
    }
}
