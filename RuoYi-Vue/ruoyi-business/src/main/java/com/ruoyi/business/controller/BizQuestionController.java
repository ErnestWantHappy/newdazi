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

        // ========== 选择题示例 (2条) ==========
        BizQuestion choiceExample1 = new BizQuestion();
        choiceExample1.setQuestionType("choice");
        choiceExample1.setQuestionContent("【选择题】中国的首都是哪里？");
        choiceExample1.setGrade(6L);
        choiceExample1.setSemester("0");
        choiceExample1.setOptionA("北京");
        choiceExample1.setOptionB("上海");
        choiceExample1.setOptionC("广州");
        choiceExample1.setOptionD("深圳");
        choiceExample1.setAnswer("A");
        choiceExample1.setIsPublic("Y");
        choiceExample1.setTypingDuration(null);
        choiceExample1.setRemark("★选择题必填：题目类型写choice，填4个选项，答案填A/B/C/D");
        exampleList.add(choiceExample1);

        BizQuestion choiceExample2 = new BizQuestion();
        choiceExample2.setQuestionType("choice");
        choiceExample2.setQuestionContent("下列哪个不是编程语言？");
        choiceExample2.setGrade(6L);
        choiceExample2.setSemester("1");
        choiceExample2.setOptionA("Python");
        choiceExample2.setOptionB("Java");
        choiceExample2.setOptionC("Word");
        choiceExample2.setOptionD("Scratch");
        choiceExample2.setAnswer("C");
        choiceExample2.setIsPublic("N");
        choiceExample2.setTypingDuration(null);
        choiceExample2.setRemark("");
        exampleList.add(choiceExample2);

        // ========== 判断题示例 (2条) ==========
        BizQuestion judgmentExample1 = new BizQuestion();
        judgmentExample1.setQuestionType("judgment");
        judgmentExample1.setQuestionContent("【判断题】计算机病毒可以通过网络传播。");
        judgmentExample1.setGrade(5L);
        judgmentExample1.setSemester("0");
        judgmentExample1.setOptionA("");
        judgmentExample1.setOptionB("");
        judgmentExample1.setOptionC("");
        judgmentExample1.setOptionD("");
        judgmentExample1.setAnswer("T");
        judgmentExample1.setIsPublic("Y");
        judgmentExample1.setTypingDuration(null);
        judgmentExample1.setRemark("★判断题必填：题目类型写judgment，答案填T(正确)或F(错误)，选项留空");
        exampleList.add(judgmentExample1);

        BizQuestion judgmentExample2 = new BizQuestion();
        judgmentExample2.setQuestionType("judgment");
        judgmentExample2.setQuestionContent("CPU是计算机的存储器。");
        judgmentExample2.setGrade(5L);
        judgmentExample2.setSemester("1");
        judgmentExample2.setOptionA("");
        judgmentExample2.setOptionB("");
        judgmentExample2.setOptionC("");
        judgmentExample2.setOptionD("");
        judgmentExample2.setAnswer("F");
        judgmentExample2.setIsPublic("N");
        judgmentExample2.setTypingDuration(null);
        judgmentExample2.setRemark("");
        exampleList.add(judgmentExample2);

        // ========== 打字题示例 (2条) ==========
        BizQuestion typingExample1 = new BizQuestion();
        typingExample1.setQuestionType("typing");
        typingExample1.setQuestionContent("【打字题】春眠不觉晓，处处闻啼鸟。夜来风雨声，花落知多少。");
        typingExample1.setGrade(4L);
        typingExample1.setSemester("0");
        typingExample1.setOptionA("");
        typingExample1.setOptionB("");
        typingExample1.setOptionC("");
        typingExample1.setOptionD("");
        typingExample1.setAnswer("");
        typingExample1.setIsPublic("Y");
        typingExample1.setTypingDuration(3);
        typingExample1.setRemark("★打字题必填：题目类型写typing，题目内容填打字文章，打字时长(分钟)必填，选项和答案留空");
        exampleList.add(typingExample1);

        BizQuestion typingExample2 = new BizQuestion();
        typingExample2.setQuestionType("typing");
        typingExample2.setQuestionContent("信息技术是指利用计算机和网络对信息进行获取、处理、存储和传递的技术。");
        typingExample2.setGrade(4L);
        typingExample2.setSemester("1");
        typingExample2.setOptionA("");
        typingExample2.setOptionB("");
        typingExample2.setOptionC("");
        typingExample2.setOptionD("");
        typingExample2.setAnswer("");
        typingExample2.setIsPublic("N");
        typingExample2.setTypingDuration(5);
        typingExample2.setRemark("");
        exampleList.add(typingExample2);

        util.exportExcel(response, exampleList, "题目导入模板");
    }
}
