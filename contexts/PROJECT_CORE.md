# 信息科技学业测评平台：当前核心事实

> 上下文版本 **v5.45**，更新日期 **2026-11-11**。130前端正式为 **1.30.93**：`releases/20261111_case_panel_resize_v1`，首页`447e7bc5`，教材资源面板可拖宽、课例标题左对齐；后端仍 **1.30.92**（`20261110_python_manual_confirm_v1`，JAR`a8162e41`）未重启，MainPID`3107625`、NRestarts0。Python批改教师确认制保持：系统判题分仅参考，教师运行查看、五星/数字打分或批量采用后才写正式成绩。电子教材16册/1840页、高中3册539页、2份课程标准、327课例/1050资源保持；本地已保存1.30.87～1.30.93源码并完成冗余清理，Git未推送。本机Python交互统一已完成第二轮复核：后端1017项、前端20项、浏览器本机/服务器双模式通过，仍未发布。

## 已上线1.30.93：教材资源面板可拖宽与课例标题左对齐（2026-11-11，仅前端）

- 教师反馈资源面板固定 290–380px 太窄，且课例标题越往下缩进越多。`reader.vue` 增加面板左侧拖拽条，支持鼠标和键盘，宽度 290–720px 并按 `localStorage` 记忆；`CasePanel.vue` 标题结构改为纵向左对齐，优秀课例与教研活动课例共用同一左边界；≤1200px 隐藏拖拽条，手机保持浮层。
- 发布：前端 `releases/20261111_case_panel_resize_v1`，首页`447e7bc5`，制品821文件 / SHA-256 `09be0cdf`；合并旧静态后线上2,821文件；后端未重启。产品更新 **171 / 1.30.93 / PUBLISHED**，真实教研员接口可见。备份 `/data/backups/xueyeceping/20261111_082806_20261111_case_panel_resize_v1/`（产品更新表75,987B / `6db6f4c6`，含 unit/nginx/index）。
- 验收：真实教研员 1440 拖拽 360→500px 并记忆，标题与面板内容左边界同为1092px，无横向溢出；390 面板380px、拖拽条不可见、无横向溢出；0页面异常、0个5xx。证据 `output/playwright/case-panel-1.30.93-online-1440.png`、`case-panel-1.30.93-online-390.png`、`output/release-20261111-case-panel-resize/`。
- **制品隔离**：发布源码由 `git archive HEAD` 加教材两文件在临时目录构建；HEAD 中混入的未上线 Python 交互改动（教师运行终端、生成输出按钮、学生本机运行提示）在发布副本中回退，未进入 1.30.93。新制品相对 1.30.92 只新增“拖拽调整宽度”“调整本课教学资源宽度”两条文案，`generate-outputs` / `本机运行` 均为 0。
- 未验边界：未与真实教师在教学时段交互使用；拖拽宽度上限 720px 未在多显示器/浏览器缩放下逐一验证。

## 2026-11-10 Python 全链路交互运行与判题统一（本机在研，未发布，不改1.30.92）

- **焦点**：吴琴琴老师反馈“点运行查看没提示语、输入3后显示‘请输入您的年龄免票’、参考答案写 `input("提示")` 验证不通过”。已按“一次系统性解决一类问题”完成改造，本轮只在本机验证，未发布 130。
- **提示语不再靠删**：新增`JudgeSourceWrapper`，只对真正调用`input(`的代码套壳，给提示语加不可见标记；缺输入写NeedInput并按“等待输入”处理；报错只保留`main.py`栈帧。`JudgeOutput`提供`plain()`判题、`visible()`兼容旧题、`terminalText(stdin)`回放“提示语→输入→换行→输出”，不做任何正则删除，不会误删学生自己打印的相同文字。
- **判题与算分唯一来源**：新增`JudgeOutcome`统一状态、`weightedScore`与中文差异说明；课程链路与刷题链路的分数比例一致（刷题原先忽略测试点权重已修）；程序停在等待输入时判`RUNTIME_ERROR`，不再因为“跑完了”而误判通过。
- **全入口统一终端**：教师批改运行、学生刷题页、`.py`作品运行共用`PythonTerminal.vue`；学生做题页保留既有`InteractiveTerminal.vue`外壳，但与前者共用同一`interactiveRunner`、降级判定和“提示语→输入→输出”交互，不再各自实现一套。高版本浏览器（JSPI）本机运行，其余自动走服务器；本机兼容性失败自动切换并保留已填输入；等待输入时输入框紧跟提示语，与Thonny一致；低版本只给升级轻提示，不拦截使用。
- **教师建题**：验证失败时逐测试点显示输入/期望/实际/原因，并提供“用参考代码生成输出”（只算不落库，需教师确认保存）。参考答案里的`input("提示文字")`可以正常验证通过；旧题把提示语写进期望输出同样判对。
- **新增接口**（均不写成绩）：`POST /business/python-practice/student/run`（刷题调试运行）、`POST /business/teacher/grading/python/run`（学生未提交时教师运行当前代码，按课程归属校验）、`POST /business/programming/question/{questionId}/generate-outputs`（生成期望输出）。无表结构变更，无SQL迁移。
- **同时修正**：终端回放文本存库时换行被`trim()`压成一行的问题；服务器模式每次重跑会重复显示旧输出的问题；学生刷题页“必须先填完所有输入”的旧交互；教师端必须存在学生提交记录才能运行的限制。
- **验证（2026-11-11 复跑）**：后端业务全量**1017项通过/0失败/3跳过**，含`JudgeSourceWrapperTest`7项（6项用本机真实Python 3.10执行套壳源码）、`JudgeOutputTest`14项、`JudgeOutcomeTest`6项、教师验题6项、做题交互4项、刷题交互7项、教师运行4项；前端`npm run test:python-runner`**20项通过**，`npm run build:prod`与发布门禁通过（入口gzip 562,501B）。
- **浏览器实测（2026-11-11，本机真实前后端+真实Python执行）**：教师批改页“运行查看”打开即运行，先显示“请输入您的年龄：”并等待输入，输入框内联跟在提示语后；输入17后回放为“请输入您的年龄：17 / 免票”。服务器模式（模拟无JSPI旧浏览器）与本机Pyodide模式结果完全一致，页面脚本错误0。学生刷题页同题同样通过两种模式。临时课程、答题行与题单验证后已精确删除，残余0。
- **建题与历史兼容实测**：参考答案写`input("请输入姓名：")`时6/6测试点通过；把旧题期望输出改成“提示语+结果”仍6/6通过；`generate-outputs`返回不含提示语的纯结果且数据库逐行未变；故意写错期望输出时返回“期望输出是…程序实际输出是…”的人话差异。
- **本机环境修复**：本机开发库缺`biz_data_lab_snapshot`表及`biz_data_lab_submission.revision/max_score`两列，刷题页会500；本轮按仓库`sql/data_lab_v2.sql`的既有DDL补齐后才完成验证。这是本机环境补齐，不是本轮产品迁移，130未执行、未改表。
- **未验边界**：未在生产Judge0真实链路、整班并发、非Chromium实机和130真实教师操作上验证；作品`.py`预览运行与教师建题向导只做了代码/接口核对，未在浏览器中逐项点完；发布前需重新前检、备份、执行与回滚准备。
- **专题**：[交互运行与判题设计](python-grading/design-interactive-terminal.md)、[任务与证据](python-grading/tasks.md)。

## 2026-11-10本地与130冗余清理（未发布，不改变1.30.92）

- 本地先以`7e5c441`保存1.30.87～1.30.92已上线源码/SQL/文档，再用`76bf7f6`、`4abcef8`、`04cfa91`移除Python缓存、旧Vue2前端`ruoyi-ui`、误入库Windows缓存、AI记忆、AGENTS备份、`dist.zip`和历史PDF临时产物；全部仅本地提交，未push。
- 本地删除38个未跟踪大目录（旧worktree、课例源包、发布制品、tmp/work/temp等），项目占用降至约33.87GB；保留`TEXTBOOK_ROOT=output/textbook-pack-20261029`、`output/playwright`、1.30.87～1.30.92发布证据、上传目录和本地备份。
- 130删除68个历史release约43.49GB，`/data/apps/xueyeceping/releases` 52GB→26GB，`/data`可用322GB→348GB；未改库、未删备份、未重启服务。保留当前/上一版、1.30.88/89回滚组合、高中回滚组合、外置配置release和共享课例软链接目标；`20261015_practical_pptx_v1`、`20261028_fullstack_v1`因root权限文件本轮保守保留。
- 验证：130 active且首页/验证码200，MainPID/当前JAR/Nginx root不变；课例327、资源1050、教材16册1840页、课标2、孤儿0前后不变；共享课例链接仍指向`20261107_teaching_resources_v1/resources/cases`。本机后端clean package成功，前端build:prod和发布门禁成功，8080验证码200、教材接口未登录返回401业务包。
- 审计与精确清单见[清理记录](cleanup-20261110/README.md)、`output/cleanup-20261110/RESULT.md`。注意：清理开始前工作区已另有一批Python交互未提交改动，未混入清理提交。

## 已上线1.30.92：Python批改教师确认制与运行查看（2026-11-10）

- 教师反馈三类问题已定位：Python在线提交无运行查看、Python禁用五星、学生0/4用例时系统0分被自动写成正式成绩并显示“已判分”。本机已改为系统判题分仅参考：`biz_programming_submission`照常保存最高分判题事实，`biz_student_answer.score`保持NULL等待教师确认。
- 教师操作路径：批改页选学生 → 查看最高分代码/用例结果 → 可点“运行查看”输入自定义数据 → 五星/数字打分或批量采用系统分；运行不写判题、成绩或任务状态。已评分后学生重交不悄悄替换被评分代码版本。
- 涉及`TeacherGradingController`、`ProgrammingSubmissionService`、`BizStudentAnswerMapper.xml`、`teacherGrading.js`、`TeacherPythonSubmissionPreview`、`grading.vue`及专题/架构文档；无表结构变更。
- 发布：整库备份`/data/backups/xueyeceping/20261110_124146_20261110_python_manual_confirm_v1/database.sql.gz`，53,410,495B，SHA-256`0178408250904a8d6aaab98f9bc576b754246042eec5b58283f3623754562cc3`；产品更新170/PUBLISHED。
- 数据修复：老师反馈课程`990607/4506` 43条系统0分转待批，教师已给安欣妍80分未动；验收教师课程`662/4268` 74条系统0分转待批，70条既有非零成绩保持已批。
- 验收：后端定向25项、前端批改页12项、生产构建通过；真实教师1440/390运行查看输出正确、0横向溢出、0页面脚本异常、0个5xx，更新170真实教师可见。证据`output/playwright/python-manual-confirm-1.30.92-20261110/`、`output/release-20261110-python-manual-confirm/remote-state.json`。
- 未验边界：未做两名教师并发评分和学生并发重交的生产实测；其他历史课程仍存在的系统0分不自动回滚，后续按`sql/python_grading_manual_confirm_20261110.sql`逐课核对后处理。

## 已上线1.30.91：课例配套材料包说明精简（2026-11-10）

- 顾意丽《返回成语现场》资源`resource_id=2314`原“使用说明”为1045字，含压缩包大小、SHA-256、完整目录、内部程序和未上架清单，教师阅读负担过重。现改为91字：“本包为‘返回成语现场’课堂工具，含成语连环画创作与作品展示。解压后双击‘启动课堂工具.bat’，浏览器打开学生端；教师端地址见启动窗口，需联网使用AI出图。教学设计和课件见资源列表。”
- 内容依据RAR实际结构：`app.py`、`templates/teacher.html`、`templates/screen.html`、`data/groups.json`、`static/generated`图片和`static/uploads`音频；教学设计、反思、说课稿和课件是同课例的其他独立资源，不在RAR内。
- 同时修正`.agents/skills/research-to-textbook-cases/scripts/build_archive_summary.py`：教师说明只保留内容与操作，技术检查仍留在`archive-inspection.json`和manifest；说明上限180字，不再显示SHA-256、完整路径、内部程序清单。新增4项说明回归测试，教研导入脚本测试24项通过。
- 发布为数据修正，不重启后端、不换前端、不改表结构。整库备份`/data/backups/xueyeceping/20261110_060850_20261110_case_summary_v1/database.sql.gz`，53,147,066B，SHA-256`4ff6cf0ed05e74c85c8a908a429df5e6aa0fbaf9a2e3658a7a3ab79d58928d4c`；仅更新资源2314与登记产品更新169/1.30.91/PUBLISHED。正式库327课例/1,050资源前后不变。
- 验收：真实教师API和阅读器页面均返回91字说明，1440/390无横向溢出、0页面脚本异常、0个5xx；产品更新169真实教师可见。证据`output/playwright/case-summary-1.30.91-20261110/`、`output/release-20261110-case-summary/remote-state.json`。回退用`python scripts/deploy/update_case_summary_130.py --mode rollback --backup-dir <备份目录>`，将说明恢复为旧版并把169转DRAFT。

## 已上线1.30.90：Python批量采用页面快照保护（2026-11-10）

- 批改页“批量采用系统分”现在提交页面加载时的学生快照；后端逐人比对`answerId`、分数、答案内容、作品版本、系统提交ID和系统分。页面打开后学生重交或另一教师改分，该生跳过并计入`skippedCount`，不用旧快照覆盖新事实。
- 快照保护叠加在1.30.88已有行锁与最高分提交复核之上；无快照的旧客户端仍保留后端行锁、退回检查和最高分校验，但新版前端可消除“页面列表过期”这一类并发窗口。
- 发布：release `20261110_python_grading_snapshot_v1`，JAR `ec36df3f9118e5b711c6cb44e445b8d64bae303ccd495cf9df7b60bfb07fd440`，108,301,513B；首页`f66da562736e9c1faf6c920a9629f7c980ab2433856bf2f3eb073a7f52a084c0`；前端制品43,377,956B/SHA-256`c3c9c75b…`/820文件，合并旧静态后2,043文件；MainPID`3097248`、NRestarts0、首页/验证码200、启动错误0。
- 整库备份`/data/backups/xueyeceping/20261110_000343_20261110_python_grading_snapshot_v1/database.sql.gz`，53,200,609B，SHA-256`2de6cf0240fbf731bc5626672cb59f3db09f20d5d8c0259f5f0d909f7b20d6cf`；产品更新168/PUBLISHED。
- 验证：后端业务全量969项通过/0失败/3跳过；新增批量采用4项（含页面快照变更跳过）通过；前端生产构建与发布门禁通过。130真实教师1440/390只读验收：45人名单、代码与系统分40/40、判题时间正常，0横向溢出、0页面脚本异常、0个5xx；更新168真实教师可见。证据`output/playwright/python-grading-1.30.90-20261110/`与`output/release-20261110-python-grading-snapshot/remote-state.json`。
- 未验边界：未在生产写测试成绩，未做两名教师真实并发改分、学生真实并发重交和整班Judge0压测；并发跳过由4项单元测试和线上只读链路覆盖，不能宣称已完成生产并发实测。

## 已上线1.30.89：Python批改页窄屏适配（2026-11-09）

- 真实验收发现手机390px仍把学生名单、代码预览和打分区压成三栏窄条；现已改为“学生名单 → 作品 → 打分区”纵向排列，筛选和期限信息整行显示，Python系统判题栏上下排列。桌面1440三栏不变。
- 仅改`grading.vue`、`TeacherPythonSubmissionPreview/index.vue`和测试桩；后端、数据库结构、权限、成绩规则未变。前端`releases/20261109_grading_mobile_v1/frontend`，首页`db0e4c385b8172d5d7ab2b1f75223255bd04f1b188022150053053bd59214212`，制品43,441,015B/SHA-256`3e4c9d4e…`/820文件。
- 整库备份`/data/backups/xueyeceping/20261109_234706_20261109_grading_mobile_v1/database.sql.gz`，53,200,867B，SHA-256`095b6398fb338bb8ea59d00b25fc2f89c6eedc38c61c6e3f2afcb8f3a47347e5`；产品更新167/PUBLISHED。
- 130真实教师1440/390只读验收：45人名单、代码、系统分40/40、判题时间与按钮正常，0横向溢出、0页面脚本异常、0个5xx；历史课程已逾期，批量按钮按规则禁用，未点击、未写成绩。证据`output/playwright/python-grading-1.30.89-20261109/`。

## 已上线1.30.88：Python批改稳定化与课例来源修复（2026-11-09）

- 后端`releases/20261109_python_grading_fix_v1`，JAR`0cfb023c559ec7e4c6b5490b90405240ccd6aba8dee62e437b4edeccbbcd5c13`，108,299,981B；同release前端首页`27d47323c3216af7a45235a64000c1c55a8823d0d067c793293e00dd3bf623d7`；产品更新166/PUBLISHED。1.30.89只切前端，后端PID保持。
- 最高分正式提交只接受`SUBMIT`、非空分数和已知终态白名单，排序`score DESC, judged_at DESC, submission_id DESC`；代码、判题时间、用时、内存和用例指标同源。无答题行但有正式提交时可预览、人工评分和批量补建。同分重交采用最新代码。
- 批量采用在事务内加行锁、复核当前最高分提交与退回状态；档案缺失、系统分缺失或越界、满分配置NULL/负数时跳过并计数，不再整班回滚。页面列表快照比对在1.30.90补齐。新增`idx_programming_submission_best`，130 MySQL 8.0.46 `EXPLAIN`命中索引且`using_filesort=false`。
- 课例来源为空/未知不再误显示“优秀课例”或开放评价；教研导入脚本增加路径穿越/软链接防护和SQL失败回退；高中教材脚本改为文件先就绪、数据库后提交并支持幂等重试。
- 验证：后端业务全量968项通过/0失败/3跳过；Python定向3项、前端`gradingRefresh`12项、前端生产构建通过。130真实教师接口Python范围45人/40人有系统判题，最高分代码、分数和提交ID 40/40一致；启动错误0。备份`/data/backups/xueyeceping/20261109_232033_20261109_python_grading_fix_v1`，53,200,539B，SHA-256`f1f6d2609f4424df4468cf1849babd13774372db518b953d05d910d0ff5d51e9`。专题[需求](python-grading/requirements.md)、[设计](python-grading/design.md)、[任务](python-grading/tasks.md)、[验收](python-grading/release-1.30.88.md)、[ADR-020](../docs/architecture/ADR/ADR-020-python-grading-best-submission-source.md)。
- 未验边界：未在生产真实成绩链触发无答题行补建；未做两名教师并发改分、学生并发重交和整班Judge0压力测试。上述路径由单元测试、本机数据路径和线上只读比对覆盖，不得宣称已生产并发实测。

## 2026-11-09审查报告结论

- `BUG-DOC-01`说1.30.87未上线是误判：发布前130已是1.30.87，只是Git HEAD停在1.30.86。`BUG-CH-01`所述“旧题fork时删图、状态仍RETAINED”未形成可复现证据；RETAINED长期未引用图仍按保守策略不自动清理，本轮未改。
- Python批改的无答题行、同分代码错位、批量采用缺少锁与个案容错、页面列表过期覆盖新成绩、满分为空、最高分慢查询，教研来源空值，以及两个导入脚本的重入/解压安全问题均为真实缺陷，已按上述1.30.88/1.30.89/1.30.90修复并上线。

## 此前1.30.87：高中教材与课程标准（2026-11-09）

- 电子教材书架新增“高中”分组：必修1《数据与计算》193页、必修2《信息系统与社会》161页、选择性必修1《数据与数据结构》185页，共3册539页、15单元82目录条目；沿用逐页阅读、目录、整册/本课PDF下载、鉴权和ETag。高中阅读器目录使用“章 / 节 / 本章导入 / 本章巩固与项目挑战”。
- 新增独立表`biz_textbook_standard`与“课程标准”资料区：义务教育信息科技课程标准（2022年版）74页、普通高中信息科技课程标准（2017年版2025年修订）64页；支持在线逐页预览与PDF下载，不伪装成教材课次。教师、教研员、管理员可用；学生查看/下载返回拒绝，匿名下载401。
- 正式库由13册/1301页变为16册/1840页，高中`book_id=14/15/16`，重复页0；旧327课例/1050资源及`research-post-15/60/70` PUBLISHED保持不变。页图在`shared/textbook/xxkj-hs-*-2019`，课标在`shared/textbook/cases/standards`，封面`covers/14-16.jpg`；`shared/textbook/cases`仍指向`20261107_teaching_resources_v1/resources/cases`，不得删除或替换该链接。
- 发布：后端JAR`cbd0042c56b217662229e570192a58e6f94afe5e6e551e344bda72db6d41f1b8`，108,296,532B；前端热修`20261109_high_school_overview_guard_v1`，首页`246b85165a8273239073759646b92e7bab8548197551faed9b1d025edb82a458`，820前端文件；后端/首页/验证码200，三服务active。整库备份`/data/backups/xueyeceping/20261109_203740_20261109_high_school_textbooks_v1/database.sql.gz`，53,167,168B，SHA-256`42ea3ea4b702d9850efa86ffc75afa875a74a1e54ca293ad0d3b09fc24263e99`；更新165/1.30.87/PUBLISHED。
- 前端热修根因：平台总览接口在用户进入教材页后才返回，旧页面卸载后仍对空图表容器调用ECharts初始化。`RuoYi-Vue3/src/views/platform/overview/index.vue`现于卸载后丢弃迟到结果，并对图表容器做空值保护；仅切Nginx前端根，后端未重启、数据库未变。热修备份`/data/backups/xueyeceping/20261109_210407_20261109_high_school_overview_guard_v1/`。
- 验收：正式API19项通过（16册、高中3册目录/页图、2份课标预览/下载、整册PDF44,409,670B、学生/匿名拒绝、教研课例未中断）；热修后真实教师浏览器连续两次完整复跑通过，1440/390/320无横向溢出、0个5xx、0页面脚本异常。证据`output/playwright/high-school-textbook-online-20261109/`。
- 回退：若只需撤销总览页热修，将Nginx根切回`releases/20261109_high_school_textbooks_v1/frontend`并reload，不重启后端、不动数据库。若回退整轮高中功能，再恢复原整库备份中的`service.before`/`nginx.before`、daemon-reload并重启后端；或仅执行`UPDATE biz_textbook_book SET status='1' WHERE book_code LIKE 'xxkj-hs-%'; UPDATE biz_textbook_standard SET status='1';`并把更新165转DRAFT。新增表/页图/课标/封面均为增量，优先保留，不整库回灌、不递归删除共享教材目录。

## 已上线（随当前版本保留）：教研活动课例导入（2026-11-09）

- `biz_textbook_case`327课例/1,050资源，其中新增3节：post 15郑东旭《语音合成》（八下第2课）、post 60赵琪《返回成语现场》（四上第17课）、post 70顾意丽《返回成语现场》（四上第17课）；`source_key`分别为`research-post-15/60/70`，均为PUBLISHED，互不覆盖。8条同名旧课例保留。
- 26个来源文件325,446,066B；深度检查ZIP/RAR，9个PDF预览、2份中文压缩包说明、17个去重上架资源。`.url`快捷方式、`.bat`、Python工具源码和缓存不单独上架，只在压缩包说明中指引教师本地查看；音频/图片提供安全下载或播放。
- 前端显示完整来源教研活动、`RESEARCH_ACTIVITY`标签、ARCHIVE/RAR/7Z识别、音频控件和窄屏换行；后端返回来源字段并扩展安全音频MIME。增量DDL为`sql/research_to_textbook_cases_v1.sql`。详见[专题任务](research-to-textbook-cases/tasks.md)与[ADR-018](../docs/architecture/ADR/ADR-018-research-post-idempotent-case-import.md)。

## 此前1.30.86：首页品牌停留节奏微调（2026-11-09）

- 品牌Logo桌面`220px`、手机`128px`、全屏遮罩和移动渐显均保持；居中停留由约2秒缩短为约1.2秒。实现上把入场后的计时器由`2200ms`改为`1400ms`，因为淡入本身约占0.2秒。
- 仅改`RuoYi-Vue3/src/components/BrandLogoLanding/index.vue`，不修改后端、数据库、权限或业务数据；实际登录首次进入才播放，刷新/站内跳转不重播，减少动态效果时跳过。
- 发布：Nginx root为`/data/apps/xueyeceping/releases/20261109_brand_logo_hold12_v1/frontend`，首页SHA-256`be732a834ec7e589cc1cb9102e753e899d777079e5c9dd654efc2d501d6df30e`；制品43,340,491B、SHA-256`66220d2c200367f4fba4c53e2bebbe3cfae134c14369be1370ca090e54f64a8d`、820文件。后端MainPID`3019157`、NRestarts0、三服务active。
- 备份`/data/backups/xueyeceping/20261109_120529_20261109_brand_logo_hold12_v1/database.sql.gz`，70,925,485B，SHA-256`9f307d75ccef89b4dcf5755673f47251672c363bd4327874d7bb5ef7c7a6d64a`；更新164/1.30.86/PUBLISHED真实教师可见。
- 正式浏览器：桌面居中停留`1240ms`、手机`1260ms`，遮罩停留时为1并在移动中渐隐；标题、透明Logo大图、刷新不重播通过，0页面脚本异常、0个5xx。证据见`output/playwright/brand-logo-hold-12-20261109/`与[发布验收](brand-logo-entry/release-1.30.86.md)。
- 回退到1.30.85时将Nginx root指向`releases/20261109_brand_logo_entry_v2/frontend`并将更新164转DRAFT；1.30.85的2秒版本仍保留。不重启后端、不整库回灌。

## 此前1.30.85：首页品牌入场体验优化（2026-11-09）

- 实际登录成功首次进入首页才播放：桌面 Logo `220px`、手机 `128px`，居中停留约2秒；起始全屏浅色遮罩不透明度1，整页内容不可见，向左侧目标移动的780ms内遮罩渐隐、页面内容逐渐出现。刷新和站内跳转不重播；系统“减少动态效果”时跳过。
- 1.30.84先完成浏览器标题“信息科技学业测评平台”、登录页/教师首页/学生首页 Logo 点击透明大图、首次登录约1秒动画；1.30.85只基于1.30.84前端替换`BrandLogoLanding/index.vue`，保留点击预览与标题，不修改后端、数据库、权限或业务数据。
- 发布：Nginx root为`/data/apps/xueyeceping/releases/20261109_brand_logo_entry_v2/frontend`，首页SHA-256`0e252cb6d8e319829173ea1c0fcc4f8aab274304c71ee54922c564982e7f7f52`；制品`frontend.tar.gz`43,342,265B、SHA-256`369c4dd07780196bfa16f90e8f77dc2d56cde12ed3723cfa2c9cc6a98c67c98a`、820文件。后端MainPID`3019157`、NRestarts0、三服务active。
- 备份`/data/backups/xueyeceping/20261109_111458_20261109_brand_logo_entry_v2/database.sql.gz`，70,878,487B，SHA-256`30aea88da7e349d9fdb71a7d4ed46b0670203e52ded79d1b3122787e224548e2`；仅登记更新163/1.30.85/PUBLISHED并真实教师可见。
- 正式浏览器：桌面居中停留`2020ms`、手机`2060ms`；两端遮罩停留时为1、移动中连续渐隐；标题、透明Logo大图和刷新不重播通过，0页面脚本异常、0个5xx。证据见`output/playwright/brand-logo-entry-20261109/`与[发布验收](brand-logo-entry/release-1.30.85.md)。
- 首次`20261109_brand_logo_entry_v1`误用了旧1.30.84包（首页`6b1912eb`），不能作为新效果上线证据；正确版本是`entry_v2`。回退到1.30.84时将Nginx root指向`releases/20261109_brand_logo_v1/frontend`并将更新163转DRAFT；回退1.30.83则指向`releases/20261109_lesson_designer_layout_v2/frontend`，更新162、163转DRAFT。不重启后端、不整库回灌。

## 此前1.30.83：课程设计页上下布局与教师工具卡片样式修复（2026-11-09）

- 教师从首页“添加课程”进入课程设计页后，宽屏不再左右并排：`课程内容设计`在上、`教学资源库`在下，两列均为整行宽度；手机端保持同一顺序。帮助教程文案由“左侧/右侧”改为“先填写课程信息，再从下方教学资源库添加题目”。
- 仅前端改动；课程布局本身改`business/lesson/designer.vue`和`help/tutorials.js`，同一前端包还包含`business/teacherTools/index.vue`的动态卡片样式修复，合计三个前端文件。后端JAR、数据库结构、权限与课程/题库/成绩规则未变，后端未重启。
- 发布：Nginx根切至`/data/apps/xueyeceping/releases/20261109_lesson_designer_layout_v2/frontend`，新首页SHA-256`0d2aa2343758c62bf3c4d3db67331bb0a04ae2b26226de8d897122566c6246bc`；制品`frontend.tar.gz`43,334,389B、SHA-256`8141371efa0321ff57aee52fb3403ffdd94c780e2664f1a74752a4949132f71f`、820文件；合并1.30.82旧static后线上1,981文件。
- 发布前整库备份`/data/backups/xueyeceping/20261109_091504_20261109_lesson_designer_layout_v2/database.sql.gz`，70,575,490B，SHA-256`ec91a843c9840435ca5165be3c76ed123a285ddfb4d63d34bf585499e142900f`。本次无业务SQL，仅执行产品更新登记`sql/platform_update_1_30_83_20261109.sql`。
- 验证：首页与`/prod-api/captchaImage`均200，三服务active，MainPID仍`3019157`、NRestarts0；产品更新161/1.30.83/PUBLISHED真实教师接口可见。正式浏览器1440与390均为上下顺序、两栏整行宽度、0横向溢出、0页面脚本异常、0个5xx；未点击保存、未创建课程。截图见`output/playwright/lesson-layout-20261109/`。
- 教师工具补充验收：真实教师页面11/11通过，65张工具卡、图标44px色块、标签底色、分区渐变和6组色调均恢复；1440/390/320无横向溢出，0脚本异常、0控制台错误、0个5xx。证据见[教师工具样式验收](teacher-tools/release-20261109-style.md)。
- 回退：Nginx根切回`releases/20261109_choice_media_v1/frontend`并reload，更新161转DRAFT；不重启后端、不回灌数据库。帮助页旧截图`step-04-course-designer.png`仍为左右布局，属后续可选补充，不影响本次页面。

## 已上线1.30.82：选择题图片与Excel图文导入（2026-11-09）

- 功能：题干最多3图、每个选项1图，支持上传/粘贴/拖拽截图；题库显示“含图”标记，教师详情与学生作答均可查看并放大。Excel原模板可直接插入单元格图片，支持预览修正、复核和整批原子确认；已引用含图题实质修改自动另存，保存回执可找回真实题目，个人抽测同步透传媒体。
- 安全：图片只存私有目录`/data/upload-question-images`（`admin2:admin2 750`），通过登录身份及题目/课程凭证读取Blob；匿名与错课拒绝。学生课程VO不返回标准答案，只回显本人选项与得分，不展示标准答案正误。
- 迁移：正式库执行`sql/choice_question_media_v1.sql`，两可空TEXT列与四张新表；迁移前后题库4792、抽测4187不变，四表0行。不回填旧题、答案或成绩。
- 发布：JAR`2b9c65e9273a324ba42d5aec0f97f942aa81ada3f5bb0dd5ce9c3e1fa7d5161d`，首页`8e2d045be4427cc598acca22d5214bfe8f0e67c6487a7c92d7e641788bce0cf3`；三服务active、NRestarts0、启动0 ERROR，旧1.30.81 release保留。
- 验证：后端951项通过（0失败、3跳过），前端图片专项5项及41项构建门禁通过；正式API9项覆盖上传、创建、回执、读图、匿名拒绝、课程凭证、学生答题、Excel批次和持久化；浏览器9组、10张截图、0页面脚本异常、0个5xx，1440/390/320无横向溢出。
- 清理与登记：测试题991673/991674、课程关联、学生答案、4个导入批次和10张图片按清单精确删除；题库/抽测/课程关联/答案回基线，四表0行、图片目录为空。产品更新160/1.30.82/PUBLISHED真实教师接口可见。
- 备份：整库`/data/backups/xueyeceping/20261109_071529_20261109_choice_media_v1/database.sql.gz`，70,566,942B，SHA-256`464ac8a37b0c4edc4886af96d730c4e64c0ab7b327f5ff0415afe00e9cd34966`。

## 1.30.82未验证边界与运维注意

- 未实测：真实WPS/Excel不同版本的内嵌图变体、整班并发图片上传/读取、TEMP七天清理跨期运行、老旧机房浏览器、区域抽测含图题真实页面。当前证据只能限定为本轮正式API、现代Chromium和生产库迁移验收。
- `/data/upload-question-images`是release之外的生产持久目录，后续发布不得删除或改为公开静态目录；应用回退到1.30.81可保留空列/空表，但一旦产生正式含图题，不得在未导出图片和媒体引用时回退到不识图旧程序。
- 主工作区仍有其他在研改动；后续发布继续使用隔离制品，禁止整包快照。[详细验收与回滚](choice-question-media/release-1.30.82.md)、[设计/任务](choice-question-media/tasks.md)。

## 本机在研：Excel图片专用列v3（2026-11-09，未发布）

- 新模板保留13个原文字列，新增`题干图片1-3`和`选项A-D图片`共7列；旧模板仍可导入。这样插图片不会再顶掉题干或选项文字。
- 图片专用列按图片左上角锚点所在列判断槽位；再检查垂直方向是否主要落在本题行内。跨题行、合并数据行或位置模糊时进入“待确认图片”，由教师在预览中选择，不静默猜。旧模板同格浮动图仍按85%主要覆盖面积规则。
- 导出改为写回图片专用列，导出再导入不丢图。WPS `DISPIMG/cellimages.xml`受限解析；Excel 365 `richData`只检测并提示改用浮动图片或预览粘贴截图，不宣称全自动兼容。
- 本机专项16项、关联35项及业务模块全量964项（0失败、3跳过）通过；前端生产构建与发布检查通过（含登录7、机位9、工作台25项门禁）。覆盖专用列、纯图题、WPS合成XML、richData提示、跨题行待确认和导出回导。无数据库迁移，未改旧`ruoyi-ui`，未升级POI/ExcelUtil。
- 未发布、未部署；真实WPS/Excel多版本、裁剪旋转和整班导入仍需样本验收。决策见[ADR-002](choice-question-media/ADR-002-excel-image-columns-and-anchor-ownership.md)。

## 已上线1.30.81：答案泄露、教材越权与课堂并发修复（2026-11-08）

- 题库列表与详情新增字段级裁剪：普通教师查看他人Python操作题时，`answer`/`analysis`不再下发；创建者和管理员保留完整答案。选择题、判断题、课程成绩与考试查询语义不变。前端Python预览对无权限题目显示“仅题目创建者或管理员可查看参考答案”。
- 学生教材阅读收紧为“当前班级课程绑定教材”：`toc/pages/cover/page`四接口校验学生档案、当前课程、学校与`bookId`；教师、教研员、管理员保留整册阅读。越权或未绑定统一返回业务失败，沿用现有`ServiceException`业务码500约定，不改HTTP鉴权语义。
- `PracticalPythonRunService`并发槽位由2提升至8，并增加3秒排队等待；仅成功取得槽位才释放，失败不会泄漏许可。这是可用性调优，未做整班45人真实压测。
- 前端同步：一年级指南明确“暂未上架”空态；操作题四模式计数改为`Promise.allSettled`并支持单项“重新获取”；汇编预览关闭重开恢复页码；教师工具去掉重复说明并修复320px收藏按钮挤压标题；成绩页修正Vue import顺序。
- 验证：发布前在130真实复现教师答案泄露、学生跨教材读取；发布后教师详情/列表`hasAnswer=false`，管理员仍333字符，绑定学生读教材1成功/读7被拒，无绑定学生读1被拒，教师读1/7均成功。浏览器7/7通过（0页面异常、0 5xx）；后端定向18项、前端登录7/机位9/题库25项、Vite生产构建与发布门禁通过；主工作区同步后业务模块全量955项（3跳过）通过。
- 发布与备份：整库备份`/data/backups/xueyeceping/20261108_232251_20261108_review_fixes_v1/database.sql.gz`，SHA-256`774464dc…`；产品更新159/1.30.81/PUBLISHED，真实教师接口可见。未Git提交/推送。
- 1.30.81未混入选择题图片/Excel图文导入，当时业务JAR递归扫描`media_json`为0；该功能已由1.30.82正式接续。整班真实并发、老旧浏览器实机及越权提示的HTTP 403语义仍待后续专项。

## 已上线1.30.80：教师工具查找路径与审查修复（2026-11-08）

- 搜索按名称/别名 → 用途 → 标签/分类/任务分级，多关键词全部命中；有关键词或其他筛选时只显示一个结果区，按 toolId 去重。正式83工具、12分类、14精选、5任务；任务覆盖6/23/15/16/7。“教研精选”恢复推荐顺序，来源名称区分原3005站/原80根站。
- “我的常用”“最近打开”为 localStorage、按 userId 隔离，页面已注明仅存本机浏览器；**跨设备偏好需新表，待用户确认未做**。下架/删除工具不再经常用回显（单测覆盖）。
- 后端新增任务/词条两表、aliases/task_tags两列，目录缓存升`catalog:v2`；迁移已备份后在130执行并幂等复核，禁止再次执行数据回填。39工具别名回填管理列；任务编码、状态、停用分类校验收紧；Redis失效异常不再把已提交写入报告为失败，缓存仍可能最多5分钟后更新。后端已重启、前端独立release已切换，无新增配置。
- 验证：后端TeacherTool 21项、前端19项通过；正式真实角色接口与浏览器24/24通过（精选顺序、筛选去重、来源、键盘收藏、弹窗拦截、未加载身份、管理别名、1440/390/320、0 JS异常）。教师管理接口业务403、教研员5任务/83工具/39非空别名；更新158/PUBLISHED纯教师可见。真实管理编辑、上下架写操作及真实iconUrl失败回退未实测。跨设备收藏与脏数据/未归任务工具继续待教研确认。[需求](teacher-tools/requirements.md)、[设计](teacher-tools/design.md)、[任务](teacher-tools/tasks.md)、[迁移及回滚证据](teacher-tools/release-20261108.md)。

## 已上线1.30.79：移除汇编数量说明（2026-11-08）

- 书架与汇编预览去掉“覆盖4/20课”等数量说明，不新增全部来源统计；汇编预览仅留2026版标记，资料和来源事实保持。
- 隔离正式源码仅两处Vue修改并删除无引用标签工具，40项构建门禁通过；本机制品及正式真实教师1440/390/320预览、真实PDF下载、关闭与关联资源/指南回归通过，0页面JS异常。首页/验证码200，PID/NRestarts保持；更新157/PUBLISHED教师可见。
- 无业务SQL/DDL/依赖或后端重启，仅Nginx reload；回退前端至1.30.78并将更新157转DRAFT，无业务数据回灌。证据、备份与未验边界见[数量说明调整验收](textbook-teaching-resources-20261107/ui-1.30.79.md)。

## 已上线1.30.78：教材资源页面精简（2026-11-08）

- 删除书架/指南重复说明与资源原始路径长文；相同课名仅显示一次，2023参考仅留来源标签；汇编改为短覆盖标记，必要使用条件按需展开。保留下载、预览、缺课、失败重试和作者评价。
- 基于隔离正式源码，仅4个Vue文件及1个标签工具变化；40项构建门禁通过。本机制品及正式真实教师1440/390/320汇编预览、PDF下载、阅读页参考预览、课名去重和指南缺课状态通过，截图人工复看；0页面JS异常。首页/验证码200、PID/NRestarts保持；更新156/PUBLISHED教师可见。
- 无业务SQL/DDL/依赖/后端配置变化，仅Nginx reload；回退前端至1.30.77，更新156转DRAFT，不回灌业务数据。详细证据与边界见[页面精简验收](textbook-teaching-resources-20261107/ui-1.30.78.md)。

## 已上线1.30.77：教学资源补全与汇编预览（2026-11-08）

- 13册“教学设计汇编”均支持书架与阅读页在线连续预览/下载，并标真实覆盖课次；八上2026原件确为16/18/19/20课，标4/20，不再称完整整册。指南为6组、179可用课件/180课次，五年级第28课标暂未提供；切册/切年级防过期响应串页，失败可重试。
- E盘并非没有其他资料：补入八上2023前15课、八下《机器学习》对应现行第6课，明确标2023参考。合并全部来源后八上19/20（缺17）、八下11/20（1/2/3/4/6/9/16–20）、九年级1/18（13）；九年级来源及汇编仅确认第13课，其他旧教材主题不得按旧号硬挂现行课次。
- 恢复2份按同名误判重复的PDF；审查61个本批配套包及嵌套压缩包，10包净化755项环境配置/运行库/日志/安装程序/缓存，源码素材保留。新增50个内容地址文件595,059,944B，线上逐文件大小/哈希通过；不改源目录、不执行来源程序、不覆盖旧文件。
- 正式324课例/1,033资源/2评价/13汇编/6指南；备份逐行复核原308课例和2评价均未变，仅计划内10包引用等字段变化、新增16课例18资源；重复和孤儿0。整理工具按内容哈希去重、递归净化，13册覆盖说明绑定已审核来源PDF哈希，避免再生错误说明。
- 实测：隔离正式前端构建/门禁及3项净化测试通过；13汇编真实预览页图、16参考预览、匿名401通过；真实教师1440/390/320书架与阅读页预览、真实下载/关闭、指南数量、无横向溢出/0页面JS异常。首页/验证码200，后端PID/NRestarts0保持；更新155/1.30.77 PUBLISHED真实教师可见。学生本轮登录失败，未将失效态401记为403。
- 已执行`sql/teaching_resources_20261108_hotfix.sql`，无DDL/后端配置/依赖变动，仅Nginx reload。备份`20261108_teaching_hotfix_v1/database.sql` SHA`e8483c66…`；精确回退SQL已修正快照换行转义并在独立副本逐字段恢复308/1,015/2及13汇编，未在正式回退。回退先切1.30.76旧前端，再按需执行热修回退SQL，不回灌整库、不需重启后端。
- 待补：有效学生身份403重验、真实机房整班、慢响应切换竞态专项、9份作者与缺课来源。1.30.77发布时未混入选择题图片/教师工具等在研功能。当前证据与准确回退见[1.30.77验收](textbook-teaching-resources-20261107/hotfix-1.30.77.md)；[1.30.76历史验收](textbook-teaching-resources-20261107/verification.md)、[需求](textbook-teaching-resources-20261107/requirements.md)、[设计](textbook-teaching-resources-20261107/design.md)、[任务](textbook-teaching-resources-20261107/tasks.md)。

## 已上线1.30.75：课堂 Python 示例展示与旧浏览器服务端运行（2026-11-07）

- 教师反馈机房 Chrome 109 运行课堂 Python 失败：本机运行依赖 JSPI（Chrome/Edge 137+），旧浏览器被拦截；Pyodide 0.29.3 仍只支持 JSPI，升级无效。现按运行时能力分流：支持的浏览器保持本机交互运行，代码框上方恢复“示例（输入/预期输出）”；不支持的浏览器自动改调 `POST /business/student-home/programming/server-run`，并显示一行弱化提示“当前浏览器版本较低，已改为服务器运行；建议升级谷歌浏览器最新版。”
- 后端 `ProgrammingSubmissionService.runPublicCases` 只跑公开测试点，不写提交/草稿/成绩/历史，按学生+班级双重限流；无 SQL、无配置变更，前后端同步发布并重启后端。
- 生产验收（2026-11-07）：学生 2025300101 的 `python6`/990787 正常加载；示例显示“输入15/预期输出是”；真实 Judge0 `server-run` 1.17s 返回，正确代码 0.58s `ACCEPTED`；Playwright 新版浏览器本机运行无提示，模拟无 JSPI 旧浏览器服务端运行 200、提示 12px `#98a2b3`、390px 无横向溢出、0 JS 错误；启动 0 ERROR、NRestarts 0。更新 153/1.30.75 PUBLISHED 教师可见。证据见 [发布登记](RELEASE_LOG.md) 与 [python-judge0/design.md](python-judge0/design.md)。
- 待验：真实机房 Chrome 109 整班使用、Judge0 整班并发与限流表现。发布规则：**禁止用整份工作区快照打包**；v1 因此带入其他会话未提交的 `media_json` 查询致学生学习页 500（已回滚）；v3 用 HEAD `9d920c4` + 本功能文件 + 按发布清单哈希复原的 1.30.74 五个源码文件重建，确认业务 JAR 无 `media_json`、教师题库页 26 卡/出题教师/0 JS 错误。

## 已上线1.30.74：题库预览改进（2026-11-06）

- 卡片显示接口已有的教师姓名 nickName，缺失标“姓名未登记”；打字正文只显示一次。Python 弹窗包含题干、输入输出说明、起始代码、参考答案和测试数据；流程图分学生起始图/参考答案图，均只读。
- 沿用原后端鉴权：完整 Python 配置需创建者/管理员及编辑权限，流程图答案需创建者/管理员；他人公开题仅学生起始内容/公开样例，答案页签明确权限限制。无后端、数据库、新依赖或配置变更；发布仅需前端制品。
- 生产构建及40项门禁通过；真实Vue页面配隔离模拟接口验证姓名、去重、Python各内容/失败重试、流程图双页签、1440/390/320布局，0页面异常。正式真实师生只读验收通过：Python4测试点/真实答案、流程图双版本、学生课252/20题和刷新、窄屏，无页面异常/HTTP500；后端PID/JAR/unit/env保持，旧763个静态资源保留。更新152/PUBLISHED教师可见，备份/回退及未验边界见[发布验收](question-workbench/release-20261106.md)。

## 历史发布 1.30.72/73 与题库工作台

- 1.30.73 修复课程推进后 Python 判题终态，1.30.72 包含题库工作台、权限审查、PY 作品和成绩按合计排名；当前正式以顶部 1.30.74 为准。
- 1.30.72 曾因均分/合计不一致回退，后经线上逐人核对收敛；原始证据、未闭环批改/跨校风险和当时验证边界完整保留在 [2026-11-06 精简前原文](PROJECT_HISTORY_20261106_before_choice_images.md)。
- 专题入口：[1.30.73](class-readiness-20261105.md)、[题库工作台](question-workbench/tasks.md)、[PY 作品](practical-python-upload/tasks.md)、[发布日志](RELEASE_LOG.md)。

## 当前焦点：2025 宁波优秀课例批次（2026-11-04）

- **已上线 1.30.71**：来源2172文件/19,941,483,508B、94个候选（85目录+9册目录压缩包）；上架90课例/497资源（355文档、59视频、19独立网页、64配套包），覆盖三上至八下12册。3份「人工智能中的算法」现行八下无对应课次保留PENDING，王蕙兰八下目录与三上同内容按重复处理。[规则](textbook-cases-2025/requirements.md)、[设计](textbook-cases-2025/design.md)、[闭环表](textbook-cases-2025/resource-closure.md)、[验收](textbook-cases-2025/verification.md)。
- **视频与网页边界**：本批统一100,000,000字节上限；课堂实录/教学录像无论大小排除（排出6个录课、3个超限、5个实名学生视频）；2个WMV只下载未转码。独立HTML新增`/business/textbook/cases/html/{id}`，以`CSP sandbox allow-scripts`（无allow-same-origin）+no-referrer+教师Cookie鉴权打开，作者页读不到平台Cookie；依赖同目录资源的网页只随包下载。
- **数据事实**：本机与130均142课例/693资源，孤儿0、重复0；本机旧52条`case_id`/196资源字段与2条评价逐字段未变，130未触碰评价/邀请表。SQL仅upsert无DELETE，`book_id`按`book_code+section_key+kind='LESSON'`解析。备份`20261104_ningbo2025_cases_v1`整库597,425,809B/SHA-256`9defe7af…cb00`，旧release/备份保留。
- **复查**：交回原AI的[自包含Bug复查提示词](textbook-cases-2025/review-prompt.md)；风险集中在教材映射歧义、WMV转码、学校机房实测与学生端正式负例。

## 上一焦点：优秀课例连续滚动预览（2026-11-03，1.30.70）

- 课件/教学设计等页图上下连续阅读，滚轮/触摸、页码、缩略图、跳页、缩放联动；按可见窗口及邻页加载、撤销离开窗口URL，单页失败可重试，保留横竖页尺寸及缩放锚点。评价计时仍只累计成功可见页面。1.30.71 前端在其基础上只新增「打开网页」按钮与API，连续滚动回归通过。

- 连续布局4项、前端构建16项及门禁、本机200页模拟/重试/保位、正式桌面/矮屏/390/320真实滚轮和触摸通过；无页面JS错误。后端PID/JAR/unit/env保持，三服务active，旧release/备份保留；无业务迁移，仅PUBLISHED更新SQL。[证据与回退](textbook-cases/release-scroll-20261103.md)。旧浏览器/低配机房未实测。

## 上一版稳定事实：优秀课例评价与教材体验（1.30.69）

- **正式1.30.69已上线并验收**：作者课例推荐/不推荐、改票/撤销、匿名文字理由、低频自愿邀请；52课例196资源、44配套包，包内内容摘要与条件同源。材料仍离线维护，评价独立可写；共享Key保留但未调用验证。
- 独立复核7项缺陷及真实并发发现的锁升级/旧快照问题已修复；菜单数据库与前端排序同步，隐藏同名路由不抢定位。teacher存在即可评价，兼researcher不被排除，纯管理员/纯教研员/学生不可评价。
- 验证：Java20项、真实MySQL、真实本机12并发、联网识别3项、前端构建含16项既有测试/门禁及菜单6项通过；异常UI与正式桌面/矮屏/390/320通过，手机全屏x/y=0。正式52/196/44包、307文件哈希、预览下载、学生基础页和权限负例通过，未造生产测试评价。
- 三服务active、后端NRestarts0、env保持；只定向替换正式business库4个课例类，未混入概览/登录等未发布改动。已执行评价/菜单SQL、审核清单导入和PUBLISHED更新，旧release/整库及元数据备份保留。[证据、哈希与回退](textbook-cases/release-feedback-fix-20261103.md)。
- 限制：学校机房工具与共享Key有效性未逐一实测；真实验收教师为teacher+researcher，登录/阅读/菜单/角色准入已验，未代写生产有效评价，其他双角色账号未逐人操作；大视频仍遵循超过100MiB提示下载。当前无本轮阻断问题，关注教师实际使用反馈。此前1.30.68的52/201为历史基线，见[旧发布](textbook-cases/release-20261103.md)。

## 平台概览/登录慢查询（2026-11-03，已随 1.30.72 上线）

- `/business/platformOverview/summary` 的三条趋势/学校统计都带冗余自连接派生表 `inner join (select max(answer_id) ... group by student_id, lesson_id, question_id)`；`biz_student_answer` 已有唯一键 `uk_student_lesson_question` 保证该组合只有一行，该派生表等于把整表再分组物化一遍。去掉后本机同数据单条 7134ms→157~640ms。
- 作答人次与试卷均分原本各查一次同结构派生集，已合并为一次扫描；`count(1)` 口径不含分数、`avg(sum(score))` 依赖 NULL 传播跳过无分试卷，逐月对比新旧结果 4 项差异检查全部 0 行（05～09月人次 6803/2962/827/1/3，均分 51.2/53.5/56.7/空/34.5）。
- 概览增加 60 秒结果快照：本机 `/summary` 首次 829ms、二次 7ms；返回结构未变，前端趋势图与学校表无需改动。
- 发布前必须执行 `sql/platform_overview_perf_20261103.sql` 三项前检（唯一键存在、重复组 0、执行计划走时间索引）；脚本另附可选覆盖索引 `(submit_time, lesson_id, student_id, score)`，本机实测 205ms→157ms 后已还原，未保留。
- 登录页 `selectLessons` 已改写：三个来源先各自按课/学生聚合再合并（不再逐行读取 TEXT 答案列做 TRIM，90 天窗口下那是最主要回表开销）、先锁定最近 200 门候选课程再展开班级、课程只按 lesson_id 关联。900 天放大测试 3.4s→1.2s 冷 / 0.4s 热，最近 6 个班级结果与旧写法逐条一致；`/business/login-preview/lessons` 实测 322ms 首次、6ms 缓存命中；`LoginPreviewServiceTest` 13 项通过。
- **多校修正**：展厅原先要求课程 `dept_id` 等于班级 `dept_id`，一名教师跨校任课时（本机课程 34 归属校 139、班级校 169 共 7 个班）这些课次整条不出现。解除后候选班级 832→839，多出 7 条、丢失 0；展厅学校取班级所属校、教师取课程创建者，教师不按单校归属限制。
- 本专题随 1.30.72 上线，130 已按同一业务 JAR 运行；`sql/platform_overview_perf_20261103.sql` 为可选覆盖索引与前检，本轮未执行（唯一键与执行计划前检已通过），索引保留为可选优化。上线后概览实际响应与登录展厅耗时未在 130 再测。

## 已上线基线（2026-11-02）

- **Python 课堂终端**：仅 `StudentProgrammingQuestion` 改为“运行→input 提示→输入/回车→回显结果”，去掉重复说明、公开样例和预填 stdin，保留草稿、交作业及折叠历史；独立题单与 DATA_LAB 不变。本站 Pyodide 0.26.4 + 独占 Worker + JSPI/run_sync，不改写学生代码；加载180秒、执行10秒、等待输入5分钟和输出64KB保护。正式 SUBMIT 及最高分规则不变。[需求/设计/验收](python-interactive-20261102/tasks.md)、[ADR-016](python-judge0/adr/ADR-016-course-browser-interactive-run.md)。
- **跨校同届同班号监控**：`ClassGroupingService.resolveManagedClassDept` 多校时采用任课学校集合内的 token 当前学校；当前学校不在集合仍拒绝，单校以任课关系为准，admin 不变。真实库确认反馈教师651666在155/181两校均有2026级1/2班；一般教师真实桌面返回43人。定向10项和业务865项通过；651666无已知凭据，未以其账号逐校实操。
- **拓展班前端入口**：教师首页和班级管理按 teacher/admin 放行双角色教师，首页区块可展开/收起；真实双角色教师已打开新建表单并进入拓展班页签。未创建生产测试班级或改成员。
- **教材封面**：新增鉴权 `/business/textbook/books/{bookId}/cover`，13张360px派生图在 `TEXTBOOK_ROOT/covers`，缺失回退第一页；`private,max-age=3600` + `Vary: Authorization` + ETag。正文页仍走原鉴权/no-cache。13张封面真实接口与浏览器均加载成功，304通过。
- **学生打字拼音提示（1.30.67，仅前端）**：学生端打字原文可显示/隐藏无声调拼音（`ü→v`），按题独立、首次展开懒加载、正文变化重算、失败可重试；不进入答案/字数/评分，刷新/切课默认隐藏。本机真实学生页桌面/390px、切换/重试/粘贴拦截已验收；正式学生测试账号当前课只有选择题，未在生产点开拼音。[发布登记](RELEASE_LOG.md)。
- **登录公开开课概览**：仅 lessons 缓存改为1小时、到期前5分钟预刷新，活动与焦点图仍60秒；登录后课堂、作业和成绩读取不受此缓存影响。
- **验证**：Java业务865项（0失败、3跳过）、课堂监控定向10项、Python控制6项/真实运行10项、Vue3生产构建通过。130真实学生桌面输入8→请买票、390px触控输入3→免票，整页无横向溢出，师生页面0 JS错误；整册PDF 16,816,368B/1.44秒、本课767,229B/0.08秒。新后端18:23:58启动，PID2853051/NRestarts0，三服务active，启动期0 ERROR，29项env未变。未代交正式作业或改成绩。
- **限制**：学校旧浏览器/低配整班内存压力待验；JSPI不支持时须用新版浏览器。`input("提示")` 的提示仍计入正式stdout比较，本机运行成功不等于判题通过。拓展班生产加人/签到写链未造测试数据，旧50班迁移仍待逐人核对。

## 今日中午已上线的基础能力（1.30.64/65）

- **拓展课双空间**：原账号保留行政班，最多一个活跃拓展班；账号偏好、独立签到、Python空间隔离、历史只读。签到读课时冻结名单，题单学情读投放名单；新成员补记已发布题单，移出/归档不缩历史。4表7列及历史快照增量已在130执行，旧50班尚未登记；登记后立即重跑 `extension_legacy_snapshot_v1.sql`。旧主库DATA_LAB历史依赖未治理。[专题](extension-class/tasks.md)。
- **双角色规则**：有teacher即可管理自己的拓展班，纯教研员和学生无管理权；负责人候选仍排除researcher身份，管理员不能从下拉选双角色教师。1.30.65后端规则与1.30.66前端入口一致。
- **P0/P1/P2**：Python题单JOIN改二进制比较解决1267；学生空间接口失败降级常规首页；空间状态换账号清理并丢弃迟到响应；拓展/legacy课程在普通推进、分组、物联、协作入口拒绝；候选学生最多201条探测截断，不逐行查部门；看板普通读不持课程锁。
- **课程教材绑定**：课程设计器按年级/学期非必填单选本课教材课次，落两列；教师课程卡和已绑定学生顶部直达课次。旧绑定下架/目录失败不能静默清空，相同绑定不阻断改其他内容。130现已有1条绑定。[专题](textbook-module/tasks.md)。
- **PDF**：教师/教研员/管理员可下载整册/本课，学生不可下载；页数/连续性校验，全页哈希缓存，全局最多2份构建，缓存清理每小时最多一次、14天/40份；1.30.66真实下载复验通过。学生可读已绑定教材整册属既定取舍。

## 接管入口与系统

- 按 `AGENTS.md` → 本文 → [架构索引](../docs/architecture/INDEX.md) → 任务相关专题接管；源码与现场证据优先，历史文档不能覆盖最新状态。
- 后端 `RuoYi-Vue/`：Java8 / RuoYi / Spring Boot / MyBatis；唯一前端 `RuoYi-Vue3/`：Vue3 / Vite / Element Plus。不要改旧 `ruoyi-ui`。
- 默认本机开发，后端8080、前端80。本机库 `xueyeceping_server_20260729`，监管/免抽测为本机迁移基线，不能直接推断130具备所有表列。
- 本机启动显式 `-Dserver.port=8080`，避开环境 `SERVER__PORT`；教材需要 `TEXTBOOK_ROOT`。配置/资源变更须clean package；指定用例加 `-Dsurefire.failIfNoSpecifiedTests=false -DforkCount=0`。
- 130为Linux/systemd/Nginx正式主站，API3009、站点80；123为旧Windows/NSSM栈，不混用发布命令。130经123跳板，沿用固定SSH指纹；私密凭据只在 `secrets.local.md` 或环境，不打印。
- 130外置配置依赖 `20261008_seat_install_v1/config`，不可清理依赖release；新release工作目录须可写logs/MQTT持久化。教材图片独立位于 `TEXTBOOK_ROOT`，不放公开 `/profile`。

## 不可混淆的业务边界

- 管理员、教师、学生、教研员复用原账号、学校、班级和权限；服务端鉴权/数据范围是最终边界，前端隐藏不能替代。
- 历史学生可能无sys_role学生角色；学生接口用 `studentSs.isStudent()` 含biz_student兜底，本人作品预览/下载不得退回严格student角色判断。
- 课程推进后旧课写入立即拒绝；教师退回和学生重交仅限当前课。旧作品、成绩仍可查阅和批改；旧15分钟补交已失效。
- 正式1.30.53文件作业已有分数可自主重交，流程图有正式成绩仍须先退回。0分也是已批；成功重交才归档旧分并转待批，暂存/取消/失败不清分，AI建议须教师采用才写正式成绩。
- 作品、答案/当前版本指针、课堂状态需事务一致；成绩/反馈/导出共享未提交事实。历史残留不能因代码已修称已治理。
- TXT/单HTML/ZIP/RAR/7Z须题目显式允许；TXT/HTML≤2MiB，文件名含扩展名≤100字符；HTML独立沙箱、压缩包不解压，不进入Office转换或AI图像评分。
- 学生反馈只返回本人正式作答/成绩；教师整班发布后生成不可变个人快照，重试不自动替换已发布反馈，作品变化提示旧来源。
- 答案/成绩、课堂状态与终端在线Presence为不同事实；推送在事务提交后发出，REST负责校准。独立Python不写课程答案/成绩/推进，物联不新造评分体系。
- 教师模型Key按教师/厂商加密，任务按创建时厂商取Key；未知单价不能写0，接口契约通过不等于真实评分准确。

## 已发布能力与专项证据

| 能力 | 当前事实与边界 | 证据 |
| --- | --- | --- |
| 电子教材/课标 | 16册1840页（小学初中13册+高中3册539页）、2份课程标准；教师书架、高中分组、课标在线预览/下载、附件鉴权+ETag+no-cache | [教材任务](textbook-module/tasks.md)、[1.30.87验收](textbook-module/release-1.30.87.md) |
| 机位助手0.2.6 | 默认C盘、迁移旧D盘配置；真实下载和53项回归已有证据，机房同传唯一性仍待验 | [发布日志](RELEASE_LOG.md)、[本机验证](seat-helper-local-20261014/verification.md) |
| 导学单/诊断 | 1.30.60修模板预览执行、自动保存、满分分母；慢SQL拦截6参查询已修 | [导学单审查](guide-sheet-audit-20261027.md)、[诊断专题](diagnosis-perf-20261028/) |
| 流程图AI标准答案 | 1.30.56～59已上线生成/排版/正交布线/判断分支左右端口；保留版本评分保护 | [专题](flowchart-ai-answer/tasks.md)、[发布日志](RELEASE_LOG.md) |
| 作品与页图 | 无student角色本人附件权限已修；PPTX端点和Word连线坐标已有正式修复；固定A4截底、Linux分页仍待治理 | [PPTX](pptx-preview-fix-20261015/tasks.md)、[连线](connector-render-fix-20261013/verification.md)、[剩余页图风险](grading-overflow-reframe-20261014.md) |
| 成绩与打字 | 已知分/待批分开，Python和打字保留最高；此前学生机房性能待验不因登录优化视为解决 | [成绩专题](score-partial-20261016/tasks.md)、[正式验收](seat-score-release-20261017.md) |
| LibreOffice | 毒文件隔离持久化、健康恢复/Quartz已有正式修复，旧空作品和派生表积压不自动视为清理 | [历史完整事实](PROJECT_HISTORY_20261102_before_13066.md) |

## 迁移、发布与回退

- **1.30.91（2026-11-10，仅数据）**：不换前后端、不重启、不改表结构；仅更新资源2314说明并登记169/PUBLISHED。整库备份`/data/backups/xueyeceping/20261110_060850_20261110_case_summary_v1/database.sql.gz`，53,147,066B，SHA-256`4ff6cf0e…`。回退运行`python scripts/deploy/update_case_summary_130.py --mode rollback --backup-dir <备份目录>`，恢复旧说明并把169转DRAFT。详见[1.30.91验收](research-to-textbook-cases/release-1.30.91.md)。
- **1.30.90（2026-11-10）**：前后端`20261110_python_grading_snapshot_v1`，仅执行`sql/platform_update_1_30_90_20261110.sql`登记168/PUBLISHED，无表结构变更。回退恢复备份`/data/backups/xueyeceping/20261110_000343_20261110_python_grading_snapshot_v1/service.before`和`nginx.before`，daemon-reload、重启后端、reload Nginx，更新168转DRAFT；不整库回灌。详见[1.30.90验收](python-grading/release-1.30.90.md)。
- **1.30.89（2026-11-09，仅前端）**：Nginx根为`20261109_grading_mobile_v1/frontend`，后端仍`20261109_python_grading_fix_v1`；只执行产品更新`sql/platform_update_1_30_89_20261109.sql`登记167/PUBLISHED。回退恢复备份`/data/backups/xueyeceping/20261109_234706_20261109_grading_mobile_v1/nginx.before`并reload，更新167转DRAFT；不重启后端、不整库回灌。详见[1.30.89验收](python-grading/release-1.30.89.md)。
- **1.30.88（2026-11-09）**：后端+前端`20261109_python_grading_fix_v1`，执行`sql/python_grading_stability_20261109.sql`新增最高分索引，登记更新166/PUBLISHED。回退恢复该备份`service.before`/`nginx.before`并重启后端、reload Nginx；新增索引和已补建正式答题行保留，不整库回灌。详见[1.30.88验收](python-grading/release-1.30.88.md)。
- **1.30.87（2026-11-09）**：release`20261109_high_school_textbooks_v1`，基于`20261109_research_python_grading_v1`增量构建；执行`sql/high_school_textbooks_20261109.sql`新增`biz_textbook_standard`并导入高中3册539页/2份课标。整库备份`/data/backups/xueyeceping/20261109_203740_20261109_high_school_textbooks_v1`，`database.sql.gz`53,167,168B，SHA-256`42ea3ea4b702d9850efa86ffc75afa875a74a1e54ca293ad0d3b09fc24263e99`，含旧unit/site/env/JAR/index；产品更新`sql/platform_update_1_30_87_20261109.sql`登记165/PUBLISHED。回退见[1.30.87验收](textbook-module/release-1.30.87.md)，不整库回灌。
- **1.30.72（2026-11-05）**：备份 `/data/backups/xueyeceping/20261105_review_py_workbench_v2` 整库 gzip SHA-256 `0350dfda788208a86aa6afb83c2c97248c2f59b7dc04bb38a9da39f03fb4e306`，含 unit/site/env/JAR/index/config 与 SHA256SUMS；首次尝试的 v1 备份（`7bc84a0c…`）与 release 目录保留。仅执行 `sql/review_workbench_release_20261105.sql` 登记更新 150。回退脚本 `output/release-20261105/rollback.py`。
- 1.30.66备份 `/data/backups/xueyeceping/20261102_all_updates_v1`：整库52,104,859B，SHA-256 `118839d5…cad4ef`，含旧unit/site/env/config/JAR/index与SHA256SUMS。新release独立创建，旧release/备份保留。
- 本轮无结构/业务SQL；仅执行幂等 `sql/all_updates_release_20261102.sql` 登记更新144/PUBLISHED，真实教师 `/business/platform-update/list` 可见。封面13张为新增派生资源，env29项未变。
- 回退：从本次备份以0644恢复 `service.before` 到systemd unit、`nginx.before` 到站点配置，daemon-reload+重启后端、nginx -t+reload，退至后端1.30.65/前端1.30.64；更新144改DRAFT。无需回灌业务库、删封面或迁移回滚。
- 首次切换因Nginx reload生效窗口未立即读到新首页而自动回退，修正为新连接最多等待30秒后第二次成功；最终HTTP/磁盘首页哈希一致。不能把首次候选当成功上线。
- 130机位两列迁移已执行/复跑，本机状态不同；课堂体验、AI多厂商、监管和免抽测等依赖须按目标库前检，保持原AI主密钥，禁止默认全库回灌。
- 重大变更同步核心/架构/需求/设计/任务/ADR；正式成功登记RELEASE_LOG和PUBLISHED更新，无发布不造产品版本。生产制品隔离无关修改，Git push仍须用户明确授权。

## 下一步与未闭环风险


1. 跨校反馈账号651666分别进入155/181两校监控复验（当前无凭据）；旧50班逐人账号映射和历史登记，不按同名自动冻结。
2. Python学校实际浏览器与低配整班加载/内存；提示语与正式期望输出兼容需单独明确规则；本轮未改评分、未造生产签到/成员/成绩。
3. 机房真实助手认领、换人、LNA/HTTP权限、同传还原唯一性、自启与250机压力；共用device_id和同传指纹风险仍保留。
4. 固定A4截底、Linux字体分页/未知图形、118条历史空作品、课程时间/归属/跨校指派冲突仍按原证据治理，不用当前时间伪造修复。
5. 普通课打字延迟仍待专项；本地课堂推进和题库切页改动仍未发布：[推进](course-advance-20261020/tasks.md)、[题库切页](question-navigation-fix-20261020.md)。
6. 三厂商真实模型准确率、旧DATA_LAB快照/AI空评分及无角色学生完整重交批改链按各自专题补验；文档/单测不能替代真实业务验收。
7. 学生打字拼音提示已随1.30.67发布；正式学生测试账号当前课无打字题，真实打字课的师生使用反馈、旧浏览器和窄屏体验仍待观察。
8. **跨校教师仍缺课**：教师首页 `getTeacherDashboardData` 按 `loginUser.deptId` 单校取班级、自建课和共享课，跨校任课的第二个学校整块看不到；本机 5 名教师跨两校（139/169、133/166、167/181、120/183）。修这里要改班级分组口径和课程查询的学校范围，属权限边界变更，需单独立项确认后再动。
9. 1.30.82图片功能待补：真实WPS/Excel多版本内嵌图、整班并发图片上传/读取、TEMP七天清理跨期、老旧浏览器和区域抽测含图题真实页面；磁盘容量告警仍需上线后观察。
10. 1.30.87前端 `getAttribute` 空值脚本异常已定位并修复：平台总览接口迟到后，旧页面仍对空图表容器初始化 ECharts。修复已随 `20261109_high_school_overview_guard_v1` 前端热修平滑上线，连续两次浏览器复跑0异常；老旧机房浏览器仍待真实设备观察。

## 文档维护与追溯

- 核心文档约150行，详细哈希/发布过程放专题或日志；本次精简前原文完整保存为 [2026-11-02归档](PROJECT_HISTORY_20261102_before_13066.md)。更早归档见 [2026-10-15](PROJECT_HISTORY_20261015.md)、[2026-10-05](PROJECT_HISTORY_20261005.md)。
- 历史事实索引 `context.md` 仅按需查关键词。AOCI已卸载，不恢复索引或重装；见 [ADR-002](../docs/architecture/ADR/ADR-002-aoci-project-memory.md)。
- 凭据/Token/Cookie仅存本机私密文件或环境，禁止写入文档、截图和发布登记；未验证的事项明确标为待验。
