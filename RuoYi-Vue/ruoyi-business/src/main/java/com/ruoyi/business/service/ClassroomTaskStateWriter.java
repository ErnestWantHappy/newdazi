package com.ruoyi.business.service;

import java.util.Date;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.business.config.ClassroomWebSocketHandler;
import com.ruoyi.business.domain.BizStudent;
import com.ruoyi.business.domain.BizStudentTaskState;
import com.ruoyi.business.mapper.BizStudentTaskStateMapper;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 课堂状态的独立事务写入器。旁路状态不得加入答案、批改等主业务事务。
 */
@Service
public class ClassroomTaskStateWriter
{
    @Autowired
    private BizStudentTaskStateMapper stateMapper;

    @Autowired(required = false)
    private ClassroomWebSocketHandler classroomWebSocketHandler;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public BizStudentTaskState mark(BizStudent student, Long deptId, Long lessonId,
                                    Long questionId, String targetState)
    {
        if (student == null || student.getStudentId() == null || deptId == null
                || lessonId == null || questionId == null || !isKnownState(targetState))
        {
            throw new ServiceException("课堂任务状态参数不完整");
        }
        BizStudentTaskState current = stateMapper.selectOne(lessonId, questionId, student.getStudentId());
        String currentState = current == null ? ClassroomTaskStateService.NOT_ENTERED : current.getTaskState();
        if (targetState.equals(currentState)) return current;
        if ((ClassroomTaskStateService.ENTERED.equals(targetState)
                && !ClassroomTaskStateService.NOT_ENTERED.equals(currentState)
                && !ClassroomTaskStateService.ENTERED.equals(currentState))
                || (ClassroomTaskStateService.WORKING.equals(targetState)
                && (ClassroomTaskStateService.SUBMITTED.equals(currentState)
                || ClassroomTaskStateService.GRADED.equals(currentState))))
        {
            return current;
        }

        BizStudentTaskState state = new BizStudentTaskState();
        state.setDeptId(deptId);
        state.setLessonId(lessonId);
        state.setQuestionId(questionId);
        state.setStudentId(student.getStudentId());
        state.setTaskState(targetState);
        state.setChangedAt(new Date());
        stateMapper.upsert(state);
        BizStudentTaskState saved = stateMapper.selectOne(lessonId, questionId, student.getStudentId());
        if (saved == null) throw new ServiceException("课堂任务状态保存失败");
        broadcastAfterCommit(student, saved);
        return saved;
    }

    private boolean isKnownState(String state)
    {
        return ClassroomTaskStateService.ENTERED.equals(state)
                || ClassroomTaskStateService.WORKING.equals(state)
                || ClassroomTaskStateService.SUBMITTED.equals(state)
                || ClassroomTaskStateService.GRADED.equals(state)
                || ClassroomTaskStateService.RETURNED.equals(state);
    }

    private void broadcastAfterCommit(BizStudent student, BizStudentTaskState state)
    {
        Runnable broadcaster = () -> {
            try
            {
                if (classroomWebSocketHandler == null) return;
                JSONObject payload = new JSONObject();
                payload.put("type", "TASK_STATE_UPDATE");
                payload.put("lessonId", state.getLessonId());
                payload.put("questionId", state.getQuestionId());
                payload.put("studentId", state.getStudentId());
                payload.put("taskState", state.getTaskState());
                payload.put("stateVersion", state.getStateVersion());
                payload.put("changedAt", state.getChangedAt());
                classroomWebSocketHandler.broadcastToClassroom(state.getDeptId(), student.getEntryYear(),
                        student.getClassCode(), state.getLessonId(), payload.toJSONString());
            }
            catch (Exception e)
            {
                org.slf4j.LoggerFactory.getLogger(ClassroomTaskStateWriter.class).warn(
                        "课堂任务状态广播失败 lessonId={} questionId={} studentId={}",
                        state.getLessonId(), state.getQuestionId(), state.getStudentId(), e);
            }
        };
        if (TransactionSynchronizationManager.isActualTransactionActive())
        {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
            {
                @Override
                public void afterCommit()
                {
                    broadcaster.run();
                }
            });
        }
        else
        {
            broadcaster.run();
        }
    }
}
