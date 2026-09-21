package com.ruoyi.business.service;

import java.util.*;
import java.nio.file.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.dto.PaperImportRequest;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import com.ruoyi.business.domain.dto.PaperImportRequest.Segment;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaperBatchImportServiceTest
{
    @TempDir Path directory;

    @Test void splitsRealSeventyTwoPagePdfWithoutLosingPagesAndReusesTokens() throws Exception
    {
        Path parent = Files.createDirectories(directory.resolve("parent")).resolve("original.pdf");
        try (PDDocument pdf = new PDDocument()) {
            for(int i=1;i<=72;i++) {
                PDPage page = new PDPage(); pdf.addPage(page);
                try(PDPageContentStream content = new PDPageContentStream(pdf,page)) {
                    content.beginText(); content.setFont(PDType1Font.HELVETICA,12); content.newLineAtOffset(50,700);
                    content.showText("PAGE-"+i+"-END"); content.endText();
                }
            }
            pdf.save(parent.toFile());
        }
        ObjectMapper json = new ObjectMapper(); PaperBatchImportService service = new PaperBatchImportService();
        TeacherPaperSubmissionService submissions = mock(TeacherPaperSubmissionService.class);
        RedisCache cache = mock(RedisCache.class); cache.redisTemplate = mock(RedisTemplate.class);
        ValueOperations ops = mock(ValueOperations.class); when(cache.redisTemplate.opsForValue()).thenReturn(ops);
        Map<String,Object> stored = new HashMap<>();
        when(cache.getCacheObject(anyString())).thenAnswer(a -> stored.get(a.getArgument(0)));
        doAnswer(a -> { stored.put(a.getArgument(0),a.getArgument(1)); return null; }).when(cache).setCacheObject(anyString(),any(),anyInt(),any());
        when(ops.setIfAbsent(any(),any(),anyLong(),any())).thenAnswer(a -> stored.putIfAbsent((String)a.getArgument(0),a.getArgument(1))==null);
        ReflectionTestUtils.setField(service,"submissions",submissions); ReflectionTestUtils.setField(service,"cache",cache); ReflectionTestUtils.setField(service,"json",json);
        PaperImportRequest request = new PaperImportRequest(); request.token="parent"; request.lessonId=1L; request.questionId=2L; request.entryYear="2024"; request.classCode="12";
        request.segments=new ArrayList<>(); List<BizStudent> roster=new ArrayList<>();
        for(long i=1;i<=36;i++) { request.segments.add(segment((int)i*2-1,(int)i*2,i)); BizStudent s=new BizStudent(); s.setStudentId(i); roster.add(s); }
        when(submissions.roster(request)).thenReturn(roster);
        PaperBatchImportService.Pending pending=new PaperBatchImportService.Pending(); pending.teacherId=7L; pending.scope=request; pending.path=parent.toString(); pending.pageCount=72;
        stored.put("teacher:class-paper:parent",json.writeValueAsString(pending));
        LoginUser user=new LoginUser(); user.setUserId(7L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,Collections.emptyList()));
        try {
            List<Map<String,Object>> first=service.split(request); assertEquals(36,first.size());
            assertEquals(first,service.split(request));
            for(int i=0;i<36;i++) {
                TeacherPaperSubmissionService.Pending child=json.readValue((String)stored.get("teacher:paper-import:"+first.get(i).get("token")),TeacherPaperSubmissionService.Pending.class);
                assertEquals(Long.valueOf(i+1),child.plannedStudentId);
                try(PDDocument pdf=PDDocument.load(new java.io.File(child.path))) {
                    assertEquals(2,pdf.getNumberOfPages());
                    assertEquals("PAGE-"+(i*2+1)+"-END\nPAGE-"+(i*2+2)+"-END",new PDFTextStripper().getText(pdf).trim().replace("\r", ""));
                }
            }
            user.setUserId(8L); assertThrows(ServiceException.class,()->service.split(request)); user.setUserId(7L);
            request.segments.get(0).studentId=2L; request.segments.get(1).studentId=1L;
            assertThrows(ServiceException.class,()->service.split(request));
        } finally { SecurityContextHolder.clearContext(); }
    }
    private Segment segment(int start, int end, long student)
    { Segment s = new Segment(); s.startPage = start; s.endPage = end; s.studentId = student; s.confirmed = true; return s; }

    @Test void acceptsWholeClassSeventyTwoPages()
    {
        List<Segment> plan = new ArrayList<>(); Set<Long> roster = new HashSet<>();
        for (long i = 1; i <= 36; i++) { roster.add(i); plan.add(segment((int) i * 2 - 1, (int) i * 2, i)); }
        assertDoesNotThrow(() -> PaperBatchImportService.validateSegments(plan, 72, roster));
    }
    @Test void rejectsGapsOverlapsOmissionsDuplicateStudentsAndUnconfirmedPlan()
    {
        Set<Long> roster = new HashSet<>(Arrays.asList(1L, 2L));
        for (List<Segment> plan : Arrays.asList(Arrays.asList(segment(1, 2, 1), segment(4, 4, 2)),
                Arrays.asList(segment(1, 2, 1), segment(2, 4, 2)), Arrays.asList(segment(1, 3, 1)),
                Arrays.asList(segment(1, 2, 1), segment(3, 4, 1)), Arrays.asList(segment(1, 4, 99))))
            assertThrows(ServiceException.class, () -> PaperBatchImportService.validateSegments(plan, 4, roster));
        Segment unconfirmed = segment(1, 4, 1); unconfirmed.confirmed = false;
        assertThrows(ServiceException.class, () -> PaperBatchImportService.validateSegments(Arrays.asList(unconfirmed), 4, roster));
    }
}
