package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizLessonAssignment;

/**
 * 课程班级指派Service接口
 * 
 * @author ruoyi
 * @date 2025-08-25
 */
public interface IBizLessonAssignmentService 
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
     * 批量删除课程班级指派
     * 
     * @param assignmentIds 需要删除的课程班级指派主键集合
     * @return 结果
     */
    public int deleteBizLessonAssignmentByAssignmentIds(Long[] assignmentIds);

    /**
     * 删除课程班级指派信息
     * 
     * @param assignmentId 课程班级指派主键
     * @return 结果
     */
    public int deleteBizLessonAssignmentByAssignmentId(Long assignmentId);
}
