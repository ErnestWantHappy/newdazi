package com.ruoyi.business.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizQuestion;
import com.ruoyi.business.domain.QuestionMedia;
import com.ruoyi.common.exception.ServiceException;

/** 统一规范化，防止题库、快照和保存指纹各自解释图片结构。 */
public final class QuestionMediaCodec {
    public static final ObjectMapper JSON = new ObjectMapper();
    private QuestionMediaCodec() { }

    public static QuestionMedia parse(String json) {
        if (json == null || json.trim().isEmpty()) return new QuestionMedia();
        try { return normalize(JSON.readValue(json, QuestionMedia.class)); }
        catch (Exception ex) { throw new ServiceException("题目图片数据异常，请联系出题教师处理"); }
    }

    public static QuestionMedia normalize(QuestionMedia source) {
        if (source == null) return new QuestionMedia();
        if (source.schemaVersion != 1) throw new ServiceException("题目图片版本不支持，请刷新页面");
        QuestionMedia target = new QuestionMedia();
        target.stem = refs(source.stem, 3);
        if (source.options != null) {
            for (String key : source.options.keySet()) {
                if (!target.options.containsKey(key)) throw new ServiceException("图片选项只能是A、B、C、D");
                target.options.put(key, refs(source.options.get(key), 1));
            }
        }
        return target;
    }

    private static List<QuestionMedia.ImageRef> refs(List<QuestionMedia.ImageRef> source, int max) {
        List<QuestionMedia.ImageRef> result = new ArrayList<>();
        if (source == null) return result;
        if (source.size() > max) throw new ServiceException("题干最多3张图片，每个选项最多1张图片");
        for (QuestionMedia.ImageRef ref : source) {
            if (ref == null || ref.imageId == null || !ref.imageId.matches("[1-9][0-9]{0,18}"))
                throw new ServiceException("图片引用无效，请重新上传");
            try { Long.parseLong(ref.imageId); }
            catch (NumberFormatException ex) { throw new ServiceException("图片引用无效"); }
            result.add(new QuestionMedia.ImageRef(ref.imageId));
        }
        return result;
    }

    public static String write(Object value) {
        try { return JSON.writeValueAsString(value); }
        catch (Exception ex) { throw new ServiceException("题目内容处理失败"); }
    }

    public static List<Long> ids(QuestionMedia media) {
        QuestionMedia m = normalize(media);
        List<Long> result = new ArrayList<>();
        for (QuestionMedia.ImageRef ref : m.stem) result.add(Long.valueOf(ref.imageId));
        for (List<QuestionMedia.ImageRef> slot : m.options.values())
            for (QuestionMedia.ImageRef ref : slot) result.add(Long.valueOf(ref.imageId));
        return result;
    }

    public static boolean hasImages(String json) { return !ids(parse(json)).isEmpty(); }
    public static boolean contains(String json, Long id) { return ids(parse(json)).contains(id); }
    public static String fingerprint(BizQuestion q) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", q.getQuestionType());
        content.put("stem", empty(q.getQuestionContent()));
        content.put("A", empty(q.getOptionA())); content.put("B", empty(q.getOptionB()));
        content.put("C", empty(q.getOptionC())); content.put("D", empty(q.getOptionD()));
        content.put("answer", empty(q.getAnswer())); content.put("analysis", empty(q.getAnalysis()));
        content.put("media", parse(q.getMediaJson()));
        return hash(write(content).getBytes(StandardCharsets.UTF_8));
    }
    public static String hash(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder s = new StringBuilder();
            for (byte b : digest) s.append(String.format("%02x", b & 255));
            return s.toString();
        } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    public static String empty(String s) { return s == null ? "" : s; }

    public static List<String> validate(BizQuestion q, boolean fourOptions) {
        List<String> errors = new ArrayList<>();
        if (!"choice".equals(q.getQuestionType())) return errors;
        QuestionMedia m;
        try { m = parse(q.getMediaJson()); } catch (ServiceException ex) { errors.add(ex.getMessage()); return errors; }
        if (empty(q.getQuestionContent()).trim().isEmpty() && m.stem.isEmpty()) errors.add("题干需填写文字或添加图片");
        if (empty(q.getQuestionContent()).getBytes(StandardCharsets.UTF_8).length > 65000) errors.add("题干过长");
        String[] values = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
        for (int i = 0; i < 4; i++) {
            String key = String.valueOf((char) ('A' + i));
            if (empty(values[i]).length() > 255) errors.add("选项" + key + "文字不能超过255字");
            if ((fourOptions || i < 2 || key.equals(q.getAnswer())) && empty(values[i]).trim().isEmpty() && m.options.get(key).isEmpty())
                errors.add("选项" + key + "需填写文字或添加图片");
        }
        if (!empty(q.getAnswer()).matches("[A-D]")) errors.add("标准答案只能为A、B、C或D");
        return errors;
    }
}
