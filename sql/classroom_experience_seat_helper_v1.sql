CREATE TABLE IF NOT EXISTS biz_seat_helper_school (
 dept_id BIGINT PRIMARY KEY, enabled TINYINT NOT NULL DEFAULT 0,
 platform_url VARCHAR(255) NOT NULL, updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS biz_seat_helper_device (
 device_id CHAR(32) PRIMARY KEY, dept_id BIGINT NOT NULL, room_name VARCHAR(80) NOT NULL,
 seat_name VARCHAR(40) NOT NULL, credential_hash CHAR(64) NOT NULL, fingerprint CHAR(64) NOT NULL,
 active TINYINT NOT NULL DEFAULT 1, registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 revoked_at DATETIME NULL, KEY idx_seat_device_school(dept_id,active),
 UNIQUE KEY uk_seat_device_place(dept_id,room_name,seat_name,device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS biz_seat_helper_binding (
 dept_id BIGINT NOT NULL, room_name VARCHAR(80) NOT NULL, entry_year VARCHAR(10) NOT NULL,
 class_code VARCHAR(20) NOT NULL, student_id BIGINT NOT NULL, device_id CHAR(32) NOT NULL,
 confirmed_by BIGINT NOT NULL, confirmed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(dept_id,room_name,entry_year,class_code,student_id),
 KEY idx_seat_binding_device(dept_id,room_name,entry_year,class_code,device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 菜单仅授予管理员；客户端下载入口由学校开关控制。
SET @parent=(SELECT menu_id FROM sys_menu WHERE path='business' ORDER BY menu_id LIMIT 1);
SET @parent=IFNULL(@parent,0);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '机位助手',@parent,90,'seatHelper','business/seatHelper/index',1,0,'C','0','0','business:seat-helper:admin','monitor','admin',NOW()
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='business:seat-helper:admin');
INSERT INTO sys_role_menu(role_id,menu_id)
SELECT r.role_id,m.menu_id FROM sys_role r JOIN sys_menu m ON m.perms='business:seat-helper:admin'
WHERE r.role_key='admin' AND NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id);
