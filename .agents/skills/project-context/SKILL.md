---
name: project-context
description: 接管或收口信息科技学业测评平台的开发任务时，加载当前架构事实、选择相关专题资料，并同步重大变更的上下文与架构文档。
---

# 项目上下文接管

用于本仓库的开发、排障、发布前检查、架构分析或任务交接。它不替代用户的具体需求，不授权生产写操作，也不要求小修改扫描全仓库。

## 开始任务

1. 读取 `AGENTS.md`、`contexts/PROJECT_CORE.md` 和 `docs/architecture/INDEX.md`。
2. 根据任务选择最小相关资料：
   - SQL、Mapper、数据规则：`DATA_MODEL.md` 和对应 `sql/`、Mapper XML。
   - CryptPad、Judge0、EMQX/MQTT、WebSocket：`INTEGRATIONS.md` 与对应 `contexts/<专题>/`。
   - 服务器、迁移、发布：`DEPLOYMENT_RUNBOOK.md` 和 `AGENTS.md`。
3. 先运行 `git status --short`。保留所有既有修改；不要把 `tmp/`、`output/`、备份或上传目录当作默认业务源码。
4. 当文档冲突时，按以下顺序判断：当前代码和可复核数据库/部署证据、最新有日期文档、历史记录。只有需要追溯时才按关键词查询 `contexts/context.md`。

## 开发与验收

- 只改任务范围内的文件，保持 Java 8、RuoYi、Vue3、MyBatis 和已有权限模型的风格。
- 不修改旧 Vue2，除非用户明确点名。
- 外部服务凭据只经环境变量或 `contexts/secrets.local.md` 使用，不写入文档、日志、代码或回复。
- 按 `AGENTS.md` 的验收顺序验证；UI、登录、路由、菜单或实时交互变更才需要浏览器冒烟。

## 重大变更收口

出现业务流程、权限、API/DTO、SQL、部署、外部协议或跨模块改动时：

1. 更新 `contexts/PROJECT_CORE.md` 的事实、验证、风险和下一步。
2. 更新 `docs/architecture/` 的受影响说明和 Mermaid 图。
3. 更新相关专题的需求、设计、任务和 ADR；新长期路线决定再写 ADR。
4. 仅记录已验证结论，并说明 SQL、配置、重启与回滚要求。

小型文案和单点样式改动不需要为了形式更新全部架构文档。
