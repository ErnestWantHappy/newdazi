package com.ruoyi.business.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.ruoyi.business.config.ClassroomRoomKey;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentTaskState;
import com.ruoyi.business.domain.vo.ClassroomStudentTaskSummaryVo;
import com.ruoyi.business.mapper.BizStudentTaskStateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 课堂任务状态统一写入口。数据库是事实源，WebSocket 只发送提交成功后的变更提示。
 */
@Service
public class ClassroomTaskStateService
{
    public static final String NOT_ENTERED = "NOT_ENTERED";
    public static final String ENTERED = "ENTERED";
    public static final String WORKING = "WORKING";
    public static final String SUBMITTED = "SUBMITTED";
    public static final String GRADED = "GRADED";
    public static final String RETURNED = "RETURNED";

    private static final Logger log = LoggerFactory.getLogger(ClassroomTaskStateService.class);

    @Autowired private BizStudentTaskStateMapper stateMapper;
    @Autowired private ClassroomTaskStateWriter stateWriter;

    public BizStudentTaskState mark(BizStudent student, Long deptId, Long lessonId,
                                    Long questionId, String targetState)
    {
        return stateWriter.mark(student, deptId, lessonId, questionId, targetState);
    }

    /**
     * 课堂状态只服务于展示与实时提示，状态表不可用时不能否定已经成功保存的答案或作品。
     * 有外层业务事务时只在提交后写入，避免状态表故障把主业务标为回滚；无外层事务时立即独立写入。
     */
    public void markSafely(BizStudent student, Long deptId, Long lessonId, Long questionId, String targetState)
    {
        Runnable marker = () -> {
            try
            {
                stateWriter.mark(student, deptId, lessonId, questionId, targetState);
            }
            catch (Exception e)
            {
                log.warn("课堂任务状态写入失败，不影响已完成的业务操作 lessonId={} questionId={} studentId={} targetState={}",
                        lessonId, questionId, student == null ? null : student.getStudentId(), targetState, e);
            }
        };
        if (TransactionSynchronizationManager.isActualTransactionActive())
        {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
            {
                @Override
                public void afterCommit()
                {
                    marker.run();
                }
            });
        }
        else
        {
            marker.run();
        }
    }

    public List<BizStudentTaskState> listClassStates(Long deptId, Long lessonId, Long questionId,
                                                     String entryYear, String classCode)
    {
        return stateMapper.selectClassStates(deptId, lessonId, questionId,
                entryYear == null ? null : entryYear.trim(), ClassroomRoomKey.normalizeClassCode(classCode));
    }

    public List<ClassroomStudentTaskSummaryVo> listClassSummary(Long deptId, Long lessonId,
                                                                  String entryYear, String classCode)
    {
        List<ClassroomStudentTaskSummaryVo> summaries = stateMapper.selectClassSummary(deptId, lessonId,
                entryYear == null ? null : entryYear.trim(), ClassroomRoomKey.normalizeClassCode(classCode));
        if (summaries == null || summaries.isEmpty())
        {
            return Collections.emptyList();
        }
        List<ClassroomStudentTaskSummaryVo> result = new ArrayList<>();
        for (ClassroomStudentTaskSummaryVo summary : summaries)
        {
            // MyBatis 在某些历史班级的左连接结果中可能返回空行，不能让一条脏行阻断整班监控。
            if (summary == null)
            {
                continue;
            }
            summary.setTaskState(resolveSummaryState(summary));
            result.add(summary);
        }
        return result;
    }

    /**
     * 课程汇总优先呈现教师此刻最需要处理的状态，题目明细仍以状态表为准。
     */
    private String resolveSummaryState(ClassroomStudentTaskSummaryVo summary)
    {
        if (number(summary.getTotalQuestionCount()) == 0) return "NO_TASK";
        if (number(summary.getReturnedQuestionCount()) > 0) return RETURNED;
        if (number(summary.getWorkingQuestionCount()) > 0) return WORKING;
        if (number(summary.getEnteredQuestionCount()) > 0) return ENTERED;
        if (number(summary.getSubmittedQuestionCount()) > 0) return SUBMITTED;
        if (number(summary.getGradedQuestionCount()) > 0) return GRADED;
        return NOT_ENTERED;
    }

    private int number(Integer value)
    {
        return value == null ? 0 : value;
    }

}
