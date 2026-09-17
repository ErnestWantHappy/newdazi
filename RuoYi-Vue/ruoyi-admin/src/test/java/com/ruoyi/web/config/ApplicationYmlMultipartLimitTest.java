package com.ruoyi.web.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * 防止 spring.servlet.multipart.max-request-size 再次写成 servlet 同级，
 * 被 Spring 忽略后回落到默认 10MB，教研 50MB 课件会被当成系统繁忙。
 */
class ApplicationYmlMultipartLimitTest
{
    @Test
    @SuppressWarnings("unchecked")
    void multipartMaxRequestSizeIsNestedUnderMultipart() throws Exception
    {
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.yml"))
        {
            assertNotNull(in, "application.yml 必须在测试类路径上");
            Map<String, Object> root = yaml.load(in);
            Map<String, Object> spring = (Map<String, Object>) root.get("spring");
            Map<String, Object> servlet = (Map<String, Object>) spring.get("servlet");
            Map<String, Object> multipart = (Map<String, Object>) servlet.get("multipart");
            assertEquals("55MB", String.valueOf(multipart.get("max-file-size")));
            assertEquals("60MB", String.valueOf(multipart.get("max-request-size")));
            assertFalse(servlet.containsKey("max-request-size"));
        }
    }
}
