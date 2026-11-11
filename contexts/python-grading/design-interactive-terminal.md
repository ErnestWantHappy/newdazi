# Python 交互运行与判题统一设计

状态：**已实现并上线 1.30.94**（2026-11-11 完成本机双模式复核与 130 真实教师验收）

## 1. 问题

1. 学生做题页只有本机 Pyodide 能逐行提问；旧浏览器降级后只能一次性填完输入。
2. `input("提示语")` 会把提示语写进标准输出，判题端拿它和期望输出比较，带提示语的题必然判错；用正则删提示语又会误删学生自己打印的同样文字。
3. 课程判题按“通过个数/总数”算分，刷题链路忽略测试点权重，同一道题两条入口分数不同。
4. 教师端多处 Python 运行入口交互不一致：有的要先填全部输入，有的没有提示语，有的必须先有学生提交记录。

## 2. 核心机制：给提示语打标记，而不是删除

`JudgeSourceWrapper` 只在源码真的调用 `input(` 时套一层薄壳：

- `builtins.input` 换成 `_platform_input`，在提示语前后写 `\x00\x01P\x01\x00` / `\x00\x01E\x01\x00`；
- 输入不够时写 `\x00\x01N\x01\x00` 到 stderr 并 `SystemExit(0)`，表示“还要输入”，不是运行错误；
- 学生源码以 base64 传入，执行时 `exec(compile(source, 'main.py', 'exec'))`，报错行号仍是学生看到的行号；
- 异常只保留 `main.py` 的调用栈，不暴露外壳内部帧。

`JudgeOutput` 负责解释标记：

- `plain()`：只有程序真正输出，用于判题比较；
- `visible()`：保留提示语文字、去掉标记，用于兼容存量题目；
- `terminalText(stdin)`：按“提示语 + 用户输入 + 换行 + 输出”回放成终端文本；
- `matches()`：`plain` 或 `visible` 任一命中即通过，存量题不需要改数据；
- `needsMoreInput()`：区分“还差输入”和真正的错误。

**不使用任何字符串删除或正则替换**，因此学生打印和提示语相同的文字不会被误删。

## 3. 统一判题结论

`JudgeOutcome` 是唯一的状态与分数来源：

- `caseStatus()`：只有判题服务接受且输出匹配才算 `ACCEPTED`；程序停在等待输入时判 `RUNTIME_ERROR`，绝不当作通过；
- `runStatus()`：自定义运行跑完即 `COMPLETED`；
- `weightedScore(fullScore, passedWeight, totalWeight)`：按权重分配分数，课程按题目分值、刷题按 100 分，比例一致；
- `summary()` / `statusMessage()` / `differenceHint()`：统一中文文案与教师可读的差异说明。

## 4. 前端统一终端

`PythonTerminal.vue` 是教师批改运行、学生刷题页、`.py` 作品运行共用的终端组件；学生做题页保留原有 `InteractiveTerminal.vue` 外壳（需要向父组件报告运行忙闲和完成事件），但两者共用同一个 `interactiveRunner`、`fallback` 判定和“提示语 → 输入 → 输出”渲染规则，不再各写一套：

- 运行时判断 `canRunLocally()`（JSPI）选择本机或服务器，师生不需要选择；
- 本机失败若属于兼容性问题，自动切服务器，并把已填输入带过去，不重复提问；
- 服务器模式每次提交“到目前为止的全部输入行”，程序停在下一个 `input()`，前端继续显示输入框；
- 服务器返回的是完整回放文本，重跑前先清空，避免重复显示；
- 等待输入时输入框紧跟在提示语后面，与 Thonny 一致。

## 5. 接口

| 入口 | 接口 | 是否写成绩 |
| :--- | :--- | :--- |
| 学生做题运行（本机） | 无，浏览器内执行 | 否 |
| 学生做题运行（降级） | `POST /business/student-home/programming/server-run` | 否 |
| 学生刷题运行 | `POST /business/python-practice/student/run`（本轮新增） | 否 |
| 教师批改运行已提交代码 | `POST /business/teacher/grading/python/{submissionId}/run` | 否 |
| 教师直接运行当前代码 | `POST /business/teacher/grading/python/run`（本轮新增） | 否 |
| 作者 `.py` 作品运行 | `POST /business/practical-attachment/{id}/run` | 否 |
| 教师验题 | `POST /business/programming-questions/{id}/validate` | 否（只改验证状态） |
| 生成期望输出 | `POST /business/programming-questions/{id}/generate-outputs`（本轮新增） | 否，不落库 |

## 6. 兼容与历史数据

- 不新增表、不改字段、不迁移历史成绩。
- 旧期望输出含提示语的题目继续判对（`matches` 同时比 `plain` 和 `visible`）。
- 旧提交记录的输出文本保持原样，不做批量重写。
- 批量采用系统分、人工评分、最高分提交选择逻辑均未改动。

## 7. 未覆盖边界

- 未在生产环境做整班并发压测和真实 Judge0 长时运行验证。
- 本机 Pyodide 兼容性判断依赖 JSPI，Safari/Firefox/Safari iOS 目前一律走服务器运行。
- 循环中动态次数的 `input()` 依赖程序自身逐次提问，前端不做次数预估。
- 作品 `.py` 预览运行和教师建题向导已完成代码/接口核对，但未在浏览器中逐项点完；本轮浏览器实测覆盖教师批改“运行查看”和学生刷题页。
- 本机开发库缺少代码依赖的 `biz_data_lab_snapshot` 表及 `biz_data_lab_submission.revision/max_score` 列，已按 `sql/data_lab_v2.sql` 的既有 DDL 补齐；这是本机环境修复，130 未执行。
