package com.ruoyi.business.controller;

import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.service.LessonAutoAdvanceService;
import com.ruoyi.business.service.TeacherDashboardCacheService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LessonAdvanceControllerTest
{
    private final LessonAutoAdvanceService service = mock(LessonAutoAdvanceService.class);
    private final TeacherDashboardCacheService cache = mock(TeacherDashboardCacheService.class);
    private MockMvc mvc;

    @BeforeEach
    void setup()
    {
        BizLessonController controller = new BizLessonController();
        ReflectionTestUtils.setField(controller, "lessonAutoAdvanceService", service);
        ReflectionTestUtils.setField(controller, "dashboardCacheService", cache);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        SysUser user = new SysUser();
        user.setUserId(40L); user.setUserName("teacher"); user.setDeptId(10L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new LoginUser(40L, 10L, user, Collections.emptySet()), null, Collections.emptyList()));
    }

    @AfterEach
    void logout()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void previewDoesNotInvokeWriteOrInvalidateCache() throws Exception
    {
        Map<String, Object> preview = new HashMap<>();
        preview.put("rows", Collections.singletonList(Collections.singletonMap("canAdvance", false)));
        when(service.previewAdvanceClasses("2025", Collections.singletonList("1"))).thenReturn(preview);
        mvc.perform(post("/business/lesson/advance-preview").contentType(MediaType.APPLICATION_JSON)
                .content("{\"entryYear\":\"2025\",\"classCodes\":[\"1\"]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.rows[0].canAdvance").value(false));
        verifyNoInteractions(cache);
        verify(service, never()).manualAdvanceClasses(any(), any(), any());
    }

    @Test
    void executionAcceptsSnapshotAndInvalidatesDepartmentCacheAfterPartialSuccess() throws Exception
    {
        when(service.manualAdvanceClasses(eq("2025"), eq(Collections.singletonList("1")),
                eq(Collections.singletonMap("1", 608L)))).thenReturn(result(1));
        mvc.perform(post("/business/lesson/manual-advance").contentType(MediaType.APPLICATION_JSON)
                .content("{\"entryYear\":\"2025\",\"classCodes\":[\"1\"],\"expectedLessonIds\":{\"1\":608}}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.advanced").value(1))
                .andExpect(jsonPath("$.data.details").isArray());
        verify(cache).evictDepartment(10L);
    }

    @Test
    void oldSingleClassParameterStillWorksAndNoSuccessDoesNotInvalidateCache() throws Exception
    {
        when(service.manualAdvanceClasses("2025", Collections.singletonList("1"), null)).thenReturn(result(0));
        mvc.perform(post("/business/lesson/manual-advance").contentType(MediaType.APPLICATION_JSON)
                .content("{\"entryYear\":\"2025\",\"classCode\":\"1\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.advanced").value(0));
        verifyNoInteractions(cache);
    }

    @Test
    void readAndWriteMapperResolveSameActiveAssignmentWithOnlyWriteTakingLock() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.getTypeAliasRegistry().registerAlias("BizLessonAssignment", BizLessonAssignment.class);
        String resource = "mapper/business/BizLessonAssignmentMapper.xml";
        try (java.io.InputStream stream = getClass().getClassLoader().getResourceAsStream(resource))
        {
            assertNotNull(stream);
            new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("entryYear", "2025"); params.put("classCode", "1"); params.put("deptId", 10L);
        String prefix = "com.ruoyi.business.mapper.BizLessonAssignmentMapper.";
        String read = configuration.getMappedStatement(prefix + "selectCurrentAssignment")
                .getBoundSql(params).getSql().replaceAll("\\s+", " ").trim();
        String locked = configuration.getMappedStatement(prefix + "selectCurrentAssignmentForUpdate")
                .getBoundSql(params).getSql().replaceAll("\\s+", " ").trim();
        assertEquals(read + " for update", locked);
        assertTrue(read.contains("dept_id = ?"));
        assertTrue(read.contains("ifnull(l.status, '0') = '0'"));
    }

    private Map<String, Object> result(int advanced)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("advanced", advanced); result.put("failed", 1); result.put("message", "推进结果");
        result.put("details", Collections.emptyList());
        return result;
    }
}
