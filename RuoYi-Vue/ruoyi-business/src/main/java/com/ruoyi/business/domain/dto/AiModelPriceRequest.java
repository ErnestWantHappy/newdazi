package com.ruoyi.business.domain.dto;

import java.math.BigDecimal;

/** 管理员维护模型参考价的最小请求。 */
public class AiModelPriceRequest
{
    private BigDecimal inputPricePerThousand;
    private BigDecimal outputPricePerThousand;
    private String priceStatus;
    private String priceNote;

    public BigDecimal getInputPricePerThousand() { return inputPricePerThousand; }
    public void setInputPricePerThousand(BigDecimal v) { inputPricePerThousand = v; }
    public BigDecimal getOutputPricePerThousand() { return outputPricePerThousand; }
    public void setOutputPricePerThousand(BigDecimal v) { outputPricePerThousand = v; }
    public String getPriceStatus() { return priceStatus; }
    public void setPriceStatus(String v) { priceStatus = v; }
    public String getPriceNote() { return priceNote; }
    public void setPriceNote(String v) { priceNote = v; }
}
