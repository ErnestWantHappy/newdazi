# 验收与发布状态

2026-10-13。**130 正式 1.30.45 已发布，25 份历史预览已重算，限定坐标修复验收完成。**

用户已明确选择等待交卷高峰结束；以连续 5 分钟无交卷、无 RUNNING AI 任务作为本轮切换门禁。

此前通过当前聊天自动续办“130连线题低峰发布”（automation id `130`）每 5 分钟检查，未达门禁保持安静。19:13 时近 5 分钟交卷和 RUNNING AI 均为 0，独立发布任务启动；完成后删除自动续办，保留聊天。

最终候选 JAR 另经 PropertiesLauncher 实际调用公共入口及隔离分支：缺线样本 1 页 / 8 条连线、正常正偏移及 v:line 均转换成功，原件和输出文件名保持。

旧预览备份 `previews-before.tar.gz`：3,650,589 bytes，SHA-256 `635c61aef069bfe8d60aeb03a55dfaf8d35a06f1790eee2a102a2b870768d3a6`；同目录保存 25 份附件状态及答案/成绩快照。

- 后端 `clean package` 与定向测试（DocxConnectorNormalizerTest、PracticalPageRendererTest、FileConversionUtilsHealthTest、OfficeLinuxProcessScopeTest、PracticalAttachmentConversionFailureTest）：57 项，失败/错误 0，BUILD SUCCESS；带 `-Dsurefire.failIfNoSpecifiedTests=false -DforkCount=0`。页图版本目录及保留旧文件已补测；既有图片尺寸断言由过时的 1800 修正为已上线的 2000，本轮未再调整清晰度。
- 49 份候选全部用本机 LibreOffice 转换：25 份修正 139 条连接符，24 份原样。修正水平定位与 Word 最大差异 0.04993pt（COM 返回值存在舍入）；原件 SHA-256 和其他 ZIP 内容不变。
- 原无线关键样本修复后 8 条完整出现，位置与 Word PDF 一致；原本已兼容的导入分支未重复右移。正偏移、正常 v:line、模板对照无修正、字节不变。
- 非 Office、损坏文件、外部实体、缺 XML、页外图形、表格/多栏和毒文件由测试覆盖；六处公共调用已核对。前端未改，不做浏览器全链 E2E；正式页图/API 验证已完成。

## 正式验收（130，时间均 +08:00）

- 2026-10-13 **19:13:31** 重启后端，**19:13:52** `rollout.status=SUCCESS`、3009 探活成功，约 21 秒恢复；前端保持。正式 unit 指向 `20261013_connector_fix_v1/backend`。
- `backfill.py` 精确领取 **25** 份，既有 100 号任务 HTTP200/业务200，仅触发一次，已有 `backfill-receipt.json`。全部 `preview_status=success/normalized_status=success/renderer_version=page-jpeg-v3`，页图地址全部换版本目录。
- 25 份正式 PDF 与本机核对水平位置、方向和相对长度；49 个原件 SHA-256 保持，25 个答案的学生/课程/题目/原件路径/成绩/作品/版本字段保持。查看 568961 的正式 JPEG/API，连线可见，资源接口 HTTP200/JPEG；也查看 575947 的两页 PDF 与 JPEG，修正线段在第 2 页可见。
- **既有边界未解决**：575947 Linux 原、新 PDF 都为 2 页，本机为 1 页；本机可见的 1 条未改线段在原正式备份 PDF 已缺失，新版仍缺失。逐段比较确认该线段本轮坐标未改且原正式不存在；新版可提取长线段 5、原正式 4，第 1 页仍有未修正连接符的显示问题。初始“正式长线段数量必须与本机相同”的断言因此不适用于跨字体环境；改为严格验证已生成线段的位置，并仅允许有备份证明的既有未改线段缺失，不允许本轮修正线段缺失。不能宣称 139 条连接符或全部作品在正式环境都与 Word 显示完全一致。
- 只读核验正常文件 101556 仍 success/retry=1；4 个毒文件 440616/514480/514546/548595 均 failed/retry=3/隔离文本，原有停止重试规则保持。服务 active/NRestarts=0，2 个 soffice.bin。
- 正式前端入口 HTTP200，SHA-256 `9cf22d85d6de7a19636806ca74b82e1b9d65cfe33ba82099497617470aa1e5c8`，与 1.30.43 基线相同。
- 执行 `sql/connector_render_release_20261013.sql`：`update_id=123/1.30.45/PUBLISHED`，**19:17:48**。真实教师 `19157727791` 平台更新列表 HTTP200/业务200，1.30.45 可见。旧管理员登录态过期后，只用已存明确管理员凭据刷新并校验任务读取权限，未泄露凭据。
- 新增证据：`formal-validation.json`、`formal-health.txt`、`registration.tsv`、`teacher-visible.json`、`backfill-receipt.json` 及 `formal/` JPEG/PDF；均留在被忽略的 output，不提交学生作品或登录态。57 项测试沿用已通过证据，本轮续办未重复运行。
- 后续复核 Linux 字体/分页、未修正连接符的显示差异；表格、多栏、其他形状等仍属未支持范围。AOCI 未同步。

## 制品与回退

正式基线 `20261013_lo_crashfix_v4`，JAR SHA-256 `9472812b974e7d041f0e19a836aafe3d5e758ca74f41b1c7a82fd3b730e53d27`。

1.30.45 最终候选 `20261013_connector_fix_v1`，108,087,498 bytes，SHA-256 `9bedc2eccce82b66d2147ec3c9cabb22c5a56fac9197dbd41e07aa2425478439`。仅替换公共入口、预处理及内部类、渲染器、两个内联常量调用方共 6 个 class；其他业务条目、其他嵌套库和前端均保持。

备份 `/data/apps/xueyeceping/backups/20261013_connector_fix_v1/database.sql`，387,644,615 bytes，SHA-256 `decef7812ccb02e52cd9bb786c62bd1f26953eaf9d1d241688e06b291be12b12`；同目录 `service.before`，旧 release 保留。

切换后回退：确认当前 release，恢复 `service.before`，daemon-reload、重启退回 v4；不要回灌整库覆盖新增成绩。先按 `preview-states-before.json` 的 25 个 attachment_id/version_id/sha256 核对当前版本，再将 `previews-before.tar.gz` 中清单列出的旧 PDF/页图恢复到原路径，精确还原这些附件的 preview/normalized 状态、路径、renderer_version、重试和错误字段；不改学生作答、分数、作品版本，不将答案旧快照整体回灌。新 v3 页图可保留，附件引用恢复旧地址即可回退；撤回登记 `UPDATE biz_platform_update SET status='DRAFT' WHERE update_id=123 AND version_no='1.30.45';`。旧版本及旧资源均保留。本轮无需结构回滚 SQL。

机器证据在 `output/connector-fix-20261013/`：package.log、coordinate-validation.json、render-validation.json、word-oracle.json、manifest.json、backup-evidence.json。学生作品与登录态不提交。
