package com.ruoyi.business.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;

/**
 * 电子教材图片存储。
 *
 * 页面图刻意放在 /profile 之外：/profile/** 由静态资源直接暴露、没有鉴权，
 * 教材受版权约束，必须走 controller 鉴权后再回图。
 */
@Service
public class TextbookStorageService
{
    /** 教材根目录，默认 ./textbook-data，生产指向 /data/apps/xueyeceping/shared/textbook */
    @Value("${textbook.root:./textbook-data}")
    private String root;

    public Path getRoot()
    {
        return Paths.get(root).toAbsolutePath().normalize();
    }

    /**
     * 把库里的相对路径（如 xxkj-7-0-2026/p012.jpg）解析为真实文件。
     * 任何越界、目录、缺失都当作"教材页不存在"，不泄露服务器路径。
     */
    public Path resolve(String imagePath)
    {
        if (imagePath == null || imagePath.trim().isEmpty())
        {
            throw new ServiceException("教材页不存在");
        }
        Path base = getRoot();
        Path target = base.resolve(imagePath).normalize();
        if (!target.startsWith(base) || !Files.isRegularFile(target))
        {
            throw new ServiceException("教材页不存在");
        }
        return target;
    }

    /**
     * 书架封面缩略图。导入教材时按册预生成 covers/{bookId}.jpg（宽 360），
     * 缺失时返回 null，由控制器回退到第 1 页原图，不影响老数据。
     */
    public Path resolveCover(Long bookId)
    {
        if (bookId == null)
        {
            return null;
        }
        Path base = getRoot();
        Path target = base.resolve("covers").resolve(bookId + ".jpg").normalize();
        if (!target.startsWith(base) || !Files.isRegularFile(target))
        {
            return null;
        }
        return target;
    }
}
