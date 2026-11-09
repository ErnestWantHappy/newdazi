-- 教师工具检索与教学任务 v2
-- 目标：本机或经确认的目标库（先前检，再执行）；MySQL 8；可重复执行，只做加列/建表/新增词表与任务段，不删改已有工具内容。
-- 注意：本脚本不修正“学生免登录工具”里需登录的条目，也不删除任何疑似重复或测试数据，由教研发在本平台管理页核对后处理。

-- 前检 1：三张原表存在
SELECT 'precheck_tables' AS check_name,
       (SELECT COUNT(*) FROM information_schema.tables
         WHERE table_schema = DATABASE()
           AND table_name IN ('biz_teacher_tool', 'biz_teacher_tool_category', 'biz_teacher_tool_category_rel')) AS found;

-- 前检 2：新列尚未存在（执行后应均为 0 或 1，为 1 时对应 ALTER 会被跳过）
SELECT 'precheck_column' AS check_name, column_name, COUNT(*) AS found
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'biz_teacher_tool'
  AND column_name IN ('aliases', 'task_tags')
GROUP BY column_name;

-- MySQL 8 不支持 ADD COLUMN IF NOT EXISTS，用存储过程判断后再加，保证重复执行不报错。
DROP PROCEDURE IF EXISTS teacher_tools_v2_add_column;
DELIMITER $$
CREATE PROCEDURE teacher_tools_v2_add_column(IN p_column VARCHAR(64), IN p_definition VARCHAR(255))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                    WHERE table_schema = DATABASE() AND table_name = 'biz_teacher_tool' AND column_name = p_column) THEN
        SET @ddl = CONCAT('ALTER TABLE biz_teacher_tool ADD COLUMN ', p_column, ' ', p_definition);
        SET @ddl_sql = @ddl;
        PREPARE stmt FROM @ddl_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL teacher_tools_v2_add_column('aliases', "VARCHAR(500) NULL COMMENT '检索别名，顿号分隔，供搜索命中' AFTER tags");
CALL teacher_tools_v2_add_column('task_tags', "VARCHAR(200) NULL COMMENT '教学任务编码，逗号分隔' AFTER aliases");
DROP PROCEDURE IF EXISTS teacher_tools_v2_add_column;

CREATE TABLE IF NOT EXISTS biz_teacher_tool_task (
    task_id      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '任务主键',
    task_code    VARCHAR(32) NOT NULL COMMENT '稳定任务编码',
    task_name    VARCHAR(50) NOT NULL COMMENT '教师看到的任务名称',
    description  VARCHAR(200) NULL COMMENT '任务说明',
    sort_order   INT         NOT NULL DEFAULT 100 COMMENT '页面排序',
    status       CHAR(1)     NOT NULL DEFAULT '0' COMMENT '0启用/1停用',
    del_flag     CHAR(1)     NOT NULL DEFAULT '0' COMMENT '0正常/2删除',
    create_by    VARCHAR(64) NULL,
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by    VARCHAR(64) NULL,
    update_time  DATETIME    NULL,
    PRIMARY KEY (task_id),
    UNIQUE KEY uk_teacher_tool_task_code (task_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师工具教学任务';

CREATE TABLE IF NOT EXISTS biz_teacher_tool_term (
    term_id      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '别名主键',
    term         VARCHAR(50) NOT NULL COMMENT '教师常用说法',
    tool_id      BIGINT      NOT NULL COMMENT '对应工具',
    sort_order   INT         NOT NULL DEFAULT 100,
    create_by    VARCHAR(64) NULL,
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (term_id),
    UNIQUE KEY uk_teacher_tool_term (term, tool_id),
    KEY idx_teacher_tool_term_tool (tool_id),
    -- 工具物理删除时连带清词条；软删除仍保留，恢复工具后别名无需重配。
    CONSTRAINT fk_teacher_tool_term_tool FOREIGN KEY (tool_id) REFERENCES biz_teacher_tool (tool_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师工具检索别名';

-- 教学任务候选：只保留本库确有工具覆盖的任务，避免出现空任务按钮。
INSERT INTO biz_teacher_tool_task (task_code, task_name, description, sort_order, status, del_flag, create_by)
SELECT s.task_code, s.task_name, s.description, s.sort_order, '0', '0', 'system'
FROM (
    SELECT 'programming' task_code, '编程与算法' task_name, '图形化、Python、算法与编程平台' description, 10 sort_order
    UNION ALL SELECT 'experiment', '课堂实验', '网络、物联网、图像识别、场景实验等课堂动手操作', 20
    UNION ALL SELECT 'resources', '备课找资源', '课件、教材、教学设计、素材下载与教研资源', 30
    UNION ALL SELECT 'material', '制作教学素材', '图片、PPT、音频、视频、文档和二维码处理', 40
    UNION ALL SELECT 'class-affairs', '班级事务', '点名、成绩、座位、考试与班级管理', 50
) s
WHERE NOT EXISTS (SELECT 1 FROM biz_teacher_tool_task k WHERE k.task_code = s.task_code);

-- 任务段回填：仅对 task_tags 为空的工具写入，重复执行不会覆盖教研发已在本平台调整的归属。
-- 顺序即优先级：编程/实验/素材/班级先写，最后资源类兜底；多任务归属由教研发在管理页补选。
UPDATE biz_teacher_tool t SET t.task_tags = 'programming'
WHERE (t.task_tags IS NULL OR t.task_tags = '')
  AND (LOWER(t.title) REGEXP 'python|编程|代码|流程|算法|小程序|掌控板|micro')
  AND t.title NOT LIKE '%流程图%';

UPDATE biz_teacher_tool t SET t.task_tags = 'experiment'
WHERE (t.task_tags IS NULL OR t.task_tags = '')
  AND (t.title LIKE '%实验%' OR t.tags LIKE '%实验%' OR t.description LIKE '%实验%'
       OR t.title LIKE '%图像识别%' OR t.title LIKE '%网络搭建%' OR t.title LIKE '%物联网%');

UPDATE biz_teacher_tool t SET t.task_tags = 'material'
WHERE (t.task_tags IS NULL OR t.task_tags = '')
  AND t.title REGEXP 'PPT|Gamma|配音|语音|朗读|音乐|视频|图片|压缩|GIF|图标|二维码|PDF|表格转换|文档|绘图|素材'
  AND t.title NOT LIKE '%小工具%';

UPDATE biz_teacher_tool t SET t.task_tags = 'class-affairs'
WHERE (t.task_tags IS NULL OR t.task_tags = '')
  AND (t.title REGEXP '点名|座位|成绩分析|教务|班级|刷题' OR t.tags LIKE '%成绩%' OR t.tags LIKE '%班主任%');

UPDATE biz_teacher_tool t SET t.task_tags = 'resources'
WHERE (t.task_tags IS NULL OR t.task_tags = '')
  AND (t.title REGEXP '平台|图书馆|教育网|教程|模板|资源|导航|文库|慕课|教科研|培训'
       OR t.tags LIKE '%备课%' OR t.tags LIKE '%资源%' OR t.tags LIKE '%研修%' OR t.description LIKE '%资源%');

-- AI 助手类归入课堂实验（学生可直接做对话/生成练习）；仍为空的工具不强行归任务，由教研发决定。
UPDATE biz_teacher_tool t SET t.task_tags = 'experiment'
WHERE (t.task_tags IS NULL OR t.task_tags = '')
  AND (t.tags LIKE '%AI%' OR t.title LIKE '%AI%' OR t.description LIKE '%智能%' OR t.description LIKE '%大模型%');

-- 检索别名：按名称精确匹配回填，只补教师常用的同义说法；新增同名工具时需在本平台管理页补充。
INSERT INTO biz_teacher_tool_term (term, tool_id, sort_order, create_by)
SELECT v.term, t.tool_id, 100, 'system'
FROM (
    SELECT 'PPT' term, 'Gamma' title
    UNION ALL SELECT '演示文稿', 'Gamma' UNION ALL SELECT '课件', 'Gamma'
    UNION ALL SELECT '配音', 'Noiz AI 配音' UNION ALL SELECT '文字转语音', 'Noiz AI 配音'
    UNION ALL SELECT '语音合成', '文字转语音' UNION ALL SELECT '配音', '文字转语音'
    UNION ALL SELECT '图片压缩', 'Optimizilla 图片压缩' UNION ALL SELECT '压缩图片', 'Optimizilla 图片压缩'
    UNION ALL SELECT '压缩图片', 'TinyPNG' UNION ALL SELECT '图片压缩', 'TinyPNG'
    UNION ALL SELECT '视频生成', 'Viggle AI 视频' UNION ALL SELECT '动图', 'Ezgif'
    UNION ALL SELECT '二维码生成', '草料二维码' UNION ALL SELECT '二维码', 'GoQR'
    UNION ALL SELECT '打字练习', '打字平台' UNION ALL SELECT '键盘', '打字平台'
    UNION ALL SELECT '打字练习', '在线打字' UNION ALL SELECT '打字', '在线打字'
    UNION ALL SELECT '流程图', '在线流程图' UNION ALL SELECT '流程图绘制', '在线流程图'
    UNION ALL SELECT '图形化编程', '图形化编程' UNION ALL SELECT 'Scratch', '图形化编程' UNION ALL SELECT '积木编程', '图形化编程'
    UNION ALL SELECT 'Python', 'Python 编程' UNION ALL SELECT 'Python', '掌控板开发教程'
    UNION ALL SELECT '点名', '课堂随机点名系统' UNION ALL SELECT '随机点名', '课堂点名校本系统'
    UNION ALL SELECT '座位表', '考试座位贴生成工具' UNION ALL SELECT '成绩', '成绩分析系统'
    UNION ALL SELECT '错题', '错题刷题系统 2.0'
    UNION ALL SELECT 'AI', '豆包' UNION ALL SELECT 'AI', 'DeepSeek' UNION ALL SELECT 'AI', '通义千问' UNION ALL SELECT 'AI', 'Kimi'
    UNION ALL SELECT '搜索', '秘塔 AI 搜索' UNION ALL SELECT '备课', '21 世纪教育网'
    UNION ALL SELECT '备课', '国家中小学智慧教育平台'
    UNION ALL SELECT '课件下载', '优品 PPT' UNION ALL SELECT 'PPT 模板', '优品 PPT'
    UNION ALL SELECT '邮件', '七上邮件系统' UNION ALL SELECT '网络搭建', '小型网络搭建仿真'
    UNION ALL SELECT '物联网', '物联数据中台' UNION ALL SELECT '物联网', '物联网数据流程演示'
    UNION ALL SELECT '朗读', 'AI 智能朗读评价系统' UNION ALL SELECT '音乐', 'AI 音乐学习工具 2.0'
    UNION ALL SELECT '云盘', '象山教育云盘' UNION ALL SELECT '大文件', '文叔叔'
    UNION ALL SELECT '图标', 'Iconfont' UNION ALL SELECT '素材', 'Pexels' UNION ALL SELECT '图片', '百度图片'
) v
JOIN biz_teacher_tool t ON t.title = v.title
ON DUPLICATE KEY UPDATE term_id = term_id;

-- 后检：任务与别名统计（期望 5 个任务均有工具，别名条数 > 0）
SELECT 'postcheck_task' AS check_name, k.task_code, k.task_name, COUNT(DISTINCT t.tool_id) AS tool_count
FROM biz_teacher_tool_task k
LEFT JOIN biz_teacher_tool t ON FIND_IN_SET(k.task_code, t.task_tags) > 0 AND t.status='0' AND t.del_flag='0'
GROUP BY k.task_id, k.task_code, k.task_name
ORDER BY k.sort_order;

SELECT 'postcheck_term' AS check_name, COUNT(*) AS term_rows, COUNT(DISTINCT tool_id) AS tool_with_term
FROM biz_teacher_tool_term;

-- 别名列回填：管理列表关键词走 aliases 列，历史别名只存在 term 明细表时此处补齐；
-- 只写 aliases 为空的工具，重复执行不覆盖教研发已在本平台维护的别名。
-- 会话内放大分组拼接上限：默认 1024 按字节计算，多别名工具存在截断风险；脚本结束随会话失效。
SET SESSION group_concat_max_len = 100000;
UPDATE biz_teacher_tool t SET t.aliases = (
    SELECT GROUP_CONCAT(m.term ORDER BY m.sort_order, m.term_id SEPARATOR '、')
    FROM biz_teacher_tool_term m
    WHERE m.tool_id = t.tool_id
)
WHERE (t.aliases IS NULL OR t.aliases = '')
  AND EXISTS (SELECT 1 FROM biz_teacher_tool_term m WHERE m.tool_id = t.tool_id);

-- 回滚（需按序执行，仅在本脚本产生的数据不再被需要时）：
--   DROP TABLE IF EXISTS biz_teacher_tool_term;
--   DROP TABLE IF EXISTS biz_teacher_tool_task;
--   ALTER TABLE biz_teacher_tool DROP COLUMN task_tags, DROP COLUMN aliases;
-- 说明：回滚会丢失教研发在本轮新增的别名与任务归属；执行前先整库备份。
