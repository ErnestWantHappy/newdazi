package com.ruoyi.business.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.business.domain.BizGuideSheet;
import com.ruoyi.business.domain.BizGuideSheetAnswer;
import com.ruoyi.business.domain.BizGuideSheetProgress;
import com.ruoyi.business.domain.BizGuideSheetUpload;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.vo.GuideSheetExportVo;
import com.ruoyi.business.domain.vo.GuideSheetProgressVo;
import com.ruoyi.business.domain.vo.GuideSheetVo;
import com.ruoyi.business.mapper.BizStudentMapper;
import com.ruoyi.business.mapper.GuideSheetProgressMapper;
import com.ruoyi.business.mapper.GuideSheetUploadMapper;
import com.ruoyi.business.service.IGuideSheetAnswerService;
import com.ruoyi.business.service.IGuideSheetService;
import com.ruoyi.business.service.GuideSheetGradingService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/business/guide-sheet")
public class GuideSheetController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(GuideSheetController.class);

    @Autowired
    private IGuideSheetService guideSheetService;

    @Autowired
    private IGuideSheetAnswerService guideSheetAnswerService;

    @Autowired
    private GuideSheetGradingService gradingService;

    @Autowired
    private BizStudentMapper bizStudentMapper;

    @Autowired
    private GuideSheetUploadMapper guideSheetUploadMapper;

    @Autowired
    private GuideSheetProgressMapper guideSheetProgressMapper;

    @PreAuthorize("@ss.hasPermi('business:guideSheet:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizGuideSheet bizGuideSheet)
    {
        startPage();
        List<BizGuideSheet> list = guideSheetService.selectBizGuideSheetList(bizGuideSheet);
        return getDataTable(list);
    }

    @GetMapping(value = "/{sheetId}")
    public AjaxResult getInfo(@PathVariable("sheetId") Long sheetId)
    {
        GuideSheetVo vo = guideSheetService.selectGuideSheetDetail(sheetId);
        return vo != null ? success(vo) : error("导学单不存在");
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:add')")
    @Log(title = "导学单管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GuideSheetVo vo)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        vo.setCreatorId(loginUser.getUserId());
        vo.setDeptId(loginUser.getUser().getDeptId());
        vo.setCreateBy(loginUser.getUsername());
        guideSheetService.saveGuideSheetDetail(vo);
        return success(vo);
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:edit')")
    @Log(title = "导学单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GuideSheetVo vo)
    {
        if (vo.getSheetId() == null)
        {
            return error("导学单ID不能为空");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        vo.setDeptId(loginUser.getUser().getDeptId());
        guideSheetService.saveGuideSheetDetail(vo);
        return success(vo);
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:remove')")
    @Log(title = "导学单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sheetIds}")
    public AjaxResult remove(@PathVariable Long[] sheetIds)
    {
        return toAjax(guideSheetService.deleteBizGuideSheetBySheetIds(sheetIds));
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:edit')")
    @Log(title = "导学单管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{sheetId}/publish")
    public AjaxResult publish(@PathVariable("sheetId") Long sheetId)
    {
        return toAjax(guideSheetService.publishGuideSheet(sheetId));
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:edit')")
    @Log(title = "导学单管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{sheetId}/close")
    public AjaxResult close(@PathVariable("sheetId") Long sheetId)
    {
        return toAjax(guideSheetService.closeGuideSheet(sheetId));
    }

    @GetMapping("/student/current")
    public AjaxResult getStudentGuideSheet()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            return error("用户未登录");
        }
        Long deptId = loginUser.getDeptId();
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null)
        {
            return error("您不是学生用户");
        }
        GuideSheetVo vo = guideSheetService.getStudentGuideSheet(
                deptId, student.getEntryYear(), student.getClassCode());
        if (vo == null)
        {
            return AjaxResult.success().put("hasSheet", false).put("message", "暂无导学单");
        }
        BizGuideSheetAnswer existingAnswer = guideSheetAnswerService.getByStudentAndSheet(
                student.getStudentId(), vo.getSheetId());
        return AjaxResult.success()
                .put("hasSheet", true)
                .put("sheetId", vo.getSheetId())
                .put("sheetTitle", vo.getSheetTitle())
                .put("formJson", vo.getFormJson())
                .put("maxPages", vo.getMaxPages())
                .put("teacherMachineIp", vo.getTeacherMachineIp())
                .put("existingAnswer", existingAnswer);
    }

    @PostMapping("/student/submit")
    public AjaxResult studentSubmit(@RequestBody Map<String, Object> request)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            return error("用户未登录");
        }
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null)
        {
            return error("您不是学生用户");
        }
        Long sheetId = request.get("sheetId") != null
                ? Long.valueOf(request.get("sheetId").toString()) : null;
        String answerJson = request.get("answerJson") != null
                ? request.get("answerJson").toString() : null;
        Integer currentPage = request.get("currentPage") != null
                ? Integer.valueOf(request.get("currentPage").toString()) : 0;
        String action = request.get("action") != null
                ? request.get("action").toString() : "save";

        if (sheetId == null)
        {
            return error("参数错误");
        }

        BizGuideSheetAnswer answer = new BizGuideSheetAnswer();
        answer.setSheetId(sheetId);
        answer.setStudentId(student.getStudentId());
        answer.setAnswerJson(answerJson);
        answer.setCurrentPage(currentPage);

        if ("submit".equals(action))
        {
            guideSheetAnswerService.submitAnswer(answer);
            return success("提交成功");
        }
        else
        {
            guideSheetAnswerService.saveAnswer(answer);
            return success("保存成功");
        }
    }

    @PostMapping("/student/upload-confirm")
    public AjaxResult studentUploadConfirm(@RequestBody BizGuideSheetUpload upload)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            return error("用户未登录");
        }
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null)
        {
            return error("您不是学生用户");
        }
        upload.setStudentId(student.getStudentId());
        upload.setUploadTime(new Date());
        guideSheetUploadMapper.insertBizGuideSheetUpload(upload);
        return success("上传记录已保存");
    }

    @GetMapping("/progress")
    public AjaxResult getProgress(@RequestParam("sheetId") Long sheetId,
                                   @RequestParam(value = "classCode", required = false) String classCode)
    {
        List<GuideSheetProgressVo> list = guideSheetService.getProgress(sheetId, classCode);
        int total = list.size();
        long submitted = list.stream().filter(p -> "Y".equals(p.getIsSubmitted())).count();
        return success()
                .put("total", total)
                .put("submitted", submitted)
                .put("list", list);
    }

    @PutMapping("/progress/heartbeat")
    public AjaxResult heartbeat(@RequestBody Map<String, Object> request)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            return error("用户未登录");
        }
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null)
        {
            return error("您不是学生用户");
        }
        Long sheetId = request.get("sheetId") != null
                ? Long.valueOf(request.get("sheetId").toString()) : null;
        Integer currentPage = request.get("currentPage") != null
                ? Integer.valueOf(request.get("currentPage").toString()) : 0;

        if (sheetId == null)
        {
            return error("参数错误");
        }

        BizGuideSheetProgress progress = new BizGuideSheetProgress();
        progress.setSheetId(sheetId);
        progress.setStudentId(student.getStudentId());
        progress.setClassCode(student.getClassCode());
        progress.setCurrentPage(currentPage);
        progress.setIsSubmitted("N");
        progress.setLastHeartbeat(new Date());
        guideSheetProgressMapper.insertOrUpdate(progress);
        return success("心跳上报成功");
    }

    @GetMapping("/uploads")
    public AjaxResult getUploads(@RequestParam("sheetId") Long sheetId,
                                  @RequestParam(value = "classCode", required = false) String classCode)
    {
        List<BizGuideSheetUpload> list;
        if (classCode != null && !classCode.isEmpty())
        {
            list = guideSheetUploadMapper.selectBySheetAndClass(sheetId, classCode);
        }
        else
        {
            list = guideSheetUploadMapper.selectBySheetId(sheetId);
        }
        return success(list);
    }

    /**
     * 导出导学单答案数据为 Excel
     */
    @PreAuthorize("@ss.hasPermi('business:guideSheet:export')")
    @Log(title = "导学单管理", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam("sheetId") Long sheetId,
                       @RequestParam(value = "classCode", required = false) String classCode)
    {
        List<BizGuideSheetAnswer> answerList;
        if (classCode != null && !classCode.isEmpty())
        {
            answerList = guideSheetAnswerService.getBySheetIdByClassCode(sheetId, classCode);
        }
        else
        {
            answerList = guideSheetAnswerService.getBySheetId(sheetId);
        }

        // 获取导学单标题用于 Sheet 名
        BizGuideSheet sheet = guideSheetService.getBySheetId(sheetId);
        String sheetName = sheet != null ? sheet.getSheetTitle() : "导学单答案";
        String exportName = sheetName + "_答案数据";

        // 构建导出 VO（含学生姓名、学号、状态、分数等可读字段）
        List<GuideSheetExportVo> exportList = new ArrayList<>();
        for (BizGuideSheetAnswer answer : answerList)
        {
            BizStudent student = bizStudentMapper.selectBizStudentByStudentId(answer.getStudentId());
            GuideSheetExportVo vo = new GuideSheetExportVo();
            vo.setStudentName(student != null ? student.getStudentName() : "未知");
            vo.setStudentNo(student != null ? student.getStudentNo() : "");
            vo.setClassCode(student != null ? student.getClassCode() : "");
            vo.setStatus(convertStatus(answer.getStatus()));
            vo.setTotalScore(answer.getTotalScore() != null ? answer.getTotalScore() : 0);
            vo.setGradingStatus(answer.getGradingStatus() != null ? answer.getGradingStatus() : "pending");
            vo.setSubmitTime(answer.getSubmitTime());
            exportList.add(vo);
        }

        ExcelUtil<GuideSheetExportVo> util = new ExcelUtil<>(GuideSheetExportVo.class);
        util.exportExcel(response, exportList, exportName);
    }

    private String convertStatus(String status)
    {
        if (status == null) return "未开始";
        switch (status)
        {
            case "0": return "未开始";
            case "1": return "填写中";
            case "2": return "已提交";
            default: return status;
        }
    }

    /**
     * 学生获取自己的评分结果
     */
    @GetMapping("/student/grading/{sheetId}")
    public AjaxResult getStudentGrading(@PathVariable Long sheetId)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        BizStudent student = bizStudentMapper.selectBizStudentByUserId(loginUser.getUserId());
        if (student == null)
        {
            return error("您不是学生用户");
        }
        BizGuideSheetAnswer existing = guideSheetAnswerService.getByStudentAndSheet(
                student.getStudentId(), sheetId);
        if (existing == null)
        {
            return success().put("hasResult", false);
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("hasResult", true);
        result.put("totalScore", existing.getTotalScore());
        result.put("gradingStatus", existing.getGradingStatus());
        result.put("gradingDetail", existing.getGradingDetail());
        result.put("submitTime", existing.getSubmitTime());
        return success(result);
    }

    /**
     * 教师手动重新评分（触发指定答案重新走评分引擎）
     */
    @PreAuthorize("@ss.hasPermi('business:guideSheet:edit')")
    @PutMapping("/grading/recore/{answerId}")
    public AjaxResult recoreAnswer(@PathVariable Long answerId)
    {
        BizGuideSheetAnswer answer = guideSheetAnswerService.getByAnswerId(answerId);
        if (answer == null)
        {
            return error("答题记录不存在");
        }
        if (!"2".equals(answer.getStatus()))
        {
            return error("该学生尚未提交导学单");
        }
        BizGuideSheet sheet = guideSheetService.getBySheetId(answer.getSheetId());
        if (sheet == null)
        {
            return error("导学单不存在");
        }
        try
        {
            GuideSheetGradingService.GradingResult gradingResult = gradingService.grade(
                    sheet.getFormJson(), answer.getAnswerJson());
            answer.setTotalScore(gradingResult.totalScore);
            answer.setGradingStatus(gradingResult.gradingStatus);
            answer.setGradingDetail(gradingResult.gradingDetail);
            guideSheetAnswerService.updateGrading(answer);
            return success(gradingResult);
        }
        catch (Exception e)
        {
            log.error("重新评分失败 answerId={}", answerId, e);
            return error("评分失败: " + e.getMessage());
        }
    }

    /**
     * 教师批量重新评分导学单下所有已提交答案
     */
    @PreAuthorize("@ss.hasPermi('business:guideSheet:edit')")
    @PutMapping("/grading/recore-all/{sheetId}")
    public AjaxResult recoreAllAnswers(@PathVariable Long sheetId)
    {
        BizGuideSheet sheet = guideSheetService.getBySheetId(sheetId);
        if (sheet == null)
        {
            return error("导学单不存在");
        }
        List<BizGuideSheetAnswer> answerList = guideSheetAnswerService.getBySheetId(sheetId);
        int count = 0;
        for (BizGuideSheetAnswer answer : answerList)
        {
            if (!"2".equals(answer.getStatus())) continue;
            try
            {
                GuideSheetGradingService.GradingResult gradingResult = gradingService.grade(
                        sheet.getFormJson(), answer.getAnswerJson());
                answer.setTotalScore(gradingResult.totalScore);
                answer.setGradingStatus(gradingResult.gradingStatus);
                answer.setGradingDetail(gradingResult.gradingDetail);
                guideSheetAnswerService.updateGrading(answer);
                count++;
            }
            catch (Exception e)
            {
                log.error("批量评分失败 answerId={}", answer.getAnswerId(), e);
            }
        }
        return success().put("count", count).put("total", answerList.size());
    }
}
