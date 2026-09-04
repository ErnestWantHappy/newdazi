package com.ruoyi.business.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.List;
import com.ruoyi.business.domain.PracticalAttachment;

/**
 * 操作题提交记录 - 批改用视图对象
 */
public class PracticalSubmissionVo {
    
    /** 答题记录ID */
    private Long answerId;
    
    /** 学生ID */
    private Long studentId;
    
    /** 学生姓名 */
    private String studentName;
    
    /** 学号 */
    private String studentNo;
    
    /** 班级代码 */
    private String classCode;
    
    /** 题目ID */
    private Long questionId;
    
    /** 题目内容 */
    private String questionContent;

    /** 操作题作答方式：FILE、PYTHON、FLOWCHART */
    private String practicalMode;
    
    /** 学生答案（文件路径） */
    private String studentAnswer;
    
    /** PDF预览路径 */
    private String previewPath;

    /** 预览状态 */
    private String previewStatus;

    /** 预览重试次数 */
    private Integer previewRetryCount;

    /** 最近一次预览重试时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date previewLastRetryTime;

    /** 预览失败原因 */
    private String previewErrorMessage;
    
    /** 已批分数（null表示未批改） */
    private Integer score;
    
    /** 该题满分 */
    private Integer maxScore;
    
    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;
    
    /** P5: 是否已提交 */
    private Boolean submitted;

    /** 服务端权威任务状态及版本；用于实时消息乱序保护。 */
    private String taskState;
    private Long stateVersion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date stateChangedAt;
    
    /** 学生备注 */
    private String remark;

    /** 当前逻辑作品版本；历史未回填数据可空 */
    private Long practicalVersionId;

    /** 当前提交的作品版本号，用于批改页展示“作品 vN”。 */
    private Integer versionNo;

    /** 当前提交绑定的评分标准快照。 */
    private Long rubricSnapshotId;

    /** 流程图正式提交版本ID；文件作品为空。 */
    private Long flowchartSubmissionId;

    /** 提交绑定快照的版本号，用于批改页展示“评分依据 vM（提交时）”。 */
    private Integer rubricSnapshotVersion;

    /** 当前版本附件，按作品顺序排列 */
    private List<PracticalAttachment> attachments = new ArrayList<PracticalAttachment>();

    // Getters and Setters
    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }

    public String getPracticalMode() { return practicalMode; }
    public void setPracticalMode(String practicalMode) { this.practicalMode = practicalMode; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public String getPreviewPath() { return previewPath; }
    public void setPreviewPath(String previewPath) { this.previewPath = previewPath; }

    public String getPreviewStatus() { return previewStatus; }
    public void setPreviewStatus(String previewStatus) { this.previewStatus = previewStatus; }

    public Integer getPreviewRetryCount() { return previewRetryCount; }
    public void setPreviewRetryCount(Integer previewRetryCount) { this.previewRetryCount = previewRetryCount; }

    public Date getPreviewLastRetryTime() { return previewLastRetryTime; }
    public void setPreviewLastRetryTime(Date previewLastRetryTime) { this.previewLastRetryTime = previewLastRetryTime; }

    public String getPreviewErrorMessage() { return previewErrorMessage; }
    public void setPreviewErrorMessage(String previewErrorMessage) { this.previewErrorMessage = previewErrorMessage; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    
    public Boolean getSubmitted() { return submitted; }
    public void setSubmitted(Boolean submitted) { this.submitted = submitted; }

    public String getTaskState() { return taskState; }
    public void setTaskState(String taskState) { this.taskState = taskState; }
    public Long getStateVersion() { return stateVersion; }
    public void setStateVersion(Long stateVersion) { this.stateVersion = stateVersion; }
    public Date getStateChangedAt() { return stateChangedAt; }
    public void setStateChangedAt(Date stateChangedAt) { this.stateChangedAt = stateChangedAt; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Long getPracticalVersionId() { return practicalVersionId; }
    public void setPracticalVersionId(Long practicalVersionId) { this.practicalVersionId = practicalVersionId; }

    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }

    public Long getRubricSnapshotId() { return rubricSnapshotId; }
    public void setRubricSnapshotId(Long rubricSnapshotId) { this.rubricSnapshotId = rubricSnapshotId; }

    public Long getFlowchartSubmissionId() { return flowchartSubmissionId; }
    public void setFlowchartSubmissionId(Long flowchartSubmissionId) { this.flowchartSubmissionId = flowchartSubmissionId; }

    public Integer getRubricSnapshotVersion() { return rubricSnapshotVersion; }
    public void setRubricSnapshotVersion(Integer rubricSnapshotVersion) { this.rubricSnapshotVersion = rubricSnapshotVersion; }

    public List<PracticalAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<PracticalAttachment> attachments) { this.attachments = attachments; }
}
