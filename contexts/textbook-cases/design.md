# 设计与当前实现

## 2026-11-03 · 连续滚动预览已上线1.30.70

用户要求课件上下滚动翻页，已改连续阅读，保留跳页/缩放/缩略图并同步当前页码；可见窗口/邻页加载，释放远处图片，混合方向与缩放保持位置，单页失败重试且同步评价计时。4项布局测试、构建16项/门禁、本机200页模拟与正式四尺寸滚轮/触摸通过。仅前端发布，后端、SQL结构和材料均保持。[发布证据](release-scroll-20261103.md)。

## 当前结果：1.30.69已正式发布（2026-11-03）

课例评价/理由/低频邀请、44包（52课例196资源）、真实条件与内容摘要、数据库及前端菜单顺序、响应式预览已上线；7项独立复核缺陷及真实并发的死锁/旧快照问题已修复。20项Java、MySQL/12并发、联网3项、前端构建/菜单6项、真实师生与四尺寸验证通过。双角色按teacher存在放行；机房/共享Key有效性仍未逐一实测。[完整证据及回退](release-feedback-fix-20261103.md)。

以下未完成/待验条目是历史阶段，当前状态以上述发布证据为准。

> 晚间v2草稿：材料仍离线维护，拟新增教师评价/邀请两表及写接口，客户端改为flex预览、缩放/跳页/缩略图与包内容摘要。SQL未执行、草稿未验收；包审计误报待修，不能视为正式设计已落地。完整规则和代码状态见[交接](handoff-feedback-ui-20261103.md)。

## 正式上线结果（2026-11-03）

已按用户18:00安排发布130正式1.30.68，52课例201资源；312文件校验、导入重复/孤儿0、全量资源最终失败0、真实教师桌面/390px预览下载与学生基础页通过。更新146/PUBLISHED真实教师可见。新后端18:02:31启动，PID2882774、三服务active、启动ERROR0、env保持。其他未发布改动隔离；学校机房工具与共享Key有效性仍待实际使用确认。

正式证据/回退以[release-20261103.md](release-20261103.md)为准，下方“准备中/未授权正式部署/未部署”均为历史阶段。自动任务18完成后删除，不再重复执行。

2026-11-03具名网页补收：add_remaining_case_web_resources.py在既有资源清单之上追加34份配套包、95入口，保留相对路径，合计覆盖125非重复HTML/HTM。源程序归档与Office预览分开展示；npm源码按原依赖说明下载，不为平台引入依赖。动态历史记录/安装包不混入网页包。神经网络缺失的XLSX脚本复用现有库并附许可，推荐算法图片复用同课素材。import_cases.py支持清单filename字段，让内部内容地址与教师下载文件名分开，旧清单仍默认原文件名。

两表DDL见sql/textbook_cases_v1.sql，运行期只读，离线审核导入。

- book_id+section_key关联既有教材，查询联验册上架、课次LESSON、课例PUBLISHED。
- /business/textbook/cases/summary返回各册课次份数；/book/{bookId}/section/{sectionKey}返回该课课例资源；/files/{id}?preview=true返回PDF览览，否则附件下载。
- 类级teacher/admin/researcher鉴权，不改学生共用教材API。前端不接收文件存储路径。
- 文件存TEXTBOOK_ROOT/cases；真实路径防穿越和符号链接越界；PDF览览、附件下载，private/no-store。
- Vue3书架标记、目录常驻图标、CasePanel分作者、览览临时URL释放、异步请求序号防串课；1200px以下面板浮层。
- import_cases.py scan登记候选清单，stage复制reviewed材料和生成事务SQL，核对SHA256与路径，不自动写库。

## 2026-11-03 预览与资源收录补充

- 预览PDF超过100MiB时（LibreOffice把高清截图逐页重复嵌入），用 `compress_case_previews.py` 逐页重采样为1920px、质量85的JPEG页面PDF，体积降到4MiB级；`prepare_case_catalog.py` 在生成后自动套用同一阈值。仅处理 `output/textbook-cases/previews` 工作副本，E盘来源与原件不动；后端100MiB上限保留为兜底。
- `apply_case_previews_local.py` 把压缩后的预览同步到 `TEXTBOOK_ROOT/cases/previews`，按旧哈希更新资源行，旧文件在无引用时才删除；只允许本机开发库。
- 实验包与素材分三层：可直接收录（已核对无凭据/学生数据）、净化后收录（移除 .env、课堂数据库、编辑器备份与运行时临时文件）、不收录（安装包、含真实学生姓名或教师课堂数据、需构建才能运行的源码）。决策与理由写入 `package-decisions.json`，脚本 `add_case_packages.py` 在 `review_prepared_catalog.py` 之后执行并入清单。
- 包审计只读条目名与统计：`audit_case_packages.py` 标出 .env/数据库/密钥/运行时，`audit_package_secrets.py` 只输出键名与表行数，不输出取值。

## 2026-11-03 网页工程与压缩包分发

- `build_web_packages.py` 统一处理 10 个网页工程、两个 RAR 与课堂包 tar：RAR 用系统 `tar.exe` 解包（Windows 自带 bsdtar，能读 RAR），tar 用 Python `tarfile` 按 UTF-8 解包（bsdtar 会把中文名解成乱码）。
- 净化以"打包时排除"实现，不删除暂存文件（避免海量删除触发环境安全限制）：排除 `.env`、课堂数据库、上传附件、日志、备份、`_shots`、`__pycache__`、venv 与 node_modules；课堂包的 `quickform.db` 保留但删除 submission/attachment 行并 `secure_delete` + `VACUUM`。
- 每个包内写 `使用说明_平台版.txt`，内容与平台展示的 description/requirements 同源，避免"包内说明与实际不符"。
- 启动方式按实测如实描述：原课堂包的 venv 绑定原作者 Windows 商店 Python 路径，换机无法启动，因此移除并改为要求 Python 3.10+；智能体原用自签证书启动 HTTPS，移除证书后 `启动.bat` 改为 HTTP。
- 用户确认本模块是教师共享资源库，原作者自愿共享的模型API Key保留，并在下载前提示失效可换自己的；原课堂收集地址与模型Key分别处理，不能把共享Key本身列为上线阻碍。
- 超过 100MiB 的视频不在弹层内解码（Chromium 对 125MiB 文件报 `Format error` 并崩溃），改为提示下载后本地播放。

待完善：智能体平台与动物印章工具需要第三方依赖/令牌，本机未实际启动；全部工具未在学校机房运行；125MiB 视频未转码；大包前端仍为浏览器原生下载；原生PDF手机兼容待验；详情资源按课例查，不是逐目录查。
