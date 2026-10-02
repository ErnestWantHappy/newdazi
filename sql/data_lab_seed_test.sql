-- 隔离保留的旧测试样例，不可执行：硬编码版本1会改动真实题单并破坏快照引用。
-- DATA_LAB 权限、快照和进度协议完成后，另建一次性测试库的造数流程。
/*
-- 插入数据分析实验题测试数据
DELETE FROM biz_data_lab_config WHERE question_id = 999901;
DELETE FROM biz_python_practice_plan_question WHERE question_id = 999901;
DELETE FROM biz_python_practice_question_snapshot WHERE question_id = 999901;
DELETE FROM biz_question WHERE question_id = 999901;

-- 1. biz_question
INSERT INTO biz_question (
  question_id, question_type, practical_mode, question_content, difficulty, grade, create_by, create_time
) VALUES (
  999901, 'practical', 'DATA_LAB', '高中运动会各班总得分统计与图表分析', 'MEDIUM', 10, 'admin', NOW()
);

-- 2. biz_data_lab_config
INSERT INTO biz_data_lab_config (
  question_id, experiment_name, description, starter_code,
  excel_file_url, excel_file_name, excel_file_size, max_score, create_by, create_time
) VALUES (
  999901,
  '高中运动会成绩统计与可视化分析',
  '任务要求：\n1. 读取高中运动会成绩统计 Excel 数据表；\n2. 统计“有得分记录”（得分 > 0）的学生所在班级的总得分；\n3. 找出总得分最高的前 6 个班级；\n4. 使用 matplotlib 绘制各班得分柱形图，图表标题为“各班得分统计”，y轴标签为“得分”，并调用 plt.show() 显示图表。',
  CONCAT(
    '# 现要统计“有得分记录”的学生所在班级的总得分，找出总得分最高的前6个班级,并绘制成柱形图\n',
    'import pandas as pd\n',
    'import matplotlib.pyplot as plt\n\n',
    'df = pd.read_excel("高中运动会成绩统计.xlsx")\n\n',
    '# 请在下方补充代码完成统计与绘图：\n'
  ),
  '/profile/datalab/excel/demo/高中运动会成绩统计.xlsx',
  '高中运动会成绩统计.xlsx',
  43387,
  100,
  'admin',
  NOW()
);

-- 3. biz_python_practice_question_snapshot
INSERT INTO biz_python_practice_question_snapshot (
  snapshot_id, source_type, source_id, question_id, snapshot_hash,
  question_title, difficulty, knowledge_points, no_input, question_content,
  starter_code, language_code, time_limit_seconds, memory_limit_kb, create_by, create_time
) VALUES (
  999901, 'BASE_VERSION', 1, 999901, 'datalab999901',
  '高中运动会成绩统计与可视化分析', 'MEDIUM', 'Pandas,Matplotlib,Excel数据分析', '1',
  '任务要求：统计有得分记录的班级总得分前6名并绘制柱形图',
  CONCAT(
    '# 现要统计“有得分记录”的学生所在班级的总得分，找出总得分最高的前6个班级,并绘制成柱形图\n',
    'import pandas as pd\n',
    'import matplotlib.pyplot as plt\n\n',
    'df = pd.read_excel("高中运动会成绩统计.xlsx")\n\n',
    '# 请在下方补充代码完成统计与绘图：\n'
  ),
  'python', 10.00, 262144, 'admin', NOW()
);

-- 4. biz_python_practice_plan_question
INSERT INTO biz_python_practice_plan_question (
  plan_version_id, question_id, snapshot_id, sort_no, stage, required_flag
) VALUES (
  1, 999901, 999901, 1, 'BEGINNER', '1'
);
*/
