CREATE TABLE IF NOT EXISTS biz_score_adjustment (
  adjustment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '修正记录ID',
  student_id BIGINT NOT NULL COMMENT '学生ID',
  lesson_id BIGINT NOT NULL COMMENT '课程ID',
  dept_id BIGINT NOT NULL COMMENT '所属学校ID',
  teacher_id BIGINT NOT NULL COMMENT '操作教师ID',
  original_homework_score INT NOT NULL DEFAULT 0 COMMENT '原始作业分',
  adjusted_homework_score INT NULL COMMENT '修正后作业分',
  action_type VARCHAR(20) NOT NULL DEFAULT 'ADJUST' COMMENT '操作类型：ADJUST修正，CANCEL取消',
  reason VARCHAR(255) DEFAULT '' COMMENT '修正原因',
  create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (adjustment_id),
  KEY idx_score_adjustment_student_lesson (student_id, lesson_id),
  KEY idx_score_adjustment_dept_lesson (dept_id, lesson_id),
  KEY idx_score_adjustment_teacher_time (teacher_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩作业分人工修正记录表';
