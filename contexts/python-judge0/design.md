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

## Python 刷题架构（已上线）

### 2026-08-19 交互修正

`entry_year` 是数据层必要字段，表示入学年份，不是“届别”。普通教师不直接填写它：服务端以 7 月 20 日为学年切换点，结合 `school_type` 和入学年份计算当前绝对年级，再按“绝对年级 + 两位班号 + 班”生成自然班名，例如初中 2024 级 1 班在 2026—2027 学年显示为 `901班`，小学同条件显示为 `301班`，高中显示为 `1201班`。未来入学和已毕业范围从教师可选班级中排除。题库选择器固定查询 `question_type=practical`、`practical_mode=PYTHON`。

刷题是独立业务域，不复用课程、课程题目或学生答案表。它只复用现有 Python 题目、题目测试点、草稿编辑器、Judge0 客户端和判题状态映射。

### V1 历史模型（已退出业务查询）

1. 年级基础题单：以学校、学期、入学届别为范围，一次发布给该年级全部班级。任一时间只有一个当前发布版本；新版本发布后保留旧版本与学生历史的关联。
2. 班级加练包：从同一个年级基础题单派生，包含名称、题目和目标班级集合。一个包可发给多个班，一个班也可同时收到多个包。
3. 学生可见题目：当前已发布基础题单，与该学生班级命中的全部已发布加练包取并集；前端分区展示，但后端按题单项来源核验，不能由前端传入班级或包标识绕过范围。

### 2026-08-22 已实施的统一题单模型

上述“年级基础题单 + 班级加练包”是 V1 历史，不再作为产品模型。当前只保留一种“练习题单”：每个题单版本显式选择一个或多个教师可管理班级，全年级通过班级全选表达；同一班级可收到多个题单，额外练习通过新建另一个普通题单表达。服务端按教师管理班级和学生本人班级重新核验，不能信任前端传入范围。

已发布题单保持不可变。首次修改时复制目标班级、题目快照、快照测试点和题目顺序到新草稿版本；学生继续访问旧发布版本，教师重新发布后才切换。删除采用显式确认后的物理删除：在同一业务操作中清理提交逐点结果、提交、草稿、进度、快照测试点、题目快照、题单题目、目标班级、版本和主题单；不保留归档状态、恢复接口或“已删除题单”页面。

选题工作台沿用课程设计器“题单设置与已选题 / 题库”左右双栏心智，但不复用课程分值、年级、学期和课次规则；Python 侧增加知识点、难度、验证状态筛选，以及批量加入、预览、排序和移除。

### 当前持久化对象

- `biz_python_practice_plan`：练习题单主题，保存学校、名称、状态、当前发布版本和创建者；允许同一学校/届别存在多份题单。
- `biz_python_practice_plan_version`：不可变发布版本和可编辑草稿版本。
- `biz_python_practice_plan_class`：题单版本和一个或多个目标班级的关联，使用 `dept_id + entry_year + class_code`。
- `biz_python_practice_plan_question`：题单版本与 Python 题目的关联，保存排序、阶段、必做标识和题目快照。
- `biz_python_practice_question_snapshot` / `biz_python_practice_snapshot_case`：发布时冻结题面、配置与测试点。
- `biz_python_practice_extension*`：仅保留空物理表用于 V1 回滚兼容，不再参与当前查询或写入流程。
- `biz_python_practice_submission`：每次刷题运行或正式提交，保存学生、题目来源、代码、状态、分数、测试汇总与幂等键；不写入课程成绩表。
- `biz_python_practice_progress`：按学生、题单项保存最高得分、通过状态、提交次数、首次通过和最近练习时间，支撑班级和学生统计。

题目关系通过发布题面快照避免教师后续编辑共享题库题目后，已发布练习的题面和判题规则悄然变化；现有字段、唯一约束和查询索引已由 `sql/python_practice_unified_plan_v2.sql` 落地并通过重复执行后检。

### 访问与统计

教师访问范围复用 `biz_teacher_class` 的学校、入学年份、班级边界，题单只能选择教师可管理且当前仍在校的班级。管理员和教研员按既有权限查看全范围。统计统一按题单版本、目标班级、学生和题目汇总，目标学生包含零提交学生；列表、详情和学情 DTO 均由服务端下发统一 `classLabel`，前端不自行猜测届别或年级。前端展示目标人数、已开始、全部通过、完成率、提交次数、学生明细和薄弱题，不再区分基础题与加练题。

## 正式落地状态与门禁

当前实现已在 V1 独立刷题表基础上执行 `sql/python_oj_modernization_v1.sql` 与 `sql/python_practice_unified_plan_v2.sql`。统一题单查询只经过题单、发布版本、版本班级和题目快照；发布前要求每题至少有公开和隐藏测试点。学生请求按本人学校、届别和班级重新核验。

判题线程有最大轮询次数；达到上限或 Judge0 异常时提交标记为 `SERVICE_ERROR`，保留代码和提交历史，不写课程答案或零分。开发库和正式库均已完成备份与迁移。正式系统题 V2 为 120 题/720 点，生产 Judge0 全量 720/720 通过；教师建题单、选 V2 题、发布、学生可见及硬删除清理的正式 API 冒烟通过，`biz_student_answer` 未新增。回滚保留旧 release、Nginx/NSSM 配置和整库备份；尚未做真实整班同时提交压测。

## 2026-08-22 已确认：OJ 化目标设计

- 题库保持统一，不增加用途字段；Python 专用标题、知识点、内容版本和验证状态放在编程配置侧，课程/刷题由现有关联表表达。配置侧的可空唯一 `external_id` 仅为系统题提供 `PYV2-xxx` 稳定标识，教师自建题保持为空。
- 学生端采用可拖动三窗格；自定义运行只执行学生输入，不访问隐藏用例、不更新进度。
- 新增独立刷题逐测试点结果持久化；公开用例可返回期望/实际，隐藏用例只返回状态、耗时和内存。
- 课程 Python 与独立刷题共用后端输出比较器：统一换行、删除行尾空白和文末空行，其余严格比较。
- 题目修改后验证状态失效；参考代码经 Judge0 全用例通过后才能加入课程或题单。
- 教师导入使用双 Sheet Excel，系统题生产使用受控 JSON；两者均先预检、后确认、事务写入。
- 详细字段、UI、接口、题库生产、V1 清理、验收和回滚见 `python-oj-modernization-final-plan.md`。
- 正式环境已实现配置元数据和验证状态、快照元数据、`CUSTOM_RUN`、`biz_python_practice_submission_case`、统一输出比较器、教师三步出题/双 Sheet 导入、学生可拖动三窗格。迁移脚本为 `sql/python_oj_modernization_v1.sql` 和 `sql/python_practice_unified_plan_v2.sql`；回滚脚本只用于尚未产生同范围多题单等新业务数据的环境。
- 双 Sheet 在浏览器解析为受控 DTO；服务端完成结构校验和参考代码全用例验证后，将原请求以“用户 ID + 随机令牌”在 Redis 保存 30 分钟。确认接口使用短锁防重复并在单事务中写入题目、配置和测试点，只有提交成功后才删除令牌。
- Python 题普通删除先检查课程关联、课程提交、刷题题单/快照/草稿/提交/进度；存在依赖时拒绝。无依赖时在同一事务内依次删除测试点、编程配置和题目，避免孤儿数据。
- 推荐题单查询只返回公开、启用、`VALID`、`external_id` 非空，且编程配置记录 `create_by=python-system-v2` 的未选题目；统一题目记录的创建者是真实人员“郑东旭”。服务层按目标数量约 50% 简单、40% 中等、余量困难分配，并优先选取尚未覆盖的主知识点。加入题单继续复用既有 `addQuestion` 与快照事务，V2 无候选时直接拒绝而不回退 V1。
- Python 题的 `grade`、`semester`、`lesson_num` 保存为空；统一题库的 Python 视图和编辑表单不展示这些课程属性。旧 V1 80 题及依赖已由 `sql/python_practice_polish_v3.sql` 在整库备份后物理清理，V2 题保持 120 题/720 点。
- 系统题 JSON 入库前使用 `tools/python_oj_validate.py` 做字段、乱码、重复、危险代码和本机 Python 全用例校验；`tools/python_oj_build_import_sql.py` 生成按稳定题号幂等、文本十六进制、事务回滚和后检齐全的 SQL。正式导入前已通过生产 Judge0 120 题/720 点全量验证，导入脚本多次复跑仍保持 120 个唯一题号和 720 个测试点。
- Judge0 HTTP 客户端对代码、输入、期望输出以及响应文本统一使用 UTF-8 Base64；解码采用 MIME Base64 以容忍 Judge0 在编码文本末尾追加的换行，避免中文输出被拒绝或误判。
