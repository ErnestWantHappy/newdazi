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
    List<String> selectClassCodesByLessonId(Long lessonId);

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
                                        @Param("currentLessonId") Long currentLessonId);

    /**
     * 根据学生的入学年份和班级查询当前被指派的课程ID
     * @param entryYear 入学年份
     * @param classCode 班级编号
     * @return 课程ID
     */
    Long selectCurrentLessonByClass(@Param("entryYear") String entryYear, 
                                     @Param("classCode") String classCode);
}
