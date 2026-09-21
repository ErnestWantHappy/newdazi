-- 仅登记更新草稿，不自动通知教师，不修改学生成绩或表结构。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.13', '纸质导入与 AI 批改修复',
    '纸质PDF按当前班级核对学生，复用原作品批改流程；先逐空识别再评分，拦截已识别空白却满分的建议，待核对建议可由教师确认后批量采用；修复AI任务创建失败、已有Key切换模型无法保存；默认Qwen3.8-Max，增加0902选项和官网参考价，费用统一以元展示。AI分数仍须教师核对确认。',
    NOW(), 'DRAFT', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.13');
