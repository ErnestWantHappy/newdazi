package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.vo.SchoolClassStatsVO;
import com.ruoyi.business.mapper.SchoolStatsMapper;
import com.ruoyi.business.service.ISchoolStatsService;

/**
 * 学校班级统计Service实现
 * 
 * @author ruoyi
 * @date 2026-02-03
 */
@Service
public class SchoolStatsServiceImpl implements ISchoolStatsService
{
    @Autowired
    private SchoolStatsMapper schoolStatsMapper;

    /**
     * 查询学校班级统计列表
     */
    @Override
    public List<SchoolClassStatsVO> selectSchoolClassStats()
    {
        return schoolStatsMapper.selectSchoolClassStats();
    }

    /**
     * 查询学校汇总统计
     */
    @Override
    public List<SchoolClassStatsVO> selectSchoolSummary()
    {
        return schoolStatsMapper.selectSchoolSummary();
    }
}
