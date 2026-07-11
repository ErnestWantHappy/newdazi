package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.CountyExamAnswer;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 县考答题记录表 Mapper接口
 * 
 * @author ruoyi
 */
public interface CountyExamAnswerMapper {
    
    /**
     * 根据考试和学生查询答题记录
     */
    List<CountyExamAnswer> selectByExamAndStudent(@Param("examId") Long examId,
                                                  @Param("studentId") Long studentId);

    CountyExamAnswer selectById(Long answerId);

    CountyExamAnswer selectLatestByExamStudentQuestion(@Param("examId") Long examId,
                                                       @Param("studentId") Long studentId,
                                                       @Param("questionId") Long questionId);

    /**
     * 新增答题记录
     */
    int insert(CountyExamAnswer countyExamAnswer);

    /**
     * 更新答题记录
     */
    int update(CountyExamAnswer countyExamAnswer);

    /**
     * 学生端保存答题记录，需要允许清空旧预览字段。
     */
    int updateStudentAnswer(CountyExamAnswer countyExamAnswer);

    int updateGrade(@Param("answerId") Long answerId,
                    @Param("score") Integer score,
                    @Param("graderId") Long graderId);

    /**
     * 根据评卷人查询待评答题
     */
    List<CountyExamAnswer> selectByGraderId(@Param("graderId") Long graderId,
                                            @Param("gradingStatus") String gradingStatus);

    /**
     * 批量更新评卷人
     */
    int batchUpdateGrader(@Param("answerIds") List<Long> answerIds,
                          @Param("graderId") Long graderId);

    int updatePreviewStatus(CountyExamAnswer countyExamAnswer);

    List<CountyExamAnswer> selectPracticalAnswersForAllocation(Long examId);

    List<CountyExamAnswer> selectPracticalAnswersForAllocationByQuestion(@Param("examId") Long examId,
                                                                         @Param("questionId") Long questionId);

    int countUngradedPracticalAnswers(Long examId);

    int countGradedPracticalAnswers(Long examId);

    int clearPracticalGraders(Long examId);

    int resetPracticalGrading(Long examId);

    List<Map<String, Object>> selectGradingTasks(@Param("graderId") Long graderId,
                                                 @Param("gradingStatus") String gradingStatus);

    Map<String, Object> selectGradingAnswerDetail(@Param("answerId") Long answerId,
                                                  @Param("graderId") Long graderId);

    int countTasksByGrader(@Param("graderId") Long graderId,
                           @Param("gradingStatus") String gradingStatus);

    int claimSubmitPreviewConversion(@Param("answerId") Long answerId,
                                     @Param("claimedAt") java.util.Date claimedAt);

    int claimRetryPreviewConversion(@Param("answerId") Long answerId,
                                    @Param("expectedStatus") String expectedStatus,
                                    @Param("expectedRetryCount") Integer expectedRetryCount,
                                    @Param("expectedLastRetryTime") java.util.Date expectedLastRetryTime,
                                    @Param("nextRetryCount") Integer nextRetryCount,
                                    @Param("claimedAt") java.util.Date claimedAt);

    List<CountyExamAnswer> selectRecoverablePreviewsForRetry(@Param("retryBefore") java.util.Date retryBefore,
                                                             @Param("stuckBefore") java.util.Date stuckBefore,
                                                             @Param("maxRetryCount") int maxRetryCount);
}
