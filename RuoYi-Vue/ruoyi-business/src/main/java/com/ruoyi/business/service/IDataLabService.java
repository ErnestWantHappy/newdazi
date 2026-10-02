package com.ruoyi.business.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.DataLabConfig;
import com.ruoyi.business.domain.DataLabSubmission;

/**
 * 数据分析实验题业务接口
 */
public interface IDataLabService {
    /** 获取实验题配置详情 */
    DataLabConfig getConfig(Long questionId);

    /** 保存或更新实验题配置 */
    int saveConfig(DataLabConfig config, Long userId, String username);

    /** 教师上传 Excel 模板文件 */
    Map<String, Object> uploadExcel(MultipartFile file, Long userId);

    /** 学生端获取实验题配置 */
    DataLabConfig getStudentConfig(Long studentId, Long questionId, Long versionId);

    /** 下载实验题绑定的 Excel 文件流 */
    void downloadExcel(Long questionId, Long versionId, HttpServletResponse response) throws IOException;

    /** 学生提交实验代码与图表 */
    DataLabSubmission submit(Long studentId, Long questionId, String sourceType, Long sourceId,
                             String sourceCode, String chartImageBase64, String outputText);

    /** 获取学生自己的历史提交记录 */
    DataLabSubmission getStudentSubmission(Long studentId, Long questionId, String sourceType, Long sourceId);

    /** 教师查询某题单下该题的所有学生提交 */
    List<DataLabSubmission> getSubmissions(Long questionId, String sourceType, Long sourceId);

    /** 教师查看学生提交详情 */
    DataLabSubmission getSubmissionDetail(Long submissionId);

    /** 教师评分打分 */
    int scoreSubmission(Long submissionId, Integer score, String scoreComment, Long teacherId, Long revision);
}
