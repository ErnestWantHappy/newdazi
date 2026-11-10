---
name: research-to-textbook-cases
description: 把教研活动已核实的课程资源自动识别、下载、深度检查、生成预览并整理为“电子教材与优秀课例”，含低峰窗口幂等导入、复核和回退边界。
---

# 教研活动资源整理为电子教材与优秀课例

## 何时使用

教师把课件、教学设计、压缩包或教育云盘链接发布到「教研活动 → 教学资源」，需要把已核实课例整理进「电子教材与优秀课例」时使用。先读 `AGENTS.md`、`contexts/PROJECT_CORE.md`、`docs/architecture/INDEX.md`、`docs/architecture/DEPLOYMENT_RUNBOOK.md`，再按本 Skill 执行。

目标环境默认是 **130 Linux/systemd/Nginx 正式主站**，123 只作 SSH 跳板；不得套用 123 Windows/NSSM 脚本。凭据只能来自环境变量或 `contexts/secrets.local.md`，任何报告都不写密码、Token、云盘提取码和完整私密链接。

## 输入与状态

- `--run-id`：本次唯一批次号；中断后用同一值续跑，不重复下载或写库。
- `--post-ids`：指定帖子；默认处理已核实的 `15,60,70`。
- `--after-post-id`：扫描上次成功游标之后的新帖子；游标见 `output/research-case-import/last-success.json`。
- `--manifest`：apply/verify 使用的清单路径。
- `--confirm-low-peak-window`：apply 的显式低峰窗口确认；缺失即拒绝写正式库。
- `--publish`：只有后端、前端、接口和浏览器验收通过后，才把课例从 DRAFT 置为 PUBLISHED。

## 命令

```text
python .agents/skills/research-to-textbook-cases/scripts/run_pipeline.py scan
python .agents/skills/research-to-textbook-cases/scripts/run_pipeline.py plan --post-ids 15,60,70
python .agents/skills/research-to-textbook-cases/scripts/run_pipeline.py stage --run-id <runId> --post-ids 15,60,70
python .agents/skills/research-to-textbook-cases/scripts/run_pipeline.py apply --manifest <manifest> --confirm-low-peak-window
python .agents/skills/research-to-textbook-cases/scripts/run_pipeline.py verify --manifest <manifest>
```

`scan/plan/stage/verify` 默认只读正式库或只写本次 staging；**只有 `apply` 允许写正式库和服务器共享资源目录**。白天窗口只执行到 `stage`，数据库写入、后端重启和前端发布必须等确认的低峰窗口。

## 执行步骤

1. `scan`：只读查询 `post_type='RESOURCE'`，关联活动、作者、学校、年级/学期/课次、附件和云盘。过滤名单、课时安排、平台/工具介绍；post 60（赵琪）已补齐有效云盘链接并纳入整理。
2. `plan`：按年级 → 学期 → 教材版本/年份 → 课次 → 目录标题解析 `book_code + section_key + kind='LESSON'`。映射不唯一、标题不符、字段缺失一律 PENDING；同名旧课例按来源帖子新增，不按标题去重。
3. `stage`：
   - 私有附件只从 `/data/upload-private/research-activity/` 读取；云盘只接受 `https://yun.xsedu.net.cn`，通过 `/api/v1/link` 的 `get/listdir/osdownload` 下载，不跟随到不明域名。
   - 下载到 `output/research-case-import/<runId>/source/`，逐文件记录大小、SHA-256、MIME 和状态；同内容按哈希去重，私有 ZIP 优先。
   - 深度检查 ZIP/RAR/7Z/TAR 等：列目录、魔数、嵌套层级、文档/图片/音视频信息、密码、损坏、异常压缩比、路径穿越、绝对路径、可执行文件、`.url` 目标和个人信息。
   - 拆出教学设计、课件、任务单、工具、素材；原包保留为 ARCHIVE，并生成来自真实检查结果的简短中文「使用说明」。说明只写内容与操作，不列哈希、完整目录或内部程序清单。
   - DOCX/PPTX/XLSX 使用独立 LibreOffice user profile、独立临时目录、单进程和超时转换为 PDF；失败只取消在线预览，不阻断原文件导入。
   - 生成 `manifest.json`、`import-draft.sql`、中文报告与回退清单。
4. `apply`：再次核对源帖子更新时间、课堂近 20 分钟活动和旧课例基线；整库备份并计算 SHA-256；文件先上传到 `.staging/<runId>` 临时文件再原子改名，逐个复核服务器哈希；执行幂等 DDL 和 DRAFT 导入。默认不发布、不重启、不切前端。
5. `verify`：只读复核课例数、资源数、来源字段、映射、重复、孤儿、私有路径和文件清单；再按项目发布流程完成后端/前端、真实教师接口和 1440/390/320 浏览器验收，全部通过后才 `--publish` 并登记发布日志。

## 质量门禁

- 上课教师、学校、课程标题、活动标题、教材映射任一缺失或矛盾 → 停止。
- 云盘 403/404/过期/权限错误 → `PENDING_SOURCE_EXPIRED`，不无限重试。
- 压缩包密码未知、损坏、路径异常、疑似 zip bomb、未知可执行程序或敏感个人信息无法安全拆分 → `PENDING_ARCHIVE`/停止。
- 内容与课程标题明显不符 → 停止。
- 文件转换失败可继续，但必须报告；不得伪造预览成功。
- 正式库写入时若近 20 分钟仍有学生提交时间更新、锁等待、错误率异常、构建或发布门禁失败 → 停止并回退。
- 旧课例 `case_id`、资源、评价、来源标签和投票规则不改；新来源不自动获得旧 LEGACY 评价语义。

## 输出文件

```text
output/research-case-import/<runId>/
  source/          私有附件与云盘原件
  extracted/       从压缩包拆出的独立资源
  previews/        转换后的 PDF
  reports/         assets/archive-inspection/resources/摘要/SQL/报告
  manifest.json    幂等导入清单
```

另有 `scan-<runId>.json`、`sections-<runId>.json` 放在 `output/research-case-import/`，供后续 AI 不依赖对话记忆接力。

## 暂停与回退

- 白天只做只读、下载、检查、预览、staging 和 manifest；不写正式库、不改正式资源表、不重启、不切前端、不删旧 release。
- 回退优先 `UPDATE biz_textbook_case SET status='DRAFT' WHERE source_key IN ('research-post-15','research-post-60','research-post-70')`，不整库回灌。
- 文件回退只允许删除本次 `.staging/<runId>` 中未被数据库引用的精确路径；删除前核对绝对路径，禁止递归删除共享目录。
- 保留旧前端 release、旧后端 JAR、Nginx/systemd 配置和数据库备份；失败或回滚的候选不得登记为已上线。

## 边界

本 Skill 不执行 `git push`、不清库、不改成绩或权限、不修无关模块、不自动全局安装解包工具。项目级脚本使用 UTF-8，Windows/Linux 都可用；若本机没有 `7z/bsdtar/tar`，RAR/7Z 检查会明确转 `PENDING_ARCHIVE`，不得假装检查完成。
