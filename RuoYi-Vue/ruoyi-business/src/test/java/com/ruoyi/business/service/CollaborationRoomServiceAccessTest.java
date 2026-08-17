package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.CollaborationRoom;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.CollaborationMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;

class CollaborationRoomServiceAccessTest
{
    private CollaborationRoomService service;
    private BizStudentMapper studentMapper;
    private BizLessonAssignmentMapper assignmentMapper;
    private CollaborationMapper collaborationMapper;

    @BeforeEach
    void setUp()
    {
        service = new CollaborationRoomService();
        studentMapper = mock(BizStudentMapper.class);
        assignmentMapper = mock(BizLessonAssignmentMapper.class);
        collaborationMapper = mock(CollaborationMapper.class);
        ReflectionTestUtils.setField(service, "studentMapper", studentMapper);
        ReflectionTestUtils.setField(service, "assignmentMapper", assignmentMapper);
        ReflectionTestUtils.setField(service, "collaborationMapper", collaborationMapper);

        SysUser user = new SysUser();
        user.setUserId(5551L);
        user.setDeptId(169L);
        user.setUserName("2025720104");
        LoginUser loginUser = new LoginUser(5551L, 169L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));

        BizStudent student = new BizStudent();
        student.setUserId(5551L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        when(studentMapper.selectBizStudentByUserId(5551L)).thenReturn(student);
        when(assignmentMapper.selectCurrentLessonByClass("2025", "1", 169L)).thenReturn(268L);
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void matchingStudentCanEnterWithoutSystemRole()
    {
        String scope = ReflectionTestUtils.invokeMethod(service, "requireRoomAccess", room("1"), 5551L);

        assertEquals("STUDENT", scope);
    }

    @Test
    void differentClassIsStillRejected()
    {
        assertThrows(ServiceException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "requireRoomAccess", room("2"), 5551L));
    }

    @Test
    void classBoundaryIsCheckedBeforePublicNetworkHealth()
    {
        when(collaborationMapper.selectRoomById(99L)).thenReturn(room("2"));

        ServiceException error = assertThrows(ServiceException.class, () -> service.createSession(99L));

        assertTrue(error.getMessage().contains("只能进入自己当前课程的班级协作房间"));
    }

    private CollaborationRoom room(String classCode)
    {
        CollaborationRoom room = new CollaborationRoom();
        room.setDeptId(169L);
        room.setLessonId(268L);
        room.setEntryYear("2025");
        room.setClassCode(classCode);
        return room;
    }
}
