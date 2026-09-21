package com.ruoyi.business.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.*;
import com.ruoyi.business.domain.dto.*;
import com.ruoyi.business.domain.vo.*;
import com.ruoyi.business.mapper.*;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** 纸质 PDF 只提供身份识别和代交，评分仍走原作品及 AI 链路。 */
@Service
public class TeacherPaperSubmissionService
{
    private static final String PREFIX = "teacher:paper-import:";
    @Autowired private GuideSheetAccessService access;
    @Autowired private BizLessonQuestionMapper questions;
    @Autowired private BizStudentMapper students;
    @Autowired private BizStudentAnswerMapper answers;
    @Autowired private PracticalArtifactService artifacts;
    @Autowired private PracticalFilePolicyService policy;
    @Autowired private PracticalPageRenderer renderer;
    @Autowired private PaperScanVisionGateway vision;
    @Autowired private ClassroomTaskStateService states;
    @Autowired private PracticalGradingDeadlineService deadlines;
    @Autowired private RedisCache cache;
    @Autowired private ObjectMapper json;

    public Long requireScope(PaperImportRequest request)
    {
        return access.requireViewableLessonClassDept(request.lessonId, request.entryYear, request.classCode);
    }

    public BizLessonQuestionDetailVo requireQuestion(PaperImportRequest request)
    {
        requireScope(request);
        for (BizLessonQuestionDetailVo question : questions.selectDetailsByLessonId(request.lessonId))
        {
            if (Objects.equals(request.questionId, question.getQuestionId())
                    && "practical".equalsIgnoreCase(question.getQuestionType())
                    && (question.getPracticalMode() == null || question.getPracticalMode().trim().isEmpty()
                        || "FILE".equalsIgnoreCase(question.getPracticalMode())))
                return question;
        }
        throw new ServiceException("请选择当前课程的文件作品操作题");
    }

    public List<BizStudent> roster(PaperImportRequest request)
    {
        Long deptId = requireScope(request);
        return students.selectPaperImportRoster(deptId, request.entryYear, request.classCode);
    }

    public Map<String, Object> upload(PaperImportRequest request, MultipartFile file) throws Exception
    {
        requireQuestion(request);
        vision.assertConfigured();
        PracticalUploadTicket inspected = policy.inspect(file, "pdf");
        String token = UUID.randomUUID().toString().replace("-", "");
        File original = storePendingPdf(file, token);
        List<String> pages = renderer.renderForOwner(token, "PDF", original);
        Pending pending = new Pending();
        pending.teacherId = SecurityUtils.getUserId();
        pending.scope = request;
        pending.path = original.getAbsolutePath();
        pending.name = inspected.getOriginalFileName();
        pending.sha256 = inspected.getSha256();
        cache.setCacheObject(PREFIX + token, json.writeValueAsString(pending), 24, TimeUnit.HOURS);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("fileName", pending.name);
        result.put("pageCount", pages.size());
        try
        {
            // 一人一份 PDF，只识别首页身份栏；完整原件交给原作品转换与评分。
            File firstPage = new File(RuoYiConfig.getProfile() + pages.get(0).substring("/profile".length()));
            List<BizStudent> classRoster = roster(request);
            List<Map<String, String>> candidates = new ArrayList<>();
            for (BizStudent student : classRoster)
            {
                Map<String, String> candidate = new LinkedHashMap<>();
                candidate.put("studentNo", student.getStudentNo());
                candidate.put("name", student.getStudentName());
                candidates.add(candidate);
            }
            Map<String, Object> identity = vision.recognizeIdentity(Collections.singletonList(firstPage), json.writeValueAsString(candidates));
            result.put("identity", identity);
            result.put("studentId", matchStudent(classRoster, identity));
        }
        catch (Exception e)
        {
            result.put("message", "身份识别未完成，请选择本班学生后导入");
        }
        return result;
    }

    static File storePendingPdf(MultipartFile file, String token) throws IOException
    {
        // Tomcat 的 Part.write 会相对临时目录解析相对路径，必须传入实际上传目录的绝对路径。
        Path directory = new File(RuoYiConfig.getUploadPath(), "student-answer-artifact/paper-pending/" + token)
                .toPath().toAbsolutePath().normalize();
        Files.createDirectories(directory);
        File original = directory.resolve("original.pdf").toFile();
        file.transferTo(original);
        return original;
    }

    static Long matchStudent(List<BizStudent> roster, Map<String, Object> identity)
    {
        double confidence;
        try { confidence = Double.parseDouble(String.valueOf(identity.get("confidence"))); }
        catch (Exception e) { return null; }
        if (!Double.isFinite(confidence) || confidence < 0.90 || confidence > 1) return null;
        String no = normalizeNo(identity.get("studentNo"));
        String name = Objects.toString(identity.get("name"), "").trim();
        if (no.isEmpty() || name.isEmpty()) return null;
        Long match = null;
        for (BizStudent student : roster)
        {
            if (no.equals(normalizeNo(student.getStudentNo())))
            {
                if (match != null || !name.equals(student.getStudentName())) return null;
                match = student.getStudentId();
            }
        }
        return match;
    }

    private static String normalizeNo(Object value)
    {
        return Objects.toString(value, "").trim().replaceFirst("^0+(?!$)", "");
    }

    /** 暂存原件不属于正式作品，超过凭证有效期后仅清理本服务专用目录。 */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 3600000L, initialDelay = 3600000L)
    public void cleanExpiredPendingFiles() throws IOException
    {
        Path root = new File(RuoYiConfig.getUploadPath(), "student-answer-artifact/paper-pending").toPath().toAbsolutePath().normalize();
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) return;
        long cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(25);
        try (java.util.stream.Stream<Path> directories = Files.list(root))
        {
            for (Path directory : (Iterable<Path>) directories::iterator)
            {
                if (!directory.getFileName().toString().matches("[a-f0-9]{32}")
                        || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)
                        || Files.getLastModifiedTime(directory).toMillis() >= cutoff) continue;
                try (java.util.stream.Stream<Path> paths = Files.walk(directory))
                {
                    for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator)
                    {
                        if (!path.toAbsolutePath().normalize().startsWith(root)) throw new IOException("暂存路径越界");
                        Files.deleteIfExists(path);
                    }
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public PracticalArtifactVo submit(PaperImportRequest request) throws Exception
    {
        BizLessonQuestionDetailVo originalQuestion = requireQuestion(request);
        String raw = cache.getCacheObject(PREFIX + request.token);
        if (raw == null) throw new ServiceException("导入文件已过期，请重新上传");
        Pending pending = json.readValue(raw, Pending.class);
        if (pending.plannedStudentId != null && !Objects.equals(pending.plannedStudentId, request.studentId))
            throw new ServiceException("该页段已确认给指定学生，更正请重新上传原件");
        if (!Objects.equals(pending.teacherId, SecurityUtils.getUserId())
                || !Objects.equals(pending.scope.lessonId, request.lessonId)
                || !Objects.equals(pending.scope.questionId, request.questionId)
                || !Objects.equals(pending.scope.entryYear, request.entryYear)
                || !Objects.equals(pending.scope.classCode, request.classCode))
            throw new ServiceException("导入文件不属于当前教师、课程或班级");
        BizStudent student = roster(request).stream()
                .filter(s -> Objects.equals(s.getStudentId(), request.studentId)).findFirst()
                .orElseThrow(() -> new ServiceException("只能选择当前班级学生"));
        PracticalArtifactVo current = artifacts.getCurrentLessonArtifact(student.getStudentId(), request.lessonId, request.questionId);
        // 同一凭证首次确认后固定学生，防止网络重试或并发请求把一份原件交给不同学生。
        String bindingKey = PREFIX + request.token + ":student";
        cache.redisTemplate.opsForValue().setIfAbsent(bindingKey, request.studentId.toString(), 24, TimeUnit.HOURS);
        if (!request.studentId.toString().equals(cache.getCacheObject(bindingKey)))
            throw new ServiceException("此文件已确认给另一名学生，如需更正请重新上传");
        if (current != null && current.getAttachments() != null)
            for (PracticalAttachment attachment : current.getAttachments())
                if (Objects.equals(pending.sha256, attachment.getSha256())) return current;
        BizStudentAnswer existing = answers.selectLatestByStudentLessonQuestion(student.getStudentId(), request.lessonId, request.questionId);
        if (existing != null && (existing.getScore() != null || (existing.getStudentAnswer() != null && !existing.getStudentAnswer().isEmpty())))
        {
            if (!request.replace || current == null || !Objects.equals(current.getVersionId(), request.expectedVersionId))
                throw new ServiceException("该学生已有作品或成绩，请刷新后明确确认替换当前版本");
            deadlines.assertCanGrade(existing.getAnswerId());
        }
        // 仅教师扫描适配允许 PDF，不修改题库配置和学生端的格式限制。
        BizLessonQuestionDetailVo paperQuestion = new BizLessonQuestionDetailVo();
        BeanUtils.copyProperties(originalQuestion, paperQuestion);
        paperQuestion.setPracticalAllowedExtensions("pdf");
        PracticalUploadTicket staged = artifacts.stageTeacherPaperFile(student.getStudentId(), request.lessonId,
                paperQuestion, new StoredPdf(new File(pending.path), pending.name));
        PracticalArtifactVo result = artifacts.submitTeacherPaperArtifact(student.getStudentId(), SecurityUtils.getUserId(),
                paperQuestion, request.lessonId, request.expectedVersionId, Collections.singletonList(staged.getToken()),
                request.replace, request.expectedScore);
        states.markSafely(student, student.getDeptId(), request.lessonId, request.questionId, ClassroomTaskStateService.SUBMITTED);
        deadlines.checkAndCreateDeadline(request.lessonId, student.getDeptId(), request.entryYear, request.classCode);
        return result;
    }

    public static class Pending
    {
        public Long teacherId;
        public PaperImportRequest scope;
        public String path;
        public String name;
        public String sha256;
        public Long plannedStudentId;
    }

    private static class StoredPdf implements MultipartFile
    {
        private final File file;
        private final String name;
        StoredPdf(File file, String name) { this.file = file; this.name = name; }
        public String getName() { return "file"; }
        public String getOriginalFilename() { return name; }
        public String getContentType() { return "application/pdf"; }
        public boolean isEmpty() { return file.length() == 0; }
        public long getSize() { return file.length(); }
        public byte[] getBytes() throws IOException { return Files.readAllBytes(file.toPath()); }
        public InputStream getInputStream() throws IOException { return new FileInputStream(file); }
        public void transferTo(File dest) throws IOException { Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING); }
    }
}
