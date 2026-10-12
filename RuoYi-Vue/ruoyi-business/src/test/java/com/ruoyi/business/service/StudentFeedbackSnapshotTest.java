package com.ruoyi.business.service;

import java.util.Map;
import java.util.List;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StudentFeedbackSnapshotTest {
    @Test void retryChangesPublicationContentWithoutReplacingPreviousSnapshot() throws Exception {
        Job job=new Job(); job.inputJson="input"; job.reportJson="report";
        EvidenceRow own=new EvidenceRow(); own.studentId=1L; own.status="FAILED";
        String before=StudentFeedbackService.publicationHash(job,java.util.Collections.singletonList(own));
        own.status="SUCCESS"; own.resultJson="personal result";
        String after=StudentFeedbackService.publicationHash(job,java.util.Collections.singletonList(own));
        assertNotEquals(before,after);
        assertEquals(after,StudentFeedbackService.publicationHash(job,java.util.Collections.singletonList(own)));
    }
    @Test void noEvidenceCannotCreatePersonalErrorClaim() {
        Student student = new Student(); student.score = 0; student.maxScore = 100;
        Result result = new Result(); Diagnosis diagnosis = new Diagnosis(); diagnosis.observation = "未证实的判断";
        ErrorDetail error = new ErrorDetail(); error.observedError = "缺证据"; error.evidenceIds.add("absent"); diagnosis.errors.add(error);
        result.diagnoses.add(diagnosis);
        Map<String,Object> snapshot = StudentFeedbackService.personalSnapshot(student,result,null);
        assertEquals(0,snapshot.get("scoreAtAnalysis"));
        assertTrue(((List<?>)snapshot.get("diagnoses")).isEmpty());
        assertFalse(snapshot.containsKey("students"));
    }
    @Test void evidenceSupportsOwnFeedbackWithoutClassReport() {
        Student student = new Student(); Result result = new Result(); Evidence evidence = new Evidence(); evidence.id = "own-1"; evidence.text = "原答";
        result.evidence.add(evidence);
        Diagnosis diagnosis = new Diagnosis(); diagnosis.evidenceIds.add(evidence.id); result.diagnoses.add(diagnosis);
        Map<String,Object> snapshot = StudentFeedbackService.personalSnapshot(student,result,null);
        assertEquals(1,((List<?>)snapshot.get("diagnoses")).size());
        assertFalse(snapshot.containsKey("teacherId"));
        assertFalse(snapshot.containsKey("studentMatrix"));
    }
}
