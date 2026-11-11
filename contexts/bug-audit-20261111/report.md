# 并发 / 事务 / 限流专项复核与修复报告

> 日期 2026-11-11 · 依据 `contexts/bug-audit-20261111/review-prompt.md`
> 阶段一：**只读复核完成**（§0–§7，当时未修改任何业务代码，数据库访问全部 `SELECT`/`SHOW` 并显式 `rollback()`）。
> 阶段二：用户指示「现在修复吧」后**已修复并本机验证**（§8）。§1–§7 保留复核当时的原始结论与行号，不按修复后代码回改。
> 两阶段均**未连 130/123 正式环境**、未发布、未升级产品版本、未 `git add/commit/push`。

---

## 0. 现场实测基线（先于任何结论）

命令与原始输出见 `output/stress/concurrency-audit-20261111/RESULT.md`。

| 项 | 实测值 | 对判定的影响 |
| :--- | :--- | :--- |
| MySQL | 8.0.39，`localhost:3306/xueyeceping_server_20260729` | 只本机，未连 130/123 |
| 隔离级别 | **REPEATABLE-READ**（会话与全局一致） | **推翻 H8 的 READ_COMMITTED 前提** |
| 全局隔离覆盖 | 全仓无 `setTransactionIsolation` / `default-transaction-isolation` | 裸 `@Transactional` 继承 RR |
| `innodb_lock_wait_timeout` | 50 秒 | 并发测试锁等待上限 |
| `innodb_deadlock_detect` | 1 | 死锁会回滚一方而非静默错乱 |
| `max_connections` | 151（Druid `initialSize: 20`） | 并发连接数需受限 |
| `uk_student_lesson_question` | 存在；重复组 **0**；`question_id IS NULL` 行 **0** | H6 唯一键兜底成立 |
| `uk_lesson_student` | 存在；重复组 **0** | H9 签到幂等有 DB 兜底 |
| `uk_programming_submission_key` | 存在（student+lesson+question+key）；重复组 **0** | 重复提交有 DB 兜底 |
| `uk_practical_artifact_context` / `uk_practical_version_no` | 均存在 | 作品版本链有 DB 兜底 |
| 免抽测 4 张表唯一键 | 均存在（表名是 `biz_exemption_*`，不是 `biz_teaching_exemption_*`） | H7 并发同键由 DB 兜住 |
| `biz_lesson_tool` | **0 行**；`lesson_id` 有**非唯一**索引（仅 PRIMARY 唯一） | H8 必须造夹具；RR 下 DELETE 取 next-key 锁 |
| `biz_programming_submission` | **0 行** | 本机从未跑过真实判题提交 |
| `biz_python_practice_submission` | **0 行** | 刷题链路同样无运行时证据 |
| `biz_lesson_checkin` | **1 行** | 签到并发测试必须造夹具 |
| `biz_student_answer` | 173686 行 | 基线，跑前跑后比对用 |
| `biz_student_task_state` | 169501 行 | 同上 |
| `biz_practical_artifact` / `_submission_version` | 12733 / 12733 | 同上 |
| `biz_student_answer_orphan_archive` | 2388 行 | 同上 |
| 单班最大学生数 | **50 人** | 具备整班 45 人并发夹具条件 |
| `biz_lesson.lesson_scope IS NULL` | **0 行** | 新发现 N2 在本机不可触发 |
| `judge0.mode` | `disabled`（`application.yml:141-155` 全部可环境变量覆盖） | H2/H10 运行时验证需开 mock/http |
| 后端 8080 | 未监听 | HTTP 并发测试需先起后端 |
| **发布制品同一性** | 本机 `RuoYi-Vue/ruoyi-admin/target/ruoyi-admin.jar` SHA-256 = `44b937814598ce970f47b448f28200ae5c7cc74217b6754c9a9549c54419b24d`，**与 130 已上线 1.30.94 制品完全一致**；构建时间 2026-11-11 08:12:32，`ProgrammingSubmissionService.java`（11-10 17:57）与 `PracticalPythonRunService.java`（11-10 16:40）均**早于构建** | **本轮确认的缺陷存在于 130 线上运行代码中**，不是仅本机在研问题 |

---

## 1. 已确认问题（P1 → P3，同一根因已合并）

### [P1] Redis 班级计数器「GET 判空后 DECR」非原子，漂移成负值后永不过期，班级并发上限被永久放宽

- **等级 / 证据级别**：P1 / **已复现**
- **位置**：
  - `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/service/ProgrammingSubmissionService.java:666`（`releaseReservation`）
  - 同文件 `:416-417`（`releaseInteractiveRun`）、`:437`（`releaseRunConcurrency`）——三处完全同构
  - 预留侧 `:646`（TTL 180s）、`:405` 与 `:427`（TTL 60s），均只在 `active == 1` 时 `expire`
- **涉及数据**：Redis `judge0:active:class:<deptId>:<entryYear>:<classCode>`、`judge0:run:class:<deptId>:<entryYear>:<classCode>`
- **触发条件**：任意学生；班级桶键在「释放线程 GET 判空」与「DECR」两条命令之间到期。班级桶 TTL 只有 60s / 180s，一节课内自然到期多次。
- **实际结果（已复现）**：
  - 定向时序：`INCR=1,TTL=60` → GET 非空 → 键到期 → `DECR` 把键**重建成 -1 且 TTL=-1** → 此后 4 轮预留/释放在 `0 ↔ -1` 之间振荡，`INCR` 永不等于 1 → `expire` 永不重设 → **immortal key**。
  - 并发闭环（16 线程 × 200 轮，`threading.Barrier` 同时出发，TTL 放大到 3ms）：
    - 现网模式：取得 3022 次，**结束值 -16，TTL=-1，immortal=True → 失败**
    - 原子对照组（Lua）：取得 1291 次，结束值 None，TTL=-2，immortal=False → 通过
  - 证据：`output/stress/concurrency-audit-20261111/h1-redis-counter-race.json`
- **预期结果**：班级桶在全部请求归还后归零或消失，TTL 不为 -1；并发上限 8 始终有效。
- **根因**：`:666` 的 `if (get(classKey) != null) decrement(classKey)` 是**两条独立命令**。Redis 的 `DECR` 对不存在的键会创建值为 -1 的新键且**不带 TTL**；而预留侧 `:646` 只在 `active == 1` 时设 TTL，计数变负后该条件永不成立，键遂永不过期。
- **误报排除（查过哪些兜底、为什么没兜住）**：
  - `releaseSlot:656` 的 `judge0:released:submission:<id>` SETNX 幂等标记：只保证「同一提交最多归还一次」，**不保证 GET/DECR 原子**，且**运行链路（`:413`/`:434`）根本没有这个标记**。
  - DB 兜底：`ProgrammingJudgeMapper.xml:66` 的 `countActiveClassSubmissions` 本可按真实在判行数校正，但**全仓 grep 确认无任何调用方（死代码）**，兜底不存在。
  - TTL 自愈：只在键正常过期时成立；漂移后 TTL=-1，**永不自愈**。
  - 前端闸门：无。
- **影响面**：负值使 `active > classConcurrency` 永不成立 → 整班可同时把 Judge0 打满，进而拖垮正常判题与教师预览。**不直接写错成绩**，故定 P1 而非 P0。需重启 Redis 或手工删键才能恢复。
  **该缺陷已在 130 线上**：本机 JAR 与已发布 1.30.94 制品 SHA-256 完全一致（见 §0），且 `PROJECT_CORE.md` 记载的 130 线上验收中教师运行接口返回了真实 Python 执行结果（`needInput=true` + 提示语回放），说明 130 的 Judge0 已启用 → 触发条件在生产**真实可达**，不是仅本机理论风险。
- **最小修复建议**：把「判空 + 递减 + 归零删除」合成一条 Lua 原子执行；预留侧改为无条件 `EXPIRE`（或 `SET ... EX` 初始化）而非仅 `count == 1`。三处 release 一并改。
- **回归测试**：待授权，计划 `T/service/ProgrammingClassCounterConcurrencyTest.java`（见 §5）。本轮证据脚本已可重复运行。
- **同类位置**：`:416-417`、`:437` 与 `:666` 同构；`releaseReservation` 还被 `:277`/`:289`/`:293` 三条失败分支调用，触发面更大。

### [P1] 学生运行锁 TTL 短于判题最长耗时、释放无 owner 校验，且交互锁与预览锁互不互斥

- **等级 / 证据级别**：P1 / **代码证实**（运行时级联待实测）
- **位置**：
  - `ProgrammingSubmissionService.java:401`（`judge0:interactive:<sid>` TTL **10s**）、`:425`（`judge0:run:student:<sid>` TTL **60s**）
  - 同文件 `:414`、`:435`（`deleteObject` **无条件删除，无 owner 校验**）
  - 同文件 `:490-497`（`executeWith` 轮询）、`:338-350`（`runPublicCases` 串行跑全部公开测试点）
  - `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/judge/Judge0Properties.java:9-21`（默认值）
- **涉及数据**：Redis `judge0:interactive:*`、`judge0:run:student:*`、`judge0:run:class:*`
- **触发条件**：学生点「运行」→ 判题耗时超过锁 TTL → 学生再点。
- **实际结果（推演，数字来自代码）**：
  - `poll-interval-ms=500` × `max-polls=24` → `executeWith` **最少 12s**，已 **> 10s 交互锁 TTL**；
  - `read-timeout-ms=4000` → 最坏 24 × (0.5 + 4) ≈ **108s**；
  - `validateTestCases:607` 允许最多 **50** 个测试点，`runPublicCases` 串行执行 → 公开点多时远超 60s 预览锁 TTL。
  - 级联：t=0 第一次拿锁 → t=10 锁自动过期 → 第二次拿锁 → t=12 第一次的 `finally` **删掉第二次的锁** → 第三次又能进。
  - **额外绕过**：`judge0:interactive:<sid>` 与 `judge0:run:student:<sid>` 是**两把不同的锁**，同一学生可同时持有 → 「上一次运行还没结束」形同虚设；两者却共用同一 `judge0:run:class:*` 桶，班级额度被双倍消耗。
- **预期结果**：同一学生任意时刻只有一个有效运行；释放只能删自己的锁。
- **根因**：`:401` 把 TTL 写成固定 10s，未按「测试点数 × 单点最坏耗时」取上界；`:414`/`:435` 用 `deleteObject(key)` 而非「比对锁值再删」，锁过期后误删他人锁。
- **误报排除**：
  - `releaseSlot:656` 的幂等标记**只在提交链路**，运行链路没有。
  - `PracticalPythonRunService.java:70` 的 `finally { if (acquired) slots.release(); }` **释放正确，不是许可泄漏**——该文件的问题只是容量/公平性（见 §3）。
  - `resolveClient:499-503` 在 `disabled` 时直接抛错，所以本机当前不会触发；一旦生产开 `http` 即暴露。
- **影响面**：限流被绕过、判题机被打满、整班运行大面积「繁忙」。不写成绩。
- **最小修复建议**：锁值写唯一 token，释放用 Lua 比对 token；TTL 按最坏耗时上界设定或改可续期；同一学生统一一把锁（交互与预览共用）。
- **回归测试**：待授权，计划 `T/service/ProgrammingRunLockConcurrencyTest.java`（注入可控慢 `Judge0Client` + 真实 Redis）。
- **同类位置**：`reserveRateAndSlot:645` 的学生提交锁 TTL 120s，同样短于 50 个测试点的最坏耗时，且 `releaseReservation:664` 也是无条件删除。

### [P2] 分钟速率配额在失败时不回退，失败请求消耗成功额度

- **等级 / 证据级别**：P2 / **代码证实**
- **位置**：`ProgrammingSubmissionService.java:644`（`judge0:rate:<sid>:<minuteBucket>` INCR + 70s TTL + 上限 10）、`:663-667`（`releaseReservation` 只删学生锁、递减班级桶，**从不递减 rateKey**）、`:289` 与 `:293`（失败分支调用 `releaseReservation`）
- **涉及数据**：Redis `judge0:rate:<studentId>:<分钟桶>`
- **触发条件**：Judge0 不可用 / 服务异常 / 重复键冲突，学生连续重试。
- **实际结果（推演）**：10 次全部失败的请求把配额吃满 → 第 11 次即使服务已恢复，仍被「提交过于频繁，请稍后再试」拒绝，最长等 70s。
- **预期结果**：失败不消耗成功额度，或至少失败后回退计数。
- **根因**：`:644` 在**判题之前**先 INCR 计数，而 `:663-667` 的释放路径完全没有 rateKey 的对称递减。
- **误报排除**：rateKey 按 `System.currentTimeMillis() / 60000` 分桶且 TTL 70s → **会自愈**，不是永久卡死，故 P2 不是 P1。
- **影响面**：判题服务抖动期间学生被误拒，体验受损；不影响成绩。
- **最小修复建议**：失败分支同时递减 rateKey；或改为「提交成功后才计数」。
- **回归测试**：待授权，计划 `T/service/ProgrammingRateQuotaConcurrencyTest.java`。

### [P2] 判题完成 / 恢复路径二次加载学生档案，档案缺失或学校变更时归还到错误的班级桶（清单外新发现 N1）

- **等级 / 证据级别**：P2 / **代码证实**（触发需特定数据，待实测）
- **位置**：
  - `ProgrammingSubmissionService.java:483`（worker `finally`）、`:452`（重启恢复）→ `:648-650`
  - `:649` `studentMapper.selectBizStudentByStudentId(studentId)`；`:650` **`if (student != null)` 才释放**
  - `RuoYi-Vue/ruoyi-business/src/main/resources/mapper/business/BizStudentMapper.xml:79-82` 与 `:25-30`（`left join sys_user` 且 `where u.del_flag = '0'`）
  - `ProgrammingSubmissionService.java:669` / `:439`（桶键含 `s.getDeptId()`）
- **涉及数据**：`biz_student` + `sys_user.del_flag` / `sys_user.dept_id`；Redis 班级桶
- **触发条件**：学生提交后、判题完成前，账号被软删（`del_flag='2'`）或转学（`dept_id` 改变）。
- **实际结果（推演）**：
  - 软删：`selectBizStudentByStudentId` 因 `u.del_flag='0'` 过滤返回 **null** → `:650` 条件不成立 → **整个释放被静默跳过**（班级桶、学生锁、slot 键都不归还）。
  - 转学：递减的是**新学校**的桶，旧学校桶泄漏、新学校桶被减成负值。
- **预期结果**：槽位归还只依赖提交时固化的班级标识，与档案后续变化无关。
- **根因**：`:649` 在释放时**重新查档案**取 dept/entryYear/classCode，而不是用提交时已确定并随提交记录保存的值；`:650` 又把「查不到」当成「不用释放」。
- **误报排除**：班级桶有 180s / 60s TTL，正常情况下会自愈 → 单独看是 P2。**但与问题 1 叠加（桶已 TTL=-1）后泄漏永久化**，这才是升级路径。`slotKey` 有 180s TTL、学生锁有 120s TTL，均自愈。
- **影响面**：条件性、需特定数据；不直接影响成绩；与问题 1 叠加可致整班判题长期不可用。
- **最小修复建议**：释放用的 dept/entryYear/classCode 在提交时固化（随 submission 行或 Redis 值保存）；档案缺失也必须归还槽位。
- **回归测试**：待授权，需造夹具改 `del_flag` / `dept_id`（**属写库，须用户明确授权**）。

### [P2] `assertStudentQuestionAccess` 在非事务调用方里执行 `FOR UPDATE`，锁实际不生效（清单外新发现 N2）

- **等级 / 证据级别**：P2 / **代码证实**
- **位置**：`ProgrammingSubmissionService.java:584`（`selectCurrentAssignmentForUpdate`）；调用方 `submit:260`（**无 `@Transactional`**）、`runPublicCases:321`、`cancel:441`
- **涉及数据**：`biz_lesson_assignment`
- **触发条件**：学生提交 / 运行 / 取消的同时教师推进课程。
- **实际结果（推演）**：MyBatis 的 `FOR UPDATE` 只在事务内有效；无事务时语句自动提交，锁瞬间释放 → 这层「当前课复核」退化为普通读，与推进之间存在窗口。
- **预期结果**：要么真加锁，要么明确说明锁在别处。
- **根因**：`:584` 用了带 `for update` 的 Mapper（`BizLessonAssignmentMapper.xml:161-164`），但 `:260` 的 `submit` 没有事务注解，锁语义与调用上下文不匹配。
- **误报排除（关键）**：**真正的互斥在 `completeInTransaction:544-546`** —— 判题回写前在事务内重新 `selectCurrentAssignmentForUpdate` + `canWriteAnswer`。所以旧课写入最终被拦住，**不是 P0**。另 `BizStudentAnswerMapper.xml:104-123` 的 `upsertProgrammingAnswer` 用 `IF(score IS NULL, ...)` + `score = score`，教师批过后异步判题**不能覆盖代码或分数**。
- **影响面**：锁语义误用，给后续维护者「已加锁」的错觉；当前无成绩错误。
- **最小修复建议**：把提交入口纳入事务，或把该处改成普通读并注释「锁在回写侧」。
- **回归测试**：待授权，计划用 SQL 文本锁存在性断言 + 真实两事务验证回写侧兜底。

### [P3] 两处「当前课」判定对 `lesson_scope IS NULL` 口径相反（清单外新发现 N3，本机不可触发）

- **等级 / 证据级别**：P3 / **代码证实**
- **位置**：`RuoYi-Vue/ruoyi-business/src/main/resources/mapper/business/BizLessonAssignmentMapper.xml:133`（`l.lesson_scope='regular'`，NULL 被排除）对比 `:152`（`ifnull(l.lesson_scope,'regular')='regular'`，NULL 被纳入）
- **实际结果**：`lesson_scope IS NULL` 的课程会被非锁定读判为「无当前课」、被锁定读判为「有当前课」，两层闸门结论相反。
- **为何难触发**：本机实测 `biz_lesson.lesson_scope IS NULL` = **0 行**，`status IS NULL` = **0 行**。需历史数据或未来写入才可能出现。
- **最小修复建议**：两处统一为 `ifnull(...)` 口径。

### [P3] 免抽测 `@Transactional` 缺 `rollbackFor`，与项目其余写法不一致（H7 降级，非 Bug）

- **等级 / 证据级别**：P3 / **代码证实**
- **位置**：`RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/service/ExemptionApplicationService.java:51`、`:148`、`:164`
- **误报排除**：全文件 grep 确认**无 `throws`、无受检异常**，调用链全为 `RuntimeException` → Spring 默认回滚规则已覆盖，**功能等价**。唯一键实测齐全（`uk_exemption_application(dept_id,teacher_id,academic_year,semester,grade)` 及班级/课程快照唯一键），并发同键申请由 DB 兜住。
- **结论**：只是写法不一致的健壮性问题，**不报 P0/P1**。

---

## 2. 被兜底排除（明确不按 Bug 处理）

| 假设 | 排除依据（实测 + 代码） |
| :--- | :--- |
| **H6 两跳「先查后锁」** | `uk_student_lesson_question` 实测存在、重复组 0、`question_id IS NULL` 行 0 → 不可能出现「新答案行」。`PracticalArtifactService.java:200` **先锁 artifact 行**、`:203` 比 `expectedVersionId`、`:212` **锁内重读答案**、`:215-227` 全部用锁内值判定；`ClassroomTaskStateController.java:128` 锁内重读 + `:132-134` 比 `expectedScore`/`expectedAnswer`。**唯一键 + 行锁 + 锁内重读 + 快照比对四重兜住**。残留仅「行被删时提示语不准」（`:129` 报「尚未批改」），P3 文案问题。 |
| **H8 工具先删后插产生并集** | 隔离级别实测 **REPEATABLE-READ**（提示词的 RC 前提不成立），`BizLessonServiceImpl.java:560` 与 `StudentToolServiceImpl.java:163` 都是裸 `@Transactional` → 继承 RR；`biz_lesson_tool.lesson_id` 是**非唯一索引** → `delete ... where lesson_id=?` 在 RR 下取 next-key/gap 锁，并发替换被串行化，**不会并集**。残留风险是死锁（`innodb_deadlock_detect=1`，一方回滚报错）而非静默半集 → 移入 §4 待实测。 |
| **H9 签到 `INSERT IGNORE` 静默** | `uk_lesson_student(lesson_id,student_id)` 实测存在、重复组 0 → 幂等有 DB 兜底。`AGENTS.md` 已记录 `2020710101` 的 0 题考勤课签到与重复签到验证通过，**不重复报为 Bug**。 |
| **H10 教师运行接口越权** | `TeacherGradingController.java:40-43` 类级 `@PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('admin')")`；`:639-656` `assertLessonVisibleToCurrentTeacher` 对 `lessonId == null` **直接抛错**、校验学校 `deptId`、creator/admin/任课三选一。`:291` 另走 `guideSheetAccessService.assertCanViewLessonClass`。**越权假设排除**。 |
| **迟到判题覆盖已批成绩** | `BizStudentAnswerMapper.xml:104-123` `upsertProgrammingAnswer` 的 `answer_time/submit_time/student_answer` 全部 `IF(score IS NULL, VALUES(...), 原值)`，且 `score = score` → 教师批过后异步判题**无法覆盖代码或分数**。 |
| **`PracticalPythonRunService` 许可泄漏** | `:37-71` `acquired` 标志 + `finally { if (acquired) slots.release(); }`，异常路径（`:40` 未取到、`:50` 超时、`:66` 中断）均正确 → **无泄漏**。 |

---

## 3. 已知设计取舍（不按 Bug 处理，但需产品决策）

1. **H3 `classSlotKey` / `classRunKey` 忽略 `lessonId`、`questionId`**
   `ProgrammingSubmissionService.java:669` 与 `:439` 的签名保留两个未使用参数。注释 `:394-395`「同时保留整班并发上限，避免降级班级把判题机打满」、`:420`「必须按学生和班级双重限流」→ **有意按班限流，不是 Bug**。
   建议：清理误导性签名（P3 整洁），否则后续维护者会以为按题隔离。

2. **H5 教师预览运行的容量取舍**
   `PracticalPythonRunService.java:23-28` `MAX_CONCURRENT_RUNS=8`、非公平 `Semaphore`、`:24` `SLOT_WAIT_SECONDS=3`、`:47` 20s deadline。
   算术：8 许可 × 最长 20s → 最坏吞吐 **0.4 次/秒**；45 人同时点运行，约 **37 人在 3s 内被拒**「运行服务繁忙」。
   这是**容量设计取舍**，释放逻辑正确。但是否满足 `PROJECT_CORE.md` 期望的「整班 45 人降级到服务器运行」需产品决策：要么提高许可数、要么排队而非快速拒绝、要么前端做退避重试。
   另：该服务与学生判题共用 `judge0HttpClient`（`:26`），**教师预览与学生判题竞争同一 Judge0 容量且无全局上限**。

---

## 4. 未测风险（不与已确认混列；写明需要什么证据）

| 风险 | 需要什么证据才能确认 | 当前障碍 |
| :--- | :--- | :--- |
| H2 锁过期级联的运行时实证 | 真实 Redis + 可控慢 `Judge0Client`，观测 TTL 到期后二次获取与误删他人锁 | `judge0.mode=disabled`；`Judge0MockClient` 约 0.5s 返回，**太快，无法自然触发 10s 过期** → 需注入慢桩 |
| H8 RR gap 锁是否真串行化 / 是否死锁 | 真实 MySQL 两事务并发替换同一 `lessonId` 的不同工具集，记录最终集合与死锁日志 | `biz_lesson_tool` **0 行**，必须造夹具 |
| H10「不写成绩」承诺 | 三接口跑前跑后对 `biz_student_answer`、`biz_programming_submission`、`biz_student_task_state`、题目期望输出列做**逐行/哈希比对** | `biz_programming_submission` 与 `biz_python_practice_submission` 均 **0 行**；需造夹具 + 开 mock/http |
| H10 套壳标记伪造 | 学生代码自行打印 `JudgeSourceWrapper` 不可见标记，构造含标记字符 / 相同提示文字 / `\r\n` / Unicode 的输出，验 `plain()`/`visible()`/`terminalText()` | 需真实 Python 执行 |
| H10 加权分一致性 | 课程链路与刷题链路同题同权重对比；权重 0/NULL/负数、总权重 0 的分母 | 需真实执行 + 造题 |
| H10 等待输入判态 | 多轮 `input()` 程序是否被误判 `RUNTIME_ERROR`；`needInput` 与 `success` 组合 | 需真实执行 |
| N1 软删 / 转学触发 | 造夹具改 `sys_user.del_flag` / `dept_id`，观测槽位归还 | **属写库，须用户明确授权** |
| H11 四条回归 | 真实 MySQL + 真实 HTTP 并发（12 并发提交 / 2 教师改分 / 学生并发重交 / 批量采用期间重交） | 后端 8080 未监听；需造夹具 |
| 问题 1 在生产的实际漂移量 | 130 只读 `redis-cli` 查 `judge0:active:class:*` / `judge0:run:class:*` 的值与 TTL，统计 TTL=-1 或负值的键 | **本轮未连 130**；需授权只读探查 |

---

## 5. 本轮新增测试清单（计划，**等用户确认后再写**）

### 5.1 已完成的只读证据脚本（本轮唯一新增代码）

| 路径 | 命令 | 本轮结果 | 覆盖 |
| :--- | :--- | :--- | :--- |
| `output/stress/concurrency-audit-20261111/h1_redis_counter_race.py` | `python -X utf8 h1_redis_counter_race.py` | **失败即证据**：现网模式毫秒 TTL 并发结束值 -16 / TTL=-1；原子对照组通过 | 问题 1 |
| `output/stress/concurrency-audit-20261111/mysql_readonly_baseline.py` | `python -X utf8 mysql_readonly_baseline.py` | 通过：唯一键齐全、重复组 0、隔离级别 RR、基线计数已固化 | H6/H7/H8/H9 前提 |
| `output/stress/concurrency-audit-20261111/artifact_identity.py` | `python -X utf8 artifact_identity.py` | 通过：本机 JAR 哈希 = 130 发布哈希，5 个被审计源文件全部早于构建 | 缺陷是否已在生产 |
| `output/stress/concurrency-audit-20261111/post_verify.py` | `python -X utf8 post_verify.py` | 通过：Redis 测试键残余 0、业务 judge0 键 0、MySQL 14 张表计数逐项一致 | 清理与基线未变 |

四者均**不写业务数据**：Redis 只用独立前缀 `bugaudit:h1:`，MySQL 只 `SELECT`/`SHOW` 并显式 `rollback()`，制品核对只读本机 JAR 与文件时间戳。

### 5.2 计划新增（授权后执行）

> **阶段二状态更新**：下表前三行（问题 1/2/3）已由 `RedisCounterAtomicityTest` + `ProgrammingRunLockContractTest` 覆盖并通过，实际落地文件与断言见 §8.2；其余各行（真实 MySQL / HTTP 并发）**仍未执行**，需要 §5.3 的授权。

| 文件 | 类型 | 覆盖 | 并发数与同步点 | 断言的不变量 |
| :--- | :--- | :--- | :--- | :--- |
| `T/service/ProgrammingClassCounterConcurrencyTest.java` | JUnit + 真实 Redis | 问题 1 | 16 线程 × 200 轮，`CountDownLatch` 同时出发 | 结束后班级桶值 = 0 或不存在，且 **TTL ≠ -1** |
| `T/service/ProgrammingRunLockConcurrencyTest.java` | JUnit + 真实 Redis + 慢 `Judge0Client` 桩 | 问题 2 | 同学生 3 次错峰（0/11/13s）+ 整班 45 | 同一学生任意时刻只有 1 个有效锁；释放**不删他人锁**；交互与预览互斥 |
| `T/service/ProgrammingRateQuotaConcurrencyTest.java` | JUnit + 真实 Redis | 问题 3 | 10 次失败 + 1 次成功 | 失败不消耗成功额度 |
| `T/service/LessonToolReplaceConcurrencyTest.java` | JUnit + **真实 MySQL** | H8 | 2 事务 × 不同工具集，`CountDownLatch` | 最终是**某一个完整集合**，非并集/空集/半集；记录是否死锁；并发读端不得读到 0 行 |
| `T/controller/AnswerSubmitConcurrencyTest.java` | 真实 MySQL + HTTP | H11-1 | 同学生同答案 12 并发 | 答案单行、分数不丢、任务状态一致 |
| `T/controller/TeacherGradeConcurrencyTest.java` | 真实 MySQL | H11-2 | 2 教师同 `answerId` | 只有一人成功或后者被快照拦下，**不出现后写覆盖先写且无提示** |
| `.../practical_resubmit_concurrency.py` | HTTP（复用 `testlib.py`） | H11-3 | 文件作品 / 流程图各 2 并发 | 旧分归档与新版本待批原子；`practicalVersionId` 不悬空；不产生两条正式记录 |
| `.../python_adopt_resubmit_concurrency.py` | HTTP | H11-4 | 批量采用 + 中途重交 | `skippedCount` 精确等于漂移人数，其余正常，**不整班回滚** |
| `.../python_run_no_write_check.py` | HTTP + 逐表比对 | H10-2 | 三接口各跑前后 | 四张表逐行哈希不变 |
| `.../fixtures.py` / `cleanup.py` / `post_verify.py` | 夹具 | 全部 | — | 只删自造数据；后检残余 **0**、基线未变 |

`T/` = `RuoYi-Vue/ruoyi-business/src/test/java/com/ruoyi/business/`。
运行命令（本机环境坑已核对）：

```bash
cd RuoYi-Vue
mvn -pl ruoyi-business -am test "-Dtest=*ConcurrencyTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-DforkCount=0"
```

SQL 文本锁存在性断言参考 `T/controller/LessonAdvanceControllerTest.java:105`（用 `configuration.getMappedStatement(...)` 断言 SQL 真含 `FOR UPDATE`），用于防 N2 类回归。

### 5.3 需要用户决策的三件事

> **阶段二状态更新**：三项**均未获授权、均未执行**。修复阶段没有造任何夹具、没有写本机业务库、没有起后端 8080；问题 2 的 TTL 与 owner 契约用 mock 断言 + 真实 Redis 原子性验证覆盖，**慢 Judge0 桩下的真实锁级联仍未验**。

1. **是否允许造夹具写本机库**（测试课程 / 班级 / 学生 / 工具 / 判题提交），以及是否允许临时改 `del_flag` / `dept_id` 验证 N1。
2. **Judge0 用哪种模式**：`mock` 可跑通链路但约 0.5s 返回，**无法触发 H2 的 TTL 过期**；H2 需注入可控慢桩或本机 `http`。
3. **是否允许起本机后端 8080**（`-Dserver.port=8080` + `TEXTBOOK_ROOT`），HTTP 并发测试的前提。

---

## 6. 与文档不符之处（现场实测值）

1. **提示词 §4 H8 的前提错了**：假设默认隔离级别为 READ_COMMITTED，现场实测 **REPEATABLE-READ**（会话与全局一致，全仓无覆盖配置）。据此 H8 的「并集超 20」风险方向应改为「RR gap 锁串行化 / 死锁」。
2. **免抽测表名**：提示词与部分文档写 `biz_teaching_exemption_*`，实际是 **`biz_exemption_standard` / `biz_exemption_application` / `biz_exemption_class_snapshot` / `biz_exemption_course_snapshot` / `biz_exemption_attachment`**（`sql/teaching_exemption_v1.sql:4,20,60,80,102`）。
3. **`PROJECT_CORE.md` 记 1.30.94「未整班 45 人并发验证」**：现场单班最大 **50 人**，具备夹具条件；但 `biz_programming_submission` 与 `biz_python_practice_submission` 均 **0 行**，说明本机**从未跑过任何真实判题提交**，「代码/接口核对」之外确实没有任何运行时证据。该记载准确，且比文档描述的更空白。
4. **`countActiveClassSubmissions` 是死代码**：`ProgrammingJudgeMapper.java:37` 声明、`ProgrammingJudgeMapper.xml:66` 定义，**全仓无调用方**。文档若把它当作班级并发的 DB 兜底，则与代码不符。
5. **提示词把 H10 框定为「最新未验面」时假定 1.30.94 尚未发布，实际已上线**：`PROJECT_CORE.md` 现记 130 前后端为 1.30.94（`releases/20261111_python_interactive_v1`，产品更新 172/PUBLISHED）。本轮进一步用制品哈希确证审计源码即线上代码（§0 末行）→ **问题 1/2 的优先级应按「已在生产」评估，而非「在研待发布」**。130 的整班 45 人并发仍未验证这一记载准确。

---

## 7. 阶段结论

**已证实最高优先级 Top 3**

1. 问题 1（P1，**已复现**）：班级计数器漂移成负值且永不过期，班级并发上限被永久放宽。
2. 问题 2（P1，代码证实）：运行锁 TTL 短于最长耗时 + 无 owner 校验 + 交互/预览双锁不互斥。
3. 问题 4 + 问题 1 叠加（P2→P1 升级路径）：软删/转学导致槽位归还到错误桶，叠加 immortal key 后泄漏永久化。

**最需要补的证据**：**130 线上 Redis 的班级桶实际值与 TTL**（只读，最高优先）；其次是真实 Redis + 慢判题下 H2 的锁级联、真实 MySQL 两事务下 H8 的 gap 锁行为。

**建议最先做的一条（5 分钟内可做完，只读、零风险）**：经授权后在 130 上执行
`redis-cli --scan --pattern 'judge0:active:class:*'` 与 `judge0:run:class:*`，对每个键取 `GET` + `TTL`，
统计**值为负数或 TTL=-1** 的键。若存在，即现场确认问题 1 已在生产发生，并可按受影响班级清单评估是否需手工删键恢复。
**本轮未连 130，此项待授权。**

**本机替代验证（同样 5 分钟）**：起本机后端与 Redis，用学生账号点「运行」，间隔 11 秒再点一次，同时
`redis-cli TTL judge0:interactive:<studentId>` —— 若第一次请求仍在跑而键已过期/消失，即现场确认问题 2。
（**该键名仅适用于阶段一的线上代码**；阶段二已把交互锁并入 `judge0:run:student:<studentId>`，复验请改看新键。）

**收尾纪律（阶段一）**：只读复核阶段不改业务代码，不升级产品版本、不生成 `biz_platform_update`、不碰 `RELEASE_LOG.md`；未 `git add/commit/push`。阶段二的修复与验证见 §8，同样未发布。

---

## 8. 修复阶段（用户指示「现在修复吧」后执行，2026-11-11）

### 8.1 改动清单

**新增基础设施（纯追加，不改任何既有方法行为）**
`RuoYi-Vue/ruoyi-common/src/main/java/com/ruoyi/common/core/redis/RedisCache.java`

| 方法 | 行 | 作用 |
| :--- | :--- | :--- |
| `expireIfValueMatches` | :86 | 认主续期：锁值仍是本次令牌才 `PEXPIRE`，长任务边跑边续期 |
| `incrementCounterWithTtl` | :112 | 原子 `INCR` + **每次都** `PEXPIRE`；返回值 ≤0 时就地重置为 1，让线上已漂移的负值键自愈 |
| `decrementCounterOrDelete` | :132 | 原子「`EXISTS` 才 `DECR`，减到 ≤0 立即 `DEL`」；键不存在返回 `nil`，**绝不重建** |

两个关键实现约束（都已写进源码注释）：
- TTL 以 `long` 字面量拼进脚本文本，**不走 ARGV**：值序列化器是 `FastJson2JsonRedisSerializer`，会把 `"60000"` 变成带引号的 JSON 字符串，`PEXPIRE` 无法解析。
- 脚本返回类型声明为 `Long`（`ReturnType.INTEGER`），绕开值序列化器的反序列化，结果是数字而不是 `byte[]`。

**业务修复**

| 文件 | 位置 | 对应问题 | 改法 |
| :--- | :--- | :--- | :--- |
| `ProgrammingSubmissionService.java` | `reserveRunConcurrency` :414 / `releaseRunConcurrency` :440 | 问题 1 + 2 | 交互锁 `judge0:interactive:*` **删除**，与示例运行统一到 `judge0:run:student:*`；锁值改为 UUID 令牌；TTL 10s/60s → **120s**；释放改认主删除；班级计数改原子封装 |
| 同上 | `renewRunConcurrency` :434，`runPublicCases` 循环内调用 | 问题 2 | 多个示例串行跑时逐个续期，总耗时不再受单个 TTL 限制，同时线程异常退出后锁最多 120s 自动恢复 |
| 同上 | `reserveRateAndSlot` :658 | 问题 1 + 3 | 三处计数改原子封装；返回本次分钟桶键；学生锁被占 / 班级队列满时就地退还分钟额度 |
| 同上 | `submit` :269 起 | 问题 3 | 接住分钟桶键，三条失败分支（幂等命中、`DuplicateKeyException`、`RuntimeException`）统一退额度；**跨分钟边界不会退错桶** |
| 同上 | `releaseSlot` :685/:694、`releaseSlotInternal` :699、`readSlotClassKey` :710 | 问题 4 | 班级桶键优先读提交时落库的 `judge0:slot:submission:*`，不再二次查档案；**删掉 `if (student != null)` 的静默跳过**，学生锁只依赖提交行上的三个 id，档案缺失也照样释放 |
| 同上 | 槽位键 TTL 常量 :68 | 问题 4 | 180s → **6 小时**：槽位键是归还班级桶的唯一线索，必须活到判题结束，否则读回失败、修复失效 |
| 同上 | `assertStudentQuestionAccess` :594 | 问题 5 | `selectCurrentAssignmentForUpdate` → `selectCurrentAssignment`（无锁），注释写明真正的当前课互斥在 `completeInTransaction` :544 |
| `PythonPracticeService.java` | `reserveInteractiveRun` :591 / `releaseInteractiveRun` :609 / 调用点 :558 | **清单外新发现，同一缺陷模式** | 刷题页交互运行是第 4 处「GET 判空后 DECR」+ 10s 锁 + 无 owner 校验；同样改令牌 + 120s + 原子封装 |

`ProgrammingSubmissionService` 与 `PythonPracticeService` 现在**不再直接触碰 `redisTemplate`**，限流全部收敛到 `RedisCache` 封装；`ValueOperations` 导入已移除。全仓 `.increment(` / `.decrement(` 裸调用**归零**（修复前 4 处）。

**未改**：Mapper XML、`sql/`、表结构、前端、配置、权限模型、`application*.yml`。无 SQL 迁移，无新增配置项，无新增依赖。

### 8.2 验证证据（全部本机）

| 验证 | 命令 | 结果 |
| :--- | :--- | :--- |
| 全量后端单测 | `mvn -pl ruoyi-business -am test -o "-Dsurefire.failIfNoSpecifiedTests=false" "-DforkCount=0"` | **1036 通过 / 0 失败 / 0 错误 / 3 跳过**（基线 1017/0/3，净增 19 条即本轮新增用例，跳过项未变） |
| 真实 Redis 原子性 | `RedisCounterAtomicityTest`（7 条，**0 跳过**说明本机 Redis 真实参与） | 键不存在时归还不重建；每次递增都带 TTL；-16/TTL=-1 的漂移键自愈为 1/TTL=60；归零即删；认主删除/续期不动他人锁；16 线程 × 50 轮并发峰值 ≤ 上限 8 且结束后键消失；16 线程 × 200 轮 + 3ms TTL 无负值永久键 |
| 服务行为契约 | `ProgrammingRunLockContractTest`（12 条） | 交互与预览共用同一把锁；TTL ≥ 120s；只释放自己的令牌、绝不无条件删锁；判题异常也归还班级额度；超限时退还自己刚加的槽位；多示例逐个续期；准入校验不再调 `FOR UPDATE`；提交失败退分钟额度；档案查不到仍按提交当时的桶归还；判题成功不退额度；同一提交重复到达只归还一次 |
| **出厂 Lua 原文**并发复核 | `python -X utf8 post_fix_verify.py` | 从 `RedisCache.java` 解析出真正上线的脚本文本再 EVAL，不手抄：`fixed-ms`(3ms) 与 `fixed-180s` 结束后键均消失、immortal=False → **通过**；同一骨架下旧模式 `buggy-ms` 仍复现 **结束值 -16 / TTL=-1 / immortal=True** |
| 清理与基线后检 | `python -X utf8 post_verify.py` | 测试键残余 **0**、业务 `judge0:*` 键 **0**、MySQL 14 张表计数逐项一致 |

新增证据脚本 `output/stress/concurrency-audit-20261111/post_fix_verify.py` 与结果 `post-fix-verify.json`。
测试只用独立前缀 `bugaudit:junit:` / `bugaudit:fix:` / `bugaudit:h1:`，`RedisCounterAtomicityTest` 逐键跟踪并在 `@AfterAll` 断言清理干净；本机无 Redis 时整类 `assumeTrue` 跳过，不会让 `mvn test` 失败。

**既有测试的同步修改**（因修复改了调用契约，不是放宽断言）：
`ProgrammingPublicRunTest`、`ProgrammingInteractiveRunTest` 的准入桩由 `selectCurrentAssignmentForUpdate` 改为 `selectCurrentAssignment`，限流桩由 `RedisTemplate/ValueOperations` 改为 `RedisCache` 封装；`PythonPracticeInteractiveRunTest` 的 `allowingRedis()/busyRedis()` 同样改桩封装层。三者的业务断言全部保留。

### 8.3 本轮刻意不修的项与理由

| 项 | 理由 |
| :--- | :--- |
| 问题 N3（P3）`lesson_scope IS NULL` 两处口径相反 | 改任一侧都会**变更业务边界**（NULL 究竟算不算常规课），本机 0 行不可触发、130 未知。需先确认口径再动，不顺手改 |
| H7（P3）免抽测 `@Transactional` 缺 `rollbackFor` | 该服务全链路无受检异常，当前写法**功能等价**，属纯风格统一，不在本轮缺陷修复范围 |
| H5 服务端运行 8 许可 × 20s 的容量取舍 | 属产品决策（最坏 0.4 次/秒，45 人班级约 37 人会在 3s 内被拒），不是缺陷 |
| H8 工具替换的 RR gap 锁 | 已排除为「静默并集」，残留风险是死锁而非数据错误；需真实 MySQL 双事务夹具才能定性，本轮未造夹具 |
| 提交路径学生锁 `judge0:active:student:*` 仍是无条件删除 | 该锁有 DB 兜底（`markJudging` 限定 WAITING、`updateSubmissionResult` 限定 WAITING/JUDGING、答案唯一键 + `IF(score IS NULL,...)`），锁过期最坏是多一次并发提交，不会写坏成绩。改认主需要把令牌随提交持久化，属额外设计，留作后续 |

### 8.4 残留风险与下一步

1. **修复尚未发布**：130 线上跑的仍是含缺陷的 1.30.94（§0 制品哈希已确证）。本轮只改本机源码并通过本机验证，**未构建发布制品、未连 130、未升级版本、未写 `RELEASE_LOG.md`、未生成 `biz_platform_update`**。
2. **130 线上 Redis 只读扫描仍待授权**（§7 建议的那一条）：`judge0:active:class:*` / `judge0:run:class:*` / `judge0:practice:class:*` 里值为负或 TTL=-1 的键。修复后的 `incrementCounterWithTtl` 会在下一次预留时**自愈**这些键，因此不必手工删键；但扫描能确认影响面与受影响班级清单。
3. **长判题超出班级计数 TTL 的欠计数**：提交路径班级桶 TTL 仍为 180s（未改），单次判题若超过 180s 且期间全班无新提交，桶会先过期，归还时 `decrementCounterOrDelete` 返回 `nil` 不重建 → 并发被**短暂低估**。这是修复前既有方向（不会造成永久键），本轮未改以免放大泄漏窗口；运行路径已用续期解决。
4. **真实整班并发仍未验证**：本机 `biz_programming_submission` / `biz_python_practice_submission` 均 0 行，从未跑过真实判题提交。整班 45/50 人 + 真实 Judge0 的运行时证据仍缺，需按 §5.3 的三项授权造夹具后补。

---

## 1.30.96 修复结果（2026-11-11 已发布）

任务书 F01–F22 全部完成，无跳过项。本机后端全量 **1074通过/0失败/0错误/3跳过**（基线1040，净增34）；前端构建门禁 login 9、seat-helper 9、question-workbench 26、python-runner 23 全过，另批改刷新14、草稿补存3，共84；`npm run build:prod` 通过。

| 项 | 状态 | 修复要点 | 本轮测试 |
|---|---|---|---|
| F01 | 已修 | 区域抽测超时上传先提交自动交卷再提示；CAS失败仍回滚 | `CountyExamServiceImplTest` 21（新增2，含事务提交/回滚） |
| F02 | 已修 | 自动推进查询/循环跳过归档课 | `LessonAutoAdvanceServiceImplTest` 19 |
| F03 | 已修 | 判题轮询未完成或服务错误不写分/进度 | `PythonPracticeInteractiveRunTest` 9 |
| F04 | 已修 | NEED_INPUT 返回中文RUNTIME_ERROR、无NUL，前端不回填失败项 | `ProgrammingTeacherValidationTest` 7；`questionWorkbenchPage.test.js` 14 |
| F05 | 已修 | `_PlatformNeedInput` 继承 `BaseException` | `JudgeSourceWrapperTest` 8（真实Python） |
| F06 | 已修 | 两处stdin每行带换行，空回车算一行 | `npm run test:python-runner` 23；`JudgeOutputTest` 15；隔离GUI实跑 |
| F07 | 已修 | release清空requestSeq，双ID监听，迟到响应作废 | `interactiveRunner.test.js` 23；隔离GUI延迟4秒实测 |
| F08 | 已修 | 转换队列拒绝退回pending、不加重试，afterCommit逐项隔离 | `PracticalAttachmentConversionFailureTest`+`PracticalArtifactRetryServiceTest` 10 |
| F09 | 已修 | AI任务PAUSED/CANCELLED终态不被覆盖，迟到结果RESULT_DISCARDED | `PracticalAiJobServiceTest` 11 |
| F10 | 已修 | 批改按studentId匹配、同步分数、仅当前学生接受服务端快照 | `gradingRefresh.test.js` 14；隔离GUI default+single |
| F11 | 已修 | 真实answerId、缺快照拒绝/跳过、两事务READ_COMMITTED | `TeacherGradingControllerValidationTest` 4 |
| F12 | 已修 | 槽位写入与入队同保护、失败退槽位和分钟额度、不标课堂已提交 | `ProgrammingRunLockContractTest` 13（含入参状态补正） |
| F13 | 已修 | 学情两处 `plan_scope IN ('extension','legacy')` | `PythonPracticeMapperScopeTest` 1；本机legacy=0 |
| F14 | 已修 | 免抽测排除拓展载体班，四类课程来源限regular | `ExemptionApplicationMapperScopeTest`+`ExemptionApplicationServiceTest` 8；本机拓展班=0 |
| F15 | 已修 | `/collaboration/health` 限 admin/teacher | `CountyExamAndCollaborationAccessTest` 2；本机学生403，线上匿名业务码401 |
| F16 | 已修 | 首次进入先锁参与记录再建卷 | `CountyExamServiceImplTest` 22 |
| F17 | 已修 | 上传归属TTL 60分钟→24小时 | 同上，常量契约 |
| F18 | 已修 | 切题/卸载前用旧课旧题ID补存草稿 | `draftFlush.test.js` 3 |
| F19 | 已修 | PDF失败finally清理临时文件 | `TextbookPdfServiceTempFileTest` 1 |
| F20 | 已修 | 投票理由INSERT/UPDATE五种组合统一 | `TextbookCaseFeedbackVoteSqlTest` 2（含真实MySQL克隆表） |
| F21 | 已修 | resume前回收超时PROCESSING结果 | `PracticalAiJobServiceTest`+`PracticalAiGradingMapperStaleTest` 13 |
| F22 | 已修 | 监管学生明细补学期左闭右开时间窗 | `TeachingSupervisionStudentDetailsWindowTest` 1 |

**未实测/未覆盖**：真实Judge0超时与整班并发、真实Redis故障和线程池饱和、真实legacy/拓展班样本、真实MySQL并发改分、旧浏览器/低配设备、真实模型取消竞态、线上不足输入专项（接口只收questionId）、线上正式学生403（正式学生凭据不可登录；本机学生403已验证）。F09取消后模型抛异常的失败事件仍记FAILED，F12提交刚入库即被取消的极端路径仍可能标课堂已提交，均非阻断。

**发布**：130前后端 `releases/20261111_bugfix_v1`，JAR `2cb253df…`、首页 `1fde9972…`，产品更新174/PUBLISHED；发布脚本登记阶段SSH连接重置，现场确认已切换且健康后单独执行登记SQL，备份与回退见 `contexts/RELEASE_LOG.md`。
5. **未做的验证**：未起本机后端 8080、未跑浏览器冒烟、未跑 HTTP 并发。本轮改动集中在 Redis 限流与准入查询，已由单测 + 真实 Redis 覆盖；UI 与端到端链路待发布前专项。