-- 电子教材模块 v1（2026-10-29）
-- 用法：在目标库执行本脚本建表 + 菜单 + 角色授权，幂等可重复执行。
--       教材页图与目录数据由 sql/textbook_seed_20261029.sql 写入。
-- 说明：不建课程↔教材绑定表，教师在阅读器里手动选课次；页面图不走 /profile，由后端鉴权后按 ETag 返回。

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 1. 册
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_textbook_book` (
  `book_id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '册ID',
  `book_code`     VARCHAR(32)  NOT NULL                COMMENT '册编码，如 xxkj-7-0-2026',
  `title`         VARCHAR(120) NOT NULL                COMMENT '书名全称',
  `short_title`   VARCHAR(40)  NOT NULL                COMMENT '短名，如 七上',
  `grade`         TINYINT      NOT NULL                COMMENT '年级 1-9',
  `semester`      CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '学期：0上册 1下册 9全一册',
  `publisher`     VARCHAR(80)  DEFAULT NULL            COMMENT '出版社',
  `edition`       VARCHAR(20)  DEFAULT NULL            COMMENT '版次年份',
  `total_pages`   INT          NOT NULL                COMMENT 'PDF 总页数',
  `page_offset`   INT          NOT NULL DEFAULT 0      COMMENT '印刷页 = PDF 页 - 该偏移（分段册以起点段为准）',
  `status`        CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态：0上架 1下架',
  `sort_no`       INT          NOT NULL DEFAULT 0      COMMENT '书架排序',
  `source_file`   VARCHAR(255) DEFAULT NULL            COMMENT '源 PDF 文件名',
  `source_sha256` CHAR(64)     DEFAULT NULL            COMMENT '源 PDF 哈希',
  `imported_at`   DATETIME     DEFAULT NULL            COMMENT '导入时间',
  `create_time`   DATETIME     DEFAULT NULL,
  `update_time`   DATETIME     DEFAULT NULL,
  PRIMARY KEY (`book_id`),
  UNIQUE KEY `uk_textbook_book_code` (`book_code`),
  KEY `idx_textbook_book_grade` (`status`, `grade`, `semester`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='电子教材-册';

-- ---------------------------------------------------------------------------
-- 2. 单元
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_textbook_unit` (
  `unit_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '单元ID',
  `book_id`     BIGINT       NOT NULL                COMMENT '册ID',
  `unit_no`     INT          NOT NULL                COMMENT '单元序号（册内）',
  `title`       VARCHAR(120) NOT NULL                COMMENT '单元标题',
  `sort_no`     INT          NOT NULL DEFAULT 0      COMMENT '册内排序',
  `create_time` DATETIME     DEFAULT NULL,
  `update_time` DATETIME     DEFAULT NULL,
  PRIMARY KEY (`unit_id`),
  UNIQUE KEY `uk_textbook_unit_no` (`book_id`, `unit_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='电子教材-单元';

-- ---------------------------------------------------------------------------
-- 3. 条目（前置 / 单元导入 / 课次 / 单元小结 / 附录）
--    section_key 是幂等键：front、unit-2-intro、lesson-6、summary-2、appendix
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_textbook_section` (
  `section_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '条目ID',
  `book_id`       BIGINT       NOT NULL                COMMENT '册ID',
  `unit_id`       BIGINT       DEFAULT NULL            COMMENT '单元ID，前置与附录为空',
  `section_key`   VARCHAR(32)  NOT NULL                COMMENT '册内稳定键',
  `kind`          VARCHAR(12)  NOT NULL                COMMENT 'FRONT/UNIT_INTRO/LESSON/SUMMARY/APPENDIX',
  `lesson_no`     INT          DEFAULT NULL            COMMENT '课次序号，仅 LESSON',
  `title`         VARCHAR(160) NOT NULL                COMMENT '条目标题',
  `printed_start` INT          DEFAULT NULL            COMMENT '起始印刷页',
  `printed_end`   INT          DEFAULT NULL            COMMENT '结束印刷页',
  `pdf_start`     INT          NOT NULL                COMMENT '起始 PDF 页',
  `pdf_end`       INT          NOT NULL                COMMENT '结束 PDF 页',
  `sort_no`       INT          NOT NULL DEFAULT 0      COMMENT '册内排序',
  `create_time`   DATETIME     DEFAULT NULL,
  `update_time`   DATETIME     DEFAULT NULL,
  PRIMARY KEY (`section_id`),
  UNIQUE KEY `uk_textbook_section_key` (`book_id`, `section_key`),
  KEY `idx_textbook_section_pdf` (`book_id`, `pdf_start`),
  KEY `idx_textbook_section_lesson` (`book_id`, `kind`, `lesson_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='电子教材-目录条目';

-- ---------------------------------------------------------------------------
-- 4. 页
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_textbook_page` (
  `page_id`     BIGINT      NOT NULL AUTO_INCREMENT COMMENT '页ID',
  `book_id`     BIGINT      NOT NULL                COMMENT '册ID',
  `page_no`     INT         NOT NULL                COMMENT 'PDF 页码（从 1 开始）',
  `printed_no`  INT         DEFAULT NULL            COMMENT '页脚印刷页码，前置页为空',
  `image_path`  VARCHAR(120) NOT NULL               COMMENT '相对教材根目录的图片路径',
  `width`       INT         NOT NULL                COMMENT '像素宽',
  `height`      INT         NOT NULL                COMMENT '像素高',
  `bytes`       INT         NOT NULL                COMMENT '文件字节数',
  `sha256`      CHAR(64)    NOT NULL                COMMENT '内容哈希，用作 ETag',
  `create_time` DATETIME    DEFAULT NULL,
  PRIMARY KEY (`page_id`),
  UNIQUE KEY `uk_textbook_page_no` (`book_id`, `page_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='电子教材-页';

-- ---------------------------------------------------------------------------
-- 5. 菜单：教师/教研员/管理员可见的顶级「电子教材」
-- ---------------------------------------------------------------------------
INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
SELECT 26100, '电子教材', 0, 9, 'textbook', 'business/textbook/index', '', 'Textbook',
       1, 0, 'C', '0', '0', 'business:textbook:query', 'education',
       'admin', NOW(), '', NULL, '教师端电子教材书架与阅读器，学生端不开放'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu
  WHERE menu_id = 26100 OR path = 'textbook' OR component = 'business/textbook/index'
);

UPDATE sys_menu
SET menu_name = '电子教材', parent_id = 0, order_num = 9, path = 'textbook',
    component = 'business/textbook/index', route_name = 'Textbook',
    menu_type = 'C', visible = '0', status = '0', perms = 'business:textbook:query',
    icon = 'education'
WHERE menu_id = 26100 OR component = 'business/textbook/index';

-- 阅读器是静态路由（/business/textbook/read/:bookId），不建隐藏菜单，避免出现第二个侧边入口。

-- ---------------------------------------------------------------------------
-- 6. 角色授权：admin / teacher / researcher
-- ---------------------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id = 26100
WHERE r.role_key IN ('admin', 'teacher', 'researcher')
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );

-- 复核：应返回 3 行（admin/teacher/researcher 各一条）。
-- SELECT r.role_key FROM sys_role_menu rm JOIN sys_role r ON r.role_id = rm.role_id WHERE rm.menu_id = 26100;
