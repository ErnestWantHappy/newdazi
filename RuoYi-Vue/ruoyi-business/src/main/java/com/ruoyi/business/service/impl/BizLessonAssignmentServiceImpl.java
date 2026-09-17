package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.mapper.BizLessonAssignmentMapper;
import com.ruoyi.business.mapper.BizLessonMapper;
import com.ruoyi.business.mapper.LessonClassScopeMapper;
import com.ruoyi.business.domain.BizLesson;
import com.ruoyi.business.domain.BizLessonAssignment;
import com.ruoyi.business.service.IBizLessonAssignmentService;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

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
    private BizLessonMapper bizLessonMapper;

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
        BizLessonAssignment assignment = bizLessonAssignmentMapper.selectBizLessonAssignmentByAssignmentId(assignmentId);
        assertSchoolAccess(assignment == null ? null : assignment.getDeptId());
        return assignment;
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
        if (bizLessonAssignment == null)
        {
            bizLessonAssignment = new BizLessonAssignment();
        }
        if (!SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            bizLessonAssignment.setDeptId(SecurityUtils.getDeptId());
        }
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
        normalizeAndValidateAssignment(bizLessonAssignment, null);
        assertSchoolAccess(bizLessonAssignment.getDeptId());
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
        if (before == null)
        {
            return 0;
        }
        assertSchoolAccess(before.getDeptId());
        normalizeAndValidateAssignment(bizLessonAssignment, before);
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
                assertSchoolAccess(assignment.getDeptId());
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
        assertSchoolAccess(assignment == null ? null : assignment.getDeptId());
        int rows = bizLessonAssignmentMapper.deleteBizLessonAssignmentByAssignmentId(assignmentId);
        if (rows > 0 && assignment != null)
        {
            lessonClassScopeMapper.markAssignmentInactive(
                    assignment.getLessonId(), assignment.getDeptId(),
                    assignment.getEntryYear(), assignment.getClassCode());
        }
        return rows;
    }

    /**
     * 指派记录的学校必须由课程归属决定，避免通用 CRUD 接口写入跨校或无学校数据。
     */
    private void normalizeAndValidateAssignment(BizLessonAssignment assignment,
                                                 BizLessonAssignment existing)
    {
        if (assignment == null)
        {
            throw new ServiceException("课程指派参数不完整");
        }
        if (existing != null)
        {
            if (assignment.getLessonId() == null) assignment.setLessonId(existing.getLessonId());
            if (assignment.getEntryYear() == null) assignment.setEntryYear(existing.getEntryYear());
            if (assignment.getClassCode() == null) assignment.setClassCode(existing.getClassCode());
            if (assignment.getDeptId() == null) assignment.setDeptId(existing.getDeptId());
        }
        if (assignment.getLessonId() == null
                || assignment.getEntryYear() == null || assignment.getEntryYear().trim().isEmpty()
                || assignment.getClassCode() == null || assignment.getClassCode().trim().isEmpty())
        {
            throw new ServiceException("课程指派参数不完整");
        }
        BizLesson lesson = bizLessonMapper.selectBizLessonByLessonId(assignment.getLessonId());
        if (lesson == null)
        {
            throw new ServiceException("课程不存在，不能创建指派");
        }
        if (lesson.getDeptId() == null)
        {
            throw new ServiceException("课程未归属学校，不能创建指派");
        }
        if (assignment.getDeptId() != null && !lesson.getDeptId().equals(assignment.getDeptId()))
        {
            throw new ServiceException("课程与指派学校不一致");
        }
        if (existing != null && existing.getLessonId() != null
                && !existing.getLessonId().equals(assignment.getLessonId()))
        {
            BizLesson oldLesson = bizLessonMapper.selectBizLessonByLessonId(existing.getLessonId());
            if (oldLesson != null && oldLesson.getDeptId() != null
                    && !oldLesson.getDeptId().equals(lesson.getDeptId()))
            {
                throw new ServiceException("不能将指派迁移到其他学校的课程");
            }
        }
        assignment.setDeptId(lesson.getDeptId());
        assignment.setEntryYear(assignment.getEntryYear().trim());
        assignment.setClassCode(assignment.getClassCode().replace("班", "").trim());
    }

    private void assertSchoolAccess(Long resourceDeptId)
    {
        if (SecurityUtils.isAdmin(SecurityUtils.getUserId()))
        {
            return;
        }
        Long currentDeptId = SecurityUtils.getDeptId();
        if (resourceDeptId == null || currentDeptId == null || !currentDeptId.equals(resourceDeptId))
        {
            throw new ServiceException("无权访问其他学校的课程指派", HttpStatus.FORBIDDEN);
        }
    }
}
