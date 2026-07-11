package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.BizSchoolMapper;
import com.ruoyi.business.domain.BizSchool;
import com.ruoyi.business.service.IBizSchoolService;

/**
 * 学校信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-06-24
 */
@Service
public class BizSchoolServiceImpl implements IBizSchoolService 
{
    @Autowired
    private BizSchoolMapper bizSchoolMapper;

    /**
     * 查询学校信息
     * 
     * @param schoolId 学校信息主键
     * @return 学校信息
     */
    @Override
    public BizSchool selectBizSchoolBySchoolId(Long schoolId)
    {
        return bizSchoolMapper.selectBizSchoolBySchoolId(schoolId);
    }

    /**
     * 查询学校信息列表
     * 
     * @param bizSchool 学校信息
     * @return 学校信息
     */
    @Override
    public List<BizSchool> selectBizSchoolList(BizSchool bizSchool)
    {
        return bizSchoolMapper.selectBizSchoolList(bizSchool);
    }

    /**
     * 新增学校信息
     * 
     * @param bizSchool 学校信息
     * @return 结果
     */
    @Override
    public int insertBizSchool(BizSchool bizSchool)
    {
        return bizSchoolMapper.insertBizSchool(bizSchool);
    }

    /**
     * 修改学校信息
     * 
     * @param bizSchool 学校信息
     * @return 结果
     */
    @Override
    public int updateBizSchool(BizSchool bizSchool)
    {
        return bizSchoolMapper.updateBizSchool(bizSchool);
    }

    /**
     * 批量删除学校信息
     * 
     * @param schoolIds 需要删除的学校信息主键
     * @return 结果
     */
    @Override
    public int deleteBizSchoolBySchoolIds(Long[] schoolIds)
    {
        return bizSchoolMapper.deleteBizSchoolBySchoolIds(schoolIds);
    }

    /**
     * 删除学校信息信息
     * 
     * @param schoolId 学校信息主键
     * @return 结果
     */
    @Override
    public int deleteBizSchoolBySchoolId(Long schoolId)
    {
        return bizSchoolMapper.deleteBizSchoolBySchoolId(schoolId);
    }
}
