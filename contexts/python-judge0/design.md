# Python 在线编程与 Judge0 CE 设计

## 数据边界

`biz_question.question_type=practical` 继续是统一题库入口，`practical_mode=FILE/PYTHON` 区分文件作品和在线编程；`biz_lesson_question` 继续保存课程分值。五张 `biz_programming_*` 表只保存 Python 配置、测试点、草稿和不可变提交历史。正式判题终态才更新既有 `biz_student_answer`，因此 Judge0 不可用不会把已有成绩变为零分。成绩汇总既有的操作题统计直接覆盖两种作答方式。

学生课程 DTO 必须下发 `practicalMode`：学生页以题目为单位渲染 `FILE` 上传作品卡片和 `PYTHON` 编辑器，两类题可在同一操作题区共存。成绩汇总保留 `practicalScore` 与 `totalScore`，可选返回 `filePracticalScore`、`pythonPracticalScore` 供学生历史成绩和教师成绩详情展示；旧调用方继续只读取原字段。

学生编程详情使用公开测试点 DTO，仅返回测试点名称、输入和期望输出；学生历史提交使用脱敏 DTO，不返回 Judge0 token、请求 IP、平台内部异常摘要或隐藏测试点数据。教师配置可选保存 `input_description`、`output_description`、`sample_explanation`、`constraints_text`、`notes_text` 五个说明字段，均不改变题型、成绩或权限模型。

为兼容历史 DTO 或缓存漏传作答方式，学生页仅对 `practicalMode` 为空的操作题调用既有平台接口 `GET /business/student-home/programming/{lessonId}/{questionId}`。该接口仍执行学生、当前课程、学校和题目作答方式校验，只有返回已启用 Python 配置时才在页面内补为 `PYTHON`；普通文件题的请求失败被视为 `FILE`。这是一层显示兼容，不增加 Judge0 浏览器入口，也不改变题目或成绩数据。

## 判题流程

1. 学生接口核对登录学生、当前课程、学校、届别、班级与题目关联。
2. 限流通过后，先保存提交和代码为 `WAITING`，再由异步工作线程领取。
3. 工作线程逐个测试点调用 Judge0、轮询 token，并且不向学生返回隐藏测试的输入、期望输出或程序输出。
4. 汇总为业务状态和按权重计算的整数分；正式终态通过事务写回 `biz_student_answer`。
5. 远端超时、不可用或协议异常标为 `SERVICE_ERROR`，保留提交以供重试。

## 部署边界

Judge0 使用独立 Compose 项目、独立 PostgreSQL/Redis 数据卷和独立 Docker 网络。API 发布在扩展服务器 `2358`，平台 Java 后端通过外置 `JUDGE0_BASE_URL`、`JUDGE0_AUTH_TOKEN` 调用，UFW 与 Judge0 IP 白名单仅允许 `10.52.1.123`；学生浏览器没有 Judge0 地址。Judge0 配置禁用代码网络、附加文件、命令行和编译参数，并在服务、容器和单提交三级限制 CPU、内存、进程、文件和输出。

部署模板固定 `judge0/judge0:1.13.1`、`postgres:16.2`、`redis:7.2.4`，服务日志使用 `json-file` 轮转，systemd 管理自启动，备份脚本同时保存 PostgreSQL dump、Compose 配置并计算 SHA-256。Judge0 1.13.1 的 isolate 依赖 cgroup v1；扩展服务器已通过 `systemd.unified_cgroup_hierarchy=0` 切换并重启，`/sys/fs/cgroup/memory` 已验证为 cgroup v1，真实 Python 执行已返回 Accepted。回滚通过恢复上一镜像 digest 或将平台 `JUDGE0_MODE=disabled`，不删除 CryptPad 资源。

## 待开发：Python 刷题架构

### 2026-08-19 交互修正

题单的届别仍是数据层必要字段，但不再由普通教师手填：创建题单时仅提交年级和名称，服务端以 7 月 20 日为学年切换点，并按小学一年级/初中七年级两个入学起点推算 `entry_year`，学期固定为 `0`。题库选择器固定查询 `question_type=practical`、`practical_mode=PYTHON`；班级学情先从教师管理班级列表选择班号，再查询统计接口。

刷题是独立业务域，不复用课程、课程题目或学生答案表。它只复用现有 Python 题目、题目测试点、草稿编辑器、Judge0 客户端和判题状态映射。

### 题单层次

1. 年级基础题单：以学校、学期、入学届别为范围，一次发布给该年级全部班级。任一时间只有一个当前发布版本；新版本发布后保留旧版本与学生历史的关联。
2. 班级加练包：从同一个年级基础题单派生，包含名称、题目和目标班级集合。一个包可发给多个班，一个班也可同时收到多个包。
3. 学生可见题目：当前已发布基础题单，与该学生班级命中的全部已发布加练包取并集；前端分区展示，但后端按题单项来源核验，不能由前端传入班级或包标识绕过范围。

### 推荐持久化对象

- `biz_python_practice_plan`：基础题单，保存 `dept_id`、`semester`、`entry_year`、版本、状态、发布信息和创建者。
- `biz_python_practice_plan_question`：基础题与现有 Python 题目的关联，保存排序、阶段、必做标识和题目快照版本。
- `biz_python_practice_extension`：班级加练包，关联基础题单，保存名称、状态、发布信息和创建者。
- `biz_python_practice_extension_class`：加练包和目标班级的关系，使用 `dept_id + entry_year + class_code`，支持一个包多个班。
- `biz_python_practice_extension_question`：加练包的追加题目和排序。
- `biz_python_practice_submission`：每次刷题运行或正式提交，保存学生、题目来源、代码、状态、分数、测试汇总与幂等键；不写入课程成绩表。
- `biz_python_practice_progress`：按学生、题单项保存最高得分、通过状态、提交次数、首次通过和最近练习时间，支撑班级和学生统计。

题目关系需要存题目版本或题面快照标识，避免教师后续编辑共享题库题目后，已经发布的练习题面和判题规则悄然变化。最终字段、外键和索引必须在实施前核对现有主键命名后再确定。

### 访问与统计

教师访问范围复用 `biz_teacher_class` 的学校、届别、班级边界。基础题单的配置要求教师对该年级有管理范围；加练包只能选择教师可管理的班级。管理员和教研员按既有权限查看全范围。统计先按学生、题目来源汇总，再计算班级完成率、正确率和薄弱题目，基础题与加练包分开呈现，避免把不同练习量的班级混为同一指标。

## 本地落地状态与门禁

当前实现已落地 `sql/python_practice_v1.sql` 对应的 11 张表、快照测试点复制、独立提交/进度更新、教师与学生控制器、CodeMirror 6 页面和菜单路由。已在开发库执行迁移并导入 80 道系统题；本机验证题单计划 1/版本 1 已发布。加练题写入前按基础题及同班已发布加练包检查重复，发布前要求每题至少有公开和隐藏测试点；学生请求仍通过已发布题单和本人班级范围重新核验。

判题线程有最大轮询次数；达到上限或 Judge0 异常时提交标记为 `SERVICE_ERROR`，保留代码和提交历史，不写课程答案或零分。开发库和正式库均已完成备份、迁移、80 题导入；正式 release 已切换到 `20260819_python_practice_v1`，真实 Judge0 HTTP 单题及 3 路低并发均通过，正式教师/学生浏览器冒烟通过。正式验收数据已按精确范围清理，`biz_student_answer` 未新增。回滚保留 Nginx/NSSM 配置和旧 release；尚未做高并发压测。
