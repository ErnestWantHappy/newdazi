package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizLessonAssignment;
import org.apache.ibatis.annotations.Param;

/**
 * 课程班级指派Mapper接口
 * 
 * @author ruoyi
 * @date 2025-08-25
 */
public interface BizLessonAssignmentMapper 
{
    /**
     * 查询课程班级指派
     * 
     * @param assignmentId 课程班级指派主键
     * @return 课程班级指派
     */
    public BizLessonAssignment selectBizLessonAssignmentByAssignmentId(Long assignmentId);

    /**
     * 查询课程班级指派列表
     * 
     * @param bizLessonAssignment 课程班级指派
     * @return 课程班级指派集合
     */
    public List<BizLessonAssignment> selectBizLessonAssignmentList(BizLessonAssignment bizLessonAssignment);

    /**
     * 新增课程班级指派
     * 
     * @param bizLessonAssignment 课程班级指派
     * @return 结果
     */
    public int insertBizLessonAssignment(BizLessonAssignment bizLessonAssignment);

    /**
     * 修改课程班级指派
     * 
     * @param bizLessonAssignment 课程班级指派
     * @return 结果
     */
    public int updateBizLessonAssignment(BizLessonAssignment bizLessonAssignment);

    /**
     * 删除课程班级指派
     * 
     * @param assignmentId 课程班级指派主键
     * @return 结果
     */
    public int deleteBizLessonAssignmentByAssignmentId(Long assignmentId);

    /**
     * 批量删除课程班级指派
     * 
     * @param assignmentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizLessonAssignmentByAssignmentIds(Long[] assignmentIds);

    /**
     * 根据课程ID查询所有被指派的班级编号
     * @param lessonId 课程ID
     * @return 班级编号列表
     */
    List<String> selectClassCodesByLessonIdAndEntryYear(@Param("lessonId") Long lessonId,
                                                         @Param("entryYear") String entryYear);

    /** 批量读取教师首页课程的当前指派，避免逐课程查询。 */
    List<BizLessonAssignment> selectAssignmentsByLessonIds(@Param("lessonIds") List<Long> lessonIds,
                                                            @Param("deptId") Long deptId);

    /**
     * 根据课程ID删除所有指派记录
     * @param lessonId 课程ID
     */
    void deleteByLessonId(Long lessonId);

    /**
     * 批量新增课程指派记录
     * @param assignments 指派记录列表
     */
    void batchInsert(List<BizLessonAssignment> assignments);

    /**
     * 班级互斥 - 删除其他课程中该班级的指派
     * @param entryYear 入学年份
     * @param classCode 班级编号
     * @param currentLessonId 当前课程ID（不删除）
     */
    void deleteOtherAssignmentsByClass(@Param("entryYear") String entryYear, 
                                        @Param("classCode") String classCode, 
                                        @Param("deptId") Long deptId,
                                        @Param("currentLessonId") Long currentLessonId);

    /**
     * 根据学生的入学年份和班级查询当前被指派的课程ID
     * @param entryYear 入学年份
     * @param classCode 班级编号
     * @return 课程ID
     */
    Long selectCurrentLessonByClass(@Param("entryYear") String entryYear, 
                                     @Param("classCode") String classCode,
                                     @Param("deptId") Long deptId);

    /** 查询某课程全部指派记录（含入学年/班号/学校） */
    List<BizLessonAssignment> selectAssignmentsByLessonId(@Param("lessonId") Long lessonId);

    /** 锁定班级当前指派，推进事务必须先调用 */
    BizLessonAssignment selectCurrentAssignmentForUpdate(@Param("entryYear") String entryYear,
                                                          @Param("classCode") String classCode,
                                                          @Param("deptId") Long deptId);

    /** 仅在仍指向预期课程时切到下一课，防止重复推进 */
    int advanceCurrentAssignment(@Param("assignmentId") Long assignmentId,
                                 @Param("currentLessonId") Long currentLessonId,
                                 @Param("nextLessonId") Long nextLessonId,
                                 @Param("assignerId") Long assignerId,
                                 @Param("assignTime") java.util.Date assignTime);

    /** 首次达标时写入班级独立计时点 */
    int markAutoAdvanceReady(@Param("assignmentId") Long assignmentId,
                             @Param("readyTime") java.util.Date readyTime);

    /** 未达标或策略变化时清除班级计时点 */
    int clearAssignmentReadyTime(@Param("assignmentId") Long assignmentId);

    /** 清除某教师本校全部常规课的班级计时点 */
    int clearReadyTimesByTeacher(@Param("teacherId") Long teacherId,
                                 @Param("creatorName") String creatorName,
                                 @Param("deptId") Long deptId);

    /** 记录推进前课程，用于短时补交和审计 */
    int insertAdvanceHistory(@Param("assignment") BizLessonAssignment assignment,
                             @Param("nextLessonId") Long nextLessonId,
                             @Param("advancedBy") Long advancedBy,
                             @Param("advanceSource") String advanceSource,
                             @Param("advancedTime") java.util.Date advancedTime);

    /** 判断学生所在班级是否在指定时间后从该课程推进离开 */
    int countRecentAdvanceHistory(@Param("lessonId") Long lessonId,
                                  @Param("entryYear") String entryYear,
                                  @Param("classCode") String classCode,
                                  @Param("deptId") Long deptId,
                                  @Param("advancedAfter") java.util.Date advancedAfter);

    /** 历史推进记录同样证明课程曾真实指派给该届班级。 */
    int countHistoricalAssignment(@Param("lessonId") Long lessonId,
                                  @Param("entryYear") String entryYear,
                                  @Param("classCode") String classCode,
                                  @Param("deptId") Long deptId);
}
