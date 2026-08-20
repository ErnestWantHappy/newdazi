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
    PYTHON_PLAN ||--o{ PYTHON_SUBMISSION : 独立刷题提交
    LESSON ||--o{ COLLAB_ROOM : 按班协作
    LESSON ||--o{ IOT_EXPERIMENT : 物联网实验
    IOT_EXPERIMENT ||--o{ IOT_GROUP : 小组
    IOT_GROUP ||--o{ IOT_MESSAGE : 消息
```

## 关键约束

- 表、字段、索引和菜单的最终定义以根目录 `sql/` 以及相关 MyBatis XML 为准。
- 所有迁移优先幂等，并带前检/后检；发布前必须对目标库备份，不能用本机结果替代。
- Python 独立表以 `biz_python_practice_*` 为主；课程答案仍在既有答题域。
- IoT 以 `biz_iot_*` 保存实验、小组、设备、消息与诊断，Broker 管理凭据不入业务表。
- 协作保留既有房间/版本资料，CryptPad 迁移只扩展 Provider 能力，不删除历史 WPS 回滚材料。
