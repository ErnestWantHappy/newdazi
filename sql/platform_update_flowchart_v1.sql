-- 画程流程图操作题首期发布（2026-08-31）。
-- 按版本号幂等；正式库执行前须先完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.27.0',
  '新增画程流程图操作题',
  CONCAT('平台内置适合小学生使用的“画程”，教师可直接制作标准答案和学生基础图，并可一键从标准答案复制基础图。\n',
         '学生支持单击或拖拽添加节点、删除节点和连线、自动保存草稿，并在完成后明确提交。\n',
         '开始、结束节点已分开内置，输入/输出节点文字居中，连接点位于上下左右四条边的正中间。\n',
         '提交后提供结构规则检查建议，正式成绩仍由教师确认。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.27.0'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.27.0';
