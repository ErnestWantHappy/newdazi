# 架构资料索引

2026-11-11 **当前正式1.30.93（仅前端）**：130前端 `releases/20261111_case_panel_resize_v1`（首页`447e7bc5`），后端仍1.30.92（JAR`a8162e41`，MainPID`3107625`、NRestarts0）；教材资源面板可拖宽、课例标题左对齐，产品更新171/PUBLISHED。详见[发布日志](../../contexts/RELEASE_LOG.md)、[部署与回滚](DEPLOYMENT_RUNBOOK.md)。

2026-11-10 **本机在研、未发布**：Python 全链路交互运行与判题统一。新增`JudgeSourceWrapper`（提示语打标记，不做正则删除）、`JudgeOutput`（plain/visible/terminalText）、`JudgeOutcome`（统一状态与加权分数）；`PythonTerminal.vue`统一教师批改、学生刷题、`.py`作品三个运行入口，学生做题页复用同一运行内核与交互规则；高版本浏览器本机跑，其余自动降级且保留已填输入。新增三个不写成绩的接口（刷题调试运行、教师直接运行、生成期望输出），无表结构变更。2026-11-11 复跑：后端业务全量1017项通过/0失败/3跳过，前端20项与生产构建通过；真实浏览器验证教师“运行查看”和学生刷题在本机Pyodide/服务器两种模式下均为“提示语→输入→输出”，页面脚本错误0。详见[设计与边界](../../contexts/python-grading/design-interactive-terminal.md)、[任务与证据](../../contexts/python-grading/tasks.md)、[当前事实](../../contexts/PROJECT_CORE.md)。正式环境前端已为1.30.93（教材资源面板可拖宽），后端仍为1.30.92；本项 Python 交互仍未发布。

2026-11-10 当前正式1.30.91：仅数据修正，130前后端仍为`releases/20261110_python_grading_snapshot_v1`（JAR`ec36df3f`、首页`f66da562`），MainPID`3097248`、NRestarts0、启动错误0。顾意丽《返回成语现场》配套材料包说明由1045字技术清单改为91字教师说明，产品更新169/PUBLISHED真实教师可见；1.30.88完成Python批改最高分代码同版本、无答题行补建、批量采用行锁/个案容错、最高分索引、课例来源空值修复与导入脚本加固；1.30.89完成批改页手机窄屏纵向布局；1.30.90补页面快照并发保护。产品更新166/1.30.88、167/1.30.89、168/1.30.90、169/1.30.91均PUBLISHED。电子教材16册/1840页、高中3册539页、2份课程标准、327课例/1050资源保持。[Python专题](../../contexts/python-grading/requirements.md)、[1.30.91验收](../../contexts/research-to-textbook-cases/release-1.30.91.md)、[发布日志](../../contexts/RELEASE_LOG.md)、[ADR-020](ADR/ADR-020-python-grading-best-submission-source.md)。

2026-11-09 已随当前release上线：教研活动课例导入3节（post 15郑东旭《语音合成》、post 60赵琪《返回成语现场》、post 70顾意丽《返回成语现场》），总课例327/资源1050；来源活动、`RESEARCH_ACTIVITY`标签、压缩包/音频识别和窄屏换行已生效。2026-11-10 1.30.91将顾意丽资源2314的教师说明改为91字，并修正后续压缩包说明生成规则。专题[需求](../../contexts/research-to-textbook-cases/requirements.md)、[设计](../../contexts/research-to-textbook-cases/design.md)、[任务](../../contexts/research-to-textbook-cases/tasks.md)、[1.30.91验收](../../contexts/research-to-textbook-cases/release-1.30.91.md)、[ADR-018](ADR/ADR-018-research-post-idempotent-case-import.md)。

2026-11-09 此前1.30.86：实际登录成功首次进入首页时，桌面Logo放大至220px、手机128px，居中停留约1.2秒，期间遮罩完全隐藏首页内容；Logo移向左上角时遮罩渐隐、页面内容逐渐出现。刷新/站内跳转不重播，减少动态效果时跳过。前端`releases/20261109_brand_logo_hold12_v1`首页`be732a83`，后端当时仍为`releases/20261109_choice_media_v1`；更新164/PUBLISHED曾教师可见，现由1.30.87接续。[历史验收](../../contexts/brand-logo-entry/release-1.30.86.md)。

2026-11-09 此前1.30.83：教师课程设计页改为“课程内容设计在上、教学资源库在下”，宽屏不再左右并排，手机端顺序一致，帮助教程文案同步；同一前端包已修复教师工具动态卡片样式，恢复图标色块、标签底色和分区渐变。课程布局改两个前端文件、样式修复改一个前端文件，后端与数据库保持1.30.82。前端`releases/20261109_lesson_designer_layout_v2`首页`0d2aa234`，合并旧static后1,981文件；真实教师1440/390验收0横向溢出、0脚本异常、0个5xx，更新161/PUBLISHED教师可见。[教师工具样式验收](../../contexts/teacher-tools/release-20261109-style.md)。

2026-11-08 已上线130正式1.30.77：13册教学设计汇编增加在线预览与准确覆盖说明；新增八上15课、八下1课2023参考，合并全部来源后八上19/20、八下11/20、九年级1/18。恢复2份PDF、净化10个配套包，指南显示179可用/180课次。无DDL和后端改动，前端独立release、Nginx平滑reload；学生本轮有效身份未重验。当前事实、逐行备份比对、副本精确回退和未验边界见[1.30.77验收](../../contexts/textbook-teaching-resources-20261107/hotfix-1.30.77.md)。下方1.30.76为历史状态。

2026-11-07 已上线130正式1.30.76：电子教材栏目改名“电子教材与教学资源”，宁波2026教学设计按现行课次补充（166新课例、48旧课例补同课版本），13册整册汇编与3–8年级课程指南（6组180课件，五年级第28课暂未提供）独立展示；新增三张表、课例来源列与附加原件列，资源按`source_key`跨库落位，教师/教研员/管理员鉴权、学生403。正式142→308课例、693→1,015资源、旧评价与693条旧资源不变；备份与回滚实测通过。[验证与回滚](../../contexts/textbook-teaching-resources-20261107/verification.md)、[需求](../../contexts/textbook-teaching-resources-20261107/requirements.md)、[设计](../../contexts/textbook-teaching-resources-20261107/design.md)、[任务](../../contexts/textbook-teaching-resources-20261107/tasks.md)。

2026-11-09 选择题图片增强A已随130正式1.30.82上线：题干最多3图、选项各1图、上传/粘贴/拖拽，Excel图文预览/修正/原子确认，已引用含图题自动另存与回执找回；普通课、错题、学情与个人抽测透传。生产原图在`/data/upload-question-images`，使用登录身份+题目/课程凭证读取，不进入公开静态根。真实WPS/Excel变体、整班并发、七天清理和区域抽测图片页仍待验。[需求](../../contexts/choice-question-media/requirements.md)、[设计](../../contexts/choice-question-media/design.md)、[任务](../../contexts/choice-question-media/tasks.md)、[ADR](../../contexts/choice-question-media/ADR-001-additive-media-and-safe-save.md)、[正式验收](../../contexts/choice-question-media/release-1.30.82.md)。

2026-11-09 本机在研、未发布：Excel导入v3新增题干图片1-3与选项A-D图片共7列，旧模板兼容；图片专用列按左上角锚点归属，跨题行进入待确认，导出写回同一组列。专项16项通过，真实WPS/Excel样本仍待验。[ADR-002](../../contexts/choice-question-media/ADR-002-excel-image-columns-and-anchor-ownership.md)、[设计](../../contexts/choice-question-media/design.md)。

教师工具1.30.80专题：83工具/12分类/14精选/5任务，39工具有别名；个人常用/最近打开仅当前浏览器按账号隔离。1.30.83同包恢复动态卡片、图标、标签和分区样式，并完成真实教师11/11验收。迁移已执行，禁止重复回填；脏数据与跨设备收藏待确认。[需求](../../contexts/teacher-tools/requirements.md)、[设计](../../contexts/teacher-tools/design.md)、[验证与待办](../../contexts/teacher-tools/tasks.md)、[样式验收](../../contexts/teacher-tools/release-20261109-style.md)、[回填与待治理数据](../../contexts/teacher-tools/data-v2-20261106.md)。

2026-11-05 题库工作台本机审查修复：保存回调、查看全部事件参数、FILE分类/计数、通配权限、批量动作5项修复；前端24项/Mapper2项/隔离浏览器10组及构建通过。含后端FILE历史空值筛选，需前后端同步发布并重启后端，无迁移SQL；真实库写链待验，未发布。详见[需求](../../contexts/question-workbench/requirements.md)、[设计](../../contexts/question-workbench/design.md)、[验证与待办](../../contexts/question-workbench/tasks.md)。

2026-11-04 本机在研操作题 PY 上传、源码预览与教师 Judge0 隔离运行，尚未发布。详见[需求](../../contexts/practical-python-upload/requirements.md)、[设计](../../contexts/practical-python-upload/design.md)、[验证与待办](../../contexts/practical-python-upload/tasks.md)。

> 整理日期 **2026-11-09**。先读 `AGENTS.md` → [PROJECT_CORE.md](../../contexts/PROJECT_CORE.md)，再按下表读任务相关资料；不默认全文扫描或通读历史。
>
> 最新正式：130 **1.30.91**（数据修正；前后端仍为`releases/20261110_python_grading_snapshot_v1`，JAR `ec36df3f`、首页`f66da562`，助手0.2.6）。1.30.88修复Python批改最高分代码/分数一致、无答题行补建与批量采用个案容错；1.30.89修复手机窄屏批改布局；1.30.90补页面快照并发保护；1.30.91精简顾意丽配套材料包说明并修正生成规则。电子教材16册1840页、2份课程标准、327课例/1050资源继续有效。详细证据见[1.30.91验收](../../contexts/research-to-textbook-cases/release-1.30.91.md)、[Python批改专题](../../contexts/python-grading/tasks.md)、[发布日志](../../contexts/RELEASE_LOG.md)与[数据模型](DATA_MODEL.md)。

## 按任务选择资料

2025宁波优秀课例批次：2026-11-04 已整理并上线 90 课例 / 497 资源，覆盖三上至八下 12 册；含独立网页在线预览沙箱、视频 100,000,000 字节上限与录课排除规则。规则/设计/闭环/验收/迁移/复查见[专题](../../contexts/textbook-cases-2025/requirements.md)。3 份「人工智能中的算法」教材归属待确认（PENDING），2 个 WMV 未转码。

2026-11-03 · 1.30.70课例预览改为连续上下滚动：可见窗口与邻页加载、页码/缩略图/跳页/缩放同步，后端与资源保持；正式四尺寸实测通过。[本轮证据](../../contexts/textbook-cases/release-scroll-20261103.md)。

2026-11-03晚间课例功能及独立复核缺陷已随1.30.69正式收口：教师唯一评价与双角色规则、44包及使用条件、菜单实际顺序、多尺寸预览；并发锁/快照问题已实测修复。[本轮事实与限制](../../contexts/textbook-cases/release-feedback-fix-20261103.md)。下方旧记录保留为历史。

2026-11-03教师优秀课例已按18:00安排发布，312文件校验、52/201导入、真实师生与桌面/390px验收通过；更新146/PUBLISHED教师可见。现场基线、隔离制品、迁移/切换及回退见[发布准备](../../contexts/textbook-cases/release-20261103.md)。
2026-11-03教师优秀课例本机52课例/201资源，全量接口0失败。已补齐具名HTML遗漏：新增34包95入口，合计覆盖125非重复HTML/HTM入口；34包下载哈希、五个关键工程与教师桌面/390px验证通过。原作者自愿共享模型Key按用户确认保留，失效可换自己的。正式1.30.68已上线、工具未机房实测。[收口清单](../../contexts/textbook-cases/resource-closure.md)、[设计](../../contexts/textbook-cases/design.md)、[验证](../../contexts/textbook-cases/tasks.md)、[接力提示词](../../contexts/textbook-cases/handoff.md)。

2026-11-02学生打字拼音提示已随**1.30.67仅前端发布**：默认隐藏、按题独立切换、首次展开懒加载、正文变化重算、失败可重试；不进入答案/字数/评分。本机真实学生页桌面/390px验收通过；正式测试学生当前课无打字题，未在生产点开拼音。[发布登记](../../contexts/RELEASE_LOG.md)、[核心事实](../../contexts/PROJECT_CORE.md)。

2026-11-02课堂Python交互终端已随**1.30.66发布130**：本站Pyodide/独占Worker/JSPI等待input及输入回显，正式SUBMIT保留Judge0，独立题单不变。真实学生桌面和390px实测通过；旧浏览器/低配整班待验。[需求](../../contexts/python-interactive-20261102/requirements.md)、[设计](../../contexts/python-interactive-20261102/design.md)、[本机及正式验收](../../contexts/python-interactive-20261102/tasks.md)、[ADR-016](../../contexts/python-judge0/adr/ADR-016-course-browser-interactive-run.md)。

拓展课双空间已于2026-11-02随1.30.64/65上线，1.30.66补齐双角色教师前端入口和首页收起；账号偏好、签到冻结名单、题单投放名单和Python隔离保持。旧50班尚未逐人登记/冻结，DATA_LAB旧依赖未治理。[需求](../../contexts/extension-class/requirements.md)、[设计](../../contexts/extension-class/design.md)、[实际验收](../../contexts/extension-class/tasks.md)、[ADR](../../contexts/extension-class/ADR-001-independent-membership-and-snapshots.md)。

2026-10-29教师端电子教材已发布 130 正式 1.30.61，1.30.62 补充阅读缩放：13 册 1301 页、4 张教材表、4 个鉴权接口、书架与阅读器、课程卡「教材」胶囊；阅读器默认适应整页并支持适应宽度/50%–200% 缩放与记忆，窄屏目录为浮层；页面图 243.8MB 存 release 之外，经鉴权接口按 ETag 回图。[需求/设计/任务与验证](../../contexts/textbook-module/tasks.md)、[存储与权限取舍](ADR/ADR-004-textbook-page-images-outside-release.md)。

课程教材绑定、学生绑定课次入口和教师整册/本课PDF已于2026-11-02随1.30.64上线，1.30.66补封面360px缩略图并真实复验PDF下载。册目录/页图放行学生，书架及下载仍限教师侧；PDF并发2份、缺页校验和全页哈希缓存保持。[任务与证据](../../contexts/textbook-module/tasks.md)、[边界](BUSINESS_BOUNDARIES.md)、[数据](DATA_MODEL.md)。

2026-10-25学生作品 HTML/TXT 预览/下载权限已发布130正式1.30.55：仅替换附件控制器 1 个 class，教师/管理员保留，未批量修改 6,839 个缺角色账号。[发布日志](../../contexts/RELEASE_LOG.md)。

2026-10-20文件作业自主重交已发布130正式1.30.53：已有分数可直接提交新版本，成功前保分，成功后旧分归档、新版待批；流程图等其他题型保持。[需求/设计/测试和发布边界](../../contexts/student-file-resubmit-20261020/tasks.md)。

2026-10-20当前焦点：课堂推进预检、自动状态、逐班结果与当前课快照保护已本地验证，未发布；[专题任务/验证](../../contexts/course-advance-20261020/tasks.md)、[正式只读诊断](../../contexts/course-advance-diagnosis-20261020.md)。本专题未发布，正式版本见上方最新记录，学生及机房待验边界保持。

同日题库切页空白已本地修复：题库保持单元素根节点以兼容主容器串行切页动画，业务和缓存策略保持；[复现、4组模拟浏览器验收与发布边界](../../contexts/question-navigation-fix-20261020.md)。未发布。

| 任务类型 | 必读资料 | 需要时继续读 |
| --- | --- | --- |
| 选择题图片、Excel图文导入、私有图片鉴权与保存回执 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/choice-question-media/requirements.md`、`design.md`、`tasks.md`、`ADR-001-additive-media-and-safe-save.md`、`release-1.30.82.md` |
| Python 操作题批改、最高分代码、无答题行补建、批量采用页面快照保护与窄屏布局 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/python-grading/requirements.md`、`design.md`、`tasks.md`、`release-1.30.88.md`、`release-1.30.89.md`、`release-1.30.90.md`、`ADR-020-python-grading-best-submission-source.md` |
| 题库浏览、新增入口与按钮权限（本机未发布） | `BUSINESS_BOUNDARIES.md` | `contexts/question-workbench/requirements.md`、`design.md`、`tasks.md`；题型按模块新增与动态按钮的边界见该专题 |
| 任何开发接管 | `SYSTEM_ARCHITECTURE.md`、`BUSINESS_BOUNDARIES.md` | 对应专题 `contexts/<专题>/` |
| 自动/手动课堂推进、逐班预检和结果 | `BUSINESS_BOUNDARIES.md` | `contexts/course-advance-20261020/requirements.md`、`design.md`、`tasks.md`、`ADR-001-authoritative-preview.md`；历史下一课年级规则见`contexts/teacher-dashboard-grade-history/` |
| 普通课成绩、操作待批汇总、全体分布与自主重打最高分 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/score-partial-20261016/requirements.md`、`design.md`、`tasks.md`、`verification.md`、`ADR-001-known-scores-and-typing-best.md`；教师重做规则见`contexts/theory-redo/` |
| 课堂作品更新、新格式、学生个人反馈、长期机位助手 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md`、`INTEGRATIONS.md` | `contexts/classroom-experience-20261002/requirements.md`、`design.md`、`tasks.md`、`verification.md`、ADR-001～003、`adr/ADR-004-student-zero-config-seat-helper.md`；`clients/seat-helper/README.md` |
| SQL / MyBatis / 数据规则 | `DATA_MODEL.md` | 根目录 `sql/`、相关 Mapper XML |
| 系统诊断中心、慢 SQL/慢接口与资源鉴权性能 | `DATA_MODEL.md`、`BUSINESS_BOUNDARIES.md` | `contexts/diagnosis-perf-20261028/`、`ADR/ADR-003-slow-sql-interceptor-six-arg.md` |
| CryptPad、Judge0、EMQX/MQTT、实时推送 | `INTEGRATIONS.md` | `contexts/online-collaboration/`、`contexts/python-judge0/`、`contexts/junior-iot-poc/` |
| 小学信息科技实验板正式接入、N17 Python代码生成、EMQX班级精确ACL | `INTEGRATIONS.md`、`DATA_MODEL.md` | `contexts/primary-iot-integration/requirements.md`、`design.md`、`tasks.md`、`ADR-001-reuse-existing-emqx-and-dual-tool-entry.md` |
| Python OJ、统一题库、练习题单、课程自定义运行、班级范围与判题容量 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md`、`INTEGRATIONS.md` | `contexts/python-judge0/requirements.md`、`design.md`、`adr/ADR-008-unified-python-question-bank.md`、`adr/ADR-009-unified-practice-plan-class-version.md`、`adr/ADR-010-hard-delete-and-natural-class-label.md`、`adr/ADR-011-queued-400-submissions-on-shared-host.md`、`adr/ADR-012-course-custom-run-non-scoring.md`、`adr/ADR-015-zero-basis-v3-data-package.md` |
| 操作题作品、批改统计、删题归档、AI 建议与费用估算 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/operation-artifact-ai-grading/requirements.md`、`design.md`、`tasks.md`、`ADR-006-current-question-answers-and-ai-cost-estimation.md` |
| 连线题页图、Word/LibreOffice 坐标兼容 | `INTEGRATIONS.md` | `contexts/connector-render-fix-20261013/requirements.md`、`design.md`、`tasks.md`、`verification.md`、`ADR-001-page-absolute-temporary-copy.md` |
| 画程流程图操作题、草稿、结构检查与教师确认 | `SYSTEM_ARCHITECTURE.md`、`BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/flowchart-tool/requirements.md`、`design.md`、`tasks.md`、四份专题 ADR |
| 画流程图题“拍照 + AI 生成标准答案”（本机未发布） | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/flowchart-ai-answer/requirements.md`、`design.md`、`tasks.md`；复用 `contexts/operation-artifact-ai-grading/` 视觉模型与教师 Key |
| 教师首页共享课程、课程管理能力、学生端顶部导航 | `BUSINESS_BOUNDARIES.md` | `contexts/teacher-course-sharing/requirements.md`、`design.md`、`tasks.md`、`ADR-001-server-authoritative-course-capabilities.md` |
| 教师端电子教材、高中分组、课程标准、课次手选、打印本课、教材页图存储 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md`、`DEPLOYMENT_RUNBOOK.md` | `contexts/textbook-module/requirements.md`、`design.md`、`tasks.md`、`release-1.30.87.md`、`ADR/ADR-004-textbook-page-images-outside-release.md`、`ADR/ADR-019-curriculum-standards-as-teaching-resources.md` |
| 通用班级分组、组长、教师端学生桌面、协作小组路由 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md`、`INTEGRATIONS.md` | `contexts/class-grouping-and-desktop/requirements.md`、`design.md`、`tasks.md`、`ADR-001-server-observed-presence-and-class-entry.md`、`contexts/online-collaboration/` |
| 2026-09-02 多功能改造总计划、P0-P1 本地状态、优先级与开发提示词 | 各相关架构文档、`ADR/ADR-001-versioned-classroom-task-state.md` | `contexts/multi-feature-upgrade-20260902/requirements.md`、`design.md`、`tasks.md`、`development-prompts.md`、`handoff-prompt.md` |
| 学生批量导入性能、事务一致性、诊断分级与重复删除幂等 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/student-import-diagnosis-governance/requirements.md`、`design.md`、`tasks.md`、`ADR-001-batched-transactional-import-and-dynamic-diagnosis.md` |
| 内网发布、服务排障 | `DEPLOYMENT_RUNBOOK.md` | `AGENTS.md`、当前外置配置和服务器实际探查 |

| 架构级取舍 | `ADR/README.md` | 相关专题 ADR 和历史证据 |
| 学生实验工具、题目开放开关 | `DATA_MODEL.md` | `contexts/student-experiment-tools/requirements.md`、`design.md`、`tasks.md` |
| 站点运维与信息展示（129 监控、平台更新、Agent 词典、帮助中心配置、版权） | `INTEGRATIONS.md`、`DEPLOYMENT_RUNBOOK.md` | `contexts/site-ops-and-info/requirements.md`、`design.md`、`tasks.md`、`contexts/RELEASE_LOG.md` |
| 登录输入性能、双展厅、匿名概览 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/login-input-fix-20261014.md`、`contexts/login-showrooms-20261005/requirements.md`、`design.md`、`tasks.md`、`ADR-003-input-priority-and-lazy-vform.md` |

## 当前证据与历史入口

- 2026-11-05夜间独立课前检查：正式1.30.72实际哈希/服务、师生查询和浏览器验证，Python跨推进判题终态P1与前端全测未通过边界，见[验收报告](../../contexts/class-readiness-20261105.md)；无业务代码、部署或结构变更。

- Python输入入门五题：[2026-10-20正式数据录入、题包与20/20判题验收](../../contexts/python-input-20261020.md)，全库235题，无部署或业务规则变更。
- 最新机位：[本机验证](../../contexts/seat-helper-local-20261014/verification.md)、[正式验收与回退](../../contexts/seat-helper-local-20261014/release-verification.md)。
- 机位0.2.4与成绩/打字修复已合并正式1.30.52：[正式验收](../../contexts/seat-score-release-20261017.md)、[机位需求/任务](../../contexts/seat-helper-browser-20261016/tasks.md)、[ADR-006](../../contexts/seat-helper-browser-20261016/ADR-006-native-browser-priority.md)。10月16日旧暂存未切换，旧停止指示已由10月17日明确授权替代。
- 登录输入：[验收及练习性能待办](../../contexts/login-input-fix-20261014.md)。
- 页图截底：[页框扩展正式验收](../../contexts/seat-logo-overflow-release-20261014.md)、[剩余问题复核](../../contexts/grading-overflow-reframe-20261014.md)。
- 批改布局：[滚动与五星验收](../../contexts/grading-ui-fix-20261013.md)；连线题：[专项验收](../../contexts/connector-render-fix-20261013/verification.md)。
- [产品发布日志](../../contexts/RELEASE_LOG.md) 记录正式版本；[v4.47核心完整归档](../../contexts/PROJECT_HISTORY_20261015.md)、[更早核心归档](../../contexts/PROJECT_HISTORY_20261005.md)、[旧架构索引](INDEX_HISTORY_20261005.md)、`contexts/context.md` 按日期/专题查证，不作为当前状态。
- PPT二班旧缓存维护：[2026-10-20三道题108份定向重算及回退](../../contexts/ppt-connector-diagnosis-20261020.md)，无应用部署或架构变更。
- 旧课堂体验/助手证据仍在 `contexts/classroom-experience-20261002/`；旧登录展厅证据在 `contexts/login-showrooms-20261005/`，以最新专题状态覆盖旧版本描述。

## 文档维护规则

- `AGENTS.md` 管操作，核心文档管当前状态，本页管任务入口；系统/数据/业务/集成/部署文档各自维护对应边界，不复制详细发布过程。
- 只读取任务相关源码及必要调用链；权限、成绩、提交状态和迁移必须补读业务约束及真实测试证据。源码与现场证据优先，旧日期文档不能覆盖最新验收。
- 核心文档约150行以内，已关闭事项和详细证据移入专题或历史归档，保留可点击链接；新增归档标明日期和适用范围。
- AOCI 已按用户要求卸载；不再有索引加载、维护或同步任务。旧工具记录仅为历史，停用决策见 [ADR-002](ADR/ADR-002-aoci-project-memory.md)。
- 1.30.51新增期限DTO计数、调整触发规则及PPTX临时副本转换，已按AGENTS完成测试、定向SQL和正式重启/验收；需求/设计/任务/ADR已同步。既有其他专题状态按各自证据，不因本轮发布升级为已完成。
- 密码、Token、Cookie只放本机私密文件或环境变量，不写入上述文档、图或交接。

## 2026-11-06 题库预览1.30.74已上线

教师姓名、打字去重、Python完整预览及流程图双版本只读预览，沿用现有接口/答案归属权限；已正式发布，见[设计](../../contexts/question-workbench/design.md)、[正式验收与回退](../../contexts/question-workbench/release-20261106.md)。无后端或数据架构变更。
