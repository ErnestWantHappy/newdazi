package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.CountyExamPaperQuestion;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 区域抽测学生试卷题目快照 Mapper。
 */
public interface CountyExamPaperQuestionMapper {
    int countByExamAndStudent(@Param("examId") Long examId, @Param("studentId") Long studentId);

    int countPracticalByExamStudentQuestion(@Param("examId") Long examId,
                                            @Param("studentId") Long studentId,
                                            @Param("questionId") Long questionId);

    int batchInsert(@Param("list") List<CountyExamPaperQuestion> list);

    List<BizLessonQuestionDetailVo> selectDetailsByExamAndStudent(@Param("examId") Long examId,
                                                                  @Param("studentId") Long studentId);
}
