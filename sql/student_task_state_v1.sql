-- 课堂任务状态事实表：先在目标库备份，再执行本脚本，执行后需重启后端。
-- 前检失败会直接终止脚本，避免在不满足数据前提时继续建表或回填。
DELIMITER //
DROP PROCEDURE IF EXISTS check_student_task_state_preconditions //
CREATE PROCEDURE check_student_task_state_preconditions()
BEGIN
    DECLARE duplicate_count BIGINT DEFAULT 0;
    DECLARE missing_dept_count BIGINT DEFAULT 0;

    SELECT COUNT(*) INTO duplicate_count
    FROM (
        SELECT user_id FROM biz_student GROUP BY user_id HAVING COUNT(*) > 1
    ) duplicated;
    SELECT COUNT(*) INTO missing_dept_count
    FROM biz_student student
    INNER JOIN sys_user user ON user.user_id = student.user_id AND user.del_flag = '0'
    WHERE user.dept_id IS NULL;

    IF duplicate_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '迁移终止：biz_student 存在重复 user_id';
    END IF;
    IF missing_dept_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '迁移终止：有效学生用户存在空 dept_id';
    END IF;
END //
CALL check_student_task_state_preconditions() //
DROP PROCEDURE check_student_task_state_preconditions //
DELIMITER ;
CREATE TABLE IF NOT EXISTS biz_student_task_state (
    state_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '状态主键',
    dept_id BIGINT NOT NULL COMMENT '学校ID',
    lesson_id BIGINT NOT NULL COMMENT '课程ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    task_state VARCHAR(20) NOT NULL COMMENT 'NOT_ENTERED/ENTERED/WORKING/SUBMITTED/GRADED/RETURNED',
    state_version BIGINT NOT NULL DEFAULT 1 COMMENT '单调递增版本，用于丢弃乱序消息',
    changed_at DATETIME NOT NULL COMMENT '状态变化时间',
    create_time DATETIME NULL,
    update_time DATETIME NULL,
    PRIMARY KEY (state_id),
    UNIQUE KEY uk_task_state_lesson_question_student (lesson_id, question_id, student_id),
    KEY idx_task_state_class_query (dept_id, lesson_id, question_id, task_state),
    KEY idx_task_state_student (student_id, lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生课堂任务实时状态';

-- 历史答案回填为已提交/已批改；重复执行不会降低状态版本。
INSERT INTO biz_student_task_state
    (dept_id, lesson_id, question_id, student_id, task_state, state_version, changed_at, create_time, update_time)
SELECT user.dept_id, answer.lesson_id, answer.question_id, answer.student_id,
       CASE WHEN answer.score IS NULL THEN 'SUBMITTED' ELSE 'GRADED' END,
       1, COALESCE(answer.submit_time, NOW()), NOW(), NOW()
FROM biz_student_answer answer
INNER JOIN (
    SELECT student_id, lesson_id, question_id, MAX(answer_id) AS answer_id
    FROM biz_student_answer
    GROUP BY student_id, lesson_id, question_id
) latest ON latest.answer_id = answer.answer_id
INNER JOIN biz_student student ON student.student_id = answer.student_id
INNER JOIN sys_user user ON user.user_id = student.user_id
                       AND user.del_flag = '0'
                       AND user.dept_id IS NOT NULL
INNER JOIN biz_lesson_question lesson_question
        ON lesson_question.lesson_id = answer.lesson_id
       AND lesson_question.question_id = answer.question_id
INNER JOIN biz_question question
        ON question.question_id = answer.question_id
WHERE answer.student_answer IS NOT NULL AND TRIM(answer.student_answer) <> ''
ON DUPLICATE KEY UPDATE
    task_state = CASE
        WHEN task_state = 'RETURNED' THEN 'RETURNED'
        WHEN task_state = 'GRADED' OR VALUES(task_state) = 'GRADED' THEN 'GRADED'
        WHEN task_state IN ('ENTERED', 'WORKING', 'SUBMITTED') THEN task_state
        ELSE VALUES(task_state)
    END,
    changed_at = GREATEST(changed_at, VALUES(changed_at)),
    update_time = NOW();

-- 后检：四项均应为 0。
SELECT COUNT(*) AS duplicate_task_state_groups
FROM (
    SELECT lesson_id, question_id, student_id
    FROM biz_student_task_state
    GROUP BY lesson_id, question_id, student_id
    HAVING COUNT(*) > 1
) duplicated;

SELECT COUNT(*) AS invalid_task_states
FROM biz_student_task_state
WHERE task_state NOT IN ('NOT_ENTERED', 'ENTERED', 'WORKING', 'SUBMITTED', 'GRADED', 'RETURNED')
   OR state_version < 1;

SELECT COUNT(*) AS orphan_task_states
FROM biz_student_task_state state
LEFT JOIN biz_lesson lesson ON lesson.lesson_id = state.lesson_id
LEFT JOIN biz_question question ON question.question_id = state.question_id
LEFT JOIN biz_student student ON student.student_id = state.student_id
LEFT JOIN sys_user user ON user.user_id = student.user_id
WHERE lesson.lesson_id IS NULL OR question.question_id IS NULL OR student.student_id IS NULL
   OR user.user_id IS NULL OR user.del_flag <> '0' OR user.dept_id IS NULL;

SELECT COUNT(*) AS task_states_with_wrong_dept
FROM biz_student_task_state state
INNER JOIN biz_student student ON student.student_id = state.student_id
INNER JOIN sys_user user ON user.user_id = student.user_id AND user.del_flag = '0'
WHERE state.dept_id <> user.dept_id;
