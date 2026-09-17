package com.ruoyi.business.domain.vo;

import java.util.ArrayList;
import java.util.List;

/** 学生批量纠错预览及执行结果。 */
public class StudentCorrectionPreview
{
    private int totalCount;
    private int validCount;
    private int invalidCount;
    private int changedCount;
    private List<StudentCorrectionRow> rows = new ArrayList<>();

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getValidCount() { return validCount; }
    public void setValidCount(int validCount) { this.validCount = validCount; }
    public int getInvalidCount() { return invalidCount; }
    public void setInvalidCount(int invalidCount) { this.invalidCount = invalidCount; }
    public int getChangedCount() { return changedCount; }
    public void setChangedCount(int changedCount) { this.changedCount = changedCount; }
    public List<StudentCorrectionRow> getRows() { return rows; }
    public void setRows(List<StudentCorrectionRow> rows) { this.rows = rows; }
}
