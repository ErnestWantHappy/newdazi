# 三份 AI 报告修复正式发布验收

日期：2026-10-07。130 正式主站版本 **1.30.35**，release `20261007_bug_review_v1`；上一版为 `20261007_login_align_v1`（1.30.34）。本次为隔离候选的前后端配套发布，未打包工作区其它未收口改动，无业务表结构迁移、依赖或共享配置变更。产品更新 SQL 为 `sql/bug_review_release_20261007.sql`。

发布前 Java 定向 180 项（178 通过、2 项因外部隔离环境跳过）、Node 32 项、Vue3 生产构建和 Chromium 模拟接口 5 项通过；正式库相关 6 条 SQL 的只读 EXPLAIN 通过。候选源码及制品哈希见 `output/bug-review-release-20261007/manifest.json`。

发布任务 `release-20261007_bug_review_v1` 状态 `COMPLETED`，后台、Nginx、aiot-lamp 三服务均 active。JAR SHA-256 `4a86d74e5b9b062a6b690fc998ce6da5fbf4be9de4fd4089b27ad5b639219c1b`，首页 SHA-256 `2e3a4838a1120ab18479ab5fb74ccfedbd00d4d19460b9540ff8731f4f408c82`，验证码代理 HTTP 200。整库备份在 `/data/backups/xueyeceping/20261007_bug_review_v1/`，335681178 bytes，SHA-256 `5c97bfb58775924afb98a911e3f77ec94bb2aa7c5ea76c7eed44f4b28b7107c6`。旧 release 保留。

正式只读验收：匿名活动 3 条、课程 6 条、焦点图 5 条，非法跟帖图片 HTTP 404；真实教师 `getInfo`、首页、课程 537 的批改班级列表（2 班）与平台更新列表 HTTP 200/业务码 200；真实学生 `getInfo`、当前课程和机位状态接口 HTTP 200/业务码 200。桌面 1440×900 与手机 390×844 登录页均加载真实图片、无横向溢出、无页面脚本错误或 HTTP 500，截图在 `output/playwright/bug-review-live-*.png`。更新 113/1.30.35 为 `PUBLISHED`，真实教师更新列表可见。结构化结果见 `output/bug-review-release-20261007/live-verification.json`、`status-result.json` 和 `published-update.json`。

后续使用本机私密凭据中的正式教研员账号复测，登录、`getInfo` 的 researcher 角色及 `getRouters` 的 16 条路由均返回 HTTP 200/业务码 200；未做教研员浏览器菜单点击。当前验收不包含真实学生作品写入/退回重交、设备重复注册写入、机房 Windows 客户端及跨机房现场。历史空作品、重复设备和不支持转换的旧文件未迁移。AOCI 未同步，相关索引后续复核。不能据此声称所有历史数据或现场设备均已验证。

回退时先核对当前仍指向本 release 与正在执行的 AI 任务，再恢复本次备份目录中的 service/Nginx 旧配置，`daemon-reload`、重启后台并检查后 reload Nginx，退回 1.30.34；将更新 113 撤回草稿。无本轮结构迁移，不整库回灌覆盖上线后新增教学数据。执行状态和旧路径见 `output/bug-review-release-20261007/deploy-result.json`。
