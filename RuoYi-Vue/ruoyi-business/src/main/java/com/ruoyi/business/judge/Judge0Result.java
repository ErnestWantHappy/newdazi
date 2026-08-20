package com.ruoyi.business.judge;

public class Judge0Result {
    private String token; private Integer statusId; private String statusDescription; private String stdout; private String stderr;
    private String compileOutput; private String message; private Double timeSeconds; private Integer memoryKb;
    public boolean isFinished() { return statusId != null && statusId.intValue() > 2; }
    public String getToken() { return token; } public void setToken(String v) { token = v; }
    public Integer getStatusId() { return statusId; } public void setStatusId(Integer v) { statusId = v; }
    public String getStatusDescription() { return statusDescription; } public void setStatusDescription(String v) { statusDescription = v; }
    public String getStdout() { return stdout; } public void setStdout(String v) { stdout = v; }
    public String getStderr() { return stderr; } public void setStderr(String v) { stderr = v; }
    public String getCompileOutput() { return compileOutput; } public void setCompileOutput(String v) { compileOutput = v; }
    public String getMessage() { return message; } public void setMessage(String v) { message = v; }
    public Double getTimeSeconds() { return timeSeconds; } public void setTimeSeconds(Double v) { timeSeconds = v; }
    public Integer getMemoryKb() { return memoryKb; } public void setMemoryKb(Integer v) { memoryKb = v; }
}
