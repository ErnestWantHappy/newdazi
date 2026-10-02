-- 先备份并执行data_lab_v1.sql，再执行本脚本。只对测试库验收后发布；不自动修复旧NULL/模糊来源。
DELIMITER $$
DROP PROCEDURE IF EXISTS migrate_data_lab_v2$$
CREATE PROCEDURE migrate_data_lab_v2()
BEGIN
  IF EXISTS(SELECT 1 FROM biz_python_practice_plan_question p JOIN biz_question q ON q.question_id=p.question_id WHERE q.practical_mode='DATA_LAB') THEN
    IF NOT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='biz_data_lab_snapshot') THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Legacy DATA_LAB plan questions require verified snapshots before migration';
    ELSEIF EXISTS(SELECT 1 FROM biz_python_practice_plan_question p JOIN biz_question q ON q.question_id=p.question_id
      LEFT JOIN biz_data_lab_snapshot d ON d.snapshot_id=p.snapshot_id WHERE q.practical_mode='DATA_LAB' AND d.snapshot_id IS NULL) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='DATA_LAB plan question has no immutable snapshot';
    END IF;
  END IF;
  IF NOT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='biz_data_lab_snapshot')
     AND EXISTS(SELECT 1 FROM biz_data_lab_submission) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Legacy DATA_LAB submissions need snapshot and score reconciliation before migration';
  END IF;
  IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='biz_data_lab_snapshot') THEN
    IF EXISTS(SELECT 1 FROM biz_data_lab_submission sub
      LEFT JOIN biz_python_practice_question_snapshot s ON s.source_type=sub.source_type AND s.source_id=sub.source_id AND s.question_id=sub.question_id
      LEFT JOIN biz_data_lab_snapshot d ON d.snapshot_id=s.snapshot_id WHERE d.snapshot_id IS NULL) THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='DATA_LAB submission has no immutable snapshot; reconcile before migration';
    END IF;
  END IF;
  IF EXISTS(SELECT 1 FROM biz_data_lab_submission GROUP BY student_id,question_id,source_type,source_id HAVING COUNT(*)>1) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Duplicate DATA_LAB submissions require reconciliation';
  END IF;
  IF EXISTS(SELECT 1 FROM biz_data_lab_submission WHERE source_id IS NULL OR source_id<=0 OR source_type IS NULL OR source_type<>'BASE_VERSION') THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='DATA_LAB legacy source requires manual reconciliation';
  END IF;
  IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_data_lab_submission' AND column_name='revision') THEN
    ALTER TABLE biz_data_lab_submission ADD revision BIGINT NOT NULL DEFAULT 1;
  END IF;
  IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_data_lab_submission' AND column_name='max_score') THEN
    ALTER TABLE biz_data_lab_submission ADD max_score INT NOT NULL DEFAULT 100;
    IF EXISTS(SELECT 1 FROM biz_data_lab_submission) THEN
      UPDATE biz_data_lab_submission sub
      JOIN biz_python_practice_question_snapshot s ON s.source_type=sub.source_type AND s.source_id=sub.source_id AND s.question_id=sub.question_id
      JOIN biz_data_lab_snapshot d ON d.snapshot_id=s.snapshot_id SET sub.max_score=d.max_score;
    END IF;
  END IF;
  ALTER TABLE biz_data_lab_submission MODIFY source_type VARCHAR(20) NOT NULL, MODIFY source_id BIGINT NOT NULL;
END$$
CALL migrate_data_lab_v2()$$
DROP PROCEDURE migrate_data_lab_v2$$
DELIMITER ;

CREATE TABLE IF NOT EXISTS biz_data_lab_snapshot (
  snapshot_id BIGINT NOT NULL PRIMARY KEY,
  experiment_name VARCHAR(200), description TEXT, starter_code TEXT,
  excel_file_url VARCHAR(500) NOT NULL, excel_file_name VARCHAR(200) NOT NULL,
  excel_file_size BIGINT NOT NULL, max_score INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验配置的不可变题单快照';

CREATE TABLE IF NOT EXISTS biz_data_lab_upload (
  file_url VARCHAR(500) NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验数据文件上传归属，UUID文件不覆盖';

-- 不将当前题库配置回填到历史快照；旧实验数据须核实来源后单独迁移。
