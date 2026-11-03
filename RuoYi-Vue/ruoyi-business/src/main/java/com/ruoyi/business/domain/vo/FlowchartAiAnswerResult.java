package com.ruoyi.business.domain.vo;

import java.util.ArrayList;
import java.util.List;

/** 题库画流程图题“拍照 + AI 生成标准答案”的返回体。 */
public class FlowchartAiAnswerResult
{
    /** 已通过服务端规范化与安全校验的标准答案 JSON，可直接载入画布。 */
    private String answerJson;
    /** 识别过程中的可解释提示，例如被忽略的悬空连线或归并的未知节点类型。 */
    private List<String> warnings = new ArrayList<String>();
    /** 生成文档包含的节点数量，便于前端与老师快速判断识别规模。 */
    private int nodeCount;

    public String getAnswerJson() { return answerJson; }
    public void setAnswerJson(String answerJson) { this.answerJson = answerJson; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings == null ? new ArrayList<String>() : warnings; }

    public int getNodeCount() { return nodeCount; }
    public void setNodeCount(int nodeCount) { this.nodeCount = nodeCount; }
}
