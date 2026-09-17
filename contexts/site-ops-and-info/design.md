# 站点运维与信息展示 —— 设计文档

> 版本：v1.0 · 创建：2026-08-22 · 配套同目录 requirements.md

## 1. 129 监控增强

### 1.1 后端（ruoyi-admin/web/controller/monitor/ExtensionHealthController 扩展）
- 现有：MONITOR_HOST129_SSH_COMMAND（NSSM 环境变量）注入 129 探针命令，命令输出 JSON，60 秒缓存，6 秒超时，失败 available=false。
- 扩展探针输出契约（hwprobe.sh 在 129 上执行后输出 JSON）：
  { hostname, ip, os, kernel, cpu:{model,cores,sockets}, memory:{totalBytes,usedBytes,availableBytes}, disks:[{mount,fs,totalBytes,freeBytes,usedPercent}], nodeVersion, cryptpadNodeVersion, java:{version,startTime,home,arguments} }
- 后端把 java 对象展开为 hostHardware.javaVersion/javaStartTime/javaHome/javaArguments；disks 与 memory 原样透传；cpu 已支持 model/cores。无新表、无新接口，字段并入现有 GET /monitor/extension/health 的 hostHardware。
- 129 侧探针脚本：deploy/host129/hwprobe.sh（uname/hostname/ip/nproc/lscpu/meminfo/df/node -v/java 进程 ps 与参数），由运维凭据恢复后部署并授权 SSH 执行。

### 1.2 前端（views/monitor/extension/index.vue）
- 现有 129 硬件卡渲染 hostHw；本次扩展：
  - 新增 JVM 区块：hostHw.java 存在则显示 版本/启动时长/安装路径/运行参数，否则显示“无 Java 进程或未采集”；
  - OS 行补全 hostHw.os；
  - 磁盘表保留，进度条已实现（el-progress usedPercent）；
  - available=false 时整卡显示 el-alert「SSH 凭据未恢复，硬件信息暂缺」，其余四服务探针与资源卡照常。

## 2. 平台更新补录 SQL

- 新增 sql/platform_update_history_v2.sql：幂等 INSERT（NOT EXISTS 按 version_no），条目结构与 v1 一致。
- 版本号规划：补录历史用 1.8.x 起的连续版本号，不与 v1 已有 8 条冲突；正式轮从 1.8.0 起。
- 前检/后检：新增条数、重复版本号 0。
- 执行顺序：本机库执行 → 后检 → 正式发布时正式库（备份后执行）。

## 3. Agent 词典落地

### 3.1 contexts/RELEASE_LOG.md（新增）
# AI 发布登记单（Agent Release Log）
> 规则（AGENTS.md 硬性要求）：任何代码修改部署上线到 123 前，必须新增一行登记并写入平台更新记录。
| 日期 | 版本号 | release 目录 | 本次改动摘要 | 平台更新已写入 |
| --- | --- | --- | --- | --- |

### 3.2 AGENTS.md 修改
- 任务收尾检查清单新增两条（部署必填）：1. contexts/RELEASE_LOG.md 已登记本次发布的日期/版本号/改动；2. biz_platform_update 已写入本次更新（草稿或 PUBLISHED）。
- 第 3.4.2 远程代操作第 4 条「构建与发布」附注：发布探活后必须登记 RELEASE_LOG 并写平台更新。

### 3.3 部署校验脚本
- tools/check-release-log.ps1：参数 -ReleaseDir；校验 RELEASE_LOG.md 是否有与当前日期匹配的新增行，未找到则以非零退出提示。
- 默认先做「发布后提示」；如用户要求强制，则放到发布切换前作为门禁。

## 4. 帮助中心推荐环境

- 新增 src/views/help/recommendedEnv.js：导出数组，每项 {name, version, os, purpose, url}：
  1. Google Chrome：version=最新稳定版（如 131.x，随官方更新），os=Windows/macOS 通用，url=https://www.google.com/chrome/；
  2. Mind+：version=V1.8.1 RC3.0，os=Windows/macOS 通用，url=https://mindplus.cc/；
  3. 掌控板：version=ESP-32 主控，os=—，purpose=初中物联网实验器材，配合 Mind+ 使用。
- help/index.vue 插入区块：hero 之后、角色区块之前；渲染三卡（名称/版本标签/适用系统/用途/下载按钮，无 url 不显示按钮）。所有角色可见。

## 5. 底部版权

- src/layout/components/Copyright/index.vue：删除地址 span 与分隔符，保留「版权所有：象山县教育局教科研中心 · 浙ICP备05007927号」。
- 检查 login.vue / register.vue 是否有「浙江省象山县…」地址文案，一并删除（2026-08-20 登录页版权改版后复查）。

## 6. 测试与验收
- 129 页：mock hostHardware available=false → 显示「暂缺」提示；模拟含 java/disks 的 JSON → 各字段渲染、进度条可见。
- 平台更新：SQL 本机执行后管理页时间轴完整、无重复版本。
- RELEASE_LOG.md、AGENTS.md 条款、check-release-log 脚本存在并可用。
- 帮助中心三卡渲染正确；底部与登录页无地址文案。
- npm run build:prod 与 mvn clean package -DskipTests 通过。

## 7. 影响面与风险
- 后端只改 ExtensionHealthController（解析扩展字段）+ deploy/host129/hwprobe.sh（129 侧，待凭据）；前端 extension 页 + help 页 + Copyright + login/register 文案。
- 不动：monitor/server（OSHI）、权限模型、其余业务模块。
- 风险：探针 JSON 新字段与旧前端兼容（前端忽略多字段即可）；SSH 凭据未恢复期间「暂缺」是预期状态而非缺陷；平台更新补录以 context.md 记录为准，不臆造日期。

## 8. 2026-08-24 小型收口设计

- `recommendedEnv.js` 支持可选 `metaLabel/metaValue`，硬件卡显示设备类型，软件卡继续显示适用系统。
- 帮助中心只保留一处开发者联系入口；扩展监控只改展示文案，不改变 Judge0 健康数据。
- `sql/hide_ruoyi_official_menu_v1.sql` 设置 menu_id=4 的 `visible='1'` 并删除 `sys_role_menu` 关联；`sql/platform_update_python_iot_ux_v1.sql` 幂等登记 1.26.1。
