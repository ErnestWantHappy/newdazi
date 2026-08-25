# 部署与回滚约定

## 拓扑

```mermaid
flowchart TB
    Client[校内浏览器 / 设备] --> P[平台主机 10.52.1.123]
    P --> API[NSSM 后端 :3009]
    P --> WEB[Nginx Vue3 :3010]
    API --> Ext[扩展服务 10.52.1.129]
    Ext --> Judge0[Judge0 :2358]
    Ext --> CryptPad[CryptPad]
    Ext --> Emqx[EMQX :1883]
```

## 发布不变量

1. 先读取真实服务器配置、服务状态和目标库状态；历史文档只作线索。
2. 先备份目标库及相关 Nginx/NSSM/外置配置，记录非敏感校验信息。
3. SQL 仅执行仓库 `sql/` 中通过前检的相关脚本；完成后做存在性和重复数据复核。
4. 后端、前端必须发布到新的 release 目录，保留旧 release；不得直接覆盖活动版本。
5. 切换后执行构建/单测、服务探活、关键 API，UI/权限变更再做 Playwright 冒烟。
6. 汇报必须注明当前 release、回滚路径、是否需回滚 SQL、未完成门禁；不得输出凭据。

## 外部服务注意事项

- Judge0、CryptPad、EMQX 是独立部署单元，不能因部署其中一个而重启或改写另一个的卷、网络、端口。
- Judge0 当前容量基线：123 `ruoyi.judge.core-pool-size=10`、`max-pool-size=10`、`queue-capacity=1000`、`judge0.class-concurrency=60`；129 `COUNT=10`、`MAX_QUEUE_SIZE=512`，server 2 CPU/2GiB，worker 10 CPU/16GiB。配置变更必须按 50/100/200/400 阶梯验收。
- 129 回滚只恢复 `/srv/judge0-python/backups/<批次>/` 中的 compose/conf 并重建 Judge0 server/workers；不得顺带停止 PostgreSQL、Redis、CryptPad 或 EMQX，更不得删除卷。
- 123 本机实测 `nssm restart NewDaziBackend3009` 可能停在 `SERVICE_STOP_PENDING` 后留下 `Stopped`。发布脚本应显式 `stop` → 等待状态变为 `SERVICE_STOPPED` → `start`，并在 3009 探活 200 后再继续；NSSM 路径统一用正斜杠，避免 PowerShell/Python 反斜杠转义破坏参数。
- CryptPad 当前内网 HTTP 状态不是正式安全验收；必须明确标识。
- EMQX 与平台 MQTT 接收器的启用是两个独立步骤，均需实测后记录。
