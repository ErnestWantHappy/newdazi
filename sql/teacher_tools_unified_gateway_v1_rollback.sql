-- 教师工具统一入口回滚脚本。
-- 仅在恢复旧 Nginx/端口访问方式后执行；执行前仍须保留正式库备份。
UPDATE biz_teacher_tool
SET url = CASE source_ref
        WHEN '3005-mail' THEN 'http://10.52.1.123:3002/'
        WHEN '3005-primary-lab' THEN 'http://10.52.1.123:3003/'
        WHEN '3005-network' THEN 'http://10.52.1.123:3020/'
        WHEN '3005-iot-flow' THEN 'http://10.52.1.123:3006/'
        WHEN '3005-image-recognition' THEN 'http://10.52.1.123:3001/'
    END,
    update_by = 'system',
    update_time = NOW()
WHERE source_type = 'LOCAL_3005'
  AND source_ref IN (
      '3005-mail',
      '3005-primary-lab',
      '3005-network',
      '3005-iot-flow',
      '3005-image-recognition'
  )
  AND url IN (
      'http://10.52.1.123/tools/mail/',
      'http://10.52.1.123/tools/primary-lab/',
      'http://10.52.1.123/tools/network/',
      'http://10.52.1.123/tools/iot-data/',
      'http://10.52.1.123/tools/image-recognition/'
  );

DELETE FROM biz_platform_update
WHERE version_no = '1.27.1'
  AND title = '教师工具统一入口与服务稳定性提升'
  AND create_by = 'AI 发布记录';
