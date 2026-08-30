package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

/** AI 模型按量计费参考价，单位为元/千 token。 */
public class AiModelPrice
{
    private String providerCode;
    private String modelName;
    private String displayName;
    private BigDecimal inputPricePerThousand;
    private BigDecimal outputPricePerThousand;
    private String priceStatus;
    private String priceNote;
    private String updateBy;
    private Date createTime;
    private Date updateTime;

    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String v) { providerCode = v; }
    public String getModelName() { return modelName; }
    public void setModelName(String v) { modelName = v; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String v) { displayName = v; }
    public BigDecimal getInputPricePerThousand() { return inputPricePerThousand; }
    public void setInputPricePerThousand(BigDecimal v) { inputPricePerThousand = v; }
    public BigDecimal getOutputPricePerThousand() { return outputPricePerThousand; }
    public void setOutputPricePerThousand(BigDecimal v) { outputPricePerThousand = v; }
    public String getPriceStatus() { return priceStatus; }
    public void setPriceStatus(String v) { priceStatus = v; }
    public String getPriceNote() { return priceNote; }
    public void setPriceNote(String v) { priceNote = v; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String v) { updateBy = v; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date v) { createTime = v; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date v) { updateTime = v; }
}
