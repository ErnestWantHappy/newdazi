# 任务清单 —— 学生实验工具 + 题目开放开关

> 状态：功能已于 2026-08-23 随 `20260823_student_tool_v1` 正式发布，同日热修 `20260823_student_tool_hotfix_v1`（v1.25.1）；发布与验证证据见 `contexts/PROJECT_CORE.md` 第 1.5 / 15 节。下方勾选为收口时回填。
> 顺序：后端 → SQL → 前端 → 联调 → 收口

## 阶段 0：确认
- [ ] 用户确认 requirements.md / design.md v1.0（开关位成绩页、推进自动复位、打字题不开关、常驻按班/全年级、面板先本节课后常驻）

## 阶段 1：后端 + SQL
- [ ] sql/student_tool_v1.sql：建 biz_student_tool / biz_student_tool_scope / biz_lesson_tool；biz_lesson_assignment 加 theory_open/practical_open；菜单
- [ ] BizLessonAssignmentMapper.advanceCurrentAssignment 推进时复位双开关
- [ ] StudentToolService/Controller/Mapper：常驻 CRUD + scope 全量替换 + 按班匹配查询
- [ ] BizLessonController：lesson tools 查/存
- [ ] StudentHomeController.getCurrentLesson：studentTools + theoryOpen/practicalOpen + hasTheory/hasPractical
- [ ] ScoreController：GET/PUT lesson-gate（dept + 当前课程匹配校验）
- [ ] 全部写接口 @PreAuthorize teacher，学生接口只读

## 阶段 2：前端
- [ ] 学生首页：顶部按钮 + 面板；theory/practical computed 按 open 过滤 + 提示条；60s 可见时自动重拉
- [ ] 教师端 studentTool 管理页 + 菜单路由
- [ ] 课程设计器「本节课工具」区块
- [ ] 成绩查询页「题目开放」卡片

## 阶段 3：验证
- [ ] mvn 编译 / package -DskipTests 通过
- [ ] 本机库执行 SQL 前检/执行/后检
- [ ] 接口探活 + 越权 403 探针
- [ ] 浏览器冒烟（配工具+开题 → 学生面板+做题；二班不可见；推进复位）
- [ ] npm run build:prod 通过

## 阶段 4：收口
- [ ] 更新 contexts/PROJECT_CORE.md
- [ ] 更新 docs/architecture/（DATA_MODEL.md 加新表）
- [ ] 汇报：改了什么、怎么验、风险、发布要求（备份/SQL/重启/回滚）

## 同日热修（2026-08-23，release 20260823_student_tool_hotfix_v1）
- [x] Bug1 课程保存失败：`BizLessonMapper.xml` insertBizLesson 的 `iot_enabled` 列值不对称，values 段补齐。
- [x] Bug2 成绩页点课报错：`ScoreQueryController.getLessonGate`/`GuideSheetAccessService` 历史课程改优雅降级（`isCurrent=false` 不报错）。
- [x] Bug3 题目开放开关不显示：`score/index.vue` loadGateContext 兼容 AjaxResult 扁平结构，卡片条件为单班+单课+当前课程。
- [x] Bug4 学生端联动：`student/index.vue` 改 60s 静默轮询，打字进行中不刷新。
- 验证与发布证据：见 `contexts/PROJECT_CORE.md` 第 15 节（备份哈希、制品哈希、生产/本地验收、RELEASE_LOG 与 biz_platform_update update_id=48）。

## 开放状态保存热修（2026-08-24，release `20260824_python_iot_ux_v1`）

- [x] 课程设计器增加新指派班级的理论/操作初始开放选择，默认隐藏并说明课中仍可在成绩查询调整。
- [x] 后端保存课程时保留已有班级开关，只为新指派班级应用初始值；回归测试覆盖“旧值保留 + 新值初始化”。
- [x] 成绩查询开放卡片移至筛选区下方，操作题说明包含 Python 编程；专项测试、构建与正式接口冒烟通过。

## 课程设计器密度收口（2026-08-24，v1.26.2）

- [x] 设计器改为 11/13 栅格并收紧间距，本节课工具默认显示摘要、详细配置按需展开。
- [x] 题目开放/物联网/协作说明改为 tooltip，电子导学单压缩为单行状态和常规按钮。
- [x] 已选题操作列固定右侧；生产教师课程 279 在 2048 宽度下完成浏览器冒烟，6 道已选题的操作入口无需横向滚动即可看到。

## 课程设计器默认开放（2026-08-29）

- [x] 课程设计器现有“学生开放”理论题、操作题开关改为默认开启。
- [x] 不修改成绩查询、课程推进、数据库及存量班级当前开放状态。
- [x] Vue3 生产构建通过；正式课程设计器双开关均为开启，页面错误与 HTTP 500 为 0。
- [x] 前端已发布到 `20260829_course_designer_default_open_v1`，平台更新 `1.26.5` 已发布（update_id=55）。
