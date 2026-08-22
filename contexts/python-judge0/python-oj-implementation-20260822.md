# Python 刷题 OJ 化本机实施记录（2026-08-22）

## 结论

方案 1 已完成开发并发布到 `10.52.1.123`：统一 Python 题库、统一练习题单、教师三步出题/双 Sheet 导入、课程设计器式选题、学生三窗格 OJ、教师学情和 120 道 V2 系统题均已上线。生产 Judge0 对 120 题、720 个测试点全量验证通过。旧 V1 80 题因课程 276 仍在使用其中 10 题而暂时保留，但推荐与新系统题选择已只使用 V2。

## 数据与后端

- `sql/python_oj_modernization_v1.sql` 新增编程配置稳定外部题号/标题/知识点/无输入/验证状态/内容版本，题单快照元数据，刷题提交自定义输入，以及逐测试点表 `biz_python_practice_submission_case`。`external_id` 仅供系统题使用并设可空唯一索引，教师自建题保持为空。
- 课程 Python 与独立刷题共用 `OutputComparator`：统一 CRLF/CR/LF，删除每行末尾空格和 Tab，删除文末空行，其余严格比较。
- `CUSTOM_RUN` 仅运行学生输入，不计分、不更新进度；`RUN` 只跑公开样例，`SUBMIT` 跑全部测试点。
- 隐藏测试点接口只返回状态、耗时和内存；不返回输入、期望、实际输出和错误详情。
- 题目或配置保存后验证状态回到 `DRAFT`；教师参考代码全用例通过后标为 `VALID`。课程和刷题题单都拒绝未验证题。
- Python 题被课程、题单、快照、草稿、提交或进度引用时禁止普通删除；未使用题按测试点、编程配置、题目同事务清理。

## 教师端

- 题库页增加全部/常规/Python 视图；Python 视图不展示年级、学期、课次，显示标题、知识点、难度、测试点数量和验证状态。
- Python 出题为题面、测试点、代码与验证三步；保存后可直接运行参考代码验证全部测试点。
- 双 Sheet Excel 在前端解析；服务端每批最多 30 题，做结构检查、同名检查、乱码检查和参考代码全用例验证。预检通过后发放用户隔离的 30 分钟令牌，确认时加短锁并在一个事务中写入。
- 题单编辑页提供“一键推荐 12 题”；只从公开、启用、`VALID` 且 `create_by=python-system-v2`、带 `PYV2-xxx` 稳定题号的题目中选择，默认按简单/中等/困难约 50%/40%/10%，同难度内优先覆盖不同知识点。V2 未就绪时明确提示，不回退选择旧 V1。

## 学生端

- 页面为左题面、右上编辑器、右下控制台的嵌套可拖动三窗格，包含结构化题面、样例卡片、自定义输入输出、样例运行、正式提交、测试点矩阵和提交记录。
- 支持亮/暗主题、全屏、恢复初始代码和本地草稿；服务端草稿仍作为账号级兜底。

## 系统题生产

- 提示词：`python-system-question-generation-prompt.md`。
- 120 题唯一蓝图：`python-system-question-blueprint-v2.json`；题号 `PYV2-001`～`PYV2-120` 连续，标题全部唯一，难度分布为 SIMPLE 47、MEDIUM 57、HARD 16。
- 结构约束：`python-system-question-package.schema.json`。
- 本机校验：`python tools/python_oj_validate.py <题包1.json> [题包2.json ...] --report <报告.json>`。
- 批次提示词生成：`python tools/python_oj_batch_prompt.py --batch <1-6> --output <提示词.txt>`；`output/python-oj/` 已生成 6 份可直接复制的批次提示词，每份包含对应 20 条蓝图。
- 校验工具检查字段、编号/标题重复、题面高度相似、乱码/ASCII 问号占位、公开/隐藏测试点、权重、危险模块/函数，并在隔离临时目录中用 Python 逐测试点运行参考代码；传入 `--blueprint` 后还会强制题号、标题、知识点和难度逐项一致，`--require-complete-blueprint` 可检查六批是否覆盖全部 120 题。
- `tools/python_oj_build_import_sql.py` 只接受完整 120 题并先调用上述校验器；通过后生成按 `PYV2-xxx` 幂等、文本十六进制写入、冲突前检、事务回滚和数量/公开隐藏点/权重后检齐全的 MySQL SQL。系统题标记固定为 `create_by=python-system-v2`，不复用教师 Excel 导入的临时外部题号。
- 网页端六批原始文本均把 `referenceCode` 内部双引号裸放进 JSON。新增 `tools/python_oj_repair_ai_json.py` 只修复这一明确模式并重新标准化 JSON，不猜测或改写题意；120 个代码字段均已恢复。

## 验证证据

- 本机相关表备份：`backups/20260822_113751_local_before_python_oj_modernization/python_oj_related_tables.sql`。
- 备份 SHA-256：`6713C3EFA4B97CF7D8334274142AA985E68884DBE75721E85FDC89DD70929A35`。
- 开发库迁移后检：配置 8 个新增列和 1 个稳定题号唯一索引、快照 4 个新增列、提交 1 个新增列、逐测试点表 1 张；脚本重复执行后数量不变。
- 稳定题号追加迁移前单表备份：`backups/20260822_1253_local_before_python_external_id/biz_programming_question_config.sql`，41,469 bytes，SHA-256 `EF89DC23E75ED2BBC7FA87B5827AF07A9E46EB0D4E9222254E0D7EA15609EAB6`。
- Java：353 项测试，0 失败、0 错误；新增用例证明教师保存请求不能抢占或清空系统稳定题号，并验证推荐 12 题的难度配比、知识点优先和题号不重复。最终 `clean package` 成功，fat JAR 已确认包含最新业务类。
- Vue3：`npm run build:prod` 成功，共转换 2755 个模块，`dist/` 共 629 个文件；最终 `dist/index.html` SHA-256 为 `02464FE90EB7F95633A0B831E792DB0B4E63BFAB4987B221D3019C119F60AF06`。
- 最终 fat JAR 为 107,604,079 bytes，SHA-256 `61B9DB75B449C0E0E4DEF8BCA5019B2475E0F94E8125A1EE6DBF04E2FBAD58C2`；完整制品与部署清单见 `output/python-oj/python-oj-release-candidate-manifest.json`，状态为 `PRODUCTION_DEPLOYED_AND_VERIFIED`。
- 教师接口：双 Sheet 预检 ready=true，参考代码 valid=true，确认导入 1 题，配置状态 VALID；删除后题目/配置/测试点均为 0。
- 学生接口：`CUSTOM_RUN` 返回 ACCEPTED、score=null、1 个公开结果；`SUBMIT` 返回 2 个结果，隐藏项的输入/期望/实际/错误详情均为空。临时提交和进度已清理。
- 真实 Judge0：因服务只允许 123 主机访问，验收时建立仅监听 `127.0.0.1` 的临时 SSH 转发，经 123 访问 129 Judge0；AC=`ACCEPTED`、错误答案=`WRONG_ANSWER`、死循环=`TIME_LIMIT`。发现 Python 语法错误被 Judge0 原始归为 Runtime Error 后，平台增加 `SyntaxError/IndentationError/TabError` 二次识别，复测为 `SYNTAX_ERROR`。自定义输入真实运行返回原文，耗时 0.015 秒、内存 3260KB；临时转发脚本、进程和全部验收数据均已删除。
- 系统题校验工具烟测：1 题 6 测试点全部运行一致。
- 系统题完整本机校验：第 1 批 20 题/120 测试点、第 2～6 批 100 题/600 测试点全部通过蓝图、字段、乱码、重复、安全规则和参考代码真实运行。Windows 校验进程显式使用 `-X utf8`，因为 `-I` 会忽略 `PYTHONIOENCODING`；中文源码和输出已复测一致。
- 系统题导入器：Python 3 项单元测试通过；本机 MySQL 实际导入烟测生成 1 题、6 测试点（2 公开、4 隐藏、权重 100），教师查询接口返回 `externalId=PYV2-001`，随后题目/配置/测试点精确清理为 0。V1 接口复测返回 200 且 `externalId=null`。
- 推荐题单：本机登录教师接口后调用推荐端点，真实执行权限、题单归属和 MyBatis 候选查询；因本机尚无 V2 系统题，按设计返回“暂无可推荐的 V2 系统题”，证明不会把旧 V1 或教师自建题混入推荐。

## 正式发布与剩余风险

- 发布前整库备份：`D:\program\3009dazipingtai\backups\20260822_161015_before_python_oj_unified_v2\ry-vue_full.sql`，81,624,952 bytes，SHA-256 `85D2BBBCCDB4FEAF1E2BC7DBEA3FAAD3F2223EAA9F5009B4E9AEE983D9BC57B1`。
- 后端 release：`D:\program\3009dazipingtai\releases\20260822_164900_python_oj_unified_v2_r5\backend`；前端 release：`D:\program\3009dazipingtai\releases\20260822_164500_python_oj_unified_v2_r3\frontend`。NSSM 为 Running，3009/3010 均 HTTP 200。
- 正式库后检：120 道 V2 题、720 个测试点（公开 240、隐藏 480），题号唯一、配置均为 `VALID`、无乱码、迁移和题库 SQL 重复执行数量不变。
- 正式 Judge0：120 题/720 点全量通过；报告 SHA-256 `022AE144A061F9B416DC9B2CD9FC76C0A4B1C84339BF59D9D03C4667F7C7A872`。
- 正式 API 冒烟：教师负责班级查询、V2 题库选择、建题单、发布、学生可见及题单硬删除清理均通过；临时验收题单已清理。
- 自动浏览器视觉验收未完成，原因是浏览器控制工具不能代填本地私密凭据；构建、生产接口和静态资源验收均通过。后续可由人工登录重点观察三窗格拖拽和窄屏布局。
- V1 80 题暂不删除：课程 276 正在使用其中 10 题且已有答题/提交。待该课程切换到 V2 后，再按引用前检与备份执行精确清理。
- 回滚应用时将 NSSM/Nginx 切回保留的 `20260821_resilience_v1` release；新增表和字段向后兼容，可留存。只有必须整体回退数据时才在维护窗口恢复上述整库备份，以免覆盖发布后的新业务数据。
