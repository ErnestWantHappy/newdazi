-- 隐藏框架自带的“若依官网”菜单，并清理历史角色授权。
-- 本脚本可重复执行；目标库执行前仍需按发布流程完成整库备份。
START TRANSACTION;

UPDATE sys_menu
SET visible = '1',
    update_by = 'system',
    update_time = NOW()
WHERE menu_id = 4;

DELETE FROM sys_role_menu
WHERE menu_id = 4;

COMMIT;

-- 后检：visible=1 表示隐藏，role_grant_count 必须为 0。
SELECT menu_id, menu_name, visible
FROM sys_menu
WHERE menu_id = 4;

SELECT COUNT(*) AS role_grant_count
FROM sys_role_menu
WHERE menu_id = 4;
