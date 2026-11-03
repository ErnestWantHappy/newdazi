# 系统诊断与性能热点治理：设计（2026-10-28）

## 1. 慢 SQL 持久化根因

`SlowSqlInterceptor` 原先只声明 `Executor.query` 的 4 参数签名；PageHelper 在外层拦截器里直接调用 6 参数 `query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql)`，导致所有经过 PageHelper 的查询绕过慢 SQL 拦截。修复为同时声明 4 参数、6 参数 `query` 和 `update` 三个签名，并保留静态调用计数用于自检。详见 ADR-003。

## 2. 诊断口径

- Druid 慢 SQL 按 `maxTimespan` 排序，`totalTimeMs` 只作展示；标注“进程内统计，自后端启动起（重启清零）”。
- 异常查询取窗口内最近 200 条，Java 侧拆成系统异常（critical/warning）和业务提示（business/info 聚合，含次数、首末时间、用户数）。
- 预览失败拆成累计值和近 7 天值，并按 `preview_error_message` 输出 Top5。
- JVM 健康判断改用 `used/max`，committed 只作展示。
- 新增 `POST /monitor/diagnosis/perf-event/self-test`，用 MyBatis `SELECT SLEEP(1.1)` 验证“拦截器 → 异步 → sys_perf_event”整链路。

## 3. 资源鉴权

`selectStudentAnswerIdByResource` 原先用 `OR` + `UNION ALL` + `JSON_CONTAINS`，`biz_student_answer.student_answer` 是 TEXT、`preview_path` 无索引，稳定全扫。改为 5 条查询：

1. `student_answer(191)` 前缀索引；
2. `preview_path(191)` 前缀索引；
3. 附件 `resource_path`（原已有索引）；
4. 附件 `preview_path(191)`；
5. 仅前四条全部未命中时才走 `JSON_CONTAINS` 页图兜底。

四条命中时取最大 `answer_id`，保持原“最新归属”语义。本轮不加资源归属缓存，避免授权失效风险。

## 4. 班级统计

- `TeacherDashboardController` 改调 `getStatusForDashboard`，服务内 45 秒进程内缓存；`assertCanGrade` 仍走无缓存 `getStatus`。
- 教师首页轮询 10 秒 → 60 秒；提交/批改后仍主动刷新。
- `selectClassStatusByLesson` 把两段相同的 `MAX(answer_id)` 派生表收敛为一个 CTE。
- `selectClassMetrics` 的单次扫描重写（B2b）暂缓，先以缓存+降频+覆盖索引观察真实收益，避免统计口径风险。

## 5. 索引

`sql/diagnosis_perf_indexes_20261028.sql`，幂等、前检、在线 DDL、附回滚：

- `biz_student_answer(preview_path(191))`
- `biz_student_answer(student_answer(191))`
- `biz_practical_attachment(preview_path(191))`
- `biz_student_answer(lesson_id, student_id, submit_time)`
- `biz_student_answer(lesson_id, student_id, question_id, answer_id)`

`biz_student.user_id_unique`、`biz_practical_grading_deadline.uk_practical_deadline_class`、`biz_practical_attachment.resource_path` 已存在，不重复新增。

## 6. 慢接口与导入

- 学生删除：`purgeFileService.deleteUnreferenced` 在事务提交后提交到通用线程池，线程池拒绝时退回同步。
- 教研图片：登录展示图生成改异步预热，读路径保留按需补生成。
- 题库导入：错误列表改为 `{row, reason}` 结构，通过 `ServiceException.data` 返回；全局异常处理器把它放到 `AjaxResult.details`；前端表格展示并支持 CSV 下载，提交按钮加 loading 与 in-flight 守卫。
- 题库导出、321 条失败分类、updatePwd 核实、平台概览 5 分钟刷新按剩余事项处理，不在本轮强改。

## 7. 兼容与安全

- `ServiceException` 新增可选 `data` 字段，`msg` 保持兼容。
- 资源鉴权边界：四条索引查询命中时不跑 JSON 兜底；同一路径多引用的极端场景需在测试中记录。
- 45 秒缓存只用于首页红点，批改锁定不缓存。
