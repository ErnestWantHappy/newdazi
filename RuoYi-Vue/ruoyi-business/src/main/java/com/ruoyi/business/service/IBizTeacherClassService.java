package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizTeacherClass;

/**
 * 教师-班级管理关联Service接口
 * 
 * @author zdx
 * @date 2025-12-29
 */
public interface IBizTeacherClassService 
{
    /**
     * 查询教师-班级管理关联
     * 
     * @param id 主键
     * @return 教师-班级管理关联
     */
    public BizTeacherClass selectBizTeacherClassById(Long id);

    /**
     * 查询教师-班级管理关联列表
     * 
     * @param bizTeacherClass 查询条件
     * @return 教师-班级管理关联集合
     */
    public List<BizTeacherClass> selectBizTeacherClassList(BizTeacherClass bizTeacherClass);

    /**
     * 查询当前教师管理的班级列表（带学生人数）
     * 
     * @param userId 教师用户ID
     * @param deptId 学校ID
     * @return 班级列表
     */
    public List<BizTeacherClass> selectMyClassList(Long userId, Long deptId);

    /**
     * 查询学校内所有可选班级
     * 
     * @param deptId 学校ID
     * @return 可选班级列表
     */
    public List<BizTeacherClass> selectAvailableClasses(Long deptId);

    /**
     * 新增教师-班级管理关联
     * 
     * @param bizTeacherClass 教师-班级管理关联
     * @return 结果
     */
    public int insertBizTeacherClass(BizTeacherClass bizTeacherClass);

    /**
     * 批量新增教师-班级管理关联
     * 
     * @param list 教师-班级关联列表
     * @return 结果
     */
    public int batchInsertBizTeacherClass(List<BizTeacherClass> list);

    /**
     * 修改教师-班级管理关联
     * 
     * @param bizTeacherClass 教师-班级管理关联
     * @return 结果
     */
    public int updateBizTeacherClass(BizTeacherClass bizTeacherClass);

    /**
     * 批量删除教师-班级管理关联
     * 
     * @param ids 主键数组
     * @return 结果
     */
    public int deleteBizTeacherClassByIds(Long[] ids);

    /**
     * 删除教师-班级管理关联信息
     * 
     * @param id 主键
     * @return 结果
     */
    public int deleteBizTeacherClassById(Long id);
}
