package com.ruoyi.business.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import com.ruoyi.business.mapper.PracticalInsightMapper;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PracticalInsightReportServiceTest
{
    PracticalInsightReportService service; PracticalInsightMapper mapper; PracticalInsightInputService inputs;
    PracticalInsightWorker worker; TeacherPaperSubmissionService scope; ObjectMapper json = new ObjectMapper();
    @BeforeEach void setup()
    {
        service = new PracticalInsightReportService(); mapper = mock(PracticalInsightMapper.class); inputs = mock(PracticalInsightInputService.class);
        worker = mock(PracticalInsightWorker.class); scope = mock(TeacherPaperSubmissionService.class);
        ReflectionTestUtils.setField(service,"mapper",mapper); ReflectionTestUtils.setField(service,"inputs",inputs);
        ReflectionTestUtils.setField(service,"worker",worker); ReflectionTestUtils.setField(service,"scope",scope);
        ReflectionTestUtils.setField(service,"json",json); ReflectionTestUtils.setField(service,"enabled",true);
        ReflectionTestUtils.setField(service,"feedbackMapper",mock(com.ruoyi.business.mapper.StudentFeedbackMapper.class));
        LoginUser user = new LoginUser(); user.setUserId(7L); SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    Input input() { Input i = new Input(); i.scope = new Request(); i.teacherId = 7L; i.deptId = 9L; i.fingerprint = "current"; return i; }
    Job job(Input i) throws Exception { Job j = new Job(); j.reportId=1L; j.teacherId=7L; j.deptId=9L; j.status="FAILED"; j.inputJson=json.writeValueAsString(i); when(mapper.get(1L)).thenReturn(j); when(scope.requireScope(any())).thenReturn(9L); return j; }
    @Test void anotherTeacherCannotReadEvidence() throws Exception { Job j=job(input()); j.teacherId=8L; assertThrows(ServiceException.class,()->service.student(1L,1L)); verify(mapper,never()).evidence(any()); }
    @Test void revokedScopeCannotReadReport() throws Exception { job(input()); when(scope.requireScope(any())).thenThrow(new ServiceException("denied")); assertThrows(ServiceException.class,()->service.get(1L)); }
    @Test void changedInputCannotRetry() throws Exception { job(input()); Input fresh=input(); fresh.fingerprint="changed"; when(inputs.build(any())).thenReturn(fresh); assertThrows(ServiceException.class,()->service.retry(1L)); verify(mapper,never()).retry(any()); }
    @Test void repeatedRetryCannotDispatchTwice() throws Exception { Input i=input(); job(i); when(inputs.build(any())).thenReturn(i); when(mapper.retry(1L)).thenReturn(1,0); service.retry(1L); service.retry(1L); verify(worker,times(1)).run(1L); }
    @Test void missingFormalGradesCannotCreate() throws Exception { Input i=input(); when(inputs.build(any())).thenReturn(i); assertThrows(ServiceException.class,()->service.create(i.scope)); verify(mapper,never()).insert(any()); }
    @Test void uninstalledFeatureDoesNotQueryNewTables() throws Exception { ReflectionTestUtils.setField(service,"enabled",false); assertEquals(false,service.latest(new Request()).get("enabled")); verifyNoInteractions(mapper); }
    @Test void pollingActiveJobDoesNotWriteAndExposesProgressTime() throws Exception
    {
        Job j=job(input()); j.status="RUNNING"; j.updatedAt=new java.util.Date();
        java.util.Map<?,?> view=(java.util.Map<?,?>)service.get(1L).get("job");
        assertEquals(j.updatedAt,view.get("updatedAt"));
        verify(mapper,never()).interruptStale(any());
        verify(mapper).staleIds(7L);
    }
    @Test void onlyExpiredOwnedTaskIsRecovered() throws Exception
    {
        Job j=job(input()); j.status="RUNNING";
        when(mapper.staleIds(7L)).thenReturn(java.util.Collections.singletonList(1L));
        service.get(1L); verify(mapper).interruptStale(1L);
    }
}
