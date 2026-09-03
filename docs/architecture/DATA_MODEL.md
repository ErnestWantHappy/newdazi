# 数据模型与迁移约定

## 数据域

```mermaid
erDiagram
    SCHOOL ||--o{ CLASS : 包含
    CLASS ||--o{ STUDENT : 包含
    TEACHER ||--o{ LESSON : 创建
    LESSON }o--o{ CLASS : 指派
    LESSON ||--o{ QUESTION : 包含
    STUDENT ||--o{ STUDENT_ANSWER : 课程作答
    STUDENT ||--o{ STUDENT_TASK_STATE : 课堂任务状态
    LESSON ||--o{ STUDENT_TASK_STATE : 课程题目六态
    STUDENT_ANSWER ||--o| ORPHAN_ANSWER_ARCHIVE : 删题前审计归档
    AI_MODEL_PRICE ||--o{ AI_GRADING_JOB : 创建时冻结单价
    PYTHON_PLAN ||--o{ PYTHON_PLAN_VERSION : 版本
    PYTHON_PLAN_VERSION ||--o{ PYTHON_PLAN_CLASS : 投放班级
    PYTHON_PLAN_VERSION ||--o{ PYTHON_PLAN_QUESTION : 题目快照
    PYTHON_PLAN_VERSION ||--o{ PYTHON_SUBMISSION : 独立刷题提交
    LESSON ||--o{ COLLAB_ROOM : 按班协作
    LESSON ||--o{ IOT_EXPERIMENT : 物联网实验
    IOT_EXPERIMENT ||--o{ IOT_GROUP : 小组
    IOT_GROUP ||--o{ IOT_MESSAGE : 消息
    LESSON ||--o{ LESSON_TOOL : 本节课工具
    STUDENT_TOOL ||--o{ STUDENT_TOOL_SCOPE : 适用范围
    SCHOOL ||--o{ STUDENT_TOOL : 常驻工具(可选)
    QUESTION ||--o| FLOWCHART_QUESTION : 画程配置
    LESSON ||--o{ FLOWCHART_LESSON_SNAPSHOT : 冻结题目口径
    STUDENT ||--o{ FLOWCHART_DRAFT : 自动保存
    STUDENT ||--o{ FLOWCHART_SUBMISSION : 不可变提交
    STUDENT_ANSWER ||--o| FLOWCHART_SUBMISSION : 当前提交引用
```

## 关键约束

- 表、字段、索引和菜单的最终定义以根目录 `sql/` 以及相关 MyBatis XML 为准。
- 所有迁移优先幂等，并带前检/后检；发布前必须对目标库备份，不能用本机结果替代。
- Python 独立表以 `biz_python_practice_*` 为主；课程答案仍在既有答题域。
- Python 专用题目标题、知识点、无输入标记、内容版本和验证状态保存在 `biz_programming_question_config`，不在统一题目表增加“适用场景”；课程用途由 `biz_lesson_question` 表达，刷题用途由题单版本关联表达。该配置表的可空唯一 `external_id` 只承载系统题稳定编号（如 `PYV2-001`），教师自建题为空。
- `biz_python_practice_plan_class` 以题单版本关联学校、入学年份和班级；同一范围允许多份题单。`entry_year` 的展示语义是“级”，当前自然年级由 7 月 20 日学年切换点和学校学段计算，不能直接显示成“届”。已发布题单修改时复制成新草稿版本，旧版本和快照继续服务既有学生，直到新版本发布。
- 题单删除一律物理清理完整依赖链，不保留 `ARCHIVED` 业务入口或恢复能力。旧 `biz_python_practice_extension*` 三表已退出查询链，正式库保留空表仅用于结构兼容。
- `biz_python_practice_submission_case` 保存独立刷题逐测试点结果；公开测试点可保存输出与错误摘要，隐藏测试点只保存状态、耗时、内存等脱敏字段。自定义运行的输入保存在刷题提交表 `custom_input`，不写课程答案或进度。
- 课程内 Python 自定义运行的输入保存在 `biz_programming_submission.custom_input`（仅 `submission_kind=CUSTOM_RUN` 使用）；逐点结果以测试点 ID 0 表达一次自定义执行。该记录不计分、不写 `biz_student_answer`，迁移脚本为 `sql/course_python_custom_run_v1.sql`。
- 题单快照保存题目标题、难度、知识点和无输入标记，已发布内容不因题库后续编辑而漂移。
- 普通 Python 题删除前必须检查课程、题单、快照、草稿、提交和进度依赖；无依赖时按“测试点 → 编程配置 → 统一题目”同事务删除。一次性 V1 退场使用经精确前检和整库备份的 `sql/python_practice_polish_v3.sql` 清理完整依赖链，不复用普通删除接口。
- Python 题的统一题目记录不承载年级、学期和课次，三个字段保存为空；题目创建人保存真实人员“郑东旭”，系统题来源使用编程配置记录的 `create_by=python-system-v2` 标记，二者不可混用。
- IoT 以 `biz_iot_*` 保存实验、小组、设备、消息与诊断，Broker 管理凭据不入业务表。
- 协作保留既有房间/版本资料，CryptPad 迁移只扩展 Provider 能力，不删除历史 WPS 回滚材料。
- 学生实验工具两类：`biz_lesson_tool`（本节课工具，随 lesson 去留）与 `biz_student_tool`+`biz_student_tool_scope`（常驻工具，scope 按 入学年份+班级，class_code 空=整个年级生效，dept_id 隔离学校）。学生端匹配：lessonTools 取当前课程 + residentTools 按 学校+年级+班级 匹配启用项。
- 题目开放开关：`biz_lesson_assignment.theory_open/practical_open`（班级x当前课程）。`advanceCurrentAssignment` 推进下一课时自动复位为 0；成绩页 `/business/score/lesson-gate` 读写。课程设计器的 `initialTheoryOpen/initialPracticalOpen` 默认均为开启；课程保存采用“先读旧指派→重建→回填”策略，已有班级保留旧值，仅新指派班级使用设计器提交值，避免重存课程覆盖课堂状态。
- 课程与 Python 题仍通过 `biz_lesson_question` 多行关联，不增加“一课一道 Python 题”唯一约束；合法性由全课程题目分值合计 100、题目启用和 `VALID` 状态共同约束。
- `biz_student_answer` 只有同时匹配当前 `biz_lesson_question(lesson_id, question_id)` 的记录才能进入批改、成绩、学情、截止进度和预览恢复等在线统计。课程保存移除题目时，必须在同一事务内把在线答案显式列复制到 `biz_student_answer_orphan_archive`，写 `biz_student_answer_orphan_archive_meta` 批次元数据并核对数量后，才能删除在线行和题目关联；归档失败必须回滚课程保存。
- `biz_ai_model_price` 保存模型输入/输出单价（元/千 token）、状态和说明；`biz_practical_ai_job` 保存新任务创建时的价格快照。任务理论费用只汇总 `biz_practical_ai_result` 已持久化的 token，用 `输入 token × 输入单价/1000 + 输出 token × 输出单价/1000` 计算，不等同于供应商账单。旧任务无快照时可引用当前价格，但必须在接口和页面标明口径。
- 画程迁移 `sql/flowchart_operation_v1.sql` 只新增四表：`biz_flowchart_question` 保存教师基础图/标准答案/权限/规则和配置修订；`biz_flowchart_lesson_snapshot` 按课程题目唯一冻结口径；`biz_flowchart_draft` 按学生课程题目唯一并以 `revision` 乐观并发；`biz_flowchart_submission` 保存递增版本、来源草稿修订、图文档、规则快照、检查证据和非正式建议分。现有 `biz_student_answer.student_answer` 只保存 `FLOWCHART:<submissionId>` 受控引用，正式分仍在原 `score` 字段。
- 画程文档真实数据为 `schemaVersion=1.0` 的平台 JSON；只保留四类节点、折线箭头、文字、坐标和受控锁定属性。PNG/SVG、LogicFlow 内部状态和未来 AI 结果都不是当前作品真源。
- 学生 Excel 导入的账号查重使用 `sys_user(user_name, del_flag)` 联合索引，迁移与回滚分别为 `sql/student_import_governance_v1.sql` 和 `sql/student_import_governance_v1_rollback.sql`。索引是普通索引，兼容同名历史软删除账号；有效账号唯一性继续由同校 Redis 导入锁、批量前检和写后数量核对共同保证。正式库已于 2026-09-01 执行迁移，前检有效用户名重复组为 0，迁移后 `EXPLAIN` 命中 `idx_sys_user_name_del_flag`。
- 一次学生导入中的 `sys_user`、`sys_user_role`、`biz_student` 和新增 `biz_teacher_class` 必须处于同一事务，按最多 200 条分批写入并核对影响行数；任何一段失败都必须回滚，禁止继续返回“部分成功”。允许在写库前排除格式错误、Excel 内重复和已有有效账号，并在结构化结果中分别计数。
- 学生安全处理复用既有状态字段：`sys_user.status` 为 `0=正常、1=停用`，停用不改 `biz_student` 及历史成绩；只有经 `StudentBusinessRecordMapper` 确认无答题及其他业务记录的学生才允许硬删除。批量纠错以 `student_id` 定位并原地更新 `user_id` 对应账号和档案，禁止删除重建。
- `biz_lesson.status` 为 `0=正常、1=已归档`，归档课程仍保留指派、答案和成绩，仅从日常课程查询中过滤；恢复通过课程状态接口完成。迁移脚本为 `sql/lesson_archive_status_v1.sql`，回滚脚本为 `sql/lesson_archive_status_v1_rollback.sql`。

## 2026-09-03 多功能改造数据模型状态

- `biz_student_task_state` 已完成本地领域类、Mapper、服务和迁移脚本，但尚未在本机或正式数据库执行。唯一键为 `lesson_id + question_id + student_id`；字段保存学校、六态 `task_state`、单调递增 `state_version`、`changed_at` 和审计时间。
- `sql/student_task_state_v1.sql` 会从当前课程仍引用的历史答案回填可确定的 `SUBMITTED/GRADED`，不猜测 `ENTERED/WORKING`；全班查询对无记录学生投影为 `NOT_ENTERED`、版本 0。回滚脚本为 `sql/student_task_state_v1_rollback.sql`。
- 星级评分不新增成绩表字段，`NUMERIC/STAR_TOTAL/STAR_ITEM` 仅为请求契约；正式成绩继续写既有整数列。
- 本机开发库已执行 `sql/class_grouping_v1.sql` 与 `sql/group_collaboration_v1.sql`：通用分组、课时快照、教师班级布局、独立协作活动/任务版本/小组映射、协作会话事件和 revision 差异表已存在。小组协作不写 `biz_student_answer`，不复用物联网分组事实；正式服务器仍须独立前检、备份、迁移和后检。详见 `contexts/online-collaboration/`。
