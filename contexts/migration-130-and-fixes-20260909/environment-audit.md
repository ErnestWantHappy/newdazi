# P0-A 只读体检：环境审计（2026-09-09）

方法：本机 paramiko 直连 123 只读，再经 123 `direct-tcpip` 跳到 130 只读。
全程未修改、未停止、未重启 123/130 任何服务，未执行 SQL，未安装软件，未格式化磁盘。
凭据仅从 `contexts/secrets.local.md` 读取，回显已脱敏。原始输出存 `output/130audit-*.json`（本机，未提交）。

## 1. 123 当前正式基线（与 PROJECT_CORE 一致，未被本轮改动）

- 服务：`NewDaziBackend3009` Running/Automatic，`UnifiedNginx` Running/Automatic。
- 活动 release：`D:\program\3009dazipingtai\releases\20260907_blankfix_v1`（NSSM AppDirectory）。
- 制品哈希：
  - 后端 `backend\ruoyi-admin.jar` SHA-256 `6B5409AC95AEF446919077E48F8997F3981CC6493A7622BED3B6CFE67A5FC756`（与 v3.46 记录一致）。
  - 前端 `frontend/index.html` SHA-256 `A432C44157F738867B547DBBFC3C1AE9AA8221A598500BEFF65C753D65E1FFDA`（一致）。
  - Nginx 当前 conf SHA-256 `009BE1E1D771A74AA9C347138061BF07C2AA989CDE1932A1D7DB5E42CF0F174B`（与去套娃记录一致）。
- Nginx：`worker_connections 1024`，含 `upstream backend_3009` + `keepalive 64`，配置内已无 `proxy_pass http://127.0.0.1:3010`（去套娃完好）。
- 监听：80 / 3009 / 3010 / 3012 / 3018 / 3019 均 LISTENING；80 上有数十个 ESTABLISHED（上课中，印证不可扰动）。
- 业务库名（外置 `application-druid.yml`）：`ry-vue`（`jdbc:mysql://127.0.0.1:3306/ry-vue`，GMT+8）。
- 磁盘：C 盘剩余约 62GB，D 盘剩余约 554GB（D 已用约 46GB）。
- 文件规模（轻量只读统计）：`uploadPath` 约 7.8GB、46446 个文件；`uploadPath-private` 约 4MB、3 个文件。
- 备份：`backups` 共 68 项，最新为 09-08 去套娃两次备份与 09-07 白卷发布备份。
- 到 130：`Test-NetConnection 10.52.1.130:22` 成功（跳板通路正常）。

## 2. 130 实际安装状态（旧初始化已做完系统层，有现场证据）

- 主机：`ubuntuserver`，Ubuntu 24.04.4 LTS，16 核 / 32GB（沿用交接记录，本轮未重测 CPU 内存）。
- 网络：`ens33` = `10.52.1.130/24`，网关 `10.52.1.254`；SSH 指纹与交接记录一致（已校验，不在此复述）。
- 启动：`system boot 2026-09-08 17:13` 后持续运行，`last reboot` 显示 09-08 16:58→17:13 重启过一次。
  `/data` 重启后仍在，fstab UUID 持久化已由重启验证通过。
- 时区：`Asia/Shanghai (CST, +0800)`，NTP active。日志 8 小时差问题已解决。
- 数据盘：`sdb1` 500G ext4（LABEL=data），挂载 `/data`，`df` 约 492G、可用约 466G；
  fstab 为 `UUID=6f3715e3-… /data ext4 defaults,nofail`；sda LVM 未动（根盘仍 100G，已用 13G）。
- 目录：`/data/{backups,frontend,libreoffice,logs,upload}` 为空待用；
  `/data/mysql` 属主 `mysql`（admin2 不可读，权限正常）；
  `/data/README-migration.txt`（09-08）已写明后续迁移约定。
- 软件（均已安装）：Nginx 1.24.0、JDK 21.0.12、MySQL 8.0.46、Redis 7.0.15、LibreOffice 24.2.7.2。
- 自启与存活：`nginx/mysql/redis-server` 均为 `enabled` + `active`。
- 端口：22（0.0.0.0）、80（0.0.0.0，默认站点，本机 curl 返回 200）、3306/33060（仅 127.0.0.1）、
  6379（仅 127.0.0.1）。无 3009（预期：尚无 Java 业务应用）。
- MySQL 配置（sudo 只读）：`datadir = /data/mysql`，`bind-address = 127.0.0.1`。符合设计。
- Redis 配置（sudo 只读，已掩码）：`bind 127.0.0.1`，`protected-mode yes`，`requirepass` 已设置；
  但持久化 `dir` 仍为 `/var/lib/redis`（在根盘，不在 /data，次要差异）。
  2026-09-09 口令已对齐私密配置记录值并验证通过（见 §5 第 1 项）。

## 3. 129 链路表（只读探测，未改 129 任何配置）

| 依赖 | 130 发起 | 浏览器公开 | 现状 | 说明 |
| --- | --- | --- | --- | --- |
| Judge0 :2358 | 130→129 OPEN | 经平台后端 | 待 P0-D 隔离验证 | 判题回调归属待确认 |
| CryptPad :80 | 130→129 OPEN | 主/沙箱仍为 123:3018/3019 | 阻塞待设计 | 不能只改平台一个 URL，需双入口兼容方案 |
| EMQX :1883 / 管理 :18083 | 130→129 OPEN | 设备经 129 | 待隔离验证 | 演练须用独立 ClientID/命名空间，禁用生产订阅 |
| 123 跳板 :22 | 123→130 TCP 通 | — | 正常 | 仅传输中转，不等于运行时依赖 |

## 4. 空间估算

- 待迁增量：文件约 7.8GB + 整库逻辑备份约 0.2GB（含增长余量按 20GB 估）+ 制品/日志 < 5GB。
- 130 `/data` 可用约 466G，数量级充足。瓶颈不在空间，而在一致性窗口与索引完整性校验。
- 123 D 盘剩余约 554G，中转目录峰值（备份+文件包+制品）无压力；仍须低峰分批限速，避免影响上课。

## 5. 缺项与阻塞（按优先排序）

1. **已解决（2026-09-09）：130 数据服务口令已对齐。**
   经 130 本机管理员通道核实实例仅有初始系统账号、无业务数据、无其他使用方；
   已备份双配置到 130 本地 `/data/backups/cred-sync-20260909/` 后对齐为私密配置记录值，
   TCP 登录验证通过（MySQL 显示库、Redis 返回 PONG）。未动 123/129 任何凭据。
2. **已解决（2026-09-09）：中文字体与转换验收通过。**
   已安装 `fonts-noto-cjk`（Ubuntu 官方源，许可允许），中文条目 30 个；
   中文 docx（含标题/表格/分页）转 PDF 为 3 页，文字完整、无方框，字体嵌入 NotoSansCJK。
   另装 `python3-docx`、`poppler-utils` 仅作验收工具。`javaldx` 警告无害。
3. **已解决（2026-09-09）：130 运行环境完善。**
   新增 `/data/apps/xueyeceping/{releases,shared/config}`；日志轮转 30 天已校验；
   UFW 放行 80/443（保留 22）；经 123 实测 `http://10.52.1.130/` 返回 200，直访通道已通。
   Redis 曾迁 `/data/redis`，因子服务沙箱只写 vendor 路径导致 RDB 落盘失败，已改回默认；
   会话属可重建数据，不占 `/data`。完整站点反代随制品部署。
4. **隔离状态（非问题）：** 130 已跑 `20260909_isolation_v1`（测试库，无生产数据）；
   后台副作用任务全关；生产库恢复、文件复制、协作双人、Judge0 单发、诊断页核对留待最终同步前。
5. **123 侧完好，无需处理：** 去套娃基线、双端哈希、服务状态均与 v3.46 一致；
   80 仍有活跃课堂连接，再次确认本轮零扰动正确。

## 6. 门禁结论

- 130 系统层（时区、数据盘、五软件、自启、目录、129 可达、口令、中文字体、运行环境）**有现场证据，已完成**。
- **不得宣布 130 正式可用**：无业务库、无文件、无后端/前端制品、无诊断接入、无业务验收、无最终同步。
- 下一步：P0-B 日志与迁移清单（正式迁库仍等明确确认）。
