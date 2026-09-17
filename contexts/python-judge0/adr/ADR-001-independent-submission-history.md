# ADR-001：编程提交历史独立于既有学生答案表

## 决策

Python 题仍是 `biz_question.question_type=practical`，以 `practical_mode=PYTHON` 表示在线编程。新增 `biz_programming_*` 表承载测试点、草稿和每次提交；只有正式判题的终态才写回 `biz_student_answer`。

## 原因

现有答案表每个学生、课程、题目只有一条记录，适合当前成绩，不足以保存代码、公开/隐藏测试点结果、Judge0 token 和故障恢复状态。该决策避免重新建立成绩体系，也避免服务故障覆盖已有成绩。
