package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import com.ruoyi.business.domain.BizGuideSheetUpload;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.mapper.GuideSheetUploadMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuideSheetUploadServiceTest
{
    @Mock
    private GuideSheetUploadMapper uploadMapper;
    @Mock
    private GuideSheetProgressMapper progressMapper;
    @Mock
    private GuideSheetAccessService accessService;
    @Mock
    private GuideSheetStudentViewService studentViewService;
    @Mock
    private IGuideSheetAnswerService answerService;
    @InjectMocks
    private GuideSheetUploadService service;

    @TempDir
    java.nio.file.Path tempDir;
    private String oldProfile;

    @BeforeEach
    void configureProfile()
    {
        oldProfile = RuoYiConfig.getProfile();
        new RuoYiConfig().setProfile(tempDir.toString());
    }

    @AfterEach
    void restoreProfile()
    {
        new RuoYiConfig().setProfile(oldProfile);
        Path privateRoot = Paths.get(tempDir.toString() + "-private");
        if (Files.exists(privateRoot))
        {
            try (java.util.stream.Stream<Path> paths = Files.walk(privateRoot))
            {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (Exception ignored) { }
                });
            }
            catch (Exception ignored) { }
        }
    }

    @Test
    void uploadPathAndUrlAreGeneratedByServer()
    {
        prepareAccess(true);
        MockMultipartFile file = new MockMultipartFile("file", "作品.pdf", "application/pdf",
                "%PDF-1.7 content".getBytes(StandardCharsets.UTF_8));
        org.mockito.Mockito.doAnswer(invocation -> {
            BizGuideSheetUpload upload = invocation.getArgument(0);
            upload.setUploadId(31L);
            return 1;
        }).when(uploadMapper).insertBizGuideSheetUpload(any());

        BizGuideSheetUpload result = service.upload(7L, "workFile", "upload_12345678", file);

        assertEquals(31L, result.getUploadId());
        assertTrue(result.getStoredPath().startsWith("7/9/"));
        assertEquals("/business/guide-sheet/student/uploads/7/upload_12345678",
                result.getAccessUrl());
        assertTrue(Files.isRegularFile(Paths.get(tempDir.toString() + "-private", "guide-sheet")
                .resolve(result.getStoredPath())));
        assertEquals("upload_12345678", result.getClientUploadId());
        assertNull(result.getTeacherMachineIp());
        verify(progressMapper).insertStartedIfAbsent(any());

        when(uploadMapper.selectByClientUploadId(7L, 9L, "upload_12345678")).thenReturn(result);
        GuideSheetUploadService.DownloadResource resource = service.requireStudentDownload(
                7L, "upload_12345678");
        assertTrue(Files.isRegularFile(resource.getPath()));
        assertEquals("作品.pdf", resource.getFileName());
    }

    @Test
    void fieldOutsideBindingSnapshotIsRejected()
    {
        prepareAccess(false);
        MockMultipartFile file = new MockMultipartFile("file", "作品.pdf", "application/pdf",
                "%PDF-1.7 content".getBytes(StandardCharsets.UTF_8));

        assertThrows(ServiceException.class,
                () -> service.upload(7L, "forgedField", "upload_12345678", file));

        verify(uploadMapper, never()).insertBizGuideSheetUpload(any());
        verify(progressMapper, never()).insertStartedIfAbsent(any());
    }

    @Test
    void repeatedClientUploadIdReturnsExistingRecord()
    {
        prepareAccess(true);
        BizGuideSheetUpload existing = new BizGuideSheetUpload();
        existing.setUploadId(31L);
        existing.setClientUploadId("upload_12345678");
        when(uploadMapper.selectByClientUploadId(7L, 9L, "upload_12345678")).thenReturn(existing);
        MockMultipartFile file = new MockMultipartFile("file", "作品.pdf", "application/pdf",
                "%PDF-1.7 content".getBytes(StandardCharsets.UTF_8));

        assertEquals(existing, service.upload(7L, "workFile", "upload_12345678", file));

        verify(uploadMapper, never()).insertBizGuideSheetUpload(any());
    }

    @Test
    void uploadListSerializationHidesStorageAndBindingInternals() throws Exception
    {
        BizGuideSheetUpload upload = new BizGuideSheetUpload();
        upload.setUploadId(31L);
        upload.setBindingId(7L);
        upload.setStudentId(9L);
        upload.setStoredPath("7/9/private.pdf");
        upload.setAccessUrl("/business/guide-sheet/student/uploads/7/key");
        upload.setClientUploadId("upload_12345678");
        upload.setTeacherMachineIp("127.0.0.1");
        upload.setFileName("作品.pdf");

        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(upload);

        assertTrue(json.contains("作品.pdf"));
        assertFalse(json.contains("bindingId"));
        assertFalse(json.contains("studentId"));
        assertFalse(json.contains("storedPath"));
        assertFalse(json.contains("accessUrl"));
        assertFalse(json.contains("clientUploadId"));
        assertFalse(json.contains("teacherMachineIp"));
    }

    @Test
    void legacyStoredPathCannotEscapeGuideSheetDirectory()
    {
        BizStudent student = new BizStudent();
        student.setStudentId(9L);
        BizLessonGuideSheetBinding binding = new BizLessonGuideSheetBinding();
        binding.setBindingId(7L);
        when(accessService.requireCurrentStudent()).thenReturn(student);
        when(accessService.requireStudentBinding(student, 7L)).thenReturn(binding);
        BizGuideSheetUpload upload = new BizGuideSheetUpload();
        upload.setStoredPath("/profile/upload/guide-sheet/../../secret.pdf");
        when(uploadMapper.selectByClientUploadId(7L, 9L, "upload_12345678")).thenReturn(upload);

        assertThrows(ServiceException.class,
                () -> service.requireStudentDownload(7L, "upload_12345678"));
    }

    private void prepareAccess(boolean validField)
    {
        BizStudent student = new BizStudent();
        student.setStudentId(9L);
        student.setDeptId(10L);
        student.setEntryYear("2025");
        student.setClassCode("1");
        BizLessonGuideSheetBinding binding = new BizLessonGuideSheetBinding();
        binding.setBindingId(7L);
        binding.setLessonId(3L);
        binding.setSourceSheetId(5L);
        binding.setSnapshotFormJson("{\"widgetList\":[]}");
        when(accessService.requireCurrentStudent()).thenReturn(student);
        when(accessService.requireStudentBinding(student, 7L)).thenReturn(binding);
        when(studentViewService.isUploadField(binding.getSnapshotFormJson(),
                validField ? "workFile" : "forgedField")).thenReturn(validField);
    }
}
