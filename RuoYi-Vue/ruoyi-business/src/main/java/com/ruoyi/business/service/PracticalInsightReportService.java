package com.ruoyi.business.service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import com.ruoyi.business.mapper.PracticalInsightMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class PracticalInsightReportService
{
    @Autowired private PracticalInsightMapper mapper;
    @Autowired private PracticalInsightInputService inputs;
    @Autowired private PracticalInsightWorker worker;
    @Autowired private TeacherPaperSubmissionService scope;
    @Autowired private ObjectMapper json;
    @Autowired private com.ruoyi.business.mapper.StudentFeedbackMapper feedbackMapper;
    @Value("${practical-insight.enabled:false}") private boolean enabled;
    @Value("${practical-insight.max-cost-yuan:20}") private BigDecimal maxCostYuan;

    public Map<String, Object> latest(Request request) throws Exception
    {
        scope.requireQuestion(request);
        if (!enabled) return PracticalInsightEngine.map("enabled", false, "message", "班级诊断V3尚未启用，请先完成诊断表迁移和配置");
        recoverStale(SecurityUtils.getUserId());
        Job job = mapper.latest(SecurityUtils.getUserId(), request);
        return job == null ? PracticalInsightEngine.map("enabled", true, "job", null) : view(job);
    }
    public synchronized Map<String, Object> create(Request request) throws Exception
    {
        requireEnabled();
        Input input = inputs.build(request);
        if (input.students.isEmpty()) throw new ServiceException("当前没有正式确认成绩，可先完成批改后生成");
        recoverStale(input.teacherId);
        Job job = mapper.find(input.teacherId, input.fingerprint);
        if (job != null) return view(job);
        if (mapper.active(input.teacherId) > 0) throw new ServiceException("已有诊断运行中，请等待完成或取消后再生成");
        job = new Job(); job.teacherId = input.teacherId; job.deptId = input.deptId; job.lessonId = request.lessonId;
        job.questionId = request.questionId; job.entryYear = request.entryYear; job.classCode = request.classCode;
        job.fingerprint = input.fingerprint; job.inputJson = json.writeValueAsString(input);
        try { mapper.insert(job); }
        catch (DuplicateKeyException e) { return view(mapper.find(input.teacherId, input.fingerprint)); }
        try { worker.run(job.reportId); }
        catch (RuntimeException e) { mapper.cancel(job.reportId); throw new ServiceException("诊断队列繁忙，请稍后重试"); }
        return view(mapper.get(job.reportId));
    }
    public Map<String, Object> get(Long id) throws Exception { return view(owned(id)); }
    public Map<String, Object> cancel(Long id) throws Exception
    { owned(id); mapper.cancel(id); return view(mapper.get(id)); }
    public synchronized Map<String, Object> retry(Long id) throws Exception
    {
        Job job = owned(id); Input input = json.readValue(job.inputJson, Input.class);
        if (!input.fingerprint.equals(inputs.build(input.scope).fingerprint)) throw new ServiceException("输入已变化，请重新生成报告");
        if (mapper.active(input.teacherId) > 0) throw new ServiceException("已有诊断运行中，请稍后重试");
        if (mapper.retry(id) > 0)
        {
            try { worker.run(id); }
            catch (RuntimeException e) { mapper.cancel(id); throw new ServiceException("诊断队列繁忙，请稍后重试"); }
        }
        return view(mapper.get(id));
    }
    public Map<String, Object> student(Long id, Long studentId) throws Exception
    {
        Job job = owned(id); Input input = json.readValue(job.inputJson, Input.class);
        Student student = input.students.stream().filter(s -> s.studentId.equals(studentId)).findFirst().orElseThrow(() -> new ServiceException("学生不在本报告范围"));
        EvidenceRow row = mapper.evidence(id).stream().filter(e -> e.studentId.equals(studentId)).findFirst().orElse(null);
        return PracticalInsightEngine.map("student", PracticalInsightEngine.map("studentId", student.studentId, "name", student.name,
                "score", student.score, "maxScore", student.maxScore, "pageCount", student.pages.size(), "scoringDetails", student.scoringDetails),
                "result", row == null || row.resultJson == null ? null : json.readValue(row.resultJson, Result.class));
    }
    public File page(Long id, Long studentId, int page) throws Exception
    {
        Job job = owned(id); Input input = json.readValue(job.inputJson, Input.class);
        Student student = input.students.stream().filter(s -> s.studentId.equals(studentId)).findFirst().orElseThrow(() -> new ServiceException("学生不在本报告范围"));
        if (page < 1 || page > student.pages.size()) throw new ServiceException("页码不存在");
        return PracticalInsightInputService.resource(student.pages.get(page - 1));
    }
    private Job owned(Long id) throws Exception
    {
        requireEnabled(); Job job = mapper.get(id);
        if (job == null || !Objects.equals(job.teacherId, SecurityUtils.getUserId())) throw new ServiceException("报告不存在或无权访问");
        Input input = json.readValue(job.inputJson, Input.class);
        if (!Objects.equals(scope.requireScope(input.scope), job.deptId)) throw new ServiceException("报告学校范围已变化");
        scope.requireQuestion(input.scope);
        recoverStale(job.teacherId);
        return mapper.get(id);
    }
    private void recoverStale(Long teacher)
    {
        // 轮询通常只读；确有过期任务才按主键更新，避免全表更新与后台进度写入互锁。
        for (Long id : mapper.staleIds(teacher)) mapper.interruptStale(id);
    }
    private Map<String, Object> view(Job job) throws Exception
    {
        Input input = json.readValue(job.inputJson, Input.class);
        Map<String, Object> report = job.reportJson == null ? null : json.readValue(job.reportJson, Map.class);
        boolean stale = "STALE".equals(job.status);
        if (!Arrays.asList("PENDING", "RUNNING").contains(job.status))
        {
            try { stale |= !input.fingerprint.equals(inputs.build(input.scope).fingerprint); }
            catch (Exception e) { stale = true; }
        }
        BigDecimal cost = input.inputPrice == null || input.outputPrice == null ? null
                : input.inputPrice.multiply(BigDecimal.valueOf(job.promptTokens)).add(input.outputPrice.multiply(BigDecimal.valueOf(job.completionTokens)))
                    .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        return PracticalInsightEngine.map("enabled", true, "job", PracticalInsightEngine.map("reportId", job.reportId,
                "status", job.status, "stage", job.stage, "completed", job.completed, "failed", job.failed,
                "total", input.students.size(), "errorMessage", job.errorMessage, "promptTokens", job.promptTokens,
                "completionTokens", job.completionTokens, "estimatedCostYuan", cost, "createdAt", job.createdAt, "updatedAt", job.updatedAt),
                "maxCostYuan", maxCostYuan, "scope", input.scope, "stale", stale, "report", report,
                "publicationId", feedbackMapper.publishedReport(job.reportId,StudentFeedbackService.publicationHash(job,mapper.evidence(job.reportId))));
    }
    private void requireEnabled() { if (!enabled) throw new ServiceException("班级诊断V3尚未启用"); }
}
