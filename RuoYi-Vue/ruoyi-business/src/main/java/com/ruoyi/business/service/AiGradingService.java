package com.ruoyi.business.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * AI 评分服务 — 带排队机制，支持多供应商
 * <p>
 * 使用线程池 + 有界队列控制并发，保证高并发场景下评分请求有序处理。
 * 每个供应商 API 调用限时 60 秒，超时自动降级。
 *
 * @author ruoyi
 */
@Service
public class AiGradingService {

    private static final Logger log = LoggerFactory.getLogger(AiGradingService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 核心线程数：最少保持的并发数 */
    private static final int CORE_POOL_SIZE = 3;
    /** 最大线程数：峰值并发上限 */
    private static final int MAX_POOL_SIZE = 8;
    /** 队列容量：超出最大线程数后排队等待 */
    private static final int QUEUE_CAPACITY = 200;
    /** 单个 API 调用超时（秒） */
    private static final int CALL_TIMEOUT_SECONDS = 60;

    private final ExecutorService executor;

    public AiGradingService() {
        this.executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                r -> {
                    Thread t = new Thread(r, "ai-grading-");
                    t.setDaemon(false);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        log.info("AI评分线程池初始化完成 core={} max={} queue={}", CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);
    }

    /**
     * Spring 容器关闭时优雅关闭线程池，避免排队任务丢失
     */
    @PreDestroy
    public void shutdown() {
        log.info("AI评分线程池正在关闭...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("AI评分线程池未在30秒内完成，强制关闭");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("AI评分线程池已关闭");
    }

    /**
     * AI 评分返回结果（分数 + 评语）
     */
    public static class AiGradeResult {
        public int score;
        public String comment;

        public AiGradeResult(int score, String comment) {
            this.score = score;
            this.comment = comment;
        }
    }

    /**
     * 提交 AI 评分任务并等待结果
     *
     * @param provider  供应商配置
     * @param apiKey    API Key
     * @param model     模型名
     * @param prompt    评分提示词
     * @param maxScore  满分
     * @return 评分结果（含分数和评语）
     * @throws Exception 评分失败
     */
    public AiGradeResult grade(AiProviderConfig provider, String apiKey, String model,
                               String customUrl, String prompt, int maxScore) throws Exception {
        String apiUrl = provider.getApiUrl(customUrl);
        Future<AiGradeResult> future = executor.submit(() ->
                callApi(apiUrl, apiKey, model, prompt, maxScore));

        try {
            return future.get(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("AI评分超时（" + CALL_TIMEOUT_SECONDS + "秒）");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw cause instanceof Exception ? (Exception) cause : new RuntimeException(cause);
        }
    }

    /**
     * 获取当前排队状态
     */
    public Map<String, Object> getQueueStatus() {
        ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("activeCount", tpe.getActiveCount());
        status.put("poolSize", tpe.getPoolSize());
        status.put("queueSize", tpe.getQueue().size());
        status.put("completedTaskCount", tpe.getCompletedTaskCount());
        return status;
    }

    /**
     * 实际调用 AI API，返回分数和评语
     */
    @SuppressWarnings("unchecked")
    private AiGradeResult callApi(String apiUrl, String apiKey, String model,
                                   String prompt, int maxScore) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("AI评分未配置API Key");
        }
        if (apiUrl == null || apiUrl.isEmpty()) {
            throw new RuntimeException("AI评分未配置API地址");
        }

        // 构建请求体
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        Map<String, Object> reqMessage = new LinkedHashMap<>();
        reqMessage.put("role", "user");
        reqMessage.put("content", prompt);
        requestBody.put("messages", Collections.singletonList(reqMessage));
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 500);

        String requestJson = objectMapper.writeValueAsString(requestBody);

        // 发送 HTTP 请求
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(50000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestJson.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int statusCode = conn.getResponseCode();
        InputStream is = statusCode >= 200 && statusCode < 300
                ? conn.getInputStream() : conn.getErrorStream();

        String responseBody;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            responseBody = sb.toString();
        }
        conn.disconnect();

        if (statusCode != 200) {
            log.error("AI评分API返回错误 status={} body={}", statusCode, responseBody);
            throw new RuntimeException("AI评分API返回错误: " + statusCode);
        }

        // 解析响应
        Map<String, Object> responseObj = objectMapper.readValue(responseBody,
                new TypeReference<Map<String, Object>>() {});

        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseObj.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            if (content == null || content.isEmpty()) {
                throw new RuntimeException("AI评分返回内容为空");
            }
            log.info("AI评分响应 model={} content={}", model, content);

            String aiComment = null;

            // 尝试从 content 中提取 JSON 并解析分数和评语
            // 策略1：提取 content 中的 JSON 对象
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start >= 0 && end > start) {
                String jsonStr = content.substring(start, end + 1);
                try {
                    Map<String, Object> scoreObj = objectMapper.readValue(jsonStr,
                            new TypeReference<Map<String, Object>>() {});
                    Object score = scoreObj.get("score");
                    Object comment = scoreObj.get("comment");
                    if (comment != null) {
                        aiComment = String.valueOf(comment).trim();
                    }
                    if (score != null) {
                        int s = extractScore(score, maxScore);
                        return new AiGradeResult(s, aiComment);
                    }
                } catch (Exception e) {
                    log.warn("解析AI评分JSON失败，尝试其他方式: jsonStr={}", jsonStr, e);
                }
            }

            // 策略2：从 content 中查找 "score" 关键字后的数字
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                    "\"score\"\\s*:\\s*(\\d+)").matcher(content);
            if (m.find()) {
                int s = Integer.parseInt(m.group(1));
                return new AiGradeResult(Math.max(0, Math.min(s, maxScore)), aiComment);
            }

            // 策略3：从 content 中提取第一个独立的数字（如 "5分"、"评分：5"）
            m = java.util.regex.Pattern.compile("(\\d+)\\s*分").matcher(content);
            if (m.find()) {
                int s = Integer.parseInt(m.group(1));
                return new AiGradeResult(Math.max(0, Math.min(s, maxScore)), aiComment);
            }

            // 策略4：回退——提取所有数字，取第一个
            m = java.util.regex.Pattern.compile("\\d+").matcher(content);
            if (m.find()) {
                int s = Integer.parseInt(m.group());
                return new AiGradeResult(Math.max(0, Math.min(s, maxScore)), aiComment);
            }
        }

        throw new RuntimeException("AI评分未返回有效结果");
    }

    /** 从 JSON 解析出的 score 值中提取整数 */
    private int extractScore(Object score, int maxScore) {
        if (score instanceof Number) {
            return Math.max(0, Math.min(((Number) score).intValue(), maxScore));
        } else if (score instanceof String) {
            try {
                return Math.max(0, Math.min(Integer.parseInt(((String) score).trim()), maxScore));
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }
}