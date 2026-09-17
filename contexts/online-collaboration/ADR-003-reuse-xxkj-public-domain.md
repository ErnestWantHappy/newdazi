# ADR-003：平台与 WebOffice 回调复用 `xxkj.xsedu.net.cn`（已被 ADR-004 取代）

## 状态

有条件接受；继续复用该域名，但截至 2026-08-12 只完成教育内网 Host 绑定，公网回调尚未完成，不能用于 WPS 真实联机。

## 决策

1. 不再申请第二个域名，平台与 WPS 回调复用 `xxkj.xsedu.net.cn`。
2. 路径分工：
   - `/`、`/prod-api/**`、`/ws/**`：现有学业测评平台；
   - `/weboffice/callback/**`：WPS 服务端回调；
   - `/weboffice/storage/**`：WPS 文件下载与一次性 PUT 上传。
3. `aitool.xsedu.net.cn` 继续只提供象山教育 AI 应用工具导航栏，不与 `xxkj` 共用站点内容。
4. 服务器两套可能监听 80 的 Nginx 配置均加入相同的 `xxkj → 127.0.0.1:3010` 规则，避免启动顺序变化后域名再次落入导航栏；当前实际监听者是 D 盘 Nginx 1.29.4。
5. 免费测试 PoC 可使用 HTTP，但域名必须先在互联网 DNS 中解析到公网 IP，并通过公网反向代理/NAT 到 3010；正式使用再迁移到 HTTPS。测试 AppID/AppSecret、官方稳定版 JSSDK 和 Token 密钥已于 2026-08-11 配置，生产协作开关已启用。

## 验证

- 服务器本机原始 TCP 请求：`Host: xxkj.xsedu.net.cn` 返回“信息科技学业测评平台”，与 3010 一致。
- `Host: aitool.xsedu.net.cn` 仍返回“象山教育 AI 应用工具平台”。
- 教育内网访问 `xxkj` 首页为 200；服务器本机模拟 Host 的 HTTP 回调可以到达 WPS 控制器。
- HTTPS TLS 握手仍被 443 端断开，但 WPS 官方回调网关支持 HTTP 与 HTTPS，因此不阻断免费测试联机。
- 2026-08-12 WPS 官方日志证明其云端 DNS 返回 `no such host`；公共 DNS 查询只得到私网地址 `10.52.1.123`。旧健康检查的 `ready=true` 是误判，现已修复为 `ready=false` 并显示解析地址。
- 正确签名的本机模拟回调已通过安全层，但不等于 WPS 官方集群已抵达平台。

## 后续

PoC 控制台回调网关继续填写 `http://xxkj.xsedu.net.cn/weboffice/callback`。信息中心先把该域名的互联网公共 DNS 指向公网 IP，并将公网请求透明代理/NAT 到 `10.52.1.123:3010`；完成后再做 WPS 官方回调和多人保存验收。正式使用前配置受信任证书和 443 TLS 终止，并把 WPS 控制台与平台公网基础地址一起切换为 HTTPS。
