package com.ruoyi.business.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.service.ResearchActivityService;
import com.ruoyi.business.service.ResearchActivityService.DownloadResource;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;

/** 教研活动图片、私有课件下载和云盘访问接口。 */
@RestController
@RequestMapping("/business/research-activity")
public class ResearchActivityResourceController extends BaseController
{
    private static final String ROLE_GUARD = "(@ss.hasRole('teacher') or @ss.hasRole('researcher') or @ss.hasRole('admin'))";

    @Autowired private ResearchActivityService service;

    @PostMapping("/images")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:add') and " + ROLE_GUARD)
    @Log(title="教研活动图片", businessType=BusinessType.INSERT, isSaveRequestData=false, isSaveResponseData=false)
    public AjaxResult uploadImage(@RequestParam("file") MultipartFile file)
    {
        String fileName = service.uploadImage(file);
        return AjaxResult.success().put("fileName", fileName).put("url", fileName);
    }

    @GetMapping("/resources/{resourceId}/download")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:download') and " + ROLE_GUARD)
    public void download(@PathVariable Long resourceId, HttpServletResponse response) throws IOException
    {
        DownloadResource download = service.prepareDownload(resourceId);
        String fileName = download.getFileName() == null ? "research-resource" : download.getFileName();
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store, max-age=0");
        Files.copy(download.getPath(), response.getOutputStream());
        response.getOutputStream().flush();
        service.recordFileAccess(download);
    }

    @PostMapping("/resources/{resourceId}/access")
    @PreAuthorize("@ss.hasPermi('business:researchActivity:download') and " + ROLE_GUARD)
    public AjaxResult accessLink(@PathVariable Long resourceId)
    {
        return success(service.accessLink(resourceId));
    }
}
