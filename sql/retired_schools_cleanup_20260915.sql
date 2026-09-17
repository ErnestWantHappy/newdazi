-- 仅用于正式库 ry-vue；执行器须先整库备份，并在同一事务内锁定和核验四校、62 个账号及 61 条学生资料。
-- 账号与学校沿用系统逻辑删除，历史答卷归档和操作日志保留。
DELETE b FROM biz_student b JOIN sys_user u ON u.user_id=b.user_id
WHERE u.dept_id IN (117,122,146,165) AND u.del_flag='0';
DELETE FROM biz_teacher_class WHERE dept_id IN (117,122,146,165);
DELETE r FROM sys_user_role r JOIN sys_user u ON u.user_id=r.user_id
WHERE u.dept_id IN (117,122,146,165) AND u.del_flag='0';
DELETE p FROM sys_user_post p JOIN sys_user u ON u.user_id=p.user_id
WHERE u.dept_id IN (117,122,146,165) AND u.del_flag='0';
DELETE ud FROM sys_user_dept ud LEFT JOIN sys_user u ON u.user_id=ud.user_id
WHERE ud.dept_id IN (117,122,146,165) OR (u.dept_id IN (117,122,146,165) AND u.del_flag='0');
DELETE FROM sys_role_dept WHERE dept_id IN (117,122,146,165);
UPDATE sys_user SET del_flag='2',update_by='school_cleanup_20260915',update_time=NOW()
WHERE dept_id IN (117,122,146,165) AND del_flag='0';
UPDATE sys_dept SET del_flag='2',update_by='school_cleanup_20260915',update_time=NOW()
WHERE dept_id IN (117,122,146,165) AND del_flag='0';
