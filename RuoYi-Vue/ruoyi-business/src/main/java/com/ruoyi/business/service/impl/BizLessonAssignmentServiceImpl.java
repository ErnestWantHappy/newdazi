package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.LessonClassScopeMapper;
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

    @Autowired
    private LessonClassScopeMapper lessonClassScopeMapper;

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
    @Transactional(rollbackFor = Exception.class)
    public int insertBizLessonAssignment(BizLessonAssignment bizLessonAssignment)
    {
        int rows = bizLessonAssignmentMapper.insertBizLessonAssignment(bizLessonAssignment);
        if (rows > 0)
        {
            lessonClassScopeMapper.upsertCurrentAssignment(bizLessonAssignment);
        }
        return rows;
    }

    /**
     * 修改课程班级指派
     * 
     * @param bizLessonAssignment 课程班级指派
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBizLessonAssignment(BizLessonAssignment bizLessonAssignment)
    {
        BizLessonAssignment before = bizLessonAssignmentMapper.selectBizLessonAssignmentByAssignmentId(
                bizLessonAssignment.getAssignmentId());
        int rows = bizLessonAssignmentMapper.updateBizLessonAssignment(bizLessonAssignment);
        if (rows > 0)
        {
            if (before != null)
            {
                lessonClassScopeMapper.markAssignmentInactive(
                        before.getLessonId(), before.getDeptId(), before.getEntryYear(), before.getClassCode());
            }
            BizLessonAssignment after = bizLessonAssignmentMapper.selectBizLessonAssignmentByAssignmentId(
                    bizLessonAssignment.getAssignmentId());
            lessonClassScopeMapper.upsertCurrentAssignment(after);
        }
        return rows;
    }

    /**
     * 批量删除课程班级指派
     * 
     * @param assignmentIds 需要删除的课程班级指派主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBizLessonAssignmentByAssignmentIds(Long[] assignmentIds)
    {
        List<BizLessonAssignment> existing = new java.util.ArrayList<>();
        for (Long assignmentId : assignmentIds)
        {
            BizLessonAssignment assignment =
                    bizLessonAssignmentMapper.selectBizLessonAssignmentByAssignmentId(assignmentId);
            if (assignment != null)
            {
                existing.add(assignment);
            }
        }
        int rows = bizLessonAssignmentMapper.deleteBizLessonAssignmentByAssignmentIds(assignmentIds);
        for (BizLessonAssignment assignment : existing)
        {
            lessonClassScopeMapper.markAssignmentInactive(
                    assignment.getLessonId(), assignment.getDeptId(),
                    assignment.getEntryYear(), assignment.getClassCode());
        }
        return rows;
    }

    /**
     * 删除课程班级指派信息
     * 
     * @param assignmentId 课程班级指派主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBizLessonAssignmentByAssignmentId(Long assignmentId)
    {
        BizLessonAssignment assignment =
                bizLessonAssignmentMapper.selectBizLessonAssignmentByAssignmentId(assignmentId);
        int rows = bizLessonAssignmentMapper.deleteBizLessonAssignmentByAssignmentId(assignmentId);
        if (rows > 0 && assignment != null)
        {
            lessonClassScopeMapper.markAssignmentInactive(
                    assignment.getLessonId(), assignment.getDeptId(),
                    assignment.getEntryYear(), assignment.getClassCode());
        }
        return rows;
    }
}
