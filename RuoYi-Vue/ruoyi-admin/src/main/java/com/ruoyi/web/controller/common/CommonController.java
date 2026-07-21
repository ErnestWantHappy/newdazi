package com.ruoyi.web.controller.common;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.business.service.ResourceAccessService;
import com.ruoyi.common.exception.ServiceException;

/**
 * 通用请求处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/common")
public class CommonController
{
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private ResourceAccessService resourceAccessService;

    private static final String FILE_DELIMETER = ",";

    /**
     * 通用下载请求
     * 
     * @param fileName 文件名称
     * @param delete 是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request)
    {
        try
        {
            if (!FileUtils.checkAllowDownload(fileName))
            {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = RuoYiConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(filePath);
            }
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）
     */
    @PostMapping("/upload")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", fileName);
            ajax.put("newFileName", FileUtils.getName(fileName));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 通用上传请求（多个）
     */
    @PostMapping("/uploads")
    @PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
    public AjaxResult uploadFiles(List<MultipartFile> files) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            List<String> urls = new ArrayList<String>();
            List<String> fileNames = new ArrayList<String>();
            List<String> newFileNames = new ArrayList<String>();
            List<String> originalFilenames = new ArrayList<String>();
            for (MultipartFile file : files)
            {
                // 上传并返回新文件名称
                String fileName = FileUploadUtils.upload(filePath, file);
                String url = serverConfig.getUrl() + fileName;
                urls.add(url);
                fileNames.add(fileName);
                newFileNames.add(FileUtils.getName(fileName));
                originalFilenames.add(file.getOriginalFilename());
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", StringUtils.join(urls, FILE_DELIMETER));
            ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMETER));
            ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMETER));
            ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMETER));
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        try
        {
            String authorizedResource = resourceAccessService.assertCanRead(resource);
            if (!FileUtils.checkAllowDownload(resource))
            {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            // 本地资源路径
            Path downloadPath = resolveProfileResource(authorizedResource);
            // 下载名称
            String downloadName = downloadPath.getFileName().toString();
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath.toString(), response.getOutputStream());
        }
        catch (ServiceException e)
        {
            log.warn("下载文件被拒绝: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
            if (!response.isCommitted())
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在或不可读取");
            }
        }
    }

    /**
     * 本地资源通用预览
     */
    @GetMapping("/resource/view")
    public void resourceView(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        try
        {
            String authorizedResource = resourceAccessService.assertCanRead(resource);
            if (!FileUtils.checkAllowDownload(resource))
            {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许预览。 ", resource));
            }
            // 本地资源路径
            Path downloadPath = resolveProfileResource(authorizedResource);
            
            // 调试日志
            log.info("【预览】请求资源: {} -> 实际路径: {}", resource, downloadPath);
            
            java.io.File file = downloadPath.toFile();
            if (!file.exists()) {
                log.error("【预览】文件不存在: {}", file.getAbsolutePath());
                response.sendError(404, "文件不存在");
                return;
            }
            
            // 设置Content-Type
            String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            String lowerRes = resource.toLowerCase();
            if (lowerRes.endsWith(".pdf")) {
                contentType = MediaType.APPLICATION_PDF_VALUE;
            } else if (lowerRes.endsWith(".jpg") || lowerRes.endsWith(".jpeg")) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            } else if (lowerRes.endsWith(".png")) {
                contentType = MediaType.IMAGE_PNG_VALUE;
            }
            
            response.setContentType(contentType);
            // P6 Fix: 使用percentEncode编码文件名以支持中文
            String fileName = FileUtils.getName(resource);
            String encodedFileName = FileUtils.percentEncode(fileName);
            // inline 表示在浏览器中打开，而不是下载
            response.setHeader("Content-Disposition", "inline; filename=" + encodedFileName + "; filename*=utf-8''" + encodedFileName);
            FileUtils.writeBytes(downloadPath.toString(), response.getOutputStream());
        }
        catch (ServiceException e)
        {
            log.warn("预览文件被拒绝: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
        catch (Exception e)
        {
            log.error("预览文件失败", e);
            if (!response.isCommitted())
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在或不可读取");
            }
        }
    }

    boolean isPrivateGuideSheetResource(String resource)
    {
        if (StringUtils.isEmpty(resource))
        {
            return false;
        }
        Set<String> candidates = new LinkedHashSet<>();
        String candidate = resource;
        for (int i = 0; i < 3 && candidates.add(candidate); i++)
        {
            try
            {
                candidate = URLDecoder.decode(candidate, StandardCharsets.UTF_8.name());
            }
            catch (Exception e)
            {
                break;
            }
        }
        Path privateRoot = Paths.get(RuoYiConfig.getProfile(), "upload", "guide-sheet")
                .toAbsolutePath().normalize();
        for (String value : candidates)
        {
            try
            {
                if (isSameOrChild(resolveProfileResource(value), privateRoot))
                {
                    // 历史作品只能由带学生归属或教师管班校验的专用接口读取。
                    return true;
                }
            }
            catch (IllegalArgumentException ignored)
            {
                // 非资源路径继续交给通用下载白名单拒绝，不在此处扩大可访问范围。
            }
        }
        return false;
    }

    private Path resolveProfileResource(String resource)
    {
        if (StringUtils.isEmpty(resource))
        {
            throw new IllegalArgumentException("资源路径不能为空");
        }
        String normalized = resource.replace('\\', '/');
        String searchable = normalized.startsWith("/") ? normalized : "/" + normalized;
        String lower = searchable.toLowerCase(Locale.ROOT);
        int searchFrom = 0;
        int prefixIndex = -1;
        while ((prefixIndex = lower.indexOf("/profile", searchFrom)) >= 0)
        {
            int prefixEnd = prefixIndex + "/profile".length();
            if (prefixEnd == lower.length() || lower.charAt(prefixEnd) == '/')
            {
                String relative = searchable.substring(prefixEnd).replaceFirst("^/+", "");
                Path profileRoot = Paths.get(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
                Path target = profileRoot.resolve(relative).toAbsolutePath().normalize();
                if (!isSameOrChild(target, profileRoot))
                {
                    throw new IllegalArgumentException("资源路径超出文件根目录");
                }
                return target;
            }
            searchFrom = prefixEnd;
        }
        throw new IllegalArgumentException("资源路径缺少 profile 前缀");
    }

    private boolean isSameOrChild(Path path, Path root)
    {
        String pathValue = path.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
        String rootValue = root.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
        return pathValue.equals(rootValue) || pathValue.startsWith(rootValue + "/");
    }
}
