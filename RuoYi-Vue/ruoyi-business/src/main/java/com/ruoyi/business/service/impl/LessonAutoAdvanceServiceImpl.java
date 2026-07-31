package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.mapper.LessonClassScopeMapper;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.business.service.LessonAutoAdvanceService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 自动/手动推进：按「指派班级」判断，达标后将本班指派切到同教师同校同年级的下一课次。
 * 「有成绩」复用 countScoredStudentsByLessonAndClass（与成绩页 scoreReady 口径一致，请假不计）。
 */
@Service
public class LessonAutoAdvanceServiceImpl implements LessonAutoAdvanceService
{
    private static final Logger log = LoggerFactory.getLogger(LessonAutoAdvanceServiceImpl.class);

    @Autowired
    private BizLessonMapper lessonMapper;

    @Autowired
    private BizLessonAssignmentMapper assignmentMapper;

    @Autowired
    private LessonClassScopeMapper lessonClassScopeMapper;

    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    @Autowired
    private BizStudentMapper studentMapper;

    @Autowired
    private BizTeacherClassMapper teacherClassMapper;

    /**
     * 自注入代理：同类方法调用时走 Spring 事务代理，避免 @Transactional 失效。
     */
    @Autowired
    @Lazy
    private LessonAutoAdvanceServiceImpl self;

    @Override
    public Map<String, Object> scanAndAdvance()
    {
        Map<String, Object> result = new HashMap<>();
        int scanned = 0;
        int advanced = 0;
        int skipped = 0;
        List<BizLesson> candidates = lessonMapper.selectAutoAdvanceCandidates();
        if (candidates == null || candidates.isEmpty())
        {
            result.put("scanned", 0);
            result.put("advanced", 0);
            result.put("skipped", 0);
            return result;
        }
        for (BizLesson lesson : candidates)
        {
            if (lesson == null || lesson.getLessonId() == null)
            {
                continue;
            }
            // 双保险：考勤课绝不推进
            if ("attendance".equalsIgnoreCase(lesson.getLessonMode()))
            {
                skipped++;
                continue;
            }
            if (!Boolean.TRUE.equals(lesson.getAutoAdvanceEnabled()))
            {
                skipped++;
                continue;
            }
            scanned++;
            try
            {
                // 经代理调用，保证 processLesson 事务生效
                int n = self.processLessonForScan(lesson);
                advanced += n;
            }
            catch (Exception ex)
            {
                log.warn("自动推进扫描失败 lessonId={}: {}", lesson.getLessonId(), ex.getMessage());
                skipped++;
            }
        }
        result.put("scanned", scanned);
        result.put("advanced", advanced);
        result.put("skipped", skipped);
        return result;
    }

    @Override
    public Map<String, Object> manualAdvanceClasses(String entryYear, List<String> classCodes)
    {
        Map<String, Object> result = new HashMap<>();
        int advanced = 0;
        int failed = 0;
        List<String> successMsgs = new java.util.ArrayList<>();
        List<String> failMsgs = new java.util.ArrayList<>();
        if (StringUtils.isBlank(entryYear))
        {
            throw new ServiceException("请选择年级");
        }
        if (classCodes == null || classCodes.isEmpty())
        {
            throw new ServiceException("请至少选择一个班级");
        }
        // 去重并清洗班号
        java.util.LinkedHashSet<String> pureClasses = new java.util.LinkedHashSet<>();
        for (String code : classCodes)
        {
            if (StringUtils.isBlank(code))
            {
                continue;
            }
            pureClasses.add(code.replace("班", "").trim());
        }
        if (pureClasses.isEmpty())
        {
            throw new ServiceException("请至少选择一个班级");
        }
        for (String pureClass : pureClasses)
        {
            try
            {
                // 经代理调用，保证单班推进事务生效
                Map<String, Object> one = self.manualAdvanceOneClassTx(entryYear.trim(), pureClass);
                advanced += one.get("advanced") instanceof Number ? ((Number) one.get("advanced")).intValue() : 0;
                if (one.get("message") != null)
                {
                    successMsgs.add(String.valueOf(one.get("message")));
                }
            }
            catch (ServiceException ex)
            {
                failed++;
                failMsgs.add(pureClass + "班：" + ex.getMessage());
            }
            catch (Exception ex)
            {
                failed++;
                failMsgs.add(pureClass + "班：推进失败");
                log.warn("手动推进班级失败 class={}: {}", pureClass, ex.getMessage());
            }
        }
        result.put("advanced", advanced);
        result.put("failed", failed);
        result.put("successMessages", successMsgs);
        result.put("failMessages", failMsgs);
        if (advanced == 0 && failed > 0)
        {
            // 全部失败时抛错，便于前端统一提示
            String detail = failMsgs.isEmpty() ? "所选班级均未推进成功" : String.join("；", failMsgs);
            throw new ServiceException(detail);
        }
        StringBuilder msg = new StringBuilder();
        msg.append("成功推进 ").append(advanced).append(" 个班级");
        if (failed > 0)
        {
            msg.append("，").append(failed).append(" 个未达条件或无法推进");
            if (!failMsgs.isEmpty())
            {
                msg.append("（").append(String.join("；", failMsgs)).append("）");
            }
        }
        result.put("message", msg.toString());
        return result;
    }

    /**
     * 单个班级手动推进（供批量调用；失败抛 ServiceException）。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> manualAdvanceOneClassTx(String entryYear, String pureClass)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("advanced", 0);
        Long deptId = SecurityUtils.getDeptId();
        Long userId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(userId))
        {
            BizTeacherClass managed = new BizTeacherClass();
            managed.setUserId(userId);
            managed.setDeptId(deptId);
            managed.setEntryYear(entryYear);
            managed.setClassCode(pureClass);
            if (teacherClassMapper.checkTeacherClassExists(managed) <= 0)
            {
                throw new ServiceException("只能推进自己管理的班级");
            }
        }
        // 锁定班级当前指派；手动推进与 Quartz 同时发生时只允许一个事务成功。
        BizLessonAssignment target = assignmentMapper.selectCurrentAssignmentForUpdate(entryYear, pureClass, deptId);
        if (target == null || target.getLessonId() == null)
        {
            throw new ServiceException("当前没有指派课程");
        }
        Long lessonId = target.getLessonId();
        BizLesson lesson = lessonMapper.selectBizLessonByLessonId(lessonId);
        if (lesson == null)
        {
            throw new ServiceException("当前课程不存在");
        }
        if (lesson.getDeptId() != null && !lesson.getDeptId().equals(deptId))
        {
            throw new ServiceException("无权操作该课程");
        }
        if ("attendance".equalsIgnoreCase(lesson.getLessonMode()))
        {
            throw new ServiceException("当前课是课堂考勤，不能推进");
        }
        // 仅允许推进本校「当前登录教师创建」的课程，防止同校其他教师误推/串班
        boolean creator = userId.equals(lesson.getCreatorId())
                || (lesson.getCreatorId() == null
                && SecurityUtils.getUsername().equals(lesson.getCreateBy()));
        if (!SecurityUtils.isAdmin(userId) && !creator)
        {
            throw new ServiceException("只能推进自己创建的课程");
        }
        // 使用教师统一阈值；默认 50%
        int threshold = lesson.getAutoAdvanceThresholdPct() != null ? lesson.getAutoAdvanceThresholdPct() : 50;
        if (threshold < 30)
        {
            threshold = 30;
        }
        if (threshold > 100)
        {
            threshold = 100;
        }
        if (!isClassReady(lessonId, target, threshold))
        {
            int scored = studentAnswerMapper.countScoredStudentsByLessonAndClass(
                    lessonId, target.getClassCode(), target.getEntryYear(), target.getDeptId());
            int total = studentMapper.countByDeptIdAndClass(target.getDeptId(), target.getEntryYear(), target.getClassCode());
            throw new ServiceException(String.format(
                    "「%s」有成绩 %s/%s 人，需达到 %s%%",
                    lesson.getLessonTitle() != null ? lesson.getLessonTitle() : ("第" + lesson.getLessonNum() + "课"),
                    scored, total, threshold));
        }
        BizLesson next = findNextLesson(lesson);
        if (next == null || next.getLessonId() == null)
        {
            throw new ServiceException("没有可推进的下一课");
        }
        int moved = advanceOneClass(lesson, next, target, "MANUAL", userId);
        if (moved != 1)
        {
            throw new ServiceException("课程已被其他操作推进，请刷新后重试");
        }
        result.put("advanced", moved);
        result.put("message", String.format("%s班：从「%s」→「%s」", pureClass,
                lesson.getLessonTitle() != null ? lesson.getLessonTitle() : ("第" + lesson.getLessonNum() + "课"),
                next.getLessonTitle() != null ? next.getLessonTitle() : ("第" + next.getLessonNum() + "课")));
        return result;
    }

    /**
     * Quartz 扫描入口：单课推进事务边界（public 以便代理）。
     */
    @Transactional(rollbackFor = Exception.class)
    public int processLessonForScan(BizLesson lesson)
    {
        return processLesson(lesson);
    }

    private int processLesson(BizLesson lesson)
    {
        List<BizLessonAssignment> assignments = assignmentMapper.selectAssignmentsByLessonId(lesson.getLessonId());
        if (assignments == null || assignments.isEmpty())
        {
            return 0;
        }
        int threshold = lesson.getAutoAdvanceThresholdPct() != null ? lesson.getAutoAdvanceThresholdPct() : 50;
        if (threshold < 30)
        {
            threshold = 30;
        }
        if (threshold > 100)
        {
            threshold = 100;
        }
        BigDecimal delayHours = lesson.getAutoAdvanceDelayHours() != null
                ? lesson.getAutoAdvanceDelayHours()
                : new BigDecimal("2.0");
        long delayMs = delayHours.multiply(BigDecimal.valueOf(TimeUnit.HOURS.toMillis(1)))
                .setScale(0, RoundingMode.HALF_UP).longValue();

        BizLesson next = findNextLesson(lesson);
        if (next == null || next.getLessonId() == null)
        {
            log.info("课程 {} 已达推进条件，但未找到下一课次，跳过", lesson.getLessonId());
            return 0;
        }

        int moved = 0;
        for (BizLessonAssignment a : assignments)
        {
            if (!isClassReady(lesson.getLessonId(), a, threshold))
            {
                assignmentMapper.clearAssignmentReadyTime(a.getAssignmentId());
                continue;
            }
            Date readyTime = a.getAutoAdvanceReadyTime();
            if (readyTime == null)
            {
                readyTime = new Date();
                assignmentMapper.markAutoAdvanceReady(a.getAssignmentId(), readyTime);
                a.setAutoAdvanceReadyTime(readyTime);
            }
            if (System.currentTimeMillis() - readyTime.getTime() < delayMs)
            {
                continue;
            }
            moved += advanceOneClass(lesson, next, a, "AUTO", lesson.getCreatorId());
        }
        return moved;
    }

    /** 将单个班级的指派从当前课切到下一课 */
    private int advanceOneClass(BizLesson current, BizLesson next, BizLessonAssignment a,
                                String source, Long advancedBy)
    {
        if (a == null || next == null || next.getLessonId() == null)
        {
            return 0;
        }
        BizLessonAssignment locked = assignmentMapper.selectCurrentAssignmentForUpdate(
                a.getEntryYear(), a.getClassCode(), a.getDeptId());
        if (locked == null || !current.getLessonId().equals(locked.getLessonId()))
        {
            return 0;
        }
        Date now = new Date();
        Long operatorId = advancedBy != null ? advancedBy : locked.getAssignerId();
        int changed = assignmentMapper.advanceCurrentAssignment(
                locked.getAssignmentId(), current.getLessonId(), next.getLessonId(), operatorId, now);
        if (changed != 1)
        {
            return 0;
        }
        lessonClassScopeMapper.markAssignmentInactive(
                current.getLessonId(), locked.getDeptId(), locked.getEntryYear(), locked.getClassCode());
        BizLessonAssignment nextAssignment = new BizLessonAssignment();
        nextAssignment.setLessonId(next.getLessonId());
        nextAssignment.setDeptId(locked.getDeptId());
        nextAssignment.setEntryYear(locked.getEntryYear());
        nextAssignment.setClassCode(locked.getClassCode());
        nextAssignment.setAssignTime(now);
        lessonClassScopeMapper.upsertCurrentAssignment(nextAssignment);
        assignmentMapper.insertAdvanceHistory(locked, next.getLessonId(), operatorId, source, now);
        log.info("课程推进：lesson {} -> {} class {}-{} dept {}",
                current.getLessonId(), next.getLessonId(), a.getEntryYear(), a.getClassCode(), a.getDeptId());
        return 1;
    }

    private boolean isClassReady(Long lessonId, BizLessonAssignment a, int thresholdPct)
    {
        if (a == null || StringUtils.isBlank(a.getClassCode()) || StringUtils.isBlank(a.getEntryYear()) || a.getDeptId() == null)
        {
            return false;
        }
        int scored = studentAnswerMapper.countScoredStudentsByLessonAndClass(
                lessonId, a.getClassCode(), a.getEntryYear(), a.getDeptId());
        int total = studentMapper.countByDeptIdAndClass(a.getDeptId(), a.getEntryYear(), a.getClassCode());
        if (total <= 0)
        {
            return false;
        }
        int pct = (int) Math.floor(scored * 100.0 / total);
        return pct >= thresholdPct;
    }

    /**
     * 下一课：同学校、同创建教师、同届别、课次号更大的最近一节常规课。
     * grade 是创建时快照，跨学年后不能再代表课程稳定届别，因此不能参与推进过滤。
     */
    private BizLesson findNextLesson(BizLesson current)
    {
        if (current.getCreatorId() == null || current.getDeptId() == null
                || StringUtils.isBlank(current.getEntryYear())
                || current.getLessonNum() == null)
        {
            return null;
        }
        BizLesson query = new BizLesson();
        query.setCreatorId(current.getCreatorId());
        query.setDeptId(current.getDeptId());
        query.setEntryYear(current.getEntryYear());
        List<BizLesson> list = lessonMapper.selectBizLessonList(query);
        if (list == null || list.isEmpty())
        {
            return null;
        }
        BizLesson best = null;
        for (BizLesson l : list)
        {
            if (l.getLessonId() == null || l.getLessonId().equals(current.getLessonId()))
            {
                continue;
            }
            if ("attendance".equalsIgnoreCase(l.getLessonMode()))
            {
                continue;
            }
            if (l.getLessonNum() == null || l.getLessonNum() <= current.getLessonNum())
            {
                continue;
            }
            if (best == null || l.getLessonNum() < best.getLessonNum()
                    || (l.getLessonNum().equals(best.getLessonNum()) && l.getLessonId() < best.getLessonId()))
            {
                best = l;
            }
        }
        return best;
    }
}
