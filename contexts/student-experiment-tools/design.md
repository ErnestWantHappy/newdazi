# 学生实验工具 + 题目开放开关 —— 设计文档

> 版本：v1.0 · 创建：2026-08-22 · 配套 requirements.md

## 1. 数据模型

### 1.1 常驻工具（教师按班配置）

```sql
CREATE TABLE IF NOT EXISTS biz_student_tool (
  tool_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '工具ID',
  tool_name   VARCHAR(100) NOT NULL COMMENT '工具名称（学生端显示）',
  tool_url    VARCHAR(500) NOT NULL COMMENT '工具网址',
  tool_desc   VARCHAR(255) DEFAULT NULL COMMENT '简要说明（可选）',
  sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序（小在前）',
  enabled     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '启用 1=启用 0=停用',
  dept_id     BIGINT       DEFAULT NULL COMMENT '学校ID（数据隔离，空=平台级）',
  create_by   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
  update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
  remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (tool_id),
  KEY idx_st_tool_dept (dept_id, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生常驻工具';

-- 适用范围：一行=（工具, 入学年份级, 班级）；class_code 为空表示整个年级
CREATE TABLE IF NOT EXISTS biz_student_tool_scope (
  scope_id   BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
  tool_id    BIGINT      NOT NULL COMMENT '工具ID',
  entry_year VARCHAR(20) NOT NULL COMMENT '入学年份/级',
  class_code VARCHAR(20) DEFAULT NULL COMMENT '班级号，NULL/空=全年级生效',
  PRIMARY KEY (scope_id),
  KEY idx_st_scope_tool (tool_id),
  KEY idx_st_scope_scope (entry_year, class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生常驻工具适用范围';
```

### 1.2 本节课工具（随课程走）

```sql
CREATE TABLE IF NOT EXISTS biz_lesson_tool (
  tool_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
  lesson_id   BIGINT       NOT NULL COMMENT '课程ID',
  tool_name   VARCHAR(100) NOT NULL COMMENT '工具名称',
  tool_url    VARCHAR(500) NOT NULL COMMENT '工具网址',
  sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
  create_by   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
  update_by   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
  update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (tool_id),
  KEY idx_lesson_tool (lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生本节课工具（随课程）';
```

### 1.3 题目开放开关（挂在 biz_lesson_assignment 上）

```sql
ALTER TABLE biz_lesson_assignment
  ADD COLUMN theory_open    TINYINT(1) NOT NULL DEFAULT 0 COMMENT '理论测试题开放开关 1=开放' AFTER auto_advance_ready_time,
  ADD COLUMN practical_open TINYINT(1) NOT NULL DEFAULT 0 COMMENT '操作题开放开关 1=开放'   AFTER theory_open;
```

- 语义：当前这门课、这个班的理论/操作题是否对学生可见可做。
- **推进课程自动复位**：`advanceCurrentAssignment`（BizLessonAssignmentMapper.xml L150）换 lesson_id 时同时置 theory_open=0、practical_open=0。
- 手动开关：成绩页接口直接置 1/0。

## 2. 后端接口

### 2.1 常驻工具 —— StudentToolController，前缀 /business/student-tool，全部 teacher 权限
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /business/student-tool/list | 分页列表（dept 过滤，教师只见本校本工具） |
| GET | /business/student-tool/{id} | 详情（含适用范围） |
| POST | /business/student-tool | 新增（含 scope） |
| PUT | /business/student-tool | 修改（scope 全量替换） |
| DELETE | /business/student-tool/{ids} | 批量删除（级联删 scope） |
| PUT | /business/student-tool/sort | 排序 |

scope 提交结构（按年级分组表达）：
{ entryYears: [ { entryYear:'2024', allGrade:true } | { entryYear:'2024', classCodes:['1','2'] } ] }

### 2.2 本节课工具 —— 并入 BizLessonController
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /business/lesson/{id}/tools | 某课程的本节课工具列表 |
| PUT | /business/lesson/{id}/tools | 全量替换 { tools: [{toolName, toolUrl, sortOrder}] } |

课程 create/update 时也可一并携带 tools 数组。

### 2.3 学生端数据 —— StudentHomeController.getCurrentLesson 增强
AjaxResult 追加：
- `studentTools`: { lessonTools:[...], residentTools:[...] }
- `theoryOpen` / `practicalOpen`: boolean（当前班当前课程的开关）
- `hasTheory` / `hasPractical`: 该课程是否含对应题型（用于未开放时的提示条，不是把区域藏到找不到）

匹配逻辑（StudentToolService）：
- lessonTools：查 biz_lesson_tool by current lessonId，按 sort_order。
- residentTools：biz_student_tool where enabled=1 and (dept_id=学生dept or dept_id is null)，join scope where entry_year=学生.entryYear and (class_code=学生.classCode or class_code='')，distinct 按 sort_order。

### 2.4 题目开关（成绩页）—— ScoreController 增加
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | /business/score/lesson-gate?lessonId=&classCode=&entryYear= | → { theoryOpen, practicalOpen, hasTheory, hasPractical } |
| PUT | /business/score/lesson-gate | body { lessonId, classCode, entryYear, kind:'theory'|'practical', open:bool }；校验 dept_id=教师 dept、assignment 存在且 lesson_id=lessonId；只更新该行的对应列 |

## 3. 前端

### 3.1 学生端（views/student/index.vue）
- header-right 新增按钮（「历史成绩」前）：`<el-button type="info" link icon="Link" @click="studentToolVisible=true">学生实验工具</el-button>`
- 新增 el-dialog「学生实验工具」：分组「本节课工具」（空则整组隐藏+提示）、分组「常驻工具」（空显示「暂无常驻工具」）；每条 el-link，`window.open(url,'_blank','noopener')`。
- 数据：getCurrentLesson 返回 studentTools + theoryOpen/practicalOpen；computed 过滤：
  - theoryQuestions：理论题 = theoryOpen 时保留，否则 []（同时显示提示条「理论测试题暂未开放，请等老师在课堂开启」）；
  - practicalQuestions：同理。
- 刷新：登录/进入 fetch 一次；页面可见时每 60s 重拉 current-lesson（老师开启后学生端自动出现，无需手动刷新；V1 不接 WebSocket 广播）。

### 3.2 教师端常驻工具配置页（views/business/studentTool/ + manage）
- 菜单 SQL 参照 sql/teacher_tools_v1.sql 幂等写法，新增「学生实验工具」（目录）+「工具管理」页面路由。
- 管理页：表格（名称/网址/适用范围/启用/排序/操作）；弹窗编辑（名称、网址、说明、适用范围：选入学年份→勾「整个年级」或勾具体班级、启用）；删除二次确认（级联删 scope）；上移/下移排序。

### 3.3 课程设计器（views/business/lesson/designer.vue）
- 「开启物联网」下方新增「本节课工具」区块：行=名称+网址+上移/下移+删除，底部「添加工具」，随课程保存（form.tools）。
- detail 加载时回填。

### 3.4 成绩查询页（views/business/score/index.vue）
- 新增顶部卡片「题目开放」：仅当单选班级 && 单选课程 && 该课有理论或操作题时显示。
- 两个 el-switch（理论测试题 / 操作题）+ GET/PUT lesson-gate 即时保存；tooltip「课程推进后自动复位为关闭，也可在此手动开启/关闭」。

## 4. SQL 与配置
- `sql/student_tool_v1.sql`：建 2 张新表（幂等）+ biz_lesson_assignment 加 2 列（information_schema 判断，参照 iot_course_switch_v1.sql）+ 菜单（幂等，前缀 student-tool-）。
- 回滚 `sql/student_tool_v1_rollback.sql`：删表/删列/删菜单。
- 无需配 Redis/服务，后端重启 + 前端 build 生效。

## 5. 影响面与风险
- 触碰：学生首页、成绩页、课程设计器、StudentHomeController / ScoreController / BizLessonController、新增 StudentToolService/Controller/Mapper、BizLessonAssignmentMapper（推进复位）。
- 不动：teacherTools、权限模型、Python 刷题、IoT、协作。
- 风险：推进复位依赖 advance 调用点唯一（已确认 advanceCurrentAssignment 是唯一推进写点，自动推进复用 markAutoAdvanceReady→advance 流程，开发时复核无第二处直接 update lesson_id）；多选课程/年级概览不显示开关（符合设计）；student current-lesson 增加 2 个轻查询，量级小。

## 6. 测试要点
- 学生：未开启无理论/操作区+提示条；开启后 60s 内自动出现；面板两组分组正确；新标签页打开。
- 教师：常驻工具按班/全年级匹配；成绩页开关即时生效；推进自动复位；A 开 B 关。
- 越权：学生调写接口 403；改他校工具 403；跨班当前课程不匹配 400。
- 幂等：重复开启/关闭无副作用。

## 7. 2026-08-24 保存语义补充

- `LessonDetailVo` 增加 `initialTheoryOpen/initialPracticalOpen`，仅表达设计器对新指派班级的初始值，不是课程全局状态。
- `saveAll` 在删除并重建指派前读取旧指派，以学校+入学年份+班号为键保留既有开关；新键才采用 DTO 初始值。批量插入显式写入两列。
- 前端设计器在“指派班级”后展示“学生开放”双开关；存在对应题型时开关默认开启，已有班级状态仍由后端保留。
- 成绩页开放卡片紧跟筛选卡，继续通过 `/business/score/lesson-gate` 即时保存当前班级状态。
- 发布不执行存量 `biz_lesson_assignment` 开关回填 SQL；现有行保持原值。

## 8. 2026-08-24 课程设计器密度收口

- 设计器左右栏调整为 11/13 栅格，收紧卡片内边距、区块间距和表单行距，把更多横向空间留给题库与已选题。
- “本节课工具”在主流程只展示名称、数量和“配置”入口；展开编辑仍复用 `form.tools`，因此不改变接口或持久化。
- 学生开放、物联网和协作说明通过 `el-tooltip` 按需呈现；电子导学单使用单行状态与小尺寸按钮，继续复用既有组件事件。
- 已选题表格的操作列使用 `fixed=right`，列宽压缩并保持题目预览、排序、移除功能不变。
