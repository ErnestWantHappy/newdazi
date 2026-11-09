# 电子教材与教学资源：正式验收与回滚（2026-11-07）

> 本文为1.30.76历史验收。2026-11-08已上线1.30.77，汇编在线预览、准确覆盖、2023参考补入、配套包净化及副本精确回退见[最新验收](hotfix-1.30.77.md)。旧文“180课件”应理解为180课次、179可用文件；本轮学生身份未重新登录验证，不沿用旧403作为本轮通过证据。1.30.76回退SQL已加固为批次精确约束，当前脚本未重新执行副本验证；下文副本结果对应加固前历史版本。当前热修优先使用1.30.77独立回退SQL。

## 1. 上线结果

- 130 正式 `releases/20261107_teaching_resources_v1`，版本 **1.30.76**，`biz_platform_update.update_id=154` 状态 `PUBLISHED`，真实教师 `/business/platform-update/list` 首条可见。
- 后端 JAR `2abe3223ed03802d90a8548bbcd0bbf58d39769f073a93d0ee36a9839536d836`，前端 `index.html` `8f092e5ddb3844c1a5372a973d8a13f2fab047cde516681e25f46eef1d6ac592`。
- 系统服务 `xueyeceping-130` active，PID 2984156，监听 3009；`/prod-api/captchaImage` 200、首页 200；启动日志 0 ERROR。
- 资源软链 `shared/textbook/cases` → `releases/20261107_teaching_resources_v1/resources/cases`，目录 2,139 个文件。

## 2. 来源与去向

- 来源（只读）：`E:\11-7\新省版义务段信息科技新教材教学设计（宁波版）` 2,447 文件/6,312,185,771B；`E:\11-7\义务教育信息科技课程教学指南及配套课件` 191 文件/1,609,948,265B。ZIP 与解压目录同时存在时按解压目录处理，未重复计入。
- 1,697 个来源文件全部有去向：`PUBLISHED` 262、`IN_PACKAGE` 1,284、`DUPLICATE_REUSED` 150、`MERGED_EXTRA` 1；无静默丢弃。
- 214 份设计形成 322 个资源；166 个新课例；48 个已收录课例补充同课其他作者版本 50 个资源，旧课例 ID、作者、学校、身份与评价未改。
- 13 册“整册教学设计汇编”独立下载，与教材“下载整册”分开；3–8 年级课程指南 6 组、180 份课件，五年级第 28 课《认识决策树算法》来源未提供课件，页面标注“暂未提供”。
- 9 份设计无法确认作者，作者/学校留空；未编造。
- 逐课覆盖缺口：三上 16/18、三下 11/18、四上 18/18、四下 18/18、五上 16/18、五下 18/18、六上 18/18、六下 17/18、七上 20/20、七下 19/20、八上 4/20、八下 9/20、九年级 1/18。缺课来自来源本身，未用其他内容替代。
- 明细：[coverage.md](../../output/textbook-resources-20261107/coverage.md)、[file-dispositions.json](../../output/textbook-resources-20261107/file-dispositions.json)、[resource-manifest.json](../../output/textbook-resources-20261107/resource-manifest.json)。

## 3. 数据库迁移

- 迁移前 130：142 课例、693 资源、2 条评价；迁移后：308 课例（LEGACY 142 + NINGBO2026 166）、1,015 资源、13 册级资源、6 指南、180 课件、0 重复资源。
- 迁移前备份：`/data/backups/xueyeceping/20261107_teaching_resources/ry-vue_before_teaching_resources.sql`，570MB，SHA-256 `f03b611c0264207795b9c25625ecf1248e4b31011983770b889afb747881cfcf`；同目录保留 systemd 单元，Nginx 配置在 `/data/backups/nginx/20261107_teaching_resources/`。
- 旧数据不变：迁移前 693 条资源与 142 条课例逐字段比对，0 行变化、0 行缺失；2 条评价保持。
- 资源落位：全部按稳定 `source_key` 解析。首版数据 SQL 对 48 个复用旧课例写死本机 `case_id`，130 上 45 条资源误挂到同号课例、5 条未插入；用备份库确认这 50 条键在迁移前不存在、未覆盖旧资源后，执行 `sql/teaching_resources_20261107_fix_legacy_placement.sql` 并重跑修正版数据 SQL。最终 308 个课例的资源键集合与本机逐一一致，50 条复用资源全部落在目标课例，错误落位 0。
- 资源文件覆盖：1,886 条数据库引用路径（课例、册级、指南）全部存在，缺失 0。
- SQL 哈希：DDL `4bc995cd…`，数据（修正版）`9a85da4e…`，落位修正 `4f23100c…`，回滚 `6ea8b7af…`；均已在 `sql/` 与 130 release `sql/` 目录。

## 4. 资源包校验

- `cases.tar` 6,377,175,040B，本地与 130 SHA-256 均为 `3c2cc2a5013e5779b52aaafc6b9a022e4a8fff2435f7780c305ea6f16fda2424`。
- `cases.sha256` 992 条逐文件校验通过；解包使用 `tar --unlink-first` 打破硬链接，未改写旧资源 inode；旧资源目录文件清单哈希 `f3aebcbe…` 迁移前后一致。
- 新目录相对旧目录为增量包：1153 条旧路径全部存在，新增文件落在新 release。

## 5. 正式验收

- 教师（郑东旭，teacher+researcher）：菜单“电子教材与教学资源”可见；书架显示 13 册汇编按钮与 6 组指南；指南 5 年级第 28 课“暂未提供”可见；指南 PDF 预览 171 页、课件预览 19 页、册级汇编预览 51 页、宁波版设计预览 7 页。
- 下载：教师 Cookie 下载指南 6,787,465B、课件 92,703,907B、课例设计 200；匿名 401；学生下载 403。
- 旧课例：第 17 课“在线协作学习（2025优秀课例）”保留“优秀课例”、作者罗波、推荐/不推荐与评价理由；同课宁波版 2026 卡片无投票入口。
- 学生（2020710101）：登录正常；教师课例、指南、册级资源接口返回业务码 403；无本课教学资源入口。
- 窄屏：书架、指南页、阅读页在 390×844 与 320×844 下 `scrollWidth == innerWidth`；320 下资源面板可用，预览弹窗宽度 320、可关闭。
- 权限矩阵：菜单 26100 仅分配 admin/teacher/researcher；student/common 未分配。

## 6. 回滚

1. 数据库：执行 `sql/teaching_resources_20261107_rollback.sql`。已在当前库副本 `ryvue_rb_test` 实测：回到 142 课例/693 资源/2 评价、0 册级资源、0 指南，菜单名恢复“电子教材与优秀课例”。该脚本不删除 `origin`、`extra_*` 列和三张新表，避免影响旧数据。
2. 文件与服务：`shared/textbook/cases` 指回 `/data/apps/xueyeceping/releases/20261104_ningbo2025_cases_v1/resources/cases`；systemd 的 `WorkingDirectory`/`ExecStart` 与 Nginx `root` 指回 `/data/apps/xueyeceping/releases/20261107_python_server_run_v3`；`nginx -t` 后 reload、重启 `xueyeceping-130`。
3. 备份配置：`/data/backups/xueyeceping/20261107_teaching_resources/xueyeceping-130.service.bak`、`/data/backups/nginx/20261107_teaching_resources/xueyeceping-130.conf.bak`。
4. 旧 release 保留且未变：旧 JAR `e6e349d3…`、旧首页 `bc81d431…`、旧资源目录清单哈希 `f3aebcbe…`。

## 7. 未验证与风险

- 未在真实学生机房整班使用；仅单账号、桌面与模拟窄屏。
- 八年级上/下与九年级大量课次来源缺失，需后续补资料，不能用其他册内容替代。
- 9 份设计作者待人工确认。
- 回滚 SQL 有意保留新增列/表；若未来彻底降级，需单独评估旧程序对新增列兼容性（旧程序不读取新列）。
