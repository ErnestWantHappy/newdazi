package com.ruoyi.business.domain.vo;

/**
 * 智能生成结果只返回教师可使用的草稿内容和通用状态。
 */
public class GuideSheetAiContentVo
{
    private boolean available;
    private String content;
    private String message;

    public static GuideSheetAiContentVo available(String content)
    {
        GuideSheetAiContentVo result = new GuideSheetAiContentVo();
        result.setAvailable(true);
        result.setContent(content);
        return result;
    }

    public static GuideSheetAiContentVo unavailable()
    {
        GuideSheetAiContentVo result = new GuideSheetAiContentVo();
        result.setAvailable(false);
        result.setMessage("AI 服务暂不可用");
        return result;
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
