-- 2026-10-12 130 正式 1.30.43 发布登记（教师端体验与课堂推进修复）
-- 目标库：130 正式库 ry-vue。幂等：重复执行不会产生重复记录。
-- 版本号说明：1.30.41 / 1.30.42 已于 2026-10-10 由 codex 发布占用（update_id 119/120），
--             本次顺延为 1.30.43，避免与历史记录冲突。
-- 发布方式：新建 release 20261012_audit_fix_v1（前后端同版）
--           前端零停机切换 Nginx root，后端切 unit 后重启一次（约 20 秒）。
-- 回滚：将本版本状态改回 DRAFT 即可（不改动历史记录内容）：
--   UPDATE biz_platform_update SET status='DRAFT' WHERE version_no='1.30.43';

INSERT INTO biz_platform_update (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.43',
       '教师端体验修复与课堂一键推进恢复',
       '修复操作题 HTML 源码只能看一截：源码面板改为撑满可用高度并在内部滚动，长文件可完整查看。'
       '修复操作题五星打分频繁闪烁：星级文案改为按题缓存复用，不再随重绘抖动。'
       '修复文件操作题连线提交后不可见：页图渲染分辨率由 120 提升至 150 DPI、最长边 1800 提升至 2000、'
       'JPEG 质量 0.85 提升至 0.92，细连线不失真；批改页同时新增“页图 / PDF 原图”切换，可对照原件。'
       '修复学生提交格式默认未勾选：新增题目时提交格式默认全选，教师按需取消。'
       '课程设计器指派班级新增全选、反选、清空按钮，班级较多时不再逐个点选。'
       '题库操作题的学生起始文件与补充资源支持 RAR、7Z 压缩包上传（压缩包无预览内容，直接标记为仅下载，'
       '不再因转换失败卡在 pending）。'
       '修复 Python 题模板下载报错（引用了不存在的变量）。'
       '恢复课堂一键流程推进：此前学生仅提交操作题作品、未填写文本答案时不计入已作答，'
       '班级长期达不到推进门槛导致自动与手动推进均被拒绝；现按唯一键直接统计，'
       '并放宽为“有文本框内容、有分数或有操作题作品”任一即计入。'
       '同时移除成绩查询与学情画像中的冗余子查询，减少全表聚合，页面加载更快。',
       NOW(), 'PUBLISHED', 'claude', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.43');

UPDATE biz_platform_update
SET status = 'PUBLISHED',
    published_at = COALESCE(published_at, NOW())
WHERE version_no = '1.30.43';

SELECT update_id, version_no, status, title, published_at
FROM biz_platform_update WHERE version_no = '1.30.43';
