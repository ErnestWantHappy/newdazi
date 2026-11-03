# 课堂监控机位与内网 IP 串机修复（2026-10-28，本机完成、未发布）

## 反馈与现象

- 教师截图（130 正式站 `/business/classroom-desktop`，lessonId=455）：未连助手的学生卡片常驻一行「未连助手」，占空间。
- 学生 1 号在同台电脑登录→正常退出→另一账号登录后，1 号卡片仍显示旧电脑内网 IP（192.168.1.252）并出现「更新为当前机位」；1 号再登录又正常。

## 代码结论

- 卡片 IP 不是按 IP 反查学生，链路是：`student:{studentId}` → `device` → `owner:{deviceId}` → `live:{deviceId}.localIp`（助手心跳，TTL 75s）。
- 真实缺陷：
  1. `studentStatus()` 每次轮询都续期 `session:` 并重写 `student:{id}`（TTL 90s）；旧登录页面只要还开着，就会持续钉住旧电脑的实时 IP。新登录若没有助手认领，原实现直接返回 `associated:false`，**不清理**旧 `student:{id}`。
  2. `liveStudentDevice()` 在 `owner == null` 时放行，缺少归属校验。
  3. 退出登录不清理 `seat-helper:` 状态（只靠 TTL）。

## 本次改动

后端 `SeatHelperService`：

- 新增 `latest:{studentId} = {session, at}`（TTL 7200，`at` = `LoginUser.loginTime`）。
- `mayOwnSeat`：只有最新登录可占用卡片；最新会话已退出（`login_tokens:{session}` 不存在）时立即让位；旧登录返回 `associated:false, superseded:true`，不续期、不改写。
- `studentStatus`：最新登录未认领设备时删除旧 `student:{id}`；`owner` 必须存在且 session 匹配；认领成功后才 `adoptLatest` 并续期。
- `associate`：消费票据前、学校锁内各做一次 `mayOwnSeat` 复判，旧页面不得消费新票据或覆盖 owner。
- `liveStudentDevice`：要求 `latest.session == link.session` 且 `owner.session == link.session`，否则该学生卡片按 `UNIDENTIFIED` 处理。

前端：

- `classroomDesktop/index.vue` 两份卡片模板：无 IP 时不渲染 `.student-ip-text`（不再出现「未连助手」），机位/助手状态保留在身份行悬浮提示；删除不可达的 `.student-ip-text.is-offline` 样式。
- `studentSession.js`：被替代的旧登录轮询降为 30s，避免反复取票。

## 验证

- `mvn -pl ruoyi-business -am test "-Dtest=SeatHelper*" "-Dsurefire.failIfNoSpecifiedTests=false" "-DforkCount=0"`：40 项通过（新增 6 项覆盖旧登录不能续期、最新登录未认领清卡片、owner 缺失不识别、superseded 不消费票据、最新会话退出让位、旧 Redis 无 latest 自愈）。
- `mvn -pl ruoyi-business -am test "-DforkCount=0"`：819 项通过、0 失败、3 跳过。
- `npm run build:prod`：通过（入口 gzip 559218B，登录图片引用检查通过）。
- 未做：本机页面联调（本机库未执行 `sql/seat_helper_local_claim_v2.sql`）、真实助手换账号复验；不得据此宣称线上已修复。

## 发布与回退

- 需前后端一起切换；制品只含 `SeatHelperService.java`、`classroomDesktop/index.vue`、`studentSession.js`（工作区其他未验收改动不得混入）。
- 回退切回旧 jar/dist；Redis 中 `seat-helper:latest:*` 2 小时自然过期，无需清理。

## 剩余风险

- 同传克隆导致两台电脑共用同一 `device_id` 时，`live.localIp` 仍会被后心跳覆盖；需 0.2.4 指纹升级/重新配对。建议在目标库只读核对 `biz_seat_helper_device` 同 `fingerprint` 多行、`biz_seat_helper_binding.local_ip='192.168.1.252'` 的记录。
- `confirm()` 仍按实时 IP 末段生成机位名（如 `252机`）；本次只让陈旧卡片不再出现「更新为当前机位」。
- `loginTime` 约 100 分钟随 token 续期刷新一次，极端情况下旧登录可短暂反超；同一学生双端同时在线按「最新登录优先」处理。
