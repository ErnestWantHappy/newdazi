-- 课程与成绩监管 / 操作题限期批改 v1
-- MySQL 8；脚本可重复执行。历史初始化时间首次执行后固定，不会因重跑改变。

CREATE TABLE IF NOT EXISTS biz_lesson_class_scope (
    scope_id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '事实主键',
    lesson_id             BIGINT       NOT NULL COMMENT '课程ID',
    dept_id               BIGINT       NOT NULL COMMENT '课程所属学校',
    entry_year            VARCHAR(4)   NOT NULL COMMENT '入学年份',
    class_code            VARCHAR(30)  NOT NULL COMMENT '班级编号',
    current_assigned      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否仍为当前指派',
    evidence_source       VARCHAR(32)  NOT NULL COMMENT 'CURRENT_ASSIGNMENT/ANSWER/PERFORMANCE/REALTIME',
    first_assigned_time   DATETIME     NULL COMMENT '首次可确认关联时间',
    last_assigned_time    DATETIME     NULL COMMENT '最近可确认关联时间',
    create_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (scope_id),
    UNIQUE KEY uk_lesson_class_scope (lesson_id, dept_id, entry_year, class_code),
    KEY idx_lesson_class_scope_lesson (lesson_id, current_assigned),
    KEY idx_lesson_class_scope_class (dept_id, entry_year, class_code, lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程班级事实';

CREATE TABLE IF NOT EXISTS biz_practical_grading_deadline (
    deadline_id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '期限主键',
    lesson_id                BIGINT       NOT NULL COMMENT '课程ID',
    dept_id                  BIGINT       NOT NULL COMMENT '课程所属学校',
    entry_year               VARCHAR(4)   NOT NULL COMMENT '入学年份',
    class_code               VARCHAR(30)  NOT NULL COMMENT '班级编号',
    trigger_time             DATETIME     NOT NULL COMMENT '首次达到50%的时间',
    trigger_answered_count   INT          NOT NULL COMMENT '触发时已有答题记录人数',
    trigger_student_count    INT          NOT NULL COMMENT '触发时有效学生总数',
    deadline_days            INT          NOT NULL COMMENT '生成时使用的期限天数',
    original_deadline_time   DATETIME     NOT NULL COMMENT '原始截止时间',
    current_deadline_time    DATETIME     NOT NULL COMMENT '当前有效截止时间',
    initialization_source    VARCHAR(32)  NOT NULL COMMENT 'REALTIME/COMPENSATION/HISTORICAL_BACKFILL',
    last_adjustment_type     VARCHAR(16)  NULL COMMENT 'EXTEND/REOPEN',
    create_by                VARCHAR(64)  NULL,
    create_time              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by                VARCHAR(64)  NULL,
    update_time              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (deadline_id),
    UNIQUE KEY uk_practical_deadline_class (lesson_id, dept_id, entry_year, class_code),
    KEY idx_practical_deadline_due (current_deadline_time),
    KEY idx_practical_deadline_class (dept_id, entry_year, class_code, lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程班级操作题批改期限';

CREATE TABLE IF NOT EXISTS biz_practical_grading_deadline_audit (
    audit_id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '审计主键',
    deadline_id          BIGINT        NOT NULL COMMENT '期限主键',
    lesson_id            BIGINT        NOT NULL COMMENT '课程ID',
    dept_id              BIGINT        NOT NULL COMMENT '学校ID',
    entry_year           VARCHAR(4)    NOT NULL COMMENT '入学年份',
    class_code           VARCHAR(30)   NOT NULL COMMENT '班级编号',
    action_type          VARCHAR(16)   NOT NULL COMMENT 'EXTEND/REOPEN',
    old_deadline_time    DATETIME      NOT NULL COMMENT '原截止时间',
    new_deadline_time    DATETIME      NOT NULL COMMENT '新截止时间',
    reason               VARCHAR(500)  NOT NULL COMMENT '调整原因',
    operator_id          BIGINT        NOT NULL COMMENT '操作人ID',
    operator_name        VARCHAR(64)   NOT NULL COMMENT '操作账号快照',
    operate_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (audit_id),
    KEY idx_practical_deadline_audit (deadline_id, operate_time),
    KEY idx_practical_deadline_audit_class (lesson_id, dept_id, entry_year, class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作题批改期限调整审计';

-- 兼容曾由早期脚本创建成 unicode_ci 的空表；与现有业务表保持同一排序规则。
ALTER TABLE biz_lesson_class_scope
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE biz_practical_grading_deadline
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE biz_practical_grading_deadline_audit
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 全局配置和首次上线时间均只在不存在时写入，保证重跑不改变已生成期限。
INSERT INTO sys_config
    (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '操作题批改期限天数', 'business.practicalGrading.deadlineDays', '21', 'Y', 'system', NOW(),
       '仅影响以后首次触发的课程班级，允许范围1-365'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_config WHERE config_key = 'business.practicalGrading.deadlineDays'
);

INSERT INTO sys_config
    (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '操作题期限功能上线时间', 'business.practicalGrading.goLiveTime',
       DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 'Y', 'system', NOW(),
       '历史初始化固定时间，禁止人工修改'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_config WHERE config_key = 'business.practicalGrading.goLiveTime'
);

-- 只修复证据唯一的5门历史课程；无教师、无班级和无答题证据的孤儿课程保持待治理。
UPDATE biz_lesson lesson
INNER JOIN (
    SELECT user_id, MIN(dept_id) AS dept_id
    FROM (
        SELECT user_id, dept_id FROM sys_user WHERE dept_id IS NOT NULL
        UNION
        SELECT user_id, dept_id FROM sys_user_dept
    ) user_dept
    GROUP BY user_id
    HAVING COUNT(DISTINCT dept_id) = 1
) one_dept ON one_dept.user_id = lesson.creator_id
SET lesson.dept_id = one_dept.dept_id
WHERE lesson.dept_id IS NULL;

UPDATE biz_lesson lesson
INNER JOIN sys_user creator ON creator.user_name = lesson.create_by
INNER JOIN (
    SELECT user_id, MIN(dept_id) AS dept_id
    FROM (
        SELECT user_id, dept_id FROM sys_user WHERE dept_id IS NOT NULL
        UNION
        SELECT user_id, dept_id FROM sys_user_dept
    ) user_dept
    GROUP BY user_id
    HAVING COUNT(DISTINCT dept_id) = 1
) one_dept ON one_dept.user_id = creator.user_id
SET lesson.dept_id = one_dept.dept_id,
    lesson.creator_id = COALESCE(lesson.creator_id, creator.user_id)
WHERE lesson.dept_id IS NULL;

UPDATE biz_lesson lesson
INNER JOIN sys_user creator ON creator.user_name = lesson.create_by
SET lesson.creator_id = creator.user_id
WHERE lesson.creator_id IS NULL;

-- 当前指派优先级最高；答案和课堂表现仅用于恢复已经发生过的历史事实。
INSERT INTO biz_lesson_class_scope
    (lesson_id, dept_id, entry_year, class_code, current_assigned, evidence_source,
     first_assigned_time, last_assigned_time)
SELECT assignment.lesson_id, assignment.dept_id, assignment.entry_year, assignment.class_code,
       1, 'CURRENT_ASSIGNMENT', assignment.assign_time, assignment.assign_time
FROM biz_lesson_assignment assignment
INNER JOIN biz_lesson lesson
        ON lesson.lesson_id = assignment.lesson_id
       AND lesson.dept_id = assignment.dept_id
ON DUPLICATE KEY UPDATE
    current_assigned = 1,
    evidence_source = 'CURRENT_ASSIGNMENT',
    last_assigned_time = VALUES(last_assigned_time);

INSERT INTO biz_lesson_class_scope
    (lesson_id, dept_id, entry_year, class_code, current_assigned, evidence_source,
     first_assigned_time, last_assigned_time)
SELECT answer.lesson_id, lesson.dept_id, student.entry_year, student.class_code,
       0, 'ANSWER', MIN(answer.submit_time), MAX(answer.submit_time)
FROM biz_student_answer answer
INNER JOIN biz_lesson lesson ON lesson.lesson_id = answer.lesson_id AND lesson.dept_id IS NOT NULL
INNER JOIN biz_student student ON student.student_id = answer.student_id
INNER JOIN sys_user user
        ON user.user_id = student.user_id
       AND user.dept_id = lesson.dept_id
       AND user.del_flag = '0'
GROUP BY answer.lesson_id, lesson.dept_id, student.entry_year, student.class_code
ON DUPLICATE KEY UPDATE
    first_assigned_time = LEAST(COALESCE(first_assigned_time, VALUES(first_assigned_time)), VALUES(first_assigned_time)),
    last_assigned_time = GREATEST(COALESCE(last_assigned_time, VALUES(last_assigned_time)), VALUES(last_assigned_time));

INSERT INTO biz_lesson_class_scope
    (lesson_id, dept_id, entry_year, class_code, current_assigned, evidence_source,
     first_assigned_time, last_assigned_time)
SELECT performance.lesson_id, performance.dept_id, student.entry_year, student.class_code,
       0, 'PERFORMANCE', MIN(performance.create_time), MAX(performance.create_time)
FROM biz_classroom_performance performance
INNER JOIN biz_lesson lesson
        ON lesson.lesson_id = performance.lesson_id
       AND lesson.dept_id = performance.dept_id
INNER JOIN biz_student student ON student.student_id = performance.student_id
INNER JOIN sys_user user
        ON user.user_id = student.user_id
       AND user.dept_id = performance.dept_id
       AND user.del_flag = '0'
GROUP BY performance.lesson_id, performance.dept_id, student.entry_year, student.class_code
ON DUPLICATE KEY UPDATE
    first_assigned_time = LEAST(COALESCE(first_assigned_time, VALUES(first_assigned_time)), VALUES(first_assigned_time)),
    last_assigned_time = GREATEST(COALESCE(last_assigned_time, VALUES(last_assigned_time)), VALUES(last_assigned_time));

-- 上线前已达到50%的历史班级统一以上线时间触发，重跑由唯一索引保证幂等。
SET @practical_deadline_go_live = (
    SELECT STR_TO_DATE(config_value, '%Y-%m-%d %H:%i:%s')
    FROM sys_config
    WHERE config_key = 'business.practicalGrading.goLiveTime'
    LIMIT 1
);
SET @practical_deadline_days = (
    SELECT CAST(config_value AS UNSIGNED)
    FROM sys_config
    WHERE config_key = 'business.practicalGrading.deadlineDays'
    LIMIT 1
);

INSERT IGNORE INTO biz_practical_grading_deadline
    (lesson_id, dept_id, entry_year, class_code, trigger_time,
     trigger_answered_count, trigger_student_count, deadline_days,
     original_deadline_time, current_deadline_time, initialization_source,
     create_by, create_time)
SELECT metrics.lesson_id, metrics.dept_id, metrics.entry_year, metrics.class_code,
       @practical_deadline_go_live,
       metrics.answered_student_count, metrics.total_student_count,
       @practical_deadline_days,
       DATE_ADD(@practical_deadline_go_live, INTERVAL @practical_deadline_days DAY),
       DATE_ADD(@practical_deadline_go_live, INTERVAL @practical_deadline_days DAY),
       'HISTORICAL_BACKFILL', 'system', NOW()
FROM (
    SELECT scope.lesson_id, scope.dept_id, scope.entry_year, scope.class_code,
           COUNT(DISTINCT student.student_id) AS total_student_count,
           COUNT(DISTINCT answered.student_id) AS answered_student_count
    FROM biz_lesson_class_scope scope
    INNER JOIN biz_student student
            ON student.entry_year = scope.entry_year
           AND student.class_code = scope.class_code
    INNER JOIN sys_user user
            ON user.user_id = student.user_id
           AND user.dept_id = scope.dept_id
           AND user.del_flag = '0'
           AND user.status = '0'
    LEFT JOIN (
        SELECT DISTINCT answer.lesson_id, answer.student_id
        FROM biz_student_answer answer
        WHERE answer.submit_time <= @practical_deadline_go_live
    ) answered
           ON answered.lesson_id = scope.lesson_id
          AND answered.student_id = student.student_id
    WHERE EXISTS (
        SELECT 1
        FROM biz_lesson_question lesson_question
        INNER JOIN biz_question question ON question.question_id = lesson_question.question_id
        WHERE lesson_question.lesson_id = scope.lesson_id
          AND question.question_type = 'practical'
    )
    GROUP BY scope.lesson_id, scope.dept_id, scope.entry_year, scope.class_code
) metrics
WHERE metrics.total_student_count > 0
  AND metrics.answered_student_count * 2 >= metrics.total_student_count;

-- 权限点沿用原菜单ID 2046，不创建第二个菜单。
UPDATE sys_menu
SET menu_name = '课程与成绩监管',
    perms = 'business:teachingSupervision:view',
    update_by = 'system',
    update_time = NOW()
WHERE menu_id = 2046;

INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '课程监管导出', 2046, 1, '#', '', '', '',
       1, 0, 'F', '0', '0', 'business:teachingSupervision:export', '#', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:teachingSupervision:export');

INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '批改期限配置', 2046, 2, '#', '', '', '',
       1, 0, 'F', '0', '0', 'business:practicalDeadline:config', '#', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:practicalDeadline:config');

INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '批改期限调整', 2046, 3, '#', '', '', '',
       1, 0, 'F', '0', '0', 'business:practicalDeadline:adjust', '#', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'business:practicalDeadline:adjust');

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role.role_id, menu.menu_id
FROM sys_role role
JOIN sys_menu menu
  ON menu.menu_id = 2046
  OR menu.perms IN (
      'business:teachingSupervision:export',
      'business:practicalDeadline:config',
      'business:practicalDeadline:adjust'
  )
WHERE role.role_key = 'researcher';

-- 补偿任务每10分钟执行一次，禁止并发；业务服务负责分页和幂等。
INSERT INTO sys_job
    (job_name, job_group, invoke_target, cron_expression, misfire_policy,
     concurrent, status, create_by, create_time, remark)
SELECT '操作题批改期限触发补偿', 'DEFAULT',
       'practicalGradingDeadlineTask.reconcileTriggers',
       '0 0/10 * * * ?', '2', '1', '0', 'system', NOW(),
       '补偿学生提交后的漏触发，调用同一期限领域服务'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_job
    WHERE invoke_target = 'practicalGradingDeadlineTask.reconcileTriggers'
);

-- 执行后必须复核：重复组、分母为0、无操作题期限、初始化来源和数量。
