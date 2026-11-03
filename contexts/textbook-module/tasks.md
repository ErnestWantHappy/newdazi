# 电子教材（教师端）任务与验证

> 状态：**130正式1.30.66**；1.30.64已上线课程绑定/学生入口/PDF修复，1.30.66补封面缩略图并正式复验13册书架和PDF下载。下方旧未发布描述为历史阶段。
> 关联：[需求](requirements.md)、[设计](design.md)、[ADR-004](../../docs/architecture/ADR/ADR-004-textbook-page-images-outside-release.md)

## 1. 已完成

- [x] 2026-11-02/1.30.66：13张360px缩略图真实接口与教师书架全部加载成功，ETag304；整册PDF16,816,368B/1.44秒、本课767,229B/0.08秒，学生下载403、学生目录200；后端0 ERROR。[集中发布与备份/回退](../release-20261102-all.md)。

### 1.1 教材内容与图片包

- [x] 核对 **13 册**原始 PDF 页数：三上 88、三下 86、四上 88、四下 86、五上 84、五下 88、六上 92、六下 84、七上 125、七下 123、八上 118、八下 128、九年级 111，合计 **1301 页**（四册补册于 2026-10-29 完成）。
- [x] 逐册核对单元/课次 PDF 页区间，产出 **13 份** `scripts/textbook/toc/*.json`；单元导入页从课次区间中拆出（小学 1 页、初中 2 页）。
- [x] 九年级处理原始 PDF 缺印刷页 31–34：`offset_breaks` 从 PDF 第 36 页起把偏移由 5 改为 1；缺口写入 `toc-review.md`，不篡改数据。
- [x] 七下源文件为「七上+七下」合订本（248 页），只取 126–248 并重编号 1–123；`import_textbook.py` 增加 `source_page_start/source_page_end`。
- [x] 渲染压缩：宽 1400px、JPEG q75，输出 `output/textbook-pack-20261029/pages/`，**1301 张合计 243.8 MB**。
- [x] 生成 `manifest.json`、`toc-review.md`、`SHA256SUMS`。
- [x] `verify_pack.py --rerender 2` 通过：1301 张哈希一致，抽样 26 页重渲染字节完全相同。

### 1.2 数据库

- [x] `sql/textbook_v1.sql`：4 张表 + 菜单 26100 + 三角色授权，幂等（`CREATE TABLE IF NOT EXISTS` + 菜单存在性判断）。
- [x] `sql/textbook_seed_20261029.sql`：13 册 / 50 单元 / 334 条目 / 1301 页，`ON DUPLICATE KEY UPDATE` 幂等（补册后整份重生成）。
- [x] 已在**本机库** `xueyeceping_server_20260729` 执行两份 SQL。
- [x] 复核：13 册页号连续 1..total_pages，`missing=0`；课次编号每册 1..N 连续；`bad_range=0`；条目分布 `LESSON 242 / UNIT_INTRO 49 / SUMMARY 19 / FRONT 13 / APPENDIX 11`。
- [x] 菜单复核：`menu_id=26100`，`path=textbook`，`component=business/textbook/index`，`visible=0`，`status=0`，授权 `admin,teacher,researcher`。

### 1.3 后端

- [x] 新增 `BizTextbookBook/Unit/Section/Page` 领域对象、`BizTextbookMapper`(+XML)、`TextbookCatalogService`、`TextbookStorageService`、`TextbookController`。
- [x] 4 个接口：`books`、`books/{id}/toc`、`books/{id}/pages`、`page/{bookId}/{pageNo}`。
- [x] 图片接口：ETag 用页 `sha256`，`If-None-Match` 命中返回 304，`Cache-Control: private, no-cache` + `Vary: Authorization`（2026-10-29 由 7 天缓存改，防共用机跨账号复用）。
- [x] `LessonInfoVo` 与 `BizLessonMapper.xml`（resultMap + 3 个 select）补 `semester`，教师首页课程卡才能解析册次。
- [x] `application.yml` 增 `textbook.root: ${TEXTBOOK_ROOT:./textbook-data}`；存储服务做根目录越界拦截。
- [x] `mvn -pl ruoyi-admin -am package -DskipTests` 成功（2026-10-29 09:13）。
- [x] `mvn -pl ruoyi-business -am test -DforkCount=0`：**822 项，0 失败，0 错误，3 跳过，BUILD SUCCESS**（2026-10-29 09:29:20）。日志中的 ERROR 是脱敏与异常分支用例的预期输出，不是失败。

### 1.4 前端

- [x] `src/api/business/textbook.js`：接口封装 + blob LRU 缓存（80 页，失败不入缓存）+ 缺册常量（现为空，13 册全上架）。
- [x] `views/business/textbook/index.vue`：书架，小学/初中分组，13 册全部上架（灰卡机制保留）。
- [x] `views/business/textbook/reader.vue`：目录树、连续滚动、课次手选、页码跳转、打印本课、页码跟随。
- [x] `components/LazyPageImage.vue`：IntersectionObserver 懒加载 + 失败态。
- [x] `router/index.js`：隐藏路由 `/business/textbook/read/:bookId(\d+)`，`activeMenu: '/textbook'`。
- [x] `views/business/teacher/index.vue`：课程卡顶部「教材」胶囊 + `openTextbook()`（九年级回退 `semester=9`，缺册提示后跳书架）。
- [x] `npm run build:prod` 成功（EXIT=0，入口 gzip 559484B，`reader-DeOEIPUC.js`）。
- [x] 修复底部状态栏误引用不存在的 `selectedSection`，改为 `currentSection`。

### 1.5 接口与权限联调（本机 8080）

以教师 `19157727791` 登录后实测：

| 检查 | 结果 |
| --- | --- |
| `/business/textbook/books` | `code=200`，13 册 |
| `/business/textbook/books/6/toc` | `code=200`，4 单元 / 125 页 |
| `/business/textbook/books/6/pages?from=10&to=14` | `code=200`，5 条，`width=1400` |
| `/business/textbook/page/6/12` | `image/jpeg`，197762 字节，带 ETag |
| 同页带 `If-None-Match` | **304** |
| 无 token | `code=401` |
| 学生 `2020710101` | `code=403 没有权限` |
| 不存在册 / 越界页 | `code=500` 业务错误（HTTP 均 200，若依惯例） |

### 1.6 浏览器冒烟

`scripts/textbook/smoke_teacher_textbook.py`（Playwright，1440×900，headless）**12 项全部 PASS**：

1. 教师登录并进入首页
2. 课程卡出现 18 个教材胶囊
3. 侧边菜单存在 `/textbook`
4. 书架 13 册、0 张灰卡
5. 封面按鉴权 blob 渲染，宽度 1400
6. 打开七上阅读器 `read/6`
7. 进入时不预选课次（显示「选择课次」）
8. 总页数 `/ 125`
9. 手选「第1课 计算机网络」定位到第 11 页
10. 打印本课生成 4 页
11. 滚动后页码跟随到第 60 页
12. 课程卡胶囊直达六上 `read/4`

产物：`output/playwright/20261029-textbook-smoke/`（7 张截图 + `result.json`）。

线上复验（2026-10-29，130）：教师端 9 项全 PASS（菜单、书架 13 册、七上 125 页、第 1 课第 11 页、打印 4 页、滚动第 60 页、0 pageerror / 0 4xx5xx）；课程卡 21 个「教材」胶囊，点击直达五上 `read/5`（84 页）；学生 390px 无教材入口、0 pageerror；证据 `output/release-20261029/online-smoke/`。

### 1.7 第二轮独立审计修复（2026-10-29，已随 1.30.61 发布）

- [x] **A-1 多页失败聚合提示**：`reader.vue` 收集懒加载失败页，达到 3 页在阅读器顶部显示「第 X… 页加载失败，可能是图片服务不可用」，并提供「重试失败页」；单页行内重试保留。
- [x] **A-2 打印期间切走路由不再撤销图片**：`textbook.js` 增加 URL 引用计数，`printSection()` 取图时持有、`win.print()` 返回后释放；阅读器卸载清理遇到打印持有只登记延迟撤销。
- [x] **A-3 强制重试复用在途请求**：`loadTextbookPage(..., force)` 遇到同页已有在途请求直接复用，避免重复请求和旧 URL 泄漏。
- [x] **A-5 页号上界**：`TextbookCatalogService.requirePage()` 增加 `pageNo <= totalPages`，配合「种子 SQL 只增不删」防止旧页残留继续回图。
- [x] **A-7 脚本文档修正**：`import_textbook.py`、`verify_pack.py` 的「918 页」改为 1301 页。
- [x] 验证：后端 `TextbookCatalogServicePageBoundsTest` 3 项 + 既有页图响应 3 项全通过；机位前端单测 9 项全过；`npm run build:prod` 通过（含登录/机位测试门禁）。
- [x] 浏览器复验：标准教师端冒烟 12/12 PASS；定向检查 6/6 PASS（拦截页图后第 1–3 页失败显示横幅、重试后恢复、打印准备中切走书架后打印窗 4 页图仍可用、无 JS 异常），证据 `output/playwright/20261029-textbook-fix-check/`。

### 1.8 阅读缩放（1.30.62，前端热更）

- [x] 阅读器新增缩放下拉：默认“适应整页”，可切“适应宽度”或 50/60/67/75/85/100/110/125/150/175/200%；`−/＋` 按档位调整；选择写入 `localStorage`，刷新和下次打开保持。
- [x] 默认整页按可视区域高度和宽度共同收敛；窄屏默认收起目录，展开时目录改为浮层，不再挤占正文。
- [x] 缩放只作用于阅读器；「打印本课」仍按整页输出，打印 4 页验证通过。
- [x] 130 验收 12/12 PASS：默认整页 465×679 完整落入 729px 视口、适应宽度 900px、125% 1125px、刷新记忆 110%、打印 4 页、390px 359×529 不溢出、0 pageerror/0 4xx5xx；首页 `7d3730f4`，后端未重启，更新 140 PUBLISHED。证据 `output/release-20261030/`。

## 2. 本机运行方式

```powershell
# 后端（必须先设教材根目录）
$env:TEXTBOOK_ROOT='D:\dmwprogram\newdazipingtai\newdazi\output\textbook-pack-20261029\pages'
java -Xms512m -Xmx2048m -jar RuoYi-Vue\ruoyi-admin\target\ruoyi-admin.jar

# 前端
cd RuoYi-Vue3
npm run dev
```

冒烟复跑：

```powershell
$env:PYTHONIOENCODING='utf-8'
python scripts/textbook/smoke_teacher_textbook.py
```

## 3. 发布与待办

- [x] **130 已于 2026-10-29 发布 1.30.61**（release `20261029_textbook_seat_v1`）：整库备份、两份 SQL、1301 张图片（255,685,333B）上传并逐张校验、shared env 增 `TEXTBOOK_ROOT`、后端重启与前后端切换完成；教师/教研员 API 与学生 403 已验证，更新 139/PUBLISHED 且教师可见。证据见[发布日志](../RELEASE_LOG.md)。
- [x] **2026-10-30 前端热更 1.30.62**（release `20261030_textbook_zoom_v1`）：默认适应整页、宽度/50%–200% 缩放与记忆、窄屏目录浮层；Nginx 平滑 reload，后端未重启，更新 140/PUBLISHED 且教师可见；回退恢复 `nginx.before` 即回 1.30.61。
- [x] 教师/教研员菜单可见（菜单 26100 已授权 `admin/teacher/researcher`，线上教师端已显示「电子教材」）。
- [x] 13 册已全部上架（三上～九年级），书架不再有灰卡；`TEXTBOOK_PENDING_VOLUMES` 机制保留，将来缺册时再加。
- [ ] `toc-review.md` 需用户抽查，重点九年级第 6 课与各单元导入页。
- [x] 学生端入口：2026-10-30 本机已实现“当前课绑定教材才显示、直达该课”（见第 5 节）；学生书架、全文检索、整册下载仍不做。
- [ ] 审计剩余 P3/边界项：书架封面不主动清缓存（A-4）、`/pages` 死代码与 ETag 强比较（A-6）、`no-cache` 回滚页 304 成本（A-8）；按需在发布后处理。

## 4. 风险与回滚

| 风险 | 说明 | 处置 |
| --- | --- | --- |
| 图片目录缺失/路径错 | 阅读器整册空白，接口 500；连续多页失败时阅读器顶部显示聚合提示 | 生产 `TEXTBOOK_ROOT` 已设为 `/data/apps/xueyeceping/shared/textbook`；探活 `/business/textbook/books/1/toc` |
| 磁盘占用 | 243.8MB 图片 + 备份 | 发布前查共享目录余量，教材目录与 release 分离 |
| 菜单不显示 | 若依菜单缓存或角色未授权 | 复核 `sys_role_menu` 中 26100 的三角色，重新登录 |
| 教材版权外流 | 图片若落 `/profile/` 即公开 | 已强制经鉴权接口回图，发布检查不得把教材拷进 profile |
| 九年级页码错位 | 原始 PDF 缺页 | 已按页脚实测配置分段偏移，抽检第 6 课首页印刷页 35 |

回滚：本期无表结构变更冲突，回滚只需下线菜单（`status=1` 或删除 26100 授权）、回退前后端 release；教材 4 张表可保留，不影响既有业务。

## 5. 2026-10-30 课程教材绑定与学生端入口（本机，未发布）

- [x] 数据：`biz_lesson` 增 `textbook_book_id` / `textbook_section_key` 两列可空，本机已执行 `sql/lesson_textbook_binding_v1.sql`，130 未执行。
- [x] 后端：保存时校验册已上架、年级/学期匹配、条目存在且为 `LESSON`；专用 update 支持显式清空；旧前端不提交 `textbookBindingTouched` 时不改写绑定。教师首页/详情返回两字段，学生 `current-lesson` 同步返回。
- [x] 权限：`/toc`、`/pages`、`/page` 放行 `@studentSs.isStudent()`；`/books` 仍仅 `admin/teacher/researcher`，页图鉴权、ETag 与 `no-cache` 不变。
- [x] 前端：设计器删除“第几课”显示并新增非必填单选；课程卡删除“第N课”标签、已绑定胶囊带 `?section=` 直达；学生顶部按钮仅在绑定时显示；阅读器支持 query 预选，并修正首屏页宽未落 DOM 导致的定位漂移。
- [x] 验证：后端教材/课程 **35 项**定向测试、Vue3 生产构建通过；Playwright **11 项断言**（教师保存→课程卡直达、学生直达/返回/未绑定隐藏）全 PASS、0 页面异常；截图与 `report.json` 在 `output/playwright/lesson-textbook-binding-20261030/`。
- [ ] 130 发布：需备份、执行两列增量 SQL、重启后端、同步前端；发布前 130 仍无绑定与学生入口。
- [ ] 学生书架、全文检索仍不做。

## 6. 2026-10-30 教师端整册/本课 PDF 下载（本机，未发布）

- [x] 后端：`TextbookPdfService` 用 PDFBox 按页图合成 A4 PDF；`/business/textbook/books/{bookId}/pdf` 与 `/books/{bookId}/sections/{sectionKey}/pdf` 仅教师侧；结果缓存在 `TEXTBOOK_ROOT/.pdf-cache`，目录不可写退回临时文件。
- [x] 前端：书架每册「下载整册」；阅读器工具栏「下载整册」+「保存本课 PDF」（打印本课旁）；学生端两个按钮不渲染；blob + `file-saver` 另存，整册超时 180s。
- [x] 修正阅读器预选定位：容器 `scroll-behavior: smooth` 会让 `scrollTop` 也走动画，改为瞬时落位，并用元素实际位置验收（此前只查页码文本会误判通过）。
- [x] 验证：后端教材/课程 35 项定向测试、Vue3 生产构建通过；六上整册 **92 页 / 16.7MB**（首次 3.2s、缓存 0.23s）、第3课 **4 页 / 736KB**；学生下载接口 403；Playwright 覆盖书架 13 个按钮、教师两处下载、学生按钮隐藏与定位，全部 PASS、0 页面异常，证据 `output/playwright/textbook-pdf-download-20261030/`。
- [ ] 130 发布：随本轮后端一起上线；需确认 `TEXTBOOK_ROOT` 对应用用户可写，或接受每次重新合成。

## 7. 2026-11-02 独立审核修复（本机，未发布）

- [x] 已绑定教材下架或目录加载失败时保留原绑定并提示，不再因打开设计器保存其他内容被静默清空；后端在“提交绑定与库内完全一致”时跳过上下架/目录校验。教材下拉加载加请求序号，迟到响应丢弃。
- [x] `TextbookPdfService` 整册校验页数等于 `total_pages`、课次校验页数等于区间长度且页号连续，缺页直接失败；缓存版本改为全部页哈希，修改中间页也会重建。
- [x] 阅读器“保存本课 PDF”只对 LESSON 可用（单元导入/小结按后端定义无本课 PDF），不再出现点必失败的按钮。
- [x] 验证：新增 `TextbookPdfServicePageValidationTest` 4 项；业务模块 850 项测试通过；Vue3 生产构建通过。
- [ ] 未做：下架写库复现、真实缺页复现、并发 PDF 构建与 130 Nginx 超时/磁盘核对；学生可读整册与打印出口属产品取舍，未改动。

## 8. 2026-11-02 续：PDF 并发与缓存（本机，未发布）

- [x] 全局并发信号量限制同时最多 2 份 PDF 构建，30 秒拿不到许可直接提示“正在生成，请稍后重试”，避免整册 92–125 页解码叠加占用内存与 CPU。
- [x] 缓存每小时最多清理一次：删除超过 14 天的文件，若仍超过 40 份则按最后修改时间删最旧的；缓存键已是全页哈希，改中间页也会重建。
- [x] 验证：业务模块 855 项测试通过；Vue3 生产构建通过。
- [x] 130 发布（2026-11-02，1.30.64）：教材绑定两列随迁移上线（绑定行 0，未使用）；PDF 缓存目录在 `TEXTBOOK_ROOT/.pdf-cache`，服务用户 admin2 可写；接口冒烟教师教材 13 册 200。
- [ ] 未做：130 上真实整册/本课 PDF 并发与 Nginx 超时实测（上线窗口无课堂），建议首次使用时观察缓存目录大小与 30 秒超时提示。
