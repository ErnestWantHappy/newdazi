package com.ruoyi.business.service;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

import com.ruoyi.business.domain.dto.GuideSheetAiGenerateRequest;
import com.ruoyi.business.domain.vo.GuideSheetAiContentVo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 为新手向导生成可审阅的教学内容草稿，不直接保存或发布模板。
 */
@Service
public class GuideSheetAiContentService
{
    private static final Logger log = LoggerFactory.getLogger(GuideSheetAiContentService.class);
    private static final int MAX_INPUT_LENGTH = 5000;
    private static final int READ_TIMEOUT_MILLIS = 20000;
    private static final long USER_RATE_LIMIT_NANOS = TimeUnit.SECONDS.toNanos(10);
    private static final int MAX_CONCURRENT_REQUESTS = 4;

    private final AiChatGateway gateway;
    private final LongSupplier nanoTimeSource;
    private final Semaphore globalConcurrencyLimiter;
    private final ConcurrentHashMap<Long, Long> lastRequestNanos = new ConcurrentHashMap<>();

    @Autowired
    public GuideSheetAiContentService(AiChatGateway gateway)
    {
        this(gateway, System::nanoTime, new Semaphore(MAX_CONCURRENT_REQUESTS));
    }

    GuideSheetAiContentService(AiChatGateway gateway, LongSupplier nanoTimeSource,
                               Semaphore globalConcurrencyLimiter)
    {
        this.gateway = gateway;
        this.nanoTimeSource = nanoTimeSource;
        this.globalConcurrencyLimiter = globalConcurrencyLimiter;
    }

    public GuideSheetAiContentVo generate(Long teacherId, GuideSheetAiGenerateRequest request)
    {
        if (!gateway.isConfigured())
        {
            return GuideSheetAiContentVo.unavailable();
        }
        if (teacherId == null)
        {
            throw new ServiceException("未识别当前教师，请重新登录后重试");
        }
        String prompt = buildPrompt(request);
        if (!globalConcurrencyLimiter.tryAcquire())
        {
            throw new ServiceException("AI 生成服务繁忙，请稍后再试");
        }
        try
        {
            enforceTeacherRateLimit(teacherId);
            try
            {
                String content = gateway.chat(prompt, 1200, READ_TIMEOUT_MILLIS);
                if (StringUtils.isBlank(content))
                {
                    return GuideSheetAiContentVo.unavailable();
                }
                return GuideSheetAiContentVo.available(content);
            }
            catch (Exception e)
            {
                // 外部能力失败与普通编辑保存解耦，日志也不记录供应商异常详情。
                log.warn("导学单创作AI服务暂不可用");
                return GuideSheetAiContentVo.unavailable();
            }
        }
        finally
        {
            globalConcurrencyLimiter.release();
        }
    }

    private void enforceTeacherRateLimit(Long teacherId)
    {
        long now = nanoTimeSource.getAsLong();
        AtomicBoolean accepted = new AtomicBoolean(false);
        lastRequestNanos.compute(teacherId, (key, previous) ->
        {
            if (previous == null || now - previous >= USER_RATE_LIMIT_NANOS)
            {
                accepted.set(true);
                return now;
            }
            return previous;
        });
        if (!accepted.get())
        {
            throw new ServiceException("AI 生成请求过于频繁，请 10 秒后再试");
        }
    }

    private String buildPrompt(GuideSheetAiGenerateRequest request)
    {
        if (request == null || StringUtils.isBlank(request.getAction()))
        {
            throw new ServiceException("请选择智能生成内容类型");
        }
        String action = normalizeAction(request.getAction());
        String topic = StringUtils.trimToEmpty(request.getTopic());
        String input = StringUtils.trimToEmpty(request.getInput());
        validateLength(topic);
        validateLength(input);

        if ("polish".equals(action))
        {
            if (StringUtils.isBlank(input))
            {
                throw new ServiceException("请先填写需要优化的题目");
            }
            return "你是一名中小学信息科技教师。请在不改变知识点和答案含义的前提下，"
                    + "把下面题目改写得清楚、准确、适合学生阅读。只输出优化后的题目，不输出分析：\n"
                    + input;
        }

        if (request.getGrade() == null || request.getGrade() < 1 || request.getGrade() > 12
                || request.getLessonNum() == null || request.getLessonNum() <= 0
                || StringUtils.isBlank(topic))
        {
            throw new ServiceException("年级、课次和教学主题必须完整填写");
        }
        String context = gradeLabel(request.getGrade()) + "第" + request.getLessonNum()
                + "课，教学主题为“" + topic + "”";
        if ("objectives".equals(action))
        {
            return "你是一名中小学信息科技教师。请为" + context
                    + "生成3条可观察、可达成的学习目标。使用学生能理解的简体中文，"
                    + "每条一行，只输出目标正文。";
        }
        if ("preClassCheck".equals(action))
        {
            return "你是一名中小学信息科技教师。请为" + context
                    + "生成3道简短的课前检测题，难度由易到难。"
                    + "只输出题目草稿，不输出答案、解析或教学分析。";
        }
        if ("reflection".equals(action))
        {
            return "你是一名中小学信息科技教师。请为" + context
                    + "生成2个能帮助学生回顾方法、困难和改进方向的课堂反思问题。"
                    + "使用学生能理解的简体中文，只输出问题。";
        }
        throw new ServiceException("不支持的智能生成内容类型");
    }

    private String normalizeAction(String action)
    {
        String normalized = action.trim().toLowerCase(Locale.ROOT);
        if ("learningobjectives".equals(normalized) || "generateobjectives".equals(normalized)
                || "generatelearningobjectives".equals(normalized) || "objectives".equals(normalized))
        {
            return "objectives";
        }
        if ("preclasscheck".equals(normalized) || "generatepreclasscheck".equals(normalized))
        {
            return "preClassCheck";
        }
        if ("reflection".equals(normalized) || "generatereflection".equals(normalized))
        {
            return "reflection";
        }
        if ("polish".equals(normalized) || "optimizequestion".equals(normalized)
                || "optimizeexpression".equals(normalized))
        {
            return "polish";
        }
        return normalized;
    }

    private void validateLength(String value)
    {
        if (value.length() > MAX_INPUT_LENGTH)
        {
            throw new ServiceException("输入内容过长，请精简后重试");
        }
    }

    private String gradeLabel(int grade)
    {
        String[] labels = { "", "一", "二", "三", "四", "五", "六",
                "七", "八", "九", "十", "十一", "十二" };
        return labels[grade] + "年级";
    }
}
