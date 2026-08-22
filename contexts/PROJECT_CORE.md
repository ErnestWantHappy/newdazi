# 信息科技学业测评平台：当前核心事实

> 版本：v2.2
> 更新：2026-08-22
> 用途：新的 Codex、Claude、Gemini 或人工开发者的默认入口。只记录当前仍有效且已验证的事实；历史发布和排障证据见 `contexts/context.md`。

## 1. 先读什么

1. `AGENTS.md`：操作边界、验收顺序、远程授权和凭据规则。
2. 本文件：项目当前状态与不可违反的边界。
3. `docs/architecture/INDEX.md`：按任务进入相应架构文档和专题。
4. 验收报告集：`output/acceptance/01_项目现状简报.md` ~ `10_发布决策与上线结论.md`。
5. 仅在任务相关时读 `contexts/<专题>/`；与本文件冲突时，先核对代码、数据库或部署证据。

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
| Judge0 | Python 判题由后端通过私有 HTTP 调用（10.52.1.129:2358，携带 X-Judge0-Token 认证）；浏览器无 Judge0 地址或令牌。AC/WA/CE/TLE 与 16 任务排队已全量实测通过。 | 生产异步提交+轮询机制稳健。 |
| CryptPad | 已采用 CryptPad Integration API，部署在 10.52.1.129:80；已完成单房间 50 人同编与 400 人×8 房间并发会话测试（100% 成功，平均时延 85.9ms）。2026-08-21 已修复 OnlyOffice 参与人身份显示（inner.js 服务器补丁：优先取成员表名字而非本地用户），编辑器新增在线成员列表与版本历史；「版本已变化」频繁提示由 15 秒自动保存携带旧版本号引起，已随本次发布修复。 | 当前内网 HTTP/WS 临时验收态已通过；HTTPS/WSS 不在本轮范围。 |
| EMQX / MQTT | 方案 A：EMQX 5.8.8 已正式启用（10.52.1.129:1883 设备接入；管理 API 18083 已开放给 10.52.1.123 后端）。平台接收器以 `platform_iot_subscriber` 订阅 `county/#`；ACL 文件规则已生效（订阅账号只读订阅、`class_*` 设备收发、deny all）。课程级物联开关（`biz_lesson.iot_enabled`）已上线：仅教师在设计器开启的课程，教师首页课程卡片与学生首页才显示物联入口。 | 真实机房多班级并发实验待实测；EMQX 管理凭据与口令密钥均由 NSSM 环境变量注入。 |
| WebSocket | 导学单、课堂和 IoT 都使用服务端 WebSocket；连接路径和鉴权见 `docs/architecture/INTEGRATIONS.md`。 | 改实时功能必须验证鉴权、断线和跨班隔离。 |
| LibreOffice | 用于 Office/PDF 转换和操作题预览，存在独立进程池、队列、健康巡检和并发参数。 | 生产参数与机器资源耦合，改配置必须按专项验收。 |

## 5. 环境与发布

- 本地：后端 `8080`，Vue3 Vite 默认 `80` 并代理后端。
- 正式平台：内网主机 `10.52.1.123`，后端 `3009`、Vue3/Nginx `3010`；当前后端与前端均为 `releases/20260822_181500_python_practice_polish_v3`。
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


**最新制品（2026-08-22 晚）**：jar 与 dist 已重新构建（含 upsertAbsent、高中夹具回归后的最终代码）；发布前以 `sha256sum RuoYi-Vue/ruoyi-admin/target/ruoyi-admin.jar RuoYi-Vue3/dist/index.html` 现算为准。
**发布状态**：这些本地改动已包含在 `20260822_181500_python_practice_polish_v3` 的最终 clean package 和前端生产构建中并完成 release 切换；Python 专项生产冒烟通过。该节所列诊断、改分和高中场景仍应补做各自的生产专项回归，不能仅以本次 Python 页面冒烟替代。回滚：切回上一 release jar + 前端目录。

**剩余风险**：①129 SSH 凭据未恢复，129 磁盘/GPU/负载仍不可见；②A-2 幂等采用「拒绝重复」而非唯一约束，极端并发窗口仍在；③教研资源全区共享语义仍未文档化（C-2）。
