-- 仅在正式部署、定向重算和真实教师验收通过后登记。
INSERT INTO biz_platform_update
 (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.51','操作作品批改计时与PPT预览修复',
 '操作题批改改为班级有效操作作品提交人数达到一半后起算21天，打字和理论答题不再提前触发；批改页分别展示整课参与人数和操作作品提交人数。改善PPTX流程图预览中的折线走向和单行输入输出文字显示，学生原始文件和已有成绩保持不变。',
 NOW(),'PUBLISHED','AI 发布记录',NOW()
FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.51');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.51' AND title='操作作品批改计时与PPT预览修复';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.51';
