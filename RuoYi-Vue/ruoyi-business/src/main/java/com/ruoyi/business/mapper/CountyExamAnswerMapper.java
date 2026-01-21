package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.CountyExamAnswer;
import java.util.List;

/**
 * 县考答题记录表 Mapper接口
 * 
 * @author ruoyi
 */
public interface CountyExamAnswerMapper {
    
    /**
     * 根据考试和学生查询答题记录
     */
    List<CountyExamAnswer> selectByExamAndStudent(Long examId, Long studentId);

    /**
     * 新增答题记录
     */
    int insert(CountyExamAnswer countyExamAnswer);

    /**
     * 更新答题记录
     */
    int update(CountyExamAnswer countyExamAnswer);

    /**
     * 根据评卷人查询待评答题
     */
    List<CountyExamAnswer> selectByGraderId(Long graderId, String gradingStatus);

    /**
     * 批量更新评卷人
     */
    int batchUpdateGrader(List<Long> answerIds, Long graderId);
}
