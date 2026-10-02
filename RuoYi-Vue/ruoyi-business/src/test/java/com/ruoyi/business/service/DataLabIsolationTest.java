package com.ruoyi.business.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import com.ruoyi.business.mapper.DataLabMapper;
import com.ruoyi.business.service.impl.DataLabServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataLabIsolationTest {
    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class Config {
        @Bean DataLabMapper mapper() { return mock(DataLabMapper.class); }
        @Bean DataLabAccessService access() { return mock(DataLabAccessService.class); }
        @Bean com.ruoyi.business.mapper.BizQuestionMapper questions() { return mock(com.ruoyi.business.mapper.BizQuestionMapper.class); }
        @Bean com.ruoyi.business.mapper.BizStudentMapper students() { return mock(com.ruoyi.business.mapper.BizStudentMapper.class); }
        @Bean com.ruoyi.business.mapper.PythonPracticeMapper practice() { return mock(com.ruoyi.business.mapper.PythonPracticeMapper.class); }
        @Bean IDataLabService service() { return new DataLabServiceImpl(); }
    }

    @Test
    void anonymousEntryIsDeniedBeforeDatabaseOrFileAccess() throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class)) {
            SecurityContextHolder.getContext().setAuthentication(new org.springframework.security.authentication.AnonymousAuthenticationToken("test", "anonymous", Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
            IDataLabService service = context.getBean(IDataLabService.class);
            for (Method method : IDataLabService.class.getMethods()) {
                InvocationTargetException error = assertThrows(InvocationTargetException.class,
                        () -> method.invoke(service, new Object[method.getParameterCount()]), method.getName());
                assertTrue(error.getCause() instanceof AccessDeniedException, method.getName());
            }
            verifyNoInteractions(context.getBean(DataLabMapper.class));
        } finally { SecurityContextHolder.clearContext(); }
    }
}
