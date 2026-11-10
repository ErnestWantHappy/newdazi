# 1.30.91：课例配套材料包说明精简

日期：2026-11-10

## 问题

顾意丽《返回成语现场》配套材料包（`resource_id=2314`，`case_id=801`）的教师“使用说明”为1,045字，包含压缩包字节数、SHA-256、完整目录、内部程序、未上架文件和安全扫描明细。旧优秀课例说明约47字，教师阅读成本明显过高。

## 实际内容

读取RAR确认：包内是成语连环画课堂AI工具，包括`app.py`、`providers.py`、`scheduler.py`、`templates/index.html`、`templates/teacher.html`、`templates/screen.html`、`data/groups.json`、`static/generated`图片和`static/uploads`音频。教学设计、反思、说课稿和课件不在此包内，而是同一课例的独立资源。

## 修复

- 资源2314说明改为91字：

  > 本包为“返回成语现场”课堂工具，含成语连环画创作与作品展示。解压后双击“启动课堂工具.bat”，浏览器打开学生端；教师端地址见启动窗口，需联网使用AI出图。教学设计和课件见资源列表。

- 修改`.agents/skills/research-to-textbook-cases/scripts/build_archive_summary.py`：教师说明只写“包内有什么、怎么打开”，技术检查仍保留在`archive-inspection.json`和manifest；说明上限180字，不再输出SHA-256、完整路径、内部程序清单和安全扫描明细。
- 新增`tests/test_archive_summary.py`4项回归；教研导入脚本共24项测试通过。
- 新增`sql/research_case_2314_short_summary_20261110.sql`和`sql/platform_update_1_30_91_20261110.sql`。
- 重新生成`output/research-case-import/20261109-1615-research-cases/manifest.json`与`reports/import-draft.sql`，旧长说明不会再被后续重跑覆盖回正式库。

## 发布

- 发布方式：仅数据修正，不重启后端、不换前端、不改表结构。
- 整库备份：`/data/backups/xueyeceping/20261110_060850_20261110_case_summary_v1/database.sql.gz`，53,147,066B，SHA-256 `4ff6cf0ed05e74c85c8a908a429df5e6aa0fbaf9a2e3658a7a3ab79d58928d4c`。
- 产品更新：`169 / 1.30.91 / 课例配套材料包说明精简 / PUBLISHED`。
- 正式库：327课例/1,050资源前后不变；资源2314说明由1,045字变为91字。

## 验收

- 真实教师API：`case_id=801`、`resource_id=2314`返回91字说明，HTTP 200。
- 真实教师阅读器：桌面1440和手机390均显示91字，`scrollWidth=clientWidth=335`，0横向溢出。
- 0页面脚本异常、0控制台错误、0个5xx；产品更新169真实教师可见。
- 证据：`output/playwright/case-summary-1.30.91-20261110/report.json`、`desktop-1440.png`、`mobile-390.png`、`output/release-20261110-case-summary/remote-state.json`。

## 回退

```powershell
python scripts/deploy/update_case_summary_130.py --mode rollback `
  --backup-dir /data/backups/xueyeceping/20261110_060850_20261110_case_summary_v1
```

该命令从备份中的`resource-2314-before.hex`恢复旧说明，并把产品更新169转DRAFT；不重启服务、不整库回灌。

## 未完成

- 本轮源码修改尚未Git提交/推送；正式库已生效，后续重跑导入时会使用新的简短说明规则。
