# Judge0 CE Python 部署包

该目录只能部署到扩展服务器 `/srv/judge0-python`，不修改 CryptPad 的 Compose、网络、数据卷或端口。Judge0 API 发布在 `2358`，由宿主机 UFW 仅放行学业测评后端 `10.52.1.123`；平台 Java 后端使用 `JUDGE0_BASE_URL` 与 `JUDGE0_AUTH_TOKEN` 调用，浏览器没有 Judge0 地址或令牌。

## 首次部署顺序

1. 先盘点 Docker、CryptPad、磁盘、内存、端口 `2358`、防火墙和备份目录。
2. 创建服务器专属 `config/judge0.conf`，从模板替换 PostgreSQL、Redis、Rails、Judge0 认证令牌四个随机值，权限设为 `600`。
3. 固定拉取 `judge0/judge0:1.13.1`、`postgres:16.2`、`redis:7.2.4`，记录实际 image digest。
4. 启动 `db` 和 `redis`，执行一次 `docker compose run --rm server bin/rake db:setup`，再启动全部服务并探测 `/system_info`。
5. 安装 `judge0-compose.service`、`judge0-backup.service`、`judge0-backup.timer`，启用自启动和每日备份。
6. 在平台后端外置环境配置 `JUDGE0_MODE=http`、`JUDGE0_BASE_URL=http://10.52.1.129:2358`、`JUDGE0_AUTH_TOKEN`，重启后端后做真实 Python 判题。

固定镜像 `judge0/judge0:1.13.1` 的 isolate 需要宿主机 cgroup v1。Ubuntu 24 默认是 cgroup v2；若真实提交日志出现 `Failed to create control group /sys/fs/cgroup/memory/box-*`，必须安排维护窗口切换 cgroup v1 并重启宿主机，不能通过关闭内存限制绕过。

## 400 人并发容量口径

- 平台保证 400 份学生提交可同时落库并进入异步队列；单台 129 不启动 400 个沙箱进程。
- 123 的 `JUDGE0_EXECUTOR_CORE_POOL_SIZE` / `MAX_POOL_SIZE` 应与本机 `COUNT` 对齐，当前生产基线为 `10`；队列容量为 `1000`。
- 129 当前基线为 `COUNT=10`、`MAX_QUEUE_SIZE=512`，worker 容器上限为 10 CPU / 16GB；不得脱离阶梯压测直接继续增大。
- 课程 Python 题还受 `JUDGE0_CLASS_CONCURRENCY` 约束，生产基线为 `60`，避免整班同时提交被旧的 8 槽限制拒绝。
- 发布按 50/100/200/400 四档验证提交成功率、全部终态耗时、CPU、内存和 Judge0 队列；CryptPad、EMQX 必须同时保持健康。

## 回滚

停止本项目服务：`docker compose down`，不会影响 CryptPad。若镜像升级失败，恢复记录的 digest 并 `docker compose up -d`；如数据库迁移有问题，从 `backups/` 的同时间戳 `pg_dump` 恢复 Judge0 专用 PostgreSQL 卷。平台侧把 `JUDGE0_MODE=disabled` 后重启即可立即停止新判题，已有提交会保留为服务异常，不会写零分。
