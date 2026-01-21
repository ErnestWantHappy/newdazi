package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.CountyExamQuestion;
import java.util.List;

/**
 * 县考题目关联表 Mapper接口
 * 
 * @author ruoyi
 */
public interface CountyExamQuestionMapper {
    
    /**
     * 根据考试ID查询题目列表
     */
    List<CountyExamQuestion> selectByExamId(Long examId);

    /**
     * 新增题目关联
     */
    int insert(CountyExamQuestion countyExamQuestion);

    /**
     * 批量新增题目关联
     */
    int batchInsert(List<CountyExamQuestion> list);

    /**
     * 删除题目关联
     */
    int deleteById(Long id);

    /**
     * 根据考试ID删除所有题目关联
     */
    int deleteByExamId(Long examId);

    /**
     * 更新题目分值和排序
     */
    int update(CountyExamQuestion countyExamQuestion);
}
