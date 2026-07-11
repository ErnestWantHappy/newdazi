package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.vo.SchoolClassStatsVO;

/**
 * 学校班级统计Service接口
 * 
 * @author ruoyi
 * @date 2026-02-03
 */
public interface ISchoolStatsService
{
    /**
     * 查询学校班级统计列表
     * 返回每个学校每个年级每个班级的学生人数
     *
     * @return 统计列表
     */
    List<SchoolClassStatsVO> selectSchoolClassStats();

    /**
     * 查询学校汇总统计
     * 返回每个学校的班级总数和学生总数
     *
     * @return 汇总列表
     */
    List<SchoolClassStatsVO> selectSchoolSummary();
}
