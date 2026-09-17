-- 小学信息科技实验板标准 MQTT 接入（2026-08-31）。
-- 按版本号幂等；正式库执行前须先完成整库备份。
INSERT INTO biz_platform_update
  (version_no, title, content, published_at, status, create_by, create_time)
SELECT
  '1.27.2',
  '小学实验板标准 MQTT 接入',
  CONCAT('小学实验板可使用平台生成的标准 MQTT Python 代码接入现有 EMQX，初中 Mind+ 链路保持兼容。\\n',
         '新增班级 Broker 精确 ACL 同步状态、失败重试和口令轮换踢线；同步未成功时不向师生下发可运行连接参数。\\n',
         '教师可复制各小组 Python 代码，学生只能复制本人小组代码；平台不保存学校 WiFi 信息。'),
  NOW(),
  'PUBLISHED',
  'AI 发布记录',
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM biz_platform_update WHERE version_no = '1.27.2'
);

SELECT update_id, version_no, status
FROM biz_platform_update
WHERE version_no = '1.27.2';
