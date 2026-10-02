-- 正式验收成功后登记，不修改学生成绩或作品。
INSERT INTO biz_platform_update
    (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.23','数据分析实验与课堂操作优化',
    '新增Python数据分析实验题，支持Excel、pandas和中文绘图，运行环境由本站提供，超时自动中止。修复课堂连续加分刷新和滚动丢失、Python题单重复添加冲突；补齐实验权限、发布快照、完成度及教师评分。优化协作保存与当前房间、退回重交对象保护、流程图空图人工批改和长文字显示、电子导学单图片编辑。保留平台监控及既有教研与学情功能。',
    NOW(),'PUBLISHED','codex',NOW()
WHERE NOT EXISTS (SELECT 1 FROM biz_platform_update WHERE version_no='1.30.23');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.23' AND status='DRAFT';
