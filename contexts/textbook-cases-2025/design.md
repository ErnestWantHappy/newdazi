# 设计与实现（2025 宁波优秀课例批次）

## 1. 批次与可追溯键

- 批次前缀 `ningbo-2025:`，`source_key = sha256("ningbo-2025:" + 来源相对路径)[:32]`，不随临时解压目录或时间变化。
- 资源键 `sha256("ningbo-2025:<课例来源>::<课例内相对路径>")`，与旧批次 `sha256(相对路径)` 命名空间隔离，避免撞键误删。
- 同一课例内按内容哈希去重（`DUPLICATE_FILE`）；跨课例不合并。

## 2. 教材映射

- 只用 `book_code + section_key` 在目标库解析 `book_id`，每个组合必须恰好 1 条 `kind='LESSON'` 条目；不关联单元导入/小结。
- 2025 课号与现行目录不一致时按**实际教学内容**映射，并在 `resource-closure.md` 记录证据。典型差异：
  - 七上第 7 课互联网应用协议 → 现行第 8 课；第 9 课 IP地址与DHCP → 现行第 10 课；第 5 课互联网接入 → 第 6 课。
  - 八下第 9 课机器学习 → 现行第 6 课认识机器学习；第 7 课图像识别 → 第 3 课；第 6 课语音合成 → 第 2 课；第 5 课语音识别 → 第 1 课。
  - 八上第 7/8 课网页数据组织/呈现保持不变；八上第 10 课网页编辑与发布保持不变。
  - 目录名与实际内容冲突时以内容为准：吕贝（目录写六下第 10 课）实际为六上《人机对话的实现》→ 现行第 17 课；陈培（目录写图像识别）实际为《语音识别技术》→ 现行第 1 课。
  - 现行八下没有「人工智能中的算法（搜索/推理/预测）」课次，3 份保留 PENDING。

## 3. 资源分类与预览

| 类别 | 判定 | 预览 |
| --- | --- | --- |
| DESIGN | 名称含教学设计/教案/教学案例 | LibreOffice→PDF，连续页图 |
| SLIDES | pptx/ppt/ppsx/pptm/enbx | Office→PDF；enbx 仅下载 |
| WORKSHEET | 学习单/任务单/导学单/学历案/实验单/作业/评价/检测等 | Office→PDF |
| MATERIAL | 其余文档、视频、音频、图片素材 | PDF 或原生媒体 |
| TOOL | 独立 HTML、配套材料包 | HTML 沙箱新标签 / ZIP 下载 |

- 只有 DOC_SUFFIX 才可能成为独立资源；`.py`、`.jpg` 即使名称含「学习单」也只进配套包。
- 预览 PDF 超过 8MB 时逐页重采样为 1920px JPEG 页面（复用 `compress_case_previews.rasterize`），仅改工作副本；仍超过平台 100MiB 上限的改为仅下载（本批 1 份）。
- 预览名按内容哈希命名；压缩改名后同步回写清单，避免 404。

## 4. 视频政策

- 硬排除：文件名/目录含「课堂实录/教学录像/教学视频/授课录像/录课」；学生作业、学生作品、学生画图、学生代表性作业；账号密码/名单/花名册；<100KB 之外的 `~$` 临时文件。
- 超限：>100,000,000 字节直接排除，不拆分、不降码率、不藏进压缩包。
- 容纳：mp4/webm 设 `preview_path = original_path`（`previewable=1`，走鉴权 `/media` 与 Range 206）。
- WMV/MOV/AVI 等浏览器不支持的容器：只设原件、不设预览（`previewable=0`），说明需下载本地播放；本批 2 个 WMV、1 个 MOV（MOV 同时超限）。
- 视频不写进配套包（网页工程内嵌视频除外，如董毅上课网站的 96MB video-1.mp4）。

## 5. 独立 HTML 与在线预览

- 逐个 HTML/HTM 读取引用：`src/href` 同目录文件、`fetch/axios/XHR/WebSocket/mqtt`、`localStorage`、CDN 绝对地址、package.json。
- 分类：`HTML_STANDALONE`（无外部依赖）/`HTML_WITH_ASSETS`（依赖同目录资源，整包分发）/`HTML_SERVICE`（需接口或实时通信）/`HTML_SOURCE_PROJECT`（npm 工程）/`HTML_UNCERTAIN`。
- 只有 STANDALONE 且实测可用的 19 个入口提供在线预览。
- 后端新增 `GET /business/textbook/cases/html/{id}`：`@Anonymous` + Cookie 教师校验，只接受 `.html/.htm`，`≤16MiB`，响应头
  `Content-Security-Policy: sandbox allow-scripts allow-forms allow-modals allow-popups`、`Referrer-Policy: no-referrer`、
  `X-Content-Type-Options: nosniff`、`Cache-Control: private, no-store`。**不含 `allow-same-origin`**，作者网页处于不透明源，
  读不到平台 Cookie/localStorage、拿不到父页面；页面通过新标签打开，不进入平台同源顶层。
- 前端 `CasePanel` 对 `htmlPreview=1` 的资源显示「打开网页」，新标签打开；仍保留下载与配套包入口。
- 资源列表 SQL 增加 `htmlPreview` 计算列（按 `original_path` 后缀），PDF 预览链路不变，不会把 HTML 送进 PDF 渲染。

## 6. 配套材料包

- 每个课例最多一个包，保留作者相对目录结构，包内附 `使用说明_平台版.txt`（与平台 description/requirements 同源）。
- 包内容：图片、数据表、程序、网页工程、Scratch/drawio、小体量 Windows 工具；不收录录课、学生作品、凭据、日志。
- 剔除规则：单文件 >50MB 的 exe、>300MB 的压缩包、`env/`、`node_modules`、`__pycache__`；包总量 >250MB 时剔除 20MB 以上文件。
- 首批因「剩余素材 <100KB」未成包，后补 23 个包（94 个小脚本/网页），已补传并 upsert。

## 7. 导入与迁移

- 离线 SQL 只做 `INSERT ... ON DUPLICATE KEY UPDATE`，**不含 DELETE**，不触碰旧批次。
- 课例 upsert 通过 `SELECT book_id ... WHERE book_code=? AND section_key=? AND kind='LESSON'` 解析，避免本机 book_id 带到正式。
- 130 侧：新 release 独立目录，资源按内容哈希硬链接旧 307 个文件 + 增量解包 823 个；`TEXTBOOK_ROOT/cases` 软链原子切换。
- 后端只替换生产 JAR 内 `ruoyi-business-3.9.0.jar` 的两个 class（`TextbookCaseController`、`TextbookCaseMapper`），其余 291 个条目逐字节一致。
