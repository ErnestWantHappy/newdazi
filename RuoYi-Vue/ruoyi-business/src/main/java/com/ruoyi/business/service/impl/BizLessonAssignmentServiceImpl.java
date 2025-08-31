package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.service.IBizLessonAssignmentService;

/**
 * 课程班级指派Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-08-25
 */
@Service
public class BizLessonAssignmentServiceImpl implements IBizLessonAssignmentService 
{
    @Autowired
    private BizLessonAssignmentMapper bizLessonAssignmentMapper;

    /**
     * 查询课程班级指派
     * 
     * @param assignmentId 课程班级指派主键
     * @return 课程班级指派
     */
    @Override
    public BizLessonAssignment selectBizLessonAssignmentByAssignmentId(Long assignmentId)
    {
        return bizLessonAssignmentMapper.selectBizLessonAssignmentByAssignmentId(assignmentId);
    }

    /**
     * 查询课程班级指派列表
     * 
     * @param bizLessonAssignment 课程班级指派
     * @return 课程班级指派
     */
    @Override
    public List<BizLessonAssignment> selectBizLessonAssignmentList(BizLessonAssignment bizLessonAssignment)
    {
        return bizLessonAssignmentMapper.selectBizLessonAssignmentList(bizLessonAssignment);
    }

    /**
     * 新增课程班级指派
     * 
     * @param bizLessonAssignment 课程班级指派
     * @return 结果
     */
    @Override
    public int insertBizLessonAssignment(BizLessonAssignment bizLessonAssignment)
    {
        return bizLessonAssignmentMapper.insertBizLessonAssignment(bizLessonAssignment);
    }

    /**
     * 修改课程班级指派
     * 
     * @param bizLessonAssignment 课程班级指派
     * @return 结果
     */
    @Override
    public int updateBizLessonAssignment(BizLessonAssignment bizLessonAssignment)
    {
        return bizLessonAssignmentMapper.updateBizLessonAssignment(bizLessonAssignment);
    }

    /**
     * 批量删除课程班级指派
     * 
     * @param assignmentIds 需要删除的课程班级指派主键
     * @return 结果
     */
    @Override
    public int deleteBizLessonAssignmentByAssignmentIds(Long[] assignmentIds)
    {
        return bizLessonAssignmentMapper.deleteBizLessonAssignmentByAssignmentIds(assignmentIds);
    }

    /**
     * 删除课程班级指派信息
     * 
     * @param assignmentId 课程班级指派主键
     * @return 结果
     */
    @Override
    public int deleteBizLessonAssignmentByAssignmentId(Long assignmentId)
    {
        return bizLessonAssignmentMapper.deleteBizLessonAssignmentByAssignmentId(assignmentId);
    }
}
