package com.ruoyi.business.service;

import java.util.*;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataLabAccessServiceTest {
    DataLabAccessService access = new DataLabAccessService();
    DataLabMapper labs = mock(DataLabMapper.class);
    PythonPracticeMapper practice = mock(PythonPracticeMapper.class);
    BizStudentMapper students = mock(BizStudentMapper.class);
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(access,"mapper",labs);
        ReflectionTestUtils.setField(access,"practiceMapper",practice);
        ReflectionTestUtils.setField(access,"studentMapper",students);
        login(20L, Collections.emptySet());
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    static void login(Long id, Set<String> permissions) {
        SysUser user = new SysUser(); user.setUserId(id); user.setDeptId(10L);
        user.setRoles(Collections.emptyList());
        LoginUser login = new LoginUser(id,10L,user,permissions);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login,"",Collections.emptyList()));
    }
    BizStudent student(Long id, String cls) {
        BizStudent s = new BizStudent(); s.setUserId(id); s.setDeptId(10L); s.setEntryYear("2026"); s.setClassCode(cls); return s;
    }
    @Test void studentMustUseOwnIdentityAndAssignedVersion() {
        assertThrows(ServiceException.class,()->access.student(21L,2L,3L));
        when(students.selectBizStudentByUserId(20L)).thenReturn(student(20L,"1"));
        Map<String,Object> row=new HashMap<>(); row.put("source_id",2L); row.put("question_id",3L); row.put("practical_mode","DATA_LAB");
        when(practice.selectStudentQuestions(10L,"2026","1")).thenReturn(Collections.singletonList(row));
        assertSame(row,access.student(20L,2L,3L));
        assertThrows(ServiceException.class,()->access.student(20L,4L,3L));
        assertThrows(ServiceException.class,()->access.student(20L,2L,4L));
    }
    @Test void chartOwnerCanReadButClassmateCannot() {
        DataLabSubmission row=new DataLabSubmission(); row.setStudentId(20L); row.setSourceId(2L); row.setQuestionId(3L);
        when(labs.selectSubmissionByChart("/profile/datalab/charts/owned.png")).thenReturn(row);
        assertDoesNotThrow(()->access.chart("/profile/datalab/charts/owned.png"));
        login(21L,Collections.emptySet());
        assertThrows(ServiceException.class,()->access.chart("/profile/datalab/charts/owned.png"));
        assertThrows(ServiceException.class,()->access.chart("/profile/datalab/charts/unknown.png"));
        assertThrows(ServiceException.class,()->access.chart("/profile/datalab/excel/a.xlsx"));
    }
    @Test void teacherMustManageStudentClassInSameSchool() {
        login(30L,Collections.singleton("business:pythonPractice:query"));
        Map<String,Object> plan=new HashMap<>();plan.put("dept_id",10L);
        when(practice.selectPlanByVersion(2L)).thenReturn(plan);
        when(labs.countVersionQuestion(2L,3L)).thenReturn(1);
        when(students.selectBizStudentByUserId(20L)).thenReturn(student(20L,"1"));
        DataLabSubmission row=new DataLabSubmission(); row.setStudentId(20L); row.setSourceId(2L); row.setQuestionId(3L);
        assertThrows(ServiceException.class,()->access.submission(row,true));
        Map<String,Object> cls=new HashMap<>();cls.put("entry_year","2026");cls.put("class_code","1");
        when(practice.selectManagedClasses(30L,10L,false)).thenReturn(Collections.singletonList(cls));
        assertDoesNotThrow(()->access.submission(row,true));
        plan.put("dept_id",11L);
        assertThrows(ServiceException.class,()->access.submission(row,true));
    }
}
