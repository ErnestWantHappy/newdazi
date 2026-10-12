package com.ruoyi.business.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.business.domain.PracticalAttachment;
import com.ruoyi.business.mapper.PracticalArtifactMapper;
import com.ruoyi.business.service.ResourceAccessService;
import com.ruoyi.business.service.PracticalTextCodec;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.DownloadFileNameUtils;

/** 作品附件按业务归属读取，网页源码只以 JSON 返回，原件只以附件下载。 */
@RestController
@RequestMapping("/business/practical-attachment")
@PreAuthorize("@ss.hasRole('student') or @ss.hasRole('teacher') or @ss.hasRole('admin')")
public class PracticalAttachmentController {
    @Autowired private PracticalArtifactMapper mapper;
    @Autowired private ResourceAccessService accessService;

    @GetMapping("/{id}/text")
    public AjaxResult text(@PathVariable Long id) throws Exception {
        PracticalAttachment attachment = authorized(id);
        if (!"TEXT".equals(attachment.getFileKind()) && !"HTML".equals(attachment.getFileKind()))
            throw new ServiceException("该附件不支持文本预览");
        Path path = resolve(attachment);
        if (Files.size(path) > PracticalTextCodec.MAX_BYTES) throw new ServiceException("文本文件过大，请下载查看");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fileKind", attachment.getFileKind());
        data.put("originalFileName", attachment.getOriginalFileName());
        data.put("text", PracticalTextCodec.decode(Files.readAllBytes(path)));
        return AjaxResult.success(data);
    }

    @GetMapping("/{id}/download")
    public void download(@PathVariable Long id, HttpServletResponse response) throws Exception {
        PracticalAttachment attachment = authorized(id);
        Path path = resolve(attachment);
        response.setContentType("application/octet-stream");
        response.setHeader("X-Content-Type-Options", "nosniff");
        FileUtils.setAttachmentResponseHeader(response, DownloadFileNameUtils.sanitize(attachment.getOriginalFileName(), "作品附件"));
        Files.copy(path, response.getOutputStream());
    }

    private PracticalAttachment authorized(Long id) {
        PracticalAttachment attachment = mapper.selectAttachmentById(id);
        if (attachment == null) throw new ServiceException("附件不存在");
        accessService.assertCanRead(attachment.getResourcePath());
        return attachment;
    }

    private Path resolve(PracticalAttachment attachment) throws Exception {
        String resource = attachment.getResourcePath();
        if (resource == null || !resource.startsWith("/profile/")) throw new ServiceException("附件路径无效");
        Path root = Paths.get(RuoYiConfig.getProfile()).toRealPath();
        Path path = root.resolve(resource.substring("/profile/".length())).normalize().toRealPath();
        if (!path.startsWith(root) || !Files.isRegularFile(path)) throw new ServiceException("附件路径无效");
        return path;
    }
}
