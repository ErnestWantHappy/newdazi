# 交接提示词：课例评价、配套材料包与教材响应式布局

> 历史交接/复核记录：所列缺陷已随1.30.69修复并正式上线；当前状态、真实验证及回退见[本轮发布](release-feedback-fix-20261103.md)。原记录保留供追溯。

请新AI直接按下文续做。这是已有半成品，不能当作已完成。

---

你接手信息科技学业测评平台，项目目录 `D:\dmwprogram\newdazipingtai\newdazi`，使用简体中文。先读 `AGENTS.md` → `contexts/PROJECT_CORE.md` → `docs/architecture/INDEX.md` → 本文件及相关源码，应用 project-context 技能。用户已确认以下推荐方案并要求开始，不再重复询问相同方案。持续完成本机开发、必要迁移、测试、真实浏览器验收和文档收口。

**本轮没有正式发布授权，不连接正式环境写库/发布，不git push。**130正式仍为1.30.68、52课例201资源。上一轮18:00发布授权针对旧功能，不能扩展为本轮授权。

## 一、用户确认的需求

1. **评价整份作者课例**：按“课名＋学校＋作者”独立统计，设计、课件、配套包共用一组👍推荐/👎不推荐。同课不同作者不混票。每教师账号每课例最多一个有效选择，可改票，再点当前选择可撤销，跨设备保留；重复提交/并发不能多计。不展示评价者身份。标题/署名右上角始终展示数量及主动评价入口，不要求先预览/下载。教师及含teacher的双角色可投票，纯管理员/纯教研员/学生不可投票；作者自评没有额外禁令，不要按姓名另加限制。
2. **低频自愿邀请**：预览成功并停留至少30秒后，在关闭预览时具备条件；下载发起后回到平台具备条件，不将发起当下载完成。约25%抽样，每位教师每天最多一次，同课例邀请关闭后14天不再邀请；已评价不再自动邀请。可叉掉、不阻断。提示“你是否推荐这份课例？”。服务器持久化限频，不能只靠localStorage。半成品另设30分钟抽样尝试间隔，避免刷新重抽；属于降打扰细节。计时需考虑后台、切文件/切课、失败、销毁和请求迟到。
3. **每份作者课例一个配套材料ZIP**：合并同作者同课的系统源码、网页、学生实验材料等，内部按用途/原工具分目录，保留子工程相对路径；教学设计、课件、任务单仍单独预览下载。不同作者不能合并。页面**简洁展示包内具体内容**，优先有意义的工具/网页/学生材料名称，不罗列运行库文件。
4. **逐包真实使用条件**：阅读实际HTML、启动脚本、依赖/配置；静态网页才写解压双击Chrome/Edge；需HTTP、Python/Node、设备/MQTT或外部服务的分别说明。页面和包内平台说明同源。只有实际提供共享模型Key才提示共享Key及失效替换办法；空Key/占位/普通配置项/许可证名称不算。保留用户认可的共享Key，不调用验证、不输出值。原始来源不动，新包和清单在发布副本生成并保留回退。
5. **自适应大预览弹窗**：桌面保留教材位置，手机全屏；顶部清楚显示当前文件，下载按钮明确“下载教学设计/教学课件/配套材料包”，与选中文件同步。支持缩小、放大、适应整页/宽度、页码跳转、缩略图定位、上一页/下一页。flex分配剩余高度，避免外层与正文重复套纵向滚动；配套材料宽屏侧栏，窄屏可收起/浮层。**同时检查调整整个电子教材书架与阅读器**的flex、目录、右侧课例、工具栏、页脚，适应不同宽高、矮屏与手机，正文不被挤成窄条。
6. **菜单顺序**：电子教材与优秀课例紧接题库管理之后；成绩查询紧接导学单管理之后；教研活动紧靠学生管理之前。实际菜单来自数据库，先查真实父级/权限/顺序，再写最小幂等SQL并备份原顺序，不只改静态路由，不新增重复一级入口。

已确认的拆包实例：

- 《人工智能物联网》郑东旭：`packages/源代码.zip`为Python/Node系统；`packages/lesson-web/4d838a023f779bca751d0ff3b68d6845.zip`含学生实验脚本、知识挑战HTML、入口快捷方式，应合包。
- 《互联网与人工智能》王纪宇：`packages/w5-internet-ai-lab.zip`是带共享DeepSeek配置的工具；另一个lesson-web包只有数据大屏，不能统一宣称都有共享Key。

## 二、已有半成品与已知问题

先 `git status --short`。工作区本来就有大量未提交/未跟踪课例文件，以及无关的概览/登录慢查询改动。全部保留，不reset、不覆盖。`git diff --stat`不包含未跟踪文件，不能漏检。

本轮新增：

- `sql/textbook_case_feedback_v1.sql`：反馈与邀请限频两表，**任何库都尚未执行**。
- 后端 `ruoyi-business/src/main/java/com/ruoyi/business/mapper/TextbookCaseFeedbackMapper.java`、`service/TextbookCaseFeedbackService.java`。
- 后端 `ruoyi-business/src/test/java/com/ruoyi/business/service/TextbookCaseFeedbackServiceTest.java`。
- 前端 `RuoYi-Vue3/src/views/business/textbook/components/CaseVotes.vue`。
- `scripts/textbook/consolidate_case_packages.py`：默认只审计，`--build`才生成新包与v2清单；**未执行--build**。

已改但未验收：

- `TextbookCaseController.java`：查询附计数/本人选择，PUT `/{caseId}/feedback`、POST `/{caseId}/invitation`；通用ss.hasRole对超级管理员放行，因此另做teacher角色显式检查；预览页新增thumbnail参数支持小JPEG。
- `RuoYi-Vue3/src/api/business/textbookCase.js`：评价、邀请、缩略图请求。
- `CasePanel.vue`：评价、邀请、明确下载对象、配套材料侧栏、flex弹窗草稿。
- `CasePreview.vue`：已重写，缩放/跳页/懒加载缩略图/flex画布草稿。
- `reader.vue`、`index.vue`：最小尺寸、动态视口高度、工具栏换行、书架网格草稿。

必须优先检查：

1. **定向测试当前失败**：原controller8项通过；新service8项中2个NullPointerException，总16项、2错误。日志 `output/textbook-cases/feedback-test.log`，失败项 `randomMissDoesNotMarkAsInvited`、`selectedInvitationRecordsBothLimits`。查 `invitation()` 读取feedback.vote分支，Mockito默认空Map可能与真实无行/null契约不一致；查清真实原因，修实现/测试契约，不删测试掩盖问题。
2. 服务设计通过教师行锁协调改票/邀请；真实事务、MyBatis空结果、日期时区、唯一键并发及权限仍须核对。表未建，**现运行旧后端不含本轮代码**。
3. 包脚本只审计产生 `output/textbook-cases/package-audit-v2.json`及`.log`。**脚本还不可靠，不得直接--build交付**：共享Key正则把神经网络runtime许可证/模块名误报4处；摘要把2777个运行库Python脚本、哈希缓存网页算成教师内容；联网判断仅按http字符串会误报XML命名空间/注释；keySlot也可能误认普通接口配置。fetch反引号、相对API、app.py等识别不完整。必须结合真实源码逐包校准、排除第三方库噪声，并核对原工具条件。
4. 前端**尚未构建或浏览器实测**。预览计时现在只按Date.now差值，未排除后台。需查切课竞态/迟到响应、下载返回邀请、失败预览不邀请、缩略图与正文渲染并发、URL释放、多尺寸溢出。
5. CaseVotes.busy目前只禁当前caseId，其他课例按钮看似可点但父级直接return，需修反馈一致性。缩略图失败/重开/切资源、弹窗关闭后状态也要查。
6. 配套包内容拟复用description字段，不需新增内容字段/依赖；合包后要验证启动脚本、相对路径、说明完整性与下载哈希。
7. **菜单排序尚未开始，未查询真实本机菜单。材料未合包、未导入，数据库未迁移，后端未重启。**

## 三、本机入口与验证

- 本机库 `xueyeceping_server_20260729`，配置由程序读取application-druid.yml，不回显凭据。写库前备份并计算SHA256。`migrate_cases_local.py`有本机目标闸门、mysqldump示例，但旧入口不包含新DDL，不要误跑。
- `load_case_samples_local.py --manifest <路径>`会备份两张课例表并stage导入；**stage会删除各课例清单外旧资源行**，必须保证新清单完整、先备份，不删除旧物理资源。新清单预定 `output/textbook-cases/catalog-feedback-v2.json`，尚未生成；原清单 `catalog-local-reviewed.json`保持。
- 原清单sourceRoot指向 `output/textbook-cases/release-materials/combined`；运行资源为 `output/textbook-pack-20261029/pages/cases`。
- 交接前只读观察80/8080监听，后端PID32340与 `output/textbook-cases/backend.pid`一致；以后需重新核实。仅停止已核对属于本项目的本机进程，`start_case_backend.ps1`可复用，保留主密钥不打印。
- 后端定向：在RuoYi-Vue执行 `mvn -pl ruoyi-business -am test "-Dtest=TextbookCase*Test" "-Dsurefire.failIfNoSpecifiedTests=false" "-DforkCount=0"`。后端打包/重启按项目入口，前端 `npm run build:prod`。
- 先编译/单测，再服务/API，再webapp-testing和已有Playwright方式真实验桌面1500×900、矮屏1366×600、手机390×844。旧 `verify_case_ui_tweaks.py`选择器/放大预期已过时，只供参考。
- 覆盖赞→踩→撤销、并发不多计、刷新/换设备、学生/纯教研员/纯管理员写拒绝、邀请抽样/限频/关闭、失败/重试、合包完整性及关键工程启动路径、菜单真实顺序、多尺寸滚动和下载对象。
- 账号从AGENTS/私密文件取，禁止输出密码/Token/Cookie；不调用共享Key消耗额度。未机房实测、Key有效性未验如实保留。

## 四、收口与交回原AI复查（必须执行）

完成全部需求后同步核心上下文、架构、专题requirements/design/tasks、ADR-017。区分“材料仍离线维护”和“新增教师评价可写”，修正运行期全只读的旧描述。写清本机/正式差异、SQL、资源同步及重启要求。未发布不造正式版本/PUBLISHED更新，不把正式201资源写成已合包数量。

**最终回复除了简短总结，还必须生成一份可直接交回原AI的《Bug复查提示词》文件，并给出绝对路径链接**，建议 `contexts/textbook-cases/review-feedback-ui-20261103.md`。内容必须自包含：

1. 用户最终需求、实际完成范围、精确改动文件、关键接口/SQL/资源清单路径。
2. 实测命令、结果/数量、日志和截图/接口证据、失败/跳过及未验证项，不能只写“测试通过”。
3. 本机迁移/导入/重启状态、备份和哈希、恢复办法，正式环境是否未动。
4. 请原AI独立读源码和证据，重点复查唯一评价/并发/角色、改票撤销、邀请时机/后台计时/抽样/跨设备限频、竞态与错误、Key误报/泄露、材料摘要真实性、合包路径完整性、菜单、flex多尺寸滚动。
5. 要求按P0/P1/P2报告**可复现Bug**：触发步骤、影响、准确文件行号、最小修复建议，区分确认Bug与待验证风险；无证据不编造，不擅自发布或大重构。

原AI会在新AI完成后根据这份提示词再检查。不要仅给一句“请检查一下”，也不要把当前半成品当最终交付。
