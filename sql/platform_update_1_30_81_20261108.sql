-- 仅登记 1.30.81 产品更新，不修改题库、教材、成绩或其他业务数据。
-- 可在同一正式库幂等复跑：版本记录最多一行，并确保最终状态为 PUBLISHED。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.81',
    '参考答案权限、教材阅读边界与课堂运行稳定性修复',
    '普通教师查看他人 Python 操作题时，参考答案和解析不再显示；题目创建者与管理员仍可正常查看。学生现在只能阅读当前课程绑定的教材，跨教材或未绑定教材的访问会被拦截，教师和教研员仍可阅读整册教材。Python 在线运行并发由 2 提升到 8，并允许短暂排队，降低课堂集中运行时的“服务繁忙”。一年级教学指南尚未上架时改为明确说明；操作题四种模式的题量支持单项失败隔离和局部重试；教学设计汇编关闭重开后恢复上次页码；教师工具修复筛选重复文案和窄屏收藏按钮挤压标题；成绩页修复前端导入顺序。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.81') existing_version
);

UPDATE biz_platform_update
SET title = '参考答案权限、教材阅读边界与课堂运行稳定性修复',
    content = '普通教师查看他人 Python 操作题时，参考答案和解析不再显示；题目创建者与管理员仍可正常查看。学生现在只能阅读当前课程绑定的教材，跨教材或未绑定教材的访问会被拦截，教师和教研员仍可阅读整册教材。Python 在线运行并发由 2 提升到 8，并允许短暂排队，降低课堂集中运行时的“服务繁忙”。一年级教学指南尚未上架时改为明确说明；操作题四种模式的题量支持单项失败隔离和局部重试；教学设计汇编关闭重开后恢复上次页码；教师工具修复筛选重复文案和窄屏收藏按钮挤压标题；成绩页修复前端导入顺序。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.81';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.81';
