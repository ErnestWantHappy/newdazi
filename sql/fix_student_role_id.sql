-- 将 biz_student 对应用户的错误 role_id=4 修正为 101（学生角色）
-- 执行前可先预览：SELECT ur.user_id, ur.role_id FROM sys_user_role ur INNER JOIN biz_student s ON ur.user_id = s.user_id WHERE ur.role_id = 4;

UPDATE sys_user_role ur
INNER JOIN biz_student s ON ur.user_id = s.user_id
SET ur.role_id = 101
WHERE ur.role_id = 4;
