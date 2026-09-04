-- 教师工具统一 80 端口路径入口（2026-08-31）。
-- 正式库执行前必须完成整库备份；只允许更新 5 个已确认的 LOCAL_3005 来源。

DELIMITER $$

DROP PROCEDURE IF EXISTS migrate_teacher_tool_gateway_v1$$
CREATE PROCEDURE migrate_teacher_tool_gateway_v1()
BEGIN
    DECLARE v_target_count INT DEFAULT 0;
    DECLARE v_unexpected_count INT DEFAULT 0;

    SELECT COUNT(*) INTO v_target_count
    FROM biz_teacher_tool
    WHERE source_type = 'LOCAL_3005'
      AND source_ref IN (
          '3005-mail',
          '3005-primary-lab',
          '3005-network',
          '3005-iot-flow',
          '3005-image-recognition'
      );

    IF v_target_count <> 5 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '教师工具统一入口前检失败：目标来源记录不是5条';
    END IF;

    SELECT COUNT(*) INTO v_unexpected_count
    FROM biz_teacher_tool
    WHERE source_type = 'LOCAL_3005'
      AND source_ref IN (
          '3005-mail',
          '3005-primary-lab',
          '3005-network',
          '3005-iot-flow',
          '3005-image-recognition'
      )
      AND CASE source_ref
          WHEN '3005-mail' THEN url NOT IN (
              'http://10.52.1.123:3002/',
              'http://10.52.1.123/tools/mail/'
          )
          WHEN '3005-primary-lab' THEN url NOT IN (
              'http://10.52.1.123:3003/',
              'http://10.52.1.123/tools/primary-lab/'
          )
          WHEN '3005-network' THEN url NOT IN (
              'http://10.52.1.123:3020/',
              'http://10.52.1.123/tools/network/'
          )
          WHEN '3005-iot-flow' THEN url NOT IN (
              'http://10.52.1.123:3006/',
              'http://10.52.1.123/tools/iot-data/'
          )
          WHEN '3005-image-recognition' THEN url NOT IN (
              'http://10.52.1.123:3001/',
              'http://10.52.1.123/tools/image-recognition/'
          )
          ELSE TRUE
      END;

    IF v_unexpected_count <> 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '教师工具统一入口前检失败：发现非预期现有地址';
    END IF;

    UPDATE biz_teacher_tool
    SET url = CASE source_ref
            WHEN '3005-mail' THEN 'http://10.52.1.123/tools/mail/'
            WHEN '3005-primary-lab' THEN 'http://10.52.1.123/tools/primary-lab/'
            WHEN '3005-network' THEN 'http://10.52.1.123/tools/network/'
            WHEN '3005-iot-flow' THEN 'http://10.52.1.123/tools/iot-data/'
            WHEN '3005-image-recognition' THEN 'http://10.52.1.123/tools/image-recognition/'
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
      );
END$$

CALL migrate_teacher_tool_gateway_v1()$$
DROP PROCEDURE migrate_teacher_tool_gateway_v1$$

DELIMITER ;

-- 后检应返回 5 条，expected_url_match 均为 1；重复来源和异常地址均为 0。
SELECT source_ref,
       title,
       url,
       CASE source_ref
           WHEN '3005-mail' THEN url = 'http://10.52.1.123/tools/mail/'
           WHEN '3005-primary-lab' THEN url = 'http://10.52.1.123/tools/primary-lab/'
           WHEN '3005-network' THEN url = 'http://10.52.1.123/tools/network/'
           WHEN '3005-iot-flow' THEN url = 'http://10.52.1.123/tools/iot-data/'
           WHEN '3005-image-recognition' THEN url = 'http://10.52.1.123/tools/image-recognition/'
           ELSE 0
       END AS expected_url_match
FROM biz_teacher_tool
WHERE source_type = 'LOCAL_3005'
  AND source_ref IN (
      '3005-mail',
      '3005-primary-lab',
      '3005-network',
      '3005-iot-flow',
      '3005-image-recognition'
  )
ORDER BY source_ref;

SELECT COUNT(*) AS duplicate_source_count
FROM (
    SELECT source_type, source_ref
    FROM biz_teacher_tool
    WHERE source_type = 'LOCAL_3005'
      AND source_ref IN (
          '3005-mail',
          '3005-primary-lab',
          '3005-network',
          '3005-iot-flow',
          '3005-image-recognition'
      )
    GROUP BY source_type, source_ref
    HAVING COUNT(*) > 1
) duplicate_sources;

SELECT COUNT(*) AS unexpected_url_count
FROM biz_teacher_tool
WHERE source_type = 'LOCAL_3005'
  AND source_ref IN (
      '3005-mail',
      '3005-primary-lab',
      '3005-network',
      '3005-iot-flow',
      '3005-image-recognition'
  )
  AND CASE source_ref
      WHEN '3005-mail' THEN url <> 'http://10.52.1.123/tools/mail/'
      WHEN '3005-primary-lab' THEN url <> 'http://10.52.1.123/tools/primary-lab/'
      WHEN '3005-network' THEN url <> 'http://10.52.1.123/tools/network/'
      WHEN '3005-iot-flow' THEN url <> 'http://10.52.1.123/tools/iot-data/'
      WHEN '3005-image-recognition' THEN url <> 'http://10.52.1.123/tools/image-recognition/'
      ELSE TRUE
  END;
