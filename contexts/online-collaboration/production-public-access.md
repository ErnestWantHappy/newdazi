# WPS WebOffice 免费测试应用与公网接入清单（已归档）

> 2026-08-14：WPS 路线已按 ADR-004 停用，本说明仅作历史记录，不再要求信息中心实施公网 DNS/NAT。

## 1. WPS 免费测试 AppID 申请

- 开发者后台：<https://365.kdocs.cn/3rd/open/developer/home>
- 没有 WPS 企业账号时创建企业：<https://plus.kdocs.cn/sign-up/?utm_source=solution>
- 官方快速上手：<https://open.wps.cn/documents/app-integration-dev/docs-center/quick-start/summary>
- 官方 JSSDK 下载：<https://open.wps.cn/documents/app-integration-dev/docs-center/online-preview-edit/web/jssdk>

操作顺序：用 WPS 企业账号登录开发者后台，创建“企业自建应用/集成应用”，在“应用能力”开启“在线预览编辑”免费试用，然后在“应用信息”复制 AppID 和 AppSecret。开放平台不支持个人认证；企业认证需营业执照，官方说明一般一个工作日内审核。

AppSecret 只能通过私密渠道交给平台运维人员，不发班级群、工作群，不写入 Git 或普通文档。

## 2. 公网地址

复用信息中心已经解析的现有域名，不需要申请第二个域名。WPS 官方回调网关支持 HTTP 与 HTTPS，因此当前 HTTP 可以直接用于免费测试 PoC：

- 最终公网基础地址：`https://xxkj.xsedu.net.cn`
- WPS 控制台“回调网关”：`https://xxkj.xsedu.net.cn/weboffice/callback`
- 文件传输前缀：`https://xxkj.xsedu.net.cn/weboffice/storage/`
- 当前 HTTP 入口：`http://xxkj.xsedu.net.cn`
- 当前 PoC 回调网关：`http://xxkj.xsedu.net.cn/weboffice/callback`
- 服务器内网上游：`http://10.52.1.123:3010`

当前只证明教育内网首页和服务器内网回调路由可达，不能在 WPS 官方集群联调。2026-08-12 WPS 日志显示其云端解析 `xxkj.xsedu.net.cn` 为 `no such host`，公共 DNS 当前也只返回私网地址 `10.52.1.123`。信息中心必须先提供公网 IP 对应的公共 DNS 和反向代理/NAT；HTTPS 仍是正式使用前的后续要求。

## 3. 信息中心实施参数

- 公网仅开放 TCP 443，使用受信任证书，禁止自签名证书。
- `/`、`/prod-api/**` 和 `/ws/**` 继续转发到现有学业测评平台，因为 `xxkj` 同时是平台访问域名。
- `/weboffice/callback/**` 原样转发至 `http://10.52.1.123:3010/weboffice/callback/**`，只允许 GET、POST。
- `/weboffice/storage/**` 原样转发至 `http://10.52.1.123:3010/weboffice/storage/**`，只允许 GET、PUT。
- 不把 3009 或 3010 端口直接暴露公网，所有访问统一经过 `xxkj` 的 443 网关。
- 不改写 URI、查询参数和请求体；透传 `Authorization`、`Date`、`Content-Type`、`X-App-Id`、`X-Weboffice-Token`、`X-Request-Id`。
- 上传请求体上限至少 6 MB，连接/读取/发送超时至少 300 秒；关闭缓存和登录重定向。
- 记录访问时间、状态码、耗时、来源 IP、URI、`X-Request-Id`，日志不得记录 AppSecret 或完整 Token。

## 4. 可直接发送给信息中心的话术

> WPS WebOffice 官方集群当前无法访问信息科技学业测评平台。`xxkj.xsedu.net.cn` 的公共 DNS 目前只解析到 RFC1918 私网地址 `10.52.1.123`，WPS 调用日志明确报 `lookup xxkj.xsedu.net.cn: no such host`。请给现有域名配置互联网可解析的公网 IP，并在公网网关通过 80/443 反向代理或 NAT 透明转发到 `10.52.1.123:3010`；公共 A/AAAA 记录不能继续直接填写私网地址。请保持 Host、原始 URI、查询参数和请求体不变，透传 `Authorization`、`Date`、`Content-Type`、`X-App-Id`、`X-Weboffice-Token`、`X-Request-Id`，允许回调路径 GET/POST、存储路径 GET/PUT，上传上限至少 6 MB，连接/读取/发送超时至少 300 秒，关闭缓存和登录重定向。若只能走教育专网，请与 WPS 确认 Solution 回调出口 IP 并开通双向路由/白名单。完成后请从普通互联网递归 DNS 和校外网络验证域名与回调路径，并反馈公网 IP、证书和测试结果。

## 5. 平台启用门禁

2026-08-11 已完成生产配置：NSSM 已配置测试 AppID/AppSecret、随机 Token 密钥、基础地址和 WPS endpoint，并启用 `COLLABORATION_ENABLED=true`；官方稳定版 JSSDK v1.1.27 已同域发布。2026-08-12 平台健康检查已纠正，因域名只解析到 `10.52.1.123` 而返回 `ready=false`；正确 WPS-2 + 平台 Token 的本机模拟签名已通过安全层，但 WPS 官方集群仍未抵达。AppSecret 不写入本文，仍只保存在 gitignore 的 `contexts/secrets.local.md` 与服务器 NSSM 私密环境中。

WPS 控制台填写 `http://xxkj.xsedu.net.cn/weboffice/callback`，平台 `WPS_WEBOFFICE_PUBLIC_BASE_URL` 填 `http://xxkj.xsedu.net.cn`，两者不要混淆。服务器配置已就绪，但还需要真实课程房间完成 WPS 官方集群回调及两名同班学生 + 一名异班学生的多人保存闭环。免费测试版只用于协议和多人交互验证：带水印、最多 5 个同时打开的不同文档、单文件 5 MB，不能据此证明全县 200+ 正式容量。
