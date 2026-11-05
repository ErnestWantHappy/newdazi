-- 130 正式发布登记：2026-11-05，1.30.72；验收通过后执行，可重复执行。
-- 仅维护本版本的平台更新记录，不修改课程、学生、作业或成绩。
INSERT INTO biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.72', '题库工作台改版与操作题 Python 运行',
    CONCAT(
        '题库工作台按题型分区排列，筛选默认展开；批量完成、编辑、删除按操作分名，各自校验权限，分页或重新查询后清空勾选。\n',
        '教师题库的操作题可上传 PY 文件，批改时可填写标准输入并直接运行，用于核对学生程序结果；运行不写成绩、不改学生代码。\n',
        '成绩导出与页面的优秀/良好/及格等级、赋分口径一致；名单统一排除停用账号。教师批改时名单加载失败会保留已填分数并提示重新加载。\n',
        '课程归属校与当前学校不一致时，学生首页不再显示他校课程；单题删除、附件读取按题目创建者和可读范围校验。'
    ), NOW(), 'PUBLISHED', 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT version_no FROM biz_platform_update WHERE version_no='1.30.72') AS existing_version
);
UPDATE biz_platform_update
SET status='PUBLISHED', published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.72' AND status<>'PUBLISHED';
SELECT update_id,version_no,title,status,published_at FROM biz_platform_update WHERE version_no='1.30.72';
