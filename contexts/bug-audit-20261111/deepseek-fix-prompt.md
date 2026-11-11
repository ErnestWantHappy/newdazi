# 给 DeepSeek 的修复任务书：信息科技学业测评平台 1.30.96 缺陷修复与上线

> 本任务书由 Claude 在 2026-11-11 逐条读源码核查后编写。下列每条缺陷都已在**当前工作区源码**中确认（标注“已核实”）。
> 你的任务：严格按本书**逐条修复 → 补测试 → 本机验证 → 发布到 130 → 登记**。
> 不要扩大范围，不要“顺手优化”，不要修改本书“禁止修改清单”里的任何内容。

---

## 0. 开工前必读（按顺序，读完再动手）

1. `AGENTS.md`（项目操作手册：目录、启动、账号、红线、发布登记要求）
2. `contexts/PROJECT_CORE.md` 前 60 行（当前线上 = **1.30.95**）
3. `docs/architecture/DEPLOYMENT_RUNBOOK.md` 前 40 行（130 发布与回退约定）
4. `scripts/deploy/deploy_python_interactive_130.py`（上一次前后端同时发布用的脚本，本次照它改）

### 0.1 当前基线事实（已核实，不要重新推翻）

- Git HEAD `9c9a97b`，工作区有大量**未提交但已上线**的改动（1.30.94 Python 交互运行 + 1.30.95 打字引号 + 判题限流修复）。这些改动**就是线上现状**，本次直接在其上修改，**不要回退、不要 stash、不要 reset**。
- 130 正式：后端 `releases/20261111_typing_quote_v1`（JAR SHA-256 `741b595902488bd6446c74a5a8fbae1828694e4f49e22fa4cad538034f5d58b8`），前端 `releases/20261111_python_interactive_v1/frontend`（首页 SHA-256 `4239bdf9fb27d1fd1c509d070653826db0bd92d09395f41541a309ff3e85cd6d`）。
- 本次版本号：**1.30.96**，release 名：`20261111_bugfix_v1`（若当天已过，用实际日期）。
- **无表结构变更、无业务数据 SQL**。唯一 SQL 是产品更新登记 `sql/platform_update_1_30_96_<日期>.sql`。

### 0.2 本机环境坑（已实测）

- 后端测试工作目录 `RuoYi-Vue`，命令必须带两个参数：
  ```
  mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false
  ```
  只跑某个类：加 `-Dtest=类名`。全量基线：**1040 通过 / 0 失败 / 3 跳过**，修完后只能多不能少。
- 前端工作目录 `RuoYi-Vue3`：`npm run test:python-runner`（基线 20 项通过）、`npm run build:prod`（含发布门禁）。
- 后端启动必须加 `-Dserver.port=8080`（本机有 `SERVER__PORT=50485` 环境变量干扰）。
- 凭据只在 `contexts/secrets.local.md`，**任何输出、日志、提交信息、文档里都不准出现密码/Token**。

### 0.3 全局编码规则

- 注释用简体中文，只解释“为什么”。保持原有代码风格（很多文件是一行写很长的风格，照旧即可）。
- 每修一条，在同一条目下补测试（有测试类的加到现有测试类里）。
- 每修完一条立即跑该条相关测试，通过再做下一条。**不要攒到最后一起跑。**

---

## 1. 必须修复的缺陷（共 22 条，按优先级）

格式：【编号｜级别】标题 → 位置 → 现象 → **修法（照做）** → 测试。

### A 组：P1/P2 高价值

#### 【F01｜P1】区域抽测超时上传：自动交卷被事务回滚

- 位置：`RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/service/impl/CountyExamServiceImpl.java` 第 567-598 行 `validateStudentWorkUpload`；调用方 `controller/CountyExamController.java` 第 278 行。
- 现象：方法有 `@Transactional(rollbackFor = Exception.class)`，第 588-590 行先 `autoSubmitStudentExam(exam, student)` 再 `throw new ServiceException(...)`，抛异常导致整个事务回滚，自动交卷全部作废，学生仍停在“作答中”。`ServiceException` 是 `final` 类，不能继承出不回滚的子类。
- 修法：
  1. 服务方法超时分支改为：调用 `autoSubmitStudentExam(exam, student)` 后 **`return null;`**（不抛异常，让事务正常提交）。在接口 `ICountyExamService.validateStudentWorkUpload` 的 Javadoc 写明“返回 null 表示已超时且系统已自动交卷”。
  2. 控制器 `uploadStudentWork` 第 278 行之后加：
     ```java
     if (studentId == null) {
         // 服务层已在独立提交的事务里自动交卷，这里再提示学生，不能在事务内抛错，否则交卷会被回滚。
         throw new ServiceException("区域抽测作答时间已结束，系统已自动交卷");
     }
     ```
  3. 先确认 `CountyExamController` 类和该方法**没有** `@Transactional`（核查时没有；如果有，停下来在报告中说明，不要继续这条）。
- 测试：`src/test/java/com/ruoyi/business/service/impl/CountyExamServiceImplTest.java` 新增用例：构造已超时的学生（参照已有 `prepareStudentContext`），断言 `validateStudentWorkUpload` 返回 `null`、`autoSubmitStudentExam` 的关键写库调用（如 `markSubmittedIfOpen`）被执行、且不抛异常。

#### 【F02｜P1】自动推进会推进到已归档课程，整班学生“暂无课程”

- 位置：`service/impl/LessonAutoAdvanceServiceImpl.java` 第 558-603 行 `findNextLesson`；SQL `resources/mapper/business/BizLessonMapper.xml` 第 42-54 行。
- 现象：查询没设 `status`，`selectBizLessonList` 在 status 为空时不过滤；循环也不检查状态。下一课若已归档（status='1'），班级当前课被改成归档课，而学生端当前课查询要求 `IFNULL(status,'0')='0'`（`BizLessonAssignmentMapper.xml` 第 133 行），整班空白且后续课不再自动推进。
- 修法：在 `query.setGrade(...)` 后加 `query.setStatus("0");`，并在 for 循环里（attendance 判断旁边）加一道保险：
  ```java
  // 已归档/停用的课学生端看不到，推进过去会让整班当前课变空。
  if (l.getStatus() != null && !"0".equals(l.getStatus())) continue;
  ```
  （先确认 `BizLesson` 有 `getStatus()`；没有就只做第一步。）
- 测试：`LessonAutoAdvanceServiceImplTest.java`（第 392 行附近有跨年级用例，照着写）新增“课序 1、2(归档)、3，从 1 推进应到 3”。注意 mock 的 `selectBizLessonList` 要能体现 status 过滤（可断言传入的 query.getStatus() 为 "0"）。

#### 【F03｜P1】独立刷题：Judge0 轮询超时被当成判题完成，写入错误成绩和进度

- 位置：`service/PythonPracticeService.java` 第 685-733 行 `judgeSubmission`，第 707 行轮询循环。
- 现象：轮询满次数仍未 `isFinished()` 不报错，未完成结果继续参与判定和加权计分，并 `upsertProgress`。课程链路 `ProgrammingSubmissionService.executeWith`（第 500-503 行）在同样情况会抛“判题轮询超时”，刷题链路漏了；任一测试点 `SERVICE_ERROR` 也没有中止。
- 修法（与课程链路对齐）：
  1. 第 707 行轮询循环后加：`if (result == null || !result.isFinished()) throw new ServiceException("Judge0 判题轮询超时");`
  2. 计算出 `status` 后加：`if (!customRun && "SERVICE_ERROR".equals(status)) throw new ServiceException("判题服务异常");`（customRun 也可以一起抛，二者都走外层 catch，结果一致）。
  3. 外层 `catch (Exception ex)` 已经会写 `SERVICE_ERROR` + `score=null` 且不写进度，**不用改**。
- 测试：在 `src/test/java/com/ruoyi/business/service/PythonPracticeInteractiveRunTest.java` 或新建 `PythonPracticeJudgeTimeoutTest.java`：mock Judge0Client 的 `poll` 永远返回未完成结果（statusId=2），断言 `updateSubmissionResult` 收到 `statusCode=SERVICE_ERROR`、`score=null`，且 `upsertProgress` **从未被调用**（`verify(mapper, never()).upsertProgress(any())`）。注意 `judgeProperties.getMaxPolls()` 设为 1~2、`pollIntervalMs` 设为 100，避免测试变慢。

#### 【F04｜P1】教师“用参考代码生成输出”：输入不够时仍判 ACCEPTED 并回填不完整答案

- 位置：`service/ProgrammingSubmissionService.java` 第 175-205 行 `generateExpectedOutputs`，第 192 行。前端回填 `RuoYi-Vue3/src/views/business/question/index.vue` 约第 1496 行只认 `statusCode === "ACCEPTED"`。
- 现象：参考代码有两个 `input()` 但测试点只给一行输入时，套壳写 NEED_INPUT 标记并以 `SystemExit(0)` 正常退出，Judge0 状态为 3，被标成 `ACCEPTED`，前端把只有一半的输出回填为期望输出，后续全班按错误标准判分。
- 修法：第 192-194 行改为：
  ```java
  boolean needInput = JudgeOutput.needsMoreInput(result == null ? null : result.getStderr());
  // 参考代码还停在 input() 等输入，说明该测试点输入行数不够，输出不完整，不能当成期望输出。
  item.put("statusCode", needInput ? "RUNTIME_ERROR" : Judge0StatusMapper.toPlatformStatus(result));
  item.put("errorSummary", needInput ? "参考代码还在等待输入：该测试点给的输入行数不够，请补足输入后再生成"
          : (result == null ? null : trim(firstNonEmpty(result.getCompileOutput(), JudgeOutput.cleanError(result.getStderr()), result.getMessage()), 1000)));
  ```
  前端不用改（非 ACCEPTED 本来就不回填）。确认前端失败的测试点会把 `errorSummary` 显示给教师；如果没有显示，在现有失败提示处加上（只加文字，不改布局）。
- 测试：`ProgrammingTeacherValidationTest.java` 新增：mock 返回 statusId=3、stderr 含 `JudgeSourceWrapper.NEED_INPUT`，断言该项 `statusCode` 为 `RUNTIME_ERROR` 且 errorSummary 不含控制字符 `\u0000`。

#### 【F05｜P1】学生代码 `except Exception:` 会吞掉“等待输入”信号

- 位置：`judge/JudgeSourceWrapper.java` 第 54 行 `class _PlatformNeedInput(Exception)`。
- 现象：学生写 `try: x = input() except Exception: ...`（初学者常见），输入耗尽时平台异常被学生代码吃掉，交互终端不会停在“等待输入”，死循环写法会直接超时；正式判题也会走进学生的异常分支。
- 修法：改为继承 `BaseException`：`builder.append("class _PlatformNeedInput(BaseException):\n");`，并在上一行加 Java 注释说明“继承 BaseException，学生的 except Exception 拦不住平台的等待输入信号；裸 except 仍会拦住，属于无法避免的边界”。外层 `except SystemExit` → `except _PlatformNeedInput` → `except BaseException` 的顺序**保持不变**。
- 测试：`JudgeSourceWrapperTest.java`（已有用本机真实 Python 3.10 执行套壳源码的用例，照写）新增：源码
  ```python
  while True:
      try:
          x = input("A:")
          print(x)
          break
      except Exception:
          pass
  ```
  stdin 为空，断言 stderr 包含 `NEED_INPUT` 标记、退出码 0、且在 5 秒内结束（不是死循环超时）。

#### 【F06｜P1】服务器交互模式：学生输入空行（直接回车）会永远卡在“等待输入”

- 位置：`RuoYi-Vue3/src/components/StudentProgrammingQuestion/interactiveRunner.js` 第 51 行；`RuoYi-Vue3/src/components/PythonRunner/serverInteractive.js` 第 9 行。
- 现象：两处都用 `inputs.join('\n')` 拼 stdin。最后一项是空串时（学生直接按回车），拼出的字符串末尾没有换行，服务端 `readline()` 读不到这一行，又返回“等待输入”，学生无论按多少次回车都推进不了。
- 修法：两处都改为**每个输入后都跟一个换行**：
  ```js
  // 每行输入都以换行结束：学生直接回车（空输入）也要算一行，否则服务器会一直等这一行。
  stdin: (inputs || []).map(value => `${value}\n`).join('')
  ```
  `serverInteractive.js` 同理：`stdin: inputs.map(value => `${value}\n`).join('')`。
  后端**不要改**：已核实 `JudgeOutput.splitLines` 会忽略末尾换行、`"\n"` 会解析成一个空输入，回放文本正确；`readline()` 读 `"a\n"` 得到 `a`。
- 测试：
  - `RuoYi-Vue3/src/components/StudentProgrammingQuestion/__tests__/interactiveRunner.test.js` 第 135、140 行断言改为 `''`（零个输入仍是空串）和 `'3\n'`；新增“输入 ['']（空回车）时 stdin 为 `'\n'`”。
  - `RuoYi-Vue3/src/components/PythonRunner/__tests__/serverInteractive.test.js` 第 24 行改为 `'3\n5\n'`，第 26 行保持 `''`。
  - 后端在 `JudgeOutputTest.java` 补一条：`terminalText("\n")` 对“提示语→空输入→输出”回放正确。

#### 【F07｜P2】服务器交互模式：切题/停止后旧请求迟到仍会改写新题终端，运行按钮被锁

- 位置：`interactiveRunner.js` 第 21-26 行 `release()`；`StudentProgrammingQuestion/index.vue` 第 281-293 行 `watch(() => props.question.questionId, ...)`。
- 现象：`release()` 不清 `requestSeq`，`stop()`/切题后，迟到的服务器响应仍通过 `requestSeq === instance` 守卫，派发 `input`/`busy` 事件，父组件 `localRunning` 被重新置为运行中，新题“运行/交作业”按钮被锁。另外 watch 只看 questionId，同题号切课（lessonId 变）不会重置。
- 修法：
  1. `release()` 内加一行 `requestSeq = null`（注释：停止、切题、重新运行都要让在途的服务器响应作废）。已核实 `runInteractiveOnServer` 是先 `release()` 再赋新 `requestSeq`，不会误伤自己。
  2. `index.vue` 的 watch 改为同时监听两个 ID：`watch(() => [props.lessonId, props.question.questionId], () => { ...原逻辑不变... }, { immediate: true })`。
- 测试：`interactiveRunner.test.js` 新增：`interactiveRun` 返回一个手动控制的 Promise；调用 `runInteractiveOnServer` 后立刻 `stop()`，再 resolve 这个 Promise（带 `needInput: true`），断言 stop 之后**没有**再收到 `output`/`input` 事件。

#### 【F08｜P2】作品附件转换：队列满时提交成功却报失败、教师批改列表 500、附件卡在“转换中”

- 位置：`service/PracticalAttachmentConversionService.java` 第 41-50 行 `convertAsync`；`service/PracticalArtifactService.java` 第 687-703 行 `registerAfterCommit`（afterCommit 回调）和约第 390-396 行 `enrichSubmissions`；线程池 `config/AsyncConfig.java` 第 82 行 `AbortPolicy`。
- 现象：`claimAttachmentNormalization` 先把状态改成 `converting`，再投递 `@Async`；队列满抛 `RejectedExecutionException`，没人捕获：在 afterCommit 里会让已提交成功的学生收到报错，在教师批改列表查询里整页 500；附件停在 `converting` 直到定时任务捞回。老链路 `AsyncConversionService` 第 117-121 行有正确兜底（`releaseClaimToPending`）。
- 修法：
  1. `PracticalArtifactMapper.java` 新增方法 `int releaseAttachmentNormalizationClaim(@Param("attachmentId") Long attachmentId);`，XML（`resources/mapper/business/PracticalArtifactMapper.xml`，参照第 202-209 行 `claimAttachmentNormalization`）新增：
     ```xml
     <!-- 投递转换线程池失败时把领取退回 pending，交给定时重试；不增加重试次数，避免被误判为毒文件。 -->
     <update id="releaseAttachmentNormalizationClaim">
         UPDATE biz_practical_attachment
         SET normalized_status = 'pending', update_time = NOW()
         WHERE attachment_id = #{attachmentId}
           AND normalized_status = 'converting'
     </update>
     ```
     **不要**复用 `resetAttachmentNormalization`（它会 `normalized_retry_count + 1`，队列满不是文件的错）。
  2. `convertAsync` 中：
     ```java
     try {
         self.processClaimedAsync(attachmentId);
     } catch (RejectedExecutionException e) {
         // 转换队列已满：退回 pending 等定时重试，不能让提交或批改列表因此报错。
         artifactMapper.releaseAttachmentNormalizationClaim(attachmentId);
         log.warn("附件转换队列已满，attachmentId={} 已退回待转换", attachmentId);
         return false;
     }
     ```
     （类里若没有 `log`，按项目习惯加 `private static final Logger log = LoggerFactory.getLogger(...)`。）
  3. `registerAfterCommit` 的 afterCommit 内，每个附件的 `convertAsync` 调用用 `try { ... } catch (RuntimeException e) { log.warn(...); }` 包住（事务已提交，任何后续异常都不能再抛给学生）。
- 测试：`PracticalAttachmentConversionFailureTest.java` 新增：`self.processClaimedAsync` 抛 `RejectedExecutionException`（`self` 是 `@Lazy` 注入的自身代理，测试里用 `ReflectionTestUtils.setField` 注入一个 mock），断言 `convertAsync` 返回 false、`releaseAttachmentNormalizationClaim` 被调用一次、不抛异常。

#### 【F09｜P2】AI 批改任务“暂停后取消”被改写成“已完成”

- 位置：`service/PracticalAiJobWorker.java` 第 393-407 行 `finish()`；`service/PracticalAiJobService.java` 第 188-213 行 `cancel` / `retryFailed`。
- 现象：任务已是 `CANCELLED` 时，worker 收尾进入 `finish()`，只拦了 `PAUSED`/`CANCEL_REQUESTED`，会把任务无条件改成 `COMPLETED`，界面显示“全部 AI 建议已生成完成”。`retryFailed` 只拦 RUNNING/PENDING，对已取消/已请求取消/暂停中的任务也能一键复活并继续花模型费用。
- 修法：
  1. `finish()` 第 397 行改为：`if (job == null || "PAUSED".equals(job.getJobStatus()) || "CANCELLED".equals(job.getJobStatus())) return;`（注释：教师已取消的任务终态不能被收尾覆盖）。
  2. `retryFailed` 在现有 RUNNING/PENDING 判断后加：
     ```java
     if ("CANCEL_REQUESTED".equals(job.getJobStatus()) || "CANCELLED".equals(job.getJobStatus()))
         throw new ServiceException("任务已取消，不能重试；如需继续请重新发起");
     if ("PAUSED".equals(job.getJobStatus()))
         throw new ServiceException("任务已暂停，请先点“继续”再重试失败作品");
     ```
  3. `gradeOne` 中 `aiMapper.updateResult(result)`（第 150 行，已核实返回 `int`）改为 `if (aiMapper.updateResult(result) == 0) { event(..., "WARN", "RESULT_DISCARDED", "任务已取消，本份 AI 建议未保存"); } else { 原来的 COMPLETED 事件 }`。
  4. **不要**改 `cancel` 里 PAUSED 分支对 PROCESSING 的取消语义（改动面大，不在本次范围）。
- 测试：`PracticalAiJobServiceTest.java` 新增 retryFailed 对 CANCELLED/CANCEL_REQUESTED/PAUSED 三种状态抛异常且 `resetFailedResults` 未调用；worker 的 finish 若难以单测，在报告中说明“未单测，靠代码审查”。

#### 【F10｜P2】教师批改页：单人打分保存后提交按钮被误锁（提示“成绩已更新请刷新”）

- 位置：`RuoYi-Vue3/src/views/business/teacher/grading.vue` 第 2136-2194 行 `submitScore`；快照比较 `sameGradingSnapshot` 第 1428 行；静默刷新逻辑第 1386-1408 行。
- 现象：保存成功后第 2193 行 `loadSubmissions({ silent: true })` 未带 `acceptServerSnapshot`。若教师仍停在同一学生（最后一名学生、或首次补建 answerId 的 Python 学生），服务器返回的新 answerId/成绩与本地不同，被判为“快照已过期”，提交按钮锁死。另外第 2167-2172 行用 `answerId` 匹配学生，多个未批 Python 学生 answerId 都是 null 时会匹配错人。
- 修法：
  1. 第 2167-2172 行一律按 `studentId` 匹配：
     ```js
     const targetItem = submissions.value.find(item => item.studentId === targetStudent.studentId);
     if (targetItem) targetItem.score = finalScore;
     if (currentStudent.value?.studentId === targetStudent.studentId) currentStudent.value.score = finalScore;
     ```
     第 2181 行 `shouldAdvance` 同样改为 `currentStudent.value?.studentId === targetStudent.studentId`。
  2. 第 2193 行改为：
     ```js
     // 仍停在刚保存的学生时，服务器返回的新成绩/新答题记录是本次保存的结果，应直接采纳，不能当成别人改过。
     loadSubmissions({ silent: true, acceptServerSnapshot: currentStudent.value?.studentId === targetStudent.studentId });
     ```
- 验证：浏览器实测（见第 3 节）。

#### 【F11｜P2】教师批改接口两处校验漏洞

- 位置：`controller/TeacherGradingController.java`。
- 修法：
  1. 第 475 行 `scoringDetail.setAnswerId(request.getAnswerId());` 改为 `scoringDetail.setAnswerId(answerId);`（第 409 行已解析出真实 answerId；首次补建答题行时 request 里是 null）。
  2. 第 418-421 行，Python 分支在现有判断前加：
     ```java
     // 页面打开时没有判题提交、现在却有了，说明学生刚交了新代码，教师还没看过，不能直接给分。
     if (best != null && request.getSystemSubmissionId() == null) {
         return AjaxResult.error("学生有新的判题提交，请刷新后重新批改");
     }
     ```
  3. 批量采用（约第 157-205 行）：`expectedByStudent` 非空（说明是新版前端传了快照）而某学生 `expected == null` 时**跳过**该生（计入 skippedCount），因为该生是页面打开后才交的、教师没看过：
     ```java
     if (expected == null ? !expectedByStudent.isEmpty() : !matchesExpectedSnapshot(expected, row)) { skippedCount++; continue; }
     ```
  4. 第 157 行（批量采用）和第 355 行（单人批改）的 `@Transactional(rollbackFor = Exception.class)` 增加 `isolation = Isolation.READ_COMMITTED`（`import org.springframework.transaction.annotation.Isolation;`）。原因：MySQL 默认可重复读，事务里先做的普通查询会固定快照，后面 `selectBestFinalSubmission` 读不到并发新提交，可能按旧最高分覆盖。项目里 `PracticalArtifactService` 已用 READ_COMMITTED，属既有做法。
- 测试：`TeacherGradingControllerPythonRunTest.java` 或现有批改控制器测试中新增：(a) `systemSubmissionId=null` 而 best 存在时返回错误；(b) 批量采用时快照列表非空但缺某生，该生被跳过。

#### 【F12｜P2】课程编程提交：入队失败后课堂状态被标成“已提交”，额度也不退

- 位置：`ProgrammingSubmissionService.java` 第 285-298 行；`controller/StudentProgrammingController.java` 第 79-83 行。
- 现象：(a) `setCacheObject(slotKey...)`（第 291 行）若 Redis 异常，外层 catch 只退额度，提交行永远停在 WAITING；(b) `worker.judge` 被拒（第 294-297 行）走 `completeServiceFailure + releaseSlot` 但不退分钟额度；(c) 控制器不看结果，SERVICE_ERROR 也把课堂状态标成 `SUBMITTED`。
- 修法：
  1. 把第 291 行 `setCacheObject` 挪进第 292 行的 `try` 块内（放在 `worker.judge` 之前），这样 Redis 写失败也走 `completeServiceFailure + releaseSlot`。
  2. 该 catch 块末尾加 `redisCache.decrementCounterOrDelete(rateKey);`（注释：判题没真正开始，不该占学生每分钟额度）。
  3. 控制器第 81-82 行改为只在非服务异常时写状态：
     ```java
     if (!"SERVICE_ERROR".equals(submission.getStatusCode()))
         taskStateService.markSafely(...原参数...);
     ```
- 测试：`ProgrammingRunLockContractTest.java`（已有额度退还契约测试）新增：`worker.judge` 抛 `RejectedExecutionException` 时 `decrementCounterOrDelete(rateKey)` 被调用、提交状态为 SERVICE_ERROR。

#### 【F13｜P2】历史拓展班（legacy）题单“查看学情”永远 0 人

- 位置：`resources/mapper/business/PythonPracticeMapper.xml` 第 212 行和第 239 行（`selectPlanAnalyticsStudents` / `selectPlanAnalyticsQuestions`）。
- 现象：`sql/extension_legacy_snapshot_v1.sql` 第 30 行把 `plan_scope='legacy'` 的题单冻结成员写进了 `biz_extension_plan_member`，前端 `views/business/pythonPractice/index.vue` 第 17、26 行对历史只读题单仍显示“查看学情”，但两处 SQL 只认 `ap.plan_scope='extension'`。
- 修法：两处 `ap.plan_scope='extension'` 改为 `ap.plan_scope IN ('extension','legacy')`。**只改这两处**，不要动该文件其他 `plan_scope` 条件。
- 验证：本机库只读查询 `SELECT COUNT(*) FROM biz_python_practice_plan WHERE plan_scope='legacy'`；有数据就调接口对比修前修后人数，无数据就在报告里写“本机无 legacy 数据，靠 SQL 审查”。

#### 【F14｜P2】拓展班载体班级（51～99 班）把教师免抽测申请拉成“不达标”

- 位置：`resources/mapper/business/ExemptionApplicationMapper.xml` 第 66-117 行 `selectTeacherClasses`、第 123 行起 `selectTeacherCourseMetrics`；拓展班建课 `BizExtensionClassMapper.xml` 第 50、52 行（`lesson_scope='extension'`，并向 `biz_lesson_assignment` 写入 `assigner_id=owner` 的载体班级行）。
- 现象：教师当过拓展班负责人，载体班级会经 `biz_lesson_assignment`/history 进入免抽测班级候选，该班没有行政班学生，有效人数 0、使用率 0%，`ExemptionApplicationService` 第 308 行 `allClassesQualified` 被拉成 false。监管 SQL（`TeachingSupervisionMapper.xml`）已限定 `lesson_scope='regular'`，免抽测漏了。
- 修法：
  1. `selectTeacherClasses` 的最终 SELECT（`FROM class_candidate candidate`）加 WHERE：
     ```sql
     WHERE NOT EXISTS (
         SELECT 1 FROM biz_extension_class ext
         WHERE ext.dept_id = candidate.dept_id
           AND ext.entry_year = candidate.entry_year
           AND ext.class_code = candidate.class_code)
     ```
     注意 XML 里 `<`、`>` 要转义；若字符集排序规则冲突报错，参照文件内已有的 `COLLATE utf8mb4_general_ci` 写法。
  2. `selectTeacherCourseMetrics` 中所有 JOIN `biz_lesson` 的地方补 `AND IFNULL(lesson.lesson_scope,'regular') = 'regular'`（没 JOIN biz_lesson 的分支，在最终聚合处 JOIN 一次过滤），并同样排除载体班级。
- 验证：本机库先只读查 `SELECT dept_id, entry_year, class_code, owner_user_id FROM biz_extension_class`，挑一位负责人教师，调用免抽测预览接口对比修前修后班级列表；`ExemptionApplicationServiceTest.java` 全部通过。

### B 组：P3 小修（改动小、风险低）

#### 【F15｜P3】协作健康检查接口学生也能调（泄露内网主机/环境变量名）

- 位置：`controller/CollaborationController.java` 第 23-27 行 `health()` 无权限注解。
- 修法：加 `@PreAuthorize("@ss.hasAnyRoles('admin,teacher')")`。保留 teacher 的原因：`scripts/release-engine.ps1` 第 123 行用教师 Token 做发布探活。

#### 【F16｜P3】区域抽测首次进入并发 500

- 位置：`CountyExamServiceImpl.java` 第 531-537 行（getStudentExam 内）。
- 现象：`ensurePaperForStudent`（先查数量再批量插入）在 `lockStudentAttempt`（FOR UPDATE）之前执行，两个标签页同时首次进入会撞唯一键 500。
- 修法：startTiming 分支改为先锁后建卷：
  ```java
  CountyExamStudent examStudent;
  if (startTiming) {
      // 先锁参与记录再建卷，同一学生并发首次进入时串行执行，避免撞试卷唯一键。
      CountyExamStudent locked = lockStudentAttempt(exam.getExamId(), student.getStudentId());
      ensurePaperForStudent(exam, student.getStudentId());
      examStudent = ensureStudentTiming(exam, locked);
  } else {
      examStudent = studentMapper.selectByExamAndStudent(exam.getExamId(), student.getStudentId());
  }
  ```
  `ensureParticipant` 在前面已执行，锁一定能锁到行。确认同文件其它调用 `ensurePaperForStudent` 的地方（第 593 行上传校验）已在锁之后，不用改。

#### 【F17｜P3】区域抽测上传归属只保留 60 分钟，长考试最后提交报“作品路径无效”

- 位置：`CountyExamController.java` 第 284-285 行。
- 修法：TTL 改为 `24, java.util.concurrent.TimeUnit.HOURS`（注释：考试时长没有上限，归属键必须覆盖整场考试）。

#### 【F18｜P3】学生 Python 代码：输入后 0.8 秒内切题/离开，最后一段草稿丢失

- 位置：`StudentProgrammingQuestion/index.vue` 第 220-248 行（scheduleDraft/saveDraft）、第 281-295 行（watch 与 onUnmounted）。
- 修法：新增函数，在 watch 回调开头和 `onUnmounted` 里、清理定时器**之前**调用：
  ```js
  // 切题或离开页面前把还没来得及自动保存的代码补存一次；只发请求不等结果，避免卡住切换。
  function flushDraftNow() {
    if (!pendingDraft.value || !loadedQuestionId || !sourceCode.value.trim() || lastSavedSource === sourceCode.value) return
    saveProgrammingDraft({ lessonId: props.lessonId, questionId: loadedQuestionId, sourceCode: sourceCode.value }).catch(() => {})
  }
  ```
  注意：watch 回调执行时 `props` 已是新题，所以必须用 `loadedQuestionId`（旧题 ID）。先确认 `loadedQuestionId` 确实保存的是当前已加载题目的 ID、lessonId 在 F07 改动后也需取旧值——如果 watch 改成监听数组，用 watch 回调第二个参数 `(newVal, oldVal)` 里的 `oldVal[0]` 作为旧 lessonId、`oldVal[1]` 作为旧 questionId，比 loadedQuestionId 更可靠。首次 immediate 执行时 oldVal 为 undefined，直接跳过。

#### 【F19｜P3】电子教材 PDF：缓存目录不可写时失败会留下临时文件

- 位置：`service/TextbookPdfService.java` 第 204-218 行。
- 现象：只 catch `IOException`，而 `buildWithPermit` 抛的是 `ServiceException`，temp 不会被删。
- 修法：
  ```java
  Path temp = null;
  boolean ok = false;
  try {
      temp = Files.createTempFile("textbook-pdf-", ".pdf");
      buildWithPermit(pages, temp, title);
      ok = true;
      return new PdfDownload(temp, filename, true);
  } catch (IOException e) {
      throw new ServiceException("PDF 生成失败，请稍后重试");
  } finally {
      // 任何失败都要清掉半成品；成功时文件交给下载流程，下载后删除。
      if (!ok) deleteQuietly(temp);
  }
  ```

#### 【F20｜P3】课例评价首次投票空原因存成空串

- 位置：`mapper/TextbookCaseFeedbackMapper.java` 第 21-25 行 `vote`；服务层 `TextbookCaseFeedbackService.vote` 第 40-52 行（约定：空串=明确清空，null=快捷改票保留旧理由）。
- 现象：INSERT 分支原样写入 `''`，UPDATE 分支却会把 `''` 归一为 NULL，两条路径不一致。
- 修法：**不能**只把 INSERT 改成 `NULLIF(#{reason},'')`——UPDATE 分支用 `VALUES(reason)` 读的就是这个插入值，会让“清空理由”退化成“保留旧理由”。正确做法是 INSERT 和 UPDATE 都直接引用参数 `#{reason}`：
  ```java
  @Insert("INSERT INTO biz_textbook_case_feedback(case_id,user_id,vote,reason) "
      + "VALUES(#{caseId},#{userId},#{vote},CASE WHEN #{vote}=0 THEN NULL ELSE NULLIF(#{reason},'') END) "
      + "ON DUPLICATE KEY UPDATE "
      + "vote=VALUES(vote), "
      + "reason=CASE WHEN VALUES(vote)=0 THEN NULL WHEN #{reason} IS NOT NULL THEN NULLIF(#{reason},'') ELSE reason END, "
      + "updated_at=NOW()")
  ```
- 验证：本机库对同一测试课例依次执行：首次投票空原因 → 查 reason 为 NULL；改票带原因“好” → 为“好”；快捷改票（reason=null）→ 仍为“好”；传空串 → NULL；撤销（vote=0）→ NULL。测试行用完精确删除。

#### 【F21｜P3】AI 批改：结果卡在“处理中”，除重启外无法恢复

- 位置：`PracticalAiJobService.java` 第 172-181 行 `resume`；`resetInterruptedResults(jobId)`（`PracticalAiGradingMapper.xml` 第 169-174 行，无时间条件）目前只在启动恢复时调用。
- 现象：worker 在写失败结果时自身出错（如数据库瞬断），该份停在 PROCESSING；`retryFailed` 只认 FAILED，`resume` 只改任务状态，教师无法自助恢复。
- 修法：**不要**在 resume 里直接调 `resetInterruptedResults`——暂停后马上继续时，旧 worker 可能还在处理那一份，会造成同一份被处理两次。新增带时间条件的方法：
  - `PracticalAiGradingMapper.java`：`int resetStaleProcessingResults(@Param("jobId") Long jobId, @Param("staleMinutes") int staleMinutes);`
  - XML（照 `resetInterruptedResults` 写）：
    ```xml
    <!-- 只回收明显卡死的份：正常一份模型请求远小于该时长，避免把仍在处理的那份重复派发。 -->
    <update id="resetStaleProcessingResults">
        UPDATE biz_practical_ai_result
        SET result_status='PENDING',processing_stage='WAITING',processing_started_time=NULL,
            stage_updated_time=NOW(),error_message=NULL,finish_time=NULL
        WHERE job_id=#{jobId} AND result_status='PROCESSING'
          AND (processing_started_time IS NULL
               OR processing_started_time &lt; DATE_SUB(NOW(), INTERVAL #{staleMinutes} MINUTE))
    </update>
    ```
  - `resume` 在 `updateJobStatus(... "PENDING" ...)` 之前调用 `mapper.resetStaleProcessingResults(jobId, 15);`
  - 已核实 `markResultProcessing` 认领时会写 `processing_started_time=NOW()`。
- 测试：`PracticalAiJobServiceTest.java` 新增 resume 调用了 `resetStaleProcessingResults(jobId, 15)`。

#### 【F22｜P3】监管学生明细缺少学期时间窗，与班级行数字对不上

- 位置：`resources/mapper/business/TeachingSupervisionMapper.xml` 约第 676-681 行（`selectStudentDetails` 的 `LEFT JOIN biz_student_answer practical`）；对照第 580-587 行班级汇总的写法。
- 修法：先确认 `selectStudentDetails` 的参数对象 `q` 有 `activityStartTime`/`activityEndTime`（看对应 Mapper 接口和 Controller 第 208-216 行传参）。有则在 practical JOIN 条件中补：
  ```xml
  AND practical.submit_time &gt;= #{q.activityStartTime}
  AND practical.submit_time &lt; #{q.activityEndTime}
  ```
  没有则**跳过**并报告，不要为此改接口。

---

## 2. 禁止修改清单（核查结论：误报、业务决策或风险过大）

以下是其他 AI 报告中出现、但**不要动**的项。原样写进最终报告“未修复及原因”一节：

| 项 | 不修原因 |
|---|---|
| hy4“限流超限续期导致滚动锁定” | **误报**：`rateKey` 含当前分钟（`ProgrammingSubmissionService` 第 722 行），每分钟换新键，旧键不会被持续续期 |
| 21 天批改期限：Python 提交未建答题行导致补批时中途锁定、监管/免抽测/首页漏计纯判题提交 | 真实存在，但改的是“批改期限如何计算”的业务规则，须用户/教研员决策；写进报告 |
| 学生转班后期限按当前班查询 | 涉及“期限按提交时班级还是当前班级”的业务口径，需决策 |
| 刷题 `submit` 无每分钟限流 | 既有设计，PROJECT_CORE 已记录 |
| 题库 fork 另存时创建人被改成当前用户 | `insertBizQuestion` 被新增接口共用，改成“仅为空才赋值”可能让前端伪造创建人，安全风险大于收益 |
| 清空“第几课”不生效、非选择题新建无幂等回执、答案大小写导致误 fork | P3，改动涉及 null 语义与回执表，留给专项 |
| 导学单草稿 reopen、人工评分并发覆盖、complete 失败残留 | P3，留给导学单专项 |
| 理论重做混合交卷整单回滚、INSERT IGNORE 误报 | 有意的原子性取舍 |
| `lesson_scope IS NULL` 两处口径、提交路径学生锁无认主删除、8 许可容量 | PROJECT_CORE 已登记“刻意不修” |
| AI 任务 PAUSED 取消时连带取消 PROCESSING 结果 | 语义变更面大，本次只修 F09 的终态覆盖 |
| 免抽测附件唯一键、监管“逾期班级数”含已批完班级、双角色教师拓展班下拉 | P3，需结合产品确认 |
| `PurgeMapper` 子表孤儿数据 | 删除类操作，须单独评审 |
| 物联网 `createGroup` 固定组号 1、DataLab 权限错配 | 当前 UI 不可达或需权限模型决策 |
| 旧 Vue2 前端 `RuoYi-Vue/ruoyi-ui/` | 项目约定不维护 |

---

## 3. 本机验证（全部通过才能发布）

按顺序执行，每一步失败都先修好再往下：

1. 后端全量单测（`RuoYi-Vue` 目录）：
   ```
   mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false
   ```
   要求：0 失败、0 错误；通过数 ≥ 1040 + 本次新增数。记录最终数字。
2. 前端（`RuoYi-Vue3` 目录）：`npm run test:python-runner` 全过；`npm run build:prod` 成功（含发布门禁）。
3. 后端打包：`mvn -pl ruoyi-admin -am clean package -DskipTests`，记录 JAR SHA-256。
4. 本机启动（参照 AGENTS.md 3.2，带 `TEXTBOOK_ROOT` 和 `-Dserver.port=8080`），前端 `npm run dev`。
5. 浏览器冒烟（Playwright，截图存 `output/playwright/bugfix-1.30.96/`，不要截到密码）：
   - 教师 `19157727791`：进入批改页，选一道 Python 题，给**最后一名**已提交学生打分保存，确认“提交批改”按钮**不被锁**、无“成绩已更新请刷新”提示（验证 F10）。
   - 学生 `st99_363`：进入当前课 Python 题，用浏览器开发者工具把 `WebAssembly.Suspending` 删掉模拟旧浏览器（或按 1.30.94 验收时的方法强制服务器模式），运行含 `input()` 的程序，**直接按回车输入空行**，确认能推进到下一步（验证 F06）；运行中立刻切到另一题，确认新题运行按钮可用（验证 F07）。
   - 学生账号调 `GET /business/collaboration/health` 应返回 403（验证 F15）。
   - 1440 与 390 两种宽度各截一张，0 页面脚本错误、0 个 5xx。
6. 所有本机造的测试数据精确删除，残余 0，报告里写明删了什么。

---

## 4. 发布到 130（用户已授权自动上线）

### 4.1 准备

1. 复制 `scripts/deploy/deploy_python_interactive_130.py` 为 `scripts/deploy/deploy_bugfix_130.py`，修改常量：
   - `RELEASE = "20261111_bugfix_v1"`，`VERSION = "1.30.96"`
   - `OLD_FRONTEND = "/data/apps/xueyeceping/releases/20261111_python_interactive_v1/frontend"`
   - `OLD_JAR` / `OLD_BACKEND` 指向 `releases/20261111_typing_quote_v1`，`OLD_JAR_SHA = "741b595902488bd6446c74a5a8fbae1828694e4f49e22fa4cad538034f5d58b8"`
   - `OLD_INDEX_SHA = "4239bdf9fb27d1fd1c509d070653826db0bd92d09395f41541a309ff3e85cd6d"`
   - `ARTIFACT_DIR = ROOT / "output/release-20261111-bugfix-130"`
   - 注册更新的 SQL 路径改成本次新文件。
   逐行读一遍脚本，确认没有其他写死的旧版本信息（如产品更新编号 172、旧 release 名）。
2. 制品目录 `output/release-20261111-bugfix-130/` 放 `ruoyi-admin.jar`、`frontend.tar.gz`、`manifest.json`（格式照上次 `output/release-20261111-python-interactive-130/manifest.json`，含文件字节数和 SHA-256）。
3. 新建 `sql/platform_update_1_30_96_20261111.sql`，照 `sql/platform_update_1_30_95_20261111.sql` 的幂等写法，status 直接 `PUBLISHED`。标题建议“Python 运行与批改多项问题修复”，正文用老师能看懂的话，例如：
   > 修复了多项课堂使用中的问题：旧版浏览器运行 Python 时直接按回车不再卡住；切换题目后运行按钮不再被锁；教师给最后一名学生打分后不再提示“成绩已更新”；用参考代码生成期望输出时，若测试点输入不够会明确提示而不是生成半截答案；区域抽测超时后上传作品会正确自动交卷；自动推进课程会跳过已归档的课；历史拓展班题单可以正常查看学情；带过拓展班的老师申请免抽测不再被误判不达标。

### 4.2 执行（每步贴非敏感证据：退出码、HTTP 状态、SHA-256、行数）

1. `python scripts/deploy/deploy_bugfix_130.py --mode inspect`：确认线上当前 JAR SHA = `741b5959…`、首页 SHA = `4239bdf9…`。**不一致就停止发布**，在报告中说明线上已漂移。
2. `--mode check`：只读门禁全部通过。
3. `--mode deploy`：整库备份（记录路径、字节数、SHA-256）→ 上传到新 release 目录（**不覆盖旧 release**）→ 切换 → 重启 → 探活（首页/验证码 200、MainPID 变化、NRestarts 0）。
4. 失败或探活不过：立即 `--mode rollback`，报告写“发布失败已回滚”，**不得宣称已上线**。

### 4.3 线上验收（用 `contexts/secrets.local.md` 里 130 的账号，不回显密码）

- 真实教师登录，调 `/business/platform-update/list` 确认 1.30.96 可见、`PUBLISHED`。
- 教师调生成期望输出接口：对一道含 `input()` 的题临时传不够的输入（只算不落库），确认返回 `RUNTIME_ERROR` 与中文提示。
- 学生身份（或无学生账号则匿名）调 `/business/collaboration/health` 不再返回 200 数据。
- 教师批改页 1440 截图，0 脚本错误、0 个 5xx。

---

## 5. 收尾登记（缺一项不算完成）

1. `contexts/RELEASE_LOG.md` 顶部追加 1.30.96：release 路径、JAR/首页 SHA-256、备份路径与 SHA-256、产品更新编号、修复清单、验收结果、回退方法。
2. `contexts/PROJECT_CORE.md`：版本号改 v5.49、130 正式改 1.30.96，新增一节“已上线 1.30.96”（焦点、修复、验证、未验边界、回退），把第 2 节“禁止修改清单”里需决策的两项（21 天期限与 Python 提交、转班期限口径）写进“下一步/待决策”。控制在 30 行内，只写已验证的事实。
3. `docs/architecture/DEPLOYMENT_RUNBOOK.md` 顶部追加本次发布与回退说明（照 1.30.95 格式）。
4. `contexts/bug-audit-20261111/report.md` 末尾追加一节“1.30.96 修复结果”：每条 F01-F22 的状态（已修/跳过及原因）、对应测试名。
5. Git：**只本地提交，不 push**。提交信息示例：`fix: 1.30.96 修复区域抽测超时交卷回滚、Python 交互运行与批改等 22 项缺陷`。只提交本次相关文件，不要 `git add -A` 把 output/ 下的大文件带进去。

---

## 6. 最终汇报格式（用简体中文，大白话）

1. 解决了哪些老师/学生的问题（按 F 编号，一句话一条）
2. 哪些跳过了、为什么
3. 本机测试数字（后端 通过/失败/跳过，前端测试数）
4. 线上发布信息（release、SHA-256、备份、产品更新编号）与验收结果
5. 没验证到的风险（例如：未在机房整班 45 人并发验证、未在 Safari/Firefox 实机验证）
6. 回退方法（一句话）

**红线重申**：不 push；不删旧 release 和备份；不改表结构；不批量改成绩；不在任何输出中出现密码/Token；发布失败必须回滚并如实报告。
