# 架构资料索引

> 2026-09-24 当前本机候选：用户确定课程推进后旧课立即停止学生提交，退回与重交仅限当前课；同时修复打字高分回显和理论提交失败后打字作答状态。Java 定向测试 46 项及 Vue3 构建通过；真实师生联调状态见 [PROJECT_CORE v3.99](../../contexts/PROJECT_CORE.md)。正式仍为 130 的 1.30.23。规则见[当前课提交 ADR](../../contexts/student-experiment-tools/ADR-004-current-lesson-only-submission.md)。以下旧课补交记录仅为历史。

> 2026-09-23 **当前本地候选**：退回重交、画程上下锚点与可选尺寸、题库 DATA_LAB 入口、打字统计年级筛选、百炼/DeepSeek/豆包视觉模型配置已实现。隔离库师生 API、Chromium 画布/页面、Java 94 项、Node 6 项、双端构建通过；真实厂商模型调用因无 Key 未验，隔离库旧 DATA_LAB 缺快照使 v2 迁移按设计阻断。只在本机执行 AI 凭据增量 SQL，正式仍为 130 的 1.30.23，未连接或部署服务器。以 [PROJECT_CORE v3.98](../../contexts/PROJECT_CORE.md) 和 [本地计划与验收](../../contexts/dev-plan-20260923.md) 为准；以下各段为此前阶段的历史记录。

> 2026-09-22当前正式：130已全量发布1.30.23，包含DATA_LAB、加分、题单并发/快照、协作与流程图、导学单修改，保留1.30.22监控。DATA_LAB四表已备份迁移，运行库/中文字体本站提供，Nginx补mjs MIME。654 Java通过/2跳过、构建、隔离库真实师生与并发、正式教师页面及线上Pyodide通过；更新102已PUBLISHED且教师可见。正式未写学生作品成绩，课堂协作/低配压力仍需观察。当前事实见PROJECT_CORE v3.96，备份回滚见DEPLOYMENT_RUNBOOK；以下较早状态是历史。

> 当前本地DATA_LAB以PROJECT_CORE v3.95及[ADR-014](../../contexts/python-judge0/adr/ADR-014-data-lab-version-access.md)为准：补齐版本快照、班级授权、进度和评分版本控制，已解除本地临时全拒绝。22项测试/构建和真实Pyodide运行通过；未迁移、未真实数据库联调、未发布。必须先v1/v2 SQL再配套应用更新，学校离线环境须准备Pyodide镜像。下段v3.94是本轮修复前的状态。

> 2026-09-21本地审查（PROJECT_CORE v3.94，未发布）：加分改为保存快照后局部更新；题单编辑事务锁共同主行并复用历史快照。DATA_LAB因来源权限/快照/统计未闭环，服务和资源入口统一拒绝，普通Python题单不依赖其新表。Worker隔离和文件校验不代表功能已可启用；未操作数据库或服务器。见[审查记录](../../contexts/local-review-20260921.md)和[ADR-013](../../contexts/python-judge0/adr/ADR-013-plan-edit-lock-and-data-lab-isolation.md)。

> 2026-09-21本机电子导学单定点移植：保留现有模板/绑定/草稿架构，补齐图片组件和可取消的设计器初始化；36项前端测试、生产构建及模拟接口浏览器回归通过。未真实三角色联调、未发布，无SQL/新依赖/后端重启。来源与边界见 `../../contexts/guide-sheet-targeted-integration-20260921.md` 和 PROJECT_CORE v3.93。正式状态仍如下1.30.21。

> 当前状态（2026-09-21）：130已发布1.30.21/20260921_share_grading_v1，分享密文列已迁移、前后端已切换；批改刷新正式页面模拟回归与教师版本可见通过。真实分享跨设备操作待正式教研员凭据补验。下方版本为历史，见PROJECT_CORE v3.92及DEPLOYMENT_RUNBOOK。


> 2026-09-21新增本机分享回显：主题密文列与AES-GCM持久化，管理接口解密回显，匿名继续校验哈希。待备份执行ciphertext_v2 SQL和联调/发布；正式仍1.30.20。见PROJECT_CORE v3.90、research-activity/ADR-006-recoverable-share-token.md。

> 2026-09-21最新正式状态：130已发布 `20260921_research_ai_full_v1` / `1.30.20`，包含教研多附件/预览/分享正文留言和AI失败数量/批改页布局修复；真实教师验收、构建测试与更新记录可见均已通过。回滚与备份见 `DEPLOYMENT_RUNBOOK.md`，当前事实见 PROJECT_CORE v3.89。

> 2026-09-21本机教研多附件：三类帖子统一最多10附件，Office/PDF受鉴权预览，复用资源表与LibreOffice，无SQL/新依赖。公开页包含三类正文，不公开附件及云盘。Java46项、前端8项/构建及模拟页面验收通过，真实文件联调待验，未发布。见PROJECT_CORE v3.88、research-activity/ADR-005-unified-attachments.md及对应需求/设计/任务。

> 2026-09-21 当前130正式1.30.19/20260920_insight_batch_pdf_v2，123仍1.30.18：学情V3.4共享试卷一次解析、已有原答文本分批、知识点隔离、显式参考状态与常驻进度；整班PDF按页段确认拆分。正式报告4完整715秒、36有效0失败、覆盖85.71%，成绩不变；PARTIAL含21人局部待核对，6/10知识点达到班级门槛。旧版35分钟仅1成功35失败。轮询全表UPDATE死锁改为教师范围过期ID主键更新，18次读取及桌面/手机原卷证据通过；Java49项含1跳过、前端构建、72页拆36份正式接口通过。无业务迁移/配置变化，已重启，发布登记PUBLISHED且教师可见。备份回滚见DEPLOYMENT_RUNBOOK；PROJECT_CORE v3.87与paper-import专题为准。

> 2026-09-20最新网络修复：123路由改由SYSTEM开机/每分钟任务按WireGuard当前接口自动维护；已实测隧道重启丢路由后自动恢复及普通SSH握手。整机再次重启未测，恢复约需一分钟。学校分流、DNS、正式应用不变；PROJECT_CORE v3.86与DEPLOYMENT_RUNBOOK为准，下面手动持久路由为历史。

> 2026-09-20网络后续：123/32路由曾再次缺失，已恢复活动/持久项并验证普通连接经WireGuard完成SSH握手；丢失原因及重启持续性未确认。无应用发布，其他任务状态保持；见PROJECT_CORE v3.85与DEPLOYMENT_RUNBOOK。

> 2026-09-20本机追加：成绩表右上角理论/打字重做，独立打字历史及轮次，清分后重新计分、同轮最高分。状态并发no-op upsert旧快照误报已复现并改写后当前读。31项Java/2项前端、构建、理论15项/打字11项实际检查通过；临时数据清理、8080重启。未发布，新环境需两份SQL。当前见PROJECT_CORE v3.84和contexts/theory-redo/；下段是理论阶段记录。

> 2026-09-20本机理论重做：教师成绩表勾选当前班级课程学生，一次确认重做全部选择/判断；旧答卷私下归档，新分替换，打字操作保留。新增theory_redo_v1.sql已仅本机备份迁移，8080已重启；25项Java/2项前端及构建通过，真实接口并发/回滚和教师学生页面已验证。正式未上传未迁移。见PROJECT_CORE v3.83、`contexts/theory-redo/requirements.md`、`design.md`、`tasks.md`、`ADR-001-current-score-with-private-history.md`，以及BUSINESS_BOUNDARIES/DATA_MODEL新增段。

> 2026-09-20平台更新收口：130的1.30.12～1.30.16已由草稿补为PUBLISHED，真实教师接口及页面可见。正式部署完成必须自动发布对应更新并验证教师可见，草稿不算完成。仅更新记录状态，无需重启；备份、SQL和回滚见DEPLOYMENT_RUNBOOK，当前事实见PROJECT_CORE v3.82。

> 2026-09-20本机网络更新：用户授权添加123/32持久主机路由走WireGuard，普通SSH已自动走10.99.1.8并连通，无需连接代码单独绑网卡；其他校园网分流不变。未重启验证，隧道需开启。正式仍1.30.16，见PROJECT_CORE v3.81和DEPLOYMENT_RUNBOOK。

> 2026-09-19当前正式1.30.16：130前后端统一为20260919_full_insight_fixes_v1，完整构建包含五项课堂修复及学情V3.3.1，三表与打字权限迁移完成，诊断开关启用且其他环境配置保持。Java591项（1跳过）/前端73项及三角色正式冒烟通过；教师导航、13校打字图、课堂三次刷新正常；正式学情报告1完成1份，成绩不变。SSH需单连接指定WireGuard→123→130，WorkBuddy校园网路由导致默认123:22超时，未改全局路由。备份、回滚和限制见PROJECT_CORE v3.80、DEPLOYMENT_RUNBOOK；以下未发布记录为历史。

> 2026-09-19五项课堂修复仅本机：会话401复核与503分流、批改口径与导航、流程图几何保真、题库详情、打字统计恢复。41项Java/7项前端测试及隔离打包通过；教研员旧权限确因监管迁移替换，本机已备份后执行恢复SQL，正式未执行。未重启本机8080、未发布；123第一跳超时，现场登录根因及37/13仍待核实。详见PROJECT_CORE v3.79、`contexts/five-issues-20260919.md`、BUSINESS_BOUNDARIES和DATA_MODEL；保留本机学情未发布边界。

> 2026-09-19本机学情V3.3：仅修复目录外具体错误归并、全部原答证据展示、逐空参考争议排除和对错并存PARTIAL。版本指纹insight-v3.3.1，旧报告保留；无SQL/依赖/正式发布。37项相关Java测试及前后端构建通过；最终报告4完成、1/46、正式成绩53不变，三图点击/原答/参考疑点/手机布局通过。参考和原页识别仍需教师核对，见PROJECT_CORE v3.78、paper-import/tasks。

> 2026-09-18正式1.30.15：130前端已切20260918_classroom_gate_fix_v1，后端仍1.30.14且未重启。修复所有教师课堂大屏误读开关顶层响应、30秒轮询误显示关闭，增加保存与查询竞态保护。原域名真实课程574连续约70秒/3次读取保持开启，前端5项及推进5项测试通过。正式JAR原已默认推进双开，无后端补发、无业务迁移；回滚仅恢复Nginx root。PROJECT_CORE v3.77、DEPLOYMENT_RUNBOOK及student-experiment-tools为准；本机学情V3未发布。

> 2026-09-18本机学情V3：已按确认的B路线新增独立异步任务与三表，整卷解析→逐人证据→后端聚合→教学活动；固定最低10人且80%覆盖，每知识点独立门槛。真实1/46报告已完成且正式成绩统计不变。四图与原卷回看可用；人工修订/编辑/打印及最终独立页仍待办。未发布130，SQL仅本机执行，开关默认关闭。以PROJECT_CORE v3.76、paper-import/ADR-002-insight-v3-evidence.md及tasks为准，下方MVP描述为历史。

> 2026-09-18 V3.2收缩优化：只做真实材料诊断和首屏简化。报告2真实1/46有效样本已完成，三类证据入口共用原答/参考/原卷页；首图保留知识、错误、热力，成绩折叠。33项测试及构建通过；历史图表定位超时已于09-19修正脚本关闭动画等待后通过，见顶部V3.3验证。未发布。

> 2026-09-17 21:15最新正式状态：130已发布1.30.14 / 20260917_ai_apply_home_fix_v1。前轮JAR仍含待核对跳过逻辑，本轮精确替换已测class并核验；真实课程575任务28采用1份，正式总分与分项均52、审计完整。批改页抽屉移入单根节点修复返回教师首页白屏，真实点击通过。14项测试及构建通过。无表迁移；备份/回滚见DEPLOYMENT_RUNBOOK，当前事实见PROJECT_CORE v3.75。下文1.30.13采用成功的推断已被本次现场证据纠正。

> 2026-09-17 20:41正式状态：130已上线 `20260917_paper_ai_v2` / 1.30.13。待核对建议可经教师确认批量采用，取代下文旧禁止规则；空白满分生成拦截保留。50项发布前测试、20项调整后测试、构建、真实本地按钮及正式教师页面/接口通过。正式成绩未代采用，版本草稿93已登记；备份及回滚见DEPLOYMENT_RUNBOOK、PROJECT_CORE v3.74。以下“未发布”均为历史阶段。

> 2026-09-17 最新本地验收：提示词标识缩短为operation-rubric-v3-paper，兼容既有VARCHAR(32)，修复AI任务创建失败。26项测试及后端构建通过，真实任务3使用0902完成全卷并保存建议，第8项5/14且空②待复核，正式成绩未改变。费用显示统一为元，四模型真实页面通过。无SQL、未发布，见PROJECT_CORE.md v3.73。

> 2026-09-17 0902与价格：qwen3.8-max-0902独立可选，默认仍普通3.8。两者官网北京参考价输入12/输出36元每百万Token，内部千Token换算；管理员价格优先，无SQL。14项测试、构建、真实页面和0902真实图片连通性通过，未发布。详见PROJECT_CORE.md v3.72与operation-artifact-ai-grading专题；0902未做全卷准确率对照。

> 2026-09-17 模型配置保存补充：已有Key时留空保留密文，仅输入新Key才替换；首次配置仍要求Key。默认3.8，选项Qwen3.8-Max（性能最强）；当前教师已通过真实页面留空Key切到3.8，其他教师未迁移。14项测试、构建、重启和页面通过，无SQL、未发布。见PROJECT_CORE.md v3.71。

> 2026-09-17 纸质评分修复本地完成：学生页逐空识别后再对照参考评分，服务端拦截已识别空白满分，待复核纸质禁止批量采用；3.8可选、未知价格待确认。真实3页全卷两模型均识别第8题②blank，第8项3.7为4/14、3.8为5/14，均需复核。35项测试、构建、重启、真实教师模型选择页面通过，未改正式成绩/教师配置，未发布、无SQL；旧建议需重新发起批改。手写和部分分仍有差异，教师确认到学情全链未验。见 PROJECT_CORE.md v3.70、BUSINESS_BOUNDARIES.md 与 operation-artifact-ai-grading/ADR-007-paper-evidence-before-grading.md。

> 2026-09-17 真实 PDF 导入已验：修复本机相对上传路径被 Tomcat 解析到临时目录的问题；身份模型使用当前授权班级候选核对手写姓名，最终严格匹配。课程380真实3页PDF已导入原作品版本16403，未评分，未发布。见 PROJECT_CORE.md v3.68 和 paper-import 专题。

> 2026-09-17 本机启动补充：`scripts/start-local-backend.ps1` 从私密配置加载原 AI 加密主密钥后启动后端。真实教师配置接口已确认 masterKeyConfigured=true；未轮换密钥、未改教师 Key、未调用外部模型。见 PROJECT_CORE.md v3.67。

> 2026-09-17 当前本地开发：纸质 PDF 导入方案 A 复用原文件作品、版本、AI 批改与成绩体系，无新表；原批改页承载导入及正式成绩学情。70 项测试、构建、真实只读接口和页面通过，模拟上传/确认通过；真实 PDF 与 AI 全链待验，未发布。见 PROJECT_CORE.md v3.66、`contexts/paper-import/`、BUSINESS_BOUNDARIES.md 和 DATA_MODEL.md。

> 2026-09-16 19:10 修正：当前为原 data Topic 发布/订阅，平台不再判定命令或自动下发。130 已切 `20260916_iot_subscribe_fix_v1`；真实班级账号原 Topic SUBACK 128→0、跨班仍128，28项测试通过。真机及登录后页面待验；见 PROJECT_CORE.md v3.65 和 junior-iot-poc/ADR-004-original-data-subscription.md。以下相冲突的自动下行记录仅为历史。


> 2026-09-14 本地修复：普通课程随机抽题按实际题量校验100分；自动/手动推进默认开放理论和操作题；学生滚动条与打字按钮布局更新。34项相关测试、前端构建及模拟浏览器验证通过，未发布、真实账号/MySQL联调待验。见 PROJECT_CORE.md v3.61、BUSINESS_BOUNDARIES.md、DATA_MODEL.md 与 `contexts/student-experiment-tools/ADR-003-advance-default-open.md`。

> 2026-09-10 统一删除入口本地完成：普通remove允许删除有成绩课程和学生，保留所有权、学校与管理班级校验；五入口提示统一，作品按课程归属清理。尚未发布、未删除真实数据。边界见 `BUSINESS_BOUNDARIES.md`，需求/设计/验收/ADR见 `contexts/unified-deletion-20260910/`；当前状态以 PROJECT_CORE.md v3.55 为准。

> 2026-09-10 本课工具本地修复：网址仅提示、不阻断保存；学生点击仅允许安全HTTP(S)链接。尚未发布，前后端配套与回退约束见 `BUSINESS_BOUNDARIES.md`、`contexts/student-experiment-tools/ADR-002-preserve-omitted-lesson-tools.md` 和 PROJECT_CORE.md v3.54。

> **2026-09-09 当前入口**：学业测评平台运行在 `http://10.52.1.130/`。协作大门为 `http://10.52.1.130:3018` 与 `:3019`（反代 129 CryptPad）。`10.52.1.123:3010` 为导航页，123 后端已停。`xxkj.xsedu.net.cn` 尚未切域名。方案与回滚见 `contexts/PROJECT_CORE.md` 与 `contexts/migration-130-and-fixes-20260909/collab-gateway-130-plan.md`。

> 2026-09-07 当前排查：流程图 AI 建议匹配与恢复存在本地候选修复，未发布；文件版本与流程图提交 ID 必须区分，不能直接 COALESCE。独立证据、待补交关联验证和示例课程访问限制见 `contexts/flowchart-tool/ai-grading-investigation-20260907.md`。

> 2026-09-08 14:30 的 123 Nginx 去套娃事实已被 09-09 入口切换取代：3010 不再提供 Vue3 平台。历史排障仍可参考当天 worker_connections 记录。

> 默认先读 `contexts/PROJECT_CORE.md`。本目录只维护会影响开发决策的当前架构，不复制完整代码、全表字段或历史聊天。

> 2026-09-18 本地学情诊断 MVP：班级 AI 学情分析已增加题干/年级/正式成绩画像/证据覆盖输入与结构化报告页；当前只分析教师选定的课程、班级和操作题，未建立诊断新表或异步任务，未部署正式服务器。

> 协作专题历史（2026-09-06）：当时 123 的 3018/3019 代理刚建立、源码未发完。后续 1.30.5 已在 123 发布；现平台入口已迁 130。不要把这段当当前部署状态。

| 任务类型 | 必读资料 | 需要时继续读 |
| --- | --- | --- |
| 任何开发接管 | `SYSTEM_ARCHITECTURE.md`、`BUSINESS_BOUNDARIES.md` | 对应专题 `contexts/<专题>/` |
| SQL / MyBatis / 数据规则 | `DATA_MODEL.md` | 根目录 `sql/`、相关 Mapper XML |
| CryptPad、Judge0、EMQX/MQTT、实时推送 | `INTEGRATIONS.md` | `contexts/online-collaboration/`、`contexts/python-judge0/`、`contexts/junior-iot-poc/` |
| 小学信息科技实验板正式接入、N17 Python代码生成、EMQX班级精确ACL | `INTEGRATIONS.md`、`DATA_MODEL.md` | `contexts/primary-iot-integration/requirements.md`、`design.md`、`tasks.md`、`ADR-001-reuse-existing-emqx-and-dual-tool-entry.md` |
| Python OJ、统一题库、练习题单、课程自定义运行、班级范围与判题容量 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md`、`INTEGRATIONS.md` | `contexts/python-judge0/requirements.md`、`design.md`、`adr/ADR-008-unified-python-question-bank.md`、`adr/ADR-009-unified-practice-plan-class-version.md`、`adr/ADR-010-hard-delete-and-natural-class-label.md`、`adr/ADR-011-queued-400-submissions-on-shared-host.md`、`adr/ADR-012-course-custom-run-non-scoring.md` |
| 操作题作品、批改统计、删题归档、AI 建议与费用估算 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/operation-artifact-ai-grading/requirements.md`、`design.md`、`tasks.md`、`ADR-006-current-question-answers-and-ai-cost-estimation.md` |
| 画程流程图操作题、草稿、结构检查与教师确认 | `SYSTEM_ARCHITECTURE.md`、`BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/flowchart-tool/requirements.md`、`design.md`、`tasks.md`、四份专题 ADR |
| 教师首页共享课程、课程管理能力、学生端顶部导航 | `BUSINESS_BOUNDARIES.md` | `contexts/teacher-course-sharing/requirements.md`、`design.md`、`tasks.md`、`ADR-001-server-authoritative-course-capabilities.md` |
| 通用班级分组、组长、教师端学生桌面、协作小组路由 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md`、`INTEGRATIONS.md` | `contexts/class-grouping-and-desktop/requirements.md`、`design.md`、`tasks.md`、`ADR-001-server-observed-presence-and-class-entry.md`、`contexts/online-collaboration/` |
| 2026-09-02 多功能改造总计划、P0-P1 本地状态、优先级与开发提示词 | 各相关架构文档、`ADR/ADR-001-versioned-classroom-task-state.md` | `contexts/multi-feature-upgrade-20260902/requirements.md`、`design.md`、`tasks.md`、`development-prompts.md`、`handoff-prompt.md` |
| 学生批量导入性能、事务一致性、诊断分级与重复删除幂等 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/student-import-diagnosis-governance/requirements.md`、`design.md`、`tasks.md`、`ADR-001-batched-transactional-import-and-dynamic-diagnosis.md` |
| 内网发布、服务排障 | `DEPLOYMENT_RUNBOOK.md` | `AGENTS.md`、当前外置配置和服务器实际探查 |

| 架构级取舍 | `ADR/README.md` | 相关专题 ADR 和历史证据 |
| 学生实验工具、题目开放开关 | `DATA_MODEL.md` | `contexts/student-experiment-tools/requirements.md`、`design.md`、`tasks.md` |
| 站点运维与信息展示（129 监控、平台更新、Agent 词典、帮助中心配置、版权） | `INTEGRATIONS.md`、`DEPLOYMENT_RUNBOOK.md` | `contexts/site-ops-and-info/requirements.md`、`design.md`、`tasks.md`、`contexts/RELEASE_LOG.md` |

> 2026-08-23 新增专题：学生实验工具与题目开放开关（见 `contexts/student-experiment-tools/`）；站点运维与信息展示（见 `contexts/site-ops-and-info/`）。2026-08-24 已补充课程多 Python 题、开放状态保存、协作首次保存、课程自定义运行和物联详情弹窗的当前边界。2026-08-25 已补充操作题当前题目统计、删题前答案归档和 AI 理论费用估算边界。2026-08-27 已补充共享课程服务端能力投影、删除校验顺序和学生端导航布局边界。2026-08-31 画程流程图操作题 v1.27.0、教师工具网关 v1.27.1、小学实验板标准 MQTT v1.27.2 均已完成正式发布；小学物联真实课堂试点仍按专题任务跟踪。2026-09-01 学生导入与诊断治理已随 v1.27.8 正式发布，正式库联合索引已迁移并保留独立回滚脚本。

## 2026-09-21 人工批改刷新修复（本机未发布）

提交并下一位与自动刷新竞态已在本机修复，9项测试、生产构建及模拟浏览器通过；真实作品/成绩变化提示重载，后端冲突保护保留。无需SQL或后端重启，仅前端待发布；390px既有三栏拥挤未处理。见`BUSINESS_BOUNDARIES.md`、操作题专题需求/设计/tasks和PROJECT_CORE v3.91；其他任务状态保留。

## 文档维护规则

- 当前代码和可复核运行证据优先于文档。
- 重大变更必须更新受影响文档；小改不为了形式制造架构文档噪音。
- 图中的凭据使用“环境变量/私密配置”占位，绝不写真实值。
- 历史发布与排障证据仍在 `contexts/context.md`，按关键词读取，不作为默认上下文。

## 2026-09-07 在线统计与登录恢复候选

近5分钟活跃账号、部门筛选、登录防重复与发布边界见 `contexts/online-recovery-20260907/requirements.md`、`design.md`、`tasks.md`。本地已验证，等待用户重启服务器后部署；不得视为已上线。


2026-09-14：130 MQTT 管理配置已补齐，用户禁止上课期间重启，当前仍待生效；现场 API 与授权源正常。最新事实见 PROJECT_CORE.md v3.59。


2026-09-14 后续状态：用户授权后已仅重启 xueyeceping-130，PID168090，首页/API/代理均200，EMQX管理API及授权源正常。外置配置已随启动加载，取代此前待重启状态；失败班级重试同步与硬件收数仍待验证。未打包部署或Git推送。见 PROJECT_CORE.md v3.60。
# 2026-09-20 AI 批改排查补充

正式课程577/题4059的qwen3.6-flash任务42/43共4/64份评分项缺失，已核验线上解析与worker字节一致；单项标准下对应空评分数组，未自动纠正、失败诊断元数据未保留。本轮只读排查，未修复或发布。证据与建议见 `../../contexts/operation-artifact-ai-grading/investigation-20260920.md`，其他当前状态见PROJECT_CORE。
