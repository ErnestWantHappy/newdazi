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
 * 默认使用独立事务写入旁路状态；理论交卷和重做显式使用主事务保证一致。
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
        return saveState(student, deptId, lessonId, questionId, targetState);
    }

    /** 理论重做需要答案与状态原子切换，不能使用旁路独立事务。 */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public BizStudentTaskState markInTransaction(BizStudent student, Long deptId, Long lessonId,
                                                 Long questionId, String targetState)
    {
        return saveState(student, deptId, lessonId, questionId, targetState);
    }

    private BizStudentTaskState saveState(BizStudent student, Long deptId, Long lessonId,
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
                || ClassroomTaskStateService.GRADED.equals(currentState)
                || ClassroomTaskStateService.RETURNED.equals(currentState))))
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
        // 并发同状态写入可能不改变行；当前读避免可重复读快照漏掉另一事务已插入的记录。
        BizStudentTaskState saved = stateMapper.selectOneForUpdate(lessonId, questionId, student.getStudentId());
        if (saved == null) throw new ServiceException("课堂任务状态保存失败");
        broadcastAfterCommit(student, saved);
        return saved;
    }

    /** 删除业务已锁定指派、作品和答案，此入口与清除提交事实使用同一事务。 */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public void withdrawSubmissionInTransaction(BizStudent student, Long lessonId, Long questionId) {
        if (student == null || student.getStudentId() == null || student.getDeptId() == null
                || lessonId == null || questionId == null) throw new ServiceException("课堂任务状态参数不完整");
        stateMapper.selectOneForUpdate(lessonId, questionId, student.getStudentId());
        BizStudentTaskState state = new BizStudentTaskState();
        state.setDeptId(student.getDeptId());
        state.setStudentId(student.getStudentId());
        state.setLessonId(lessonId);
        state.setQuestionId(questionId);
        state.setTaskState(ClassroomTaskStateService.WORKING);
        state.setChangedAt(new Date());
        stateMapper.withdrawSubmission(state);
        BizStudentTaskState saved = stateMapper.selectOneForUpdate(lessonId, questionId, student.getStudentId());
        if (saved == null || !ClassroomTaskStateService.WORKING.equals(saved.getTaskState()))
            throw new ServiceException("撤销提交状态失败");
        broadcastAfterCommit(student, saved);
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
