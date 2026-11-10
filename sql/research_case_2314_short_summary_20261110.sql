-- 精简顾意丽《返回成语现场》配套材料包的教师使用说明。
-- 只更新 resource_id=2314 一行；重复执行时旧说明已不存在，不会再次修改。
UPDATE biz_textbook_case_resource r
JOIN biz_textbook_case c ON c.case_id = r.case_id
SET r.requirements = '本包为“返回成语现场”课堂工具，含成语连环画创作与作品展示。解压后双击“启动课堂工具.bat”，浏览器打开学生端；教师端地址见启动窗口，需联网使用AI出图。教学设计和课件见资源列表。'
WHERE r.resource_id = 2314
  AND r.title = '10.20公开课（用这个）（配套材料包）'
  AND c.author = '顾意丽'
  AND c.status = 'PUBLISHED'
  AND CHAR_LENGTH(COALESCE(r.requirements, '')) > 500;

SELECT resource_id, CHAR_LENGTH(requirements) AS requirements_length
FROM biz_textbook_case_resource
WHERE resource_id = 2314;
