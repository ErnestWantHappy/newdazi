package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizSchool;

/**
 * 学校信息Service接口
 * 
 * @author ruoyi
 * @date 2025-06-24
 */
public interface IBizSchoolService 
{
    /**
     * 查询学校信息
     * 
     * @param schoolId 学校信息主键
     * @return 学校信息
     */
    public BizSchool selectBizSchoolBySchoolId(Long schoolId);

    /**
     * 查询学校信息列表
     * 
     * @param bizSchool 学校信息
     * @return 学校信息集合
     */
    public List<BizSchool> selectBizSchoolList(BizSchool bizSchool);

    /**
     * 新增学校信息
     * 
     * @param bizSchool 学校信息
     * @return 结果
     */
    public int insertBizSchool(BizSchool bizSchool);

    /**
     * 修改学校信息
     * 
     * @param bizSchool 学校信息
     * @return 结果
     */
    public int updateBizSchool(BizSchool bizSchool);

    /**
     * 批量删除学校信息
     * 
     * @param schoolIds 需要删除的学校信息主键集合
     * @return 结果
     */
    public int deleteBizSchoolBySchoolIds(Long[] schoolIds);

    /**
     * 删除学校信息信息
     * 
     * @param schoolId 学校信息主键
     * @return 结果
     */
    public int deleteBizSchoolBySchoolId(Long schoolId);
}
