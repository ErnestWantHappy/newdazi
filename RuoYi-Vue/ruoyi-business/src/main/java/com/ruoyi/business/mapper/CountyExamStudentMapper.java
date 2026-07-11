package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.CountyExamStudent;
import java.math.BigDecimal;
import java.util.Date;
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

    /** 锁定学生本场作答记录，串行化草稿、最终提交和自动提交。 */
    CountyExamStudent selectByExamAndStudentForUpdate(@Param("examId") Long examId,
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

    int updateScores(@Param("id") Long id,
                     @Param("totalScore") BigDecimal totalScore,
                     @Param("theoryScore") BigDecimal theoryScore,
                     @Param("techScore") BigDecimal techScore);

    int markSubmittedIfOpen(@Param("id") Long id,
                            @Param("submitTime") Date submitTime,
                            @Param("autoSubmit") String autoSubmit);

    /**
     * 按学校统计成绩
     */
    List<CountyExamStudent> selectStatsByDept(Long examId);

    List<CountyExamStudent> selectParticipants(Long examId);

    List<Map<String, Object>> selectStudentRows(@Param("examId") Long examId,
                                                @Param("keyword") String keyword);

    List<Map<String, Object>> selectSummaryRows(@Param("examId") Long examId);
}
