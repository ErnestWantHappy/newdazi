package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.vo.SchoolScoreStatsVO;

/**
 * 学校成绩统计 Mapper 接口
 */
public interface SchoolScoreMapper {
    /**
     * 查询指定时间范围内全区各校成绩统计
     * 
     * @param startDate 学期开始日期
     * @param endDate 学期结束日期
     * @param deptName 学校名称(可选模糊查询)
     * @return 统计列表
     */
    List<SchoolScoreStatsVO> selectSchoolScoreStats(@Param("startDate") String startDate, 
                                                    @Param("endDate") String endDate, 
                                                    @Param("deptName") String deptName);

    /**
     * 查询指定学校在指定时间范围内的班级成绩详情
     */
    List<SchoolScoreStatsVO> selectClassScoreStats(@Param("startDate") String startDate, 
                                                   @Param("endDate") String endDate, 
                                                   @Param("deptId") Long deptId);
}
