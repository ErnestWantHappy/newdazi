package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizStudent;

/**
 * 学生管理Service接口
 * 
 * @author zdx
 * @date 2025-06-25
 */
public interface IBizStudentService 
{
    /**
     * 查询学生管理
     * 
     * @param studentId 学生管理主键
     * @return 学生管理
     */
    public BizStudent selectBizStudentByStudentId(Long studentId);

    /**
     * 查询学生管理列表
     * 
     * @param bizStudent 学生管理
     * @return 学生管理集合
     */
    public List<BizStudent> selectBizStudentList(BizStudent bizStudent);

    /**
     * 新增学生管理
     * 
     * @param bizStudent 学生管理
     * @return 结果
     */
    public int insertBizStudent(BizStudent bizStudent);

    /**
     * 修改学生管理
     * 
     * @param bizStudent 学生管理
     * @return 结果
     */
    public int updateBizStudent(BizStudent bizStudent);

    /**
     * 批量删除学生管理
     * 
     * @param studentIds 需要删除的学生管理主键集合
     * @return 结果
     */
    public int deleteBizStudentByStudentIds(Long[] studentIds);

    /**
     * 删除学生管理信息
     * 
     * @param studentId 学生管理主键
     * @return 结果
     */
    public int deleteBizStudentByStudentId(Long studentId);

    /**
     * 导入学生数据
     * * @param studentList 学生数据列表
     * @param operName 操作用户
     * @return 结果
     */
    public String importStudent(List<BizStudent> studentList, String operName);

    /**
     * 批量重置学生密码
     *
     * @param userIds 需要重置密码的用户ID数组
     * @return 结果
     */
    public int resetStudentPwd(Long[] userIds);
}
