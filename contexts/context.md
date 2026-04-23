# 信息科技学业测评平台 (Context)

> **版本**：v2.8
> **更新时间**：2026-04-22
> **核心定位**：服务于中小学信息科技课程的综合性教学与评价平台，集课程管理、多维度测评（选择/判断/操作/打字）、智能评分、学情分析与可视化于一体。

---

## 🏗️ 1. 项目架构 (Technology Stack)

基于 **RuoYi-Vue** 前后端分离架构进行深度定制开发。

- **后端**：Spring Boot, MyBatis-Plus, Spring Security, JWT
- **前端**：Vue 3, Element Plus, Vite, Pinia, **ECharts 5.6** (可视化图表)
- **数据库**：MySQL 8.0
- **文件预览**：**LibreOffice** (headless 模式，将 docx 转换为 PDF 预览)
- **核心模块**：`ruoyi-business` (业务逻辑), `RuoYi-Vue3` (前端交互)

---

## 🧩 2. 系统核心功能与业务流程 (System Core & Workflows)

### 2.x 2026-04-22 更新摘要 (工作快照 - 操作题预览重试 / 答题记录唯一化 / 学年画像)

> [!IMPORTANT]
> **状态口径**：以下内容表示**仓库代码已经修改**，但**尚未确认上线、尚未确认数据库脚本已执行、尚未确认定时任务已创建、尚未完成构建与联调回归**。后续继续开发或排障时，请先按本节的“待验证事项”补齐环境确认。

#### 🔁 操作题预览重试链路（已改代码，待验证）
- **学生端预览状态流**：`StudentHomeController` 返回的已提交答案新增 `previewStatus`、`previewPath`；学生页上传操作题后会根据服务端状态显示“可预览 / 转换中 / 转换失败”，并补充失败下载兜底。
- **状态字段扩展**：`biz_student_answer` 已按代码引入 `preview_retry_count`、`preview_last_retry_time`、`preview_error_message` 三个预览失败重试字段；实体 `BizStudentAnswer` 与 `PracticalSubmissionVo` 已同步承载这些字段。
- **异步转换统一入口**：`AsyncConversionService` 不再依赖提交时临时拼路径，而是统一按答题记录读取源文件；支持 PDF 直出成功、DOC/DOCX 转换、失败原因落库、自动重试次数控制。
- **教师端人工重转**：教师批改页新增“重新转换本班失败文件”按钮；后端新增 `/business/teacher/grading/retry-failed-previews` 接口，由 `PracticalPreviewRetryService` 触发当前课程、当前班级、当前操作题下的失败文件重转。
- **定时自动重试**：`ruoyi-quartz` 已新增 `PracticalPreviewRetryTask`，调用目标为 `practicalPreviewRetryTask.retryFailedStudentAnswerPreviews`，用于按时间窗口重试失败的 DOC/DOCX 预览转换。
- **配套 SQL**：仓库已新增 `sql/practical_preview_retry_fields.sql`，用于补充字段、回填默认值、创建辅助索引，并给出 Quartz 任务建议配置。

#### 🧹 答题记录去重与唯一化（已改代码，待验证）
- **查询语义变更**：`BizStudentAnswerMapper.xml` 新增 `latestAnswerIdSubquery`，多处查询已统一改为按 `student_id + lesson_id + question_id` 仅取最新一条答题记录，避免旧提交混入统计、画像和批改列表。
- **提交流程变更**：学生提交答案从原先“先删旧记录，再批量插入”改为“按题逐条查询最新记录，存在则更新，不存在则新增”，降低重复记录风险，并让操作题预览状态可以持续更新。
- **预留数据库收口方案**：仓库已新增 `sql/typing_answer_dedup_fix.sql`，内容包括历史数据备份、重复记录清理、以及 `uk_student_lesson_question` 唯一索引补充脚本。
- **直接影响范围**：课程答题详情、成绩统计、操作题批改列表、学生画像等依赖 `biz_student_answer` 的查询，后续应默认按“最新答题记录视图”理解，不再按“历史全量答题流水”理解。

#### 👤 学生画像改为学年维度（已改代码，待验证）
- **接口参数统一调整**：学生画像相关接口已从 `semesterStart / semesterEnd` 改为 `academicYearStart / academicYearEnd`，前后端参数名保持一致。
- **前端筛选器升级**：`StudentSelector.vue` 已由“学期选择”改为“学年选择”，默认学年按 9 月开学口径计算。
- **画像查询口径同步**：`StudentProfileController`、`IStudentProfileService`、`StudentProfileServiceImpl`、`StudentProfileMapper.xml` 已同步按学年时间范围查询课程成绩、打字速度、课堂表现与排名变化。
- **画像数据去重同步受益**：由于画像底层查询也切换为读取最新答题记录，理论上可以避免重复答题造成的成绩与排名失真，但尚未完成联调验证。

#### ✍️ 题库与课程设计器体验优化（已改代码，待验证）
- **打字题时长改为可手动覆盖推荐值**：题库页面不再只读展示推荐时长，教师可在推荐值基础上手动调整；仅在未手工覆盖时，才随字数和年级自动更新。
- **操作题模板文件改为可选**：题库中操作题的参考模板文件不再强制上传；如上传，仅允许 1 个 `docx` 文件，避免多文件带来预览和管理歧义。
- **课程可先创建后指派班级**：课程设计器移除了“至少指派一个班级”的前端硬校验，允许教师先完成课程设计、后续正式上课前再补充班级指派。

#### 🧩 其他配套修补（已改代码，待验证）
- **教师批改体验补强**：批改页会保留当前选中的学生，并在学生列表中直接展示预览状态；失败记录支持显示失败原因与已重试次数。
- **学生页体验补强**：打字题提交过程新增 `submitting` 防重入状态；操作题上传限制为单文件，并在页面销毁时清理轮询定时器。
- **排序与导入细节修补**：课程与班级查询的排序做了进一步规范；`biz_student` 插入语句已补上 `remark` 字段；系统重置密码时会清理登录错误缓存，减少“已重置但仍显示锁定”的假象。

#### ⚠️ 当前待验证事项（截至 2026-04-22）
- **数据库脚本未确认执行**：没有证据表明 `sql/practical_preview_retry_fields.sql` 与 `sql/typing_answer_dedup_fix.sql` 已在线下或生产数据库中执行。
- **Quartz 任务未确认创建**：代码中已有 `PracticalPreviewRetryTask`，但没有证据表明系统中已经真正创建 `practicalPreviewRetryTask.retryFailedStudentAnswerPreviews` 定时任务。
- **构建与联调未确认完成**：没有证据表明本轮改动已完成后端构建、前端构建、接口联调或回归测试。
- **运行产物不纳入核心记忆**：`RuoYi-Vue/uploadPath/upload/2026/04/` 属于本地运行产物，默认不写入业务上下文，只在排查具体附件问题时再单独引用。

### 2.0 2026-03-12 更新摘要 (v2.9 - 请假/缺考管理与视觉优化)

#### 📝 请假/缺考 (Absent/Leave) 深度集成
- **核心逻辑**：在 `biz_classroom_performance` 表新增 `is_absent` 字段。标记请假后，学生该课总分记为 `NULL`（而非 0）。
- **均分计算修正**：`score/index.vue` 逻辑重构，平均分分母使用 `validScoreCount`（自动排除所有请假记录），确保均分真实反映在校生水平。
- **交互入口**：
  - **汇总表快捷操作**：只选中单门课程时，表格新增“请假”图标列（小日历），支持一键标记/恢复。
  - **状态说明**：标记请假后的成绩 Tag 显示为灰色“请假”字样。
- **后端支持**：`ScoreQueryController` 新增 `setAbsent` 接口，同步更新 `BizStudentAnswerMapper` 关联查询逻辑。

#### 🎨 视觉体验与图表清晰度优化
- **字体规范统一**：全站图表禁用 `bold` 加粗（解决小字模糊），全局应用字体系列：`"Microsoft YaHei", "PingFang SC", "Helvetica Neue", Arial, sans-serif`。
- **异常状态高亮**：
  - **RankChart 姓名变色**：有备注的学生姓名在 X 轴自动显示为**橙色** (`#E6A23C`)。
  - **零分灰色占位**：分数为 0 且有备注（跳级/休学/请假）的学生，柱状图柱体强制变灰 (`#C0C4CC`)。
- **性能/响应式提升**：修正 `RankChart` 全屏状态下的像素级布局计算，支持旋转 45 度的长姓名标签不被截断。

#### ⚡ 效率提升（自动保存）
- **课堂表现分**：实现“失去焦点即保存”逻辑，移除手动保存按钮，提升实时打分体验。
- **字段宽度调整**：微调列表布局，增加特定字段的可视范围级。

### 2.0 2026-03-12 更新摘要 (v2.8 - 跨校数据隔离修复)

#### 🔐 多学校数据串台 Bug 修复

- **问题现象**：殷夫中学老师能看到大目湾实验学校创建的课程；大目湾的课堂表现分列表混入了殷夫中学的学生（多个"张三"）；理论测试详情页出现跨校学生重复。
- **根因分析**：系统最初为单学校设计，核心业务表（`biz_lesson`, `biz_lesson_assignment`, `biz_classroom_performance`）缺少 `dept_id`（学校ID）字段。当多所学校的班级编号相同（如都有"2024级8班"）时，SQL 查询仅按 `entry_year + class_code` 关联，导致跨校数据混合。更严重的是，`deleteOtherAssignmentsByClass` 方法会**跨校删除课程指派记录**。
- **修复方案**：

| 修复层面 | 具体内容 |
|:---|:---|
| **数据库** | 为 `biz_lesson`、`biz_lesson_assignment`、`biz_classroom_performance` 三张表追加 `dept_id` 字段，并通过 SQL 回填历史数据 |
| **后端代码** | 12 个文件中所有涉及跨表关联 `entry_year + class_code` 的查询，全部追加 `dept_id` 过滤条件 |

- **修改的后端文件清单**：

| 文件 | 修改内容 |
|:---|:---|
| `BizLessonMapper.xml` | `selectLessonsByGradeAndCreator` 加 `AND dept_id = #{deptId}`；共享课程查询 JOIN 加 `AND la.dept_id = tc.dept_id` |
| `BizLessonMapper.java` | `selectLessonsByGradeAndCreator` 方法签名加 `deptId` 参数 |
| `BizLessonServiceImpl.java` | 教师首页调用传入 `deptId`；`saveLessonDetails` 中 `deleteOtherAssignmentsByClass` 传入 `deptId` |
| `BizLessonAssignmentMapper.xml` | `deleteOtherAssignmentsByClass`、`selectCurrentLessonByClass` 加 `dept_id` 过滤 |
| `BizLessonAssignmentMapper.java` | 对应方法签名加 `deptId` 参数 |
| `BizClassroomPerformanceMapper.xml` | `selectListByLessonAndClass` 加 `AND u.dept_id = #{deptId}` |
| `BizClassroomPerformanceMapper.java` | 对应方法签名加 `deptId` 参数 |
| `ClassroomPerformanceController.java` | `list` 方法传入 `deptId` |
| `ScoreQueryController.java` | 课程下拉、答题分析、答题矩阵均传入 `deptId` |
| `StudentHomeController.java` | `getCurrentLesson` 传入 `deptId` |
| `BizStudentAnswerMapper.xml` | `selectByLessonAndClass`、`selectStudentAnswerMatrix`、`selectPracticalSubmissions` 加 `u.dept_id` 过滤 |
| `BizStudentAnswerMapper.java` | 对应方法签名加 `deptId` 参数 |
| `TeacherGradingController.java` | `getPracticalSubmissions` 传入 `deptId` |
| `FileConversionUtils.java` | `stopOfficeManager` 优化：关闭前先 `taskkill` 残留进程，消除重启超时 |

> [!CAUTION]
> **关键注意**：`biz_student` 表本身**没有** `dept_id` 字段！学生的学校归属必须通过 `LEFT JOIN sys_user u ON s.user_id = u.user_id` 后使用 `u.dept_id` 获取，直接写 `s.dept_id` 会报 `Unknown column` 错误。

### 2.0 2026-01-21/22 更新摘要 (学生管理功能增强)

#### 🔧 学生导入验证逻辑修复

- **问题背景**：导入学生时，如果 Excel 中缺少入学年份、班级等必填字段，系统仍会创建记录，导致成绩查询页面报错 `TypeError: Cannot read properties of null (reading 'entry_year')`
- **修复内容** (`BizStudentServiceImpl.importStudent`)：
  - 添加必填字段校验：`studentName`, `entryYear`, `classCode`, `studentNo`
  - 校验失败的记录不会创建用户和学生记录
  - **导入结果消息优化**：失败记录**置顶显示**（红色），成功记录在下方（绿色）
- **前端防御性过滤** (`score/index.vue`)：
  - `loadClasses` 函数过滤掉 `entry_year` 或 `class_code` 为空的无效记录
  - 防止数据库孤儿记录导致页面崩溃

#### 🔐 学生账号锁定状态管理

- **业务场景**：学生连续5次输错密码后账号被锁定（Redis 缓存 `pwd_err_cnt:username`），教师需要能够查看和解锁
- **后端实现**：
  - `BizStudentServiceImpl.resetStudentPwd`：重置密码后**自动清除锁定缓存**，实现解锁
  - `BizStudentController.getLockStatus`：新增接口批量查询学生锁定状态
- **前端实现** (`student/index.vue`)：
  - 表格新增「状态」列（操作列左边），锁定显示红色文字
  - 搜索栏新增「账号状态」下拉筛选器（全部/正常/锁定）
  - 锁定学生的重置按钮显示「重置密码并解锁」
  - 重置成功后自动刷新锁定状态，无需手动刷新页面
- **表格列顺序调整**：登录账号 → 学生姓名 → 班级 → 学号 → 入学年份 → 状态 → 操作

### 2.0.1 2026-01-20 更新摘要 (题库管理与成绩查询优化)

#### 📤 题库导出功能

- **导出按钮**：题库管理页面新增"导出"按钮，调用后端 `/business/question/export` 接口
- **操作题限制**：当筛选条件选择"操作题"时，点击导出会提示"操作题包含附件文件，无法导出到Excel"
- **预览按钮**：操作题新增"预览"按钮，点击在新标签页打开 `previewPath`（PDF预览）

#### 📊 成绩查询页面增强

- **账号列新增**：成绩汇总表最左侧新增"账号"列（`userName`），因账号具有唯一性且不可修改
- **Excel导出同步**：后端 `ScoreQueryController.exportScoreExcel` 表头和数据行均新增账号列
- **班级显示修复**：修复班级显示重复拼接年级的Bug（如 `6604` → `604`），逻辑改为：仅对1-2位数班号拼接年级
- **前端导出配置**：`exportColumnOptions` 新增 `userName` 必选列

#### 🔧 操作题批改页面修复

- **统计数据修正**：
  - **已交人数**：修改为统计 `submitted === true` 的学生数量（原错误统计所有学生）
  - **已批改人数**：修改为统计 `submitted === true && score != null` 的学生数量
- **新增计算属性**：`submittedCount` 精确统计已提交学生数

#### 🔐 系统优化

- **登录错误日志**：`GlobalExceptionHandler.handleServiceException` 日志级别从 `log.error` 改为 `log.warn`，减少登录失败时的日志噪音
- **学生管理URL筛选**：从班级管理跳转到学生管理时，自动读取URL参数 `entryYear` 和 `classCode` 并设置筛选条件
- **个人中心页面**：
  - 左侧卡片"用户名称"改为"用户账号"
  - 隐藏手机号码、用户邮箱字段
  - 恢复显示"所属部门"

#### 📁 关键文件变更

| 文件                          | 修改内容                                   |
| ----------------------------- | ------------------------------------------ |
| `question/index.vue`          | 添加导出按钮、操作题预览按钮               |
| `score/index.vue`             | 添加账号列、修复班级显示逻辑               |
| `grading.vue`                 | 修复已交/已批改统计                        |
| `ScoreQueryController.java`   | 返回数据新增 userName、Excel导出新增账号列 |
| `GlobalExceptionHandler.java` | ServiceException 日志级别改为 WARN         |
| `student/index.vue`           | 添加URL参数自动筛选                        |
| `user/profile/index.vue`      | 调整个人信息显示                           |

---

### 2.0.2 2026-02-05 更新摘要 (用户批量导入优化)

- **用户导入模板优化**：
  - 导入模板新增"归属校区"动态下拉框，从数据库获取所有学校名称供选择
  - 下拉框设置为**允许自由输入**模式，支持选择后手动追加逗号输入多个学校
  - 使用 `deptMapper.selectDeptList()` 绕过 RuoYi 数据权限限制，确保所有用户都能获取完整学校列表
- **导入逻辑修复**：
  - 修复导入时角色和归属校区未设置的问题（原因：调用了 `userMapper.insertUser` 而非 `this.insertUser`）
  - 修正教师角色 ID 为 `100`（原错误配置为 `102` 教研员）
  - 导入时自动设置默认密码（从系统参数 `sys.user.initPassword` 获取）
- **Excel 工具类增强** (`ExcelUtil.java`)：
  - 新增 `comboMap` 动态下拉框配置支持
  - 新增 `allowFreeInput` 参数，控制下拉框是否允许自由输入（不弹出错误提示）
  - 兼容原有静态 combo 配置

### 2.0.2 2026-02-03 更新摘要 (学生个人成绩画像)

- **📊 新增"学生个人成绩画像"页面** (`/business/student-profile`)：
  - **入口**：侧边栏"成绩查询"下的"学生个人成绩画像"，或从成绩汇总表点击学生姓名跳转
  - **筛选器**：学期选择 + 班级级联筛选（年级→班级）+ 学生搜索/选择
  - **信息卡片**：深色科技风格 UI，展示学生姓名、备注(remark)、年级、班级、入学年份、班级排名、平均成绩、打字速度、课堂表现平均分
  - **可视化图表**：
    - **历次课程成绩**：柱状图，仅显示学生个人每次课程得分
    - **历次打字速度**：双折线对比（学生速度 vs 年级基准）
    - **课堂表现分变化**：折线图，**支持负分显示**
    - **班级平均分对比**：双折线对比（我的成绩 vs 班级平均）
    - **班级排名变化**：折线图，展示排名趋势
- **关键技术实现**：
  - **后端**：`StudentProfileController`, `StudentProfileServiceImpl`, `StudentProfileMapper.xml`
  - **前端**：`student-profile/index.vue`, `StudentSelector.vue`, `StudentInfoCard.vue`, 5个图表组件
  - **VO**：`StudentProfileVo` 包含内部类 `CourseScoreItem`, `TypingSpeedItem`, `PerformanceItem`, `RankItem`
- **Bug 修复**：
  - 修复学生列表重复问题（Service 层 Stream API 去重）
  - 修复跳转后班级筛选器未回显问题（watch immediate + 自动设置 selectedClass）
  - 修复课堂表现负分不显示问题（SQL `p.score > 0` → `p.score != 0`，Java 过滤条件同步修改）
  - 修复成绩查询页面 `showStudentProfile` 函数未定义问题
- **UI 优化**：
  - 信息卡片使用通用学习图标（`UserFilled`）替代图片头像
  - "表现评分"改为"课堂表现平均"
  - 柱状图移除班级平均分，改在独立折线图中展示对比

### 2.0.1 2026-01-14 更新摘要 (Chart Fullscreen & Visualization)

- **图表全屏优化**：成绩分析页所有 ECharts 图表支持全屏查看，右上角悬浮全屏按钮，全屏时图表占屏幕 98% 宽度 x 95% 高度。
- **全屏字体增大**：全屏模式下 X 轴标签 18px、Y 轴标签 16px、Y 轴名称 20px、数据标签 16px。
- **图表高度翻倍**：默认图表高度从 280px 增加到 560px，展示更多数据细节。
- **Tooltip 增强**：成绩排名图悬浮提示显示学号及各项分数明细（打字/理论/操作）。
- **年级计算修复**：修复了初中部硬编码逻辑，现在根据学校类型（小学/初中/高中）正确计算年级（如小学 2020 级显示为 6 年级）。
- **表格显示优化**：成绩汇总表和理论测试详情表现在仅显示班级号（如 "1" 而非 "1201"），且支持正确的数字排序。
- **年级概览模式**：(v2.5.1 Planned) 当成绩查询全选班级时，自动切换为年级概览模式，展示年级 Top 50 / Bottom 50 榜单及班级对比分析。
- **分数整数化**：所有分数显示统一四舍五入为整数，使用 `Roboto Mono` 等宽字体。
- **批改流程优化**：Enter 键提交后优先跳转到下一个未批改学生，输入框自动全选。

### 2.0.1 2026-01-12 更新摘要 (Random & Analytics)

- **随机出题系统**：支持课程内选择题/判断题的随机乱序与随机抽取，每个学生的考卷基于学号唯一确定。
- **成绩精细分析**：教师端新增题目维度的分析功能，包括易错题 TOP10 和选项分布详情。

### 2.1 角色定义

- **管理员 (Admin)**: 拥有系统完整权限，负责基础数据维护（学校、部门、用户）。
- **教师 (Teacher)**: 核心业务操作者，负责课程创建、指派、批改、学情分析。
- **学生 (Student)**: 终端用户，进行打字练习、答题、上传作品、查看个人成绩。

### 2.2 核心业务流程

1.  **课程创建流程**: 教师创建课程 → 添加/导入题目 (选择/判断/打字/操作) → 配置题目顺序与分值 → 保存课程。
2.  **发布指派流程**: 教师选择课程 → 选择指派班级 (关联 `biz_teacher_class` 权限) → 学生端首页自动显示该课程。
3.  **答题与评分流程**:
    - **理论题**: 学生提交 → 系统自动比对答案 → 实时判分。
    - **打字题**: 实时监控输入 → 计算 WPM/正确率/完成率 → 结合基准速度公式 → 自动判分。
    - **操作题**: 学生上传文件 → 系统转 PDF 预览 → 教师后台查看并根据评分项打分 → 系统自动折算最终分。
4.  **成绩分析流程**: 记录所有答题数据 → 聚合计算班级/课程平均分 → 生成图表 (排名/分布) → 教师查阅/导出。

### 2.3 功能模块详解

#### 🟢 教学管理端

| 模块         | 功能点     | 详细描述                                                                                          | 关键交互/接口                                           |
| :----------- | :--------- | :------------------------------------------------------------------------------------------------ | :------------------------------------------------------ |
| **课程设计** | 课程管理   | 创建/编辑课程，支持**随机出题模式** (固定/乱序/抽题) 配置，拖拽排序。                             | `designer.vue`, `BizLessonController`                   |
| **题目库**   | 试题维护   | 维护四类题型，支持 **Word/Excel 批量导入**，富文本题干编辑。                                      | `question/index.vue`, `ImportController`                |
| **班级指派** | 教学安排   | 灵活将课程指派给多个行政班级，支持按年级快速筛选。                                                | `BizLessonAssignmentController`                         |
| **作业批改** | 操作题评分 | **在线预览 PDF** (无需下载)，支持**分项打分** (如: 创新性 40%，完整性 60%)，自动计算总分。        | `grading.vue`, `PdfPreview`                             |
| **学情分析** | 数据看板   | **ECharts 可视化**：班级均分、**题目答题分析** (易错题/选项分布)、不及格名单、进退步分析。        | `score/index.vue`, `ScoreQueryController`               |
| **学生画像** | 个人分析   | **v2.6 新增**：查看单个学生的历次成绩、打字速度、课堂表现、班级排名变化，支持班级筛选、跳转入口。 | `student-profile/index.vue`, `StudentProfileController` |

#### 🔵 学生学习端

| 模块           | 功能点   | 详细描述                                                                          | 关键交互/接口                      |
| :------------- | :------- | :-------------------------------------------------------------------------------- | :--------------------------------- |
| **智能工作台** | 任务驱动 | 首页根据时间轴展示最新作业，状态区分：未开始/进行中/已完成/待批改。               | `StudentHomeController`            |
| **打字测评**   | 实时反馈 | 沉浸式打字界面，**防作弊** (禁粘贴)，实时显示速度/进度，练习结束后生成详细报告。  | `Typer.vue`, `typing-utils.js`     |
| **作品提交**   | 文件处理 | 支持大文件分片上传，**自动格式转换** (Docx -> PDF) 以供预览，支持多版本覆盖提交。 | `FileUpload`, `LibreOfficeService` |
| **错题本**     | 巩固提升 | 自动收集历史错题，支持筛选课程回顾，查看正确答案与解析。                          | `WrongQuestionDialog.vue`          |

### 2.3.1 学生端考试体验优化 (v2.6.1 - 2026-01-29)

本次更新针对 `student/index.vue` 进行了多项 UX 优化：

#### 🔒 顶部导航栏固定

- **实现**：`.dashboard-header` 设置 `position: sticky; top: 0; z-index: 2000;`
- **效果**：学生滚动答题时导航栏始终可见，方便快速访问历史成绩、错题本等功能

#### ✅ 理论测试答题反馈

- **需求演变**：从"选项高亮正确答案"调整为"仅在卡片右上角显示对错标记"
- **实现细节**：
  - 提交后每道题卡片右上角显示 `正确` (绿色✓) 或 `错误` (红色✗) 标记
  - 使用 Element Plus 的 `<el-icon>` 组件 (`Check`, `Close`)
  - 样式类：`.result-tag.correct` / `.result-tag.wrong`
- **判断题选中高亮**：
  - 提交后判断题的选项变为 `disabled`，但选中项保持蓝色高亮
  - 使用 `:deep()` 穿透 Element Plus 默认的禁用灰色样式
  - 选择器：`.audit-group :deep(.el-radio.is-disabled.is-checked .el-radio__inner)` + `!important`

#### ⌨️ 打字题防作弊与对齐

- **防复制/拖拽**：
  - 原文容器 `.text-content` 添加 `user-select: none; pointer-events: none;`
  - 同时绑定 `@copy.prevent @paste.prevent @cut.prevent @dragstart.prevent @contextmenu.prevent`
- **输入框与原文严格对齐**：
  - **统一字体**：`font-family: Consolas, "Courier New", monospace, "Microsoft YaHei";`
  - **统一排版**：`font-size: 18px; line-height: 2; letter-spacing: 1px;`
  - **统一换行**：`word-break: break-all; white-space: pre-wrap;`
  - **关键修复**：`.input-box` 添加 `padding: 0 17px;` 补偿 `.original-text-box` 的 `padding(16px) + border(1px)` 宽度差异，确保每行字数完全一致
  - **盒模型**：两者均使用 `box-sizing: border-box;`

---

## 💾 3. 详细数据库设计 (Database Schema)

### 3.1 核心业务表结构

#### 1. `biz_lesson` (课程/作业主表)

_定义了一次教学活动或作业的基本属性_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `lesson_id` | `bigint` | **PK** | 课程主键 ID |
| `lesson_title` | `varchar` | Yes | 课程标题 (如: "三年级上册期末考核") |
| `grade` | `int` | Yes | 适用年级 (1-9, 对应小学至初中) |
| `semester` | `char(1)` | Yes | 学期 (0:上册, 1:下册) |
| `lesson_num` | `int` | Yes | 课次序号 (用于排序, 如: 第 1 课) |
| `creator_id` | `bigint` | Yes | 创建教师 ID (数据权限控制) |
| `shuffle_mode` | `int` | - | 出题模式 (0:固定 1:随机排序 2:随机抽题) (**v2.5 新增**) |
| `random_choice_count` | `int` | - | 随机抽取选择题数 (模式 2 有效) (**v2.5 新增**) |
| `random_judgment_count` | `int` | - | 随机抽取判断题数 (模式 2 有效) (**v2.5 新增**) |
| `dept_id` | `bigint` | **FK** | 所属学校 ID (**v2.8 新增，多校隔离**) |
| `create_time` | `datetime` | - | 创建时间 |

#### 2. `biz_question` (统一题库表)

_存储所有类型的题目元数据_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `question_id` | `bigint` | **PK** | 题目主键 ID |
| `question_type` | `varchar` | Yes | 题型: `choice`(选择), `judgment`(判断), `typing`(打字), `practical`(操作) |
| `question_content`| `text` | Yes | 题干内容 (支持 HTML 富文本) |
| `option_a`~`d` | `varchar` | - | 选择题选项内容 |
| `answer` | `text` | Yes | 标准答案 (选择:A/B, 判断:T/F, 打字:全文) |
| `analysis` | `text` | - | 题目解析 |
| `file_path` | `varchar` | - | 操作题素材文件路径 |
| `preview_path` | `varchar` | - | 操作题素材预览路径 (PDF) |
| `typing_duration` | `int` | - | 打字限时 (**分钟**) |
| `is_public` | `char(1)` | - | 是否公共题目 (Y/N) |

#### 3. `biz_lesson_question` (课程-题目关联表)

_实现课程与题目的多对多关系，并定义题目在课程中的特定属性_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `bigint` | **PK** | 关联主键 |
| `lesson_id` | `bigint` | **FK** | 课程 ID |
| `question_id` | `bigint` | **FK** | 题目 ID |
| `question_score`| `int` | Yes | **本题分值** (同一题目在不同课程可分值不同) |
| `order_num` | `int` | Yes | 题目在课程中的排序号 |

#### 4. `biz_lesson_assignment` (班级指派表)

_控制哪些班级的学生可以看到并进行该课程_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `assignment_id` | `bigint` | **PK** | 指派记录 ID |
| `lesson_id` | `bigint` | **FK** | 课程 ID |
| `entry_year` | `varchar` | Yes | 入学年份 (如 "2024") |
| `class_code` | `varchar` | Yes | 班级编号 (如 "01", "02") |
| `dept_id` | `bigint` | **FK** | 所属学校 ID (**v2.8 新增，多校隔离**) |
| _联合索引_ | - | - | `idx_year_class` (`entry_year`, `class_code`) |

#### 5. `biz_student_answer` (答题记录表)

_存储学生的每一次答题详情与评分结果，是学情分析的数据源_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `answer_id` | `bigint` | **PK** | 记录 ID |
| `student_id` | `bigint` | **FK** | 学生 ID |
| `lesson_id` | `bigint` | **FK** | 课程 ID |
| `question_id` | `bigint` | **FK** | 题目 ID |
| `student_answer`| `text` | - | 学生提交内容 (文本或文件路径) |
| `is_correct` | `tinyint`| - | 自动判分结果 (0:错 1:对) |
| `score` | `int` | - | 获得分数 (未批改则为 null) |
| `answer_time` | `int` | - | 答题耗时 (秒) |
| **`typing_speed`**| `int` | - | **打字速度 (WPM/字每分)** |
| **`accuracy_rate`**| `decimal`| - | **打字正确率 (%)** |
| **`completion_rate`**| `decimal`| - | **打字完成率 (%)** |

#### 6. `biz_scoring_item` (操作题评分项表)

_定义操作题的细分评分维度 (v2.4 新增)_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `item_id` | `bigint` | **PK** | 评分项 ID |
| `question_id` | `bigint` | **FK** | 关联题目 ID |
| `item_name` | `varchar` | Yes | 评分维度名称 (如 "颜色搭配") |
| `item_score` | `int` | Yes | **权重比例** (合计应为 100) |

#### 7. `biz_scoring_detail` (操作题分项得分表)

_记录教师对每个评分项的具体打分 (v2.4 新增)_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `detail_id` | `bigint` | **PK** | 详情 ID |
| `answer_id` | `bigint` | **FK** | 关联答题记录 |
| `item_id` | `bigint` | **FK** | 关联评分项 |
| `score` | `int` | Yes | 实际得分 (原始分, 需按权重折算) |

#### 8. `biz_teacher_class` (教师班级权限表)

_定义教师可以管理哪些班级 (v2.4 新增)_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `bigint` | **PK** | 主键 |
| `user_id` | `bigint` | **FK** | 教师用户 ID |
| `dept_id` | `bigint` | **FK** | 学校 ID |
| `entry_year` | `varchar` | Yes | 入学年份 |
| `class_code` | `varchar` | Yes | 班级编号 |

#### 9. `biz_student` (学生信息表)

_存储学生的扩展信息，关联 sys_user_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `student_id` | `bigint` | **PK** | 学生主键 ID |
| `user_id` | `bigint` | **FK** | 关联 sys_user.user_id |
| `entry_year` | `varchar` | Yes | 入学年份 (如 "2024") |
| `class_code` | `varchar` | Yes | 班级编号 (如 "01") |
| `student_name` | `varchar` | - | 学生姓名 (冗余字段，主要从 sys_user.nick_name 获取) |
| `remark` | `varchar` | - | 备注信息 (用于学生画像显示) |

#### 10. `biz_classroom_performance` (课堂表现分记录表)

_记录学生每节课的课堂表现加减分 (v2.6 新增)_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `bigint` | **PK** | 记录 ID |
| `student_id` | `bigint` | **FK** | 学生 ID |
| `lesson_id` | `bigint` | **FK** | 课程 ID |
| `score` | `int` | Yes | 表现分 (**支持正负值**，如 +5 或 -3) |
| `dept_id` | `bigint` | **FK** | 所属学校 ID (**v2.8 新增，多校隔离**) |
| `create_time` | `datetime` | - | 记录时间 |

> **注意**：`score` 字段支持负数，用于表示扣分项。查询时使用 `score != 0` 过滤无效记录。

### 3.2 系统管理表 (System Management Tables)

#### 1. `sys_user` (用户信息表)

_存储系统用户信息，包括管理员、教师和学生_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `user_id` | `bigint` | **PK** | 用户主键 ID |
| `dept_id` | `bigint` | **FK** | 部门/学校 ID |
| `user_name` | `varchar` | Yes | 用户账号 (唯一) |
| `nick_name` | `varchar` | Yes | 用户昵称 |
| `user_type` | `varchar` | - | 用户类型 (00:系统用户) |
| `email` | `varchar` | - | 邮箱 |
| `phonenumber`| `varchar` | - | 手机号码 |
| `sex` | `char(1)` | - | 性别 (0 男 1 女 2 未知) |
| `password` | `varchar` | Yes | 密码 (BCrypt 加密) |
| `status` | `char(1)` | - | 状态 (0 正常 1 停用) |
| `del_flag` | `char(1)` | - | 删除标志 (0 存在 2 删除) |
| `login_ip` | `varchar` | - | 最后登录 IP |
| `login_date` | `datetime`| - | 最后登录时间 |

> **注意**: 实体类 `SysUser.java` 中的 `schoolId` 字段目前在数据库中未通过物理列存储，而是通过 `dept_id` 关联或业务逻辑处理。

#### 2. `sys_dept` (部门/学校表)

_存储组织架构，包括地区教育局、学校及校内部门_
| 字段名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `dept_id` | `bigint` | **PK** | 部门主键 ID |
| `parent_id` | `bigint` | Yes | 父节点 ID (0 为根) |
| `ancestors` | `varchar` | Yes | 祖级列表 (如 "0,100,101") |
| `dept_name` | `varchar` | Yes | 部门/学校名称 |
| `order_num` | `int` | - | 显示顺序 |
| `leader` | `varchar` | - | 负责人 |
| `school_code` | `varchar` | - | 学校官方代码 (**新增**) |
| `school_type` | `char(1)` | - | 学校类型 (1 小学 2 初中 3 高中) (**新增**) |

---

## 🛠️ 4. 业务逻辑规范 (Business Rules)

### 4.1 打字题评分公式 (v4.1)

采用简化公式，评分以原文字数为准（不依赖时长）：

```
速度系数 = min(正确字数 / 原文字数, 1.0)
得分 = 满分 × 速度系数 × 正确率
```

- **基准速度**：小学（1-6年级）20 字/分，初中及以上 40 字/分。
- **打字时长**：根据字数自动计算（字数÷基准速度），仅作为答题时间限制，不影响评分。
- **正确率**：正确字数 / 完成字数。

### 4.2 自动判分规则

- **选择题**：忽略大小写比较，全对得满分。
- **判断题**：支持中文答案（对/错/正确/错误）自动转换为 T/F 后比较。
- **操作题**：教师手动批改，支持**分项评分**（如界面设计、功能实现等），学生端显示"待批阅"状态直到教师评分。

### 4.4 分项评分逻辑 (P6)

- **配置入口**：题库管理 → 新增/编辑操作题 → 评分项配置（比例分配）
- **比例要求**：所有评分项的比例值合计必须为 **100**，否则无法保存
- **折算公式**：`实际分值 = 比例值 × (课程设定总分 / 100)`
  - 例：题库设置「界面 40、功能 60」→ 课程设置该题 50 分 → 批改时显示「界面 20 分、功能 30 分」
- **批改界面**：自动加载评分项，逐项打分后自动求和

### 4.5 随机出题机制 (v2.5)

- **配置模式**：
  - **固定顺序** (Default)：按题目添加顺序展示。
  - **随机排序**：仅针对**选择题**和**判断题**进行全量乱序，打字题/操作题固定在顶部。
  - **随机抽题**：从课程关联的所有题目中随机抽取 N 道选择题 + M 道判断题。
- **一致性保证**：学生端的随机基于 `Student_ID + Lesson_ID` 作为种子，保证同一学生多次进入或刷新页面看到的题目顺序/内容**完全一致**，但不同学生查看到的题目不同。

### 4.3 防作弊机制

- **打字题禁止复制粘贴**：`@paste.prevent`, `@copy.prevent`, `@cut.prevent`, `@contextmenu.prevent`
- 尝试粘贴时显示警告：`ElMessage.warning('打字练习禁止使用粘贴功能，请手动输入')`

---

## 🔧 5. 技术实现细节

### 5.1 文件预览 (LibreOffice)

- **依赖**：服务器需安装 LibreOffice（默认路径 `C:\Program Files\LibreOffice\program\soffice.exe`）
- **转换命令**：`soffice --headless --convert-to pdf --outdir "输出目录" "源文件"`
- **工具类**：`FileConversionUtils.convertDocxToPdfWithLibreOffice(docxPath, outputDir)`
- **前端预览**：使用 `PdfPreview` 组件（`@/components/PdfPreview/index.vue`）以 iframe 方式显示 PDF

### 5.2 操作题流程

1. 教师创建操作题时上传 `.docx` 素材 → 后端调用 LibreOffice 生成 `preview_path`（PDF）
2. 学生下载素材 → 修改后上传作品 → **显示"正在转换中"loading** → 后端转换 → 自动保存到 `biz_student_answer.student_answer`
3. 学生预览作品：调用 `/common/resource/view?resource=xxx` 接口（通过后端读取文件流，解决特殊字符文件名问题）
4. 右上角状态：未提交显示总分，已提交未批阅显示"待批阅"，已批阅显示"得分/总分"

### 5.3 理论测试提交规则

- **只能提交一次**：`theorySubmitted` 状态在页面加载时从 `submittedAnswers` 恢复，刷新后依然禁止重复提交
- 提交后回显已答答案和得分

### 5.4 请求超时配置

- 前端 Axios 超时设置为 **60 秒**（`request.js: timeout: 60000`），以支持 LibreOffice 转换等耗时操作

### 5.5 Loading 等待规范

- **操作题上传**：上传后显示 `uploadingQuestionId` loading，直到后端转换完成才允许预览
- 所有涉及 LibreOffice 转换的操作都需要等待，前端必须显示 loading 提示

---

## 🚀 6. 开发路线图 (Roadmap)

### P0: 核心体验优化 (立即执行)

### P0: 核心体验优化 (立即执行)

- [√] **打字题防作弊增强**：禁止复制粘贴，检测异常输入速度。
- [√] **历史成绩单**：学生端新增页面，默认显示今年历次课程成绩。
- [√] **错题本功能**：默认显示当前课程错题，可选历史课程，查看题目解析与正确答案。

### P1: 教学互动功能 (本月规划)

- [√] **教师批改操作题**：教师按班级/题目批改学生提交的操作题，支持 PDF 预览与快捷打分（入口：课程卡片）。
- [√] **分项评分体系** (P6)：操作题支持在题库配置评分维度，批改时按分项打分并自动折算。

### P2: 深度学情分析 (v2.4 完成)

- [√] **成绩查询与导出**
  - [√] 年级/班级/课程多维筛选、学生姓名/学号搜索
  - [√] 动态计算班级、分类平均分
  - [√] 全表格字段排序
  - [] 支持"所见即所得" Excel 导出功能
- [√] **ECharts 可视化图表**
  - [√] 班级平均分对比柱状图
  - [√] 总分 Top20 排名图
  - [√] 学生个人画像弹窗（成绩趋势折线图）
- [√] **学生个人成绩画像页面** (v2.6 新增)
  - [√] 独立页面展示单个学生的全面分析
  - [√] 五大可视化图表：历次成绩、打字速度、课堂表现、班级对比、排名变化
  - [√] 班级级联筛选器，支持从成绩汇总表跳转并自动回显
  - [√] 课堂表现支持负分计算
- [√] **打字能力分析**
  - [√] 表格新增"打字速度"列（字/分），悬停显示正确率/完成率
  - [√] 成绩详情弹窗展示打字三项指标
  - [√] 数据条 (Data Bars) 可视化总分/平均分
  - [√] 成绩详情弹窗展示打字三项指标
  - [√] 数据条 (Data Bars) 可视化总分/平均分
- [√] **题目深度分析** (v2.5)
  - [√] 课程维度：易错题 TOP 10 柱状图
  - [√] 题目维度：详细展示每道题的正确率及选项分布 (A/B/C/D 选择人数)
- [ ] **知识点图谱**：分析学生在不同知识点的掌握程度。
- [ ] **操作题 AI 评分**：引入 AI 自动批改操作题（需评估可行性）。

---

## 📝 开发备忘

1. **时间单位**：数据库中 `typing_duration` 为**分钟**，前端计算倒计时需转换为**秒**。
2. **所有权**：修改题目/课程时，需校验 `creator_id` 或管理员权限。
3. **文件存储**：配置路径为相对路径 `./uploadPath`（`application.yml: ruoyi.profile`）。
4. **LibreOffice 路径**：`FileConversionUtils.LIBRE_OFFICE_PATH` 配置为默认安装路径。
5. **判断题答案**：数据库存储 `T/F`，前端提交中文后后端自动转换（`normalizeJudgmentAnswer`）。
6. **评分项表结构** (P6)：`biz_scoring_item` 仅关联 `question_id`（不再关联 `lesson_id`），一道题的评分项全局通用。
7. **中文文件名预览**：`CommonController.resourceView` 使用 `FileUtils.percentEncode` 编码文件名，避免 HTTP 头报错。
8. **数据权限绕过**：查询所有部门/学校时（如导入模板下拉框），需使用 `deptMapper.selectDeptList()` 而非 `deptService.selectDeptList()`，后者受 `@DataScope` 注解限制会返回空结果。
9. **角色 ID 配置**：`100=教师`, `101=学生`, `102=教研员`。用户导入时默认分配教师角色 (`role_id=100`)。
10. **用户导入关联表**：必须调用 `this.insertUser()` 而非 `userMapper.insertUser()`，前者会自动插入 `sys_user_role` 和 `sys_user_dept` 关联表。
11. **Excel 动态下拉框**：使用 `ExcelUtil.setComboMap(Map<String, String[]>)` 设置动态下拉数据，key 为 `@Excel` 注解的 `name` 属性值。
12. **学生画像数据获取**：`StudentProfileServiceImpl` 中获取 `deptId` 需使用 `SecurityUtils.getDeptId()`，确保数据隔离。
13. **课堂表现负分**：`biz_classroom_performance.score` 支持负数，SQL 查询使用 `score != 0` 过滤，Java 计算平均分时同样使用 `!= 0`。
14. **学生列表去重**：`getStudentList` 方法使用 Stream API + TreeSet 按 `studentId` 去重，避免下拉框重复显示。
15. **跳转自动回显**：`StudentSelector.vue` 中 watch `studentId` 时需设置 `immediate: true`，并在加载学生信息后自动设置 `selectedClass`。
16. **Element Plus 样式穿透**：在 Vue 3 scoped CSS 中使用 `:deep()` 覆盖组件库默认样式，配合 `!important` 提升优先级（如禁用状态下的 Radio 高亮）。
17. **打字题输入框对齐**：需补偿父容器的 `padding + border` 宽度差异（如 `.input-box { padding: 0 17px; }`），使用等宽字体和一致的 `box-sizing: border-box`。
18. **防复制组合拳**：CSS (`user-select: none; pointer-events: none;`) + Vue 事件修饰符 (`@copy.prevent @paste.prevent` 等) 双重保险。
19. **学生账号锁定机制**：账号锁定通过 Redis 缓存 `pwd_err_cnt:username` 实现，值为失败次数，达到5次即锁定。解锁需清除该缓存键。
20. **重置密码同时解锁**：`BizStudentServiceImpl.resetStudentPwd` 重置密码后调用 `redisCache.deleteObject(CacheConstants.PWD_ERR_CNT_KEY + userName)` 清除锁定。
21. **锁定状态查询接口**：`BizStudentController.getLockStatus` 接收逗号分隔的用户名列表，返回 `Map<String, Boolean>` 锁定状态映射。
22. **学生导入必填校验**：`importStudent` 方法需校验 `studentName`, `entryYear`, `classCode`, `studentNo` 四个字段不能为空，否则跳过该记录。
23. **前端 computed 筛选**：锁定状态筛选使用 Vue computed 属性 `filteredStudentList` 在前端过滤，避免后端额外查询。
24. **班级显示格式化**：成绩查询中格式化班级名时，仅对1-2位数班号拼接年级（如 `1` → `601`），3位以上直接使用（如 `604`），避免重复拼接。
25. **账号唯一性**：`userName`（登录账号）具有唯一性且创建后不可修改，适合作为学生身份的稳定标识；`studentNo`（学号）可能因转班等原因变动。
26. **操作题不支持导出**：操作题的附件文件无法嵌入Excel，导出时需在前端检查 `questionType === 'practical'` 并提示用户。
27. **登录错误日志级别**：登录失败（如密码错误）属于正常业务场景，日志级别应为 WARN 而非 ERROR，避免日志噪音。
28. **多校数据隔离原则 (v2.8)**：所有涉及 `entry_year + class_code` 关联查询的地方，**必须同时加 `dept_id` 过滤**，否则不同学校的同名班级会串台。`biz_student` 表无 `dept_id`，需通过 `JOIN sys_user` 取 `u.dept_id`。
## 2.x 2026-03-16 待实现计划（学生机位锁 / 固定座位 / 固定机器）

### 状态
- **状态**：Pending / 待实现
- **实施阶段**：方案冻结，暂不编码
- **适用范围**：学生端登录识别、教师端机位锁管理、学生机本地助手
- **目标人群**：机房信息课场景下的教师与学生
- **目标问题**：防止学生换座位、换电脑登录同一个学生账号

### 一、业务结论与原理说明
本需求不是“普通网页登录拿 IP”能直接实现的功能，而是“若伊平台 + 学生机本地助手”联合实现的功能。

#### 1. 当前网络事实
- 县教育局服务器通过网页登录，通常只能看到学校出口或网关地址
- 服务器看不到学生机自己的 `192.168.x.x`
- 因此，单靠若伊后端现有的登录 IP 记录，无法判断“是不是同一台学生机”

#### 2. 为什么必须有本地助手
- 只有运行在学生电脑本机上的程序，才能读取这台电脑自己的局域网 IP、MAC、机器名等信息
- 浏览器网页本身不能可靠获取学生机真实内网身份
- 所以若要实现“固定哪台机子”，必须由学生机本地程序主动提供机器身份

#### 3. 若伊是怎么知道学生换座位的
若伊并不是“看到了学生坐在哪”，而是通过“同一个学生账号是不是从另一台机器来登录”来判断是否换了座位。

判断逻辑：
- 教师先把学生账号锁定到“当前这台机器”
- 后续这个账号再登录时，平台会检查当前登录机器是否还是原来那台
- 如果不是，就判定为“换机器/换座位”

#### 4. 锁定的真实对象
- 表面叫“IP锁”
- 实际上不应只锁 IP
- 正式方案必须改为：**设备码为主，IP/MAC为辅**
- 原因：IP 可能变化，MAC 也不适合单独做唯一判定，设备码最稳定

### 二、总体方案冻结
采用：**轻量本地助手 + 登录联动 + 教师审批换机** 方案。

#### 1. 方案核心
- 学生机安装一个轻量本地助手
- 助手开机自动运行
- 登录页先向本机助手询问机器身份
- 登录时把机器身份一起提交给若伊后端
- 若学生账号未启用机位锁，则登录行为不受影响
- 若学生账号已启用机位锁，则后端比对机器身份
- 不匹配时，拒绝登录，并生成“待老师审批的新机器记录”

#### 2. 本方案的最终目标
- 教师可主动点击“锁定当前机器”
- 学生账号被锁定后，只允许在已绑定机器上登录
- 学生若换机器登录，系统提示无法登录
- 老师若确实调整了座位，可以点击“同意换机器”
- 同意后，新机器成为新的绑定机器

### 三、机器身份规则（冻结）
#### 1. 主识别字段
使用 `deviceCode` 作为唯一主识别字段。

#### 2. `deviceCode` 的定义
- 每台学生机第一次安装/启动本地助手时生成一次
- 生成后保存在本地
- 后续始终使用同一个值
- 平台锁定与比对都以这个值为准

#### 3. 辅助字段
同时采集并上传：
- `localIp`
- `mac`
- `machineName`
- `helperVersion`

#### 4. 判定优先级
- 第一优先级：`deviceCode`
- 第二优先级：老师人工查看 `localIp / mac / machineName`
- 明确禁止：只用 IP 做唯一判定

#### 5. 设计理由
- IP 会变
- MAC 有时会因为网卡、系统策略、虚拟网卡等情况带来误判
- `deviceCode` 最稳，最适合“这是不是原来那台机子”的长期识别

### 四、本地助手设计（冻结）
#### 1. 部署形式
本地助手做成 **Windows 服务**。

默认要求：
- 学校统一安装
- 开机自动启动
- 后台常驻
- 不要求老师和学生手动打开

#### 2. 是否需要一直运行
- **需要**
- 但这是“后台自动运行”，不是让人手动一直开着
- 只要电脑正常开机，本地助手就自动可用

#### 3. 助手职责
本地助手只做少量工作：
- 生成并保存 `deviceCode`
- 读取本机内网 IP
- 读取 MAC
- 读取机器名
- 在本机 `localhost` 提供查询接口

#### 4. 本机接口约定
推荐固定接口：
- `GET http://127.0.0.1:<固定端口>/device-info`

返回字段：
- `deviceCode`
- `localIp`
- `mac`
- `machineName`
- `helperVersion`

#### 5. 对现有原型代码的定位
用户提供的 `HttpListener + localhost + 返回 ip/mac` 示例，应视为本地助手原型方向正确，但正式版必须补充：
- 稳定 `deviceCode`
- `machineName`
- 版本号
- Windows 服务形态
- 自动启动能力
- 统一异常处理

### 五、若伊登录联动方案（冻结）
#### 1. 登录前流程
学生点击登录前，登录页先访问本机助手，读取机器身份。

#### 2. 登录请求扩展字段
登录请求体新增：
- `deviceCode`
- `localIp`
- `mac`
- `machineName`
- `helperVersion`

#### 3. 后端登录判定顺序
后端固定按下面顺序处理：
1. 验证码校验
2. 用户名密码校验
3. 确认是否学生账号
4. 查询该学生账号是否启用机位锁
5. 若未启用，直接正常登录
6. 若已启用：
   - 当前 `deviceCode` 与绑定记录一致：允许登录
   - 当前 `deviceCode` 与绑定记录不一致：拒绝登录，并生成待审批记录

#### 4. 本地助手不可用时的规则
固定规则如下：
- **未启用机位锁的学生账号**：允许登录
- **已启用机位锁的学生账号**：拒绝登录，并提示“请联系老师或启动机位助手”

#### 5. 作用边界
v1 只做“登录时识别是否换机器”，不做登录后的实时巡检，不做持续在线心跳监控。

### 六、教师端业务流程（冻结）
#### 1. 功能入口
机位锁功能不放教师首页，放在现有学生管理相关页面中，避免首页复杂化。

建议入口：
- 学生管理页
- 按班级查看学生页
- 班级管理进入学生列表后的学生管理页

#### 2. 教师可见字段
每个学生应展示：
- 学生姓名
- 班级
- 登录账号
- 机位锁状态
- 当前绑定机器名
- 当前绑定 `deviceCode`
- 当前绑定内网 IP
- 当前绑定 MAC
- 最近登录机器信息
- 待审批新机器信息

#### 3. 教师操作按钮
每个学生应提供：
- `锁定当前机器`
- `解除锁定`
- `同意换机器`
- `拒绝换机器`
- `查看最近机器信息`

#### 4. 课堂实际使用流程
- 学生在固定座位电脑登录
- 教师核对无误后点击“锁定当前机器”
- 之后该学生账号只能在这台机器登录
- 若学生换了电脑，系统拒绝登录
- 若教师调换座位，学生在新机器登录会触发“待审批”
- 教师点击“同意换机器”后，平台更新绑定记录

### 七、后端数据设计（冻结）
新增一张“学生机位锁绑定表”。

建议保存字段：
- `user_id`
- `enabled`
- `locked_device_code`
- `locked_local_ip`
- `locked_mac`
- `locked_machine_name`
- `last_login_device_code`
- `last_login_local_ip`
- `last_login_mac`
- `last_login_machine_name`
- `last_login_time`
- `pending_device_code`
- `pending_local_ip`
- `pending_mac`
- `pending_machine_name`
- `pending_time`
- `approved_by`
- `approved_time`
- `status`

固定规则：
- 一名学生仅有一条当前有效绑定记录
- 最近登录信息持续更新
- 待审批记录保留最近一次异常机器
- 审批通过后，待审批信息替换当前绑定信息

### 八、接口设计（冻结）
#### 1. 登录接口
扩展登录请求体，接收机器信息字段。

#### 2. 教师端机位锁接口
需要以下能力：
- 查询某班学生机位锁状态
- 锁定学生当前机器
- 解除学生机位锁
- 同意学生换机器
- 拒绝学生换机器

#### 3. 本地助手接口
本机固定提供：
- `GET /device-info`

### 九、安装与运维策略（冻结）
#### 1. 安装方式
默认方式已确定：
- **学校统一安装**
- 不走老师手动逐台安装
- 不走学生自己安装

#### 2. 运维边界
- 每个学校机房管理员负责一次性部署到机房电脑
- 安装后自动运行
- 老师不负责客户端维护
- 学生不接触客户端

#### 3. 现实成本判断
- 该方案的主要成本不在若伊代码，而在“机房电脑统一安装本地助手”
- 但这仍是唯一能满足“固定哪台机子”要求的可靠方案
- 如果不接受本地助手，就无法可靠实现“固定学生机”这一目标

### 十、明确不采用的方案
以下方案明确判定为不满足需求：
- 只靠若伊现有登录 IP 锁定
- 只用学校出口 IP 识别机器
- 只靠网页直接拿学生机 `192.168.x.x`
- 不安装任何本地程序，却要识别具体哪台学生机

### 十一、测试与验收标准（冻结）
#### 1. 核心测试
1. 未启用机位锁时，学生登录完全不受影响
2. 已安装本地助手时，登录页能读到机器身份
3. 教师锁定当前机器后，同一台机器可继续登录
4. 同一账号换另一台学生机登录时被拒绝
5. 教师端出现“待审批新机器”
6. 教师点击“同意换机器”后，新机器可登录，旧机器失效
7. 教师点击“解除锁定”后，账号恢复自由登录
8. 本地助手未运行时：
   - 未锁定账号可登录
   - 已锁定账号拒绝登录
9. 同一台机器 IP 变化但 `deviceCode` 不变时，仍视为同一台机器
10. 机器重装导致 `deviceCode` 丢失时，平台视为新机器并要求老师重新审批

#### 2. 验收结论标准
只有同时满足以下条件，才算实施成功：
- 平台能稳定区分“是不是同一台学生机”
- 教师能主动锁定和解除
- 教师能审批换机器
- 未锁定账号不受影响
- 已锁定账号不能绕过机器识别
- 整套逻辑不依赖服务器直接看到学生机 `192` 地址

### 十二、默认假设（冻结）
- 本条为项目核心上下文中的**待实现计划**
- 当前不实施、不改代码
- 机位锁只对教师主动锁定的学生账号生效
- 机器唯一识别主依据固定为 `deviceCode`
- 本地助手固定为 Windows 服务，开机自启，后台常驻
- 学校统一安装，不走学生自装
- v1 只做登录时机器识别，不做登录后持续监控
