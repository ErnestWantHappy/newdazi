package com.ruoyi.business.controller;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.security.QuestionReferenceAnswerPolicy;
import com.ruoyi.business.service.IBizQuestionService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 题库管理Controller
 * 
 * @author zdx
 * @date 2025-08-18
 */
@RestController
@RequestMapping("/business/question")
public class BizQuestionController extends BaseController
{
    @Autowired
    private IBizQuestionService bizQuestionService;
    @Autowired private com.ruoyi.business.service.QuestionSaveService questionSaveService;
    @Autowired private com.ruoyi.business.service.QuestionWorkbookWriter questionWorkbookWriter;
    @Autowired private com.ruoyi.business.service.QuestionWorkbookReader questionWorkbookReader;

    /**
     * 查询题库管理列表
     */
    @PreAuthorize("@ss.hasPermi('business:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizQuestion bizQuestion)
    {
        startPage();
        List<BizQuestion> list = bizQuestionService.selectBizQuestionList(bizQuestion);
        trimReferenceAnswers(list);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:question:add') or @ss.hasPermi('business:question:edit')")
    @GetMapping("/save-receipts/{requestId}")
    public AjaxResult saveReceipt(@PathVariable String requestId) { return success(questionSaveService.lookup(requestId)); }

    /**
     * 导出题库管理列表（自动过滤操作题，操作题包含附件无法导出）
     */
    @PreAuthorize("@ss.hasPermi('business:question:export')")
    @Log(title = "题库管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizQuestion bizQuestion) throws java.io.IOException
    {
        List<BizQuestion> list = bizQuestionService.selectBizQuestionList(bizQuestion);
        // 过滤掉操作题（操作题包含附件无法导出）
        int practicalCount = 0;
        List<BizQuestion> exportList = new ArrayList<>();
        for (BizQuestion q : list) {
            if ("practical".equals(q.getQuestionType())) {
                practicalCount++;
            } else {
                exportList.add(q);
            }
        }
        if (exportList.stream().anyMatch(q -> com.ruoyi.business.utils.QuestionMediaCodec.hasImages(q.getMediaJson()))) {
            questionWorkbookWriter.export(response,exportList);
            return;
        }
        ExcelUtil<BizQuestion> util = new ExcelUtil<BizQuestion>(BizQuestion.class);
        String sheetName = practicalCount > 0 
            ? "题库数据（已过滤" + practicalCount + "道操作题）" 
            : "题库管理数据";
        util.exportExcel(response, exportList, sheetName);
    }

    /**
     * 获取题库管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:question:query')")
    @GetMapping(value = "/{questionId}")
    public AjaxResult getInfo(@PathVariable("questionId") Long questionId)
    {
        BizQuestion question = bizQuestionService.selectBizQuestionByQuestionId(questionId);
        trimReferenceAnswer(question);
        return success(question);
    }

    private void trimReferenceAnswers(List<BizQuestion> questions)
    {
        if (questions == null || questions.isEmpty())
        {
            return;
        }
        Long viewerUserId = SecurityUtils.getUserId();
        boolean admin = SecurityUtils.isAdmin(viewerUserId);
        for (BizQuestion question : questions)
        {
            QuestionReferenceAnswerPolicy.apply(question, viewerUserId, admin);
        }
    }

    private void trimReferenceAnswer(BizQuestion question)
    {
        Long viewerUserId = SecurityUtils.getUserId();
        QuestionReferenceAnswerPolicy.apply(question, viewerUserId, SecurityUtils.isAdmin(viewerUserId));
    }

    /**
     * 新增题库管理
     */
    @PreAuthorize("@ss.hasPermi('business:question:add')")
    @Log(title = "题库管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizQuestion bizQuestion)
    {
        java.util.Map<String,Object> saved=questionSaveService.save(bizQuestion,true);
        return success(saved).put("questionId",saved.get("questionId")).put("saveMode",saved.get("saveMode"));
    }

    /**
     * 修改题库管理
     */
    @PreAuthorize("@ss.hasPermi('business:question:edit')")
    @Log(title = "题库管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizQuestion bizQuestion)
    {
        java.util.Map<String,Object> saved=questionSaveService.save(bizQuestion,false);
        return success(saved).put("questionId",saved.get("questionId")).put("saveMode",saved.get("saveMode"));
    }

    /**
     * 删除题库管理
     */
    @PreAuthorize("@ss.hasPermi('business:question:remove')")
    @Log(title = "题库管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{questionIds}")
    public AjaxResult remove(@PathVariable Long[] questionIds)
    {
        int result = bizQuestionService.deleteBizQuestionByQuestionIds(questionIds);
        if (result == -1) {
            return AjaxResult.error("无权删除他人创建的题目");
        }
        return toAjax(result);
    }

    @Log(title = "题库管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('business:question:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        if(questionWorkbookReader.hasArtwork(file.getBytes())) return error("此文件包含图片，请使用题库的Excel图文导入预览，确认后导入，避免丢失图片");
        ExcelUtil<BizQuestion> util = new ExcelUtil<BizQuestion>(BizQuestion.class);
        List<BizQuestion> questionList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = bizQuestionService.importQuestion(questionList, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) throws java.io.IOException
    {
        questionWorkbookWriter.template(response);
    }
}
