package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.DataLabConfig;
import com.ruoyi.business.domain.DataLabSubmission;

/**
 * 数据分析实验题 Mapper 接口
 */
public interface DataLabMapper {
    int countVersionQuestion(@Param("versionId") Long versionId, @Param("questionId") Long questionId);
    DataLabSubmission selectSubmissionByChart(@Param("resource") String resource);
    DataLabConfig selectSnapshot(@Param("versionId") Long versionId, @Param("questionId") Long questionId);
    int insertSnapshot(@Param("snapshotId") Long snapshotId, @Param("config") DataLabConfig config);
    int cloneSnapshots(@Param("fromVersionId") Long fromVersionId, @Param("toVersionId") Long toVersionId);
    int insertUpload(@Param("url") String url, @Param("userId") Long userId);
    int countOwnedUpload(@Param("url") String url, @Param("userId") Long userId);
    int deletePlanSubmissions(@Param("planId") Long planId);
    int deletePlanSnapshots(@Param("planId") Long planId);
    List<java.util.Map<String, Object>> selectStudentProgress(@Param("userId") Long userId);
    /** 查询实验配置 */
    DataLabConfig selectConfigByQuestionId(Long questionId);

    /** 新增或覆盖配置 */
    int upsertConfig(DataLabConfig config);

    /** 查询学生单个实验提交 */
    DataLabSubmission selectSubmission(@Param("studentId") Long studentId,
                                      @Param("questionId") Long questionId,
                                      @Param("sourceType") String sourceType,
                                      @Param("sourceId") Long sourceId);

    /** 根据ID查询提交详情 */
    DataLabSubmission selectSubmissionById(Long submissionId);

    /** 查询某题单/来源下的所有学生实验提交列表 */
    List<DataLabSubmission> selectSubmissionsByQuestionAndSource(@Param("questionId") Long questionId,
                                                               @Param("sourceType") String sourceType,
                                                               @Param("sourceId") Long sourceId,
                                                               @Param("teacherId") Long teacherId,
                                                               @Param("deptId") Long deptId,
                                                               @Param("privileged") boolean privileged);

    /** 新增或更新学生实验提交（UPSERT 语义） */
    int upsertSubmission(DataLabSubmission submission);

    /** 教师打分与评语更新 */
    int updateScore(@Param("submissionId") Long submissionId,
                    @Param("score") Integer score,
                    @Param("scoreComment") String scoreComment,
                    @Param("scoredBy") Long scoredBy, @Param("revision") Long revision);
}
