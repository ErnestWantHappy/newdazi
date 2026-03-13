UPDATE biz_lesson bl
INNER JOIN sys_user su ON bl.create_by = su.user_name
INNER JOIN sys_user_dept sud ON su.user_id = sud.user_id
INNER JOIN sys_dept target ON sud.dept_id = target.dept_id
    AND target.school_type = (
        CASE 
            WHEN bl.grade >= 1 AND bl.grade <= 6 THEN '1'
            WHEN bl.grade >= 7 AND bl.grade <= 9 THEN '2'
            WHEN bl.grade >= 10 THEN '3'
        END
    )
SET bl.dept_id = target.dept_id, bl.creator_id = su.user_id;
