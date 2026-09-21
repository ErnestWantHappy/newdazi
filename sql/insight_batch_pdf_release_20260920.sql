-- 正式验收成功后登记；不修改学生、作品或成绩。
INSERT INTO biz_platform_update
    (version_no, title, content, published_at, status, create_by, create_time)
SELECT '1.30.19', '整班纸质作业导入与AI学情分析优化',
    '整班合并PDF支持按学生页段核对后拆分导入。AI学情分析复用已保存的批改原答和正式成绩，试卷知识点统一解析、学生文本分批诊断，减少重复识别；显示处理进度、耗时、费用和取消入口。修复参考答案无争议误判、局部证据错误导致整份诊断丢弃及进度轮询冲突。未确认或证据不足仍提示教师核对，不自动改动正式成绩。',
    NOW(), 'PUBLISHED', 'codex', NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no = '1.30.19');
UPDATE biz_platform_update SET status='PUBLISHED', published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.19' AND status='DRAFT';
