package com.ruoyi.business.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import com.ruoyi.business.constant.ResearchActivityConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 教研活动富文本清洗器。
 * 正文会长期被教师反复编辑，因此后端必须保存可回显的安全 HTML，而不是信任前端 v-html。
 */
@Service
public class ResearchActivityHtmlSanitizer
{
    private static final Pattern SAFE_CLASS = Pattern.compile(
            "^(ql-align-(center|right|justify)|ql-size-(small|large|huge)|ql-indent-[1-9]|ql-direction-rtl|ql-font-(serif|monospace))$");
    private static final Pattern SAFE_DIMENSION = Pattern.compile("^[1-9][0-9]{0,3}$");

    private final Safelist safelist;

    public ResearchActivityHtmlSanitizer()
    {
        safelist = new Safelist()
                .addTags("p", "br", "h1", "h2", "h3", "h4", "h5", "h6", "strong", "em", "u", "s",
                        "blockquote", "pre", "code", "ol", "ul", "li", "span", "div",
                        "table", "thead", "tbody", "tfoot", "tr", "th", "td", "a", "img")
                .addAttributes(":all", "class")
                .addAttributes("table", "summary")
                .addAttributes("th", "rowspan", "colspan", "scope")
                .addAttributes("td", "rowspan", "colspan", "data-row")
                .addAttributes("a", "href", "title", "target", "rel")
                .addAttributes("img", "src", "alt", "title", "width", "height");
    }

    public SanitizedHtml sanitize(String html)
    {
        if (StringUtils.isBlank(html))
        {
            throw new ServiceException("请输入正文内容");
        }
        if (html.length() > ResearchActivityConstants.MAX_HTML_LENGTH)
        {
            throw new ServiceException("富文本内容过长，请精简后重试");
        }

        Document dirty = Jsoup.parseBodyFragment(html);
        Document clean = new Cleaner(safelist).clean(dirty);
        Element body = clean.body();
        filterClasses(body);
        filterLinks(body);
        filterImages(body);
        filterTableAttributes(body);

        String text = body.text().trim();
        int imageCount = body.select("img").size();
        boolean hasTable = !body.select("table").isEmpty();
        // 活动纪实等场景允许「仅图片/仅表格」：无可见文字时，只要还有有效媒体或表格即可
        if (StringUtils.isBlank(text) && imageCount == 0 && !hasTable)
        {
            throw new ServiceException("正文去除格式后不能为空");
        }
        if (text.length() > ResearchActivityConstants.MAX_TEXT_LENGTH)
        {
            throw new ServiceException("正文文字内容过长，请精简后重试");
        }
        if (imageCount > ResearchActivityConstants.MAX_IMAGES)
        {
            throw new ServiceException("每条内容最多包含20张图片");
        }
        return new SanitizedHtml(body.html(), text, imageCount);
    }

    private void filterClasses(Element body)
    {
        for (Element element : body.select("[class]"))
        {
            List<String> safe = new ArrayList<>();
            for (String className : element.classNames())
            {
                if (SAFE_CLASS.matcher(className).matches())
                {
                    safe.add(className);
                }
            }
            element.removeAttr("class");
            for (String className : safe)
            {
                element.addClass(className);
            }
        }
    }

    private void filterLinks(Element body)
    {
        for (Element link : body.select("a"))
        {
            String href = link.attr("href").trim();
            if (!isHttpUrl(href))
            {
                link.removeAttr("href");
                link.removeAttr("target");
                link.removeAttr("rel");
                continue;
            }
            link.attr("target", "_blank");
            link.attr("rel", "noopener noreferrer");
        }
    }

    private void filterImages(Element body)
    {
        for (Element image : new ArrayList<>(body.select("img")))
        {
            String src = image.attr("src").trim();
            if (!isSafeImageSource(src))
            {
                image.remove();
                continue;
            }
            if (!SAFE_DIMENSION.matcher(image.attr("width")).matches()) image.removeAttr("width");
            if (!SAFE_DIMENSION.matcher(image.attr("height")).matches()) image.removeAttr("height");
        }
    }

    private void filterTableAttributes(Element body)
    {
        for (Element cell : body.select("th,td"))
        {
            normalizeSpan(cell, "rowspan");
            normalizeSpan(cell, "colspan");
        }
    }

    private void normalizeSpan(Element cell, String name)
    {
        if (!cell.hasAttr(name)) return;
        try
        {
            int value = Integer.parseInt(cell.attr(name));
            if (value < 1 || value > 100) cell.removeAttr(name);
        }
        catch (NumberFormatException e)
        {
            cell.removeAttr(name);
        }
    }

    private boolean isSafeImageSource(String src)
    {
        String lower = src.toLowerCase();
        if (lower.startsWith("data:") || lower.startsWith("javascript:") || lower.startsWith("//"))
        {
            return false;
        }
        if (isHttpUrl(src)) return true;
        return src.startsWith("/common/resource/view?")
                || src.startsWith("/dev-api/common/resource/view?")
                || src.startsWith("/prod-api/common/resource/view?")
                || src.startsWith("/profile/upload/research-activity/images/")
                || src.startsWith("/dev-api/profile/upload/research-activity/images/")
                || src.startsWith("/prod-api/profile/upload/research-activity/images/");
    }

    private boolean isHttpUrl(String value)
    {
        try
        {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            return uri.isAbsolute() && uri.getHost() != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    /** 清洗结果同时保存 HTML 与纯文本，避免查询时反复解析 HTML。 */
    public static class SanitizedHtml
    {
        private final String html;
        private final String text;
        private final int imageCount;

        public SanitizedHtml(String html, String text, int imageCount)
        {
            this.html = html;
            this.text = text;
            this.imageCount = imageCount;
        }

        public String getHtml() { return html; }
        public String getText() { return text; }
        public int getImageCount() { return imageCount; }
    }
}
