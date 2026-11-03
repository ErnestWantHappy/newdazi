# 电子教材（教师端）13 册补齐 + P1 修复 —— 代码审核交接提示词

> 用法：把本文件从标题开始全文发给新的 AI。它应先读仓库文档，再按第 9 节清单逐项审查，最后按第 11 节格式输出。
> 本文件只含审核指令，不含密码、Token；账号见 `AGENTS.md` 4.1 或 `contexts/secrets.local.md`，审核报告不得回显。

---

## 0. 你的角色与硬性约束

你是独立代码审计员。本轮任务：**审查「电子教材」这次新增的 4 册导入 + 上一轮 P1 修复是否引入新 bug**，并确认旧问题没有被改坏。

硬性约束：

1. **只读审查**。不要改文件，不要 `git reset/checkout/clean/stash`，不要提交或推送。
2. 不要执行写库 SQL、不要发布、不要重启生产服务。本机只读查询可以；需要启服务时先确认端口占用，不要杀别人的进程。
3. 不要输出任何密码、Token、Cookie、数据库连接串。报告里只写账号名。
4. 结论必须区分 **源码可直接证明** 与 **需要运行时验证的推测**，不要把疑点全写成确定 bug。
5. 先读文档和代码再下结论；文档可能滞后，**以当前源码为准**。
6. 不要审查与本专题无关的改动。工作区有 200+ 未提交文件，其中很多属于其他任务（机位助手 0.2.6、导学单等）。
7. 仓库根目录：`D:\dmwprogram\newdazipingtai\newdazi`

---

## 1. 项目背景（极简）

中小学信息科技教学 + 多维度测评平台。后端 RuoYi / Spring Boot（启动模块 `RuoYi-Vue/ruoyi-admin`，业务 `RuoYi-Vue/ruoyi-business`），前端只认 `RuoYi-Vue3/`（Vue3 + Vite + Element Plus），MySQL，SQL 在 `sql/`。角色：admin / teacher / researcher / student。

本机：后端 `http://localhost:8080`，前端 dev `http://localhost`（Vite 80 代理 8080），本机库 `xueyeceping_server_20260729`。130 是正式主站（Linux/systemd/Nginx），**本轮所有内容都没有发布到 130**。

---

## 2. 本轮改动范围

### 2.1 做了什么

1. **补齐 3–9 年级全部 13 册**：上一版只有 9 册；三下/四上/四下/七下四册 PDF 于 2026-10-29 上午才到位，本轮完成渲染、目录、入库、前端缺册常量清空。
2. **修上一轮审计的 3 个 P1**：失败页永久缓存、打印静默缺页、图片 HTTP 缓存可跨身份复用。
3. 顺带：`request.js` 的 blob 分支尊重 `silentError`；书架缺册常量清空。

### 2.2 明确不做（不要报成 bug）

- 不自动匹配平台课次：进入阅读器**不预选**，必须老师手选；没有课程↔教材绑定表。
- 学生端不做：学生无菜单、接口 403。
- 不提供整册下载、不做全文检索（三下/四上/四下无文本层）。
- 图片不放 `/profile/`（该路径无鉴权），必须走 `/business/textbook/page/**`。
- 若依错误统一 HTTP 200 + 业务码（401/403/500），不是 bug。
- 主教材（小学三册）没有独立「单元小结」页，`summary=null` 是沿用三上口径，不是漏数据。

---

## 3. 文件地图（只看这些）

### 3.1 本轮新增

| 文件 | 说明 |
| :-- | :-- |
| `scripts/textbook/toc/xxkj-3-1-2026.json` | 三下，86 页，偏移 6 |
| `scripts/textbook/toc/xxkj-4-0-2026.json` | 四上，88 页，偏移 6 |
| `scripts/textbook/toc/xxkj-4-1-2026.json` | 四下，86 页，偏移 6 |
| `scripts/textbook/toc/xxkj-7-1-2026.json` | 七下，123 页，偏移 5，**带 `source_page_start:126 / source_page_end:248`** |
| `RuoYi-Vue/ruoyi-business/src/test/java/com/ruoyi/business/controller/TextbookControllerPageResponseTest.java` | 页图响应头单测 3 项 |

### 3.2 本轮修改

| 文件 | 改了什么 |
| :-- | :-- |
| `RuoYi-Vue3/src/api/business/textbook.js` | 失败不入缓存、`loadTextbookPage(bookId,pageNo,force)`、`orphanUrls` 延迟释放、`silentError:true`、`TEXTBOOK_PENDING_VOLUMES=[]` |
| `RuoYi-Vue3/src/views/business/textbook/components/LazyPageImage.vue` | `load(force)`、`reload()` 走 force |
| `RuoYi-Vue3/src/views/business/textbook/reader.vue` | `printSection()` 收集失败页→关窗+报页码+取消打印；`win.closed` 保护 |
| `RuoYi-Vue3/src/utils/request.js` | blob 分支 `else if (!res.config.silentError) ElMessage.error(msg)` |
| `RuoYi-Vue/ruoyi-business/.../controller/TextbookController.java` | 响应头 `private, no-cache` + `Vary: Authorization`（原 `max-age=604800`） |
| `scripts/textbook/import_textbook.py` | `source_page_start/end` 支持合订本；manifest 增同名两字段 |
| `scripts/textbook/verify_pack.py` | 重渲染抽样同样按 `source_page_start` 取样 |
| `scripts/textbook/volumes.json` | 13 册，`missing: []` |
| `sql/textbook_seed_20261029.sql` | 整份重生成：13 册 / 1301 页插入（幂等，无 DELETE） |

### 3.3 既有（上一轮已审过，本轮只做回归）

`TextbookController` / `TextbookCatalogService` / `TextbookStorageService` / `BizTextbookMapper(.xml)` / `views/business/textbook/index.vue` / `router/index.js` / `views/business/teacher/index.vue` 的教材胶囊。

---

## 4. 数据事实（本机库已入库）

- **13 册 / 50 单元 / 334 条目 / 1301 页**；条目分布：LESSON 242、UNIT_INTRO 49、SUMMARY 19、FRONT 13、APPENDIX 11。
- 每册页数：三上 88、三下 86、四上 88、四下 86、五上 84、五下 88、六上 92、六下 84、七上 125、七下 123、八上 118、八下 128、九年级 111。
- 每册课次：小学 18 课，七/八年级 20 课，九年级 18 课。
- `biz_textbook_book.book_id`：三上 1、五上 2、五下 3、六上 4、六下 5、七上 6、八上 7、八下 8、九年级 9、**三下 11、四上 12、四下 13、七下 19**（14–18 被自增消耗，属正常）。
- 图片包：`output/textbook-pack-20261029/pages/` 13 个目录 1301 张，1400px JPEG q75，**243.8 MB**；清单 `manifest.json` / `toc-review.md` / `SHA256SUMS`。
- 入库前备份：`backups/20261029_textbook_13volumes/textbook_before_13volumes.sql`（148,763 B，SHA-256 `b85d640b…`）。
- 130 正式库**尚未**执行 `sql/textbook_v1.sql` 与本轮种子 SQL，也**没有**上传图片。

### 4.1 七下是合订本（重点）

`七年级下册.pdf` 共 248 页，**第 1–125 页其实是七上**（与已入库七上逐课一致），第 126–248 页才是七下。因此：

- `import_textbook.py` 只渲染 126–248 并重新编号 1–123；
- `toc/xxkj-7-1-2026.json` 的 `source_page_start=126`、`source_page_end=248`、`total_pages=123`、`page_offset=5`；
- manifest 记录同样的两字段，`verify_pack.py` 依赖它们取样。

---

## 5. 接口契约（本轮唯一变化在响应头）

| 方法 | 路径 | 说明 |
| :-- | :-- | :-- |
| GET | `/business/textbook/books` | 13 册，含 unitCount/lessonCount/stage |
| GET | `/business/textbook/books/{id}/toc` | book/front/units/appendix/pages |
| GET | `/business/textbook/books/{id}/pages?from&to` | 页元数据（前端目前未调用） |
| GET | `/business/textbook/page/{bookId}/{pageNo}` | image/jpeg |

图片响应头（**本轮改动**）：

```
Cache-Control: private, no-cache      # 原 private, max-age=604800
Vary: Authorization                    # 新增
ETag: "<sha256>"
X-Content-Type-Options: nosniff
```

命中 `If-None-Match` 返回 304。类级鉴权 `@PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")`。

---

## 6. 前端行为（本轮改动后的样子）

### 6.1 图片缓存 `api/business/textbook.js`

- 模块级 `Map`，key `${bookId}/${pageNo}`，上限 80。
- **失败不再留在缓存**：请求 reject 时 `dropEntry(key, entry)`，避免"点击重试"永远命中同一条 rejected promise。
- `loadTextbookPage(bookId, pageNo, force)`：`force=true` 先 `dropEntry` 再重新请求。
- `orphanUrls`：被淘汰/被重试/被替换的 URL 不立即 `revoke`，登记后等 `clearTextbookPageCache()`（离开阅读器）统一释放。
- 请求带 `silentError: true`。

### 6.2 阅读器 `reader.vue`

- `printSection()`：`Promise.all` 收集 `{pageNo, url}`，**任一页失败 → `win.close()` + `ElMessage.error("第 X、Y 页加载失败，已取消打印…")` + return**；成功后才写 `innerHTML` 并 `print()`。
- `onBeforeUnmount` 仍调 `clearTextbookPageCache()`。

### 6.3 书架

- `TEXTBOOK_PENDING_VOLUMES = []`：13 册全上架，不再渲染灰卡（灰卡机制保留）。

---

## 7. 本机运行与复现

### 7.1 两个必踩的坑（已实测）

1. **`SERVER__PORT=50485`**：本机环境有该变量，Spring 会当成 `server.port`，应用起在 50485 后端口冲突直接退出，日志只停在 `DruidDataSource inited` 附近，很像崩溃。**启动必须显式 `-Dserver.port=8080`**。
2. **`TEXTBOOK_ROOT`**：不设则整册 500。

### 7.2 启动

```powershell
cd D:\dmwprogram\newdazipingtai\newdazi\RuoYi-Vue
$env:TEXTBOOK_ROOT='D:\dmwprogram\newdazipingtai\newdazi\output\textbook-pack-20261029\pages'
java -Xms512m -Xmx2048m -Dserver.port=8080 -jar ruoyi-admin\target\ruoyi-admin.jar
```

**注意：后端当前是停止状态**（上一轮打包时停的）。当前 jar：108,155,250 B / 2026-10-29 11:53 / SHA-256 `9a887bc39591e8d4adee78fb14911b4c9a78f16f383a6174f91fbbf1e745a0b9`。

前端：`cd RuoYi-Vue3 && npm run dev`（80 端口）。

### 7.3 工具可用性

- Python 已装 `pymupdf`、`pillow`（本轮 pip 安装）；`scripts/textbook/*.py` 可直接跑。
- **Python 版 Playwright 未安装**；`RuoYi-Vue3/node_modules` 里有 `playwright-core`，用 Node 写验证脚本可行（浏览器二进制已在）。
- 沙箱里 `wmic.exe` 被禁，只影响应用内 LibreOffice 残留进程清理，与教材无关。

### 7.4 账号

教师 `19157727791`（密码见 `AGENTS.md` 4.1），教研员 `laoda`，学生 `2020710101`。首次登录可能弹「选择所属校区」，需选 radio value=`139` 再确定。浏览器验收统一 1440×900。

---

## 8. 已验证证据（不要重复怀疑，除非找到反证）

1. **图片包**：`verify_pack.py` → 13 册 / 1301 张哈希全部一致、26 页抽样重渲染字节完全相同、区间连续。
2. **数据库**：每册页号 1..total_pages 连续、无越界、`bad_range=0`、课次号连续；13/50/334/1301 与上面第 4 节一致。
3. **响应头实测**（新 jar 起服务后 curl）：`Cache-Control: private, no-cache`、`Vary: Authorization`、`ETag`、条件请求 **304**、无 token **401**、`books=13`、单页 197,762 B `image/jpeg`。
4. **后端单测**：`TextbookControllerPageResponseTest` 3/3（no-cache/Vary/ETag/304/陈旧 ETag 回全图）。
5. **浏览器实测**（1440×900）：
   - 书架 13 册、0 张灰卡、13 张封面均 `naturalWidth=1400`；
   - 七下 `read/19`：123 页、28 个可选条目、选「第1课 互联网和物联网」定位第 11 页、打印 **5 页 0 坏图**；
   - 三下 `read/11`：86 页、封面正常；
   - 失败重试：注入一次请求失败 → 行内错误、**0 条全局 toast**、点重试后图片恢复且**确实重发了请求**（同一页命中数 1→2）；
   - 打印缺页：注入永久失败 → 报「第 12 页加载失败，已取消打印…」且空白窗口被关闭；不注入故障时仍打印 4 页无坏图。
6. **构建**：`vite build` EXIT=0；`mvn -pl ruoyi-admin -am clean package -DskipTests` BUILD SUCCESS。

---

## 9. 重点审查清单

请逐项验证；确认是 bug 的给出触发条件，确认不是的说明原因。

### P1 候选（本轮改动最可能出问题的地方）

**P1-1 `force` 重试与在途请求的竞态（源码可证，需实测）**
`textbook.js loadTextbookPage()` + `dropEntry()`。`force=true` 时先把 key 从缓存删掉再发新请求；如果此时**同一个 key 有在途请求**（例如用户快速连点两次"点击重试"，或打印预取与滚动加载撞在同一页），会出现：旧 entry 被删 → 旧请求的 `.then` 仍会 `createObjectURL` 并把 URL 记到已脱离缓存的 entry 上 → 新 entry 又建一个 URL。检查：是否重复发请求、旧 URL 是否只在 `orphanUrls` 里等到卸载才释放（内存是否可控）、`pageCache.get(key) !== entry` 的兜底是否覆盖所有路径。

**P1-2 全册图片失败时没有任何汇总提示（源码可证，UX 风险）**
页面图请求改成 `silentError: true` 后，500/403 不再弹全局 toast。若 `TEXTBOOK_ROOT` 配错或整册文件缺失：toc 能成功、128 页全部显示"本页加载失败，点击重试"，**页面上没有任何"整册不可用/请联系管理员"的说明**，老师只会看到满屏失败格。判断：这算不算回归（修复前至少有 toast）？要不要在阅读器加"连续 N 页失败即整册错误提示"。

**P1-3 打印期间 `clearTextbookPageCache()` 仍会 revoke 打印窗口的 URL（源码可证）**
上一轮已记录为 P2-8，本轮只加了缺页中止，**没有**加打印期间引用计数。若老师在预取期间点"书架"跳走：`onBeforeUnmount` → `clearTextbookPageCache()` + `orphanUrls` 全清 → 打印窗口里的 `<img>` 失效。复现：打印页数多的课，图片未加载完就返回书架，看打印预览是否缺图。

**P1-4 七下合订本偏移是否只在这三处生效（源码可证）**
`source_page_start` 目前被 `import_textbook.py`（渲染/标题校验）、`verify_pack.py`（重渲染抽样）使用。检查是否还有别处按 `doc[pageNo-1]` 取页（例如未来新增脚本、或 `toc-review.md` 的说明会不会误导运维以为要上传 248 页）；以及 `manifest.json` 里 `total_pages=123` 与 `source_bytes` 指向 248 页文件是否会让后来者误判。

### P2 候选

**P2-1 四册新目录的页区间是否可信（需人工抽查）**
三下/四上/四下无文本层，页码来自目录页人工抄录 + 「印刷页 1 = PDF 7（偏移 6）」换算；七下有文本层，20 课按「第N课 课名」逐页校验。建议抽查：三下第 10 课（44–48）、四上第 9 课（40–44）、四下第 14 课（63–67）、七下第 14 课（83–88）的起始页是否真的是该课首页；以及三册小学教材的**单元导入是否真的各占 1 页**（若某单元实际 2 页，会把上一课末尾算进导入页）。

**P2-2 `orphanUrls` 只在离开阅读器时清空（源码可证）**
只看书架不进阅读器的用户，13 张封面 URL 会一直留到刷新页面；`clearTextbookPageCache` 只在 `reader.vue onBeforeUnmount` 调用。判断泄漏量级与是否值得在书架 `onBeforeUnmount` 也清一次（注意别把正在用的封面上 URL 撤掉）。

**P2-3 `no-cache` 后的重复校验成本（需实测）**
80 页 LRU + `no-cache`：滚过 80 页再回滚，每次都要 304 往返。低配教师机 + 弱网下翻页是否会明显变慢？对比 `max-age` 版本体感。若明显变慢，给出折中（如 `max-age=0, must-revalidate` 与 `no-cache` 的差别、或按 `sha256` 换 URL 后恢复长缓存）。

**P2-4 种子 SQL 仍是"只增不删"（源码可证，本轮风险上升）**
13 册全量 1301 条 `INSERT ... ON DUPLICATE KEY UPDATE`，没有 DELETE。本轮**没有**发生目录删减，所以本机无残留；但四册新目录若将来修正页区间（例如某课多算 1 页），旧 `section_key` 不会消失、旧 `page_no` 也不会删。检查：`requirePage` 仍无 `pageNo <= totalPages` 上界，改小 `total_pages` 后旧页是否还能取到。

**P2-5 前端缺册常量清空后的空数组路径（源码可证）**
`TEXTBOOK_PENDING_VOLUMES = []`，`index.vue` 里 `build()` 的 `pending` 过滤、`v-if="group.books.length || group.pending.length"`、搜索关键字过滤是否都还能正常工作（关键字只匹配灰卡时会不会出现空白分组）。

**P2-6 课程卡「教材」胶囊的命中率（需数据核对）**
现在 13 册全上架，`grade + semester` 精确匹配应全部命中（九年级回退 `9`）。核对 `biz_lesson` 里是否存在 `grade` 不在 3–9 或 `semester` 为 NULL/异常的课程，用它们点胶囊看提示是否合理。参考只读 SQL：
`SELECT grade, IFNULL(semester,'NULL'), COUNT(*) FROM biz_lesson GROUP BY grade, semester;`

### P3 候选

- 阅读器仍把整册最多 128 页 `<img>` 常驻 DOM（旧 P2-7，未修）：滚完整册的内存表现，是否需要窗口化。
- `stageOf(null)` 仍归小学；`requirePage` 仍无上界；`/pages` 接口与 `listTextbookPages` 仍是死代码；`selectBookById` 每次取图仍跑两次 COUNT 子查询；`Files.copy` 在响应已提交后 IO 异常会截断；ETag 只做强比较不认 `W/`。以上都是旧问题，只确认是否因本轮改动而恶化。
- 前端仍无单元测试（`node --test` 只覆盖 `loginIdleWork` 等），`force` 重试与 LRU 淘汰没有自动化回归。

---

## 10. 不要误报的点

- 类级 `@PreAuthorize` 对所有方法生效，不是"漏加注解"。
- 页面图 HTTP 200 + `code:500` 是若依全局异常处理语义。
- toc 一次返回整册页元数据（≤128 条）是有意为之。
- 不建课程↔教材绑定表、不预选课次、学生端不做、不能整册下载、不做全文检索，都是需求边界。
- 小学三册没有独立「单元小结」页（`summary=null`）是内容现状。
- `book_id` 出现 11/12/13/19 的跳号是自增被 `ON DUPLICATE KEY UPDATE` 消耗，不是数据错误。
- 七下 `total_pages=123` 而源 PDF 248 页，是合订本只取后半，不是缺页。

---

## 11. 输出格式

中文，先结论后细节。

### 11.1 问题表

| ID | 严重级别 | 位置（文件:函数/行） | 结论（源码可证/需实测） | 触发条件 | 影响 | 建议修复 | 验证方法 |
| :-- | :-- | :-- | :-- | :-- | :-- | :-- | :-- |

级别定义：P0 数据泄露/整模块不可用；P1 明确功能缺陷或安全风险；P2 边界/性能/维护风险；P3 体验/清理项。

### 11.2 最可能被老师遇到的 3 个问题

按真实路径排序（打开书架 → 选册 → 选课 → 翻页 → 打印 → 从课程卡进入），写清现象与临时规避。

### 11.3 发布 130 前的阻断清单

1. 本轮 P1 里哪些必须发布前修，哪些可以发布后观察；
2. 130 执行两份 SQL 前必须做的检查（菜单 26100 冲突、角色、整库备份、单会话执行种子）；
3. 243.8MB 图片的上传方式、属主、`TEXTBOOK_ROOT`、回滚；
4. 教师/教研员/学生三种角色的验收步骤。

### 11.4 最终结论

「建议发布」/「修复 P1 后发布」/「不建议发布」，理由不超过 5 条；若建议继续修，给出最小修复顺序（不要直接改代码）。

---

## 12. 审核结束后的交接

- 若用户要求修复：只改与本专题相关的文件，每项修复补对应测试，不顺手重构，不动 `RuoYi-Vue/ruoyi-ui`。
- 修复后按 `AGENTS.md` 第 6 节顺序验证：编译/单测 → 接口探活 →（可选）浏览器冒烟。
- 若改变接口语义、表结构、权限或部署方式，同步 `contexts/PROJECT_CORE.md`、`contexts/textbook-module/*`、`docs/architecture/` 与 ADR-004。
- 未实际部署前，不得在 `contexts/RELEASE_LOG.md` 登记为已发布，也不得把 `biz_platform_update` 标成 PUBLISHED。
- 报告与提交信息里不得出现密码、Token、Cookie。

---

## 附：本轮最值得先看的 4 个点

1. `loadTextbookPage(force)` 与在途请求的竞态：会不会重复拉图、旧 URL 泄漏到卸载才释放。
2. 全册图片失败时没有任何汇总提示（`silentError` 之后老师只会看到满屏"本页加载失败"）。
3. 打印预取期间离开阅读器，`clearTextbookPageCache()` 仍会 revoke 打印窗口正在用的 blob。
4. 四册新目录的页区间是人工换算的，尤其三下/四上/四下没有文本层，只能靠渲染核对——抽查课次首页是否真的对得上。
