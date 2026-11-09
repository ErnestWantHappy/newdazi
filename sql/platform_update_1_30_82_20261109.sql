-- 仅登记 1.30.82 产品更新，不修改题库、教材、成绩或其他业务数据。
-- 可在同一正式库幂等复跑：版本记录最多一行，并确保最终状态为 PUBLISHED。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.82',
    '选择题图片与 Excel 图文导入',
    '选择题现支持题干最多 3 张图片、每个选项最多 1 张图片，可直接上传、粘贴或拖拽截图；题库列表提供“含图”标记，教师详情页和学生作答页均可查看并点击放大。题库新增 Excel 图文导入，可识别单元格内嵌图片，完成预览、修正、复核后原子确认导入。含图题被课程引用后再次编辑会自动另存，避免改动已引用题目；网络抖动时可通过保存回执找回真实题目，防止重复建题。图片使用私有目录存储，所有读取均校验登录身份和题目/课程范围，不通过静态目录公开。1.30.81 的参考答案权限、教材阅读边界、Python 运行并发和界面修复继续保留。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.82') existing_version
);

UPDATE biz_platform_update
SET title = '选择题图片与 Excel 图文导入',
    content = '选择题现支持题干最多 3 张图片、每个选项最多 1 张图片，可直接上传、粘贴或拖拽截图；题库列表提供“含图”标记，教师详情页和学生作答页均可查看并点击放大。题库新增 Excel 图文导入，可识别单元格内嵌图片，完成预览、修正、复核后原子确认导入。含图题被课程引用后再次编辑会自动另存，避免改动已引用题目；网络抖动时可通过保存回执找回真实题目，防止重复建题。图片使用私有目录存储，所有读取均校验登录身份和题目/课程范围，不通过静态目录公开。1.30.81 的参考答案权限、教材阅读边界、Python 运行并发和界面修复继续保留。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.82';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.82';
