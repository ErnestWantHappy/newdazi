package com.ruoyi.business.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.config.IotMqttProperties;
import com.ruoyi.business.domain.IotDevice;

/**
 * SIoT 2618 的认证适配层。SIoT 仅保存 MD5 字符串，因此平台不把明文密码写入数据库。
 * 该适配器只在服务端启用，浏览器和教师页面永远不会接触 SQLite 文件。
 */
@Service
public class IotSiotCredentialAdapter
{
    @Autowired private IotMqttProperties properties;

    public void provision(String username, String secret, String topic)
    {
        if (!properties.isSiotCredentialSyncEnabled()) return;
        if (properties.useSharedDeviceCredential())
        {
            provisionTopic(properties.getDeviceUsername(), topic);
            return;
        }
        if (isBlank(properties.getSiotDbPath())) throw new IllegalStateException("SIoT 凭据同步已开启但未配置数据库路径");
        execute((connection) -> {
            try (PreparedStatement user = connection.prepareStatement(
                    "insert into user(username,password) values(?,?) "
                    + "on conflict(username) do update set password=excluded.password");
                 PreparedStatement topicStatement = connection.prepareStatement(
                    "insert into topic(name,username,description) values(?,?,?) "
                    + "on conflict(name) do update set username=excluded.username"))
            {
                user.setString(1, username);
                user.setString(2, md5(secret));
                user.executeUpdate();
                topicStatement.setString(1, topic);
                topicStatement.setString(2, username);
                topicStatement.setString(3, "平台物联网实验设备 Topic");
                topicStatement.executeUpdate();
            }
        });
    }

    public void revoke(String username)
    {
        if (properties.useSharedDeviceCredential()) return;
        if (!properties.isSiotCredentialSyncEnabled() || isBlank(username)) return;
        execute((connection) -> {
            try (PreparedStatement user = connection.prepareStatement("update user set password=? where username=?"))
            {
                user.setString(1, md5(java.util.UUID.randomUUID().toString()));
                user.setString(2, username);
                user.executeUpdate();
            }
        });
    }

    /** 用于部署前只读核验，不返回密码或密钥。 */
    public List<String> validateConfiguration()
    {
        if (!properties.isSiotCredentialSyncEnabled()) return Collections.singletonList("凭据同步未开启");
        if (isBlank(properties.getSiotDbPath())) return Collections.singletonList("未配置 SIoT SQLite 路径");
        try
        {
            execute((connection) -> { });
            return Collections.singletonList("SIoT SQLite 可写");
        }
        catch (RuntimeException e)
        {
            return Collections.singletonList("SIoT SQLite 不可写");
        }
    }

    private void execute(SqlWork work)
    {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + properties.getSiotDbPath()))
        {
            connection.setAutoCommit(false);
            try (PreparedStatement pragma = connection.prepareStatement("pragma busy_timeout=5000")) { pragma.execute(); }
            work.run(connection);
            connection.commit();
        }
        catch (SQLException e)
        {
            throw new IllegalStateException("SIoT 认证数据同步失败", e);
        }
    }

    /**
     * SIoT 2618 的 Topic 表按用户名做授权。共享设备账号模式仅维护 Topic，
     * 不创建或修改 SIoT 用户，避免依赖未验证的密码摘要实现。
     */
    private void provisionTopic(String username, String topic)
    {
        if (isBlank(properties.getSiotDbPath())) throw new IllegalStateException("共享 SIoT 账号已启用但未配置数据库路径");
        execute((connection) -> {
            try (PreparedStatement topicStatement = connection.prepareStatement(
                    "insert into topic(name,username,description) values(?,?,?) "
                    + "on conflict(name) do update set username=excluded.username"))
            {
                topicStatement.setString(1, topic);
                topicStatement.setString(2, username);
                topicStatement.setString(3, "平台物联网实验设备 Topic");
                topicStatement.executeUpdate();
            }
        });
    }

    private String md5(String value)
    {
        try
        {
            byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(32);
            for (byte item : digest) result.append(String.format("%02x", item));
            return result.toString();
        }
        catch (Exception e) { throw new IllegalStateException("无法生成 SIoT 密码摘要", e); }
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }

    @FunctionalInterface
    private interface SqlWork { void run(Connection connection) throws SQLException; }
}
