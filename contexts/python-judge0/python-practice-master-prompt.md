# Python 刷题功能总架构与实施提示词

> 状态：**历史实施提示词，已过时**。Python 刷题已完成开发、正式迁移与发布；当前准确状态见 `contexts/PROJECT_CORE.md` 和 `contexts/context.md` 的 2026-08-19 至 2026-08-20 发布记录。
> 用途：仅用于追溯最初产品约束，不得据此判断当前是否已开发、迁移或发布。新的工作从仓库根目录 `AGENTS.md`、`contexts/PROJECT_CORE.md`、`docs/architecture/INDEX.md` 和本专题最新任务文档开始。

## 1. 任务目标

在现有 RuoYi-Vue3 + Spring Boot/RuoYi + MySQL + Judge0 CE 平台中，新增独立的“Python 刷题”功能。目标用户是小学高年级和初中 Python 初学者；产品定位是日常分层练习工具，不是竞赛 OJ。

必须复用现有 Python 题库、题目测试点、CodeMirror 6 编辑器、Judge0 客户端、异步轮询、取消、限流和脱敏能力。不得复制第二套判题引擎。

## 2. 已确认的产品规则

### 2.1 与课程完全隔离

- 刷题不是课程，不进入课程设计器。
- 刷题提交不写入 `biz_student_answer`，不改变课程总分、课程推进、课程成绩或现有课程 Python 判题行为。
- 刷题可以复用已有 Python 题目，但刷题草稿、运行记录、提交记录、学习进度和统计必须独立保存。

### 2.2 教师入口与直观路径

- 教师端只有一个一级菜单：`Python 刷题`。
- 首屏沿用教师首页：`年级分组 -> 年级 Python 练习卡 -> 配置题目 / 查看班级情况`。
- 题库不是首屏菜单；只有配置题目时才打开。
- 学情不是报表大屏；先选班级，再看学生，薄弱题目作为二级查看。

### 2.3 题目分层规则

- 一个学校、一个学期、一个入学届别，只有一套当前发布的**年级基础题单**。
- 基础题对该年级全部可见班级开放，是共同教学基线和班级横向比较的基准。
- 教师可创建一个或多个**班级加练包**。一个加练包可一次选一个或多个班级；一个班也可收到多个加练包。
- 加练包只能追加题目，不能删除、替换或隐藏基础题。
- 同一学生实际可见范围内，同一题目只能出现一次。保存或发布加练包时，服务端必须阻止与基础题、同班其他已发布加练包重复的题目。
- 学生端先展示“基础题”，再展示“班级加练”；班级统计必须分开计算基础题与加练题，禁止把题量不同的班级混成一个完成率。

### 2.4 第一阶段刻意不做

- 不做排行榜、竞赛房间、打卡、积分商城、复杂闯关、截止日期、限时考试、公开题库社区或社交功能。
- 不做多语言、交互式输入、教师给单个学生单独布置题、班级完整独立题单。
- 不为了刷题改造既有课程、成绩、题库主表或 Judge0 部署。

## 3. 教师端页面与交互

### 3.1 年级练习首页

每个年级一张练习卡，最多两个主操作：`配置题目`、`查看班级情况`。卡片展示发布状态、基础题数、覆盖班级、本周练习人数；已有加练时以简洁标记显示“若干班加练”，不展开统计报表。

首次使用时只给出一个主路径：`开始设置 -> 推荐 Python 零基础入门题单 -> 发布给全年级`。推荐题单默认 20 题，80 道系统题是题库总量，不应一次全部推给新手。

### 3.2 基础题配置

年级和学期由卡片自动带入。教师可：

1. 使用系统推荐题单；
2. 从 Python 题库增删题、调整顺序；
3. 保存草稿、发布、撤回；
4. 在基础题发布后进入“为部分班级加练”。

发布页必须明确告知“基础题将对全年级全部班级生效”。已发布版本不能原地修改；修改时创建新的草稿版本，发布后保留旧版本、学生历史和统计口径。

### 3.3 班级加练

加练页按以下顺序：填写名称 -> 勾选一个或多个接收班级 -> 选择题目 -> 保存草稿或发布。

页面固定提示：`班级加练只增加题目，不会删除或替换全年级基础题。`

若教师尝试选择重复题，后端返回可读提示并拒绝保存，例如“该题已在八年级基础题中，无需重复加入”。撤回加练只影响后续学生可见性，已提交代码、尝试记录和学习统计必须保留。

### 3.4 班级学情

先复用现有课程的班级选择习惯。班级页默认展示学生列表：基础题已通过数、基础题完成率、加练完成率、正确率和最近练习时间。点击学生可见其本人题目状态与提交概要。

“薄弱题目”独立切换，分别展示基础题薄弱项和加练薄弱项。学生代码只在教师已有该班级权限时可查看；隐藏测试输入、标准输出、Judge0 内部异常和 Token 永不返回前端。

## 4. 学生端页面与交互

- 学生顶部导航新增 `Python 练习`，进入独立页面，不塞入课程答题页。
- 页面显示基础题与本班加练两个分区，支持全部、未完成、已完成、错题筛选。
- 一次只显示一道题，复用现有编辑器、草稿保存、公开样例运行、正式提交、结果和历史记录。
- 每题通过按 100 分记录，仅用于刷题统计；学生可多次提交，保留最高分、是否通过、提交次数、首次通过和最近练习时间。
- 学生只可访问本人学校、当前届别、当前班级命中的已发布基础题和加练包；接口必须由服务端根据登录身份计算范围，前端参数不可决定年级或班级。

## 5. 推荐持久化模型

> 表名是实施建议。编码前先核对仓库已有实体、主键策略、审计字段和 MySQL 版本，再在 `sql/` 编写幂等迁移。

### 5.1 配置与版本

| 表 | 职责 | 关键字段与约束 |
| --- | --- | --- |
| `biz_python_practice_plan` | 年级基础题单逻辑主体 | `plan_id, dept_id, semester, entry_year, plan_name, current_version_no, status, creator_id`；同一学校/学期/届别只允许一个逻辑主体 |
| `biz_python_practice_plan_version` | 基础题单不可变版本 | `plan_version_id, plan_id, version_no, status(DRAFT/PUBLISHED/RETRACTED), published_time`；同一计划最多一个 `PUBLISHED` 版本 |
| `biz_python_practice_plan_question` | 某基础题版本中的题目 | `plan_version_id, question_id, sort_no, stage, required_flag, question_snapshot_version`；版本内题目唯一 |
| `biz_python_practice_extension` | 班级加练包 | `extension_id, plan_id, extension_name, status, creator_id, published_time`；一个基础计划可有多个包 |
| `biz_python_practice_extension_class` | 加练包目标班级 | `extension_id, dept_id, entry_year, class_code`；唯一 `(extension_id, class_code)` |
| `biz_python_practice_extension_question` | 加练包追加题目 | `extension_id, question_id, sort_no, question_snapshot_version`；包内题目唯一 |

基础题和加练题均保存“题目版本或题面快照标识”。共享题库题目后来被编辑时，已发布练习的题面、测试点和判题规则不能静默改变。第一期可先使用不可变的 Python 题目版本字段；若现有题库没有版本能力，则在发布时复制必要的题面与测试点快照，并在设计评审中明确存储位置。

### 5.2 学生练习数据

| 表 | 职责 | 关键字段与约束 |
| --- | --- | --- |
| `biz_python_practice_draft` | 学生当前草稿 | `student_id, source_type(BASE/EXTENSION), source_id, question_id, code, updated_time`；同一可见题唯一 |
| `biz_python_practice_submission` | 运行与正式提交的不可变历史 | `submission_id, student_id, source_type, source_id, question_id, submit_type(RUN/SUBMIT), code, status, passed_count, total_count, score, judge_summary, idempotency_key` |
| `biz_python_practice_progress` | 学生单题汇总 | `student_id, source_type, source_id, question_id, best_score, passed_flag, submit_count, first_pass_time, last_practice_time`；唯一学生 + 题目来源 |

`source_type + source_id` 分别指向基础题版本或加练包。提交历史写入题目来源和版本，保证后来发布新基础题版本、撤回加练包后，历史仍可追溯。基础题和加练题在有效范围内禁止重复，避免同一学生为同一题产生两个进度。

### 5.3 索引与数据保留

- 所有范围表建立 `dept_id + semester + entry_year + status` 或对应查询索引。
- 提交与进度至少建立 `student_id + last_practice_time`、`source_type + source_id + question_id`、`question_id + status` 索引，支撑学生列表和薄弱题统计。
- 撤回只改状态，不能物理删除已发布题单、加练包、草稿、提交或进度。删除未发布草稿可软删除。

## 6. 服务端边界与接口清单

所有接口统一在 `/business/python-practice` 下。以下仅是契约方向，最终 DTO 需符合本项目返回结构与权限注解规范。

### 教师端

- `GET /teacher/workbench`：按教师可管理范围返回年级练习卡。
- `GET /teacher/plans/{planId}`：读取基础题单、当前草稿或发布版本。
- `POST /teacher/plans`、`PUT /teacher/plans/{planId}/draft`：创建或保存基础题草稿。
- `POST /teacher/plans/{planId}/publish`、`POST /teacher/plans/{planId}/retract`：发布或撤回。
- `GET /teacher/extensions`、`POST /teacher/extensions`、`PUT /teacher/extensions/{extensionId}`：查询、创建、保存班级加练。
- `POST /teacher/extensions/{extensionId}/publish`、`POST /teacher/extensions/{extensionId}/retract`：发布或撤回加练包。
- `GET /teacher/analytics/classes`、`GET /teacher/analytics/classes/{classCode}/students`、`GET /teacher/analytics/classes/{classCode}/weak-questions`：班级、学生和薄弱题统计；参数必须含基础题或加练范围。
- `GET /teacher/students/{studentId}/records`：查看受权限保护的学生练习概要。

### 学生端

- `GET /student/overview`：由登录学生身份计算基础题、命中加练包和总体进度。
- `GET /student/questions/{sourceType}/{sourceId}/{questionId}`：返回题面、公开样例、草稿、本人历史摘要；不返回隐藏数据。
- `PUT /student/drafts/...`：防抖保存草稿。
- `POST /student/submissions/run`：仅公开样例运行。
- `POST /student/submissions`：正式隐藏测试提交。
- `GET /student/submissions/{submissionId}`、`POST /student/submissions/{submissionId}/cancel`：复用既有异步轮询和取消语义。

### 必须的服务端校验

1. 题单与加练包归属学校、学期、届别、班级的校验；
2. 教师只能操作自己有 `biz_teacher_class` 权限的班级；
3. 学生必须属于目标学校、届别和班级，且题单或加练包处于 `PUBLISHED`；
4. 题目必须是已启用、配置完整的 Python 题，包含公开与隐藏测试点；
5. 发布时校验非空题目、排序、重复题和加练范围；
6. 提交使用幂等键、限流和既有 Judge0 安全策略；
7. 任何 DTO 不得返回 Judge0 地址、Token、隐藏测试输入/输出、隐藏程序输出或内部异常详情。

## 7. 权限矩阵

| 角色 | 基础题单 | 班级加练 | 学情 | 学生代码 |
| --- | --- | --- | --- | --- |
| 管理员 | 全校管理 | 全校管理 | 全校查看 | 按既有管理权限查看 |
| 教研员 | 全校或授权范围管理 | 全校或授权范围管理 | 授权范围查看 | 按授权范围查看 |
| 教师 | 仅自己有管理范围的年级 | 仅可管理班级 | 仅可管理班级 | 仅可管理班级学生 |
| 学生 | 仅阅读本人可见题目 | 无 | 仅本人进度 | 仅本人代码与历史 |

角色名称、菜单权限标识和后端注解必须复用项目现有权限模型，不能仅靠前端隐藏按钮。

## 8. 80 道系统题规划

第一批系统题统一进入已有 Python 题库，并标注适用阶段、难度、知识点、题目版本和来源模板：

- 入门启蒙 20 题：输出、变量、输入、数值计算；
- 基础语法 30 题：条件、循环、字符串、列表、简单统计；
- 进阶应用 20 题：函数、字典、嵌套结构、简单模拟；
- 综合挑战 10 题：排序、计数、综合模拟、基础算法。

每题必须使用适龄中文题面，配置起始代码、输入/输出说明、公开样例、至少一个隐藏测试点、约束、参考解法与 Judge0 真实验证记录。题库导入必须先在本机验证，再通过幂等 SQL 导入目标库；禁止直接批量写正式库。

推荐先提供模板：`零基础入门 20 题`、`条件与循环 20 题`、`字符串与列表 20 题`、`综合提升 20 题`。模板只帮助教师创建基础题草稿，不自动向学生发布。

## 9. 实施顺序与验收门槛

1. 现状核对：动态菜单、角色、题库、Python 表、教师班级范围、学生身份字段、Judge0 服务和现有单测。
2. 数据评审：SQL 表、索引、题目快照策略、版本发布策略、接口 DTO、权限矩阵和回滚方案，先得到人工确认。
3. 教师端：菜单、年级工作台、基础题草稿/发布、加练包、班级/学生/薄弱题统计。
4. 学生端：顶部入口、题目分区、单题编辑、草稿、运行、提交、历史。
5. 题库：导入并逐题验证 80 道系统题。
6. 体验与回归：统一 CodeMirror 单层聚焦边框，运行后端单测、前端构建、接口权限验收、教师/学生浏览器冒烟和 Judge0 真实提交验收。
7. 发布：先备份目标库、执行幂等 SQL、构建新 release、探活、真实角色验证，并记录回滚路径。

## 10. 后续可扩展但当前不实现的方向

当前模型可在不破坏基础题/加练规则的情况下扩展：分阶段解锁、截止时间、教师给小组加练、题单模板市场、学习徽章、校级竞赛、跨年级题库统计、更多语言。新增能力必须先评估是否会改变“课程隔离、基础题统一、加练只追加、历史不可丢失”四条核心规则。

## 11. 给后续 AI 的执行限制

- 先输出本项目现状核对、最终 SQL、接口清单、权限矩阵、页面线框、迁移风险和分阶段任务，获得确认后才改代码。
- 只维护 `RuoYi-Vue3` 前端，不改旧 `RuoYi-Vue/ruoyi-ui`。
- 所有新增 SQL 放 `sql/`，必须幂等；改动业务规则、表结构、接口或权限后同步更新 `contexts/context.md`、本专题需求/设计/任务和必要 ADR。
- 不提交、不推送、不发布，除非用户明确要求；不泄露账号、密码、Token、Judge0 地址或隐藏测试数据。
