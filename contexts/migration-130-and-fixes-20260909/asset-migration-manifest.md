# P0-B 迁移清单 v1：数据库与文件映射（2026-09-09）

来源：123 生产库 `ry-vue` 只读查询 + `uploadPath` 轻量统计 + 本机代码默认值。
完整存在性校验（4.6 万文件逐个对哈希）留到 P0-D 低峰执行，本轮只做规模与映射，
不上课时段不跑全盘扫描。129 外部数据不属于本清单（CryptPad 文档、Judge0 用例、EMQX 消息另见 §4）。

## 1. 路径映射（已验证，无需改库）

- 库内写法：`/profile/upload/<子目录>/…`（前导斜杠、正斜杠），共 17089 条附件全部此前缀。
- 123 外置配置：`profile: D:/program/3009dazipingtai/uploadPath`，即 `/profile` = 上传根。
- 130 目标：外置配置 `profile: /data/upload`，相对层级原样复制，**库内路径零修改**。
- 本机默认 `./uploadPath`（相对路径）保持不动，仅 130 外置配置覆盖。

## 2. 数据库规模（行数/引用数，2026-09-09 14:xx）

| 表 | 行数 | 文件引用 |
| --- | --- | --- |
| `biz_student_answer` | 约 21 万 | preview 16328 条 |
| `biz_student_task_state` | 约 21 万 | 无（纯状态，可重建逻辑） |
| `biz_practical_attachment` | 17089 | resource 17089（全）、preview 16640 |
| `biz_question` | 2549 | file_path 152、preview 141 |
| `biz_county_exam_answer` | 4184 | file_path 1 |
| `biz_practical_question_material` | 181 | resource 181 |
| `biz_research_resource` | 3 | stored 2 |
| `biz_guide_sheet_upload` | 0 | —（空表，建表结构即可） |
| `biz_exemption_attachment` | 0 | —（空表） |
| `biz_flowchart_submission` | 108 | JSON 存库内，无外部文件列 |
| `biz_flowchart_draft` | 222 | 同上 |
| `sys_logininfor` / `sys_oper_log` | 17.9 万 / 6786 | 日志随库走，不单独迁 |

整库逻辑备份参考：09-07 发布备份约 140MB。预计本次同量级。

## 3. 文件规模

| 目录 | 大小 | 文件数 | 说明 |
| --- | --- | --- | --- |
| `uploadPath/upload/` | 约 7.97GB | 46137 | 主体：`student-answer-artifact/<uuid>/<日期>/<hash>.<ext>`，JPG/DOCX/PDF 及预览 PDF |
| `uploadPath/collaboration/` | 约 24MB | 340 | 协作附件 |
| `uploadPath/avatar/` | 约 2MB | 22 | 头像 |
| `uploadPath-private/` | 约 4MB | 3 | 私有上传 |

130 `/data` 可用约 466G，充足。

## 4. 不属于文件目录迁移的外部数据（独立确认）

- 129 CryptPad：协作房间文档本体（`biz_collab_room` 只存 `current_file_path` 等引用，文档在 129 卷内）。
- 129 Judge0：判题用例与执行环境（平台库只存提交与结果）。
- 129 EMQX：设备消息流（`biz_iot_message` 1.35 万行是落库副本，流本身不迁）。
- 协作 `temp_file_path`（上传票据临时路径）：演练前必须清空/过期处理，不随正式数据迁移。

## 5. 源端已知缺失/空项（非迁移丢失）

- 导学单、免抽测附件表当前为 0 行：只需迁结构。
- 预览缺口：附件 17089 vs preview 16640（449 无预览）、答案预览仅 16328/21 万（多数无预览为正常：只 accessory 题型生成）。
- 历史缺失文件基线：未扫（等 P0-D）。P0-D 复制后以“索引有、文件无”清单为准，单独登记，不伪造占位。

## 6. P0-D 执行要点（到时再做）

1. 低峰整库逻辑备份 → 130 独立演练库恢复 → 表数/行数/约束核对。
2. `upload/` 分批复制（保持层级与中文名）→ 两端 SHA 抽查 + 索引存在性全查。
3. 后台副作用任务在 130 保持隔离（判题恢复、通知、MQTT 订阅、账号同步不启用）。
4. 本清单 v1 随演练结果升级 v2（补存在性结论）。
