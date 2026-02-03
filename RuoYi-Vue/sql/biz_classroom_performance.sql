-- 课堂表现记录表（平时分）
-- 用于记录教师对学生每课程的课堂表现评分
CREATE TABLE IF NOT EXISTS `biz_classroom_performance` (
  `performance_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `lesson_id` bigint NOT NULL COMMENT '课程ID',
  `score` int NOT NULL DEFAULT 0 COMMENT '平时分（-10到+10）',
  `reason` varchar(200) DEFAULT NULL COMMENT '加分/扣分原因',
  `teacher_id` bigint NOT NULL COMMENT '打分教师ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`performance_id`),
  UNIQUE KEY `uk_student_lesson` (`student_id`, `lesson_id`),
  KEY `idx_lesson` (`lesson_id`),
  KEY `idx_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课堂表现记录表';
