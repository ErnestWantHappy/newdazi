package com.ruoyi.business.service.impl;

import com.ruoyi.business.config.ClassroomWebSocketHandler;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.LessonDetailVo;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.mapper.GuideSheetBindingMapper;
import com.ruoyi.business.service.FlowchartService;
import com.ruoyi.business.service.PracticalRubricSnapshotService;
import com.ruoyi.business.service.StudentToolService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P0-P1 核心功能业务治理单测：
 * 涵盖：题目分值防篡改、题库导入校验、共享课程班级边界和 WebSocket 广播格式化。
 */
@ExtendWith(MockitoExtension.class)
class P0P1GovernanceTest
{
    @Mock
    private BizLessonMapper bizLessonMapper;
    @Mock
    private BizLessonQuestionMapper lessonQuestionMapper;
    @Mock
    private BizStudentAnswerMapper studentAnswerMapper;
    @Mock
    private BizLessonAssignmentMapper lessonAssignmentMapper;
    @Mock
    private BizTeacherClassMapper teacherClassMapper;
    @Mock
    private GuideSheetBindingMapper guideSheetBindingMapper;
    @Mock
    private PracticalRubricSnapshotService practicalRubricSnapshotService;
    @Mock
    private FlowchartService flowchartService;
    @Mock
    private StudentToolService studentToolService;
    @Mock
    private BizQuestionMapper bizQuestionMapper;

    @InjectMocks
    private BizLessonServiceImpl lessonService;

    @InjectMocks
    private BizQuestionServiceImpl questionService;

    @BeforeEach
    void setUp()
    {
        SysUser user = new SysUser();
        user.setUserId(101L);
        user.setDeptId(10L);
        user.setUserName("currentTeacher");
        LoginUser loginUser = new LoginUser(101L, 10L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("P1-C: 同校教师查看他人共享课程，返回 readOnly=true 且 canDesign=false")
    void selectLessonDetails_allowsColleagueToViewSharedLessonAsReadOnly()
    {
        Long lessonId = 999L;
        BizLesson sharedLesson = new BizLesson();
        sharedLesson.setLessonId(lessonId);
        sharedLesson.setLessonTitle("他人共享公开课");
        sharedLesson.setDeptId(10L); // 同校
        sharedLesson.setCreatorId(202L); // 他人创建
        sharedLesson.setCreateBy("otherTeacher");
        sharedLesson.setEntryYear("2024");
        sharedLesson.setGrade(7L);

        when(bizLessonMapper.selectBizLessonByLessonId(lessonId)).thenReturn(sharedLesson);
        BizLessonAssignment assignment = new BizLessonAssignment();
        assignment.setLessonId(lessonId);
        assignment.setDeptId(10L);
        assignment.setEntryYear("2024");
        assignment.setClassCode("1");
        when(teacherClassMapper.checkTeacherClassExists(any())).thenReturn(1);
        when(lessonQuestionMapper.selectDetailsByLessonId(lessonId)).thenReturn(Collections.emptyList());
        when(lessonAssignmentMapper.selectClassCodesByLessonIdAndEntryYear(lessonId, "2024"))
                .thenReturn(Collections.singletonList("1"));
        when(teacherClassMapper.selectBizTeacherClassList(any())).thenReturn(Collections.emptyList());

        LessonDetailVo vo = lessonService.selectLessonDetailsByLessonId(lessonId);

        assertNotNull(vo);
        assertEquals(Boolean.TRUE, vo.getReadOnly(), "他人共享课必须标记为只读模式");
        assertEquals(Boolean.FALSE, vo.getCanDesign(), "他人共享课无权直接设计修改");
    }

    @Test
    @DisplayName("P1-C: 同校但不管理课程指派班级的教师不能查看共享课")
    void selectLessonDetails_rejectsUnrelatedColleague()
    {
        BizLesson sharedLesson = new BizLesson();
        sharedLesson.setLessonId(998L);
        sharedLesson.setDeptId(10L);
        sharedLesson.setCreatorId(202L);
        sharedLesson.setEntryYear("2024");
        BizLessonAssignment assignment = new BizLessonAssignment();
        assignment.setLessonId(998L);
        assignment.setDeptId(10L);
        assignment.setEntryYear("2024");
        assignment.setClassCode("2");
        when(bizLessonMapper.selectBizLessonByLessonId(998L)).thenReturn(sharedLesson);
        when(lessonAssignmentMapper.selectClassCodesByLessonIdAndEntryYear(998L, "2024"))
                .thenReturn(Collections.singletonList("2"));
        when(teacherClassMapper.checkTeacherClassExists(any())).thenReturn(0);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> lessonService.selectLessonDetailsByLessonId(998L));
        assertTrue(ex.getMessage().contains("无权查看该课程"));
    }

    @Test
    @DisplayName("P1-C: 查看跨校课程时抛出无权查看异常")
    void selectLessonDetails_rejectsCrossSchoolAccess()
    {
        Long lessonId = 888L;
        BizLesson crossSchoolLesson = new BizLesson();
        crossSchoolLesson.setLessonId(lessonId);
        crossSchoolLesson.setDeptId(9999L); // 跨校

        when(bizLessonMapper.selectBizLessonByLessonId(lessonId)).thenReturn(crossSchoolLesson);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                lessonService.selectLessonDetailsByLessonId(lessonId));
        assertTrue(ex.getMessage().contains("无权查看该课程"));
    }

    @Test
    @DisplayName("P0-B: 题库导入前置校验题干必填，失败整批不入库")
    void importQuestion_rejectsEmptyContentBeforeWritingAnyRow()
    {
        List<BizQuestion> list = new ArrayList<>();
        BizQuestion q1 = new BizQuestion();
        q1.setQuestionType("choice");
        q1.setQuestionContent(""); // 空题干
        q1.setOptionA("A");
        q1.setOptionB("B");
        q1.setAnswer("A");
        list.add(q1);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                questionService.importQuestion(list, "currentTeacher"));

        assertTrue(ex.getMessage().contains("题干内容不能为空"), "必须提示题干内容不能为空");
        verify(bizQuestionMapper, never()).insertBizQuestion(any());
    }

    @Test
    @DisplayName("P0-B: 题库导入选择题缺少选项直接拦截")
    void importQuestion_rejectsChoiceMissingOptions()
    {
        List<BizQuestion> list = new ArrayList<>();
        BizQuestion q1 = new BizQuestion();
        q1.setQuestionType("choice");
        q1.setQuestionContent("计算机的核心组件是？");
        q1.setOptionA("CPU");
        // 缺少选项 B
        q1.setAnswer("A");
        list.add(q1);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                questionService.importQuestion(list, "currentTeacher"));

        assertTrue(ex.getMessage().contains("选择题必须提供选项 A 和选项 B"), "必须提示选择题必须提供选项 A 和选项 B");
        verify(bizQuestionMapper, never()).insertBizQuestion(any());
    }

    @Test
    @DisplayName("P0-B: 题库导入拒绝越界答案及不存在的选项")
    void importQuestion_rejectsInvalidChoiceAnswer()
    {
        BizQuestion invalidLetter = new BizQuestion();
        invalidLetter.setQuestionType("choice");
        invalidLetter.setQuestionContent("非法答案");
        invalidLetter.setOptionA("甲");
        invalidLetter.setOptionB("乙");
        invalidLetter.setAnswer("E");

        BizQuestion missingOption = new BizQuestion();
        missingOption.setQuestionType("choice");
        missingOption.setQuestionContent("答案指向空选项");
        missingOption.setOptionA("甲");
        missingOption.setOptionB("乙");
        missingOption.setAnswer("C");

        ServiceException ex = assertThrows(ServiceException.class, () ->
                questionService.importQuestion(java.util.Arrays.asList(invalidLetter, missingOption), "currentTeacher"));
        assertTrue(ex.getMessage().contains("只能填写 A、B、C 或 D"));
        assertTrue(ex.getMessage().contains("对应的选项内容不能为空"));
        verify(bizQuestionMapper, never()).insertBizQuestion(any());
    }

    @Test
    @DisplayName("P0-A: 课程题目已有提交时禁止修改分值")
    void saveLessonDetails_blocksQuestionScoreChangeIfAnswersExist()
    {
        Long lessonId = 777L;
        BizLesson existing = new BizLesson();
        existing.setLessonId(lessonId);
        existing.setDeptId(10L);
        existing.setCreatorId(101L);
        existing.setCreateBy("currentTeacher");
        existing.setLessonTitle("已开课作业");
        existing.setEntryYear("2024");
        existing.setGrade(7L);
        when(bizLessonMapper.selectBizLessonByLessonId(lessonId)).thenReturn(existing);

        // 原题目两道：题1 80分，题2 20分，总分 100分
        BizLessonQuestionDetailVo prevQ1 = new BizLessonQuestionDetailVo();
        prevQ1.setQuestionId(301L);
        prevQ1.setQuestionScore(80L);
        prevQ1.setQuestionContent("测试题目1");

        BizLessonQuestionDetailVo prevQ2 = new BizLessonQuestionDetailVo();
        prevQ2.setQuestionId(302L);
        prevQ2.setQuestionScore(20L);
        prevQ2.setQuestionContent("测试题目2");

        List<BizLessonQuestionDetailVo> prevQuestions = new ArrayList<>();
        prevQuestions.add(prevQ1);
        prevQuestions.add(prevQ2);
        when(lessonQuestionMapper.selectDetailsByLessonId(lessonId)).thenReturn(prevQuestions);
        when(bizLessonMapper.updateBizLesson(any())).thenReturn(1);

        // 模拟题 1 已有学生答题提交记录
        when(studentAnswerMapper.countAnswersByLessonAndQuestion(lessonId, 301L)).thenReturn(5);

        LessonDetailVo requestVo = new LessonDetailVo();
        requestVo.setLessonId(lessonId);
        requestVo.setLessonTitle("已开课作业");
        requestVo.setEntryYear("2024");
        requestVo.setGrade(7L);
        requestVo.setGuideSheetEnabled(false);

        // 尝试将 题1 调为 70分，题2 调为 30分（总分仍然是 100分满足前置总分校验）
        BizLessonQuestionDetailVo modifiedQ1 = new BizLessonQuestionDetailVo();
        modifiedQ1.setQuestionId(301L);
        modifiedQ1.setQuestionScore(70L);
        BizLessonQuestionDetailVo modifiedQ2 = new BizLessonQuestionDetailVo();
        modifiedQ2.setQuestionId(302L);
        modifiedQ2.setQuestionScore(30L);

        List<BizLessonQuestionDetailVo> newQuestions = new ArrayList<>();
        newQuestions.add(modifiedQ1);
        newQuestions.add(modifiedQ2);
        requestVo.setQuestions(newQuestions);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                lessonService.saveLessonDetails(requestVo));

        assertTrue(ex.getMessage().contains("已有学生答题提交记录，不能修改题目分值"), "实际异常信息为: " + ex.getMessage());
    }

    @Test
    @DisplayName("P1-D: WebSocket 广播向规范化房间广播消息")
    void websocketBroadcast_formatsRoomKeyWithNormalizedClassCode() throws Exception
    {
        ClassroomWebSocketHandler handler = new ClassroomWebSocketHandler();
        WebSocketSession session = org.mockito.Mockito.mock(WebSocketSession.class);
        when(session.getId()).thenReturn("sess-001");
        when(session.isOpen()).thenReturn(true);

        Map<String, Object> attrs = new HashMap<>();
        // Interceptor 规范化后的 roomKey 为: 10_2024_1_999 (去掉'班'字)
        attrs.put("roomKey", "10_2024_1_999");
        attrs.put("userId", 101L);
        when(session.getAttributes()).thenReturn(attrs);

        handler.afterConnectionEstablished(session);

        // 业务广播传入带"班"或不带"班"的 classCode，均能精准命中该房间
        handler.broadcastToClassroom(10L, "2024", "1班", 999L, "{\"type\":\"SUBMISSION_UPDATE\"}");

        verify(session).sendMessage(any(TextMessage.class));
    }
}
