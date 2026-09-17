-- 1.30.0 在线协作大改发布记录（2026-09-05）。
-- 仅登记平台更新，不变更业务表结构；按版本号幂等。
-- 同版数据治理（已执行，有备份）：368 精简至 1 道操作题、373 精简至 1 道文件题，316 存量 2 道保留；
-- 备份见服务器 D:\program\3009dazipingtai\backups\20250905_1300_onepractical_audit\。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.30.0',
  '在线协作大改：一课一道操作题与协作独立',
  '一课最多一道操作题（前后端校验，316存量保留）；协作开关独立，起始文件只来自题库；协作页按学号自动分组冻结；教师房间列表显示组名人数成员与进入按钮；学生单房间自动进入本组。后端453单测通过，无增量SQL。',
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.0');

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.30.0';
