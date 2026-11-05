# 《信息科技学业测评平台》专属深度 Bug 审查提示词

> 生成日期 2026-11-05，基于本机工作区源码（含未发布改动）只读探查。生成过程未修改代码。
> 用法：整份复制到全新 AI 会话，无需本会话背景即可开始。审查者仍须遵守 `AGENTS.md` 的只读与文档规则。

---

## 0. 审查角色与目标

你是本项目（中小学信息科技学业测评平台）的**业务逻辑审计专家 + 测试设计专家**，只做**只读**审查。目标不是代码风格或重构，而是找出会让真实教师、学生拿到**错误成绩、错误状态、错误数据或越权数据**的缺陷，并给出可复核的证据。

本次审查按阶段推进，每阶段结束输出一份报告再进入下一阶段，不要一次铺开全仓。

| 阶段 | 范围 | 产出 |
| :--- | :--- | :--- |
| A | 学生作答写入与交卷、当前课边界 | 报告 A |
| B | 教师批改、成绩口径与导出、重做/退回/清分 | 报告 B |
| C | 课程/班级/权限/学校数据边界、题库归属 | 报告 C |
| D | 异步任务（Python 判题、AI 批改、学情、文件转换）、外部集成、定时任务 | 报告 D |
| E | 前端关键页面的状态机与反馈真实性（与学生/教师端后端结论交叉） | 报告 E |
| F | 你自行发现的跨阶段关联问题与未列出的风险 | 报告 F |

**先读**：`AGENTS.md` → `contexts/PROJECT_CORE.md` → `docs/architecture/INDEX.md`，再按阶段读 `docs/architecture/BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md`、`INTEGRATIONS.md`、`SYSTEM_ARCHITECTURE.md`。文档只作边界线索，**结论一律以源码为准**；文档与代码冲突时按代码写“文档与代码不一致”。

---

## 1. 项目背景与架构（真实事实，不得改写为模板）

- **用途**：初中/小学信息科技课程的教学与多维度测评。教师建课→指派班级→学生作答（选择/判断/打字/Python/文件作品/画程流程图/数据分析实验）→教师批改→成绩汇总与导出→学情诊断→区域抽测→导学单→教研活动→电子教材与优秀课例→课堂大屏监控→物联网实验→在线协作。
- **角色**：管理员（admin）、教师（teacher）、学生（student）、教研员（researcher）。学生身份有权重：部分历史导入学生**没有 `sys_role` 学生角色**，由 `biz_student` 档案兜底（`security/StudentSecurityService.java`，Bean 名 `studentSs`）。
- **技术栈**：后端 `RuoYi-Vue/`（Java 8 / Spring Boot / RuoYi / MyBatis，`ruoyi-admin` 启动 + `ruoyi-business` 业务 + `ruoyi-quartz` 定时任务）；前端唯一 `RuoYi-Vue3/`（Vue3 + Vite + Element Plus）；数据库 MySQL，缓存 Redis，文件在 `uploadPath`/`/profile/`，教材页图在 `TEXTBOOK_ROOT`（release 之外，经鉴权接口回图）。
- **外部依赖**：Judge0（Python 判题，仅后端调用）、LibreOffice 转换池（页图/PDF）、CryptPad（在线协作，经 Integration API，密钥由平台后端签发）、EMQX MQTT（物联网实验）、Windows 机位助手客户端（可选，回环 127.0.0.1:28123）、阿里云百炼等 AI 厂商（AI 批改/学情，教师个人 Key 按厂商 AES-GCM 加密）、WPS 回调（已退役，默认开关关闭）。
- **两条必须区分的事实线**：①答案/成绩（`biz_student_answer`，最终业务事实）②课堂任务六态（`biz_student_task_state`）③终端在线 Presence（Redis TTL）。三者**不是**同一事实，不能互相冒充。
- **环境**：本机开发库 `xueyeceping_server_20260729`；130 为正式主站、123 为旧 Windows 栈。`sql/` 有 270+ 个增量脚本，**本机与 130 的表列/数据不必然一致**。

### 关键数据模型（必读 `docs/architecture/DATA_MODEL.md`）
- `biz_student_answer`：唯一键 `uk_student_lesson_question(student_id, lesson_id, question_id)`；`student_id` **必须**是 `biz_student.student_id`（不是 `sys_user.user_id`）；`score IS NULL` = 待批，`0` = 已批。
- `biz_lesson_assignment`：班级×课程指派，含 `theory_open/practical_open`；按 `assign_time DESC` 的第一行是“当前课”；`lesson_scope='regular'`，`status='0'`。
- `biz_lesson_question`：课程题目关系；**答案进入任何在线统计的前提**是课程仍引用该题；移除题目必须先归档到 `biz_student_answer_orphan_archive` 并核对数量。
- 作品版本：`PracticalArtifact` / `PracticalSubmissionVersion` / `PracticalAttachment`，答案是 `score`/`practicalVersionId`/版本指针的聚合。
- 题单/Python：`biz_python_practice_*` 独立域；课程 Python 复用 Judge0 但用 `biz_programming_submission`。
- AI：`biz_practical_ai_job` / `_result` 冻结厂商、模型、价格、版本锚点；教师确认才写正式成绩。
- 学校/班级：`sys_dept`、`biz_student.entry_year`（入学年份，“级”不是“届”，学年切换点 7 月 20 日）、`biz_teacher_class`（任课关系）。

---

## 2. 重点审查模块（含风险原因与起始位置）

路径前缀：**B** = `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/`；**M** = `RuoYi-Vue/ruoyi-business/src/main/resources/mapper/business/`；**F** = `RuoYi-Vue3/src/`。

| 模块 | 起始位置 | 为什么高风险 |
| :--- | :--- | :--- |
| 学生作答与交卷 | `B/controller/StudentHomeController.java`（`/business/student-home/*`，`submit-answers` 约 569-779、`current-lesson` 176-357、`practical-upload`/`practical-artifact/*` 449-551）、`B/service/StudentAnswerSubmissionService.java`、`M/BizStudentAnswerMapper.xml`（`upsertAnswer` 54-87、`insertAnswerIfAbsent` 90-101、`selectCurrentLessonByClass` 130-139、`selectCurrentAssignmentForUpdate` 147-164） | 成绩真源；同一行被多条路径写；当前课指针与旧课拒写 |
| 文件作品版本链 | `B/service/PracticalArtifactService.java`（staging 100-142、submit 156-302、delete 304-347、afterCommit 684-703）、`B/controller/PracticalAttachmentController.java`、`B/service/ResourceAccessService.java` | 版本/分数/文件三者一致性，文件鉴权 |
| 教师批改 | `B/controller/TeacherGradingController.java`（`/grade` 177-263、`/practical-submissions` 124、`validateGradeScope` 332）、`B/controller/ClassroomTaskStateController.java`（`/return` 95）、`B/service/PracticalScoringPolicyService.java`、`B/service/PracticalRubricSnapshotService.java`、`B/service/PracticalGradingDeadlineService.java` | 正式成绩唯一写入点；版本/期限/退回状态 |
| 成绩查询与导出 | `B/controller/ScoreQueryController.java`（summary 416、export 1023、analysis 1321、matrix 1411、`parseExportColumns` 880、导出填充 1246-1304）、`M/BizStudentAnswerMapper.xml`（`selectScoreSummaryByStudents` 455） | 页面/导出/AI 三处口径必须同源 |
| 重做与清分 | `B/service/TheoryRedoService.java`、`B/controller/TheoryRedoController.java`、`M/TheoryRedoMapper` | 删旧答案、轮次乐观锁、教师清分 |
| 课程设计器 | `B/service/impl/BizLessonServiceImpl.java`（`saveLessonDetails` 561-804、`validateLessonContent` 855-947、`assertCanManageLesson` 1136-1157、`purgeBizLessonByLessonIds` 221-336） | 总分 100、指派开放状态、教材绑定、共享课程能力 |
| 课堂推进 | `B/service/impl/LessonAutoAdvanceServiceImpl.java`（`manualAdvanceClasses` 124-226、`advanceOneClass` 504-538、`findNextLesson` 558-603、`previewAdvanceClasses` 368-445） | 逐班事务、锁、快照比对 |
| 题库 | `B/service/impl/BizQuestionServiceImpl.java`（详情 70-94、编辑 127-159、批量删 175-214、单删 216-230、导入 247-390）、`F/views/business/question/index.vue`、`F/utils/questionWorkbench.js` | 私有题归属、批量动作、权限渲染 |
| 学生导入 | `B/service/impl/BizStudentServiceImpl.java`（import 486-583、锁 1024-1058、写库 840-920、纠错 398-452） | 事务原子性、Redis 锁 |
| Python 判题 | `B/service/ProgrammingSubmissionService.java`、`ProgrammingSubmissionWorker.java`、`ProgrammingSubmissionRecoveryService.java`、`B/service/PracticalPythonRunService.java`、`M/ProgrammingJudgeMapper.xml` | 状态机、幂等键、迟到结果 |
| AI 批改 | `B/service/PracticalAiJobService.java`、`PracticalAiJobWorker.java`、`PracticalAiSuggestionApplyService.java`、`PracticalAiJobRecoveryService.java`、`M/PracticalAiGradingMapper.xml` | 版本锚点、价格冻结、批量采用 |
| 学情诊断 | `B/service/PracticalInsightWorker.java`、`PracticalInsightEngine.java`、`PracticalInsightReportService.java`、`PracticalInsightMapper` | runToken、门槛聚合、旧 worker |
| 文件转换 | `B/service/AsyncConversionService.java`、`AsyncConversionWorkerService.java`、`PracticalAttachmentConversionService.java`、`B/utils/FileConversionUtils.java`、`B/config/AsyncConfig.java` | 领取/重试/毒文件/池拒绝 |
| 学生反馈快照 | `B/service/StudentFeedbackService.java` | 不可变快照、版本提示 |
| 在线协作/物联网 | `B/controller/CollaborationController.java`、`CryptPadCollaborationController.java`、`IotExperimentController.java`、`B/service/IotMqttReceiver.java`、`IotCredentialExpiryJob.java` | 会话签发、Topic 隔离、凭据撤销 |
| 机位助手 | `B/controller/SeatHelperController.java`、`B/service/SeatHelperService.java`、`SeatHelperPairService.java` | 设备身份、换账号、撤销 |
| 前端学生页 | `F/views/student/index.vue`、`F/views/student/` 各组件、`F/api/business/studentHome.js` | 迟到响应、切课、自动交卷 |
| 前端教师页 | `F/views/business/teacher/index.vue`、`grading.vue`、`performance.vue`、`F/views/business/score/index.vue` | 快照锁、切人、保存反馈 |
| 前端请求层 | `F/utils/request.js`、`F/utils/session.js`、`F/permission.js`、`F/directive/permission/hasPermi.js` | 401/403/500、重复提交、权限隐藏 |

---

## 3. 专属 Bug 检查场景（按阶段，必须逐条执行并给证据）

> 每条都要求你**追踪完整调用链**：前端 API → Controller → Service → Mapper XML/SQL → 权限注解 → 相关表约束/唯一键。只读一个文件下的结论一律标注“未追踪”。

### 阶段 A：学生作答与交卷

- **A1 理论题重复提交与“已提交”误判**。追 `StudentAnswerSubmissionService.java:30-75` 与 `M/BizStudentAnswerMapper.xml:90-101`：`INSERT IGNORE` 影响行数为 0 时一律抛“理论题已提交”。验证：若写入失败原因是**非唯一键冲突**（字段超长、约束、字符集截断），学生看到的是“已提交”而非失败，成绩是否永久缺失？给出能把该分支误判的具体输入。
- **A2 打字最高分与迟到响应**。追 `keepBestScore` 分支（`M/BizStudentAnswerMapper.xml:54-87`）：确认“取最高分”是 SQL 整体择优还是前端比较；构造“学生先交高分、切题后又交低分”“计时到点自动提交与手动提交同时触发（`F/views/student/index.vue` 的 `autoSubmitTyping`/`submitTyping`）”两种顺序，判断最终分数与文本是否可能错配（分数来自 A、文本来自 B）。
- **A3 打字提交不要求 revision**。`B/service/TheoryRedoService.java:147-156`：教师重做打字后，旧页面提交是否会用无 revision 的请求把新轮成绩写回旧轮语义。要求给出前端是否总会带 `typingRevision` 的证据（`F/views/student/index.vue` 打字提交处）。
- **A4 旧课拒写的两层闸门不一致**。控制器 `validateSubmissionAccess`（非锁）与事务内 `selectCurrentAssignmentForUpdate`（锁）同时存在。构造：A 学生打开旧课页面 → 教师推进 → A 提交。验证是否只在其中一条路径被拦住；并检查**所有**写 `biz_student_answer` 的入口（含 Python 判题回写 `ProgrammingSubmissionService.java:351-382`、画程 `FlowchartService.submit`、文件提交）是否都有事务内当前课复核。重点：Python 判题在教师推进后才回写，`writeExistingAnswer` 是否复核当前课。
- **A5 dept 来源混用**。`TheoryRedoService` 用 `student.getDeptId()`，`StudentHomeController` 用 `loginUser.getDeptId()`，`PracticalArtifactService.java:157` 用学生档案 dept，控制器 `assertPracticalOpen` 用登录 dept。在“学生档案 dept ≠ 登录部门”（跨校转学、数据错配）时，找出两处判定相反的具体分支，并判断是否可能让学生写入非本校课程或教师看不到已交作业。
- **A6 `getCurrentLesson` 不校验 lesson.deptId**（`StudentHomeController.java` 约 235，对照 checkin 约 393 有校验）。判断跨校数据错配时学生首页是否可能拿到他校课程与题目。
- **A7 文件作品提交的版本与分数一致性**。`PracticalArtifactService.submitArtifact`（173-302）：`upsertAnswer(LAST_INSERT_ID)` 回填 answerId、`updateCurrentVersion` 乐观锁、`expectedVersionId` 比对、`existingAnswer.getScore()` 与 `currentVersionId` 组合分支（约 214-219）。构造：①已有分+已有当前版本重交 ②已有分但 currentVersionId 为 null ③无答案但有附件 ④同学生双击提交 ⑤两个浏览器同时提交。判断是否可能出现“旧分未归档但新版本已成当前”或“答案指向不存在的版本”。
- **A8 上传暂存与 ticket**。`stageStudentFile`/`resolveTickets`（`PracticalArtifactService.java` 100-142、524-557）：ticket 是否绑定 student/lesson/question 三元组；过期、跨学生重放、提交失败后 ticket 清理（`registerAfterCommit` 684-703 在回滚时是否残留）。
- **A9 附件读取越权**。`PracticalAttachmentController.authorized` → `ResourceAccessService.assertCanRead`。逐条核对：学生读他班同学附件、教师读他校学生附件、匿名请求、`/profile/` 路径穿越、兜底放行分支（`ResourceAccessService.java:130-135` 对 teacher/researcher 未命中业务表的路径放行）的具体可达性。
- **A10 操作题删除不删盘**：`deleteCurrentLessonArtifact`（304-347）确认删除后旧附件文件是否仍在，是否可被再次读取（通过旧版本 id 直取附件接口）。

### 阶段 B：教师批改与成绩口径

- **B1 页面/导出/分析三处口径一致**。对比 `ScoreQueryController` 的 summary、export（1023-1304）、analysis（1321）、matrix（1411）与前端 `F/views/business/score/index.vue`：
  - 等级比例：前端可改 `gradeRatios`，导出固定 25/40/30（导出约 984-986）——是否会出现“页面 A 等级、导出 B 等级”。
  - 排名口径：前端按 `finalTotal/课程总分` 排名，导出按 `finalScore` 均值排名（约 967-971）；多课+请假/缺考时是否排名相反。
  - 0 分：前端 `getRankScore>0` 才入排名，导出同判但无“包含 0 分”开关。
  - 打字速度：页面用 `avgTypingSpeed` 真值累加，导出用 `typingSpeed>0`（约 1229）——构造 avgTypingSpeed 为 0 或 null 的课次，给出计数差。
- **B2 `totalScore = SUM(IFNULL(score,0))` 与待批区分**（`M/BizStudentAnswerMapper.xml:473`）。验证：全部待批的作业是否在某个页面上显示为 0 分而非“待批”；导出单元格在 `hasPracticalPending` 的分支（导出 1279-1284）覆盖了哪些列。
- **B3 分页导致缺考标记漂移**。`buildScoreSummaryRows`（`ScoreQueryController.java` 约 531-546）用本页 studentIds 查课程记录判断缺考。构造学生 300 人分 2 页：同一学生在第 1 页标“缺考”、在第 2 页是否标“未提交”？导出是否与当前页一致？
- **B4 人数分母不同源**。`selectScoreStudents`（`M/BizStudentMapper.xml:159`）不过滤 `u.status/del_flag`，而期限分母 `selectClassMetrics`（`M/PracticalGradingDeadlineMapper.xml:25-33`）过滤有效学生。构造：停用账号或软删账号。成绩页人数、课堂表现人数、期限起算人数三者是否不同，谁是对的？
- **B5 课堂表现计入分母**。`applyPerformanceToScore`（约 698-734）在 `unsubmitted` 情形把 `unsubmitted` 置 false 并参与表现分母。构造：某生请假/未提交但有课堂表现分，验证平均分分母是否被改变、是否与前端 `isExcludedFromAverage` 一致。
- **B6 批改期间的数据竞态**。`TeacherGradingController./grade`（177-263）的陈旧校验顺序：有 `practicalVersionId` 比版本；否则 FLOWCHART 比 `expectedAnswer`；否则比 `submitTime`；再比 `expectedScore`。构造：教师在批改页停留 10 分钟，期间学生重交（当前课允许）→ 教师保存。判断是否可能把分数写到新版作品上或以旧作品名义保存。
- **B7 批改期限竞态**。`PracticalGradingDeadlineService.assertCanGrade` 与写分同事务；`COMPLETED` 且逾期时 `canGrade=false`（约 335）。构造：第 N 份恰好跨过期限瞬间保存；全班已批完但已过期限后教师想修正错分——是否存在无法修正又无替代入口的死角。
- **B8 退回与重交**。`ClassroomTaskStateController./return`（95）：仅当前课、仅文件/画程、未批不可退、带 `expectedScore/expectedAnswer`。构造：教师在批改页点退回 → 学生重交 → 教师再次批改。核对退回是否清任务状态、`score` 是否保留、旧版本是否归档；对照 `BUSINESS_BOUNDARIES.md` 2026-10-02 段落的规则。
- **B9 AI 建议批量采用**。`PracticalAiSuggestionApplyService`（50-162）：`FILL_UNGRADED/OVERWRITE_ALL` 模式，逐份版本校验、`assertCanGrade`、已采用过的结果是否会被二次采用（`apply_status` 过滤缺失，约 65-67）。构造：①同一结果点两次“采用” ②中途学生重交 ③批改期限在循环中途到期 ④写分失败是否整体回滚。要求给出审计表里实际会多出/少掉哪些行。
- **B10 重做与清分**。`TheoryRedoService.reset`（65-123）：构造“理论重做进行中，学生交卷”“重做后教师解冻/学生可见旧分”“打字重做带 typingRevision 与不带”三种顺序，验证旧页面能否写回、旧分是否私下留存可查、`RETURNED` 状态是否被正确撤销。
- **B11 学生反馈**。`StudentFeedbackService.publish`（87-110）：同报告重试是否幂等；作品版本变化后已发布快照是否被覆盖；`mine`（42-84）的 `oldVersion` 判定是否只在本人可见；理论题参考答案暴露边界（64）。

### 阶段 C：课程、班级、权限与题库

- **C1 课程保存的分值规则**。`validateLessonContent`（855-947）：①随机抽题 `shuffleMode==2` 时扣除未抽题分值后是否仍等于 100 ②不同题型抽样比例组合 ③考勤课 0 题 ④一课一操作题 ⑤题型分值不一致。给出能保存出“学生可得总分 ≠ 100”或“合计 ≠ 100”的组合。
- **C2 保存时题目被清空**。`saveLessonDetails` 中 `questions == null` 时是否等价清空全部题（结合约 697-748 的归档与删除）。核对前端 `F/api/business/lesson.js` / 设计器保存请求：只改标题、只改班级、只改教材绑定时是否会省略 `questions`。
- **C3 开放状态被覆盖**。保留旧班级 `theoryOpen/practicalOpen`，新班级用设计器值，同时 `advanceCurrentAssignment` 推进时总把两开关置 1（`M/BizLessonAssignmentMapper.xml:175-186`）。构造：教师课上关闭操作题 → 保存课程（改标题）→ 推进下一课 → 回看上一课开关。找出任何“正在上课班级的开关被设计器默认值覆盖”的路径。
- **C4 共享课程能力**。`canDesign = isAdmin || isCreator`（约 483-485、553），不存在 `canDelete`；删除由 `assertCanManageLesson` 决定。核对：共享教师是否可能在界面上看到、并能真正调用设计/删除接口；`sameDept` 算出未用（约 1149）后 creator 调岗跨校保存，是否会把课程 `deptId` 改到新学校（约 649）。
- **C5 单题删除无归属校验**。`BizQuestionServiceImpl.deleteBizQuestionByQuestionId`（216-230）无 creator/admin 判断，控制器仅 `business:question:remove`（`BizQuestionController.java:118`）。验证：任意持该权限的教师可否删除他人私有题；批量删除（175-214）与此规则不一致会带来什么后果（如前端只调单删）。
- **C6 编辑越权返回 -1**。`updateBizQuestion`（127-159）非本人返回 -1 且无写入，`@Transactional` 正常提交。核对所有调用方（Controller、导入、课程保存、题单保存）是否把 -1 当成功；导入路径 `importQuestion`（247-390）是否会因此产生“部分成功”。
- **C7 题库列表与工作台权限**。`selectBizQuestionList`（96-103）非管理员强制 creatorId=自己；前端 `cardActions` / `hasPermi` / `canManageQuestion`（`F/utils/questionWorkbench.js`、`F/views/business/question/index.vue:747, 794-798, 1513-1518, 1549-1552`）。构造：管理员、有 add 无 edit 的教师、只有 remove 的教师、权限快照过期后（`permissions` 是首屏一次性快照）。判断按钮可见性与服务端是否出现**任一方向**不一致（能点但不能做 / 不能点但接口能做）。
- **C8 班级指派与互斥**。`deactivateOtherCurrentAssignments`（846-853）会停掉同班在其他课程上的当前指派。构造：同班两门课同时开课、学期切换、课程归档后恢复。判断是否丢历史、是否造成学生看到错误“当前课”。
- **C9 手动/自动推进一致性**。对比 `manualAdvanceClasses` 与 `scanAndAdvance`：锁、条件更新、`biz_lesson_class_scope` 写入、`ready_time`、逐班结果语义。构造：自动任务与教师手动同时推进同班；教师打开预检 → 另一教师推进 → 教师确认（`expectedLessonIds`）。核对是否会出现重复推进、推进到已删除课程、或后续班级因系统异常被误报为已执行。
- **C10 学生导入原子性**。`importStudent`（486-583）+ Redis 锁（1024-1058）：构造锁过期、同校两人同时导入、单行写库后回查数量不符、纠错时原用户名被并发修改（398-452）。验证是否可能产生“用户存在但无学生档案/无角色”。
- **C11 跨校与多校**。`ClassGroupingService.resolveManagedClassDept`（52-65）、`GuideSheetAccessService.assertCanViewLessonClass`（251-376）、`ResourceAccessService`、`BizStudentServiceImpl.selectAuthorizedStudents`（773-774 管理员 deptId 置 null）。构造跨校任课、调岗、同届同班号两校并存，核对“能看到但不能管理”“能管理但看不到”的边界。
- **C12 统计与消息的越权**。`ClassGroupingService.detail(schemeId)`（127-132）无 `requireClass`；核对控制器层是否有补救。另核对课堂 WebSocket 订阅（`B/config/ClassroomWebSocketHandler.java`、`ClassroomWebSocketHandshakeInterceptor.java`）的课程/班级校验，尤其是四段路径与三段兼容路径是否同样严格。

### 阶段 D：异步任务、外部集成、定时任务

- **D1 Python 幂等与限频顺序**。`ProgrammingSubmissionService.submit`（209-243）：先查 submission_key（214）还是先加限频计数（452-454）？重复提交是否消耗槽位；`releaseReservation` 非原子（474）；`reserveRateAndSlot` 抛异常不回滚。构造并发 20 次同键提交，给出最终行数、Redis 计数与报错类型。
- **D2 判题完成与交卷状态顺序**。判题回写 `writeExistingAnswer`（380-382，keepBest）与 `StudentProgrammingController` 提交后广播 SUBMITTED 的先后。构造：判题 SERVICE_ERROR、判题期间教师推进、判题期间学生重交。判断成绩/任务状态/推送是否三者不一致，以及是否会在旧课写回。
- **D3 取消语义**。`M/ProgrammingJudgeMapper.xml:49-53`：只能取消 WAITING。前端按钮逻辑（Python 提交页）与后端口径是否一致；学生是否被误导为“可取消”却无法取消。
- **D4 AI 任务三写点竞态**。`PracticalAiJobWorker`（111-137 校验、153-162 catch）与 `M/PracticalAiGradingMapper.xml:151-164`：构造 ①恢复线程与 API 同时 `run` ②catch 分支在结果已被新版本/新状态覆盖后写回 ③学生补交导致 `practicalVersionId` 变化。要求给出写回 SQL 的完整 WHERE 与影响行数，判断是否存在“旧模型输出覆盖新任务”。
- **D5 AI 价格与 Key**。`PracticalAiJobService`（48-106）冻结价格、`TeacherAiConfigService.configForJob`（83-98）按任务厂商取 Key。构造：教师删除某厂商 Key 后继续任务、切换默认厂商、价格被管理员修改、未知价格模型。核对是否会以 0 元估价、是否会借用他家 Key。
- **D6 学情 runToken**。`PracticalInsightWorker`（`running()` 162、`authorize()` 164-178）与 Mapper 的 `stage/progress/finish` token 条件；`saveEvidence` 无 token 条件。构造：取消后 30 秒内重启同一报告、旧 worker 写 evidence 的窗口、`PracticalInsightReportService.create` 的 synchronized 与唯一键冲突 NPE（36-52）。
- **D7 文件转换池**。`AsyncConversionService`（55-72 调度、531-557 领取、167-171 毒文件拦截）、`AsyncConfig`（47-89 CallerRunsPolicy / AbortPolicy）。构造：LibreOffice 全挂、线程池满、同一答卷被小时重试任务与修复任务同时领取。核对页图状态是否可能永久停在 pending、毒文件是否被自动捞回。
- **D8 定时任务并发**。`sql/` 中 `sys_job` 的 `concurrent` 设置与注释冲突（如 `practicalGradingDeadlineTask` 注释写“禁止并发”但 `concurrent='1'`）。核对 `LibreOfficeMaintenanceTask.cleanupAndRestart`（00:00）与 `healthCheckAndRecover`（每分钟）同时运行的影响；核对 `lessonAutoAdvanceTask` 每 5 分钟与手动推进的关系。
- **D9 IoT 凭据**。`IotCredentialExpiryJob`（28-33）：本地置失效成功、远端撤销失败的路径；`IotMqttReceiver`（229-244 设备缺失回退组、300-317 限流、314-317 主题）消息去重缺失。判断学生端接口是否可能拿到已撤销设备的口令。
- **D10 机位助手**。`SeatHelperController`/`SeatHelperService`：构造设备被管理员撤销后重新配对、同机换学生（latest/owner）、浏览器切换（open 票据 75 秒）、伪造 Origin、Redis TTL 过期边界。判断是否会出现“A 学生判给 B 电脑”的显示错误。
- **D11 协作会话**。`CollaborationTokenService`/`CollaborationSecretService`（CryptPad）与 `CollaborationController`：构造跨班、跨校、已归档轮次、房间副本映射错误时的会话签发与回调；核对平台侧版本号是否可能被越权提交。

### 阶段 E：前端状态机与反馈真实性

- **E1 “提示成功但实际失败/部分成功”**。逐页核对：`F/views/business/teacher/grading.vue`（保存 2016 前后、失败不跳下一位 2033-2035）、`F/views/business/teacher/performance.vue`（`batchSave` 171-183 无 catch、成功后无条件清 `modified`）、`F/views/business/question/index.vue`（删除 1845-1855）、`F/views/student/index.vue`（`commitPracticalArtifact` 2588-2596 的“已提交，刷新失败”）、课程保存、题库保存。找出“页面显示已保存但服务端未落库或只落一部分”的路径。
- **E2 切班/切课/切人的迟到响应**。`performance.vue` 的 `loadStudentList`（129-145）无请求序号，快速切班是否旧覆盖新；`grading.vue` 的多组请求序号（1047/1123/1284/1801/1880）与快照锁（1306-1343）；`student/index.vue` 的 `lessonFetchSequence`/`practicalUploadEpoch`。给出至少一个可复现的“A 班数据出现在 B 班”时序。
- **E3 批改页一次网络抖动抹掉整页状态**。`grading.vue` 1329-1334：错误时清空 `submissions` 与 `currentStudent` 并重复弹错；`loadScoringItems`（1124）无 catch。判断教师已录入的分数是否会丢、是否无法重试。
- **E4 前端隐藏与后端校验的缺口**。`F/directive/permission/hasPermi.js`、`F/permission.js`、`F/router/index.js`（静态路由只按 roles）、`F/store/modules/permission.js`。逐一列出“只靠前端隐藏”的入口，并到后端确认对应接口是否都有 `@PreAuthorize`/数据范围校验；没有的列为越权候选（注明“代码证实”或“待验证”）。
- **E5 登录/会话**。`F/utils/session.js`（401 二次确认 107-126）、`F/utils/request.js`（重复请求 1 秒去重 63-89、blob 错误 36-44、403 无专分支 120-131）。构造：令牌过期瞬间提交答卷/批改分数/保存课程，判断是否会静默失败且用户误以为成功。
- **E6 空间切换（常规/拓展）**。`F/store/modules/studentSpace.js`（12-27）、`F/components/StudentSpaceSwitch`、`F/views/login.vue:617`。构造：切换失败、迟到响应、context 为空、切换后仍停留在旧空间题目页。判断是否可能显示 A 空间数据却在 B 空间提交。
- **E7 教材阅读器**。`F/views/business/textbook/reader.vue`（连续滚动、`applyPresetSection` 437-454 定位重试无作废控制、失败重试）、`F/components/LazyPageImage.vue`。判断快速连续切换课次/缩放时是否错位到旧定位。
- **E8 消息可见性**。`ElMessage` 顶部层叠与 `student/index.vue` sticky 头部（2866-2896）。构造 390px 窄屏、页面长滚动，判断提交结果/失败提示是否被遮挡或滚出视野。

---

## 4. 跨模块与异常场景（必须额外设计，不得只跑上面清单）

至少覆盖以下组合，并为每个组合写出操作顺序：

1. **课程生命周期 × 作答**：建课 → 指派 → 学生答题 → 教师改分 → **课程保存（删题/改分值/换班）** → 推进 → 查看成绩/导出。重点：删题归档 → 成绩与导出是否仍含旧题分；改分值是否影响已有成绩。
2. **重做 × 反馈快照 × AI**：重做后旧反馈快照、旧 AI 建议是否仍可被采用。
3. **自动推进 × 手动推进 × 规则变更**：推进后强制开放开关与设计器开关冲突。
4. **学生导入 × 成绩 × 期限**：新增学生、停用学生、纠错改名后，缺考、分母、期限起算是否一致。
5. **文件格式扩展 × 旧题**：新格式（TXT/HTML/压缩包/PY）只对显式允许的题开放；旧题、参考上传、区域抽测不得自动放开。构造：旧题上旧客户端直接调新上传接口。
6. **拓展班空间 × 常规课 × 题单**：拓展课成员/签到/题单与常规课推进、分组、物联入口的排斥；空间切换后的迟到提交。
7. **跨校 × 共享课程 × 调岗**：教师调岗后管理原校课程、学生跨校转学后成绩归属。
8. **AI Key × 厂商 × 价格**：见 D5，并与 B9 的采用链路联合。
9. **机位助手 × 换账号 × 撤销**：见 D10，并与课堂大屏展示联合（IP 变化只提示不封禁）。
10. **异常与边界**：超长文本、超 2MiB 文本、0 字节文件、图片格式伪装、文件名特殊字符、跨时区时间、学期切换日（7 月 20 日前后）、空班级、0 题考勤课、全部请假、教师请假期间推进。

---

## 5. 审查执行方法（硬要求）

1. **调用链优先**：任何结论必须写明“前端函数 → API path → Controller 方法:行 → Service 方法:行 → Mapper id:行 → 表/唯一键”。跳步的结论标 `未追踪`。
2. **故障推演**：每个场景写“操作顺序 + 关键状态 + 期望 + 实际（推演）”，推演中必须说明是哪一行代码允许了错误发生。
3. **证据分级**（报告里每条 bug 必须标注其一）：
   - `已复现`：你在不写库的前提下用只读查询、单测运行、或现有测试证明；
   - `代码证实`：调用链完整、条件明确，能指出确切代码路径与触发输入；
   - `待验证`：依赖运行时数据、并发时序、外部系统或未读到的调用方，必须写“需要什么证据才能确认”。
   禁止把 `待验证` 写成确定结论；禁止凭“看起来很危险”下结论。
4. **误报排除**：下结论前必须检查——是否已有唯一键/锁/条件更新兜住；是否有外层事务回滚；是否前端另有闸门；是否该规则在 `BUSINESS_BOUNDARIES.md` 中被明确设计如此（若如此则改为“设计与文档不一致”或“设计风险”列出，不算 Bug）。
5. **测试与运行**：允许只读查库、运行已有单测/构建（`mvn -pl ruoyi-business -am test "-DforkCount=0"`、`npm run test:*`）；**禁止**写库、启服务连接正式环境、调用有副作用的业务接口、修改任何文件。
6. **规模控制**：阶段 A→F 顺序执行；每阶段最多输出 8 条高价值条目，超出则合并或降级到附录。

---

## 6. Bug 报告格式（每条严格按此结构）

```
### [P?] 一句话标题
- 等级：P0 / P1 / P2 / P3
- 证据级别：已复现 / 代码证实 / 待验证
- 位置：文件路径:行号（必要时列 2~4 个关键位置）
- 涉及数据：表/字段/Redis key/文件目录
- 触发条件：角色、前置状态、具体操作顺序
- 实际结果：（推演或复现到的）
- 预期结果：（引用 BUSINESS_BOUNDARIES.md / 用户可接受行为）
- 根因：代码行 + 为什么写错了（不要只写“缺少校验”）
- 影响面：谁会受害、数据是否可恢复、是否影响正式成绩
- 最小修复建议：只描述方向与落点（不改代码）
- 需要的补充证据：（待验证时必填）
- 同类位置：我顺带发现的相同模式（若有）
```

**等级定义**（订单务必一致）：
- **P0**：正式成绩错误/丢失、越权读写他人成绩或作品、旧课写入生效、数据不可恢复、能把错误状态当成功展示给用户。
- **P1**：高频操作产生错误统计/错误排名/错误反馈、教师或学生核心流程卡死且无替代路径、异步任务静默丢失。
- **P2**：条件性错误（需特定顺序/特定数据）、状态短时不一致但可自愈、导出与页面口径差异。
- **P3**：体验缺陷、提示重复或遮挡、可自动恢复的竞态窗口、无法触发的理论风险（需注明为何难以触发）。

---

## 7. 只读安全约束（不可逾越）

- **不修改、不创建、不删除**任何项目文件（含 `contexts/`、`sql/`、文档）；不 `git add/commit/push`。
- **不执行写库 SQL**、不 `mysqldump` 覆盖文件、不改 Redis key、不改上传目录、不清理缓存。
- **不启动正式环境**（130/123），不调用会产生业务写入的接口（提交答案、批改、推进、导入、AI 任务、MQTT 下发）。
- 如需运行测试或构建，只在本机工作区；失败不视为结论，需写明原因。
- 凭据、Token、Cookie、私密路径不得出现在报告、截图或命令回显中。

---

## 8. 自主审查要求（防止清单化）

1. 本清单是**起点不是终点**。每阶段完成后，你必须主动去读“还没被任何人看过的代码”：至少找到 3 个清单外的新入口或新调用链，并同样追踪到底。
2. 优先怀疑以下模式，并在项目里找实例：
   - 同一业务事实存在多个写入口，其中一个少了校验；
   - “先查后写”但没有锁，或锁的粒度与判定的数据不一致；
   - 条件更新没检查影响行数，失败被当成功；
   - 前端用本地状态推断成功，后端实际失败或部分失败；
   - 旧页面/旧客户端/旧数据在新规则下产生错误；
   - 统计口径在页面、导出、接口、AI 四处各写一份。
3. 对无法确认的项目细节（表列是否存在、数据量、配置值、130 与 123 差异），写“需要查证：具体查什么”，**不得编造**。
4. 每份阶段报告结尾给出：本阶段**已证实最高优先级问题 Top 3**、**最需要补的证据**、**建议最先人工验证的一条操作**（能 5 分钟内做完的）。
5. 最终报告（阶段 F 之后）给出跨阶段总表：按 P0→P3 排序，去重合并同一根因，标注每条是“代码证实/已复现/待验证”，并单列“已知设计取舍、不按 Bug 处理”的清单。

---

**开始执行前先做一件事**：仅输出阶段 A 的审查计划（要读的文件与调用链、要构造的场景），等待确认后进入正式审查。
