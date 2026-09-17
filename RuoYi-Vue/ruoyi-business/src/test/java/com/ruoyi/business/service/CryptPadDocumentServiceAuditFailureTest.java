package com.ruoyi.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.business.domain.CollaborationRoom;
import com.ruoyi.business.mapper.CollaborationMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;

class CryptPadDocumentServiceAuditFailureTest
{
    @TempDir Path storage;

    @BeforeEach
    void setUp()
    {
        SysUser user = new SysUser();
        user.setUserId(5551L);
        user.setDeptId(169L);
        LoginUser loginUser = new LoginUser(5551L, 169L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditExtractionFailureDoesNotRejectSavedRevision() throws Exception
    {
        CryptPadDocumentService service = new CryptPadDocumentService();
        CollaborationRoomService roomService = mock(CollaborationRoomService.class);
        CollaborationMapper mapper = mock(CollaborationMapper.class);
        CollaborationRevisionDiffService diffService = mock(CollaborationRevisionDiffService.class);
        CollaborationRoom room = room();
        ReflectionTestUtils.setField(service, "roomService", roomService);
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "revisionDiffService", diffService);
        ReflectionTestUtils.setField(service, "provider", "MOCK");
        ReflectionTestUtils.setField(service, "maxFileBytes", 1024 * 1024L);
        when(roomService.requireRoom(9L)).thenReturn(room);
        when(roomService.resolveStoredFile(any(String.class))).thenAnswer(invocation ->
                storage.resolve(invocation.getArgument(0, String.class)).normalize());
        when(mapper.commitRoomVersion(anyLong(), anyInt(), anyInt(), any(String.class), any(String.class),
                any(String.class), any(String.class), anyLong(), any(String.class), anyLong(), any(java.util.Date.class)))
                .thenAnswer(invocation -> { room.setCurrentVersion(invocation.getArgument(2)); return 1; });
        doThrow(new RuntimeException("差异队列不可用")).when(diffService).pending(9L, 2);
        MockMultipartFile file = new MockMultipartFile("file", "小组文档.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", new byte[] { 1, 2, 3 });

        Map<String, Object> saved = service.save(9L, file, 1);

        assertEquals(2, saved.get("version"));
    }

    private CollaborationRoom room()
    {
        CollaborationRoom room = new CollaborationRoom();
        room.setRoomId(9L);
        room.setProvider("MOCK");
        room.setPublicFileId("group-room");
        room.setStatus("OPEN");
        room.setCurrentVersion(1);
        room.setCurrentFileName("小组文档.docx");
        room.setCurrentFileExtension("docx");
        return room;
    }
}
