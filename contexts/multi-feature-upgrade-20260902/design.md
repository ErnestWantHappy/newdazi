# 2026-09-02 多功能改造总体设计

## 1. 总体策略

采用“先修正确性，再建共享状态底座，最后改协作模型”的增量路线。P0 热修不等待分组大功能；通用分组和学生桌面先独立交付；在线协作只读取已经稳定的课时分组快照，避免在一个版本中同时重写课程、分组、房间、成绩和实时通信。

```text
学生端公共布局
  ├─ Presence WebSocket ─> 在线状态服务 ─> Redis TTL ─> 学生桌面
  ├─ 作答/提交 API ──────> 权威答案/任务状态 ─> 事务后事件 ─> 教师相关页面
  └─ 协作会话 ──────────> 课时分组快照 ─> 小组房间 ─> revision ─> 异步版本差异

教师端
  ├─ 班级管理 -> 学生桌面（主入口）
  ├─ 教师首页 -> 当前课程/班级学生桌面（快捷入口）
  ├─ 批改页 -> 数字/星级 + 提交时评分快照
  └─ 协作活动 -> 多任务版本 + 小组映射 + 学生轨迹
```

## 2. 学生桌面入口与 IP 结论

### 2.1 推荐入口

主入口不新增一级菜单，也不把现有“班级管理”菜单改成父目录。在 `RuoYi-Vue3/src/views/business/teacherClass/index.vue` 的“我管理的班级”操作列中，在“学生管理”旁增加“学生桌面”。路由使用隐藏动态路由 `/business/classroom-desktop`，参数为 `entryYear`、`classCode`。

教师首页在具体课程班级上下文中增加第二入口，携带 `lessonId + entryYear + classCode`，进入后自动选中当前课程，直接看到在线和作业状态。

选择该结构的原因：

- 学生桌面首先是班级终端视图，不属于某一道题或某一个协作文档。
- 教师必须先管理该班级，入口天然具备权限语境。
- 不调整现有一级菜单顺序，不破坏帮助中心“班级管理”的既有教程和路由。
- 教师上课又可以从当前课程一键进入，无需重新选班。

### 2.2 为什么当前不能直接显示每台电脑 IP

普通浏览器不能稳定读取电脑真实局域网 IP 或 Windows 计算机名。当前 `ClassroomWebSocketHandshakeInterceptor` 只保存房间、用户、教师标识和课程，没有提取 IP；`ClassroomWebSocketHandler` 也只保存进程内连接，不提供教师在线查询模型。因此当前页面无法展示学生终端 IP。

平台现有 `IpUtils` 已能读取 `X-Forwarded-For`、`X-Real-IP` 和远端地址，历史操作日志也出现过真实 `10.52.*` 来源地址，说明服务端链路具备取得连接来源 IP 的基础。但新功能必须：

1. 在 WebSocket 握手时由服务端提取地址；
2. 确保 3010 Nginx 的 `/ws/` 转发写入 `X-Real-IP` 和 `X-Forwarded-For`；
3. 只信任配置白名单中的代理，防止客户端伪造头部；
4. 把它命名为“连接 IP”。若学校网络使用 NAT、VPN 或多级网关，多台电脑可能显示同一出口 IP，这是网络事实，不应伪装成独立电脑 IP。

## 3. 组件设计

| 组件 | 类型 | 职责 | 不负责 |
| --- | --- | --- | --- |
| `QuestionIntegrityService` | 后端服务 | 题型字段规范化、导入前置校验 | 页面展示 |
| `DiagnosisClassificationService` | 后端服务 | 业务拒绝、系统异常、慢接口分级 | 吞掉未知异常 |
| `SharedLessonReadService` | 后端服务 | 共享课程只读授权和白名单 DTO | 修改课程/题库 |
| `PracticalGradingService` 扩展 | 后端服务 | 快照解析、星级重算、总分锁定 | AI 自动写正式分 |
| `StudentTaskStateService` | 后端服务 | 统一作业状态事实和事务后事件 | 在线终端状态 |
| `PresenceWebSocketHandler` | WebSocket | 学生连接、设备、IP、心跳 | 考勤、远控 |
| `StudentPresenceStore` | Redis 服务 | 在线 TTL、多设备聚合 | 长期审计全文 |
| `ClassGroupingService` | 后端服务 | 分组方案、成员、组长、快照 | IoT 实验专用分组 |
| `ClassroomLayoutService` | 后端服务 | 教师个人班级网格布局 | 改变分组成员 |
| `CollaborationActivityService` | 后端服务 | 独立活动、任务版本、小组映射 | 成绩和个人答案 |
| `CollaborationTraceService` | 后端服务 | 会话/保存事件与轨迹查询 | 声称精确字符作者 |
| `CollaborationDiffWorker` | 异步工作器 | 相邻 Office revision 结构化差异 | 阻塞文档保存 |
| `ClassroomDesktopView` | Vue3 页面 | 终端/作业视图、布局、分组、轨迹抽屉 | 点名、远控 |

## 4. 数据模型

### 4.1 P0/P1 小改字段

- `biz_student_answer.score_input_mode varchar(16) NULL`：`NUMERIC`、`STAR_TOTAL`、`STAR_ITEM`；历史空值按 `NUMERIC` 展示。若不希望为辅助交互迁移字段，可把该项降为审计日志，但推荐保留以便解释成绩来源。
- `biz_student_task_state`：统一记录课程题目级课堂状态。
  - 业务键：`lesson_id + question_id + student_id`
  - 字段：`state`、`state_version`、`entered_time`、`started_time`、`submitted_time`、`graded_time`、`returned_time`、`update_time`
  - 状态只允许单向或显式重交流转；重复事件幂等。

### 4.2 通用分组

1. `biz_class_group_scheme`
   - `scheme_id`、`dept_id`、`entry_year`、`class_code`、`scheme_name`、`scheme_version`、`status`、`creator_user_id`、审计时间。
2. `biz_class_group`
   - `group_id`、`scheme_id`、`group_no`、`group_name`、`color`、`sort_no`、`leader_student_id`。
3. `biz_class_group_member`
   - `scheme_id`、`group_id`、`student_id`、`sort_no`。
   - 唯一约束：`scheme_id + student_id`，防止一个方案内重复入组。
4. `biz_lesson_group_snapshot`
   - `snapshot_id`、`lesson_id`、`dept_id`、`entry_year`、`class_code`、`source_scheme_id`、`source_scheme_version`、`snapshot_hash`、`frozen_time`。
5. `biz_lesson_group_snapshot_group` / `biz_lesson_group_snapshot_member`
   - 完整复制组名、顺序、成员和组长事实，禁止运行时反查可变模板。

通用分组不复用 `biz_iot_group` 和 `biz_iot_group_student`。IoT 表与实验配置绑定，生命周期和权限不同。

### 4.3 学生桌面布局与在线状态

1. `biz_classroom_layout`
   - 业务键：`teacher_user_id + dept_id + entry_year + class_code`
   - 字段：列数、布局版本、更新时间。
2. `biz_classroom_layout_item`
   - `layout_id + student_id` 唯一，保存 `grid_row`、`grid_col`、`sort_no`。
3. Redis 当前在线状态
   - `presence:device:{sessionId}`：学生、学校、班级、设备 ID、连接 IP、最近心跳，TTL 60 秒。
   - `presence:student:{studentId}`：活跃 sessionId 集合。
   - 连接关闭主动删除；服务异常时由 TTL 自动过期。

不把每个 30 秒心跳写 MySQL。需要历史在线时长时，只保存会话开始/结束摘要；协作活动自己的轨迹使用专门表。

### 4.4 独立协作活动

1. `biz_collab_activity`
   - `activity_id`、`lesson_id`、`title`、`status`、`freeze_time`、创建人和审计时间。
2. `biz_collab_task_variant`
   - `variant_id`、`activity_id`、`variant_name`、`source_material_id/path`、文件元数据、顺序和摘要。
3. `biz_collab_group_assignment`
   - `activity_id + snapshot_group_id` 唯一，映射 `variant_id`。
4. 扩展 `biz_collab_room`
   - 增加可空 `activity_id`、`group_snapshot_id`、`snapshot_group_id`、`variant_id`。
   - 新模式唯一键：`activity_id + snapshot_group_id`；旧 `lesson + question + class` 房间继续兼容。
5. `biz_collab_member_route_audit`
   - 记录课中换组的学生、原组、新组、教师、原因和时间。

### 4.5 操作轨迹与版本差异

1. `biz_collab_session`
   - 房间、学生、设备哈希、连接 IP、进入、最后心跳、离开、结束原因。
2. `biz_collab_activity_event`
   - `ENTER`、`LEAVE`、`RECONNECT`、`SAVE_TRIGGERED`、`SAVE_SUCCESS`、`SAVE_FAILED`、`GROUP_CHANGED`。
3. `biz_collab_revision_diff`
   - 当前 revision、上一 revision、状态、格式、摘要 JSON、统计 JSON、错误摘要、开始/结束时间。

差异记录属于小组文档版本，不保存“该差异作者=保存触发者”。保存接口先提交 revision，再异步投递差异任务。

## 5. API 与 WebSocket 契约

### 5.1 共享课程

- `GET /business/lesson/{lessonId}/shared-view`
  - 返回课程基本信息、当前教师可见班级、已引用题目只读 DTO 和能力对象。
  - `capabilities={canViewContent:true,canDesign:false,canDelete:false,canCopy:false}`。
- 不能复用设计器更新接口；所有修改接口继续执行管理权校验。

### 5.2 星级评分与快照

- `GET /business/teacher/grading/scoring-items?lessonId=&questionId=&practicalVersionId=`
- 保存请求增加：

```json
{
  "practicalVersionId": 123,
  "rubricSnapshotId": 176,
  "mode": "STAR_TOTAL",
  "starCount": 4,
  "score": 56,
  "details": []
}
```

服务端按题目/评分项上限重新计算星级分，校验请求中的 `score` 只用于冲突提示，不作为可信结果。

### 5.3 统一作业状态

- `GET /business/classroom-state?lessonId=&entryYear=&classCode=`：返回全班学生状态、版本和汇总。
- WebSocket 事件：

```json
{
  "type": "student_task_state_changed",
  "lessonId": 293,
  "questionId": 2029,
  "studentId": 1001,
  "state": "SUBMITTED",
  "stateVersion": 8,
  "changedAt": "2026-09-02T19:00:00+08:00"
}
```

事件在数据库事务成功提交后发布，页面按 `stateVersion` 丢弃旧消息并周期性全量校准。

### 5.4 学生在线状态

- `WS /ws/presence`：使用 `Admin-Token` Cookie 认证；只有学生建立在线设备状态，教师连接只订阅有权限班级。
- 学生消息只允许 `heartbeat` 和受控页面状态，不接受自报学校、班级、IP。
- `GET /business/classroom-desktop/{entryYear}/{classCode}`：教师读取班级学生、聚合在线设备、布局、分组和当前课程状态。
- `PUT /business/classroom-desktop/{entryYear}/{classCode}/layout`：乐观锁保存教师个人布局。

### 5.5 分组与协作

- `/business/class-group/schemes`：分组方案 CRUD、平均分组、区间分组、成员移动、组长设置。
- `POST /business/lesson/{lessonId}/group-snapshots`：按班级生成/确认课时快照。
- `/business/collaboration/activities`：活动、任务版本和小组映射管理。
- `GET /business/collaboration/student/current`：只返回本人小组开放房间。
- `GET /business/collaboration/room/{roomId}/students/{studentId}/timeline`：教师读取学生事件时间线和关联版本差异。

## 6. 关键数据流

### 6.1 在线终端

```text
学生登录 -> 公共布局建立 /ws/presence
-> 服务端由登录态解析 student/dept/class
-> 由可信代理链解析连接 IP
-> Redis 写入 device TTL
-> 教师学生桌面初次 REST 拉取
-> presence_changed 推送局部变化
-> 60 秒无心跳自动离线
```

### 6.2 提交实时更新

```text
学生打开题目 -> UPSERT ENTERED
首次保存 -> UPSERT WORKING
正式提交事务 -> 答案/作品版本 + SUBMITTED 同事务
事务 afterCommit -> 班级 WebSocket 推送
教师页面按版本局部合并 -> 定时 REST 校准
```

### 6.3 分组多文档协作

```text
教师选择班级分组方案 -> 生成课时快照
-> 建立协作活动 -> 上传/导入多个任务版本
-> 自动映射小组并由教师确认
-> 为每组复制独立起始文件、创建房间
-> 学生登录按快照找到本人组 -> 只返回该组房间
-> 首人进入冻结活动
```

### 6.4 文档变化摘要

```text
浏览器 onSave -> 保存完整文档并追加 revision
-> 记录触发学生和保存结果
-> 异步队列比较 previous/current
-> POI 结构化提取 + 页图/对象摘要
-> 保存 group-level diff
-> 学生轨迹按一分钟聚合展示
```

## 7. 各问题的最小实现点

### 7.1 线上诊断

- `BizQuestionServiceImpl.processQuestionByType` 和 `BizQuestionMapper.xml` 不再对非图片模式写 NULL，统一保留 10。
- 题库导入先解析全部行和必填字段，校验通过后才批量/逐行写入；事务失败整批回滚。
- 诊断不要继续无限扩充中文字符串匹配。引入可识别的业务错误类别或错误码，`DiagnosisAdvisor` 根据类别分级；未知数据库异常保持严重。
- 下一次发布把 NSSM stdout/stderr 改到稳定目录，例如 `D:\program\3009dazipingtai\logs\backend\`，日志轮转与 release 切换解耦。

### 7.2 评分

- 先修 `RuoYi-Vue3/src/api/business/scoringItem.js` 丢失 `practicalVersionId`，再开发星级 UI。
- 后端锁住当前提交版本和快照，前端任何模式都不能绕过现有并发/期限校验。
- 已有提交后锁定课程题目总分；评价标准文字和权重修改生成新快照。

### 7.3 流程图

- 开始/结束从椭圆模型改为胶囊矩形模型，半径取高度一半。
- 处理矩形 `radius=0`；输入/输出平行四边形保持直角。
- 默认锚点可见半径约 6～7，悬浮/命中半径约 12～14；以 100%、125%、触摸板完成实测后确定最终值。

### 7.4 指派班级

- 删除教师首页跳转设计器时传入全年级 `classes` 的逻辑。
- 新建默认空；只有明确来自某个班级的入口才传单个 `presetClass`；编辑只使用服务端已保存数据。

## 8. 错误处理与降级

- Presence WebSocket 断开：卡片在 TTL 后离线；重连后重新全量获取，不影响登录和作答。
- 实时提交推送失败：权威数据已入库，页面轮询校准恢复。
- IP 不可取得：显示“未知/代理地址”，仍正常显示在线。
- 文档差异失败：revision 保留，显示“版本已保存，差异暂不可用”，支持重试。
- POI 遇到加密、宏或超限文档：不展开内容，记录安全错误摘要，不阻塞保存。
- 分组映射不完整：禁止发布活动，并列出未映射小组。
- 课中换组：事务更新路由并写审计；任何一步失败保持原组访问。

## 9. 性能与安全门禁

- 50 人班级在线卡片首次加载目标小于 1 秒，推送后 2 秒内更新。
- 400 个学生连接验证成功率 100%，心跳不得每次访问 MySQL。
- 差异工作器独立线程池，建议初始并发 2、队列有界、单任务超时 60 秒；以真实 50MB 上限文件压测后再调整。
- 轨迹文本片段默认折叠并截断；只有课程创建者、管理员或负责该班级的教师可读取。
- 代理信任列表必须外置配置；不能无条件信任公网客户端传入的 `X-Forwarded-For`。

## 10. 迁移与回滚原则

- P0 题目 NULL 和前端快照修复本身不需要 SQL，可优先独立发布。
- 星级模式字段、任务状态、分组、布局、协作活动、轨迹表分别使用独立幂等 SQL，不合并成一个不可拆回的大脚本。
- 新增协作字段先全部可空，回填旧房间为历史全班模式并后检，再启用新代码；不重写历史 `public_file_id`、路径、密钥或 revision。
- 应用回滚时兼容新增表/可空列保留；不可删除发布后产生的分组、活动和文档。
- 正式发布每阶段单独备份、记录 SHA-256、release 路径、NSSM/Nginx 切换点和平台更新记录。
