package com.ruoyi.business.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.github.pagehelper.PageHelper;
import com.ruoyi.business.domain.query.TeachingSupervisionQuery;
import com.ruoyi.business.mapper.TeachingSupervisionMapper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.DownloadFileNameUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.sign.Md5Utils;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.business.util.AcademicYearUtils;

/**
 * 教研员课程与成绩监管，只提供跨学校只读事实。
 */
@RestController
@RequestMapping("/business/schoolScore")
@PreAuthorize("(@ss.hasRole('researcher') or @ss.hasRole('admin')) and "
        + "@ss.hasPermi('business:teachingSupervision:view')")
public class TeachingSupervisionController extends BaseController
{
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String SUMMARY_CACHE_PREFIX = "business:teaching-supervision:summary:v2:";
    private static final int SUMMARY_CACHE_SECONDS = 60;

    @Autowired
    private TeachingSupervisionMapper supervisionMapper;

    @Autowired
    private RedisCache redisCache;

    private final Map<String, Object> summaryCacheLocks = new ConcurrentHashMap<>();

    @GetMapping("/schools")
    public TableDataInfo schools(TeachingSupervisionQuery query)
    {
        normalizePeriod(query);
        PageDomain pageDomain = TableSupport.buildPageRequest();
        String cacheKey = buildSchoolCacheKey(query, pageDomain);
        return loadCachedSummary(cacheKey, () -> loadSchoolSummaries(query, pageDomain));
    }

    private TableDataInfo loadSchoolSummaries(TeachingSupervisionQuery query, PageDomain pageDomain)
    {
        long total = supervisionMapper.countSchoolSummaries(query);
        // 学校汇总的自动 count 会重复执行全部成绩聚合，改用轻量计数避免同一请求计算两遍。
        PageHelper.startPage(pageDomain.getPageNum(), pageDomain.getPageSize(), false)
                .setReasonable(pageDomain.getReasonable());
        TableDataInfo result = getDataTable(supervisionMapper.selectSchoolSummaries(query));
        result.setTotal(total);
        return result;
    }

    String buildSchoolCacheKey(TeachingSupervisionQuery query, PageDomain pageDomain)
    {
        return buildSummaryCacheKey("schools", query, pageDomain);
    }

    String buildSummaryCacheKey(String scope, TeachingSupervisionQuery query, PageDomain pageDomain)
    {
        String canonical = String.join("\u001f",
                value(query.getAcademicYear()), value(query.getSemester()),
                value(query.getUsageStartDate()), value(query.getUsageEndDate()),
                value(query.getUsageSort()), value(query.getDeptId()), value(query.getTeacherId()),
                value(query.getLessonId()), value(query.getKeyword()), value(query.getLessonMode()),
                value(query.getEntryYear()), value(query.getGrade()), value(query.getClassCode()),
                value(query.getHasPractical()), value(query.getStatusCode()));
        if (pageDomain != null)
        {
            canonical = String.join("\u001f", canonical,
                    value(pageDomain.getPageNum()), value(pageDomain.getPageSize()),
                    value(pageDomain.getOrderByColumn()), value(pageDomain.getIsAsc()));
        }
        return SUMMARY_CACHE_PREFIX + scope + ":" + Md5Utils.hash(canonical);
    }

    private String value(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }

    @GetMapping("/teachers")
    public TableDataInfo teachers(TeachingSupervisionQuery query)
    {
        normalizePeriod(query);
        return loadCachedPagedSummary("teachers", query,
                () -> supervisionMapper.selectTeacherSummaries(query));
    }

    @GetMapping("/courses")
    public TableDataInfo courses(TeachingSupervisionQuery query)
    {
        normalizePeriod(query);
        return loadCachedPagedSummary("courses", query,
                () -> supervisionMapper.selectCourseSummaries(query));
    }

    /**
     * 跨学校按首次真实使用日期展示课程班级，避免教研员必须逐校下钻才能看到最新课堂。
     */
    @GetMapping("/timeline")
    public TableDataInfo timeline(TeachingSupervisionQuery query)
    {
        normalizePeriod(query);
        return loadCachedPagedSummary("timeline", query,
                () -> supervisionMapper.selectTimelineSummaries(query));
    }

    private TableDataInfo loadCachedPagedSummary(String scope, TeachingSupervisionQuery query,
            Supplier<List<Map<String, Object>>> loader)
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        // 同一筛选条件的不同页共享一份聚合结果，避免每翻一页重算全部课堂事实。
        String cacheKey = buildSummaryCacheKey(scope + ":all", query, null);
        TableDataInfo fullResult = loadCachedSummary(cacheKey, () ->
        {
            return getDataTable(loader.get());
        });
        return paginate(fullResult, pageDomain);
    }

    private TableDataInfo paginate(TableDataInfo fullResult, PageDomain pageDomain)
    {
        List<?> rows = fullResult.getRows();
        long total = rows == null ? 0L : rows.size();
        int pageSize = Math.max(1, pageDomain.getPageSize());
        int pageNum = Math.max(1, pageDomain.getPageNum());
        int pages = total == 0 ? 1 : (int) ((total + pageSize - 1) / pageSize);
        if (pageDomain.getReasonable() && pageNum > pages)
        {
            pageNum = pages;
        }
        int fromIndex = (int) Math.min(total, (long) (pageNum - 1) * pageSize);
        int toIndex = (int) Math.min(total, (long) fromIndex + pageSize);
        List<?> pageRows = rows == null ? java.util.Collections.emptyList() : rows.subList(fromIndex, toIndex);
        TableDataInfo pageResult = new TableDataInfo(pageRows, total);
        pageResult.setCode(fullResult.getCode());
        pageResult.setMsg(fullResult.getMsg());
        return pageResult;
    }

    private TableDataInfo loadCachedSummary(String cacheKey, Supplier<TableDataInfo> loader)
    {
        TableDataInfo cached = redisCache.getCacheObject(cacheKey);
        if (cached != null)
        {
            return cached;
        }
        Object cacheLock = summaryCacheLocks.computeIfAbsent(cacheKey, key -> new Object());
        try
        {
            // 同一统计参数只允许一个请求回源，避免聚合 SQL 在瞬时并发下击穿数据库。
            synchronized (cacheLock)
            {
                cached = redisCache.getCacheObject(cacheKey);
                if (cached != null)
                {
                    return cached;
                }
                TableDataInfo result = loader.get();
                redisCache.setCacheObject(cacheKey, result, SUMMARY_CACHE_SECONDS, TimeUnit.SECONDS);
                return result;
            }
        }
        finally
        {
            summaryCacheLocks.remove(cacheKey, cacheLock);
        }
    }

    @GetMapping("/classes")
    public TableDataInfo classes(TeachingSupervisionQuery query)
    {
        normalizePeriod(query);
        if (query.getLessonId() == null)
        {
            throw new ServiceException("课程不能为空");
        }
        startPage();
        return getDataTable(supervisionMapper.selectClassSummaries(query));
    }

    @GetMapping("/students")
    public TableDataInfo students(TeachingSupervisionQuery query)
    {
        normalizePeriod(query);
        requireClassKey(query);
        assertClassExists(query);
        startPage();
        return getDataTable(supervisionMapper.selectStudentDetails(query));
    }

    @GetMapping("/questions")
    public AjaxResult questions(@RequestParam Long lessonId,
                                @RequestParam(required = false) String academicYear,
                                @RequestParam(required = false) String semester)
    {
        TeachingSupervisionQuery query = new TeachingSupervisionQuery();
        query.setLessonId(lessonId);
        query.setAcademicYear(academicYear);
        query.setSemester(semester);
        normalizePeriod(query);
        if (supervisionMapper.selectCourseSummaries(query).isEmpty())
        {
            throw new ServiceException("课程不存在或不在所选监管周期");
        }
        return AjaxResult.success(supervisionMapper.selectQuestionDetails(lessonId));
    }

    @GetMapping("/practical-answers")
    public AjaxResult practicalAnswers(@RequestParam Long lessonId,
                                       @RequestParam Long deptId,
                                       @RequestParam String entryYear,
                                       @RequestParam String classCode,
                                       @RequestParam(required = false) Long studentId,
                                       @RequestParam(required = false) String academicYear,
                                       @RequestParam(required = false) String semester)
    {
        TeachingSupervisionQuery query = new TeachingSupervisionQuery();
        query.setLessonId(lessonId);
        query.setDeptId(deptId);
        query.setEntryYear(entryYear);
        query.setClassCode(classCode);
        query.setAcademicYear(academicYear);
        query.setSemester(semester);
        normalizePeriod(query);
        assertClassExists(query);
        return AjaxResult.success(supervisionMapper.selectPracticalAnswerDetails(
                lessonId, entryYear.trim(), normalizeClassCode(classCode), studentId));
    }

    @PostMapping("/export/schools")
    @PreAuthorize("(@ss.hasRole('researcher') or @ss.hasRole('admin')) and "
            + "@ss.hasPermi('business:teachingSupervision:export')")
    @Log(title = "课程监管学校汇总导出", businessType = BusinessType.EXPORT)
    public void exportSchools(HttpServletResponse response, TeachingSupervisionQuery query) throws IOException
    {
        normalizePeriod(query);
        writeCsv(response, "课程监管-学校汇总.csv", query,
                new String[] {"学校", "课程总数", "常规课", "考勤课", "有课程教师", "班级数",
                        "参与学生", "操作题应批", "已批", "未批", "逾期班级", "平均分"},
                new String[] {"deptName", "courseCount", "assessmentCourseCount", "attendanceCourseCount",
                        "teacherCount", "classCount", "participantCount", "practicalDueCount",
                        "practicalGradedCount", "practicalUngradedCount", "overdueClassCount", "avgTotalScore"},
                supervisionMapper.selectSchoolSummaries(query));
    }

    @PostMapping("/export/courses")
    @PreAuthorize("(@ss.hasRole('researcher') or @ss.hasRole('admin')) and "
            + "@ss.hasPermi('business:teachingSupervision:export')")
    @Log(title = "课程监管教师课程导出", businessType = BusinessType.EXPORT)
    public void exportCourses(HttpServletResponse response, TeachingSupervisionQuery query) throws IOException
    {
        normalizePeriod(query);
        writeCsv(response, "课程监管-教师课程汇总.csv", query,
                new String[] {"学校", "教师", "课程", "类型", "入学年份", "年级", "课次",
                        "班级数", "参与学生", "操作题应批", "已批", "未批", "使用日期", "创建时间", "修改时间"},
                new String[] {"deptName", "teacherName", "lessonTitle", "lessonMode", "entryYear",
                        "grade", "lessonNum", "classCount", "participantCount", "practicalDueCount",
                        "practicalGradedCount", "practicalUngradedCount", "usageDate", "createTime", "updateTime"},
                supervisionMapper.selectCourseSummaries(query));
    }

    @PostMapping("/export/students")
    @PreAuthorize("(@ss.hasRole('researcher') or @ss.hasRole('admin')) and "
            + "@ss.hasPermi('business:teachingSupervision:export')")
    @Log(title = "课程监管学生明细导出", businessType = BusinessType.EXPORT)
    public void exportStudents(HttpServletResponse response, TeachingSupervisionQuery query) throws IOException
    {
        normalizePeriod(query);
        requireClassKey(query);
        assertClassExists(query);
        writeCsv(response, "课程监管-学生成绩明细.csv", query,
                new String[] {"姓名", "学号", "入学年份", "班级", "已提交", "作业分",
                        "课堂表现", "课程总成绩", "操作题应批", "已批", "未批", "批改状态"},
                new String[] {"studentName", "studentNo", "entryYear", "classCode", "submitted",
                        "homeworkScore", "performanceScore", "finalScore", "practicalDueCount",
                        "practicalGradedCount", "practicalUngradedCount", "practicalStatus"},
                supervisionMapper.selectStudentDetails(query));
    }

    private void assertClassExists(TeachingSupervisionQuery query)
    {
        List<Map<String, Object>> classes = supervisionMapper.selectClassSummaries(query);
        if (classes.isEmpty())
        {
            throw new ServiceException("课程班级不存在或不在当前监管周期");
        }
    }

    private void requireClassKey(TeachingSupervisionQuery query)
    {
        if (query.getLessonId() == null || query.getDeptId() == null
                || StringUtils.isBlank(query.getEntryYear()) || StringUtils.isBlank(query.getClassCode()))
        {
            throw new ServiceException("学校、课程、入学年份和班级不能为空");
        }
        query.setEntryYear(query.getEntryYear().trim());
        query.setClassCode(normalizeClassCode(query.getClassCode()));
    }

    private void normalizePeriod(TeachingSupervisionQuery query)
    {
        normalizePeriod(query, LocalDate.now(BEIJING_ZONE));
    }

    /**
     * 监管周期必须复用平台 7 月 20 日学年边界，避免暑期误查上一学年。
     */
    void normalizePeriod(TeachingSupervisionQuery query, LocalDate today)
    {
        int academicStartYear;
        try
        {
            academicStartYear = StringUtils.isBlank(query.getAcademicYear())
                    ? AcademicYearUtils.resolveAcademicStartYear(today)
                    : Integer.parseInt(query.getAcademicYear().trim());
        }
        catch (NumberFormatException e)
        {
            throw new ServiceException("学年格式不正确");
        }
        if (academicStartYear < 2000 || academicStartYear > 2100)
        {
            throw new ServiceException("学年超出允许范围");
        }
        String semester = StringUtils.isBlank(query.getSemester()) ? "all" : query.getSemester().trim();
        LocalDate start;
        LocalDate end;
        if ("1".equals(semester))
        {
            start = LocalDate.of(academicStartYear, 7, 20);
            end = LocalDate.of(academicStartYear + 1, 2, 1);
        }
        else if ("2".equals(semester))
        {
            start = LocalDate.of(academicStartYear + 1, 2, 1);
            end = LocalDate.of(academicStartYear + 1, 7, 20);
        }
        else if ("all".equalsIgnoreCase(semester))
        {
            start = LocalDate.of(academicStartYear, 7, 20);
            end = LocalDate.of(academicStartYear + 1, 7, 20);
        }
        else
        {
            throw new ServiceException("学期只能为1、2或all");
        }
        query.setAcademicYear(String.valueOf(academicStartYear));
        query.setSemester(semester);
        query.setStartTime(java.util.Date.from(start.atStartOfDay(BEIJING_ZONE).toInstant()));
        query.setEndTime(java.util.Date.from(end.atStartOfDay(BEIJING_ZONE).toInstant()));
        normalizeUsageRange(query, start, end);
        if (StringUtils.isNotBlank(query.getClassCode()))
        {
            query.setClassCode(normalizeClassCode(query.getClassCode()));
        }
        String usageSort = StringUtils.isBlank(query.getUsageSort())
                ? "desc" : query.getUsageSort().trim().toLowerCase(java.util.Locale.ROOT);
        if (!"asc".equals(usageSort) && !"desc".equals(usageSort))
        {
            throw new ServiceException("使用日期排序只能为asc或desc");
        }
        query.setUsageSort(usageSort);
    }

    /**
     * 显式使用日期范围按自然日闭区间接收，查询时转成左闭右开。
     */
    private void normalizeUsageRange(TeachingSupervisionQuery query, LocalDate periodStart, LocalDate periodEnd)
    {
        boolean hasStart = StringUtils.isNotBlank(query.getUsageStartDate());
        boolean hasEnd = StringUtils.isNotBlank(query.getUsageEndDate());
        if (!hasStart && !hasEnd)
        {
            query.setUsageDateFiltered(false);
            query.setActivityStartTime(java.util.Date.from(
                    periodStart.atStartOfDay(BEIJING_ZONE).toInstant()));
            query.setActivityEndTime(java.util.Date.from(
                    periodEnd.atStartOfDay(BEIJING_ZONE).toInstant()));
            return;
        }
        try
        {
            LocalDate usageStart = hasStart
                    ? LocalDate.parse(query.getUsageStartDate().trim()) : periodStart;
            LocalDate usageEndExclusive = hasEnd
                    ? LocalDate.parse(query.getUsageEndDate().trim()).plusDays(1) : periodEnd;
            if (usageStart.isBefore(periodStart) || usageEndExclusive.isAfter(periodEnd)
                    || !usageStart.isBefore(usageEndExclusive))
            {
                throw new ServiceException("使用日期范围必须位于所选学期内");
            }
            query.setUsageDateFiltered(true);
            query.setActivityStartTime(java.util.Date.from(
                    usageStart.atStartOfDay(BEIJING_ZONE).toInstant()));
            query.setActivityEndTime(java.util.Date.from(
                    usageEndExclusive.atStartOfDay(BEIJING_ZONE).toInstant()));
        }
        catch (java.time.format.DateTimeParseException e)
        {
            throw new ServiceException("使用日期格式必须为yyyy-MM-dd");
        }
    }

    private String normalizeClassCode(String value)
    {
        return value.replace("班", "").trim();
    }

    private void writeCsv(HttpServletResponse response, String filename, TeachingSupervisionQuery query,
                          String[] headers, String[] keys, List<Map<String, Object>> rows) throws IOException
    {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        FileUtils.setAttachmentResponseHeader(response, DownloadFileNameUtils.withTimestamp(filename));
        java.io.PrintWriter writer = response.getWriter();
        writer.write('\ufeff');
        writer.println(csvCell("统计时间") + "," + csvCell(java.time.ZonedDateTime.now(BEIJING_ZONE).toString()));
        writer.println(csvCell("筛选条件") + "," + csvCell(
                "学年=" + query.getAcademicYear() + "，学期=" + query.getSemester()
                        + "，学校=" + valueOrAll(query.getDeptId()) + "，教师=" + valueOrAll(query.getTeacherId())
                        + "，班级=" + valueOrAll(query.getClassCode())
                        + "，使用日期=" + valueOrAll(query.getUsageStartDate())
                        + "至" + valueOrAll(query.getUsageEndDate())
                        + "，关键词=" + valueOrAll(query.getKeyword())));
        writer.println(csvCell("成绩口径") + "," + csvCell("沿用现有作业分（含人工修正）＋课堂表现；请假不计；考勤课不进入平均分"));
        writer.println(String.join(",", java.util.Arrays.stream(headers)
                .map(this::csvCell).toArray(String[]::new)));
        for (Map<String, Object> row : rows)
        {
            String[] values = new String[keys.length];
            for (int i = 0; i < keys.length; i++)
            {
                values[i] = csvCell(mapValue(row, keys[i]));
            }
            writer.println(String.join(",", values));
        }
        writer.flush();
    }

    private String mapValue(Map<String, Object> row, String key)
    {
        Object value = row.get(key);
        if (value == null)
        {
            for (Map.Entry<String, Object> entry : row.entrySet())
            {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key))
                {
                    value = entry.getValue();
                    break;
                }
            }
        }
        return value == null ? "" : String.valueOf(value);
    }

    private String csvCell(Object raw)
    {
        String value = raw == null ? "" : String.valueOf(raw);
        if (!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0)
        {
            value = "'" + value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String valueOrAll(Object value)
    {
        return value == null || String.valueOf(value).trim().isEmpty() ? "全部" : String.valueOf(value);
    }
}
