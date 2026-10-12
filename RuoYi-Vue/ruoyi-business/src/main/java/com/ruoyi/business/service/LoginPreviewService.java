package com.ruoyi.business.service;

import java.time.LocalDate;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.business.mapper.LoginPreviewMapper;
import com.ruoyi.business.util.AcademicYearUtils;

/** 无筛选、无分页的公开概览，固定容量并短时缓存，避免匿名请求反复聚合课堂数据。 */
@Service
public class LoginPreviewService
{
    private final LoginPreviewMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(LoginPreviewService.class);
    private static final long CACHE_TTL = 60_000L;
    private static final long REFRESH_AHEAD = 30_000L;
    private final Clock clock;
    private final Object activitiesLock = new Object();
    private final Object lessonsLock = new Object();
    private final Object focusSlidesLock = new Object();
    private volatile PreviewCache activitiesCache;
    private volatile PreviewCache lessonsCache;
    private volatile PreviewCache focusSlidesCache;

    @Autowired(required = false)
    private ResearchActivityUploadService uploadService;


    private static final java.util.regex.Pattern IMG_SRC_PATTERN =
            java.util.regex.Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"]", java.util.regex.Pattern.CASE_INSENSITIVE);

    @Autowired
    public LoginPreviewService(LoginPreviewMapper mapper) { this(mapper, Clock.systemUTC()); }

    LoginPreviewService(LoginPreviewMapper mapper, Clock clock)
    {
        this.mapper = mapper;
        this.clock = clock;
    }

    public List<Map<String, Object>> activities()
    {
        PreviewCache cached = activitiesCache;
        if (valid(cached)) return cached.rows;
        synchronized (activitiesLock)
        {
            if (!valid(activitiesCache)) activitiesCache = loadActivities();
            return activitiesCache.rows;
        }
    }

    private PreviewCache loadActivities()
    {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> source : bounded(mapper.selectActivities(), 5))
        {
            Map<String, Object> row = project(source, "date", "title", "school", "teacher");
            Long topicId = source.get("topicId") == null ? null : Long.valueOf(source.get("topicId").toString());
            if (topicId != null)
            {
                String imageUrl = extractFirstImage(topicId);
                if (imageUrl != null)
                {
                    row.put("image", imageUrl);
                }
            }
            result.add(row);
        }
        return new PreviewCache(result, clock.millis() + CACHE_TTL);
    }

    public String extractFirstImage(Long topicId)
    {
        if (topicId == null) return null;
        try
        {
            String html = mapper.selectTopicContentHtml(topicId);
            if (html == null || html.isEmpty()) return null;
            java.util.regex.Matcher m = IMG_SRC_PATTERN.matcher(html);
            while (m.find())
            {
                String src = m.group(1);
                if (isLocalImageSource(src))
                {
                    return "/business/login-preview/activity-image?topicId=" + topicId;
                }
            }
        }
        catch (Exception ex)
        {
            log.warn("提取教研活动首图失败: topicId={}", topicId);
        }
        return null;
    }

    public java.nio.file.Path resolveActivityImage(Long topicId)
    {
        if (topicId == null || uploadService == null) return null;
        try
        {
            String html = mapper.selectTopicContentHtml(topicId);
            if (html == null || html.isEmpty()) return null;
            java.util.regex.Matcher m = IMG_SRC_PATTERN.matcher(html);
            while (m.find())
            {
                String src = m.group(1);
                if (isLocalImageSource(src)) return uploadService.resolveLoginPreviewImage(uploadService.resolvePublicImagePath(src));
            }
        }
        catch (Exception ex)
        {
            log.warn("解析教研活动图片文件路径失败: topicId={}", topicId);
        }
        return null;
    }

    public List<Map<String, Object>> focusSlides()
    {
        PreviewCache cached = focusSlidesCache;
        if (valid(cached)) return cached.rows;
        synchronized (focusSlidesLock)
        {
            if (!valid(focusSlidesCache)) focusSlidesCache = loadFocusSlides();
            return focusSlidesCache.rows;
        }
    }

    private PreviewCache loadFocusSlides()
    {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> posts = mapper.selectLatestPostImages();
        if (posts != null)
        {
            for (Map<String, Object> post : posts)
            {
                if (result.size() >= 5) break;
                Long postId = post.get("postId") == null ? null : Long.valueOf(post.get("postId").toString());
                String html = (String) post.get("contentHtml");
                if (postId == null || html == null || html.isEmpty()) continue;

                String topicTitle = (String) post.get("topicTitle");
                String school = (String) post.get("school");
                String teacher = (String) post.get("teacher");
                String date = (String) post.get("date");

                java.util.regex.Matcher m = IMG_SRC_PATTERN.matcher(html);
                int imgIdx = 0;
                while (m.find() && result.size() < 5)
                {
                    String src = m.group(1);
                    if (isLocalImageSource(src))
                    {
                        Map<String, Object> slide = new LinkedHashMap<>();
                        slide.put("image", "/business/login-preview/post-image?postId=" + postId + "&imgIndex=" + imgIdx);
                        slide.put("title", topicTitle != null && !topicTitle.isEmpty() ? topicTitle : "教研课堂实况展评");
                        slide.put("school", (school != null ? school : "本校") + " · " + (teacher != null ? teacher : "教师"));
                        slide.put("date", date != null ? date : "近期");
                        slide.put("tag", "课堂实况");
                        result.add(slide);
                    }
                    imgIdx++;
                }
            }
        }
        return new PreviewCache(result, clock.millis() + CACHE_TTL);
    }

    static boolean isLocalImageSource(String src)
    {
        if (src == null) return false;
        // 外链不能交给仅支持本机文件的代理；资源包装地址仍由上传服务复核路径。
        return src.startsWith("/profile/upload/research-activity/images/")
                || src.startsWith("/dev-api/profile/upload/research-activity/images/")
                || src.startsWith("/prod-api/profile/upload/research-activity/images/")
                || src.startsWith("/common/resource/view?resource=")
                || src.startsWith("/dev-api/common/resource/view?resource=")
                || src.startsWith("/prod-api/common/resource/view?resource=");
    }

    public java.nio.file.Path resolvePostImage(Long postId, Integer imgIndex)
    {
        if (postId == null || uploadService == null) return null;
        try
        {
            String html = mapper.selectPostContentHtml(postId);
            if (html == null || html.isEmpty()) return null;
            java.util.regex.Matcher m = IMG_SRC_PATTERN.matcher(html);
            int targetIdx = (imgIndex == null || imgIndex < 0) ? 0 : imgIndex;
            int currentIdx = 0;
            while (m.find())
            {
                if (currentIdx == targetIdx)
                {
                    String src = m.group(1);
                    return uploadService.resolveLoginPreviewImage(uploadService.resolvePublicImagePath(src));
                }
                currentIdx++;
            }
        }
        catch (Exception ex)
        {
            log.warn("解析教研跟帖图片文件路径失败: postId={}, imgIndex={}", postId, imgIndex);
        }
        return null;
    }

    public List<Map<String, Object>> lessons()
    {
        PreviewCache cached = lessonsCache;
        if (valid(cached)) return cached.rows;
        synchronized (lessonsLock)
        {
            if (!valid(lessonsCache)) lessonsCache = loadLessons();
            return lessonsCache.rows;
        }
    }

    private PreviewCache loadLessons()
    {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> source : bounded(mapper.selectLessons(), 6))
        {
            Map<String, Object> row = project(source, "time", "school", "teacher", "topic",
                    "participantCount", "totalStudentCount", "pending", "submittedCount");
            row.put("className", className(source));
            result.add(row);
        }
        return new PreviewCache(result, clock.millis() + CACHE_TTL);
    }

    // 使用现有调度器预热并提前刷新；请求直接读有效快照，不等后台查询释放锁。
    @Scheduled(initialDelay = 3_000L, fixedDelay = 20_000L)
    public void refreshPreviews()
    {
        try
        {
            synchronized (activitiesLock)
            {
                if (refreshDue(activitiesCache)) activitiesCache = loadActivities();
            }
        }
        catch (RuntimeException ex) { log.warn("教研概览预刷新失败，保留仍有效的缓存"); }
        try
        {
            synchronized (lessonsLock)
            {
                if (refreshDue(lessonsCache)) lessonsCache = loadLessons();
            }
        }
        catch (RuntimeException ex) { log.warn("开课概览预刷新失败，保留仍有效的缓存"); }
        try
        {
            synchronized (focusSlidesLock)
            {
                if (refreshDue(focusSlidesCache)) focusSlidesCache = loadFocusSlides();
            }
        }
        catch (RuntimeException ex) { log.warn("焦点图预刷新失败，保留仍有效的缓存"); }
    }


    private boolean valid(PreviewCache cached)
    {
        return cached != null && clock.millis() < cached.expiresAt;
    }

    private boolean refreshDue(PreviewCache cached)
    {
        return cached == null || cached.expiresAt - clock.millis() <= REFRESH_AHEAD;
    }

    private static final class PreviewCache
    {
        private final List<Map<String, Object>> rows;
        private final long expiresAt;

        private PreviewCache(List<Map<String, Object>> rows, long expiresAt)
        {
            this.rows = Collections.unmodifiableList(rows);
            this.expiresAt = expiresAt;
        }
    }

    private List<Map<String, Object>> bounded(List<Map<String, Object>> rows, int limit)
    {
        return rows == null ? Collections.emptyList() : rows.subList(0, Math.min(rows.size(), limit));
    }

    private Map<String, Object> project(Map<String, Object> source, String... fields)
    {
        // 第二层显式白名单，后续 SQL 增加内部字段也不会自动扩大公开数据面。
        Map<String, Object> row = new LinkedHashMap<>();
        for (String field : fields) row.put(field, source.get(field));
        return row;
    }

    private String className(Map<String, Object> row)
    {
        String entryYear = String.valueOf(row.get("entryYear"));
        String gradeName;
        try
        {
            LocalDate date = LocalDate.parse(String.valueOf(row.get("usageDay")));
            gradeName = AcademicYearUtils.resolveGradeName(entryYear, String.valueOf(row.get("schoolType")), date);
        }
        catch (IllegalArgumentException ex)
        {
            // 缺学段或历史届证据不足时展示原始入学年份，不猜测年级。
            gradeName = entryYear + "级";
        }
        return gradeName + "(" + row.get("classCode") + ")班";
    }
}
