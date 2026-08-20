# 在线协作任务

## CryptPad 当前任务（2026-08-17）

- [x] C1 保留既有 WPS 房间/版本/回滚材料，建立统一 Provider 适配边界。
- [x] C2 实现 `CryptPadAdapter`、Mock Provider、外置配置和 AES-GCM 房间密钥。
- [x] C3 实现教师按授课班创建房间和文件副本、学生同班访问、跨班/跨校/关闭状态校验。
- [x] C4 实现文档下载、onSave 回传、大小/类型校验、版本 CAS 和 revision 追加。
- [x] C5 接入 Vue3 教师设置入口、教师编辑页和学生班级入口。
- [x] C6 完成幂等 SQL、后端专项测试、业务测试 308/308 和 Vue3 生产构建。
- [x] C7 完成扩展服务器 Docker/Nginx/UFW/Compose/systemd/备份目录和固定版本源码构建准备。
- [x] C8 正式库迁移前备份并执行 `sql/cryptpad_collaboration_v1.sql`，WPS 历史房间保持不变。
- [x] C9 完成 CryptPad 固定镜像构建、服务启动、`/checkup/`、Integration API、WebSocket 和日志验收；服务器本机带正确 Host 的 `/`、`/checkup/` 与 `/cryptpad-api.js` 返回 200，WebSocket 握手返回 101，容器健康，固定镜像摘要已记录。
- [ ] C10 完成教师/学生真实房间验收；当前按用户要求改用内网 HTTP，可信 HTTPS 暂未启用，不能作为正式发布证明。
  - [x] 修复学生编辑器跳回首页：新增 `/student/collaboration/editor/:roomId` 专用路由并完成 3010 热发布；前端入口和路由守卫验证通过，后端房间权限规则未放宽。
- [x] CryptPad `enableEmbedding` 已通过合法 decree 记录启用；容器重启后 `api/config` 返回 `true`，CSP 已允许 `xxkj.xsedu.net.cn`。
- [x] 课程设计器“文件作品”行内开启协作、教师首页按课程选择班级入口、低版本 Chrome 语法门禁和稳定参与者 ID 已在代码中完成；复用既有 `biz_collab_room` 和 CryptPad 成员列表，不新增表。
  - [x] 本地修复并部署 CryptPad Nginx CSP：补齐主域/sandbox 的脚本、Wasm、Worker、双域 WebSocket 和 iframe 来源；`nginx -t`、reload、`/checkup/` 200 通过，配置 SHA-256 已写入 `contexts/context.md`。
  - [x] 正式教师房间 17 浏览器加载 Word/OnlyOffice 成功，6 个 frame、编辑器画布可见，页面显示“已保存”，page error 为 0，未发现 CSP/Worker/Wasm 阻断。
  - [x] 修复协作会话身份：学生优先显示“学号 姓名”，教师显示系统昵称；兼容旧 Provider，清理姓名控制字符。
  - [x] 增加老浏览器兼容保护：WebAssembly/WebSocket/Blob 能力检查、CryptPad 脚本错误提示、20 秒 iframe 超时和诊断信息复制降级。
  - [x] 2026-08-20 发布 `20260820_collaboration_identity_compat_v1`；后端测试 317/317、Vue3 生产构建和 fat JAR 打包通过；NSSM/Nginx 切换、配置检查、reload 和 3009/3010 外部探活通过；备份位于 `D:\program\3009dazipingtai\backups\20260820_collaboration_identity_compat_v1`。
  - [ ] 教师/同班学生继续验证多人编辑、刷新保留和保存回平台版本递增；当前可用学生账号不在房间 17 的当前课程权限内，不能用跨班账号代替。
  - 推荐处理：在 `10.52.4.70` 为两个 Host 各增加 HTTPS 反向代理到 `10.52.1.129:80`，保留 `Host`、`Upgrade/Connection` 和 `X-Forwarded-Proto`，再从浏览器复验 `/checkup/`、`/cryptpad-api.js` 和 WebSocket。
- [ ] C11 完成真实 Word/Excel/PPT 导入、同班协作、保存回平台和 200 人容量门禁。
- [ ] C12 发布上下文、回滚证据和最终验收报告；本轮部署与浏览器证据已写入 `contexts/context.md`，待 C10/C11 后收口；Judge0 已完成独立服务，MQTT 保持认证门禁。

## 历史 WPS 任务

> 2026-08-14：WPS 路线因收费和公网回调条件终止。T12、T13 不再执行；历史任务保留为实施记录。

- [x] T1 明确每班独立房间、与个人作品/物联网解耦。
- [x] T2 对齐 WPS 文件信息、权限、用户与三阶段保存协议。
- [x] T3 建立房间、版本、上传票据 SQL。
- [x] T4 实现提供方配置、房间服务、会话鉴权和诊断。
- [x] T5 实现 WPS-2 回调校验、下载与三阶段保存。
- [x] T6 教师课程设计器接入协作设置面板。
- [x] T7 学生操作题入口和 WebOffice 编辑器承载页。
- [x] T8 后端测试/打包、前端构建、SQL 幂等验证。
- [x] T9 正式库备份、幂等迁移并发布平台侧 PoC；内网窄路径网关已通过，功能开关保持关闭。
- [x] T10 免费测试应用已配置到生产；内网 Host 路由、官方稳定版 JSSDK、WPS-2 签名和平台 Token 已通过。原健康检查把私网域名误判为公网，已在 T11 纠正。
- [x] T11 修复现有学生无 `sys_user_role` 导致的 403；增加真实 DNS/私网地址健康门禁和教师中文诊断，生产学生当前房间接口已验证 200，匿名仍为 401、异班仍拒绝。
- [x] T12（取消）不再要求信息中心为 WPS 开放公网 DNS/NAT。
- [x] T13（取消）不再进行 WPS 实机多人验收。
- [x] T14 前端移除教师/学生 WPS 入口和直达路由；后端关闭时不返回学生房间并拒绝 WPS 回调。
- [x] T15 核验 LibreOffice Online/Collabora、Umo/Yjs、CryptPad、ONLYOFFICE、Univer、SuperDoc 的能力、许可和容量边界。
- [ ] T16 用户在 CryptPad PoC（推荐）和 Collabora CODE PoC 中确认一条路线。
- [ ] T17 依据选定路线补齐需求、设计、部署和 200 人压测计划后再开发。
