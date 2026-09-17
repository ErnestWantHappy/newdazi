package com.ruoyi.business.domain.dto;

import java.util.List;

/**
 * 将新手预设和常用教学模块组装为 VForm3 表单的请求。
 */
public class GuideSheetBeginnerAssembleRequest
{
    private String preset;
    private List<String> modules;
    private String existingFormJson;

    public String getPreset() { return preset; }
    public void setPreset(String preset) { this.preset = preset; }
    public List<String> getModules() { return modules; }
    public void setModules(List<String> modules) { this.modules = modules; }
    public String getExistingFormJson() { return existingFormJson; }
    public void setExistingFormJson(String existingFormJson) { this.existingFormJson = existingFormJson; }
}
