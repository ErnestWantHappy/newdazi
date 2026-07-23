# 教研活动模块实施计划

> 版本：v1.0  
> 日期：2026-07-22  
> 需求基线：[requirements.md](./requirements.md)  
> 技术设计：[design.md](./design.md)

## 1. 执行边界

### 必须完成

- 单一教研活动信息流、主题详情和三类留言。
- 主题/留言完整富文本、图片和表格。
- 课程资源结构化字段、课后反思、一个 50 MiB 主课件、最多三个云盘链接。
- 云盘提取码、永久有效/过期时间和过期标识。
- 活动/资源搜索和结构化筛选。
- 学段/指定教师通知、首页顶部通知栏、已读和再次通知。
- 角色/作者权限、软删除、恢复、置顶、统计。
- SQL、单测、构建、接口验收、浏览器冒烟和发布清单。

### 明确不做

- 原平台迁移和旧入口。
- 学生端。
- 板块、积分、售帖、点赞、收藏、排行榜。
- 主题状态、审批、草稿、开启/关闭和关闭回复。
- Elasticsearch、WebSocket 通知、外部推送。
- 未经确认替换全平台编辑器或引入新技术栈。

### 当前仓库风险

当前工作分支存在与教研活动无关的热修和未提交文件。实施代理不得直接清理、覆盖、提交或混入这些改动。第一项任务必须解决工作隔离问题。

## 2. 里程碑

| 里程碑 | 可交付结果 | 通过条件 |
| :--- | :--- | :--- |
| M0 技术门槛 | 安全开发基线 + Quill 2 表格验证 | 不污染现有热修；编辑器验证证据完整 |
| M1 后端基础 | 四张表、权限、领域模型、清洗和访问控制 | SQL 演练、核心单测通过 |
| M2 核心业务 | 主题、留言、资源、搜索、通知 API | 接口角色矩阵和边界用例通过 |
| M3 Vue3 页面 | 信息流、详情、资源表单、搜索、首页通知 | Vue3 构建与两角色冒烟通过 |
| M4 发布候选 | 全套测试、文档、迁移和回滚清单 | clean package、生产构建、验收报告通过 |

## 3. 分阶段任务

- [x] 0. 需求分析与方案确认

  - [x] 0.1 检查原平台论坛
    - 已确认旧平台的板块、主题、楼层回复、富文本、图片、附件、表格和低效搜索逻辑。
    - 已确认不迁移旧数据、不保留旧入口、不继承板块与积分体系。
    - _Requirements: REQ-02, REQ-08, REQ-12_

  - [x] 0.2 确认产品边界
    - 已确认角色、主题类型、三类留言、结构化课程字段、课后反思、课件和云盘规则。
    - 已确认通知、搜索、权限、无主题状态和教师首页顶部通知栏。
    - _Requirements: REQ-01—REQ-12_

- [x] 1. 建立安全开发基线与编辑器门槛

  - [x] 1.1 隔离当前脏工作区
    - 读取 `AGENTS.md`、`contexts/context.md` 顶部焦点、`git status` 和现有 diff。
    - 向用户确认基线分支/提交后，使用 `codex/research-activity-v1` 独立分支或独立 worktree。
    - 不执行 `reset --hard`、不删除现有未提交文件、不把热修改动带入论坛提交。
    - 记录基线提交哈希和原工作区状态。
    - _Requirements: REQ-12_
    - _Dependencies: none_
    - _Success: 新开发环境干净，原工作区内容和 diff 完整保留。_
    - _Evidence: 2026-07-22 以已推送提交 `730d3f2` 为基线创建 `codex/research-activity-v1`；新分支工作区干净，原热修分支已同步远端。_

  - [x] 1.2 复核本机运行条件
    - 确认本机 MySQL、Redis、后端 8080、Vue3 前端端口和教师/教研员账号可用。
    - 不使用用户提供的旧平台密码作为当前平台凭据；当前凭据只从 `AGENTS.md` / `secrets.local.md` 获取。
    - 记录无法满足的依赖，但不先安装无关工具。
    - _Requirements: REQ-12_
    - _Dependencies: 1.1_
    - _Evidence: Java 8、Maven 3.9.16、Node 22.14.0、npm 10.9.2 可用；MySQL `xueyeceping1` 8.0.39 查询成功，Redis 返回 `PONG`；80/8080 当前未启动，留待编辑器与接口验收阶段按需启动。_

  - [x] 1.3 完成 Quill 2 表格兼容验证
    - 在隔离组件/验证页中开启 Quill 2 table module。
    - 实测插入 3×3 表格、增删行列、删除表格、HTML 保存回显和再次编辑。
    - 实测图片选择、粘贴、会话头刷新、同页多编辑器和生产构建。
    - 输出截图、控制台结果和最小验证说明。
    - 如失败，立即停止编辑器路线实施并报告复现步骤；未经确认不得安装 WangEditor。
    - _Requirements: REQ-03_
    - _Dependencies: 1.1, 1.2_
    - _Success: 六项门槛全部通过，或形成可重复的失败证据并获得新路线确认。_
    - _Evidence: 2026-07-22 使用 Playwright 实测 3×3 表格增删行列、删除整表、HTML 回显再编辑、文件选择与粘贴图片、最新授权头和双编辑器隔离全部通过；生产构建退出码 0，报告见 `output/playwright/quill-gate-report.json`。_

  - [x] 1.4 冻结接口和表结构
    - 对照 `design.md` 复核命名、枚举、字段长度、索引和 REST 路径。
    - 确认不使用 `sys_notice`、不增加主题状态字段、不建立旧论坛迁移表。
    - 在编码前把任何必要偏差更新回设计文档并说明原因。
    - _Requirements: REQ-02, REQ-09, REQ-12_
    - _Dependencies: 1.3_
    - _Evidence: 已逐项复核 `requirements.md` 与 `design.md`，四表、枚举、REST 路径和权限点一致；确认不使用 `sys_notice`、不增加主题状态、不建立旧论坛迁移表。_

- [x] 2. 数据库、菜单和领域模型

  - [x] 2.1 编写幂等增量 SQL
    - 新建 `sql/research_activity_v1.sql`。
    - 创建 `biz_research_topic`、`biz_research_post`、`biz_research_resource`、`biz_research_notice_recipient`。
    - 创建主题信息流、资源筛选、作者、资源归属、通知未读和主题+用户唯一索引。
    - 幂等创建顶级菜单“教研活动”和功能权限。
    - admin/teacher/researcher 分角色授权；notify/pin/manage 只给 admin/researcher。
    - 文件尾加入表、索引、菜单唯一性、角色权限和重复通知组复核查询。
    - _Requirements: REQ-01, REQ-02, REQ-08, REQ-09, REQ-12_
    - _Dependencies: 1.4_

  - [x] 2.2 在隔离本机库演练 SQL
    - 执行前确认目标仅为本机测试库。
    - 连续执行脚本两次，第二次不得报错或产生重复菜单/权限。
    - 核对四张表、全部索引、三角色授权和重复通知组为 0。
    - 保存非敏感执行证据。
    - _Requirements: REQ-12_
    - _Dependencies: 2.1_

  - [x] 2.3 新增领域对象、DTO 和 VO
    - 新增四个 domain、主题/留言/资源/通知请求 DTO 和页面 VO。
    - 枚举值集中定义或通过常量类统一，禁止散落魔法字符串。
    - DTO 使用 Bean Validation 处理单字段长度；组合规则留给服务层。
    - `toString`、操作日志不得包含正文、云盘提取码、完整 URL 或私有路径。
    - _Requirements: REQ-02—REQ-09, REQ-12_
    - _Dependencies: 2.1_

  - [x] 2.4 实现 Mapper 和 XML 基础 CRUD
    - 实现主题、留言、资源、通知和教师目标查询。
    - 软删除条件作为所有正常查询的固定条件。
    - 实现原子计数、行锁、批量资源查询和通知 upsert。
    - 为 LIKE 参数提供统一转义，禁止字符串拼接 SQL。
    - _Requirements: REQ-02, REQ-04, REQ-08, REQ-09, REQ-11_
    - _Dependencies: 2.2, 2.3_

- [x] 3. HTML 清洗、权限和校验基础

  - [x] 3.1 实现富文本白名单清洗
    - 新建 `ResearchActivityHtmlSanitizer`，保留安全排版、表格、图片、链接和受控 Quill class。
    - 移除 script、iframe、事件属性、任意 style、base64 图片和危险协议。
    - 统一链接 `rel=noopener noreferrer`，生成 `content_text`。
    - 校验正文非空、HTML/纯文本长度和图片最多 20 张。
    - _Requirements: REQ-03, REQ-12_
    - _Dependencies: 2.3_

  - [x] 3.2 编写清洗单元测试
    - 覆盖合法表格、rowspan/colspan、Quill 对齐/字号、内部图片和 HTTP(S) 链接。
    - 覆盖 script、onerror、javascript、data、恶意嵌套和超量图片。
    - _Requirements: REQ-03, REQ-12_
    - _Dependencies: 3.1_

  - [x] 3.3 实现访问控制服务
    - 新建 `ResearchActivityAccessService`。
    - 实现 admin/teacher/researcher 可读、学生拒绝。
    - 实现作者编辑/本人软删除，管理角色隐藏/恢复/置顶/通知。
    - 明确管理员和教研员不能修改他人正文。
    - 实现主题、留言、资源三层未删除归属校验。
    - _Requirements: REQ-01, REQ-06, REQ-09, REQ-11, REQ-12_
    - _Dependencies: 2.3, 2.4_

  - [x] 3.4 编写权限矩阵单元测试
    - 覆盖教师、教研员、管理员、学生和匿名。
    - 覆盖本人/他人内容、软删除后资源访问和通知本人已读。
    - _Requirements: REQ-01, REQ-11, REQ-12_
    - _Dependencies: 3.3_

- [x] 4. 主题与普通留言后端

  - [x] 4.1 实现主题服务
    - 新增信息流、详情、立即创建、本人编辑、软删除、恢复和置顶。
    - 不创建 status、draft、open、close 或 replyLock 逻辑。
    - 保存清洗 HTML 和纯文本，初始化 `last_activity_time`。
    - 编辑主题不触发通知。
    - _Requirements: REQ-02, REQ-03, REQ-11_
    - _Dependencies: 3.1, 3.3_

  - [x] 4.2 实现普通留言和活动纪实服务
    - 新增、编辑、软删除、恢复、类型筛选和最新排序。
    - 留言成功/删除时正确维护回复数和最后互动时间。
    - 管理角色可以隐藏，不可编辑正文。
    - _Requirements: REQ-04, REQ-11_
    - _Dependencies: 4.1_

  - [x] 4.3 实现主题和留言 Controller
    - 加入明确的 `@PreAuthorize` 权限点和 `@Log`。
    - `@Log` 排除 `contentHtml`、links、extractCode 和文件 payload。
    - 列表强制 pageSize ≤50，错误统一为可理解中文。
    - _Requirements: REQ-01—REQ-04, REQ-11, REQ-12_
    - _Dependencies: 4.1, 4.2_

  - [x] 4.4 编写主题/留言服务测试
    - 覆盖教师只能分享、教研员通知主题、无状态立即可用、持续回复。
    - 覆盖置顶排序、软删除、恢复、计数和“已编辑”。
    - _Requirements: REQ-02, REQ-04, REQ-11_
    - _Dependencies: 4.1—4.3_

- [x] 5. 课程资源、文件和云盘后端

  - [x] 5.1 实现课程资源组合校验
    - 校验学段和绝对年级一致、学期、课次类型/编号、课程标题。
    - 课后反思清洗后非空，不设置最低字数。
    - 最多一文件、三链接，且至少一种资源。
    - _Requirements: REQ-05_
    - _Dependencies: 3.1, 4.1_

  - [x] 5.2 实现云盘链接校验和状态
    - 只允许绝对 HTTP(S) URL。
    - 永久有效映射为空过期时间。
    - 非永久链接必须晚于保存时当前时间。
    - VO 计算 `PERMANENT/VALID/EXPIRED` 和中文展示文本。
    - 已过期链接保留打开和复制能力。
    - _Requirements: REQ-07_
    - _Dependencies: 5.1_

  - [x] 5.3 实现主课件安全上传服务
    - 校验 50 MiB 字节边界、ZIP/RAR/7z 扩展、MIME 和文件头。
    - 使用 `${profile}-private/research-activity` 和规范化相对路径。
    - 实现新增、保留、删除、替换和失败孤儿清理。
    - 不复用公开 `/profile/**`，不把绝对路径返回前端。
    - _Requirements: REQ-06, REQ-12_
    - _Dependencies: 5.1_

  - [x] 5.4 实现一体化资源事务
    - multipart `payload + optional file` 保存资源留言。
    - 锁定目标留言，防止并发突破一文件/三链接。
    - 同事务写 post、resource 行和主题计数。
    - 更新资源时正确处理 `KEEP/REMOVE/REPLACE`。
    - _Requirements: REQ-05—REQ-07, REQ-11, REQ-12_
    - _Dependencies: 5.2, 5.3_

  - [x] 5.5 实现课件下载和链接访问接口
    - 下载前执行三层归属和角色校验，流式响应并原子计数。
    - 链接访问先授权，再尽力计数；计数失败不能阻止打开。
    - 响应头防嗅探、私有缓存、中文文件名正确。
    - _Requirements: REQ-06, REQ-07, REQ-11_
    - _Dependencies: 5.4_

  - [x] 5.6 实现专用富文本图片上传
    - JPG/JPEG/PNG/WebP，单张 10 MiB，校验 MIME 和文件头。
    - 返回与现有 Editor 兼容的 `fileName/url`。
    - 为通用受控预览增加 WebP Content-Type，不放宽学生权限。
    - _Requirements: REQ-03, REQ-12_
    - _Dependencies: 3.3_

  - [x] 5.7 编写资源和上传测试
    - 覆盖 49MiB、恰好50MiB、50MiB+1字节。
    - 覆盖伪装扩展、ZIP/RAR4/RAR5/7z 文件头和路径穿越。
    - 覆盖 0资源、1文件+3链接成功、第二文件/第四链接失败。
    - 覆盖永久、未来、过去和刚过期状态。
    - _Requirements: REQ-05—REQ-07, REQ-12_
    - _Dependencies: 5.1—5.6_

- [x] 6. 搜索与统计后端

  - [x] 6.1 实现主题检索
    - 标题、正文纯文本、教师姓名；类型、作者、创建时间和活动时间独立筛选。
    - 置顶和最后互动排序；分页上限。
    - _Requirements: REQ-08_
    - _Dependencies: 4.1, 2.4_

  - [x] 6.2 实现课程资源检索
    - 学段、年级、学期、课次、作者、日期筛选。
    - 课程标题精确/前缀/包含优先，再主题、反思、教师。
    - 分页 post 后批量资源项，禁止 N+1 和 JOIN 重复分页。
    - _Requirements: REQ-08_
    - _Dependencies: 5.4, 2.4_

  - [x] 6.3 验证查询计划和性能
    - 准备代表性本机数据或生成可清理测试数据。
    - 对常用筛选执行 `EXPLAIN`，确认结构化条件命中索引。
    - 记录 2 万资源下 P95；未达标先优化 SQL/索引，不引入 Elasticsearch。
    - 精确清理本次生成数据。
    - _Requirements: REQ-08, REQ-12_
    - _Dependencies: 6.1, 6.2_

- [x] 7. 通知后端

  - [x] 7.1 实现教师远程选择器查询
    - JOIN user/role/dept，限定启用 teacher 账号。
    - 支持姓名、学校、账号、手机号和可选学段，后端分页。
    - 返回姓名+学校+账号，禁止固定上限静默截断。
    - _Requirements: REQ-09_
    - _Dependencies: 2.4, 3.3_

  - [x] 7.2 实现学段接收人扩展
    - 同时按主学校和 `sys_user_dept` 多校关系选择目标学段的启用 teacher 账号。
    - 多账号分别通知；同主题+账号唯一去重。
    - 每批最多 500 行写入。
    - _Requirements: REQ-09_
    - _Dependencies: 7.1_

  - [x] 7.3 实现首次通知和再次通知
    - 主题新增与首次通知接收人同事务。
    - 主题编辑不自动通知。
    - 再次通知 upsert，重置未读、更新时间并允许新增接收人。
    - _Requirements: REQ-09, REQ-12_
    - _Dependencies: 7.2, 4.1_

  - [x] 7.4 实现当前教师通知接口
    - summary 返回有效未读总数和最多 5 条待办：定时通知到活动开始前持续显示，无时间通知仅显示未读。
    - 定时通知按活动时间升序优先；达到活动时间后移出首页但保留历史。
    - 全部通知分页、本人单条已读、本人全部已读。
    - JOIN 未删除主题，禁止读写他人 recipient ID。
    - _Requirements: REQ-09, REQ-10_
    - _Dependencies: 7.3_

  - [x] 7.5 编写通知测试
    - 覆盖小学+初中、多账号、指定教师重复 ID、停用账号。
    - 覆盖编辑不重发、再次通知重置、并发 upsert、已读幂等。
    - 覆盖软删除主题后通知不可见。
    - _Requirements: REQ-09, REQ-10, REQ-12_
    - _Dependencies: 7.1—7.4_

- [x] 8. Vue3 富文本和基础组件

  - [x] 8.1 以可选配置增强公共 Editor
    - 增加 `enableTable/uploadAction/allowedImageTypes/maxImageCount` 等 props。
    - 默认值必须与现状一致，避免影响现有使用者。
    - 增加中文表格菜单和 Quill 2 table 操作。
    - 组件卸载时移除 paste listener，避免重复绑定。
    - _Requirements: REQ-03_
    - _Dependencies: 1.3, 5.6_

  - [x] 8.2 新建 `ResearchRichEditor`
    - 传入专用图片上传、10 MiB、JPG/PNG/WebP、最多 20 张和表格开关。
    - 显示清晰上传/表格错误，不支持视频和 base64 图片。
    - _Requirements: REQ-03_
    - _Dependencies: 8.1_

  - [x] 8.3 新建 API 模块和纯工具函数
    - 主题、留言、multipart 资源、搜索、通知、blob 下载和访问计数。
    - 课程年级联动、课次组合、链接状态和请求转换放纯 JS 工具。
    - 为工具函数添加 `node --test` 测试，不引入 Vitest/Jest。
    - _Requirements: REQ-05—REQ-10_
    - _Dependencies: 4—7 后端接口稳定_

- [x] 9. Vue3 页面与交互

  - [x] 9.1 实现教研活动主页
    - 单一信息流，不出现板块。
    - 默认活动主题视图；提供课程资源检索。
    - 关键词、创建时间/活动时间双筛选、重置、防抖、分页和空状态。
    - 有活动时间的通知卡片在右上角显示活动时间。
    - 教师显示发起分享；教研员/管理员显示发布活动。
    - _Requirements: REQ-02, REQ-08_
    - _Dependencies: 6.1, 6.2, 8.3_

  - [x] 9.2 实现主题新建/编辑
    - 教师只能交流分享。
    - 教研员/管理员发布活动通知时选择学段或指定教师，通知统一进入教师首页。
    - 活动时间可选，填写时必须晚于当前时间，并在主题详情中回显。
    - 指定教师采用远程分页选择器，显示姓名+学校+账号。
    - 富文本表格、图片和校验完整。
    - _Requirements: REQ-02, REQ-03, REQ-09_
    - _Dependencies: 7.1, 8.2_

  - [x] 9.3 实现主题详情和三类留言
    - 主题正文、统计、筛选、最新排序和置顶资源。
    - 普通留言、活动纪实、课程资源切换。
    - 作者编辑/软删除；管理角色隐藏/恢复/置顶，不提供编辑他人正文按钮。
    - _Requirements: REQ-04, REQ-11_
    - _Dependencies: 4.2, 8.2_

  - [x] 9.4 实现一体化课程资源表单
    - 学段→年级、学期、数字/专题/复习课、标题和必填反思。
    - 一个主课件，文件选择后本地显示大小和 50 MiB 校验。
    - 最多三个云盘行；永久有效默认开启，关闭后要求过期时间。
    - multipart 上传进度、防重复提交、失败保留表单内容。
    - _Requirements: REQ-05—REQ-07_
    - _Dependencies: 5.4, 8.2, 8.3_

  - [x] 9.5 实现资源卡片和访问交互
    - 显示课程结构、反思摘要、作者学校、活动和更新时间。
    - 文件下载使用授权 blob 请求并保存原文件名。
    - 云盘显示永久/有效至/已过期，打开新窗口并可复制提取码。
    - 已过期链接仍可尝试访问。
    - _Requirements: REQ-06—REQ-08_
    - _Dependencies: 5.5, 9.4_

  - [x] 9.6 实现教师首页顶部通知栏
    - 在 `.teacher-dashboard` 中置于区域抽测评卷之前。
    - 独立加载，不阻塞课程 dashboard；空状态紧凑。
    - 有活动时间时到活动开始前持续显示，无活动时间时仅显示未读；展示活动时间和全部通知入口。
    - 点击标记已读后进入主题；失效主题友好提示。
    - _Requirements: REQ-10_
    - _Dependencies: 7.4, 8.3_

  - [x] 9.7 配置路由和菜单联动
    - SQL 提供一级动态菜单。
    - 添加主题详情和全部通知隐藏路由，限定三角色并设置 activeMenu。
    - 学生路由不增加任何入口。
    - _Requirements: REQ-01, REQ-10_
    - _Dependencies: 2.1, 9.1, 9.6_

- [x] 10. 配置、测试与发布候选

  - [x] 10.1 调整上传配置
    - Spring `max-file-size` 调整为 55MB，`max-request-size` 调整为 60MB。
    - 核对该核心配置变更只扩大解析上限，业务服务仍严格限制 50 MiB。
    - 记录生产 Nginx 3010 需要 `client_max_body_size 60m`。
    - _Requirements: REQ-06, REQ-12_
    - _Dependencies: 5.3_

  - [x] 10.2 执行后端测试和构建
    - `mvn -pl ruoyi-business -am test`。
    - `mvn -pl ruoyi-admin -am clean package`。
    - 修复所有与本模块相关失败；不顺手重构无关模块。
    - _Requirements: REQ-12_
    - _Dependencies: 2—7, 10.1_

  - [x] 10.3 执行前端测试和构建
    - 运行新增 `node --test` 纯函数测试。
    - `npm run build:prod`。
    - 检查构建产物未引入 WangEditor/Tiptap，除非已走确认回退路线。
    - _Requirements: REQ-03, REQ-12_
    - _Dependencies: 8—9_

  - [x] 10.4 完成接口角色矩阵验收
    - 教师、教研员、学生至少覆盖 requirements 的角色矩阵。
    - 测试内容归属、通知、软删除、附件、提取码和过期时间。
    - 保存脱敏报告，不写完整密码/token。
    - _Requirements: REQ-01—REQ-12_
    - _Dependencies: 10.2, 10.3_

  - [x] 10.5 完成浏览器冒烟
    - 教研员发布通知；教师首页顶部接收并跳转。
    - 教师发布纪实、课程资源、编辑、搜索、下载和云盘打开。
    - 主题/留言各做表格、多图、刷新回显和再次编辑。
    - 截图和报告存入 `output/playwright/`，不包含密码/token。
    - _Requirements: REQ-02—REQ-10_
    - _Dependencies: 10.4_

  - [x] 10.6 更新项目文档
    - 更新 `contexts/context.md` 版本、实现状态、表名、接口、配置和验收事实。
    - 更新本 `tasks.md` 复选框，不把未做事项标成完成。
    - 如设计发生变化，同步 `requirements.md/design.md` 和 ADR。
    - _Requirements: REQ-12_
    - _Dependencies: 10.5_

### 本机完成证据（2026-07-23）

- SQL：`research_activity_v1.sql` 已包含可空 `activity_time`；`research_activity_activity_time.sql` 已执行；`research_activity_multischool_notice_backfill.sql` 将主题 15 接收人从 25 补至 31，剩余缺失 0，已有已读状态未重置。
- 后端：`mvn -pl ruoyi-admin -am clean package -DskipTests` 通过，教研活动专项 32/32。
- 前端：纯函数测试 7/7，`npm run build:prod` 通过；多校通知、双时间筛选和卡片时间专项浏览器/API 冒烟 12/12，报告 `output/playwright/research-activity-multischool-filter-smoke.json`。
- 性能：20,000 条代表资源结构化筛选 P95 34.801 ms、关键词 P95 41.864 ms；精确标题优先，测试数据清理余量 0。
- 文档：`contexts/context.md` 已升级 v2.43，并同步 requirements/design/tasks 的多校投递与双时间筛选口径。
- 第 11 阶段仍未勾选：本轮提示词明确只授权本机开发，内网 3009/3010 发布须由后续新任务明确授权。

- [ ] 11. 内网发布（仅在开发完成并进入发布任务时）

  - [ ] 11.1 发布前只读检查和备份
    - 按 `AGENTS.md` 读取 10.52.1.123 当前数据库、Nginx、NSSM 和 release。
    - 备份目标库和相关配置到新备份目录，计算 SHA-256。
    - 核对磁盘空间足以存储课件和新 release。
    - _Requirements: REQ-12_
    - _Dependencies: 10.6_

  - [ ] 11.2 执行 SQL 和配置变更
    - 在正式库执行 `research_activity_v1.sql`，复核四表、索引、菜单和权限。
    - 修改 3010 对应 Nginx 配置前保存副本，设置 60m 并执行 `nginx -t`。
    - 不修改 80 端口主站和无关服务。
    - _Requirements: REQ-01, REQ-06, REQ-12_
    - _Dependencies: 11.1_

  - [ ] 11.3 发布新 release 并探活
    - 上传 jar/dist 到新 `releases/<时间戳>_<hash>/`。
    - 切换 NSSM 3009 和 Nginx 3010，保留上一 release。
    - 探活 API、菜单和静态资源。
    - _Requirements: REQ-12_
    - _Dependencies: 11.2_

  - [ ] 11.4 正式环境验收和回滚说明
    - 教师/教研员线上冒烟，重点复核通知顶部、50 MiB、下载、搜索和权限。
    - 汇报 HTTP 状态、表/菜单数量、构建哈希和截图，不泄露凭据。
    - 写明切回旧 release 的命令/路径；应用回滚保留新业务表，不执行 DROP。
    - _Requirements: REQ-01—REQ-12_
    - _Dependencies: 11.3_

## 4. 最低验收用例清单

| ID | 用例 | 预期 |
| :--- | :--- | :--- |
| UAT-01 | 教师登录查看菜单 | 可见教研活动，不可见通知发布控制 |
| UAT-02 | 学生访问列表/详情/下载 | 401/403，无信息泄露 |
| UAT-03 | 教研员按小学+初中发布活动通知 | 两学段启用教师账号各一条未读 |
| UAT-04 | 教师首页打开 | 通知栏为第一内容块，最近通知优先 |
| UAT-05 | 点击通知 | 标记已读并直达主题 |
| UAT-06 | 教师发布普通留言和活动纪实 | 成功，类型/图片/时间正确 |
| UAT-07 | 发布数字课课程资源 | 必填字段、反思、文件/链接同卡显示 |
| UAT-08 | 发布专题课和复习课 | 不要求数字课次，搜索筛选正确 |
| UAT-09 | 上传恰好 50 MiB ZIP | 成功 |
| UAT-10 | 上传 50 MiB+1 字节 | 拒绝并提示改用云盘 |
| UAT-11 | 伪装压缩包 | MIME/签名校验拒绝 |
| UAT-12 | 永久云盘链接 | 显示永久有效，可打开/复制提取码 |
| UAT-13 | 有效期云盘到期 | 自动显示已过期但仍可尝试打开 |
| UAT-14 | 第四个云盘链接 | 前后端均拒绝 |
| UAT-15 | 搜索“三年级上学期第5课+标题” | 目标课程资源优先出现 |
| UAT-16 | 作者编辑资源 | 显示已编辑，文件/链接组合仍合法 |
| UAT-17 | 教研员尝试编辑他人反思 | 无编辑入口，接口 403 |
| UAT-18 | 教研员隐藏资源 | 列表/搜索/下载立即不可见，可恢复 |
| UAT-19 | 主题持续留言 | 无开启/关闭状态，创建后一直可用 |
| UAT-20 | 富文本表格保存刷新再编辑 | 表格、图片、链接完整，无脚本执行 |

## 5. 完成定义

只有同时满足以下条件才可标记任务完成：

- requirements 中全部必须需求均有对应代码和验收证据。
- 四张表、索引、菜单和权限的幂等 SQL 演练通过。
- 后端单测和 clean package 通过。
- Vue3 纯函数测试和生产构建通过。
- 教师、教研员、学生权限矩阵通过。
- 50 MiB、文件签名、HTML XSS、云盘过期、通知已读和搜索有专项验证。
- 浏览器完成教研员发布 → 教师首页接收 → 跳转 → 资源发布/搜索/下载的闭环。
- 未修改旧 Vue2、未迁移旧论坛、未引入未批准依赖、未覆盖无关热修。
- `contexts/context.md` 与实际实现一致，并明确剩余风险和发布/回滚方式。
