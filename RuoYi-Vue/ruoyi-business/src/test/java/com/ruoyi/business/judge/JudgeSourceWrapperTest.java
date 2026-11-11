package com.ruoyi.business.judge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * 套壳逻辑必须用真实 Python 验证：拼接后的源码是否可执行、提示语标记是否成对、
 * 报错行号是否仍指向学生代码。只用字符串断言无法发现缩进或转义错误。
 */
class JudgeSourceWrapperTest {
    @Test
    void onlyWrapsRealInputCalls() {
        assertTrue(JudgeSourceWrapper.needsWrap("age = input(\"年龄\")"));
        assertTrue(JudgeSourceWrapper.needsWrap("age = input ()"));
        assertTrue(JudgeSourceWrapper.needsWrap("for i in range(2):\n    v = input()"));
        // 单纯出现 input 这个词不该改变执行链路
        assertFalse(JudgeSourceWrapper.needsWrap("print('input')"));
        assertFalse(JudgeSourceWrapper.needsWrap("input_value = 3\nprint(input_value)"));
        assertFalse(JudgeSourceWrapper.needsWrap(null));
    }

    @Test
    void sourceWithoutInputIsReturnedUnchanged() {
        String source = "print('你好')";
        assertEquals(source, JudgeSourceWrapper.wrap(source));
    }

    @Test
    void wrappedSourceKeepsPromptAndOutputSeparate() throws Exception {
        String source = "age = input(\"请输入您的年龄\")\nprint(\"免票\")\n";
        Execution execution = runPython(source, "3\n");
        assertEquals(0, execution.exitCode, execution.stderr);
        JudgeOutput output = JudgeOutput.of(execution.stdout);
        assertEquals("免票", output.plain().trim());
        assertEquals("请输入您的年龄免票", output.visible().trim());
        // 回放成终端时，提示语后面的输入必须换行，不能和结果粘成一句
        assertEquals("请输入您的年龄3\n免票", output.terminalText("3").trim());
        assertFalse(output.raw().contains("正在等待输入"), output.raw());
    }

    @Test
    void missingInputIsReportedInsteadOfSilentlyEnding() throws Exception {
        String source = "name = input(\"请输入姓名\")\nprint(name)\n";
        Execution execution = runPython(source, "");
        assertEquals(0, execution.exitCode, execution.stderr);
        assertTrue(JudgeOutput.needsMoreInput(execution.stderr), execution.stderr);
        assertEquals("", JudgeOutput.cleanError(execution.stderr));
        // 还没输入完，不算答案错误
        assertEquals("RUNTIME_ERROR", JudgeOutcome.caseStatus(result(execution), "张三"));
    }

    @Test
    void loopsAndChineseTextKeepInputOrder() throws Exception {
        String source = "total = 0\n"
                + "for i in range(2):\n"
                + "    total += int(input(\"第%d个数：\" % (i + 1)))\n"
                + "print(\"合计\", total)\n";
        Execution execution = runPython(source, "3\n5\n");
        assertEquals(0, execution.exitCode, execution.stderr);
        JudgeOutput output = JudgeOutput.of(execution.stdout);
        assertEquals("合计 8", output.plain().trim());
        assertEquals("第1个数：3\n第2个数：5\n合计 8", output.terminalText("3\n5").trim());
    }

    @Test
    void syntaxErrorStillPointsToStudentLine() throws Exception {
        String source = "print(1)\nprint(2\n";
        Execution execution = runPython(source, "");
        assertEquals(1, execution.exitCode);
        assertTrue(execution.stderr.contains("SyntaxError"), execution.stderr);
        assertFalse(execution.stderr.contains("_platform"), execution.stderr);
    }

    @Test
    void runtimeErrorShowsStudentTracebackWithoutWrapperFrames() throws Exception {
        String source = "age = int(input(\"年龄\"))\nprint(10 / 0)\n";
        Execution execution = runPython(source, "3\n");
        assertEquals(1, execution.exitCode);
        assertTrue(execution.stderr.contains("ZeroDivisionError"), execution.stderr);
        assertTrue(execution.stderr.contains("main.py"), execution.stderr);
        assertFalse(execution.stderr.contains("_platform_show"), execution.stderr);
        assertFalse(execution.stderr.contains("exec(compile"), execution.stderr);
    }

    @Test
    void studentExceptionHandlerCannotSwallowWaitingForInput() throws Exception {
        Execution execution = runPython("while True:\n"
                + "    try:\n"
                + "        x = input('A:')\n"
                + "        print(x)\n"
                + "        break\n"
                + "    except Exception:\n"
                + "        pass\n", "");
        assertEquals(0, execution.exitCode, execution.stderr);
        assertTrue(execution.stderr.contains(JudgeSourceWrapper.NEED_INPUT));
    }

    private static Judge0Result result(Execution execution) {
        Judge0Result result = new Judge0Result();
        result.setStatusId(3);
        result.setStdout(execution.stdout);
        result.setStderr(execution.stderr);
        return result;
    }

    private static final class Execution {
        private final int exitCode;
        private final String stdout;
        private final String stderr;

        private Execution(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }

    /** 用本机 Python 执行与线上完全相同的套壳源码；没有 Python 时跳过，不伪造通过。 */
    private static Execution runPython(String source, String stdin) throws Exception {
        String python = findPython();
        Assumptions.assumeTrue(python != null, "本机未安装 Python，跳过真实执行校验");
        File work = File.createTempFile("judge-wrap", ".py");
        work.deleteOnExit();
        java.nio.file.Files.write(work.toPath(), JudgeSourceWrapper.wrap(source).getBytes(StandardCharsets.UTF_8));
        ProcessBuilder builder = new ProcessBuilder(python, "-X", "utf8", work.getAbsolutePath());
        builder.redirectErrorStream(false);
        Process process = builder.start();
        try (OutputStream input = process.getOutputStream()) {
            input.write(stdin.getBytes(StandardCharsets.UTF_8));
        }
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            org.junit.jupiter.api.Assertions.fail("Python 执行超过 5 秒");
        }
        String stdout = read(process.getInputStream());
        String stderr = read(process.getErrorStream());
        return new Execution(process.exitValue(), stdout, stderr);
    }

    private static String read(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = stream.read(chunk)) != -1) buffer.write(chunk, 0, read);
        return buffer.toString("UTF-8");
    }

    private static String findPython() {
        List<String> candidates = new ArrayList<String>();
        candidates.add("python");
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null) {
            candidates.add(localAppData + "\\Programs\\Python\\Python310\\python.exe");
            candidates.add(localAppData + "\\Programs\\Python\\Python311\\python.exe");
            candidates.add(localAppData + "\\Programs\\Python\\Python312\\python.exe");
        }
        for (String candidate : candidates) {
            try {
                Process process = new ProcessBuilder(candidate, "-c", "print(1)").redirectErrorStream(true).start();
                read(process.getInputStream());
                if (process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0) return candidate;
            } catch (Exception ignored) {
                // 换下一个候选，本机环境差异不该让测试失败
            }
        }
        return null;
    }
}
