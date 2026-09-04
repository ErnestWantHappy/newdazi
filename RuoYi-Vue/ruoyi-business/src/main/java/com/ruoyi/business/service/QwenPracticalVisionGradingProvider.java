package com.ruoyi.business.service;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.business.domain.TeacherAiConfig;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/** 阿里云百炼 OpenAI 兼容视觉接口适配器。 */
@Service
public class QwenPracticalVisionGradingProvider implements PracticalVisionGradingProvider
{
    private static final int MAX_IMAGES = 50;
    private static final long MAX_IMAGE_BYTES = 7L * 1024 * 1024;
    private static final long MAX_TOTAL_BYTES = 60L * 1024 * 1024;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PracticalAiGradingOutput grade(TeacherAiConfig config, String apiKey, PracticalAiGradingInput input)
    {
        validateEndpoint(config);
        if (input.getPageImages() == null || input.getPageImages().isEmpty())
            throw new ServiceException("作品尚无可供 AI 识别的规范化页图");
        try
        {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", config.getModelName());
            body.put("temperature", 0.1D);
            body.put("max_tokens", 2200);
            body.put("enable_thinking", false);
            body.putObject("response_format").put("type", "json_object");
            ArrayNode messages = body.putArray("messages");
            messages.addObject().put("role", "system").put("content", systemPrompt());
            ObjectNode user = messages.addObject();
            user.put("role", "user");
            ArrayNode content = user.putArray("content");
            content.addObject().put("type", "text").put("text", gradingPrompt(input));

            long totalBytes = 0;
            List<File> pages = input.getPageImages();
            if (pages.size() > MAX_IMAGES) throw new ServiceException("作品超过 AI 单次最多 50 页限制");
            for (int index = 0; index < pages.size(); index++)
            {
                File page = pages.get(index);
                if (page == null || !page.isFile()) throw new ServiceException("作品页图不存在");
                long size = page.length();
                totalBytes += size;
                if (size <= 0 || size > MAX_IMAGE_BYTES || totalBytes > MAX_TOTAL_BYTES)
                    throw new ServiceException("作品页图体积超过 AI 安全限制");
                String label = input.getPageLabels() != null && index < input.getPageLabels().size()
                        ? input.getPageLabels().get(index) : "作品第" + (index + 1) + "页";
                content.addObject().put("type", "text").put("text", "以下图片是" + label);
                String dataUrl = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(page.toPath()));
                content.addObject().put("type", "image_url").putObject("image_url").put("url", dataUrl);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            ResponseEntity<String> response = restTemplate().exchange(config.getEndpointUrl(), HttpMethod.POST,
                    new HttpEntity<String>(objectMapper.writeValueAsString(body), headers), String.class);
            return parse(response.getBody(), input);
        }
        catch (ServiceException e) { throw e; }
        catch (RestClientResponseException e)
        {
            throw new ServiceException(friendlyHttpError(e));
        }
        catch (Exception e)
        {
            throw new ServiceException("千问视觉评分调用失败");
        }
    }

    PracticalAiGradingOutput parse(String responseBody, PracticalAiGradingInput input) throws Exception
    {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.size() == 0) throw new ServiceException("千问未返回评分内容");
        String raw = choices.get(0).path("message").path("content").asText();
        JsonNode result = objectMapper.readTree(raw);
        JsonNode rubricResults = result.path("rubricResults");
        if (!rubricResults.isArray()) throw new ServiceException("AI 评分 JSON 缺少 rubricResults");

        Map<Long, Integer> maxima = new HashMap<Long, Integer>();
        for (PracticalScoringItemVo item : input.getScoringItems()) maxima.put(item.getItemId(), item.getMaxScore());
        Set<Long> seen = new HashSet<Long>();
        ArrayNode details = objectMapper.createArrayNode();
        int sum = 0;
        for (JsonNode item : rubricResults)
        {
            long itemId = item.path("rubricItemId").asLong(Long.MIN_VALUE);
            int score = item.path("score").asInt(Integer.MIN_VALUE);
            Integer max = maxima.get(itemId);
            if (max == null || !seen.add(itemId) || score < 0 || score > max)
                throw new ServiceException("AI 返回了无效的评分项或分数");
            sum += score;
            details.addObject().put("itemId", itemId).put("score", score);
        }
        int total = result.path("totalScore").asInt(Integer.MIN_VALUE);
        int rubricMax = input.getRubric().getQuestionScore();
        if (seen.size() != maxima.size()) throw new ServiceException("AI 未完整返回全部评分项");
        if ((!maxima.isEmpty() && total != sum) || total < 0 || total > rubricMax)
            throw new ServiceException("AI 返回的总分不符合评分标准");
        double confidence = result.path("confidence").asDouble(0D);
        if (confidence < 0D || confidence > 1D) confidence = 0D;

        PracticalAiGradingOutput output = new PracticalAiGradingOutput();
        output.setSuggestedScore(total);
        output.setScoringDetailsJson(objectMapper.writeValueAsString(details));
        output.setEvidenceJson(objectMapper.writeValueAsString(result));
        output.setConfidence(BigDecimal.valueOf(confidence));
        output.setRequestId(root.path("id").asText(null));
        output.setPromptTokens(integerOrNull(root.path("usage").path("prompt_tokens")));
        output.setCompletionTokens(integerOrNull(root.path("usage").path("completion_tokens")));
        return output;
    }

    private String systemPrompt()
    {
        return "你是中小学信息科技操作题的审慎评分助手。只依据提供的题干、逐项评分标准和学生作品页图评分；"
             + "不得猜测看不见的操作过程，不得使用学生身份信息，不得因排版美观额外加分。证据不足时从严并标记需人工复核。"
             + "必须只输出一个合法 JSON 对象，禁止 Markdown、解释前缀或代码围栏。";
    }

    String gradingPrompt(PracticalAiGradingInput input) throws Exception
    {
        ObjectNode contract = objectMapper.createObjectNode();
        contract.put("question", input.getRubric().getQuestionContent());
        contract.put("maxScore", input.getRubric().getQuestionScore());
        contract.set("rubric", objectMapper.valueToTree(input.getScoringItems()));
        contract.put("imageCount", input.getPageImages().size());
        contract.set("imageLabels", objectMapper.valueToTree(input.getPageLabels()));
        if (StringUtils.isNotBlank(input.getAuxiliaryContextJson())) {
            contract.set("flowchartAuxiliaryContext", objectMapper.readTree(input.getAuxiliaryContextJson()));
        }
        return "请先识别标签：学生作品是待评分内容；空白起始材料用于判断学生实际完成了哪些修改；教师参考答案表示目标完成状态。"
             + "必须比较学生作品、空白起始材料和教师参考答案后再逐项评分。教师参考答案只能作为对照，不得当作学生作品；"
             + "流程图题以学生作品图和标准答案图为主要依据，JSON与结构检查仅作辅助，旧结构规则不得单独决定分数。"
             + "证据中的 page 只填写学生作品页码，不填写空白材料或教师参考答案页码。"
             + "每项分数必须是0到maxScore之间的整数；总分必须等于逐项分数之和且不超过题目满分。\n"
             + "评分输入：" + objectMapper.writeValueAsString(contract) + "\n"
             + "如果 rubric 为空，请按整体完成质量直接给总分并返回空 rubricResults。输出契约："
             + "{\"rubricResults\":[{\"rubricItemId\":整数,\"score\":整数,\"maxScore\":整数,"
             + "\"evidence\":[{\"page\":从1开始的页码,\"description\":\"可核验事实\"}],"
             + "\"reason\":\"简短理由\",\"confidence\":0到1,\"riskFlags\":[]}],"
             + "\"totalScore\":整数,\"maxScore\":" + input.getRubric().getQuestionScore()
             + ",\"overallComment\":\"简短总评\",\"confidence\":0到1,\"needsHumanReview\":true}";
    }

    private RestTemplate restTemplate()
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(120000);
        return new RestTemplate(factory);
    }

    private void validateEndpoint(TeacherAiConfig config)
    {
        if (config == null || !TeacherAiConfigService.DEFAULT_ENDPOINT.equals(config.getEndpointUrl()))
            throw new ServiceException("AI 接口地址不在允许列表");
    }

    String friendlyHttpError(RestClientResponseException exception)
    {
        int status = exception.getRawStatusCode();
        String code = null;
        String message = null;
        try
        {
            JsonNode root = objectMapper.readTree(exception.getResponseBodyAsString());
            JsonNode error = root.path("error").isObject() ? root.path("error") : root;
            code = textOrNull(error.path("code"));
            message = textOrNull(error.path("message"));
        }
        catch (Exception ignored)
        {
            // 错误响应不一定是 JSON；此时只返回 HTTP 状态，避免把未知原文直接展示给教师。
        }
        String normalizedCode = code == null ? "" : code.toLowerCase();
        if ("arrearage".equalsIgnoreCase(code))
            return "阿里云账户可能余额不足或已欠费，请充值后重试";
        if (normalizedCode.startsWith("invalidapikey"))
            return "AI Key 无效或已过期，请重新配置";
        if (normalizedCode.startsWith("throttling") || normalizedCode.contains("ratequota")
                || normalizedCode.contains("ratelimit") || normalizedCode.contains("rate_limit"))
            return "请求过于频繁，已触发限流，请稍后重试";
        String safeMessage = truncateMessage(message, 200);
        return "千问接口调用失败（HTTP " + status + "）"
                + (StringUtils.isBlank(safeMessage) ? "" : "：" + safeMessage);
    }

    private String textOrNull(JsonNode node)
    {
        String value = node == null || !node.isValueNode() ? null : node.asText(null);
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String truncateMessage(String value, int max)
    {
        if (value == null) return null;
        String normalized = value.replaceAll("(?i)bearer\\s+[a-z0-9._~+/=-]+", "Bearer ***")
                .replaceAll("(?i)sk-[a-z0-9_-]{6,}", "sk-***")
                .replaceAll("(?i)(api[_ -]?key\\s*[:=]\\s*)[^\\s,;，；]+", "$1***")
                .replaceAll("\\s+", " ").trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max) + "…";
    }

    private Integer integerOrNull(JsonNode node) { return node == null || !node.isNumber() ? null : node.asInt(); }
}
