# 在线协作设计（当前 CryptPad，WPS 实施记录已归档）

## 当前 CryptPad 架构（2026-08-17）

```text
教师课程设计器 -> 平台协作设置 API -> 每班房间 + 起始文件副本
学生/教师 -> 平台登录和课程/学校/班级校验 -> CryptPad Integration API
CryptPad 浏览器编辑器 -> onSave Blob -> 平台文件类型/大小/版本 CAS -> revision
```

- 业务房间仍使用 `biz_collab_room`，Provider 为 `CRYPTPAD`；WPS 历史记录保留。
- `provider_session_key` 只保存 AES-GCM 密文，主密钥由 `COLLABORATION_KEY_SECRET` 外置注入。
- CryptPad 服务固定为 `cryptpad/cryptpad:2026.5.1`，Compose 目录为服务器 `/srv/cryptpad`，容器端口只绑定 `127.0.0.1:3000` 和 `127.0.0.1:3003`。
- Nginx 负责两个域名的 HTTP/WebSocket 转发；正式 HTTPS 由现有受信任网关提供，网关必须把两个 Host 转发到扩展服务器。
- 平台接口包括健康检查、教师课程协作设置、学生当前班级房间、房间会话、文档下载、CAS 保存和教师密钥轮换。

> 2026-08-19：按用户要求，当前部署临时改为 HTTP（80 端口）验证。HTTP 不提供传输加密，只允许内网测试；恢复 HTTPS 后才能进入正式使用门禁。
> 2026-08-19：补充平台嵌入来源 `http://xxkj.xsedu.net.cn` 到 CryptPad Nginx 的 Content-Security-Policy `frame-ancestors`，否则平台 iframe 会显示“office.xsedu.net.cn 拒绝连接”。
> 2026-08-19：CryptPad 默认 `enableEmbedding=false` 导致平台进入房间时提示“此 CryptPad 实例禁用嵌入”；已从干净 decree 备份恢复并追加合法 `ENABLE_EMBEDDING=true` 记录，重启后 `api/config` 返回 `true`，容器健康。该开关只解决实例嵌入门禁，仍需现场完成多人编辑和保存闭环验收。

## 安全边界

平台先做业务授权再生成会话；匿名、跨学校、跨班级、非当前课程和关闭房间均拒绝。CryptPad 不维护平台学生账号，也不作为平台权限来源。

## 运维

Compose、Nginx、systemd 自启动、日志轮转和每日备份均放在 `deploy/cryptpad/`；数据和备份分开保存。镜像构建或升级必须保留版本标签、镜像摘要、备份和上一版 release。

### 2026-08-17 部署验收补充

- 实际 Compose 根目录为 `/srv/cryptpad`，固定镜像摘要为 `sha256:689634b77d1ef739efcd79b02e136788cb1b03793a7b6b6a46b2debcce130feb`；OnlyOffice v9 与 x2t 固定组件挂载在 `/srv/cryptpad/data/onlyoffice`，避免启动时访问 GitHub。
- 宿主机仅监听 Nginx 80；CryptPad 容器仅发布到回环 3000/3003。`cryptpad-compose.service` 和 `cryptpad-backup.timer` 已启用，备份落在 `/srv/cryptpad/backups` 并写 SHA-256。
- 2026-08-19 按用户要求切换为 HTTP 后，服务器本机带正确 Host 的 `/`、`/checkup/` 和 `/cryptpad-api.js` 返回 200，容器为 healthy；两个域名已直接解析到 `10.52.1.129`，但当前验收电脑访问 80 端口超时，网络/防火墙路径仍未放行。

> 2026-08-14：本设计对应的 WPS 提供方已停用，保留内容仅用于回滚和复用房间/版本模型；当前 CryptPad 路线已由 ADR-005 采纳。下方 WPS 架构、协议和配置段落均为历史归档，不代表当前实现。

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

## 7. 2026-08-24 首次保存设计

- 设计器开启协作时，从当前课程题目中筛选 `STARTER` 文件作品候选；需要时通过题目详情补齐作答方式，不能依赖尚不存在的 lessonId。
- 单候选自动绑定，多候选通过选择对话框绑定，取消时回滚开关；最终由现有 `save-all` 一次保存课程和协作配置。
- 已保存课程继续复用既有协作房间与版本模型，本次不新增表、接口或 Provider 权限。

## 8. 2026-08-24 紧凑布局与前置校验

- 设计器把在线协作开关渲染在物联网开关的下一行；说明文字收进 tooltip，默认页面只保留标题、状态和开关。
- 开启动作先从当前已选操作题中过滤 `practicalMode=FILE`，再核验其文件配置可编辑且存在 `STARTER`；零候选提示并回滚，单候选直接写入，多候选沿用选择对话框。
- 题目集合变化时重新校验当前绑定；绑定题被删除后同步关闭开关并清空待保存的绑定值。
- 这些规则只影响前端保存前校验和布局，不改变协作房间、版本、Provider 或权限模型。
