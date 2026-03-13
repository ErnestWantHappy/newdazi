-- 通过学校代码 (school_code) 和课程所在的年级 (grade) 智能分配正确的校区 dept_id
-- 解决一校两部（小学部、初中部）教师的跨学部课程挂载问题

UPDATE `biz_lesson` bl
INNER JOIN `sys_user` su ON bl.create_by = su.user_name
INNER JOIN `sys_dept` user_dept ON su.dept_id = user_dept.dept_id
INNER JOIN `sys_dept` target_dept ON user_dept.school_code = target_dept.school_code 
    AND user_dept.school_code IS NOT NULL AND user_dept.school_code != ''
    AND target_dept.school_type = (
        CASE 
            WHEN bl.grade >= 1 AND bl.grade <= 6 THEN '1'   -- 1至6年级归属于小学部 (school_type = 1)
            WHEN bl.grade >= 7 AND bl.grade <= 9 THEN '2'   -- 7至9年级归属于初中部 (school_type = 2)
            WHEN bl.grade >= 10 THEN '3'                    -- 10年级以上归属于高中部 (school_type = 3)
            ELSE user_dept.school_type                      -- 异常情况退化为老师当前学段
        END
    )
SET 
    bl.dept_id = target_dept.dept_id,
    bl.creator_id = su.user_id
WHERE bl.dept_id IS NULL OR bl.dept_id = su.dept_id;

-- 【如果你也需要刷新曾经基于错误 SQL 回填为单一学部的课程】：
-- 上面的 `OR bl.dept_id = su.dept_id` 已经涵盖了洗数据逻辑，可以直接重新执行。

