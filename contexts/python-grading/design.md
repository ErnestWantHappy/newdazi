# Python 操作题批改稳定化设计

状态：**1.30.88～1.30.92 均已上线**

## 1. 唯一判题事实

最高分正式提交由 `ProgrammingJudgeMapper.selectBestFinalSubmission` 和批改列表子查询共同遵守：

```sql
WHERE submission_kind = 'SUBMIT'
  AND score IS NOT NULL
  AND status_code IN (
    'ACCEPTED','PARTIAL','WRONG_ANSWER','SYNTAX_ERROR',
    'RUNTIME_ERROR','TIME_LIMIT','MEMORY_LIMIT'
  )
ORDER BY score DESC, judged_at DESC, submission_id DESC
LIMIT 1
```

使用终态白名单而不是 `NOT IN` 排除，避免历史 NULL 或未来未知状态被误当成有效最高分。

## 2. 代码与正式成绩边界

`BizStudentAnswerMapper.xml` 的批改查询直接返回 `ps.source_code AS systemSourceCode`。前端 `TeacherPythonSubmissionPreview` 优先显示该字段，复制和下载也复用它；答题表代码只作旧数据兼容回退。

Python 提交写回答答表仍使用 `upsertProgrammingAnswer`，但语义调整为“教师确认制”：

- 首次或待批阶段：同步最高分提交代码、提交时间与用时，`score` 保持 NULL。
- 教师已评分后：保留被评分的代码与时间，不为后续重交悄悄替换版本。
- 系统判题分继续保存在最高分提交记录中，供批改页展示和批量采用。

这样教师看到的“系统参考分”和“正式成绩”是两个状态，学生提交不会自动消耗批改任务。

## 2a. 教师运行查看

`POST /business/teacher/grading/python/{submissionId}/run` 按提交 ID 读取正式 `SUBMIT` 源码，经学生档案还原课程班级并复用教师课程班级权限校验，再进入 `PracticalPythonRunService` 的隔离运行限制。

运行不写 `biz_programming_submission`、`biz_student_answer` 或任务状态；前端切换学生时丢弃迟到响应并清空上一名学生输入与输出。

## 3. 无答题行补建

批改列表的 `submitted` 以“有非空答题内容或存在正式编程提交”为真；题目、班级和学校的连接仍由原查询限定。

批量采用遇到 `answerId == null` 时：

1. 先锁定该生课程题任务状态并检查是否退回。
2. 重新读取当前最高分提交，核对与页面快照一致。
3. 用正式提交代码构建答题行，执行 `INSERT IGNORE`。
4. 再按学生/课程/题目加锁读取答题行；若并发提交已插入，则本轮跳过。
5. 写分、清空分项评分明细并标记任务状态 `GRADED`。

## 4. 批量采用事务与并发

批量接口保持单个 Spring 事务，但把“整班不可回滚”的个案异常改为跳过计数：

- 有答题行：`selectByIdForUpdate` 加行锁，比较分数、提交时间、作品版本和答案内容。
- 1.30.90 起前端提交页面加载时的学生快照；后端逐人比对 `answerId`、分数、答案内容、作品版本、系统提交 ID 和系统分，过期快照直接跳过。
- 核对当前最高分提交 ID 与分数，防止学生刚重交后把旧系统分写给新版本。
- 已退回、学生档案缺失、无系统分、分数越界、满分配置 NULL/负数均跳过。
- 单人异常不抛出整班异常；数据库/事务级异常仍整体回滚，避免半写入。

## 5. 查询索引

`sql/python_grading_stability_20261109.sql` 增量添加：

```sql
KEY idx_programming_submission_best
  (student_id, lesson_id, question_id, submission_kind, score, judged_at, submission_id)
```

130 MySQL 8.0.46 实测 `EXPLAIN FORMAT=JSON` 命中该索引，`using_filesort=false`，按索引反向扫描取得最高分。索引仅增加，不回填或重算业务数据。

## 6. 窄屏布局

批改页在 `max-width: 900px` 下：

- 页面高度改为自动，允许整页纵向滚动。
- 筛选控件整行显示，批改期限信息纵向排列。
- 学生列表固定约 220px 高，内部滚动。
- 代码预览与打分区整行排列，评分按钮可正常触控。
- Python 组件在 `max-width: 600px` 下把系统判题栏和元信息改为上下排列。

桌面宽度保持原三栏结构。

## 7. 回退影响

- 1.30.88：后端可回退到 1.30.87 JAR；新增索引可保留。已补建的答题行属于正式业务数据，不应为回退删除。
- 1.30.89：只切回 1.30.88 的前端根并 reload Nginx，不重启后端、不回滚数据库。
- 1.30.90：恢复该版本备份的 `service.before`/`nginx.before`，重启后端并 reload Nginx，退回 1.30.89 前端与 1.30.88 后端；更新 168 转 DRAFT，不整库回灌。

