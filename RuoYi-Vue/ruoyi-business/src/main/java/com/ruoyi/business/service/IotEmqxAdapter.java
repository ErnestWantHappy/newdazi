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
import com.alibaba.fastjson2.JSON;
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
     * 设置班级账号 Topic 权限（AIoT 双向）：
     * 1）发布：只允许本班各小组的上行数据主题 county/{学校}/{课程}/{班级}/{实验}/{小组}/data；
     * 2）订阅：允许本班原有数据主题 …/data，并兼容已使用的 …/control。
     *
     * 班级账号为全班共用，Broker 只能约束到班级前缀，不能区分同班各组设备；
     * 跨班、跨校发布与订阅仍被默认拒绝。
     */
    public boolean syncClassAcl(String username, String topicPrefix)
    {
        if (!isApiConfigured())
        {
            return false;
        }

        String base = normalizeTopicBase(topicPrefix);
        if (base.isEmpty())
        {
            log.warn("班级 ACL 前缀为空，拒绝写入宽权限 username={}", username);
            return false;
        }
        String segment = properties.getDownlinkSegment();

        JSONArray rules = new JSONArray();
        rules.add(buildRule("publish", "allow", base + "/+/+/data"));
        rules.add(buildRule("subscribe", "allow", base + "/+/+/" + segment));
        // Mind+ 原程序在同一个 data Topic 发布和订阅，不能仅授权新 control Topic。
        if (!"data".equals(segment))
        {
            rules.add(buildRule("subscribe", "allow", base + "/+/+/data"));
        }
        return putUserRules(username, rules, "班级 Topic 权限");
    }

    /**
     * 为平台订阅账号补齐下行发布权限。
     * 该账号原本只有订阅权限；下发必须能向各组 control 主题发布。
     * 用户规则是覆盖写，因此必须同时写回订阅规则，否则会丢掉整条接收链路。
     */
    public boolean syncDownlinkPublisherAcl(String username)
    {
        if (username == null || username.trim().isEmpty() || !isApiConfigured())
        {
            return false;
        }

        JSONArray rules = new JSONArray();
        rules.add(buildRule("subscribe", "allow", "county/#"));
        rules.add(buildRule("publish", "allow", "county/+/+/+/+/+/" + properties.getDownlinkSegment()));
        return putUserRules(username, rules, "平台下行发布权限");
    }

    /** 去掉末尾的 /# 与 #，只保留可拼接的 Topic 前缀。 */
    private String normalizeTopicBase(String topicPrefix)
    {
        String base = topicPrefix == null ? "" : topicPrefix.trim();
        if (base.endsWith("/#"))
        {
            base = base.substring(0, base.length() - 2);
        }
        else if (base.endsWith("#"))
        {
            base = base.substring(0, base.length() - 1);
        }
        while (base.endsWith("/"))
        {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    private JSONObject buildRule(String action, String permission, String topic)
    {
        JSONObject rule = new JSONObject();
        rule.put("action", action);
        rule.put("permission", permission);
        rule.put("topic", topic);
        return rule;
    }

    /**
     * 覆盖写入某账号的用户级规则。EMQX v5 的用户规则 PUT 接口要求同时携带 username，缺少时会返回 400。
     */
    private boolean putUserRules(String username, JSONArray rules, String scene)
    {
        String baseUrl = trimTrailingSlash(properties.getEmqxApiUrl());
        String aclUrl = baseUrl + "/authorization/sources/built_in_database/rules/users/" + urlEncode(username);

        try
        {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("rules", rules);

            HttpResult response = sendRequest(aclUrl, "PUT", body.toJSONString());
            if (response.statusCode == 200 || response.statusCode == 204)
            {
                log.info("EMQX {} 设置成功 username={} rules={}", scene, username, rules.size());
                return true;
            }
            log.warn("EMQX {} 设置响应非预期 status={} body={}", scene, response.statusCode, response.body);
            return false;
        }
        catch (Exception e)
        {
            log.warn("EMQX {} 设置异常 username={}, 原因: {}", scene, username, e.getMessage());
            return false;
        }
    }

    /**
     * 确认精确授权使用的内置数据库数据源已经启用。
     * 未就绪时不能把班级参数发给学生，否则账号可能落入旧的宽泛文件规则。
     */
    public boolean isBuiltInAuthorizationReady()
    {
        if (!isApiConfigured()) return false;
        String url = trimTrailingSlash(properties.getEmqxApiUrl()) + "/authorization/sources";
        try
        {
            HttpResult response = sendRequest(url, "GET", null);
            if (response.statusCode != 200) return false;

            Object parsed = JSON.parse(response.body);
            JSONArray sources = null;
            if (parsed instanceof JSONArray)
            {
                sources = (JSONArray) parsed;
            }
            else if (parsed instanceof JSONObject)
            {
                JSONObject object = (JSONObject) parsed;
                sources = object.getJSONArray("sources");
                if (sources == null) sources = object.getJSONArray("data");
            }
            if (sources == null) return false;

            for (int i = 0; i < sources.size(); i++)
            {
                JSONObject source = sources.getJSONObject(i);
                if (source != null && "built_in_database".equals(source.getString("type"))
                        && !Boolean.FALSE.equals(source.getBoolean("enable")))
                {
                    return true;
                }
            }
            return false;
        }
        catch (Exception e)
        {
            log.warn("EMQX 精确授权源检查异常，原因: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 按账号踢掉旧 MQTT 连接。口令轮换后立即执行，避免旧会话继续发布。
     */
    public boolean disconnectClientsByUsername(String username)
    {
        if (!isApiConfigured() || username == null || username.trim().isEmpty()) return false;
        String baseUrl = trimTrailingSlash(properties.getEmqxApiUrl());
        String listUrl = baseUrl + "/clients?username=" + urlEncode(username) + "&limit=1000";
        try
        {
            HttpResult response = sendRequest(listUrl, "GET", null);
            if (response.statusCode != 200) return false;
            JSONObject object = JSON.parseObject(response.body);
            JSONArray clients = object == null ? null : object.getJSONArray("data");
            if (clients == null || clients.isEmpty()) return true;

            boolean success = true;
            for (int i = 0; i < clients.size(); i++)
            {
                String clientId = clients.getJSONObject(i).getString("clientid");
                if (clientId == null || clientId.trim().isEmpty()) continue;
                HttpResult deleteResponse = sendRequest(baseUrl + "/clients/" + urlEncode(clientId), "DELETE", null);
                if (deleteResponse.statusCode != 200 && deleteResponse.statusCode != 204 && deleteResponse.statusCode != 404)
                {
                    success = false;
                }
            }
            return success;
        }
        catch (Exception e)
        {
            log.warn("EMQX 旧连接清理异常 username={}, 原因: {}", username, e.getMessage());
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
