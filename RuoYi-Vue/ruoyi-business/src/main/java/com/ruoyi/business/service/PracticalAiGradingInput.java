package com.ruoyi.business.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.ruoyi.business.domain.PracticalRubricSnapshot;
import com.ruoyi.business.domain.vo.PracticalScoringItemVo;

/** 已去除学生身份信息的单份视觉评分输入。 */
public class PracticalAiGradingInput
{
    private PracticalRubricSnapshot rubric;
    private List<PracticalScoringItemVo> scoringItems = new ArrayList<PracticalScoringItemVo>();
    private List<File> pageImages = new ArrayList<File>();
    private List<String> pageLabels = new ArrayList<String>();
    /** 流程图 JSON、结构检查等辅助上下文，不替代图片证据。 */
    private String auxiliaryContextJson;
    public PracticalRubricSnapshot getRubric() { return rubric; } public void setRubric(PracticalRubricSnapshot v) { rubric=v; }
    public List<PracticalScoringItemVo> getScoringItems() { return scoringItems; } public void setScoringItems(List<PracticalScoringItemVo> v) { scoringItems=v; }
    public List<File> getPageImages() { return pageImages; } public void setPageImages(List<File> v) { pageImages=v; }
    public List<String> getPageLabels() { return pageLabels; } public void setPageLabels(List<String> v) { pageLabels=v; }
    public String getAuxiliaryContextJson() { return auxiliaryContextJson; }
    public void setAuxiliaryContextJson(String v) { auxiliaryContextJson = v; }
}
