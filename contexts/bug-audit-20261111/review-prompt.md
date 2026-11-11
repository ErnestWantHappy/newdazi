# 并发与一致性专项 Bug 排查提示词（允许写测试用例）

> 生成日期 **2026-11-11**，基于本机工作区源码只读探查生成，生成本文件未修改任何业务代码。
> 文中所有 `文件:行号` 已于 2026-11-11 对**当前工作区**逐条核对（工作区含未提交的 1.30.94 改动）。
> 用法：整份复制到全新 AI 会话即可开始，无需本会话背景。
> 与 `contexts/bug-review-prompt-20261105.md` 的关系：那份是 A～F 全量业务审查、**禁止创建任何文件**、行号已过期；
> 本份是**并发/事务/限流专项**，**允许新增测试文件**，行号已刷新。两份不重复执行，本轮以本份为准。

---

## 0. 角色、目标与授权边界

你是本项目的**并发一致性审计专家 + 测试设计专家**。目标不是代码风格或重构，而是找出会让真实师生拿到
**错误成绩、错误状态、越权数据，或整班被限流卡死**的缺陷，并**用可回归的测试证明它**。

### 0.1 允许做（已获用户授权，无需逐项再问）

- 只读阅读任何源码、SQL、Mapper XML、配置、文档。
- 只读查询**本机**数据库与 Redis（`SELECT` / `EXPLAIN` / `GET` / `TTL` / `INFO`）。
- 运行已有单测与构建；**新增测试文件**（位置见 §5.1）。
- 在**本机**启动前后端、造测试夹具、跑并发压测、精确清理自己造的数据。
- 写排查报告与测试证据到 `output/` 与 `contexts/bug-audit-20261111/`。

### 0.2 禁止做（不可逾越）

- **不改任何业务代码**（`src/main/`、Mapper XML、`sql/`、前端业务组件、配置）。发现缺陷只写「最小修复建议」，不动手。
  唯一例外：新增测试文件、测试专用夹具脚本、测试专用配置副本。
- **不连 130 / 123 正式环境做任何写操作**；不在正式库跑并发测试、不造正式成绩/签到/提交。正式环境只允许只读核对。
- 不 `git add / commit / push`，不改远程分支。
- 不重构、不换技术栈、不改权限模型、不改表结构。
- 不删旧 release、不删备份、不清 `uploadPath`、不清教材目录。
- 凭据、Token、Cookie 不出现在报告、截图、命令回显里；密码只从 `contexts/secrets.local.md` 或 `AGENTS.md` 本机账号表读取，不回显。

### 0.3 推进节奏

先做 §2 的只读复核 → 输出「确认清单 + 测试计划」（哪些假设成立、哪些被兜底排除、准备写哪几个测试）→
等用户确认后再写测试。测试写完后跑一轮，输出报告。**不要一次铺开全仓。**

---

## 1. 当前事实基线（不得改写为模板）

- 正式版本 **1.30.94**，130 release `20261111_python_interactive_v1`；本机 Git HEAD 是 `9c9a97b`（1.30.93），
  **1.30.94 的 Python 交互改动仍在工作区未提交**。排查对象是**工作区当前代码**，不是 HEAD。
- 后端 `RuoYi-Vue/`：Java 8 / Spring Boot / RuoYi / MyBatis，`ruoyi-admin` 启动 + `ruoyi-business` 业务 + `ruoyi-quartz` 定时。
- 前端唯一 `RuoYi-Vue3/`：Vue3 / Vite / Element Plus。**不要碰旧 `ruoyi-ui`**。
- 本机库 `xueyeceping_server_20260729`；后端 8080、前端 80。130 是 Linux/systemd/Nginx 正式主站，123 是旧 Windows 栈，发布命令不混用。
- 三条**必须区分**的事实线：①答案/成绩 `biz_student_answer` ②课堂任务六态 `biz_student_task_state` ③终端在线 Presence（Redis TTL）。
  三者不是同一事实，不能互相冒充；Presence 不写签到。
- `biz_student_answer` 唯一键 `uk_student_lesson_question(student_id, lesson_id, question_id)`；
  `student_id` 必须是 `biz_student.student_id`（不是 `sys_user.user_id`）；`score IS NULL` = 待批，`0` = 已批。
- 学生身份有兜底：部分历史学生无 `sys_role` 学生角色，由 `studentSs.isStudent()` 含 `biz_student` 兜底。
- Judge0 默认参数（`B/judge/Judge0Properties.java:9-21`，均可被环境变量覆盖，**排查前必须核对本机与 130 实际值**）：
  `mode=disabled`、`poll-interval-ms=500`、`max-polls=24`、`read-timeout-ms=4000`、
  `student-submits-per-minute=10`、`class-concurrency=8`、`recovery-timeout-seconds=180`。

路径前缀：**B** = `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/`；
**M** = `RuoYi-Vue/ruoyi-business/src/main/resources/mapper/business/`；
**T** = `RuoYi-Vue/ruoyi-business/src/test/java/com/ruoyi/business/`；**F** = `RuoYi-Vue3/src/`。

---

## 2. 两个必须先知道的陷阱

### 2.1 `output/` 下有 4 份旧后端源码副本，会污染搜索与行号

```
output/deploy-20260920/backend-build/ruoyi-business/...
output/five-issues-20260919/backend-build/ruoyi-business/...
output/full-release-20260919/backend-build/ruoyi-business/...
output/login-perf-20261006/build/RuoYi-Vue/ruoyi-business/...
```

同名类、不同内容、不同行号。**所有 Grep/Glob/搜索必须排除 `output/`、`node_modules/`、`dist/`、`target/`**，
只认 `RuoYi-Vue/ruoyi-business/` 与 `RuoYi-Vue3/` 下的真实源码。引用行号前确认路径不含 `output/`。

### 2.2 Mockito 桩测试不能证明并发安全

`ruoyi-business/src/test/` 下**只有 `java/`，没有 `resources/`** —— 该模块没有自带测试配置、没有内存库、没有真实 MySQL 测试基类。
现有「并发」测试绝大多数是 Mockito 桩 Mapper，**桩掉的 `selectByIdForUpdate` 不会真的加行锁，桩掉的 `insertAnswerIfAbsent` 不会真的撞唯一键**。

- 全模块**真多线程** JUnit 只有 2 处：`T/service/GuideSheetAiContentServiceTest.java:165-177`（`CountDownLatch` + 4 线程池）、
  `T/service/LoginPreviewServiceTest.java:35,56,113`（2/8 线程 + latch 验缓存单飞）。
- `AGENTS.md` 记的「同一答案 12 并发重复提交仍单行」「批量采用并发跳过」是**接口/压测层的一次性验收事实，没有可回归的自动化测试**。
- 值得推广的手法：`T/controller/LessonAdvanceControllerTest.java:105` 通过 `configuration.getMappedStatement(...)`
  **断言 SQL 文本里真的含 `FOR UPDATE`** —— 这是本仓少见的「锁存在性」测试，成本低、能防回归。

**判定规则：凡是结论依赖「行锁生效」「唯一键冲突」「Redis 原子性」，桩测试一律不算证据，必须真实 MySQL / 真实 Redis / 真实 HTTP 并发。**

---

## 3. 已核对的并发机制现状（省去重复摸索，但仍需自行复核）

平台并发一致性靠三种机制支撑，**事务边界大量写在 Controller 层而非 Service 层**：

| 机制 | 已核对位置 |
| :--- | :--- |
| 答题行当前读 | `M/BizStudentAnswerMapper.xml:622,630` 的 `FOR UPDATE`；`B/mapper/BizStudentAnswerMapper.java:131-134` |
| 唯一键消化重放 | 同 XML `:65,116` `ON DUPLICATE KEY UPDATE`、`:92` `INSERT IGNORE`；`:75` 注明 score 必须放赋值组最后（MySQL 按顺序求值） |
| 指派行锁（推进/退回/重交互斥） | `selectCurrentAssignmentForUpdate`；`B/service/impl/LessonAutoAdvanceServiceImpl.java:504-538` 锁 + CAS 返回 1 才写 scope/历史 |
| 页面快照 + 乐观 CAS | `B/controller/TeacherGradingController.java:577-598` 三个比对方法；`PracticalArtifactService` `updateCurrentVersion` 带 `lockVersion` |
| 状态写入当前读确认 | `B/service/ClassroomTaskStateWriter.java:45-80`：upsert 后必须 `selectOneForUpdate` 复核（并发同态写入可能不改行） |

已核对的事务注解（**注意 Controller 层事务**）：

- `B/service/StudentAnswerSubmissionService.java:29-30` `persistAnswers` — `@Transactional(rollbackFor=Exception.class, READ_COMMITTED)`
- `B/controller/StudentHomeController.java:370-371` `checkin` — Controller 上 `@Transactional`
- `B/controller/StudentHomeController.java:577` `submitAnswers` — **无 `@Transactional`**（校验在事务外，落库委托 Service）
- `B/controller/TeacherGradingController.java:157-158` `batchAdoptPythonSystemScores`、`:355-356` `grade` — Controller 上 `@Transactional`，**整班一个大事务**
- `B/controller/ClassroomTaskStateController.java:96-97` `returnForResubmission` — `@Transactional`
- `B/service/PracticalArtifactService.java:144-145,164,304` — `@Transactional(rollbackFor=Exception.class, READ_COMMITTED)`
- `B/service/FlowchartService.java:206-207` `submit`、`B/service/TheoryRedoService.java:53-54` `reset` — `@Transactional`
- `B/service/ProgrammingSubmissionService.java:260` `submit` — **无 `@Transactional`**
- `B/service/ExemptionApplicationService.java:51,148,164` — **裸 `@Transactional`，无 `rollbackFor`**

---

## 4. 重点排查清单（11 条假设，按优先级）

> **这些是假设，不是已确认的 Bug。** 每条都必须先做「误报排除」：是否已有唯一键/行锁/条件更新/外层事务/前端闸门兜住？
> 是否 `docs/architecture/BUSINESS_BOUNDARIES.md` 明确设计如此？排除后再下结论。
> 每条给出：位置 → 假设 → 验证方法 → 判定标准（什么算成立、什么算被兜住）。

### H1【P0 候选】Redis 班级计数器「GET 判空后 DECR」非原子 + TTL 只在计数==1 时设置 → 计数器永久漂移

- **位置**：`B/service/ProgrammingSubmissionService.java`
  - 释放：`:662-667` `releaseReservation`、`:434-438` `releaseRunConcurrency`、`:413-418` `releaseInteractiveRun`
  - 预留：`:642-646` `reserveRateAndSlot`、`:422-432` `reserveRunConcurrency`、`:398-411` `reserveRateAndSlotForInteractive`
- **代码事实**：三处释放都是同一模式
  `if (values.get(classKey) != null) values.decrement(classKey);`
  三处预留都是 `INCR` 后**仅当返回值 == 1 才 `expire`**；超限回退也是裸 `decrement`。
- **假设**：`INCR` 对不存在的键会创建**无 TTL** 的键。若键在 `GET` 与 `DECR` 之间过期，`DECR` 会把键重建成 `-1` 且**无 TTL**；
  此后 `INCR` 返回 `0`（≠1），`expire` 永远不再设置 → ** immortal key**。计数向上漂移则整班永久「本班判题队列已满 / 同时运行人数较多」；
  向下漂移则限流静默失效，Judge0 被打满。
- **验证方法**：真实 Redis（本机），用**测试专用 key 前缀**或可注入的 key 构造器；
  ①多线程 N×（reserve→release）循环，断言结束后键不存在或值为 0；
  ②定向构造时序：`INCR`→`GET`→手动 `DEL`（模拟过期）→`DECR`，读回值与 `TTL`；
  ③读回 `TTL == -1` 即为 immortal 证据。
- **判定标准**：能拿到「结束后计数 ≠ 0」或「TTL = -1 且值为负」的实测输出 → 成立（P0/P1 按影响面定）。
  若证明 `expire` 总能在键存活期内设置且 `GET`/`DECR` 窗口不可能跨过期 → 写明排除理由。
- **注意**：`releaseSlot`（`:652-661`）有 `judge0:released:submission:<id>` 的 SETNX 幂等标记，**这条路径已有保护**；
  缺保护的是另外两条 run 释放路径。不要把三者混为一谈。

### H2【P0 候选】学生运行锁 TTL 短于最长执行时间，且释放无 owner 校验

- **位置**：`B/service/ProgrammingSubmissionService.java:401`（`judge0:interactive:<sid>` TTL **10 秒**）、
  `:425`（`judge0:run:student:<sid>` TTL **60 秒**）、`:495`（`executeWith` 轮询 `maxPolls × pollIntervalMs`）、
  `:414` 与 `:435`（无条件 `deleteObject` 学生键）、`:338-350`（`runPublicCases` 串行跑**全部**公开测试点）。
- **代码事实**：默认 `max-polls=24`、`poll-interval-ms=500` → 单个测试点最长约 **12 秒**（另加每次 HTTP `read-timeout-ms=4000`）。
  交互锁 TTL 10 秒 **< 12 秒**；`runPublicCases` 跑 N 个公开测试点，N≥5 即超 60 秒。
- **假设**：锁在运行结束前过期 → 同学生可发起第二次运行 → 第一次的 `finally` 删掉**第二次**的锁（无 owner/value 校验），
  且两次释放各 `DECR` 一次班级桶 → 与 H1 叠加放大漂移。
- **验证方法**：桩 `Judge0Client` 让 `poll` 慢返回（可控时长），真实 Redis；
  同学生并发 2 次运行，记录两次锁的获取/删除时刻与班级桶值序列；断言「第二次运行的锁不被第一次释放」「结束后班级桶归零」。
  同时核对本机与 130 的 `judge0.max-polls` / `poll-interval-ms` **实际值**（可能已被环境变量改小，那样窗口不成立）。
- **判定标准**：实测到锁提前过期 + 误删他人（自己第二次）锁 → 成立。若实际配置使最长执行 < TTL → 降级为「配置依赖型风险」，写明依赖哪个配置值。

### H3【P1 候选】`classSlotKey` / `classRunKey` 忽略传入的 `lessonId`、`questionId`

- **位置**：`B/service/ProgrammingSubmissionService.java:669`
  `classSlotKey(BizStudent s, Long lessonId, Long questionId)` 返回 `judge0:active:class:<deptId>:<entryYear>:<classCode>`；
  `:439` `classRunKey(BizStudent s)` 同结构。**两个参数在 key 里完全没用到。**
- **假设**：跨课、跨题共享同一班级桶 → 一节课的判题拥塞阻塞同班另一节课/另一题；
  「本班判题队列已满」的提示语义与实际粒度不符；签名接受却忽略参数，易被后续维护者误读。
- **验证方法**：读代码即可确认 key 组成；再用真实 Redis 观察同班不同 `lessonId/questionId` 的提交是否命中同一键。
- **判定标准**：**先判断是否设计取舍**——代码注释写的是「整班并发上限」，可能就是有意按班限流。
  若有意 → 归「设计风险 / 参数误导」，**不算 Bug**，但建议写明「参数未使用应删除或纳入 key」。不要直接报 P0。

### H4【P1 候选】`submit` 无事务，且分钟速率配额在失败时不回退

- **位置**：`B/service/ProgrammingSubmissionService.java:260`（无 `@Transactional`）、
  `:279-282`（`insertSubmission` → `setCacheObject(slotKey)` → `worker.judge` 三步非原子）、
  `:643`（`rateKey = judge0:rate:<sid>:<分钟桶>`，`INCR` + 70s TTL，超 `studentSubmitsPerMinute=10` 拒绝）、
  `:662-667`（`releaseReservation` 只删学生锁 + 班级计数，**不减 rateKey**）。
- **假设**：①学生因「本班队列已满」连续失败 10 次后，即使队列已空，也会被「提交过于频繁」挡住整整一分钟——
  **失败消耗了成功配额**；②进程在 `insert` 与 `judge` 之间崩溃时，提交行已落库但槽位键/队列状态不一致（需核对 `ProgrammingSubmissionRecoveryService` 是否兜住）。
- **验证方法**：真实 HTTP 并发（本机 8080）+ 真实 Redis + 真实 MySQL：
  ①同 `submissionKey` 并发 20 次 → 断言 `biz_programming_submission` **单行**、班级桶归零、记录 `rateKey` 实际计数；
  ②先制造队列满 → 连续失败 10 次 → 队列清空后立即重试 → 记录是否被速率误挡与剩余等待秒数。
- **判定标准**：拿到「失败后成功重试被速率挡住」的实测 → 成立。恢复服务若已兜住②，则②写明排除。

### H5【P1 可用性】教师预览运行：单机非公平 `Semaphore(8)` + 持锁轮询 20 秒 + 排队仅 3 秒

- **位置**：`B/service/PracticalPythonRunService.java:23`（`MAX_CONCURRENT_RUNS=8`）、`:24`（`SLOT_WAIT_SECONDS=3`）、
  `:28`（`new Semaphore(8)`，**非公平**）、`:39`（`tryAcquire(3s)`）、`:47`（持锁期间轮询上限 **20 秒**）、`:70`（`if (acquired) release()`）。
- **假设**：①最坏 8 槽 × 20 秒，3 秒排队的后续请求几乎全被「运行服务繁忙」拒绝——整班 45 人或多教师同时点「运行查看」时大面积失败；
  ②**无 per-teacher 配额**，一名教师可占满 8 槽；③该入口是**单机 JVM 信号量**，完全绕过 Redis 班级桶，
  教师运行 + 学生运行可叠加超过 Judge0 实际容量；④多实例部署时信号量不共享。
- **重要澄清**：`acquired` 标志与 `finally` 释放是**正确的**，`InterruptedException` 分支也重置了中断标志。
  **这不是许可泄漏**，是吞吐/公平性/容量口径问题。不要误报为泄漏。
- **验证方法**：桩 `Judge0Client` 慢返回，并发 12/24/45 个 `run`，统计成功/繁忙比例、P50/P95 等待时长；
  用反射读 `availablePermits()` 断言「`tryAcquire` 失败时不释放」「全部结束后许可数回到 8」。
- **判定标准**：给出并发数 vs 失败率曲线即成立（P1 可用性）。若 130 实际 `judge0.mode` 非 `http`，该入口直接抛「未启用」，需写明。

### H6【P1 候选，需先排除唯一键兜底】两跳「先查后锁」

- **位置**：`B/controller/ClassroomTaskStateController.java:125→128`、
  `B/service/PracticalArtifactService.java:208→212` 与 `:326→332`、`B/service/FlowchartService.java:320→321`。
- **代码事实**：先 `selectLatestByStudentLessonQuestion`（**非锁定读**）拿到 id，再 `selectByIdForUpdate(id)` 加锁。
- **假设**：两步之间若并发产生新答案行/新版本，锁保护的对象与判定对象不一致。
- **必须先排除**：`uk_student_lesson_question` 保证同一 `student+lesson+question` **只有一行**，
  所以「新答案行」在该唯一键下不可能出现；风险只剩「行内容已变」，而后续 `expectedScore/expectedAnswer/practicalVersionId` 快照比对**可能已兜住**。
- **验证方法**：真实 MySQL 两事务并发（A 持锁改分，B 走两跳），记录 B 读到的 id 与最终写入结果；
  核对快照比对是否拦下。同时确认 `selectLatestByStudentLessonQuestion` 的 `ORDER BY` 与唯一键是否真能保证单行（历史重复数据是否已治理）。
- **判定标准**：只有实测到「绕过快照比对写错行/写错版本」才成立；否则明确写「唯一键 + 快照比对已兜住，非 Bug」。

### H7【P2 候选，需先核对异常类型】免抽测 `@Transactional` 缺 `rollbackFor`

- **位置**：`B/service/ExemptionApplicationService.java:51`（`submit`）、`:148`、`:164`（审核）——均为裸 `@Transactional`；
  `:85` catch `DuplicateKeyException` 转业务提示；班级/课程快照批量插入在其后（`:95-124`）。
  唯一键见 `sql/teaching_exemption_v1.sql:15,53,76,98`。
- **假设**：默认只回滚 `RuntimeException`/`Error`；若快照插入链路抛受检异常，会留下「申请已插入、快照半提交」。
- **必须先排除**：核对该方法及调用链**是否真的可能抛受检异常**。若全为 `RuntimeException` → 实际无差异，
  归「与项目其余 `@Transactional(rollbackFor=Exception.class)` 写法不一致」的健壮性问题，**不要报 P0**。
- **验证方法**：读调用链 + 真实 MySQL 并发同键申请（同 `dept+teacher+学年+学期+年级`），断言申请单行、快照行数与申请匹配、无孤儿快照。

### H8【P2 候选】`replaceLessonTools` 先删后插，无行锁/版本校验

- **位置**：`B/service/impl/StudentToolServiceImpl.java:163`（`@Transactional`）、`:164`（方法）、`:178`（`deleteLessonToolsByLessonId`）→ 循环 `insertLessonTool`。
- **假设**：并发替换互相覆盖（后提交者赢，但中间态可能丢工具）；读端可能读到空列表。
- **验证方法**：真实 MySQL 两事务并发替换同一 `lessonId` 的不同工具集，断言最终集合是**某一个完整集合**而非并集/空集/半集；
  并发读端记录是否读到 0 行。
- **判定标准**：出现半集/空集且对外可见 → 成立。若隔离级别 + 事务边界使读端永远看不到中间态 → 写明排除。

### H9【P2】签到 `INSERT IGNORE` 静默，无冲突感知

- **位置**：`B/controller/StudentHomeController.java:370-427`（先 `selectByLessonAndStudent` 命中返回「已签到」，
  未命中 `insertIgnore`，再查回显时间）；`M/BizLessonCheckinMapper.xml:32`（`INSERT IGNORE INTO biz_lesson_checkin`）；
  0 题考勤课「不能交卷」在 `submitAnswers:607-612`，**签到侧无题数要求**；
  拓展课走 `B/service/ExtensionClassService.java:210-221`（班级锁 + 成员锁 + 当前课 `FOR UPDATE` + 建课名单快照）。
- **已知事实**：`AGENTS.md` 记录学生 `2020710101` 已完成 0 题考勤课签到与重复签到验证，签到后未生成作业答案、当前课未被推进。
  **这是已验证行为，不要重复报为 Bug。**
- **本轮只查**：①并发下回显的签到时间是否可能来自另一请求；②考勤课签到是否可能误触发推进；
  ③拓展班「建课快照」与「后加入成员补投题单」两套名单在并发加入/移出时是否一致。
- **验证方法**：真实 HTTP 同学生并发 12 次签到 → 断言 `biz_lesson_checkin` 单行、时间稳定、当前课未推进、无作业答案生成。

### H10【P0 候选 · 最新未验面】1.30.94 三个新接口的权限与「不写成绩」承诺

`PROJECT_CORE.md` 明确记载：1.30.94 **未在 130 整班 45 人并发验证**，作品 `.py` 预览运行与教师建题向导只做了代码/接口核对。

- **位置**：
  - `POST /business/python-practice/student/run`（刷题调试运行）→ `B/controller/PythonPracticeStudentController.java`、`B/service/PythonPracticeService.java`
  - `POST /business/teacher/grading/python/{submissionId}/run` → `B/controller/TeacherGradingController.java:282` `runPythonSubmission`；
    `POST /business/teacher/grading/python/run` → 同文件 `:307` `runPythonSource`（两者**无事务无锁，按设计只运行不写判题记录和成绩**，权限按课程归属校验）
  - `POST /business/programming/question/{questionId}/generate-outputs`（生成期望输出，**只算不落库**）
  - 判题内核：`B/judge/JudgeSourceWrapper.java`、`B/judge/JudgeOutput.java`、`B/judge/JudgeOutcome.java`
- **必须逐条查**：
  1. **越权**：教师运行是否真按课程归属校验？构造跨校、跨课、他班学生、已归档课程、非任课教师；学生/匿名调教师运行接口的业务码（ documented 为 401）。
  2. **「不写成绩」是否真不写**：三个接口跑完后逐表比对 `biz_student_answer`、`biz_programming_submission`、`biz_student_task_state`、题目期望输出列 —— **必须给出跑前跑后的逐行/哈希比对**，不能只看代码。
  3. **套壳可绕过/标记伪造**：学生代码里自己打印 `JudgeSourceWrapper` 的不可见标记，能否污染 `plain()`/`visible()`/`terminalText()` 的判题与回放？构造含标记字符、含相同提示文字、含 `\r\n`/Unicode 的输出。
  4. **加权分一致性**：`JudgeOutcome.weightedScore` 在**课程链路**与**刷题链路**是否真的一致（1.30.94 声称修了刷题忽略权重）；测试点权重为 0/NULL/负数、总权重为 0 时的分母。
  5. **等待输入判态**：程序停在 `input()` 判 `RUNTIME_ERROR` 的规则，是否会把「正常需要多轮输入」误判为失败；`needInput` 与 `success` 的组合是否自相矛盾。
  6. **并发**：同学生并发点运行、同教师并发运行不同学生、整班 45 人同时降级到服务器运行 —— 与 H1/H2/H5 联合。
- **验证方法**：真实 HTTP + 真实 Python/Judge0（本机 `judge0.mode` 需为 `http`，否则写明无法验证）；
  权限负例用真实角色账号；判题正确性用可控源码 + 可控期望输出。

### H11【本轮最该补的产出】把一次性验收变成可回归测试

- **缺口**：见 §2.2。「12 并发重复提交单行」「批量采用并发跳过」「两名教师并发改分」「学生并发重交」
  在 `PROJECT_CORE.md` 里反复被列为**未验边界**（1.30.88/89/90/92/94 各自都写了「未做生产并发实测」）。
- **要求**：本轮至少把下列 4 条变成**可重复运行**的测试，并给出运行命令与结果：
  1. 同学生同答案 12 并发提交 → `biz_student_answer` 单行、分数不丢、任务状态一致。
  2. 两名教师并发对同一 `answerId` 打分 → 只有一人成功或后者被快照比对拦下，**不出现后写覆盖先写且无提示**。
  3. 学生并发重交（文件作品 / 流程图各一）→ 旧分归档与新版本待批原子，`practicalVersionId` 不指向不存在版本，不产生两条正式记录。
  4. 批量采用系统分期间学生重交 → 该生进 `skippedCount`，其余学生正常，**不整班回滚**。
- **参考桩**：`T/controller/TeacherGradingControllerPythonAdoptTest.java:122` 已有「并发快照跳过」桩测试，
  可作为断言口径参考，但**必须升级为真实 MySQL 并发**才算证据。

---

## 5. 并发测试怎么写（硬要求）

### 5.1 落点

| 类型 | 位置 |
| :--- | :--- |
| JUnit（真多线程 / 真实 MySQL） | `T/` 下新增，包名与被测类对应，类名以 `ConcurrencyTest` 结尾 |
| SQL 文本锁存在性断言 | 同上，参考 `T/controller/LessonAdvanceControllerTest.java:105` |
| HTTP 并发压测脚本 | `output/stress/concurrency-audit-20261111/`（新建） |
| 夹具 / 清理 / 后检脚本 | 同上目录 |
| 报告与证据 | `output/stress/concurrency-audit-20261111/RESULT.md` + `contexts/bug-audit-20261111/report.md` |

**不得**把测试脚本塞进 `scripts/deploy/`、`sql/` 或业务源码目录。

### 5.2 优先复用已有压测框架，不要从零造

`output/stress/multi-role-concurrency/AI_FIX_20260731_001/` 已有一套可用框架：

- `testlib.py` — `API = http://127.0.0.1:8080`、`login(username, preferred_dept)`、`local_password(account)`（从 `AGENTS.md` 本机账号表读取，不回显）、`manifest()`
- `closed_vu_load.py` — `aiohttp` + `pymysql` 闭环 VU 压测，`argparse` 参数化，输出 `system-metrics.csv`
- `prepare_test_data.py` — `load_database_config()`
- `create_business_fixtures.py` / `final_cleanup.py` / `post_cleanup_verify.py` — 造夹具、精确清理、清理后残余核验

另有 `output/stress/local_submit_stress.py`、`judge0_400_acceptance.py`、`judge0_python_stress.py`、`setup_test_class.py` 可参考。
**复用其登录/夹具/清理骨架，新增本轮场景**；不要重写一套。

### 5.3 每个并发场景必须写清 5 件事

1. **并发数与同步点**：用 `CountDownLatch` / `asyncio.gather` 让 N 个请求**同时**出发，不是串行循环。
2. **断言的不变量**（不是「没报错」）：例如「答案单行」「分数不丢」「班级桶归零且 TTL ≠ -1」「旧分已归档」「skippedCount 精确等于漂移人数」。
3. **跑前跑后数据比对**：涉及的表逐行/计数/哈希比对，Redis 键值与 TTL 快照。
4. **失败证据**：失败时保留请求响应体、SQL 结果、Redis 快照、后端日志片段到证据目录。
5. **精确清理 + 后检残余 0**：只删自己造的夹具，按清单逐条删；清理后跑后检脚本证明残余 0、基线数据未变。

### 5.4 本机执行命令（已核对的环境坑，照抄）

```bash
# 后端测试：必须带这两个参数，否则整轮 build 失败
#   -DforkCount=0                          本机 surefire fork JVM 会启动失败（环境问题，不是用例失败）
#   -Dsurefire.failIfNoSpecifiedTests=false 其他模块「无匹配用例」会让整轮失败
cd RuoYi-Vue
mvn -pl ruoyi-business -am test "-Dtest=XxxConcurrencyTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-DforkCount=0"

# 全量业务回归（当前基线：1017 项通过 / 0 失败 / 3 跳过）
mvn -pl ruoyi-business -am test "-Dsurefire.failIfNoSpecifiedTests=false" "-DforkCount=0"

# 打包（改配置/资源后必须 clean）
mvn -pl ruoyi-admin -am clean package

# 启动：必须显式指定端口，本机环境有 SERVER__PORT=50485 会劫持 server.port
#       教材接口需要 TEXTBOOK_ROOT，否则整册 500
java -Xms512m -Xmx2048m -Dserver.port=8080 -jar ruoyi-admin/target/ruoyi-admin.jar
# TEXTBOOK_ROOT=D:\dmwprogram\newdazipingtai\newdazi\output\textbook-pack-20261029\pages

# 前端
cd RuoYi-Vue3 && npm run dev          # http://localhost
npm run test:python-runner            # 当前基线 20 项通过
```

- 复杂多行 Python/SQL/JSON **先写文件再调用**，不要塞进 `python -c` / `node -e` 多层引号。
- Windows Python 用 `python -X utf8 脚本路径`；文件显式 UTF-8 读写。
- 本机可用账号见 `AGENTS.md` §4.1（教师 `19157727791`、教研员 `laoda`、学生 `st99_363` / `2020710101`）。
  **这些只用于本机**，不可用于正式环境登录验证。管理员账号未写死时向用户索取，不要猜。
- 启动后必须**探活确认就绪**（8080 监听 + `/captchaImage` 200），不能只凭 PID 报成功。

### 5.5 什么算证据、什么不算

| 算 | 不算 |
| :--- | :--- |
| 真实 MySQL 两事务/多线程并发 + 跑前跑后逐行比对 | Mockito 桩掉 Mapper 后「断言抛了某异常」 |
| 真实 Redis 键值 + TTL 快照 | 假设 Redis 命令原子 |
| 真实 HTTP N 并发 + 响应体 + 数据库落地结果 | 串行循环 N 次 |
| 断言 SQL 文本含 `FOR UPDATE`（防回归） | 只断言「调用了 `selectByIdForUpdate`」 |
| 真实 Python/Judge0 执行结果 | 只看判题代码推演 |

---

## 6. 输出要求

### 6.1 证据分级（每条必须标注其一）

- **`已复现`**：用真实并发测试/只读查询/实际执行证明，附命令与输出。
- **`代码证实`**：调用链完整、条件明确，能指出确切代码路径与触发输入，但未实际跑并发。
- **`待验证`**：依赖运行时数据、并发时序、外部系统或未读到的调用方；**必须写「需要什么证据才能确认」**。

禁止把 `待验证` 写成确定结论；禁止凭「看起来很危险」下结论；禁止因为文档写了「通过」就相信全部正确。

### 6.2 等级定义

- **P0**：正式成绩错误/丢失、越权读写他人成绩或作品、旧课写入生效、数据不可恢复、整班永久卡死、把错误状态当成功展示。
- **P1**：高频操作产生错误统计/排名/反馈、核心流程卡死且无替代路径、异步任务静默丢失、限流大面积误拒。
- **P2**：条件性错误（需特定顺序/数据）、短时不一致可自愈、导出与页面口径差异、写法不一致的健壮性问题。
- **P3**：体验缺陷、可自愈竞态窗口、难以触发的理论风险（需注明为何难触发）。

### 6.3 每条 Bug 的结构

```
### [P?] 一句话标题
- 等级 / 证据级别
- 位置：文件:行号（2~4 个关键位置，路径不得含 output/）
- 涉及数据：表/字段/Redis key/文件目录
- 触发条件：角色、前置状态、具体操作顺序
- 实际结果（复现到的或推演的）
- 预期结果（引用 BUSINESS_BOUNDARIES.md 或用户可接受行为）
- 根因：哪一行代码允许了错误发生（不要只写「缺少校验」）
- 误报排除：检查过哪些兜底（唯一键/行锁/CAS/外层事务/前端闸门/设计取舍），为什么没兜住
- 影响面：谁受害、数据可否恢复、是否影响正式成绩
- 最小修复建议：只描述方向与落点，不改代码
- 回归测试：新增的测试类/脚本路径 + 运行命令 + 本轮结果
- 同类位置：顺带发现的相同模式
```

### 6.4 报告必须单列的三节

1. **已确认问题**（P0→P3，去重合并同一根因）
2. **未测风险**（不与已确认混在一起；写明需要什么证据）
3. **已知设计取舍、不按 Bug 处理**（例如 H3 若属有意按班限流）

另附：**本轮新增测试清单**（路径 + 命令 + 通过/失败 + 覆盖了哪条假设）、**清理后检结果**（残余 0、基线未变）、
**与文档不符之处**（若发现 `PROJECT_CORE.md` 的数字/结论与现场不符，直接指出并给现场实测值）。

---

## 7. 自主排查要求（防止清单化）

1. 本清单是**起点不是终点**。完成 H1～H11 后，主动去找**至少 3 个清单外**的并发写入口并追踪到底。
2. 优先怀疑这些模式，并在项目里找实例：
   - 同一业务事实有多个写入口，其中一个少了校验/锁；
   - 「先查后写」无锁，或锁粒度与判定数据不一致；
   - 条件更新没检查影响行数，失败被当成功；
   - Redis 计数器/锁的 TTL 与业务最长耗时不匹配，释放无 owner 校验；
   - 事务注解写在 Controller 上，内部自调用导致 `@Transactional` 失效（参考 `LessonAutoAdvanceServiceImpl.java:60` 的自注入代理注释）；
   - 前端用本地状态推断成功，后端实际失败或部分失败；
   - 统计口径在页面、导出、接口、AI 四处各写一份。
3. 无法确认的细节（表列是否存在、数据量、配置实际值、130 与本机差异）写「需要查证：具体查什么」，**不得编造**。
4. 每阶段结尾给出：**已证实最高优先级 Top 3**、**最需要补的证据**、**建议最先人工验证的一条操作**（5 分钟内能做完）。

---

## 8. 收尾要求

- 本轮属排查 + 新增测试，**不改业务代码**，因此不升级产品版本、不生成 `biz_platform_update` 记录、不碰 `RELEASE_LOG.md`。
- 若确认了 P0/P1 缺陷：把**已验证事实**写入 `contexts/bug-audit-20261111/report.md`，
  并在 `contexts/PROJECT_CORE.md` 的「下一步与未闭环风险」追加一条（只写已验证的，未验的标待验）。
- 新增测试若纳入回归，写明运行命令与当前基线（后端业务全量 1017 项 / 0 失败 / 3 跳过；前端 `test:python-runner` 20 项）。
- 不 `git commit`／不 `push`，除非用户明确要求。
- 向用户汇报：查了什么、确认了哪几条、写了哪些测试、怎么跑、剩余风险、下一步建议。

---

**开始执行前先做一件事**：只输出 §4 的**只读复核结论 + 测试计划**
（每条假设：成立 / 被兜底排除 / 需实测，以及准备新增哪几个测试文件、并发数、断言的不变量），
等用户确认后再动手写测试。