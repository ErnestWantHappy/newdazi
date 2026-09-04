-- 教师集中反馈与学生导入诊断治理发布记录（2026-09-01）。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.27.8',
  '优化教师常用功能与系统诊断',
  CONCAT('修复课堂表现分按所选字段导出；课程设计选题列表增加出题人；',
         '优化教师首页课程增删反馈；题库操作列固定；学生管理增加导出；',
         '批改页明确未提交状态；学生批量导入增加分阶段校验、并发锁、耗时统计与联合索引；',
         '诊断中心区分业务提示、慢接口与系统异常。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.27.8'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.27.8';
