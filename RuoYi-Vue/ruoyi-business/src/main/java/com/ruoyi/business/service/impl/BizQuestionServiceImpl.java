package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.BizScoringItem; // P6 import
import com.ruoyi.business.mapper.BizQuestionMapper;
import com.ruoyi.business.mapper.BizScoringItemMapper; // P6 import
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.business.mapper.ProgrammingJudgeMapper;
import com.ruoyi.business.mapper.FlowchartMapper;
import com.ruoyi.business.domain.PracticalQuestionMaterial;
import com.ruoyi.business.service.AsyncConversionService;
import com.ruoyi.business.service.AnswerDeletionGuardService;
import com.ruoyi.business.service.IBizQuestionService;
import com.ruoyi.business.utils.FileConversionUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants; // 导入常量类
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 题库管理Service业务层处理 (集成PDF转换与字数统计功能)
 * @author ruoyi
 */
@Service
public class BizQuestionServiceImpl implements IBizQuestionService
{
    private static final Logger log = LoggerFactory.getLogger(BizQuestionServiceImpl.class);

    @Autowired
    private BizQuestionMapper bizQuestionMapper;

    @Autowired
    private BizScoringItemMapper bizScoringItemMapper; // P6 mapper

    @Autowired
    private ProgrammingJudgeMapper programmingJudgeMapper;

    @Autowired
    private AsyncConversionService asyncConversionService;

    @Autowired
    private PracticalArtifactMapper practicalArtifactMapper;

    @Autowired
    private AnswerDeletionGuardService answerDeletionGuardService;

    @Autowired
    private FlowchartMapper flowchartMapper;

    @Override
    public BizQuestion selectBizQuestionByQuestionId(Long questionId) {
        BizQuestion question = bizQuestionMapper.selectBizQuestionByQuestionId(questionId);
        // P6: 查询评分项 (仅操作题)
        if (isFilePractical(question)) {
            question.setScoringItems(bizScoringItemMapper.selectItemsByQuestion(questionId));
            question.setPracticalMaterials(practicalArtifactMapper.selectMaterialsByQuestion(questionId));
        }
        return question;
    }

    @Override
    public List<BizQuestion> selectBizQuestionList(BizQuestion bizQuestion) {
        // 权限过滤：非管理员只能看到公共题目和自己的私有题目
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
            bizQuestion.setCreatorId(SecurityUtils.getUserId());
        }
        return bizQuestionMapper.selectBizQuestionList(bizQuestion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBizQuestion(BizQuestion bizQuestion)
    {
        bizQuestion.setCreateTime(DateUtils.getNowDate());
        bizQuestion.setCreatorId(SecurityUtils.getUserId());
        bizQuestion.setCreateBy(SecurityUtils.getUsername());

        processQuestionByType(bizQuestion);

        int rows = bizQuestionMapper.insertBizQuestion(bizQuestion);
        
        // P6: 保存评分项
        insertScoringItems(bizQuestion);
        replacePracticalMaterials(bizQuestion);
        
        // 操作题异步转换
        triggerAsyncConversionIfNeeded(bizQuestion);
        
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBizQuestion(BizQuestion bizQuestion)
    {
        // 权限校验：非管理员只能编辑自己创建的题目
        Long currentUserId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(currentUserId);
        
        if (!isAdmin) {
            BizQuestion existing = bizQuestionMapper.selectBizQuestionByQuestionId(bizQuestion.getQuestionId());
            if (existing != null && !currentUserId.equals(existing.getCreatorId())) {
                // 返回-1表示无权限，由Controller层处理
                return -1;
            }
        }
        
        bizQuestion.setUpdateTime(DateUtils.getNowDate());
        bizQuestion.setUpdateBy(SecurityUtils.getUsername());

        processQuestionByType(bizQuestion);

        int rows = bizQuestionMapper.updateBizQuestion(bizQuestion);
        
        // P6: 更新评分项 (先删后增)
        bizScoringItemMapper.deleteBizScoringItemByQuestion(bizQuestion.getQuestionId());
        insertScoringItems(bizQuestion);
        replacePracticalMaterials(bizQuestion);
        
        // 操作题异步转换
        triggerAsyncConversionIfNeeded(bizQuestion);
        
        return rows;
    }

    /**
     * P6: 自定义辅助方法：批量保存评分项
     */
    private void insertScoringItems(BizQuestion bizQuestion) {
        if (isFilePractical(bizQuestion) && bizQuestion.getScoringItems() != null) {
            int order = 0;
            for (BizScoringItem item : bizQuestion.getScoringItems()) {
                item.setQuestionId(bizQuestion.getQuestionId());
                item.setOrderNum(order++);
                bizScoringItemMapper.insertBizScoringItem(item);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBizQuestionByQuestionIds(Long[] questionIds) {
        // 权限校验：非管理员只能删除自己创建的题目
        Long currentUserId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin(currentUserId);
        
        if (!isAdmin) {
            for (Long questionId : questionIds) {
                BizQuestion question = bizQuestionMapper.selectBizQuestionByQuestionId(questionId);
                if (question != null && !currentUserId.equals(question.getCreatorId())) {
                    // 返回-1表示无权限，由Controller层处理
                    return -1;
                }
            }
        }
        answerDeletionGuardService.assertQuestionsDeletable(questionIds);
        for (Long questionId : questionIds) {
            BizQuestion question = bizQuestionMapper.selectBizQuestionByQuestionId(questionId);
            if (isPythonPractical(question) || programmingJudgeMapper.selectConfig(questionId) != null) {
                assertPythonQuestionDeletable(questionId);
            }
        }
        for (Long questionId : questionIds) {
            BizQuestion question = bizQuestionMapper.selectBizQuestionByQuestionId(questionId);
            if (isPythonPractical(question) || programmingJudgeMapper.selectConfig(questionId) != null) {
                deletePythonQuestionChildren(questionId);
            }
            if (isFlowchartPractical(question) || flowchartMapper.selectQuestionConfig(questionId) != null) {
                flowchartMapper.deleteQuestionConfig(questionId);
            }
        }
        return bizQuestionMapper.deleteBizQuestionByQuestionIds(questionIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBizQuestionByQuestionId(Long questionId) {
        answerDeletionGuardService.assertQuestionsDeletable(new Long[] { questionId });
        BizQuestion question = bizQuestionMapper.selectBizQuestionByQuestionId(questionId);
        if (isPythonPractical(question) || programmingJudgeMapper.selectConfig(questionId) != null) {
            assertPythonQuestionDeletable(questionId);
            deletePythonQuestionChildren(questionId);
        }
        if (isFlowchartPractical(question) || flowchartMapper.selectQuestionConfig(questionId) != null) {
            flowchartMapper.deleteQuestionConfig(questionId);
        }
        return bizQuestionMapper.deleteBizQuestionByQuestionId(questionId);
    }

    /** Python 题被课程、题单或历史记录使用时只能走专门迁移，普通删除不能制造业务孤儿。 */
    private void assertPythonQuestionDeletable(Long questionId) {
        if (programmingJudgeMapper.countQuestionDependencies(questionId) > 0) {
            throw new ServiceException("Python 题已被课程、题单、快照或提交记录使用，不能直接删除");
        }
    }

    private void deletePythonQuestionChildren(Long questionId) {
        programmingJudgeMapper.deleteTestCases(questionId);
        programmingJudgeMapper.deleteConfig(questionId);
    }

    @Override
    public String importQuestion(List<BizQuestion> questionList, String operName)
    {
        if (StringUtils.isNull(questionList) || questionList.size() == 0)
        {
            throw new ServiceException("导入题目数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (BizQuestion question : questionList)
        {
            try
            {
                // P6.1: 数据规范化 (导入时处理)
                if ("judgment".equals(question.getQuestionType())) {
                    String ans = question.getAnswer();
                    if (StringUtils.isNotEmpty(ans)) {
                         if ("正确".equals(ans) || "对".equals(ans) || "1".equals(ans) || "T".equalsIgnoreCase(ans)) {
                             question.setAnswer("T");
                         } else if ("错误".equals(ans) || "错".equals(ans) || "0".equals(ans) || "F".equalsIgnoreCase(ans)) {
                             question.setAnswer("F");
                         }
                    }
                } else if ("choice".equals(question.getQuestionType())) {
                    if (StringUtils.isNotEmpty(question.getAnswer())) {
                        question.setAnswer(question.getAnswer().toUpperCase());
                    }
                }
                
                this.insertBizQuestion(question);
                successNum++;
                String plainTextContent = question.getQuestionContent().replaceAll("<[^>]*>", "");
                String contentPreview = StringUtils.substring(plainTextContent, 0, 15);
                successMsg.append("<br/>").append(successNum).append("、题目 ").append(contentPreview).append("... 导入成功");
            }
            catch (Exception e)
            {
                failureNum++;
                String plainTextContent = question.getQuestionContent().replaceAll("<[^>]*>", "");
                String contentPreview = StringUtils.substring(plainTextContent, 0, 15);
                String msg = "<br/>" + failureNum + "、题目 " + contentPreview + "... 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    private void processQuestionByType(BizQuestion bizQuestion) {
        String questionType = bizQuestion.getQuestionType();

        if ("typing".equals(questionType)) {
            calculateWordCount(bizQuestion);
            // 未手动设置时，按字数和年级给出推荐时长
            if ((bizQuestion.getTypingDuration() == null || bizQuestion.getTypingDuration() <= 0)
                    && bizQuestion.getWordCount() != null && bizQuestion.getWordCount() > 0) {
                int baseSpeed = (bizQuestion.getGrade() != null && bizQuestion.getGrade() <= 6) ? 20 : 40;
                int duration = (int) Math.ceil((double) bizQuestion.getWordCount() / baseSpeed);
                bizQuestion.setTypingDuration(duration);
            }
            bizQuestion.setFilePath(null);
            bizQuestion.setPreviewPath(null);
        } else if ("practical".equals(questionType)) {
            if (isPythonPractical(bizQuestion)) {
                // 在线编程由 Judge0 配置和测试点评分，不能混入文件作品和人工评分项。
                bizQuestion.setPracticalMode("PYTHON");
                bizQuestion.setFilePath(null);
                bizQuestion.setPreviewPath(null);
                bizQuestion.setPreviewStatus(null);
                bizQuestion.setPracticalAllowedExtensions(null);
                bizQuestion.setPracticalImageMaxCount(null);
                bizQuestion.setPracticalMaterials(null);
                bizQuestion.setScoringItems(null);
                bizQuestion.setWordCount(null);
                bizQuestion.setTypingDuration(null);
                return;
            }
            if (isFlowchartPractical(bizQuestion)) {
                // 画程作品由结构化 JSON 和独立版本表承载，不能混入文件上传与 Office 转换链。
                bizQuestion.setPracticalMode("FLOWCHART");
                bizQuestion.setFilePath(null);
                bizQuestion.setPreviewPath(null);
                bizQuestion.setPreviewStatus(null);
                bizQuestion.setPracticalAllowedExtensions(null);
                bizQuestion.setPracticalImageMaxCount(null);
                bizQuestion.setPracticalMaterials(null);
                bizQuestion.setScoringItems(null);
                bizQuestion.setWordCount(null);
                bizQuestion.setTypingDuration(null);
                return;
            }
            bizQuestion.setPracticalMode("FILE");
            if (StringUtils.isEmpty(bizQuestion.getPracticalAllowedExtensions())) {
                bizQuestion.setPracticalAllowedExtensions(
                        "doc,docx,pdf,ppt,pptx,xls,xlsx,jpg,jpeg,png");
            }
            int imageMax = bizQuestion.getPracticalImageMaxCount() == null
                    ? 10 : bizQuestion.getPracticalImageMaxCount();
            bizQuestion.setPracticalImageMaxCount(Math.min(Math.max(imageMax, 1), 10));
            handlePracticalQuestionFile(bizQuestion);
            bizQuestion.setWordCount(null);
            bizQuestion.setTypingDuration(null);
        } else {
            bizQuestion.setWordCount(null);
            bizQuestion.setTypingDuration(null);
            bizQuestion.setFilePath(null);
            bizQuestion.setPreviewPath(null);
            bizQuestion.setPracticalAllowedExtensions(null);
            bizQuestion.setPracticalImageMaxCount(null);
            bizQuestion.setPracticalMaterials(null);
        }
    }

    /**
     * 处理操作题的文件转换 (异步模式)
     */
    private void handlePracticalQuestionFile(BizQuestion bizQuestion) {
        if (StringUtils.isEmpty(bizQuestion.getFilePath())) {
            bizQuestion.setPreviewPath(null);
            bizQuestion.setPreviewStatus(null);
            return;
        }

        String urlPath = bizQuestion.getFilePath();
        String lower = urlPath.toLowerCase();
        if (lower.endsWith(".pdf") || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
            bizQuestion.setPreviewStatus("success");
            bizQuestion.setPreviewPath(urlPath);
        } else {
            // Office 起始文件统一生成 PDF；不再把 Word 后缀规则误用于 PPT/Excel。
            bizQuestion.setPreviewStatus("pending");
            String fileSystemRelativePath = urlPath.replaceFirst(Constants.RESOURCE_PREFIX, "");
            String pdfRelativePath = fileSystemRelativePath.replaceAll("(?i)\\.[^.\\/]+$", ".pdf");
            bizQuestion.setPreviewPath(Constants.RESOURCE_PREFIX + pdfRelativePath);
        }
    }

    private void calculateWordCount(BizQuestion bizQuestion) {
        if (StringUtils.isNotEmpty(bizQuestion.getQuestionContent())) {
            String plainText = bizQuestion.getQuestionContent().replaceAll("<[^>]*>", "");
            String denseText = plainText.replaceAll("\\s+", "");
            bizQuestion.setWordCount(denseText.length());
        } else {
            bizQuestion.setWordCount(0);
        }
    }

    /**
     * 触发操作题异步转换（如果需要）
     */
    private void triggerAsyncConversionIfNeeded(BizQuestion bizQuestion) {
        if (isFilePractical(bizQuestion)
            && "pending".equals(bizQuestion.getPreviewStatus())
            && bizQuestion.getQuestionId() != null) {
            
            String urlPath = bizQuestion.getFilePath();
            String fileSystemRelativePath = urlPath.replaceFirst(Constants.RESOURCE_PREFIX, "");
            String originalFullPath = RuoYiConfig.getProfile() + fileSystemRelativePath;
            String outputDir = new java.io.File(originalFullPath).getParent();
            
            asyncConversionService.convertQuestionAsync(
                bizQuestion.getQuestionId(), 
                originalFullPath, 
                outputDir, 
                bizQuestion.getPreviewPath()
            );
            log.info("【题库】已触发异步转换 questionId={}", bizQuestion.getQuestionId());
        }
    }

    /**
     * 题目素材采用独立清单；起始文件可见、补充资源可见、参考答案仅教师可见。
     */
    private void replacePracticalMaterials(BizQuestion question)
    {
        if (question.getQuestionId() == null) return;
        practicalArtifactMapper.deleteMaterialsByQuestion(question.getQuestionId());
        if (!isFilePractical(question)) return;

        int order = 0;
        if (StringUtils.isNotEmpty(question.getFilePath()))
        {
            PracticalQuestionMaterial starter = buildMaterial(
                    question.getQuestionId(), "STARTER", order++, question.getFilePath());
            practicalArtifactMapper.insertQuestionMaterial(starter);
        }
        if (question.getPracticalMaterials() == null) return;
        for (PracticalQuestionMaterial source : question.getPracticalMaterials())
        {
            if (source == null || StringUtils.isEmpty(source.getResourcePath())
                    || "STARTER".equalsIgnoreCase(source.getMaterialType())) continue;
            String type = "REFERENCE".equalsIgnoreCase(source.getMaterialType())
                    ? "REFERENCE" : "RESOURCE";
            String extension = extensionOf(source.getResourcePath());
            if ("zip".equals(extension) && "STARTER".equals(type))
            {
                throw new ServiceException("压缩包只能作为教师资源或参考材料");
            }
            PracticalQuestionMaterial material = buildMaterial(
                    question.getQuestionId(), type, order++, source.getResourcePath());
            material.setOriginalFileName(StringUtils.isNotEmpty(source.getOriginalFileName())
                    ? source.getOriginalFileName() : material.getOriginalFileName());
            practicalArtifactMapper.insertQuestionMaterial(material);
        }
    }

    private PracticalQuestionMaterial buildMaterial(Long questionId, String type, int order, String path)
    {
        PracticalQuestionMaterial material = new PracticalQuestionMaterial();
        material.setQuestionId(questionId);
        material.setMaterialType(type);
        material.setFileOrder(order);
        material.setResourcePath(path);
        material.setOriginalFileName(path.substring(path.lastIndexOf('/') + 1));
        material.setFileExtension(extensionOf(path));
        material.setCreateBy(SecurityUtils.getUsername());
        material.setCreateTime(DateUtils.getNowDate());
        return material;
    }

    private String extensionOf(String path)
    {
        int dot = path == null ? -1 : path.lastIndexOf('.');
        return dot < 0 ? "" : path.substring(dot + 1).toLowerCase();
    }

    private boolean isPythonPractical(BizQuestion question)
    {
        return question != null && "practical".equals(question.getQuestionType())
                && "PYTHON".equalsIgnoreCase(question.getPracticalMode());
    }

    private boolean isFilePractical(BizQuestion question)
    {
        return question != null && "practical".equals(question.getQuestionType())
                && (question.getPracticalMode() == null
                    || "FILE".equalsIgnoreCase(question.getPracticalMode()));
    }

    private boolean isFlowchartPractical(BizQuestion question)
    {
        return question != null && "practical".equals(question.getQuestionType())
                && "FLOWCHART".equalsIgnoreCase(question.getPracticalMode());
    }
}
