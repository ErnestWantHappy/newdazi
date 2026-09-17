# 系统架构

## 组件关系

```mermaid
flowchart LR
    U[管理员 / 教师 / 学生 / 教研员] --> FE[Vue3 前端\nRuoYi-Vue3]
    FE -->|HTTP / WebSocket| API[Spring Boot\nruoyi-admin + ruoyi-business]
    FE --> LF[画程 LogicFlow\n随 Vue3 制品发布]
    API --> DB[(MySQL)]
    API --> REDIS[(Redis)]
    API --> FILES[uploadPath 文件与作品]
    API --> LO[LibreOffice 转换池]
    API --> J0[Judge0 CE\n私有 HTTP]
    API --> CP[CryptPad\nIntegration API]
    API --> MQ[EMQX MQTT Broker]
    DEV[掌控板 / Mind+] --> MQ
```

## 代码分层

- `ruoyi-admin`：应用启动、资源配置和聚合依赖。
- `ruoyi-business`：业务 Controller、Service、Provider、异步任务、WebSocket、MyBatis Mapper 和业务测试。
- `ruoyi-framework`、`ruoyi-system`、`ruoyi-common`：RuoYi 基础能力、认证、权限、缓存与通用工具。
- `RuoYi-Vue3/src/api`：前端 API 封装；`views`：角色和业务页面；`router`、`permission.js`：前端路由与守卫。

动态路由、Spring 注入、MyBatis XML 和反射会让静态调用图不完整。因此排查端到端链路必须至少核对：前端 API → Controller → Service → Mapper XML/SQL → 权限校验 → 单测或接口证据。

画程的 LogicFlow 仅是 Vue3 内嵌图编辑底座，不是独立服务，也不拥有课程权限或作品真源。教师配置、课程快照、学生草稿、正式提交、结构检查和成绩确认均由 `ruoyi-business` 与 MySQL 承担；浏览器离线备份只用于防丢和冲突恢复，不能绕过服务端修订号与交卷事务。流程图预览只返回学生基础图和不泄露答案的配置完成状态；课程设计器与课程快照均拒绝半成品或无法规范化的旧配置。流程图文字安全校验由前后端共同执行，服务端是最终边界。流程图 AI 批改复用普通文档操作题任务队列：服务端把学生提交和课程标准答案 JSON 渲染为图片，视觉模型以图片为主要依据，JSON/结构检查作为辅助上下文，建议分只有教师确认后才进入正式成绩。

## 真实时间链路

```mermaid
sequenceDiagram
    participant Device as 掌控板 / Mind+
    participant Broker as EMQX
    participant Receiver as IotMqttReceiver
    participant DB as MySQL
    participant WS as IoT WebSocket
    participant Teacher as 教师 Vue3 页面

    Device->>Broker: publish 班级/实验 Topic
    Broker->>Receiver: county/# 订阅消息
    Receiver->>DB: Topic 映射、消息和诊断入库
    Receiver->>WS: 广播已授权实验数据
    WS->>Teacher: 实时刷新
```
