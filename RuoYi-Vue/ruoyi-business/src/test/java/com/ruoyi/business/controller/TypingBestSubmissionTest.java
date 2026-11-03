package com.ruoyi.business.controller;

import com.ruoyi.business.domain.*;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.*;
import com.ruoyi.business.service.*;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.system.mapper.SysDeptMapper;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 从交卷入口核对择优标志和返回记录，覆盖空答案以及一次提交多道打字题。 */
@ExtendWith(MockitoExtension.class)
class TypingBestSubmissionTest {
    @Mock BizStudentMapper bizStudentMapper;
    @Mock BizStudentAnswerMapper studentAnswerMapper;
    @Mock BizLessonMapper lessonMapper;
    @Mock BizLessonAssignmentMapper lessonAssignmentMapper;
    @Mock BizLessonQuestionMapper lessonQuestionMapper;
    @Mock SysDeptMapper deptMapper;
    @Mock ICountyExamService countyExamService;
    @Mock StudentAnswerSubmissionService studentAnswerSubmissionService;
    @Mock PracticalGradingDeadlineService practicalGradingDeadlineService;
    @InjectMocks StudentHomeController controller;

    @BeforeEach void setup() {
        LoginUser login = new LoginUser(20L, 9L, new SysUser(), Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login, null, Collections.emptyList()));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"测", "测试原文"})
    void lowerOrEmptyAttemptReturnsSavedBestPerQuestion(String typed) {
        BizStudent student = new BizStudent(); student.setStudentId(30L); student.setDeptId(9L); student.setEntryYear("2022"); student.setClassCode("5");
        when(bizStudentMapper.selectBizStudentByUserId(20L)).thenReturn(student);
        when(countyExamService.checkCurrentStudentExam()).thenReturn(Collections.emptyMap());
        BizLesson lesson = new BizLesson(); lesson.setLessonId(10L); lesson.setDeptId(9L);
        when(lessonMapper.selectBizLessonByLessonId(10L)).thenReturn(lesson);
        when(lessonAssignmentMapper.selectCurrentLessonByClass("2022", "5", 9L)).thenReturn(10L);
        List<BizLessonQuestionDetailVo> questions = new ArrayList<>();
        for (long id : new long[] {1, 2}) {
            BizLessonQuestionDetailVo q = new BizLessonQuestionDetailVo();
            q.setQuestionId(id); q.setQuestionType("typing"); q.setQuestionContent("测试原文"); q.setQuestionScore(50L); q.setTypingDuration(5);
            questions.add(q);
            BizStudentAnswer saved = new BizStudentAnswer(); saved.setQuestionId(id); saved.setScore(id == 1 ? 40 : 45);
            saved.setStudentAnswer("测试原文"); saved.setTypingSpeed(24); saved.setAccuracyRate(100D); saved.setCompletionRate(100D);
            when(studentAnswerMapper.selectLatestByStudentLessonQuestion(30L, 10L, id)).thenReturn(saved);
        }
        when(lessonQuestionMapper.selectDetailsByLessonId(10L)).thenReturn(questions);
        StudentHomeController.SubmitAnswerRequest request = new StudentHomeController.SubmitAnswerRequest();
        request.setLessonId(10L); request.setTypingRevision(0L);
        Map<Long, String> answers = new HashMap<>(); answers.put(1L, typed); answers.put(2L, typed); request.setAnswers(answers);
        AjaxResult result = controller.submitAnswers(request);
        assertEquals(200, result.get("code"));
        assertEquals(85, result.get("totalScore"));
        Map<?, ?> results = (Map<?, ?>) result.get("typingResults");
        assertEquals(40, ((Map<?, ?>) results.get(1L)).get("score"));
        assertEquals(24, ((Map<?, ?>) results.get(1L)).get("typingSpeed"));
        assertEquals(45, ((Map<?, ?>) results.get(2L)).get("score"));
        ArgumentCaptor<List<BizStudentAnswer>> capture = ArgumentCaptor.forClass(List.class);
        verify(studentAnswerSubmissionService).persistAnswers(eq(30L), eq(10L), capture.capture());
        assertTrue(capture.getValue().stream().allMatch(a -> Boolean.TRUE.equals(a.getKeepBestScore())));
    }
}
