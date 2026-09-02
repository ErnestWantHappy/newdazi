package com.ruoyi.business.domain.vo;

/**
 * 学生导入结果。
 *
 * 结构化计数和分阶段耗时供诊断中心判断吞吐，message 继续兼容现有导入结果弹窗。
 */
public class StudentImportResult
{
    private int totalCount;

    private int successCount;

    private int failureCount;

    private long parseDurationMs;

    private long validationDurationMs;

    private long passwordDurationMs;

    private long databaseDurationMs;

    private long totalDurationMs;

    private String message;

    public int getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(int totalCount)
    {
        this.totalCount = totalCount;
    }

    public int getSuccessCount()
    {
        return successCount;
    }

    public void setSuccessCount(int successCount)
    {
        this.successCount = successCount;
    }

    public int getFailureCount()
    {
        return failureCount;
    }

    public void setFailureCount(int failureCount)
    {
        this.failureCount = failureCount;
    }

    public long getParseDurationMs()
    {
        return parseDurationMs;
    }

    public void setParseDurationMs(long parseDurationMs)
    {
        this.parseDurationMs = parseDurationMs;
    }

    public long getValidationDurationMs()
    {
        return validationDurationMs;
    }

    public void setValidationDurationMs(long validationDurationMs)
    {
        this.validationDurationMs = validationDurationMs;
    }

    public long getPasswordDurationMs()
    {
        return passwordDurationMs;
    }

    public void setPasswordDurationMs(long passwordDurationMs)
    {
        this.passwordDurationMs = passwordDurationMs;
    }

    public long getDatabaseDurationMs()
    {
        return databaseDurationMs;
    }

    public void setDatabaseDurationMs(long databaseDurationMs)
    {
        this.databaseDurationMs = databaseDurationMs;
    }

    public long getTotalDurationMs()
    {
        return totalDurationMs;
    }

    public void setTotalDurationMs(long totalDurationMs)
    {
        this.totalDurationMs = totalDurationMs;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
