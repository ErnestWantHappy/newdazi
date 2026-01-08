# 信息科技学业测评平台 (Context)

> **版本**：v2.3
> **更新时间**：2026-01-07
> **核心定位**：服务于中小学信息科技课程的综合性教学与评价平台，集课程管理、多维度测评（选择/判断/操作/打字）、智能评分与学情分析于一体。

---

## 🏗️ 1. 项目架构 (Technology Stack)

基于 **RuoYi-Vue** 前后端分离架构进行深度定制开发。

- **后端**：Spring Boot, MyBatis-Plus, Spring Security, JWT
- **前端**：Vue 3, Element Plus, Vite, Pinia
- **数据库**：MySQL 8.0
- **文件预览**：**LibreOffice** (headless 模式，将 docx 转换为 PDF 预览)
- **核心模块**：`ruoyi-business` (业务逻辑), `RuoYi-Vue3` (前端交互)

---

## 🧩 2. 系统核心功能 (Current Features)

### 2.1 教学管理端 (Teacher/Admin)

| 功能模块       | 描述                                                                                                  | 状态        | 关键代码位置                                  |
| :------------- | :---------------------------------------------------------------------------------------------------- | :---------- | :-------------------------------------------- |
| **课程设计器** | 图形化界面创建课程，支持拖拽排序题目，配置分值。                                                      | ✅ 已完成   | `BizLessonController@saveAll`, `designer.vue` |
| **题库管理**   | 支持 **选择、判断、打字、操作** 四种题型。支持 Excel 批量导入。**操作题支持配置评分项（比例分配）**。 | ✅ 已完成   | `BizQuestionController`, `question/index.vue` |
| **打字题配置** | 可设置基准速度、时长限制、是否允许退格等参数。                                                        | ✅ 已完成   | `BizQuestion (typingDuration)`                |
| **班级指派**   | 将课程指派给特定年级和班级。                                                                          | ✅ 已完成   | `BizLessonAssignmentController`               |
| **学情仪表盘** | 按年级分组显示课程列表，支持实时排行榜。**班级平均分、及格率、高频错题待实现**。                      | ⏳ 部分完成 | `TeacherDashboardController@getDashboardData` |
| **实时排行榜** | 查看特定课程的学生实时排名和得分详情。                                                                | ✅ 已完成   | `TeacherDashboardController@getLessonRanking` |
| **学生管理**   | 学生账号管理，支持 Excel 导入和密码重置。                                                             | ✅ 已完成   | `BizStudentController@importData`             |
| **分项评分**   | 操作题批改支持分项评分，评分项按课程设定分数自动折算比例。                                            | ✅ 已完成   | `grading.vue`, `BizScoringItemMapper`         |

### 2.2 学生学习端 (Student)

| 功能模块       | 描述                                                             | 状态      | 关键代码位置                                            |
| :------------- | :--------------------------------------------------------------- | :-------- | :------------------------------------------------------ |
| **智能首页**   | 根据班级自动加载当前课程，显示未完成任务。                       | ✅ 已完成 | `StudentHomeController@getCurrentLesson`                |
| **打字测评**   | 实时计算速度(WPM)、正确率、完成率。**禁止复制粘贴**。            | ✅ 已完成 | `index.vue (autoSubmitTyping)`, `StudentHomeController` |
| **理论测评**   | 在线完成选择题和判断题，自动判分，显示总分和得分。               | ✅ 已完成 | `StudentHomeController@submitAnswers`                   |
| **操作题提交** | 下载素材 → 上传作品 →**在线预览**→ 显示批阅状态（待批阅/已批分） | ✅ 已完成 | `student/index.vue`, `PdfPreview` 组件                  |
| **历史成绩单** | 页面顶部导航栏进入，查看历次课程成绩及分项得分。                 | ✅ 已完成 | `StudentHomeController@getHistoryScores`                |
| **成绩反馈**   | 提交后即时显示得分、正确率及错题解析。                           | ✅ 已完成 | `index.vue (submitResult)`                              |

---

## 💾 3. 数据模型设计 (Database Schema)

### 核心业务表

- **`biz_lesson`** (课程表): 存储课程基本信息（标题、年级、学期）。
- **`biz_lesson_question`** (课程题目关联): 定义课程包含哪些题目、分值及顺序。
- **`biz_question`** (题库表): 存储题目内容、类型(`choice/judgment/typing/practical`)、标准答案、解析。
  - _特别字段_: `typing_duration` (打字限时), `file_path` (操作题素材), `preview_path` (预览文件 PDF 路径)。
- **`biz_student`** (学生表): 存储学号、入学年份、班级，关联 `sys_user`。
- **`biz_lesson_assignment`** (指派表): 记录课程指派给了哪些班级。
- **`biz_student_answer`** (答题记录表): 存储学生对每道题的答案、得分、耗时、是否正确。
  - 操作题的 `student_answer` 字段存储上传的文件路径, `score=null` 表示未批阅。
- **`biz_scoring_item`** (评分项表): 定义操作题的评分维度（名称、比例值）。
  - 比例值合计应为 **100**，系统按课程设定的分数自动折算实际分值。
- **`biz_scoring_detail`** (分项得分表): 记录每次批改的分项得分详情。

---

## 🛠️ 4. 业务逻辑规范 (Business Rules)

### 4.1 打字题评分公式 (v3.0)

为了兼顾速度与准确性，采用以下公式：

```
速度系数 = 实际速度 / 年级基准速度 (允许 > 1.0)
得分 = 满分 × 速度系数 × 正确率 × 完成率
最终得分 = min(得分, 满分)
```

- **基准速度**：七年级及以上 40 字/分，小学 20 字/分。
- **实际速度**：`完成字数 / (实际用时/60)`。
- **实际用时**：前端精确计算 (`durationLimit - timeLeft`)，防止刷新作弊。

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

- [√] **打字题防作弊增强**：禁止复制粘贴，检测异常输入速度。
- [√] **历史成绩单**：学生端新增页面，默认显示今年历次课程成绩。
- [√] **错题本功能**：默认显示当前课程错题，可选历史课程，查看题目解析与正确答案。

### P1: 教学互动功能 (本月规划)

- [ ] **课堂互动**：教师可发起"一键锁屏"、"随机点名"。
- [√] **教师批改操作题**：教师按班级/题目批改学生提交的操作题，支持 PDF 预览与快捷打分（入口：课程卡片）。
- [√] **分项评分体系** (P6)：操作题支持在题库配置评分维度，批改时按分项打分并自动折算。
- [ ] **即时反馈**：教师端实时看到学生答题进度条。
- [ ] **作品展示墙**：优秀的操作题作品自动展示。

### P2: 深度学情分析 (下季度)

- [x] **成绩查询与导出**
  - [x] 年级/班级/课程多维筛选
  - [x] 动态计算班级、分类平均分
  - [x] 全表格字段排序
  - [x] 支持"所见即所得" Excel 导出功能：查询综合成绩（打字/理论/操作），自动计算平均分。
  - 导出：支持导出学期汇总 Excel。
- [ ] **知识点图谱**：分析学生在不同知识点的掌握程度。
- [ ] **长期成长曲线**：展示学生打字速度、理论成绩的变化趋势。
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
