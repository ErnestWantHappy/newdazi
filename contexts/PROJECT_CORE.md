# 信息科技学业测评平台：当前核心事实

> 版本：v2.0
> 更新：2026-08-21
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
- 正式平台：内网主机 `10.52.1.123`，后端 `3009`（release `20260821_iot_course_switch_v1`）、Vue3/Nginx `3010`。
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
