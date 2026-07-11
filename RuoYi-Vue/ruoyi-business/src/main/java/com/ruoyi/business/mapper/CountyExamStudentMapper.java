package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.CountyExamStudent;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 县考学生成绩总表 Mapper接口
 * 
 * @author ruoyi
 */
public interface CountyExamStudentMapper {
    
    /**
     * 根据考试和学生查询成绩
     */
    CountyExamStudent selectByExamAndStudent(@Param("examId") Long examId,
                                             @Param("studentId") Long studentId);

    /**
     * 根据考试ID查询所有学生成绩
     */
    List<CountyExamStudent> selectByExamId(Long examId);

    /**
     * 新增学生成绩记录
     */
    int insert(CountyExamStudent countyExamStudent);

    int insertOrIgnore(CountyExamStudent countyExamStudent);

    /**
     * 更新学生成绩
     */
    int update(CountyExamStudent countyExamStudent);

    /**
     * 按学校统计成绩
     */
    List<CountyExamStudent> selectStatsByDept(Long examId);

    List<CountyExamStudent> selectParticipants(Long examId);

    List<Map<String, Object>> selectStudentRows(@Param("examId") Long examId,
                                                @Param("keyword") String keyword);

    List<Map<String, Object>> selectSummaryRows(@Param("examId") Long examId);
}
