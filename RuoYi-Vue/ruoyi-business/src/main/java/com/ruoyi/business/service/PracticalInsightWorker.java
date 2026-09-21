package com.ruoyi.business.service;

import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.dto.PracticalInsightV3.*;
import com.ruoyi.business.mapper.PracticalInsightMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PracticalInsightWorker
{
    private static final Logger log = LoggerFactory.getLogger(PracticalInsightWorker.class);
    @Autowired private PracticalInsightMapper mapper;
    @Autowired private PracticalInsightInputService inputs;
    @Autowired private PracticalInsightAiService ai;
    @Autowired private TeacherPaperSubmissionService scope;
    @Autowired private SysUserMapper users;
    @Autowired private ObjectMapper json;
    @Value("${practical-insight.min-samples:10}") private int minSamples;
    @Value("${practical-insight.min-coverage:0.8}") private double minCoverage;

    @Async("practicalAiExecutor")
    public void run(Long reportId)
    {
        String token = UUID.randomUUID().toString();
        if (mapper.claim(reportId, token) == 0) return;
        Job job = mapper.get(reportId);
        job.runToken = token;
        SecurityContext previous = SecurityContextHolder.getContext();
        try
        {
            Input input = json.readValue(job.inputJson, Input.class);
            authorize(input);
            if (!input.fingerprint.equals(inputs.build(input.scope).fingerprint)) { mapper.finish(reportId, token, "STALE", "材料或成绩已变化，请重新生成"); return; }
            mapper.stage(reportId, token, "READING_PAPER");
            String cached = mapper.paper(input.teacherId, input.paperFingerprint);
            Paper paper;
            if (cached == null)
            {
                List<String> labels = new ArrayList<>();
                paper = ai.paper(job, input, inputs.materialPages(input, labels), labels);
                mapper.savePaper(input.teacherId, input.paperFingerprint, json.writeValueAsString(paper));
            }
            else paper = json.readValue(cached, Paper.class);
            Map<Long, Result> results = new LinkedHashMap<>();
            Map<Long, EvidenceRow> existing = mapper.evidence(reportId).stream().collect(Collectors.toMap(e -> e.studentId, e -> e));
            job.completed = 0; job.failed = 0;
            List<Student> pending = new ArrayList<>();
            for (Student s : input.students)
            {
                if (!running(job)) return;
                authorize(input);
                mapper.stage(reportId, token, "REUSING_EVIDENCE");
                EvidenceRow row = existing.get(s.studentId);
                if (row != null && "SUCCESS".equals(row.status) && s.fingerprint.equals(row.fingerprint))
                {
                    results.put(s.studentId, json.readValue(row.resultJson, Result.class)); job.completed++;
                }
                else
                {
                    String reusable = mapper.reusable(input.teacherId, s.studentId, s.fingerprint);
                    if (reusable == null) pending.add(s);
                    else { Result result = json.readValue(reusable, Result.class); results.put(s.studentId, result); job.completed++; save(job, s, result, "SUCCESS"); }
                }
            }
            progress(job, input, paper, results);
            // 限制每批知识点诊断总数，避免整班长输出截断；不再逐人上传原卷图片。
            int batchSize = Math.max(1, Math.min(6, 60 / paper.knowledgePoints.size()));
            int consecutiveFailures = 0;
            for (int offset = 0; offset < pending.size(); offset += batchSize)
            {
                if (!running(job)) return; authorize(input);
                mapper.stage(reportId, token, "DIAGNOSING_BATCH");
                List<Student> batch = pending.subList(offset, Math.min(offset + batchSize, pending.size()));
                Map<Long, Result> diagnosed = null;
                try
                {
                    diagnosed = ai.diagnoseBatch(job, input, paper, batch);
                }
                catch (PracticalInsightAiService.BudgetExceeded e) { throw e; }
                catch (Exception e)
                {
                    if (!running(job)) return;
                    log.warn("学情批次失败，报告{}，类型{}", reportId, e.getClass().getSimpleName());
                    for (Student s : batch)
                    {
                        Result failed = PracticalInsightAiService.unknown(paper);
                        failed.reviewReasons.add("本批文本诊断请求失败或输出不完整，可重试；既有原答与正式成绩不受影响");
                        save(job, s, failed, "FAILED"); job.failed++;
                    }
                    consecutiveFailures++;
                }
                if (diagnosed != null)
                {
                    if (!running(job)) return;
                    // 持久化失败应停止任务，不能当作模型失败再次计数或覆盖已保存结果。
                    for (Student s : batch)
                    {
                        Result result = diagnosed.get(s.studentId);
                        save(job, s, result, result.reviewReasons.isEmpty() ? "SUCCESS" : "REVIEW");
                        results.put(s.studentId, result); job.completed++;
                    }
                    consecutiveFailures = diagnosed.values().stream().allMatch(r -> r.diagnoses.stream().allMatch(d -> d.state == State.INSUFFICIENT))
                            ? consecutiveFailures + 1 : 0;
                }
                progress(job, input, paper, results);
                if (consecutiveFailures >= 2)
                {
                    mapper.finish(reportId, token, "PARTIAL", "连续两批未获得有效诊断，已暂停后续计费请求，请核对试卷映射及原答证据"); return;
                }
            }
            if (!running(job)) return;
            authorize(input);
            mapper.stage(reportId, token, "PLANNING_LESSON");
            Map<String, Object> report = report(input, paper, results);
            try { report.put("teaching", ai.teaching(job, input, paper, report)); }
            catch (PracticalInsightAiService.BudgetExceeded e) { throw e; }
            catch (Exception e) { report.put("teachingError", "教学安排未完成，可重试；已完成的学生诊断会复用"); }
            if (!running(job)) return;
            authorize(input);
            boolean stale = !input.fingerprint.equals(inputs.build(input.scope).fingerprint);
            report.put("stale", stale); job.reportJson = json.writeValueAsString(report); mapper.progress(job);
            boolean review = ((Number) report.get("reviewCount")).intValue() > 0;
            mapper.finish(reportId, token, stale ? "STALE" : job.failed > 0 || review || report.containsKey("teachingError") ? "PARTIAL" : "COMPLETED",
                    stale ? "材料或正式成绩已变化，报告仅供历史追溯" : job.failed > 0 || review ? "部分诊断待核对，已保留其余有效知识点和正式成绩" : null);
        }
        catch (PracticalInsightAiService.BudgetExceeded e)
        { mapper.finish(reportId, token, "PARTIAL", e.getMessage()); }
        catch (Exception e)
        {
            // 不把供应商响应、文件路径或账号信息落入面向教师的错误摘要。
            mapper.finish(reportId, token, "FAILED", "诊断未完成，请核对材料、模型配置及访问权限后重试");
        }
        finally { SecurityContextHolder.setContext(previous); }
    }
    private void save(Job job, Student student, Result result, String status) throws Exception
    {
        EvidenceRow row = new EvidenceRow(); row.reportId = job.reportId; row.studentId = student.studentId;
        row.fingerprint = student.fingerprint; row.status = status; row.resultJson = json.writeValueAsString(result); mapper.saveEvidence(row);
    }
    private Map<String, Object> report(Input input, Paper paper, Map<Long, Result> results)
    {
        Map<String, Object> report = PracticalInsightEngine.aggregate(input, paper, results, minSamples, minCoverage);
        report.put("reviewCount", results.values().stream().filter(r -> !r.reviewReasons.isEmpty()).count());
        return report;
    }
    private void progress(Job job, Input input, Paper paper, Map<Long, Result> results) throws Exception
    { job.reportJson = json.writeValueAsString(report(input, paper, results)); mapper.progress(job); }
    private boolean running(Job job) { Job current = mapper.get(job.reportId); return current != null && "RUNNING".equals(current.status) && Objects.equals(job.runToken, current.runToken); }

    private void authorize(Input input)
    {
        SysUser user = users.selectUserById(input.teacherId);
        if (user == null || !"0".equals(user.getStatus()) || !"0".equals(user.getDelFlag())
                || (!SecurityUtils.isAdmin(input.teacherId) && (user.getRoles() == null || user.getRoles().stream()
                    .noneMatch(r -> "0".equals(r.getStatus()) && ("teacher".equals(r.getRoleKey()) || "admin".equals(r.getRoleKey()))))))
            throw new ServiceException("教师身份已失效");
        // 后台每阶段从数据库重建当前身份并重验班级范围，绝不继承请求线程的登录态。
        LoginUser login = new LoginUser(); login.setUserId(input.teacherId); login.setDeptId(input.deptId); login.setUser(user);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(login, null, Collections.emptyList()));
        SecurityContextHolder.setContext(context);
        if (!Objects.equals(scope.requireScope(input.scope), input.deptId)) throw new ServiceException("班级授权已变化");
        scope.requireQuestion(input.scope);
    }
}
