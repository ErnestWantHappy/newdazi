package com.ruoyi.business.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.service.CryptPadDocumentService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.file.FileUtils;

/** CryptPad 浏览器端文档下载和 onSave 回传，所有业务权限仍由平台登录态校验。 */
@RestController
public class CryptPadCollaborationController
{
    @Autowired private CryptPadDocumentService documentService;

    @GetMapping("/business/collaboration/room/{roomId}/document")
    public void document(@PathVariable Long roomId, javax.servlet.http.HttpServletResponse response) throws Exception
    {
        Path file = documentService.currentFile(roomId);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        FileUtils.setAttachmentResponseHeader(response, documentService.currentFileName(roomId));
        response.setContentLengthLong(Files.size(file));
        Files.copy(file, response.getOutputStream());
    }

    @PostMapping(value = "/business/collaboration/room/{roomId}/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult save(@PathVariable Long roomId,
                           @RequestPart("file") MultipartFile file,
                           @RequestParam("expectedVersion") Integer expectedVersion) throws Exception
    {
        return AjaxResult.success(documentService.save(roomId, file, expectedVersion));
    }

    @PostMapping("/business/collaboration/room/{roomId}/rotate-key")
    @org.springframework.security.access.prepost.PreAuthorize("@ss.hasAnyRoles('admin,teacher')")
    public AjaxResult rotateKey(@PathVariable Long roomId)
    {
        documentService.rotateKey(roomId);
        return AjaxResult.success("协作密钥已轮换");
    }
}
