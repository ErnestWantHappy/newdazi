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
    PYTHON_PLAN ||--o{ PYTHON_PLAN_VERSION : 版本
    PYTHON_PLAN_VERSION ||--o{ PYTHON_PLAN_CLASS : 投放班级
    PYTHON_PLAN_VERSION ||--o{ PYTHON_PLAN_QUESTION : 题目快照
    PYTHON_PLAN_VERSION ||--o{ PYTHON_SUBMISSION : 独立刷题提交
    LESSON ||--o{ COLLAB_ROOM : 按班协作
    LESSON ||--o{ IOT_EXPERIMENT : 物联网实验
    IOT_EXPERIMENT ||--o{ IOT_GROUP : 小组
    IOT_GROUP ||--o{ IOT_MESSAGE : 消息
```

## 关键约束

- 表、字段、索引和菜单的最终定义以根目录 `sql/` 以及相关 MyBatis XML 为准。
- 所有迁移优先幂等，并带前检/后检；发布前必须对目标库备份，不能用本机结果替代。
- Python 独立表以 `biz_python_practice_*` 为主；课程答案仍在既有答题域。
- Python 专用题目标题、知识点、无输入标记、内容版本和验证状态保存在 `biz_programming_question_config`，不在统一题目表增加“适用场景”；课程用途由 `biz_lesson_question` 表达，刷题用途由题单版本关联表达。该配置表的可空唯一 `external_id` 只承载系统题稳定编号（如 `PYV2-001`），教师自建题为空。
- `biz_python_practice_plan_class` 以题单版本关联学校、入学年份和班级；同一范围允许多份题单。`entry_year` 的展示语义是“级”，当前自然年级由 7 月 20 日学年切换点和学校学段计算，不能直接显示成“届”。已发布题单修改时复制成新草稿版本，旧版本和快照继续服务既有学生，直到新版本发布。
- 题单删除一律物理清理完整依赖链，不保留 `ARCHIVED` 业务入口或恢复能力。旧 `biz_python_practice_extension*` 三表已退出查询链，正式库保留空表仅用于结构兼容。
- `biz_python_practice_submission_case` 保存独立刷题逐测试点结果；公开测试点可保存输出与错误摘要，隐藏测试点只保存状态、耗时、内存等脱敏字段。自定义运行的输入保存在刷题提交表 `custom_input`，不写课程答案或进度。
- 题单快照保存题目标题、难度、知识点和无输入标记，已发布内容不因题库后续编辑而漂移。
- 普通 Python 题删除前必须检查课程、题单、快照、草稿、提交和进度依赖；无依赖时按“测试点 → 编程配置 → 统一题目”同事务删除。一次性 V1 退场使用经精确前检和整库备份的 `sql/python_practice_polish_v3.sql` 清理完整依赖链，不复用普通删除接口。
- Python 题的统一题目记录不承载年级、学期和课次，三个字段保存为空；题目创建人保存真实人员“郑东旭”，系统题来源使用编程配置记录的 `create_by=python-system-v2` 标记，二者不可混用。
- IoT 以 `biz_iot_*` 保存实验、小组、设备、消息与诊断，Broker 管理凭据不入业务表。
- 协作保留既有房间/版本资料，CryptPad 迁移只扩展 Provider 能力，不删除历史 WPS 回滚材料。
