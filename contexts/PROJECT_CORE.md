# 信息科技学业测评平台：当前核心事实

> 版本：v2.9
> 更新：2026-08-24
> 用途：新的 Codex、Claude、Gemini 或人工开发者的默认入口。只记录当前仍有效且已验证的事实；历史发布和排障证据见 `contexts/context.md`。

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
- 正式平台：内网主机 `10.52.1.123`，后端 `3009`、Vue3/Nginx `3010`；当前后端与前端均为 `releases/20260824_course_designer_python_input_v1`（v1.26.2，第 19 节）。
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
