# ADR-005：在线协作采用 CryptPad Integration API

- 状态：已采纳，2026-08-17
- 决策：平台通过统一 `CollaborationProvider` 接入自建 CryptPad；当前实现为 `CryptPadAdapter`。
- 固定版本：CryptPad `2026.5.1`，禁止使用 `latest`。

## 部署验证补充（2026-08-17）

- 扩展服务器 `/srv/cryptpad` 已运行固定镜像摘要 `sha256:689634b77d1ef739efcd79b02e136788cb1b03793a7b6b6a46b2debcce130feb`；容器仅发布回环端口 `3000/3003`，`cryptpad-compose.service` 与 `cryptpad-backup.timer` 已启用。
- OnlyOffice v9 与 x2t 组件已在本机完成官方 SHA-512 校验后放入独立宿主机目录，避免生产启动依赖 GitHub 出口；备份目录 `/srv/cryptpad/backups` 已生成带 SHA-256 的压缩备份。
- 本机 Nginx 的两个 Host 和 WebSocket 转发已通过配置校验；服务器本机 `/`、`/checkup/` 返回 200。DNS 仍指向 `10.52.4.70`，外部 HTTPS 目前返回网关 404，故可信浏览器验收和真实房间验收保持门禁状态。

## 背景

WPS WebOffice 已因正式容量收费和公网回调条件终止。平台仍需要保留课程、学校、班级、文件、版本和权限边界，并让同班学生共同编辑同一份文档。

## 决策内容

CryptPad 独立运行在扩展服务器 `10.52.1.129` 的 Docker Compose 服务中，使用主域名 `office.xsedu.net.cn` 和 sandbox 域名 `office-sandbox.xsedu.net.cn`。平台按课程授课班创建房间和文件副本，平台登录态先完成课程、学校、届别、班级、房间状态校验，再向浏览器提供 Integration API 会话配置。学生不注册 CryptPad 账号。

房间会话密钥以 AES-GCM 密文保存到 `biz_collab_room.provider_session_key`，密钥轮换和房间关闭由平台控制。保存使用房间版本号 CAS，成功保存追加不可变 revision，失败不能覆盖旧版本。

## 取舍

CryptPad 能自托管并提供 Document、Spreadsheet、Presentation 集成，避免 WPS 云回调；代价是平台自行承担镜像构建、容量压测、升级和 AGPL 合规核查。200 人并发仍是后续验收指标，不在本 ADR 中作容量保证。

## 回滚

平台 Provider 可切回既有 WPS/Mock 代码路径；历史 WPS 表、房间、版本和回滚材料不删除。CryptPad 服务回滚使用 `/srv/cryptpad` 的 Compose 配置、固定镜像标签和数据备份，平台发布回滚切回上一版 JAR/Vue3 release。
