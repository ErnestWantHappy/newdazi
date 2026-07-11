package com.ruoyi.business.controller;

import com.ruoyi.business.domain.CountyExam;
import com.ruoyi.business.domain.CountyExamClass;
import com.ruoyi.business.domain.CountyExamQuestion;
import com.ruoyi.business.domain.dto.CountyExamGradeRequest;
import com.ruoyi.business.domain.dto.CountyExamGraderAllocateRequest;
import com.ruoyi.business.domain.dto.CountyExamSubmitRequest;
import com.ruoyi.business.service.ICountyExamService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 区域抽测 Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/countyExam")
public class CountyExamController extends BaseController {

    @Autowired
    private ICountyExamService countyExamService;

    /**
     * 查询区域抽测列表
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/list")
    public TableDataInfo list(CountyExam countyExam) {
        startPage();
        List<CountyExam> list = countyExamService.selectCountyExamList(countyExam);
        return getDataTable(list);
    }

    /**
     * 获取区域抽测详情
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/{examId}")
    public AjaxResult getInfo(@PathVariable Long examId) {
        return success(countyExamService.getExamDetail(examId));
    }

    /**
     * 新增区域抽测
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "区域抽测", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CountyExam countyExam) {
        return toAjax(countyExamService.insertCountyExam(countyExam));
    }

    /**
     * 修改区域抽测
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "区域抽测", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CountyExam countyExam) {
        return toAjax(countyExamService.updateCountyExam(countyExam));
    }

    /**
     * 删除区域抽测
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "区域抽测", businessType = BusinessType.DELETE)
    @DeleteMapping("/{examIds}")
    public AjaxResult remove(@PathVariable Long[] examIds) {
        return toAjax(countyExamService.deleteCountyExamByIds(examIds));
    }

    /**
     * 保存组卷题目
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "区域抽测组卷", businessType = BusinessType.UPDATE)
    @PostMapping("/{examId}/questions")
    public AjaxResult saveQuestions(@PathVariable Long examId, @RequestBody List<CountyExamQuestion> questions) {
        return toAjax(countyExamService.saveQuestions(examId, questions));
    }

    /**
     * 保存参考班级
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "区域抽测班级", businessType = BusinessType.UPDATE)
    @PostMapping("/{examId}/classes")
    public AjaxResult saveClasses(@PathVariable Long examId, @RequestBody List<CountyExamClass> classes) {
        return toAjax(countyExamService.saveClasses(examId, classes));
    }

    /**
     * 查询可指派班级
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/classes/assignable")
    public AjaxResult assignableClasses(@RequestParam(required = false) String schoolType) {
        return success(countyExamService.getAssignableClasses(schoolType));
    }

    /**
     * 手动开启区域抽测
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "开启区域抽测", businessType = BusinessType.UPDATE)
    @PostMapping("/{examId}/open")
    public AjaxResult open(@PathVariable Long examId, @RequestBody(required = false) CountyExam countyExam) {
        return success(countyExamService.openExam(examId, countyExam == null ? null : countyExam.getDurationMinutes()));
    }

    /**
     * 手动关闭区域抽测
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "关闭区域抽测", businessType = BusinessType.UPDATE)
    @PostMapping("/{examId}/close")
    public AjaxResult close(@PathVariable Long examId) {
        return success(countyExamService.closeExam(examId));
    }

    /**
     * 分配匿名评卷任务
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "区域抽测评卷分配", businessType = BusinessType.UPDATE)
    @PostMapping("/{examId}/graders/allocate")
    public AjaxResult allocateGraders(@PathVariable Long examId, @RequestBody CountyExamGraderAllocateRequest request) {
        return success(countyExamService.allocateGraders(examId, request));
    }

    /**
     * 重置并重新生成匿名评卷任务
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "区域抽测评卷重置", businessType = BusinessType.UPDATE)
    @PostMapping("/{examId}/graders/reset")
    public AjaxResult resetGraders(@PathVariable Long examId) {
        return success(countyExamService.resetGraders(examId));
    }

    /**
     * 开启匿名评卷入口
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "开启区域抽测评卷", businessType = BusinessType.UPDATE)
    @PostMapping("/{examId}/grading/enable")
    public AjaxResult enableGrading(@PathVariable Long examId) {
        return success(countyExamService.updateGradingEnabled(examId, true));
    }

    /**
     * 关闭匿名评卷入口
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "关闭区域抽测评卷", businessType = BusinessType.UPDATE)
    @PostMapping("/{examId}/grading/disable")
    public AjaxResult disableGrading(@PathVariable Long examId) {
        return success(countyExamService.updateGradingEnabled(examId, false));
    }

    /**
     * 查询可选评卷教师
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/graders/assignable")
    public AjaxResult assignableGraders(@RequestParam(required = false) String keyword) {
        return success(countyExamService.getAssignableGraders(keyword));
    }

    /**
     * 发布区域抽测成绩
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @Log(title = "发布区域抽测", businessType = BusinessType.UPDATE)
    @PostMapping("/{examId}/publish")
    public AjaxResult publish(@PathVariable Long examId) {
        return success(countyExamService.publishExam(examId));
    }

    /**
     * 学校汇总
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/{examId}/summary")
    public AjaxResult summary(@PathVariable Long examId) {
        return success(countyExamService.getSummary(examId));
    }

    /**
     * 学生明细
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/{examId}/students")
    public TableDataInfo students(@PathVariable Long examId, @RequestParam(required = false) String keyword) {
        startPage();
        return getDataTable(countyExamService.getStudents(examId, keyword));
    }

    /**
     * 导出学生成绩
     */
    @PreAuthorize("@ss.hasAnyRoles('admin,researcher')")
    @GetMapping("/{examId}/export")
    public void export(@PathVariable Long examId, HttpServletResponse response) {
        countyExamService.exportStudents(examId, response);
    }

    /**
     * 学生检测当前开启抽测，不启动个人计时
     */
    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/student/check")
    public AjaxResult checkStudentExam() {
        return success(countyExamService.checkCurrentStudentExam());
    }

    /**
     * 学生查询当前开启抽测
     */
    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/student/current")
    public AjaxResult currentStudentExam() {
        return success(countyExamService.getCurrentStudentExam());
    }

    /**
     * 学生保存草稿
     */
    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/student/draft")
    public AjaxResult saveStudentDraft(@RequestBody CountyExamSubmitRequest request) {
        return success(countyExamService.saveStudentDraft(request));
    }

    /**
     * 学生最终提交
     */
    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/student/submit")
    public AjaxResult submitStudentExam(@RequestBody CountyExamSubmitRequest request) {
        return success(countyExamService.submitStudentExam(request));
    }

    /**
     * 学生上传区域抽测作品，使用匿名文件名避免评卷端泄露身份。
     */
    @PreAuthorize("@studentSs.isStudent()")
    @PostMapping("/student/upload")
    public AjaxResult uploadStudentWork(@RequestParam("examId") Long examId,
                                        @RequestParam("questionId") Long questionId,
                                        @RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空，请重新选择文件");
        }
        Long studentId = countyExamService.validateStudentWorkUpload(examId, questionId);
        String scopedUploadPath = RuoYiConfig.getUploadPath() + "/county-exam/" + examId
                + "/" + studentId + "/" + questionId;
        String fileName = FileUploadUtils.upload(
                scopedUploadPath, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION, true);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("fileName", fileName);
        ajax.put("newFileName", FileUtils.getName(fileName));
        return ajax;
    }

    /**
     * 教师评卷入口状态
     */
    @PreAuthorize("@ss.hasRole('teacher')")
    @GetMapping("/grading/entry")
    public AjaxResult gradingEntry() {
        return success(countyExamService.getGradingEntry());
    }

    /**
     * 我的匿名评卷任务
     */
    @PreAuthorize("@ss.hasRole('teacher')")
    @GetMapping("/grading/tasks")
    public AjaxResult gradingTasks(@RequestParam(required = false) String gradingStatus) {
        return success(countyExamService.getGradingTasks(gradingStatus));
    }

    /**
     * 匿名答卷详情
     */
    @PreAuthorize("@ss.hasRole('teacher')")
    @GetMapping("/grading/answers/{answerId}")
    public AjaxResult gradingAnswer(@PathVariable Long answerId) {
        return success(countyExamService.getGradingAnswer(answerId));
    }

    /**
     * 提交匿名评分
     */
    @PreAuthorize("@ss.hasRole('teacher')")
    @Log(title = "区域抽测匿名评卷", businessType = BusinessType.UPDATE)
    @PostMapping("/grading/grade")
    public AjaxResult grade(@RequestBody CountyExamGradeRequest request) {
        return success(countyExamService.gradeAnswer(request));
    }
}
