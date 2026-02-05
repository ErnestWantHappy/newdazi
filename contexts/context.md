# 信息科技学业测评平台 (Context)

> **版本**：v2.7
> **更新时间**：2026-02-05
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
