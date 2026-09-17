# Python 系统题库 V2 网页端生成提示词

> 用法：每次先把下方“主提示词”完整发给网页端 AI，再从 `python-system-question-blueprint-v2.json` 复制本批 20 条蓝图。AI 必须保持蓝图的题号、标题、知识点、难度和任务边界，只负责扩写完整题面、参考代码和测试点。生成结果保存为 UTF-8 `.json` 文件后交给 Codex；不要直接导入数据库。

## 主提示词

你是一名熟悉中国中小学信息科技教学和在线判题系统的 Python 题库编辑。请原创一批面向 Python 初学者的 ACM 标准输入输出编程题，并严格输出一个合法 JSON 数组，不要输出 Markdown 代码围栏、解释、前言或结语。

必须遵守：

1. 每题只考查指定的一个核心知识点，可带少量已学前置知识；题意清楚、任务唯一，不让学生猜输入输出。
2. 不复制、改写或近似复刻牛客、洛谷、LeetCode 等网站的具体题面；可以使用原创校园、生活、科学和信息科技情境。
3. 第一阶段只使用 Python 3 ACM 模式：学生通过 `input()` 或标准输入读取，通过 `print()` 输出；不要求实现指定函数，不做交互题。
4. `description` 说明已知信息、任务和目标；`inputDescription` 必须说明行数、类型、分隔方式和范围；`outputDescription` 必须说明精确格式。
5. 无输入题设置 `noInput=true`、所有测试点 `inputText=""`，输入说明写“本题无需输入”。
6. 每题恰好 2 个公开样例，另有 4～8 个隐藏测试点。测试点覆盖适用的正常值、零、负数、重复值、最小值、最大值和格式边界。
7. 所有期望输出必须由 `referenceCode` 真实运行得到；禁止使用 `?`、`待定`、`略`、占位符或自然语言解释代替输出。
8. `referenceCode` 只使用 Python 标准库，写法清楚，符合初学者学习阶段；不得读写文件、联网、启动进程、使用 `eval` 或 `exec`。
9. 输出比较会统一换行、忽略行尾空白和文末空行，但大小写、内部空格、数值格式和顺序严格匹配。
10. 题目之间不得只替换人名或数字；标题、任务、数据结构、解题步骤和测试数据都要有实质差异。
11. 本批附带的蓝图是硬约束：`externalId`、`title`、`knowledgePoints`、`difficulty` 必须逐字一致，题目任务不得偏离 `taskBrief`，也不得把两条蓝图合并为一题。
12. 最终内容必须能被标准 JSON 解析器直接解析。JSON 字符串内部出现英文双引号时必须写成 `\"`，换行必须写成 `\n`，不得把 Python 代码中的双引号或真实换行直接裸放进 JSON 字符串。输出前必须在内部执行一次等价于 `JSON.parse` 的语法自检。
13. 不要用省略号截断内容，不要输出“其余题目同理”。必须完整输出本批全部 20 道题，编号连续且无重复。

每个对象必须具有以下字段，字段名和枚举不得修改：

- `externalId`：本批唯一编号，例如 `PYV2-001`。
- `title`：不超过 30 个汉字。
- `knowledgePoints`：字符串数组，只能使用本批指定知识点词表。
- `difficulty`：`SIMPLE`、`MEDIUM` 或 `HARD`。
- `description`：完整题目描述。
- `inputDescription`：精确输入格式。
- `outputDescription`：精确输出格式。
- `constraints`：数据范围和约束。
- `sampleExplanation`：逐个解释公开样例；无需解释时写空字符串。
- `noInput`：布尔值。
- `starterCode`：学生起始代码，允许统一为一行简短中文注释。
- `referenceCode`：可直接执行的 Python 3 完整参考代码。
- `timeLimitSeconds`：默认 `2.0`。
- `memoryLimitKb`：默认 `131072`。
- `isPublic`：系统题固定为 `true`。
- `testCases`：测试点数组。

每个测试点对象必须具有：

- `caseName`：公开样例 1、公开样例 2、隐藏测试点 1 等。
- `inputText`：完整标准输入，不额外包引号说明。
- `expectedOutput`：完整且非空的标准输出；不得使用 `?`、`待定`、省略号或自然语言占位。
- `isPublic`：布尔值。
- `scoreWeight`：整数；所有测试点权重合计恰好为 100。
- `orderNum`：从 1 连续递增。

生成前请在内部逐题检查参考代码和全部测试点的一致性，但最终只能输出 JSON 数组。

## 每批追加参数模板

本批编号范围：`PYV2-___` 至 `PYV2-___`  
本批数量：`20` 道  
本批蓝图：（从 `python-system-question-blueprint-v2.json` 原样复制对应 20 个对象）  
允许的前置知识：只使用蓝图题目之前已经出现的 Python 基础知识  
特别要求：逐题检查边界数据，最终只返回 20 个完整题目对象组成的 JSON 数组

## 交回 Codex 前自检

- 文件能被标准 JSON 解析器直接打开。
- 没有 Markdown 围栏和 JSON 前后的解释文字。
- 编号连续且不重复。
- 每题 2 个公开样例、4～8 个隐藏测试点。
- 每题测试点权重之和为 100。
- 中文和特殊字符显示正常，无 `?` 占位符。

## Codex 校验命令

可先用工具生成任意一批完整提示词：

```bash
python tools/python_oj_batch_prompt.py --batch 1 --output output/python-oj/python-v2-batch-01-prompt.txt
```

单批校验：

```bash
python tools/python_oj_validate.py batch-001-020.json --blueprint contexts/python-judge0/python-system-question-blueprint-v2.json --report output/python-oj/batch-001-020-report.json
```

六批到齐后的完整校验：

```bash
python tools/python_oj_validate.py batch-*.json --blueprint contexts/python-judge0/python-system-question-blueprint-v2.json --require-complete-blueprint --report output/python-oj/python-v2-final-report.json
```
