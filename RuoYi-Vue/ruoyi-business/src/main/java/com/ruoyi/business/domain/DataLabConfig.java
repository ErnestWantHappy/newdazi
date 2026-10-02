package com.ruoyi.business.domain;

import java.util.Date;

/**
 * 数据分析实验题配置实体
 */
public class DataLabConfig {
    private Long questionId;
    private String experimentName;
    private String description;
    private String starterCode;
    private String excelFileUrl;
    private String excelFileName;
    private Long excelFileSize;
    private Integer maxScore;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getExperimentName() { return experimentName; }
    public void setExperimentName(String experimentName) { this.experimentName = experimentName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

    public String getExcelFileUrl() { return excelFileUrl; }
    public void setExcelFileUrl(String excelFileUrl) { this.excelFileUrl = excelFileUrl; }

    public String getExcelFileName() { return excelFileName; }
    public void setExcelFileName(String excelFileName) { this.excelFileName = excelFileName; }

    public Long getExcelFileSize() { return excelFileSize; }
    public void setExcelFileSize(Long excelFileSize) { this.excelFileSize = excelFileSize; }

    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
