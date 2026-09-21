package com.ruoyi.business.service;

import java.util.*;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.dto.PaperImportRequest;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.mapper.BizLessonQuestionMapper;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherPaperSubmissionServiceTest
{
    @Test void storesRelativeProfileUploadAtAbsolutePath() throws Exception
    {
        String previous = com.ruoyi.common.config.RuoYiConfig.getProfile();
        java.nio.file.Path temp = java.nio.file.Files.createTempDirectory(java.nio.file.Paths.get("."), "paper-upload-test-");
        com.ruoyi.common.config.RuoYiConfig config = new com.ruoyi.common.config.RuoYiConfig();
        try
        {
            config.setProfile(temp.toString());
            org.springframework.web.multipart.MultipartFile upload = mock(org.springframework.web.multipart.MultipartFile.class);
            byte[] content = "%PDF-1.4 regression".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
            doAnswer(invocation -> {
                java.io.File target = invocation.getArgument(0);
                assertTrue(target.isAbsolute(), "Tomcat 上传目标必须为绝对路径");
                java.nio.file.Files.write(target.toPath(), content);
                return null;
            }).when(upload).transferTo(any(java.io.File.class));
            java.io.File stored = TeacherPaperSubmissionService.storePendingPdf(upload, "test-token");
            assertTrue(stored.toPath().startsWith(temp.toAbsolutePath().normalize()));
            assertArrayEquals(content, java.nio.file.Files.readAllBytes(stored.toPath()));
            try (org.apache.pdfbox.pdmodel.PDDocument pdf = new org.apache.pdfbox.pdmodel.PDDocument())
            {
                pdf.addPage(new org.apache.pdfbox.pdmodel.PDPage());
                pdf.save(stored);
            }
            List<String> pages = new PracticalPageRenderer().renderForOwner("test-token", "PDF", stored);
            assertEquals(1, pages.size());
            assertTrue(pages.get(0).startsWith("/profile/upload/student-answer-artifact/paper-pending/test-token/"));
        }
        finally
        {
            config.setProfile(previous);
            try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(temp))
            {
                for (java.nio.file.Path path : (Iterable<java.nio.file.Path>) paths.sorted(Comparator.reverseOrder())::iterator)
                    java.nio.file.Files.deleteIfExists(path);
            }
        }
    }
    private BizStudent student(long id, String no, String name)
    {
        BizStudent s = new BizStudent(); s.setStudentId(id); s.setStudentNo(no); s.setStudentName(name); return s;
    }
    private Map<String, Object> identity(String no, String name, double confidence)
    {
        Map<String, Object> value = new HashMap<>(); value.put("studentNo", no); value.put("name", name); value.put("confidence", confidence); return value;
    }
    @Test void matchesOnlyUniqueNumberAndNameWithHighConfidence()
    {
        List<BizStudent> roster = Arrays.asList(student(1, "7", "张三"), student(2, "8", "李四"));
        assertEquals(Long.valueOf(1), TeacherPaperSubmissionService.matchStudent(roster, identity("007", "张三", .99)));
        assertNull(TeacherPaperSubmissionService.matchStudent(roster, identity("7", "李四", .99)));
        assertNull(TeacherPaperSubmissionService.matchStudent(roster, identity("9", "张三", .99)));
        assertNull(TeacherPaperSubmissionService.matchStudent(roster, identity("7", "张三", .89)));
        assertNull(TeacherPaperSubmissionService.matchStudent(roster, identity("7", "张三", Double.NaN)));
        assertNull(TeacherPaperSubmissionService.matchStudent(roster, identity("7", "", 1)));
        assertNull(TeacherPaperSubmissionService.matchStudent(Arrays.asList(student(1,"7","张三"),student(2,"07","张三")), identity("7","张三",1)));
    }
    @Test void rejectsOutOfScopeBeforeReadingQuestions()
    {
        TeacherPaperSubmissionService service = new TeacherPaperSubmissionService();
        GuideSheetAccessService access = mock(GuideSheetAccessService.class);
        BizLessonQuestionMapper questions = mock(BizLessonQuestionMapper.class);
        ReflectionTestUtils.setField(service, "access", access); ReflectionTestUtils.setField(service, "questions", questions);
        PaperImportRequest request = new PaperImportRequest(); request.lessonId = 1L; request.questionId = 2L; request.entryYear = "2024"; request.classCode = "3";
        when(access.requireViewableLessonClassDept(1L,"2024","3")).thenThrow(new ServiceException("无权访问"));
        assertThrows(ServiceException.class, () -> service.requireQuestion(request)); verifyNoInteractions(questions);
    }
    @Test void rejectsOtherQuestionAndNonFileModes()
    {
        TeacherPaperSubmissionService service = new TeacherPaperSubmissionService();
        GuideSheetAccessService access = mock(GuideSheetAccessService.class);
        BizLessonQuestionMapper questions = mock(BizLessonQuestionMapper.class);
        ReflectionTestUtils.setField(service, "access", access); ReflectionTestUtils.setField(service, "questions", questions);
        PaperImportRequest request = new PaperImportRequest(); request.lessonId = 1L; request.questionId = 2L;
        BizLessonQuestionDetailVo q = new BizLessonQuestionDetailVo(); q.setQuestionId(2L); q.setQuestionType("practical"); q.setPracticalMode("PYTHON");
        when(questions.selectDetailsByLessonId(1L)).thenReturn(Collections.singletonList(q));
        assertThrows(ServiceException.class, () -> service.requireQuestion(request));
        q.setPracticalMode("FILE"); assertSame(q, service.requireQuestion(request));
        q.setQuestionId(3L); assertThrows(ServiceException.class, () -> service.requireQuestion(request));
    }
}
