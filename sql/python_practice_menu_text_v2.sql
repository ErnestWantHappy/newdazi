-- Python 刷题菜单文字修复：幂等更新，修复历史发布中出现的问号乱码。
UPDATE sys_menu
SET menu_name = CASE perms
  WHEN 'business:pythonPractice:query' THEN 'Python刷题'
  WHEN 'business:pythonPractice:edit' THEN 'Python刷题配置'
  WHEN 'business:pythonPractice:publish' THEN 'Python刷题发布'
  WHEN 'business:pythonPractice:analytics' THEN 'Python刷题学情'
  ELSE menu_name
END
WHERE perms IN (
  'business:pythonPractice:query',
  'business:pythonPractice:edit',
  'business:pythonPractice:publish',
  'business:pythonPractice:analytics'
);

-- 执行后复核：上述四个权限对应的菜单名应全部为正常中文。
SELECT menu_id, menu_name, HEX(menu_name) AS menu_name_hex, perms, path
FROM sys_menu
WHERE perms LIKE 'business:pythonPractice:%'
ORDER BY menu_id;
