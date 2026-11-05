# 教师优秀课例晚间功能扩充与响应式验收：原AI独立Bug复查提示词

> 历史交接/复核记录：所列缺陷已随1.30.69修复并正式上线；当前状态、真实验证及回退见[本轮发布](release-feedback-fix-20261103.md)。原记录保留供追溯。

> 本文档由接手AI在彻底完成全部开发、数据迁移、合包构建、单元测试、接口全面回归和Playwright真实多端验收后生成。  
> 请原AI以严苛的资深架构师与QA视角，独立阅读源码和证据，对本次改动开展无死角代码审查与潜在缺陷挖掘。

---

## 一、用户最终需求与实际完成范围

### 1.1 需求与落地对照表

| 序号 | 需求项 | 用户确认标准 | 实际落地与实现逻辑 | 落地状态 |
| :--- | :--- | :--- | :--- | :--- |
| 1 | **课例综合评价** | 按“课名+学校+作者”独立统计 👍/👎；每教师账号每课例唯一评价，可改票，再次点击撤销（vote=0）；不展示评价者个人身份 | 数据库主键 `(case_id, user_id)` 防重，`TextbookCaseFeedbackMapper.vote()` 采用 `ON DUPLICATE KEY UPDATE` 原子更新；并发与多设备不重不漏 | **已完成** |
| 2 | **教师文字评价理由与同行查阅** | 用户明确新增：“能不能让教师文字写原因，然后平台上别的老师也能看”；字数限制500字；完全匿名化展示给全平台教师 | `biz_textbook_case_feedback` 增加 `reason varchar(500)`；新增 `GET /{caseId}/feedback/comments` 接口；前端点击“写理由”可输入评价理由并保存，点击“评价理由”弹窗查阅同行评价，作者均脱敏展示为“同行教师”，仅展示态度、时间和理由内容，无任何敏感ID/姓名泄露 | **已完成** |
| 3 | **低频自愿邀请** | 预览有效停留≥30秒（失焦/切后台冻结计时）或发起下载返回后抽样（约25%）；单教师每日最多1次，同课例关闭后14天内不再弹出；服务端持久化限频 | 教师级行锁 `lockInvitation(userId)`；`last_attempt_at` 30分钟尝试退避；`previewActiveMs` + `visibilitychange`/`blur`/`focus` 监听失焦与切后台；预览失败不触发邀请 | **已完成** |
| 4 | **配套材料包同课合包** | 合并同作者同课的系统源码、网页、实验材料为唯一配套材料ZIP；保留子工程相对路径；生成平台说明；页面复用 `description` 简洁展示内容 | `consolidate_case_packages.py --build` 生成 44 份合包 ZIP；总资源数由 204 精简整合为 196（44 TOOL 包、52 设计、49 课件、13 任务单、38 配套素材）；内嵌《打开材料目录.html》与《使用说明_平台版.txt》 | **已完成** |
| 5 | **逐包真实技术条件与共享Key校准** | 排除运行库（site-packages）噪声；排除许可证SPDX误报；真实识别Node/Python/静态双击；保留自愿共享Key并注明失效可换 | 过滤 `runtime/Lib/site-packages` 排除 2700+ Python 运行库噪声；精准识别 4 处真实自愿共享Key（王纪宇DeepSeek、卢旭浩Coze、张楚人与仇蓓蓓各1处OpenAI兼容配置），无许可证误报 | **已完成** |
| 6 | **菜单顺序（数据库幂等）** | 电子教材与优秀课例紧接题库管理之后；成绩查询紧接导学单管理之后；教研活动紧靠学生管理之前 | 执行 `sql/menu_order_textbook_v1.sql`：教研活动 30、学生管理 40、题库管理 70、电子教材与优秀课例 80、导学单 90、成绩查询 100；已备份至 `sql/menu_order_backup_20261103.sql` | **已完成** |
| 7 | **自适应大弹窗与响应式** | 桌面保留教材位置，手机全屏；顶部明确下载对象；缩放/跳页/缩略图懒加载与Blob URL释放；flex 94dvh 杜绝双重滚动条 | 桌面端 `max-height: 94dvh` flex 布局，移动端 `98dvh / 100vw` 全屏覆盖；`IntersectionObserver` 懒加载缩略图；`URL.revokeObjectURL()` 即时释放内存图片 Blob | **已完成** |

---

## 二、改动文件清单与核心产物

### 2.1 后端改动文件
- `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/mapper/TextbookCaseFeedbackMapper.java`：
  - 新增 `comments()` 匿名评论查询，按时间倒序最多 50 条；
  - `vote()` 支持 `reason` 存取与撤销时（vote=0）自动清空 reason；
  - `summary()` 返回当前用户 `myReason` 与汇总数据；
  - `attempted()` 升级为原子 `INSERT ... ON DUPLICATE KEY UPDATE`，消除 `biz_textbook_case_invitation` 插入与加锁之间的空档死锁隐患；
- `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/service/TextbookCaseFeedbackService.java`：
  - 修复 `invitation()` 中由于 `vote` 为 null 导致的 NPE；
  - 实现 `vote(caseId, userId, vote, reason)` 文本裁剪（trim）与 500 字长度校验；
  - 彻底移除 `vote()` 中不必要的跨表行锁（`ensureInvitation`/`lockInvitation`），消除并发点赞时的 MySQL S-lock/X-lock 互锁死锁隐患；
  - 实现 `comments(caseId)` 接口；
- `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/controller/TextbookCaseController.java`：
  - 更新 `PUT /{caseId}/feedback` 接收 `reason` 字段；
  - 新增 `GET /{caseId}/feedback/comments` 匿名同行评价查询接口；
  - `GET /files/{resourceId}/pages/{pageNo}` 支持 `thumbnail=true` 小图返回；
- `RuoYi-Vue/ruoyi-business/src/test/java/com/ruoyi/business/service/TextbookCaseFeedbackServiceTest.java`：
  - 修复 Mockito 空 Map 契约，新增文字理由与评论匿名测试，共 11 项测试用例全部通过；

### 2.2 前端改动文件
- `RuoYi-Vue3/src/api/business/textbookCase.js`：
  - 新增 `getCaseComments(caseId)` 请求方法；
  - 更新 `saveCaseFeedback(caseId, vote, reason)` 支持传递评价理由；
- `RuoYi-Vue3/src/views/business/textbook/components/CaseVotes.vue`：
  - 新增 👍/👎 快速评价与防重、互斥与再次点击撤销；
  - 新增“写理由”弹窗，提供 500 字输入、字数统计、提交反馈；
  - 新增“评价理由”弹窗，展示同行教师匿名评价列表与空状态；
  - 增加统一 `busy` 状态和行内“保存中…”提示；
- `RuoYi-Vue3/src/views/business/textbook/components/CasePanel.vue`：
  - 重构停留计时逻辑（`previewActiveMs` + `visibilitychange`/`blur`/`focus` 监听），后台与失焦冻结计时；
  - 优化配套材料卡片与行内下载提示，明确显示下载对象；
  - 接入自适应大预览弹窗与宽屏配套侧栏；
- `RuoYi-Vue3/src/views/business/textbook/components/CasePreview.vue`：
  - 重写多尺寸预览工作区，支持缩放、跳页、懒加载缩略图与内存 URL 释放；
- `RuoYi-Vue3/src/views/business/textbook/reader.vue` & `index.vue`：
  - 响应式样式与 flex 布局调整，杜绝内外双滚动条。

### 2.3 数据库脚本与备份
- `sql/textbook_case_feedback_v1.sql`：新增 `biz_textbook_case_feedback` 与 `biz_textbook_case_invitation`；
- `sql/menu_order_backup_20261103.sql`：备份 24 个一级菜单原顺序；
- `sql/menu_order_textbook_v1.sql`：幂等更新菜单顺序；
- 全库备份产物：`output/textbook-cases/backups/20261103-202806/before_feedback_menu_migration.sql`（105,279,415 字节，SHA-256 `e0ad4f3fbdf6aa5d8481ffb79109315579f1ec52c93842c525f0a0c49be47137`）。

### 2.4 材料包产物与清单
- 合包脚本：`scripts/textbook/consolidate_case_packages.py`；
- 合包产物：`output/textbook-cases/release-materials/combined/packages/consolidated-v2/`（44 份合包 ZIP）；
- 最新清单：`output/textbook-cases/catalog-feedback-v2.json`（52 课例、196 发布资源）。

---

## 三、真实测试命令与证据清单

### 3.1 后端单元测试
- **命令**：
  ```powershell
  cd RuoYi-Vue
  mvn -pl ruoyi-business -am test "-Dtest=TextbookCase*Test" "-Dsurefire.failIfNoSpecifiedTests=false" "-DforkCount=0"
  ```
- **测试结果**：
  - `TextbookCaseControllerTest`：8 项通过，0 失败，0 错误；
  - `TextbookCaseFeedbackServiceTest`：11 项通过，0 失败，0 错误；
  - **总计**：19 项用例 100% 通过（此前 2 个 NPE 与并发死锁隐患已彻底根治）。

### 3.2 前端生产构建与单元测试
- **命令**：
  ```powershell
  cd RuoYi-Vue3
  npm run build:prod
  ```
- **测试结果**：
  - 前端单元测试：9 项测试全过（`guide-sheet-template-preview.spec.js` 5项，`login-preview.spec.js` 4项）；
  - Vite 编译：生产打包无报错，入口文件与哈希输出正常，产物完整位于 `dist/`。

### 3.3 全面接口回归测试
- **测试脚本**：`scripts/textbook/verify_api_suite.py`
- **执行命令**：`python scripts/textbook/verify_api_suite.py`
- **涵盖用例与输出证据**：
  1. `[1/6]` 教师登录与 `/getRouters` 相对顺序验证：
     - 教研活动(2) -> 学生管理(3)
     - 题库管理(5) -> 电子教材与优秀课例(6)
     - 导学单管理(7) -> 成绩查询(8)
     - 顺序校验 100% 通过；
  2. `[2/6]` 课例结构读取：成功读取 `case_id=19`，包含 `likes`、`dislikes`、`myVote` 字段；
  3. `[3/6]` 教师评价闭环：
     - 提交推荐 + 理由成功 (`myVote=1`, `myReason='教学设计层次分明，案例生动，课堂互动性强！'`)；
     - 匿名列表验证通过：读取到刚才提交的评价，时间有效，态度为推荐，无 `user_id`/`userName`/`nickName` 等隐私字段泄露；
     - 改票为不推荐成功 (`myVote=-1`, `myReason='实验配套材料缺少部分说明文档，建议补充'`)；
     - 撤销评价成功 (`myVote=0`, `myReason=null`)，且撤销后评论列表中该教师的评价立即不再展示；
  4. `[4/6]` 角色权限拦截：学生角色 (`2021710501`) 评价被正确拦截 403 Forbidden；纯教研员角色 (`laoda`) 评价被正确拦截 403 Forbidden；
  5. `[5/6]` 预览与缩略图接口：
     - `preview-info` 正常返回总页数 6 页；
     - `thumbnail=true` 缩略图接口成功返回 8,168 字节轻量 JPEG；
     - 正文高清图接口成功返回 282,109 字节高清图；
  6. `[6/6]` 配套材料包下载：`download/{resourceId}` 成功返回，`Content-Disposition` 带 attachment 且文件名正常编码。

### 3.4 Playwright 真实多端自动化浏览器验收
- **测试脚本**：`scripts/textbook/smoke_feedback_and_ui.py`
- **执行命令**：`python scripts/textbook/smoke_feedback_and_ui.py`
- **涵盖场景与截图产物**（位于 `output/playwright/textbook-cases/`）：
  1. `01-desktop-menu-order.png`：桌面 1500x900 教师侧边栏菜单相对顺序实测截图；
  2. `02-desktop-vote-reason.png`：课例面板点赞并填写 500 字评价理由实测截图；
  3. `03-desktop-peer-comments.png`：查看全平台同行教师匿名评价列表弹窗截图（确认展示“同行教师”）；
  4. `04-desktop-tool-download-hint.png`：配套材料卡片展示与行内下载提示截图（无多余全局弹窗）；
  5. `05-desktop-preview-dialog.png`：桌面自适应预览大弹窗，验证图片放大（464px -> 900px）与缩略图导轨展开；
  6. `06-lowscreen-preview-dialog.png`：矮屏 1366x600 预览弹窗渲染，验证阅读器无多余整页水平滚动条；
  7. `07-mobile-case-panel.png`：移动端 390x844 课例抽屉与评价按钮排布；
  8. `08-mobile-preview-fullscreen.png`：移动端 390x844 全屏大弹窗预览（弹窗宽度 390px 占满视口）。

---

## 四、当前运行环境与安全边界声明

1. **环境与服务状态**：
   - 本机开发库：`localhost:3306` / `xueyeceping_server_20260729`；
   - 后端服务：Daemon 进程运行中，PID 932，监听 `http://localhost:8080`；
   - 前端开发服务：运行中，PID 29336，监听 `http://localhost:80`；
2. **安全与发布边界（严格遵守）**：
   - **本轮开发严格限定在本机环境**，未连接生产 130 服务器，未向生产数据库执行任何迁移，未向生产上传制品，未执行任何 `git push`；
   - 生产环境 130 仍严格保持为 `1.30.68` 基线（52 课例、201 资源、更新 146/PUBLISHED）；
   - 凭据安全：任何脚本、日志、截图、代码提交中严禁包含任何真实密码与明文密钥。

---

## 五、请原AI重点复查的项目与 Bug 挖掘指引

请原AI独立检查以下核心逻辑，如发现问题，请按 **P0（致命阻塞/数据异常/权限泄漏）**、**P1（高频困惑/状态不一致/竞态异常）**、**P2（细节与样式瑕疵）** 格式进行规范报告：

1. **评价系统与并发一致性**：
   - 检查 `TextbookCaseFeedbackMapper.vote()` 与唯一主键 `(case_id, user_id)`，在高并发或网络抖动重复点击时，是否存在导致数据库锁死或点赞数虚高的风险；
   - 检查改票（赞变踩/踩变赞）以及撤销（再次点击撤销）时，`myVote`、`myReason` 与 `likes`、`dislikes` 计数的事务原子性；
2. **文字理由与匿名安全性**：
   - 检查 `comments()` 接口是否绝对不存在泄漏教师 `user_id`、`login_name`、`nick_name` 或班级信息的路径；
   - 检查 500 字理由在包含特殊字符、HTML标签或 Emoji 时的存取与防 XSS 安全性；
3. **低频自愿邀请触发与限频机制**：
   - 检查 `CasePanel.vue` 中 `previewActiveMs` 在浏览器多标签切换、后台挂起、弹窗关闭又重开时的累计是否准确，是否会因系统休眠产生时间突跃；
   - 检查单教师每日最多 1 次跨课例邀请和服务端行锁（`lockInvitation`）的实现是否存在死锁隐患；
4. **配套包合包与相对路径完整性**：
   - 检查 44 份合包 ZIP 中的子工程（尤其是 Python/Node/静态网页）在解压后，内部相对路径是否保持完好，启动脚本和生成的《使用说明_平台版.txt》是否与网页内容完全吻合；
   - 核对共享 Key 的保留情况，确认仅保留了用户确认的共享 Key，未有新的噪声误报；
5. **菜单顺序与多端响应式体验**：
   - 检查 `sql/menu_order_textbook_v1.sql` 对其他角色（如纯教研员、学生、管理员）的侧边栏菜单是否产生非预期副作用；
   - 检查 `CasePreview.vue` 在极小屏幕（如 320px 宽度）或超大屏（如 4K 宽屏）下，flex 高度分配是否仍然严丝合缝，是否存在遮挡工具栏操作按钮的极端情况。

---
请根据上述指引展开独立代码审查与测试复核。
