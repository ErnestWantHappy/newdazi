# 验收与发布状态

2026-10-13。**本机候选完成，正式服务尚未切换。**

用户已明确选择等待交卷高峰结束；以连续 5 分钟无交卷、无 RUNNING AI 任务作为本轮切换门禁。

已创建当前聊天自动续办“130连线题低峰发布”（automation id `130`），每 5 分钟检查一次；未达门禁保持安静，达到条件后切换、重算、正式验收和登记。完成或失败才通知，完成后停用。电脑和 Codex 应保持运行；当前仍未上线。

最终候选 JAR 另经 PropertiesLauncher 实际调用公共入口及隔离分支：缺线样本 1 页 / 8 条连线、正常正偏移及 v:line 均转换成功，原件和输出文件名保持。

旧预览备份 `previews-before.tar.gz`：3,650,589 bytes，SHA-256 `635c61aef069bfe8d60aeb03a55dfaf8d35a06f1790eee2a102a2b870768d3a6`；同目录保存 25 份附件状态及答案/成绩快照。

- 后端 `clean package` 与定向测试（DocxConnectorNormalizerTest、PracticalPageRendererTest、FileConversionUtilsHealthTest、OfficeLinuxProcessScopeTest、PracticalAttachmentConversionFailureTest）：57 项，失败/错误 0，BUILD SUCCESS；带 `-Dsurefire.failIfNoSpecifiedTests=false -DforkCount=0`。页图版本目录及保留旧文件已补测；既有图片尺寸断言由过时的 1800 修正为已上线的 2000，本轮未再调整清晰度。
- 49 份候选全部用本机 LibreOffice 转换：25 份修正 139 条连接符，24 份原样。修正水平定位与 Word 最大差异 0.04993pt（COM 返回值存在舍入）；原件 SHA-256 和其他 ZIP 内容不变。
- 原无线关键样本修复后 8 条完整出现，位置与 Word PDF 一致；原本已兼容的导入分支未重复右移。正偏移、正常 v:line、模板对照无修正、字节不变。
- 非 Office、损坏文件、外部实体、缺 XML、页外图形、表格/多栏和毒文件由测试覆盖；六处公共调用已核对。前端未改，不做浏览器全链 E2E；正式页图/API 验证待切换后完成。

## 制品与回退

正式基线 `20261013_lo_crashfix_v4`，JAR SHA-256 `9472812b974e7d041f0e19a836aafe3d5e758ca74f41b1c7a82fd3b730e53d27`。

1.30.45 最终候选 `20261013_connector_fix_v1`，108,087,498 bytes，SHA-256 `9bedc2eccce82b66d2147ec3c9cabb22c5a56fac9197dbd41e07aa2425478439`。仅替换公共入口、预处理及内部类、渲染器、两个内联常量调用方共 6 个 class；其他业务条目、其他嵌套库和前端均保持。

备份 `/data/apps/xueyeceping/backups/20261013_connector_fix_v1/database.sql`，387,644,615 bytes，SHA-256 `decef7812ccb02e52cd9bb786c62bd1f26953eaf9d1d241688e06b291be12b12`；同目录 `service.before`，旧 release 保留。

切换后回退：确认当前 release，恢复 `service.before`，daemon-reload、重启退回 v4；不要回灌整库覆盖新增成绩。旧预览/状态须按本轮精确备份恢复；已登记时另撤回 1.30.45 为 DRAFT。

机器证据在 `output/connector-fix-20261013/`：package.log、coordinate-validation.json、render-validation.json、word-oracle.json、manifest.json、backup-evidence.json。学生作品与登录态不提交。
