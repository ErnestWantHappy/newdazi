-- 教师工具失效入口修复 v1
-- MySQL 8；可重复执行。只修正稳定来源标识对应的打字平台和网络仿真地址。

DELIMITER $$

DROP PROCEDURE IF EXISTS migrate_teacher_tool_endpoints_v1$$
CREATE PROCEDURE migrate_teacher_tool_endpoints_v1()
BEGIN
    DECLARE v_target_count INT DEFAULT 0;
    DECLARE v_unexpected_count INT DEFAULT 0;

    SELECT COUNT(*) INTO v_target_count
    FROM biz_teacher_tool
    WHERE source_type = 'LOCAL_3005'
      AND source_ref IN ('3005-typing', '3005-primary-lab', '3005-network', '3005-image-recognition');

    IF v_target_count <> 4 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '教师工具端点修复前检失败：目标来源记录不是4条';
    END IF;

    SELECT COUNT(*) INTO v_unexpected_count
    FROM biz_teacher_tool
    WHERE source_type = 'LOCAL_3005'
      AND (
          (source_ref = '3005-typing' AND url NOT IN ('http://10.52.1.92/', 'http://10.52.1.94/'))
          OR (source_ref = '3005-network' AND url NOT IN ('http://10.52.1.123:3000/', 'http://10.52.1.123:3020/'))
          OR (source_ref = '3005-primary-lab' AND url <> 'http://10.52.1.123:3003/')
          OR (source_ref = '3005-image-recognition' AND url <> 'http://10.52.1.123:3001/')
      );

    IF v_unexpected_count <> 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '教师工具端点修复前检失败：发现非预期现有地址';
    END IF;

    UPDATE biz_teacher_tool
    SET url = 'http://10.52.1.94/',
        update_by = 'system',
        update_time = NOW()
    WHERE source_type = 'LOCAL_3005'
      AND source_ref = '3005-typing'
      AND url <> 'http://10.52.1.94/';

    UPDATE biz_teacher_tool
    SET url = 'http://10.52.1.123:3020/',
        update_by = 'system',
        update_time = NOW()
    WHERE source_type = 'LOCAL_3005'
      AND source_ref = '3005-network'
      AND url <> 'http://10.52.1.123:3020/';
END$$

CALL migrate_teacher_tool_endpoints_v1()$$
DROP PROCEDURE migrate_teacher_tool_endpoints_v1$$

DELIMITER ;

-- 后检应返回4条，expected_url_match均为1，重复来源和异常地址均为0。
SELECT source_ref,
       title,
       url,
       CASE source_ref
           WHEN '3005-typing' THEN url = 'http://10.52.1.94/'
           WHEN '3005-primary-lab' THEN url = 'http://10.52.1.123:3003/'
           WHEN '3005-network' THEN url = 'http://10.52.1.123:3020/'
           WHEN '3005-image-recognition' THEN url = 'http://10.52.1.123:3001/'
           ELSE 0
       END AS expected_url_match
FROM biz_teacher_tool
WHERE source_type = 'LOCAL_3005'
  AND source_ref IN ('3005-typing', '3005-primary-lab', '3005-network', '3005-image-recognition')
ORDER BY source_ref;

SELECT COUNT(*) AS duplicate_source_count
FROM (
    SELECT source_type, source_ref
    FROM biz_teacher_tool
    WHERE source_type = 'LOCAL_3005'
      AND source_ref IN ('3005-typing', '3005-primary-lab', '3005-network', '3005-image-recognition')
    GROUP BY source_type, source_ref
    HAVING COUNT(*) > 1
) duplicate_sources;

SELECT COUNT(*) AS unexpected_url_count
FROM biz_teacher_tool
WHERE source_type = 'LOCAL_3005'
  AND source_ref IN ('3005-typing', '3005-primary-lab', '3005-network', '3005-image-recognition')
  AND CASE source_ref
      WHEN '3005-typing' THEN url <> 'http://10.52.1.94/'
      WHEN '3005-primary-lab' THEN url <> 'http://10.52.1.123:3003/'
      WHEN '3005-network' THEN url <> 'http://10.52.1.123:3020/'
      WHEN '3005-image-recognition' THEN url <> 'http://10.52.1.123:3001/'
      ELSE TRUE
  END;
