-- 2026-10-13 130 正式 1.30.46 发布登记。
-- 前提：Python 零基础 V3 题包已导入并通过 Judge0 340/340；前后端 release 已切换验收。
-- 幂等：重复执行不新增重复版本。无表结构迁移。
USE `ry-vue`;
START TRANSACTION;
INSERT INTO biz_platform_update
    (version_no,title,content,published_at,status,create_by,create_time,update_by,update_time)
SELECT '1.30.46','Python 零基础题库与作业预览体验升级',
       '<p>新增 100 道适合小学五、六年级零基础学习的 Python 编程题，覆盖输出、变量、输入、条件判断、循环和教材趣味案例，并完成自动判题校验。</p><p>教师选择 Python 题目时可按出题人、标题、知识点和难度筛选；系统自动推荐同时支持新版题库。</p><p>HTML 作业源码区提高默认可视高度、支持内部滚动和放大查看；本版本同时包含连线题页图兼容修复。</p>',
       NOW(),'PUBLISHED','codex',NOW(),'codex',NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no='1.30.46');
UPDATE biz_platform_update
SET status='PUBLISHED',published_at=COALESCE(published_at,NOW()),update_by='codex',update_time=NOW()
WHERE version_no='1.30.46';
SELECT update_id,version_no,title,status,published_at
FROM biz_platform_update WHERE version_no='1.30.46';
COMMIT;
