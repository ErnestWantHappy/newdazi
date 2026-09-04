package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.config.IotMqttProperties;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.IotMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;

class IotExperimentServiceAccessTest
{
    private IotExperimentService service;
    private BizStudentMapper studentMapper;
    private BizLessonAssignmentMapper assignmentMapper;
    private IotMapper iotMapper;

    @BeforeEach
    void setUp()
    {
        service = new IotExperimentService();
        studentMapper = mock(BizStudentMapper.class);
        assignmentMapper = mock(BizLessonAssignmentMapper.class);
        iotMapper = mock(IotMapper.class);
        ReflectionTestUtils.setField(service, "studentMapper", studentMapper);
        ReflectionTestUtils.setField(service, "assignmentMapper", assignmentMapper);
        ReflectionTestUtils.setField(service, "mapper", iotMapper);
        ReflectionTestUtils.setField(service, "mqttProperties", new IotMqttProperties());

        SysUser user = new SysUser();
        user.setUserId(701L);
        user.setDeptId(169L);
        user.setNickName("学生");
        LoginUser loginUser = new LoginUser(701L, 169L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));

        BizStudent student = new BizStudent();
        student.setUserId(701L);
        student.setStudentId(801L);
        student.setDeptId(169L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        when(studentMapper.selectBizStudentByUserId(701L)).thenReturn(student);
        when(assignmentMapper.selectCurrentLessonByClass("2025", "1", 169L)).thenReturn(268L);
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsNonCurrentLessonBeforeLoadingExperiments()
    {
        ServiceException error = assertThrows(ServiceException.class,
                () -> service.getStudentOverview(269L));

        org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("当前课程"));
        org.mockito.Mockito.verifyNoInteractions(iotMapper);
    }

    @Test
    void rejectsStudentProfileWithDifferentSchoolBeforeLoadingExperiments()
    {
        BizStudent otherSchoolStudent = new BizStudent();
        otherSchoolStudent.setUserId(701L);
        otherSchoolStudent.setStudentId(801L);
        otherSchoolStudent.setDeptId(170L);
        otherSchoolStudent.setEntryYear("2025");
        otherSchoolStudent.setClassCode("1");
        when(studentMapper.selectBizStudentByUserId(701L)).thenReturn(otherSchoolStudent);

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.getStudentOverview(268L));

        org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("学校"));
        org.mockito.Mockito.verifyNoInteractions(assignmentMapper, iotMapper);
    }

    @Test
    void parsesBrokerUrlWithoutConfusingHostAndPort()
    {
        assertEquals("10.52.1.129", ReflectionTestUtils.invokeMethod(service,
                "parseBrokerHost", "tcp://10.52.1.129:1883/mqtt"));
        assertEquals(Integer.valueOf(1883), (Integer) ReflectionTestUtils.invokeMethod(service,
                "parseBrokerPort", "tcp://10.52.1.129:1883/mqtt"));
        assertEquals("2001:db8::1", ReflectionTestUtils.invokeMethod(service,
                "parseBrokerHost", "ssl://[2001:db8::1]:8883"));
        assertEquals(Integer.valueOf(8883), (Integer) ReflectionTestUtils.invokeMethod(service,
                "parseBrokerPort", "ssl://[2001:db8::1]:8883"));
    }
}
