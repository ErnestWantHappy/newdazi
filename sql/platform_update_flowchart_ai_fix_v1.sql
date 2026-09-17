-- 仅登记本次流程图 AI 批改热修，无业务表结构变更。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.7', '流程图 AI 批改建议回显与采用修复',
  '修复流程图 AI 建议分、总评和置信度不显示、刷新后建议不恢复及批量采用不可用；无评分项时容忍模型冗余分项，仍严格校验总分；修复批改页评分框聚焦异常。AI 仅生成建议，正式成绩仍由教师复核采用。',
  NOW(), 'PUBLISHED', 'AI 发布记录', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.7');

SELECT update_id, version_no, status FROM biz_platform_update WHERE version_no = '1.30.7';
