package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.ruoyi.business.judge.Judge0Client;
import com.ruoyi.business.judge.Judge0Request;
import com.ruoyi.business.judge.Judge0Result;
import com.ruoyi.business.judge.Judge0Properties;
import com.ruoyi.business.judge.JudgeOutput;
import com.ruoyi.business.judge.JudgeSourceWrapper;
import com.ruoyi.common.exception.ServiceException;

/** 批改运行只返回输出，不写提交、成绩或判题记录。 */
@Service
public class PracticalPythonRunService {
    /** 与课堂判题默认班级容量保持一致，避免教师预览入口先于业务服务成为整班瓶颈。 */
    private static final int MAX_CONCURRENT_RUNS = 8;
    private static final long SLOT_WAIT_SECONDS = 3L;

    @Autowired @Qualifier("judge0HttpClient") private Judge0Client client;
    @Autowired private Judge0Properties properties;
    private final Semaphore slots = new Semaphore(MAX_CONCURRENT_RUNS);

    public Map<String, Object> run(String source, String stdin) {
        if (!"http".equalsIgnoreCase(properties.getMode()))
            throw new ServiceException("Python 运行服务未启用，请联系管理员");
        if (source.getBytes(StandardCharsets.UTF_8).length > 65536)
            throw new ServiceException("在线运行的代码不能超过64 KiB，请下载后运行");
        if (stdin != null && stdin.length() > 4096)
            throw new ServiceException("程序输入不能超过4096个字符");
        boolean acquired = false;
        try {
            acquired = slots.tryAcquire(SLOT_WAIT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) throw new ServiceException("运行服务繁忙，请稍后重试");
            Judge0Request request = new Judge0Request();
            // 套壳让 input() 提示语带标记，运行结果才能还原成 Thonny 那样的逐行交互文本。
            request.setSourceCode(JudgeSourceWrapper.wrap(source)); request.setStdin(stdin);
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
            boolean needInput = JudgeOutput.needsMoreInput(result.getStderr());
            // 等输入不是失败：程序还要下一个值，界面会继续显示输入框。
            data.put("success", result.getStatusId() == 3 || needInput);
            data.put("needInput", needInput);
            // 只展示学生输出及错误，不透出 Judge0 的令牌或内部服务信息。
            // 提示语、用户输入、正式输出按真实顺序回放；标记不会出现在教师看到的文本里。
            String output = JudgeOutput.of(value(result.getStdout())).terminalText(stdin)
                    + JudgeOutput.cleanError(result.getStderr()) + value(result.getCompileOutput());
            if (!needInput && result.getStatusId() != 3) output += "\n" + friendlyError(result.getStatusDescription());
            data.put("output", output.length() > 32768 ? output.substring(0, 32768) + "\n输出已截断" : output);
            return data;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new ServiceException("运行请求已中断，请重试");
        } finally {
            if (acquired) slots.release();
        }
    }
    private String value(String text) { return text == null ? "" : text; }

    /** 判题器的英文状态名对师生没有意义，统一换成可以直接照做的中文提示。 */
    private String friendlyError(String description) {
        if (description == null || description.trim().isEmpty()) return "";
        String value = description.trim();
        if ("Time Limit Exceeded".equalsIgnoreCase(value)) return "程序运行超过时间限制，请检查是否有死循环";
        if ("Runtime Error (NZEC)".equalsIgnoreCase(value)) return "程序运行出错，请检查输入格式或变量类型";
        if (value.startsWith("Runtime Error")) return "程序运行出错，请检查代码";
        if ("Compilation Error".equalsIgnoreCase(value)) return "程序有语法错误，请检查代码";
        if ("Memory Limit Exceeded".equalsIgnoreCase(value)) return "程序占用内存过多";
        if ("Internal Error".equalsIgnoreCase(value) || "Exec Format Error".equalsIgnoreCase(value))
            return "运行服务暂时不可用，请稍后重试";
        return value;
    }
}
