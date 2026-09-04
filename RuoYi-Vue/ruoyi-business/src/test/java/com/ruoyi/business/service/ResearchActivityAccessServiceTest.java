package com.ruoyi.business.service;

import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruoyi.business.domain.BizResearchPost;
import com.ruoyi.business.domain.BizResearchTopic;
import com.ruoyi.business.domain.vo.ResearchResourceVo;
import com.ruoyi.business.mapper.ResearchActivityMapper;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResearchActivityAccessServiceTest
{
    @Mock private ResearchActivityMapper mapper;
    @InjectMocks private ResearchActivityAccessService service;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void teacherResearcherAndAdminCanReadButStudentAndAnonymousCannot()
    {
        login(20L, "teacher");
        assertDoesNotThrow(service::requireReadableRole);
        login(21L, "researcher");
        assertDoesNotThrow(service::requireReadableRole);
        login(1L, "admin");
        assertDoesNotThrow(service::requireReadableRole);
        login(22L, "student");
        assertEquals(403, assertThrows(ServiceException.class, service::requireReadableRole).getCode());
        SecurityContextHolder.clearContext();
        assertThrows(ServiceException.class, service::requireReadableRole);
    }

    @Test
    void onlyResearcherOrAdminMayManage()
    {
        login(20L, "teacher");
        assertEquals(403, assertThrows(ServiceException.class, service::requireManager).getCode());
        login(21L, "researcher");
        assertDoesNotThrow(service::requireManager);
        login(1L, "admin");
        assertDoesNotThrow(service::requireManager);
    }

    @Test
    void managerStillCannotEditAnotherAuthorsReflection()
    {
        BizResearchPost post = new BizResearchPost();
        post.setAuthorId(50L);

        login(21L, "researcher");
        assertEquals(403, assertThrows(ServiceException.class,
                () -> service.requirePostAuthor(post)).getCode());
        login(1L, "admin");
        assertEquals(403, assertThrows(ServiceException.class,
                () -> service.requirePostAuthor(post)).getCode());
        login(50L, "teacher");
        assertDoesNotThrow(() -> service.requirePostAuthor(post));
    }

    @Test
    void softDeletedParentMakesPostAndResourceUnavailable()
    {
        login(20L, "teacher");
        BizResearchTopic topic = new BizResearchTopic();
        topic.setTopicId(9L);
        topic.setDelFlag("2");
        BizResearchPost post = new BizResearchPost();
        post.setPostId(30L);
        post.setTopicId(9L);
        post.setDelFlag("0");
        ResearchResourceVo resource = new ResearchResourceVo();
        resource.setResourceId(40L);
        resource.setPostId(30L);
        resource.setDelFlag("0");
        when(mapper.selectTopicByIdAny(9L)).thenReturn(topic);
        when(mapper.selectPostByIdAny(30L)).thenReturn(post);
        when(mapper.selectResourceById(40L)).thenReturn(resource);

        assertThrows(ServiceException.class, () -> service.requireActivePost(30L));
        assertThrows(ServiceException.class, () -> service.requireActiveResource(40L));
    }

    private void login(Long userId, String roleKey)
    {
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setDeptId(10L);
        user.setRoles(Collections.singletonList(role));
        LoginUser loginUser = new LoginUser(userId, 10L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }
}
