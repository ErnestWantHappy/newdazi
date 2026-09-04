package com.ruoyi.business.service;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.exception.ServiceException;
import static org.junit.jupiter.api.Assertions.*;

class ResearchActivityHtmlSanitizerTest
{
    private final ResearchActivityHtmlSanitizer sanitizer = new ResearchActivityHtmlSanitizer();

    @Test
    void keepsTablesQuillClassesImagesAndSafeLinks()
    {
        String html = "<h2 class='ql-align-center evil'>教研记录</h2>"
                + "<table style='color:red'><tbody><tr><td rowspan='2' colspan='3' data-row='r1'>内容</td></tr></tbody></table>"
                + "<img src='/dev-api/common/resource/view?resource=a.png' width='320' onerror='alert(1)'>"
                + "<a href='https://example.com/a' onclick='bad()'>资料</a>";

        ResearchActivityHtmlSanitizer.SanitizedHtml result = sanitizer.sanitize(html);

        assertTrue(result.getHtml().contains("<table>"));
        assertTrue(result.getHtml().contains("rowspan=\"2\""));
        assertTrue(result.getHtml().contains("colspan=\"3\""));
        assertTrue(result.getHtml().contains("ql-align-center"));
        assertFalse(result.getHtml().contains("evil"));
        assertFalse(result.getHtml().contains("style="));
        assertFalse(result.getHtml().contains("onerror"));
        assertFalse(result.getHtml().contains("onclick"));
        assertTrue(result.getHtml().contains("rel=\"noopener noreferrer\""));
        assertEquals(1, result.getImageCount());
        assertTrue(result.getText().contains("教研记录"));
    }

    @Test
    void preservesOnlyControlledResearchImageDirectory()
    {
        ResearchActivityHtmlSanitizer.SanitizedHtml result = sanitizer.sanitize(
                "<p>图片</p><img src='/dev-api/profile/upload/research-activity/images/2026/07/a.png'>"
                + "<img src='/dev-api/profile/upload/other/a.png'>");
        assertTrue(result.getHtml().contains("research-activity/images/2026/07/a.png"));
        assertFalse(result.getHtml().contains("upload/other"));
        assertEquals(1, result.getImageCount());
    }

    @Test
    void removesScriptsDangerousProtocolsIframeAndBase64Images()
    {
        String html = "<p>安全正文</p><script>alert(1)</script><iframe src='https://evil.test'></iframe>"
                + "<a href='javascript:alert(1)'>危险</a>"
                + "<img src='data:image/png;base64,AAAA'><img src='javascript:alert(1)'>";

        ResearchActivityHtmlSanitizer.SanitizedHtml result = sanitizer.sanitize(html);

        assertFalse(result.getHtml().contains("script"));
        assertFalse(result.getHtml().contains("iframe"));
        assertFalse(result.getHtml().contains("javascript:"));
        assertFalse(result.getHtml().contains("data:image"));
        assertEquals(0, result.getImageCount());
    }

    @Test
    void rejectsMoreThanTwentyImages()
    {
        String images = IntStream.range(0, 21)
                .mapToObj(i -> "<img src='https://example.com/" + i + ".png'>")
                .collect(Collectors.joining());
        assertThrows(ServiceException.class, () -> sanitizer.sanitize("<p>正文</p>" + images));
    }

    @Test
    void rejectsContentWithoutVisibleTextAfterCleaning()
    {
        assertThrows(ServiceException.class,
                () -> sanitizer.sanitize("<script>alert(1)</script><img src='data:image/png;base64,AAAA'>"));
    }

    @Test
    void allowsImageOnlyContent()
    {
        ResearchActivityHtmlSanitizer.SanitizedHtml result = sanitizer.sanitize(
                "<p><img src='/common/resource/view?resource=a.png' alt='活动现场'></p>");
        assertEquals(1, result.getImageCount());
        assertTrue(result.getHtml().contains("resource=a.png"));
        assertEquals("", result.getText());
    }

    @Test
    void allowsTableOnlyContent()
    {
        ResearchActivityHtmlSanitizer.SanitizedHtml result = sanitizer.sanitize(
                "<table><tbody><tr><td><br></td></tr></tbody></table>");
        assertTrue(result.getHtml().contains("<table>"));
        assertEquals(0, result.getImageCount());
    }
}
