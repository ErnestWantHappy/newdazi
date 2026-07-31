package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.mapper.ExemptionApplicationMapper;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 教师免抽测申请领域服务。
 *
 * 预览和提交共用同一个统计入口，提交时重新计算并保存快照，
 * 防止前端篡改班级、分母或比例。
 */
@Service
public class ExemptionApplicationService
{
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int DEFAULT_REQUIRED_LESSONS = 15;
    private static final BigDecimal USAGE_THRESHOLD = new BigDecimal("80.00");
    private static final int MAX_ATTACHMENTS = 5;

    @Autowired
    private ExemptionApplicationMapper mapper;

    public Map<String, Object> preview(String academicYear, String semester, Integer grade)
    {
        Period period = requirePeriod(academicYear, semester, grade);
        return buildPreview(period);
    }

    /**
     * 教师一次提交，统计和快照必须在同一事务内完成。
     */
    @Transactional
    public Map<String, Object> submit(Map<String, Object> request)
    {
        Period period = requirePeriod(stringValue(request.get("academicYear")),
                stringValue(request.get("semester")), integerValue(request.get("grade")));
        String teacherRemark = trimToNull(request.get("teacherRemark"));
        if (teacherRemark != null && teacherRemark.length() > 2000)
        {
            throw new ServiceException("补充说明不能超过2000个字符");
        }

        Map<String, Object> preview = buildPreview(period);
        if (booleanValue(preview.get("alreadySubmitted")))
        {
            throw new ServiceException("该学年、学期和年级已经提交过申请");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> classes = (List<Map<String, Object>>) preview.get("classes");
        if (classes == null || classes.isEmpty())
        {
            throw new ServiceException("未找到该年级的真实任教班级，暂不能提交");
        }

        Map<String, Object> application = new LinkedHashMap<>();
        copy(application, preview, "deptId", "deptName", "teacherId", "teacherName",
                "academicYear", "semester", "grade", "entryYear", "requiredLessonCount",
                "classCount", "allClassesQualified", "practicalDueCount",
                "practicalGradedCount", "practicalRate", "practicalQualified");
        application.put("teacherRemark", teacherRemark);
        application.put("operator", SecurityUtils.getUsername());
        try
        {
            mapper.insertApplication(application);
        }
        catch (DuplicateKeyException e)
        {
            throw new ServiceException("该学年、学期和年级已经提交过申请");
        }
        Long applicationId = longValue(application.get("applicationId"));
        if (applicationId == null)
        {
            throw new ServiceException("申请保存失败，请稍后重试");
        }

        for (Map<String, Object> classMetric : classes)
        {
            Map<String, Object> classSnapshot = new LinkedHashMap<>();
            copy(classSnapshot, classMetric, "deptId", "entryYear", "classCode",
                    "validStudentCount", "requiredLessonCount", "usedLessonCount",
                    "usageRate", "usageQualified", "practicalDueCount",
                    "practicalGradedCount", "practicalRate");
            classSnapshot.put("applicationId", applicationId);
            mapper.insertClassSnapshot(classSnapshot);
            Long classSnapshotId = longValue(classSnapshot.get("classSnapshotId"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> courses =
                    (List<Map<String, Object>>) classMetric.get("courses");
            if (courses != null && !courses.isEmpty())
            {
                List<Map<String, Object>> courseSnapshots = new ArrayList<>();
                for (Map<String, Object> course : courses)
                {
                    Map<String, Object> item = new LinkedHashMap<>();
                    copy(item, course, "lessonId", "lessonTitle", "entryYear", "classCode",
                            "usageDate", "validStudentCount", "participantCount",
                            "participationRate", "countedAsUsed", "practicalDueCount",
                            "practicalGradedCount", "practicalRate");
                    item.put("applicationId", applicationId);
                    item.put("classSnapshotId", classSnapshotId);
                    courseSnapshots.add(item);
                }
                mapper.insertCourseSnapshots(courseSnapshots);
            }
        }

        saveAttachments(applicationId, request.get("attachments"));
        return detail(applicationId);
    }

    public List<Map<String, Object>> myApplications()
    {
        return mapper.selectMyApplications(SecurityUtils.getUserId(), SecurityUtils.getDeptId());
    }

    public List<Map<String, Object>> reviewApplications(Map<String, Object> query)
    {
        return mapper.selectReviewApplications(query == null ? new LinkedHashMap<>() : query);
    }

    public List<Map<String, Object>> standards(String academicYear, String semester)
    {
        requireAcademicYear(academicYear);
        requireSemester(semester);
        return mapper.selectStandards(academicYear.trim(), semester.trim());
    }

    @Transactional
    public void saveStandard(Map<String, Object> request)
    {
        String academicYear = stringValue(request.get("academicYear"));
        String semester = stringValue(request.get("semester"));
        Integer grade = integerValue(request.get("grade"));
        Integer requiredLessonCount = integerValue(request.get("requiredLessonCount"));
        requirePeriod(academicYear, semester, grade);
        if (requiredLessonCount == null || requiredLessonCount < 1 || requiredLessonCount > 100)
        {
            throw new ServiceException("应使用课数必须在1到100之间");
        }
        mapper.upsertStandard(academicYear.trim(), semester.trim(), grade,
                requiredLessonCount, SecurityUtils.getUsername());
    }

    @Transactional
    public Map<String, Object> review(Long applicationId, Map<String, Object> request)
    {
        Map<String, Object> application = requireApplication(applicationId);
        String status = stringValue(request.get("status"));
        if (!"PASS".equals(status) && !"FAIL".equals(status))
        {
            throw new ServiceException("审核结果只能为通过或不通过");
        }
        if (!"PENDING".equals(stringValue(application.get("status"))))
        {
            throw new ServiceException("该申请已经审核，不能重复操作");
        }
        String reviewRemark = trimToNull(request.get("reviewRemark"));
        if (reviewRemark != null && reviewRemark.length() > 1000)
        {
            throw new ServiceException("审核备注不能超过1000个字符");
        }
        Integer version = integerValue(request.get("version"));
        if (version == null)
        {
            throw new ServiceException("申请版本不能为空，请刷新后重试");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String reviewerName = loginUser.getUser() == null
                ? SecurityUtils.getUsername()
                : loginUser.getUser().getNickName();
        int affected = mapper.reviewApplication(applicationId, status, reviewRemark,
                SecurityUtils.getUserId(), reviewerName, SecurityUtils.getUsername(), version);
        if (affected != 1)
        {
            throw new ServiceException("申请已被其他人审核，请刷新后查看");
        }
        return detail(applicationId);
    }

    public Map<String, Object> detail(Long applicationId)
    {
        Map<String, Object> application = requireApplication(applicationId);
        assertCanRead(application);
        application.put("classes", mapper.selectClassSnapshots(applicationId));
        application.put("courses", mapper.selectCourseSnapshots(applicationId));
        application.put("attachments", mapper.selectAttachments(applicationId));
        return application;
    }

    Map<String, Object> buildPreview(Period period)
    {
        Long teacherId = SecurityUtils.getUserId();
        Long deptId = SecurityUtils.getDeptId();
        Map<String, Object> identity = mapper.selectTeacherIdentity(teacherId, deptId);
        if (identity == null)
        {
            throw new ServiceException("当前教师未绑定有效学校");
        }

        Integer requiredLessonCount = mapper.selectRequiredLessonCount(
                period.academicYear, period.semester, period.grade);
        if (requiredLessonCount == null)
        {
            requiredLessonCount = DEFAULT_REQUIRED_LESSONS;
        }
        List<Map<String, Object>> classRows =
                mapper.selectTeacherClasses(teacherId, deptId, period.entryYear);
        List<Map<String, Object>> courseRows = mapper.selectTeacherCourseMetrics(
                teacherId, deptId, period.entryYear, period.startTime, period.endTime);

        Map<String, Object> summary = summarizeMetrics(classRows, courseRows, requiredLessonCount);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> summarizedClasses =
                (List<Map<String, Object>>) summary.get("classes");

        Map<String, Object> result = new LinkedHashMap<>(identity);
        result.put("academicYear", period.academicYear);
        result.put("semester", period.semester);
        result.put("grade", period.grade);
        result.put("entryYear", period.entryYear);
        result.put("startTime", period.startTime);
        result.put("endTime", period.endTime);
        result.put("requiredLessonCount", requiredLessonCount);
        result.put("usageThresholdPct", USAGE_THRESHOLD);
        result.put("participationThresholdPct", new BigDecimal("50.00"));
        result.put("practicalThresholdPct", USAGE_THRESHOLD);
        result.putAll(summary);
        result.put("alreadySubmitted", mapper.countApplication(deptId, teacherId,
                period.academicYear, period.semester, period.grade) > 0);
        result.put("classes", summarizedClasses);
        return result;
    }

    /**
     * 纯统计步骤单独保留，便于覆盖50%、80%、0分和零分母等边界。
     */
    Map<String, Object> summarizeMetrics(List<Map<String, Object>> classRows,
                                         List<Map<String, Object>> courseRows,
                                         int requiredLessonCount)
    {
        Map<String, List<Map<String, Object>>> coursesByClass = courseRows.stream()
                .collect(Collectors.groupingBy(row -> classKey(row.get("entryYear"), row.get("classCode")),
                        LinkedHashMap::new, Collectors.toList()));

        int overallDue = 0;
        int overallGraded = 0;
        boolean allClassesQualified = !classRows.isEmpty();
        for (Map<String, Object> classRow : classRows)
        {
            int validStudentCount = intValue(classRow.get("validStudentCount"), 0);
            List<Map<String, Object>> courses = coursesByClass.getOrDefault(
                    classKey(classRow.get("entryYear"), classRow.get("classCode")),
                    new ArrayList<>());
            int usedLessonCount = 0;
            int classDue = 0;
            int classGraded = 0;
            for (Map<String, Object> course : courses)
            {
                int participants = intValue(course.get("participantCount"), 0);
                int courseValidStudents = intValue(course.get("validStudentCount"), validStudentCount);
                BigDecimal participationRate = percentage(participants, courseValidStudents);
                boolean countedAsUsed = courseValidStudents > 0
                        && participants * 2 >= courseValidStudents;
                int due = intValue(course.get("practicalDueCount"), 0);
                int graded = intValue(course.get("practicalGradedCount"), 0);
                course.put("validStudentCount", courseValidStudents);
                course.put("participationRate", participationRate);
                course.put("countedAsUsed", countedAsUsed);
                course.put("practicalRate", percentage(graded, due));
                if (countedAsUsed)
                {
                    usedLessonCount++;
                }
                classDue += due;
                classGraded += graded;
            }
            BigDecimal usageRate = percentage(usedLessonCount, requiredLessonCount);
            boolean usageQualified = usageRate.compareTo(USAGE_THRESHOLD) >= 0;
            classRow.put("requiredLessonCount", requiredLessonCount);
            classRow.put("usedLessonCount", usedLessonCount);
            classRow.put("usageRate", usageRate);
            classRow.put("usageQualified", usageQualified);
            classRow.put("practicalDueCount", classDue);
            classRow.put("practicalGradedCount", classGraded);
            classRow.put("practicalUngradedCount", classDue - classGraded);
            classRow.put("practicalRate", percentage(classGraded, classDue));
            classRow.put("courses", courses);
            allClassesQualified = allClassesQualified && usageQualified;
            overallDue += classDue;
            overallGraded += classGraded;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("classCount", classRows.size());
        result.put("allClassesQualified", allClassesQualified);
        result.put("practicalDueCount", overallDue);
        result.put("practicalGradedCount", overallGraded);
        result.put("practicalUngradedCount", overallDue - overallGraded);
        BigDecimal practicalRate = percentage(overallGraded, overallDue);
        result.put("practicalRate", practicalRate);
        result.put("practicalQualified", practicalRate == null
                ? null : practicalRate.compareTo(USAGE_THRESHOLD) >= 0);
        result.put("classes", classRows);
        return result;
    }

    private void saveAttachments(Long applicationId, Object rawAttachments)
    {
        if (!(rawAttachments instanceof List))
        {
            return;
        }
        @SuppressWarnings("unchecked")
        List<Object> attachments = (List<Object>) rawAttachments;
        if (attachments.size() > MAX_ATTACHMENTS)
        {
            throw new ServiceException("证明附件最多上传5个");
        }
        for (Object raw : attachments)
        {
            if (!(raw instanceof Map))
            {
                throw new ServiceException("附件信息格式错误");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> source = (Map<String, Object>) raw;
            String resourcePath = normalizeUploadPath(source.get("resourcePath"));
            String originalFileName = trimToNull(source.get("originalFileName"));
            if (StringUtils.isBlank(originalFileName))
            {
                originalFileName = trimToNull(source.get("name"));
            }
            if (StringUtils.isBlank(originalFileName) || originalFileName.length() > 255)
            {
                throw new ServiceException("附件文件名不能为空且不能超过255个字符");
            }
            Map<String, Object> attachment = new LinkedHashMap<>();
            attachment.put("applicationId", applicationId);
            attachment.put("originalFileName", originalFileName);
            attachment.put("resourcePath", resourcePath);
            attachment.put("fileSize", longValue(source.get("fileSize")));
            attachment.put("mimeType", trimToNull(source.get("mimeType")));
            attachment.put("operator", SecurityUtils.getUsername());
            mapper.insertAttachment(attachment);
        }
    }

    private String normalizeUploadPath(Object raw)
    {
        String value = trimToNull(raw);
        if (value == null)
        {
            throw new ServiceException("附件资源路径不能为空");
        }
        value = value.replace('\\', '/');
        int profileIndex = value.toLowerCase(Locale.ROOT).indexOf("/profile/");
        if (profileIndex >= 0)
        {
            value = value.substring(profileIndex);
        }
        value = value.replaceAll("/{2,}", "/");
        if (!value.toLowerCase(Locale.ROOT).startsWith("/profile/upload/")
                || value.contains("../") || value.endsWith("/.."))
        {
            throw new ServiceException("附件资源路径非法");
        }
        return value;
    }

    private Map<String, Object> requireApplication(Long applicationId)
    {
        if (applicationId == null)
        {
            throw new ServiceException("申请ID不能为空");
        }
        Map<String, Object> application = mapper.selectApplicationById(applicationId);
        if (application == null)
        {
            throw new ServiceException("申请不存在");
        }
        return application;
    }

    private void assertCanRead(Map<String, Object> application)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null)
        {
            throw new ServiceException("请先登录");
        }
        if (loginUser.getUser().isAdmin() || hasRole(loginUser, "researcher"))
        {
            return;
        }
        if (hasRole(loginUser, "teacher")
                && Objects.equals(SecurityUtils.getUserId(), longValue(application.get("teacherId")))
                && Objects.equals(SecurityUtils.getDeptId(), longValue(application.get("deptId"))))
        {
            return;
        }
        throw new ServiceException("无权查看该申请");
    }

    private Period requirePeriod(String academicYear, String semester, Integer grade)
    {
        int startYear = requireAcademicYear(academicYear);
        requireSemester(semester);
        if (grade == null || grade < 1 || grade > 9)
        {
            throw new ServiceException("年级必须在1到9之间");
        }
        int gradeInSection = grade > 6 ? grade - 6 : grade;
        String entryYear = String.valueOf(startYear - gradeInSection + 1);
        LocalDate start;
        LocalDate end;
        if ("1".equals(semester.trim()))
        {
            start = LocalDate.of(startYear, 7, 20);
            end = LocalDate.of(startYear + 1, 2, 1);
        }
        else
        {
            start = LocalDate.of(startYear + 1, 2, 1);
            end = LocalDate.of(startYear + 1, 7, 20);
        }
        return new Period(String.valueOf(startYear), semester.trim(), grade, entryYear,
                Date.from(start.atStartOfDay(BEIJING_ZONE).toInstant()),
                Date.from(end.atStartOfDay(BEIJING_ZONE).toInstant()));
    }

    private int requireAcademicYear(String academicYear)
    {
        try
        {
            int value = Integer.parseInt(StringUtils.trim(academicYear));
            if (value < 2000 || value > 2100)
            {
                throw new ServiceException("学年超出允许范围");
            }
            return value;
        }
        catch (NumberFormatException e)
        {
            throw new ServiceException("学年格式不正确");
        }
    }

    private void requireSemester(String semester)
    {
        if (!"1".equals(StringUtils.trim(semester)) && !"2".equals(StringUtils.trim(semester)))
        {
            throw new ServiceException("学期只能为1或2");
        }
    }

    private boolean hasRole(LoginUser loginUser, String roleKey)
    {
        return loginUser.getUser().getRoles() != null
                && loginUser.getUser().getRoles().stream()
                        .anyMatch(role -> roleKey.equals(role.getRoleKey()));
    }

    private BigDecimal percentage(int numerator, int denominator)
    {
        if (denominator <= 0)
        {
            return null;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private String classKey(Object entryYear, Object classCode)
    {
        return stringValue(entryYear) + "|" + stringValue(classCode);
    }

    private void copy(Map<String, Object> target, Map<String, Object> source, String... keys)
    {
        for (String key : keys)
        {
            target.put(key, source.get(key));
        }
    }

    private String stringValue(Object value)
    {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String trimToNull(Object value)
    {
        return StringUtils.trimToNull(stringValue(value));
    }

    private Integer integerValue(Object value)
    {
        if (value == null || StringUtils.isBlank(String.valueOf(value)))
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Integer.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            throw new ServiceException("数字格式不正确");
        }
    }

    private int intValue(Object value, int defaultValue)
    {
        Integer parsed = integerValue(value);
        return parsed == null ? defaultValue : parsed;
    }

    private Long longValue(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        try
        {
            return Long.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            throw new ServiceException("标识格式不正确");
        }
    }

    private boolean booleanValue(Object value)
    {
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static class Period
    {
        private final String academicYear;
        private final String semester;
        private final Integer grade;
        private final String entryYear;
        private final Date startTime;
        private final Date endTime;

        private Period(String academicYear, String semester, Integer grade, String entryYear,
                       Date startTime, Date endTime)
        {
            this.academicYear = academicYear;
            this.semester = semester;
            this.grade = grade;
            this.entryYear = entryYear;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
