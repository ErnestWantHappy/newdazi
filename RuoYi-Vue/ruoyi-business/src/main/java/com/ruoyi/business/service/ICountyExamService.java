package com.ruoyi.business.service;

import com.ruoyi.business.domain.CountyExam;
import java.util.List;

/**
 * 县考管理 Service接口
 * 
 * @author ruoyi
 */
public interface ICountyExamService {
    
    /**
     * 查询县考
     * 
     * @param examId 县考ID
     * @return 县考信息
     */
    CountyExam selectCountyExamById(Long examId);

    /**
     * 查询县考列表
     * 
     * @param countyExam 查询条件
     * @return 县考列表
     */
    List<CountyExam> selectCountyExamList(CountyExam countyExam);

    /**
     * 新增县考
     * 
     * @param countyExam 县考信息
     * @return 结果
     */
    int insertCountyExam(CountyExam countyExam);

    /**
     * 修改县考
     * 
     * @param countyExam 县考信息
     * @return 结果
     */
    int updateCountyExam(CountyExam countyExam);

    /**
     * 删除县考
     * 
     * @param examId 县考ID
     * @return 结果
     */
    int deleteCountyExamById(Long examId);

    /**
     * 批量删除县考
     * 
     * @param examIds 县考ID数组
     * @return 结果
     */
    int deleteCountyExamByIds(Long[] examIds);
}
