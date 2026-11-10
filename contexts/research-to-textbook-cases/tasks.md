# 教研活动资源整理为电子教材与优秀课例：任务与证据

状态：**2026-11-09 已随130正式release上线；2026-11-10 完成1.30.91说明精简**。3节课例PUBLISHED，正式库327课例/1,050资源不变；顾意丽资源2314说明改为91字并通过真实教师页面复验。

## 已完成

- [x] 接管：读`AGENTS.md`、`PROJECT_CORE.md`、`INDEX.md`、`DEPLOYMENT_RUNBOOK.md`、课例/教研Mapper、Controller、Vue页面与`sql/`；`git status --short`起始为空。
- [x] 130只读前检：1.30.86、三服务active、MainPID`3019157`、首页/验证码200；324课例/1,033资源；来源字段尚不存在；7条资源帖与两条教材映射唯一；post 60暂缓；8条同名旧课例保留。
- [x] 用户指令追加：赵琪（post 60）云盘链接更新（今日15:09:58）并实测有效，解除暂缓并纳入整理，教材解析为四上第17课。
- [x] 新增`.agents/skills/research-to-textbook-cases/`：SKILL、10个脚本、5份参考文档、4个测试文件；默认dry-run，apply需显式低峰确认，支持run id续跑。
- [x] Skill单测15项、Python编译、后端教材定向40项、前端登录9/机位9/工作台25项、Vite生产构建和发布门禁通过。
- [x] 白天stage（批次 `20261109-1615-research-cases`）：下载26个来源文件325,446,066B；检查2个独立压缩包；生成9个Office/PDF预览（0失败）、2份中文说明、manifest与SQL草稿。最终3节课例、17个去重上架资源、9个预览；无缺失文件、哈希不符、`.txt/.url`独立资源、`/profile`路径或敏感信息。
- [x] 代码候选：后端来源字段与音频MIME、前端活动来源/标签/ARCHIVE/RAR/7Z/音频/窄屏；幂等DDL写入`sql/research_to_textbook_cases_v1.sql`。
- [x] 上下文与架构资料同步；明确当前不是低峰窗口，未写正式库、未上传正式文件、未重启、未切前端、未发布登记。

## 低峰窗口执行结果

- [x] 2026-11-09 17:31完成整库备份与DRAFT导入：备份`/data/backups/xueyeceping/20261109_173147_research_cases_20261109-1615-research-cases`，`database.gz` SHA-256`8320b49ea70399499df3e8963892a7ffbc6cb4ceea9e37551f2fd670f5abe70d`；基线324课例/1,033资源。
- [x] 17个资源文件上传到`.staging/20261109-1615-research-cases`，服务器前后两次哈希复核`HASH_OK`，原子落位后3节课例为`case_id=799/800/801`、17资源、重复/孤儿/`/profile`路径均为0。
- [x] 已发布后端与前端release，正式服务切换并保留旧release；3个`source_key`已转PUBLISHED，当前总课例327/资源1,050。
- [x] 真实教师接口与资源权限11项通过：语音合成/郑东旭展示、返回成语现场/赵琪展示、来源活动与post 60、2项资源、课件PDF预览200、教师音频Range 206、匿名401、学生403；报告`live-api-verification.json`。
- [ ] 三尺寸浏览器未另留本专题专项截图；高中教材复跑只验证了课例summary未中断。后续若教师反馈展示问题，再补1440/390/320完整课例面板验收。
- [x] 已在核心上下文、架构索引、数据模型、部署手册和`RELEASE_LOG.md`记录当前事实；产品更新165/1.30.87以高中教材为主，不另造重复版本。

## 白天证据

- `output/research-case-import/recon-20261109.json`：130只读版本、服务、课例表、帖子、资源元数据、映射、同名旧课例、课堂活动与产品更新基线。
- `output/research-case-import/cloud-listdir-api-20261109.json`、`anyshare-download-probe-20261109.json`：云盘目录与单文件下载实测，报告不含完整链接或提取码。
- `output/research-case-import/20261109-1615-research-cases/manifest.json`、`reports/`：assets、archive-inspection、resources、preview-conversion、archive-summaries、import-draft.sql、chinese-report。包含 post 15、60、70 三份课例完整数据。

## 未实测与风险

- 正式库写入、服务器文件落位、后端/Nginx切换和真实接口权限已执行；三尺寸课例面板浏览器专项未单独留证，不能把接口通过等同于完整视觉验收。
- 120MiB RAR内本地课堂工具需教师本机运行，不提供在线运行；ZIP的两个公网`.url`只保留在原包，不转为可点击独立资源。
- 音频/视频时长未用专用库逐项解析，报告只记录大小、格式与「未实测时长」；在线播放用浏览器原生控件。
- 新来源评价行为仍待用户确认；本次不改LEGACY投票规则。
- 学校旧浏览器、低配机房、真实整班并发、云盘链接未来失效与RAR内第三方工具兼容性仍需上线后观察。

## 2026-11-10 1.30.91 说明精简

- [x] 用户反馈顾意丽《返回成语现场》配套材料包使用说明过长；核实资源`resource_id=2314`原说明1,045字，旧优秀课例说明47字。
- [x] 阅读RAR内`app.py`、`templates/teacher.html`、`templates/screen.html`、`data/groups.json`、`static/generated`和`static/uploads`，确认其为课堂AI绘画工具，教学设计和课件是同课例的独立资源。
- [x] 资源说明改为91字；修正`build_archive_summary.py`，教师说明只写内容与操作，不列SHA-256、完整路径、内部程序清单；新增4项说明测试，全部24项通过。
- [x] 重新生成该批次`manifest.json`和`import-draft.sql`，压缩包说明已替换为短文案，后续重跑不会恢复旧长清单。
- [x] 整库备份`/data/backups/xueyeceping/20261110_060850_20261110_case_summary_v1/database.sql.gz`，53,147,066B，SHA-256`4ff6cf0e…`；仅更新资源2314，正式库327课例/1,050资源不变。
- [x] 产品更新169/1.30.91/PUBLISHED；真实教师API和页面均返回91字，1440/390无横向溢出、0脚本异常、0个5xx。证据`output/playwright/case-summary-1.30.91-20261110/`、`output/release-20261110-case-summary/remote-state.json`。
- [ ] 说明生成规则的源码修改尚未Git提交；本机与正式库已生效，后续重新生成manifest时使用新规则。
