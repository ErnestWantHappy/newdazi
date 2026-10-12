# AGENTS.md — 信息科技学业测评平台

> 给 **AI 代理 + 人工开发** 的操作手册：怎么启动、改哪里、怎么验收、什么不能碰。  
> **当前业务真相与在研焦点以 `contexts/PROJECT_CORE.md` 为准**；架构入口见 `docs/architecture/INDEX.md`。`contexts/context.md` 保留为历史事实索引，不是默认必读全文。

---

## 1. 项目一句话

中小学信息科技 **教学 + 多维度测评** 平台（选择/判断/操作/打字、批改、学情、导学单、区域抽测等）。  
技术栈：后端 **RuoYi / Spring Boot（Java）**，前端 **仅维护 `RuoYi-Vue3`**。

---

## 2. 目录约定（只认这些）

| 路径 | 用途 |
| :--- | :--- |
| `RuoYi-Vue/` | **后端** monorepo（`ruoyi-admin` 启动模块，`ruoyi-business` 业务） |
| `RuoYi-Vue3/` | **唯一前端**（Vue3 + Vite + Element Plus） |
| `sql/` | 仓库级增量 SQL（优先看这里） |
| `contexts/PROJECT_CORE.md` | 多 AI / 人工接力的短小当前事实；**任务开始必须先读** |
| `contexts/context.md` | 历史发布、排障与证据索引；仅在需追溯冲突或旧决策时按关键词读取 |
| `docs/architecture/` | 系统、数据边界、外部集成、部署与 ADR 的可读架构资料 |
| `.agents/skills/project-context/` | 仓库级上下文接管与重大变更收口流程 |
| `contexts/secrets.local.md` | **本机私密凭据**（密码/Token，**已 gitignore，禁止提交**） |
| `output/playwright/` | 浏览器验收截图 / storage state 等产物 |
| `output/stress/` | 本机并发/交卷压测脚本与报告 |

**不要默认改：**

- `RuoYi-Vue/ruoyi-ui/`（旧 Vue2 前端，**本项目默认不认**；除非用户明确点名）
- 与当前 `contexts/context.md` 焦点无关的模块（例如焦点是 LibreOffice 时 **勿混改导学单 / guide-sheet**）
- 无必要的大范围重构、换技术栈、改核心权限模型

---

## 3. 本地启动（默认环境）

### 3.1 端口

| 服务 | 地址 |
| :--- | :--- |
| 后端 API | `http://localhost:8080` |
| 前端 dev | `http://localhost`（Vite `port: 80`，代理到 8080） |

配置参考：`RuoYi-Vue/ruoyi-admin/.../application.yml`、`RuoYi-Vue3/vite.config.js`。

### 3.2 后端

工作目录必须是 **`RuoYi-Vue`**：

```bash
# 开发常用：编译并打 fat jar（改 yml/资源后务必 clean package）
mvn -pl ruoyi-admin -am clean package

# 运行（内存可按机器调整）
java -Xms512m -Xmx2048m -jar ruoyi-admin/target/ruoyi-admin.jar
```

### 3.3 前端（只认 Vue3）

工作目录 **`RuoYi-Vue3`**：

```bash
npm install   # 首次
npm run dev
```

浏览器打开：`http://localhost`（若 80 端口被占用，以终端实际端口为准，并在验收时写明）。

### 3.4 环境策略

- **默认开发/联调：本机**（本地库 + 本地前后端）。
- **内网正式联调机**：`10.52.1.123`（后端 **3009**、Vue3/Nginx **3010**；发布根目录 `D:\program\3009dazipingtai\`）。非机密说明写在 `contexts/context.md`；**密码只在** `contexts/secrets.local.md`。
- Redis / MySQL / LibreOffice 等依赖以目标环境配置为准；缺依赖时先查服务器配置再问用户。

### 3.4.1 凭据存放（2026-07-22 已定）

| 内容 | 写哪里 |
| :--- | :--- |
| IP、端口、发布路径、库名、**用户名**、角色验收账号（本地） | `contexts/context.md` / 本文 |
| Windows / SSH / MySQL **密码**、Token、Cookie | **仅** `contexts/secrets.local.md`（gitignore） |
| 聊天回复、日志、截图、commit message | **禁止**出现密码/Token |

### 3.4.2 远程代操作（最大程度减轻用户手动操作）

用户授权原则：**涉及线上/内网排障、表结构核对、迁移、发布时，代理应主动用 SSH/远程会话完成查库、备份、执行 SQL、上传制品与部署，而不是把长串命令甩给用户手敲。**

**在任务已指向内网 `10.52.1.123`（或用户本轮明确「线上/服务器」）时，下列操作视为已获站立授权，代理应自行执行并回报结果：**

1. **只读探查**：SSH/远程登录；读 Nginx/NSSM/发布目录；读外置 `application*.yml`；MySQL 只读查询表结构、菜单、角色、版本差异。  
2. **写库前备份**：整库或相关库 `mysqldump`（或等价）到 `D:\program\3009dazipingtai\backups\<时间戳>_\<git短哈希>\`，计算 **SHA-256** 并写入汇报。  
3. **执行增量 SQL**：仅执行仓库 `sql/` 中与任务相关、且前置检查通过的脚本；优先幂等；执行后做存在性/重复组复核。  
4. **构建与发布**：本机或服务器打包；上传 jar/前端 dist 到新 `releases\<时间戳>_<hash>\`；**不覆盖**旧 release；切换 NSSM 工作目录/ jar 与 Nginx 根后重启并探活。  
5. **回滚准备**：每次发布保留上一版路径；汇报中写明「如何切回旧 release + 是否需要跑回滚 SQL」。

**仍须先口头/书面确认再做的（破坏面过大）：**

- `git push` / force-push、改远程分支保护  
- 删库、清空业务大表、批量改学生/成绩且无法按备份秒级回滚的操作  
- 改 80 端口主站（若与 3010 并行存在）或动他人无关服务  
- 删除旧 `releases` / 旧备份以腾磁盘  

**远程操作纪律：**

- 先说明本步目标 → 执行 → 贴**非敏感**证据（退出码、行数、HTTP 状态、菜单条数），再进入下一步。  
- 回复中 **永不回显** `secrets.local.md` 中的密码。  
- 能本机复现的编译/单测优先本机；只有菜单/数据/发布状态必须以服务器为准时再连远程。

### 3.5 本机数据库迁移基线（2026-07-30）

当前本机开发库为 `xueyeceping_server_20260729`，由内网服务器 `ry-vue` 完整复制后执行 `sql/teaching_supervision_v1.sql` 和 `sql/teaching_exemption_v1.sql` 得到；后端 `application-druid.yml` 已切换到该库。旧库 `xueyeceping1` 完整保留，不再作为当前联调基线。

复制源备份位于 `D:\dmwprogram\newdazipingtai\backups\20260729_180020_server_before_local_clone_d827415\ry-vue_server_full.sql`，62,283,806 bytes，SHA-256 `350E73232CB17F0C4D8EC0B973EAE47837976B1752E70ABE8CE686ADA5236BED`。

免抽测迁移前的本机备份位于 `D:\dmwprogram\newdazipingtai\backups\20260730_233001_local_before_teaching_exemption_d827415\xueyeceping_server_20260729_before_teaching_exemption.sql`，62,678,787 bytes，SHA-256 `8095118C6486663DF9EF98F3AF14A4E5C51565F4AF79E1A68F8F5071371D5354`。

复核结果：课程班级事实 873 条、当前事实 129 条、历史批改期限 473 条；免抽测 5 张表已创建，测试申请、班级快照、课程快照均已精确清理为 0；重复事实、重复期限、重复免抽测标准/申请、零分母期限、无操作题期限、孤儿事实和快照汇总异常均为 0。研究员拥有监管 4 项和免抽测审核/标准权限，教师拥有申请权限，学生免抽测权限为 0。206 门历史课程缺 `create_time`、225 门缺 `update_time`，不得以执行迁移当天时间回填；课程 139、158、246 的时间证据仍待治理。课程 1 的归属和课程 34 的 4 条跨学校指派冲突同样保持待治理。

以上仅代表 **2026-07-30 本机库状态**。正式服务器尚未执行本轮监管和免抽测迁移，发布时仍须分别重新前检、备份、执行增量 SQL 和后检，不能因本机已完成而跳过。

---

## 4. 角色与本地验收账号

角色：**管理员 / 教师 / 学生 / 教研员**。

### 4.1 文档内账号（本地联调 / 浏览器冒烟）

| 角色 | 账号 | 密码 | 说明 |
| :--- | :--- | :--- | :--- |
| 教师 | `19157727791` | `zdx5201ZDX!` | 教师端菜单、批改、指派等 |
| 教研员 | `laoda` | `123456` | 教研员菜单与权限冒烟 |
| 学生（本机常规课/压测） | `st99_363` | `123456` | 2026-07-15 实测可登录；`dept_id=167`、`class_code=99`，当前常规课 `lesson_id=239` |
| 学生（本机 0 题考勤） | `2020710101` | `123456` | 2026-07-15 实测可登录；`dept_id=139`、`entry_year=2020`、`class_code=1`，当前考勤课 `lesson_id=250` |

130 正式环境教研员凭据以 `contexts/secrets.local.md` 为准；上表仅是本机联调账号，不可用于正式环境登录验证。

管理员账号以当前库 `sys_user` 为准；未在本文写死时 **向用户索取**，不要猜。

### 4.2 安全约定

- 上表仅供 **本地联调**；内网服务器 Windows/MySQL 密码 **只写** `contexts/secrets.local.md`，**禁止**写入 `context.md`、公开文档或 git 跟踪文件。
- 浏览器自动化日志、截图、`output/` 中 **不要打印完整密码**；需要 storage state 时写入 `output/playwright/` 本地文件即可。
- 正式环境账号/密码变更：改 `secrets.local.md`；`context.md` 只记「已轮换」与用户名/主机。
- `st99_363` 已完成本人作品可读、匿名读取业务码 401、跨学生读取 403，以及同一答案 12 并发重复提交仍保持单行的验证。
- `2020710101` 已完成 0 题考勤课签到和重复签到验证；签到后未生成作业答案，当前课未被推进。

---

## 5. AI 改代码边界（严格）

1. **先读** `contexts/PROJECT_CORE.md` 和 `docs/architecture/INDEX.md`；只有需要追溯旧决策、发布或排障证据时，才按关键词读取 `contexts/context.md`。
2. **接管顺序固定为**：`AGENTS.md` → `contexts/PROJECT_CORE.md` → `docs/architecture/INDEX.md` → 任务相关专题；不要为明确的小改重复全仓扫描。
3. **只改任务相关文件**；最小可用修改，不顺手重构。
4. 业务规则、表结构、接口语义变更 → **同步更新 `contexts/PROJECT_CORE.md`**（版本与日期）；需要追溯证据时再追加或链接 `contexts/context.md`。
5. SQL 放 `sql/`，尽量幂等；说明是否需在目标库执行、是否要重启。
6. 注释用 **简体中文**，解释「为什么」，避免废话。
7. 方案级选择（换栈、大改库表、大范围重构）→ **先给 2～3 方案并等用户确认**，再动手。
8. **每轮出现重大修改时，AI 必须在本轮结束前主动维护项目核心上下文。**重大修改包括：改变核心业务流程、接口或 DTO 语义、数据库表结构或迁移、权限边界、部署配置、跨模块行为，或一个完整功能/热修集中修改多个关键文件并需要系统回归。必须更新 `contexts/PROJECT_CORE.md` 的当前状态、验证证据、SQL/配置/重启要求、剩余风险与下一步；同步 `docs/architecture/` 中受影响图或说明。涉及专题需求、设计、任务或架构决策时，还必须同步对应 `requirements.md`、`design.md`、`tasks.md` 和 ADR。只写已经验证的事实；上下文未更新不得宣称任务完成。
9. 小型文案或单点样式修改可不升级核心上下文，除非改变业务含义。
10. 不得把密码、Token、Cookie 或私密路径内容写入上下文；私密凭据仍只保存在 `contexts/secrets.local.md`。
11. 交接前必须确保新 AI 只读 `contexts/PROJECT_CORE.md` 和 `docs/architecture/INDEX.md` 即可知道当前真实焦点、已验证状态和下一步。

---

## 6. 验收优先级（硬顺序）

改完代码按顺序做，能停在更早一层就不要强上 UI：

1. **编译 / 单测**  
   - 后端示例：`mvn -pl ruoyi-business -am test` 或任务相关模块测试  
   - 打包：`mvn -pl ruoyi-admin -am clean package`（改配置/资源时保留 `clean`）  
2. **接口 / 服务探活**  
   - 后端是否监听 8080；关键 API 是否 200（可用 curl 或已有 `output/stress` 脚本）  
3. **（可选）浏览器冒烟** — 见第 7 节  
4. **性能相关** 才跑 `output/stress/`（交卷并发、LibreOffice 等）

**不要**用 Playwright 替代 Java 单测；**不要**每次小改都跑全链路 E2E。

---

## 7. 浏览器验收（webapp-testing / Playwright）

### 7.1 能力说明

`webapp-testing`（Playwright）**可以**：打开登录页 → 填账号 → 点击菜单/按钮 → 模拟 **教师 / 教研员 / 学生** 使用 → 截图、保存登录态。

**定位：可选验收**，不是写业务代码的引擎。  
在以下情况启用：改了登录/路由/菜单/关键页面交互、用户要求「打开页面验一下」、需要多端截图留证。

### 7.2 默认冒烟范围（登录 + 各进 1 个核心页）

| 角色 | 账号 | 最低通过标准 |
| :--- | :--- | :--- |
| 教师 | `19157727791` | 登录成功 → 进入教师侧核心页（如首页或批改/课程相关入口）无白屏、无持续 500 |
| 教研员 | `laoda` | 登录成功 → 进入教研员可见菜单页，路由可用 |
| 学生 | 本机可用学生号（如 `st99_001`） | 登录成功 → 学生任务/首页可见；无账号则跳过并注明 |

完整业务链（指派 → 答题 → 交卷 → 批改）**仅专项任务**执行，不作为每次默认。

### 7.3 产物

- 截图 / 失败现场：优先 `output/playwright/`  
- 可复用登录态：`output/playwright/*-state.json`（勿提交敏感 token 到公共远端，若仓库私有可按团队约定）

### 7.4 前置条件

前后端已启动、账号在当前库有效、本机已安装 Playwright 浏览器运行时（`playwright install` 按需）。  
未满足时先启动/询问，不要假装测过。

---

## 8. 危险操作与远程授权边界

### 8.1 内网代操作（见 3.4.2，任务指向线上时直接做）

对 **`10.52.1.123` 学业测评发布栈（3009/3010）**：SSH 探查、备份后增量 SQL、上传新 release、切换 NSSM/Nginx、重启并探活 —— **以减轻用户手操为第一目标**，按 3.4.2 站立授权执行，做完用非敏感证据汇报。

### 8.2 仍须先问用户

- `git push`、force-push、改远程分支保护  
- 删库、无法快速回滚的批量改成绩/删学生  
- 删除分支、`reset --hard`、覆盖他人未提交工作  
- 向外部渠道发消息、开 PR（若用户未要求）  
- 改 80 端口主站或与本项目无关的服务  

本机可逆操作（改代码、跑测试、本地启停自己起的进程）可直接做；破坏性本机操作（大删 `uploadPath`、清库）仍需确认。

---

## 9. 常用命令速查

```bash
# 后端测试（业务模块）
cd RuoYi-Vue
mvn -pl ruoyi-business -am test

# 后端打包
mvn -pl ruoyi-admin -am clean package

# 前端
cd RuoYi-Vue3
npm run dev
npm run build:prod
```

### 9.0 本机（Windows 开发机）执行环境坑（2026-09-16 实测）

首次只改本地代码、不碰服务器时，按下面写法跑，能少踩四个坑：

```powershell
# 1) 本项目 PowerShell 前台调用常不回显 stdout，用后台执行再取输出：
#    以 run_in_background 启动，随后用 TaskOutput 读取。
# 2) 只跑指定测试类时必须加 -Dsurefire.failIfNoSpecifiedTests=false，
#    否则其他模块“无匹配用例”会让整轮 build 失败。
# 3) 本机 surefire fork JVM 会启动失败（forked VM terminated without properly
#    saying goodbye / Error occurred in starting fork），必须加 -DforkCount=0
#    在进程内跑；这是环境问题，不是用例失败。
Set-Location "D:\...\RuoYi-Vue"
mvn -pl ruoyi-business -am test "-Dtest=Iot*Test" `
  "-Dsurefire.failIfNoSpecifiedTests=false" "-DforkCount=0"
```

- Bash 工具在本机不可用（报 `error launching git`），一律改用 PowerShell。
- 修改 `application.yml` 等资源后要带上 `clean`：`mvn -pl ruoyi-admin -am clean package`。
- 需要“默认不改动生产行为”的功能，用**默认关闭的配置开关**（如 `iot.mqtt.downlink-enabled=false`），并在 `tasks.md`/ADR 写明打开步骤。

### 9.1 正式发布统一入口（2026-09-07 起）

正式机发布不再临时拼接 SSH/PowerShell。统一从仓库根目录调用 `scripts/deploy.py`，脚本使用固定 SSH 主机指纹、远端 SYSTEM 计划任务和阶段状态文件；SSH 断开后用 `status` 查询，不重复启动发布。`check` 只做制品/线上基线/服务/数据库鉴权/Nginx/集成健康检查，`deploy` 才会创建新 release、整库备份、切换并在失败时自动回滚。

```powershell
# 先看服务器真实指向和服务状态（只读）
python scripts/deploy.py inspect

# 只读发布门禁；--baseline-index 必须填现场核对得到的线上 index.html SHA-256
python scripts/deploy.py check --release 20260907_blankfix_v1 `
  --artifacts output/deploy-20260907-1330 `
  --baseline-index A4B7EAB6EA981E3D22901F28A6B5F54588C2182E6E2379E194F031C0F91BBA51 --wait 30

# 正式发布：备份写入服务器 backups/<jobId>/，失败自动恢复旧前后端和服务日志配置
python scripts/deploy.py deploy --release 20260907_blankfix_v1 `
  --artifacts output/deploy-20260907-1330 `
  --baseline-index A4B7EAB6EA981E3D22901F28A6B5F54588C2182E6E2379E194F031C0F91BBA51 --wait 180

# SSH 中断或稍后接续
python scripts/deploy.py status --job <jobId>

# 仅对已记录备份且当前确实为该 release 的版本回滚
python scripts/deploy.py rollback --release 20260907_blankfix_v1 --backup D:/program/3009dazipingtai/backups/<jobId> --wait 180
```

制品目录必须包含 `ruoyi-admin.jar`、`frontend.zip`、`manifest.json`；发布前不得把工作区其他未收口改动混入制品。脚本会拒绝基线漂移、清单不一致、环境变量数量变化、Nginx 候选语法错误和不完整数据库备份；数据库脚本仍须单独放在 `sql/`，没有明确迁移时不会执行任何业务 SQL。变更脚本本身可用 `python scripts/test_release.py` 做本机 4 条故障恢复模拟。

增量 SQL 示例目录：`sql/`（如 `libreoffice_health_check_quartz_job.sql` 等）。  
执行 SQL 前确认目标库是本机还是服务器。

---

## 10. 任务收尾检查清单

- [ ] 改动范围与 `PROJECT_CORE.md` 当前焦点一致，无跨模块误伤
- [ ] 编译/相关单测通过（或说明无法跑的原因）  
- [ ] 若动 UI/权限/登录：完成第 7.2 冒烟或说明跳过原因  
- [ ] 需要的 SQL / 配置 / 重启步骤已写明  
- [ ] 业务结论变更已写入 `contexts/PROJECT_CORE.md`；需要保留审计证据时已链接或追加到 `contexts/context.md`
- [ ] 若属重大修改：已更新 `contexts/PROJECT_CORE.md` 的版本、日期、焦点、事实、证据、发布/回滚状态、风险与下一步，并同步受影响的架构资料
- [ ] 若影响专题需求/设计/任务/架构：已同步对应 `requirements.md`、`design.md`、`tasks.md` 和 ADR
- [ ] 上下文只包含已验证事实，不含密码、Token、Cookie 或私密路径内容
- [ ] 新 AI 只读 `contexts/PROJECT_CORE.md` 和 `docs/architecture/INDEX.md` 即可知道当前真实焦点
- [ ] 向用户汇报：改了什么、怎么验、剩余风险、下一步  
- [ ] **正式发布登记（130必填，123历史发布同样适用）**：每次正式服务器部署验收成功后，必须自动在 `contexts/RELEASE_LOG.md` 登记日期、版本号、改动摘要，并将对应 `biz_platform_update` 记录设为 `PUBLISHED`，用真实教师的 `/business/platform-update/list` 接口确认可见。只写DRAFT不算完成；用户已授权部署时，配套发布更新记录无需再次确认。仅用户明确要求暂不公开时才保留草稿并注明。失败或回滚的候选不得宣称已上线；仅本机修改、网络维护不生成产品更新版本。

---

## 11. 文档关系

| 文件 | 职责 |
| :--- | :--- |
| **本文件 `AGENTS.md`** | 怎么干活：目录、启动、账号、验收、远程代操作、红线 |
| **`contexts/PROJECT_CORE.md`** | 当前业务事实、焦点、已验证状态、风险与下一步 |
| **`docs/architecture/`** | 当前系统、数据、集成、部署边界与 ADR 索引 |
| **`contexts/context.md`** | 历史发布、排障、回滚与决策证据索引 |
| **`contexts/secrets.local.md`** | 密码与 Token（gitignore，禁止提交） |
| **`contexts/*.md` 其它** | 专题方案、部署、接力提示词 |


## 用户交互语言要求

- 与用户交流时必须使用简体中文。
- 所有任务计划、进度说明、分析结论、测试结果和最终总结必须使用简体中文。
- Bash、PowerShell、Git、npm 等命令本身保持原样，不需要翻译。
- 命令执行结果如果是英文，需要用简体中文解释其含义。
- 不要使用英文向用户描述“我接下来要做什么”。
- 技术术语可以保留英文，但第一次出现时用中文解释。
- 用户不需要理解详细开发过程，过程汇报应简短、通俗。
- 每完成一个重要阶段，用大白话说明：
  1. 做了什么
  2. 成功还是失败
  3. 有没有问题
  4. 下一步做什么

冲突时：**具体业务以 context 为准；操作以 AGENTS 为准。**

## AOCI 调用策略（2026-10-07：日常按需，优先于下方托管区块）

用户已要求减少每次任务的索引等待。本节决定**是否进入 AOCI 工作流**；下方工具生成区块只在明确进入 AOCI 专项任务后适用，不再作为所有任务的启动前置条件。工具升级或重新生成托管区块时保留本节。

1. **默认日常模式**：文案、样式、单点修复、功能开发、只读分析、生成检查提示词，以及限定日期的改动梳理，均不自动调用 `aoci_rules`、普通 `aoci_overview` 或 `aoci_maintain`。先读本文件、`contexts/PROJECT_CORE.md`、`docs/architecture/INDEX.md`，再按任务定位相关源码和必要调用链；不默认扫描全仓、读取完整历史或全文索引。
2. **近期改动任务**：先查看指定日期的 Git 日志、相关 diff 和当前未提交/未跟踪文件，再阅读涉及的 Vue 页面、API、Controller、Service、Mapper 及相关业务约束。Git 日期只用于定位，最终结论以当前源码为准；需要时沿调用链扩大范围。
3. **高风险任务**：权限、身份、成绩、提交/退回/重交、跨模块状态及数据迁移，必须补读相关架构与业务规则、核对真实源码和测试；风险高不自动等于必须全量 AOCI。不能因省去索引加载而减少必要证据。
4. **AOCI 专项模式**：只有用户明确要求建立、继续维护、验证索引或使用 AOCI 完整理解项目，才进入下方托管流程。用户仅提到 AOCI、询问耗时或要求调整接入规则，不等于授权启动全量加载。普通的“理解项目”或“完整阅读项目”要求也不自动触发 AOCI。
5. **压缩与恢复**：日常模式在上下文压缩后重读核心资料和任务相关源码，不因此启动全量 Overview；不得声称保留可靠完整 AOCI 认知。已进入的 AOCI 专项流程才遵循下方压缩恢复与完整传输规则，不能把局部读取冒充完整验证。
6. **维护与真实性**：日常任务继续按原规范同步业务文档，但不自动启动全仓索引补齐或维护；受管源码变化后，交接中说明“AOCI 未同步，相关索引需后续复核”。专门维护时仍严格遵守真实证据、完整机器批次、CAS、Recovery 和审批边界，不伪造收据、不手改 Baseline、不宣称未验证的 aligned。
7. **能力边界**：当前 `aoci_rules` 仍要求新 Run 完整 Overview，本策略是项目选择不在日常任务中进入该工作流，不代表 AOCI 已具备轻量模式。不要虚构配置字段、自动调大分块，或为省时删除既有索引。

<!-- aoci:begin -->
## AOCI 仓库认知

AOCI 为本仓库维护一个稳定、可版本化、可增量更新的仓库级认知层，供模型跨任务复用对系统的理解。

`aoci.txt` 是面向模型的结构化认知索引。它以每个受管理文件、数据库表或其他受管理对象一条独立 Entry 的方式，用符号标签与 F/R/A/S 语义表达对象的核心职责、重要关系、对外契约，以及理解或修改系统时必须知道的非显然约束和设计决策。

Header、目录段和全部 Entry 共同组成完整仓库索引，可以覆盖前端、后端、配置、数据库结构及其他受管理内容。受管理内容发生变化时，通常只需维护受影响的认知条目，不需要重新生成整个索引。

AOCI 提供系统架构、对象职责、重要关系、对外契约和关键约束的高密度视图。

### 工作原理

AOCI 采用“模型生成、模型读取”的认知闭环。

Header、Entry 和 Curation 语义的创作只按当前机器签发的 Plan 与实时 Guide 执行；由 Host 模型基于当前绑定证据独立完成。

Entry 的语义必须来自模型对真实证据的理解。不得仅依据路径、文件名、扩展名、AST、符号列表、依赖扫描、正则、固定模板或规则引擎推导、预填、拼接或改写索引语义。

对 Fresh Bootstrap，只按当前机器签发的 Plan 和实时 Guide 执行。当它们要求创作时，Host 模型创作 Root、Meta、Tag 和 F/R/A/S，提供 authoring-run 声明，并把它绑定到 Plan、Evidence 与完整 Candidate。不得要求 AOCI 填写 `origin=host_model`、制造 Receipt 或把程序生成的 Framework 当作语义。本文件不自行重建 Onboarding 流程。内部批次不是用户决策；只有遇到既有批准边界或真实的安全、漂移、CAS、Recovery 条件才停止。

### 最小使用入口

- `aoci_rules`：取得当前AOCI版本的会话运行合同。
- `aoci_overview`：建立或恢复本仓库的完整认知。
- `aoci_maintain`：受管理对象达到最终稳定状态后检查认知是否需要维护。
- `aoci_update_entry`：提交与当前证据和源码摘要绑定的完整语义更新批次。
- `aoci_report`：仅当当前布局和工具状态支持时，在证据不足、无法可靠生成语义时登记待办，不猜写。

其他MCP工具、CLI命令、参数和专项流程，以当前工具说明、Guide和 `--help` 返回内容为准，不在本文件中重复完整手册。

本区块只规定仓库接入、认知使用和收尾原则。`aoci_rules` 承载当前会话合同，Guide实时输出承载当前Plan的执行顺序与停点，工具Schema、Spec和Validator承载机器结构与判据；Prompt、Description、README和静态文档不能覆盖这些机器事实。

### 建立、生成和恢复认知

1. 每个新的 Agent Run 开始时，应先判断：

   - 本仓库是否已经存在可用的完整AOCI索引；
   - 当前上下文中是否已有与本仓库根、当前索引版本和当前AOCI服务相匹配，并且模型仍可可靠使用的完整仓库认知。

2. 仓库已经存在可用的完整索引，但当前Run没有可靠完整认知时，先调用 `aoci_rules`，再调用 `aoci_overview`。

   完整认知仍可靠时直接复用。局部不确定本身不要求机械重读系统全貌。

   本Run从已知Host上下文压缩恢复时（包括宿主注入的压缩摘要），必须把此前模型认知视为不可靠。压缩handoff不得保留或摘要正式Whole-Index，也不得保留或摘要任何Overview Header、Entry、Chunk、Challenge或Attestation正文；只能保留安全续接所需的receipt身份、未完成write或Recovery状态，以及立即重载指令。复制进handoff的Whole-Index语义或receipt不能证明恢复后模型的当前认知可靠。若当前上下文已无法可靠保留运行合同，先调用 `aoci_rules`。继续业务任务前，使用 `refresh_reasons=["context_compaction"]` 和新的 `refresh_event_id` 调用普通完整Whole-Index `aoci_overview`（不设置 `check_only` 或设为false）；不得使用 `check_only` 或认知probe。原样跟随每个 `next_cursor` 直到 `completed=true`，确认交付，并且只基于新交付正文提交一次Attestation。完成这次新的完整传输后，即使Attestation为partial或fail也消费该generation，并按既有合同继续source-bound任务，不再自动调用第二次Overview。

   AOCI可以针对 `context_compaction`、项目 `cognition_refresh_threshold` 下的机器 `semantic_threshold` 或主要 `phase_transition` 提供checkpoint与认知状态事实。只需要这些紧凑事实时使用 `check_only=true`；这些事实只向Agent提供建议，不替模型决定是否需要系统全貌。

   Agent显式调用普通 `aoci_overview`（未设置 `check_only` 或为false）时，只要能形成一致的CognitionSet，AOCI必须完整交付请求scope。不得因为已有receipt、阈值未达到或没有待处理刷新原因而抑制正文。正式认知Dirty或Stale时仍交付正文，但必须标记不可靠。存在未决恢复或无法形成一致snapshot时失败关闭，不返回混合正文。

   普通Overview返回 `continuation_required=true` 时，必须原样提交 `next_cursor` 并自动继续到 `completed=true`。不得询问用户、开始业务任务或给出阶段性系统结论。Host截断、缺块、重复、乱序、cursor失败、Index变化或`chunk_tokens`变化时停止本次认知链。Attestation完成前不得用Memory、源码、Spec、`aoci.txt`、历史会话、scope、search或Entry读取修补或补充Whole-Index认知。Challenge ordinal是正式Entry序列中的1-based位置；Header内容、注释、空行、Section/Overview/Chunk Marker、Receipt与Metadata均不计数，Chunk Receipt ordinal使用同一序列。Attestation必须原样回绑本次Challenge发布的当前`index_sha256`、`entry_sequence_sha256`与`entry_count`；旧Index、旧Entry序列、旧数量或旧Attestation均无效。完整链结束后只正式提交一次既有模型认知Attestation；同一响应只允许一次不改变语义答案的JSON Schema或字段格式修正。对象、Tag或F不匹配即失败且认知吸收不确定，不得语义重试或旁路补答。首次认知失败时还不得执行Root/Meta、Migration、全局布局或其他未重新绑定的系统级决策。上下文压缩刷新若传输完整、认知身份不变、治理对齐且没有Recovery或第三方冲突，即使Attestation为partial或fail也消耗该refresh generation，并继续原任务，不再自动重读Overview。`system_mastery_percent`只自评系统框架——架构、职责、强关系、稳定外部契约以及高熵安全和维护约束——不表示完整实现或运行实况知识；机器索引覆盖率必须分开。默认只向用户输出由本次真实覆盖率、Challenge、块数、Token和掌握度生成的规定成功或失败一句话。Host截断时提示用户把 `overview_delivery.chunk_tokens` 设置为更小的合法值后重新开始，不得自动修改。

   加法认知等级必须与严格证明字段分开解释。`delivery_verified`表示已加载Index且Host交付已确认，但完整认知验证仍未完成；应表达为“已加载且交付已验证”，不得描述为“没有认知”或“没有理解系统”。`cognition_verified`要求Attestation通过（Challenge至少80%的ordinal完全正确且对象身份至多失手一处），`cognition_governed`还要求治理对齐。通用完整读取失败句只用于真实交付故障。

   当Overview响应包含可选`cognition-state/v2`投影时，必须分别解释各维度。其Level止于`model_cognition_usable`；`strict_attestation_verified`、`governance_aligned`与`current_system_cognition_reliable`都是独立状态，绝不参与该Level。ordinal、对象身份、Tag或核心F不匹配可以导致严格Attestation失败，而模型认知仍然可用；不得仅凭这种不匹配就宣称模型没有理解系统。只有`current_system_cognition_reliable=true`允许无保留地声称当前完整系统认知可靠。投影缺失时继续使用上述Legacy解释。

   普通的只读审计、分析、检查、不修改代码或不提交、不push，不自动等于严格零写入，也不改变上述认知有效性判断。Codex Memory和历史Skill只能辅助恢复经验、用户偏好与调查方向，不能替代与当前仓库根、索引摘要、AOCI服务身份和认知范围匹配的当前认知收据；项目AGENTS和当前AOCI身份在AOCI状态上优先于历史Memory。

   只有用户明确禁止Ledger、元数据、`.aoci`运行资产及任何文件写入时，才按严格零写入处理。若必要的认知建立与该边界冲突，必须报告冲突并请求用户裁决或建议使用隔离副本，不得静默以Memory替代当前仓库认知。

3. 仓库没有可用的完整索引，或当前只有最小骨架、Header不完整、Entries未完成、必要Curation尚未裁决时，如果需要建立正式完整AOCI索引，先取得 `aoci_rules`，然后进入当前AOCI Guide。由Guide依据仓库真实状态决定下一阶段并完成必要安全步骤。

   `aoci_maintain` 不替代索引建立流程。

   不在本文件中自行重建或硬编码完整索引生成状态机。

4. 在长程任务中，模型负责保留当前认知收据并正确使用刷新门禁：

   - Host报告上下文压缩或模型已知系统全貌丢失时，执行上述强制 `context_compaction` 重载规则；AOCI不能自行推断Host事件；
   - 进入真正的主要阶段时声明 `phase_transition`，不得把函数、测试运行或小步骤当作阶段；
   - 在有用的稳定检查点通过 `check_only=true` 取得机器语义计数；
   - 除已知压缩的强制重载外，由Agent判断当前任务是否需要再次显式获取指定scope或完整Overview；
   - 在维护和对齐完成前，保留AOCI报告的Dirty或Stale可靠性状态。

### 任务收尾与认知维护

5. 纯只读问答、分析、版本核验，或没有产生受AOCI管理对象变化的任务，不需要调用维护工具。当前AOCI版本是任意`aoci_overview` check_only或`aoci_maintain`响应里的`cognition_receipt.mcp_service_version`；二进制路径是项目`.mcp.json`里的`command`，CLI不必在PATH上。

6. 发生受AOCI管理对象变化时，待其达到本次任务的最终稳定状态后，只调用一次 `aoci_maintain`。不要在每次中间修改后逐文件维护。

7. 若维护结果返回真实语义候选，Host 模型必须基于每个候选绑定的对象和必要证据，独立创作完整标签与F/R/A/S更新。通过 `aoci_update_entry` 一次提交当前机器签发批次的完整候选集合，同时原样保留每项 `source_sha256`、`candidate_id` 与对应domain批次身份。`max_entries`只限制单次请求和原子事务，不限制logical plan、Whole-Index或Managed Scope。`remaining`非零时，在当前批次成功Apply后重新调用Maintain并从新preimage继续；绝不能为满足transport上限缩减Index覆盖或自行截取返回批次。

   没有足够证据且当前布局支持 `aoci_report` 时，使用它而不猜测、套用模板或为消除待办而生成缺乏证据的认知。

8. 必须遵守工具返回的结构化状态和安全边界：

   - `repair_required`：只修复明确命中的候选，再重新提交当前机器签发的完整批次；
   - `stopped`：结束当前写入尝试并检查 `failed_step`、错误、正式写入证据与Recovery。auto模式下，已证明零写入则记录closure并重新Plan；完整Intent和可证明postimage则Resume；策略要求Rollback且preimage可证明则精确恢复后重新Plan。只有证据不足、第三方正式字节冲突、需要审批或外部动作，或命中其他真实安全边界时，才停止整个用户任务；
   - 冲突、审批、人工裁决、权限和安全信号不得忽略；
   - 已经对齐后不得重复维护或重复写入；`refresh_ready_for_overview` 是checkpoint事实，由Agent决定是否为下一阶段请求普通完整Overview。

   维护完成后如果又修改了任何受管理对象，之前的维护结果失效，应在新的最终稳定状态重新完成收尾。

9. 用户只限制业务文件范围，但没有明确禁止仓库托管资产时，AOCI托管资产可以在收尾阶段为保持认知一致而更新，并应在审计和提交中与业务文件区分。

   用户明确禁止修改 `aoci.txt`、`.aoci`、元数据或任何额外文件时，以用户限制为准，不得写入，并如实报告剩余不一致。

### 专项流程

初始化、完整索引生成、Header生成、Entries生成、数据库结构索引、Curation、人工评审和故障恢复，只按当前AOCI Guide或工具在对应阶段返回的指令、命令和安全停点执行。

不预加载、不猜测，也不自行重建这些专项流程。平台调用方式、请求格式、批次上限、审批规则、索引格式细节和恢复步骤由对应Guide、工具说明、模型Prompt和CLI帮助按需提供。
<!-- aoci:end -->
