# 在线协作设计（WPS 实施记录已归档）

> 2026-08-14：本设计对应的 WPS 提供方已停用，保留内容用于回滚和复用房间/版本模型。下一阶段优先评估 CryptPad Integration API，备选 Collabora CODE + WOPI；未获得用户路线确认前不改表结构，详见 `provider-research-20260814.md` 与 ADR-004。

## 1. 架构

平台继续拥有课程、班级权限和文件；WPS 仅作为可替换的在线编辑器提供方。

```text
教师课程设计器 -> 协作设置 API -> 每班一条 collab_room -> 复制 STARTER 文件
学生/教师编辑页 -> 会话 API -> WPS JSSDK(fileId + 短期 token)
WPS 服务端 -> 公网回调网关 -> 文件信息/权限/用户/三阶段保存 -> 平台磁盘与版本表
```

计划复用平台域名 `xxkj.xsedu.net.cn`：平台页面和 API 仍由 3010 提供，
`/weboffice/callback/**` 与 `/weboffice/storage/**` 由 3010 按原始 URI 转发到
3009。服务器内网 Host 绑定已经验证，但该域名在公共 DNS 中只指向 `10.52.1.123`，
WPS 云端实际返回 `no such host`；必须先由信息中心完成公网 DNS 和反向代理/NAT。

## 2. 数据模型

### `biz_collab_room`

- 唯一业务键：`lesson_id + question_id + dept_id + entry_year + class_code`。
- `public_file_id` 是 WPS 唯一文件 ID，使用字母数字且不超过 47 位。
- `source_material_id` 指向题库起始文件，只用于创建时复制。
- `current_version`、`current_file_path`、大小、SHA-256 表示当前已提交版本。
- `status`：`OPEN`、`READ_ONLY`、`CLOSED`。
- 保存最近回调、WPS 请求 ID、错误和最后保存时间，供课堂排障。

### `biz_collab_revision`

每次保存完成后追加一条不可变版本记录，版本从 1 递增。版本 1 是房间初始化副本。

### `biz_collab_upload_ticket`

三阶段保存第二步创建短期票据，WPS 使用一次性公开 PUT 地址上传到临时文件。完成回调在事务内校验票据、大小与摘要，原子移动到正式版本目录并推进房间版本。

## 3. 权限

- 教师管理：课程本校，且为课程创建人或管理员。
- 学生加入：当前登录学生的学校、届别、班号、当前课程必须与房间一致。
- WPS 回调：匿名进入 Spring Security，但必须同时通过 WPS-2 签名和平台短期 Token；公开 PUT 还必须有未过期的一次性票据。
- 学生/教师不能直接读取磁盘路径，WPS 下载使用短期签名 URL。

## 4. WPS 协议

- 文件信息：`GET /weboffice/callback/v3/3rd/files/{fileId}`。
- 下载地址：`GET .../{fileId}/download`，返回公网短期下载 URL。
- 权限：`GET .../{fileId}/permission`。
- 用户：`GET /weboffice/callback/v3/3rd/users?user_ids=...`。
- 保存：实现 `/upload/prepare`、`/upload/address`、公开 `PUT`、`/upload/complete`。
- WPS 回调签名：`SHA1(AppSecret + Content-Md5 + Content-Type + Date)`；空 Body 用 URI 的 MD5，GET 的 Content-Type 按空串。

## 5. 配置

全部使用外置环境变量：

- `COLLABORATION_ENABLED`
- `WPS_WEBOFFICE_APP_ID`
- `WPS_WEBOFFICE_APP_SECRET`
- `WPS_WEBOFFICE_PUBLIC_BASE_URL`
- `WPS_WEBOFFICE_SDK_URL`
- `WPS_WEBOFFICE_TOKEN_SECRET`

测试应用真实密钥只进入 `contexts/secrets.local.md` 或服务器环境，不进入 Git。

## 6. 已知约束

- 当前内网 `10.52.1.123` 不能直接作为 WPS 回调网关；联机验收前必须提供公网 HTTP/HTTPS 地址。
- 生产 3010 已建立 WebOffice 窄路径入口；公网侧必须保持 URI 和请求体不变，并透传 `Authorization`、`Date`、`X-App-Id`、`X-Weboffice-Token`、`X-Request-Id` 等签名相关请求头。
- 免费测试应用最多 5 个同时打开的不同文档，故不能用它验证全县 200+ 长时并发，只验证协议与课堂交互。
- WPS 官方 JSSDK 不作为 npm 公共依赖假设；前端由可配置 URL 动态加载，部署时使用控制台下载的官方 SDK 文件或获准的官方托管地址。
- 平台健康检查必须解析 `WPS_WEBOFFICE_PUBLIC_BASE_URL` 的主机名；只得到私网、回环或链路本地地址时 `ready=false`，会话接口直接返回中文诊断，不初始化注定失败的 WPS iframe。
- 学生会话入口不依赖 `sys_user_role`；身份来自 `biz_student`，访问边界仍由学校、届别、班号和当前课程四项业务事实控制。
