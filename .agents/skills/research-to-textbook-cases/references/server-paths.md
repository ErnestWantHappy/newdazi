# 130 正式环境路径与边界

| 用途 | 当前路径/配置 |
| --- | --- |
| 后端 API | `xueyeceping-130` systemd 服务，端口 3009 |
| 前端 | Nginx `xueyeceping-130` 站点，前端 root 以现场配置为准 |
| 数据库配置 | `/data/apps/xueyeceping/releases/20261008_seat_install_v1/config/application-druid.yml` |
| 教研私有附件 | `/data/upload-private/research-activity/` |
| 课例共享根 | `/data/apps/xueyeceping/shared/textbook/cases/` |
| 数据库备份 | `/data/backups/xueyeceping/<时间戳>_research_cases_<runId>/database.sql.gz` |
| 正式文件暂存 | `/data/apps/xueyeceping/shared/textbook/cases/.staging/<runId>/` |

130 经 123 跳板，固定 SSH 指纹由 `scripts/deploy/remote130.py` 管理。根目录 `scripts/deploy.py` 面向 123 Windows/NSSM，不得用于 130。

课例文件不得进入 Nginx 默认公开 `/profile`；下载和预览继续由 `TextbookCaseController` 按教师/教研员/管理员身份读取。新 release 独立创建，旧 release、旧 JAR、Nginx 配置、systemd 配置和备份保留；删除只针对本次 staging 里未被数据库引用的精确文件。
