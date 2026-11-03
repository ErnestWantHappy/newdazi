# 电子教材（教师端）设计

## 2026-11-02 封面缩略图（已发布1.30.66）

`GET /business/textbook/books/{bookId}/cover`先通过目录服务校验上架册和第一页，再读 `TEXTBOOK_ROOT/covers/{bookId}.jpg`，缺失回退第一页原图；鉴权允许教师侧及studentSs学生，匿名拒绝。360px派生图随制品校验上传，不覆盖原页图。封面private/max-age3600、Vary Authorization、尺寸+mtime ETag，正文仍no-cache；前端请求缓存和失败回退原页图。[正式证据](../release-20261102-all.md)。

> 关联：[需求](requirements.md)、[任务与验证](tasks.md)、[ADR-004](../../docs/architecture/ADR/ADR-004-textbook-page-images-outside-release.md)

## 1. 总体结构

```
原始 PDF（本机 G:）
  └─ scripts/textbook/import_textbook.py（PyMuPDF 渲染 + 压缩）
       ├─ output/textbook-pack-20261029/pages/<book_code>/pNNN.jpg
       ├─ manifest.json（册/单元/课次/页元数据/哈希）
       └─ sql/textbook_seed_20261029.sql（幂等种子）
              ↓ 执行
MySQL：biz_textbook_book / unit / section / page
              ↓
后端 /business/textbook/*（鉴权 + ETag）
              ↓
前端书架 /textbook、阅读器 /business/textbook/read/:bookId
```

教材图片**不进 jar，也不进前端 dist**，单独放在 release 之外的共享目录，由后端按鉴权读文件回图。

## 2. 数据模型

### 2.1 `biz_textbook_book`（册）

| 关键字段 | 说明 |
| --- | --- |
| `book_id` | 主键 |
| `book_code` | 唯一册编码，`xxkj-<grade>-<semester>-2026` |
| `title` / `short_title` | 全称（信息科技 七年级上册）/ 短名（七上） |
| `grade` / `semester` | 年级 1–9；学期 `0/1/9` |
| `publisher` / `edition` | 出版社、版次年份 |
| `total_pages` | PDF 总页数，用于页号校验 |
| `page_offset` | 印刷页 = PDF 页 − 偏移（分段册取起点段） |
| `status` | `0` 上架、`1` 下架；书架只查上架 |
| `sort_no` | 书架排序（小学 10–41，初中 50–70） |
| `source_file` / `source_sha256` / `imported_at` | 导入溯源 |

### 2.2 `biz_textbook_unit`（单元）

`book_id + unit_no` 唯一；只存单元序号和标题，不冗余页码范围。

### 2.3 `biz_textbook_section`（条目）

`kind` 五类：`FRONT`（封面/编者的话/目录）、`UNIT_INTRO`（单元导入）、`LESSON`（课次）、`SUMMARY`（单元小结与反思）、`APPENDIX`（版权/封底）。

- `section_key` 为册内稳定键（`front`、`unit-1-intro`、`lesson-1`、`summary-1`、`appendix`），重跑种子不会产生重复。
- `lesson_no` 仅 `LESSON` 有值，册内连续 1..N。
- 同时保存 `printed_start/end` 与 `pdf_start/end`：前者用于页脚和打印标题，后者用于定位。
- `UNIT_INTRO` 单独成条，是因为单元导入常与课次共享印刷页区间，混在一起会导致「选第 1 课却从导入页开始」。

### 2.4 `biz_textbook_page`（页）

`book_id + page_no` 唯一；`image_path` 是**相对教材根目录**的路径（如 `xxkj-7-0-2026/p011.jpg`），换机器只改根目录即可。`sha256` 直接作为 ETag，`width/height/bytes` 供前端占位避免滚动抖动。

### 2.5 不建课程↔教材绑定表

用户明确要求手动选课次，且平台课程标题不可靠。前端只在课程卡胶囊上按 `grade + semester` 解析册次，教材与平台课程没有数据库层关联。

## 3. 后端

### 3.1 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/business/textbook/books` | 上架册列表（含 `unitCount` / `lessonCount`） |
| GET | `/business/textbook/books/{bookId}/toc` | 册 + 单元树 + 前置/附录 + 页元数据 |
| GET | `/business/textbook/books/{bookId}/pages?from&to` | 页元数据闭区间，可省略 |
| GET | `/business/textbook/page/{bookId}/{pageNo}` | 单页 JPEG，鉴权 + ETag + 304 |

统一 `@PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")`；无 token 由若依安全链返回 `code:401`，学生返回 `code:403`。

### 3.2 图片响应

- `Content-Type: image/jpeg`，`Content-Length` 取真实文件大小。
- `ETag: "<sha256>"`，`If-None-Match` 相等直接 304，不读文件流。
- `Cache-Control: private, no-cache` + `Vary: Authorization`（2026-10-29 修复）：原先是 `private, max-age=604800`，实测在同一浏览器里**去掉 token 再请求同一 URL 会直接命中磁盘缓存**（`transferSize=0`），等于在共用教师机上把教材交给下一个登录账号；同时改图后 7 天不生效。改为每次回源校验：命中 ETag 仍返回 304，只省传输不省鉴权。
- `X-Content-Type-Options: nosniff`。
- 页不存在或越界抛业务异常，返回 `code:500` 业务码（若依一律 HTTP 200）。

### 3.3 存储根目录

`application.yml`：

```yaml
textbook:
  root: ${TEXTBOOK_ROOT:./textbook-data}
```

- 本机：启动前设 `TEXTBOOK_ROOT=output/textbook-pack-20261029/pages`。
- 生产：`/data/apps/xueyeceping/shared/textbook`（release 之外，切版本不重复上传 171MB）。
- `TextbookStorageService.resolve()` 做规范化并校验仍在根目录内，拦截 `..` 越界。

## 4. 前端

### 4.1 书架 `views/business/textbook/index.vue`

- 顶部标题 + 说明；按 `stage` 分「小学 / 初中」两块。
- 上架册显示封面（按 blob 取回）、短名、「N 单元 · N 课」。
- 未上架册渲染灰卡「暂未上架」，点击提示后不跳转。

### 4.2 阅读器 `views/business/textbook/reader.vue`

- 三栏：左侧目录树、中间连续滚动页面、顶部工具条 + 底部状态栏。
- 页面图统一走 `LazyPageImage`：IntersectionObserver 触发加载，占位高度按 `width/height` 预算，避免图片回填时跳动。
- 页码跟踪用 IntersectionObserver 窄带（`rootMargin: -6% 0px -86% 0px`），平滑滚动时约 2 秒收敛。
- 顶部「选择课次」下拉按单元分组（单元导入 / 第X课 / 单元小结），进入时不选中。
- 「打印本课」：先确保该课页面 blob 全部就绪，再 `window.open('')` 写入一个只含 `.head` + `.sheet img` 的文档并 `print()`，避免侧边栏和工具栏入纸。**任何一页取不到就关闭空白窗口并报出页码、取消打印**（2026-10-29 修复）：原先单页失败被 `catch(() => '')` 吞掉，老师会拿到少页的讲义还以为打全了。
- 底部状态栏显示 `第 N 页 · 第X课 名称（印刷页 a–b）`。

### 4.3 图片缓存 `api/business/textbook.js`

- 页面图必须带 `Authorization`，`<img src>` 直连会 401，因此统一 axios `responseType: 'blob'` 取回后 `URL.createObjectURL`。
- LRU 上限 80 页，淘汰时 `URL.revokeObjectURL`，避免长时间阅读累计几百 MB。
- **失败不入缓存**（2026-10-29 修复）：请求失败时把该 key 移出缓存，否则"点击重试"永远命中同一条 rejected promise，页面再也恢复不了且不再发请求。`loadTextbookPage(bookId, pageNo, force)` 的 `force` 供重试强制绕过缓存（覆盖"blob 拿到但解码失败"的情况）。
- 在途请求被淘汰时不立即 revoke，登记到 `orphanUrls`，离开阅读器统一释放，避免打断正在等同一 promise 的 `<img>`。
- 页面图请求带 `silentError: true`，失败由行内提示承担；`utils/request.js` 的 blob 分支同时尊重该标记，否则一册多页缺失会连续弹全局 toast。
- 打印前批量 `prefetch` 本课页面，命中缓存直接返回，未命中并发受控。

### 4.4 课程卡胶囊

`views/business/teacher/index.vue`：

- 只在课程卡顶部信息区渲染「教材」胶囊，不占底部操作排。
- `openTextbook(lesson)`：用 `lesson.grade` + `lesson.semester` 在已加载册列表里找 `book_code`；九年级回退 `semester=9`。
- 命中则 `router.push('/business/textbook/read/' + bookId)`；未命中提示「本册暂未上架」并跳 `/textbook`。
- 课程 DTO `LessonInfoVo` 与 `BizLessonMapper.xml` 补 `semester` 字段（原先只有 `grade`）。

### 4.5 路由

- 书架为菜单路由 `/textbook`（`menu_id=26100`，顶级菜单，`parent_id=0`，`order_num=9`，图标 `education`）。
- 阅读器为静态隐藏路由：

```js
{
  path: '/business/textbook',
  component: Layout,
  hidden: true,
  children: [
    {
      path: 'read/:bookId(\\d+)',
      component: () => import('@/views/business/textbook/reader'),
      name: 'TextbookReader',
      meta: { title: '电子教材', activeMenu: '/textbook', noCache: true }
    }
  ]
}
```

不建隐藏菜单，避免菜单表与角色授权出现第二份真相。

## 5. 图片生产流水线

`scripts/textbook/`：

| 文件 | 作用 |
| --- | --- |
| `volumes.json` | 13 册源文件、年级学期、排序、缺册清单（现为空） |
| `toc/*.json` | 每册人工核对后的单元/课次 PDF 页区间 |
| `import_textbook.py` | 渲染 + 压缩 + 生成 manifest 与种子 SQL |
| `verify_pack.py` | 校验页数/哈希/区间，`--rerender N` 抽样重渲染比对 |
| `smoke_teacher_textbook.py` | 教师端 12 项 Playwright 冒烟 |

压缩参数：按页面原始宽度计算缩放系数，直接用 PyMuPDF 渲染成宽 1400px 的 JPEG，质量 75；**13 册 1301 页合计 243.8MB**（原 13 个 PDF 合计约 740MB，压缩后约为 1/3）。选 1400px 是因为在 1440 宽视口下整页显示已接近 1:1，再高只增加流量不增加可读性。

合订本：`七年级下册.pdf` 前 125 页是七上、126–248 页才是七下，靠 `source_page_start/source_page_end` 只渲染后者并重编号，源文件不动。

## 6. 已知边界

- 三上 PDF 无文本层，故不做全文检索；不影响图片阅读。
- 九年级原始 PDF 缺印刷页 31–34，已在 `toc-review.md` 标注；第 6 课从 PDF 第 36 页（印刷页 35）开始，`offset_breaks` 在 p36 从 5 切到 1。
- 1301 页图占 244MB，生产必须放共享目录并确认磁盘余量；不要塞进 release 目录，否则每次发布重复传输。
- `/profile/` 静态路径无鉴权，教材图绝不能放进去。

## 7. 2026-10-30 增量设计：课程教材绑定（本机，未发布）

- 存储：`biz_lesson.textbook_book_id` + `textbook_section_key` 两列可空，不建独立绑定表；`section_key` 取册内稳定键（如 `lesson-3`）。
- 保存：前端提交 `textbookBindingTouched=true` 才校验并写绑定；两字段都空表示清空。校验册 `status='0'`、年级一致、学期一致（或册为 `9`）、条目存在且 `kind='LESSON'`。旧版缓存页面不带该标记，服务端保留原绑定。
- 前端：设计器按 `grade + semester` 找册（未命中回退 `9`）→ `toc` 只取 `LESSON`；换学期重载并清空不属于新册的选择。教师首页胶囊有绑定则带 `?section=` 直达。阅读器 `load()` 后等页宽落到 DOM，再按 `section.pdfStart` 瞬时定位（容器 CSS 是 smooth，直接赋 `scrollTop` 仍会动画，需临时改 `scroll-behavior: auto`）；学生端复用同一阅读器，返回 `/student/index`，高度 `100vh`。
- 权限：`/books` 仍教师侧；`/toc`、`/pages`、`/page` 加 `@studentSs.isStudent()`。学生可读取任意已上架册的阅读接口，但不提供书架列表。
- 风险：教材重导入导致 `section_key` 失效时阅读器提示重选；册下架使旧绑定跳转失败；课程改学期会要求重选。

## 8. 2026-10-30 增量设计：整册与本课 PDF 下载（本机，未发布）

- 服务器只有按页渲染的 JPEG，没有原始 PDF，因此下载文件由 `TextbookPdfService` 用 PDFBox 把页图逐页合成 A4 PDF（无文本层）。
- 接口：`GET /business/textbook/books/{bookId}/pdf`、`GET /business/textbook/books/{bookId}/sections/{sectionKey}/pdf`，方法级只允许 `admin/teacher/researcher`；学生即使能读页图也拿不到下载接口。
- 缓存：生成结果写 `TEXTBOOK_ROOT/.pdf-cache`，文件名带首页/末页 sha256 前缀和页数，教材重导入后自动换键；同键并发用内存锁避免重复合成。共享目录不可写时退回系统临时文件并在响应后删除。
- 前端：书架卡片「下载整册」与阅读器「下载整册 / 保存本课 PDF」用 blob + `file-saver` 另存；整册请求超时放宽到 180s，按钮带 loading，学生端不渲染这两个按钮。
- 边界：`.pdf-cache` 每册约 16–22MB，可整目录清理；下载文件仍受登录态与角色约束，不得放进 `/profile/`。
