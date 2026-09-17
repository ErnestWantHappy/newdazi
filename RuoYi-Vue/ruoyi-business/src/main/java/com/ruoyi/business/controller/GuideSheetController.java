package com.ruoyi.business.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.business.config.GuideSheetProperties;
import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.BizGuideSheetUpload;
import com.ruoyi.business.domain.BizLessonGuideSheetBinding;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.vo.GuideSheetExportVo;
import com.ruoyi.business.domain.vo.GuideSheetProgressVo;
import com.ruoyi.business.domain.vo.GuideSheetVo;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.business.mapper.GuideSheetUploadMapper;
import com.ruoyi.business.service.AiGradingService;
import com.ruoyi.business.service.GuideSheetAccessService;
import com.ruoyi.business.service.GuideSheetGradingService;
import com.ruoyi.business.service.GuideSheetStudentViewService;
import com.ruoyi.business.service.GuideSheetUploadService;
import com.ruoyi.business.service.IGuideSheetAnswerService;
import com.ruoyi.business.service.IGuideSheetService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/business/guide-sheet")
public class GuideSheetController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(GuideSheetController.class);

    @Autowired
    private IGuideSheetService guideSheetService;

    @Autowired
    private IGuideSheetAnswerService answerService;

    @Autowired
    private GuideSheetAccessService accessService;

    @Autowired
    private GuideSheetGradingService gradingService;

    @Autowired
    private GuideSheetProgressMapper progressMapper;

    @Autowired
    private GuideSheetUploadMapper uploadMapper;

    @Autowired
    private AiGradingService aiGradingService;

    @Autowired
    private GuideSheetProperties guideSheetProperties;

    @Autowired
    private GuideSheetStudentViewService studentViewService;

    @Autowired
    private GuideSheetUploadService guideSheetUploadService;

    @PreAuthorize("@ss.hasPermi('business:guideSheet:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizGuideSheet query,
                              @RequestParam(value = "creatorFilter", required = false) String creatorFilter,
                              @RequestParam(value = "scope", required = false) String scope)
    {
        // 产品仅保留：all（权限内全部）/ public（公共导学单）/ mine（我的私有）
        // creatorFilter=self 为历史兼容，等价于 mine
        String normalizedScope = StringUtils.isNotEmpty(scope) ? scope.trim()
                : ("self".equals(creatorFilter) ? "mine" : "all");
        if (!"public".equals(normalizedScope) && !"mine".equals(normalizedScope))
        {
            normalizedScope = "all";
        }
        query.getParams().put("scope", normalizedScope);
        startPage();
        return getDataTable(guideSheetService.selectBizGuideSheetList(query));
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:list')")
    @GetMapping("/creators")
    public AjaxResult getCreators()
    {
        return success(guideSheetService.getCreatorList());
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:list')")
    @GetMapping("/capabilities")
    public AjaxResult getCapabilities()
    {
        return success()
                .put("aiConfigured", aiGradingService.isConfigured())
                .put("teacherHelperEnabled", guideSheetProperties.getTeacherHelper().isEnabled());
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:list')")
    @GetMapping("/{sheetId}")
    public AjaxResult getInfo(@PathVariable Long sheetId)
    {
        return success(guideSheetService.selectGuideSheetDetail(sheetId));
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:list')")
    @GetMapping("/{sheetId}/preview")
    public AjaxResult previewTemplate(@PathVariable Long sheetId)
    {
        BizGuideSheet sheet = accessService.requireVisibleTemplate(sheetId);
        return success().put("title", sheet.getSheetTitle())
                .put("formJson", studentViewService.sanitizeFormJson(sheet.getFormJson()))
                .put("maxPages", sheet.getMaxPages());
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:add')")
    @Log(title = "导学单模板", businessType = BusinessType.INSERT,
            isSaveResponseData = false, excludeParamNames = { "formJson", "teacherMachineIp" })
    @PostMapping
    public AjaxResult add(@RequestBody GuideSheetVo vo)
    {
        if (vo == null)
        {
            return error("导学单内容不能为空");
        }
        if (vo.getSheetId() != null)
        {
            return error("新增导学单不能携带导学单ID");
        }
        return success(guideSheetService.saveGuideSheetDetail(vo));
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:edit')")
    @Log(title = "导学单模板", businessType = BusinessType.UPDATE,
            isSaveResponseData = false, excludeParamNames = { "formJson", "teacherMachineIp" })
    @PutMapping
    public AjaxResult edit(@RequestBody GuideSheetVo vo)
    {
        if (vo == null || vo.getSheetId() == null)
        {
            return error("导学单ID不能为空");
        }
        return success(guideSheetService.saveGuideSheetDetail(vo));
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:remove')")
    @Log(title = "导学单模板归档", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sheetIds}")
    public AjaxResult archive(@PathVariable Long[] sheetIds)
    {
        int count = 0;
        for (Long sheetId : sheetIds)
        {
            count += guideSheetService.archiveGuideSheet(sheetId);
        }
        return toAjax(count);
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:remove')")
    @Log(title = "导学单模板归档", businessType = BusinessType.DELETE)
    @PutMapping("/{sheetId}/archive")
    public AjaxResult archiveOne(@PathVariable Long sheetId)
    {
        return toAjax(guideSheetService.archiveGuideSheet(sheetId));
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:add')")
    @Log(title = "复制导学单模板", businessType = BusinessType.INSERT)
    @PostMapping("/{sheetId}/copy")
    public AjaxResult copy(@PathVariable Long sheetId)
    {
        BizGuideSheet source = accessService.requireSelectableTemplate(sheetId);
        GuideSheetVo copy = new GuideSheetVo();
        copy.setSheetTitle(source.getSheetTitle() + " - 副本");
        copy.setGrade(source.getGrade());
        copy.setSemester(source.getSemester());
        copy.setLessonNum(source.getLessonNum());
        copy.setIsPublic("N");
        copy.setFormJson(source.getFormJson());
        copy.setMaxPages(source.getMaxPages());
        copy.setTeacherMachineIp(source.getTeacherMachineIp());
        return success(guideSheetService.saveGuideSheetDetail(copy));
    }

    @PreAuthorize("@ss.hasAnyPermi('business:lesson:query,business:guideSheet:dashboard')")
    @GetMapping("/bindings/{bindingId}")
    public AjaxResult getBinding(@PathVariable Long bindingId)
    {
        return success(accessService.requireBindingManagementAccess(bindingId));
    }

    @PreAuthorize("@ss.hasAnyPermi('business:lesson:query,business:guideSheet:dashboard')")
    @GetMapping("/bindings/{bindingId}/preview")
    public AjaxResult previewBinding(@PathVariable Long bindingId)
    {
        BizLessonGuideSheetBinding binding = accessService.requireBindingManagementAccess(bindingId);
        return success().put("title", binding.getSnapshotTitle())
                .put("formJson", studentViewService.sanitizeFormJson(binding.getSnapshotFormJson()))
                .put("maxPages", binding.getSnapshotMaxPages());
    }

    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/student/current")
    public AjaxResult getStudentGuideSheet()
    {
        BizStudent student = accessService.requireCurrentStudent();
        BizLessonGuideSheetBinding binding = accessService.requireCurrentStudentBinding(student);
        if (binding == null)
        {
            return success().put("hasSheet", false).put("message", "当前课程未开启电子导学单");
        }
        BizGuideSheetAnswer existing = answerService.getByStudentAndBinding(
                student.getStudentId(), binding.getBindingId());
        return success()
                .put("hasSheet", true)
                .put("bindingId", binding.getBindingId())
                .put("sheetTitle", binding.getSnapshotTitle())
                .put("formJson", studentViewService.sanitizeFormJson(binding.getSnapshotFormJson()))
                .put("maxPages", binding.getSnapshotMaxPages())
                .put("websocketPath", classroomWebSocketPath(student))
                .put("existingAnswer", studentAnswerPayload(existing));
    }

    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/student/submit")
    public AjaxResult studentSubmit(@RequestBody Map<String, Object> request)
    {
        BizStudent student = accessService.requireCurrentStudent();
        Long bindingId = longValue(request.get("bindingId"));
        BizLessonGuideSheetBinding binding = accessService.requireStudentBinding(student, bindingId);
        BizGuideSheetAnswer answer = new BizGuideSheetAnswer();
        answer.setBindingId(binding.getBindingId());
        answer.setLessonId(binding.getLessonId());
        answer.setSourceSheetId(binding.getSourceSheetId());
        answer.setStudentId(student.getStudentId());
        answer.setAnswerJson(stringValue(request.get("answerJson")));
        answer.setCurrentPage(integerValue(request.get("currentPage"), 0));
        answer.setDraftRevision(nullableLongValue(request.get("clientRevision")));
        Integer tabIndex = request.get("tabIndex") == null ? null : integerValue(request.get("tabIndex"), 0);
        if ("submit".equals(stringValue(request.get("action"))))
        {
            answerService.submitAnswer(answer, tabIndex);
            return success("提交成功");
        }
        answerService.saveAnswer(answer);
        return success("保存成功").put("savedRevision", answer.getDraftRevision());
    }

    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/student/upload")
    public AjaxResult studentUpload(@RequestParam("file") MultipartFile file,
                                    @RequestParam Long bindingId,
                                    @RequestParam String questionName,
                                    @RequestParam String clientUploadId)
    {
        BizGuideSheetUpload upload = guideSheetUploadService.upload(
                bindingId, questionName, clientUploadId, file);
        return success("上传成功").put("uploadId", upload.getUploadId())
                .put("clientUploadId", upload.getClientUploadId())
                .put("fileName", upload.getFileName())
                .put("fileSize", upload.getFileSize())
                .put("mimeType", upload.getMimeType())
                .put("accessUrl", upload.getAccessUrl());
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:dashboard')")
    @GetMapping("/progress")
    public AjaxResult getProgress(@RequestParam Long bindingId, @RequestParam String entryYear,
                                  @RequestParam String classCode)
    {
        accessService.requireBindingClassAccess(bindingId, entryYear, classCode);
        String normalizedClass = normalizeClassCode(classCode);
        List<GuideSheetProgressVo> list = guideSheetService.getProgress(
                bindingId, SecurityUtils.getDeptId(), entryYear, normalizedClass);
        long submitted = list.stream().filter(item -> "Y".equals(item.getIsSubmitted())).count();
        Double avgScore = answerService.getAvgScore(
                bindingId, SecurityUtils.getDeptId(), entryYear, normalizedClass);
        return success().put("total", list.size()).put("submitted", submitted)
                .put("avgScore", avgScore == null ? 0D : avgScore).put("list", list);
    }

    @PreAuthorize("@ss.hasAnyPermi('business:guideSheet:dashboard,business:guideSheet:edit')")
    @GetMapping("/bindings/{bindingId}/students/{studentId}/answer")
    public AjaxResult getTeacherStudentAnswer(@PathVariable Long bindingId,
                                               @PathVariable Long studentId,
                                               @RequestParam String entryYear,
                                               @RequestParam String classCode)
    {
        accessService.requireBindingClassAccess(bindingId, entryYear, classCode);
        accessService.assertStudentInBindingClass(bindingId, studentId,
                SecurityUtils.getDeptId(), entryYear, classCode);
        BizGuideSheetAnswer answer = answerService.getByStudentAndBinding(studentId, bindingId);
        if (answer == null)
        {
            return success().put("hasAnswer", false);
        }
        return success().put("hasAnswer", true)
                .put("answerJson", answer.getAnswerJson())
                .put("status", answer.getStatus());
    }

    @PreAuthorize("@studentSs.isStudent()")
    @PutMapping("/progress/heartbeat")
    public AjaxResult heartbeat(@RequestBody Map<String, Object> request)
    {
        BizStudent student = accessService.requireCurrentStudent();
        Long bindingId = longValue(request.get("bindingId"));
        accessService.requireStudentBinding(student, bindingId);
        BizGuideSheetProgress progress = new BizGuideSheetProgress();
        progress.setBindingId(bindingId);
        progress.setStudentId(student.getStudentId());
        progress.setDeptId(student.getDeptId());
        progress.setEntryYear(student.getEntryYear());
        progress.setClassCode(student.getClassCode());
        progress.setCurrentPage(integerValue(request.get("currentPage"), 0));
        progress.setIsSubmitted("N");
        progress.setLastHeartbeat(new Date());
        progressMapper.insertOrUpdate(progress);
        return success("心跳上报成功");
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:dashboard')")
    @GetMapping("/uploads")
    public AjaxResult getUploads(@RequestParam Long bindingId, @RequestParam String entryYear,
                                 @RequestParam String classCode)
    {
        accessService.requireBindingClassAccess(bindingId, entryYear, classCode);
        return success(uploadMapper.selectByBindingAndClass(bindingId, SecurityUtils.getDeptId(),
                entryYear, normalizeClassCode(classCode)));
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:export')")
    @Log(title = "导学单成绩", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, @RequestParam Long bindingId,
                       @RequestParam String entryYear, @RequestParam String classCode)
    {
        BizLessonGuideSheetBinding binding = accessService.requireBindingClassAccess(
                bindingId, entryYear, classCode);
        List<GuideSheetProgressVo> progressRows = guideSheetService.getProgress(
                bindingId, SecurityUtils.getDeptId(), entryYear, normalizeClassCode(classCode));
        List<GuideSheetExportVo> rows = new ArrayList<>();
        for (GuideSheetProgressVo progress : progressRows)
        {
            GuideSheetExportVo row = new GuideSheetExportVo();
            row.setBindingId(bindingId);
            row.setStudentName(progress.getStudentName());
            row.setStudentUserName(progress.getStudentUserName());
            row.setStudentNo(progress.getStudentNo());
            row.setEntryYear(progress.getEntryYear());
            row.setClassCode(progress.getClassCode());
            row.setStatus(convertStatus(progress.getAnswerStatus()));
            row.setAutoScore(progress.getAutoScore());
            row.setManualAdjustment(progress.getManualAdjustment());
            row.setTotalScore(progress.getTotalScore());
            row.setGradingStatus(progress.getGradingStatus());
            row.setSubmitTime(progress.getSubmitTime());
            rows.add(row);
        }
        new ExcelUtil<>(GuideSheetExportVo.class).exportExcel(
                response, rows, binding.getSnapshotTitle() + "_电子导学单成绩");
    }

    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/student/grading/{bindingId}")
    public AjaxResult getStudentGrading(@PathVariable Long bindingId)
    {
        BizStudent student = accessService.requireCurrentStudent();
        accessService.requireStudentBinding(student, bindingId);
        BizGuideSheetAnswer answer = answerService.getByStudentAndBinding(student.getStudentId(), bindingId);
        if (answer == null)
        {
            return success().put("hasResult", false);
        }
        return success().put("hasResult", true).put("totalScore", answer.getTotalScore())
                .put("gradingDetail", studentViewService.sanitizeGradingDetail(answer.getGradingDetail()))
                .put("submitTime", answer.getSubmitTime());
    }

    @PreAuthorize("@ss.hasAnyPermi('business:guideSheet:dashboard,business:guideSheet:edit')")
    @PutMapping("/grading/recore/{answerId}")
    public AjaxResult recoreAnswer(@PathVariable Long answerId, @RequestBody Map<String, Object> request)
    {
        BizGuideSheetAnswer answer = answerService.getByAnswerId(answerId);
        if (answer == null || !"2".equals(answer.getStatus()))
        {
            return error("答卷不存在或尚未提交");
        }
        Long bindingId = longValue(request.get("bindingId"));
        if (bindingId == null || !bindingId.equals(answer.getBindingId()))
        {
            return error("答卷与课程导学单绑定不匹配");
        }
        String entryYear = stringValue(request.get("entryYear"));
        String classCode = stringValue(request.get("classCode"));
        BizLessonGuideSheetBinding binding = accessService.requireBindingClassAccess(
                bindingId, entryYear, classCode);
        accessService.assertStudentInBindingClass(bindingId, answer.getStudentId(),
                SecurityUtils.getDeptId(), entryYear, classCode);
        GuideSheetGradingService.GradingResult result = gradingService.grade(
                binding.getSnapshotFormJson(), answer.getAnswerJson(), answer.getStudentId(), bindingId);
        answer.setAutoScore(result.totalScore);
        answer.setManualAdjustment(0);
        answer.setTotalScore(result.totalScore);
        answer.setGradingStatus(result.gradingStatus);
        answer.setGradingDetail(result.gradingDetail);
        answerService.updateGrading(answer);
        return success(result);
    }

    @PreAuthorize("@ss.hasAnyPermi('business:guideSheet:dashboard,business:guideSheet:edit')")
    @PutMapping("/bindings/{bindingId}/grading/manual/{studentId}")
    public AjaxResult saveManualGrades(@PathVariable Long bindingId, @PathVariable Long studentId,
                                       @RequestBody Map<String, Object> request)
    {
        String entryYear = stringValue(request.get("entryYear"));
        String classCode = stringValue(request.get("classCode"));
        accessService.requireBindingClassAccess(bindingId, entryYear, classCode);
        accessService.assertStudentInBindingClass(bindingId, studentId,
                SecurityUtils.getDeptId(), entryYear, classCode);
        Object rawItems = request.get("items");
        if (!(rawItems instanceof List))
        {
            return error("请填写人工评分");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) rawItems;
        BizGuideSheetAnswer answer = answerService.saveManualGrades(bindingId, studentId, items);
        return success().put("autoScore", answer.getAutoScore())
                .put("manualAdjustment", answer.getManualAdjustment())
                .put("totalScore", answer.getTotalScore())
                .put("gradingStatus", answer.getGradingStatus())
                .put("gradingDetail", answer.getGradingDetail());
    }

    @PreAuthorize("@ss.hasAnyPermi('business:guideSheet:dashboard,business:guideSheet:edit')")
    @PutMapping("/grading/recore-all/{bindingId}")
    public AjaxResult recoreAll(@PathVariable Long bindingId, @RequestParam String entryYear,
                                @RequestParam String classCode)
    {
        BizLessonGuideSheetBinding binding = accessService.requireBindingClassAccess(
                bindingId, entryYear, classCode);
        List<BizGuideSheetAnswer> answers = answerService.getByBindingAndClass(
                bindingId, SecurityUtils.getDeptId(), entryYear, normalizeClassCode(classCode));
        int count = 0;
        for (BizGuideSheetAnswer answer : answers)
        {
            if (!"2".equals(answer.getStatus()))
            {
                continue;
            }
            try
            {
                GuideSheetGradingService.GradingResult result = gradingService.grade(
                        binding.getSnapshotFormJson(), answer.getAnswerJson(), answer.getStudentId(), bindingId);
                answer.setAutoScore(result.totalScore);
                answer.setManualAdjustment(0);
                answer.setTotalScore(result.totalScore);
                answer.setGradingStatus(result.gradingStatus);
                answer.setGradingDetail(result.gradingDetail);
                answerService.updateGrading(answer);
                count++;
            }
            catch (Exception e)
            {
                log.error("批量重新评分失败 answerId={}", answer.getAnswerId(), e);
            }
        }
        return success().put("count", count).put("total", answers.size());
    }

    private Map<String, Object> studentAnswerPayload(BizGuideSheetAnswer answer)
    {
        if (answer == null)
        {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answerJson", answer.getAnswerJson());
        result.put("currentPage", answer.getCurrentPage());
        result.put("status", answer.getStatus());
        result.put("draftRevision", answer.getDraftRevision());
        result.put("submitTime", answer.getSubmitTime());
        return result;
    }

    private String classroomWebSocketPath(BizStudent student)
    {
        return "/ws/classroom/" + student.getDeptId() + "/"
                + student.getEntryYear() + "/" + normalizeClassCode(student.getClassCode());
    }

    private String normalizeClassCode(String classCode)
    {
        String normalized = StringUtils.trim(classCode);
        return normalized != null && normalized.endsWith("班")
                ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private Long longValue(Object value)
    {
        if (value == null)
        {
            throw new ServiceException("缺少课程导学单绑定ID");
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Integer integerValue(Object value, int defaultValue)
    {
        return value == null ? defaultValue : Integer.valueOf(String.valueOf(value));
    }

    private Long nullableLongValue(Object value)
    {
        return value == null || StringUtils.isEmpty(String.valueOf(value))
                ? null : Long.valueOf(String.valueOf(value));
    }

    private String stringValue(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }

    private String convertStatus(String status)
    {
        if (status == null || "0".equals(status)) return "未开始";
        if ("1".equals(status)) return "填写中";
        if ("2".equals(status)) return "已提交";
        return status;
    }
}
