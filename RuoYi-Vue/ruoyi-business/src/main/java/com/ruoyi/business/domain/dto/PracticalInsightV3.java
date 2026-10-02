package com.ruoyi.business.domain.dto;

import java.util.*;
import com.ruoyi.business.domain.PracticalQuestionMaterial;

/** 诊断建议与正式成绩分开保存；公共字段用于固定 JSON 契约。 */
public final class PracticalInsightV3
{
    private PracticalInsightV3() { }
    public static final String VERSION = "insight-v3.4.0";
    public static class Request extends PaperImportRequest
    {
        public String subject = "信息科技";
        public String assessmentDate;
        public Integer grade;
        public int lessonMinutes = 40;
    }
    public static class Input
    {
        public Request scope;
        public Long teacherId, deptId;
        public String fingerprint, paperFingerprint, questionContent, gradeLabel, provider, model;
        public java.math.BigDecimal inputPrice, outputPrice;
        public int total;
        public List<PracticalQuestionMaterial> materials = new ArrayList<>();
        public List<Student> students = new ArrayList<>();
        public Map<String, Object> scoringStats;
        public List<Object> sourceSlots = new ArrayList<>();
    }
    public static class Student
    {
        public Long studentId, answerId, versionId;
        public String name, number, fingerprint;
        public Integer score, maxScore;
        public List<String> pages = new ArrayList<>();
        public String recognitionJson;
        public Object scoringDetails;
        public Long rubricSnapshotId;
    }
    public static class Paper
    {
        public String summary;
        public List<Question> questions = new ArrayList<>();
        public List<Knowledge> knowledgePoints = new ArrayList<>();
        public List<String> limitations = new ArrayList<>();
    }
    public static class Question
    {
        public String id, number, stem, referenceAnswer;
        public String type, codeContext;
        public boolean contextComplete;
        public int optionCount;
        public List<String> options = new ArrayList<>();
        public List<Part> parts = new ArrayList<>();
        public List<Integer> sourcePages = new ArrayList<>();
    }
    public static class Part
    {
        public String id, prompt, referenceAnswer;
        public String referenceIssue;
        public String referenceStatus;
        public List<String> sourceKeys = new ArrayList<>();
    }
    public static class ErrorPattern
    {
        public String id, name;
        public List<String> questionIds = new ArrayList<>();
    }
    public static class Knowledge
    {
        public String id, name, rationale;
        public boolean core;
        public List<String> questionIds = new ArrayList<>();
        public List<ErrorPattern> errorPatterns = new ArrayList<>();
    }
    public enum State { MASTERED, PARTIAL, MISCONCEPTION, CONFLICT, BLANK, INSUFFICIENT, NOT_COVERED }
    public static class Evidence
    {
        public String id, questionId, partId, text, status;
        public int page;
    }
    public static class Recognition
    {
        public List<Evidence> evidence = new ArrayList<>();
    }
    public static class Diagnosis
    {
        public String knowledgeId, observation, causeHypothesis, errorCode;
        public State state;
        public List<String> evidenceIds = new ArrayList<>();
        public String comparability;
        public List<String> correctEvidenceIds = new ArrayList<>();
        public List<ErrorDetail> errors = new ArrayList<>();
    }
    public static class ErrorDetail
    {
        public String questionId, patternId, category, observedError, expectedApproach;
        public String causeHypothesis, verificationQuestion;
        public String actualFragment, expectedFragment;
        public List<String> evidenceIds = new ArrayList<>();
    }
    public static class Result
    {
        public List<Evidence> evidence = new ArrayList<>();
        public List<Diagnosis> diagnoses = new ArrayList<>();
        public List<String> reviewReasons = new ArrayList<>();
    }
    public static class BatchResult
    {
        public Map<String, Result> students = new LinkedHashMap<>();
    }
    public static class Activity
    {
        public String knowledgeId, objective, explanation, foundationPractice, extensionPractice;
        public String referenceAnswer, exitQuestion, exitAnswer, successCriteria;
        public int minutes;
    }
    public static class Teaching
    {
        public List<Activity> activities = new ArrayList<>();
    }
    public static class Job
    {
        public Long reportId, teacherId, deptId, lessonId, questionId;
        public String entryYear, classCode, fingerprint, status, stage, inputJson, reportJson, errorMessage, runToken;
        public int completed, failed;
        public long promptTokens, completionTokens;
        public Date createdAt, updatedAt;
    }
    public static class EvidenceRow
    {
        public Long reportId, studentId;
        public String fingerprint, status, resultJson;
    }
}
