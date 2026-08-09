# 操作题多格式作品与 AI 辅助批改设计

## 1. 总体设计

系统以“逻辑作品”作为教师和学生看到的业务对象，以“提交版本”和“附件”作为存储对象，以“统一渲染产物”作为人工预览与 AI 视觉输入。人工正式成绩与 AI 草稿严格分离。

```text
题目材料/允许类型
        ↓
学生逻辑作品 → 不可变提交版本 → 附件列表 → 转换任务 → 页面图/PDF
        ↓                                      ↓
教师人工批改 ← 评分标准快照 ← AI批改任务 ← 规范化视觉输入
        ↓                         ↓
正式成绩/分项明细          AI草稿/证据/审计
```

## 2. 分阶段实施

### P1 评分正确性门禁（已完成）

- 普通课程分项满分由服务端用最大余数法计算，复用区域抽测的同一算法。
- 保存接口锁定答案行，校验答案提交时间和预期旧成绩，防止迟到响应覆盖补交或他人新成绩。
- 服务端验证题目归属、题目满分、评分项归属/完整性/重复/上限/合计。
- 前端保存期间禁用换人、重复回车和翻页；异步评分项/明细响应带请求序号，过期响应直接丢弃。

### P2 作品、版本与附件

建议新增结构：

- `biz_practical_artifact`：题目、学生、课程/抽测上下文下的逻辑作品。
- `biz_practical_submission_version`：版本号、提交人、提交时间、是否当前版本、失效原因。
- `biz_practical_attachment`：版本、用途、顺序、原名、存储键、扩展名、MIME、大小、哈希和安全状态。
- `biz_question_material`：起始文件、补充资源、参考答案/评分依据。
- `biz_practical_allowed_type` 或题目 JSON 配置：允许的作品类型与图片数量。

旧 DOCX 字段保留只读兼容；迁移与新写入采用双读单写，再逐步回填，禁止一次性破坏历史数据。

> 2026-08-04 实现说明：实际表名为 `biz_practical_artifact`、`biz_practical_submission_version`、`biz_practical_attachment`、`biz_practical_question_material`；`biz_student_answer` 保留兼容文件与预览字段，并绑定当前作品/版本。学生先逐文件暂存，再以一组上传凭证原子提交新版本；图片组按附件顺序保存，Office/PDF 仅允许一个。历史未知格式仅标记 `LEGACY_UNVERIFIED`，不会冒充已通过新安全校验。

### P3 统一预览

- 一个转换服务接收附件，按类型输出 PDF/页面图及状态 `PENDING/PROCESSING/SUCCEEDED/FAILED`。
- Office 经 LibreOffice 转 PDF，PDF 再渲染页面图；图片只做方向归一、尺寸限制和安全解码。
- 用内容哈希 + 转换器版本缓存；预览失败保留原文件授权下载与人工评分能力。
- 常规课程与区域抽测调用相同服务，权限仍由各自业务上下文校验。

> 2026-08-04 实现说明：普通课程附件已使用 `normalized_status`、`normalized_pages_json`、`renderer_version`、重试次数/时间与错误信息形成独立状态机，人工 PDF 预览状态不再与 AI/连续页图状态混用。PDFBox 2.0.32 以 120 DPI 输出 JPEG，单附件最多 50 页，图片最长边 1800 像素。哈希缓存命中后复制页图到当前附件的 `normalized-v1/<hash>-attachment-<id>/`，以资源 URL 隔离保证作品鉴权明确；区域抽测使用独立字段复用同一页图渲染器、重试语义和连续页图界面。普通课程人工重转覆盖全部新附件类型，并保护未超过 10 分钟的正常转换任务。

> 2026-08-07 正式环境热修：DOC/DOCX 保留 JODConverter 常驻池；容易卡住的 PPT/PPTX/XLS/XLSX 使用最多 2 个并发、120 秒硬超时的独立 `soffice.com` 进程。独立进程的用户目录固定在系统短临时路径，避免 Windows 深路径导致 LibreOffice 以 0 退出却不生成 PDF。新附件同时接入每小时恢复任务和 LibreOffice 自愈后的即时恢复，原子认领并遵守最多 3 次限制。决定与证据见 `ADR-003-office-conversion-isolation-and-recovery.md`。

### P4 评分快照与人工闭环

- 发布课程/抽测时固化题干、题目满分、评分项名称/权重/绝对上限、参考材料版本。
- 正式成绩绑定提交版本和评分快照版本。
- 补交后旧成绩标记失效；教师确认最新版本后形成新的正式成绩。

> 2026-08-04 实现说明：实际新增 `biz_practical_rubric_snapshot`，课程保存时按内容变化递增快照版本；提交版本通过 `rubric_snapshot_id` 固定评分口径。历史回填以当前课程关系为主，对已移除关系的 3 组历史题目仅按 `sys_oper_log` 中提交前最后保存证据固化，未猜测分值。

### P5 AI 草稿批改

- `biz_teacher_ai_credential`：教师、供应商、加密 Key、掩码、校验状态和更新时间。
- `biz_ai_grading_job`：班级任务、模型、提示词版本、状态、进度、失败数和取消标记。
- `biz_ai_grading_result`：提交版本、评分快照、逐项分数、证据、理由、置信度、风险、原始结构化响应摘要和状态。
- 后台工作器按教师 Key 调用千问；失败按可重试/不可重试分类，限流退避，不因单份失败终止全班。
- 教师审核页并排展示作品、评分项、AI 草稿和证据；“确认成绩”复用 P1 正式评分服务。

> 2026-08-04 实现说明：实际表名为 `biz_teacher_ai_config`、`biz_practical_ai_job`、`biz_practical_ai_result`。Key 使用部署主密钥派生 AES-256-GCM 密钥，每次随机 12 字节 IV；任务在 `practicalAiExecutor` 独立线程池顺序处理班级作品，结果严格绑定 `answer_id + practical_version_id + rubric_snapshot_id`。模型适配器使用百炼 OpenAI 兼容接口和 Base64 JPEG，不生成公网作品 URL；输入刻意不含学生姓名、学号和班级。前端“采用到评分框”只填值，不写成绩，最终仍复用原人工评分接口。

> 2026-08-07 发布说明：正式服务器当前 release 为 `20260807_165505_4dcb696`。服务器生成的部署主密钥通过 NSSM 服务环境注入，教师 Key 保存为 `v1:` 密文；郑东旭教师账号在 `deptId=169` 使用无学生数据测试图调用 `qwen3.7-plus` 成功，服务重启后直接复用既有密文再次成功。随后用 3 个匿名合成学生完成 PPTX、XLSX、PNG+JPG 的班级任务，3/3 返回结构化建议且正式成绩保持空值；测试账号、结果和文件已清理。该验证证明 Key、网络、视觉输入、批处理和人工确认边界可运行，不代表真实学生作品的准确率、隐私门禁、真实班级时延或成本通过。

### P5.1 批改范围与建议采用闭环（2026-08-09 方案 A）

- 新增课程级 `biz_teacher_practical_reference_answer`。教师在批改弹窗上传当前课程、题目使用的参考答案；题库已有的可识别 `REFERENCE` 文件可作为初始默认依据。
- `biz_practical_ai_job` 新增 `scope_mode`、`reference_answer_json`、`starter_materials_json`。任务创建时把参考答案和 `STARTER` 材料序列化为不可变快照，提示词升级为 `operation-rubric-v2`。
- `UNGRADED_ONLY` 是默认范围，只为创建瞬间尚未评分且页图就绪的作品建立结果；`ALL_SUBMITTED` 为全班生成对照建议，但不改变正式成绩。
- 页面按教师、课程、题目、届别和班级读取最近任务，因此刷新后仍能恢复暂停/进行中/完成状态及逐份建议。缺少参考答案快照的 v1 旧任务允许查看和取消，禁止继续及批量采用。
- 暂停任务取消时直接把剩余待处理结果与任务收口为 `CANCELLED`；不能停留在无人消费的 `CANCEL_REQUESTED` 而阻塞下一次任务。
- `biz_practical_ai_result` 新增采用状态、采用教师和时间。批量采用锁定每份答卷后再次检查正式成绩为空、提交版本一致、评分快照有效、分项合法和期限开放；不满足者逐份跳过。
- “批量采用”是教师明确确认动作，不等同于 AI 自动写分。采用后的正式成绩仍可由教师按现有人工批改流程修改。

### P5.2 可观测处理与安全接续（2026-08-09 方案 A）

- `biz_practical_ai_job` 增加公共材料准备状态、对照页图缓存、当前结果和心跳；`biz_practical_ai_result` 增加处理阶段、阶段时间、单份耗时和尝试次数。
- 新增 `biz_practical_ai_event`，只保存教师可理解的安全事件。结果事件通过 `result_id` 在前端映射学生姓名，后端日志不冗余学生身份信息。
- 工作器先认领单份结果为 `PROCESSING`，再依次更新 `PREPARING_STUDENT → REQUESTING_MODEL → VALIDATING_RESULT → COMPLETED/FAILED`；条件更新防止重复工作器处理同一结果。
- 教师参考答案和空白起始材料先统一生成 `ComparisonPage(resourcePath,label)` 列表并写回任务，后续学生只加载缓存，不重复执行 Office 转换和页图渲染。
- `ApplicationReadyEvent` 触发恢复器：取消请求直接收口；中断的 `PROCESSING` 退回 `PENDING`；任务随后由原异步线程池接续。暂停任务保持暂停，已经结束的结果不重跑。
- 详情接口直接从任务与逐份结果计算进度、平均耗时和 ETA；模型首次完成前不伪造 ETA。心跳超过 6 分钟只告警，不自动制造并发恢复。

## 3. AI 输出契约

```json
{
  "rubricResults": [
    {
      "rubricItemId": 1,
      "score": 8,
      "maxScore": 10,
      "evidence": [{"page": 2, "description": "可核验的画面事实"}],
      "reason": "基于评分项的简短理由",
      "confidence": 0.86,
      "riskFlags": []
    }
  ],
  "totalScore": 8,
  "maxScore": 10,
  "overallComment": "总评",
  "needsHumanReview": true
}
```

服务端必须重新校验：评分项集合、逐项上限、合计、总分上限、JSON schema、提交版本和评分快照。模型输出不能直接写 `biz_student_answer.score`。

## 4. 安全与运维

- 上传路径使用服务端生成存储键，不信任原始文件名；下载/预览每次做作品归属与角色权限校验。
- Office 转换在受限目录和超时下运行，拒绝宏执行、路径穿越和压缩炸弹。
- Key 加密主密钥只存在部署环境配置；日志统一脱敏，请求体不得记录 Key 和学生作品内容。
- AI 上线前记录供应商区域、数据留存/训练策略、调用日志、删除机制和学校授权结论。
- 所有表结构变化提供幂等 SQL、前检、后检、备份和回滚说明。

## 5. 已记录架构决策

- `ADR-001-unified-static-page-preview.md`：统一使用静态 JPEG 页图；缓存复用计算结果但不跨附件共享 URL；人工预览失败不阻断下载和评分。
- `ADR-002-qwen-draft-only-ai-grading.md`：百炼千问作为首期单一生产适配器；教师 Key 后端加密；AI 仅生成版本化建议，正式成绩必须人工确认。
- `ADR-003-office-conversion-isolation-and-recovery.md`：Word 保留常驻池，PPT/Excel 使用短用户目录的一次性进程；新附件加入定时与自愈恢复队列。
- `ADR-004-ai-scope-reference-and-safe-batch-adoption.md`：任务前确认范围，冻结教师参考答案/空白材料，刷新恢复任务；批量采用只写当前未人工评分答卷。
- `ADR-005-ai-job-observability-and-recovery.md`：持久化阶段/心跳/安全事件，公共对照页图单次准备，服务重启只接续未完成结果。

## 6. 正式发布与回滚

- 正式库迁移顺序固定为 P2→P3→P4→P5；2026-08-07 后检为作品/版本/附件各 12,732、材料 95、评分快照 106，重复、孤儿、未绑定快照和非法 AI 结果均为 0。
- 当前应用 release 为 `20260807_165505_4dcb696`。热修回滚可切到 `20260807_164904_011941d`，但该版本仍存在 Windows 深路径下 PPT/Excel 可能不产出 PDF 的已知问题；若回滚到原始发布版则使用 `20260807_152632_9328868`。P2～P5 新表和兼容列可保留，不需要为应用回滚立即还原数据库。
- 若必须完整回退数据，维护窗口内恢复备份目录 `20260807_152125_9328868_operation_ai` 中的 `ry-vue` dump；这会移除发布后教师 AI 配置及全部 P2～P5 结构/回填，必须与应用旧 release 同步执行。
- 部署主密钥不得写入 Git、业务文档或应用参数；恢复新 release 时须同时恢复服务器受保护的 NSSM 注册表备份，否则既有教师 Key 密文无法解密。
