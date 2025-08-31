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
import com.ruoyi.business.service.IBizQuestionService;
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

    /**
     * 查询题库管理列表
     */
    @PreAuthorize("@ss.hasPermi('business:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizQuestion bizQuestion)
    {
        startPage();
        List<BizQuestion> list = bizQuestionService.selectBizQuestionList(bizQuestion);
        return getDataTable(list);
    }

    /**
     * 导出题库管理列表
     */
    @PreAuthorize("@ss.hasPermi('business:question:export')")
    @Log(title = "题库管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizQuestion bizQuestion)
    {
        List<BizQuestion> list = bizQuestionService.selectBizQuestionList(bizQuestion);
        ExcelUtil<BizQuestion> util = new ExcelUtil<BizQuestion>(BizQuestion.class);
        util.exportExcel(response, list, "题库管理数据");
    }

    /**
     * 获取题库管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:question:query')")
    @GetMapping(value = "/{questionId}")
    public AjaxResult getInfo(@PathVariable("questionId") Long questionId)
    {
        return success(bizQuestionService.selectBizQuestionByQuestionId(questionId));
    }

    /**
     * 新增题库管理
     */
    @PreAuthorize("@ss.hasPermi('business:question:add')")
    @Log(title = "题库管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizQuestion bizQuestion)
    {
        return toAjax(bizQuestionService.insertBizQuestion(bizQuestion));
    }

    /**
     * 修改题库管理
     */
    @PreAuthorize("@ss.hasPermi('business:question:edit')")
    @Log(title = "题库管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizQuestion bizQuestion)
    {
        return toAjax(bizQuestionService.updateBizQuestion(bizQuestion));
    }

    /**
     * 删除题库管理
     */
    @PreAuthorize("@ss.hasPermi('business:question:remove')")
    @Log(title = "题库管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{questionIds}")
    public AjaxResult remove(@PathVariable Long[] questionIds)
    {
        return toAjax(bizQuestionService.deleteBizQuestionByQuestionIds(questionIds));
    }

    @Log(title = "题库管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('business:question:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        ExcelUtil<BizQuestion> util = new ExcelUtil<BizQuestion>(BizQuestion.class);
        List<BizQuestion> questionList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = bizQuestionService.importQuestion(questionList, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<BizQuestion> util = new ExcelUtil<BizQuestion>(BizQuestion.class);
        List<BizQuestion> exampleList = new ArrayList<>();

        BizQuestion choiceExample = new BizQuestion();
        choiceExample.setQuestionType("choice");
        choiceExample.setQuestionContent("中国的首都是哪里？");
        choiceExample.setGrade(7L);
        choiceExample.setSemester("0");
        choiceExample.setOptionA("北京");
        choiceExample.setOptionB("上海");
        choiceExample.setOptionC("广州");
        choiceExample.setOptionD("深圳");
        choiceExample.setAnswer("A");
        choiceExample.setIsPublic("Y"); // 核心修正：使用 "Y"
        choiceExample.setRemark("选择题示例：请填写 题目类型(choice), 题干, 年级(数字), 学期(0上册/1下册), 4个选项, 标准答案(A/B/C/D), 是否公开(Y是/N否)。");
        exampleList.add(choiceExample);

        BizQuestion typingExample = new BizQuestion();
        typingExample.setQuestionType("typing");
        typingExample.setQuestionContent("请输入这段用于打字练习的文字。");
        typingExample.setGrade(8L);
        typingExample.setSemester("1");
        typingExample.setIsPublic("Y"); // 核心修正：使用 "Y"
        typingExample.setRemark("打字题示例：请填写 题目类型(typing), 打字题内容, 年级, 学期, 是否公开(Y是/N否)。其他项可留空。");
        exampleList.add(typingExample);

        BizQuestion judgmentExample = new BizQuestion();
        judgmentExample.setQuestionType("judgment");
        judgmentExample.setQuestionContent("地球是平的。");
        judgmentExample.setGrade(7L);
        judgmentExample.setSemester("0");
        judgmentExample.setAnswer("F");
        judgmentExample.setIsPublic("N"); // 核心修正：使用 "N"
        judgmentExample.setRemark("判断题示例：请填写 题目类型(judgment), 题干, 年级, 学期, 标准答案(T正确/F错误), 是否公开(Y是/N否)。");
        exampleList.add(judgmentExample);



        util.exportExcel(response, exampleList, "题目导入模板");
    }
}
