# 给原 AI 的 Bug 复查提示词（自包含）

你是本项目（`D:\dmwprogram\newdazipingtai\newdazi`）的独立复查人。另一个 AI 已完成「2025 宁波优秀课例」批次的整理、本机导入与 130 正式迁移，现在需要你**只做检查**，不要替我重做搬文件、生成预览等重复工作。请用简体中文，按 P0/P1/P2 列**可复现**缺陷，给触发步骤、影响、准确文件/数据位置、最小修复建议；明确区分「已确认问题」与「未测风险」，不要因为总结里写了「通过」就相信全部正确。

## 一、用户规则（复述，作为判定标准）

1. 可预览的小型教学素材视频要能在平台播放；体积很大的视频不收录。
2. **老师上课的录课、课堂实录、授课录像一律不收录，不论大小**（包括 ZIP/RAR/Office 内嵌）。
3. **本批视频上限 100MB＝100,000,000 字节**（不是 100MiB），超限不上传、不拆分、不降码率、不改扩展名、不藏进压缩包；旧批次的 100MiB 政策不得改动。
4. 独立 HTML 尽可能提供平台内「打开网页/在线预览」入口；必须判断是否真正独立，不能只看扩展名。判定：
   - `HTML_STANDALONE`：CSS/JS/图片内联或 data 资源，不依赖丢失的同目录文件、本地服务、绝对盘符、构建环境；受限预览环境里主要功能可用。
   - `HTML_WITH_ASSETS`：需要同目录脚本/图片/数据 → 完整打包下载，不能当单文件硬开。
   - `HTML_SERVICE`：依赖接口/WebSocket/MQTT/数据库 → 给完整包和真实启动条件。
   - `HTML_SOURCE_PROJECT`：React/Vue/npm 源码 → 保留源码包与启动说明。
   - 作者网页必须视为不可信内容：**禁止与平台同源顶层执行**，禁止 `innerHTML/document.write` 进主页面，禁止用 `allow-scripts allow-same-origin` 放行；应经过教师鉴权、在 sandbox/no-referrer 下预览，作者页不能读平台 Cookie/Token/父页面。
5. 每份作者课例最多一个配套材料包；包内说明要与平台展示一致；包内禁止录课/超限视频、实名学生数据、课堂数据库残留、私人凭据、日志备份。
6. 教学设计/课件按原类别独立预览下载；Office 用 PDF 派生预览，原件不改；保留 1.30.70 的连续页图滚动、页码/缩略图/缩放同步。
7. 教材匹配必须按**实际内容**，禁止只按「第几课」数字；一个 `book_code + section_key` 只能对应一条 `kind='LESSON'`；歧义保留 PENDING，不编造署名。
8. 只维护 Vue3；不改旧 `ruoyi-ui`；保留既有 `case_id`/`resource_id`/评价/邀请；不改核心权限模型。

## 二、这次实际改了什么（精确位置）

### 源码（后端，仅课例模块）

- `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/controller/TextbookCaseController.java`
  - 新增 `GET /business/textbook/cases/html/{id}`（`@Anonymous` + `requireBrowserTeacher` Cookie 教师校验，只允许 `.html/.htm`，≤16MiB，返回
    `Content-Security-Policy: sandbox allow-scripts allow-forms allow-modals allow-popups`、`Referrer-Policy: no-referrer`、
    `X-Content-Type-Options: nosniff`、`Cache-Control: private, no-store`）。
- `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/mapper/TextbookCaseMapper.java`
  - `resources()` 增加计算列 `htmlPreview`（`LOWER(original_path) LIKE '%.html'/'%.htm'`）。
- `RuoYi-Vue/ruoyi-business/src/test/java/com/ruoyi/business/controller/TextbookCaseControllerTest.java`
  - 新增 2 项：HTML 沙箱头/学生 403；非 HTML 资源拒绝。当前 11 项通过。

### 源码（前端，Vue3）

- `RuoYi-Vue3/src/api/business/textbookCase.js`：新增 `caseHtmlUrl(id)`。
- `RuoYi-Vue3/src/views/business/textbook/components/CasePanel.vue`：
  - 资源行新增「打开网页」按钮（`v-if="Number(resource.htmlPreview)"`）；`openHtml()` 新标签打开，失败给行内提示。

### 脚本（新建，均在 `scripts/textbook/`）

`ningbo2025_inventory.py`（盘点）、`ningbo2025_extract.py`（解包）、`ningbo2025_read_summary.py`（读两份汇总表）、`ningbo2025_listing.py`（人工审核清单）、`ningbo2025_html_audit.py`（HTML 依赖审计）、`ningbo2025_cases.py`（94 条人工映射表）、`ningbo2025_prepare.py`（分类/预览/合包/SQL/校验，子命令 `plan|stage|assemble|sql|verify`）、`ningbo2025_import_local.py`（本机备份+导入+后检）、`ningbo2025_repack.py`（补 23 个小包）、`ningbo2025_verify_api.py`（接口 25 项）、`ningbo2025_smoke_ui.py`（浏览器 13 项）、`ningbo2025_db.py`、`ningbo2025_closure.py`（生成闭环表）。

### SQL / 数据

- `sql/textbook_cases_ningbo2025_v1.sql`：90 课例 + 497 资源，**只有 `INSERT … ON DUPLICATE KEY UPDATE`，无 DELETE**；`book_id` 由 `book_code + section_key + kind='LESSON'` 解析。
- 未改表结构；未新增列；`biz_platform_update` 只新增 1 行（149 / 1.30.71 / PUBLISHED）。

### 配置/重启

- 130 unit 的 JAR 路径、Nginx root、`TEXTBOOK_ROOT/cases` 软链三项切换；后端重启一次（MainPID 2889414），Nginx `reload` 一次；env 29 项未变。

## 三、数字与证据（自查用，不要只信这段）

- 来源：2172 文件 / 19,941,483,508 字节 / 94 个候选（85 目录 + 9 册目录下压缩包，13 个压缩包含 4 个嵌套）。
- 上架：**90 课例 / 497 资源** = 文档 355（DESIGN 90、SLIDES 105、WORKSHEET 102、MATERIAL 117）+ 视频 59 + 独立 HTML 19 + 配套包 64。
- 未上架：PENDING 3（董文琴/马小辉/叶晓静「人工智能中的算法」，现行八下无对应课次）；重复来源 1（王蕙兰八下目录与三上目录文件哈希相同）。
- 视频：59 个全部 ≤100,000,000 字节；其中 2 个 WMV 仅下载（无预览）；排除录课 6 个（最大 2,468MB）、超限 3 个、学生实名 5 个、重复 2 个。
- 本机库：52/196 → **142/693**，孤儿 0、重复 0，评价 2 条保留。
- 130 库：同样 **142/693**，配套包 64。
- 证据目录：`output/textbook-cases-2025/`（`source-inventory.json`、`resource-decisions.json`、`video-decisions.json`、`html-decisions.json`、`catalog-reviewed-2025.json`、`files-manifest.json`、`verify-result.json`、`api-verification.json`、`local-import-result.json`、`backups/`、`release-130/`）；浏览器截图与结果 `output/playwright/textbook-cases-2025/`。

## 四、正式环境事实（130）

- 版本 **1.30.71**；release `/data/apps/xueyeceping/releases/20261104_ningbo2025_cases_v1`。
- 后端 JAR SHA-256 `18d735a19dfb17407db5ae53b8b79f89035b3a76dee5ab50bb2133fa1eff0bb3`（以生产 JAR 为基线，只换业务 jar 内 2 个 class，其余 291 条目逐字节一致）。
- 前端首页 SHA-256 `f9347b300db4cc7859f9b9b3efc976c4913734146bbbdfe07e355980bbecec20`。
- 资源软链：`shared/textbook/cases → .../20261104_ningbo2025_cases_v1/resources/cases`；上传增量 `resource-delta.tar` 6,092,738,560B / SHA-256 `8928881b7113f6443d234825456a579e2464afd53d1740385e0b10255d817623`，823 个文件远端逐个 SHA256 复核通过；旧 307 个文件硬链接复用。
- 备份：`/data/backups/xueyeceping/20261104_ningbo2025_cases_v1`，`database.sql.gz` 597,425,809B / SHA-256 `9defe7afbcbaee1ff1787d7e36cdbc63a1af960c49e430b7e0d92bb4bb1ecb00`；含 unit/site/env/jar/index/课例元数据/软链目标与 `SHA256SUMS`。
- 服务：`xueyeceping-130`、`nginx` 均 active；启动期无 ERROR/Exception。
- 回退：`python output/textbook-cases-2025/release-130/deploy.py rollback`（等价手工步骤见 `contexts/textbook-cases-2025/migration-report.md` 第 7 节），不回灌整库。

## 五、请重点检查（按此顺序）

1. **旧教材误匹配**：逐条抽查 `catalog-reviewed-2025.json` 的 `bookCode/sectionKey` 与课名、作者、教学设计正文是否一致，尤其七上（互联网应用协议/IP地址）、八下（语音识别/语音合成/图像识别/机器学习）、六下（人机对话、扩音系统）。核对「吕贝」「陈培」「冯梦婷」「洪美芬」四个目录名与实际内容相反的映射。
2. **批次键碰撞/重复导入**：`source_key = sha256("ningbo-2025:" + 来源)[:32]`、资源键含课例前缀，是否存在与旧批次 52 条撞键的可能；重复执行 SQL 是否真的不增量（本机已跑 2 次，130 已跑 2 次）。
3. **`NOT IN` 删除旧资源**：确认真实执行的 SQL 不含任何 DELETE（旧 `import_cases.py stage` 有该风险，本次未使用）。
4. **旧评价损伤**：`biz_textbook_case_feedback` 是否仍为原记录、是否被 upsert 影响；`case_id ≤ 52` 的课例字段是否变化。
5. **录课/超限视频混入**：核对 59 个视频列表，是否含任何课堂实录；检查配套包内是否夹带 mp4/wmv 超限文件；检查 3 个 >100MB 的 PPTX 是否被误当视频处理。
6. **WMV 伪装 MP4**：2 个 WMV 的 `preview_path` 是否为 NULL（不是指向 WMV 文件）；下载接口是否只给原件；数据库里有没有 `.pdf` 后缀存视频字节的历史残留。
7. **HTML 沙箱/鉴权/类型分流**：`/html/{id}` 是否真的不返回非 HTML 文件；CSP 是否被 Nginx 或全局过滤覆盖；`htmlPreview` 计算列是否会把 `.html` 误送进 PDF 预览链路；作者页是否能读到平台 Cookie（本机实测 `window.origin === "null"`、`document.cookie` 被拒）。
8. **独立性误判**：抽查 19 个在线预览入口，是否真为单文件可运行；40 个 WITH_ASSETS 是否被错放成在线预览；`HTML_UNCERTAIN`（徐佳艳 CDN）降级说明是否准确。
9. **相对路径/启动说明**：随机解压 5 个配套包，检查目录结构、`使用说明_平台版.txt` 与平台 description/requirements 是否一致；需要本地服务的包（刘元 AI 学习助手、胡龄方学生素材、顾陆艇发布教程）是否写清条件。
10. **资料隐私**：检查是否仍有实名学生作业、班级账号密码、课堂数据库被发布（重点：刘宇镔学生作业、郑琦 801 班账号、应慧敏邮箱密码表、俞科 401.mp4）。
11. **缓存/断线重传**：`/html`、`/download`、`/media` 的 `Cache-Control` 是否正确；资源软链切换后旧 URL 是否 404；上传中断后重跑 `deploy.py upload` 是否按 SHA 跳过。
12. **课例连续滚动/双角色回归**：1.30.70 的连续页图滚动是否被本次前端构建影响；教师兼教研员仍能评价；纯教研员可读不可评；学生仍被拒。
13. **迁移是否完整**：130 上 497 个资源的 `original_path/preview_path` 是否都能解析到实际文件；随机抽 10 个资源做 SHA 比对；`case_id` 与本地是否一致（不一致属正常，用 `source_key` 对）。

## 六、输出要求

- 按 **P0（阻断/数据损坏/权限泄露）→ P1（功能错误/内容错配）→ P2（体验/文案/待完善）** 分组。
- 每条：现象 → 复现步骤（命令或页面路径）→ 影响范围（多少课例/资源）→ 准确位置（文件:行 或 表:主键）→ 最小修复建议。
- 单列「未测风险」，不要与已确认问题混在一起。
- 如果你发现先前总结里的数字/哈希与现场不符，直接指出并给出现场实测值。
