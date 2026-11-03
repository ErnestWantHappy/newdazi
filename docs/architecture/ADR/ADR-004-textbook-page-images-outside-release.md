# ADR-004：教材页图走鉴权接口并存放于 release 之外

- 状态：已采纳（2026-10-29，130 已随 1.30.61 发布）；2026-10-30 本机局部修订课程绑定、学生阅读放行与教师端 PDF 下载（未发布）
- 关联：`contexts/textbook-module/`

## 背景

要把 3–9 年级 13 册信息科技教材（1301 页）接进教师端。原始 PDF 单册 17–140MB（合计约 740MB），必须压缩成图片才能流畅翻页。由此产生三个绕不开的取舍：

1. 图片放哪里：放进 jar、放进前端 dist、放进 `/profile/` 静态目录，还是独立目录？
2. 图片怎么取：`<img src>` 直连静态路径，还是经后端鉴权接口？
3. 教材与平台课程怎么关联：建绑定表/自动匹配，还是只按年级学期选册、课次手选？

`/profile/` 是若依默认的无鉴权静态映射，任何拿到 URL 的人都能下载；教材有版权约束，不能走这条路。教师首页课程标题由教师自拟（库内存在 `333333`、`rrrrr`），按标题自动匹配课次必然误判。

## 备选方案

### 图片存放

1. **打进 jar 的 `static/`**：部署简单，但 244MB 会让每次发布都重复传输，jar 体积失控，且无法单独更新教材。
2. **放进前端 dist**：同上，且前端制品与教材版本强耦合，回滚前端会连带回滚教材。
3. **放进 `/profile/`**：零改造，但无鉴权，等于公开整册，不可接受。
4. **release 之外的共享目录 + 后端鉴权读取**（采纳）：发布只换 jar/dist，教材目录不动；权限沿用平台登录态。

### 图片读取

1. `<img src="/business/textbook/page/...">` 直连：浏览器不会带 `Authorization` 头，401。
2. 后端签发短时签名 URL：多一套令牌与过期逻辑，收益不明显。
3. **axios 取 blob + `URL.createObjectURL` + LRU 缓存**（采纳）：复用现有请求拦截器带 token，前端可控缓存与回收。

### 课程关联

1. 建 `biz_lesson_textbook_section` 绑定表并自动匹配：需要教师逐课维护，标题不可靠，维护成本高于收益。
2. **不建绑定，课程卡按 `grade + semester` 解析册次，课次强制手选**（采纳）：零维护，不会误判。

## 决策

1. 教材图片存放于 `TEXTBOOK_ROOT`（生产 `/data/apps/xueyeceping/shared/textbook`，本机指向 `output/textbook-pack-20261029/pages`），**位于 release 目录之外**，不打包进 jar 或前端 dist。
2. 仅通过 `GET /business/textbook/page/{bookId}/{pageNo}` 回图，类级 `@PreAuthorize("@ss.hasAnyRoles('admin,teacher,researcher')")`；响应带 `ETag`（页 `sha256`）、`Cache-Control: private, no-cache`、`Vary: Authorization`、`X-Content-Type-Options: nosniff`，`If-None-Match` 命中返回 304。
3. 存储服务对 `image_path` 做规范化并校验仍在根目录内，拦截路径穿越。
4. 教材元数据落 4 张表（book/unit/section/page），页图路径存**相对路径**，换机器只改根目录。
5. 不建课程↔教材绑定表；阅读器进入不预选课次；课程卡胶囊只按年级+学期选册（九年级回退 `semester=9`）。
6. 不提供整册下载与全文检索；打印走浏览器「打印本课」。

## 2026-10-30 局部修订（本机，未发布）

- 课程↔教材由“无绑定”改为**按课程显式绑定一课**：`biz_lesson.textbook_book_id/textbook_section_key` 可空，设计器只列本年级本学期册的 `LESSON`，非必填；教师首页胶囊和学生端入口据此直达。仍不建独立绑定表，也不做标题自动匹配。
- `/toc`、`/pages`、`/page` 放行 `@studentSs.isStudent()`（含 `biz_student` 兜底）；`/books` 仍仅 `admin/teacher/researcher`。页图鉴权、ETag 与 `no-cache` 不变。
- 教师端新增 PDF 下载：书架「下载整册」与阅读器「下载整册 / 保存本课 PDF」走 `/business/textbook/books/{bookId}/pdf` 和 `/books/{bookId}/sections/{sectionKey}/pdf`，仍仅 `admin/teacher/researcher`。服务器没有原始 PDF，下载文件由页图合成 A4 PDF（无文本层），缓存在 `TEXTBOOK_ROOT/.pdf-cache`，版本取首页/末页哈希+页数；目录不可写时退回系统临时文件。学生端不显示下载入口。
- 本机迁移 `sql/lesson_textbook_binding_v1.sql` 已执行，130 未执行；本节只描述本机源码，130 正式 1.30.63 仍按原文边界（无绑定、学生 403、无整册下载）。

## 后果

- 发布体积与教材体积解耦：244MB 图片只上传一次，后续前后端发布不再重复传输。
- 教材访问受登录态与角色约束，撤权后立即失效；代价是每页多一次后端读文件与鉴权，实测单页 197KB、ETag 命中 304 后几乎零成本。
- 教师本机翻页由前端 blob LRU 缓存承担，弱网体验不依赖 HTTP 缓存；`private, no-cache` 让每次取图都回源校验，ETag 命中返回 304，只省传输不省鉴权。
- **2026-10-29 修正**：原方案是 `private, max-age=604800`（缓存一周）。实测同一浏览器里**去掉 token 再请求同一 URL 会直接命中磁盘缓存**（`transferSize=0`、197762B `image/jpeg`，服务端零请求），等于在共用教师机上把受版权约束的教材交给下一个登录账号；同时页图修正后 7 天不生效。因此改为 `no-cache` + `Vary: Authorization`，放弃"一周免回源"换取"每次鉴权 + 改图即生效"。
- 部署多一个前置条件：`TEXTBOOK_ROOT` 必须指向正确目录，否则整册空白；已写入发布检查项。
- 教材更新（换册/补四上四下七下）只需重跑导入脚本并替换共享目录，不涉及应用发版。
- 不建绑定表意味着平台课程与教材课次没有数据层关联，后续若要做「按课次自动带出教材」需另立专题。

## 验证

- 本机教师账号：`books=9`、七上 `toc` 4 单元 125 页、`pages?from=10&to=14` 5 条。
- `/page/6/12` 返回 `image/jpeg` 197762 字节 + ETag；带 `If-None-Match` 返回 **304**。
- 无 token `code=401`；学生 `code=403`；不存在册/越界页 `code=500`。
- 数据复核：1301 页（13 册）页号连续、`bad_range=0`、课次编号连续。
- Playwright 12 项全部通过，含封面 blob 渲染、手选课次定位、打印 4 页、滚动页码跟随、课程卡直达。

## 回滚影响

- 下线：菜单 26100 置 `status=1` 或删除三角色授权，前端入口即消失；4 张教材表保留不影响其他业务。
- 回退版本：前后端回退到上一 release 即可，教材共享目录可保留待下次使用。
- 2026-10-30 起新增两列可空结构；应用回退可保留列不影响旧代码，彻底回退再按备份决定是否删列。种子脚本仍幂等，可重复执行。
