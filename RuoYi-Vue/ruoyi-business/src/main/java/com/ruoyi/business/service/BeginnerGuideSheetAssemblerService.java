package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 将教师选择的教学模块直接组装成兼容 VForm3 的 formJson。
 */
@Service
public class BeginnerGuideSheetAssemblerService
{
    private static final int MAX_FORM_JSON_BYTES = 2 * 1024 * 1024;
    private static final int MAX_REQUESTED_MODULES = 200;
    private static final Set<String> SUPPORTED_MODULE_TYPES;
    private static final Map<String, List<String>> PRESET_MODULES;

    static
    {
        LinkedHashSet<String> moduleTypes = new LinkedHashSet<>(Arrays.asList(
                "learningObjective", "preClassCheck", "knowledgeExplanation",
                "singleChoice", "multipleChoice", "shortAnswer", "fileSubmission",
                "selfAssessment", "reflection"));
        SUPPORTED_MODULE_TYPES = Collections.unmodifiableSet(moduleTypes);

        LinkedHashMap<String, List<String>> presets = new LinkedHashMap<>();
        presets.put("before-class", Arrays.asList(
                "learningObjective", "preClassCheck", "shortAnswer"));
        presets.put("class-task", Arrays.asList(
                "learningObjective", "knowledgeExplanation", "singleChoice", "shortAnswer",
                "selfAssessment", "reflection"));
        presets.put("project-practice", Arrays.asList(
                "learningObjective", "knowledgeExplanation", "fileSubmission",
                "selfAssessment", "reflection"));
        presets.put("after-class", Arrays.asList(
                "learningObjective", "singleChoice", "multipleChoice", "shortAnswer", "reflection"));
        presets.put("group-cooperation", Arrays.asList(
                "learningObjective", "knowledgeExplanation", "shortAnswer", "fileSubmission",
                "selfAssessment", "reflection"));
        presets.put("it-operation", Arrays.asList(
                "learningObjective", "knowledgeExplanation", "preClassCheck", "fileSubmission",
                "selfAssessment", "reflection"));
        PRESET_MODULES = Collections.unmodifiableMap(presets);
    }

    private final ObjectMapper objectMapper;

    public BeginnerGuideSheetAssemblerService(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public Set<String> getSupportedModuleTypes()
    {
        return SUPPORTED_MODULE_TYPES;
    }

    public Set<String> getSupportedPresetCodes()
    {
        return PRESET_MODULES.keySet();
    }

    public String assemble(String presetCode, List<String> moduleTypes, String existingFormJson)
    {
        ObjectNode root = parseRoot(existingFormJson);
        ensureFormConfig(root);
        ArrayNode targetWidgets = ensureHomeTab(root);
        Set<String> usedNames = new LinkedHashSet<>();
        collectNames(root, usedNames);

        List<String> requested = new ArrayList<>();
        if (StringUtils.isNotBlank(presetCode))
        {
            List<String> preset = PRESET_MODULES.get(normalizePresetCode(presetCode));
            if (preset == null)
            {
                throw new ServiceException("未知的教学结构模板");
            }
            requested.addAll(preset);
        }
        if (moduleTypes != null)
        {
            requested.addAll(moduleTypes);
        }
        if (requested.size() > MAX_REQUESTED_MODULES)
        {
            throw new ServiceException("一次添加的教学模块过多，请分批操作");
        }
        for (String moduleType : requested)
        {
            if (!SUPPORTED_MODULE_TYPES.contains(moduleType))
            {
                throw new ServiceException("不支持的教学模块：" + moduleType);
            }
            targetWidgets.add(createModule(moduleType, nextFieldName(moduleType, usedNames)));
        }

        try
        {
            String result = objectMapper.writeValueAsString(root);
            if (exceedsFormJsonLimit(result))
            {
                throw new ServiceException("导学单内容过大，请精简后重试");
            }
            return result;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("导学单表单生成失败");
        }
    }

    private ObjectNode parseRoot(String existingFormJson)
    {
        if (StringUtils.isBlank(existingFormJson))
        {
            ObjectNode root = objectMapper.createObjectNode();
            root.set("widgetList", objectMapper.createArrayNode());
            return root;
        }
        if (exceedsFormJsonLimit(existingFormJson))
        {
            throw new ServiceException("导学单内容过大，请精简后重试");
        }
        try
        {
            JsonNode parsed = objectMapper.readTree(existingFormJson);
            if (parsed == null || !parsed.isObject())
            {
                throw new ServiceException("导学单表单格式无效");
            }
            ObjectNode root = (ObjectNode) parsed;
            JsonNode widgetList = root.get("widgetList");
            if (widgetList == null)
            {
                root.set("widgetList", objectMapper.createArrayNode());
            }
            else if (!widgetList.isArray())
            {
                throw new ServiceException("导学单组件结构无效");
            }
            return root;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("导学单表单内容已损坏，请返回高级模式检查");
        }
    }

    private void ensureFormConfig(ObjectNode root)
    {
        JsonNode existing = root.get("formConfig");
        if (existing != null && existing.isObject())
        {
            return;
        }
        if (existing != null)
        {
            throw new ServiceException("导学单全局配置结构无效");
        }
        ObjectNode config = objectMapper.createObjectNode();
        config.put("modelName", "formData");
        config.put("refName", "vForm");
        config.put("rulesName", "rules");
        config.put("labelWidth", 110);
        config.put("labelPosition", "top");
        config.put("size", "");
        config.put("labelAlign", "label-left-align");
        config.put("cssCode", "");
        config.put("customClass", "");
        config.put("functions", "");
        config.put("layoutType", "PC");
        config.put("jsonVersion", 3);
        config.put("onFormCreated", "");
        config.put("onFormMounted", "");
        config.put("onFormDataChange", "");
        root.set("formConfig", config);
    }

    private ArrayNode ensureHomeTab(ObjectNode root)
    {
        ArrayNode rootWidgets = (ArrayNode) root.get("widgetList");
        ObjectNode homeTab = null;
        int homeTabIndex = -1;
        for (int i = 0; i < rootWidgets.size(); i++)
        {
            JsonNode candidate = rootWidgets.get(i);
            if (candidate.isObject() && "tab".equals(candidate.path("type").asText())
                    && "HomeTab".equals(candidate.path("options").path("name").asText()))
            {
                homeTab = (ObjectNode) candidate;
                homeTabIndex = i;
                break;
            }
        }

        ArrayNode originalRootWidgets = objectMapper.createArrayNode();
        if (homeTab == null)
        {
            originalRootWidgets.addAll(rootWidgets);
            rootWidgets.removeAll();
            homeTab = createHomeTab();
            rootWidgets.add(homeTab);
        }
        else
        {
            for (int i = rootWidgets.size() - 1; i >= 0; i--)
            {
                if (i != homeTabIndex)
                {
                    originalRootWidgets.insert(0, rootWidgets.remove(i));
                }
            }
            if (homeTabIndex != 0)
            {
                rootWidgets.removeAll();
                rootWidgets.add(homeTab);
            }
        }

        homeTab.put("internal", true);
        ObjectNode options = objectOptions(homeTab);
        options.put("name", "HomeTab");
        ArrayNode tabs = objectArray(homeTab, "tabs");
        if (tabs.size() == 0 || !tabs.get(0).isObject())
        {
            tabs.insert(0, createHomePane());
        }
        ObjectNode firstPane = (ObjectNode) tabs.get(0);
        firstPane.put("internal", true);
        ArrayNode targetWidgets = objectArray(firstPane, "widgetList");
        targetWidgets.addAll(originalRootWidgets);
        return targetWidgets;
    }

    private ObjectNode createHomeTab()
    {
        ObjectNode tab = objectMapper.createObjectNode();
        tab.put("id", "bg-home-tab");
        tab.put("type", "tab");
        tab.put("category", "container");
        tab.put("icon", "tab");
        tab.put("displayType", "border-card");
        tab.put("internal", true);
        ArrayNode tabs = objectMapper.createArrayNode();
        tabs.add(createHomePane());
        tab.set("tabs", tabs);
        ObjectNode options = objectMapper.createObjectNode();
        options.put("name", "HomeTab");
        options.put("hidden", false);
        options.put("customClass", "");
        tab.set("options", options);
        return tab;
    }

    private ObjectNode createHomePane()
    {
        ObjectNode pane = objectMapper.createObjectNode();
        pane.put("id", "bg-home-pane");
        pane.put("type", "tab-pane");
        pane.put("category", "container");
        pane.put("icon", "tab-pane");
        pane.put("internal", true);
        pane.set("widgetList", objectMapper.createArrayNode());
        ObjectNode options = objectMapper.createObjectNode();
        options.put("name", "tab1");
        options.put("label", "学习任务");
        options.put("hidden", false);
        options.put("active", false);
        options.put("disabled", false);
        options.put("customClass", "");
        pane.set("options", options);
        return pane;
    }

    private ObjectNode createModule(String moduleType, String fieldName)
    {
        ObjectNode widget = objectMapper.createObjectNode();
        widget.put("id", fieldName.replace('_', '-'));
        widget.put("name", fieldName);
        widget.put("type", widgetType(moduleType));
        widget.put("category", "basic");
        widget.put("icon", widgetType(moduleType));
        widget.put("formItemFlag", !"static-text".equals(widgetType(moduleType)));

        ObjectNode options = objectMapper.createObjectNode();
        options.put("name", fieldName);
        options.put("label", moduleLabel(moduleType));
        options.put("labelAlign", "");
        options.putNull("defaultValue");
        options.put("columnWidth", "200px");
        options.put("size", "");
        options.putNull("labelWidth");
        options.put("beginnerModuleType", moduleType);
        options.put("hidden", false);
        options.put("disabled", false);
        options.put("required", isRequiredByDefault(moduleType));
        options.put("labelHidden", false);
        options.put("requiredHint", isRequiredByDefault(moduleType) ? "请完成此项内容" : "");
        options.put("validation", "");
        options.put("validationHint", "");
        options.put("customClass", "");
        populateModuleOptions(moduleType, options);
        widget.set("options", options);
        populateScoring(moduleType, widget);
        return widget;
    }

    private void populateModuleOptions(String moduleType, ObjectNode options)
    {
        if ("learningObjective".equals(moduleType))
        {
            options.put("textContent", "完成本课后，学生能够……");
            options.put("labelHidden", true);
            options.put("fontSize", "16px");
            options.put("fontWeight", "normal");
            options.put("textAlign", "left");
        }
        else if ("knowledgeExplanation".equals(moduleType))
        {
            options.put("textContent", "请填写本课的核心知识和操作提示。");
            options.put("labelHidden", true);
            options.put("fontSize", "16px");
            options.put("fontWeight", "normal");
            options.put("textAlign", "left");
        }
        else if ("singleChoice".equals(moduleType) || "multipleChoice".equals(moduleType))
        {
            ArrayNode items = objectMapper.createArrayNode();
            items.add(option("选项 A", "A"));
            items.add(option("选项 B", "B"));
            items.add(option("选项 C", "C"));
            items.add(option("选项 D", "D"));
            options.set("optionItems", items);
            if ("multipleChoice".equals(moduleType))
            {
                options.set("defaultValue", objectMapper.createArrayNode());
            }
            else
            {
                options.put("defaultValue", "");
            }
            options.put("displayStyle", "inline");
            options.set("optionItemsChecked", objectMapper.createArrayNode());
        }
        else if ("selfAssessment".equals(moduleType))
        {
            options.put("max", 5);
            options.put("allowHalf", false);
            options.put("showScore", true);
            options.put("defaultValue", 0);
        }
        else if ("fileSubmission".equals(moduleType))
        {
            options.put("uploadTip", "请提交本课作品或操作文件");
            options.put("withCredentials", true);
            options.put("multipleSelect", false);
            options.put("limit", 1);
            options.put("fileMaxSize", 20);
            options.set("fileTypes", objectMapper.createArrayNode());
        }
        else
        {
            options.put("placeholder", placeholder(moduleType));
            options.put("rows", 4);
            options.put("autoFullWidth", true);
            options.put("defaultValue", "");
        }
    }

    private void populateScoring(String moduleType, ObjectNode widget)
    {
        ObjectNode scoring = objectMapper.createObjectNode();
        if ("singleChoice".equals(moduleType))
        {
            scoring.put("score", 10);
            scoring.put("type", "exact");
            scoring.put("answer", "");
            widget.set("scoring", scoring);
        }
        else if ("multipleChoice".equals(moduleType))
        {
            scoring.put("score", 10);
            scoring.put("type", "exact");
            scoring.put("answer", "");
            widget.set("scoring", scoring);
        }
        else if ("shortAnswer".equals(moduleType) || "preClassCheck".equals(moduleType))
        {
            scoring.put("score", 10);
            // 文本题没有预设参考答案，默认人工评分可避免学生正常作答被自动判零分。
            scoring.put("type", "manual");
            scoring.put("answer", "");
            widget.set("scoring", scoring);
        }
    }

    private boolean exceedsFormJsonLimit(String formJson)
    {
        return formJson != null
                && formJson.getBytes(StandardCharsets.UTF_8).length > MAX_FORM_JSON_BYTES;
    }

    private ObjectNode option(String label, String value)
    {
        ObjectNode option = objectMapper.createObjectNode();
        option.put("label", label);
        option.put("value", value);
        return option;
    }

    private String widgetType(String moduleType)
    {
        if ("learningObjective".equals(moduleType) || "knowledgeExplanation".equals(moduleType))
        {
            return "static-text";
        }
        if ("singleChoice".equals(moduleType))
        {
            return "radio";
        }
        if ("multipleChoice".equals(moduleType))
        {
            return "checkbox";
        }
        if ("fileSubmission".equals(moduleType))
        {
            return "file-upload";
        }
        if ("selfAssessment".equals(moduleType))
        {
            return "rate";
        }
        return "textarea";
    }

    private String moduleLabel(String moduleType)
    {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("learningObjective", "学习目标");
        labels.put("preClassCheck", "课前检测");
        labels.put("knowledgeExplanation", "知识讲解");
        labels.put("singleChoice", "单选题");
        labels.put("multipleChoice", "多选题");
        labels.put("shortAnswer", "填空或简答");
        labels.put("fileSubmission", "文件或作品提交");
        labels.put("selfAssessment", "学生自评");
        labels.put("reflection", "课堂反思");
        return labels.get(moduleType);
    }

    private boolean isRequiredByDefault(String moduleType)
    {
        return "preClassCheck".equals(moduleType) || "singleChoice".equals(moduleType)
                || "multipleChoice".equals(moduleType)
                || "shortAnswer".equals(moduleType) || "fileSubmission".equals(moduleType);
    }

    private String placeholder(String moduleType)
    {
        if ("preClassCheck".equals(moduleType))
        {
            return "请填写课前检测题目";
        }
        if ("reflection".equals(moduleType))
        {
            return "请回顾本课的收获和仍有疑问的地方";
        }
        return "请输入回答";
    }

    private String nextFieldName(String moduleType, Set<String> usedNames)
    {
        String base = "bg_" + moduleType + "_";
        int index = 1;
        while (usedNames.contains(base + index))
        {
            index++;
        }
        String name = base + index;
        usedNames.add(name);
        return name;
    }

    private String normalizePresetCode(String presetCode)
    {
        if ("preClassPreview".equals(presetCode)) { return "before-class"; }
        if ("classroomTask".equals(presetCode)) { return "class-task"; }
        if ("projectPractice".equals(presetCode)) { return "project-practice"; }
        if ("afterClassReview".equals(presetCode)) { return "after-class"; }
        if ("groupCollaboration".equals(presetCode)) { return "group-cooperation"; }
        if ("informationTechnologyTask".equals(presetCode)) { return "it-operation"; }
        return presetCode;
    }

    private void collectNames(JsonNode node, Set<String> names)
    {
        if (node == null)
        {
            return;
        }
        if (node.isObject())
        {
            JsonNode name = node.get("name");
            if (name != null && name.isTextual() && StringUtils.isNotBlank(name.asText()))
            {
                names.add(name.asText());
            }
            node.fields().forEachRemaining(entry -> collectNames(entry.getValue(), names));
        }
        else if (node.isArray())
        {
            for (JsonNode child : node)
            {
                collectNames(child, names);
            }
        }
    }

    private ObjectNode objectOptions(ObjectNode node)
    {
        JsonNode options = node.get("options");
        if (options != null && options.isObject())
        {
            return (ObjectNode) options;
        }
        ObjectNode created = objectMapper.createObjectNode();
        node.set("options", created);
        return created;
    }

    private ArrayNode objectArray(ObjectNode node, String field)
    {
        JsonNode value = node.get(field);
        if (value != null && value.isArray())
        {
            return (ArrayNode) value;
        }
        if (value != null)
        {
            throw new ServiceException("导学单组件结构无效");
        }
        ArrayNode created = objectMapper.createArrayNode();
        node.set(field, created);
        return created;
    }
}
