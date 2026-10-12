package com.ruoyi.business.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.LoginPreviewService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;

/** 公开入口仅提供固定数量概览；内部详情接口继续执行原登录、角色和数据范围校验。 */
@RestController
@RequestMapping("/business/login-preview")
public class LoginPreviewController
{
    private final LoginPreviewService service;

    public LoginPreviewController(LoginPreviewService service) { this.service = service; }

    @Anonymous
    @GetMapping("/activities")
    public AjaxResult activities() { return AjaxResult.success(service.activities()); }

    @Anonymous
    @GetMapping("/lessons")
    public AjaxResult lessons() { return AjaxResult.success(service.lessons()); }

    @Anonymous
    @GetMapping("/focus-slides")
    public AjaxResult focusSlides() { return AjaxResult.success(service.focusSlides()); }

    @Anonymous
    @GetMapping("/activity-image")
    public void activityImage(@org.springframework.web.bind.annotation.RequestParam("topicId") Long topicId,
                              javax.servlet.http.HttpServletResponse response) throws java.io.IOException
    {
        java.nio.file.Path imagePath = service.resolveActivityImage(topicId);
        if (imagePath == null || !java.nio.file.Files.exists(imagePath))
        {
            response.sendError(javax.servlet.http.HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeImage(imagePath, response);
    }

    @Anonymous
    @GetMapping("/post-image")
    public void postImage(@org.springframework.web.bind.annotation.RequestParam("postId") Long postId,
                          @org.springframework.web.bind.annotation.RequestParam(value = "imgIndex", defaultValue = "0") Integer imgIndex,
                          javax.servlet.http.HttpServletResponse response) throws java.io.IOException
    {
        java.nio.file.Path imagePath = service.resolvePostImage(postId, imgIndex);
        if (imagePath == null || !java.nio.file.Files.exists(imagePath))
        {
            response.sendError(javax.servlet.http.HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeImage(imagePath, response);
    }

    private void writeImage(java.nio.file.Path imagePath, javax.servlet.http.HttpServletResponse response) throws java.io.IOException
    {
        String fileName = imagePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".webp")) response.setContentType("image/webp");
        else if (fileName.endsWith(".png")) response.setContentType("image/png");
        else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) response.setContentType("image/jpeg");
        else response.setContentType("application/octet-stream");

        // 展示图内容由原图唯一派生，短期浏览器缓存能明显减少同一批学生重复进入登录页的下载量。
        response.setHeader("Cache-Control", "public, max-age=86400");
        response.setHeader("ETag", "\"" + java.nio.file.Files.getLastModifiedTime(imagePath).toMillis()
                + "-" + java.nio.file.Files.size(imagePath) + "\"");
        response.setContentLengthLong(java.nio.file.Files.size(imagePath));
        java.nio.file.Files.copy(imagePath, response.getOutputStream());
        response.getOutputStream().flush();
    }
}

