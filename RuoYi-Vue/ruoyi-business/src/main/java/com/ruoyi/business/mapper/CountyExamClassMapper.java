package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.CountyExamClass;
import java.util.List;

/**
 * 县考班级指派表 Mapper接口
 * 
 * @author ruoyi
 */
public interface CountyExamClassMapper {
    
    /**
     * 根据考试ID查询班级列表
     */
    List<CountyExamClass> selectByExamId(Long examId);

    /**
     * 新增班级指派
     */
    int insert(CountyExamClass countyExamClass);

    /**
     * 批量新增班级指派
     */
    int batchInsert(List<CountyExamClass> list);

    /**
     * 根据考试ID删除所有班级指派
     */
    int deleteByExamId(Long examId);

    /**
     * 根据学生信息查询是否在县考班级中
     */
    CountyExamClass selectByStudentInfo(Long deptId, String entryYear, String classCode);
}
