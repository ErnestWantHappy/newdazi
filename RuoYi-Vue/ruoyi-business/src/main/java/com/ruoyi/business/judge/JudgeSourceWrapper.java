package com.ruoyi.business.judge;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 判题前给代码套一层很薄的外壳，只做一件事：让 input() 的提示语在输出里带可识别标记。
 *
 * 为什么不直接把提示语从输出里删掉：学生程序自己 print 的内容也可能和提示语一样，
 * 用正则或字符串替换会误删真实输出。这里让提示语自己带上标记，判题端只处理带标记的片段。
 *
 * 学生代码是 base64 传入后在执行时解码的，因此：
 * 1. 不改变学生代码本身，也不做任何字符串拼接转义；
 * 2. compile 时文件名固定为 main.py，报错行号仍是学生看到的行号。
 */
public final class JudgeSourceWrapper {
    /** 提示语起始标记，正常 Python 输出几乎不可能自然出现。 */
    public static final String PROMPT_START = "\u0000\u0001P\u0001\u0000";
    /** 提示语结束标记，后面紧跟真实的标准输出。 */
    public static final String PROMPT_END = "\u0000\u0001E\u0001\u0000";
    /** 程序还在等下一次输入时写到 stderr 的标记，用来区分“输入不够”和真正的运行错误。 */
    public static final String NEED_INPUT = "\u0000\u0001N\u0001\u0000";

    private JudgeSourceWrapper() {
    }

    /** 是否需要套壳：不含 input( 的代码完全保持原样，避免改动既有判题行为。 */
    public static boolean needsWrap(String source) {
        return source != null && source.contains("input");
    }

    public static String wrap(String source) {
        if (!needsWrap(source)) return source;
        String encoded = Base64.getEncoder().encodeToString(source.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        builder.append("import sys, builtins, base64, linecache, traceback\n");
        builder.append("_platform_source = base64.b64decode('").append(encoded)
                .append("').decode('utf-8')\n");
        builder.append("_platform_stdin = sys.stdin\n");
        // 让报错信息仍指向学生代码的真实行号和源码，而不是执行外壳。
        builder.append("linecache.cache['main.py'] = (len(_platform_source), None, ")
                .append("_platform_source.splitlines(True), 'main.py')\n");
        builder.append("_PLATFORM_PROMPT_START = '\\x00\\x01P\\x01\\x00'\n");
        builder.append("_PLATFORM_PROMPT_END = '\\x00\\x01E\\x01\\x00'\n");
        builder.append("_PLATFORM_NEED_INPUT = '\\x00\\x01N\\x01\\x00'\n");
        builder.append("class _PlatformNeedInput(Exception):\n");
        builder.append("    pass\n");
        builder.append("def _platform_input(prompt=''):\n");
        builder.append("    _text = '' if prompt is None else str(prompt)\n");
        builder.append("    # 只给提示语加标记，提示语本身照常输出，学生看不到标记。\n");
        builder.append("    sys.stdout.write(_PLATFORM_PROMPT_START + _text + _PLATFORM_PROMPT_END)\n");
        builder.append("    sys.stdout.flush()\n");
        builder.append("    _line = _platform_stdin.readline()\n");
        builder.append("    if _line == '':\n");
        // 输入还没给够时不是学生程序出错，而是交互终端要继续等输入。
        builder.append("        raise _PlatformNeedInput()\n");
        builder.append("    return _line.rstrip('\\n').rstrip('\\r')\n");
        builder.append("builtins.input = _platform_input\n");
        // 只保留学生 main.py 的调用栈，避免外壳内部的堆栈把初学者吓退。
        builder.append("def _platform_show(error):\n");
        builder.append("    frames = [f for f in traceback.extract_tb(error.__traceback__) if f.filename == 'main.py']\n");
        builder.append("    sys.stdout.flush()\n");
        builder.append("    sys.stderr.write(''.join(traceback.format_list(frames)))\n");
        builder.append("    sys.stderr.write(''.join(traceback.format_exception_only(type(error), error)))\n");
        builder.append("    sys.stderr.flush()\n");
        builder.append("try:\n");
        builder.append("    exec(compile(_platform_source, 'main.py', 'exec'), {'__name__': '__main__'})\n");
        builder.append("except SystemExit:\n");
        builder.append("    raise\n");
        builder.append("except _PlatformNeedInput:\n");
        builder.append("    sys.stdout.flush()\n");
        builder.append("    sys.stderr.write(_PLATFORM_NEED_INPUT)\n");
        builder.append("    sys.stderr.flush()\n");
        builder.append("    raise SystemExit(0)\n");
        builder.append("except BaseException as _platform_error:\n");
        builder.append("    _platform_show(_platform_error)\n");
        builder.append("    raise SystemExit(1)\n");
        return builder.toString();
    }
}
