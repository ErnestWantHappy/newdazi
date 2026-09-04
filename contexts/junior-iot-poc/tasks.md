# 初中物联网县级 SIoT / EMQX 方案 A 任务与进展

> **2026-08-20 最终路线结论**：用户明确选定“方案 A”，正式 MQTT Broker 使用标准 EMQX (`10.52.1.129:1883`)；原 SIoT (`10.52.1.123:1883`) 保留为回退。同班学生共享班级账号与 6 位易读课堂口令，学号升序自动分组生成持久快照，Topic 业务隔离，投屏配置卡与学生端物联看板已全部开发并通过测试。

## P0：县服务器软件链路（已完成）

- [x] 核对 SIoT 2618 本机包和端口配置。
- [x] 部署 SIoT 2618 到独立 release，注册 `CountySIoTPoc` NSSM 自动服务，保留为回退。
- [x] 配置 1883/18080 私网入站规则。

## P1：硬件与 EMQX 兼容性验证（已通过）

- [x] 掌控板现场连接 EMQX `10.52.1.129:1883` 成功。
- [x] Mind+ SIoT 模块现场连接 EMQX 成功。
- [x] 129 的 EMQX 容器、systemd 自启动、1883 监听与防火墙规则验证通过。

## P2：方案 A 全链路开发与验证（已全部完成）

- [x] **数据库设计与迁移**：
  - 编写 `sql/iot_class_grouping_v2.sql`，创建 `biz_iot_class_config`（班级账号、口令密文与版本）、`biz_iot_group_student`（分组关系快照），扩展 `biz_iot_group` 字段。
  - 本地库 `xueyeceping_server_20260729` 迁移执行成功，7 张物联表核验完毕。
- [x] **工具类与安全**：
  - 编写 `IotPasscodeUtil.java`：6 位易读口令生成（排除 0/O/1/I/L）、AES-256-GCM 对称加解密、BCrypt 哈希、学号自然升序比较器。
- [x] **EMQX 适配器与平台接收器**：
  - 编写 `IotEmqxAdapter.java`：通过 EMQX v5 内置数据库 API 同步班级账号与 Topic 前缀 ACL，支持断网与无配置降级。
  - 增强 `IotMqttReceiver.java`：支持设备级和小组级 Topic 自动入库与 WebSocket 实时广播。
- [x] **业务逻辑与控制器**：
  - 实现自动读已指派班级、按学号自然升序连续分组、生成关系快照、口令安全轮换、配置卡生成。
  - 增加 `@studentSs.isStudent()` 学生端概览接口，严密校验学校/班级/学生边界。
- [x] **前端教师端**：
  - 自动读取课程已指派班级，支持设置每组人数并生成/重新生成分组（带二次确认警示）。
  - 显示班级 MQTT 账号、6 位课堂口令、一键轮换口令按钮。
  - 课堂物联配置卡弹窗（支持投屏与打印），展示各组 Topic 与组员名单。
  - 小组列表、实时消息流与诊断时间轴展示。
- [x] **前端学生端**：
  - 在 `/student/iot` 打造物联看板，顶部增加快速入口。
  - 展示本班 MQTT 服务器、端口、账号、6 位口令、所属小组、专属 Topic、同组成员与最近一条数据。
  - 提供“一键复制 Mind+ 配置”快捷按钮与醒目业务隔离提示。
- [x] **测试与构建**：
  - 编写并运行 `IotPasscodeUtilTest`、`IotClassGroupingTest`、`IotMqttReceiverTest` 等后端单元测试（324 项全通）。
  - 后端 fat jar clean package 成功。
  - 前端 Vue3 `npm run build:prod` 生产构建成功。

## P3：多校试点与上线部署（已完成正式上线）

- [x] 在正式服务器 `10.52.1.123` 执行增量 SQL、发布新版本 `20260820_iot_scheme_a_v1` 并探活（HTTP 200）。
- [x] 生产环境真实教师/学生账号端到端业务链路验证（分组、配置卡、口令轮换、学生专属看板）全部通过。
- [ ] 开展真实机房多班级、多小组并发物联教学实验。


## P4：课程级物联开关与教师数据收集（2026-08-21 已发布，release `20260821_iot_course_switch_v1`）

- [x] 物联入口改为课程级开关：`sql/iot_course_switch_v1.sql` 给 `biz_lesson` 加 `iot_enabled` 并按已有实验回填（生产后检 3 门课开启、一致性 0）；教师课程设计器新增「开启物联网」开关（考勤课强制关闭）；教师首页课程卡片底部入口条与学生首页按钮均按开关显示，学生页带 `lessonId` 进入。
- [x] 教师物联页新增「学生数据收集」卡：小组统计表（消息数/最近上报）、消息分页列表（时间/小组/设备/类型/载荷/主题）、payloadType 与关键词过滤；接口 `GET /business/iot/experiments/{experimentId}/messages`（教师按负责班级范围、管理员/教研员全量）。
- [x] 生产启用数据链路：EMQX 5.8.8 管理 API 18083 由仅本机改为开放给内网（容器同镜像同挂载重建，旧容器 `school-emqx-poc-bak-20260821` 留作回滚）；ACL 替换为订阅账号只读订阅 county/# + `class_*` 设备收发 + deny all（备份 `acl.conf.bak-20260821`）；新建 `platform_iot_subscriber` 订阅账号与 `dazi-backend` API 密钥（凭据见 secrets.local.md）；后端 NSSM 环境变量注入 `IOT_MQTT_ENABLED=true`、订阅账号密码、`IOT_PASSCODE_SECRET`（与历史默认值一致以兼容存量密文）与 EMQX API 密钥，重启后日志确认「物联网 MQTT 接收器已连接 broker=tcp://10.52.1.129:1883 subscription=county/#」。
- [x] 生产 API 验证：学生 current-lesson `iotEnabled=true`；教师课程详情 252 `iotEnabled=True`；实验 1 消息接口 200（共 0 条）。
- [ ] 待真实机房设备经 Mind+ 上报后验证：小组统计、消息分页、实时消息流与教师收集页数据联动。

## P5：数据链路修复与教师/学生端页面重构（2026-08-23 已发布，release `20260823_iot_frontend_v1`，v1.25.2）

- [x] 「教师端收不到数据」根因修复：`biz_iot_message.device_id` 外键+哨兵值 0+空设备表致每条消息外键冲突，paho 回调异常引发断连重连死循环。删外键（本机+正式库）+ `IotMqttReceiver` 异常隔离与断线自动重连重订阅（`MESSAGE_PROCESS_FAILED`/`BROKER_RECONNECTED` 事件）。
- [x] 教师端物联页重构：合并冗余区块为「小组数据总览」大表格（每组持续显示最新数据/条数/最近接收时间），小组详情含全量历史明细与格式/关键词筛选；诊断事件区块页面移除、后台保留。
- [x] 学生端物联页：新增「本组历史数据」分页列表（新接口 `GET /business/iot/student/messages`，20 秒静默轮询）；最新数据样式美化；复制按钮改用 `utils/clipboard.js` 兜底方案，修复内网 HTTP 环境「无法复制」。
- [x] 生产冒烟：实验 4 班级账号发布 TEXT/NUMBER/JSON 三条测试消息全部正确落库后清理归位（基线 110 条）；平台订阅客户端在线、真实课堂设备已接入；3010 新前端关键 chunk 可访问。
- [x] 遗留处置：2026-08-21 之前创建的三个班级配置（实验 1/2/3）曾未同步进 EMQX 认证库（设备连接被拒 rc=5），已经管理 API 补注册完成并复测连接成功；新页面布局仍建议真实教师/学生账号复测。

## P6：教师端入口与详情体验（2026-08-24 已发布，release `20260824_python_iot_ux_v1`）

- [x] 首页点击物联先选择班级，并将届别/班号带入工作台。
- [x] 总览增加只看有数据、接收数量排序、两分钟实时状态和离线文案。
- [x] 小组明细改弹窗，统一 TEXT/NUMBER/JSON 展示，移除恒值来源列。
- [x] Vue3 生产构建、正式教师 IoT 班级/实验接口和 3010 新静态资源均验收通过；真实有消息班级的弹窗视觉效果留待课堂抽查。
