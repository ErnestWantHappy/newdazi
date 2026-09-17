package com.ruoyi.business.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.PurgeMapper;
import com.ruoyi.common.config.RuoYiConfig;

/**
 * 彻底清除的文件收尾：只删“无任何存活引用”的独占文件。
 * 数据库删除先提交（purge 事务），文件在 afterCommit 后清理；
 * 中途崩溃只会残留孤儿文件（可重复清理），不会产生断裂引用。
 */
@Service
public class PurgeFileService
{
    private static final Logger log = LoggerFactory.getLogger(PurgeFileService.class);

    @Autowired
    private PurgeMapper purgeMapper;

    /**
     * 清理候选路径，返回实际删除数。IO 失败抛异常由调用方决定是否告警。
     */
    public int deleteUnreferenced(List<String> candidates)
    {
        Set<String> normalized = new LinkedHashSet<>();
        if (candidates != null)
        {
            for (String raw : candidates)
            {
                String key = normalize(raw);
                if (key != null)
                {
                    normalized.add(key);
                }
            }
        }
        int deleted = 0;
        int kept = 0;
        for (String key : normalized)
        {
            if (purgeMapper.countPathReferences(key) > 0)
            {
                kept++;
                continue;
            }
            Path target = resolveSecure(key);
            if (target == null)
            {
                continue;
            }
            try
            {
                if (Files.deleteIfExists(target))
                {
                    deleted++;
                }
            }
            catch (Exception e)
            {
                log.warn("彻底清除残留文件待人工处理: {}", target, e);
            }
        }
        log.info("彻底清除文件收尾：删除 {} 个，保留被引用 {} 个", deleted, kept);
        return deleted;
    }

    /** 仅接受 /profile/... 形态的库内路径，其余一律忽略。 */
    static String normalize(String raw)
    {
        if (raw == null)
        {
            return null;
        }
        String value = raw.trim().replace('\\', '/');
        if (value.isEmpty())
        {
            return null;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        int index = lower.indexOf("/profile/");
        if (index < 0)
        {
            if (lower.startsWith("profile/"))
            {
                value = "/" + value;
            }
            else
            {
                return null;
            }
        }
        else if (index > 0)
        {
            value = value.substring(index);
        }
        return value;
    }

    /** 钳制在上传根内，防止路径越界（/profile/ 前缀对应上传根本身，需剥离）。 */
    static Path resolveSecure(String normalized)
    {
        try
        {
            Path root = Paths.get(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
            String relative = normalized.substring("/profile/".length());
            Path target = root.resolve(relative.replace('/', java.io.File.separatorChar)).normalize();
            if (!target.startsWith(root))
            {
                log.warn("彻底清除拒绝越界路径: {}", normalized);
                return null;
            }
            return target;
        }
        catch (Exception e)
        {
            log.warn("彻底清除路径解析失败: {}", normalized, e);
            return null;
        }
    }

    /** 供单测：不碰数据库的纯归一化。 */
    List<String> collectForTest(List<String> raws)
    {
        Set<String> out = new LinkedHashSet<>();
        for (String raw : raws)
        {
            String key = normalize(raw);
            if (key != null)
            {
                out.add(key);
            }
        }
        return new ArrayList<>(out);
    }
}
