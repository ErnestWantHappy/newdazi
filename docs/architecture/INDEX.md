# 架构资料索引

> 2026-09-07 当前排查：流程图 AI 建议匹配与恢复存在本地候选修复，未发布；文件版本与流程图提交 ID 必须区分，不能直接 COALESCE。独立证据、待补交关联验证和示例课程访问限制见 `contexts/flowchart-tool/ai-grading-investigation-20260907.md`。

> 2026-09-07 19:35 最新线上事实：后端 `releases/20260907_blankfix_v1`、3010 前端同 release 已完成白卷批改修复发布；备份、健康检查和四角色可用页面证据见 `contexts/PROJECT_CORE.md` 顶部。在线恢复候选已在此前 1.30.9 上线。发布统一走 `scripts/deploy.py`，禁止依据历史 release 名称直接切换。

> 默认先读 `contexts/PROJECT_CORE.md`。本目录只维护会影响开发决策的当前架构，不复制完整代码、全表字段或历史聊天。

> 当前焦点（2026-09-06 交接）：在线协作实施中断，尚未修复。123 的 3018/3019 主/沙箱代理已建立，129 外部地址已调整，但平台 NSSM 仍指向旧 `/cryptpad/`；源码部分改造、SQL 未执行、构建/多人验收/新应用发布均未完成。课堂固定组默认四人并按班跨课持久保存，协作按所选文档数均分且同页设置。先读 `PROJECT_CORE.md` 顶部交接状态、`contexts/online-collaboration/CONTEXT.md` 和 `repair-plan-20260906.md`，不要重复部署脚本或误报修复完成。

| 任务类型 | 必读资料 | 需要时继续读 |
| --- | --- | --- |
| 任何开发接管 | `SYSTEM_ARCHITECTURE.md`、`BUSINESS_BOUNDARIES.md` | 对应专题 `contexts/<专题>/` |
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

> 2026-08-23 新增专题：学生实验工具与题目开放开关（见 `contexts/student-experiment-tools/`）；站点运维与信息展示（见 `contexts/site-ops-and-info/`）。2026-08-24 已补充课程多 Python 题、开放状态保存、协作首次保存、课程自定义运行和物联详情弹窗的当前边界。2026-08-25 已补充操作题当前题目统计、删题前答案归档和 AI 理论费用估算边界。2026-08-27 已补充共享课程服务端能力投影、删除校验顺序和学生端导航布局边界。2026-08-31 画程流程图操作题 v1.27.0、教师工具网关 v1.27.1、小学实验板标准 MQTT v1.27.2 均已完成正式发布；小学物联真实课堂试点仍按专题任务跟踪。2026-09-01 学生导入与诊断治理已随 v1.27.8 正式发布，正式库联合索引已迁移并保留独立回滚脚本。

## 文档维护规则

- 当前代码和可复核运行证据优先于文档。
- 重大变更必须更新受影响文档；小改不为了形式制造架构文档噪音。
- 图中的凭据使用“环境变量/私密配置”占位，绝不写真实值。
- 历史发布与排障证据仍在 `contexts/context.md`，按关键词读取，不作为默认上下文。

## 2026-09-07 在线统计与登录恢复候选

近5分钟活跃账号、部门筛选、登录防重复与发布边界见 `contexts/online-recovery-20260907/requirements.md`、`design.md`、`tasks.md`。本地已验证，等待用户重启服务器后部署；不得视为已上线。
