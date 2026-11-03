# 系统架构

## 2026-11-02 当前正式1.30.66

今日拓展双空间、教材绑定/PDF、课堂Python终端、跨校监控消歧及封面/公开概览优化已发布。课堂“运行”由本站Pyodide Worker交互处理，不改变独立题单；“交作业”仍经原后端鉴权、Judge0和成绩链。封面是教材根目录内的鉴权派生图，公开概览缓存不参与课堂事实。[运行边界图](INTEGRATIONS.md)、[发布验收](../../contexts/release-20261102-all.md)。下方带旧日期的“未发布”为历史开发阶段。

## 2026-10-30拓展课双空间（本机，未发布）

拓展班模块复用现有学生档案、签到及Python判题，不接入普通作业/成绩链路。新建班级不写任教行政班关系；前端共用StudentSpaceSwitch，教师共用ExtensionClassPanel。权限与空间隔离由服务端最终检查，token不因切换改变。

```mermaid
flowchart LR
  A[原学生账号] --> B[行政班常规课]
  A --> C[活跃拓展成员]
  C --> D[建课名单与当前签到]
  C --> E[题单投放名单与Python练习]
  D --> F[历史签到保留]
  E --> G[历史学情保留]
```

当前仅本地代码与隔离库验收；原8080保留，18081/18080用于演示。130历史班迁移未执行。[设计、验证及限制](../../contexts/extension-class/tasks.md)。

2026-10-17当前正式1.30.52，双端`20261017_seat_score_v1`、助手0.2.4，机位浏览器优先与成绩/打字修复合并发布。无结构迁移，期限/PPTX和页图v4保持；真实教师/教研员通过，学生及机房待验。制品隔离、备份、重启、更新130/PUBLISHED和回退见[正式验收](../../contexts/seat-score-release-20261017.md)。

## 2026-10-14机位回环认领（130正式1.30.50）

```mermaid
sequenceDiagram
    participant S as 当前学生网页
    participant H as 本机助手127.0.0.1:28123
    participant API as 原机位业务API
    participant DB as MySQL确认快照
    participant T as 教师大屏
    H->>API: 设备密钥认证心跳及单次open凭证
    S->>H: GET identity（精确Origin / 800ms）
    H-->>S: installed与短期单次token，不含secret
    S->>API: 当前学生claim-local
    API->>API: 学校/设备/撤销校验，绑定会话及当前使用者
    T->>API: 5秒读取当前授权班级观测
    API-->>T: 助手IP及快照比对状态，未知无网关IP
    T->>API: 明确指定机房，整班或单人确认
    API->>DB: 当前指派/学校/基准加锁后保存IP和名称快照
```

新指纹增加MAC并经旧密钥升级保留设备ID，数据库新增两可空快照列；无新框架/依赖。回环接口有普通用户兼容路径，浏览器权限/还原/同传现场未验。配套发布与回退见[设计](../../contexts/seat-helper-local-20261014/design.md)，130正式1.30.50/0.2.3已迁移发布，教师/教研员通过、学生及机房待验，见[正式验收](../../contexts/seat-helper-local-20261014/release-verification.md)。

## 2026-10-14 登录输入优先（正式1.30.48）

Vue3入口不静态注册VForm，相关路由确认前按需注册，覆盖独立导学单、学生首页与课程面板。生产UMD按结构解析，失败不静默放行。登录引用WebP，展厅子树不随账号/密码重绘。

```mermaid
flowchart LR
    I[输入 / 中文组合态] --> Q[输入空闲调度器]
    Q -->|暂停| C[轮播]
    P[公开展厅响应] --> Q
    Q -->|输入空闲后分批| D[展厅渲染 / 图片扩展]
    R[相关导学单路由] --> V[按需加载并注册VForm]
    V --> G[路由确认 / 渲染]
```

不改认证、公开白名单、计分或数据库。构建自动运行登录回归与资源检查；模拟性能和机房待验边界见[验收](../../contexts/login-input-fix-20261014.md)。

## 2026-10-05 登录概览增量（正式1.30.28）

```mermaid
flowchart LR
    L[原Vue3登录页\n原三图轮播与精简登录框] --> P[两个固定匿名概览端点\n活动3条 / 开课6条]
    P --> S[LoginPreviewService\n字段白名单 / 60秒有效快照 / 独立锁]
    S --> M[LoginPreviewMapper\n现有表只读摘要聚合]
    T[既有调度器预热和提前刷新] --> S
    L --> A[原登录 / 按需验证码弹窗 / 选校]
    A --> D[原活动与监管详情\n登录 / 角色 / 数据范围校验]
```

不放开内部Controller或复用含正文/成绩的内部VO；不新增数据库表或依赖。页面概览不包含学生身份、具体成绩、资源地址或详情ID。正式已配套发布双端并重启后端，共享环境与机位助手保持；规则及证据见[设计](../../contexts/login-showrooms-20261005/design.md)及[正式验收](../../contexts/login-showrooms-20261005/release-verification.md)。

> 2026-10-03正式1.30.26已启用可选助手（61校），复用现有HTTP平台/API与Redis；独立客户端不成为登录/作答前置条件。无HTTPS/新依赖/表结构要求，正式注册、关联、跨页续期及无助手作答通过，机房现场分发仍待完成。当前以 [PROJECT_CORE v4.05](../../contexts/PROJECT_CORE.md) 为准。

> 2026-10-03正式1.30.24已上线（v4.02），先前本机候选描述转为历史；四份课堂SQL及厂商密钥依赖已迁移，正式API13组/浏览器9组通过。118条历史状态未治理，助手保持关闭、机房待验。见 [正式验收](../../contexts/classroom-experience-20261002/release-verification-20261003.md)。

> 2026-10-03 v4.01本机修复候选：学生布局提供机位会话，首页组件只展示；异步结果由身份/课班/学校键及请求代次约束。作品删除→清答案/版本→专用任务撤回→事务提交后广播；反馈、成绩及导出共享未提交事实。架构与依赖不变。 证据与限制见 [修复验收](../../contexts/classroom-experience-20261002/repair-verification-20261003.md)，未正式发布。

## 2026-10-02 课堂体验增量（本机候选）

```mermaid
flowchart LR
    S[原学生首页] --> API[原 Spring Boot 业务与权限]
    T[原教师批改/大屏] --> API
    API --> DB[(原答案/版本/成绩\n新增反馈快照和机位表)]
    API --> Files[原作品文件与 LibreOffice]
    API --> Text[受权 TXT/HTML JSON 和附件下载]
    Text --> Sandbox[Vue3 独立 HTML 沙箱\n效果/源码]
    H[可选 Windows 助手\n默认关闭] --> API
    API --> Redis[(短时设备心跳/单次关联)]
```

没有第二套登录或新教学入口：作品更新仍走原版本/事务，学生个人反馈在首页打开，教师在原报告抽屉整班发布；长期机位只作为原大屏的核实信息。TXT/HTML/压缩包不进入 LibreOffice/图像 AI，旧预览保持。客户端是按学校选择的独立 Windows 程序，不是后端必须安装的常驻依赖。五张新业务表和历史字段需配套迁移，即使助手关闭也需先建表；明确迁移、配置与未验现场边界见 [交付报告](../../contexts/classroom-experience-20261002/verification.md)。

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


## 2026-10-05 机位助手自动关联（正式1.30.27）

Windows客户端自动识别机器与IP；学生从鉴权下载包获得公开入口，原浏览器登录决定学校，原服务完成设备登记和配对。教师设置名称和显式基准。未新增服务、依赖或第二套账号。 证据、限制与下一步见 [正式验收](../../contexts/classroom-experience-20261002/seat-helper-auto-verification-20261005.md) 和 [ADR-004](../../contexts/classroom-experience-20261002/adr/ADR-004-student-zero-config-seat-helper.md)。

## 2026-10-07 本机补充

登录页现有匿名图片代理保留，统一源数据有效性检查；待选校Cookie只恢复前端选择流程，不承担授权。流程图预览按名单版本读取，草稿冲突显式选择后重取revision。机位单轮询不依赖首次成功，布局响应合并概览状态。无新依赖、表或服务；见 [复核记录](../../contexts/bug-review-20261007.md)。
