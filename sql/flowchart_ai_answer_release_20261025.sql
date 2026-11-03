-- 仅在 1.30.56 发布验收成功后登记；不修改学生作品或成绩，可重复执行。
INSERT INTO biz_platform_update
 (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.56','画流程图题支持拍照AI生成标准答案',
 '题库新增“画流程图”操作题标准答案的智能录入：老师在标准答案页签上传一张流程图照片，系统结合本题的名称、年级、学期和第几课，调用已配置的视觉模型自动识别流程结构并排好版，直接填进画布。老师核对微调后保存即可，省时省力。识别结果始终为草稿，需老师确认后才写入，不自动改动任何成绩或既有题目。',
 NOW(),'PUBLISHED','AI 发布记录',NOW()
FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.56');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.56' AND title='画流程图题支持拍照AI生成标准答案';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.56';
