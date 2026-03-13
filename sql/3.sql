UPDATE `biz_lesson` bl 
INNER JOIN `sys_user` su ON bl.create_by = su.user_name 
SET bl.dept_id = su.dept_id,
    bl.creator_id = su.user_id
WHERE bl.dept_id IS NULL OR bl.creator_id IS NULL;
