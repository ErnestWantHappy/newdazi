-- Python、课程开放与物联课堂体验优化发布记录（2026-08-24）。
-- 按版本号幂等；正式库执行前须先完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.26.1',
  'Python 与物联课堂体验优化',
  CONCAT('修复教研员进入 Python 刷题页面时因机构未配置学段而报错的问题。\n',
         'Python 练习新增上一题、下一题、自动保存草稿和清晰的排队判题提示，修复提交结果显示不完整。\n',
         '课程设计可设置新班级的理论题与操作题初始开放状态，已有班级课堂状态不会被覆盖；成绩页开关更易找到。\n',
         '物联入口先选择班级，小组数据支持筛选、排序和弹窗查看，实时状态与数据格式显示更准确。\n',
         '在线协作可在新课程第一次保存时直接完成配置。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.26.1'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.26.1';
