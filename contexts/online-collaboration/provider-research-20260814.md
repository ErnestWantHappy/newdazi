# 在线协作免费引擎调研与下一阶段方案（2026-08-14）

## 1. 结论

WPS WebOffice 因正式容量收费、依赖云端公网回调且当前教育网无法满足其公网 DNS/NAT 要求，停止继续投入。

在“现有 Word/Excel/PPT 文件、同班共同编辑、多个学校、至少 200 人同时在线、自托管、零软件许可费”这组约束下，没有一个现成产品能同时提供免费许可、厂商生产承诺和 200 人容量保证。免费路线必须由平台自行承担部署、压测、监控、升级和故障处理。

建议按以下顺序验证：

1. **首选 PoC：CryptPad Integration API**。它是完整自托管服务，Office 编辑器在浏览器端运行，协作服务不需要第三方云回调；集成 API 可由平台传入文件、房间密钥和用户名称，并在保存时把文件交还平台。
2. **备选 PoC：Collabora Online Development Edition（CODE）+ 平台 WOPI Host**。Office 文件能力更接近 LibreOffice，但官方明确 CODE 仅适合测试、个人和小团队，不推荐生产；200 人正式使用没有免费厂商保证。
3. **不作为主线：Umo Editor/Yjs**。免费 Umo Editor 是类 Word 富文本编辑器；官方协作、批注、版本和 Office 导入导出属于商业版 Umo Editor Next/Server。自研 Yjs 只能解决多人同步，不能补齐 Word/Excel/PPT 编辑器和格式兼容。

## 2. 候选项目核验

### 2.1 短视频中的 LibreOffice Online

- `LibreOffice/online` 是历史只读镜像，不应作为新项目部署基线。
- 当前活跃实现是 `CollaboraOnline/online`，主要采用 MPL-2.0，支持文字、表格和演示文稿及多人编辑。
- Collabora 不是上传 Docker 就能嵌入平台：平台必须实现 WOPI 文件宿主、访问令牌、文件读写和锁；Nginx 还要正确代理 WebSocket。
- 官方明确 CODE 是开发版，适合测试、家庭或小团队，不推荐生产。官方 FAQ 说明单文档没有形式上的硬上限，约 20 人协作通常可用，但性能取决于资源。这不能直接证明一个班 40 人、全县 200 人可稳定使用。

结论：**技术上可做 PoC，短视频把集成和生产运维难度省略了。**

### 2.2 短视频中的 Umo Editor Engine

- 可核验的官方仓库是 `umodoc/editor`，采用 MIT 许可证，是 Vue3 + Tiptap3 的类 Word 富文本编辑器。
- 官方文档明确 Umo Editor Next 是商业产品；多人协作、批注、修订、版本、Office 导入导出由 Next 和 Umo Editor Server 增强。
- Office 导入接口会把 docx/xlsx/pptx 等转换为 Umo 自身文档模型，不代表提供 Excel 单元格/公式或 PowerPoint 幻灯片的原生编辑体验。
- 没有检索到名为 “Umo Editor Engine” 且与截图全部宣传项一致的官方 GitHub 仓库。截图更像作者自行拼装 Umo + Yjs + 转换服务的二次项目，未给出仓库地址、许可证、发布记录和压测报告前不能作为平台依赖。

结论：**适合未来做导学单、协同写作，不适合当前 Office 操作题主需求。**

### 2.3 Yjs

- Yjs 是 CRDT（冲突自由复制数据类型）协作引擎，支持共享光标、离线、撤销和多种富文本绑定。
- 它不提供 Word、Excel、PPT 编辑器，也不负责 Office 文件导入导出、分页、公式或幻灯片。
- 自研还要补 WebSocket 鉴权、持久化、快照、版本清理、断线恢复和集群广播。

结论：**它是底层零件，不是 Online Office 产品。**

### 2.4 CryptPad

- 官方仓库是 AGPL-3.0 的自托管协作套件，提供 Document、Spreadsheet、Presentation。
- Office 三类应用使用 OnlyOffice 的浏览器端代码，但不使用 OnlyOffice Document Server；协作和存储由 CryptPad 自己完成，因此不继承 Community Document Server 的 20 连接限制。
- 官方 Integration API 支持业务系统嵌入，传入 `document.url`、`fileType`、会话 `key`、编辑/只读模式、用户名称和 `onSave` 回调。集成方负责权限、密钥和最终文件存储，正好对应平台现有房间与版本模型。
- 生产部署建议 Debian，完整安全隔离需要主域名和 sandbox 域名，并应使用 HTTPS。它不需要 WPS 云端访问平台，因此两个域名可以由教育内网 DNS 和证书解决，无需开放公网回调网关。
- 官方没有给出“200 人必然通过”的容量承诺，必须使用真实 docx/xlsx/pptx、40 人同文档和 200 人总并发做压测。
- AGPL 要求必须单独核对：优先保持 CryptPad 独立服务且不修改其源码；若修改并通过网络提供服务，需要向用户提供对应源码。平台通过公开 Integration API 嵌入是否构成衍生作品，正式上线前仍建议做一次许可证确认。

结论：**目前最值得先做的免费 PoC，但必须通过格式兼容、授权边界和 200 人压测三道门禁。**

### 2.5 其他项目排除理由

| 项目 | 结论 |
| --- | --- |
| ONLYOFFICE Community Document Server | Office 体验成熟，但免费社区版同时连接上限为 20，不符合目标。 |
| Univer | Apache-2.0 核心免费，但实时协作、Office 导入导出和服务端组件属于 Pro。 |
| SuperDoc | DOCX 专用，缺 Excel/PPT；社区版为 AGPL，专有部署另有商业许可。 |
| Luckysheet | 已转向 Univer；只解决表格，不能覆盖 Word/PPT。 |

## 3. 推荐方案：CryptPad PoC

### 3.1 架构

```text
教师开启课程协作
    -> 平台按学校/届别/班级创建房间
    -> 复制操作题 STARTER 文件，生成不可猜测的 CryptPad 会话密钥

教师或学生进入房间
    -> 平台先校验课程、学校、届别、班号
    -> 前端加载自托管 cryptpad-api.js
    -> 传入短期文件下载地址 + 同班会话密钥 + 用户显示名
    -> CryptPad 在浏览器内协同编辑

自动保存
    -> CryptPad onSave 交回 docx/xlsx/pptx
    -> 平台校验房间令牌、大小和文件类型
    -> 追加 revision，原子切换 current_file_path
```

### 3.2 基础设施

- 新建独立 Debian 12 虚拟机，不在现有 Windows 3009/3010 进程内硬塞运行时。
- PoC 起步建议 8 vCPU、16GB 内存、100GB SSD；是否够 200 人由压测决定，不把建议值当容量保证。
- 内网域名建议：`office.xsedu.net.cn` 与 `office-sandbox.xsedu.net.cn`，两者都只需学校可访问，不要求互联网公开。
- Nginx/HTTPS、固定 CryptPad 发布版本、日志轮转、每日数据备份；不使用 `latest` 自动漂移。

### 3.3 平台改造

- 保留 `biz_collab_room` 和 `biz_collab_revision`，新增通用 `provider` 与加密存储的 `provider_session_key`；废弃 WPS 专用上传票据和回调事件仅在迁移完成后再清理。
- 把现有 `CollaborationRoomService` 拆成平台房间服务和编辑器适配器，先只实现 `CryptPadAdapter`，避免下一次换引擎再重做课程和班级权限。
- 教师设置保持“选择操作题起始文件、按班创建、关闭保留历史”。
- 学生链接仍要求平台登录；前端绝不把另一个班的会话密钥写入页面或接口响应。
- 增加课堂诊断：CryptPad 服务可达、编辑器资源加载、房间握手、浏览器 WebSocket 状态、最近自动保存、保存版本和失败原因。

### 3.4 分阶段验收

#### P0：独立服务验证（0.5～1 天）

- 部署固定版本 CryptPad，完成两个域名、HTTPS 和 `/checkup/`。
- 各用一份真实教学 docx、xlsx、pptx 做导入、两浏览器协同和导出回原格式。
- 任一核心题型出现明显版式、公式或对象丢失，则停止平台集成。

#### P1：平台最小闭环（2～4 天）

- 只接一门课程、两个班，不改个人作品/考试流程。
- 验证同班同会话、异班不同会话、匿名/跨班拒绝、自动保存生成新版本、断线重连。
- 关闭房间后旧链接不可编辑，历史版本仍能下载。

#### P2：容量验证（2～3 天）

- 场景 A：5 个房间 × 40 人，共 200 人，检验真实班级同文档。
- 场景 B：20 个房间 × 10 人，共 200 人，检验多校多课程调度。
- docx、xlsx、pptx 分别测试；持续 30 分钟，并进行 10% 客户端断网重连。
- 放行指标：授权错误 0、保存丢失 0、最终内容收敛 100%、连接成功率 ≥99%、P95 打开 ≤10 秒、服务 CPU 持续低于 75%、内存低于 80%。

#### P3：真实课堂试点（1～2 周）

- 先一所学校两个班，再扩到五所学校；保留一键关闭协作并回退普通文件下载/上传。
- 收集浏览器、学校网络、文件复杂度和教师排错记录后再决定全县开放。

## 4. 备选方案：Collabora CODE + WOPI

### 4.1 必须开发的接口

- 读取 `/hosting/discovery`，按扩展名获得编辑器 URL。
- `GET /wopi/files/{fileId}`：CheckFileInfo，返回文件名、大小、用户和读写权限。
- `GET /wopi/files/{fileId}/contents`：GetFile。
- `POST /wopi/files/{fileId}/contents`：PutFile 并生成平台版本。
- 实现 LOCK、UNLOCK、REFRESH_LOCK 等 WOPI 锁，防止并发保存覆盖。
- iframe 使用短期 `access_token`；Nginx 正确代理 `/browser`、`/hosting`、`/cool` 与 WebSocket。

### 4.2 优缺点

- 优点：基于 LibreOffice，真正覆盖 Writer/Calc/Impress；平台继续持有原 Office 文件；没有 WPS 云回调。
- 缺点：集成和运维重，编辑器服务端渲染消耗高；CODE 官方不建议生产，免费路线没有 SLA、长期支持或 200 人保证。
- 使用边界：只适合作为第二 PoC 或明确接受“自行维护开源开发版”的内部试点，不应先承诺全县生产。

## 5. 决策门禁

下一步只选择一个 PoC，不能同时开发 CryptPad 和 Collabora。推荐先选 CryptPad；只有真实教学文件兼容性不合格，才转 Collabora CODE。Umo/Yjs 不进入 Office 主线。

## 6. 主要来源

- LibreOffice Online 历史镜像：https://github.com/LibreOffice/online
- Collabora Online：https://github.com/CollaboraOnline/online
- Collabora CODE 与生产边界：https://www.collaboraonline.com/code/
- Collabora FAQ：https://www.collaboraonline.com/faqs/
- Collabora WOPI 示例：https://github.com/CollaboraOnline/collabora-online-sdk-examples
- Umo Editor：https://github.com/umodoc/editor
- Umo Editor Next：https://dev.umodoc.com/en/docs/next
- Umo Editor Server API：https://dev.umodoc.com/en/docs/server/api
- Yjs：https://github.com/yjs/yjs
- CryptPad：https://github.com/cryptpad/cryptpad
- CryptPad Integration API：https://github.com/cryptpad/cryptpad-api-examples
- CryptPad Office 说明：https://docs.cryptpad.org/en/FAQ.html
- Univer：https://github.com/dream-num/univer
- SuperDoc：https://github.com/superdoc-dev/superdoc
