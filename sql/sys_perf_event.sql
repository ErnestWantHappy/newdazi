-- 系统性能事件表：持久化慢 SQL、慢接口、异常操作
CREATE TABLE IF NOT EXISTS sys_perf_event (
  event_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  event_type     VARCHAR(32)  NOT NULL COMMENT 'slow_sql | slow_api | error_api',
  severity       VARCHAR(16)  NOT NULL COMMENT 'warning | critical',
  title          VARCHAR(200) NOT NULL COMMENT '中文简述',
  description    VARCHAR(500) DEFAULT NULL COMMENT '业务说明',
  source_name    VARCHAR(300) DEFAULT NULL COMMENT 'Mapper ID 或操作模块',
  source_url     VARCHAR(500) DEFAULT NULL COMMENT '请求地址',
  sql_text       TEXT         DEFAULT NULL COMMENT 'SQL 文本',
  sql_hash       VARCHAR(64)  DEFAULT NULL COMMENT 'SQL 哈希',
  duration_ms    BIGINT       NOT NULL DEFAULT 0 COMMENT '耗时毫秒',
  error_msg      VARCHAR(2000) DEFAULT NULL COMMENT '错误消息',
  oper_name      VARCHAR(64)  DEFAULT NULL COMMENT '操作用户',
  dept_name      VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
  occur_time     DATETIME     NOT NULL COMMENT '发生时间',
  create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '写入时间',
  PRIMARY KEY (event_id),
  KEY idx_occur_time (occur_time),
  KEY idx_event_type (event_type),
  KEY idx_sql_hash (sql_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统性能事件';
