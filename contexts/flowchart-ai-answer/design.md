# 题库画流程图题：拍照 + AI 生成标准答案（设计）

> 状态：方案 A 设计草案，待实施。日期 2026-10-23。

## 1. 总体流程

```text
建“画流程图”题（已填 名称/年级/学期/第几课）
        │ 选一张流程图照片上传（/common/upload，服务端存储键）
        ▼
POST /business/flowchart/question/ai-generate-answer
   { title, grade, semester, lessonNum, imageStorageKey, providerCode? }
        │ 取教师多厂商 Key（biz_teacher_ai_provider_key，AES-GCM）
        │ QwenPracticalVisionGradingProvider.chatJson（照片 base64 + Prompt）
        │ 模型返回 {nodes:[{type,text}], edges:[{from,to,label}]}（无坐标）
        │ 平台坐标自动排版
        │ FlowchartDocumentService.normalizeDocument 校验并规范
        ▼
返回 answer_json  →  回填 LogicFlow 画布  →  “AI 生成，请核对后保存”  →  老师保存(saveTeacherConfig)
```

## 2. 复用能力（已存在，直接使用）

- 题库建题表单与字段：`RuoYi-Vue3/src/views/business/question/index.vue`（年级/学期/第几课下拉）；`BizQuestion.grade/semester/lessonNum/title`。
- 流程图配置与存储：`RuoYi-Vue3/src/components/FlowchartEditor/FlowchartQuestionDesigner.vue`、`RuoYi-Vue3/src/api/business/flowchart.js`（`saveFlowchartQuestion`）；后端 `FlowchartController`、`FlowchartService.saveTeacherConfig`、`FlowchartDocumentService.normalizeDocument`、`FlowchartStructureCheckService`、`FlowchartImageRenderer`；表 `biz_flowchart_question.answer_json`。
- 视觉模型调用：`QwenPracticalVisionGradingProvider`（`chatJson`、base64 多图 + label、`response_format=json_object`）、`PracticalVisionModelCatalog`（端点与模型白名单，含 QWEN/DEEPSEEK/DOUBAO）、`TeacherAiConfigService.configForJob`（按厂商取 Key）、`PracticalAiCipherService`（AES-GCM）。
- 图片 → 结构化范式：`PaperScanVisionGateway.recognizeIdentity`。
- 选图上传：`ImageUpload` + `/common/upload`；`PracticalFilePolicyService`。

## 3. 新增：后端生成接口

- 控制器方法 `generateAnswer`，挂载 `POST /business/flowchart/question/ai-generate-answer`。
- 入参：`imageStorageKey`、`title`、`grade`（文本化标签）、`semester`、`lessonNum`，可选 `providerCode`（默认取教师当前厂商）。
- 处理链：读图片 → 构造 Prompt → `chatJson` → 解析 → 排版 → `normalizeDocument` → 返回 `{ document, warnings }`。
- 权限：仅题目创建者或管理员（与 `saveTeacherConfig` 一致）；不接受外部任意 URL。

## 4. Prompt 与输出契约

- 模型输出严格 JSON：

```json
{
  "nodes": [{ "tempId": "n1", "type": "terminal", "text": "开始" }],
  "edges": [{ "fromTempId": "n1", "toTempId": "n2", "label": "" }]
}
```

- `type` 仅四类；开始/结束都归 `terminal`，文字分别内置“开始”“结束”。
- 要求模型**只识别拓扑与文字，不输出坐标**；图例或非四类元素归并为最近类型或写入 `warnings` 提示老师手动补。
- 服务端不信任模型：数量、文字、类型再统一校验。
- Prompt 以 Java 常量维护（与现有 `gradingPrompt` 一致），记录 `prompt_version`。

## 5. 坐标自动排版算法（关键不确定点）

- 用 `tempId` 建图，拓扑排序分层（入度 0 起，逐层 `y` 递增，示例行距 120px）。
- 同层按出现顺序分配 `x`（示例列距 220px）；判断节点分支左右或上下展开。
- 尺寸与文字居中：`process` 矩形；`decision` 菱形内部 `rx/ry` 半径换算完整宽高；`inputOutput` 平行四边形顶点 `[[12,0],[140,0],[128,60],[0,60]]`，文字取图形中心（沿用 flowchart-tool 几何约束）。
- 连线锚点：非 `decision` 仅上、下中线；`decision` 四锚点。
- 超 200 节点 / 400 连线，或 JSON > 512KiB，或含环、孤立节点 → 拒绝并 `warnings`，避免损坏文档。
- 坐标限 ±10000、两位小数，遵守 schema 规则。

## 6. 前端交互

- `FlowchartQuestionDesigner.vue` 标准答案页签加按钮 → `ImageUpload`（单图）→ 调 `generateAnswer` → 成功：载入画布 + 顶部 `el-alert`“AI 生成，请核对后保存”；失败：展示中文错误/警告。
- 载入前若画布已有内容，二次确认覆盖。
- 保存仍走原链路，不新增第二条写入。

## 7. 安全 / 成本 / 隐私

- 照片经服务端传给模型，不落公网 URL；日志脱敏，不记 Key 与照片内容。
- 出题阶段同样消耗教师 Key，按厂商计费；默认展示费用/需配 Key 提示。
- 权限以服务端为准，仅题目创建者与管理员可调用。

## 8. 回滚

- 预计**不改数据库表结构**（复用 `answer_json` 与现有乐观锁）。
- 纯应用层新增：回滚即切回旧 release，无需回滚 SQL。

## 9. 决策（待独立 ADR）

- 草案 1：AI 标准答案走“草稿回填 + 老师核对保存”，不引入独立建议表与自动写库。
- 草案 2：坐标由平台排版而非信任模型输出，保证画布几何合法。
