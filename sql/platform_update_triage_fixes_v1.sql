-- 1.29.8 分诊四项修复发布记录（2026-09-05）。
-- 仅登记平台更新，不变更业务表结构与业务数据；按版本号幂等。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.29.8',
  '批改进度口径、课堂只读、设计器与协作向导修复',
  '批改页整课口径与当前操作题口径分离并加注，未提交名单标未进入；课堂未指派班级返回历史只读不再报错；课程设计器缺失引用导致空白已修复；协作页三步向导（开协作/冻结分组/建活动）。后端 448 单测通过，无增量 SQL。',
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.29.8');

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.29.8';
