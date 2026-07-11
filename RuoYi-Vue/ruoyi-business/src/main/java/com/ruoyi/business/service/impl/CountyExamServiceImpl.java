package com.ruoyi.business.service.impl;

import com.ruoyi.business.domain.CountyExam;
import com.ruoyi.business.mapper.CountyExamMapper;
import com.ruoyi.business.service.ICountyExamService;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 县考管理 Service实现类
 * 
 * @author ruoyi
 */
@Service
public class CountyExamServiceImpl implements ICountyExamService {

    @Autowired
    private CountyExamMapper countyExamMapper;

    /**
     * 查询县考
     */
    @Override
    public CountyExam selectCountyExamById(Long examId) {
        return countyExamMapper.selectCountyExamById(examId);
    }

    /**
     * 查询县考列表
     */
    @Override
    public List<CountyExam> selectCountyExamList(CountyExam countyExam) {
        return countyExamMapper.selectCountyExamList(countyExam);
    }

    /**
     * 新增县考
     */
    @Override
    public int insertCountyExam(CountyExam countyExam) {
        // 设置默认状态为草稿
        if (countyExam.getStatus() == null) {
            countyExam.setStatus("0");
        }
        // 设置创建人
        countyExam.setCreatorId(SecurityUtils.getUserId());
        countyExam.setCreateBy(SecurityUtils.getUsername());
        return countyExamMapper.insertCountyExam(countyExam);
    }

    /**
     * 修改县考
     */
    @Override
    public int updateCountyExam(CountyExam countyExam) {
        countyExam.setUpdateBy(SecurityUtils.getUsername());
        return countyExamMapper.updateCountyExam(countyExam);
    }

    /**
     * 删除县考
     */
    @Override
    public int deleteCountyExamById(Long examId) {
        return countyExamMapper.deleteCountyExamById(examId);
    }

    /**
     * 批量删除县考
     */
    @Override
    public int deleteCountyExamByIds(Long[] examIds) {
        return countyExamMapper.deleteCountyExamByIds(examIds);
    }
}
