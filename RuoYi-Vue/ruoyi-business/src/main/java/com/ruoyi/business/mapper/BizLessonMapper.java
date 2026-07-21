package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.vo.LessonInfoVo; // 确保导入
import org.apache.ibatis.annotations.Param;
/**
 * 课程/作业信息Mapper接口
 * 
 * @author ruoyi
 * @date 2025-08-14
 */
public interface BizLessonMapper 
{
    /**
     * 查询课程/作业信息
     * 
     * @param lessonId 课程/作业信息主键
     * @return 课程/作业信息
     */
    public BizLesson selectBizLessonByLessonId(Long lessonId);

    /**
     * 查询课程/作业信息列表
     * 
     * @param bizLesson 课程/作业信息
     * @return 课程/作业信息集合
     */
    public List<BizLesson> selectBizLessonList(BizLesson bizLesson);

    /**
     * 新增课程/作业信息
     * 
     * @param bizLesson 课程/作业信息
     * @return 结果
     */
    public int insertBizLesson(BizLesson bizLesson);

    /**
     * 修改课程/作业信息
     * 
     * @param bizLesson 课程/作业信息
     * @return 结果
     */
    public int updateBizLesson(BizLesson bizLesson);

    /**
     * 删除课程/作业信息
     * 
     * @param lessonId 课程/作业信息主键
     * @return 结果
     */
    public int deleteBizLessonByLessonId(Long lessonId);

    /**
     * 批量删除课程/作业信息
     * 
     * @param lessonIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizLessonByLessonIds(Long[] lessonIds);

    /**
     * 根据年级和创建者查询简化课程信息
     * @param grade 年级
     * @param creatorName 创建者用户名
     * @return 简化课程信息列表
     */
    List<LessonInfoVo> selectLessonsByGradeAndCreator(@Param("grade") Long grade, @Param("creatorName") String creatorName, @Param("deptId") Long deptId);

    List<LessonInfoVo> selectScoreLessons(@Param("grade") Long grade,
                                          @Param("entryYear") String entryYear,
                                          @Param("creatorName") String creatorName,
                                          @Param("userId") Long userId,
                                          @Param("deptId") Long deptId);

    /**
     * 查询共享给指定教师的课程（指派给其管理班级的、非其创建的课程）
     * @param grade 年级
     * @param userId 教师用户ID
     * @param deptId 学校ID
     * @param creatorName 创建者用户名（用于排除自己创建的）
     * @return 共享课程列表
     */
    List<LessonInfoVo> selectSharedLessonsByGradeAndUser(
            @Param("grade") Long grade, 
            @Param("userId") Long userId, 
            @Param("deptId") Long deptId, 
            @Param("creatorName") String creatorName);

    /**
     * 查询当前教师某年级已创建课程的最大课次。
     */
    Integer selectMaxLessonNumByGradeAndCreator(@Param("grade") Long grade,
                                                @Param("creatorName") String creatorName,
                                                @Param("deptId") Long deptId);

    /** 清除自动推进达标时间 */
    int clearAutoAdvanceReadyTime(@Param("lessonId") Long lessonId);

    /** 扫描开启自动推进且当前仍有班级指派的测评课 */
    List<BizLesson> selectAutoAdvanceCandidates();

    /**
     * 读取教师在本校的统一推进策略（任取一门常规课的配置；全校统一口径）
     */
    BizLesson selectAdvancePolicyByCreator(@Param("creatorName") String creatorName, @Param("deptId") Long deptId);

    /** 读取教师在本校持久化的统一推进策略 */
    BizLesson selectAdvancePolicyByTeacher(@Param("teacherId") Long teacherId, @Param("deptId") Long deptId);

    /** 新增或更新教师在本校的统一推进策略 */
    int upsertAdvancePolicy(@Param("teacherId") Long teacherId,
                            @Param("deptId") Long deptId,
                            @Param("autoAdvanceEnabled") Boolean autoAdvanceEnabled,
                            @Param("autoAdvanceThresholdPct") Integer autoAdvanceThresholdPct,
                            @Param("autoAdvanceDelayHours") java.math.BigDecimal autoAdvanceDelayHours,
                            @Param("updateBy") String updateBy);

    /**
     * 将统一推进策略写回该教师本校全部常规课
     */
    int updateAdvancePolicyByCreator(@Param("teacherId") Long teacherId,
                                     @Param("creatorName") String creatorName,
                                     @Param("deptId") Long deptId,
                                     @Param("autoAdvanceEnabled") Boolean autoAdvanceEnabled,
                                     @Param("autoAdvanceThresholdPct") Integer autoAdvanceThresholdPct,
                                     @Param("autoAdvanceDelayHours") java.math.BigDecimal autoAdvanceDelayHours,
                                     @Param("updateBy") String updateBy);
}
