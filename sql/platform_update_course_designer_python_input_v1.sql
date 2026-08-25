-- 课程设计器与课程内 Python 输入体验优化（2026-08-24）。
-- 按版本号幂等；正式库执行前须先完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.26.2',
  '课程设计与 Python 输入体验优化',
  CONCAT('课程设计器压缩布局与间距，电子导学单改为紧凑配置，常用操作无需横向滚动。\n',
         '物联网与在线协作开关集中展示；未选择带起始文件的文件作品题时，会明确提示且不能开启协作。\n',
         '课程内 Python 输入输出题新增自定义输入和自定义运行，可直接查看实际输出；自定义运行不计分、不覆盖课程答案。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.26.2'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.26.2';
