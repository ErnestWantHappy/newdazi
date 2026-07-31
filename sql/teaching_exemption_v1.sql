-- 教师免抽测申请 / 课程真实使用日期 / 课程时间默认值 v1
-- MySQL 8；脚本可重复执行，不回填任何历史课程时间，不修改学生答案或成绩。

CREATE TABLE IF NOT EXISTS biz_exemption_standard (
    standard_id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '标准主键',
    academic_year           VARCHAR(4)   NOT NULL COMMENT '学年起始年份',
    semester                CHAR(1)      NOT NULL COMMENT '学期1/2',
    grade                   INT          NOT NULL COMMENT '年级',
    required_lesson_count   INT          NOT NULL DEFAULT 15 COMMENT '每班应使用课数',
    create_by               VARCHAR(64)  NULL,
    create_time             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by               VARCHAR(64)  NULL,
    update_time             DATETIME     NULL,
    PRIMARY KEY (standard_id),
    UNIQUE KEY uk_exemption_standard (academic_year, semester, grade),
    CONSTRAINT chk_exemption_standard_semester CHECK (semester IN ('1', '2')),
    CONSTRAINT chk_exemption_standard_count CHECK (required_lesson_count BETWEEN 1 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='免抽测每学期应使用课数标准';

CREATE TABLE IF NOT EXISTS biz_exemption_application (
    application_id               BIGINT          NOT NULL AUTO_INCREMENT COMMENT '申请主键',
    dept_id                      BIGINT          NOT NULL COMMENT '学校ID快照',
    dept_name_snapshot           VARCHAR(100)    NOT NULL COMMENT '学校名称快照',
    teacher_id                   BIGINT          NOT NULL COMMENT '教师用户ID',
    teacher_name_snapshot        VARCHAR(100)    NOT NULL COMMENT '教师姓名快照',
    academic_year                VARCHAR(4)      NOT NULL COMMENT '学年起始年份',
    semester                     CHAR(1)         NOT NULL COMMENT '学期1/2',
    grade                        INT             NOT NULL COMMENT '年级',
    entry_year_snapshot          VARCHAR(4)      NOT NULL COMMENT '入学年份快照',
    required_lesson_count        INT             NOT NULL COMMENT '应使用课数快照',
    usage_threshold_pct          DECIMAL(5,2)    NOT NULL DEFAULT 80.00 COMMENT '平台使用阈值快照',
    participation_threshold_pct  DECIMAL(5,2)    NOT NULL DEFAULT 50.00 COMMENT '单课参与阈值快照',
    practical_threshold_pct      DECIMAL(5,2)    NOT NULL DEFAULT 80.00 COMMENT '操作题批改阈值快照',
    class_count                  INT             NOT NULL COMMENT '申请班级数',
    all_classes_qualified        TINYINT(1)      NOT NULL COMMENT '是否全部班级平台使用达标',
    practical_due_count          INT             NOT NULL DEFAULT 0 COMMENT '非空操作题提交数',
    practical_graded_count       INT             NOT NULL DEFAULT 0 COMMENT '已批操作题提交数',
    practical_rate               DECIMAL(7,2)    NULL COMMENT '整体操作题批改率，无提交时为空',
    practical_qualified          TINYINT(1)      NULL COMMENT '操作题是否达标，无提交时为空',
    teacher_remark               VARCHAR(2000)   NULL COMMENT '教师总补充说明',
    status                       VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PASS/FAIL',
    submit_time                  DATETIME        NOT NULL COMMENT '提交时间',
    reviewer_id                  BIGINT          NULL COMMENT '审核人ID',
    reviewer_name_snapshot       VARCHAR(100)    NULL COMMENT '审核人姓名快照',
    review_time                  DATETIME        NULL COMMENT '审核时间',
    review_remark                VARCHAR(1000)   NULL COMMENT '审核备注',
    version                      INT             NOT NULL DEFAULT 0 COMMENT '审核乐观锁版本',
    create_by                    VARCHAR(64)     NULL,
    create_time                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by                    VARCHAR(64)     NULL,
    update_time                  DATETIME        NULL,
    PRIMARY KEY (application_id),
    UNIQUE KEY uk_exemption_application (dept_id, teacher_id, academic_year, semester, grade),
    KEY idx_exemption_application_review (status, academic_year, semester, grade, dept_id),
    KEY idx_exemption_application_teacher (teacher_id, submit_time),
    CONSTRAINT chk_exemption_application_semester CHECK (semester IN ('1', '2')),
    CONSTRAINT chk_exemption_application_status CHECK (status IN ('PENDING', 'PASS', 'FAIL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师免抽测申请';

CREATE TABLE IF NOT EXISTS biz_exemption_class_snapshot (
    class_snapshot_id        BIGINT          NOT NULL AUTO_INCREMENT COMMENT '班级快照主键',
    application_id           BIGINT          NOT NULL COMMENT '申请ID',
    dept_id                  BIGINT          NOT NULL COMMENT '学校ID快照',
    entry_year               VARCHAR(4)      NOT NULL COMMENT '入学年份快照',
    class_code               VARCHAR(30)     NOT NULL COMMENT '班级编号快照',
    valid_student_count      INT             NOT NULL COMMENT '有效学生数',
    required_lesson_count    INT             NOT NULL COMMENT '应使用课数快照',
    used_lesson_count        INT             NOT NULL COMMENT '实际有效使用课数',
    usage_rate               DECIMAL(7,2)    NOT NULL COMMENT '平台使用率',
    usage_qualified          TINYINT(1)      NOT NULL COMMENT '平台使用是否达到80%',
    practical_due_count      INT             NOT NULL DEFAULT 0 COMMENT '非空操作题提交数',
    practical_graded_count   INT             NOT NULL DEFAULT 0 COMMENT '已批操作题提交数',
    practical_rate           DECIMAL(7,2)    NULL COMMENT '班级操作题批改率，无提交时为空',
    create_time              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (class_snapshot_id),
    UNIQUE KEY uk_exemption_class_snapshot (application_id, entry_year, class_code),
    KEY idx_exemption_class_application (application_id, class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='免抽测申请班级统计快照';

CREATE TABLE IF NOT EXISTS biz_exemption_course_snapshot (
    course_snapshot_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '课程快照主键',
    application_id           BIGINT          NOT NULL COMMENT '申请ID',
    class_snapshot_id        BIGINT          NOT NULL COMMENT '班级快照ID',
    lesson_id                BIGINT          NOT NULL COMMENT '课程ID快照',
    lesson_title_snapshot    VARCHAR(255)    NOT NULL COMMENT '课程名称快照',
    entry_year               VARCHAR(4)      NOT NULL COMMENT '入学年份快照',
    class_code               VARCHAR(30)     NOT NULL COMMENT '班级编号快照',
    usage_date               DATETIME        NULL COMMENT '周期内首次真实参与时间',
    valid_student_count      INT             NOT NULL COMMENT '有效学生数',
    participant_count        INT             NOT NULL COMMENT '周期内去重参与人数',
    participation_rate       DECIMAL(7,2)    NULL COMMENT '参与率，分母为0时为空',
    counted_as_used          TINYINT(1)      NOT NULL COMMENT '是否计为使用一课',
    practical_due_count      INT             NOT NULL DEFAULT 0 COMMENT '非空操作题提交数',
    practical_graded_count   INT             NOT NULL DEFAULT 0 COMMENT '已批操作题提交数',
    practical_rate           DECIMAL(7,2)    NULL COMMENT '课程班级批改率，无提交时为空',
    create_time              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (course_snapshot_id),
    UNIQUE KEY uk_exemption_course_snapshot (application_id, entry_year, class_code, lesson_id),
    KEY idx_exemption_course_application (application_id, class_code, usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='免抽测申请课程明细快照';

CREATE TABLE IF NOT EXISTS biz_exemption_attachment (
    attachment_id            BIGINT          NOT NULL AUTO_INCREMENT COMMENT '附件主键',
    application_id           BIGINT          NOT NULL COMMENT '申请ID',
    original_file_name       VARCHAR(255)    NOT NULL COMMENT '原文件名',
    resource_path            VARCHAR(500)    NOT NULL COMMENT '通用上传资源路径',
    file_size                BIGINT          NULL COMMENT '文件字节数',
    mime_type                VARCHAR(100)    NULL COMMENT 'MIME类型',
    create_by                VARCHAR(64)     NULL,
    create_time              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (attachment_id),
    UNIQUE KEY uk_exemption_attachment_path (resource_path),
    KEY idx_exemption_attachment_application (application_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='免抽测申请附件归属';

-- 历史NULL保持不变；仅为未来没有显式传值的新增提供数据库兜底。
ALTER TABLE biz_lesson
    MODIFY COLUMN create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN update_time DATETIME NULL DEFAULT NULL COMMENT '教师最后修改时间';

-- 教师独立入口；按权限幂等定位，避免依赖可能冲突的新菜单ID。
INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '免抽测申请', 0, 1, 'teacher-exemption', 'business/teacher/exemption/index', '', 'TeacherExemption',
       1, 0, 'C', '0', '0', 'business:exemption:apply', 'document', 'system', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'business:exemption:apply'
);

INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '免抽测申请审核', 2046, 4, '#', '', '', '',
       1, 0, 'F', '0', '0', 'business:exemption:review', '#', 'system', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'business:exemption:review'
);

INSERT INTO sys_menu
    (menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '免抽测课数标准', 2046, 5, '#', '', '', '',
       1, 0, 'F', '0', '0', 'business:exemption:standard', '#', 'system', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'business:exemption:standard'
);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role.role_id, menu.menu_id
FROM sys_role role
INNER JOIN sys_menu menu ON menu.perms = 'business:exemption:apply'
WHERE role.role_key = 'teacher';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role.role_id, menu.menu_id
FROM sys_role role
INNER JOIN sys_menu menu
        ON menu.perms IN ('business:exemption:review', 'business:exemption:standard')
WHERE role.role_key = 'researcher';

-- 回滚说明（不自动执行）：
-- 1. 应用回滚：切回旧jar/dist，旧应用会忽略新增表；课程create_time默认值可继续保留。
-- 2. 数据库回滚：优先恢复执行前整库备份。
-- 3. 若确认五张表没有正式申请数据，可先删除对应菜单/角色授权，再按附件→课程快照→班级快照→申请→标准顺序DROP。
-- 4. 禁止在存在正式申请快照时直接DROP表。
