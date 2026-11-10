-- Python批改教师确认制：先核对，再修复指定lesson_id/question_id。
-- 默认不填写目标ID；两个占位值同时仍为0时仅输出候选，不做任何UPDATE。
SET @lesson_id = 0;
SET @question_id = 0;

-- 候选范围：仅Python课程题、正式成绩为0、存在同代码最高分0分提交。
SELECT a.answer_id, a.student_id, l.lesson_title, q.question_id, a.score, a.submit_time,
       ps.submission_id, ps.score AS system_score, ps.status_code,
       ps.judged_at, state.task_state
FROM biz_student_answer a
JOIN biz_lesson_question lq ON lq.lesson_id=a.lesson_id AND lq.question_id=a.question_id
JOIN biz_question q ON q.question_id=a.question_id AND q.practical_mode='PYTHON'
JOIN biz_lesson l ON l.lesson_id=a.lesson_id
LEFT JOIN biz_student_task_state state
  ON state.student_id=a.student_id AND state.lesson_id=a.lesson_id AND state.question_id=a.question_id
JOIN biz_programming_submission ps
  ON ps.student_id=a.student_id AND ps.lesson_id=a.lesson_id AND ps.question_id=a.question_id
 AND ps.submission_kind='SUBMIT' AND ps.score=0
 AND ps.source_code=a.student_answer
 AND ps.status_code IN ('ACCEPTED','PARTIAL','WRONG_ANSWER','SYNTAX_ERROR','RUNTIME_ERROR','TIME_LIMIT','MEMORY_LIMIT')
WHERE a.score=0
  AND (@lesson_id=0 OR a.lesson_id=@lesson_id)
  AND (@question_id=0 OR a.question_id=@question_id)
ORDER BY a.lesson_id, a.student_id, a.question_id;

-- 现场人工核对上面候选后才可设置目标ID执行；只撤回0分，不碰教师给过的非0分。
UPDATE biz_student_answer a
JOIN biz_student_task_state state
  ON state.student_id=a.student_id AND state.lesson_id=a.lesson_id AND state.question_id=a.question_id
SET a.score=NULL,
    a.is_correct=0,
    state.task_state='SUBMITTED',
    state.changed_at=NOW(),
    state.update_time=NOW()
WHERE @lesson_id<>0 AND @question_id<>0
  AND a.lesson_id=@lesson_id AND a.question_id=@question_id AND a.score=0
  AND state.task_state='GRADED'
  AND EXISTS (
      SELECT 1 FROM biz_programming_submission ps
      WHERE ps.student_id=a.student_id AND ps.lesson_id=a.lesson_id AND ps.question_id=a.question_id
        AND ps.submission_kind='SUBMIT' AND ps.score=0
        AND ps.source_code=a.student_answer
        AND ps.status_code IN ('ACCEPTED','PARTIAL','WRONG_ANSWER','SYNTAX_ERROR','RUNTIME_ERROR','TIME_LIMIT','MEMORY_LIMIT')
  );

SELECT ROW_COUNT() AS reverted_zero_score_rows;
