-- 修复：教师角色虽已关联「课程管理」菜单，但菜单 status=1 导致 selectMenuPerms 过滤掉 business:lesson:list
-- 幂等：仅当 perms 与预期一致时启用；可重复执行
-- 回滚：UPDATE sys_menu SET status = '1' WHERE menu_id = 2006 AND perms = 'business:lesson:list';

UPDATE sys_menu
SET status = '0',
    update_by = 'system',
    update_time = NOW()
WHERE menu_id = 2006
  AND perms = 'business:lesson:list'
  AND status <> '0';

-- 兜底：若环境 menu_id 不同，按 perms + 课程管理 名称启用
UPDATE sys_menu
SET status = '0',
    update_by = 'system',
    update_time = NOW()
WHERE perms = 'business:lesson:list'
  AND status <> '0';
