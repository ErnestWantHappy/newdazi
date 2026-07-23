package com.ruoyi.business.domain.vo;

import com.ruoyi.business.domain.BizResearchResource;

/** 资源页面对象；链接状态由服务层按当前时间计算。 */
public class ResearchResourceVo extends BizResearchResource
{
    private static final long serialVersionUID = 1L;
    private String linkStatus;
    private String linkStatusText;

    public String getLinkStatus() { return linkStatus; }
    public void setLinkStatus(String linkStatus) { this.linkStatus = linkStatus; }
    public String getLinkStatusText() { return linkStatusText; }
    public void setLinkStatusText(String linkStatusText) { this.linkStatusText = linkStatusText; }
}
