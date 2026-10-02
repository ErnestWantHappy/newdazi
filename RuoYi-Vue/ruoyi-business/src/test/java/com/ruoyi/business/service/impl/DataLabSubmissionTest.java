package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.*;
import com.ruoyi.business.mapper.DataLabMapper;
import com.ruoyi.business.service.DataLabAccessService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import java.util.Collections;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataLabSubmissionTest {
    DataLabMapper mapper=mock(DataLabMapper.class);
    DataLabAccessService access=mock(DataLabAccessService.class);
    com.ruoyi.business.mapper.PythonPracticeMapper practice=mock(com.ruoyi.business.mapper.PythonPracticeMapper.class);
    DataLabServiceImpl service=new DataLabServiceImpl();
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service,"dataLabMapper",mapper); ReflectionTestUtils.setField(service,"access",access);
        ReflectionTestUtils.setField(service,"practiceMapper",practice);
        when(practice.lockPlanByVersion(2L)).thenReturn(1L);
        SysUser user=new SysUser();user.setUserId(20L);user.setDeptId(10L);
        LoginUser login=new LoginUser(20L,10L,user,Collections.singleton("business:pythonPractice:edit"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login,"",Collections.emptyList()));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void resubmissionRevisionAndSnapshotMaximumProtectGrading() {
        DataLabSubmission row=new DataLabSubmission();row.setMaxScore(50);
        when(mapper.selectSubmissionById(1L)).thenReturn(row);
        assertThrows(ServiceException.class,()->service.scoreSubmission(1L,51,"",20L,1L));
        assertThrows(ServiceException.class,()->service.scoreSubmission(1L,40,"",20L,null));
        assertThrows(ServiceException.class,()->service.scoreSubmission(1L,40,"",20L,1L));
        when(mapper.updateScore(1L,40,"",20L,2L)).thenReturn(1);
        assertEquals(1,service.scoreSubmission(1L,40,"",999L,2L));
        verify(mapper,never()).updateScore(any(),any(),any(),eq(999L),any());
    }
    @Test void submitUsesFrozenConfigurationAndRequiresVersion() {
        assertThrows(ServiceException.class,()->service.submit(20L,3L,"BASE_VERSION",null,"print(1)",null,""));
        DataLabConfig config=new DataLabConfig();config.setMaxScore(50);
        when(mapper.selectSnapshot(2L,3L)).thenReturn(config);
        service.submit(20L,3L,"BASE_VERSION",2L,"print(1)",null,"");
        verify(access).student(20L,2L,3L);
        verify(mapper).upsertSubmission(argThat(row->row.getMaxScore()==50 && row.getStudentId()==20L));
        verify(mapper,never()).selectConfigByQuestionId(any());
    }
}
