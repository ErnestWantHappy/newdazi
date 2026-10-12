-- 前端上线并通过真实教师验收后登记，不修改学生成绩或作品。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.25', '课堂班级成绩人数显示修复',
    '教师首页点击课堂后，班级选择弹窗与成绩查询一致，显示已有成绩人数和班级总人数；没有成绩时显示暂无成绩，修复已有成绩的班级仍显示暂无提交的问题。批改入口继续显示操作题提交和批改状态。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.25');

UPDATE biz_platform_update
SET status = 'PUBLISHED', published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.25' AND status = 'DRAFT';
