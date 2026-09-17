-- 教师首页按课程开设年级独立编号：无歧义历史课次修正（MySQL 8）
--
-- 执行前必须完成目标库备份。本脚本只处理已人工确认的课程 263～267：
--   * 2025级七年级：263～266，由第12～15课修正为第1～4课；
--   * 2025级八年级：267，由第16课修正为第1课。
-- 2024级八年级存在重复课次和缺号，本脚本明确不处理。

DROP PROCEDURE IF EXISTS sp_fix_teacher_dashboard_opening_grade_lesson_num_v1;
DELIMITER $$

CREATE PROCEDURE sp_fix_teacher_dashboard_opening_grade_lesson_num_v1()
BEGIN
    DECLARE v_teacher_count INT DEFAULT 0;
    DECLARE v_target_count INT DEFAULT 0;
    DECLARE v_invalid_count INT DEFAULT 0;
    DECLARE v_collision_count INT DEFAULT 0;
    DECLARE v_post_count INT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_teacher_count
    FROM sys_user
    WHERE user_name = '19157727791';

    IF v_teacher_count <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson-num fix aborted: teacher account is missing or duplicated';
    END IF;

    SELECT COUNT(*)
    INTO v_target_count
    FROM biz_lesson
    WHERE lesson_id IN (263, 264, 265, 266, 267);

    IF v_target_count <> 5 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson-num fix aborted: expected five target lessons';
    END IF;

    -- 每条记录同时接受“修正前”与“修正后”状态，使脚本可安全重复执行。
    SELECT COUNT(*)
    INTO v_invalid_count
    FROM biz_lesson lesson
    INNER JOIN sys_user teacher ON teacher.user_name = '19157727791'
    WHERE lesson.lesson_id IN (263, 264, 265, 266, 267)
      AND NOT (
          lesson.dept_id = 169
          AND (lesson.creator_id = teacher.user_id OR lesson.create_by = teacher.user_name)
          AND lesson.entry_year = '2025'
          AND (
              (lesson.lesson_id = 263 AND lesson.grade = 7 AND lesson.lesson_num IN (12, 1))
              OR (lesson.lesson_id = 264 AND lesson.grade = 7 AND lesson.lesson_num IN (13, 2))
              OR (lesson.lesson_id = 265 AND lesson.grade = 7 AND lesson.lesson_num IN (14, 3))
              OR (lesson.lesson_id = 266 AND lesson.grade = 7 AND lesson.lesson_num IN (15, 4))
              OR (lesson.lesson_id = 267 AND lesson.grade = 8 AND lesson.lesson_num IN (16, 1))
          )
      );

    IF v_invalid_count > 0 THEN
        SELECT lesson_id, dept_id, creator_id, create_by, entry_year, grade, lesson_num, lesson_title
        FROM biz_lesson
        WHERE lesson_id IN (263, 264, 265, 266, 267)
        ORDER BY lesson_id;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson-num fix aborted: target evidence differs from the reviewed data';
    END IF;

    -- 防止目标课次已被同教师、同届别、同开设年级的其他课程占用。
    SELECT COUNT(*)
    INTO v_collision_count
    FROM biz_lesson lesson
    INNER JOIN sys_user teacher ON teacher.user_name = '19157727791'
    WHERE lesson.dept_id = 169
      AND (lesson.creator_id = teacher.user_id OR lesson.create_by = teacher.user_name)
      AND lesson.entry_year = '2025'
      AND lesson.lesson_id NOT IN (263, 264, 265, 266, 267)
      AND (
          (lesson.grade = 7 AND lesson.lesson_num IN (1, 2, 3, 4))
          OR (lesson.grade = 8 AND lesson.lesson_num = 1)
      );

    IF v_collision_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson-num fix aborted: target lesson numbers are already occupied';
    END IF;

    SELECT lesson_id, entry_year, grade, lesson_num AS before_lesson_num, lesson_title
    FROM biz_lesson
    WHERE lesson_id IN (263, 264, 265, 266, 267)
    ORDER BY lesson_id;

    UPDATE biz_lesson
    SET lesson_num = CASE lesson_id
        WHEN 263 THEN 1
        WHEN 264 THEN 2
        WHEN 265 THEN 3
        WHEN 266 THEN 4
        WHEN 267 THEN 1
        ELSE lesson_num
    END
    WHERE lesson_id IN (263, 264, 265, 266, 267);

    SELECT COUNT(*)
    INTO v_post_count
    FROM biz_lesson
    WHERE (lesson_id = 263 AND entry_year = '2025' AND grade = 7 AND lesson_num = 1)
       OR (lesson_id = 264 AND entry_year = '2025' AND grade = 7 AND lesson_num = 2)
       OR (lesson_id = 265 AND entry_year = '2025' AND grade = 7 AND lesson_num = 3)
       OR (lesson_id = 266 AND entry_year = '2025' AND grade = 7 AND lesson_num = 4)
       OR (lesson_id = 267 AND entry_year = '2025' AND grade = 8 AND lesson_num = 1);

    IF v_post_count <> 5 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'lesson-num fix failed: postflight verification did not pass';
    END IF;

    SELECT lesson_id, entry_year, grade, lesson_num AS after_lesson_num, lesson_title
    FROM biz_lesson
    WHERE lesson_id IN (263, 264, 265, 266, 267)
    ORDER BY lesson_id;
END$$

DELIMITER ;
CALL sp_fix_teacher_dashboard_opening_grade_lesson_num_v1();
DROP PROCEDURE IF EXISTS sp_fix_teacher_dashboard_opening_grade_lesson_num_v1;
