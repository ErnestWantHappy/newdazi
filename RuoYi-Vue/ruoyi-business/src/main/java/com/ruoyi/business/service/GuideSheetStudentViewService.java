package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.StudentLessonQuestionVo;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 构造学生可见的导学单数据，避免把教师设计器的可执行配置交给浏览器。
 */
@Service
public class GuideSheetStudentViewService
{
    private static final Logger log = LoggerFactory.getLogger(GuideSheetStudentViewService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Set<String> SAFE_WIDGET_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "grid", "grid-col", "table", "table-cell", "tab", "tab-pane", "card",
            "input", "textarea", "number", "radio", "checkbox", "select", "time", "time-range",
            "date", "date-range", "switch", "rate", "color", "slider", "cascader", "tree-select",
            "static-text", "html-text", "divider", "picture", "image", "picture-upload", "file-upload",
            "rich-editor", "signature")));

    private static final Set<String> SAFE_WIDGET_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "key", "type", "category", "icon", "formItemFlag", "id", "internal", "displayType",
            "merged", "rows", "cols", "tabs", "widgetList", "options")));

    private static final Set<String> SAFE_OPTION_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "name", "label", "labelAlign", "type", "placeholder", "startPlaceholder", "endPlaceholder",
            "columnWidth", "autoFullWidth", "size", "displayStyle", "buttonStyle", "border", "labelWidth",
            "labelHidden", "rows", "required", "requiredHint", "readonly", "disabled", "hidden",
            "clearable", "editable", "showPassword", "textContent", "htmlContent", "format", "valueFormat",
            "filterable", "allowCreate", "automaticDropdown", "checkStrictly", "showAllLevels", "multiple",
            "multipleLimit", "contentPosition", "optionItems", "uploadTip", "multipleSelect", "limit",
            "fileMaxSize", "fileTypes", "contentHeight", "showBlankRow", "showRowNumber", "cellWidth",
            "cellHeight", "colHeight", "gutter", "responsive", "span", "offset", "push", "pull", "md",
            "sm", "xs", "min", "max", "precision", "step", "controlsPosition", "showWordLimit",
            "minLength", "maxLength", "activeText", "inactiveText", "switchWidth", "lowThreshold",
            "highThreshold", "maxStars", "allowHalf", "showScore", "showText", "showStops", "range",
            "folded", "showFold", "cardWidth", "shadow", "active", "content")));

    private static final Set<String> SAFE_FORM_CONFIG_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "modelName", "refName", "rulesName", "labelWidth", "labelPosition", "size", "labelAlign",
            "layoutType", "jsonVersion")));

    private static final Set<String> SAFE_GRADING_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "fieldTitle", "score", "maxScore", "desc", "tabIndex")));

    private static final Set<String> UPLOAD_WIDGET_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "file-upload", "picture-upload")));

    private static final Safelist TEACHING_HTML = Safelist.relaxed()
            .addTags("section", "article", "h1", "h2", "h3", "h4", "h5", "h6")
            .addAttributes("a", "target", "rel")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https", "data");

    public String sanitizeFormJson(String formJson)
    {
        if (formJson == null || formJson.trim().isEmpty())
        {
            return "{\"widgetList\":[],\"formConfig\":{}}";
        }
        try
        {
            Map<String, Object> source = OBJECT_MAPPER.readValue(formJson,
                    new TypeReference<Map<String, Object>>() { });
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("widgetList", sanitizeWidgetList(source.get("widgetList")));
            result.put("formConfig", sanitizeFormConfig(source.get("formConfig")));
            return OBJECT_MAPPER.writeValueAsString(result);
        }
        catch (Exception e)
        {
            log.warn("学生导学单表单结构无效，已返回空表单");
            return "{\"widgetList\":[],\"formConfig\":{}}";
        }
    }

    public String sanitizeGradingDetail(String gradingDetail)
    {
        if (gradingDetail == null || gradingDetail.trim().isEmpty())
        {
            return "[]";
        }
        try
        {
            Object source = OBJECT_MAPPER.readValue(gradingDetail, Object.class);
            if (!(source instanceof List))
            {
                return "[]";
            }
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : (List<?>) source)
            {
                if (!(item instanceof Map))
                {
                    continue;
                }
                Map<?, ?> sourceItem = (Map<?, ?>) item;
                Map<String, Object> safeItem = new LinkedHashMap<>();
                for (String key : SAFE_GRADING_KEYS)
                {
                    if (sourceItem.containsKey(key))
                    {
                        safeItem.put(key, sourceItem.get(key));
                    }
                }
                result.add(safeItem);
            }
            return OBJECT_MAPPER.writeValueAsString(result);
        }
        catch (Exception e)
        {
            log.warn("学生评分明细结构无效，已返回空明细");
            return "[]";
        }
    }

    public boolean isUploadField(String formJson, String fieldName)
    {
        if (fieldName == null || fieldName.trim().isEmpty())
        {
            return false;
        }
        try
        {
            Map<String, Object> source = OBJECT_MAPPER.readValue(formJson,
                    new TypeReference<Map<String, Object>>() { });
            return findUploadField(source.get("widgetList"), fieldName.trim());
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public List<StudentLessonQuestionVo> toStudentLessonQuestions(List<BizLessonQuestionDetailVo> questions)
    {
        List<StudentLessonQuestionVo> result = new ArrayList<>();
        if (questions == null)
        {
            return result;
        }
        for (BizLessonQuestionDetailVo source : questions)
        {
            if (source == null) continue;
            StudentLessonQuestionVo target = new StudentLessonQuestionVo();
            target.setId(source.getId());
            target.setLessonId(source.getLessonId());
            target.setQuestionId(source.getQuestionId());
            target.setQuestionScore(source.getQuestionScore());
            target.setOrderNum(source.getOrderNum());
            target.setQuestionContent(source.getQuestionContent());
            target.setQuestionType(source.getQuestionType());
            target.setOptionA(source.getOptionA());
            target.setOptionB(source.getOptionB());
            target.setOptionC(source.getOptionC());
            target.setOptionD(source.getOptionD());
            target.setTypingDuration(source.getTypingDuration());
            target.setWordCount(source.getWordCount());
            target.setPreviewPath(source.getPreviewPath());
            target.setFilePath(source.getFilePath());
            target.setPracticalAllowedExtensions(source.getPracticalAllowedExtensions());
            target.setPracticalImageMaxCount(source.getPracticalImageMaxCount());
            result.add(target);
        }
        return result;
    }

    private List<Object> sanitizeWidgetList(Object value)
    {
        if (!(value instanceof List))
        {
            return new ArrayList<>();
        }
        List<Object> result = new ArrayList<>();
        for (Object item : (List<?>) value)
        {
            if (item instanceof Map)
            {
                result.add(sanitizeWidget(castMap(item)));
            }
        }
        return result;
    }

    private Map<String, Object> sanitizeWidget(Map<String, Object> source)
    {
        String type = stringValue(source.get("type"));
        if (!SAFE_WIDGET_TYPES.contains(type))
        {
            return safePlaceholder(source);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : SAFE_WIDGET_KEYS)
        {
            if (!source.containsKey(key))
            {
                continue;
            }
            Object value = source.get(key);
            if ("options".equals(key))
            {
                result.put(key, sanitizeOptions(value));
            }
            else if ("widgetList".equals(key))
            {
                result.put(key, sanitizeWidgetList(value));
            }
            else if ("rows".equals(key) || "cols".equals(key) || "tabs".equals(key))
            {
                result.put(key, sanitizeStructuralList(value));
            }
            else
            {
                result.put(key, value);
            }
        }
        result.put("type", type);
        return result;
    }

    private List<Object> sanitizeStructuralList(Object value)
    {
        if (!(value instanceof List))
        {
            return new ArrayList<>();
        }
        List<Object> result = new ArrayList<>();
        for (Object item : (List<?>) value)
        {
            if (item instanceof Map)
            {
                result.add(sanitizeWidget(castMap(item)));
            }
        }
        return result;
    }

    private Map<String, Object> sanitizeOptions(Object value)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(value instanceof Map))
        {
            return result;
        }
        Map<String, Object> source = castMap(value);
        for (String key : SAFE_OPTION_KEYS)
        {
            if (!source.containsKey(key))
            {
                continue;
            }
            Object optionValue = source.get(key);
            if ("optionItems".equals(key))
            {
                result.put(key, sanitizeOptionItems(optionValue));
            }
            else if ("htmlContent".equals(key))
            {
                result.put(key, Jsoup.clean(stringValue(optionValue), TEACHING_HTML));
            }
            else if ("textContent".equals(key) || "label".equals(key) || "placeholder".equals(key)
                    || "requiredHint".equals(key) || "uploadTip".equals(key))
            {
                result.put(key, plainText(optionValue));
            }
            else
            {
                result.put(key, optionValue);
            }
        }
        return result;
    }

    private List<Map<String, Object>> sanitizeOptionItems(Object value)
    {
        if (!(value instanceof List))
        {
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : (List<?>) value)
        {
            if (!(item instanceof Map))
            {
                continue;
            }
            Map<String, Object> source = castMap(item);
            Map<String, Object> safe = new LinkedHashMap<>();
            if (source.containsKey("label")) safe.put("label", plainText(source.get("label")));
            if (source.containsKey("value")) safe.put("value", source.get("value"));
            if (source.containsKey("disabled")) safe.put("disabled", source.get("disabled"));
            if (source.containsKey("children")) safe.put("children", sanitizeOptionItems(source.get("children")));
            result.add(safe);
        }
        return result;
    }

    private Map<String, Object> sanitizeFormConfig(Object value)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(value instanceof Map))
        {
            return result;
        }
        Map<String, Object> source = castMap(value);
        for (String key : SAFE_FORM_CONFIG_KEYS)
        {
            if (source.containsKey(key)) result.put(key, source.get(key));
        }
        return result;
    }

    private Map<String, Object> safePlaceholder(Map<String, Object> source)
    {
        Map<String, Object> options = source.get("options") instanceof Map
                ? castMap(source.get("options")) : Collections.emptyMap();
        String label = plainText(options.get("label"));
        Map<String, Object> safeOptions = new LinkedHashMap<>();
        safeOptions.put("name", plainText(options.get("name")));
        safeOptions.put("textContent", label.isEmpty() ? "此高级组件无法在学生端安全显示" : label);
        safeOptions.put("hidden", false);
        Map<String, Object> placeholder = new LinkedHashMap<>();
        placeholder.put("type", "static-text");
        placeholder.put("id", plainText(source.get("id")));
        placeholder.put("options", safeOptions);
        return placeholder;
    }

    private boolean findUploadField(Object value, String fieldName)
    {
        if (value instanceof List)
        {
            for (Object item : (List<?>) value)
            {
                if (findUploadField(item, fieldName)) return true;
            }
            return false;
        }
        if (!(value instanceof Map))
        {
            return false;
        }
        Map<String, Object> map = castMap(value);
        String type = stringValue(map.get("type"));
        Map<String, Object> options = map.get("options") instanceof Map
                ? castMap(map.get("options")) : Collections.emptyMap();
        if (UPLOAD_WIDGET_TYPES.contains(type)
                && (fieldName.equals(stringValue(options.get("name")))
                || fieldName.equals(stringValue(map.get("id")))))
        {
            return true;
        }
        for (Object child : map.values())
        {
            if (child instanceof Map || child instanceof List)
            {
                if (findUploadField(child, fieldName)) return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value)
    {
        return (Map<String, Object>) value;
    }

    private String plainText(Object value)
    {
        return Jsoup.parse(stringValue(value)).text();
    }

    private String stringValue(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }
}
