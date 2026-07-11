package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.CountyExamGrader;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 评卷任务分配表 Mapper接口
 * 
 * @author ruoyi
 */
public interface CountyExamGraderMapper {
    
    /**
     * 根据考试ID查询评卷任务
     */
    List<CountyExamGrader> selectByExamId(Long examId);

    /**
     * 根据考试和教师查询
     */
    CountyExamGrader selectByExamAndGrader(@Param("examId") Long examId, @Param("graderId") Long graderId);

    /**
     * 根据考试、题目和教师查询
     */
    CountyExamGrader selectByExamQuestionAndGrader(@Param("examId") Long examId,
                                                   @Param("questionId") Long questionId,
                                                   @Param("graderId") Long graderId);

    /**
     * 新增评卷任务
     */
    int insert(CountyExamGrader countyExamGrader);

    /**
     * 更新评卷任务
     */
    int update(CountyExamGrader countyExamGrader);

    /**
     * 删除考试的所有评卷任务
     */
    int deleteByExamId(Long examId);
}
