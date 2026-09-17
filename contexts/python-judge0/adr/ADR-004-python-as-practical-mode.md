# ADR-004：Python 作为操作题作答方式

## 决策

Python 在线编程不新增第四类题型。统一题库使用 `question_type=practical`，通过 `practical_mode=FILE` 表示文件作品、`practical_mode=PYTHON` 表示 Judge0 在线编程；理论、打字、操作三类仍合计 100 分。

## 原因

平台现有课程总分、学生答案、成绩查询和权限模型都以三类测评为业务边界。把 Python 作为独立题型会造成课程设计器误报、操作题成绩漏算和教师重复理解。保留 `practical` 能复用既有成绩统计，同时让 Python 保留独立草稿、测试点和提交历史。

## 迁移与回滚

`sql/python_judge0_practical_mode_v2.sql` 幂等补充作答方式字段，将历史 `python` 题转为 `practical/PYTHON`，不删除编程表数据。回滚使用正式库备份和旧 release；恢复旧代码前不得重新启用旧 Python 字典选项。

同一课程允许任意数量的 `FILE` 与 `PYTHON` 操作题；限制只保留在课程总分必须为 100 分。成绩主口径仍是三类，细分字段只作为操作题明细，不能参与第二次累计。
