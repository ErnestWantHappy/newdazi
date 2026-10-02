package com.ruoyi.business.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.DataLabConfig;
import com.ruoyi.business.domain.DataLabSubmission;
import com.ruoyi.business.mapper.PythonPracticeMapper;
import com.ruoyi.business.mapper.DataLabMapper;
import com.ruoyi.business.service.IDataLabService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.business.service.DataLabAccessService;
import com.ruoyi.common.utils.file.FileUtils;

/**
 * 数据分析实验题业务实现
 */
@Service
@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
public class DataLabServiceImpl implements IDataLabService {
    private static final Logger log = LoggerFactory.getLogger(DataLabServiceImpl.class);

    @Autowired
    private DataLabMapper dataLabMapper;
    @Autowired private DataLabAccessService access;

    @Autowired private PythonPracticeMapper practiceMapper;

    @Override
    public DataLabConfig getConfig(Long questionId) {
        access.question(questionId, false);
        if (questionId == null) {
            return null;
        }
        return dataLabMapper.selectConfigByQuestionId(questionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveConfig(DataLabConfig config, Long userId, String username) {
        if (config == null || config.getQuestionId() == null) {
            throw new ServiceException("题目ID不能为空");
        }
        access.question(config.getQuestionId(), true);
        if (config.getExperimentName() == null || config.getExperimentName().trim().isEmpty() || config.getExperimentName().length() > 200
                || (config.getDescription() != null && config.getDescription().length() > 10000)
                || (config.getStarterCode() != null && config.getStarterCode().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 60000)) {
            throw new ServiceException("实验名称、说明或初始代码长度无效");
        }
        if (dataLabMapper.countOwnedUpload(config.getExcelFileUrl(), SecurityUtils.getUserId()) != 1) {
            throw new ServiceException("只能绑定本人上传的数据文件");
        }
        validateFileName(config.getExcelFileName());
        File dataset = resolveExcelFile(config.getExcelFileUrl());
        if (!dataset.isFile()) throw new ServiceException("数据文件不存在");
        config.setExcelFileSize(dataset.length());
        if (config.getMaxScore() == null || config.getMaxScore() < 1 || config.getMaxScore() > 100) {
            throw new ServiceException("满分必须为1到100");
        }
        Date now = new Date();
        config.setUpdateTime(now);
        config.setUpdateBy(SecurityUtils.getUsername());
        if (config.getCreateTime() == null) {
            config.setCreateTime(now);
            config.setCreateBy(SecurityUtils.getUsername());
        }

        // 保存或更新实验题配置
        return dataLabMapper.upsertConfig(config);
    }

    @Override
    public Map<String, Object> uploadExcel(MultipartFile file, Long userId) {
        if (!SecurityUtils.hasPermi("business:pythonPractice:edit")) throw new ServiceException("无权上传实验文件", 403);
        userId = SecurityUtils.getUserId();
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        validateFileName(originalFilename);
        if (StringUtils.isEmpty(originalFilename)) {
            throw new ServiceException("文件名无效");
        }
        String ext = com.ruoyi.common.utils.file.FileUploadUtils.getExtension(file);
        if (!"xlsx".equalsIgnoreCase(ext)) {
            throw new ServiceException("仅支持上传 .xlsx 格式的 Excel 数据表格");
        }
        // 限制文件最大 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ServiceException("文件大小不能超过 10MB");
        }
        // 扩展名和客户端 MIME 均不可作为内容证明，使用现有 POI 解析真实工作簿。
        try (java.io.InputStream input = file.getInputStream();
             org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(input)) {
            if (!(workbook instanceof org.apache.poi.xssf.usermodel.XSSFWorkbook)) {
                throw new ServiceException("文件内容不是 XLSX 工作簿");
            }
        } catch (IOException | RuntimeException e) {
            throw new ServiceException("Excel 文件损坏或格式不受支持");
        }

        try {
            String datePath = DateUtils.datePath();
            String relativeDir = "/datalab/excel/" + datePath;
            String baseDir = RuoYiConfig.getProfile() + relativeDir;
            File dir = new File(baseDir).getAbsoluteFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String savedFileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            File targetFile = new File(dir, savedFileName);
            file.transferTo(targetFile);

            String accessUrl = Constants.RESOURCE_PREFIX + relativeDir + "/" + savedFileName;
            dataLabMapper.insertUpload(accessUrl, userId);
            Map<String, Object> result = new HashMap<>();
            result.put("url", accessUrl);
            result.put("fileName", originalFilename);
            result.put("fileSize", file.getSize());
            return result;
        } catch (IOException e) {
            log.error("保存上传的 Excel 文件失败", e);
            throw new ServiceException("保存 Excel 文件失败: " + e.getMessage());
        }
    }

    @Override
    public DataLabConfig getStudentConfig(Long studentId, Long questionId, Long versionId) {
        access.student(studentId, versionId, questionId);
        DataLabConfig config = dataLabMapper.selectSnapshot(versionId, questionId);
        if (config == null) throw new ServiceException("实验快照不存在");
        return config;
    }

    @Override
    public void downloadExcel(Long questionId, Long versionId, HttpServletResponse response) throws IOException {
        DataLabConfig config = getStudentConfig(SecurityUtils.getUserId(), questionId, versionId);
        if (config == null || StringUtils.isEmpty(config.getExcelFileUrl())) {
            throw new ServiceException("该题目未配置 Excel 数据文件");
        }

        File localFile = resolveExcelFile(config.getExcelFileUrl());
        if (!localFile.exists() || !localFile.isFile()) {
            throw new ServiceException("服务器上的 Excel 数据文件不存在");
        }

        String downloadName = StringUtils.isNotEmpty(config.getExcelFileName())
                ? config.getExcelFileName()
                : localFile.getName();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        FileUtils.setAttachmentResponseHeader(response, downloadName);

        try (FileInputStream in = new FileInputStream(localFile)) {
            IOUtils.copy(in, response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataLabSubmission submit(Long studentId, Long questionId, String sourceType, Long sourceId,
                                   String sourceCode, String chartImageBase64, String outputText) {
        requireSource(sourceType, sourceId);
        // 与发布、编辑使用相同主行锁，防止校验后题单版本被切换。
        if (practiceMapper.lockPlanByVersion(sourceId) == null) throw new ServiceException("题单版本不存在");
        DataLabConfig snapshot = getStudentConfig(studentId, questionId, sourceId);
        if (chartImageBase64 != null && chartImageBase64.length() > 8 * 1024 * 1024 + 22) throw new ServiceException("图表不能超过6MB");
        if (sourceCode == null || sourceCode.trim().isEmpty() || sourceCode.length() > 100000) throw new ServiceException("实验代码不能为空或超过100000字符");
        if (studentId == null || questionId == null) {
            throw new ServiceException("学生与题目参数不完整");
        }

        String chartImageUrl = null;
        if (StringUtils.isNotEmpty(chartImageBase64)) {
            // 保存图表图片
            try {
                String base64Data = chartImageBase64;
                if (base64Data.contains(",")) {
                    base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                }
                byte[] imageBytes = decodePng(base64Data);

                String datePath = DateUtils.datePath();
                String relativeDir = "/datalab/charts/" + datePath;
                File dir = new File(RuoYiConfig.getProfile() + relativeDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String imageName = UUID.randomUUID().toString().replace("-", "") + ".png";
                File targetImage = new File(dir, imageName);
                try (FileOutputStream out = new FileOutputStream(targetImage)) {
                    out.write(imageBytes);
                    out.flush();
                }
                if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
                    org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                        new org.springframework.transaction.support.TransactionSynchronizationAdapter() {
                            @Override public void afterCompletion(int status) {
                                if (status != STATUS_COMMITTED && !targetImage.delete()) log.warn("实验事务回滚后的图表清理失败");
                            }
                        });
                }
                chartImageUrl = Constants.RESOURCE_PREFIX + relativeDir + "/" + imageName;
            } catch (Exception e) {
                throw new ServiceException("图表无效或保存失败");
            }
        }

        // 截断文本防超长
        String safeOutput = outputText;
        if (safeOutput != null && safeOutput.length() > 65000) {
            safeOutput = safeOutput.substring(0, 65000) + "\n... (输出已截断)";
        }

        DataLabSubmission submission = new DataLabSubmission();
        submission.setStudentId(studentId);
        submission.setQuestionId(questionId);
        submission.setSourceType(StringUtils.isNotEmpty(sourceType) ? sourceType : "PLAN");
        submission.setSourceId(sourceId);
        submission.setSourceCode(sourceCode);
        submission.setChartImageUrl(chartImageUrl);
        submission.setOutputText(safeOutput);
        submission.setSubmittedAt(new Date());
        submission.setMaxScore(snapshot.getMaxScore());

        dataLabMapper.upsertSubmission(submission);
        return dataLabMapper.selectSubmission(studentId, questionId, submission.getSourceType(), sourceId);
    }

    @Override
    public DataLabSubmission getStudentSubmission(Long studentId, Long questionId, String sourceType, Long sourceId) {
        requireSource(sourceType, sourceId);
        access.student(studentId, sourceId, questionId);
        if (studentId == null || questionId == null) {
            return null;
        }
        return dataLabMapper.selectSubmission(studentId, questionId,
                StringUtils.isNotEmpty(sourceType) ? sourceType : "PLAN", sourceId);
    }

    @Override
    public List<DataLabSubmission> getSubmissions(Long questionId, String sourceType, Long sourceId) {
        requireSource(sourceType, sourceId);
        access.teacher(sourceId, questionId);
        if (questionId == null) {
            return null;
        }
        return dataLabMapper.selectSubmissionsByQuestionAndSource(questionId, sourceType, sourceId,
            SecurityUtils.getUserId(), SecurityUtils.getDeptId(),
            SecurityUtils.isAdmin(SecurityUtils.getUserId()) || SecurityUtils.hasRole("researcher"));
    }

    @Override
    public DataLabSubmission getSubmissionDetail(Long submissionId) {
        if (submissionId == null) {
            return null;
        }
        DataLabSubmission row = dataLabMapper.selectSubmissionById(submissionId);
        access.submission(row, true);
        return row;
    }

    @Override
    public int scoreSubmission(Long submissionId, Integer score, String scoreComment, Long teacherId, Long revision) {
        if (!SecurityUtils.hasPermi("business:pythonPractice:edit")) throw new ServiceException("无权评分", 403);
        if (submissionId == null) {
            throw new ServiceException("提交记录ID不能为空");
        }
        if (score == null || score < 0 || score > 100 || (scoreComment != null && scoreComment.length() > 500)) {
            throw new ServiceException("分数或评语长度无效");
        }
        DataLabSubmission submission = dataLabMapper.selectSubmissionById(submissionId);
        access.submission(submission, true);
        if (submission.getMaxScore() == null || score > submission.getMaxScore()) throw new ServiceException("分数超过满分");
        if (revision == null || dataLabMapper.updateScore(submissionId, score, scoreComment, SecurityUtils.getUserId(), revision) != 1) {
            throw new ServiceException("学生已重新提交，请刷新作品后再评分");
        }
        return 1;
    }

    static void requireSource(String sourceType, Long sourceId) {
        if (!"BASE_VERSION".equals(sourceType) || sourceId == null || sourceId <= 0) {
            throw new ServiceException("必须指定有效题单版本");
        }
    }

    static void validateFileName(String name) {
        if (name == null || name.isEmpty() || name.length() > 200 || name.matches(".*[\\\\/:\\x00-\\x1f].*") || name.contains("..")) {
            throw new ServiceException("数据文件名无效");
        }
    }

    static File resolveExcelFile(String url) {
        if (url == null || !url.matches("/profile/datalab/excel/[A-Za-z0-9/_-]+\\.xlsx")) {
            throw new ServiceException("数据文件路径无效");
        }
        try {
            File root = new File(RuoYiConfig.getProfile(), "datalab/excel").getCanonicalFile();
            File file = new File(RuoYiConfig.getProfile(), url.substring("/profile/".length())).getCanonicalFile();
            if (!file.toPath().startsWith(root.toPath())) throw new ServiceException("数据文件越界");
            return file;
        } catch (IOException e) { throw new ServiceException("数据文件路径无效"); }
    }

    static byte[] decodePng(String data) throws IOException {
        if (data.length() > 8 * 1024 * 1024) throw new ServiceException("图表不能超过6MB");
        byte[] bytes = Base64.getDecoder().decode(data);
        byte[] signature = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
        if (bytes.length < 24 || !java.util.Arrays.equals(signature, java.util.Arrays.copyOf(bytes, 8))) throw new ServiceException("图表必须为PNG");
        try (javax.imageio.stream.ImageInputStream input = javax.imageio.ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(bytes))) {
            java.util.Iterator<javax.imageio.ImageReader> readers = javax.imageio.ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new ServiceException("PNG图表无效");
            javax.imageio.ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 4096 || height > 4096 || (long)width * height > 16000000) throw new ServiceException("图表尺寸过大");
                if (reader.read(0) == null) throw new ServiceException("PNG图表损坏");
            } finally { reader.dispose(); }
        }
        return bytes;
    }
}
