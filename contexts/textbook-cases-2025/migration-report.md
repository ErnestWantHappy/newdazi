# 130 迁移与发布报告（2026-11-04）

## 1. 迁移前现场基线

| 项 | 值 |
| --- | --- |
| 主机 | 10.52.1.130（Ubuntu，经 123 跳板，固定 SSH 指纹） |
| 当前版本 | 1.30.70（后端 `20261103_case_feedback_v2`、前端 `20261103_case_scroll_v1`） |
| 后端 JAR | `/data/apps/xueyeceping/releases/20261103_case_feedback_v2/backend/ruoyi-admin.jar`，SHA-256 `75520782…1603` |
| 首页 | `/data/apps/xueyeceping/releases/20261103_case_scroll_v1/frontend/index.html`，SHA-256 `7d8e056a…31d7` |
| unit / site / env | `523641a8…` / `a2349426…` / `ca11cd4f…`（发布前逐项校验，未漂移） |
| 资源软链 | `shared/textbook/cases → releases/20261103_case_feedback_v2/resources/cases` |
| 库 | `ry-vue`，课例 52 / 资源 196，`case_id` 1–52 |
| 磁盘 | `/data` 492G，已用 104G，可用 363G |
| 教材 | 13 册、`xxkj-3-0-2026` … `xxkj-9-9-2026`，与 `book_code` 一一对应；每册 18–20 个 LESSON |

## 2. 备份（切换前）

- 目录：`/data/backups/xueyeceping/20261104_ningbo2025_cases_v1`
- 整库：`database.sql.gz` **597,425,809 字节**，SHA-256 `9defe7afbcbaee1ff1787d7e36cdbc63a1af960c49e430b7e0d92bb4bb1ecb00`（mysqldump 退出码 0）
- 同目录：`service.before` `523641a8…`、`nginx.before` `a2349426…`、`env.before` `ca11cd4f…`、`jar.before` `75520782…`、`index.before` `7d8e056a…`、`case-metadata.before.sql` `9c1edb67…`、`resource-link.before`（旧软链目标）、`SHA256SUMS`
- 备份文件权限 0600，含完成标记与逐文件 SHA256。

## 3. 制品与上传（逐文件校验）

- release：`/data/apps/xueyeceping/releases/20261104_ningbo2025_cases_v1`（新目录，旧 release 保留）
- 后端 JAR：108,216,901 字节，SHA-256 `18d735a19dfb17407db5ae53b8b79f89035b3a76dee5ab50bb2133fa1eff0bb3`
  - 以生产 JAR 为基线，只替换 `BOOT-INF/lib/ruoyi-business-3.9.0.jar` 内 `TextbookCaseController.class`、`TextbookCaseMapper.class`；其余 291 个条目逐字节一致，无新增/缺失条目。
- 前端：`frontend.tar.gz` 43,420,397 字节，SHA-256 `d03a68a9b40622713e5628b38cd1e57c6a94c9bedc1befffa94ef2547730a4c8`；新首页 `f9347b300db4cc7859f9b9b3efc976c4913734146bbbdfe07e355980bbecec20`，构建门禁（入口 gzip 560,586B、登录图片引用）通过。
- 资源增量：`resource-delta.tar` 6,092,738,560 字节，SHA-256 `8928881b7113f6443d234825456a579e2464afd53d1740385e0b10255d817623`；823 个文件在远端逐个 SHA256 复核通过。
- 复用：旧 307 个资源文件在新 release 内硬链接复用，不重复上传、不改旧文件。
- 资源树共 1362 项（823 文件 + 目录）；前端 792 个文件。
- 上传后补传 23 个小型配套包（94 个小脚本/网页），已并入 `resources/cases/packages/` 并重跑 SQL（幂等）。

## 4. 数据库迁移

- SQL：`sql/textbook_cases_ningbo2025_v1.sql`（809,333 字节，SHA-256 `edafea62…1fdd`），仅 `INSERT … ON DUPLICATE KEY UPDATE`，**无 DELETE**。
- 课例 `book_id` 由 `book_code + section_key + kind='LESSON'` 在目标库解析，不使用本机 id。
- 执行后：课例 **142**（52 旧 + 90 新）、资源 **693**（196 旧 + 497 新）、配套包 64；孤儿 0、重复 0；`case_id ≤ 52` 的旧课例仍在。
- 本机导入前后对旧 52 条课例、196 条资源做了逐字段比对（`case_id`/`resource_id`/标题/作者/学校/路径/字节数），全部无变化；本机评价记录 2 条保留。130 侧本次 SQL 未引用 `biz_textbook_case_feedback`/`biz_textbook_case_invitation`，评价数据未被修改。

## 5. 切换动作

1. `systemd` unit 内 JAR 路径替换为新 release 的 `backend/ruoyi-admin.jar`；`daemon-reload` + `restart xueyeceping-130`。
2. Nginx 站点 root 替换为 `.../20261104_ningbo2025_cases_v1/frontend`；`nginx -t` 通过后 `reload`（不中断）。
3. `TEXTBOOK_ROOT/cases` 软链以「临时软链 + `os.replace`」原子切到新 release 的 `resources/cases`。
4. 服务状态：`xueyeceping-130`、`nginx` 均 `active`，后端 MainPID `2889414`，unit SHA `1488a0bf…`、site SHA `75fc15ec…`、env 未变。
5. 启动期 `ERROR/Exception` 日志：无。

## 6. 发布登记

- `biz_platform_update`：`update_id 149`、`version_no 1.30.71`、`status PUBLISHED`、`published_at 2026-11-04 03:28:53`，标题「优秀课例扩充：2025宁波优秀课例90份」。
- 真实教师 `19157727791` 调用 `/business/platform-update/list` 返回 `code 200`、`total 125`，本版本在列表中可见。
- `contexts/RELEASE_LOG.md` 已登记；`PROJECT_CORE.md` 与架构索引同步为 1.30.71。

## 7. 回退方法（精确、可执行）

```bash
python output/textbook-cases-2025/release-130/deploy.py rollback
```

等价手工步骤（在 130 上）：

1. `cp /data/backups/xueyeceping/20261104_ningbo2025_cases_v1/service.before /etc/systemd/system/xueyeceping-130.service`
2. `cp .../nginx.before /etc/nginx/sites-available/xueyeceping-130`
3. 软链改回 `.../20261103_case_feedback_v2/resources/cases`（值见 `resource-link.before`），用临时软链 + `os.replace` 原子替换
4. `systemctl daemon-reload && systemctl restart xueyeceping-130 && nginx -t && systemctl reload nginx`
5. 更新 149 转 `DRAFT`（不删记录）

回退**不回灌整库**、不覆盖新产生的学生成绩与作业；只影响本次程序/前端/资源指向与更新状态。若需同时撤掉本批课例，可按 `source_key` 删除本批 90 条课例（`source_key` 见 `catalog-reviewed-2025.json`），不影响旧 52 条。

## 8. 现有风险

- 新 release 与旧 release 各持一份资源树（硬链接，占额外 inode 不占额外数据块）；确认稳定后可择机删旧 release，**本次未删**。
- 5.8GB 资源使 `TEXTBOOK_ROOT` 体积明显增大；后续批次建议继续用增量 tar + 硬链接复用。
- 2 个 WMV 仅下载；3 份 PENDING 课例未上架。
