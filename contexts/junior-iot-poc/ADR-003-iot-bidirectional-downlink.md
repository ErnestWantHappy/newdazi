# ADR-003：AIoT 双向通信（设备→平台→AI→平台→设备）本地实现

> 2026-09-16 19:10 修正：当前为原 data Topic 发布/订阅，平台不再判定命令或自动下发。130 已切 `20260916_iot_subscribe_fix_v1`；真实班级账号原 Topic SUBACK 128→0、跨班仍128，28项测试通过。真机及登录后页面待验；见 PROJECT_CORE.md v3.65 和 junior-iot-poc/ADR-004-original-data-subscription.md。以下相冲突的自动下行记录仅为历史。


> 日期：2026-09-16
> 状态：**已发布到 130 并验证（2026-09-16 18:22 CST）**：后端 jar `38a78fbf…869f1b` 就位、前端 dist 更新、`IOT_DOWNLINK_ENABLED=true` 生效、EMQX 两步 ACL 已写入并经真链路探针证明下行贯通。
> 前置证据：`bidirectional-downlink-analysis.md`（只读核查，定位 `MQTTException: 128` 根因）。

## 1. 背景与问题

掌控板在 Mind+（MicroPython / SIoT 平台）下：

- `publish` 到 `county/{dept}/{lesson}/{class}/{act}/{group}/data` → 成功，平台落库并实时显示；
- `mqtt.getsubscribe(...)` → `MQTTException: 128`。

根因（已由固件源码 + `IotEmqxAdapter` 源码双向确认）：EMQX 授权源 `built_in_database` 默认拒绝，而平台给班级账号只写了 **1 条** `publish allow county/{dept}/{lesson}/{class}/#` 规则，**没有 subscribe 规则**，因此 SUBACK 返回 `0x80`（十进制 128），设备端 siot 库抛异常。

同时，全仓搜索确认当时**不存在任何下行**：后端唯一 MQTT 客户端只 connect + subscribe，无 `publish` 调用。

## 2. 决策

### 2.1 主题约定：同前缀换末段

```text
上行（设备→平台）  county/{dept}/{lesson}/{class}/{act}/{group}/data
下行（平台→设备）  county/{dept}/{lesson}/{class}/{act}/{group}/control
```

- 末段由 `iot.mqtt.downlink-segment` 控制，默认 `control`，可配置调整而不改代码。
- 下行主题一律**由该组已入库的上行主题换末段推导**（`IotExperimentService.controlTopicOfTopic` / `controlTopicOfGroup`），不按字段重算。理由：`biz_iot_group.topic` 是设备真正在用的主题，若按字段重算出现前缀漂移，会产生「平台发到 A、设备订阅 B」的静默失效。`buildGroupControlTopic` 仅作兜底。

### 2.2 ACL：班级账号两条规则，平台账号收发配对

- 班级账号（`syncClassAcl`，**覆盖写**）：
  - `publish allow` → `county/{dept}/{lesson}/{classId}/+/+/data`
  - `subscribe allow` → `county/{dept}/{lesson}/{classId}/+/+/control`
- 平台订阅账号（`syncDownlinkPublisherAcl`，仅在 `downlink-enabled=true` 时调用）：
  - `subscribe allow` → `county/#`
  - `publish allow` → `county/+/+/+/+/+/control`

注意：EMQX v5 用户规则是**整组覆盖写**，新增 publish 时若丢掉 subscribe，平台会立刻收不到任何上报，因此两条规则必须同写。

### 2.3 下行链路解耦：Spring 事件而非直接依赖

`IotMqttReceiver`（接收）与 `IotDownlinkService`（下发）互相需要对方的 MQTT 客户端能力。为绕开循环依赖：

```
IotMqttReceiver.receiveInternal()
  → 落库 + 记 MESSAGE_RECEIVED
  → eventPublisher.publishEvent(IotMessageReceivedEvent)
       └─ @Async @EventListener IotDownlinkService.onMessageReceived()
            → 判定 → receiver.publish(controlTopic, payload, qos1)
  → websocketHandler.publishRefresh()   （判定不阻塞页面刷新）
```

`ApplicationEventPublisher` 用 `@Autowired(required=false)`，使单元测试直接 `new IotMqttReceiver()` 时不注入容器也能跑。

### 2.4 判定策略：AI 优先，关键词兜底

- AI：复用 `AiChatGateway.chat(prompt, maxTokens, timeoutMillis)`（deepseek / doubao / qwen / zhipu），Prompt 要求只输出 `ON` / `OFF` / `HOLD`。
- 快速模型可能答非所问，`parseDecision` 无法识别时返回 `null`，**自动降级**到 `decideByKeywords`。
- 关键词判定**先看否定词方向**：`不要关灯 → ON`、`不要开灯 → OFF`。
- 判定结果用不可变内部类 `Decision`（值 + 依据）承载，避免多组并发时共享字段串到别组回显。

### 2.5 安全默认：开关关闭

`iot.mqtt.downlink-enabled` 默认 `false`：

- 关闭时 `onMessageReceived` 直接 return，不判定、不发布；
- `syncClassBroker` 也不会去改平台账号 ACL；
- 因此把这份代码发到正在上课的服务器上，**行为与现在完全一致**，直到显式打开开关。

### 2.6 载荷格式

```json
{"ai":"ON","reason":"AI 判定","text":"请开灯","source":"platform","ts":1758000000000}
```

`text` 截断 120 字，`reason` 截断 40 字，整体控制在 200 字节内——设备端 siot 订阅缓冲区有限。

## 3. 受影响文件

| 层 | 文件 | 变更 |
| :--- | :--- | :--- |
| 配置 | `IotMqttProperties` | 新增 5 个下行字段（默认关闭） |
| 配置 | `application.yml` | 新增 `IOT_DOWNLINK_*` 环境变量映射 |
| ACL | `IotEmqxAdapter` | `syncClassAcl` 改双规则；新增 `syncDownlinkPublisherAcl` |
| 接收 | `IotMqttReceiver` | 新增 `publish()`；上行只认 `/data`，`/control` 记 `UPLINK_TOPIC_REJECTED`；发事件 |
| 事件 | `domain/event/IotMessageReceivedEvent` | 新增 |
| 下发 | `IotDownlinkService` | 新增：自动判定 + 教师手动下发 + 事件记账 |
| 服务 | `IotExperimentService` | 新增 `buildGroupControlTopic` / `controlTopicOfTopic` / `controlTopicOfGroup` / `requireManageableGroup`；`syncClassBroker` 内可选同步平台 ACL |
| 接口 | `IotExperimentController` | 新增 `POST /business/iot/groups/{groupId}/downlink` |
| 接口 | `IotClassCardVo` / `IotStudentOverviewVo` | 新增 `controlTopic` 字段 |
| DTO | `IotDownlinkRequest` | 新增（`text` 走判定 / `command` 强制指定） |
| 前端 | `api/business/iot.js` | 新增 `sendIotDownlink` |
| 前端 | `views/business/iot/index.vue` | 操作列「下发」、详情面板「AI 判定并下发」、下发弹窗、配置卡显示订阅 Topic |
| 前端 | `views/student/iot.vue` | 展示「我的专属订阅 Topic」+订阅说明；复制文本含订阅主题 |
| 前端 | `utils/iotPythonTemplate.js` | 有下行 Topic 时生成 `subscribe_control` + `check_msg` 轮询 |
| 测试 | `IotDownlinkDecisionTest`（新）、`IotEmqxAdapterTest`、`IotMqttReceiverTest`、`IotExperimentServiceAccessTest` | 覆盖双规则 ACL、`/control` 拒收、判定兜底、主题推导 |

## 4. 已验证（2026-09-16 本机）

- `mvn -pl ruoyi-business -am -DskipTests compile` → BUILD SUCCESS。
- `mvn -pl ruoyi-business -am test -Dtest=Iot*Test` → **33 项全通过，BUILD SUCCESS**（含新增 `IotDownlinkDecisionTest` 7 项）。
  - 注意：本机 surefire fork JVM 启动失败，须加 `-DforkCount=0` 才能在进程内跑通，属环境问题不是用例问题。
- `npm run build:prod` → **built in 1m 29s，退出码 0**（仅既有 manualChunks 提示）。
- Python 模板双分支渲染目视核对：有下行 Topic 时生成 `subscribe_control` + `check_msg`，无下行 Topic 时不生成任何订阅代码。

## 5. 未验证 / 上线前必做

1. ~~**EMQX 侧 ACL 与平台账号发布权限**~~ → **已完成（2026-09-16 18:10–18:18）**。详见第 7 节。
2. **真实硬件端到端**：掌控板订阅 → 平台下发 → 灯变色，**仍未在真机跑过**。设备侧必须烧录带 `subscribe_control` + `check_msg` 的新模板（`output/iot-downlink-20260916/experiment-board-code-with-control.py`）后才有意义；已烧旧固件的设备不会订阅 control，平台下发无人接收（无害）。
3. **AI 网关**：130 的进程环境里**没有** `GUIDE_SHEET_AI_API_KEY`，`ServerAiChatGateway.isConfigured()` 为假 → 当前下行判定**走关键词兜底**（开灯/关灯/不动作），符合"课堂可靠性优先"设计。若要用 AI 判定需另行注入该环境变量。
4. **同班跨组订阅**：班级账号为全班共用（`mqttUsername = class_<dept>_<entryYear>_<classCode>`），Broker 只能约束到班级前缀，**同班 A 组可以订阅 B 组的 control 主题**。平台侧 `requireManageableGroup` 只校验教师权限、不校验设备身份。课堂场景可接受，若需强隔离须改为「每组独立账号」，属另一次决策。

## 6. 回滚

代码回滚即可恢复纯上行行为；若已打开 `downlink-enabled`，需同时把开关改回 `false` 并重启，班级账号 ACL 会退回单条 publish 规则（下一次同步时）。下行主题与上行主题同前缀，无 SQL、无表结构变更。

**2026-09-16 本次发布的精确回滚**：

```bash
# 1) 后端 jar 回滚（备份 SHA-256 = b5efdf26…25d9eb，即"她的补丁包"）
sudo cp -a /data/backups/xueyeceping/20260916_182118_aiot_downlink/ruoyi-admin.jar.bak \
           /data/apps/xueyeceping/releases/20260914_lesson_fixes_v1/backend/ruoyi-admin.jar
# 2) 前端回滚（备份 SHA-256 = 2e665f1c…1114b）
sudo bash -c 'cd /data/apps/xueyeceping/releases/20260914_lesson_fixes_v1/frontend && \
  rm -rf static index.html index.html.gz loader.css loader.css.gz && \
  tar xzf /data/backups/xueyeceping/20260916_182118_aiot_downlink/frontend-before.tar.gz'
# 3) 关掉下行开关（env 备份 SHA-256 = 1ef172d3…8335d）
sudo sed -i 's/^IOT_DOWNLINK_ENABLED=.*/IOT_DOWNLINK_ENABLED=false/' \
  /data/apps/xueyeceping/shared/xueyeceping-130.env
sudo systemctl restart xueyeceping-130
```

EMQX ACL 回滚：删掉 14 个班级账号的 `subscribe` 规则、删掉 `platform_iot_subscriber` 的用户规则条目即可。写前原始规则已落盘 `output/iot-downlink-20260916/emqx_rules_backup_before_acl.json`（14 个班级账号均为单条 `publish allow county/{dept}/{lesson}/{class}/#`）与 `emqx_rules_backup_step2.json`。

## 7. 发布记录（2026-09-16，130 正式环境）

### 7.1 关键前情：另一位 AI 已先发布过一版

发布前核查发现，130 当天 17:06 重启后所跑的 jar（`b5efdf26…25d9eb`，107,832,566 B）**不是** 09-14 原版，而是另一位 AI 当天的产物：她以 09-14 制品为 base，**只把编译好的 2 个类注入** `BOOT-INF/lib/ruoyi-business-3.9.0.jar`（`integration/platform/build_patch.py`，脚本内含 `assert changed == [business]` 自校验），用于修 `/prod-api/aiot-classroom/teacher|student/context` 的 404。她的构建证据 `build-evidence.json` 记录 `base_sha256=f42701b8…`、`output_sha256=b5efdf26…`。

**风险点**：她 13:45:17 又改了一次 `AiotClassroomService.java`（16381 B，`46d72102…`，含 3 处授权加固），而平台仓库工作区里那份停在 12:19:27（15332 B，`8a9bcd34…`）。若直接全量重编译发布，会**把她的授权加固盖回旧版**。

**处置**：把她的 13:45 版同步进平台工作区（旧版备份至 `output/iot-downlink-20260916/backup-src/`），再全量重编译。她的三处加固：① 返回体补 `schoolId`；② 学生查组时校验小组属于本班，否则 `小组不属于当前学生班级`；③ 独立接口必须验证教师授课范围（`selectTeacherClassListWithCount`），否则 `不在当前教师授课范围内`。

### 7.2 制品一致性证明

以线上那份（她的补丁包）为基准逐类比对 `ruoyi-business`：

- **REMOVED = 0**（什么都没丢）；
- ADDED = `IotDownlinkService` + `$Decision`、`IotDownlinkRequest`、`IotMessageReceivedEvent`；
- CHANGED = 本次 IoT 各类 + `AiotClassroom*` + `BizLessonServiceImpl`；
- 差异性质：关键类的 `only_in_ref` 仅为 `MethodParameters`（她用 `javac -parameters`，maven 不带），反向多出的是 `LocalVariableTable`/泛型签名等**编译元数据**，业务符号集一致；
- 内容硬校验：新制品 `AiotClassroomService.class` 常量池含 `小组不属于当前学生班级`、`不在当前教师授课范围内`、`schoolId`、`selectTeacherClassListWithCount` 四项标记，全部命中。

### 7.3 EMQX 两步（本轮授权的"两步"）

| 步骤 | 内容 | 结果 |
| :--- | :--- | :--- |
| 1 | 14 个班级账号补 `subscribe allow county/{dept}/{lesson}/{class}/+/+/control` | 写前预检 `GET=200`，14 次 `PUT` 全 **204**，回读确认 |
| 2 | `platform_iot_subscriber` 建用户规则：`subscribe county/#` + `publish county/+/+/+/+/+/control` | 写前无任何规则 → `PUT` **204**，回读语义一致 |

**采用"加法"而非"窄化"**：班级账号原有 `publish allow …/{class}/#` **原样保留**，只追加 subscribe。原因是覆盖性校验发现 **102 个 `biz_iot_group.topic` 里有 24 个的 base 不在现有 ACL 集合内**（例：存在 `county/169/274/2024-01/...`，而 ACL 里只有 `county/169/274/2024-02`）。若按新代码把 publish 窄化为 `.../+/+/data`，这 24 条上行会被拒。窄化应交给平台自身在教师「重试同步」时按当前课程执行（届时 ACL 会与课程对齐，反而修正这个漂移）。

另注：`platform_iot_subscriber` 原先**不在** `built_in_database` 里，其订阅权来自 file 源（`{allow,{username,"platform_iot_subscriber"},subscribe,["county/#"]}`）。EMQX v5 用户规则是覆盖写，所以第 2 步必须"订阅+发布"同写，否则会弄丢接收链。

### 7.4 真链路端到端探针（决定性证据）

用纯标准库手写 MQTT 3.1.1 报文（不依赖 paho），建临时探针账号 `acl_probe_class`（规则形状与真实班级账号一致，仅前缀换成 `county/999/999/probe-01`）：

| 验证项 | 结果 | 含义 |
| :--- | :--- | :--- |
| 探针 CONNACK | `rc=0` | 连接正常 |
| 订阅 `.../exp/group01/control` 的 SUBACK | `[0]`，granted | **128/0x80 消失** |
| 越权订阅 `county/888/...` 的 SUBACK | `[128]`，拒绝 | **规则仍有约束力**，不是一刀放开 |
| 平台账号 CONNACK | `rc=0` | — |
| 平台发布 → 探针收到 | 收到同一 topic 与 payload | **完整下行链路贯通** |
| 平台越权发布 → 探针 | 未收到 | 平台发布同样受约束 |

探针账号与规则验证后已删除（各 204），账号清单复核无残留。

### 7.5 发布动作与核验

- 制品：`mvn -pl ruoyi-admin -am clean package -DskipTests` → **BUILD SUCCESS**，jar `38a78fbf…869f1b`（107,842,402 B）；前端 `RuoYi-Vue3/dist` 本地已构建（17:00:46）。
- 就地替换当前 release（未新建 release、未改 systemd/nginx 指向）：`releases/20260914_lesson_fixes_v1/backend/ruoyi-admin.jar` 与 `frontend/`。
- 前端用**叠加解包**（tar 覆盖到现有目录）而非先删后解，避免 404 窗口；旧 hash 分片保留（无害，目录 23M / 974 文件）。
- `IOT_DOWNLINK_ENABLED=true` 追加进 `shared/xueyeceping-130.env`；重启 `xueyeceping-130`，探活 4 次 000 → **第 5 次 200**。
- 核验：线上 jar SHA = `38a78fbf…`；进程 `/proc/<pid>/environ` 可见 `IOT_DOWNLINK_ENABLED=true`；服务 `xueyeceping-130`/`nginx`/`aiot-lamp` 均 active；`/`、`/prod-api/captchaImage`、`/prod-api/aiot-classroom/teacher/context`（她的功能）、`/aiot-lamp/`（她的工具）**均 200**；线上 `frontend/index.html` SHA `1c44fc04…` 与本地 dist 完全一致，入口 JS 已切到新的 `index-B5OhbLwc.js` 且 200。
- 单测：`-Dtest=Iot*Test -DforkCount=0` → **33 项 0 失败 BUILD SUCCESS**（`IotDownlinkDecisionTest` 7、`IotEmqxAdapterTest` 4、`IotMqttReceiverTest` 8、`IotExperimentServiceAccessTest` 6 等）。
- 备份（可 1 分钟回滚）：`/data/backups/xueyeceping/20260916_182118_aiot_downlink/{ruoyi-admin.jar.bak,frontend-before.tar.gz,xueyeceping-130.env.bak}`。

### 7.6 本次发布的已知遗留

1. **真机未验**：掌控板需重烧带订阅逻辑的模板才能验证"说开灯→灯亮"。
2. **AI 判定未启用**：无 `GUIDE_SHEET_AI_API_KEY`，当前为关键词判定。
3. **班级 ACL 仍宽**：14 个班级账号的 `publish …/#` 未窄化（见 7.3 的理由），待教师「重试同步」时由平台代码自然收敛。
4. **`BizLessonServiceImpl.class` 与线上版本字节不同**：已确认差异仅为调试表（线上那份缺 `LocalVariableTable`，`utf8_only_in_ref=[]`，即新版本是旧版本的严格超集），非语义变更。成因疑为该 class 历史上由不同编译参数产出，未进一步追根因。
