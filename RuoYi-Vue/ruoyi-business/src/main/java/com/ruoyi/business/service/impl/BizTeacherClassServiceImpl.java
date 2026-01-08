package com.ruoyi.business.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.BizTeacherClassMapper;
import com.ruoyi.business.domain.BizTeacherClass;
import com.ruoyi.business.service.IBizTeacherClassService;
import com.ruoyi.common.exception.ServiceException;

/**
 * 教师-班级管理关联Service业务层处理
 * 
 * @author zdx
 * @date 2025-12-29
 */
@Service
public class BizTeacherClassServiceImpl implements IBizTeacherClassService 
{
    @Autowired
    private BizTeacherClassMapper bizTeacherClassMapper;

    /**
     * 查询教师-班级管理关联
     * 
     * @param id 主键
     * @return 教师-班级管理关联
     */
    @Override
    public BizTeacherClass selectBizTeacherClassById(Long id)
    {
        return bizTeacherClassMapper.selectBizTeacherClassById(id);
    }

    /**
     * 查询教师-班级管理关联列表
     * 
     * @param bizTeacherClass 查询条件
     * @return 教师-班级管理关联
     */
    @Override
    public List<BizTeacherClass> selectBizTeacherClassList(BizTeacherClass bizTeacherClass)
    {
        return bizTeacherClassMapper.selectBizTeacherClassList(bizTeacherClass);
    }

    /**
     * 查询当前教师管理的班级列表（带学生人数）
     * 
     * @param userId 教师用户ID
     * @return 班级列表
     */
    @Override
    public List<BizTeacherClass> selectMyClassList(Long userId, Long deptId)
    {
        return bizTeacherClassMapper.selectTeacherClassListWithCount(userId, deptId);
    }

    /**
     * 查询学校内所有可选班级
     * 
     * @param deptId 学校ID
     * @return 可选班级列表
     */
    @Override
    public List<BizTeacherClass> selectAvailableClasses(Long deptId)
    {
        return bizTeacherClassMapper.selectAvailableClasses(deptId);
    }

    /**
     * 新增教师-班级管理关联
     * 
     * @param bizTeacherClass 教师-班级管理关联
     * @return 结果
     */
    @Override
    public int insertBizTeacherClass(BizTeacherClass bizTeacherClass)
    {
        // 检查是否已存在
        int count = bizTeacherClassMapper.checkTeacherClassExists(bizTeacherClass);
        if (count > 0)
        {
            throw new ServiceException("您已管理该班级，无需重复添加");
        }
        bizTeacherClass.setCreateTime(new Date());
        return bizTeacherClassMapper.insertBizTeacherClass(bizTeacherClass);
    }

    /**
     * 批量新增教师-班级管理关联
     * 
     * @param list 教师-班级关联列表
     * @return 结果
     */
    @Override
    public int batchInsertBizTeacherClass(List<BizTeacherClass> list)
    {
        if (list == null || list.isEmpty())
        {
            return 0;
        }
        Date now = new Date();
        for (BizTeacherClass item : list)
        {
            item.setCreateTime(now);
        }
        return bizTeacherClassMapper.batchInsertBizTeacherClass(list);
    }

    /**
     * 修改教师-班级管理关联
     * 
     * @param bizTeacherClass 教师-班级管理关联
     * @return 结果
     */
    @Override
    public int updateBizTeacherClass(BizTeacherClass bizTeacherClass)
    {
        return bizTeacherClassMapper.updateBizTeacherClass(bizTeacherClass);
    }

    /**
     * 批量删除教师-班级管理关联
     * 
     * @param ids 主键数组
     * @return 结果
     */
    @Override
    public int deleteBizTeacherClassByIds(Long[] ids)
    {
        return bizTeacherClassMapper.deleteBizTeacherClassByIds(ids);
    }

    /**
     * 删除教师-班级管理关联信息
     * 
     * @param id 主键
     * @return 结果
     */
    @Override
    public int deleteBizTeacherClassById(Long id)
    {
        return bizTeacherClassMapper.deleteBizTeacherClassById(id);
    }
}
