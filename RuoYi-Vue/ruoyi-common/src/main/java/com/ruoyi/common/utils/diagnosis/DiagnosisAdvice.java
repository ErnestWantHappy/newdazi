package com.ruoyi.common.utils.diagnosis;

/**
 * 诊断处置建议
 */
public class DiagnosisAdvice
{
    private final String category;
    private final String severity;
    private final String advice;

    public DiagnosisAdvice(String category, String severity, String advice)
    {
        this.category = category;
        this.severity = severity;
        this.advice = advice;
    }

    public String getCategory()
    {
        return category;
    }

    public String getSeverity()
    {
        return severity;
    }

    public String getAdvice()
    {
        return advice;
    }
}
