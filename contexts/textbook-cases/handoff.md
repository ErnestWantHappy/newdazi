# 教师优秀课例：接手指南与当前状态

## 正式上线结果（2026-11-03）

已按用户18:00安排发布130正式1.30.68，52课例201资源；312文件校验、导入重复/孤儿0、全量资源最终失败0、真实教师桌面/390px预览下载与学生基础页通过。更新146/PUBLISHED真实教师可见。新后端18:02:31启动，PID2882774、三服务active、启动ERROR0、env保持。其他未发布改动隔离；学校机房工具与共享Key有效性仍待实际使用确认。

正式证据/回退以[release-20261103.md](release-20261103.md)为准，下方“准备中/未授权正式部署/未部署”均为历史阶段。自动任务18完成后删除，不再重复执行。

## 当前下一步：今晚18:00已获授权正式发布

用户明确要求2026-11-03北京时间18:00上线，当前正在准备。Codex任务18已创建ACTIVE单次唤醒本对话；旧任务仍PAUSED。正式只读核对与备份完成，资源上传解包以output/release-20261103-cases/staged.json为准；下午不改unit/site/env、不执行SQL或重启。晚间按[release-20261103.md](release-20261103.md)继续，不重新询问已授权部署。下文“未授权正式部署”属于早期状态，由本节替代；正式尚未切换。

准备现已完成：staged.json、final-preparation.json存在，312个实际资源文件哈希全部通过，正式进程/配置保持，候选backend已归服务用户admin2可写。下一步直接按发布准备文档在18:00现场复检、追加备份、迁移和切换，勿重复覆盖准备目录。

## 最新完成：具名网页遗漏已补齐（2026-11-03）

当前本机52课例/201资源，新增34份配套包、95个HTML/HTM入口。所有125非重复来源网页入口均有原包或新包对应，无遗漏。报告output/textbook-cases/remaining-web-resources.json、remaining-web-verification.json；全量接口201项0失败见remaining-full-verification.log。五个关键工程、34包哈希与完整性、教师桌面/390px入口通过；前端测试/Vite临时目录构建/发布门禁与导入失败恢复通过。

390px预览弹窗内已实际点击下载原件，原生下载触发且弹窗内“已发起下载”提示可见，截图output/playwright/textbook-cases/remaining-dialog-download-hint.png。早先用固定课例数量定位目录的临时验收脚本超时，改用真实summary课次直达后通过，并非页面故障。

新脚本add_remaining_case_web_resources.py必须在旧build_web_packages.py之后、load_case_samples_local.py之前执行，保留当前清单并追加配套包。它补齐原网页缺失XLSX脚本（复用项目现有库并附许可）、同课图片引用，保留共享Key、相对路径和启动说明。React源码已随该课配套包收录，旧“不收React”结论作废。import_cases.py新增可选filename，用于下载时显示册次课名作者，旧输入行为保持。

原始E盘未修改；平台其他模块代码未动。新增网页有不同运行环境，只有五个关键工程本机实测，不宣称所有网页按钮或机房功能均已验证。共享Key有效性未调用验证。正式未部署。旧段落的167数量、仅10个index工程收口、具名HTML遗漏等状态均以本节为准。

## 用户最新纠正与已执行结果（2026-11-03）

此模块为教师共享资源库。用户明确同意保留原作者自愿共享模型Key，并在下载前提示“内含共享Key，若失效可替换自己的”。已用restore_shared_model_keys.py恢复七上5 DeepSeek与三下11 Coze原始共享配置，更新模型包说明与本机清单；校园智能体原JSON有行尾注释，恢复其模型参数为有效JSON，原模型Key本就为空，说明要求自行填写。未调用外部Key验证有效性。不要再用旧净化规则删掉已授权共享的模型Key。包内样例文件名的核对不等于平台学生端开发，先核实性质再判断。上一轮审查已确认城市大脑student/teacher.html、张楚人评价平台和王诗莹具名互动HTML未收录，不能再声称仅10个index.html项目就覆盖所有网页。自动任务已通过应用接口再次确认PAUSED。

用户已授权继续本机开发，**不要重新索取授权**。仅本机开发，未授权正式部署或 `git push`。
工作区 `D:/dmwprogram/newdazipingtai/newdazi`；原始课例 `E:/2026年宁波市义教信息科技学科新教材优秀课例展示` **只读**。
七上第18课已由用户确认按"江北区展示课例（未署名）"收录。

读取顺序：`AGENTS.md` → `contexts/PROJECT_CORE.md` → `docs/architecture/INDEX.md` → 本目录 `requirements.md`、`design.md`、`tasks.md`、本文件 → 实际代码。
资源收口明细见 [`resource-closure.md`](resource-closure.md)（脚本生成，逐目录列出收了什么）。

## 一、当前真实状态（2026-11-03 12:44，本机）

- 本机库 `xueyeceping_server_20260729`：**52 课例 / 167 资源**（教学设计 52、课件 49、任务单 13、实验包 12、素材 41），全部 `PUBLISHED`；运行目录约 3.85 GiB。
- 全量真实接口复核：52 课例 167 资源 **失败 0**（`output/textbook-cases/catalog-api-verification.json`）。
- 后端 8080 由本专题脚本重启：PID **32340**（`scripts/textbook/start_case_backend.ps1`，JAR 为 10:56 版本，`output/textbook-cases/backend.pid` 已同步）。前端 dev 80 沿用既有进程。**不要同时起第二个后端。**
- Codex 自动任务「三小时后继续优秀课例开发」（`C:/Users/PC/.codex/automations/automation/automation.toml`）已置 `PAUSED`，不会在 13:22 重复执行。
- 工作区仍有并发改动：`PlatformOverviewController.java`、`sql/platform_overview_perf_20261103.sql`，不属于本专题，**不要回退或覆盖**。

## 二、本轮完成

### 1. 两个 RAR 已能读取并收录

系统 `C:\Windows\System32\tar.exe` 可列目录与解包（52 / 12 条目，0 路径穿越）。上一轮"本机无 RAR 工具"的结论已作废。

- 七上19 `智能体.rar` → 校园智能体平台（FastAPI+SQLite）。原件含**真实 `config.json`、`agent.db`、`cert.pem`、`key.pem`**；发布副本已移除，`启动.bat` 去掉 `--ssl-*` 改 HTTP，`config.json` 首次启动自动从 example 生成。包 14.9MiB。
- 七下18 `教学工具 源程序代码.rar` → 深搜教学工具，零依赖 Node，包 43KiB。

### 2. 七上18 课堂包（`.tar`）净化

原包 229MB，含真实 `.env`、`quickform.db`（31 提交/6 附件）、`static/uploads` 16 个学生附件、`_shots` 验证截图、`qf2.log`、`backups`。

- 发布副本 2.5MiB：保留 5 个任务与默认管理员（`wst/quickform`），**submission=0、attachment=0**；数据库经 `secure_delete` + `VACUUM` 重建。
- 新增发现：原包 `venv` 绑定原作者 Windows 商店版 Python 路径，换机 `python.exe` 直接报 `did not find executable`，**不是即开即用**；因此移除 venv，说明改为"需教师机装 Python 3.10+ 并 pip install"。
- `static/student.html` 与 `学生端-互动课堂.html` 原先向 `https://quickform.cn/api/xqugfct8iu` 外发学生姓名/班级/成绩，已改为本机 `API_BASE + /api/fhXTZJnVyZJ`。
- 实测：用隔离 venv 启动后 `/`、`/static/student.html`、`/static/gallery.html` 均 200；向本机 API 提交返回"提交成功"，数据库写入 1 行（仅测试副本）。

### 3. 10 个网页工程收口

7 个新包 + 3 个已收录/不收。新包全部带 `使用说明_平台版.txt`，平台上的描述同时写明用途、包含内容、启动方式、环境与是否联网。

| 课例 | 包 | 说明 |
| :--- | :--- | :--- |
| 七上5 王纪宇 | `w5-internet-ai-lab.zip` | 原页面硬编码作者 DeepSeek Key（`sk-e…`）与 2 个 QuickForm 地址，已清空 |
| 七上19 | `w19-campus-agent.zip` | 见上 |
| 七下18 | `w18-dfs-teaching-tool.zip` | Node 零依赖，HTTP 200 |
| 七上18 | `w18-quickform-class.zip` | 见上 |
| 三下11 卢旭浩 | `s11-animal-stamp.zip` | 移除 `.env`（Coze 令牌）与 venv；需自备令牌 |
| 三下11 岑姝锌 | `s11-dream-wall.zip` | 移除上届 `works.sqlite3` 与上传图片；HTTP 200 |
| 三下11 陈凯 | `s11-film-vote.zip` | 零依赖 Node；HTTP 200 |
| 五上11 田甜 | `g5-decision-tree-cards.zip` | 纯静态学单 |
| 六下16 余姚 | `g6-16-smart-control-site.zip` | 纯静态学习单网站，57MiB |
| 六下 李炯荣 | `g6-18-plant-agent.zip` | 移除 `classroom_data` 与缓存；HTTP 200 |

不收：七上19 `学生素材/2更丰富的智能体/app`（Vite+React 源码，需 npm 构建；同课已有可运行 Python 版）。

### 4. 验收结果

- 10 个新包真实下载哈希与清单一致，包内无 `.env`/私钥/课堂数据库（课堂包的 `quickform.db` 为已净化必要文件）。
- 4 个零依赖工程启动返回 200（`web-packages-smoke.json`），课堂包另行用隔离依赖验证。
- 大视频：125MiB `操作视频完整版.mp4`（H.264 High）在 Chromium 中解码报 `Format error` 并**导致页面崩溃**；已改为超过 100MiB 只提示"下载后本地播放"，桌面/390px 均无 `<video>`、无溢出。47MiB `操作视频整理归档.mp4` 真实播放并拖到 75.8/126.3 秒。
- UI：下载提示改为面板内行内提示（无全局 ElMessage、手机不遮标题）；宽屏"放大"由 1572px → 2358px。
- 既有 `smoke_cases.py` 回归通过（师生权限、桌面预览、下载哈希、390px）。
- 前端 `vite build --outDir dist-check` 通过（`build:prod` 因 safe-delete 保护不能清空 780 文件的 `dist/static`，属环境限制）；构建产物 `dist-check` 已删除，`dist` 保持 738 文件未动。
- 新包在面板中桌面/390px 均显示用途、使用条件与下载按钮，无横向溢出（`results-new-packages-ui.json`）。

## 三、流水线（顺序不能乱）

1. `prepare_case_catalog.py` 生成/压缩预览；
2. `review_prepared_catalog.py` 重建 `catalog-local-reviewed.json`（会覆盖，必须在打包前）；
3. `add_case_packages.py` 并入早期实验包/素材；
4. `build_web_packages.py` 构建网页工程、两个 RAR 与课堂包，并写 `web-packages.json`；
5. `validate_web_packages.py` 写库前核对净化结果；
6. `load_case_samples_local.py --manifest output/textbook-cases/catalog-local-reviewed.json` 复制文件、生成 SQL、写库（自动快照两表）；
7. `verify_case_catalog.py` + `verify_new_packages.py` + `smoke_web_packages.py`；
8. `update_package_decisions.py` 与 `build_closure_report.py` 刷新决策清单和收口文档。

Python：`C:/Users/PC/AppData/Local/Programs/Python/Python310/python.exe`，统一 `-X utf8`，在 `scripts/textbook` 目录运行。

## 四、下一步

1. **机房实测**（需用户安排）：智能体平台、深搜工具、课堂包、投票与展示墙在真实学生机/局域网跑一遍；这是当前唯一未覆盖的验证层级。
2. 智能体平台与动物印章工具需要教师自备依赖/令牌，若机房无 Python 环境，考虑补一个可离线运行的打包方案（本轮未做）。
3. 125MiB 视频若要在平台内播放，需要转码为可在 Chrome 解码的低码率版本（本机无 ffmpeg，未做）；否则保持"下载后播放"。
4. 发布前：两表 DDL、资源同步到正式 `TEXTBOOK_ROOT`、容量评估（本机 3.85GiB）、正式库导入前检与回滚脚本；不要把本机导入命令直接用于 130。

## 五、风险与未闭环

- 课堂包/智能体的默认口令（`wst/quickform`、`teacher_test/123456`）是原作者设置，说明中已提示首次修改，但平台无法强制。
- 原课堂包 venv 不可移植的结论来自本机实测；若目标机房恰好装有原路径 Python，也不会再使用（已移除），需按说明装 Python。
- 10 个新包的启动说明基于本机实测与代码静态核对，**未在任何学校机房运行**。
- 素材中 41 项含图片/视频等，未逐个人工审看内容；仅做了类型、体积与凭据层面核对。
- 金阳小学两份含学生姓名的 xlsx 仍未收录，未做进一步授权核实。
- `output/textbook-cases/staging` 残留约 1.4GB 打包暂存（含原件副本与已排除的敏感文件），清理时被环境的批量删除保护拦住；该目录在 gitignore 的 `output/` 下，不会进入仓库，建议在 IDE 外手动删除。

## 六、UI 参考（只指导布局，不能当作真实教材正文）

- 原书架 `C:/Users/PC/AppData/Local/Temp/codex-clipboard-a24cbfcc-e421-4f1b-844e-f7f8a315dd58.png`
- 原阅读器 `C:/Users/PC/AppData/Local/Temp/codex-clipboard-36ef79e2-cc82-4a2f-a896-3bdcca7e241f.png`
- 示意图目录 `C:/Users/PC/.codex/generated_images/01a19ec1-ee81-7883-9503-a346838c2831/`
