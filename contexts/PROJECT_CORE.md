# 信息科技学业测评平台：当前核心事实

> 版本：v3.21

> 更新：2026-09-03
> 用途：新的 Codex、Claude、Gemini 或人工开发者的默认入口。只记录当前仍有效且已验证的事实；历史发布和排障证据见 `contexts/context.md`。

## 2026-09-03 P2 通用分组、学生桌面与 P3 小组协作（本机迁移及代码验证完成，未发布）

- 新增 `sql/class_grouping_v1.sql`，建立通用班级分组方案、成员、课时分组快照、教师个人座位布局表；不复用物联网分组表。
- 后端新增 `/business/class-group` 接口：方案查询/保存/自动生成/删除、课时快照生成、学生桌面查询和布局保存。服务端按教师管理班级校验范围，保存方案时校验当前班级学生不重不漏，默认学号最小者为视觉组长；组长不改变权限。
- 学生端公共 `StudentLayout` 建立独立 `/ws/presence/{deviceId}` 认证连接，30 秒心跳、Redis 60 秒 TTL，教师桌面聚合多设备在线数和服务端观察到的连接 IP；不写签到考勤、不接受浏览器自报 IP。
- Vue3 班级管理每行增加“学生桌面”入口，桌面支持终端/作业状态视图切换、默认学号网格和教师个人拖动布局；教师协作页可创建小组活动、查看小组房间与操作轨迹，学生编辑器每 30 秒上报心跳并在离开时记录事件。
- 新增独立非计分协作活动、任务版本、小组房间映射、操作轨迹和 revision 差异摘要。每个课时快照小组取得独立文档副本；学生只能进入本人小组房间，首名学生进入后冻结活动。保存触发者仅表示触发保存的会话，不能被当作版本内容的全部作者；协作全程不写 `biz_student_answer`，不改变成绩、课程总分或自动推进。
- 本机迁移：已在 `xueyeceping_server_20260729` 完成 `sql/class_grouping_v1.sql` 与 `sql/group_collaboration_v1.sql`，后检新增分组、活动、任务映射、轨迹和差异表均存在。迁移前备份分别为 `backups/20260903_163318_before_class_grouping`（SHA-256：`8BCBC34D66A4A1ABCAF8E1C8D357D333BE37EC418AEC0AC0B213D66AE5BE2F3A`）和 `backups/20260903_164649_before_group_collaboration`（SHA-256：`9EF7C170335A0A9F56FB018C7D20F24A9C605813D9F5C6569C4B92086D5FCD20`）。正式服务器未迁移、未发布。
- 已验证：`CollaborationRoomServiceAccessTest`、`CollaborationRevisionDiffServiceTest`、`CryptPadDocumentServiceAuditFailureTest`、`WebSocketConfigTest` 共 9 项通过，0 失败、0 错误；`mvn -pl ruoyi-admin -am clean package -DskipTests`（8 模块）和 Vue3 `npm run build:prod`（2912 模块）均通过，`git diff --check` 无错误。未进行浏览器、真实多人、断线恢复、历史房间回归或容量验收。

## 2026-09-03 多功能改造 P0-P1（本地纠偏完成，未发布）

另一代理提交的 P0-P1 实现已完成代码审查并按已确认口径纠偏；本轮没有连接、修改或发布 `10.52.1.123`，也没有在本机或正式库执行新增 SQL。当前事实如下：

1. **P0-A/P0-B 正确性修复**
   - 批改请求携带 `practicalVersionId`，旧作品按提交时评分快照读取；课程题目已有答题后，服务端拒绝改变该题总分。郑琦账号 `696260` 的正式真实数据仍需发布后专项验收，当前不得写成线上问题已解决。
   - 非图片操作题不再把 `practical_image_max_count` 更新为 NULL。题库导入使用整批事务和写前校验，选择题答案还会校验为 A～D 且对应选项存在；未知数据库错误对前端脱敏。文档转换任务改为事务提交后触发，避免回滚批次仍启动外部转换。
   - 已知学生删除保护、班级范围校验和分值保护归为业务提示；正式 NSSM 日志路径治理尚未执行。
2. **P1-A 指派与流程图**
   - 普通新建课程不再默认勾选全年级班级；编辑仍使用保存值。具体班级入口的单班预选仍需在实际入口存在时补验。
   - 开始/结束改为胶囊矩形，处理框为直角矩形，输入/输出保持直角平行四边形；四向锚点可见半径 7、悬浮命中半径 14。旧图数据、鼠标/触摸板及 100%/125% 缩放仍需浏览器手工回归。
   - 题库管理和课程设计器均可只读预览流程图题的学生基础图。新增 `/business/flowchart/question/{questionId}/preview`：管理员、题目创建者或公开题可访问，只返回基础图，不泄露标准答案和结构规则；完整配置接口的编辑权限不变。
3. **P1-B 五星辅助评分**
   - 数字输入保持默认；支持整题 `STAR_TOTAL`、逐项 `STAR_ITEM` 和显式清零。前端发送星数，后端按 `BigDecimal + HALF_UP` 基于满分重算并校验，拒绝非法星数、缺失分项和客户端篡改分值；7 分题五档为 1/3/4/6/7。
   - 评分模式是请求契约，不新增成绩小数字段，也不依赖客户端折算结果。
4. **P1-C 共享课程只读**
   - 非创建教师必须同时满足同校、本人管理班级与课程指派班级有交集，才能读取课程内全部已引用题目；公有/私有不再二次过滤，返回班级仅限管理交集。
   - 共享教师能力固定为只读：无设计、删除、复制、修改入口。先前越界增加的“复制为我的课程”前后端代码已删除；同校但无负责班级关系仍拒绝。
5. **P1-D 统一作业状态（核心链路本地实现）**
   - 新增 `biz_student_task_state`，状态为 `NOT_ENTERED/ENTERED/WORKING/SUBMITTED/GRADED/RETURNED`，唯一键为课程+题目+学生，`state_version` 用于丢弃乱序消息。迁移和回滚脚本为 `sql/student_task_state_v1.sql`、`sql/student_task_state_v1_rollback.sql`；脚本尚未执行。
   - 学生打开/开始作答、流程图与 Python 草稿、正式提交、教师批改/退回均接入统一状态；状态作为展示旁路，在答案、作品或批改主事务成功后以独立事务写入，状态表失败只告警，不能使主业务返回失败。教师批改页接收版本化 `TASK_STATE_UPDATE`，250ms 合并刷新并每 10 秒全量校准，新提交不会切走当前批改学生。教师首页操作题状态每 10 秒校准。
   - WebSocket 推送改为事务 `afterCommit`，课堂房间支持显式 `lessonId`，统一规范化班号并原子清理断开连接；学生只能订阅当前课，教师订阅历史课仍需通过课程班级关系和管理范围校验。学生连接仅允许心跳，不能伪造状态事件。成绩页共享状态 store、学生桌面和 Presence 仍属后续阶段，不能宣称 P1-D 全页面完成。
6. **本地验证边界**
   - 已完成定向回归：`ClassroomTaskStateServiceTest`、`ClassroomWebSocketSecurityTest`、`BizQuestionPracticalImageMaxCountTest`、`DiagnosisGovernanceTest` 共 17 项，0 失败、0 错误；随后执行 `mvn -pl ruoyi-business -am test`，生成的 94 份 Surefire 报告均无失败或错误标记。
   - `mvn -pl ruoyi-admin -am clean package -DskipTests`：8 个 Maven 模块全部 `BUILD SUCCESS`；`npm run build:prod`：2909 个模块转换完成，生产构建成功。仅有既有依赖的弃用/eval/大包提示，无构建错误。
   - 尚未做本机真实数据库迁移、角色页面浏览器冒烟、正式账号/真实课程验证或服务器发布。

## 2026-09-02 多功能改造规划（方案已收口，P0-P1 本地实施中）

总需求、总体设计、分阶段任务、开发提示词和新任务交接提示词位于 `contexts/multi-feature-upgrade-20260902/`。P0-A/P0-B 与 P1-A/P1-B/P1-C 的核心代码已完成本地纠偏；P1-D 核心状态链已实现，但全页面接入与环境验收未完成；P2 与 P3 小组协作已完成本机实现和迁移，P3-PoC、真实课堂与容量验收仍未完成。

- 已完成正式服务器近 24 小时诊断事件的只读归类：题目更新把非空字段 `practical_image_max_count` 写为 NULL 是真实缺陷（P0-A 已修复，见上节）；学生删除保护和课程班级范围校验属于业务拦截误报；题库导入缺少题型前置校验并可能暴露 SQL 细节；学生导入历史慢事件需要核对线上运行包的动态分级。NSSM 当前运行目录与 stdout/stderr 日志目录不一致，后续发布需一并治理。此次探查未写库、未重启服务。
- 郑琦近期使用账号 `696260` 的旧作业无法批改已定位为前端未传 `practicalVersionId`（P0-A 已修复，见上节）：后端和数据库已有评价标准快照，旧提交绑定 v1、新标准为 v2，无需覆盖历史数据；批改页已按提交时快照显示和保存。
- 已确认共享课程第二阶段边界：仍只共享给负责课程指派班级的同校教师；共享教师只读查看课程内全部已引用题目，不区分题目公有/私有；不得查看未加入课程的私人题，也不得复制或编辑原课程。
- 已确认通用分组方向：班级可保存多套方案，课程生成课时快照；教师端学生桌面是机房终端监控网格，主要显示姓名、在线/离线、IP、分组和视觉组长，并分离“调整座位”和“调整分组”。它不承担点名，不写考勤，现有签到考勤课仍是考勤入口。专题入口为 `contexts/class-grouping-and-desktop/`。
- 学生桌面主入口确定放在现有“班级管理”页面每个已管理班级的操作列，教师首页增加携带当前课程/班级的快捷入口；不改造一级菜单结构，不归入教师工具或在线协作。在线由学生登录后的独立认证 WebSocket + Redis TTL 表达，30 秒心跳、60 秒离线；服务端展示可信代理链观察到的“连接 IP”，普通浏览器不能保证取得真实网卡 IP 或计算机名。
- 已确认在线协作升级不计分、不写个人答案，并从操作题解耦为独立课堂协作活动：同一活动可有多个起始文件版本，按课时小组进入不同房间；历史全班房间保留为全班组。操作轨迹包含进出、心跳、保存和相邻版本变化摘要，但当前 Provider 只能确定保存触发者，不能直接证明版本内全部变化的实际作者；最后一项仍需 PoC 门禁。详见 `contexts/online-collaboration/` 与 ADR-006。
- P0-P1 已进入本地代码实施，但新增任务状态 SQL 尚未执行，也未发布；P2/P3 已完成本机代码与迁移，正式网络能否观察到每台学生机独立连接 IP，以及 CryptPad/OnlyOffice 能否提供比“小组版本差异”更精确的作者数据，仍必须通过后续 PoC/机房验收确认。
- 第二轮已确认：数字评分仍为默认，五星为辅助并支持整题/逐项切换；星级结果按题目或评分项满分的五等比例四舍五入为整数，零星显式清零。旧作品使用提交时评价标准快照，重新提交绑定最新快照，已有提交后禁止修改题目总分。学生提交采用认证 WebSocket 推送加周期性全量校准，并同步接入教师首页、成绩/提交列表、批改页和学生桌面；打开题目为“已进入”，首次保存为“作答中”，正式提交为“已提交待批”。决策见 `contexts/operation-artifact-ai-grading/ADR-007-integer-star-rating-and-rubric-snapshot.md`。

## 2026-09-02 画程流程图前端恢复（已正式发布 1.28.6）

- 根因已确认：9 月 2 日公开通知热修将 3010 Nginx root 切换到不含画程资源的前端 release，导致题库流程图入口和新增编辑器消失；正式库 `biz_question` 中流程图题 2479、2028 仍在，`biz_flowchart_question` 配置 2 条，未发生数据删除。
- 已基于当前 Vue3 源码重新构建并发布前端 `releases/20260902_flowchart_frontend_restore_v1/frontend`，仅替换静态资源，后端、数据库结构和题目数据不变；构建包含 `FlowchartEditor` 与题库 FLOWCHART 逻辑。
- 正式探活：3010 HTTP 200；新目录 `index.html` SHA-256 `B9663E6A64F1F1E33963C47CE02F1779EDBBF3B281DFF29CF00B0A7E9D92B888`；前端压缩包 SHA-256 `C203CF7D008F35519AC8C241A31A269E0AAEB890C8790A4CA7669315276CD82F`。切换前 nginx 配置备份保存在 `backups/20260902_flowchart_frontend_restore_v1_before/nginx.conf.before`。
- 平台更新记录 `1.28.6` 已写入正式库并置为 `PUBLISHED`。回滚：恢复上述 nginx 配置并 reload `UnifiedNginx`，切回 `releases/20260902_research_public_notice_share_hotfix_v1/frontend`；无需后端重启或数据库结构回滚。

## 2026-09-02 教研通知公开分享热修（已正式发布 1.28.5）

- 根因已确认：公开通知接口直接返回 `ResearchPublicNoticeVo`，前端错误读取 `.data` 导致通知对象为空；HTTP 正式地址不满足安全上下文，`navigator.clipboard` 不可用导致复制失败。
- 前端热修 release：`releases/20260902_research_public_notice_share_hotfix_v1/frontend`；仅替换 Vue3 静态资源，后端无需重启，数据库无新增 SQL/结构变更。`index.html` SHA-256 `57210430F56E6229205C8C618082BEF4AFD13658B9651CAA5B339284D580044A`。
- 正式发布：3010 Nginx root 已切换，`UnifiedNginx` 为 Running；3009、3010、代理验证码均 HTTP 200。平台更新记录 `1.28.5` 为 `PUBLISHED`（update_id=70）。匿名分享地址实测可显示“测试活动通知”标题、发布信息、活动时间和正文表格。
- 发布前平台更新表备份：`D:/program/3009dazipingtai/backups/20260902_research_public_notice_share_hotfix_v1/biz_platform_update.before.sql`，SHA-256 `E5C0E6D0E7C7C3DA124BE2F10CE0DF0AE4665869C243A626A93C01CE2B82AA1A`，19,664 bytes。
- 回滚：将 3010 Nginx root 恢复到 `releases/20260902_research_public_notice_share_v1/frontend`，使用该备份目录中的 `nginx.conf.before`，重启 `UnifiedNginx`；无需后端或数据库结构回滚，平台更新记录可按版本精确改为草稿。

## 2026-09-02 教研活动通知公开分享（已正式发布 1.28.4）

- 已按随机令牌方案实现：教研员/管理员可为 NOTICE 主题生成 7 天、30 天或永久链接，支持查询状态、撤销和重新生成；数据库只保存 SHA-256 哈希。
- 匿名仅放行 `/business/research-activity/public/notices/{token}` 及正文图片接口；专用 DTO 不含评论、资源、附件、云盘、接收人和内部 ID。图片还需验证原始 URL 出现在正文 HTML 中并通过路径安全校验。
- 正式 release：`releases/20260902_research_public_notice_share_v1`，后端 NSSM `NewDaziBackend3009` 与 3010 Nginx 已切换并运行；jar SHA-256 `37B11F2253DC669CD0F65C1A510AB67CEEA42FB77DED46CCE9C116F1EB1918E8`，前端 `index.html` SHA-256 `FF6974FF077F1DCB72E0C5B7A7CA85A68B6F4EC5100440478C27F1B122FF4D30`。
- 正式库 `ry-vue` 已执行 `sql/research_activity_public_share_v1.sql`，后检启用但无哈希记录为 0、令牌唯一索引已创建；平台更新记录 `1.28.4` 为 `PUBLISHED`（update_id=69）。发布前整库备份：`D:/program/3009dazipingtai/backups/20260902_research_public_share_before/ry-vue_before.sql`，SHA-256 `1D6DA9339B4D420650D425543F8D9F5A77A499BE280B769A5FE7F4AF7BDB22B1`。
- 正式探活：3009、3010、验证码均 HTTP 200；匿名无效令牌返回业务码 404；临时令牌匿名读取通知正文成功（HTTP 200），验收后已清理临时令牌，业务通知保持未启用分享。
- 回滚：后端将 NSSM `AppDirectory/AppParameters` 恢复备份中的旧 release 后显式 stop/start；Nginx 恢复 `backups/20260902_research_public_share_before/nginx.conf.before` 并重启 `UnifiedNginx`；数据库结构可兼容保留，若必须撤销按 SQL 逆向删除新增列/索引（当前不建议）。

## 1. 先读什么

1. `AGENTS.md`：操作边界、验收顺序、远程授权和凭据规则。
2. 本文件：项目当前状态与不可违反的边界。
3. `docs/architecture/INDEX.md`：按任务进入相应架构文档和专题。
4. 验收报告集：`output/acceptance/01_项目现状简报.md` ~ `10_发布决策与上线结论.md`。
5. 仅在任务相关时读 `contexts/<专题>/`；与本文件冲突时，先核对代码、数据库或部署证据。


## 1.5 学生实验工具 + 题目开放开关 + 站点运维信息（2026-08-23 已正式发布 release 20260823_student_tool_v1）

**功能**：
- 学生端顶部新增「学生实验工具」按钮 → 弹出面板：先本节课工具（课程设计器配，随课程走），后常驻工具（教师按 入学年份+班级 或整个年级 配置）。
- 理论题/操作题改为**班级×当前课程**开关：老师上课在成绩查询页开启，学生端才显示可做；课程推进自动复位关闭；打字题不加开关。
- 站点运维：129 扩展监控补齐硬件/JVM/磁盘进度条（SSH 探针，凭据未恢复显示暂缺）；帮助中心新增「平台推荐环境」三卡（Chrome 最新版 / Mind+ V1.8.1 RC3.0 / 掌控板 ESP-32）；平台更新历史补录至 25 条；底部版权去掉地址。
- Agent 词典：任何部署 123 的 AI 轮次必须登记 `contexts/RELEASE_LOG.md` 并写 `biz_platform_update`（AGENTS.md 硬性条款）。

**数据**：新表 `biz_student_tool`/`biz_student_tool_scope`/`biz_lesson_tool`；`biz_lesson_assignment` 增 `theory_open`/`practical_open`。SQL：`sql/student_tool_v1.sql`（幂等）、`sql/platform_update_history_v2.sql`（幂等补 17 条）。
**接口**：`/business/student-tool/*`（教师工具 CRUD + 学生 mine）、`/business/student-tool/lesson/{id}`、`/business/score/lesson-gate`（GET/PUT）、`current-lesson` 新增 `studentTools`/`theoryOpen`/`practicalOpen`/`hasTheory`/`hasPractical`。
**本机验证（2026-08-23）**：`mvn -pl ruoyi-admin -am clean package -DskipTests` 通过；`npm run build:prod` 通过；本机库执行两个 SQL 成功（后检 3 表 + 2 列 + 菜单 2；平台更新 26 条无重复）；接口全链路：学生端工具下发匹配（lesson 252 → 2 本课 + 1 常驻）、教师开/关理论题→学生端联动、越权 403 拦截、测试数据已清零。
**正式发布（2026-08-23，release `20260823_student_tool_v1`）**：正式库已备份（`backups/20260823_student_tool_before/ry-vue_before_student_tool.sql`，92,524,408 bytes，SHA-256 `29A39F8C3E0818B77B56037294D368AAC5DF98B68220F86CDCF75ED3328270B0`）；两个增量 SQL 已执行并后检（3 表 + 2 列 + 菜单；平台更新由 8 条补至 26 条 PUBLISHED 无重复）；jar SHA-256 `d49edb1708b1d5e92e56370ddba8f0bafe2d6df1f21686c2f5fecab5d277a790` 与本机一致；NSSM 已切至新 release 并 RUNNING，Nginx root 已切（注意：改 nginx.conf 必须无 BOM 写入，本轮曾踩 BOM 坑后修复）；3009/3010/captcha 200；正式教师接口（student-tool list、lesson-gate GET、getRouters 含 student-tool 菜单）与学生接口（current-lesson 含 studentTools/theoryOpen 等新字段）均验收通过，学生越权写接口 403。
**129 硬件信息**：正式 yml 已配置 `monitor.host129.ssh-command`（指向 129 的 hwprobe key），但当前 SSH 通道不可达（与 PROJECT_CORE 既定事实一致），页面显示「暂缺」为预期状态；续 SSH 凭据恢复后自动出数据。
**热修（同日）**：「工具管理」404 修复：`src/router/index.js` 未注册 `/student-tool/manage` 路由，已按 teacher-tools/manage 模式补 dynamicRoutes 块（permissions=business:studentTool:manage）。前端已重 build 并替换正式 3010 frontend，正式冒烟确认 manage 页标题/新增工具正常、无 404。后端无改动。
**回滚**：NSSM 切回 `20260822_host_hw_v1`（参数备份 `backups/20260823_student_tool_before/nssm-*-before.reg`、nginx.conf.before）+ 后端重启；正式库若需回滚删除新表/列按 git 中 SQL 逆序执行（rollback 脚本建议后续固化）。
**同日热修（第 15 节）**：`20260823_student_tool_v1` 上线后发现 4 个缺陷（课程保存失败、成绩页点课报错、题目开放开关不显示、学生端刷新），已随 `20260823_student_tool_hotfix_v1`（v1.25.1）修复并发布，详见第 15 节。

## 1.6 学生导入班级号范围修复（2026-08-31）

- 学生新增、Excel 导入及上传前校验统一允许班级号 `01～99`（服务端规范化为 1～99 的数字字符串）；`00`、`100` 及 `601/602` 等带年级的三位数仍拒绝。
- 学生管理下拉框、导入模板提示和帮助中心文案已同步为 `01～99`，可正常录入 11 班及以上班级。
- 本机验证：`StudentImportRulesTest` 覆盖 11、99、00、100、三位年级号边界并通过（4/4）；前端 `npm run build:prod` 已通过（Vite 2905 modules transformed，仅有既有依赖警告）。
- 正式发布（2026-08-31，v1.27.5）：后端与 Vue3 前端已切换到 `releases/20260831_student_class_99_v1`；正式库发布记录为 `PUBLISHED`（update_id=61）。发布前整库备份位于 `D:\program\3009dazipingtai\backups\20260831_student_class_99_v1_before\ry-vue_before.sql`，SHA-256 `175751E3B969097C9FDB4EFFAB705A32433392A01CA37ABE8989778B75638173`。3009、3010、`/prod-api` 和学生管理静态脚本均 HTTP 200，线上脚本已包含 `01～99` 校验和 11 班示例。
- 回滚：后端恢复备份的 NSSM 配置并切回 `releases/20260831_primary_iot_v1/backend` 后重启；Nginx root 切回 `releases/20260831_primary_iot_python_template_v1/frontend` 并 reload；数据库无需回滚（仅新增一条平台更新记录，必要时按版本精确改为草稿）。
- 热修发布（2026-09-01，v1.27.6）：学生管理单个新增的学号正则误把 `\\d` 当作两字符文本，导致 `10～99` 被前端拦截；3010 前端已切换至 `releases/20260901_student_add_validation_hotfix_v4/frontend`，仅替换该静态脚本中的 1 处规则为 `\d`。后端仍为 v1.27.5，无 SQL 结构迁移或后端重启。复核：错误规则 0 处、正确规则 1 处，3009、3010、验证码及静态脚本均 HTTP 200，`UnifiedNginx` 为 Running；正式库平台更新为 PUBLISHED（update_id=62）。备份目录 `D:\program\3009dazipingtai\backups\20260901_student_add_validation_hotfix_v4`，Nginx 配置 SHA-256 `ABD43B14A7D01BF25CF9F96CBFC4C0F0DFCF301FC52B300A71E1DD484D46B5EE`，`biz_platform_update` 备份 SHA-256 `7C20CD9E5C6A90016347D98749DDE160D88569A21AD5E22F19F9B527925AE544`。回滚：将 Nginx 3010 root 切回 `releases/20260831_student_class_99_v1/frontend` 后重启 `UnifiedNginx`；无需回滚结构 SQL。

## 2. 系统与目录

| 层 | 当前事实 |
| --- | --- |
| 后端 | `RuoYi-Vue/`，Java 8、Spring Boot、RuoYi 3.9 Maven 多模块；`ruoyi-admin` 启动，`ruoyi-business` 承载业务。 |
| 前端 | 仅维护 `RuoYi-Vue3/`，Vue 3、Vite、Element Plus。`RuoYi-Vue/ruoyi-ui/` 是旧 Vue2，除非用户点名不得修改。 |
| 数据 | MySQL 为主，Redis 用于缓存、限流和分布式锁；MyBatis Mapper XML 是数据访问的重要事实来源。 |
| 迁移 | 新 SQL 放根目录 `sql/`，先备份、前检、执行、后检；不得把本机迁移成功等同于正式库已迁移。 |
| 文件 | `uploadPath*` 为业务文件，`backups/`、`output/`、`tmp/` 不能被当作业务源码或索引默认输入。 |

## 3. 角色与业务边界

- 平台角色：管理员、教师、学生、教研员。服务端权限校验是最终边界，前端隐藏不构成权限控制。
- 课程、答题、批改、学情是主业务域。新功能应复用既有用户、学校、班级、课程与权限模型，不新造账号或成绩体系。
- Python 刷题是独立业务域：不写 `biz_student_answer`，不改变课程成绩、课程推进或既有课程 Python 判题。
- 在线协作由平台持有课程、班级、房间、文件和访问权限；CryptPad 只提供受限编辑会话。
- 物联网是课程实验辅助，不重做 Mind+，不做考试和自动评分；浏览器不接触 Broker 管理凭据。

## 4. 当前外部集成状态

| 集成 | 已确认事实 | 当前风险 / 下步 |
| --- | --- | --- |
| Judge0 | Python 判题由后端通过私有 HTTP 调用（10.52.1.129:2358，携带 X-Judge0-Token 认证）；浏览器无 Judge0 地址或令牌。2026-08-23 已扩为“400 份同时提交进入平台队列、10 路真实判题”，50/100/200/400 四档均 100% 接收并全部 Accepted。 | 单机不承诺 400 个沙箱同时运行；独立刷题执行器队列仍是进程内队列，后端重启期间的在途恢复未包含在本轮容量结论。 |
| CryptPad | 已采用 CryptPad Integration API，部署在 10.52.1.129:80；已完成单房间 50 人同编与 400 人×8 房间并发会话测试（100% 成功，平均时延 85.9ms）。2026-08-21 已修复 OnlyOffice 参与人身份显示（inner.js 服务器补丁：优先取成员表名字而非本地用户），编辑器新增在线成员列表与版本历史；「版本已变化」频繁提示由 15 秒自动保存携带旧版本号引起，已随本次发布修复。 | 当前内网 HTTP/WS 临时验收态已通过；HTTPS/WSS 不在本轮范围。 |
| EMQX / MQTT | 方案 A：EMQX 5.8.8 已正式启用（10.52.1.129:1883 设备接入；管理 API 18083 已开放给 10.52.1.123 后端）。平台接收器以 `platform_iot_subscriber` 订阅 `county/#`；ACL 文件规则已生效（订阅账号只读订阅、`class_*` 设备收发、deny all）。课程级物联开关（`biz_lesson.iot_enabled`）已上线：仅教师在设计器开启的课程，教师首页课程卡片与学生首页才显示物联入口。2026-08-23 修复「教师端收不到数据」：根因为 `biz_iot_message.device_id` 外键+哨兵值 0+空设备表致每条消息外键冲突、paho 回调异常引发断连重连循环；已删外键（本机+正式库）并加接收异常隔离与断线自动重连重订阅（第 16 节）。 | 真实机房多班级并发实验持续实测中（冒烟发布时已有真实课堂设备在线）；EMQX 管理凭据与口令密钥均由 NSSM 环境变量注入。 |
| WebSocket | 导学单、课堂和 IoT 都使用服务端 WebSocket；连接路径和鉴权见 `docs/architecture/INTEGRATIONS.md`。 | 改实时功能必须验证鉴权、断线和跨班隔离。 |
| LibreOffice | 用于 Office/PDF 转换和操作题预览，存在独立进程池、队列、健康巡检和并发参数。 | 生产参数与机器资源耦合，改配置必须按专项验收。 |

## 5. 环境与发布

- 本地：后端 `8080`，Vue3 Vite 默认 `80` 并代理后端。
- 正式平台：内网主机 `10.52.1.123`，后端 `3009`、Vue3/Nginx `3010`；后端为 `releases/20260901_scheme2_score_numeric_v1/backend`，3010 前端为 `releases/20260902_student_entry_year_grade_v1/frontend`（v1.28.3，第 36 节）。
- 扩展服务：`10.52.1.129` 承载 Judge0、CryptPad、EMQX 等独立服务。
- 2026-08-21 发布前综合验收：经全链路 4 角色权限、数据一致性、防重幂等、400 活跃并发阶梯压测（13,653 请求，100% 成功，0 丢单）、Python 判题与在线协作 8 房间并发测试，已全项通过并准予发布上线。

## 6. 2026-08-21 发布前综合验收证据与结论

1. **备份与环境基准**：执行前在 10.52.1.123 生成了完整备份 `D:\program\3009dazipingtai\backups\20260821_acceptance_pre_fixture_9f29fff\ry-vue_pre_fixture.sql`（80,943,056 bytes，SHA-256 `239569625C3F863AC74352704419574B1B8B8056B971B1E03E1B5E667F9DF786`）。
2. **四角色与权限隔离**：管理员（8376 用户/85 部门）、教师（494 题库）、教研员（活动列表）、学生（当前课程）全量验证通过；8 项越权探针拦截率 100%（返回 403）。
3. **数据一致性与幂等性**：10 并发重复交卷严格幂等（8 题落盘 8 行）；跨学生画像与成绩越权 100% 拦截；匿名访问 401 拦截；无数据库敏感凭据泄露。
4. **阶梯并发压测（400 活跃并发）**：20 档（P95 135ms）、50 档（P95 260ms）、100 档（P95 693ms）、200 档（P95 1681ms）、400 档（P95 3478ms），全流程 13,653 笔请求成功率 100.00%，400 名学生 3200 笔答案落盘零丢单。
5. **Python 判题与在线协作**：Judge0 (129:2358) AC/WA/CE/TLE 判定 100% 正确，16 任务排队 2.98s 消化；CryptPad (129:80) 400 学生在 8 个协作房间并行建立会话 100% 成功（0.68s 完成，平均延迟 85.9ms）。
6. **数据清理与残留扫描**：所有 `ACC26` 夹具已执行软归档与关系清理，全库 8 表活跃数据扫描残留全部为 0。
7. **最终结论**：签署 **【准予发布 · ALLOW TO SHIP】**。

## 7. 重大变更收口


以下任一变更完成前，必须同步更新本文件和受影响的架构/专题资料：业务流程、权限、DTO/API、数据库迁移、外部协议、部署配置、跨模块行为、集中热修。

- 更新 `docs/architecture/` 中对应说明和 Mermaid 图。
- 更新相关专题 `requirements.md`、`design.md`、`tasks.md`，重大决策新增 ADR。
- 写明已验证证据、SQL、配置/重启要求、回滚方式、未完成门禁。
- 小型文案或单点样式修改不要求升级核心上下文，除非改变业务含义。

## 8. 2026-08-21 课程级物联开关与协作体验修复发布（已上线）

**改动内容**：物联入口从全局显示改为课程级开关（教师课程设计器「开启物联网」，教师首页课程卡片与学生首页按 `biz_lesson.iot_enabled` 显示入口，学生页面带 `lessonId` 进入）；教师物联页新增「学生数据收集」卡（小组统计 + 消息分页 + 类型/关键词过滤，接口 `GET /business/iot/experiments/{id}/messages`）；协作编辑器新增在线成员列表、版本状态 tag 与版本历史接口 `GET /business/collaboration/room/{roomId}/revisions`；修复自动保存版本 CAS 失败重试与 OnlyOffice 参与人身份显示。

**SQL 与配置**：`sql/iot_course_switch_v1.sql`（幂等加列 + 按已有实验回填，后检一致），回滚 `sql/iot_course_switch_v1_rollback.sql`。正式后端 NSSM 环境变量新增 `IOT_MQTT_ENABLED=true`、`IOT_MQTT_USERNAME/PASSWORD`（platform_iot_subscriber）、`IOT_PASSCODE_SECRET`（与旧默认值一致，兼容已有密文）、`IOT_EMQX_API_KEY/SECRET`；外置 application.yml `iot.mqtt.enabled=true`、broker `tcp://10.52.1.129:1883`、订阅 `county/#`。重启后端生效。

**EMQX（10.52.1.129）**：管理 API 18083 由仅本机改为开放给内网（容器以相同镜像/挂载重建，旧容器改名 `school-emqx-poc-bak-20260821` 留作回滚）；`acl.conf` 替换为订阅账号只读订阅 + `class_*` 设备收发 + deny all（备份 `acl.conf.bak-20260821`）；新增 `platform_iot_subscriber` 账号与 `dazi-backend` API 密钥（见 secrets.local.md）。

**CryptPad（10.52.1.129）**：`/srv/cryptpad/patches/inner.js`（SHA-256 `72CD97D17E6646D5BA0704F39B58EF6E22261A260623481A2F12FB2218CB94B3`）以只读挂载覆盖容器内 onlyoffice inner.js，修复参与人用户名解析优先级（成员表名字 > 本地 integrationConfig）；compose 备份 `docker-compose.yml.bak-20260821`，容器重建后 healthy，公网入口已验证返回补丁内容。

**验证证据**：备份 `D:\program\3009dazipingtai\backups\20260821_185607_before_iot_course_switch_9f29fff\ry-vue_full.sql`（140,735,436 bytes，SHA-256 `88F1BA8F24CC9ED3FB9A5C7FF2F4C8B05E229286365021691A51323D12B44664`）；SQL 后检：3 门课开启、实验课未开启为 0；新 release jar/yml SHA-256 与本地一致、前端 616 文件一致；3009 HTTP 200 且日志「物联网 MQTT 接收器已连接 broker=tcp://10.52.1.129:1883 subscription=county/#」；3010 线上 index.html SHA-256 与新版一致；生产 API：学生 2020710101 current-lesson `iotEnabled=true`、教师课程详情 252 `iotEnabled=True`、实验 1 消息接口 200（共 0 条，待设备上报）。

**回滚方式**：切回 NSSM `AppParameters` 指向上一个 release（`20260820_iot_scheme_a_v1`）+ 移除新增环境变量并重启；数据库执行 `sql/iot_course_switch_v1_rollback.sql`；EMQX 容器 rename 回退或恢复 `acl.conf.bak-20260821`；CryptPad 恢复 `docker-compose.yml.bak-20260821` 后 `docker compose up -d`。

**剩余风险与下一步**：教师收集页目前「共 0 条」，需真实机房设备经 Mind+ 上报后验证小组统计与消息分页；EMQX 管理面板已对内网开放 18083，密码见 secrets.local.md；协作多人编辑的保存版本递增仍待真实同班账号复测。

## 9. 2026-08-21 全区上线前实战排雷与可用性加固（已发布上线 release `20260821_resilience_v1`）

**背景**：数十所学校、上千名学生同时上机前的全真排雷。6 路并行代码侦察结论与分级隐患清单见本轮汇报；主链路（签到/发题/答题/交卷/推进）整体设计稳健，未发现阻塞 Tomcat 请求线程的同步重活。

**已落地补丁**：
1. 打字题重复提交保留历史最高分：`BizStudentAnswer` 新增瞬态 `keepBestScore`，`BizStudentAnswerMapper.xml` upsert 对 score/typing_speed/accuracy_rate/completion_rate 按 `IF(#{keepBestScore} = TRUE, GREATEST(...), VALUES(...))` 处理，仅打字题置位；选择题/判断题/操作题覆盖语义不变。
2. 判题队列扩容可配置：`AsyncConfig.judge0Executor` 队列 120→`ruoyi.judge.queue-capacity:500`（环境变量可调），降低整班集中交编程题被拒判为「判题服务异常」的概率。
3. `Judge0HttpClient` RestTemplate 改双检锁单例复用，消除每提交新建连接的开销。
4. 黑匣子第一步——MDC 日志上下文：新增 `ruoyi-framework/.../DiagnosticContextInterceptor`（traceId/userId/deptId，afterCompletion 清理），注册于 `ResourcesConfig`；logback pattern 加日期与前缀 `[trace:%X{traceId}] u=%X{userId} dept=%X{deptId}`；GlobalExceptionHandler 业务异常日志补请求地址。注意 @Async 判题线程不继承 MDC，判题链路结构化日志为下一步。
5. 双机一体化监控看板（129 扩展机）：新增后端聚合接口 `GET /monitor/extension/health`（`ExtensionHealthController`，admin+researcher，Judge0/CryptPad/EMQX 并行探针各 2s 超时、失败降级 down+脱敏 error + MQTT 接收器内存状态）；前端 `api/monitor/extension.js` + `views/monitor/extension/index.vue`（10s 自动刷新四卡看板）；菜单 SQL `sql/extension_monitor_menu_v1.sql`（幂等，menu_id 25011）。
6. 学生端防误关：区抽测页考试中与学生首页有未交答案/打字进行中时注册 beforeunload 二次确认。

**验证证据（2026-08-21 本机）**：`mvn -pl ruoyi-admin -am compile/package -DskipTests` 通过；本地 8080 后端以新 jar 重启（PID 34928）；admin 登录后 `GET /monitor/extension/health` 返回 200（22ms，judge0/emqx=unconfigured、cryptpad=degraded、mqttReceiver=down，本机未配 129 环境变量属预期降级）；`sql/extension_monitor_menu_v1.sql` 已在本机库执行复核（menu 25011 + admin/researcher 各 1 条授权），`/getRouters` 已下发 `extension -> monitor/extension/index`；三个改动 Vue 文件经 Vite dev 编译 200 无错。

**发布要求**：正式发布需重新 package 前端 build:prod；`sql/extension_monitor_menu_v1.sql` 在正式库执行；打字题保最高分为行为变更需教师知晓；重启后端生效。回滚：git revert 本轮补丁 + 删 menu 25011。

**构建修复（已解决）**：`npm run build:prod` 曾失败于 index.html 内联 `<style>` 的 vite html-inline-proxy 缺陷；已将加载动画样式外置到 `public/loader.css` 并规范化 IE 条件注释，build:prod 通过并随本版发布。

## 10. 2026-08-21 发布记录：release `20260821_resilience_v1`（已上线）

**发布内容**：第 9 节全部补丁 + 前端构建修复（index.html 加载动画样式外置到 `public/loader.css`，规避 Vite 6.3 对 index.html 内联 style 的 html-inline-proxy 构建缺陷；IE 条件注释同步规范化）。

**发布前备份**：正式库 ry-vue 全量 `D:\program\3009dazipingtai\backups\20260821_222000_before_resilience_v1\ry_vue_full.sql`（91,816,061 bytes，SHA-256 `56127A5C3DBC2A8915F9E0018D3FECAF5CA8E46300CC8D09614EF56189AAF65B`）。

**SQL**：`sql/extension_monitor_menu_v1.sql` 已在正式库执行并后检（menu_id 25011「扩展服务监控」，admin(1)/researcher(102) 各授权 1 条；注意必须经 mysql `source` 直读 UTF-8 文件执行，PowerShell 管道会把中文转成问号）。

**制品与切换**：jar SHA-256 `CB20741B...A22C51C5`（与本地构建一致，107,570,501 bytes）；前端 621 文件、index.html SHA 与本地一致（含 loader.css）；NSSM AppDirectory/AppParameters 切至新 release（日志路径 AppStdout/AppStderr 同步迁至新 release logs）；Nginx 3010 root 切至新 frontend。**教训：改 nginx.conf 必须用无 BOM 写入（UTF8 BOM 会致 "unknown directive" 且 reload 失败），路径用正斜杠。**

**EMQX 看板修复**：外置 application.yml 原缺 `emqx-api-key/secret` 映射，环境变量 `IOT_EMQX_API_KEY/SECRET` 因连字符属性无法宽松绑定导致探针与平台 EMQX 账号同步均降级；已在 yml 补 `${IOT_EMQX_API_KEY:}`/`${IOT_EMQX_API_SECRET:}` 显式映射（备份 `.bak-emqx-mapping`）并重启生效。

**验证证据（生产）**：3009 HTTP 200；admin 登录后 `/monitor/extension/health` 四探针全绿——judge0 up(v1.13.1)、cryptpad up、emqx up(nodes=1, conn=1)、mqttReceiver up，整体 64ms；getRouters 下发 `extension -> monitor/extension/index`；3010 index.html SHA `85223e25b6c080d495aef7cf783b3d3e20572d77` 与本地构建一致。打字题保最高分已在本地库三向实测（慢→快保最高、快→慢保最高、非打字题正常覆盖，测试行已清理）。

**资产瘦身（123）**：releases 68→4（保留当前/上一版/上上版 + `20260817_142550_cryptpad` 仅剩 2 个被运行中服务占用的日志文件 8MB，下次重启后可删）、backups 101→3（本次备份 + 两份验收基准），D 盘余量 561→570GB。根目录散落的 ruoyi-admin4~8.jar、hs_err 转储、dist4/6/8 尚未清理（未在授权范围）。

**剩余风险与下一步**：①10.52.1.129 SSH 密码认证失败（secrets 中 admin2 密码疑似已轮换），129 磁盘盘点脚本备好待凭据恢复后执行；②本机 npm build:prod 修复方式为样式外置，后续升级 Vite 可尝试还原内联写法；③黑匣子第二步（biz_error_event 表 + 判题链路日志 + 前端错误上报）待下一版。

## 11. 2026-08-22 监控体系攻坚 + 安全热修发布（已上线）

**后端（jar SHA-256 `97E6062D...8E14057`，原地更新于 release `20260821_resilience_v1`）**：
1. `SystemDiagnosisController.summary` 全面并行化：12 个探针（OSHI/Redis/Druid/线程池/日志/任务/转换/在线数/perf事件）独立 CompletableFuture 并发采集，每个 2 秒超时预算独立降级；`sys_oper_log` 慢查询治理——slowOperations/topInterfaces 改为主键倒查限窗（2000/10000 行上限），日志表超百万行自动切换 `sys_perf_event` 聚合。
2. NaN 序列化炸接口根治：`Cpu.java` 全部 getter 加 `safePercent` 兜底（OSHI 首采 0/NaN 时 `Arith.mul` 解析 “NaN” 抛 NumberFormatException 是诊断接口偶发 500 的根因）；`ApplicationConfig` Jackson 开启 `QUOTE_NON_NUMERIC_NUMBERS`；summary 返回前递归清洗非有限数值。
3. `ExtensionHealthController` 新增 `systemInfo` 节点：CPU load1/5/15、内存 used/total/usagePercent（解析 EMQX v5.8 的“31.34G”可读串）、判题管道队列、磁盘/GPU 明确标记不可用（需 129 侧探针）；EMQX 探针修正真实字段名 load1/load5/load15。
4. 热修合入：resetStudentPwd 越权防护（dept_id 归属 + biz_student 学籍校验，本地三场景实测通过）、打字题整体择优覆盖（本地库三场景实测）、SchoolScoreController 调试输出清理、ScoreQueryController.setAbsent 与 PythonPractice 两控制器参数防御性校验。

**前端（index.html SHA-256 `42F75744...68239F8`）**：扩展服务监控页新增「129 主机资源仪表盘」（CPU 负载/内存进度条/磁盘 GPU 占位/判题排队）与「双机对比 · 123 主站 vs 129 扩展机」表格；诊断中心 loadData 增加失败容错（保留上次数据+提示）。注：骨架屏/v-else 结构改造经实测在部分环境渲染异常，已回退，仅保留脚本层容错。

**部署与验证证据**：分块上传 SHA 校验一致后合并；重启后生产实测——extension health 四探针全 up（judge0 v1.13.1/cryptpad/emqx nodes=1 conn=1/mqtt），systemInfo 实测 cpu load 0.0/0.01/0.0、内存 2.24G/31.34G=7.1%、延迟 61ms；诊断中心连续 5 次全 200（首调 2.25s、后续约 1.2s，无 NaN、无 500），浏览器实测两页面完整渲染。回滚：backups\20260822_before_monitor_upgrade\ruoyi-admin.jar.bak（cb20741b）+ frontend.bak-20260822。

**剩余风险**：①129 SSH 凭据仍未恢复，磁盘/GPU 指标需部署 node_exporter 或恢复凭据后接入；②EMQX 管理 API 的 IOT_EMQX_API_KEY 映射依赖外置 yml 显式占位符（已修），升级配置时勿删；③nginx.conf 编辑必须无 BOM 写入（UTF-8 BOM 会导致 reload 静默失败的教训已两次踩坑）。

## 12. 2026-08-22 Python 刷题 OJ 化与统一题单（已正式发布）

**产品与边界**：统一题库继续使用 `practical/PYTHON`，不增加“适用场景”字段；课程或独立刷题用途由关联关系决定。独立刷题只有一种“练习题单”，题单版本关联一个或多个教师可管理班级，全年级只是班级全选；旧“年级基础题单/班级加练包”退出界面和查询。发布版本不可变，修改时复制成新草稿；显式删除则完整物理清理题单及练习历史，不显示“已删除题单”且不可恢复。专题决策见 `contexts/python-judge0/adr/ADR-008-unified-python-question-bank.md`、`ADR-009-unified-practice-plan-class-version.md` 与 `ADR-010-hard-delete-and-natural-class-label.md`。

**已上线功能**：教师端提供目标班级选择、课程设计器式左右双栏选题、知识点/难度筛选、批量加入、预览、排序、发布、物理删除和统一学情；教师建 Python 题仍为三步极简流程和双 Sheet 导入，不接在线 AI。学生端按题单分组，使用左题面、右上代码、右下控制台的三窗格，支持公开样例、自定义输入、正式提交、逐测试点矩阵和历史；隐藏点保持脱敏，独立刷题不写 `biz_student_answer`。

**数据库与题库**：正式库在完整备份后执行 `sql/python_oj_modernization_v1.sql`、`sql/python_practice_unified_plan_v2.sql`、`sql/python_system_questions_v2.sql` 和 `sql/python_practice_polish_v3.sql`。正式系统题为 `PYV2-001`～`PYV2-120`，共 120 个唯一题号、720 点；旧 V1 80 题及课程/刷题依赖、历史提交和快照已物理删除，V1 后检为 0。当前全部 121 道 Python 题（120 道系统题 + 1 道教师题）的年级/学期/课次均为空，创建人均为“郑东旭”；V2 系统来源改由编程配置的 `python-system-v2` 标记识别。

**Judge0 中文修复与验证**：Judge0 请求/响应文本统一使用 UTF-8 Base64，解码兼容返回文本末尾换行，解决中文输出被拒绝或误判。生产真实 Judge0 全量验证为 120/120 题、720/720 点通过，报告 `output/python-oj/python-v2-production-judge0-validation.json`，SHA-256 `022AE144A061F9B416DC9B2CD9FC76C0A4B1C84339BF59D9D03C4667F7C7A872`。后端全量测试 353/353、fat JAR clean package、Vue3 生产构建均通过。

**发布与回滚**：本轮正式整库备份为 `D:\program\3009dazipingtai\backups\20260822_181500_before_python_practice_polish_v3_3c6721a\ry-vue_full.sql`，81,964,606 bytes，SHA-256 `6372018D34886E57FF73EC6668621CF28E4C5177DF84B17C58D2B386464A7D31`。当前 release 为 `20260822_181500_python_practice_polish_v3`，JAR SHA-256 `65A3005950C4687AA453326D8D29AF14E03E8981629055438D0C758C764991D9`，前端 index SHA-256 `7BD3F46F5BCBBB99D7ADF477F090324BBE9A9C54489E9521F6CE0A88EBC1D1E3`；NSSM Running，3009/3010 HTTP 200。应用可切回后端 `20260822_164900_python_oj_unified_v2_r5` 与前端 `20260822_164500_python_oj_unified_v2_r3`；被删除的 V1 和题单历史只能恢复整库备份，恢复会覆盖备份后的新数据，必须在维护窗口单独评估。

**生产冒烟与剩余风险**：教师正式账号在初中校区完成 API 与浏览器冒烟：班级接口返回 `801班`～`808班`、`901班`～`908班`，页面题单显示 `803班` 等自然班名，不再出现“2024届”；题单页无“已删除题单”；Python 题库共 121 题，课程元数据均为空、创建人均为郑东旭。页面 page error 0、服务端 500 为 0，截图与报告位于 `output/playwright/python-practice-polish-v3-production-*`。后端最终 355 个业务测试和 3 个 admin 测试、fat JAR clean package、Vue3 生产构建均通过。当前仍未执行真实整班同时提交压测。

**2026-08-22 隐藏测试点编辑热修（已上线）**：教师题目编辑接口 `GET /business/programming/question/{id}` 改为要求编辑权限并返回题目所有者/管理员可见的完整测试点；题单和课程设计器预览改走 `GET /business/programming/question/{id}/preview`，只返回公开样例，继续保护隐藏数据。保存和双 Sheet 导入统一要求至少 1 个公开样例、1 个隐藏测试点，所有期望输出非空、权重为正且合计恰好 100；教师测试点步骤显示公开数、隐藏数和总权重，并可平均分配到 100。正式环境只读抽查题目 1882：编辑 6 点（2 公开 + 4 隐藏）、预览 2 点、总权重 100，页面已进入“代码与验证”，page error/服务端 500 均为 0。当前 release `20260822_203251_python_hidden_cases_edit_fix_v1`，JAR SHA-256 `B9E7D6AF125F201B6DA3CA68D304254CF989C07903023A7B0EAA1F72DFF2B5DB`，前端 index SHA-256 `70059C64433EF75BCC0C055107BB7B46CA902CA63741C82970808E790D509DDB`；后端 357 个业务测试 + 3 个 admin 测试、clean package 和 Vue3 生产构建均通过。无 SQL/配置变更；应用回滚切回 `20260822_181500_python_practice_polish_v3` 即可，数据库无需回滚。

## 13. 2026-08-22 独立审计与全量缺陷修复（已随最新制品发布，专项生产回归待补）

**审计结论**：非 Python 全量上线审计判定【有条件上线】。主链路四角色真实账号实测通过，无越权通道；报告见 `output/audit/20260822_non_python_full_rollout_audit.md`。

**已修复（本地运行时验证通过）**：
1. **诊断中心/服务监控 CPU 恒 0%（P1）**：`Cpu.java` getter 分母误写死 0，改为按 total tick 差值换算真实百分比；实测负载态 used=74.75%、空闲态 5.54%，和≈100%。同时新增 `cpu.model`（OSHI ProcessorIdentifier）。
2. **探针超时降级假象（P2）**：`SystemDiagnosisController` 探针预算 2s→4s，响应新增 `serverDegraded` 标记；前端超时显示告警横幅而非全零。
3. **畸形 JSON 500→400（P2/D-2）**：GlobalExceptionHandler 新增 HttpMessageNotReadableException →「请求参数格式错误」code 400；MethodArgumentNotValid fieldError 判空（D-3）。
4. **setAbsent 并发双插（P2/A-3）**：新增 `upsertAbsent` 原子 upsert（依赖 uk_student_lesson），标记缺考清零表现分、取消缺考保留原分。
5. **手工改分幂等（P2/A-2）**：save 前查最新 ADJUST 流水，已有生效修正则拒绝（先取消再改）。
6. **课堂 WS 传输层兜底（A-4）**：ServletServerContainerFactoryBean 文本/二进制缓冲上限 64KB。
7. **P3**：lesson/details 不存在返回「课程不存在」；resetPwd 返回「成功重置 X 个，跳过 Y 个」明细；扩展监控双机对比表 CPU 行量纲改名（使用率% / 负载数值）。

**新增功能**：诊断中心新增「主机硬件信息」（CPU 型号/核数/总内存/服务器名/IP/OS/Node.js 版本懒探测缓存）、「磁盘状态」表（盘符/文件系统/容量/进度条）、「Java 虚拟机信息」面板（版本/启动时间/运行时长/路径/运行参数）。数据源为既有 OSHI server 节点，129 硬件仍受 SSH 凭据限制（页面已明示）。

**验证证据（2026-08-22 本机）**：`mvn -pl ruoyi-admin -am compile/package -DskipTests` 通过；本地 8080 实测——CPU 负载态 74.75%、空闲态 5.54%，畸形 JSON 返回 400、details/-1 返回课程不存在、resetPwd 明细话术、nodeVersion=v22.19.0、upsertAbsent 并发 3 发仅落 1 行且取消正常；诊断中心浏览器实测硬件三面板（主机硬件信息/磁盘状态/Java 虚拟机信息）渲染正常。本机库 menu 25011 乱码已修正（生产库本就正常）。

**高中/跨学段专项验证（2026-08-22，AUDIT26 夹具已清理归零）**：
1. 数据模型完好：7 个高中部部门 school_type=3 正确标注（滨海学校高中部/象山中学等）；后端 `AcademicYearUtils` 支持平台年级 10-12，打字基准高中 40 字/分。
2. **正式库此前无任何高中学生/任教数据**——高中场景在真实使用前从未被覆盖（夹具测试补上了这块）。
3. 高中教师（dept 176 夹具）实测：登录→dashboard-data 三年级组「高一(10)/高二(11)/高三(12)」与入学年 2026/2025/2024 全部正确；成绩查询班级列表带 deptName；越权校验正确拦截他校课程改分。
4. 跨学段机制：教师多校区来自 `biz_teacher_class` 多 dept_id 行（如郑东旭 139 小学部+169 初中部），登录时 `needsSchoolSelection` 触发校区选择对话框；现有跨学段教师均为小学↔初中组合，暂无小学↔高中或初中↔高中组合的真实教师。
5. 未发现高中场景 Bug。注意点：新造账号 pwd_update_date 为空会被强制跳转个人中心改密，属预期安全行为；`biz_lesson_assignment` 有 uk(dept_id,entry_year,class_code)「每班一节当前课」唯一键，删除课程时必须同步清理指派行，否则该班无法指派新课（本次夹具测试踩到并确认）。
6. 学生端高中年级显示未做浏览器级验收（夹具已清理）；如需可后续补。

**修复期间事故记录**：本机 8080 曾被并行 AI 启动的进程占用旧 jar，为打包短暂停止后以新 jar 重启（新 jar 同时包含双方改动）；诊断页编辑过程中曾引入 riskList 丢失与 data 重复声明，均已修复并浏览器回归通过。


**最新制品（2026-08-22 晚）**：jar 与 dist 已重新构建（含 upsertAbsent、高中夹具回归及 Python 隐藏测试点编辑热修）；当前已发布制品哈希见第 12 节。
**发布状态**：这些本地改动已包含在 `20260822_203251_python_hidden_cases_edit_fix_v1` 的最终 clean package 和前端生产构建中并完成 release 切换；Python 隐藏测试点编辑专项生产冒烟通过。该节所列诊断、改分和高中场景仍应补做各自的生产专项回归，不能仅以本次 Python 页面冒烟替代。回滚：切回上一 release jar + 前端目录。

**剩余风险**：①129 SSH 凭据未恢复，129 磁盘/GPU/负载仍不可见；②A-2 幂等采用「拒绝重复」而非唯一约束，极端并发窗口仍在；③教研资源全区共享语义仍未文档化（C-2）。

## 14. 2026-08-23 129 硬件探针上线（release `20260822_host_hw_v1`，已发布）

**功能**：扩展服务监控页新增「129 硬件信息」面板——CPU 型号/路数/线程、内存、磁盘三分区表（进度条）、OS/IP/主机名、CryptPad Node 与 Judge0 Java 版本。数据链路：129 宿主机 `/home/admin2/hwprobe.sh`（只读采集，输出单行 JSON）← 123 后端经**专用受限密钥 SSH** 按需拉取（密钥 `command=` 强制只能执行该脚本，无端口转发/PTY），60 秒成功缓存 / 15 秒失败缓存。

**服务器侧变更（回滚点）**：
- 123：`C:\ProgramData\ssh\hwprobe_id_ed25519` + `hwprobe_known_hosts`（ACL=SYSTEM/Administrators，owner=Administrators；旧位置 D:\...\secrets\ 下副本可删）；外置 application.yml 追加 `monitor.host129.ssh-command`（备份 `.bak-pre-hwhw`）；NSSM AppParameters/AppDirectory/AppStdout/AppStderr 切至新 release；Nginx 3010 root 切至新 frontend（conf 备份 `.bak-pre-hwhw`）。
- 129：`/home/admin2/hwprobe.sh` + `~/.ssh/authorized_keys` 追加一行受限公钥（删除该行即撤销）。

**发布前备份**：`backups\20260822_pre_host_hw_v1\ry_vue_full.sql`（81,894,397 bytes，SHA-256 `9FA5643F2DD223CAEDFFB816F63C3B9EE5E4CF52F6F9956EE7B641B634992D70`）。jar SHA-256 `D49EDB17...277A790`（SFTP 上传前后哈希校验一致）；前端 index.html SHA-256 `70059C64...` 与本地构建一致。

**生产验证（2026-08-23）**：extension/health 200，hostHardware.available=true（Xeon Gold 5218N/16 线程/31.3GB/3 分区/CryptPad Node v24.19.0/Judge0 Java 13.0.1）；诊断中心 123 CPU 真实分布（free 99.03%+sys 0.97%+used≈0=100%，非恒零）；3010 浏览器实测硬件面板渲染完整。

**排障教训（重要）**：①Windows OpenSSH 私钥 ACL 必须仅含 SYSTEM/Administrators 组且 owner 为组而非单独 Administrator 账户，否则服务身份报 "UNPROTECTED PRIVATE KEY FILE"；②服务环境 PATH 不含 OpenSSH 时须用绝对路径；③ProcessBuilder 必须「先读管道到 EOF 再 waitFor」（配看门狗强杀），waitFor 在前会因管道缓冲死锁超时；④joinQuietly 的 3 秒预算会掐死慢探针，慢探针需独立更长预算；⑤scp 大文件经此网络不稳（lost connection/内容陈旧假象），改用 SFTP+上传后哈希校验最可靠；⑥Git Bash 下 `ssh-keygen -N '""'` 会把字面引号设为口令，必须用 `-N ''`。

**剩余风险**：①129 探针依赖 admin2 账户与 authorized_keys 单行，账户密码轮换不影响密钥认证，但删行即失效；②GPU 字段诚实标注"无 GPU"（129 无独立显卡）；③hwprobe 输出未做字段级 schema 校验，129 端脚本被篡改可能注入异常 JSON（已由 objectMapper 宽松转换兜底为 available=false）。

## 15. 2026-08-23 上线后热修发布（release `20260823_student_tool_hotfix_v1`，v1.25.1）

**背景**：第 1.5 节 `20260823_student_tool_v1` 发布后发现 4 个线上缺陷，当日完成定位、修复、本地验证与正式发布。

**根因与修复**：
1. **课程设计器保存失败**（"数据处理失败，请稍后重试"）：`BizLessonMapper.xml` 的 `insertBizLesson` 列清单含 `iot_enabled` 动态列，但 values 清单缺对应 `#{iotEnabled}` 动态项，列值不对称导致 SQL 异常。已在 values 段补齐，恢复列值对称。
2. **成绩查询页点课程报错**（"该课程未指派给当前班级"/"不是当前课程"，历史课程也报）：`ScoreQueryController.getLessonGate` 与 `GuideSheetAccessService` 对历史课程/无指派记录直接抛业务异常。改为优雅降级：校归属校验后按 `findAssignmentByLesson` 取指派，历史课程返回 `isCurrent=false` + 历史开关状态不报错；导学单上下文返回 `enabled=false`；`setLessonGate` 保持严格校验（仅当前课程可开关，历史课程提示"请回到当时的课堂上开启"）。
3. **教师成绩页看不到"题目开放"开关**：前端 `score/index.vue` 的 `loadGateContext` 未兼容 RuoYi AjaxResult 扁平结构，改为 `gateContext.value = res?.data || res || null`；卡片渲染条件明确为"单班级 + 单课程 + 当前课程"（`!isGradeMode && selectedLessonIds.length===1 && gateContext.isCurrent`）。
4. **学生端题目开放联动**：`student/index.vue` 改为静默轮询——每 60 秒 `fetchData({silent:true})`，仅页面可见时执行，且打字进行中不刷新，不打断学生输入。

**备份**：正式整库 `D:\program\3009dazipingtai\backups\20260823_131008_75a99dde\ry-vue_full_before_hotfix.sql`（82,150,110 bytes，SHA-256 `30A7FE37C0AE9871572529640DEE1451405AB3A583B5F768BB3BCC43A57DB415`）。

**制品与切换**：新 release 目录（旧版保留未覆盖），config 沿用旧 release 外置 yml；jar SHA-256 `B1E3E5E80503FF7A2B9AB9AE658713AAD9DA592F583FBEC61598077BAF70CC3E`、前端 index.html SHA-256 `C72FAFBFBCA850778C059301378745EAB354F62613869B618628E0C79D4CB23D`，均与本机构建一致（服务器 certutil 复核）；NSSM 指向新 release 后 Running（服务级环境变量不变）；Nginx 3010 root 切至新 frontend（conf 无 BOM UTF-8 写入、`nginx -t` 通过，改前备份 `nginx.20260823_student_tool_hotfix_v1.candidate.conf`）；3009/3010 探活 200。

**验证证据**：
1. 生产 Bug1 实证：admin POST `lesson/save-all` 新建课程 → 200（lessonId=280、iotEnabled=true，此前该操作必现 SQL 列值不匹配），库内确认 `iot_enabled=1` + 1 题目行，随后 DELETE 清理、残留 0。
2. 生产 Bug2/3 接口：历史课程 `lesson-gate` GET 返回 `isCurrent=false` 不报错；`guide-sheet-context` 200 + `enabled=false`；当前课程 PUT 开关正常、历史课程 PUT 被拒并返回正确话术。
3. 生产浏览器冒烟（admin）：登录/帮助中心/扩展监控/平台更新/成绩页控制台错误 0；帮助中心推荐环境三卡、扩展监控 129 硬件卡与 4 服务绿、版权精简均保留（截图 `output/playwright/hotfix_0*.png`）。
4. 本地 UI 验证：教师成绩页选"单班级 2020-8 + 当前课程 236"后"🧭 题目开放"卡片渲染出理论/操作双开关（`output/playwright/local_gate_06_card.png`）；学生端工具入口齐全，开关未开启时显示"本课理论测试题暂未开放"，控制台错误 0（`output/playwright/local_student_home.png`）。

**发布登记**：`contexts/RELEASE_LOG.md` 已登记 1.25.1；平台更新记录 `biz_platform_update` update_id=48（1.25.1，PUBLISHED）。

**回滚**：NSSM 的 AppDirectory/AppParameters 切回 `releases\20260823_student_tool_v1` 并重启；nginx conf 恢复备份后 reload。本轮数据库仅新增 1 行平台更新记录，无结构变更，无需 SQL 回滚。

**剩余风险与下一步**：①生产侧因学校隔离且无可用的生产教师/学生登录凭据，Bug3/Bug4 的 UI 直观验收以"本地 UI + 生产 API"组合证据替代，建议后续用真实教师账号抽查；②`score/summary` 缺学段参数时报"学段类型必须是 1、2 或 3"，为既有入参校验、非本次回归；③工作区包含学生工具发布与本次热修的全部改动（基线提交 `75a99dd`），提交与推送待用户确认后执行。

## 16. 2026-08-23 物联网数据链路修复与教师/学生端物联页重构（release `20260823_iot_frontend_v1`，v1.25.2，已发布）

**根因（教师端收不到数据）**：`biz_iot_message.device_id` 指向 `biz_iot_device` 的外键仍然存在，而小组级消息落库时 device_id 用哨兵值 0、设备表为空，导致每条上报都触发 `ERROR 1452` 外键冲突；异常冒到 paho `messageArrived` 回调线程 → broker 断连 → automaticReconnect 以同 clientId 重连触发 EMQX takeover，陷入断连重连死循环，QoS0 消息全部丢失。

**后端修复**：
1. 删除 `biz_iot_message.device_id` 外键约束（本机库与正式库均已直接执行，无增量 SQL 文件，回滚需重建外键）。
2. `IotMqttReceiver` 防御热修：`receive()` 包异常隔离壳（单条消息失败只记 `MESSAGE_PROCESS_FAILED` 诊断事件，不影响回调线程）；回调改 `MqttCallbackExtended`，断线记录 cause 并在重连成功后自动重订阅 `county/#`（`BROKER_RECONNECTED` 事件）。
3. 新增学生端历史数据接口 `GET /business/iot/student/messages`（@studentSs.isStudent，复用学生概览全链路校验后按本人小组分页查消息，返回 groupId/groupName/rows/total）。
4. 小组统计查询（教师端两个 stats SQL）增加「最新数据」标量子查询列 lastPayload/lastPayloadType；学生概览 VO 增加 `latestReceivedAt`。

**前端重构（Vue3）**：
1. 教师端 `views/business/iot/index.vue` 整体重写：移除分层诊断条、诊断事件时间轴、最近消息流等冗余区块（诊断事件仅后台保留），改为「小组数据总览」大表格（小组/组员/Topic/在线/收到条数/最新数据/最近接收时间/查看数据），点击小组进入详情视图（最新数据横幅 + 格式/关键词筛选 + 时间/来源/格式/数据内容明细表 + 分页），WebSocket `iot_refresh` 实时刷新。
2. 学生端 `views/student/iot.vue` 重写：新增「本组历史数据」卡片（接收时间/格式/数据内容，分页，20 秒静默轮询）；最新数据改浅色渐变横幅样式；所有复制按钮改用新 `utils/clipboard.js`（优先 navigator.clipboard，HTTP 非安全上下文自动回退 execCommand 方案），修复内网环境「无法复制」问题。

**发布与验证**：发布前正式整库备份（见发布流程记录）；新 release `20260823_iot_frontend_v1`，jar SHA-256 `3CA41A1D67A5D092E7F87A80EF278E3248B93EA49699E7536FACE4B0C2CF5EC6`（107,627,798 bytes）、前端 zip SHA-256 `2933056C07FD07A9D3F78A55F3B06D131F9AC1AE9AB862433419F9C4FBFEB89F`，服务器 certutil 复核一致；NSSM 切换新 release（保留 judge0 并发 -D 参数），nginx 3010 root 切换（无 BOM 写入，备份 `nginx.conf.bak-20260823iot`）；3009/3010 探活 200，启动日志确认 MQTT 接收器连接 `tcp://10.52.1.129:1883` 订阅 `county/#`。后端 358/358 业务测试通过。

**生产冒烟（2026-08-23 17:00）**：用实验 4（光照采集，dept 169）班级账号向 `county/169/279/2025-06/guangzhao/group01/data` 发布 TEXT/NUMBER/JSON 各 1 条测试消息，全部正确落库（类型识别正确、哨兵 device_id=0 不再报错），随后精确清理 3 条测试记录、消息总数回到基线 110；EMQX 侧确认平台订阅客户端 `dazi-platform-iot` 在线且已有真实课堂设备接入；3010 新前端关键 chunk（教师端含「小组数据总览」、学生端含「本组历史数据」）均可访问。

**已知遗留**：①2026-08-21 之前创建的班级配置（实验 1/2/3 的 `class_139_2020_07`、`class_169_2025_01`、`class_169_2024_01`）曾未同步到 EMQX 内置认证库致设备连接被拒，已于发布当日经管理 API 补注册完成（内置库现有 7 账号，`class_139_2020_07` 复测连接 rc=0）；②教师/学生页新布局的 UI 直观验收建议由真实账号复测确认；③回滚：NSSM/nginx 切回 `20260823_student_tool_hotfix_v1` 即可，外键删除无结构回滚必要（重建外键需先清零哨兵值）。

## 17. 2026-08-23 Python 判题 400 份同时提交扩容（v1.26.0，已上线）

**容量口径**：采用“400 份提交同时进入平台、10 路真实判题”的排队方案，不在共享的 129 单机上同时启动 400 个沙箱。123 的 `judge0Executor` 从硬编码核心 2 / 最大 4 改为可配置；生产为核心 10、最大 10、队列 1000，课程班级并发门限为 60。129 Judge0 为 `COUNT=10`、`MAX_QUEUE_SIZE=512`，server 限额 2 CPU / 2GiB，worker 限额 10 CPU / 16GiB。

**根因与代码**：旧线程池虽然 `maxPoolSize=4`，但 Java `ThreadPoolExecutor` 会先把任务放入容量 500 的队列，只有队列满后才扩到最大线程，因此正常高峰实际长期只有 2 个判题线程。`AsyncConfig` 现从 `ruoyi.judge.core-pool-size`、`max-pool-size`、`queue-capacity` 读取并校验正数与大小关系；`application.yml` 提供对应 `JUDGE0_EXECUTOR_*` 环境变量。当前活动后端仍为 `20260823_iot_frontend_v1`，其 JAR SHA-256 `3CA41A1D67A5D092E7F87A80EF278E3248B93EA49699E7536FACE4B0C2CF5EC6`，已核验包含本次线程池实现与生产启动参数。

**验证证据**：业务模块测试在禁用 Surefire fork 后 357/357 通过，fat JAR clean package 通过；正式环境先以 20 条直连 Judge0 请求验证 20/20 Accepted，再通过平台学生提交接口执行 50/100/200/400 四档 `CUSTOM_RUN`。四档接收率均为 100%，终态均为全量 `ACCEPTED`；400 档接口 P95 约 1012ms，接口接收完成后约 22.2s 排空。压力采样时 worker 约 739% CPU、1.236GiB 内存，CryptPad `/checkup/` 与 EMQX `/status` 同时保持 200。报告：`output/stress/judge0-400-acceptance-20260823_171057.json`。临时题单、751 条提交及逐点结果已按 `ACCJ400` 标记精确清理，正式库夹具残留为 0。

**备份、发布登记与回滚**：123 数据库备份 `D:\program\3009dazipingtai\backups\20260823_judge0_concurrency_400_v1\ry-vue-before-judge400.sql`（82,873,577 bytes，SHA-256 `621218F9F9EF9C5B4A2DA6CCA147D72B5BA27B3FCD3A5E572ED930E8C948CC9E`）；129 原配置保存在 `/srv/judge0-python/backups/config-20260823-judge400-v1/`。平台更新 `1.26.0` 已发布（update_id=50）。应用回滚为把 123 的四个判题参数恢复旧值并按 stop→等待 stopped→start 重启；129 回滚为恢复上述 compose/conf 备份后只重建 Judge0 server/workers，不动 PostgreSQL、Redis、CryptPad 或 EMQX。

**仍需明确的边界**：本轮 400 档使用一个受控学生账号发出 400 个同时请求，验证的是 HTTP、落库、Java 异步队列、Judge0 Redis/worker 与结果回写容量，不等同于 400 个真实账号的课堂交互演练；每份 `CUSTOM_RUN` 只有 1 个真实用例。独立刷题执行器队列是进程内队列，后端在排空期间重启的恢复能力未纳入本轮结论。

## 18. 2026-08-24 Python / 课程开放 / 协作 / 物联体验收口（release `20260824_python_iot_ux_v1`，v1.26.1，已上线）

**本轮业务结论**：课程允许配置多道 Python 操作题，只要全课程题目分值合计仍为 100；学生课程首页继续复用既有 Python 编辑器，不限制为一道题。课程设计器新增“学生开放”初始设置，但它只作用于本次新指派的班级，保存既有课程时必须保留各班 `theory_open/practical_open` 的课堂状态。教师首次开启在线协作时，在首次保存前完成文件作品题候选选择，避免“先保存、重进、再开启”。

**代码与交互**：县级教研员部门没有 `school_type` 时，Python 刷题班级名降级为原始“入学年份级+班号”，接口不再抛错；学生 OJ 增加上一题/下一题、切题前草稿保存、等待/排队/判题中状态，并统一服务端驼峰响应到页面字段，修复结果区长期为空。教师物联入口先选班级；总览增加“只看有数据”和数量排序，实时状态按两分钟窗口显示，详情改弹窗，格式化 TEXT/NUMBER/JSON 数据并移除恒值来源列。帮助中心、扩展监控、空态和离线文案同步精简；“若依官网”菜单已隐藏并清理角色授权。

**本机验证**：`npm run build:prod` 通过；后端专项测试在 Windows 禁用 Surefire fork 后 19/19 通过；`mvn -pl ruoyi-admin -am clean package -DskipTests` 通过；教师 dashboard、Python 题单/班级、IoT 班级/实验接口均为业务码 200。构建仅保留既有 vform `eval` 与大 chunk 警告。

**正式发布证据**：发布前整库备份 `D:\program\3009dazipingtai\backups\20260824_python_iot_ux_v1_before\ry-vue_before.sql`，83,622,741 bytes，SHA-256 `ED62FE364F6535DF7954B595F076CA0354CB0EB671BC70771EC7174672B12E49`。已执行 `sql/hide_ruoyi_official_menu_v1.sql` 和 `sql/platform_update_python_iot_ux_v1.sql`，后检为菜单 `visible=1`、角色授权 0、平台更新 1.26.1 唯一且 PUBLISHED（update_id=51）。JAR SHA-256 `F430EDE244195714236855E54F09F2CD6C933E394A6009030D45DF2F1AB79D09`，前端 ZIP SHA-256 `D4FBA7F74ACC5ADF1268FF5630B513407E489E52AD880B964B0148F9041B4A52`；NSSM 与 Nginx 已切到新 release，3009/3010 均为 HTTP 200。

**生产验收边界**：教师首页、Python 教师题单/班级、IoT 班级/实验、成绩班级接口均为业务码 200；县级教研员 Python 题单和班级接口为业务码 200（当前数据为空但不再报错）；可用学生账号登录与 OJ 总览为 200，当前无刷题题目，因此未在正式库造题验证一次提交。3010 首页已引用本机构建的 `/static/js/index-CEY4D_W4.js`，资源请求 200。首次协作保存、真实有题学生的 OJ UI、物联详情弹窗仍建议在下一次真实课堂数据出现时做直观抽查，不能把本轮只读接口冒烟扩大表述为完整课堂验收。

**回滚**：NSSM AppDirectory/AppParameters 与 Nginx root 切回 `releases/20260823_iot_frontend_v1` 后重启/reload；数据库从上述备份恢复，或单独恢复菜单 4 的可见性与角色授权、删除 1.26.1 更新记录。旧 release 和备份均已保留。

## 19. 2026-08-24 课程设计器与课程 Python 输入体验优化（release `20260824_course_designer_python_input_v1`，v1.26.2，已上线）

**业务与交互结论**：课程设计器采用 11/13 双栏并在窄屏自动上下排列；电子导学单压缩为单行配置，解释移入问号提示；本课工具默认收起、按需配置；学生开放双开关改为紧凑行。已选题合并“题型/作答方式”列并把操作列固定在右侧，不再要求教师横向滚动。在线协作位于物联网下一行；没有文件作品题，或文件作品题没有 Word/Excel/PPT `STARTER` 起始文件时，前端明确提示并保持关闭，多候选才弹窗选择。

**课程 Python 自定义运行**：`no_input=0` 的课程 Python 题显示 stdin 输入框和“自定义运行”。新增 `POST /business/student-home/programming/custom-run`，`CUSTOM_RUN` 只执行学生当前代码和输入，不比较标准答案、不访问隐藏测试点、不计分、不写或覆盖 `biz_student_answer`。输入上限 64KB；自定义输入保存在 `biz_programming_submission.custom_input`，逐点结果使用保留测试点 ID 0 并对本人展示实际输出。决策见 `contexts/python-judge0/adr/ADR-012-course-custom-run-non-scoring.md`。

**构建与本地验证**：`ProgrammingSubmissionServiceTest` 在 Windows 禁用 Surefire fork 后 12/12 通过；fat JAR clean package 与 Vue3 `build:prod` 通过。Playwright 本地验收教师紧凑布局、导学单高度、协作顺序、固定操作列和学生 stdin/自定义运行请求均通过，截图为 `output/playwright/course-designer-compact-local.png`、`student-python-custom-input-local.png`。

**正式发布证据**：执行 `sql/course_python_custom_run_v1.sql` 与 `sql/platform_update_course_designer_python_input_v1.sql` 前完成整库备份 `D:\program\3009dazipingtai\backups\20260824_course_designer_python_input_v1_before\ry-vue_before.sql`，83,677,318 bytes，SHA-256 `5DFFEECC916B409D7051172B06D9AA96F2D60EDCE3CD321D10C7C70121F85FC9`。后检为 `custom_input` 1 列、1.26.2 PUBLISHED 记录 1 条（update_id=52）。JAR SHA-256 `2977015640C4DDD0ACC2FFD8089664021E5F4BEC68A8125E0CA4713B509F7099`，前端 index SHA-256 `FEEDC7E658FF882466C3860A503406D715BD9285165ACFCFD98194ECCC1224C9`；NSSM AppDirectory/AppParameters、Nginx root 均已指向新 release，3009/3010/代理为 HTTP 200。

**生产验收**：学生 `2025720103` 的当前课程 279、题目 1883（`noInput=0`）真实自定义运行输入 `123 456`，提交 76 返回 `COMPLETED`、实际输出 `579`、score=null；运行前后课程答案的行数/分数/内容摘要一致，证明未覆盖课程答案。生产 Playwright 验证教师课程 279 的 6 道题、固定操作列、紧凑导学单、协作位置，以及学生 stdin 输入区/自定义运行按钮均通过；截图为 `output/playwright/course-designer-compact-production.png`、`student-python-custom-input-production.png`。

**发布教训与回滚**：首次切换只修改 NSSM `AppDirectory`，但 `AppParameters` 仍显式指向旧 JAR，探活 200 却新接口 404；已改为同时精确替换 JAR、外置 config 与 Nginx release 路径并重启，随后真实接口通过。回滚应用时必须同时把 NSSM AppDirectory/AppParameters 和 Nginx root 切回 `20260824_python_iot_ux_v1`；新增可空字段可兼容保留，无需结构回滚，平台更新记录可按版本精确删除。

## 20. 2026-08-26 操作题旧题答案隔离与 AI 用量/费用可见（release `20260826_operation_ai_usage_archive_v1`，v1.26.3，已上线）

**业务规则**：课程批改、成绩、学情、截止状态和失败预览恢复等所有课程答案统计，只承认仍存在于当前 `biz_lesson_question` 的题目。课程保存移除题目关联时，服务端必须先把对应 `biz_student_answer` 完整复制到 `biz_student_answer_orphan_archive`，写入 `biz_student_answer_orphan_archive_meta` 批次元数据并核对行数，再删除在线答案；任何归档核对失败都回滚课程保存，禁止静默丢失审计证据。

**AI 费用口径**：新增 `biz_ai_model_price` 维护模型输入/输出单价（元/千 token）、价格状态和说明。教师可读取参考价与估算，只有管理员可修改。新建 `biz_practical_ai_job` 时冻结当时单价/状态/说明；任务详情汇总结果表已记录的输入、输出和总 token，并按冻结单价计算理论费用。旧任务没有价格快照时使用当前参考价并明确标注；所有页面固定提示“估算值，实际以阿里云账单为准”。`qwen3.7-plus` 暂按 Qwen-VL-Max 档、`qwen3.6-flash` 暂按 Qwen-VL-Plus 档初始化为 `TO_CONFIRM`，管理员确认前不得把它们表述为官方精确账单价。余额查询仍不在本轮范围。

**异常话术**：百炼 `Arrearage` 映射为余额不足提醒；`InvalidApiKey` 前缀映射为 Key 无效提醒；限流/配额类错误映射为请求过快或配额受限提醒。未知错误只保留 HTTP 状态和截断后的脱敏供应商消息，不回显 Key、提示词、模型原文或堆栈。

**代码与迁移**：未来删题归档由 `StudentAnswerArchiveService` 与 `StudentAnswerArchiveMapper` 在课程保存事务内完成；现有统计 SQL 已统一补当前课程题目关联。迁移为 `sql/lesson_question_answer_archive_v1.sql`（定向归档课程 279 的题目 1882/1883）与 `sql/operation_ai_usage_pricing_v1.sql`（价格表、5 条初始参考价、任务价格快照列），均为幂等脚本；平台更新记录由 `sql/platform_update_operation_ai_usage_archive_v1.sql` 写入。

**本机验证证据**：本机库执行前完整备份为 `backups/20260825_230145_local_before_ai_usage_answer_archive_1247099/xueyeceping_server_20260729_before.sql`，82,284,428 bytes，SHA-256 `AD43A34AED5CBAFD7FB4AB9391A6A5E675643B9B3A4906C7CA080A2FE2DC5DAC`。两份 SQL 连续执行两次均通过；价格行 5、任务价格列 4，归档表与在线答案表字段一致。本机基线早于课程 279，故定向旧答案前检/归档均为 0，不能替代正式库核对。后端专项 16/16、业务全量 371/371、admin clean package（业务 371 + admin 3）和 Vue3 `build:prod` 均通过。浏览器在课程 236 临时插入一条不属于当前课程题目关系的已评分答案后，8 班 `practicalSubmitted` 保持 40，测试行已精确清理为 0；教师价格读取、教师修改价格业务码 403、费用设置弹窗和任务用量抽屉布局均通过，报告与截图在 `output/playwright/20260825-ai-grading-smoke/`。任务用量抽屉使用受控接口桩验证显示，不代表真实模型调用或账单核对。

**正式发布证据**：发布前只读前检确认服务运行、无活动 AI 任务；课程 657/`lesson_id=279` 当前操作题为 2003，已有 119 条在线提交，已删除题目 1882/1883 遗留 7 条答案（5 名学生）。整库备份为 `D:\program\3009dazipingtai\backups\20260826_operation_ai_usage_archive_v1_before\ry-vue_before.sql`，86,253,892 bytes，SHA-256 `CDB7C3910E0F9647BEE88F50242E20610EC5EC6DA721602D0142F7164668D17B`，同目录保留 NSSM 与 Nginx 配置。迁移后旧题在线答案 0、归档答案 7、元数据 7，当前题答案仍为 119；价格行 5、任务价格列 4，平台 1.26.3 更新唯一且 `PUBLISHED`（update_id=53）。运行 JAR SHA-256 `7338F37318D27EBFE80A3887FAF6AA500896282DDF92AE66EB9E6654B0A126A5`，前端 ZIP SHA-256 `1F23F90A58AE466C2D5A625058B88AE79D640FAB3A696FD7F9ADB6A1C34DC3A4`；NSSM、Nginx 已切到新 release，3009、3010 与代理均为 HTTP 200。

**生产浏览器验收与剩余风险**：教师正式账号完成 13 项验收：课程 657 的 1 班接口为 `practicalSubmitted=0/practicalUngraded=0`，下拉框显示“暂无提交”；教师可读 5 个模型价格、修改被业务码 403 拒绝；历史任务 5 的 41 份结果展示真实汇总 `205,240 token`、理论费用 `¥0.7244` 和账单免责声明；页面 JavaScript 错误与非预期 HTTP 错误均为 0，证据位于 `output/playwright/20260826-operation-ai-production/`。本轮没有向模型发送学生作品，也没有修改正式成绩。应用回滚需同时把 NSSM AppDirectory/AppParameters 和 Nginx root 切回 `20260824_course_designer_python_input_v1`；新增兼容表/可空列及已归档旧题答案可保留。若必须数据级回滚，恢复上述整库备份并同步切回旧应用。剩余门禁是管理员按百炼实际计费项确认 `qwen3.7-plus`、`qwen3.6-flash` 的价格映射，以及下一次经授权真实新任务对价格快照和账单差异的核对。

## 21. 2026-08-27 共享课程权限提示与学生端导航吸顶（release `20260827_162145_1247099_shared_course_sticky_v1`，v1.26.4，已上线）

**业务边界**：教师首页继续展示“本人创建课程 + 因负责已指派班级而可见的共享课程”。共享课程只保留批改、成绩等授课协作入口，不允许非创建教师设计或删除；共享课程只展示当前教师实际负责班级与课程指派班级的交集。课程删除在检查答案或导学单历史前先校验课程管理权，避免把越权误报成“已有作答不能删除”。

**接口与前端**：`LessonInfoVo` 新增 `creatorName`、`canDesign`、`canDelete`、`deleteBlockReason`，教师首页直接消费服务端能力，不再由浏览器猜权限。共享卡片显示“共享课程”和创建教师，不渲染设计/删除入口；本人课程有历史数据时保留禁用删除按钮及原因。学生布局解除 `AppMain` 的 `overflow:hidden` 滚动祖先限制，使学生首页、导学单等页面原有 `position: sticky` 顶栏真正吸顶，不使用 `fixed`，因此不新增内容占位或遮挡。

**验证证据**：后端 `BizLessonServiceImplTest` 与 `AnswerDeletionGuardServiceTest` 共 22/22 通过；`ruoyi-admin` 8 模块 clean package 成功；Vue3 `npm run build:prod` 成功。本地 Playwright 验收共享卡片无设计/删除入口，学生页滚动 1100px 后 64px 顶栏仍位于 y=0，实验工具弹窗可正常打开，页面脚本错误为 0。

**正式发布证据**：发布前确认正式环境运行 v1.26.3、无活动 AI 批改任务。整库备份为 `D:\program\3009dazipingtai\backups\20260827_162145_1247099_shared_course_sticky_v1_before\ry-vue_before.sql`，89,104,446 bytes，SHA-256 `75C4F8F6D744DFB85F2EC2DFB019EB310AE8BAB054946D098C5EE9A3B508D28E`，同目录保存 NSSM 和 Nginx 配置。JAR SHA-256 `71E7E85D67611DBA4A5CBDA9101AF64ABB0FFFBCD4E6394FB7F9D80E31C25B36`，前端 ZIP SHA-256 `99DD7D73589A2D5313FB284643374289682B64F623BF21F0451968C7FA7CDD42`，上传前后哈希一致；线上 index SHA-256 `254698CD779AC91BBA8176214B0A62E476809C47364FAE4B87DCB0AD22517885`。NSSM 为 Running，3009、3010 和反向代理均为 HTTP 200；平台更新 1.26.4 唯一且 `PUBLISHED`（update_id=54）。无业务表迁移、无新依赖，课程 259 的 282 条作答在发布前后保持不变。

**正式浏览器验收**：账号 689071 的真实任教范围为 2024级 10班、11班；课程 259 显示“共享课程”和创建教师，只展示这两个负责班级，API 返回 `canDesign=false/canDelete=false`，页面设计按钮 0、删除按钮 0、成绩入口可用。学生账号 2025720103 页面滚动 2721px 后 64px 顶栏仍位于 y=0，实验工具弹窗正常；两端页面错误、控制台错误和 HTTP 500 均为 0。证据位于 `output/playwright/20260827_shared_course_sticky/production/`。

**回滚**：应用回滚时将 NSSM AppDirectory/AppParameters 和 Nginx root 切回 `20260826_operation_ai_usage_archive_v1` 并重启/reload；本轮没有业务数据迁移，无需恢复整库。若回滚应用，应把 1.26.4 平台更新记录改为草稿或按版本精确删除。专题说明见 `contexts/teacher-course-sharing/`。

## 22. 2026-08-29 课程设计器学生开放默认开启（release `20260829_course_designer_default_open_v1`，v1.26.5，已上线）

**业务边界**：课程设计器保留原有“学生开放”理论题、操作题双开关，三个初始化/重置入口均改为默认开启。教师仍可在保存前关闭；后端继续只把初始值应用于新增班级指派，已有班级当前状态不被课程编辑覆盖。成绩查询、课程推进和数据库结构均未修改。

**构建与正式发布**：Vue3 `npm run build:prod` 通过，仅有既有 vform `eval` 与大 chunk 警告。发布前正式库备份为 `D:\program\3009dazipingtai\backups\20260829_course_designer_default_open_v1_before\ry-vue_before.sql`，89,712,914 bytes，SHA-256 `67FF06E647D31953297F3AF5964DF20A6741E90D6A871DD2890E13459D860A99`；Nginx 配置同目录备份。前端 ZIP 本地/服务器 SHA-256 均为 `8C04FF8E09AB9DB0A5C47F4917C5C73DF3F8282B3D581A12DE02AD799CD5029D`，线上 index SHA-256 `32C40F9917ABBE1AC612FDC9636170AB60B87C817F0C07D7F1BCD35BA88AEE1C`。3010 与 `/prod-api` 均为 HTTP 200；后端未重启。

**数据与验收**：未执行业务表迁移，也未批量修改 131 条存量指派；发布前后开启计数保持理论 1、操作 9。平台更新 `1.26.5` 唯一且 `PUBLISHED`（update_id=55）。正式 Playwright 打开课程 279 设计器，确认“学生开放”双开关均为 `true`，页面脚本错误、控制台错误和 HTTP 500 均为 0；未保存课程，截图见 `output/playwright/20260829_course_designer_default_open/student-open-default-on-production.png`。

**回滚**：把 Nginx root 恢复为 `releases/20260827_162145_1247099_shared_course_sticky_v1/frontend` 并 reload；后端和业务数据无需回滚。平台更新记录可按版本 `1.26.5` 精确删除或改为草稿。

## 23. 2026-08-31 小学信息科技实验板标准 MQTT 兼容验证（已通过）

**已验证事实**：真实小学实验板在教师机临时 Broker 上成功使用固件自带 `umqtt.simple.MQTTClient`，可显式传入自定义 ClientID、用户名、密码和完整 Topic，并成功发布 JSON；无需依赖省平台 `mqtt.config(..., projectId, userId)` 固定封装。由此确认小学实验板与现有 EMQX 标准 MQTT 协议兼容。

**129准备状态**：正式 EMQX 新增临时认证账号 `primary_board_probe`，文件 ACL 仅允许其发布已映射的小学测试小组 Topic `county/139/252/2020-07/iot_demo_exp/group01/data`。管理 API 回读通过，账号连接返回 CONNACK 0；`dazi-platform-iot` 仍在线，原平台订阅、班级账号和 device01 规则均完整保留。EMQX、123后端、数据库均未重启，平台代码未修改。

**备份与回滚**：129 SSH 通过 123 跳板完成；ACL 备份位于 `/srv/emqx-school-poc/backups/20260830_050321_before_primary_board_probe/acl.conf`，SHA-256 `6e7df6236dcb759e08831542b40f6124eb3ccb7207bea0644293944454ab37f3`。回滚为删除临时认证账号并从文件授权源移除该单 Topic 规则；不影响 Judge0、CryptPad 或原有 MQTT 账号。

**真实板验收证据**：实验板于 `2026-08-31 09:19:19` 使用上述账号和 Topic 成功发布 `{"source":"primary-board","value":123}`。正式库 `biz_iot_message` 新增消息 8966（实验 1、小组 45、JSON），`biz_iot_event` 新增事件 9025（`MESSAGE_RECEIVED` / 平台接收 / 消息已存档）。至此“真实小学实验板 → 129 EMQX → 123平台 MQTT 接收器 → 正式数据库”全链路闭环；实验板一次性脚本结束后不再出现在 EMQX 在线客户端列表属于正常行为。

**剩余工作**：临时账号和精确 ACL 仍保留，方便短期复测，不得作为正式课堂账号长期分发。正式推广应复用平台现有班级账号、课堂口令和平台生成的班级/小组 Topic；完成现场展示后删除临时账号及对应单 Topic 规则。教师物联页面的可视展示可另做一次人工冒烟，但不影响本次服务器侧接收闭环结论。

**正式开发方案与本地实现（2026-08-31）**：用户已确认方案并完成 P1/P2 本地开发。平台继续复用现有EMQX与IoT链路，初中保留Mind+，小学新增N17 Python代码入口；不部署省平台、不新增Broker或消息表。后端新增 `PENDING/SYNCED/FAILED` 同步状态、授权源健康检查、失败重试和口令轮换踢线；教师/学生页面按状态生成各自小组 Python 模板。`sql/iot_primary_board_v1.sql` 已在本机开发库连续执行两次，均退出码 0，3 个字段存在，配置行数为 0。IoT 相关测试 15/15、Vue3 `build:prod` 均通过。

**生产门禁（未执行）**：129 EMQX 当前仍需先启用内置数据库授权源、回填每班精确 Topic 前缀 ACL，并验证跨班/跨校拒绝后再删除 `class_* → county/#` 宽规则；生产 SQL、发布、临时账号清理和真实课堂试点均未在本轮执行。临时账号 `primary_board_probe` 及其单 Topic ACL 仍保留，禁止作为正式课堂账号长期分发。下一步按 `contexts/primary-iot-integration/tasks.md` 从 P3.2 本地权限/隔离验收开始，再进入 P4 生产备份与迁移。

## 24. 2026-08-31 画程流程图操作题（v1.27.0 已正式发布）

**产品与技术边界**：画程是现有普通课程中的 `practicalMode=FLOWCHART` 操作题，不建设独立作业或第二套成绩体系，不接入县级抽测。首期采用随 Vue3 制品发布的 `@logicflow/core@2.2.5` 原生组件，不嵌公网 diagrams.net，也不部署完整 draw.io 服务。教师在题库中分别制作标准答案和学生基础图，可配置题目级编辑权限、节点/连线锁定、同义文字、规则权重；学生使用开始/结束、处理、判断、输入/输出和箭头完成受限编辑。

**作答与批改主链**：课程保存时首次冻结题目基础图、答案、权限和规则。学生每次变更立即写浏览器本地备份，停止操作 2 秒后携带修订号保存服务端草稿；旧页面冲突不得覆盖新草稿。只有学生明确“完成并提交”才生成不可变提交版本，并以 `FLOWCHART:<submissionId>` 受控引用接入现有 `biz_student_answer`。结构检查比较节点类型、规范化文字/教师同义词、箭头方向和分支文字，忽略坐标和布局，生成逐项证据与建议分；建议分不自动写成绩，教师仍使用现有操作题评分接口确认。后续 AI 只能增加建议和证据，不改变教师最终确认边界。

**数据、代码和接口**：迁移 `sql/flowchart_operation_v1.sql` 新建 `biz_flowchart_question`、`biz_flowchart_lesson_snapshot`、`biz_flowchart_draft`、`biz_flowchart_submission` 四表，不改存量表。后端入口为 `/business/flowchart/question/*`、`/student/*`、`/grading/submission`；服务端重新规范化 JSON（512KB、200 节点、400 连线、四类节点、受控 ID/文字/坐标/属性），并拒绝空基础图/答案、悬空规则引用、重复规则编号和异常规则权重。首次并发打开通过草稿唯一键原子收敛，其他数据库异常不吞掉。学生权限校验包含本人身份、当前班级当前课程、课程题目关系和 `practical_open=1`。前端已接入题库、课程设计器、学生首页和教师连续批改页；历史成绩细分新增“画程”显示；宽度小于 900px 的小屏按首期边界强制只读。

**本机验证（2026-08-31）**：画程定向单测 7/7 通过；其中浏览器验收发现并修复“规则 JSON 因 Java 字符串重载被误放入 `msg`”的接口问题，并增加控制器回归测试。最终 `mvn -pl ruoyi-admin -am clean package` 成功（业务 380 + admin 3），fat JAR 为 107,674,408 bytes、SHA-256 `E1727E86013D8CC47286F72C00A1EAA6F164334AE90AF8C085901206FC4CD0D6`；Vue3 `npm run build:prod` 成功，`dist/index.html` SHA-256 `99D57443BDBB1AB275192C1C581C3776B29E0EB37D1D8E41F8681B666750D901`。构建仅有既有 vform `eval`、大 chunk 和 Java 弃用警告。专题资料见 `contexts/flowchart-tool/`，包含需求、设计、任务和三份已接受 ADR。

**迁移状态（2026-08-31）**：本机迁移前整库备份仍为 `backups/20260830_220340_local_before_flowchart_v1/xueyeceping_server_20260729_before_flowchart.sql`，82,318,371 bytes，SHA-256 `E0A8B85C88FDF7AE3E572D84A82079EAC6F6A1C4DBBBC1114CC8EACBEE18AB7F`。正式库也已在完整备份后执行 `sql/flowchart_operation_v1.sql`：四表和 4 个关键唯一约束均存在，发布前及验收清理后四表均为 0 行；迁移只新增表，没有改动存量业务表和成绩数据。

**浏览器闭环（2026-08-30）**：在本机普通课程 252、2020 级 1 班用临时画程题完成 15/15 检查：教师题库设计器、题目加入课程、学生拖动未锁节点、2 秒自动保存（revision 1→2）、小屏强制只读、明确提交并锁定第 1 版、教师读取 3 条结构证据、建议分展示和人工确认入口均通过，页面错误与 HTTP 500 均为 0。报告与截图位于 `output/playwright/flowchart-v1/`。验收结束已恢复课程原 20 题/100 分和操作题关闭状态，临时题目、答案及画程四表测试行均为 0。正式发布后又完成教师设计器专项，学生主链没有在正式库重复制造数据。

**教师设计器易用性补齐（2026-08-31）**：学生基础图页新增“从标准答案复制一份”，已有基础图时先二次确认，复制后两份 JSON 独立；开始和结束改为两个独立工具按钮，仍共享 `terminal` 文档类型并分别内置文字；全部节点同时支持单击自动排布和拖到画布指定位置；选中节点或连线后显示明确删除按钮，学生端继续受题目删除权限与元素锁定限制。输入/输出多边形改为 LogicFlow 要求的非负左上角坐标系，创建与重载时文字坐标位于节点中心，4 个默认连接点改为上、右、下、左四条边的正中间，不再使用四个顶点。本机教师题库专项浏览器验收 13/13 通过，页面错误与 HTTP 500 为 0，报告为 `output/playwright/flowchart-v1/teacher_ux_acceptance_report.json`。

**正式发布与验收（2026-08-31，v1.27.0）**：发布前正式库完整备份为 `D:\program\3009dazipingtai\backups\20260831_084148_c0aa4f8_flowchart_v1_before\ry-vue_before.sql`，90,364,594 bytes，SHA-256 `3984FFC78175DD4C7334C4F7B3E2744D8089ECAAB11B95431CD301F457BC3928`，同目录含 NSSM 与 Nginx 配置备份。后端和前端已切至 `releases/20260831_084148_c0aa4f8_flowchart_v1`；3009、3010 和 `/prod-api` 均为 200，线上 JAR 与 `index.html` 哈希分别为 `E1727E86013D8CC47286F72C00A1EAA6F164334AE90AF8C085901206FC4CD0D6`、`99D57443BDBB1AB275192C1C581C3776B29E0EB37D1D8E41F8681B666750D901`。平台更新 `1.27.0` 唯一且 `PUBLISHED`（update_id=56）。正式 Playwright 用临时题完成 14/14：四边中点连接点、文字居中、开始/结束、单击/拖拽、节点/连线删除、复制基础图及页面无 500 均通过；临时题已删除，四张画程表和验收标记残留均为 0。报告为 `output/playwright/flowchart-v1/production_anchor_acceptance_report.json`。

**回滚与剩余风险**：应用回滚可导入上述 `nssm-before.reg`、恢复 `nginx.conf.before` 并重启后端/热重载 Nginx，分别回到后端 `20260827_162145_1247099_shared_course_sticky_v1` 和前端 `20260829_course_designer_default_open_v1`。数据回滚优先停用画程入口并保留四张新表；当前正式表为空，仍禁止无备份删表。尚未完成双页面草稿冲突、重复提交、越权、补交、PNG 导出、真实机房 Chrome/Edge 和 400 学生自动保存容量专项；这些是后续加固项，不影响本次教师设计器和基础发布验收结论。

## 25. 2026-08-31 教师工具统一网关与 3006 自动服务（1.27.1，已上线）

**服务器状态**：正式主机 `10.52.1.123` 已将教师工具统一到 D 盘 Nginx 的 80 端口路径：邮件 `/tools/mail/` → 3002、 小学实验 `/tools/primary-lab/` → 3003、网络仿真 `/tools/network/` → 3020、物联网数据演示 `/tools/iot-data/` → 3006、图像识别 `/tools/image-recognition/` → 3001。内部服务端口不作为教师入口要求开放；Nginx 负责反向代理和必要的资源路径改写。

**服务与进程**：3006 已注册为 NSSM 服务 `TeacherToolIotData3006`，自动启动、异常退出自动重启，当前 Running；统一 Nginx 已注册为 `UnifiedNginx`，自动启动并统一监听 80、3010、3012。C 盘 `OpenResty` 已停止并禁用，旧 `Nginx-1.29.4-Server`、`Zuowen-Nginx-Reload` 计划任务已禁用；清理后仅保留 UnifiedNginx 服务进程树，避免 AI 修改错配置入口。D 盘活动配置为 `D:/programsoftware/nginx/nginx-1.29.4/conf/nginx.conf`。

**数据库与发布登记**：`sql/teacher_tools_unified_gateway_v1.sql` 已执行，5 条 `LOCAL_3005` 来源地址全部更新为 `/tools/.../`，重复来源 0、异常地址 0；平台更新 `1.27.1` 已登记为 `PUBLISHED`（update_id=57）。

**验证证据**：切换后 80、3010、3012、3009、验证码、旧站点路径、五个教师工具首页及 CSS/JS/SVG 静态资源均 HTTP 200；`xxkj.xsedu.net.cn` 与 `aitool.xsedu.net.cn` Host 路由均 200。最终监听端口全部由 D 盘 Nginx 同一进程树提供。备份目录 `D:\program\3009dazipingtai\backups\20260831_094758_teacher_tool_gateway_v1`；整库备份 `ry-vue_before.sql` 90,514,389 bytes，SHA-256 `81481B2A6653A84CA0CEFF5E28DA6046BC3EF075C58A45752800D768E89B0AF1`，同目录保存 C/D Nginx 配置、NSSM 注册表和计划任务 XML。

**回滚与剩余风险**：回滚前停止 `UnifiedNginx` 与 `TeacherToolIotData3006`，恢复备份的 D/C Nginx 配置和服务/计划任务注册表，再按原端口启动；数据库可执行 `sql/teacher_tools_unified_gateway_v1_rollback.sql` 恢复 5 条旧地址，平台更新记录按版本精确删除或改为草稿。当前五个工具已通过本机回环探活，跨网段真实教师浏览器访问和工具内部深层交互仍建议安排一次现场抽查。

## 26. 2026-08-31 小学实验板标准 MQTT 正式接入（release `20260831_primary_iot_v1`，v1.27.2，已上线）

**正式发布**：后端与 Vue3 前端已切换到 `D:\program\3009dazipingtai\releases\20260831_primary_iot_v1`，版本 `1.27.2`；NSSM 后端、统一 Nginx、3009/3010 与 `/prod-api` 探活均通过。正式库已执行 `sql/iot_primary_board_v1.sql`，`biz_iot_class_config` 的 3 个同步字段存在，8 条班级配置全部为 `SYNCED`。

**EMQX 精确授权**：129 EMQX 授权源顺序确认为 `built_in_database` → `file`；8 个班级账号已同步到内置数据库，并生成精确班级 Topic ACL。历史 `{re, "^class_[0-9]+_.*"} -> county/#` 宽权限规则已删除，临时账号 `primary_board_probe` 及其单 Topic ACL 已清理。平台接收器仍使用 `platform_iot_subscriber` 订阅 `county/#`。

**协议验收**：标准 MQTT 3.1.1 本班发布成功；跨班发布被 Broker 拒绝（MQTT 5 返回 `Not authorized`，MQTT 3.1.1 与订阅均未获得跨班消息）。测试消息 `final_acl_probe`、`cross_acl_q` 等残留均为 0，证明小学实验板可直接使用 `umqtt.simple.MQTTClient`，显式填写平台生成的账号、课堂口令和小组 Topic。

**发布登记与备份**：平台更新 `1.27.2` 已登记为 `PUBLISHED`（`update_id=58`）。发布前整库备份位于 `D:\program\3009dazipingtai\backups\20260831_primary_iot_v1_before`，SHA-256 为 `3991854E2157BB4430FDC00E4C1C39173BBDC0A57A7099F3CF5AE19E28AC4D6E`；旧 release、NSSM 与 Nginx 配置均保留，可按备份目录回滚应用和数据库。

**剩余风险与下一步**：生产服务器链路和权限隔离已闭环；仍需在真实课堂完成两组双板并行、连续 10 分钟到达率、断网恢复、教师页面实时显示及初中 Mind+ 回归试点。课堂推广不得使用临时账号，教师应复制平台按班级/小组生成的 Python 代码。

## 27. 2026-08-31 学生端小学 Python 入口默认化（release `20260831_primary_iot_student_python_v2`，v1.27.3，已上线）

**改动**：学生物联页面默认打开“**小学实验板 Python**”页签，并将一键复制本人小组 Python 代码置于首要位置；页面说明改为明确区分小学 Python 与初中 Mind+。初中 Mind+ 参数页签和复制功能继续保留，后端账号、口令、Topic、权限和数据库结构均未改变。

**正式发布与验证**：仅更新 Vue3 前端，后端继续使用 v1.27.2。前端 ZIP SHA-256 `7ECEB67018D9EDAFD7F7AB8566BE237104D4F8670C4074A47710630AE74727C3`，线上 `index.html` SHA-256 `9E61CC6612A2DB792AB5E27BB26CF595EB0F0994B3E2C929E5F26C082218776C`；Nginx root 已切换到新前端。首次 reload 未刷新旧 worker，随后按精确进程完成 Nginx 重启，外部 3010 已返回新脚本 `index-BnYGJemc.js` 与物联模块 `iot-qm3q2Mog.js`（均包含小学 Python 入口），3010 与 `/prod-api` 均 HTTP 200。平台更新 `1.27.3` 为 `PUBLISHED`（`update_id=59`）。

**备份与回滚**：备份目录 `D:\program\3009dazipingtai\backups\20260831_primary_iot_student_python_v2_before`，整库 SHA-256 `95140C9B6E98B7CDED565DD4CF4C9300CB78E8E2E8E133B899A11AA29B9EAFE5`。回滚只需将 Nginx root 恢复到 `releases/20260831_primary_iot_v1/frontend` 并 reload；后端和数据库无需回滚。

## 28. 2026-08-31 小学 N17 Python 模板修正（release `20260831_primary_iot_python_template_v1`，v1.27.4，已上线）

**改动**：教师端和学生端共用的一键复制模板改为 N17 示例格式：增加 `# -*- coding: utf_8 -*-`、`from npython import *` 导入说明，并在 WiFi 连接、MQTT 连接、发送成功和断线重连阶段调用 `oled.print(1,1,"...",1)` 显示状态。MQTT 账号、课堂口令、Topic、消息格式、循环发送和初中 Mind+ 均未改变。

**验证与发布**：Vue3 `npm run build:prod` 成功；生成模板已核对文件头、`npython`、`oled.print` 以及动态账号/口令/Topic。前端 ZIP SHA-256 `6A8147685FC5F79231F2C8D9D43529EB8B24C216438827AC60E0C04C87ED2BFB`，线上 `index.html` SHA-256 `9F99705DF3E0486B5BE956C83E928CABBD6E09665632BD5A4CE56166400694C2`；线上模块 `iotPythonTemplate-Dv8_MI0v.js` 已确认包含新格式，3010 和 `/prod-api` 均 HTTP 200。平台更新 `1.27.4` 为 `PUBLISHED`（`update_id=60`）。

**备份与回滚**：备份目录 `D:\program\3009dazipingtai\backups\20260831_primary_iot_python_template_v1_before`，旧前端 release 完整保留。回滚只需把 Nginx root 恢复到 `releases/20260831_primary_iot_student_python_v2/frontend` 并重启统一 Nginx；后端和 MQTT 无需回滚，数据库仅需按版本精确撤销平台更新记录。

## 29. 2026-09-01 教师高频问题优化（v1.27.8，已上线）

**实施边界**：初始阶段按用户要求只做正式环境只读排查和本地修改；用户在后续轮次明确要求部署后，于 2026-09-01 按备份、迁移、切换、探活和登记流程正式上线。后端相关能力已包含在活动 1.27.7 JAR 中，本轮未重复上传或重启后端；3010 前端独立切换到 1.27.8 新 release。

**正式环境只读证据**：朱屹老师删除课程 283 的首次请求在 `2026-09-01 10:22:51` 成功且服务端仅耗时 28ms，页面随后在 `10:23:13`、`10:23:29` 又重复请求同一删除接口并收到“课程不存在”，说明长延迟主要来自教师首页缓存未失效和前端没有即时移除卡片，不是数据库删除耗时。林晓晓老师视频对应课程 287、2026 级 10 班的操作题批改页；同一时段课程、班级、评分项、提交列表等接口均为 HTTP 200，未发现新 5xx 或后端异常。录屏中的灰色名单和禁用光标对应“未交学生不可进入批改”但页面没有解释；本轮按交互问题处理，不把它误判为视频文件播放故障。

**本地改动**：成绩导出新增明确的“各课程成绩明细”列组和“仅课堂表现”快捷选择，服务端仅在选中该列组时写出逐课程三列，未传列参数的旧调用仍保持全量导出兼容；课程设计器选题列表显示出题人；课程保存/删除成功后按学校清理教师首页 Redis 缓存，删除成功时前端立即移除课程卡片并阻止重复点击；操作题批改列表把已交学生排在前面并自动选中，未交学生点击时明确提示，整班无提交时显示空状态；题库操作列固定在右侧；学生管理复用既有后端导出接口和 `business:student:export` 权限增加导出按钮。没有新增依赖、数据库表、SQL 或配置项。

**验证**：定向测试 `TeacherDashboardCacheTest`、`TeacherDashboardCacheServiceTest`、`ScoreQueryExportColumnsTest` 共 5/5 通过；完整构建阶段业务模块 387/387、管理端 3/3 测试通过；发布前合并治理专项共 38/38 再次通过。Vue3 `npm run build:prod` 成功（2905 modules），仅有既有 vform `eval` 和大 chunk 警告。正式教师 Playwright 已确认教师首页、学生导出、题库右侧固定操作列和课程设计“出题人”可见，页面脚本错误与 HTTP 500 均为 0；该账号当前无成绩数据，未真实点开“仅课堂表现”对话框，相关线上静态制品标记已核对存在。

**正式发布、回滚与剩余风险**：1.27.8 前端活动目录为 `releases/20260901_172047_teacher_feedback_governance_v1/frontend_v4`，`index.html` SHA-256 `24E0D2D92AA8E9525037592D477E8F2FCCE84EB5F944600DA5FAE167B513602D`；平台更新为 `PUBLISHED`（update_id=64）。回滚前端时将 3010 root 切回 `releases/20260901_student_add_validation_hotfix_v4/frontend` 并重启 `UnifiedNginx`，本节功能本身无业务数据回滚。仍建议使用朱屹、林晓晓及有成绩数据的正式账号补做“仅课堂表现”实际下载、课程卡片删除即时消失和未交学生提示的现场验收；Redis 按学校清理在学校课程规模显著增大时再评估集合索引或游标扫描。

## 30. 2026-09-01 线上严重告警完整治理（方案 B，v1.27.8 已上线）

**线上只读结论**：正式环境近 24 小时 11 条严重慢接口全部来自 `/business/student/importData`，典型耗时 4.2～4.5 秒、7.8 秒、12.3 秒，最大 37.316 秒；对应 48～420 行的导入。相同时间窗没有学生导入慢 SQL。抽样新账号显示每名学生密码哈希均不同，服务器生成速度约 11 次/秒；同时 `sys_user` 没有 `user_name` 索引，查重 `EXPLAIN` 为全表扫描（约 1.2 万行）。根因是逐行 BCrypt、逐行查重/写库/班级检查叠加，而非单条 SQL 卡死。抽查 335 个导入账号的用户、学生档案和角色关系均完整，当前没有发现已发生的数据破坏。

**误报结论**：课程 283 首次删除成功后两次前端重试产生“课程不存在”，服务端分别仅耗时 5ms、1ms；手动推进的四个班均为 0 分人数、未达到 50% 阈值，属于正常业务拒绝。旧诊断把二者均标成“严重”，会掩盖真正故障。

**实现**：学生导入改为先全量规范化和 Excel 内重复校验，再分批查重；每批导入只计算一次默认密码 BCrypt，用户、角色、学生档案按 200 条批量写入并核对影响行数，写库异常直接上抛触发整批事务回滚；同校导入使用 Redis 带令牌锁防并发，事务结束后比较令牌释放；教师班级只预取一次并按班级去重补齐。接口返回总数、成功/失败数和解析、校验、密码、数据库、总耗时，前端显示结构化汇总并限制明细为 20 条，学生姓名在 HTML 结果中转义。正式库已执行 `sql/student_import_governance_v1.sql`，新增普通联合索引 `(user_name, del_flag)`；精确回滚脚本为 `sql/student_import_governance_v1_rollback.sql`。

**诊断与幂等**：诊断现在把已知阈值未达、课程重复删除等标为“业务提示/info”，学生导入按 `<10s / 10～30s / >=30s` 分成提示、关注、严重；未知数据库或系统异常仍为 `system/critical`。历史 `sys_perf_event` 在读取时按新规则动态重算，无需改表。健康摘要只统计 warning/critical。课程删除对已不存在的课程返回幂等成功，但仍先校验现存课程管理权；手动推进未达条件返回结构化失败清单，不再抛系统异常，未知异常继续上抛。

**验证**：发布前合并 `ScoreQueryExportColumnsTest`、缓存、学生导入、诊断、课程与推进测试共 38/38 通过；覆盖批量写入、单次哈希、Excel 内重复、防并发锁、异常传播、三档导入耗时、未知数据库错误保持严重、历史事件动态重分级、课程删除幂等和手动推进业务结果。Vue3 生产构建成功（2905 modules，仅既有 vform `eval`/大 chunk 警告）。专题资料见 `contexts/student-import-diagnosis-governance/`。

**正式发布与回滚**：发布前整库备份位于 `D:\program\3009dazipingtai\backups\20260901_172047_teacher_feedback_governance_v1\ry-vue_before.sql`，94,476,970 bytes，SHA-256 `C9A5A901B2DBAC954C30E3363975AE15468751F558BCD55A0BCCD251D1BFA7E0`；同目录保留 Nginx 配置、发布包、正向/回滚 SQL 与哈希清单。前检有效用户名重复组为 0；迁移后索引列为 `user_name`、`del_flag`，探针 `EXPLAIN` 命中 `idx_sys_user_name_del_flag` 且 `Using index`。3010 已切换到上述 1.27.8 前端，3009、3010、`/prod-api`、80、3012 均 HTTP 200，`UnifiedNginx` 为 Running。回滚应用仅需切回上一前端 release；数据库执行 `student_import_governance_v1_rollback.sql` 删除本次普通索引，不修改任何用户、成绩或业务数据。50/200/420 行真实 Excel 耗时与并发双导入仍需在低峰期使用可清理验收数据补测。

## 31. 2026-09-01 操作题预览状态误报热修（1.27.7，已上线）

**根因与修复**：旧流程命中共享页图缓存后直接返回，漏写当前 Office 附件的 PDF 预览状态，导致附件显示 pending、而答卷已 success，监控中的活跃数和队列数均为 0。新流程只复用页图，不跳过当前附件的 Office→PDF 转换和状态同步；应用启动时对 pending/converting 且页图成功的首附件执行对账：同名 PDF 已存在则直接回写 success，否则重新入队转换。

**正式发布与验证**：正式库备份 `D:\program\3009dazipingtai\backups\20260901_operation_attachment_preview_cache_hotfix_v1_before\ry-vue_before.sql`，94,186,669 bytes，SHA-256 `603859157A2B326F019FCB9221BECD65881440383BB0377538623656DE4C5701`；新后端 release `20260901_operation_attachment_preview_cache_hotfix_v2`，JAR SHA-256 `9C3A0B7CC151FD74C2905F106D251CD9C047797A5F567B2AC29ED53CC67F1618`，与本机构建一致。切换后 NewDaziBackend3009 Running，3009、3010、`/prod-api` 均 HTTP 200；受影响附件剩余 0，平台更新 `1.27.7` 为 `PUBLISHED`（update_id=63）。本轮发现并修正首次发布脚本的配置目录多一层问题，未影响旧版本持续运行。

**回滚与风险**：应用回滚导入上述备份中的 `nssm-before.reg`，切回 `releases/20260831_student_class_99_v1/backend` 后重启；无业务表结构变更，无需 SQL 回滚（仅需将平台更新 1.27.7 精确改为草稿/删除）。后续仍需观察高峰期新提交的 Office 转换耗时和失败率。

## 32. 2026-09-01 方案二安全处理与成绩数值排序（本地已实现，未发布）

**业务规则**：学生没有任何答题、签到、课堂表现、导学单、抽测或作品记录时才允许物理删除；存在历史记录时由 `sys_user.status` 执行停用/恢复，学生档案、`studentId`、`userId` 与历史成绩均保留。批量纠错通过 `/business/student/correction/preview` 预览、`/correction/apply` 整批事务更新，只能按 `studentId` 原地修改姓名、账号、入学年份、班级、学号和备注；重复编号、越权、账号/学号冲突或下载后资料变更会整批拦截。课程 `biz_lesson.status`（0 正常、1 已归档）用于有历史数据课程的安全退出，归档不删除成绩并从日常课程入口隐藏，支持恢复。

**排序修复**：成绩查询主表、课堂表现、年级排行榜、理论详情和导学单相关列表的学号/班级/账号排序均改为自然数值优先；后端班级、导学单和 Python 列表 SQL 增加 `CAST(... AS UNSIGNED)` 并保留原字符串作为同值兜底，避免 1、10、2 的字典序。

**本地验证**：Vue3 `npm run build:prod` 通过（2905 modules，仅既有依赖警告）；`AnswerDeletionGuardServiceTest`、`BizStudentImportGovernanceTest` 共 7/7 通过。业务全量测试曾因旧删除守卫测试未注入新增业务记录 Mapper 出现 1 个 NPE，已补测试依赖后定向测试通过；本轮未执行数据库迁移、未发布正式环境。下一步为有成绩数据账号补做浏览器排序与停用/历史查询验收，再决定是否进入正式备份发布流程。

## 33. 2026-09-01 方案二安全处理与成绩数值排序（1.28.0，已上线）

**正式发布**：后端与 Vue3 前端已切换至 `releases/20260901_scheme2_score_numeric_v1`。后端 `NewDaziBackend3009` 为 Running，3010 由 `UnifiedNginx` 提供；3009、3010、`/prod-api`、80、3012 均 HTTP 200。线上 JAR SHA-256 为 `2693A1E3395B34902C75CE9619C958CE1F6EEA16AAA1A42E8BBC3B64C0F930DB`，前端 `index.html` SHA-256 为 `3E705B24CA3EA0023E1C9E5D1F8B85A4B9752D1D54BA88C4926218C52AFEB2A5`，与本机构建制品一致。

**数据库迁移**：正式库执行 `sql/lesson_archive_status_v1.sql`，`biz_lesson.status` 已创建（0 正常、1 已归档）；执行 `sql/platform_update_scheme2_score_numeric_v1.sql` 后平台更新 `1.28.0` 唯一且为 `PUBLISHED`（update_id=65）。学生导入联合索引保持原状，未重复执行。

**备份与回滚**：发布前整库备份位于 `D:\program\3009dazipingtai\backups\20260901_scheme2_score_numeric_v1_before\ry-vue_before.sql`，94,520,460 字节，SHA-256 `61BDC6848D01CADC46BB0672CF453FBBC3DD67AEC6AD9220AC7D4F34993D6364`；同目录保留 `nssm-before.reg`、`nginx.conf.before`、迁移 SQL 和前端包。回滚应用时导入 NSSM 备份、恢复 Nginx 配置并重启 `NewDaziBackend3009`/`UnifiedNginx`；数据库新增字段可兼容保留，平台更新记录按版本精确改为草稿或删除，禁止删除历史成绩。

**剩余风险**：本轮完成服务与静态资源探活，尚未用正式有成绩账号做成绩页面自然排序、停用/启用和归档课程的浏览器现场闭环；建议低峰期补做，不影响本次制品和服务发布结果。

## 34. 2026-09-01 成绩列表自然排序回调热修（1.28.1，已上线）

**根因**：Element Plus `sort-method` 接收单元格值而非整行对象，上一版回调读取 `a.studentNo` 导致比较结果恒为 0，页面继续沿用原始字典序。

**修复与发布**：成绩主表、课堂表现、排行榜、理论详情等列改为值级自然排序回调；课程成绩列改用 `sort-by` 行级函数。Vue3 构建 2905 modules 成功，前端已切换至 `releases/20260901_score_numeric_sort_hotfix_v1`，`index.html` SHA-256 `77290EE155FD65218E67894E572281CD3F24B73356DE1A02F802F128463BCF9D`。后端未重启，3009/3010/代理/80/3012 均 HTTP 200。

**正式登记与回滚**：平台更新 `1.28.1` 唯一且为 `PUBLISHED`（update_id=66）。发布前备份位于 `D:\program\3009dazipingtai\backups\20260901_score_numeric_sort_hotfix_v1_before`，整库 SHA-256 `A4B255F74419F8A0CBAA03756CF660FBB9BA7C162056A68B0BE348FA8FD42B1A`。回滚只需恢复该目录的 `nginx.conf.before` 并重启 `UnifiedNginx`；后端和业务数据无需回滚。

**剩余风险**：尚未使用正式有成绩账号在浏览器中点击学号排序箭头核验 `1、2、10` 的实际显示，建议低峰期补做一次只读验收。

## 35. 2026-09-02 学生批量纠错上传热修（1.28.2，已上线）

**故障证据**：正式环境张金桥（账号 `611869`，用户 ID `1589`）在 08:46 三次调用 `/business/student/correction/preview`；请求到达后端但 `MultipartFile file` 为 `null`，触发空指针并被前端统一显示为“系统繁忙”。此前 08:42 下载纠错表成功；本次失败请求未修改后端或数据库。服务器旧日志另有超过 10MB 上传被拒记录，本轮未扩大该限制。

**修复**：`RuoYi-Vue3/src/api/business/student.js` 的纠错预览请求显式携带 `Content-Type: multipart/form-data`，并关闭该上传请求的全局防重复提交头，避免 FormData 被 JSON 默认头序列化。正式前端基于线上当前 release 精确修补并将学生模块重命名为 `student-correction-upload-hotfix-20260902.js`，引用替换 6 处；后端无需改动或重启。

**正式发布与验证**：3010 已切换至 `releases/20260902_student_correction_upload_hotfix_v1/frontend`；线上修补模块 SHA-256 为 `BC211F79861A1A233DB31C76520D43B19187700B6841CC68887BFEFA288F6906`，静态请求返回 200 且包含 multipart 修复。正式库平台更新 `1.28.2` 为 `PUBLISHED`（`update_id=67`）。3009、3010、`/prod-api`、80、3012 均 HTTP 200，`UnifiedNginx` 最终状态为 Running。

**备份与回滚**：发布前整库备份位于 `D:\\program\\3009dazipingtai\\backups\\20260902_student_correction_upload_hotfix_v1_before_retry\\ry-vue_before.sql`，164,298,852 bytes，SHA-256 `7CBBD9CF7BF6BB06C9EB63E2063B0E3CE817D7FB201A1DB70F12CF4887C7B8E9`；同目录保留 Nginx/NSSM 配置、前端包和 SQL。回滚仅需将 3010 root 切回 `releases/20260901_score_numeric_sort_hotfix_v1/frontend` 并重启 `UnifiedNginx`；数据库无结构变更，仅需按版本精确将平台更新记录改为草稿或删除。原失败的空备份目录 `..._before` 保留作排障现场，不作为回滚备份。

**剩余风险**：本轮完成静态制品和服务探活，尚未使用张金桥或其他正式教师账号上传真实纠错表做现场闭环；纠错 Excel 仍受正式环境现有 10MB 上传上限约束。

## 36. 2026-09-02 学生入学年份年级备注（1.28.3，已上线）

**功能**：学生管理的入学年份筛选、列表、编辑和按班删除下拉框均根据当前校区学部实时附加年级备注。小学显示 `x年级`，初中显示 `初x`，高中显示 `高x`；学年按每年 7 月 20 日切换。实际查询、保存和提交的入学年份值仍为纯年份，不修改学生数据或接口语义。

**正式发布与验证**：3010 前端已切换至 `releases/20260902_student_entry_year_grade_v1/frontend`，线上学生管理模块为 `student-entry-year-grade-20260902.js`，SHA-256 `F0AEF9F6F851B86092E6F6AC347F5CA4332CF30AF3760B4584A51F2E9A189B87`。正式库平台更新 `1.28.3` 为 `PUBLISHED`（`update_id=68`）。`UnifiedNginx` 为 Running；3009、3010、`/prod-api`、80、3012 均 HTTP 200。浏览器验收通过：管理员可进入学生管理，入学年份下拉显示 `（1年级）`，无页面异常和控制台错误；报告见 `output/playwright/20260902_student_entry_year_grade_v1/report.json`。

**备份与回滚**：发布前整库备份位于 `D:\program\3009dazipingtai\backups\20260902_student_entry_year_grade_v1_before\ry-vue_before.sql`，165,011,196 bytes，SHA-256 `9661D33CF475A2C67A80F08CB60AC257059FA95A29DF17659FE63F81106EDC3C`，同目录保留 `nginx.conf.before`、前端包和平台更新 SQL。回滚只需将 3010 root 恢复为 `releases/20260902_student_correction_upload_hotfix_v1/frontend` 并重启 `UnifiedNginx`；无需后端重启或业务 SQL 回滚，必要时仅按版本精确撤销 `1.28.3` 平台更新记录。
