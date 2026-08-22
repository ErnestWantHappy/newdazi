# 架构资料索引

> 默认先读 `contexts/PROJECT_CORE.md`。本目录只维护会影响开发决策的当前架构，不复制完整代码、全表字段或历史聊天。

| 任务类型 | 必读资料 | 需要时继续读 |
| --- | --- | --- |
| 任何开发接管 | `SYSTEM_ARCHITECTURE.md`、`BUSINESS_BOUNDARIES.md` | 对应专题 `contexts/<专题>/` |
| SQL / MyBatis / 数据规则 | `DATA_MODEL.md` | 根目录 `sql/`、相关 Mapper XML |
| CryptPad、Judge0、EMQX/MQTT、实时推送 | `INTEGRATIONS.md` | `contexts/online-collaboration/`、`contexts/python-judge0/`、`contexts/junior-iot-poc/` |
| Python OJ、统一题库、练习题单与班级范围 | `BUSINESS_BOUNDARIES.md`、`DATA_MODEL.md` | `contexts/python-judge0/requirements.md`、`design.md`、`adr/ADR-008-unified-python-question-bank.md`、`adr/ADR-009-unified-practice-plan-class-version.md`、`adr/ADR-010-hard-delete-and-natural-class-label.md` |
| 内网发布、服务排障 | `DEPLOYMENT_RUNBOOK.md` | `AGENTS.md`、当前外置配置和服务器实际探查 |
| 架构级取舍 | `ADR/README.md` | 相关专题 ADR 和历史证据 |

## 文档维护规则

- 当前代码和可复核运行证据优先于文档。
- 重大变更必须更新受影响文档；小改不为了形式制造架构文档噪音。
- 图中的凭据使用“环境变量/私密配置”占位，绝不写真实值。
- 历史发布与排障证据仍在 `contexts/context.md`，按关键词读取，不作为默认上下文。
