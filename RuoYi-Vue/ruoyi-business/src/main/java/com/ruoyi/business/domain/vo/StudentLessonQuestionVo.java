package com.ruoyi.business.domain.vo;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.business.domain.PracticalQuestionMaterial;

/**
 * 学生日常课程题目只承载作答所需字段，不复用包含答案和评分配置的教师视图。
 */
public class StudentLessonQuestionVo
{
    private Long id;
    private Long lessonId;
    private Long questionId;
    private Long questionScore;
    private Long orderNum;
    private String questionContent;
    private String questionType;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer typingDuration;
    private Integer wordCount;
    private String previewPath;
    private String filePath;
    private String practicalAllowedExtensions;
    /** 操作题作答方式：FILE 为文件作品，PYTHON 为在线编程。 */
    private String practicalMode;
    private Integer practicalImageMaxCount;
    private List<PracticalQuestionMaterial> practicalMaterials = new ArrayList<PracticalQuestionMaterial>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Long getQuestionScore() { return questionScore; }
    public void setQuestionScore(Long questionScore) { this.questionScore = questionScore; }
    public Long getOrderNum() { return orderNum; }
    public void setOrderNum(Long orderNum) { this.orderNum = orderNum; }
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public Integer getTypingDuration() { return typingDuration; }
    public void setTypingDuration(Integer typingDuration) { this.typingDuration = typingDuration; }
    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }
    public String getPreviewPath() { return previewPath; }
    public void setPreviewPath(String previewPath) { this.previewPath = previewPath; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getPracticalAllowedExtensions() { return practicalAllowedExtensions; }
    public void setPracticalAllowedExtensions(String practicalAllowedExtensions) { this.practicalAllowedExtensions = practicalAllowedExtensions; }
    public String getPracticalMode() { return practicalMode; }
    public void setPracticalMode(String practicalMode) { this.practicalMode = practicalMode; }
    public Integer getPracticalImageMaxCount() { return practicalImageMaxCount; }
    public void setPracticalImageMaxCount(Integer practicalImageMaxCount) { this.practicalImageMaxCount = practicalImageMaxCount; }
    public List<PracticalQuestionMaterial> getPracticalMaterials() { return practicalMaterials; }
    public void setPracticalMaterials(List<PracticalQuestionMaterial> practicalMaterials) { this.practicalMaterials = practicalMaterials; }
}
