package com.ruoyi.business.controller;

import java.nio.file.Files;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.business.service.GuideSheetUploadService;
import com.ruoyi.business.service.GuideSheetUploadService.DownloadResource;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.DownloadFileNameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导学单作品只通过具备学生归属或教师管班校验的接口读取。
 */
@RestController
@RequestMapping("/business/guide-sheet")
public class GuideSheetUploadController
{
    @Autowired
    private GuideSheetUploadService uploadService;

    @PreAuthorize("@studentSs.isStudent()")
    @GetMapping("/student/uploads/{bindingId}/{clientUploadId}")
    public void studentContent(@PathVariable Long bindingId,
                               @PathVariable String clientUploadId,
                               HttpServletResponse response) throws Exception
    {
        writeContent(response, uploadService.requireStudentDownload(bindingId, clientUploadId));
    }

    @PreAuthorize("@ss.hasPermi('business:guideSheet:dashboard')")
    @GetMapping("/uploads/{uploadId}/content")
    public void teacherContent(@PathVariable Long uploadId,
                               @RequestParam String entryYear,
                               @RequestParam String classCode,
                               HttpServletResponse response) throws Exception
    {
        writeContent(response, uploadService.requireTeacherDownload(uploadId, entryYear, classCode));
    }

    private void writeContent(HttpServletResponse response, DownloadResource resource) throws Exception
    {
        String mimeType = resource.getMimeType();
        if (mimeType == null || !mimeType.matches("[A-Za-z0-9.+-]+/[A-Za-z0-9.+-]+"))
        {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        String fileName = DownloadFileNameUtils.withBusinessPrefix(
                "电子导学单_学生作品", resource.getFileName());
        response.setContentType(mimeType);
        response.setContentLengthLong(Files.size(resource.getPath()));
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store");
        FileUtils.setInlineResponseHeader(response, fileName);
        Files.copy(resource.getPath(), response.getOutputStream());
    }
}
