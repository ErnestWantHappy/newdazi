package com.ruoyi.business.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.business.config.IotMqttProperties;

/**
 * EMQX v5 管理 API 适配器（兼容 Java 8）：同步班级 MQTT 账号与 Topic 前缀 ACL 规则。
 * 管理凭据仅由部署环境注入，绝不流向浏览器、日志或 Git。
 */
@Service
public class IotEmqxAdapter
{
    private static final Logger log = LoggerFactory.getLogger(IotEmqxAdapter.class);

    @Autowired
    private IotMqttProperties properties;

    /**
     * 创建或更新班级账号及密码
     */
    public boolean syncClassAccount(String username, String password)
    {
        if (!isApiConfigured())
        {
            log.info("EMQX 管理 API 未配置或未启用，跳过账号同步 username={}", username);
            return false;
        }

        String baseUrl = trimTrailingSlash(properties.getEmqxApiUrl());
        // EMQX v5 内置数据库认证端点
        String userUrl = baseUrl + "/authentication/password_based:built_in_database/users";

        try
        {
            JSONObject body = new JSONObject();
            body.put("user_id", username);
            body.put("password", password);

            HttpResult result = sendRequest(userUrl, "POST", body.toJSONString());
            int status = result.statusCode;

            if (status == 201 || status == 200)
            {
                log.info("EMQX 班级账号创建成功 username={}", username);
                return true;
            }
            else if (status == 409 || status == 400)
            {
                // 账号已存在，调用 PUT 更新密码
                String updateUrl = userUrl + "/" + urlEncode(username);
                JSONObject updateBody = new JSONObject();
                updateBody.put("password", password);

                HttpResult putResp = sendRequest(updateUrl, "PUT", updateBody.toJSONString());
                if (putResp.statusCode == 200 || putResp.statusCode == 204)
                {
                    log.info("EMQX 班级账号密码更新成功 username={}", username);
                    return true;
                }
                else
                {
                    log.warn("EMQX 班级账号更新失败 status={} body={}", putResp.statusCode, putResp.body);
                    return false;
                }
            }
            else
            {
                log.warn("EMQX 班级账号创建响应非预期 status={} body={}", status, result.body);
                return false;
            }
        }
        catch (Exception e)
        {
            log.warn("EMQX 班级账号同步异常 username={}, 原因: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * 设置班级 Topic 前缀发布 ACL（例如 county/169/270/2024-01/#）
     */
    public boolean syncClassAcl(String username, String topicPrefix)
    {
        if (!isApiConfigured())
        {
            return false;
        }

        String baseUrl = trimTrailingSlash(properties.getEmqxApiUrl());
        // EMQX v5 内置数据库授权端点
        String aclUrl = baseUrl + "/authorization/sources/built_in_database/rules/users/" + urlEncode(username);

        try
        {
            String pattern = topicPrefix.endsWith("/#") ? topicPrefix : (topicPrefix.endsWith("/") ? topicPrefix + "#" : topicPrefix + "/#");

            JSONObject rule = new JSONObject();
            rule.put("action", "publish");
            rule.put("permission", "allow");
            rule.put("topic", pattern);

            JSONArray rules = new JSONArray();
            rules.add(rule);

            JSONObject body = new JSONObject();
            body.put("rules", rules);

            HttpResult response = sendRequest(aclUrl, "PUT", body.toJSONString());
            if (response.statusCode == 200 || response.statusCode == 204)
            {
                log.info("EMQX 班级 Topic ACL 设置成功 username={} prefix={}", username, pattern);
                return true;
            }
            else
            {
                log.warn("EMQX 班级 ACL 设置响应非预期 status={} body={}", response.statusCode, response.body);
                return false;
            }
        }
        catch (Exception e)
        {
            log.warn("EMQX 班级 ACL 设置异常 username={}, 原因: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * 注销班级账号
     */
    public boolean revokeClassAccount(String username)
    {
        if (!isApiConfigured() || username == null || username.trim().isEmpty())
        {
            return false;
        }

        String baseUrl = trimTrailingSlash(properties.getEmqxApiUrl());
        String userUrl = baseUrl + "/authentication/password_based:built_in_database/users/" + urlEncode(username);

        try
        {
            HttpResult response = sendRequest(userUrl, "DELETE", null);
            return response.statusCode == 200 || response.statusCode == 204 || response.statusCode == 404;
        }
        catch (Exception e)
        {
            log.warn("EMQX 账号注销异常 username={}, 原因: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * 检查 EMQX API 是否配置
     */
    public boolean isApiConfigured()
    {
        String url = properties.getEmqxApiUrl();
        if (url == null || url.trim().isEmpty()) return false;
        return (properties.getEmqxApiToken() != null && !properties.getEmqxApiToken().trim().isEmpty())
                || (properties.getEmqxApiKey() != null && !properties.getEmqxApiKey().trim().isEmpty());
    }

    private HttpResult sendRequest(String urlStr, String method, String jsonBody) throws Exception
    {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("Content-Type", "application/json");

        if (properties.getEmqxApiToken() != null && !properties.getEmqxApiToken().trim().isEmpty())
        {
            conn.setRequestProperty("Authorization", "Bearer " + properties.getEmqxApiToken().trim());
        }
        else if (properties.getEmqxApiKey() != null && properties.getEmqxApiSecret() != null)
        {
            String token = Base64.getEncoder().encodeToString(
                    (properties.getEmqxApiKey().trim() + ":" + properties.getEmqxApiSecret().trim()).getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + token);
        }

        if (jsonBody != null && !jsonBody.isEmpty())
        {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream())
            {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        }

        int status = conn.getResponseCode();
        InputStream is = status >= 200 && status < 400 ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder body = new StringBuilder();
        if (is != null)
        {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    body.append(line);
                }
            }
        }
        conn.disconnect();
        return new HttpResult(status, body.toString());
    }

    private static class HttpResult
    {
        final int statusCode;
        final String body;

        HttpResult(int statusCode, String body)
        {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

    private String trimTrailingSlash(String url)
    {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String urlEncode(String value)
    {
        try
        {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (Exception e)
        {
            return value;
        }
    }
}
