package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.ruoyi.business.judge.Judge0Client;
import com.ruoyi.business.judge.Judge0Request;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.common.exception.ServiceException;

/** 批改运行只返回输出，不写提交、成绩或判题记录。 */
@Service
public class PracticalPythonRunService {
    @Autowired @Qualifier("judge0HttpClient") private Judge0Client client;
    @Autowired private Judge0Properties properties;
    private final Semaphore slots = new Semaphore(2);

    public Map<String, Object> run(String source, String stdin) {
        if (!"http".equalsIgnoreCase(properties.getMode()))
            throw new ServiceException("Python 运行服务未启用，请联系管理员");
        if (source.getBytes(StandardCharsets.UTF_8).length > 65536)
            throw new ServiceException("在线运行的代码不能超过64 KiB，请下载后运行");
        if (stdin != null && stdin.length() > 4096)
            throw new ServiceException("程序输入不能超过4096个字符");
        if (!slots.tryAcquire()) throw new ServiceException("运行服务繁忙，请稍后重试");
        try {
            Judge0Request request = new Judge0Request();
            request.setSourceCode(source); request.setStdin(stdin);
            request.setCpuTimeLimit(3D); request.setMemoryLimitKb(131072);
            request.setMaxProcesses(8); request.setMaxFileSizeKb(128); request.setMaxOutputKb(32);
            Judge0Result result = client.submit(request);
            long deadline = System.nanoTime() + 20_000_000_000L;
            while (result.getStatusId() == null || result.getStatusId() <= 2) {
                if (result.getToken() == null || System.nanoTime() >= deadline)
                    throw new ServiceException("运行服务响应超时，请稍后重试");
                Thread.sleep(250);
                result = client.poll(result.getToken());
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("success", result.getStatusId() == 3);
            // 只展示学生输出及错误，不透出 Judge0 的令牌或内部服务信息。
            String output = value(result.getStdout()) + value(result.getStderr()) + value(result.getCompileOutput());
            if (result.getStatusId() != 3) output += "\n" + value(result.getStatusDescription());
            data.put("output", output.length() > 32768 ? output.substring(0, 32768) + "\n输出已截断" : output);
            return data;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new ServiceException("运行请求已中断，请重试");
        } finally { slots.release(); }
    }
    private String value(String text) { return text == null ? "" : text; }
}
