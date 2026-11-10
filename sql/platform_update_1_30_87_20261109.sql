-- 仅登记 1.30.87 产品更新，不修改课程、题库、成绩或其他业务数据。
-- 先确认 1.30.87 未被其他发布占用；脚本可幂等复跑，但不会改写同版本的其他标题。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time, update_by, update_time)
SELECT
    '1.30.87',
    '高中教材与课程标准上线',
    '电子教材与教学资源新增高中教材分组，上架必修1《数据与计算》、必修2《信息系统与社会》、选择性必修1《数据与数据结构》3册，共539页，支持逐页阅读、课次目录和整册PDF下载。另新增“课程标准”资料区，提供义务教育信息科技课程标准（2022年版）和普通高中信息科技课程标准（2017年版2025年修订）的在线逐页预览与PDF下载。原有小学、初中13册教材和教研活动课例保持不变；课程标准和高中教材面向教师、教研员和管理员开放，学生不显示该资料区。',
    NOW(),
    'PUBLISHED',
    'admin',
    NOW(),
    'admin',
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM (SELECT version_no FROM biz_platform_update WHERE version_no = '1.30.87') existing_version
);

UPDATE biz_platform_update
SET content = '电子教材与教学资源新增高中教材分组，上架必修1《数据与计算》、必修2《信息系统与社会》、选择性必修1《数据与数据结构》3册，共539页，支持逐页阅读、课次目录和整册PDF下载。另新增“课程标准”资料区，提供义务教育信息科技课程标准（2022年版）和普通高中信息科技课程标准（2017年版2025年修订）的在线逐页预览与PDF下载。原有小学、初中13册教材和教研活动课例保持不变；课程标准和高中教材面向教师、教研员和管理员开放，学生不显示该资料区。',
    status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW()),
    update_by = 'admin',
    update_time = NOW()
WHERE version_no = '1.30.87'
  AND title = '高中教材与课程标准上线';

SELECT update_id, version_no, title, status, published_at
FROM biz_platform_update
WHERE version_no = '1.30.87';
