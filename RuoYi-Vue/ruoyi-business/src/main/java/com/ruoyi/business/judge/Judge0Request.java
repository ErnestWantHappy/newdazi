package com.ruoyi.business.judge;

public class Judge0Request {
    private String sourceCode; private String stdin; private String expectedOutput; private Double cpuTimeLimit;
    private Integer memoryLimitKb; private Integer maxProcesses; private Integer maxFileSizeKb; private Integer maxOutputKb;
    public String getSourceCode() { return sourceCode; } public void setSourceCode(String v) { sourceCode = v; }
    public String getStdin() { return stdin; } public void setStdin(String v) { stdin = v; }
    public String getExpectedOutput() { return expectedOutput; } public void setExpectedOutput(String v) { expectedOutput = v; }
    public Double getCpuTimeLimit() { return cpuTimeLimit; } public void setCpuTimeLimit(Double v) { cpuTimeLimit = v; }
    public Integer getMemoryLimitKb() { return memoryLimitKb; } public void setMemoryLimitKb(Integer v) { memoryLimitKb = v; }
    public Integer getMaxProcesses() { return maxProcesses; } public void setMaxProcesses(Integer v) { maxProcesses = v; }
    public Integer getMaxFileSizeKb() { return maxFileSizeKb; } public void setMaxFileSizeKb(Integer v) { maxFileSizeKb = v; }
    public Integer getMaxOutputKb() { return maxOutputKb; } public void setMaxOutputKb(Integer v) { maxOutputKb = v; }
}
