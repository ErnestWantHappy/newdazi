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
