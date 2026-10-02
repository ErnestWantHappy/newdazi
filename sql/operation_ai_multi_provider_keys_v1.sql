-- 教师视觉模型凭据按厂商独立保存；先执行本脚本再启动新代码。
CREATE TABLE IF NOT EXISTS biz_teacher_ai_provider_key (
    teacher_user_id BIGINT NOT NULL COMMENT '教师用户ID',
    provider_code VARCHAR(32) NOT NULL COMMENT '模型厂商',
    api_key_ciphertext TEXT NOT NULL COMMENT 'AES-GCM 密文，仅服务端读取',
    api_key_hint VARCHAR(16) NOT NULL COMMENT '末四位掩码',
    model_name VARCHAR(80) NOT NULL COMMENT '该厂商上次选用模型',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (teacher_user_id, provider_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师各模型厂商独立凭据';

-- 迁入原先正在使用的配置；重复执行时保留新表中后来更新的 Key。
INSERT IGNORE INTO biz_teacher_ai_provider_key
  (teacher_user_id,provider_code,api_key_ciphertext,api_key_hint,model_name,create_time,update_time)
SELECT teacher_user_id,provider_code,api_key_ciphertext,api_key_hint,model_name,create_time,update_time
FROM biz_teacher_ai_config
WHERE api_key_ciphertext IS NOT NULL AND api_key_ciphertext <> '';
