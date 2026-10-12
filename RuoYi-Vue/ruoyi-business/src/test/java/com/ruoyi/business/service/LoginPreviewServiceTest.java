package com.ruoyi.business.service;

import java.io.InputStream;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import com.ruoyi.business.controller.LoginPreviewController;
import com.ruoyi.business.mapper.LoginPreviewMapper;
import com.ruoyi.common.annotation.Anonymous;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginPreviewServiceTest
{
    @Test void scheduledRefreshWarmsEmptyCachesAndSkipsFreshOnes()
    {
        LoginPreviewMapper mapper = mock(LoginPreviewMapper.class);
        when(mapper.selectActivities()).thenReturn(Collections.singletonList(activity()));
        when(mapper.selectLessons()).thenReturn(Collections.singletonList(lesson()));
        LoginPreviewService service = new LoginPreviewService(mapper);
        service.refreshPreviews(); service.refreshPreviews();
        assertEquals(1, service.activities().size()); assertEquals(1, service.lessons().size());
        verify(mapper, times(1)).selectActivities(); verify(mapper, times(1)).selectLessons();
    }

    @Test void validCacheDoesNotWaitForBackgroundRefresh() throws Exception
    {
        LoginPreviewMapper mapper = mock(LoginPreviewMapper.class);
        AtomicLong now = new AtomicLong(); Clock clock = mock(Clock.class);
        when(clock.millis()).thenAnswer(call -> now.get());
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1);
        when(mapper.selectLessons()).thenReturn(Collections.singletonList(lesson())).thenAnswer(call -> {
            started.countDown(); assertTrue(release.await(3, TimeUnit.SECONDS)); return Collections.emptyList();
        });
        LoginPreviewService service = new LoginPreviewService(mapper, clock);
        List<Map<String, Object>> original = service.lessons(); now.set(40_000L);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try
        {
            Future<?> refresh = workers.submit(service::refreshPreviews);
            assertTrue(started.await(1, TimeUnit.SECONDS));
            assertSame(original, workers.submit(service::lessons).get(500, TimeUnit.MILLISECONDS));
            release.countDown(); refresh.get(1, TimeUnit.SECONDS);
            assertTrue(service.lessons().isEmpty());
        }
        finally { release.countDown(); workers.shutdownNow(); }
    }

    @Test void slowLessonsDoNotBlockActivityRequests() throws Exception
    {
        LoginPreviewMapper mapper = mock(LoginPreviewMapper.class);
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1);
        when(mapper.selectActivities()).thenReturn(Collections.singletonList(activity()));
        when(mapper.selectLessons()).thenAnswer(call -> {
            started.countDown(); assertTrue(release.await(3, TimeUnit.SECONDS)); return Collections.emptyList();
        });
        LoginPreviewService service = new LoginPreviewService(mapper);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try
        {
            Future<?> load = workers.submit(service::lessons);
            assertTrue(started.await(1, TimeUnit.SECONDS));
            assertEquals(1, workers.submit(service::activities).get(500, TimeUnit.MILLISECONDS).size());
            release.countDown(); load.get(1, TimeUnit.SECONDS);
        }
        finally { release.countDown(); workers.shutdownNow(); }
    }

    @Test void refreshFailureKeepsValidDataButNeverServesExpiredData()
    {
        LoginPreviewMapper mapper = mock(LoginPreviewMapper.class);
        AtomicLong now = new AtomicLong(); Clock clock = mock(Clock.class);
        when(clock.millis()).thenAnswer(call -> now.get());
        when(mapper.selectLessons()).thenReturn(Collections.singletonList(lesson()))
                .thenThrow(new IllegalStateException("查询失败"));
        LoginPreviewService service = new LoginPreviewService(mapper, clock);
        List<Map<String, Object>> original = service.lessons(); now.set(40_000L);
        service.refreshPreviews(); assertSame(original, service.lessons());
        now.set(60_000L); assertThrows(IllegalStateException.class, service::lessons);
        verify(mapper, times(3)).selectLessons();
    }

    @Test void concurrentColdRequestsShareOneDatabaseQuery() throws Exception
    {
        LoginPreviewMapper mapper = mock(LoginPreviewMapper.class);
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1);
        when(mapper.selectLessons()).thenAnswer(call -> {
            started.countDown(); assertTrue(release.await(3, TimeUnit.SECONDS)); return Collections.singletonList(lesson());
        });
        LoginPreviewService service = new LoginPreviewService(mapper);
        ExecutorService workers = Executors.newFixedThreadPool(8);
        try
        {
            List<Future<List<Map<String, Object>>>> requests = new ArrayList<>();
            for (int i = 0; i < 8; i++) requests.add(workers.submit(service::lessons));
            assertTrue(started.await(1, TimeUnit.SECONDS)); release.countDown();
            List<Map<String, Object>> first = requests.get(0).get(1, TimeUnit.SECONDS);
            for (Future<List<Map<String, Object>>> request : requests) assertSame(first, request.get(1, TimeUnit.SECONDS));
            verify(mapper, times(1)).selectLessons();
        }
        finally { release.countDown(); workers.shutdownNow(); }
    }

    @Test void activityProjectionNeverPublishesBodyResourceTokenOrIdentity()
    {
        LoginPreviewMapper mapper = mock(LoginPreviewMapper.class);
        Map<String, Object> source = activity();
        source.put("contentHtml", "内部正文"); source.put("resourcePath", "/profile/internal.pdf");
        source.put("publicShareTokenHash", "private"); source.put("creatorId", 7L);
        when(mapper.selectActivities()).thenReturn(Collections.nCopies(9, source));
        LoginPreviewService service = new LoginPreviewService(mapper);
        List<Map<String, Object>> rows = service.activities();
        assertEquals(5, rows.size());
        assertEquals(new HashSet<>(Arrays.asList("date", "title", "school", "teacher")), rows.get(0).keySet());
        assertSame(rows, service.activities()); verify(mapper, times(1)).selectActivities();
    }

    @Test void lessonProjectionContainsCountsAndHistoricalClassLabelOnly()
    {
        LoginPreviewMapper mapper = mock(LoginPreviewMapper.class);
        Map<String, Object> source = lesson(); source.put("studentId", 1L); source.put("score", 98);
        when(mapper.selectLessons()).thenReturn(Collections.nCopies(12, source));
        LoginPreviewService service = new LoginPreviewService(mapper);
        List<Map<String, Object>> rows = service.lessons();
        assertEquals(6, rows.size()); assertEquals("八年级(1)班", rows.get(0).get("className"));
        assertEquals(new HashSet<>(Arrays.asList("time", "school", "teacher", "topic", "participantCount",
                "totalStudentCount", "pending", "submittedCount", "className")), rows.get(0).keySet());
        assertEquals(0, rows.get(0).get("pending"));
        assertSame(rows, service.lessons()); verify(mapper, times(1)).selectLessons();
    }

    @Test void missingSchoolTypeDoesNotInventGradeAndFailedQueryCanRetry()
    {
        LoginPreviewMapper mapper = mock(LoginPreviewMapper.class);
        Map<String, Object> source = lesson(); source.remove("schoolType");
        when(mapper.selectLessons()).thenThrow(new IllegalStateException("暂不可用"))
                .thenReturn(Collections.singletonList(source));
        LoginPreviewService service = new LoginPreviewService(mapper);
        assertThrows(IllegalStateException.class, service::lessons);
        assertEquals("2025级(1)班", service.lessons().get(0).get("className"));
    }

    @Test void emptyDataDoesNotGenerateExampleRows()
    {
        LoginPreviewMapper mapper = mock(LoginPreviewMapper.class);
        when(mapper.selectActivities()).thenReturn(Collections.emptyList());
        when(mapper.selectLessons()).thenReturn(Collections.emptyList());
        LoginPreviewService service = new LoginPreviewService(mapper);
        assertTrue(service.activities().isEmpty()); assertTrue(service.lessons().isEmpty());
    }

    @Test void onlyFixedPreviewMethodsAreAnonymousAndAcceptNoQueryArguments() throws Exception
    {
        for (String name : Arrays.asList("activities", "lessons", "focusSlides"))
        {
            assertNotNull(LoginPreviewController.class.getMethod(name).getAnnotation(Anonymous.class));
            assertEquals(0, LoginPreviewController.class.getMethod(name).getParameterCount());
        }
        assertNotNull(LoginPreviewController.class.getMethod("activityImage", Long.class, javax.servlet.http.HttpServletResponse.class)
                .getAnnotation(Anonymous.class));
        assertNotNull(LoginPreviewController.class.getMethod("postImage", Long.class, Integer.class, javax.servlet.http.HttpServletResponse.class)
                .getAnnotation(Anonymous.class));
        assertNotNull(com.ruoyi.business.controller.TeachingSupervisionController.class
                .getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class));
        assertNotNull(com.ruoyi.business.controller.ResearchActivityController.class
                .getMethod("topic", Long.class).getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class));
    }

    @Test void focusSlidesExtractsUpToFiveRealImagesFromPosts()
    {
        LoginPreviewMapper mapper = mock(LoginPreviewMapper.class);
        Map<String, Object> post1 = new HashMap<>();
        post1.put("postId", 40L);
        post1.put("topicId", 13L);
        post1.put("topicTitle", "高三高考赋分研讨与观课纪实");
        post1.put("school", "象山县第二中学");
        post1.put("teacher", "潘桑柔");
        post1.put("date", "10-05");
        post1.put("contentHtml", "<p>活动现场照片：</p><img src=\"/profile/upload/research-activity/images/2026/10/05/pic1.jpg\" /><p>图2</p><img src=\"/profile/upload/research-activity/images/2026/10/05/pic2.png\" /><img src=\"/profile/upload/research-activity/images/2026/10/05/pic3.jpg\" />");

        Map<String, Object> post2 = new HashMap<>();
        post2.put("postId", 28L);
        post2.put("topicId", 12L);
        post2.put("topicTitle", "初中信息科技实验课");
        post2.put("school", "实验初中");
        post2.put("teacher", "张老师");
        post2.put("date", "10-04");
        post2.put("contentHtml", "<img src=\"/profile/upload/research-activity/images/2026/10/04/p4.jpg\" /><img src=\"/profile/upload/research-activity/images/2026/10/04/p5.jpg\" /><img src=\"/profile/upload/research-activity/images/2026/10/04/p6.jpg\" />");

        when(mapper.selectLatestPostImages()).thenReturn(Arrays.asList(post1, post2));
        LoginPreviewService service = new LoginPreviewService(mapper);
        List<Map<String, Object>> slides = service.focusSlides();

        // 验证最多截取 5 张
        assertEquals(5, slides.size());
        assertEquals("/business/login-preview/post-image?postId=40&imgIndex=0", slides.get(0).get("image"));
        assertEquals("高三高考赋分研讨与观课纪实", slides.get(0).get("title"));
        assertEquals("象山县第二中学 · 潘桑柔", slides.get(0).get("school"));
        assertEquals("10-05", slides.get(0).get("date"));
        assertEquals("课堂实况", slides.get(0).get("tag"));

        assertEquals("/business/login-preview/post-image?postId=40&imgIndex=1", slides.get(1).get("image"));
        assertEquals("/business/login-preview/post-image?postId=40&imgIndex=2", slides.get(2).get("image"));
        assertEquals("/business/login-preview/post-image?postId=28&imgIndex=0", slides.get(3).get("image"));
        assertEquals("/business/login-preview/post-image?postId=28&imgIndex=1", slides.get(4).get("image"));
    }

    @Test void mapperParsesWithFixedLimitsAndNeverSelectsDetailColumns() throws Exception
    {
        Configuration configuration = new Configuration();
        String resource = "mapper/business/LoginPreviewMapper.xml";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource))
        {
            new XMLMapperBuilder(in, configuration, resource, configuration.getSqlFragments()).parse();
        }
        String activitiesSql = configuration.getMappedStatement(LoginPreviewMapper.class.getName() + ".selectActivities")
                .getBoundSql(null).getSql();
        String lessonsSql = configuration.getMappedStatement(LoginPreviewMapper.class.getName() + ".selectLessons")
                .getBoundSql(null).getSql();
        String postImagesSql = configuration.getMappedStatement(LoginPreviewMapper.class.getName() + ".selectLatestPostImages")
                .getBoundSql(null).getSql();
        assertTrue(activitiesSql.contains("LIMIT 5")); assertTrue(lessonsSql.contains("LIMIT 6"));
        assertTrue(postImagesSql.contains("LIMIT 10"));
        assertFalse(activitiesSql.contains("content_html")); assertFalse(activitiesSql.contains("public_share_token"));
        assertTrue(lessonsSql.contains("TRIM(a.student_answer) <> ''"));
        assertTrue(lessonsSql.contains("a.score IS NULL"));
        assertTrue(lessonsSql.contains("lq.question_id = a.question_id"));
    }


    private Map<String, Object> activity()
    {
        Map<String, Object> row = new HashMap<>();
        row.put("date", "10-05"); row.put("title", "教研主题"); row.put("school", "学校"); row.put("teacher", "教师");
        return row;
    }

    private Map<String, Object> lesson()
    {
        Map<String, Object> row = new HashMap<>();
        row.put("time", "10-05 09:30"); row.put("usageDay", "2026-10-05");
        row.put("entryYear", "2025"); row.put("schoolType", "2"); row.put("classCode", "1");
        row.put("school", "学校"); row.put("teacher", "教师"); row.put("topic", "课程");
        row.put("participantCount", 3); row.put("totalStudentCount", 40); row.put("pending", 0); row.put("submittedCount", 3);
        return row;
    }
}
