-- 仅登记 1.30.83 产品更新，不修改课程、题库、成绩或其他业务数据。
-- 可在正式库幂等复跑：版本记录最多一行，并确保最终状态为 PUBLISHED。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.83',
    '课程设计页布局调整',
    '教师从首页新建课程进入课程设计页时，课程内容设计改为在上、教学资源库在下，宽屏也不再左右并排，填写课程信息和选题的顺序更符合实际操作。教学资源库、题目筛选、已选题目和保存流程保持不变；帮助教程中的操作说明已同步更新。本次仅调整前端页面布局，不改变课程、题目、成绩或权限规则。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.83') existing_version
);

UPDATE biz_platform_update
SET title = '课程设计页布局调整',
    content = '教师从首页新建课程进入课程设计页时，课程内容设计改为在上、教学资源库在下，宽屏也不再左右并排，填写课程信息和选题的顺序更符合实际操作。教学资源库、题目筛选、已选题目和保存流程保持不变；帮助教程中的操作说明已同步更新。本次仅调整前端页面布局，不改变课程、题目、成绩或权限规则。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.83';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.83';
