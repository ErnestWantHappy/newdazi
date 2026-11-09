# 题库工作台：设计与实现（2026-11-05，本机）

## 页面结构

```
app-container.qw（单一根节点，DataLabEditor 与全部弹窗都在根容器内）
├─ 头部：题库工作台 / 说明 / 导入导出下拉
├─ 筛选区（默认展开）：搜索·年级·学期·课次 ｜ 公开状态·创建人·Python 难度·搜索·重置·收起筛选 + 范围说明
├─ 打字题模块（独占一行，桌面 4 列）
├─ 选择题 ｜ 判断题（同一行各半，桌面各 2 列）
├─ 操作题模块
│   ├─ 四种作答方式卡片（各带“＋ 新增”，不触发切换）
│   └─ 当前方式的题目卡片（桌面 3 列）
└─ 查看全部视图（同页切换）：标题+题量、批量管理、完整卡片列表、分页、返回工作台
```

## 文件与职责

| 文件 | 改动 |
| --- | --- |
| `RuoYi-Vue3/src/views/business/question/index.vue` | 顶部表格导航替换为工作台；原有编辑器、流程设计器、DATA_LAB、导入导出、详情与流程图预览全部保留 |
| `src/views/business/question/components/WorkbenchSection.vue` | 题型模块：真实题量、新增/查看全部、加载中/空/失败、加载更多 |
| `src/views/business/question/components/QuestionCard.vue` | 缩略卡片：题干 3 行截断、选项摘要、元信息、按权限渲染操作 |
| `src/views/business/question/components/PracticalModePicker.vue` | 四种作答方式入口，各带新增与题量 |
| `src/utils/questionWorkbench.js` | 纯函数：默认条数、模块范围、查询构造、卡片摘要、权限按钮、追加去重 |
| `src/utils/__tests__/questionWorkbench.test.js` | 12 项：默认值、范围、权限、去重、历史空方式 |
| `src/utils/__tests__/questionWorkbenchPage.test.js` | 9 项：模块顺序、默认值集中、新增入口、单根节点、原能力保留（读源码静态断言） |
| `RuoYi-Vue3/package.json` | 增加 `test:question-workbench`（两套工作台测试）并纳入 `build:prod` 门禁 |

2026-11-05 独立审查修复增加 `BizQuestionMapper.xml` 的 FILE/历史空值条件及 `QuestionWorkbenchMapperTest`。无数据库迁移、新接口或权限边界调整；`vite.config.js`、`AppMain.vue`、PY 扩展名与原编辑器数据契约保持。

## 关键实现决定

1. **默认条数集中配置**：`WORKBENCH_DEFAULTS = { typing: 8, choice: 6, judgment: 6, practical: 6 }`，模板不出现 8/6 字样。
2. **每个模块独立查询**：用各自 `questionType`（操作题再加当前 `practicalMode`）分页请求，`pageSize` 等于默认条数；total 取服务端返回，不把一页结果拆开冒充总数。
3. **文件作品兼容历史空方式**：前端明确传 `practicalMode=FILE`，Mapper 限定 `question_type=practical` 且方式为 FILE/NULL/空字符串；不会混入 Python/实验/流程图，也不回填数据库。列表与 total 使用同一查询条件，取消原错误的全操作题统计累加。
4. **范围差异透明**：`buildModuleQuery` 对 PYTHON / DATA_LAB 不传年级、学期、课次；难度只在 PYTHON 模块生效。筛选区下方写明该口径。
5. **新增入口**：`beginAdd(type, mode)` 先 `reset()` 再设题型/方式，交替新增不会串类型；DATA_LAB 直接 `dataLabEditor.open()`；年级/学期/课次只在筛选明确选中时带入。
6. **保存反馈**：新增成功停留在对应方式并刷新四模块；若当前筛选（搜索、公开、创建人、年级范围、Python 难度）可能隐藏新题，改为警告“已保存，但当前筛选条件下可能看不到”，不硬插列表。Python 验证未通过时题目按草稿保存并刷新列表。
7. **多阶段保存幂等**：`createdQuestionId` 记录已建主记录，配置保存失败后重试走更新，不重复建题；保存按钮 `submitting` 防重复提交。
8. **按钮动态**：`cardActions` 识别具体权限及 `*:*:*` 通配权限，并综合创建者归属与管理员身份；操作题只给“预览”（流程图或已有预览页），普通题只给“查看”。查看全部显式传当前 row，模块视图继续由区块包装传 row；删除保留确认。
9. **异步安全**：每个模块与查看全部各有 `requestId`，过期响应丢弃；切换筛选重置追加页码；加载更多按 `questionId` 去重，`total` 与去重后条数共同决定是否还有剩余；追加失败保留列表并显示“重新加载更多”。
10. **操作方式题量**：`loadPracticalModeTotals` 只取四种方式各自的 `total`（4 次 `pageSize=1` 请求），不再额外查询全操作题并累加；失败显示“题量未知”。
11. **查看全部**：原页面内切换，保留筛选与当前方式；根据来源模块带入题型范围，分页用现有 `pagination` 组件；批量管理仅在有编辑/删除权限时出现，只允许处理本人题目；删除最后一条回退有效页。
12. **视觉**：浅灰底、白色模块、细边框、蓝色主操作，牌色点缀（打字紫、选择蓝、判断绿、操作橙）；桌面三层布局，≤768px 单列、作答方式两列；无新依赖。

## 已知取舍

- 操作方式入口的数字来自4次 `pageSize=1` 查询，筛选变化时刷新。
- 批量管理显示“编辑选中题目”（单选及edit权限）、“删除选中题目”（remove权限）和“退出批量管理”；动作函数再次检查权限、归属与当前页范围，退出不写数据，翻页/重查清空选择。
- 卡片不做附件缩略图，使用题型图标与文字，避免暴露私有资源地址。

## 独立审查修复的发布要求

修复保存回调参数遮蔽：`savedMode` 与 Vue `practicalMode` ref 分名，避免保存已成功却显示失败。需同步构建前后端、重启后端加载 Mapper；无结构SQL、新配置。仅本机修复，未发布。验证与剩余风险见 tasks.md。

## 2026-11-06 预览实现补充

QuestionCard 直接使用列表既有 nickName，不额外逐题查教师；打字不再渲染重复 excerpt。新增 PythonQuestionPreviewDialog 独立管理加载、失败重试及过期响应丢弃，复用 question 详情、programming 完整/preview 接口。流程图组件新增默认关闭的 canReadAnswer，题库按归属开启，课程设计器保持默认的起始图预览权限。双图切换重新挂载只读画布。无新接口、权限模型、数据库或依赖变更，无需新 ADR。

## 2026-11-06 正式状态覆盖

本轮题库预览已随1.30.74上线。真实师生、真实Python答案/4测试点、真实流程图两版本、桌面及窄屏验证通过；本机“未发布/真实联调未验”是发布前记录，已由[正式验收](release-20261106.md)覆盖。无架构路线或权限变更；旧浏览器/机房并发仍未验。

## 2026-11-08 答案可见性与计数重试（1.30.81）

题库通用列表和详情仍供工作台读取题干、测试点与归属信息，但`practical + PYTHON`的`answer`/`analysis`只在创建者或管理员响应中保留；其他教师收到空值。裁剪在Controller响应层完成，避免改变课程、考试、成绩分析共用的Mapper语义。前端预览据空值显示“仅题目创建者或管理员可查看参考答案”，不把空答案误报成题目损坏。

操作题四模式计数改为`Promise.allSettled`：单个弱网失败只把该模式标为`null`并显示“题量未知/重新获取”，其他模式保留真实总数；单项重试沿用当前筛选和请求序号，迟到响应不能覆盖新结果。无SQL、新接口或新依赖。
