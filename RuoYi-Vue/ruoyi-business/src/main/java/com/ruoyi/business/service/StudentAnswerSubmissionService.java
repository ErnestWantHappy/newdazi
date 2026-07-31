package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizStudentAnswer;
import com.ruoyi.business.mapper.BizStudentAnswerMapper;

/**
 * 学生整单答案落库事务。
 */
@Service
public class StudentAnswerSubmissionService
{
    @Autowired
    private BizStudentAnswerMapper studentAnswerMapper;

    /**
     * 事务只包围答案写入，避免前置查询延长锁持有时间；READ_COMMITTED 用于减少无关键的间隙锁竞争。
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<Long> persistAnswers(Long studentId, Long lessonId, List<BizStudentAnswer> answers)
    {
        if (answers == null || answers.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> pendingConversionAnswerIds = new ArrayList<>();
        for (BizStudentAnswer answer : answers) {
            studentAnswerMapper.upsertAnswer(answer);
            if ("pending".equals(answer.getPreviewStatus())
                    && answer.getAnswerId() != null
                    && answer.getStudentAnswer() != null
                    && !answer.getStudentAnswer().trim().isEmpty()) {
                pendingConversionAnswerIds.add(answer.getAnswerId());
            }
        }
        return pendingConversionAnswerIds;
    }
}
