# 分阶段开发提示词

> 使用方法：一次新任务只复制一个提示词，不要把 P0～P3 全部交给同一个开发任务。每个任务先读取项目文档、检查脏工作区，再说明修改范围；未得到用户对关键路线的确认时不得扩大范围。

## 提示词 1：P0-A 评分快照与题目 NULL 热修

```text
你接管 D:\dmwprogram\newdazipingtai\newdazi 的 P0-A 热修。使用 project-context，按 AGENTS.md → contexts/PROJECT_CORE.md → docs/architecture/INDEX.md → contexts/multi-feature-upgrade-20260902/{requirements,design,tasks}.md → contexts/operation-artifact-ai-grading/ 的顺序阅读。先执行 git status --short，保护现有未提交改动，不得顺手重构或发布。

本任务只做两件事：
1. 修复 RuoYi-Vue3/src/api/business/scoringItem.js 接收但未发送 practicalVersionId、辅助函数引用未定义变量的问题；批改页必须按提交版本显示评价标准快照，标注作品版本和评分依据版本。用郑琦账号 696260 的课程 293、题目 2029 场景验证：旧提交使用 v1 三项 30/35/35，新提交使用 v2 两项 50/50，不覆盖历史数据。
2. 修复 BizQuestionServiceImpl/BizQuestionMapper.xml 在 PYTHON、FLOWCHART 或非图片模式更新时把 practical_image_max_count 写 NULL 的问题。生产列保持 NOT NULL，非图片模式保留安全默认值 10但业务忽略。

先列出准备修改的文件、原因、风险；随后做最小实现。添加前后端专项测试，按顺序运行定向测试、mvn -pl ruoyi-business -am test、mvn -pl ruoyi-admin -am clean package、Vue3 npm run build:prod。不要连接正式服务器，除非用户另行要求发布。完成后更新对应 requirements/design/tasks/ADR 和 PROJECT_CORE，只写已验证事实，并汇报文件、测试、风险、SQL/重启要求。
```

## 提示词 2：P0-B 题库导入与诊断治理

```text
你接管 2026-09-02 线上诊断与题库导入治理。先读 AGENTS.md、PROJECT_CORE、架构 INDEX、contexts/student-import-diagnosis-governance/ 和 contexts/multi-feature-upgrade-20260902/。先只读核对正式服务器近 24 小时 sys_perf_event、sys_oper_log、NSSM AppDirectory/AppStdout/AppStderr；严禁回显凭据。

已知基线：13 条严重接口异常中 5 条为 /business/question 写 practical_image_max_count=NULL，8 条为学生删除保护/班级范围业务拒绝误报；另有 4 条题库导入缺 question_type，可能部分写入并泄露 SQL；学生导入 10 条约 3.3～3.7 秒的持久化严重记录应按当前动态规则核验；NSSM 运行目录和 stdout/stderr 指向不同 release。

本任务实现：题库导入先全量解析和校验再事务写库，任一行失败整批回滚；返回行号、字段和中文原因但不返回 SQL/JAR/堆栈；预期业务拒绝使用明确类别/错误码并在诊断中降为 info，未知 SQL/系统错误保持 critical；验证历史事件动态重分级。下一次发布方案把 stdout/stderr 统一到稳定日志目录并保留旧日志。

不要用大量中文字符串模糊匹配吞掉异常。先说明设计与修改文件，测试覆盖事务回滚、错误脱敏、业务/系统分类和慢事件三档。未获发布要求时只完成本机代码、测试和文档。
```

## 提示词 3：P1-A 指派班级与流程图体验

```text
只实施两个低风险体验问题，不改数据库：
1. 新建课程默认不勾班级；从具体班级入口进入时只预选该班；编辑课程只回显服务端保存值。重点检查 teacher/index.vue 传全年级 classes 和 lesson/designer.vue 初始化逻辑。
2. FlowchartEditor 开始/结束改为胶囊，处理框与输入输出框去圆角，输入输出保持平行四边形；扩大 LogicFlow 锚点可见/hover/点击热区。旧 JSON 和结构检查必须兼容。

先读 AGENTS.md、PROJECT_CORE、flowchart-tool 专题和总计划；先检查脏工作区，因为流程图目录已有未提交实现。只改任务相关文件。完成后运行流程图专项测试、旧 JSON 回归和 Vue3 build，并在鼠标/触摸板、100%/125% 缩放下做浏览器冒烟。不要发布，除非用户明确要求。
```

## 提示词 4：P1-B 五星批改

```text
实现操作题五星辅助评分。数字输入仍为默认；教师可切换整题评星和逐评分项评星；0分有独立清零按钮。星级分由服务端按 round(maxScore * stars / 5) 使用 BigDecimal HALF_UP 重算并保存整数，7分题必须得到 1/3/4/6/7。前端每颗星显示实际分数。

整题星级只写总分、清除旧分项，不伪造分项；逐项星级必须完整覆盖当前提交绑定快照并沿用上限/合计/并发/期限校验。数字、整题星级、逐项星级记录明确 mode。已有提交后服务端禁止改变课程题目总分；重新提交绑定当时最新快照，旧提交仍用旧快照。

先读 operation-artifact-ai-grading requirements/design/tasks 和 ADR-007。先给出是否新增 biz_student_answer.score_input_mode 的兼容 SQL 方案、优缺点和推荐，等待用户确认后再改表相关代码。测试必须覆盖 0～5星、7分/70分、模式切换、旧分项清理、快照版本、补交、并发保存。保持回车提交和翻页工作流。
```

## 提示词 5：P1-C 共享课程内容只读

```text
实现共享课程只读详情。共享教师定义为：同校，且本人管理班级与课程指派班级至少有一个交集。共享教师可查看课程内已经引用的全部题目，不区分题目公有/私有；不能查看创建教师未加入课程的私人题库；只能查看课程内容和成绩，不能设计、删除、复制或修改。

不要直接放宽现有 assertCanManageLesson，也不要复用可编辑设计器接口。新增服务端 assertCanViewLesson/SharedLessonReadService、白名单 DTO 和专用只读页面；能力由服务端返回。题目参考答案、起始材料、评分依据等字段逐项白名单，按代课查看学生任务所需最小范围开放。

先读 teacher-course-sharing 专题和总计划。测试创建者、共享班级教师、同校无负责关系教师、跨校教师，以及直接调用修改接口。无必要不新增表。完成后做教师浏览器冒烟并更新文档。
```

## 提示词 6：P1-D 统一作业实时状态

```text
建设统一课堂作业状态：NOT_ENTERED、ENTERED、WORKING、SUBMITTED、GRADED、RETURNED。打开题目为 ENTERED，首次保存草稿/答案/临时作品为 WORKING，正式提交为 SUBMITTED。成绩/答案仍是权威事实。

先提出 biz_student_task_state 数据模型和幂等 SQL，包含 lesson/question/student 唯一键和 state_version；历史只回填能确定的 SUBMITTED/GRADED，不猜测进入/作答。状态在业务事务内更新，WebSocket 必须 afterCommit 推送。教师首页、成绩/提交列表、批改页和学生桌面使用同一查询 DTO 和前端状态 store。页面按 stateVersion 丢弃旧消息，断线后重连并全量校准，新提交不得切走当前批改学生。

先读 INTEGRATIONS、operation-artifact-ai-grading REQ-13 和总设计。关键路线涉及 SQL 和 WebSocket，先给方案并等待用户确认。测试事务回滚不发事件、跨班隔离、多标签页、乱序消息、断线校准、补交和退回。
```

## 提示词 7：P2 通用分组与学生桌面

```text
实现通用班级分组和学生终端桌面，不做点名、考勤、远控或计算机名读取。先读 class-grouping-and-desktop 全部文档、ADR-001 和总计划；先检查现有 WebSocket/Redis/班级管理代码。

分组支持多套方案、按组数平均、学号区间和手动拖动；成员不重不漏；非空组一名视觉组长，默认学号最小、教师可改；课程使用课时分组快照。不能复用 IoT 实验专用分组表。

学生桌面主入口加在班级管理“我管理的班级”行内，教师首页增加当前课程/班级快捷入口，不新增一级菜单。默认按学号网格，终端/作业状态视图切换，教师可拖动并按教师+班级保存布局。卡片显示姓名、学号、在线、连接IP、设备数、小组和组长；点击显示设备和轨迹抽屉。

新增独立认证 /ws/presence，学生公共布局登录后连接；30秒心跳、60秒离线；Redis TTL 保存当前在线，不逐心跳写MySQL；同一学生多设备聚合。IP由服务端从可信代理链观察，不能信任浏览器自报。先给出表结构、Redis键、可信代理配置和菜单/隐藏路由方案，等待确认后实现。

验收需覆盖50人班级和400学生多班连接、心跳MySQL零写放大、伪造X-Forwarded-For、NAT同IP、多设备、跨班跨校权限、布局乐观锁。正式机房IP只能在发布前只读核对Nginx并用真实学生机验证，不能在本机伪造结论。
```

## 提示词 8：P3-PoC 文档版本差异

```text
只做在线协作文档版本差异 PoC，不修改正式协作数据、不开发分组房间。用匿名样本验证 DOC/DOCX、XLS/XLSX、PPT/PPTX 相邻版本差异；优先复用项目现有 Apache POI 和 LibreOffice，不引入新依赖，若确需依赖先列2～3方案等用户确认。

目标输出统一 diff JSON：DOCX 段落/表格文字，XLSX sheet/单元格值/公式，PPTX 幻灯片/文字；旧格式转换失败降级；图片/格式/图表只给对象或视觉变化摘要。任务必须异步、有界队列、超时、失败不阻塞保存。

同时核对当前 CryptPad Integration API 和部署补丁能否获得更细作者历史。无确凿证据时必须坚持：只能说“某学生客户端触发保存，小组文档vN到vN+1发生变化”，不能说“该学生修改了这些内容”。输出格式支持矩阵、耗时/内存报告、Go/No-Go结论和后续表结构建议。
```

## 提示词 9：P3 分组多文档协作

```text
只有 P2 通用分组/快照已稳定、P3-PoC 已通过后才执行本任务。把在线协作从计分操作题解耦为独立非计分课堂协作活动：一个活动可有多个任务版本，小组默认顺序映射且教师可调整；多个组可复用模板但必须创建独立副本；学生只进入本人组房间。

建立 activity、variant、group assignment、member route audit、session/event/revision diff 数据模型；biz_collab_room 兼容增加 activity/snapshot group/variant 维度。旧班级房间迁移为全班组历史模式，严禁改写或删除 public_file_id、密钥、文件路径和 revision。首名学生进入后冻结活动；课中换组需教师确认、原子更新权限并审计，旧文档保留、旧访问默认撤销。

在线协作任何流程不得写 biz_student_answer、评分明细、课程总分或推进状态。先给SQL分步迁移、前检、后检、应用回滚兼容方案，等待用户确认后实现。完成后用4～8组、1～8模板、模板复用、换组、断线、保存冲突、历史房间和400学生多房间验收。
```

## 提示词 10：正式发布验收

```text
本任务只负责将已经在本机完整验收的单一阶段发布到 10.52.1.123，不夹带其他未完成功能。先读 AGENTS.md 3.4.2/8/10、PROJECT_CORE、DEPLOYMENT_RUNBOOK、对应专题任务和 RELEASE_LOG。先核对 git diff、构建制品、正式 AppDirectory/AppParameters/AppStdout/AppStderr、Nginx root 和目标库状态。

按顺序：只读前检 → 整库/配置备份到新时间戳目录 → SHA-256 → 执行该阶段幂等SQL → 后检 → 上传新release且不覆盖旧版 → 切换NSSM/Nginx → 重启/探活 → 角色接口和浏览器冒烟 → WebSocket/权限专项 → 登记RELEASE_LOG和biz_platform_update。任何前检不满足立即停止；回复绝不显示密码、Token、Cookie。汇报明确旧release回滚路径和SQL是否需要回滚。
```
