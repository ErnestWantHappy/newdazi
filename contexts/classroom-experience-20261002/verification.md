# 课堂体验改进：本机交付与验收记录

> 后续状态：2026-10-03已正式发布130/1.30.24，真实API13组与浏览器9组通过、更新103教师可见。以 [正式验收](release-verification-20261003.md) 为当前状态；下文未发布描述保留为本机阶段历史。

> 本报告记录2026-10-02历史验收。2026-10-03修复后的当前状态、39项Java等新证据及客户端0.1.1-pilot制品以 [修复验收](repair-verification-20261003.md) 为准；下方0.1.0-pilot哈希是旧制品。

日期：2026-10-02。状态：本机候选，未发布到正式服务器。用户确认全部采用推荐并授权自动开发，按 P0→P4 顺序实现。原 Vue3/Spring Boot 技术栈保留，没有新平台运行依赖和无关重构。

## 实际结果

| 优先级 | 已交付能力 | 产品边界 |
| --- | --- | --- |
| P0 | 文件与流程图未批改可反复更新；正式评分后只读；教师已批后退回；新提交成功后清当前分、留旧版本及原分 | `score=NULL` 才是未批改，0 分也锁定。退回期间保留原分；上传/保存失败不清分。旧课推进后禁止学生写入和教师退回，历史可读。Python、理论、打字保持原规则 |
| P1 | 普通/分组大屏已提交及已批姓名绿色、退回红色；打字原文/输入纯黑 | 姓名颜色与在线和理论成绩独立。已有 480px 画布、0.25 缩放、上下锚点与图形缩放复用 |
| P2 | TXT、单个 HTML/HTM、ZIP/RAR/7Z；HTML 内联 CSS/JS 效果与源码查看 | 题目显式允许才可上传。TXT/HTML ≤2MiB，非图片只交一个文件；推荐 `index.html`，不强制改名。压缩包只下载、不自动解压；新格式人工评分，不进入 AI 图像评分 |
| P3 | 学生“我的作答与反馈”、本人正式评分项、个人学情证据；教师整班发布与更新发布 | 复用已有报告，没有额外学生端付费模型。发布不可变快照，重试或重新分析不自动覆盖；新作品提示反馈来自旧版。只返回本人，历史课程可读 |
| P4 | 可选 Windows 机位助手、内网 IP、原浏览器登录关联、学校配置、长期常用机位和异常核实 | 全局及学校默认关闭；管理员逐机配置、教师明确确认基准。不封禁账号、不自动改基准；实际操作者仍由教师核实。Windows 10/极域/还原卡现场待验 |

没有实现压缩包自动网站部署、任意软件在线预览、IP 强制锁定、网页静默保存到 D 盘或自动执行程序。这些能力需要额外环境和维护，不能作为原批改流程的前置条件。现有 Office/PDF/图片仍在原批改页预览；不支持预览的压缩包明确提供下载。

## 验收环境与隔离

- 后端 API `http://localhost:8097`、Vue3 `http://127.0.0.1:8098`、独立 Redis DB 15，上传路径位于 `output/classroom-experience/profile/`。使用新验收库 `xueyeceping_classroom_accept_20261002`，源为本机旧隔离库 `xueyeceping_local_accept_20260923_v4`，没有更改原开发库或正式库。
- 克隆前原库备份 `output/classroom-experience/source-before.sql`，102,270,240 bytes，SHA-256 `62473557535ADA02997F16D6C68092B391C0F39C9B56CC553A94AC336A033F6D`。环境信息见 `database-baseline.json`。
- 所有写数据脚本先断言目标库名；学生、跨校权限账号、课程、作品和模拟报告只用于独立验收库。凭据与登录态仅位于 gitignore 的本机私密文件，没有写入本报告或 Git。
- 原工作区 `scripts/__pycache__/deploy.cpython-310.pyc`、`%SystemDrive%/`、`.workbuddy/memory/2026-09-24.md`、`RuoYi-Vue/uploadPath/datalab/` 保留。未执行 Git 提交/推送、远程登录、服务器 SQL 或发布。
- 测试客户端已逐一撤销、两个临时跨校账号已按 ID/账号/学校/创建者精确清理，学校开关全部恢复关闭，临时班级指派恢复至文件测试课。保留隔离库测试作品、反馈快照和撤销记录用于复核；未清空任何真实业务表。
- 验收结束已关闭本次 8097/8098 后端/前端及后端所属 LibreOffice 子进程，其他服务未操作；证据 `output/classroom-experience/services-cleanup.json`。隔离环境不作为常规登录入口，需复核时使用本机私密验收配置重新启动。

## 验证证据

| 层级 | 结果 | 本机证据 |
| --- | --- | --- |
| Java 业务全量测试 | 共 665，663 通过、2 默认跳过；0 失败、0 错误 | `output/classroom-experience-all-tests-final.log` |
| 前端 Node 测试 | 24 通过，0 失败；覆盖流程图布局/模式、批改刷新/退回和短时关联凭证 | `output/classroom-experience-node-final.log` |
| 后端构建 | `mvn -pl ruoyi-admin -am clean package -DskipTests` 成功；此前已完成全量测试 | `output/classroom-experience-package-final.log` |
| Vue3 构建 | `npm run build:prod` 成功 | `output/classroom-experience-vue-build-final.log` |
| 文件和主要接口 | 10 组通过：实际登录、文本授权、更新/零分/失败留分、压缩包、评分上传并发、本人反馈、EXE、长期机位、撤销 | `output/classroom-experience/api-results.json` |
| 流程图接口 | 4 组通过：草稿冲突/不泄露答案、原样重交留旧零分、提交评分并发、推进后禁止写与历史可读 | `output/classroom-experience/flowchart-api-results.json` |
| 发布反馈 | 3 组通过：发布幂等、报告结果变化显式更新发布、旧作品提示与陈旧报告拒绝 | `output/classroom-experience/feedback-api-results.json` |
| 兼容性/设备接口 | 8 组通过：真实 Word/LibreOffice、PDF、多图预览；RAR/7Z；文本二进制/大小和静态 HTML 拒绝；并发注册、防伪、失效重连与关闭开关 | `output/classroom-experience/additional-api-results.json` |
| 权限接口 | 3 组通过：跨学生/跨班/跨学校资源、报告及设备拒绝 | `output/classroom-experience/permission-api-results.json` |
| 三角色浏览器 | 6 组通过，页面异常 0；本人反馈、纯黑打字、实际 HTML CSS/JS/父页面隔离、两种大屏颜色、教研员核心页面 | `output/classroom-experience/browser-results.json` |
| 助手浏览器 | 3 组通过，页面异常 0；真实 EXE 链接经原登录关联、凭证不进入登录查询参数、实际下载 EXE | `output/classroom-experience/seat-browser-results.json` |
| 流程图页面 | 5 组通过：退回可编辑、平行四边形实际拖宽高及自动保存、两个锚点中线对齐、100%→75% 缩小、确认后实际重交 | `output/classroom-experience/flow-ui-results.json` |

以上合计 42 组接口与页面通过；最终设备/学校开关/指派清理、制品哈希、UTF-8 与新增内容无私密值、Git 空白检查证据见 `output/classroom-experience/final-audit.json`。浏览器截图在 `output/playwright/classroom-experience-20261002/`。四份增量 SQL 在独立验收库重复执行并核对，确认幂等。

两个真实并发验收分别针对文件与流程图，在评分和提交同时发起时最多一个使用原版本成功，另一个被拒绝，未出现旧分写入新作品。AI 采用服务的 Java 用例还验证：退回作品不采用旧建议、写正式分后状态异常抛出并回滚，不能把失败伪装成已跳过而留下半次写入。

个人反馈使用真实报告输入指纹和人工构造的已完成诊断结果，验证数据库快照、权限和学生展示；本轮没有调用外部付费模型。因此这些证据不代表新模型的真实学情分析或评分准确率测试。

## 主要文件清单

以仓库根目录为基准，完整实际改动可用 `git diff` 和新文件清单核对。

| 范围 | 文件 |
| --- | --- |
| 作品更新/批改事务 | `RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/` 下 `controller/{ClassroomTaskState,Flowchart,StudentHome,TeacherGrading}Controller.java`；`service/PracticalSubmissionPolicy.java`、`ClassroomTaskStateWriter.java`、`{PracticalArtifact,Flowchart,ClassroomTaskState,PracticalAiSuggestionApply}Service.java`；相关 Flowchart domain/VO/Mapper/XML |
| 文件接收/预览 | `controller/PracticalAttachmentController.java`、`service/{PracticalFilePolicyService,PracticalTextCodec,PracticalAttachmentConversionService}.java`；Vue3 `components/PracticalTextPreview/`、`utils/practicalHtmlPreview.js`、题库/学生/批改页 |
| 个人反馈 | `controller/{StudentFeedback,PracticalInsight}Controller.java`、`service/{StudentFeedback,PracticalInsightReport}Service.java`、`mapper/{StudentFeedback,PracticalInsight}Mapper.java`；Vue3 `components/StudentFeedbackDialog/`、教师 `PracticalInsightDrawer.vue`、`api/business/paperScan.js` |
| 机位助手 | `controller/SeatHelperController.java`、`domain/SeatHelperDevice.java`、`service/SeatHelperService.java`、`mapper/SeatHelperMapper.java`；Vue3 `views/business/seatHelper/`、`components/SeatHelper/`、`utils/seatHelper.js`、`permission.js`；`clients/seat-helper/` |
| 课堂页面 | `RuoYi-Vue3/src/views/student/index.vue`、`views/business/classroomDesktop/index.vue`、`views/business/teacher/grading.vue`、`components/FlowchartEditor/StudentFlowchartDialog.vue` |
| 验证 | 新增/更新业务策略、格式、个人快照、机位冲突、流程图权限及 AI 应用 Java 用例；Vue3 关联凭证和批改状态用例 |
| 升级/文档 | 下述四份 SQL；本专题 requirements/design/tasks/ADR-001～003；`PROJECT_CORE.md`、架构入口与边界文档；客户端 README |

## 目标环境升级顺序（本轮未执行）

1. 核对目标库、正式应用基线与既有迁移；先完整备份并计算 SHA-256，不把本机已迁移视为正式已迁移。
2. 在停写或维护窗口按顺序执行以下脚本：
   - `sql/classroom_experience_flowchart_history_v1.sql`：流程图提交增加旧成绩及分项 JSON。
   - `sql/classroom_experience_attachment_kinds_v1.sql`：作品种类 CHECK 接受 TEXT/HTML/ARCHIVE，不扩展题目默认格式。
   - `sql/classroom_experience_feedback_v1.sql`：两张反馈发布/个人快照表及内容指纹唯一键。
   - `sql/classroom_experience_seat_helper_v1.sql`：学校/设备/长期绑定三表，管理员菜单与权限。
3. 后检新列、新表、唯一键、CHECK、重复菜单/角色菜单。按完整 SQL 升级后再配套更新后端和 Vue3、重启后端。即使暂不启用助手，新接口查询依赖其表，不能省略建表脚本。
4. 首轮保持助手默认关闭，先用实际师生账号验收文件/流程图/历史反馈与旧预览；原 AI 配置主密钥不变，不把验收库配置或私密数据复制到目标环境。
5. 仅试点学校准备助手：显式设置 `seat-helper.enabled=true`、`seat-helper.client-path` 为服务器实际 EXE 绝对路径，再重启；管理员按学校开启、配置 HTTPS 网页根地址、逐机发放注册码。客户端接口地址使用实际 API 前缀（如 `/prod-api`）。非 loopback 不接受 HTTP；当前正式内网 HTTP 入口需先准备可信 HTTPS，不能直接启用为正式助手环境。
6. 如需正式发布，按仓库发布工具与目标环境纪律执行，保留旧 release 并验证教师更新记录。本轮未发布，因此没有登记新的正式产品版本。

## 客户端制品与使用

- 程序：[SeatHelper.exe](../../clients/seat-helper/dist/SeatHelper.exe)，版本 `0.1.0-pilot`，20,480 bytes，.NET Framework 4，未签名。SHA-256 `B2A6EDB7B4B896A6FCDDFC2E1151F401BB1999F219171211B4987C88A88CD5C0`；对应 `dist/manifest.json` 和 `.sha256`。
- 安装、学校开关、逐机注册、还原基线、自启与卸载见 [客户端 README](../../clients/seat-helper/README.md)。不把注册好的配置复制到其他电脑；注册码、设备凭据和 DPAPI 文件不进入公开文档。
- 管理员下载/分发 EXE，按真实学生 Windows 用户完成一次配置。程序提供原浏览器入口，登录后自动关联机位；网页下载位置由浏览器决定。
- 教师在当前课堂选择机房，核实后整班确认常用机位；换座异常、未识别、同机多账号只能作为核实依据。缺席或异常不自动覆盖长期基准，调座可单独确认。

## 回退与剩余限制

- 应用回退：切回原前后端制品并重启，保留本次新增列、表、原件、提交版本和个人反馈；不要恢复整库备份覆盖期间新增作品/成绩，也不要删新表“清理”。旧应用不读取新增功能即可。回退后旧应用不能接收新格式，已上传的新格式需保留并另行受权导出/核对；回退前应暂停新格式提交并说明兼容边界。
- 助手止用：关闭学校或全局开关，不影响原浏览器登录答题；撤销设备即使本地程序仍在也不能继续关联。卸载时删除实际用户自启项、快捷方式和已确认的安装目录，教师长期基准可保留。
- Windows 10 真实机、极域批量分发、还原卡重启、Windows 用户自启及 DPAPI 密钥保留、系统镜像克隆均未现场验证；仅 D 盘不还原不能保证这些状态保留。先选少量机位试点再扩班。
- 客户端未签名；系统提示与学校分发策略待现场处理。不是远程控制、防作弊认证或锁座工具，不能识别实际操作者或阻止分享短时链接。
- HTML 沙箱只支持单文件内联资源和受限脚本，不提供外部网络、服务器接口、多文件相对路径、压缩包解压站点；没有脚本 CPU 配额，异常循环可能影响预览响应，需要教师关闭/切源码。
- 未开展大班级助手容量压测、所有浏览器兼容性测试和新增付费 AI 准确率评估。本机证据不能替代正式环境/真实机房验收。
