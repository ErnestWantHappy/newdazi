package com.ruoyi.business.domain.dto;

/**
 * 平台→设备下发请求。
 * text 走判定（AI 失败自动退回关键词）；command 非空时直接指定 ON/OFF/HOLD，两者可只填一个。
 */
public class IotDownlinkRequest
{
    private String text;
    private String command;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
}
