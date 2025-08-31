package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizStudent;
/**
 * 学生管理Mapper接口
 * 
 * @author zdx
 * @date 2025-06-25
 */
public interface BizStudentMapper 
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
     * 删除学生管理
     * 
     * @param studentId 学生管理主键
     * @return 结果
     */
    public int deleteBizStudentByStudentId(Long studentId);

    /**
     * 批量删除学生管理
     * 
     * @param studentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizStudentByStudentIds(Long[] studentIds);

    /**
     * 根据学校ID查询所有不重复的入学年份和班级组合
     * @param deptId 学校ID
     * @return 学生信息列表，每个对象只包含entryYear和classCode
     */
    public List<BizStudent> selectDistinctYearAndClassByDeptId(Long deptId);
}
