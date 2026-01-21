package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 县考主表 biz_county_exam
 * 
 * @author ruoyi
 */
public class CountyExam extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 学校类型(1小学/2初中/3高中) */
    private String schoolType;

    /** 考试年级 */
    private Integer examGrade;

    /** 状态(0草稿/1开启/2关闭/3已发布) */
    private String status;

    /** 出题模式(0固定/1乱序/2抽题) */
    private Integer shuffleMode;

    /** 创建人ID */
    private Long creatorId;

    /** 关闭时间 */
    private Date closeTime;

    /** 发布时间 */
    private Date publishTime;

    /** 删除标志(0正常 2删除) */
    private String delFlag;

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getSchoolType() {
        return schoolType;
    }

    public void setSchoolType(String schoolType) {
        this.schoolType = schoolType;
    }

    public Integer getExamGrade() {
        return examGrade;
    }

    public void setExamGrade(Integer examGrade) {
        this.examGrade = examGrade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(Integer shuffleMode) {
        this.shuffleMode = shuffleMode;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }
}
