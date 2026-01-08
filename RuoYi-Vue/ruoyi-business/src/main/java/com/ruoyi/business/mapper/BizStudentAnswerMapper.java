package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.domain.vo.BizLessonQuestionDetailVo;
import com.ruoyi.business.domain.vo.LessonRankingVo;
import com.ruoyi.business.domain.vo.PracticalSubmissionVo;
import org.apache.ibatis.annotations.Param;

/**
 * 学生答题记录Mapper接口
 * 
 * @author zdx
 * @date 2025-12-30
 */
public interface BizStudentAnswerMapper 
{
    /**
     * 批量插入答题记录
     */
    void batchInsert(List<BizStudentAnswer> answers);

    /**
     * 查询学生某课程的答题记录
     */
    List<BizStudentAnswer> selectByStudentAndLesson(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId);

    /**
     * 删除学生某课程的旧答题记录（用于重新提交）
     */
    void deleteByStudentAndLesson(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId);

    /**
     * 查询课程排行榜
     */
    List<LessonRankingVo> selectLessonRanking(@Param("lessonId") Long lessonId);

    /**
     * 查询学生是否已提交过该课程
     */
    /**
     * 删除学生某课程指定题目的旧答题记录（用于增量更新）
     */
    void deleteByStudentLessonAndQuestions(@Param("studentId") Long studentId, 
                                          @Param("lessonId") Long lessonId, 
                                          @Param("questionIds") List<Long> questionIds);

    /**
     * 查询学生有答题记录的所有课程ID（按年份筛选）
     */
    List<Long> selectDistinctLessonIdsByStudent(@Param("studentId") Long studentId, @Param("year") Integer year);

    /**
     * 查询错题列表
     */
    List<BizLessonQuestionDetailVo> selectWrongQuestions(@Param("studentId") Long studentId, @Param("lessonId") Long lessonId);

    /**
     * P5: 查询班级所有学生的操作题提交情况（含未提交）
     */
    List<PracticalSubmissionVo> selectPracticalSubmissions(@Param("lessonId") Long lessonId, @Param("questionId") Long questionId, @Param("classCode") String classCode, @Param("entryYear") String entryYear);

    /**
     * 更新答题记录分数（批改打分）
     */
    int updateScore(@Param("answerId") Long answerId, @Param("score") Integer score);
    
    /**
     * P2: 查询学生成绩汇总（按课程分组）
     */
    List<java.util.Map<String, Object>> selectScoreSummaryByStudent(
        @Param("studentId") Long studentId, 
        @Param("lessonId") Long lessonId);
}
