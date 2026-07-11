package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.CountyExam;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 县考主表 Mapper接口
 * 
 * @author ruoyi
 */
public interface CountyExamMapper {
    
    /**
     * 查询县考
     */
    CountyExam selectCountyExamById(Long examId);

    /**
     * 查询县考列表
     */
    List<CountyExam> selectCountyExamList(CountyExam countyExam);

    /**
     * 新增县考
     */
    int insertCountyExam(CountyExam countyExam);

    /**
     * 修改县考
     */
    int updateCountyExam(CountyExam countyExam);

    /**
     * 更新区域抽测状态。
     */
    int updateStatus(@Param("examId") Long examId, @Param("status") String status);

    /**
     * 更新匿名评卷开关。
     */
    int updateGradingEnabled(@Param("examId") Long examId,
                             @Param("gradingEnabled") String gradingEnabled,
                             @Param("updateBy") String updateBy);

    /**
     * 删除县考
     */
    int deleteCountyExamById(Long examId);

    /**
     * 批量删除县考
     */
    int deleteCountyExamByIds(Long[] examIds);
}
