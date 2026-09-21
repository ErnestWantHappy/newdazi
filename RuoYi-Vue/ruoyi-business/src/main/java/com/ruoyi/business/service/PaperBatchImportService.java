package com.ruoyi.business.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.dto.PaperImportRequest;
import com.ruoyi.business.domain.dto.PaperImportRequest.Segment;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 整班原件只有在完整页段确认后才能生成单人导入凭证。 */
@Service
public class PaperBatchImportService
{
    private static final String PREFIX = "teacher:class-paper:";
    @Autowired private TeacherPaperSubmissionService submissions;
    @Autowired private PracticalFilePolicyService policy;
    @Autowired private PaperScanVisionGateway vision;
    @Autowired private RedisCache cache;
    @Autowired private ObjectMapper json;

    public static class Pending extends TeacherPaperSubmissionService.Pending { public int pageCount; }

    public Map<String, Object> upload(PaperImportRequest request, MultipartFile file) throws Exception
    {
        submissions.requireQuestion(request); policy.inspect(file, "pdf");
        String token = UUID.randomUUID().toString().replace("-", "");
        File original = TeacherPaperSubmissionService.storePendingPdf(file, token);
        Pending pending = new Pending(); pending.teacherId = SecurityUtils.getUserId(); pending.scope = request;
        pending.path = original.getAbsolutePath(); pending.name = file.getOriginalFilename();
        try (PDDocument document = PDDocument.load(original))
        {
            pending.pageCount = document.getNumberOfPages();
            if (document.isEncrypted() || pending.pageCount < 1 || pending.pageCount > 500)
                throw new ServiceException("整班PDF须为未加密的1至500页文件");
        }
        cache.setCacheObject(PREFIX + token, json.writeValueAsString(pending), 24, TimeUnit.HOURS);
        return PracticalInsightEngine.map("batchToken", token, "pageCount", pending.pageCount, "fileName", pending.name);
    }

    private Pending owned(PaperImportRequest request) throws Exception
    {
        submissions.requireQuestion(request);
        String raw = cache.getCacheObject(PREFIX + request.token);
        if (raw == null) throw new ServiceException("整班原件已过期，请重新上传");
        Pending p = json.readValue(raw, Pending.class);
        if (!Objects.equals(p.teacherId, SecurityUtils.getUserId()) || !Objects.equals(p.scope.lessonId, request.lessonId)
                || !Objects.equals(p.scope.questionId, request.questionId) || !Objects.equals(p.scope.entryYear, request.entryYear)
                || !Objects.equals(p.scope.classCode, request.classCode)) throw new ServiceException("原件不属于当前教师或班级");
        return p;
    }

    public Map<String, Object> identity(PaperImportRequest request) throws Exception
    {
        Pending pending = owned(request);
        if (request.page == null || request.page < 1 || request.page > pending.pageCount) throw new ServiceException("页码越界");
        String key = PREFIX + request.token + ":page:" + request.page;
        String previous = cache.getCacheObject(key);
        if (previous != null) return json.readValue(previous, Map.class);
        vision.assertConfigured();
        File image = new File(new File(pending.path).getParentFile(), "identity-" + request.page + ".jpg");
        try (PDDocument document = PDDocument.load(new File(pending.path)))
        { ImageIO.write(new PDFRenderer(document).renderImageWithDPI(request.page - 1, 100, ImageType.RGB), "jpg", image); }
        List<BizStudent> roster = submissions.roster(request); List<Object> candidates = new ArrayList<>();
        for (BizStudent s : roster) candidates.add(PracticalInsightEngine.map("studentNo", s.getStudentNo(), "name", s.getStudentName()));
        Map<String, Object> identity = vision.recognizeIdentity(Collections.singletonList(image), json.writeValueAsString(candidates));
        Map<String, Object> result = PracticalInsightEngine.map("page", request.page, "identity", identity,
                "studentId", TeacherPaperSubmissionService.matchStudent(roster, identity));
        cache.setCacheObject(key, json.writeValueAsString(result), 24, TimeUnit.HOURS); return result;
    }

    static void validateSegments(List<Segment> segments, int pages, Set<Long> roster)
    {
        if (segments == null || segments.isEmpty()) throw new ServiceException("请确认全部学生页段");
        int next = 1; Set<Long> students = new HashSet<>();
        for (Segment s : segments)
        {
            if (s == null || !s.confirmed || s.startPage != next || s.endPage < s.startPage || s.endPage > pages
                    || s.endPage - s.startPage >= 50 || !roster.contains(s.studentId) || !students.add(s.studentId))
                throw new ServiceException("页段须逐段确认、连续且不重复，单人最多50页，学生须属于本班且不能重复");
            next = s.endPage + 1;
        }
        if (next != pages + 1) throw new ServiceException("仍有页面未分配，不能遗漏原件页面");
    }

    public synchronized List<Map<String, Object>> split(PaperImportRequest request) throws Exception
    {
        Pending parent = owned(request); Set<Long> roster = new HashSet<>();
        submissions.roster(request).forEach(s -> roster.add(s.getStudentId()));
        validateSegments(request.segments, parent.pageCount, roster);
        String plan = DigestUtils.sha256Hex(json.writeValueAsBytes(request.segments));
        String planKey = PREFIX + request.token + ":plan";
        cache.redisTemplate.opsForValue().setIfAbsent(planKey, plan, 24, TimeUnit.HOURS);
        if (!plan.equals(cache.getCacheObject(planKey))) throw new ServiceException("此原件已确认其他页段方案，更正请重新上传");
        List<Map<String, Object>> result = new ArrayList<>();
        try (PDDocument document = PDDocument.load(new File(parent.path)))
        {
            for (Segment segment : request.segments)
            {
                // 凭证由原件和已确认方案确定，网络重试不会产生重叠的新提交。
                String token = DigestUtils.md5Hex(request.token + ":" + plan + ":" + segment.startPage);
                Path directory = new File(parent.path).getParentFile().getParentFile().toPath().resolve(token);
                Files.createDirectories(directory); File target = directory.resolve("original.pdf").toFile();
                if (!target.exists()) try (PDDocument part = new PDDocument())
                {
                    for (int page = segment.startPage; page <= segment.endPage; page++) part.importPage(document.getPage(page - 1));
                    Path temporary = directory.resolve("writing.pdf"); part.save(temporary.toFile());
                    Files.move(temporary, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                TeacherPaperSubmissionService.Pending child = new TeacherPaperSubmissionService.Pending();
                child.teacherId = parent.teacherId; child.scope = parent.scope; child.path = target.getAbsolutePath();
                child.name = "pages-" + segment.startPage + "-" + segment.endPage + ".pdf";
                try (InputStream stream = new FileInputStream(target)) { child.sha256 = DigestUtils.sha256Hex(stream); }
                child.plannedStudentId = segment.studentId;
                cache.setCacheObject("teacher:paper-import:" + token, json.writeValueAsString(child), 24, TimeUnit.HOURS);
                result.add(PracticalInsightEngine.map("token", token, "studentId", segment.studentId, "startPage", segment.startPage,
                        "endPage", segment.endPage, "pageCount", segment.endPage - segment.startPage + 1, "fileName", child.name));
            }
        }
        return result;
    }
}
