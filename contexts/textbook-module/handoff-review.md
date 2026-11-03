# 电子教材（教师端）代码审核交接提示词

> 用法：把本文件从标题开始全文发给新的 AI。它应先读仓库文档，再按第 9 节清单逐项审查，最后按第 11 节格式输出。
> 本文件只含审核指令，不含密码、Token；账号密码见 `AGENTS.md` 4.1 或 `contexts/secrets.local.md`，审核报告不得回显。

---

## 0. 你的角色与硬性约束

你是独立代码审计员，任务是找出「电子教材（教师端）」实现中的真实 bug、权限/数据风险、边界错误和明显性能问题。

硬性约束：

1. **只读审查**。不要修改任何文件，不要 `git reset` / `git checkout` / `git clean` / `git stash`，不要提交或推送。
2. **不要执行写库 SQL、不要发布、不要重启生产服务**。本机只读查询可以；如需启动本机服务，先确认端口占用，不要杀别人的进程。
3. **不要输出任何密码、Token、Cookie、数据库连接串**。需要账号时读 `AGENTS.md` 4.1 或 `contexts/secrets.local.md`，报告里只写账号名。
4. 结论必须区分 **“源码可直接证明”** 与 **“需要运行时验证的推测”**；不要把所有疑点都写成确定 bug。
5. 先读文档和代码，再下结论；文档可能滞后，**以当前源码为准**。
6. 不要审查与本专题无关的改动。工作区有 234 个未提交文件，其中很多属于其他任务。

仓库根目录：`D:\dmwprogram\newdazipingtai\newdazi`

---

## 1. 项目背景

这是一个中小学**信息科技教学 + 多维度测评**平台，包含选择/判断/操作/打字等题型、批改、学情、导学单、区域抽测等模块。

技术栈与目录：

| 部分 | 说明 |
| --- | --- |
| 后端 | RuoYi / Spring Boot，Maven monorepo；启动模块 `RuoYi-Vue/ruoyi-admin`，业务模块 `RuoYi-Vue/ruoyi-business` |
| 前端 | **只维护** `RuoYi-Vue3/`（Vue3 + Vite + Element Plus）；`RuoYi-Vue/ruoyi-ui` 是旧 Vue2，本项目不认 |
| 数据库 | MySQL；本机库 `xueyeceping_server_20260729` |
| SQL | 仓库根目录 `sql/`，增量脚本优先 |
| 当前上下文 | `contexts/PROJECT_CORE.md`；架构入口 `docs/architecture/INDEX.md` |
| 正式环境 | 130 为当前正式主站，当前版本 1.30.60（`20261028_fullstack_v1`）；123 是旧 Windows 发布栈，命令不可混用 |

角色：`admin`（管理员）、`teacher`（教师）、`researcher`（教研员）、`student`（学生）。前端角色键与后端 `sys_role.role_key` 一致。

本机启动约定：

- 后端 `http://localhost:8080`
- 前端 `http://localhost`（Vite 80 端口，代理 8080）
- 本机后端启动前必须设置 `TEXTBOOK_ROOT`，否则教材图片 500

---

## 2. 本专题目标与明确边界

### 2.1 目标

把信息科技 3–9 年级电子教材接入教师端：

1. 教师侧边菜单新增顶级入口「电子教材」→ 书架 `/textbook`。
2. 书架按小学/初中分组展示已上架册，点封面进入阅读器。
3. 阅读器连续滚动整册页面，支持目录跳转、页码跳转、**手动选择课次**、打印本课。
4. 教师首页课程卡顶部增加「教材」小胶囊，按课程年级+学期直达对应册。
5. 页面图必须鉴权后返回，不提供整册下载，不做全文检索。

### 2.2 明确不做（不要报成 bug）

- **不自动匹配平台课次**：进入阅读器不预选课次，必须老师手动选；教材与平台课程没有数据库绑定表。
- **学生端不做**：学生不应看到菜单，也不应能拿到图片；学生直接访问接口应 403。
- **不提供整册 PDF 下载**、不做全文检索（三上 PDF 无文本层）。
- **4 上、4 下、7 下三册原始 PDF 未提供**，书架保持「暂未上架」灰卡，这是数据现状。
- **九年级原始 PDF 缺印刷页 31–34**：已在 `toc-review.md` 标注，用 `offset_breaks` 处理，不允许“修正”数据掩盖。
- 图片**不放** `/profile/`：该路径由静态资源直接暴露、无鉴权；必须走 `/business/textbook/page/**`。
- 若依错误统一 **HTTP 200 + 业务码**（401/403/500），这不是 bug。

### 2.3 关键设计决策

- 4 张表：`biz_textbook_book` / `biz_textbook_unit` / `biz_textbook_section` / `biz_textbook_page`，**没有课程↔教材绑定表**。
- 册编码 `xxkj-<grade>-<semester>-2026`；`semester`：`0` 上册、`1` 下册、`9` 全一册。
- 条目类型 `kind`：`FRONT` / `UNIT_INTRO` / `LESSON` / `SUMMARY` / `APPENDIX`；单元导入单独成条，避免“选第 1 课却从导入页开始”。
- 图片压缩：宽 1400px、JPEG q75，918 页约 171.3 MB；生产放 release 之外的共享目录，切换版本不重复上传。
- 单页接口用页内容 `sha256` 作 ETag，支持 `If-None-Match` → 304，`Cache-Control: private, max-age=604800`。
- 菜单 `menu_id=26100`、`parent_id=0`、`path=textbook`、`component=business/textbook/index`、`perms=business:textbook:query`、`icon=education`，授权 `admin/teacher/researcher`。
- 阅读器是静态隐藏路由 `/business/textbook/read/:bookId(\d+)`，`activeMenu: '/textbook'`；不建第二个隐藏菜单。
- 前端页面图统一用 axios `responseType: 'blob'` 取回，再 `URL.createObjectURL`；模块级 LRU 80 页；不能用 `<img src>` 直连接口（无 Authorization 会 401）。
- 「打印本课」用 `window.open` 新窗口写入本课页面图后 `print()`，避免侧边栏/工具栏入纸。

---

## 3. 本专题文件地图

### 3.1 SQL

- `sql/textbook_v1.sql`：4 张表 DDL + 菜单 26100 + 三角色授权，幂等。
- `sql/textbook_seed_20261029.sql`：9 册 / 34 单元 / 233 条目 / 918 页种子，`ON DUPLICATE KEY UPDATE`，**没有 DELETE/TRUNCATE**。

### 3.2 后端（新增/修改）

新增：

- `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/domain/BizTextbookBook.java`
- `.../domain/BizTextbookUnit.java`
- `.../domain/BizTextbookSection.java`
- `.../domain/BizTextbookPage.java`
- `.../mapper/BizTextbookMapper.java`
- `.../service/TextbookCatalogService.java`
- `.../service/TextbookStorageService.java`
- `.../controller/TextbookController.java`
- `RuoYi-Vue/ruoyi-business/src/main/resources/mapper/business/BizTextbookMapper.xml`

修改（本专题相关部分）：

- `RuoYi-Vue/ruoyi-admin/src/main/resources/application.yml`：新增
  ```yaml
  textbook:
    root: ${TEXTBOOK_ROOT:./textbook-data}
  ```
- `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/domain/vo/LessonInfoVo.java`：补 `semester`。
- `RuoYi-Vue/ruoyi-business/src/main/resources/mapper/business/BizLessonMapper.xml`：resultMap + 3 个 select 补 `semester`。
- 注意：`BizLessonController.java`、`BizLessonServiceImpl.java`、`BizLessonAssignmentMapper.java/.xml` 也有未提交改动，但**不一定属于本专题**；不要把它们当教材模块审查对象。

### 3.3 前端（新增/修改）

新增：

- `RuoYi-Vue3/src/api/business/textbook.js`
- `RuoYi-Vue3/src/views/business/textbook/index.vue`（书架）
- `RuoYi-Vue3/src/views/business/textbook/reader.vue`（阅读器）
- `RuoYi-Vue3/src/views/business/textbook/components/LazyPageImage.vue`

修改：

- `RuoYi-Vue3/src/router/index.js`：隐藏阅读器路由。
- `RuoYi-Vue3/src/views/business/teacher/index.vue`：课程卡「教材」胶囊 + `openTextbook()`。

### 3.4 脚本、内容资产与文档

- `scripts/textbook/volumes.json`：9 册源文件/年级学期/排序/缺册清单。
- `scripts/textbook/toc/*.json`：逐册人工核对后的单元/课次 PDF 页区间。
- `scripts/textbook/import_textbook.py`：PDF → 1400px JPEG + manifest + 种子 SQL。
- `scripts/textbook/verify_pack.py`：页数/哈希/区间校验，支持抽样重渲染。
- `scripts/textbook/smoke_teacher_textbook.py`：教师端 12 项 Playwright 冒烟。
- `output/textbook-pack-20261029/pages/`：918 张页面图（171.3 MB，本机产物）。
- `contexts/textbook-module/requirements.md` / `design.md` / `tasks.md`：需求、设计、任务与验证。
- `docs/architecture/ADR/ADR-004-textbook-page-images-outside-release.md`：页图放 release 外、鉴权 + ETag、不建绑定表的取舍。
- `contexts/PROJECT_CORE.md`：v4.73（2026-10-29）已登记本专题状态。

---

## 4. 数据与内容事实

- 9 册：三上 88、五上 84、五下 88、六上 92、六下 84、七上 125、八上 118、八下 128、九年级 111，共 **918 页**。
- 本机库已执行 `sql/textbook_v1.sql` + `sql/textbook_seed_20261029.sql`，并复核：
  - 9 册 / 34 单元 / 233 条目 / 918 页；
  - 每册页号连续 `1..total_pages`，无缺页；
  - 每册课次编号 `1..N` 连续；
  - 无倒置区间（`bad_sections=0`）；
  - 条目分布：`LESSON 168 / UNIT_INTRO 33 / SUMMARY 15 / FRONT 9 / APPENDIX 8`。
- **130 正式环境尚未执行这两份 SQL，也没有上传图片**；不要把本机状态当成正式状态。
- 九年级 PDF 缺印刷页 31–34：`offset_breaks` 从 PDF 第 36 页起把偏移由 5 改为 1；第 6 课从 PDF 第 36 页（印刷页 35）开始。
- 三上 PDF 无文本层，所以不做全文检索。

---

## 5. 后端接口契约

统一类级鉴权：

```java
@RestController
@RequestMapping("/business/textbook")
@PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")
public class TextbookController
```

| 方法 | 路径 | 返回 |
| --- | --- | --- |
| GET | `/business/textbook/books` | 上架册列表，含 `unitCount` / `lessonCount` / `stage` |
| GET | `/business/textbook/books/{bookId}/toc` | `{ book, front, units, appendix, pages }` |
| GET | `/business/textbook/books/{bookId}/pages?from&to` | 页元数据闭区间；`from/to` 可省略，越界收敛 |
| GET | `/business/textbook/page/{bookId}/{pageNo}` | `image/jpeg`；`ETag: "<sha256>"`；`If-None-Match` 命中返回 304 |

`toc` 结构：

- `book`：册实体；
- `front` / `appendix`：`FRONT` / `APPENDIX` 条目；
- `units[]`：`{ unitId, unitNo, title, intro, lessons[], summary }`；`lessons` 按 `sort_no`；
- `pages[]`：仅 `{ pageNo, printedNo, width, height }`，用于渲染占位和页脚，不返回 `imagePath`。

图片响应：

- `Content-Type: image/jpeg`
- `Content-Length` 取真实文件大小
- `ETag` 用页 `sha256`（带双引号）
- `Cache-Control: private, max-age=604800`
- `X-Content-Type-Options: nosniff`
- 页不存在/越界抛 `ServiceException`，由若依全局异常处理返回 **HTTP 200 + `code:500` JSON**。

已实测（教师 `19157727791`）：

| 检查 | 结果 |
| --- | --- |
| `books` | `code=200`，9 册 |
| `/books/6/toc` | `code=200`，4 单元 / 125 页 |
| `/books/6/pages?from=10&to=14` | `code=200`，5 条，`width=1400` |
| `/page/6/12` | `image/jpeg`，197762 字节，带 ETag |
| 同页带 `If-None-Match` | **304** |
| 无 token | `code:401` |
| 学生 `2020710101` | `code=403 没有权限` |
| 不存在册/越界页 | `code=500` 业务错误 |

---

## 6. 前端行为与缓存

### 6.1 书架 `index.vue`

- 小学（3–6）/ 初中（7–9）分组；已上架册显示封面（鉴权 blob）、短名、「N 单元 · N 课」。
- 静态缺册常量 `TEXTBOOK_PENDING_VOLUMES` 显示 4 上、4 下、7 下三张灰卡。
- 点卡片 `router.push('/business/textbook/read/' + bookId)`。
- 搜索只匹配标题/短名/`第N册`，不搜正文。

### 6.2 阅读器 `reader.vue`

- 左侧目录树（前置、单元导入、课次、小结、附录），中间连续滚动，顶部工具条，底部状态栏。
- 进入时 `selectedKey=''`，**不预选课次**，显示「请先选择课次」。
- 下拉 `el-select` 按单元分组，可搜索/清空；选中后 `goToPage(section.pdfStart)`。
- 页码跟踪：`IntersectionObserver` 窄带 `rootMargin: '-6% 0px -86% 0px'`，平滑滚动后约 2 秒稳定。
- 底部：`第 N 页 · 第X课 名称（印刷页 a–b）`。
- 打印：取 `currentSection.pdfStart..pdfEnd`，`Promise.all(loadTextbookPage(...))`，把 blob URL 写入新窗口的 `.sheet img`，图片加载完成后 `print()`。

### 6.3 图片缓存 `api/business/textbook.js`

- 模块级 `Map`，key 为 `${bookId}/${pageNo}`，上限 80。
- 命中后 `delete` + `set` 移到队尾，实现 LRU。
- 未命中时 `request({ responseType: 'blob' })`，成功后 `URL.createObjectURL`。
- `clearTextbookPageCache()` 在阅读器 `onBeforeUnmount` 调用。
- 失败时 `entry.promise` 仍留在 `Map` 中（这是重点审查点）。
- 请求没有设置 `silentError: true`（这是重点审查点）。

### 6.4 课程卡胶囊 `teacher/index.vue`

- 每张课程卡顶部渲染「教材」小胶囊，`@click.stop`。
- `openTextbook(lesson)`：
  - `grade = Number(lesson.grade)`；
  - `semester = String(lesson.semester ?? '')`；
  - 拉 `listTextbookBooks()`；
  - 先精确匹配 `grade + semester`，再回退 `semester='9'`（九年级全一册）；
  - 未命中提示并跳 `/textbook`；
  - 命中跳 `/business/textbook/read/{bookId}`。

---

## 7. 本机运行与复现

后端工作目录必须是 `RuoYi-Vue`：

```powershell
$env:TEXTBOOK_ROOT='D:\dmwprogram\newdazipingtai\newdazi\output\textbook-pack-20261029\pages'
java -Xms512m -Xmx2048m -jar RuoYi-Vue\ruoyi-admin\target\ruoyi-admin.jar
```

前端：

```powershell
cd RuoYi-Vue3
npm run dev
```

冒烟：

```powershell
$env:PYTHONIOENCODING='utf-8'
python scripts/textbook/smoke_teacher_textbook.py
```

账号（**密码不要写进报告**）：

- 教师 `19157727791`；密码见 `AGENTS.md` 4.1 或 `contexts/secrets.local.md`。
- 教研员 `laoda / 123456`。
- 学生 `2020710101 / 123456`。
- 首次登录会弹「选择所属校区」，需选 radio `value='139'` 再点「确定」，否则停在 `/login`。

浏览器验收统一 1440×900；页面滚动追踪用条件等待，不要用固定延时。Playwright 产物在 `output/playwright/20261029-textbook-smoke/`。

---

## 8. 已验证证据（不要重复怀疑，除非找到反证）

- 后端全量单测：`mvn -pl ruoyi-business -am test -DforkCount=0` → **822 项，0 失败，0 错误，3 跳过，BUILD SUCCESS**（2026-10-29 09:29:20）。
- 后端打包：`mvn -pl ruoyi-admin -am package -DskipTests` 成功。
- 前端构建：`npm run build:prod` 成功（EXIT=0，`reader-DeOEIPUC.js`）。
- Playwright 教师端 12 项全部 PASS：
  1. 教师登录并进入首页
  2. 课程卡出现 18 个教材胶囊
  3. 侧边菜单存在 `/textbook`
  4. 书架 9 册 + 3 册待上架
  5. 封面按鉴权 blob 渲染
  6. 打开七上阅读器 `read/6`
  7. 进入时不预选课次
  8. 总页数 `/ 125`
  9. 手选第 1 课定位到第 11 页
  10. 打印本课生成 4 页
  11. 滚动后页码跟随到第 60 页
  12. 课程卡胶囊直达六上 `read/4`
- 数据校验：918 张哈希一致；抽样 18 页重渲染字节相同；页号/课次连续；无倒置区间。

这些证据只说明主流程可用，**不代表边界、缓存、并发、权限和生产部署没有问题**。

---

## 9. 重点审查清单

> 下面按优先级列出源码中已经能看到的疑点。请逐项验证；确认是 bug 的给出触发条件，确认不是的也要说明原因。

### P1-1 失败页被永久缓存，重试按钮无效（源码可证）

位置：

- `RuoYi-Vue3/src/api/business/textbook.js` `loadTextbookPage()` / `evictOldest()` / `clearTextbookPageCache()`
- `RuoYi-Vue3/src/views/business/textbook/components/LazyPageImage.vue` `load()` / `reload()`

现象/原因：

- `loadTextbookPage` 把 `entry.promise` 放进 `pageCache`；请求失败时 promise reject，但 `entry` 没有被删除。
- `LazyPageImage` 的「本页加载失败，点击重试」调用 `reload()` → `load()` → `loadTextbookPage()`，命中同一个 rejected promise，立即再次失败；Network 面板不会再发请求。
- 只有离开阅读器触发 `clearTextbookPageCache()`，或该页被 LRU 淘汰，才可能恢复。

复现：

1. 停后端或临时把某页图片改名；
2. 打开该页，看到失败态；
3. 恢复后端/文件，点「点击重试」；
4. 仍然失败，且没有新请求。

建议：

- 失败时从 `pageCache` 删除对应 key（注意只删当前 entry，避免删掉后来者的新 entry）；
- 或缓存 `{ promise, error }` 并允许强制重试；
- 补一个「失败后可重试」的前端测试。

### P1-2 「打印本课」静默缺页（源码可证）

位置：`RuoYi-Vue3/src/views/business/textbook/reader.vue` `printSection()`

现象/原因：

```js
const urls = await Promise.all(
  ranges.map(pageNo => loadTextbookPage(bookId.value, pageNo).catch(() => ''))
)
```

单页失败被 `.catch(() => '')` 吞掉，`printSection` 仍然生成文档并调用 `print()`；打印结果看起来成功，但少页，且没有明确的缺页提示（只有请求拦截器可能弹全局 toast）。

复现：让本课中某一页 500/文件缺失，点「打印本课」，检查打印预览页数和页面内容。

建议：收集失败页码；有失败时中止打印并提示「第 X、Y 页加载失败，请重试」，或至少在本课标题下标注缺页。

### P1-3 图片 HTTP 缓存可能跨账号复用/长期不更新（需运行时验证）

位置：`RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/controller/TextbookController.java` 图片响应头

现象/原因：

- 响应是 `Cache-Control: private, max-age=604800`，URL 固定，未设置 `Vary: Authorization`。
- `private` 只阻止共享代理缓存，**不阻止同一浏览器本地缓存**。
- 同一台教师机/学生机上，教师看过图片后退出，另一个账号登录，浏览器可能直接用本地缓存返回图片，不再请求服务器，从而绕过接口鉴权。
- 另外，页面图修正后（同 URL、同 pageNo，但 `sha256` 已变），浏览器在 7 天新鲜期内不会回源，教师看不到修正。

验证：

1. 教师账号打开某页，确认响应进入 disk cache；
2. 退出，学生账号登录同一浏览器；
3. 用 `fetch('/business/textbook/page/6/12', { headers: { Authorization: studentToken } })` 或直接访问阅读器，观察是否命中缓存、服务器是否收到请求；
4. 替换服务器图片文件并更新 DB `sha256`，教师端是否仍显示旧图。

建议：

- 改为 `Cache-Control: private, no-cache`（每次回源校验，304 仍然省流量），并加 `Vary: Authorization`；
- 或缩短 `max-age`，并在图片更新时换 URL/加版本参数；
- 若产品接受 7 天缓存，需要在安全评审中明确“共用电脑”的风险。

### P1-4 LRU 淘汰与在途/已显示图片存在撤销竞态（源码可证，需复现）

位置：`RuoYi-Vue3/src/api/business/textbook.js` `evictOldest()`

现象/原因：

- 淘汰时如果 `entry.url` 尚未生成，会挂 `entry.promise.then(url => URL.revokeObjectURL(url))`。
- 正在等待同一 promise 的 `LazyPageImage` 可能在 revoke 之后才把 URL 设到 `<img src>`，导致图片加载失败。
- 已显示但仍在 DOM 中的图片被 revoke 后，浏览器在内存压力下重新解码时也可能失败。
- 上限 80 页，正常单册最多 128 页；快速滚动整册或打印较大区间时可能触发。

复现：DevTools 网络限速；快速滚动超过 80 页，或连续打印多个课次；观察是否出现随机「本页加载失败」。

建议：

- 缓存项加引用计数：`acquire/release`，只有没有消费者时才 revoke；
- 或淘汰时先标记，等 `nextTick` / 图片 `load` 后再 revoke；
- 不要把仍在 DOM 中显示的 URL 直接撤销。

### P2-1 切换册次时 `load()` 无竞态保护（源码可证）

位置：`RuoYi-Vue3/src/views/business/textbook/reader.vue` `load()` + `watch(route.params.bookId)`

现象/原因：

- `watch` 触发 `load()`，但没有请求序号/取消机制；快速从 A 册切到 B 册时，A 的响应可能晚到并覆盖 B 的 `book/units/pages`。
- `loading` / `errorText` 也可能被旧请求改写。

复现：网络限速，快速修改 URL 或从书架连续打开两册，观察标题/目录/页数是否错乱。

建议：为每次 `load` 分配 `seq`，响应落地前校验；或使用 `AbortController` 取消旧请求。

### P2-2 种子 SQL 没有删除逻辑，目录修正会残留（源码可证）

位置：`sql/textbook_seed_20261029.sql`

现象/原因：

- 全部是 `INSERT ... ON DUPLICATE KEY UPDATE`，没有 `DELETE`/`TRUNCATE`。
- 如果 TOC JSON 删掉一个课次、调整 `section_key`、减少页数，旧行仍留在库里：
  - 旧 `section` 会继续出现在 `toc`（未知 kind 还会被当 LESSON）；
  - 旧 `page` 行会留在 `biz_textbook_page`；`requirePage` 只按 `book_id + page_no` 查，不校验 `pageNo <= totalPages`，可能仍能取到已下架的旧页。
- 重跑种子不会自动清理这些残留。

复现：从某册 TOC 删除一课，重新生成并执行种子；查 `biz_textbook_section` 是否还有旧 `section_key`；直接请求旧页看是否仍返回图片。

建议：

- 导入时按册做「当前 manifest 集合」的对账删除（先备份）；
- 或引入 `import_batch` / 版本号，查询只取当前批次；
- 至少在 `requirePage` 加 `pageNo <= book.totalPages` 的上界校验。

### P2-3 菜单 SQL 的幂等边界可能导致菜单缺失或误改（源码可证）

位置：`sql/textbook_v1.sql` 第 95–126 行

现象/原因：

- `INSERT ... SELECT 26100 ... WHERE NOT EXISTS (menu_id=26100 OR path='textbook' OR component='business/textbook/index')`。
- 如果库里已存在一个 **path 为 `textbook`、但 menu_id 不是 26100、component 也不是目标值** 的菜单：
  - INSERT 被跳过；
  - 后面的 UPDATE 只匹配 `menu_id=26100 OR component=...`，不会创建 26100；
  - `sys_role_menu` 又只 JOIN `menu_id=26100`，导致授权 0 行，教师看不到菜单。
- 反过来，如果 26100 已被其他功能占用，INSERT 被跳过，UPDATE 会把 26100 改成电子教材，可能破坏原功能。

验证：在测试库构造上述两种数据，执行脚本，检查 `sys_menu` 和 `sys_role_menu` 的实际行数。

建议：

- 先显式检查 ID 冲突，冲突就报错停止，不要静默跳过；
- 用确定的 menu_id + `ON DUPLICATE KEY UPDATE`，并在脚本末尾输出复核结果；
- 角色授权按实际 `menu_id` JOIN，不要假设一定是 26100。

### P2-4 接口直接序列化实体，泄露内部字段（源码可证）

位置：

- `TextbookCatalogService.listBooks()` / `getToc()` 返回 `BizTextbookBook` 实体；
- `TextbookController.books()` / `toc()` 直接 `AjaxResult.success(entity)`；
- `BizTextbookBook` 没有 `@JsonIgnore`。

现象：`books` 和 `toc.book` 会返回 `sourceFile`、`sourceSha256`、`status`、`pageOffset`、`sortNo`、`importedAt`、`createTime`、`updateTime`；`/pages` 还会返回 `imagePath`、`sha256`、`bytes`。这些不是密码，但暴露了源 PDF 文件名/哈希、存储相对路径和导入时间；以后实体加字段会自动泄露。

建议：为对外接口建 VO（只暴露 `bookId/bookCode/title/shortTitle/grade/semester/totalPages/unitCount/lessonCount` 等），或对内部字段加 `@JsonIgnore`。

### P2-5 菜单权限串与接口鉴权模型不一致（源码可证）

位置：

- `sql/textbook_v1.sql`：`perms='business:textbook:query'`
- `TextbookController`：类级 `@PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")`

现象：

- 菜单权限串是 `business:textbook:query`，但接口只认角色键，不认这个权限串。
- 如果以后给某个自定义角色只授菜单/权限、不给 `teacher` 角色键，菜单能看到但接口 403；
- 反过来，教师角色即使菜单被撤，接口仍然可访问（只要角色还在）。

建议：二选一并写进文档：
- 改为 `@PreAuthorize("@ss.hasPermi('business:textbook:query')")`，按权限授权；
- 或明确“本模块只按角色开放”，删掉/忽略菜单 perms，并补三角色权限测试（admin/researcher 目前没有浏览器冒烟）。

### P2-6 教材根目录缺少启动校验，符号链接可绕过根目录（源码可证）

位置：`RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/service/TextbookStorageService.java`

现象：

- `getRoot()` 每次请求才 `Paths.get(root).toAbsolutePath().normalize()`；根目录不存在/不可读时应用仍能启动，直到第一张图才 500。
- `resolve()` 做了 `..` 越界拦截，但 `Files.isRegularFile(target)` 会跟随符号链接；如果教材根目录内存在指向外部的 symlink，可能读到根目录外的文件（需要能写该目录的人才可利用，属于防御纵深）。

建议：

- `@PostConstruct` 校验根目录存在、是目录、可读，失败直接启动失败或输出醒目错误；
- 用 `toRealPath()` 对根目录和目标文件做真实路径比较；
- 补路径穿越/符号链接单测。

### P2-7 阅读器保留全部 `<img>` 节点，LRU 只释放 URL，不释放已解码图像（需实测）

位置：`reader.vue` 的 `v-for="page in pages"` + `LazyPageImage.vue` 的 `<img v-else-if="src">`

现象：

- 整册 125–128 页全部渲染为 DOM 节点；滚动过的页面 `<img>` 仍然保留。
- LRU 只 `revokeObjectURL`，不会卸载 `<img>`，也不会清空 `src`；已解码位图可能继续占用内存（浏览器是否回收取决于内存压力）。
- 教师机上长时间滚动整册，可能出现内存上涨/卡顿。

验证：打开 128 页的八下，滚动到底再回到顶部，DevTools Memory 快照对比；或在低配机器上观察。

建议：虚拟列表/窗口化，或对远离视口的页面卸载图片（保留占位高度），不要只靠 revoke URL。

### P2-8 打印期间离开阅读器会撤销打印窗口仍在用的 blob（源码可证）

位置：`reader.vue` `onBeforeUnmount()` 调用 `clearTextbookPageCache()`

现象：`printSection` 打开的新窗口持有 blob URL；如果用户在图片完全加载/打印前点「书架」或路由跳走，`onBeforeUnmount` 会 revoke 这些 URL，打印窗口可能缺图。

建议：打印期间引用计数/延迟清理；或在 `printSection` 完成（或打印窗口关闭）后再允许清缓存。

### P2-9 课程 `semester` 为空时胶囊可能误报“未上架”（需数据核对）

位置：`teacher/index.vue` `openTextbook()`

现象：

- 只做 `grade + semester` 精确匹配，再回退 `semester='9'`。
- 历史课程如果 `semester` 为 `NULL`/空串，除九年级外都不会命中，即使该年级上册/下册教材已上架，也会提示「该年级的教材还未上架」。

验证：在目标库执行只读查询：

```sql
SELECT grade, semester, COUNT(*) AS cnt
FROM biz_lesson
GROUP BY grade, semester
ORDER BY grade, semester;
```

重点看 `semester IS NULL OR semester=''` 的课程，并用其中一条课程打开教师首页点胶囊。

建议：数据回填；或匹配失败时按年级唯一册回退；或提示「请到书架选择」而不是「未上架」。

### P3-1 选中课次后跳页/滚动会把其他页整体变暗（UX，源码可证）

位置：`reader.vue` `isOutsideSection()` + `.page-wrap.is-outside { opacity: 0.55 }`

现象：选中第 1 课后，用页码跳到第 60 页，第 60 页及之后全部半透明；用户可能以为页面不可用。若这是刻意“聚焦本课”，需要明确的开关/说明。

建议：跳页时清除选择，或把变暗改成可关闭的聚焦模式，或去掉整体变暗。

### P3-2 图片请求失败会触发全局 toast 风暴（源码可证）

位置：`textbook.js` 的 `request({ responseType: 'blob' })` 没有 `silentError`

现象：每张失败页都会经过 `request.js` 的 500/非 200 分支弹一次 `ElMessage`；一册多页缺失时会连续弹很多条。`LazyPageImage` 本身已有行内失败态。

建议：页面图请求加 `silentError: true`，由 `LazyPageImage` 行内提示；打印时统一提示缺页。

### P3-3 `requirePage` 没有 `totalPages` 上界（源码可证）

位置：`TextbookCatalogService.requirePage()`

现象：只校验 `pageNo >= 1` 和 DB 是否有该行，不校验 `pageNo <= book.totalPages`。正常种子不会越界，但配合 P2-2 的残留页，可能继续提供已下架页。

建议：加 `pageNo > book.getTotalPages()` → 业务异常；补测试。

### P3-4 `stageOf(null)` 归为小学（源码可证）

位置：`TextbookCatalogService.stageOf()`

```java
return grade == null || grade <= 6 ? "primary" : "junior";
```

`grade` 在 DB 是 `NOT NULL`，正常不会为 null；但实体/接口边界上 null 会被误标为小学。建议对 null 返回 `null` 或抛异常，并校验 1–9。

### P3-5 `/pages` 接口与 `listTextbookPages` 是死代码（源码可证）

位置：`textbook.js` `listTextbookPages()` 没有任何调用方；`reader` 的页元数据来自 `toc.pages`。

不是运行时 bug，但会增加维护面。建议：要么阅读器按区间使用它（真正懒加载元数据），要么删除接口/前端封装，避免两套真相。

### P3-6 每张图片请求都执行 unit/lesson 计数子查询（性能，源码可证）

位置：`BizTextbookMapper.xml` 的 `selectBookById` 带两个 `COUNT` 子查询；`requireBook()` 被 `requirePage()` 调用，所以**每次图片请求**都会执行这两次计数，尽管图片接口不需要计数。

建议：拆一个 `selectBookBasicById` 给 `requireBook`，计数只用于列表/目录。

### P3-7 种子 SQL 的 `VALUES()` 与事务（维护性）

- `ON DUPLICATE KEY UPDATE ... VALUES(col)` 在 MySQL 8.0.20+ 已弃用（仍可用），未来版本可能移除；可用别名语法 `AS new` 替代。
- 918 条 INSERT 没有显式事务；中途失败会留下部分导入，虽然重跑可修复，但排障不直观。

建议：按册包事务；或至少在文档里写明“失败后整份重跑”。

### P3-8 ETag 比较过于严格（边界）

位置：`TextbookController.page()`

只做 `etag.equals(request.getHeader("If-None-Match"))`；不处理 `W/"..."`、逗号分隔多值、`*`。浏览器通常只发一个强 ETag，问题不大；但代理/客户端行为不完全可控。建议用 Spring 的 `ServletWebRequest.checkNotModified(etag)` 或自行解析。

### P3-9 `Files.copy` 异常时响应已提交（边界）

位置：`TextbookController.page()`

先设置 `Content-Type` / `Content-Length` 再 `Files.copy`；如果复制中途 IO 异常，响应可能已提交，客户端收到截断图片而不是 JSON 错误。可在复制前确认文件可读/大小稳定；对截断场景记录日志。优先级低。

### P3-10 新模块没有单元/集成测试（风险）

现状：后端 822 项测试通过，但新增的 `TextbookController` / `TextbookCatalogService` / `TextbookStorageService` 没有专门测试；前端只有 Playwright 冒烟。

建议至少补：

- `resolve()` 的 `..`、绝对路径、符号链接、缺失文件；
- `requirePage()` 的 0/负数/超过 totalPages；
- `listPages()` 的 from/to 收敛；
- 图片接口 304 / 401 / 403；
- 前端失败缓存重试、LRU 淘汰不破坏在途图片。

---

## 10. 不要误报的点

- 类级 `@PreAuthorize` 在 Spring Method Security 中对所有方法生效，不是“没加注解”。
- 页面图接口返回 `HTTP 200 + code:500` 是若依全局异常处理的既有语义，不是这个模块发明的。
- `toc` 一次返回整册页元数据（最多 128 条）是有意为之，当前不算性能问题；不要建议改成 918 页全量。
- 不建课程↔教材绑定表、进入阅读器不预选课次、学生端不做、不能整册下载、不做全文检索，都是需求边界。
- 4 上/4 下/7 下灰卡、九年级缺印刷页 31–34，是内容现状，不是代码 bug；可以提示“数据待补”，但不要建议改代码掩盖。
- 图片不放 `/profile/`、必须带 Authorization 走 blob，是版权与鉴权要求，不要建议改回 `<img src>` 直连。
- 书架封面用第 1 页、打印用浏览器打印对话框，都是当前产品选择。

---

## 11. 输出格式

请按下面结构输出，中文，先结论后细节。

### 11.1 问题表

| ID | 严重级别 | 位置（文件:函数/行） | 结论 | 触发条件 | 影响 | 建议修复 | 验证方法 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ... | P0/P1/P2/P3 | ... | 源码可证 / 需实测 | ... | ... | ... | ... |

要求：

- 严重级别定义：P0 数据泄露/整模块不可用；P1 明确功能缺陷或安全风险；P2 边界/性能/维护风险；P3 体验/清理项。
- 每条必须能定位到具体文件与函数；不要写“某处可能有问题”。
- “源码可证”的条目要引用关键代码行为；“需实测”的条目要给出复现步骤。
- 不要重复第 8 节已经验证过的内容，除非你发现了反证。

### 11.2 最可能被用户遇到的 3 个问题

按真实教师使用路径排序（打开书架 → 选册 → 选课 → 翻页 → 打印 → 从课程卡进入），说明现象和规避方式。

### 11.3 发布 130 前的阻断清单

逐条回答：

1. P1 中哪些必须在发布前修复？
2. 哪些可以在发布后观察？
3. 数据库脚本在 130 执行前必须做哪些检查（菜单 ID 冲突、角色、备份、幂等）？
4. 171 MB 图片目录的上传、权限、`TEXTBOOK_ROOT`、回滚怎么做？
5. 教师/教研员/学生三种角色的验收命令或浏览器步骤是什么？

### 11.4 最终结论

明确写：

- 「建议发布」/「修复 P1 后发布」/「不建议发布」；
- 理由（最多 5 条）；
- 如果用户要求你继续修，列出最小修复顺序（不要直接改代码）。

---

## 12. 审核结束后的交接

- 如果用户要求修复：只改与本专题相关的文件；每项修复补对应测试；不要顺手重构；不要动 `RuoYi-Vue/ruoyi-ui`。
- 修复后按 `AGENTS.md` 第 6 节顺序验证：编译/单测 → 接口探活 →（可选）浏览器冒烟。
- 若改变了接口语义、表结构、权限或部署方式，同步更新 `contexts/PROJECT_CORE.md`、`contexts/textbook-module/*`、`docs/architecture/` 与 ADR。
- 未实际部署前，不得在 `contexts/RELEASE_LOG.md` 登记为已发布，也不得把 `biz_platform_update` 标成 `PUBLISHED`。
- 报告和提交信息中不得出现密码、Token、Cookie。
