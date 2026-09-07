package com.ruoyi.web.controller.monitor;
import java.util.Collections;
import com.ruoyi.business.controller.ClassroomPerformanceController;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.mapper.*;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ClassroomPerformanceOptionalReasonTest {
    private ClassroomPerformanceController controller;
    private BizClassroomPerformanceMapper mapper;
    private BizStudent student;
    @BeforeEach void setup() {
        LoginUser user=new LoginUser();user.setUserId(1L);user.setDeptId(12L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,Collections.emptyList()));
        controller=new ClassroomPerformanceController();mapper=mock(BizClassroomPerformanceMapper.class);
        BizStudentMapper students=mock(BizStudentMapper.class);student=new BizStudent();student.setDeptId(12L);
        when(students.selectBizStudentByStudentId(2L)).thenReturn(student);when(mapper.insertOrUpdate(any())).thenReturn(1);
        ReflectionTestUtils.setField(controller,"performanceMapper",mapper);ReflectionTestUtils.setField(controller,"studentMapper",students);
        ReflectionTestUtils.setField(controller,"guideSheetAccessService",mock(GuideSheetAccessService.class));
    }
    @AfterEach void cleanup() {SecurityContextHolder.clearContext();}
    @Test void singleSaveAcceptsEmptyReason() {
        BizClassroomPerformance p=new BizClassroomPerformance();p.setStudentId(2L);p.setLessonId(1L);p.setScore(1);p.setReason("");
        assertEquals(200,controller.save(p).get("code"));verify(mapper).insertOrUpdate(p);
    }
    @Test void batchSaveAcceptsMissingReason() {
        ClassroomPerformanceController.BatchSaveRequest request=new ClassroomPerformanceController.BatchSaveRequest();request.setLessonId(1L);
        ClassroomPerformanceController.PerformanceItem item=new ClassroomPerformanceController.PerformanceItem();item.setStudentId(2L);item.setScore(-1);
        request.setPerformances(Collections.singletonList(item));assertEquals(200,controller.batchSave(request).get("code"));verify(mapper).insertOrUpdate(any());
    }
    @Test void emptyReasonDoesNotBypassSchoolBoundary() {
        student.setDeptId(13L);BizClassroomPerformance p=new BizClassroomPerformance();p.setStudentId(2L);p.setLessonId(1L);p.setScore(1);
        assertEquals(500,controller.save(p).get("code"));verify(mapper,never()).insertOrUpdate(any());
    }
}
