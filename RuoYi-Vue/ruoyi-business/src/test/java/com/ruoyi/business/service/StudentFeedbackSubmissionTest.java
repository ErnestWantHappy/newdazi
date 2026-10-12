package com.ruoyi.business.service;

import java.util.*;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentFeedbackSubmissionTest {
    @Test void clearedPlaceholderIsUnsubmittedButFormalZeroIsStillGraded() throws Exception {
        StudentFeedbackService service = spy(new StudentFeedbackService());
        BizStudent student = new BizStudent(); student.setStudentId(7L); student.setDeptId(3L);
        doReturn(student).when(service).currentStudent();
        BizStudentAnswerMapper answers = mock(BizStudentAnswerMapper.class);
        BizLessonQuestionMapper questions = mock(BizLessonQuestionMapper.class);
        ClassroomTaskStateService states = mock(ClassroomTaskStateService.class);
        ReflectionTestUtils.setField(service, "answers", answers);
        ReflectionTestUtils.setField(service, "questions", questions);
        ReflectionTestUtils.setField(service, "states", states);
        StudentFeedbackMapper feedback = mock(StudentFeedbackMapper.class);
        when(feedback.latest(7L, 3L, 9L, 10L)).thenReturn(null);
        ReflectionTestUtils.setField(service, "feedback", feedback);
        BizStudentAnswer answer = new BizStudentAnswer(); answer.setQuestionId(10L); answer.setStudentAnswer("");
        BizLessonQuestionDetailVo question = new BizLessonQuestionDetailVo(); question.setQuestionId(10L); question.setQuestionType("practical"); question.setPracticalMode("FILE");
        when(answers.selectByStudentAndLesson(7L, 9L)).thenReturn(Collections.singletonList(answer));
        when(questions.selectDetailsByLessonId(9L)).thenReturn(Collections.singletonList(question));
        Map<String,Object> row = service.mine(9L).get(0);
        assertEquals(false, row.get("submitted")); assertEquals("WORKING", row.get("taskState"));
        verifyNoInteractions(states);
        answer.setScore(0);
        row = service.mine(9L).get(0);
        assertEquals(true, row.get("submitted")); assertEquals("GRADED", row.get("taskState"));
        assertEquals(0, row.get("score"));
    }
}
