# 操作题页图「页框外内容」修复（page-jpeg-v4）

> 后续正式状态（2026-10-14）：1.30.49已发布，反馈班级44份重算v4，原件/成绩/版本保持；**真机内容流仍有固定A4裁剪，底部完全恢复未解决**。旧18行墨点统计不能证明恢复，以下保留本机候选历史。当前发布、补修路径坐标和内联常量、真机残留证据见[正式验收](seat-logo-overflow-release-20261014.md)。

2026-10-14。本机已完成实现、定向测试与真机样本复核；**尚未发布到 130**（SSH 通道不可用，见文末）。

## 问题

教师反馈：某些操作题页图滑到最底部仍看不到底部内容（账号 `684487`）；另有 HTML 操作题预览与源码只有放大/全屏才可见（账号 `677430`）。

HTML 预览问题**已单独修复并作为 1.30.47 发布**（前端 `grading.vue` 的两条 `v-if` 链互斥 + 折叠栏），见 [1.30.47 验收](grading-ui-fix-20261013.md)。本文只记页图底部被裁。

## 根因（有明确证据，不是页眉页脚设置）

用户最初怀疑学生上传的原始文档页眉/页脚不对。**该假设不成立**：DOCX 是标准 A4（`pgSz 11906×16838`，页边距 1440/1800/1440/1800），用 WPS 打开完整可见。

真机证据（同一份 DOCX）：

| 指标 | 130（Linux + LibreOffice） | 本机（Windows + LibreOffice） |
| :--- | :--- | :--- |
| 最低矢量 y | **883.3pt**（超出页框 41.4pt） | 783.9pt（页内） |
| 越界绘图 / 文本块 | 5 / 2 | 0 / 0 |
| 正文字体 | NotoSansCJKsc、DejaVuSans | SimSun、Calibri |

130 的 PDF 里，第一段文本块起点 y0 为 82.5（本机 78.9），字体替换使行高变大，把第 2 题的**浮动锚定图形组**（`<wp:anchor>` + `<wp:wrapNone>`）推过了页面下边界。浮动图形不参与正文流，**不会分页到第 2 页**，只会静默留在页外。PDFBox 按 CropBox（此处等于 MediaBox）栅格化，页图忠实再现了这一裁切——页图 1240×1753 本身是完整 A4，**缺的内容根本不在 PDF 里**。

子代理复核确认：应用内**不存在**按页裁剪、固定页高、只看第一页或视口截图逻辑；`DocxConnectorNormalizer` 只重写水平连接符坐标，从不处理页面尺寸、页边距、页眉页脚或分节符。

仓库既有未解决边界与此同源：样本 575947 在 Linux 上 2 页、本机 1 页。

## 修复

方案 A（本机完成）：渲染时先测内容真实边界，越界就把取景框扩到内容处再栅格化。

`RuoYi-Vue/ruoyi-business/.../PracticalPageRenderer.java`：

- `RENDERER_VERSION` 升 `page-jpeg-v3` → **`page-jpeg-v4`**，缓存目录与资源地址同步换版本，旧页图保留可回退。
- 新增 `ContentBoundsEngine extends PDFGraphicsStreamEngine`，遍历内容流累计包围盒：矩形、路径、图像四角、底纹、字形原点。`PDFStreamEngine` 自动下钻 Form XObject，嵌套图形同样计入。
- 新增 `expandFrameToContent(PDPage)`：内容在框内返回 false（正常文档字节不变）；越界则取「页框 ∪ 内容边界」并加 1% 余量，同时扩 CropBox 与 MediaBox。
- 新增 `persistExpandedFrame(File)`：把扩大后的页框回写到**平台自己生成的转换副本 PDF**，使「PDF 原图」视图与页图一致。先写 `.reframe.tmp` 再 `ATOMIC_MOVE`，失败只记日志。
- **学生原件绝不改写**：`isDerivedPreview()` 只在 `previewPath` 能解析到同一文件时才允许回写；PDF 附件与区域抽测（`renderForOwner`）一律传 `false`。原件哈希是缓存身份，改了会被当成新文件。

一个关键 API 陷阱：PDFBox 的 `Matrix.transform(Point2D)` 是**原地变换且返回 void**，不是返回新点。首轮编译因此报 4 处 `void 无法转换为 Point2D`，改为 `transformInPlace(Point2D.Float)` 传一次性副本。

## 验证（本机）

- **新增 4 项测试**，`PracticalPageRendererTest` 共 **9/9 通过**：
  - 内容画到 y=-40 → 页图高度 > 1800px，且源文件字节完全不变；
  - 内容在框内 → 页宽 1240、页高 1750～1755（与旧行为一致，未被缩放偏移）；
  - OFFICE + `previewPath` 指向同一文件 → 源文件字节**变化**、重载 CropBox 高度 > 850pt；
  - `renderForOwner` 抽测路径 → 同样能看到越界内容，且源文件字节不变。
- **全模块回归**：`mvn -o -pl ruoyi-business -am test -DforkCount=0` → **747 通过，0 失败，3 跳过**。
- **后端打包**：`mvn -o -pl ruoyi-admin -am clean package -DskipTests` → BUILD SUCCESS。
- **前端**：`npm run build:prod` 通过（发布检查：入口 gzip 558892B）。
- **真机样本实测**（把 130 导出的 `p-3.pdf` 当 OFFICE 转换副本跑一遍渲染）：
  - 页图 1754 → **1876px**；旧页框底部（1754）以下恢复 **18 行、5668 个墨点**；
  - 转换副本 CropBox 841.9 → **900.9pt** 并成功落盘；
  - 越界量 41.36pt，与 PDFBox 独立测量一致。

## 重算清单必须精确，不能全表重置

`page-jpeg-v4` 只对**新转换**生效。旧 `success` 行的 `normalized_pages_json` 仍指向 v3 目录，不会自己重算；`claimAttachmentNormalization` 只认 `normalized_status='pending'`，所以必须显式重置。但 **OFFICE 重算会真的再跑一次 LibreOffice 转换**，几千份一起放出去会打满 soffice 进程池并拖慢正式服务。

因此拆成两步：

1. `scripts/measure_page_overflow.py`（**只读**，PyMuPDF 近似测量，判定口径与渲染器一致：内容包围盒 vs CropBox，容差 0.5pt）。输入 `attachment_id`/`preview_path` 清单与拉取的 PDF，输出越界 id 列表和可直接粘贴的 `@ids` 串。已用本机样本自检：`p-3.pdf` 判为越界 41.36pt，本机正常渲染判为不越界。
2. `sql/page_overflow_reframe_20261014.sql`：前检 → 填 `@ids` → 事务内重置为 `failed` + `preview_status='pending'` + 回拨 `last_retry_time` → 后检。置 `failed`（而非 `pending`）是为了走既有 `selectRecoverableAttachments` 每批 200 的限流领取，避免瞬间打满转换池。脚本幂等：已带 v4 或顶满重试次数的行不会被重复处理。

## 未完成 / 风险

- **未发布**。130 的 SSH 通道不可用（22/2222/20220 等端口均超时/被过滤），`scripts/deploy.py` 的 `deploy_transport.py` 只配了 `10.52.1.123`。需要恢复访问后才能：备份 → 发布 → 跑测量 → 执行重置 SQL → 抽查页图 → 登记 `biz_platform_update`。
- 方案 B（服务器装度量兼容中文字体）同样被访问阻断。装字体是**治本**方向（能让 LibreOffice 排版贴近 WPS），但字节更重，需单独评估。
- 本方案只解决「内容被画到页框外」这一类。**Linux 字体分页差异**（同一文档 Linux 2 页 / 本机 1 页，样本 575947）不是本方案能解决的，仍待治理。
- 页框扩大后页面会多出一圈白边（本次样本宽 1240→1277px），属预期。
- HTML 预览修复（1.30.47）已上线；本次后端修复版本号应为 **1.30.48**，前端如需同步再发。
- AOCI 未同步。
