# 交接提示词：配置 Ubuntu 10.52.1.130（时区 / 数据盘 / 基础软件）

把下面「从这里复制到 Gemini」整段发给 Gemini。不要改写后再发，以免漏掉跳板、禁区和验收标准。

凭据只在 `contexts/secrets.local.md`，提示词里没有密码。Gemini 回复用户时禁止回显任何密码、Token、私钥。

---

## 从这里复制到 Gemini

你是郑东旭的长期开发代理，正在配置信息科技学业测评平台的新 Linux 机。用户本人不操作服务器。你必须自己经跳板 SSH 完成配置并回报证据。全程用简体中文和用户交流。

### 0. 先读这些（按顺序，不要全仓乱扫）

1. 仓库根目录 `AGENTS.md`
2. `contexts/PROJECT_CORE.md`（只取当前焦点和正式机拓扑）
3. `docs/architecture/DEPLOYMENT_RUNBOOK.md`、`docs/architecture/INTEGRATIONS.md`
4. `contexts/secrets.local.md`（只读，禁止写入 git，禁止把密码贴进聊天、日志、截图、commit）

工作区：`d:\dmwprogram\newdazipingtai\newdazi`  
前端只认 `RuoYi-Vue3`，后端在 `RuoYi-Vue`。本轮 **不要改业务代码、不要发布 123、不要切 DNS、不要动 129 上的 CryptPad/Judge0/EMQX**。

### 1. 本轮目标（必须全部做完，用户已授权）

教科研新开的 Ubuntu `10.52.1.130` 已空机探活。用户要求 **AI 全部搞定、自己不动手**。本轮只做 **迁平台之前的系统层 + 软件安装**，不切生产流量。

必须完成：

1. 时区改为 **Asia/Shanghai**（现在是 UTC，日志会和 123 差 8 小时）
2. 把 **`/dev/sdb` 整盘 500GB** 做成数据盘，挂载 **`/data`**，给 MySQL、上传文件、LibreOffice 用；**不要**把业务数据堆在根分区 100GB 上
3. 安装并设为开机自启：Nginx、JDK 21、MySQL 8、Redis、LibreOffice（无界面/headless）
4. 在 `/data` 建好目录并授权，写好简要说明：以后 123 的库、上传目录、转换临时目录迁到哪里
5. 验证 130 仍能访问 129 的 80/1883/2358/18083
6. 用非敏感证据向用户汇报（命令退出码、`timedatectl`、`df -h`、`ss -lnt`、版本号）。禁止输出密码。

本轮 **不要**：

- 从 123 拷贝业务库、上传文件、切 `xxkj.xsedu.net.cn` 解析
- 在 123 上改 Nginx / NSSM / 停 Java
- 把 Judge0、CryptPad、EMQX 装到 130 或改 129
- 在 123 的 Windows 里套虚拟机
- `git push`、删库、对 80 端口主站做切换

迁 123 生产的具体步骤可以写在汇报末尾当「下一步计划」，等用户再点头才执行。

### 2. 已核实的现场事实（2026-09-08，以 SSH 为准）

**跳板**

- `10.52.1.130` **只能从** `10.52.1.123` 连 SSH。本机不要假设能直连 130。
- 123：Windows Server 2019，用户 `Administrator`，密码在 `secrets.local.md`「内网服务器 10.52.1.123」。
- 本机连 123：仓库已有 `scripts/deploy_transport.py`（paramiko + 固定指纹 `uXOH9wM5raU+ngVjFjZr3GtCK4rTL27ydlXCI3qR5yU`）。Windows 自带 OpenSSH 远程 exec 可能挂死，优先 paramiko 或 Git 的 `ssh.exe`。
- 123 → 130：ICMP ping 可能失败，**TCP 22 已通**。用 paramiko `direct-tcpip` 经 123 打开到 `10.52.1.130:22` 的通道再登录。
- 130 SSH：用户 `admin2`，密码在 `secrets.local.md`「Ubuntu 扩展服务器 10.52.1.130」。
- 130 主机密钥 SHA256（Base64，无填充）：`DiFXqb+9fyOV/iZWLVSKjOe7UCPyX8lreNgjd20QfOI`。不匹配必须停，不要盲目覆盖。
- `admin2` 在 `sudo` 组，**sudo 要密码**（无 NOPASSWD）。用 secrets 里的同一密码做 sudo，不要把密码写进 130 上的脚本明文文件；用完不要留在世界可读文件里。

**130 硬件（空机）**

- 主机名 `ubuntuserver`，Ubuntu 24.04.4 LTS，VMware
- 16 核 Xeon Silver 4316，32GB 内存，约 30GB 空闲
- 网卡 `ens33`：`10.52.1.130/24`，网关 `10.52.1.254`
- 根分区：`/dev/mapper/ubuntu--vg-ubuntu--lv` ext4 **100GB**（sda LVM，已用约 11GB）
- `sda` 500GB，VG 里大约还有 400GB 未划给 LV → **本轮不要动 sda/LVM**，留给以后扩系统盘
- `sdb` **500GB 整盘空白**（无分区）→ **本轮只用这块做 `/data`**
- 已装：Python 3.12。未装：nginx / java / mysql / redis / docker / soffice
- 对外监听几乎只有 SSH 22
- 130 能 ping 通 123、129；对 129 的 **22、80、1883、2358、18083 均能连**

**123 上仍在跑的生产（禁止弄停）**

- 测评后端 NSSM `NewDaziBackend3009`，Java 21，`-Xms512m -Xmx2048m`，端口 3009
- Nginx `UnifiedNginx` 监听 80/3010/3012/3018/3019
- 正式前端/后端 release：`D:\program\3009dazipingtai\releases\20260907_blankfix_v1`
- MySQL `127.0.0.1:3306` 库名以 123 外置 `application-druid.yml` 为准（常见 `ry-vue`）
- Redis 本机 6379
- LibreOffice 在 123 上跑过，Windows 上有崩溃记录；迁 Linux 后应更稳，但本轮只安装不切流量
- 123 调用 129：Judge0 `:2358`、CryptPad `:80`、EMQX `:1883` 与管理 API `:18083`

### 3. 连接方法（必须自己连，不要把命令甩给用户）

推荐：本机 Python paramiko 先连 123，再 `open_channel("direct-tcpip", ("10.52.1.130", 22), ("127.0.0.1", 0))` 连 130。密码只从 `secrets.local.md` 读取。

在 130 上跑需要 sudo 的命令时，用 `sudo -S` 从 stdin 喂密码，或 SSH 后单次特权会话。输出里若出现密码，必须擦掉再写给用户。

先做只读确认（hostname、`lsblk`、`df -h`、`timedatectl`）再改系统。每一步：说明目的 → 执行 → 贴非敏感证据 → 再下一步。不要一次丢 20 条命令也不看结果。

### 4. 系统层怎么做（按这个做，不要另起花样）

**4.1 时区**

```bash
sudo timedatectl set-timezone Asia/Shanghai
timedatectl
date
```

验收：`Time zone: Asia/Shanghai`，`date` 为东八区，与今天北京时间一致（不要再显示 UTC）。

**4.2 数据盘 `/dev/sdb` → `/data`**

约束：

- 只动 **`/dev/sdb`**。禁止 mkfs/parted `sda`，禁止 `lvextend` 根分区。
- 整盘一个分区，ext4，挂载 `/data`
- 写入 `/etc/fstab` 用 **UUID**，reboot 后仍在
- 目录（属主先 `admin2`，装完 MySQL 后再把数据目录交给 `mysql`）：

```text
/data/mysql
/data/upload
/data/libreoffice
/data/logs
/data/backups
```

建议步骤：`lsblk -f` 确认 sdb 无文件系统 → `parted` gpt + 主分区 → `mkfs.ext4 -L data /dev/sdb1` → `blkid` 取 UUID → 写 fstab → `mkdir /data && mount -a` → `df -hT /data` 约 500GB。

做完必须 `findmnt /data` 和 `df -h /data`。若 sdb 不是空盘（已有分区/文件系统），**停止并汇报**，不要格式化。

**4.3 软件安装（Ubuntu 24.04 仓库即可，不要乱加来源，除非仓库没有 JDK 21）**

安装：

| 软件 | 要求 |
| :--- | :--- |
| Nginx | 官方/发行版稳定版，开机自启，先用默认站点证明 80 本机可访问即可，**不要**把 80 对校园放开到抢 123 的域名 |
| JDK | **21**（123 生产是 jdk-21）。`java -version` 必须是 21 |
| MySQL | 8.x，**数据目录放到 `/data/mysql`**，不要默认堆 `/var/lib/mysql` 在根盘。root 密码设置为Xsdata@123qwe，**只写入 123 跳板侧或本机 `contexts/secrets.local.md` 的 130 段**，禁止写入 git 跟踪文件、禁止在聊天里打印。本轮 **不要** 从 123 导库 |
| Redis | 本机 127.0.0.1:6379，设密码，同样只写 secrets.local，bind 不要对全网 0.0.0.0 裸奔 |
| LibreOffice | 安装 writer/calc 等能 `--headless` 转换的包。验收：`soffice --version` 或 `libreoffice --headless --version` 成功。临时目录指向 `/data/libreoffice` |

`systemctl enable --now` 对应服务。UFW：若开启防火墙，先保证 **22 从 123 可入**；80/3010/3306 本轮不必对全校开放。3306/6379 只听 127.0.0.1。

apt 若要确认，使用非交互：`DEBIAN_FRONTEND=noninteractive`。

装完探测（在 130 本机，不要对校园发公告）：

- `nginx -t` 且 `ss -lnt | grep ':80'`
- `java -version`
- `mysqladmin ping`（用你设置的本地凭据，输出里打码）
- `redis-cli -a <密码> ping` 只报 `PONG`，不要把命令行密码写进用户可见日志；可用 `REDISCLI_AUTH` 环境变量
- LibreOffice headless 转一个最小 txt/docx 到 `/data/libreoffice` 成功
- 再测 129：80、1883、2358、18083 仍通

### 5. 给后续「从 123 迁移」预留的说明（只写计划，本轮不迁）

在 `/data/README-migration.txt`（不要写密码）写明：

- 前端静态将放到例如 `/var/www/xueyeceping/frontend` 或 `/data/frontend`
- Java 上传目录 → `/data/upload`（对应现在 123 的 `D:/program/3009dazipingtai/uploadPath`）
- MySQL datadir → `/data/mysql`
- LibreOffice 工作目录 → `/data/libreoffice`
- 下一步才做：123 整库 mysqldump、rsync 上传目录、Nginx 反代 3009、改域名解析；**129 服务保持不动**，130 后端环境变量指向 129 的 Judge0/CryptPad/EMQX

### 6. 汇报格式（给用户看）

简体中文，分条：

1. 时区是否已是东八区（贴 `timedatectl` 关键行）
2. `/data` 是否约 500GB、fstab 是否 UUID、reboot **不是必须**，若未重启要说明 `mount -a` 已成功；**不要为了验证擅自 reboot 除非你能保证 SSH 跳板回来**（130 只从 123 进，reboot 前必须确认网卡 DHCP/静态 IP 仍是 10.52.1.130）
3. 五个软件版本与自启状态
4. 对 129 端口仍通的证据
5. secrets.local.md 是否已追加 130 的 MySQL/Redis 密码（只说「已写入、未提交」，不要贴密码）
6. 没做的：未切 DNS、未迁 123 数据
7. 下一步建议（等用户确认再迁库）

IP 保持 `10.52.1.130`。改主机名可以改为 `xueyeceping-130`，不是必须。

### 7. 失败就停

- 连不上 130、指纹不对、sdb 不是空盘、sudo 失败、apt 把系统依赖拆了、误动 sda、误连 123 生产服务导致中断 → **停下来用中文说明**，不要继续「尝试另一种破坏性方案」。
- 写 secrets.local.md 时只追加 130 的 MySQL/Redis 段落，不要改 123/129 旧密码，不要提交该文件。

现在开始：先跳板连上 130 做只读确认，再改时区，再做数据盘，再装软件。

---

## 复制结束
