# ADR-002：内网认证的独立 Judge0 CE 部署

## 状态

已接受，服务器部署完成；cgroup v1 已切换并完成真实执行验证。

## 背景

学生代码需要在隔离沙箱执行，Judge0 不得成为浏览器可访问的公共 API，也不能与现有 CryptPad 容器、网络和数据卷混用。

## 决策

Judge0 CE 使用独立 Compose 项目 `judge0-python`，独立 Docker 网络、PostgreSQL/Redis 数据卷、端口和 systemd 服务。API 发布在扩展服务器 2358，宿主 UFW 及 Judge0 `ALLOW_IP` 只允许平台后端地址；所有平台请求携带服务器端认证令牌，令牌只存在外置配置。Judge0 worker 保留运行 isolate 所需的特权，但代码网络、附加文件、命令行和编译参数均关闭，并由平台请求和 Judge0 全局上限双重限制资源。固定版本 1.13.1 所需的 cgroup v1 已通过 GRUB 参数切换并重启验证，不能通过放宽 API 访问或关闭资源限制规避。

## 后果

Judge0 维护和备份可独立回滚，不影响 CryptPad；平台可在 Judge0 故障时切换为 `disabled` 并保留提交。worker 使用特权容器仍需依赖服务器 Docker/内核安全基线，正式部署后必须核对镜像 digest、宿主防火墙和磁盘告警。
