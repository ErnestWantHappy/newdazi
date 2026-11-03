-- 仅在 1.30.58 连线布线优化验收成功后登记；不修改学生作品或成绩，可重复执行。
INSERT INTO biz_platform_update
 (version_no,title,content,published_at,status,create_by,create_time)
SELECT '1.30.58','流程图AI生成连线布线优化',
 '优化拍照生成流程图标准答案的连线画法：为每条连线自动规划正交折线并固定进出锚点（相邻步骤上下走 Z 形、同层绕行、循环回边走侧边通道），并按文字给出统一节点宽高。生成后的流程图更规整、连线不再胡乱穿插，便于老师核对微调。',
 NOW(),'PUBLISHED','AI 发布记录',NOW()
FROM DUAL WHERE NOT EXISTS(SELECT 1 FROM biz_platform_update WHERE version_no='1.30.58');
UPDATE biz_platform_update SET status='PUBLISHED',published_at=COALESCE(published_at,NOW())
WHERE version_no='1.30.58' AND title='流程图AI生成连线布线优化';
SELECT update_id,version_no,status FROM biz_platform_update WHERE version_no='1.30.58';
