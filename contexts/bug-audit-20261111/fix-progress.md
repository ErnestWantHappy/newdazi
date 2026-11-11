# 1.30.96 修复进度（2026-11-11，已发布）

F01–F22 已按任务书逐项修复并补测；本机全量回归、构建、发布与线上验收已完成。保留既有未提交上线基线，未回退、未stash、未reset；未改表结构、未执行业务数据SQL（仅产品更新登记）。发布异常与备份见 `contexts/RELEASE_LOG.md`。

- F03：轮询结束未完成及正式判题测试点 SERVICE_ERROR 均转外层服务失败，score=null、不写进度。新增超时与服务错误2项。`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=PythonPracticeInteractiveRunTest`：9通过/0失败/0错误/0跳过，BUILD SUCCESS；日志 `output/f03-test.log`。未验：真实 Judge0 超时与生产进度。

- F04：生成输出遇NEED_INPUT返回RUNTIME_ERROR与无控制字符的中文原因；前端成功/失败混合时也提示失败原因、不回填失败项。`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=ProgrammingTeacherValidationTest`：7通过/0失败/0错误/0跳过（新增1）；`cd RuoYi-Vue3 && node --test src/utils/__tests__/questionWorkbenchPage.test.js`：14通过/0失败（新增1行为测试）；日志 `output/f04-test.log`。未验：浏览器真实生成输出链路。

- F05：等待输入信号继承BaseException，外层捕获顺序不变；新增真实Python循环/except Exception回归，执行器在5秒内强制失败清理，避免死循环测试卡住。`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=JudgeSourceWrapperTest`：8通过/0失败/0错误/0跳过（新增1），日志 `output/f05-test.log`。边界：裸except仍可吞信号，未改变该语义。

- F06：两处stdin改为每项附带末尾换行，零项仍为空串；增加空回车前端断言与后端回放测试，后端业务代码未改。`cd RuoYi-Vue3 && npm run test:python-runner`：21通过/0失败（新增1项，另一现有测试补空行断言）；`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=JudgeOutputTest`：15通过/0失败/0错误/0跳过（新增1），日志 `output/f06-test.log`。未验：真实旧浏览器直接回车。

- F07：release清空requestSeq，使停止/切题后在途响应作废；watch同时监听lessonId与questionId。新增手控Promise迟到响应、真实Vue watch同题切课行为2项；`cd RuoYi-Vue3 && npm run test:python-runner`：23通过/0失败/0跳过。未验：真实浏览器弱网切题场景。

- F08：新增专用领取退回Mapper（不递增重试），转换投递RejectedExecutionException返回false；afterCommit逐附件兜底RuntimeException。新增队列拒绝与提交后转换失败2项。`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=PracticalAttachmentConversionFailureTest`：5通过/0失败/0错误/0跳过；日志 `output/f08-test.log`。未验：真实线程池饱和/数据库回退。任务书原方案遗漏pending定时选取，按主代理要求补齐：pending按update_time/create_time与failedBefore限时选取，仅OFFICE/PDF/IMAGE、原200条与重试上限保持；retry遇pending直接convertAsync原子领取不加重试数。附件已在提交事务内插入，定时普通查询只能看到已提交行，未改变旧版本附件处理边界。追加Mapper解析/条件、pending直接领取、定时调度3项测试。`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=PracticalAttachmentConversionFailureTest,PracticalArtifactRetryServiceTest`：10通过/0失败/0错误/0跳过（本项合计新增5），日志 `output/f08-retry-test.log`。

- F09：finish遇CANCELLED直接返回；retryFailed拒绝CANCEL_REQUESTED/CANCELLED/PAUSED；迟到updateResult返回0改发RESULT_DISCARDED，不再发COMPLETED。新增取消/请求取消/暂停重试拒绝、finish不覆盖CANCELLED/PAUSED、迟到结果丢弃3组（共4项）。首次迟到用例因临时页图未放/profile/upload白名单失败，修正夹具后`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=PracticalAiJobServiceTest`：11通过/0失败/0错误/0跳过，日志 `output/f09-test.log`。未验：真实模型取消竞态。

- F10：保存后仍停留同一学生时静默刷新带acceptServerSnapshot，直接采纳服务端新answerId/分数；成绩回写和shouldAdvance一律按studentId，避免多个空answerId学生串人。新增最后一名接受快照、空answerId换人保护2项；`cd RuoYi-Vue3 && node --test src/views/business/teacher/__tests__/gradingRefresh.test.js`：14通过/0失败/0跳过。未验：真实教师浏览器流程由主代理后续验收。

- F11：分项明细用真实answerId；页面无systemSubmissionId而best已存在时拒绝；批量采用快照列表非空但缺该生则跳过；两个事务改READ_COMMITTED。新增4项（事务隔离、晚到提交、缺快照跳过、补建答题行明细归属）；`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=TeacherGradingControllerValidationTest`：4通过/0失败/0错误/0跳过，日志`output/f11-test.log`。未验：真实MySQL并发改分与RR快照对比。

- F12：槽位Redis写入移入入队异常保护内；入队被拒/写槽位失败统一服务失败、归还槽位并退分钟额度；控制器仅非SERVICE_ERROR写课堂状态。新增入队拒绝1项（断言SERVICE_ERROR、rate与active class均退还、结果落库）；`mvn ... -Dtest=ProgrammingRunLockContractTest,PythonPracticeMapperScopeTest`：13+1通过/0失败/0错误/0跳过，日志`output/f12-f13-test.log`。**主代理复核补正**：completeServiceFailure 只更新了重新查出的行，未同步提交接口持有的入参对象，控制器仍会把课堂任务标成已提交；已在收尾后同步入参状态并补断言，`ProgrammingRunLockContractTest` 13项通过（日志`output/f12-return-status-test.log`）。未验：真实Redis故障与消息队列饱和。

- F13：两处学情SQL的plan_scope放开legacy；新增Mapper解析契约1项。本机legacy题单=0、biz_extension_class空集，按主代理只读核查确认无真实样本，未做接口人数对比；测试通过同上`output/f12-f13-test.log`（PythonPracticeMapperScopeTest 1项）。

- F14：免抽测教师班级查询加 `NOT EXISTS biz_extension_class` 排除拓展班载体；教师课程指标四个 union 分支统一按常规课过滤并排除载体班级。新增Mapper解析契约1项，补跑既有服务测试：`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=ExemptionApplicationServiceTest,ExemptionApplicationMapperScopeTest`：8通过/0失败/0错误/0跳过，日志 `output/f14-test.log`。本机只读查询 `biz_extension_class` 总数=0，无真实样本，仅靠 SQL 审查与 XML 解析测试，未做真实接口人数对比。

- 主代理浏览器隔离验收（2026-11-11，真实 Vue 组件 + 模拟 API，未连真实库/后端）：
  - F06：空行题运行后直接回车，终端显示“收到空行”，请求日志 `stdin="\n"`、`needInput:false`、106ms。
  - F07：96002 延迟题点击运行后立即切 96003，旧响应 4010ms 才到且 `needInput:true`；切题后页面为 96003、运行按钮可用、无“等待输入/请输入旧数据”，迟到响应未污染。
  - F10：default 场景最后一名提交学生保存 88 分成功、名单同步；single 场景仅学生2已提交，原地保存 91 分后“已判分 1/1”、按钮可用、无“成绩已更新”，模拟服务记录 `answerId:96502/score:91`。
- F01：主代理已完成；`CountyExamServiceImplTest` 21 项通过、0 失败（新增 2 项）。本接续代理未修改 F01 文件；数字由主代理交接提供。
- F02：已加查询 `status=0` 和循环过滤归档课程；新增从课序1跳过归档2到3的测试，同时断言查询状态。`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=LessonAutoAdvanceServiceImplTest`：19 项通过/0失败/0错误/0跳过（新增1项），BUILD SUCCESS；日志 `output/f02-test.log`。未验：真实数据库与整班自动推进。

- F15：协作健康检查加 `@PreAuthorize("@ss.hasAnyRoles('admin,teacher')")`，阻止学生读取内网主机和环境变量名。新增注解契约1项；`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=CountyExamServiceImplTest,CountyExamAndCollaborationAccessTest`：24通过/0失败/0错误/0跳过，日志 `output/f15-f17-test.log`。未验：真实学生账号请求返回403。

- F16：首次进入计时先 `lockStudentAttempt` 再 `ensurePaperForStudent`/`ensureStudentTiming`，后续保持原查询；新增 `shouldLockParticipantBeforeBuildingPaperOnFirstEntry`，用 InOrder 锁定先加行锁再建卷。测试同上 24 项通过（其中 CountyExamServiceImplTest 22 项）。未验：真实双标签页并发首次进入。

- F17：上传归属键 TTL 改为 `UPLOAD_OWNER_TTL_HOURS=24` 小时，覆盖长考试；新增常量契约。测试同上 24 项通过（CountyExamAndCollaborationAccessTest 2 项）。未验：真实长考试最后上传后提交。

- F18：切题和卸载前用旧题目ID补存最后一段代码：新增 `flushDraftNow(lessonId, questionId)`，watch 旧值变化与 onUnmounted 均调用；仅 pending、非空代码、未保存时发请求。新增3项 Vue 行为测试（旧ID补存、无修改/已保存/空代码不发请求、监听与卸载复用同一函数）；`cd RuoYi-Vue3 && node --test src/components/StudentProgrammingQuestion/__tests__/draftFlush.test.js`：3通过/0失败/0跳过。未验：真实浏览器快速切换与断网补存。

- F19：缓存目录不可写退回系统临时文件时，`buildCached` 用成功标记 + finally 清理；`ServiceException` 中途失败也会删除半成品，成功文件仍交给下载流程。新增1项：用普通文件冒充教材根目录走临时文件分支，页图缺失触发失败，比对系统临时目录前后无新增 `textbook-pdf-*`；`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=TextbookPdfServiceTempFileTest`：1通过/0失败/0错误/0跳过，日志 `output/f19-test.log`。未验：真实只读共享目录权限。

- F20：投票 INSERT 与 UPDATE 统一直接引用 `#{reason}`：首次空原因写 NULL，空串明确清空，null 快捷改票保留旧理由，撤销票清空。新增2项：注解 SQL 契约；真实 MySQL 随机克隆表隔离用例，依次验证空串→NULL、有原因→“好”、null→保留“好”、空串→NULL、撤销→NULL，结束 rollback 并 DROP 克隆表。`REVIEW_MYSQL_URL/USER/PASSWORD/DRIVER` 指向本机库运行 `TextbookCaseFeedbackVoteSqlTest`：2通过/0失败/0错误/0跳过，日志 `output/f20-test.log`；`information_schema` 复核 `review_%` 临时表残留 0。主代理另带同一驱动变量合并运行 F19+F20 3项通过（日志 `output/f19-f20-test.log`）。未验：真实教师连续投票交互。

- F21：新增 `resetStaleProcessingResults(jobId, staleMinutes)`，只把 `PROCESSING` 且开始时间超过15分钟或为空的份退回 PENDING；`resume` 在任务改 PENDING 前调用，避免把仍在请求模型的份重复派发。新增2项：resume 调用顺序（先回收、再改任务、再唤醒 worker）与 Mapper SQL 时间条件契约；`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=PracticalAiJobServiceTest,PracticalAiGradingMapperStaleTest`：13通过/0失败/0错误/0跳过，日志 `output/f21-test.log`。未验：真实模型请求中途暂停/继续竞态。

- F22：已确认 `TeachingSupervisionQuery` 有 `activityStartTime/activityEndTime`，Controller 已先规范化时间窗；`selectStudentDetails` 的操作题 JOIN 补 `practical.submit_time >= q.activityStartTime` 与 `< q.activityEndTime`，与班级汇总共用左闭右开窗口。核对时 `git diff` 为空且 HEAD 第676行业务 JOIN 确实缺少时间条件（第580行是班级汇总，不是学生明细），因此按任务书补入。新增1项 Mapper 解析契约；`cd RuoYi-Vue && mvn -pl ruoyi-business -am test -DforkCount=0 -Dsurefire.failIfNoSpecifiedTests=false -Dtest=TeachingSupervisionStudentDetailsWindowTest`：1通过/0失败/0错误/0跳过，日志 `output/f22-test.log`。未验：真实监管页面班级行与学生明细人数对比。

## 收尾证据（主代理，2026-11-11）

- 本机后端全量：`Tests run: 1074, Failures: 0, Errors: 0, Skipped: 3`（日志 `output/full-backend-1.30.96.log`；基线1040，净增34）。
- 前端：构建门禁 login 9、seat-helper 9、question-workbench 26、python-runner 23，全过；另 `gradingRefresh` 14、`draftFlush` 3，全过；`npm run build:prod` 退出0。F18 引入后 F07 的 watch 源码切片测试缺 `flushDraftNow` 沙箱桩，已在测试上下文补空函数后复跑23项通过。
- 制品：JAR `2cb253df9e7f703f429d6994317a36a5111309003715e87e4ab0042311928be5`（108,317,622B）；首页 `1fde9972c70b5a14698d734a9855bf2cfd867693ede974c264b10a34be6ab562`；`frontend.tar.gz` 43,392,889B / SHA-256 `15c1a391f1382c240912f14df1483353358d00510462c87cf74a8c903b682ff2`，825文件。
- 130已切换至 `releases/20261111_bugfix_v1`，MainPID 3162268、NRestarts 0、active/running；产品更新174/1.30.96/PUBLISHED。备份、异常处置、线上验收与回退见 `contexts/RELEASE_LOG.md`。
- 发布脚本在登记产品更新阶段SSH连接被重置，自动回滚未建立连接；现场确认新版本已切换且健康后单独执行登记SQL。此异常不得省略。
