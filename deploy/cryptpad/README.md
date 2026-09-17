# CryptPad 服务器部署文件

- 固定镜像：`cryptpad/cryptpad:2026.5.1`，禁止改成 `latest`。
- 数据目录：`/srv/cryptpad/data`，配置目录：`/srv/cryptpad/config`。
- OnlyOffice v9 与 x2t 组件挂载在 `/srv/cryptpad/data/onlyoffice`，使用固定版本官方 SHA-512 校验，避免启动依赖外网下载。
- 本机容器只绑定 `127.0.0.1:3000/3003`，只通过 Nginx 访问。
- 每日 03:20 备份到 `/srv/cryptpad/backups`，保留 30 天并写入 SHA-256。
- 当前两个域名按用户要求使用内网 HTTP 直接访问本机 Nginx；本机 Nginx 负责 HTTP 上游和 WebSocket。
- HTTP 仅适合内网临时验收，账号、会话和协作内容均未加密；正式使用前必须恢复可信 HTTPS。

扩展服务器本次盘点根盘可用约 453GB，低于长期目标 500GB；当前备份按 30 天保留，后续容量增长前必须增加独立数据盘或扩容。2026-08-17 服务器本机验收已通过；当前不使用 HTTPS，公网或跨网段使用仍保持禁止。
