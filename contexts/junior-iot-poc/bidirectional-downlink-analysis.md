# 初中掌控板双向 MQTT（AIoT 下行）只读核查报告

> 日期：2026-09-16
> 范围：**只读核查**。未修改任何代码、未连接/重启服务器、未执行 SQL、未改 EMQX 配置。
> 触发问题：掌控板 publish 到 `county/169/274/2024-02/guangzhao/group02/data` 成功、平台能收库，但设备 `mqtt.getsubscribe(...)` 报 `MQTTException: 128`。
>
> **后续状态（2026-09-16 同日）**：本报告的改造方案已在**本地代码**落地并通过编译/单测/前端构建，正式环境仍未改。
> 实现决策、受影响文件、验证证据与上线前必做项见 `ADR-003-iot-bidirectional-downlink.md`。下方结论仍为只读核查时的原始事实，保留作为根因证据。

---

## 0. 结论速览

| 问题 | 结论 |
| :--- | :--- |
| 128 来自哪里 | **EMQX 授权拒绝订阅**，SUBACK 返回 `0x80`，设备端 siot 库把它抛成 `MQTTException(128)` |
| 是不是平台 Java 代码的 bug | 不是 bug，是**当前设计使然**：平台给班级账号只写了 1 条 `publish allow` 规则，没有 subscribe 规则 |
| 有没有下行 Topic | **没有**。全仓搜索 `/control`、`/cmd`、`/command`、`/reply`、`/result`、`downlink`、`下行` → 0 命中 |
| 平台会不会 publish | **不会**。全后端只有 1 个 MQTT 客户端，只做 connect + subscribe，没有任何 publish 调用 |
| 能否不改服务器代码就实现双向 | 否。设备侧（ACL + 平台发布权限）必须动 EMQX 配置；平台侧必须加发布代码 |

---

## 1. 当前 MQTT 完整链路（含实测证据）

```
[掌控板 mpython v2.4.9]
  WiFi 连接 192.168.1.x  ✅ 实测通过
  from siot import iot  (Mind+ 平台=SIoT + 主板=esp32 时自动生成)
  iot("<Mind+自动生成clientId>", "10.52.1.129", user="class_169_2024_02", password="T6PCUQ")
        │  port 默认 1883（siot.py: port==0 → 1883）
        ▼
[EMQX 5.8.8 @ 10.52.1.129:1883]  容器 school-emqx-poc
        │ 认证：built_in_database（账号 class_169_2024_02）
        │ 授权：built_in_database → file，未命中默认拒绝
        ├── publish county/169/274/2024-02/...#     ✅ allow（唯一一条规则）
        └── subscribe 任意 county/...                 ❌ 未命中 → SUBACK 0x80 → 128
        ▼
[平台后端 IotMqttReceiver @ 10.52.1.123]
  账号 platform_iot_subscriber，订阅 county/#
  receiveInternal() → biz_iot_message 落库 + biz_iot_event + WebSocket 刷新
        ▼
[教师物联页 / 学生页]  ✅ 实测能看到 hello、AI建议开灯
```

**关键点：设备 publish 成功而 subscribe 失败，不是网络层差异，而是授权层差异。** EMQX 对 publish 拒绝在 MQTT 3.1.1 下往往是静默丢弃或断连（本项目 2026-08-31 记录：跨班发布用 MQTT 5 才返回 `Not authorized`），而订阅拒绝一定会回 SUBACK `0x80`，所以「发得出去、订不回来」是同一套只写不读 ACL 的必然表现。

---

## 2. 关键文件与关键方法（全部为只读定位）

### 2.1 平台后端（RuoYi-Vue）

| 关注点 | 文件 | 方法/行号 |
| :--- | :--- | :--- |
| Broker 连接配置 | `ruoyi-business/.../config/IotMqttProperties.java` | `brokerUrl=tcp://10.52.1.129:1883`（L12）、`subscription=county/#`（L15）、`clientId=dazi-platform-iot`（L16）、EMQX 管理 API 与密钥（L24-27）、班级口令加密密钥（L30） |
| **平台订阅 `county/#`** | `service/IotMqttReceiver.java` | `startIfEnabled()`（L52）→ `mqtt.subscribe(properties.getSubscription(), new Listener())`（L73） |
| 收到消息后落库 | 同上 | `receiveInternal()`（L164）→ `mapper.insertMessage`（L241）+ `touchGroup`（L246）+ `insertEvent`（L287）+ `websocketHandler.publishRefresh`（L249） |
| Topic → 小组/设备映射 | 同上 | `selectDeviceByTopic`（L187）/ `selectGroupByTopic`（L196） |
| **Topic 生成** | `service/IotExperimentService.java` | `buildClassTopicPrefix()`（L830，`county/{deptId}/{lessonId}/{entryYear-class}/#`）、`buildGroupTopic()`（L837，`.../{activityCode}/{groupCode}/data`）、`buildPrimaryClientId()`（L825） |
| **账号 + ACL 下发** | `service/IotEmqxAdapter.java` | `syncClassAccount()`（L36，建号/改密）、**`syncClassAcl()`（L97-142）**、`isBuiltInAuthorizationReady()`（L148）、`disconnectClientsByUsername()`（L192）、`revokeClassAccount()`（L228） |
| ACL 同步调用点 | `service/IotExperimentService.java` | `syncClassBroker()`（L777）→ L794 建号 → **L799 传 `buildClassTopicPrefix(...)` 给 `syncClassAcl`** |
| 设备级凭据（半成品） | `service/IotExperimentService.java` | `rotateCredential()`（L743）：账号 `iot_{deptId}_{deviceId}`，但只调 `siotCredentialAdapter.provision`（历史 SIoT 通道），**没有走 EMQX ACL** |
| 接口 | `controller/IotExperimentController.java` | `@RequestMapping("/business/iot")`；messages L148、student/overview L166 等；**无下发/判定接口** |
| AI 能力（可复用） | `service/ServerAiChatGateway.java` | `chat(String prompt, int maxTokens, int readTimeoutMillis)`（L44）；`AiProviderConfig` 支持 deepseek/doubao/qwen/zhipu |
| 表结构 | `sql/iot_mqtt_integration_v1.sql`、`sql/iot_class_grouping_v2.sql` | `biz_iot_experiment / group / device / message / event`；`biz_iot_class_config`；`biz_iot_group.topic`（L71-85） |
| 前端 | `RuoYi-Vue3/src/views/business/iot/index.vue`（初中 Mind+ 页签 L401）、`views/student/iot.vue`（Mind+ 页签 L109、`copyMindPlusConfig` L340）、`api/business/iot.js` | 复制内容仅「服务器/端口/账号/密码/Topic」，**无下行 Topic** |

### 2.2 设备侧（Mind+ 1.8.1，本机 `D:\programSoftware\Mind+\`）

| 关注点 | 证据 |
| :--- | :--- |
| MicroPython 模式的 MQTT 积木族 | 官方示例 `examples/mpython/Micropython/SIoT物联网.mp` → 解包 project.json，opcode：`mpyMqtt.mpyMqttInit`、`mpyMqtt.mpy_MQTTconnect`、**`mpyMqtt.mpy_MQTTAddSub`（订阅）**、`mpyMqtt.mpy_waitMSG`、`mpyMqtt.mpy_MQTTreceive`、`mpyMqtt.mpy_MQTTpublish_save` |
| 初始化积木的设置结构 | `{"platform":"siot","platform_child":{"siot":{"Server":"…","User":"…","Password":"…"}}}`，另有 `client_id` 字段但 siot 分支不暴露给学生 |
| 生成的初始化代码 | `resources/app.asar` 内：`from siot import iot` + `from mpython import *` + `mqtt = iot("<getClientId()>", "10.52.1.129", user="…", password="…")` + `mqtt.set_callback(mqtt_callback)` |
| 生成的订阅代码 | `from siot import iot`；`mqtt.getsubscribe(<topic>)`；并 `addInitTail("mqtt.loop()")`（自动装 50ms 定时器轮询） |
| **`siot` 库真实实现** | 冻结在固件 `Arduino/fw/esp32/mpython_firmware_v2.4.9.bin`（v2.3.6 同样）：`class MQTTException(Exception)`、`class iot.__init__(client_id, server, port=0, user, password, keepalive=0, ssl=False, ssl_params={})`（`port==0 → 1883`）、`getsubscribe(topic, qos=0)`、`subscribe(topic, callback)`、`loop()`、`publish()` |

---

## 3. `MQTTException: 128` 的准确原因（含代码级证据）

### 3.1 设备端为什么会抛 128

固件内 `siot.py` 的订阅实现（原文，字符串直接取自 mpython_firmware_v2.4.9.bin）：

```python
class MQTTException(Exception):
    pass

class iot:
  def getsubscribe(self, topic, qos=0):
    assert self.cb is not None, "getsubscribe callback is not set"
    pkt = bytearray(b"\x82\0\0\0")
    self.pid += 1
    struct.pack_into("!BH", pkt, 1, 2 + 2 + len(topic) + 1, self.pid)
    self.sock.write(pkt)
    self._send_str(topic)
    self.sock.write(qos.to_bytes(1, "little"))
    while 1:
      op = self.wait_msg()
      if op == 0x90:                      # SUBACK
        resp = self.sock.read(4)
        assert resp[1] == pkt[2] and resp[2] == pkt[3]
        if resp[3] == 0x80:               # 0x80 = Failure
          raise MQTTException(resp[3])    # → MQTTException: 128
        return
```

- `0x90` = SUBACK，`0x80` = MQTT 3.1.1 规范里的「订阅失败」返回码，十进制就是 **128**。
- 这是**标准 SUBSCRIBE 报文**（`0x82`），不涉及 SIoT 的 HTTP 数据接口；所以 128 只能由 Broker 的 SUBACK 决定。
- 该异常未被捕获 → 会中断主流程（灯不亮、后续积木不执行）。

### 3.2 Broker 为什么会拒绝

平台给班级账号写入的授权规则**只有一条**，且是 publish：

```java
// IotEmqxAdapter.java L108-125
String pattern = topicPrefix.endsWith("/#") ? topicPrefix : ...;   // county/169/274/2024-02/#
JSONObject rule = new JSONObject();
rule.put("action", "publish");        // ← 只有 publish
rule.put("permission", "allow");
rule.put("topic", pattern);
rules.add(rule);
body.put("username", username);
body.put("rules", rules);
// PUT /api/v5/authorization/sources/built_in_database/rules/users/{username}
```

调用链：`IotExperimentService.syncClassBroker()`（L777）→ `syncClassAcl(username, buildClassTopicPrefix(...))`（L799-800）。

因此 `class_169_2024_02` 的授权库里**不存在任何 subscribe 规则**：

| 请求 | 匹配规则 | 结果 |
| :--- | :--- | :--- |
| publish `county/169/274/2024-02/guangzhao/group02/data` | `publish allow county/169/274/2024-02/#` | ACK，成功 ✅ |
| subscribe 同一 Topic | 无 | 默认拒绝 → SUBACK `0x80` → 128 ❌ |

### 3.3 排除其他嫌疑（都不是原因）

| 嫌疑 | 判断依据 |
| :--- | :--- |
| Java 代码写错了吗 | 没有 128 相关逻辑；平台侧不参与设备订阅的鉴权，仅由 EMQX 决定 |
| Mosquitto？ | 否。Broker 是 EMQX 5.8.8 容器 `school-emqx-poc`，`10.52.1.123` 的 SIoT 只是保留的历史回退链路 |
| 动态鉴权（HTTP auth） | 否。授权源为 `built_in_database → file`，不存在外部 HTTP 授权服务 |
| 数据库权限表 | 与 MQTT 订阅无关。`biz_iot_*` 只存业务配置与消息，不参与 Broker 鉴权 |
| Mind+ 积木用错了吗 | 否。`mpy_MQTTAddSub` 的 siot+esp32 分支就是生成 `mqtt.getsubscribe(topic)`，写法正确 |
| Topic 拼错了吗 | 不是。128 是**订阅被拒**，不是 Topic 不存在（EMQX 不要求预建 Topic） |

### 3.4 佐证：本项目自己已经见过同样的 128

- `contexts/junior-iot-poc/design.md:35`：`测试设备仅可访问自己的 county/test/device01/#，跨设备订阅返回 SUBACK 128`。
- `contexts/PROJECT_CORE.md:882`：授权源顺序 `built_in_database → file`；8 个班级账号已同步精确班级 ACL；**历史 `{re,"^class_[0-9]+_.*"} -> county/#` 宽权限规则已删除**。
- `contexts/primary-iot-integration/requirements.md` REQ-5.3：`班级账号 SHALL 不得订阅 county/#` —— 也就是说「班级账号不能订阅」是**当前明确写入需求的设计**，不是缺陷。

---

## 4. 当前是否已有下行 Topic

**没有。** 全仓（Java + Vue3 + SQL）检索 `control`、`cmd`、`command`、`reply`、`result`、`downlink`、`下行`：

- `ruoyi-business` 内 `publish(` 的命中全部是无关业务（考试发布 `CountyExamController:199`、刷题计划发布 `PythonPracticeService:377`），**没有一个 MQTT publish**。
- 平台唯一 MQTT 客户端 `IotMqttReceiver` 只 `connect` + `subscribe`，无 publish 分支、无 published 记录。
- `IotMqttReceiver.isValidTopic()`（L266）只做长度与控制字符校验，不区分 `/data` 与 `/control`。
- 前端两个物联页只展示上行 Topic；`biz_iot_group.topic` 只有一条（上行 data）。
- 因此现状是**严格单向「设备 → 平台」**。

---

## 5. 推荐的最小改造方案（未执行）

### 5.1 Topic 规范（沿用你的命名，确认可行）

```
上行（设备 publish / 平台 subscribe）：county/169/274/2024-02/guangzhao/group02/data
下行（平台 publish / 设备 subscribe）：county/169/274/2024-02/guangzhao/group02/control
```

- 与现有 `buildGroupTopic` 只差最后一段，改动面最小。
- 两者段数一致（`county/{deptId}/{lessonId}/{class}/{activity}/{group}/xxx`），EMQX 通配符可直接覆盖。

### 5.2 ACL 设计（这是 128 能不能解决的关键）

给班级账号从「1 条规则」扩到「2 条规则」，并**顺手收紧 publish**（防止学生伪造别组下行）：

| 账号 | action | topic | 说明 |
| :--- | :--- | :--- | :--- |
| `class_169_2024_02` | publish allow | `county/169/274/2024-02/+/+/data` | 由 `/#` 收紧为只允许各组 data，堵住「发到别组 /control 伪造平台下发」 |
| `class_169_2024_02` | subscribe allow | `county/169/274/2024-02/+/+/control` | 解决 128 |
| `platform_iot_subscriber` | subscribe allow | `county/#` | 保持现状 |
| `platform_iot_subscriber` | **publish allow** | `county/+/+/+/+/+/control` | **新增**，否则平台下发会被 Broker 拒（当前该账号是"只读订阅"） |

### 5.3 组间隔离的现实约束（必须知情）

班级账号是**全班共用**的（`biz_iot_class_config.mqtt_username`），因此 Broker 层只能约束到「班级前缀」粒度：

- ✅ 可保证：跨班、跨校订阅被拒（不受影响）。
- ⚠️ 无法保证：**同班 group02 的设备订阅 group03 的 `/control`**（因为 ACL 只能匹配 `+/+/control`）。
- 若要严格到「一组一块板、不能碰别组」，可选两条路（本次不建议先做）：
  1. **设备级账号**：`biz_iot_device.broker_username` + `iot_{deptId}_{deviceId}` 已存在，但 `rotateCredential()`（L743）目前只调历史 SIoT 通道，**没有同步 EMQX ACL**，需要补齐设备级 ACL 下发；课堂要多一步"登记设备"。
  2. **按 clientId 做规则**：Mind+ 的 clientId 由 `getClientId()` 自动生成、对话框不暴露（示例里的 `9049020183412981` 是自动值），学生不可控 → 当前不可用。
- 第一阶段的兜底：平台只向**正确小组**的 control 发消息；`IotMqttReceiver` 增加「上行只接受 `/data` 结尾」校验，把学生发到 `/control` 的消息直接记诊断事件并丢弃。

### 5.4 业务闭环（AI 判定）

```
掌控板语音识别文字 → publish /data
平台 IotMqttReceiver 收到 → 落库 biz_iot_message（已有能力）
平台调用 ServerAiChatGateway.chat(prompt) 做语义判定 → 得到 ON / OFF / HOLD
平台 publish 到该组 /control（QoS1，retain=false）
掌控板 getsubscribe 收到回调（mpython 为 50ms 定时轮询 check_msg）
掌控板按 payload 控制 RGB
```

- 设备侧积木顺序（MicroPython 模式）：`MQTT初始化参数` → `连接MQTT` → `订阅主题 "…/control"` → `设置等待主题消息（阻塞/非阻塞）` → `当主题 "…/control" 接收到` → 判断 payload → RGB。
- 无需预建 Topic（EMQX 不做 Topic 白名单）。
- 建议 payload 用短 JSON：`{"ai":"OFF","reason":"用户要求不开灯"}`，仍受平台 16KB / 120 条每分钟限流约束。

---

## 6. 需要修改哪些文件（预估，尚未动手）

### 后端（RuoYi-Vue）

| 文件 | 改动要点 |
| :--- | :--- |
| `service/IotEmqxAdapter.java` | `syncClassAcl()` 改为写 2 条规则（publish `+/+/data`、subscribe `+/+/control`）；新增平台账号下行发布权限的幂等同步方法 |
| `service/IotExperimentService.java` | 新增 `buildGroupControlTopic()`（复用 L837-844 逻辑）；`buildClassTopicPrefix` 配对调整；必要时在 VO 暴露 controlTopic |
| `domain/vo/IotClassCardVo.java`、`IotStudentOverviewVo.java` | 增加 `controlTopic` 字段 |
| `service/IotMqttReceiver.java` | 新增 `publish(topic, payload, qos)`；`receiveInternal()` 仅接受 `/data` 结尾的上行；下行发送记 `biz_iot_event` |
| 新增 `service/IotDownlinkAiService.java` | 语义判定（复用 `ServerAiChatGateway.chat`）+ 下发 + 权限校验（教师仅限本人课程/班级，学生仅限本人小组） |
| `controller/IotExperimentController.java` | 新增下发/判定接口，沿用 `@PreAuthorize` 现有风格 |
| `mapper/IotMapper.java` + XML | 若新增 `biz_iot_command` 表则补；若只用 `biz_iot_event` 则无需 |

### 前端（RuoYi-Vue3）

| 文件 | 改动要点 |
| :--- | :--- |
| `api/business/iot.js` | 新增下发/判定 API |
| `views/business/iot/index.vue` | 初中 Mind+ 页签显示「下行 Topic」+「AI 判定并下发」按钮 + 下发记录 |
| `views/student/iot.vue` | Mind+ 页签显示本组 `/control`，附订阅积木说明 |
| `utils/iotPythonTemplate.js` | （可选）小学 Python 模板补 `subscribe` 示例，保持两套工具一致 |

### 数据/SQL

- 若只复用 `biz_iot_event`（新增 event_type 如 `DOWNLINK_COMMAND`）：**无需迁移**。
- 若要留存下发历史：新增 `sql/iot_downlink_v1.sql`（幂等建表 `biz_iot_command`）。

### 服务器（不属于本仓代码）

见第 7 节。

---

## 7. 项目代码里找不到、必须去服务器检查的部分

> **明确结论：班级账号的 subscribe 规则、平台账号的 publish 权限、acl.conf 文件本身，都不在本项目代码里。** 代码只负责通过 EMQX 管理 API「写入」班级账号的 publish 规则；授权源的其余部分（文件 ACL、账号权限、授权源顺序）配置在 `10.52.1.129` 上。

需要检查（只读）：

1. **EMQX 管理 API：班级账号当前规则**
   `GET http://10.52.1.129:18083/api/v5/authorization/sources/built_in_database/rules/users/class_169_2024_02`
   预期只看到 1 条 `publish allow county/169/274/2024-02/#` —— 这是 128 的直接证据。

2. **EMQX 管理 API：授权源顺序与启用状态**
   `GET http://10.52.1.129:18083/api/v5/authorization/sources`
   预期 `built_in_database` 在前且 `enable=true`（与 `PROJECT_CORE.md:882` 一致）。

3. **平台账号的权限**
   `GET .../rules/users/platform_iot_subscriber`（或文件 ACL 中对应规则）
   预期只有 `subscribe county/#`，**没有 publish** → 下行必须补。

4. **文件 ACL 本体**：容器 `school-emqx-poc` 内 `/opt/emqx/etc/acl.conf`（宿主机挂载路径用 `docker inspect school-emqx-poc --format '{{json .Mounts}}'` 确认，历史备份位于 `/srv/emqx-school-poc/backups/<时间戳>/acl.conf`）。检查里面 `class_*`、`platform_iot_subscriber`、`deny all` 三类规则的现状。

5. **容器与服务状态**：`docker ps | grep school-emqx-poc`、`systemctl status emqx-school-poc`、端口 `1883/TCP` 对 `10.52.0.0/16` 的防火墙规则。

6. **可用性**：`built_in_database` 已启用时，改班级 ACL 走平台接口即可；给平台账号加 publish 权限，优先走管理 API（`rules/users/{username}`）以保持与现有同步逻辑一致，改动文件 ACL 需先备份。

---

## 8. 下一步建议（按顺序，均待你确认后再执行）

1. **先验证判断，不改任何东西**：用 EMQX 管理 API 只读回读 `class_169_2024_02` 的规则，确认「只有 1 条 publish」。（本报告结论的决定性一步）
2. **最小可逆实验**：在 EMQX 上临时给班级账号加 `subscribe allow county/169/274/2024-02/+/+/control`，用同一块掌控板只加「订阅主题 …/control」积木，平台侧先用 `mosquitto_pub`/脚本手工发一条，验证设备能收到并亮灯 → 打通硬件链路。
3. **设备侧定型**：确认 Mind+ 积木顺序与 payload 解析（含断线重连：`mqtt.loop()` 与 `start()` 的行为差异要实测）。
4. **平台侧最小闭环**：`buildGroupControlTopic` + `IotMqttReceiver.publish` + AI 判定接口（复用 `ServerAiChatGateway`）→ 教师页按钮 → 落 `biz_iot_event`。
5. **权限收口**：平台账号 publish 权限、班级账号 publish 由 `/#` 收紧为 `+/+/data`；补跨班/跨组越权测试（沿用 2026-08-31 的验收方法）。
6. **回归**：初中 Mind+ 原有单向上报必须无退化；小学 N17 实验板（`umqtt.simple`）不受影响。
7. **收尾**：更新 `contexts/PROJECT_CORE.md`、`contexts/junior-iot-poc/` 对应 requirements/design/tasks 与 ADR（下行 Topic、ACL 模型、组间隔离残余风险）。

---

## 附：本次核查用到的证据命令

```powershell
# 1) 打开 Mind+ 官方 SIoT 示例（.mp 即 zip）
#    examples\mpython\Micropython\SIoT物联网.mp → project.json → opcode 列表
# 2) 在 app.asar 中定位代码生成模板
#    命中：mpy_MQTTAddSub / getsubscribe → "from siot import iot" + "mqtt.getsubscribe(topic)"
# 3) 在固件二进制中定位 siot 库源码
#    Arduino\fw\esp32\mpython_firmware_v2.4.9.bin → "file siot.py" / getsubscribe / MQTTException
```

（本次未执行任何 SSH、未改服务器、未改代码。）
