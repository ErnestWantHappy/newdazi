# 信息科技学业测评平台 — 开发进度上下文

> **会话日期**：2026-06-12 ~ 2026-06-14  
> **状态**：电子导学单模块核心闭环打通，教师端路由修复，看板 classCode 修复

---

## 1. 项目概览

基于 **RuoYi-Vue** 前后端分离架构的中小学信息科技教学与评价平台。

| 层级 | 技术栈 | 路径 |
|------|--------|------|
| 后端 | Spring Boot + MyBatis + JWT | `E:\Project\newdazi\RuoYi-Vue\` |
| 前端 | Vue 3 + Element Plus + Vite + Pinia | `E:\Project\newdazi\RuoYi-Vue3\` |
| 数据库 | MySQL 8.0 (ry-vue) | `localhost:3306` |

### 后端模块

```
ruoyi-admin       — 启动入口、全局配置
ruoyi-business    — 核心业务模块（课程/题目/考试/导学单/成绩）
ruoyi-framework   — 框架层（安全/数据权限/配置）
ruoyi-system      — 系统管理（用户/角色/菜单/部门）
ruoyi-common      — 通用工具
ruoyi-quartz      — 定时任务
ruoyi-generator   — 代码生成器
```

### 角色体系

| 角色 | role_id | 说明 |
|------|---------|------|
| 管理员 | 1 | 全权限 |
| 教师 | 100 | 教学管理 |
| 学生 | 101 | 学习端 |
| 教研员 | 102 | 数据查看 |

---

## 2. 功能模块总览

### 2.1 核心业务模块 (ruoyi-business)

| 模块 | Controller | 前端页面 | 功能说明 |
|------|-----------|---------|---------|
| 课程管理 | `BizLessonController` | `lesson/index.vue`, `lesson/designer.vue` | 创建/编辑课程，随机出题，班级指派 |
| 题库管理 | `BizQuestionController` | `question/index.vue` | 选择/判断/打字/操作题 CRUD，Excel 导入导出 |
| 学生管理 | `BizStudentController` | `student/index.vue` | 学生导入、锁定状态、密码重置 |
| 班级管理 | `BizTeacherClassController` | `teacherClass/index.vue` | 教师-班级权限绑定 |
| 成绩查询 | `ScoreQueryController` | `score/index.vue` | 多维度成绩汇总、ECharts 分析、请假标记 |
| 学生首页 | `StudentHomeController` | `student/index.vue` | 当前课程展示、答题、作品上传 |
| 教师首页 | `TeacherDashboardController` | `teacher/index.vue` | 年级课程概览、成绩入口 |
| 批改管理 | `TeacherGradingController` | `teacher/grading.vue` | 操作题 PDF 预览、分项打分 |
| 课堂表现 | `ClassroomPerformanceController` | `teacher/performance.vue` | 加减分记录 |
| 学生画像 | `StudentProfileController` | `student-profile/index.vue` | 个人成绩趋势、班级对比 |
| 学校统计 | `SchoolStatsController` | `schoolStats/index.vue` | 校级数据汇总 |
| 学校成绩 | `SchoolScoreController` | `schoolScore/index.vue` | 校级成绩查询 |
| **导学单** | **`GuideSheetController`** | **`guideSheet/`** | **详见第3节** |
| 区统考 | `CountyExamController` | — | 区级统一考试 |

### 2.2 系统管理模块 (ruoyi-system)

| 模块 | 功能 |
|------|------|
| 用户管理 | 用户 CRUD、角色分配、导入导出 |
| 角色管理 | 角色权限配置 |
| 菜单管理 | 动态菜单树配置 |
| 部门管理 | 学校/部门组织架构 |
| 字典管理 | 数据字典 |

---

## 3. 电子导学单模块 (v3.0 核心)

### 3.1 数据表 (5张)

| 表名 | 说明 |
|------|------|
| `biz_guide_sheet` | 导学单模板主表（表单JSON、标题、状态、页数、教师机IP） |
| `biz_guide_sheet_assignment` | 班级指派表（入学年份+班级编号） |
| `biz_guide_sheet_answer` | 学生填写记录（answer_json、当前页码、状态、提交时间） |
| `biz_guide_sheet_progress` | 实时进度表（当前页码、是否已提交、心跳时间） |
| `biz_guide_sheet_upload` | 多媒体上传记录（文件名、教师机路径、访问URL） |

### 3.2 前端页面

| 页面 | 路径 | 功能 |
|------|------|------|
| 导学单列表 | `guideSheet/index.vue` | 搜索/新增/删除/发布/关闭/设计/看板 |
| 设计器 | `guideSheet/designer.vue` | VForm3 拖拽设计表单、班级指派、关联课程、autoRenameWidgets |
| 看板 | `guideSheet/dashboard.vue` | ECharts 进度饼图、学生状态表格、WebSocket 控制、作品展示 |
| 预览 | `guideSheet/preview.vue` | 学生端填写预览 |

### 3.3 核心流程

```
教师设计导学单 → 指派班级 → 发布
    ↓
学生登录 → 填写表单 → 提交
    ↓
教师看板：
  - 实时进度 (WebSocket 心跳)
  - 页面控制 (翻页/广播/刷新)
  - 作品展示 (Flask 轮询)
  - 数据导出
```

### 3.4 关键接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `GET /business/guide-sheet/list` | list | 导学单列表 |
| `GET /business/guide-sheet/{sheetId}` | getInfo | 导学单详情 |
| `POST /business/guide-sheet` | add | 新增导学单 |
| `PUT /business/guide-sheet` | edit | 编辑导学单 |
| `DELETE /business/guide-sheet/{sheetIds}` | remove | 删除导学单 |
| `PUT /business/guide-sheet/publish/{sheetId}` | publish | 发布 |
| `PUT /business/guide-sheet/close/{sheetId}` | close | 关闭 |
| `GET /business/guide-sheet/progress` | getProgress | 学生进度 (classCode 可选) |
| `GET /business/guide-sheet/{sheetId}/uploads` | getUploads | 上传记录 |
| `GET /business/guide-sheet/{sheetId}/stats` | getStats | 统计数据 |

---

## 4. 本会话修复与变更记录

### 4.7 正确答案 UI 全面优化（可视化一键选择） ✅

- **文件**：`src/views/business/guideSheet/designer.vue`
- **问题**：上一版虽按字段类型渲染不同控件，但仍不够直观（如 select 用下拉框、switch 用开关）
- **优化**：
  - `radio/select` → A/B/C/D 字母按钮组（`indexToLetter()` 自动生成），点击即选，无需下拉
  - `checkbox` → 同上，可多选，`toggleCheck()` 手动管理数组
  - `switch` → ✅开 / ❌关 按钮，一目了然
  - `cascader` → 扁平化按钮组（`flattenCascaderOptions()` 展平路径），点击选择
  - `text/textarea` → 带 `clearable` 的增强文本框
  - `number/slider/rate` → 数值输入框
  - `date/time` → 日期选择器
  - `color` → 颜色选择器 + 文本输入
  - 所有类型统一使用 `isDisabled(row)` 判断禁用状态
- **新增**：`isDisabled()`, `isSelected()`, `toggleCheck()`, `indexToLetter()`, `arraysEqual()`, `flattenCascaderOptions()` 辅助函数
- **级联选择**：使用 `_cascaderPath` 临时字段存储路径数组，保存时同步到 `answer`
- **数据加载**：`extractScoredFields()` 处理级联选择的路径同步

### 4.6 评分配置正确答案 UI 改造 ✅

- **文件**：`src/views/business/guideSheet/designer.vue`
- **问题**：所有字段共用一个文本输入框，教师无法看到 radio/checkbox/select 的选项值
- **修复**：按字段类型动态渲染正确答案输入控件：
  - `radio` → `el-radio-group`（从 formJson 提取选项列表）
  - `checkbox` → `el-checkbox-group`（多选，支持部分给分）
  - `select` → `el-select`（下拉选择）
  - `cascader` → `el-cascader`（级联选择）
  - `switch` → `el-switch`（开关）
  - `slider/rate/number/input` → `el-input-number`（数值）
  - `date/time` → `el-date-picker`（日期）
  - `text/textarea/rich-editor` → `el-input`（文本输入）
- **新增** `getFieldOptions()` / `findWidgetById()` / `getCascaderOptions()` — 从 formJson 中递归提取字段选项
- **新增** `rawFormJson` ref — 保存原始 formJson 供选项提取使用
- **优化** `getFormJsonString()` — 序列化时正确处理各类型 answer（checkbox→数组, switch→布尔值, 其他→字符串）
- **后端**：[GuideSheetGradingService.java](RuoYi-Vue/ruoyi-business/src/main/java/com/ruoyi/business/service/GuideSheetGradingService.java) 新增 `gradePartialCheckbox()` — checkbox 多选支持按比例部分给分（答对 2/3 选项 → 得 2/3 分数）

### 4.1 autoRenameWidgets 字段标签修复 ✅

- **文件**：`src/views/business/guideSheet/designer.vue` (line 217-235)
- **问题**：VForm3 小部件的 label/name 存储在 `widget.options` 内，而非 widget 顶层
- **修复**：使用 `labelHolder = value.options || value` 兼容两种结构
- **效果**：空白标签→中文标签+序号，预设标签→拼音数字命名

### 4.2 重复路由问题修复 ✅

- **问题**：后台返回 `//business/guide-sheet-list` 双斜杠路由，且 `path: "/"` 的 Layout 路由被去重逻辑全部跳过
- **前端修复** (`src/permission.js` line 42-48)：
  - 保留路径规范化（`route.path.replace(/\/{2,}/g, '/')`）去除双斜杠
  - 移除了过于激进的 `if (!dup)` 去重逻辑，允许多个 `path: "/"` 的 Layout 路由共存
- **数据库修复**：`课程管理` 菜单 (menu_id=2006) 从 `status=1, visible=1` 恢复为正常状态
- **注意**：`store/modules/permission.js` 完全保持原始代码，不做任何修改

### 4.3 total_score 列缺失修复 ✅

- **问题**：学生端加载导学单报 `Unknown column 'total_score' in 'field list'`
- **原因**：迁移脚本 `sql/guide_sheet_grading.sql` 未在数据库执行
- **修复**：执行 ALTER TABLE 添加三列：
  ```sql
  ALTER TABLE biz_guide_sheet_answer
    ADD COLUMN total_score INT DEFAULT NULL COMMENT '总分',
    ADD COLUMN grading_status VARCHAR(10) DEFAULT NULL COMMENT '评分状态',
    ADD COLUMN grading_detail TEXT DEFAULT NULL COMMENT '评分明细 JSON';
  ```

### 4.4 看板 classCode 参数缺失修复 ✅

- **问题**：教师点击看板报 `Required request parameter 'classCode' is not present`
- **后端**：
  - `GuideSheetController.getProgress` 的 `classCode` 改为 `required = false`
  - `GuideSheetServiceImpl.getProgress` 空值时调用 `selectBySheetId` 查询全部班级
  - `GuideSheetProgressMapper.xml` 新增 `selectBySheetId` 查询
  - `GuideSheetProgressMapper.java` 新增接口方法
- **前端** (`guideSheet/dashboard.vue`)：
  - 班级输入框改为下拉选择框（`el-select`）
  - 自动解析导学单的 `assignedClassCodes` 作为选项
  - 支持「全部班级」选项

### 4.5 关联课程用户过滤 ✅

- **结论**：`BizLessonMapper.selectBizLessonList` 没有 `create_by` 过滤条件
- **设计意图**：课程为公开数据源，所有教师共享课程列表
- **前端行为**：`designer.vue` 中 `fetchLessonOptions` 虽然传了 `createBy` 参数，但后端忽略，无害

---

## 5. 关键文件索引

### 后端核心文件

```
ruoyi-business/src/main/java/com/ruoyi/business/
├── controller/
│   ├── GuideSheetController.java          # 导学单 CRUD + 进度 + 上传
│   ├── BizLessonController.java           # 课程管理
│   ├── BizQuestionController.java         # 题库管理
│   ├── BizStudentController.java          # 学生管理
│   ├── BizTeacherClassController.java     # 班级管理
│   ├── ScoreQueryController.java          # 成绩查询
│   ├── StudentHomeController.java         # 学生首页
│   ├── TeacherDashboardController.java    # 教师首页
│   ├── TeacherGradingController.java      # 批改管理
│   ├── ClassroomPerformanceController.java # 课堂表现
│   ├── StudentProfileController.java      # 学生画像
│   ├── SchoolStatsController.java         # 学校统计
│   └── SchoolScoreController.java         # 学校成绩
├── domain/
│   ├── BizGuideSheet.java                 # 导学单实体
│   ├── BizGuideSheetAnswer.java           # 填写记录实体
│   ├── BizGuideSheetProgress.java         # 进度实体
│   └── vo/
│       ├── GuideSheetProgressVo.java       # 进度VO
│       ├── GuideSheetVo.java               # 导学单VO
│       └── ...
├── mapper/
│   ├── GuideSheetMapper.java/xml           # 导学单MR
│   ├── GuideSheetAnswerMapper.java/xml     # 填写记录MR
│   ├── GuideSheetProgressMapper.java/xml   # 进度MR
│   └── GuideSheetUploadMapper.java/xml     # 上传记录MR
├── service/
│   ├── IGuideSheetService.java            # 导学单服务接口
│   ├── impl/GuideSheetServiceImpl.java    # 导学单服务实现
│   ├── GuideSheetGradingService.java      # 导学单评分服务
│   └── AsyncConversionService.java        # 异步文件转换
└── config/
    ├── WebSocketConfig.java               # WebSocket 配置
    └── ClassroomWebSocketHandler.java     # 课堂 WebSocket 处理器
```

### 前端核心文件

```
RuoYi-Vue3/src/
├── router/index.js                        # 路由配置 (constantRoutes + dynamicRoutes)
├── permission.js                          # 路由守卫 (路由规范化)
├── store/modules/permission.js            # 权限store (filterAsyncRouter, 保持原始)
├── views/business/
│   ├── guideSheet/
│   │   ├── index.vue                      # 导学单列表
│   │   ├── designer.vue                   # 设计器 (autoRenameWidgets fix)
│   │   ├── dashboard.vue                  # 看板 (班级下拉 fix)
│   │   └── preview.vue                    # 预览
│   ├── lesson/
│   │   ├── index.vue                      # 课程列表
│   │   └── designer.vue                   # 课程设计器
│   ├── question/index.vue                 # 题库管理
│   ├── student/index.vue                  # 学生管理
│   ├── teacher/
│   │   ├── index.vue                      # 教师首页
│   │   ├── grading.vue                    # 操作题批改
│   │   └── performance.vue                # 课堂表现
│   ├── score/index.vue                    # 成绩查询
│   └── student-profile/index.vue          # 学生画像
├── api/business/
│   ├── guideSheet.js                      # 导学单API
│   ├── lesson.js                          # 课程API
│   ├── question.js                        # 题目API
│   ├── student.js                         # 学生API
│   ├── score.js                           # 成绩API
│   ├── teacher.js                         # 教师API
│   ├── studentHome.js                     # 学生首页API
│   ├── teacherGrading.js                  # 批改API
│   └── ...
└── plugins/websocket.js                   # WebSocket 客户端
```

### SQL 脚本

```
RuoYi-Vue/sql/
├── biz_guide_sheet.sql                    # 导学单5表DDL
├── guide_sheet_grading.sql                # 评分字段迁移 (total_score等)
├── menu_guide_sheet.sql                   # 导学单菜单+权限
└── ry_20250522.sql                        # 系统初始化数据
```

---

## 6. 已知问题 & 待办

### 待执行数据库脚本

- [ ] `guide_sheet_grading.sql` — `biz_guide_sheet_answer` 评分字段迁移（已在本会话手动执行 ALTER TABLE）
- [ ] `typing_answer_dedup_fix.sql` — 打字答案去重
- [ ] `practical_preview_retry_fields.sql` — 操作题预览重试字段

### 待确认功能

- [ ] 学生端导学单文件上传集成 Flask 教师机
- [ ] 学生端 WebSocket 翻页指令响应
- [ ] 导学单发布校验（formJson 非空 + 至少指派一个班级）
- [ ] 提交后不可编辑（已提交→只读模式）
- [ ] 断线自动保存进度
- [ ] 导出导学单 Excel

### 数据库连接

```
Host: localhost:3306
Database: ry-vue
User: root
Password: Zhuyi3625!
```

### 后端启动

```
cd E:\Project\newdazi\RuoYi-Vue
java -jar ruoyi-admin/target/ruoyi-admin.jar
# 或 IDE 运行 RuoYiApplication
```

### 前端启动

```
cd E:\Project\newdazi\RuoYi-Vue3
npm run dev
# 访问 http://localhost:8081
# Vite proxy: /dev-api → http://localhost:8080
```