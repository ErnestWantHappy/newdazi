# 项目长期记忆（工作区）

## 物联 / MQTT 链路事实（2026-09-16 核查确认）

- Broker：EMQX 5.8.8，`10.52.1.129:1883`，容器 `school-emqx-poc`；授权源顺序 `built_in_database → file`，未命中默认拒绝。
- 平台订阅：`platform_iot_subscriber` 订阅 `county/#`，接收器 `IotMqttReceiver` 只有 connect + subscribe，**无 publish**（无下行）。
- 班级账号 ACL 由 `IotEmqxAdapter.syncClassAcl` 写入，**每条只有 1 条 `publish allow county/{deptId}/{lessonId}/{date-class}/#`** → 设备订阅必回 SUBACK 0x80（设备端 `siot.iot.getsubscribe` 抛 `MQTTException: 128`）。
- 「班级账号不得订阅」是 `contexts/primary-iot-integration/requirements.md` REQ-5 的既定设计，不是 bug。
- Topic：上行 `county/{deptId}/{lessonId}/{entryYear-class}/{activity}/{group}/data`（`buildGroupTopic`）；`biz_iot_group.topic` 只存上行。
- 设备时长：小学 N17 实验板用 `umqtt.simple.MQTTClient`（平台生成的 Python 模板，已验证）；初中掌控板用 Mind+ SIoT 积木。

## 掌控板 / Mind+ 事实（本机取证）

- Mind+ 装于 `D:\programSoftware\Mind+\`（1.8.1-202506121900）。
- MicroPython 模式 + 平台=SIoT + 主板=esp32 → 生成 `from siot import iot`，`iot(clientId, server, user=, password=)`，订阅用 `mqtt.getsubscribe(topic)`，会自动注入 `mqtt.loop()`（Timer 50ms）。
- `siot.py` 冻结在固件 `Arduino/fw/esp32/mpython_firmware_v2.4.9.bin`，端口默认 1883；SUBACK `0x80` 即抛 `MQTTException(128)`。
- clientId 由 Mind+ `getClientId()` 自动生成（如 `9049020183412981`），积木对话框不暴露，学生不可控。
- 取证方法：`.mp` 文件就是 zip（含 project.json）；代码生成模板在 `resources/app.asar`（按字节搜 `getsubscribe`）；库源码在固件 .bin 里搜字符串。

## 约定

- 涉及硬件的排查：先设备侧（积木/生成代码/固件库）→ 再平台代码 → 最后 EMQX 配置，每层留证据。
- 报告类产出放 `contexts/junior-iot-poc/`（初中物联）或 `contexts/primary-iot-integration/`（小学物联）。

## 多 AI 协作仓库纪律（2026-09-16 血泪）

- 本仓库**同时被多个 AI 会话改**（例：`D:\dmwprogram\xinxikeji\grade7xia\lesson5new\` 是另一位 AI 的工作区，她可能在工作区外留「补丁包/编译好的类」，并**直接上线**）。**工作区源码未必是最新版。**
- **发布前必做**：① 扫今天被改过的文件（按 mtime）；② 搜工作区外是否有更新的同名 `.java` / `build_patch.py` / 已编译 jar；③ 比对线上 jar SHA 是否等于仓库历史制品——不等就说明有人发过东西。
- 全量重编译前，**先把她更新过的源码同步进工作区**，否则会静默盖掉她的加固。
- 制品一致性比对：fat jar 的类在 `BOOT-INF/lib/*.jar` 嵌套里；先比增删清单（REMOVED 必须 0），再比类常量池 UTF8 集合（裸字节会被 `MethodParameters`/`LocalVariableTable` 污染），最后做业务字符串硬校验。
- 内网发布：`10.52.1.123`(Administrator，跳板) → `10.52.1.130`(admin2，Ubuntu，nginx+java:3009)；EMQX 在 `10.52.1.129`。发布采用**就地在位替换 + 备份 + 精确回滚命令写进 ADR**。
