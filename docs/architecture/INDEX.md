# 架构资料索引

> 整理日期2026-10-05。接管先读 `AGENTS.md` 和 [PROJECT_CORE.md](../../contexts/PROJECT_CORE.md)，再按下表进入专题。
> 最新正式：130 **1.30.37**，release `20261008_seat_install_v1`，助手0.2.1；并发与页面状态修复验收见下方入口。123另为1.30.29，不能混用基线。

## 按任务选择资料

> AOCI 日常调用策略（2026-10-07）：普通开发、分析及提示词生成不自动完整加载或维护索引，先按本页定位源码。仅明确的 AOCI 专项任务进入完整流程，详见 `AGENTS.md` 的“AOCI 调用策略”和 [ADR-002](ADR/ADR-002-aoci-project-memory.md)。本规则覆盖旧文档中的无条件加载要求。

| 任务类型 | 必读资料 | 需要时继续读 |
| --- | --- | --- |
| 任何开发接管 | `SYSTEM_ARCHITECTURE.md`、`BUSINESS_BOUNDARIES.md` | 对应专题 `contexts/<专题>/` |
| 课堂作品更新、新格式、学生个人反馈、长期机位助手 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md`、`INTEGRATIONS.md` | `contexts/classroom-experience-20261002/requirements.md`、`design.md`、`tasks.md`、`verification.md`、ADR-001～003、`adr/ADR-004-student-zero-config-seat-helper.md`；`clients/seat-helper/README.md` |
| SQL / MyBatis / 数据规则 | `DATA_MODEL.md` | 根目录 `sql/`、相关 Mapper XML |
| CryptPad、Judge0、EMQX/MQTT、实时推送 | `INTEGRATIONS.md` | `contexts/online-collaboration/`、`contexts/python-judge0/`、`contexts/junior-iot-poc/` |
| 小学信息科技实验板正式接入、N17 Python代码生成、EMQX班级精确ACL | `INTEGRATIONS.md`、`DATA_MODEL.md` | `contexts/primary-iot-integration/requirements.md`、`design.md`、`tasks.md`、`ADR-001-reuse-existing-emqx-and-dual-tool-entry.md` |
| Python OJ、统一题库、练习题单、课程自定义运行、班级范围与判题容量 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md`、`INTEGRATIONS.md` | `contexts/python-judge0/requirements.md`、`design.md`、`adr/ADR-008-unified-python-question-bank.md`、`adr/ADR-009-unified-practice-plan-class-version.md`、`adr/ADR-010-hard-delete-and-natural-class-label.md`、`adr/ADR-011-queued-400-submissions-on-shared-host.md`、`adr/ADR-012-course-custom-run-non-scoring.md` |
| 操作题作品、批改统计、删题归档、AI 建议与费用估算 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/operation-artifact-ai-grading/requirements.md`、`design.md`、`tasks.md`、`ADR-006-current-question-answers-and-ai-cost-estimation.md` |
| 画程流程图操作题、草稿、结构检查与教师确认 | `SYSTEM_ARCHITECTURE.md`、`BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/flowchart-tool/requirements.md`、`design.md`、`tasks.md`、四份专题 ADR |
| 教师首页共享课程、课程管理能力、学生端顶部导航 | `BUSINESS_BOUNDARIES.md` | `contexts/teacher-course-sharing/requirements.md`、`design.md`、`tasks.md`、`ADR-001-server-authoritative-course-capabilities.md` |
| 通用班级分组、组长、教师端学生桌面、协作小组路由 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md`、`INTEGRATIONS.md` | `contexts/class-grouping-and-desktop/requirements.md`、`design.md`、`tasks.md`、`ADR-001-server-observed-presence-and-class-entry.md`、`contexts/online-collaboration/` |
| 2026-09-02 多功能改造总计划、P0-P1 本地状态、优先级与开发提示词 | 各相关架构文档、`ADR/ADR-001-versioned-classroom-task-state.md` | `contexts/multi-feature-upgrade-20260902/requirements.md`、`design.md`、`tasks.md`、`development-prompts.md`、`handoff-prompt.md` |
| 学生批量导入性能、事务一致性、诊断分级与重复删除幂等 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/student-import-diagnosis-governance/requirements.md`、`design.md`、`tasks.md`、`ADR-001-batched-transactional-import-and-dynamic-diagnosis.md` |
| 内网发布、服务排障 | `DEPLOYMENT_RUNBOOK.md` | `AGENTS.md`、当前外置配置和服务器实际探查 |

| 架构级取舍 | `ADR/README.md` | 相关专题 ADR 和历史证据 |
| 学生实验工具、题目开放开关 | `DATA_MODEL.md` | `contexts/student-experiment-tools/requirements.md`、`design.md`、`tasks.md` |
| 站点运维与信息展示（129 监控、平台更新、Agent 词典、帮助中心配置、版权） | `INTEGRATIONS.md`、`DEPLOYMENT_RUNBOOK.md` | `contexts/site-ops-and-info/requirements.md`、`design.md`、`tasks.md`、`contexts/RELEASE_LOG.md` |
| AI项目记忆、AOCI-CODE安装/接入/维护 | [AOCI_CODE.md](AOCI_CODE.md)、`AGENTS.md` | [开发工具ADR](ADR/ADR-002-aoci-project-memory.md) |
| 登录页双展厅、匿名公开概览 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/login-showrooms-20261005/requirements.md`、`design.md`、`tasks.md`、`verification.md`、`ADR-001-public-summary-only.md` |

> 2026-10-08机位显示及助手安装已发布，备份/下载/页面/回滚见 [1.30.37正式验收](../../contexts/seat-install-release-verification-20261008.md)。实际机房还原基线保存与重启仍待现场操作，详见 `clients/seat-helper/README.md`。

## 当前证据与历史入口

- [课堂体验正式验收](../../contexts/classroom-experience-20261002/release-verification-20261003.md)：1.30.24的规则、迁移、验证和回退。
- [助手启用正式验收](../../contexts/classroom-experience-20261002/seat-helper-release-verification-20261003.md)：1.30.26、HTTP、可选助手及机房未验边界。
- [自动关联正式验收](../../contexts/classroom-experience-20261002/seat-helper-auto-verification-20261005.md)：1.30.27、实际Windows EXE配对、重复关联、教师命名、备份与回退。
- [学校动态性能正式验收](../../contexts/login-showrooms-20261005/performance-verification-20261006.md)：1.30.30、查询一致性、跨缓存周期、独立后端发布与回退。
- [登录展厅正式验收](../../contexts/login-showrooms-20261005/release-verification.md)：1.30.28、匿名摘要与详情权限、五尺寸、备份和回退；正式教研员凭据冒烟及冷查询性能待跟进。
- [平台发布日志](../../contexts/RELEASE_LOG.md)：正式产品版本登记。
- [原核心文档历史归档](../../contexts/PROJECT_HISTORY_20261005.md)：完整保留整理前原文（含同日登录专题更新），按日期/版本追溯。
- [原架构索引历史归档](INDEX_HISTORY_20261005.md)：完整保留原发布提示和专题备注，不作为当前状态。
- `contexts/context.md` 保留较早历史证据；只在需要追溯时按关键词读取。

## 文档维护规则

- 当前源码和可复核运行证据优先于文档；架构页中的旧日期段落仅代表当时，最新覆盖关系以PROJECT_CORE及专题为准。
- 当前事实更新PROJECT_CORE对应段落；INDEX只维护入口，历史过程放归档/专题，不再逐轮堆叠版本状态。
- 重大变更更新受影响架构、需求/设计/任务和ADR；小型样式或文案不制造无关文档修改。
- 图中凭据使用环境变量/私密配置占位，索引不得包含真实密码、Token、Cookie或学生作品。
- AOCI属于本机开发辅助；2026-10-06代码索引十二批累计237条、建立中且未aligned，后续按[AOCI_CODE.md](AOCI_CODE.md)继续。索引对齐不代表业务正确、实际部署成功或全链路验收通过。

## 2026-10-07 本机审查修复入口

三份AI报告逐条结论、用户确认口径和文件清单见 [复核记录](../../contexts/bug-review-20261007.md)；130正式1.30.35发布见 [前版验收](../../contexts/bug-review-release-verification-20261007.md)，最新1.30.36并发与页面状态修复见 [正式验收](../../contexts/bug-race-release-verification-20261007.md)。无结构迁移；AOCI未同步。
