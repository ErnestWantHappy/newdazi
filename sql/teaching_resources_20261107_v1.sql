-- 电子教材与教学资源 v1（2026-11-07）
-- 用途：宁波 2026 教学设计、整册教学设计汇编、3-8 年级课程指南与配套课件。
-- 幂等：可重复执行；只新增结构和菜单名，不改旧课例的评价、作者与资源。
-- 执行后需重启 ruoyi-admin（新增 Mapper 与接口）。

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 1. 课例增加来源批次；旧数据统一视为 LEGACY，不改变既有身份与评价。
-- ---------------------------------------------------------------------------
SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_textbook_case ADD COLUMN origin VARCHAR(32) NOT NULL DEFAULT ''LEGACY'' COMMENT ''来源批次：LEGACY/NINGBO2026'' AFTER school',
  'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_textbook_case' AND column_name = 'origin');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- 2. 资源增加附加原件（同一教学设计的 Word/PDF 合并为一条资源）。
-- ---------------------------------------------------------------------------
SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_textbook_case_resource ADD COLUMN extra_filename VARCHAR(255) DEFAULT NULL COMMENT ''附加原件文件名'' AFTER preview_path',
  'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_textbook_case_resource' AND column_name = 'extra_filename');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_textbook_case_resource ADD COLUMN extra_path VARCHAR(500) DEFAULT NULL COMMENT ''附加原件相对路径'' AFTER extra_filename',
  'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_textbook_case_resource' AND column_name = 'extra_path');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0,
  'ALTER TABLE biz_textbook_case_resource ADD COLUMN extra_bytes BIGINT NOT NULL DEFAULT 0 COMMENT ''附加原件字节数'' AFTER extra_path',
  'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_textbook_case_resource' AND column_name = 'extra_bytes');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------------
-- 3. 册级教学设计汇编（与“下载整册”教材 PDF 分开）。
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_textbook_book_resource` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `book_id`       BIGINT       NOT NULL,
  `resource_key`  VARCHAR(100) NOT NULL,
  `category`      VARCHAR(32)  NOT NULL DEFAULT 'DESIGN',
  `title`         VARCHAR(200) NOT NULL,
  `filename`      VARCHAR(255) NOT NULL,
  `original_path` VARCHAR(500) NOT NULL,
  `preview_path`  VARCHAR(500) DEFAULT NULL,
  `bytes`         BIGINT       NOT NULL DEFAULT 0,
  `description`   TEXT,
  `requirements`  TEXT,
  `sort_no`       INT          NOT NULL DEFAULT 0,
  `create_time`   DATETIME     DEFAULT NULL,
  `update_time`   DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_book_resource` (`book_id`, `resource_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='电子教材-册级教学设计汇编';

-- ---------------------------------------------------------------------------
-- 4. 课程教学指南：3-8 年级独立书架，不映射到现行教材课次。
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_guide_grade` (
  `guide_id`       BIGINT       NOT NULL AUTO_INCREMENT,
  `grade`          TINYINT      NOT NULL,
  `title`          VARCHAR(120) NOT NULL,
  `pdf_filename`   VARCHAR(255) NOT NULL,
  `pdf_path`       VARCHAR(500) NOT NULL,
  `pdf_bytes`      BIGINT       NOT NULL DEFAULT 0,
  `pdf_sha256`     CHAR(64)     DEFAULT NULL,
  `usage_filename` VARCHAR(255) DEFAULT NULL,
  `usage_path`     VARCHAR(500) DEFAULT NULL,
  `usage_bytes`    BIGINT       NOT NULL DEFAULT 0,
  `note`           TEXT,
  `sort_no`        INT          NOT NULL DEFAULT 0,
  `status`         CHAR(1)      NOT NULL DEFAULT '0',
  `create_time`    DATETIME     DEFAULT NULL,
  `update_time`    DATETIME     DEFAULT NULL,
  PRIMARY KEY (`guide_id`),
  UNIQUE KEY `uk_guide_grade` (`grade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程教学指南-年级';

CREATE TABLE IF NOT EXISTS `biz_guide_lesson` (
  `lesson_id`     BIGINT       NOT NULL AUTO_INCREMENT,
  `guide_id`      BIGINT       NOT NULL,
  `lesson_no`     INT          NOT NULL,
  `title`         VARCHAR(200) NOT NULL DEFAULT '',
  `filename`      VARCHAR(255) NOT NULL DEFAULT '',
  `original_path` VARCHAR(500) NOT NULL DEFAULT '',
  `preview_path`  VARCHAR(500) DEFAULT NULL,
  `bytes`         BIGINT       NOT NULL DEFAULT 0,
  `sha256`        CHAR(64)     DEFAULT NULL,
  `available`     TINYINT      NOT NULL DEFAULT 1 COMMENT '1有课件 0暂未提供',
  `note`          VARCHAR(500) DEFAULT NULL,
  `sort_no`       INT          NOT NULL DEFAULT 0,
  `create_time`   DATETIME     DEFAULT NULL,
  `update_time`   DATETIME     DEFAULT NULL,
  PRIMARY KEY (`lesson_id`),
  UNIQUE KEY `uk_guide_lesson` (`guide_id`, `lesson_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程教学指南-课次课件';

-- ---------------------------------------------------------------------------
-- 5. 菜单改名：仍是一个教师端入口，学生端不新增备课资料。
-- ---------------------------------------------------------------------------
UPDATE sys_menu
SET menu_name = '电子教材与教学资源', update_time = NOW()
WHERE menu_id = 26100 OR component = 'business/textbook/index';

-- 复核：应返回 1 行，menu_name=电子教材与教学资源。
SELECT menu_id, menu_name, path FROM sys_menu
WHERE menu_id = 26100 OR component = 'business/textbook/index';
