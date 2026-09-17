-- 信息科技学业测评平台数据治理前检（只读）
-- 本文件只允许在目标库执行 SELECT；不包含 INSERT、UPDATE、DELETE、ALTER、DROP。
-- 输出用于人工确认候选记录，不能直接作为清理依据。

-- 1. 结构存在性与关键字段
SELECT table_name, column_name
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND ((table_name = 'biz_collab_room' AND column_name = 'provider_session_key')
    OR (table_name = 'biz_lesson' AND column_name IN ('dept_id', 'create_time', 'update_time'))
    OR (table_name = 'biz_lesson_assignment' AND column_name IN ('lesson_id', 'dept_id'))
    OR (table_name = 'biz_student' AND column_name IN ('user_id', 'student_no')))
ORDER BY table_name, ordinal_position;

-- 2. 重复登录名/学号候选。重复本身不等于错误，必须核对真实学籍、登录和成绩。
SELECT u.user_name,
       COUNT(*) AS student_record_count,
       GROUP_CONCAT(s.student_id ORDER BY s.student_id) AS student_ids,
       GROUP_CONCAT(DISTINCT s.student_no ORDER BY s.student_no) AS student_nos,
       GROUP_CONCAT(DISTINCT s.entry_year ORDER BY s.entry_year) AS entry_years,
       GROUP_CONCAT(DISTINCT s.class_code ORDER BY s.class_code) AS class_codes
FROM biz_student s
JOIN sys_user u ON u.user_id = s.user_id
GROUP BY u.user_name
HAVING COUNT(*) > 1
ORDER BY student_record_count DESC, u.user_name;

SELECT student_no,
       COUNT(*) AS student_record_count,
       GROUP_CONCAT(student_id ORDER BY student_id) AS student_ids,
       GROUP_CONCAT(DISTINCT entry_year ORDER BY entry_year) AS entry_years,
       GROUP_CONCAT(DISTINCT class_code ORDER BY class_code) AS class_codes
FROM biz_student
WHERE student_no IS NOT NULL AND TRIM(student_no) <> ''
GROUP BY student_no
HAVING COUNT(*) > 1
ORDER BY student_record_count DESC, student_no;

-- 3. 课程无归属、指派与课程学校不一致、以及指派孤儿记录
SELECT l.lesson_id, l.lesson_title, l.dept_id, l.create_time, l.update_time
FROM biz_lesson l
WHERE l.dept_id IS NULL
ORDER BY l.lesson_id;

SELECT a.assignment_id, a.lesson_id, a.dept_id AS assignment_dept_id,
       l.dept_id AS lesson_dept_id, a.entry_year, a.class_code
FROM biz_lesson_assignment a
LEFT JOIN biz_lesson l ON l.lesson_id = a.lesson_id
WHERE l.lesson_id IS NULL
   OR l.dept_id IS NULL
   OR a.dept_id IS NULL
   OR a.dept_id <> l.dept_id
ORDER BY a.assignment_id;

-- 4. 历史课程时间缺失；不得按执行迁移时间盲目回填。
SELECT COUNT(*) AS missing_create_time
FROM biz_lesson
WHERE create_time IS NULL;

SELECT COUNT(*) AS missing_update_time
FROM biz_lesson
WHERE update_time IS NULL;

SELECT lesson_id, lesson_title, create_time, update_time, create_by, update_by
FROM biz_lesson
WHERE create_time IS NULL OR update_time IS NULL
ORDER BY lesson_id;

-- 5. 教研活动压测残留候选，只读统计和明细。
SELECT COUNT(*) AS research_load_candidate_count,
       MIN(create_time) AS first_create_time,
       MAX(create_time) AS last_create_time
FROM biz_research_topic
WHERE del_flag = '0'
  AND title LIKE 'QARA0811CONC%';

SELECT topic_id, topic_type, title, creator_id, dept_id, create_time, del_flag
FROM biz_research_topic
WHERE title LIKE 'QARA0811CONC%'
ORDER BY create_time, topic_id;

-- 6. 公共导学单测试候选。结果只用于人工确认，不自动归档。
SELECT sheet_id, sheet_title, grade, semester, lesson_num,
       creator_id, dept_id, county_dept_id, is_public, del_flag,
       create_time, update_time
FROM biz_guide_sheet
WHERE del_flag = '0'
  AND is_public = 'Y'
  AND (sheet_title LIKE '%测试%' OR sheet_title LIKE '%test%')
ORDER BY update_time DESC, sheet_id;

-- 7. 区域抽测测试候选及仍处于开启状态的记录。
SELECT exam_id, exam_name, school_type, exam_grade, status,
       grading_enabled, create_time, update_time, open_time, close_time, del_flag
FROM biz_county_exam
WHERE del_flag = '0'
  AND (exam_name IN ('33', '222') OR exam_name LIKE '%测试%');

SELECT exam_id, exam_name, status, grading_enabled, open_time, close_time
FROM biz_county_exam
WHERE del_flag = '0'
  AND status IN ('1', 'OPEN', 'open')
ORDER BY open_time, exam_id;

-- 8. 乱码定时任务候选。先查看调用目标，禁止仅凭显示名称删除。
SELECT job_id, job_name, invoke_target, status, create_time, update_time
FROM sys_job
WHERE job_name LIKE '%?%' OR job_name LIKE '%�%'
ORDER BY job_id;

