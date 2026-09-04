package com.ruoyi.business.judge;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** 本地单测和页面联调的确定性替身，生产必须使用 http 模式。 */
@Component("judge0MockClient")
public class Judge0MockClient implements Judge0Client {
    private final Map<String, Judge0Result> results = new ConcurrentHashMap<String, Judge0Result>();
    @Override public Judge0Result submit(Judge0Request request) {
        String token = UUID.randomUUID().toString().replace("-", ""); Judge0Result result = new Judge0Result(); result.setToken(token);
        if (request.getSourceCode() == null || request.getSourceCode().trim().isEmpty()) { result.setStatusId(6); result.setStatusDescription("Compilation Error"); result.setCompileOutput("SyntaxError: empty source"); }
        else if (request.getSourceCode().contains("# mock-timeout")) { result.setStatusId(5); result.setStatusDescription("Time Limit Exceeded"); }
        else if (request.getSourceCode().contains("# mock-memory")) { result.setStatusId(12); result.setStatusDescription("Memory Limit Exceeded"); }
        else { result.setStatusId(3); result.setStatusDescription("Accepted"); result.setStdout(request.getExpectedOutput()); result.setTimeSeconds(0.01); result.setMemoryKb(8192); }
        results.put(token, result); Judge0Result queued = new Judge0Result(); queued.setToken(token); queued.setStatusId(1); queued.setStatusDescription("In Queue"); return queued;
    }
    @Override public Judge0Result poll(String token) { return results.get(token); }
}
