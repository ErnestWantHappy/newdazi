# 选择题图片增强 A：发布候选 20261108

**候选状态：可审阅、迁移已演练、课堂空闲已确认；硬阻断为生产图片目录不存在（需 sudo）+ 生产时间窗未授权。**

2026-11-08。本文记录隔离正式源码的发布候选制品、白名单哈希、迁移顺序、回退路径、已知限制与未验边界。工作区 HEAD **`9d920c4`**（`feat: 题库工作台改版、操作题 PY 运行与审查修复，上线 1.30.72`），工作区 `git status --porcelain` 仍 **152 行**（含其他在研修改），**全程零 Git 写操作**。

---

## 1. 功能概述

- **教师**：录题支持题干 3 图/每选项 1 图，点击上传/粘贴截图/拖拽；Excel 一次识别图文、在线预览修正、原子确认、导出回导；修改已用含图题自动另存保留历史，响应丢失可按回执找回。
- **学生**：普通课/抽测/错题正常展示图文选择题，图片可放大，A—D 答题与评分保持。
- **私有存储**：原图在 `<profile目录名>-question-images/` 私有目录，经业务鉴权读取，不置于公开 `/profile/` 映射。
- **迁移**：两列（`biz_question.media_json`、`biz_county_exam_paper_question.media_json`）+ 四表（图片、保存回执、导入批次/明细）+ 索引，全幂等。
- **回退边界**：只能回到**能读媒体的兼容程序**，不能直接回不识图前端；仅在两列全 NULL 且无媒体记录时，才可在备份+人工确认后删表列；不回灌整库。

详细需求、设计、任务、ADR 见 `contexts/choice-question-media/`；本机验收见 `docs/choice-question-images-development-verification-20261106.md`。

---

## 2. 制品与哈希

### 2.1 制品位置

```
output/release-candidates/20261108_choice_media_a/artifacts/
```

**仅两个制品文件**，无 `manifest.json`：

| 制品 | 字节 | SHA-256 |
| :--- | ---: | :--- |
| `ruoyi-admin.jar` | 108,283,591 | `89E1BD93E30B470F420549FC0B91AFBF4DF933BFA9310F1E68C19F83B5F906AE` |
| `frontend.zip` | 43,940,620 | `C85972D79BAE404A461812B0A5042DB7DE3D68F2C18E17C6A00BBAFF8BE4DB47` |

### 2.2 前端 ZIP 结构

- **820 个条目**，根目录平铺：`index.html`（1,199 B）、`index.html.gz`、`favicon.ico`、`loader.css`、`assets/`、`datalab/`、`static/`。
- **`index.html` 包内字节 SHA-256**（用 Python `ZipFile` 读字节计算，非文件系统）：**`BF6C5B73EFF26BB3E284C11C106AB691ECA656FBF749EC114368311D64F1E278`**
- 前端为**全量 dist 整目录替换**：所有 chunk 文件名变化，已打开页面懒加载会 404 直到刷新；**必须保留旧 release 目录做回切点**。

---

## 3. 上线白名单

### 3.1 白名单来源

隔离正式源码的工作树位于 `output/release-candidates/20261108_choice_media_a/{RuoYi-Vue,RuoYi-Vue3}`；按以下规则复原：

1. **后端**：130 正式 JAR `/data/apps/xueyeceping/releases/20261108_teacher_tools_v1/ruoyi-admin.jar`（108,222,401 B，SHA `ca13521278d3516a099073b9db5dcc3b11739ccae475b14c9e735ec84c105d41`）反编译提取五个共用 `.class` 作源码哈希基准。
2. **前端**：130 正式 dist `/data/apps/xueyeceping/releases/20261108_teacher_tools_frontend_v1/frontend`（`index.html` SHA `0a2702000274026d4a0f82f7b6cd6fcf9b986f3642912c7a67514450d87c71e5`）解压作基准。
3. **叠加本功能文件**：15 个新增主代码 + 4 个新增测试 + 18 个共用文件（后端 5 个 + 前端 13 个）的按需差异。

### 3.2 前端白名单（2 个文件）

`output/release-candidates/20261108_choice_media_a/overlay-frontend.json`：

```json
[
  {
    "path": "src/components/QuestionMedia/QuestionMediaInput.vue",
    "sha256": "6F5FC68CBD53D4D7B55B123BD78614E273C2B57B3E916EC4AED8493331482B2C"
  },
  {
    "path": "src/views/business/question/components/QuestionImportPreview.vue",
    "sha256": "1E7F4952F628A074BFAF680012D660B952AE72DA6815ADD7F5835B18FCA1F986"
  }
]
```

**白名单哈希逐项复验**：在隔离工作树 `output/release-candidates/20261108_choice_media_a/RuoYi-Vue3/` 中重算 **2 项，bad=0**。

### 3.3 后端白名单（33 个文件）

`output/release-candidates/20261108_choice_media_a/overlay-backend.json` 共 **33 项**：

- **11 个新增主代码**：`QuestionImageController`、`QuestionImportController`、`QuestionMedia`、`QuestionImageCleanupJob`、`QuestionImageService`、`QuestionImportService`、`QuestionMediaStore`、`QuestionSaveService`、`QuestionMediaCodec`、`QuestionWorkbookReader`、`QuestionWorkbookWriter`
- **4 个新增测试**：`QuestionMediaCodecTest`、`QuestionSaveServiceTest`、`QuestionWorkbookReaderTest`、前端 `questionMedia.test.js`
- **18 个共用文件**（后端 5 个 + 前端 13 个）：按差异块核对，仅本功能调用链受影响部分叠加。

**白名单哈希逐项复验**：在隔离工作树 `output/release-candidates/20261108_choice_media_a/RuoYi-Vue/` 中重算 **33 项，bad=0**。

后端五个共用源码文件及其差异：`BizQuestion`（新增 `media_json` 字段与透传）、`BizQuestionController`（保存回执与最终 ID）、`BizQuestionServiceImpl`（含图依赖防删、锁题扩展、媒体复制）、`BizLessonServiceImpl`（课程锁题）、`CountyExamServiceImpl`（抽测锁题）。前端 13 个共用文件为正常课/抽测/错题/教师题库/组卷页面的媒体透传入口。

---

## 4. 前端归一化等价证据

### 4.1 候选 dist vs 正式 dist 文件级对比

- 候选 `frontend.zip` 解压后：**499 个文件**
- 正式 `/data/apps/xueyeceping/releases/20261108_teacher_tools_frontend_v1/frontend`：**499 个文件**
- 文件级哈希不同：**206 个文件**（主要是 Vite chunk，含 hash 文件名）

### 4.2 按内容哈希归一化的多重集合比对

证据文件：`output/choice-media/dist-normalized-diff-20261108.json`

```json
{
  "differ": [
    "static/css/QuestionImportPreview.css",
    "static/js/QuestionImportPreview.js"
  ]
}
```

**归一化后仅 2 个真实差异**，正好是白名单 2 个 Vue 文件编译产物。

### 4.3 Vite 模块依赖图

- 候选构建模块图：**378 个逻辑模块**
- 正式构建模块图：**378 个逻辑模块**
- **模块图 0 增减**，符合「只叠加必要文件、不改无关模块」口径。

详细证据：`output/choice-media/dist-logical-diff-20261108.json`（4,882 B）。

---

## 5. 迁移与数据库

### 5.1 迁移 SQL

**`sql/choice_question_media_v1.sql`**（68 行），内容：

1. **两列**（条件 ALTER）：`biz_question.media_json TEXT NULL`、`biz_county_exam_paper_question.media_json TEXT NULL`
2. **四表**（`IF NOT EXISTS`）：
   - `biz_question_image`（图片元数据、鉴权路径、sha256、state、绑定时间）
   - `biz_question_save_receipt`（幂等回执、owner + requestId 唯一键）
   - `biz_question_import_batch`（导入批次、owner + requestId 唯一键、revision）
   - `biz_question_import_item`（逐题草稿、batch_id + source_key 唯一键）
3. **索引**：图片表三索引（唯一 path、owner+state+time、cleanup）、回执表一唯一键、导入两表各一唯一键 + 一普通键

**全脚本幂等**，重复执行安全。

### 5.2 迁移演练（已完成）

证据文件：`output/choice-media/migration-rehearsal-20261108.json`（状态 `REHEARSED`）

- 临时库：`choice_media_rehearsal_1a17e13ea8`（已删除）
- 备份：`output/choice-media/backups/20261106-155601/before.sql`（3,839,805 B，SHA-256 `c3e32c6c5dcb20b68be919ed13d82c10de5916e68f87de42ad1777850464b432`）
- 演练步骤：
  1. 导入备份 → `biz_question` 1738 行、`biz_county_exam_paper_question` 4187 行
  2. 首次执行 SQL → 成功
  3. 重复执行（幂等检查）→ 成功
  4. 后检：两列存在、四表存在、旧表行数 1738/4187 不变
  5. 演练回退 SQL → 成功
  6. 回退后再次前向 → 成功

**结论**：首次+重复幂等、旧表行数不变、回退后数据完好。

### 5.3 正式迁移后检清单

执行 `sql/choice_question_media_v1.sql` 后，**必须逐项复核**：

```sql
-- 1. 两列存在性
SELECT COUNT(*) FROM information_schema.columns 
WHERE table_schema=DATABASE() 
  AND table_name='biz_question' AND column_name='media_json';  -- 期望 1

SELECT COUNT(*) FROM information_schema.columns 
WHERE table_schema=DATABASE() 
  AND table_name='biz_county_exam_paper_question' AND column_name='media_json';  -- 期望 1

-- 2. 四表存在性
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema=DATABASE() 
  AND table_name IN ('biz_question_image','biz_question_save_receipt',
                     'biz_question_import_batch','biz_question_import_item');  -- 期望 4

-- 3. 旧表行数不变（130 正式已知口径）
SELECT COUNT(*) FROM biz_question;  -- 期望 4792（2026-11-08 18:29 实测）
SELECT COUNT(*) FROM biz_county_exam_paper_question;  -- 期望 4187

-- 4. 新表初始行数
SELECT COUNT(*) FROM biz_question_image;  -- 期望 0（迁移不回填）
SELECT COUNT(*) FROM biz_question_save_receipt;  -- 期望 0
SELECT COUNT(*) FROM biz_question_import_batch;  -- 期望 0
SELECT COUNT(*) FROM biz_question_import_item;  -- 期望 0

-- 5. 索引存在性（示例）
SHOW INDEX FROM biz_question_image WHERE Key_name='uk_question_image_path';
SHOW INDEX FROM biz_question_save_receipt WHERE Key_name='uk_question_save_request';
SHOW INDEX FROM biz_question_import_batch WHERE Key_name='uk_question_import_request';
SHOW INDEX FROM biz_question_import_item WHERE Key_name='uk_question_import_source';
```

---

## 6. 生产前检结果（2026-11-08 18:29）

证据文件：`output/choice-media/production-preflight-20261108.json`

### 6.1 线上基线（未变，与交接一致）

- **前端**：`/data/apps/xueyeceping/releases/20261108_teacher_tools_frontend_v1/frontend`，`index.html` SHA-256 `0a2702000274026d4a0f82f7b6cd6fcf9b986f3642912c7a67514450d87c71e5`
- **后端 JAR**：`/data/apps/xueyeceping/releases/20261108_teacher_tools_v1/ruoyi-admin.jar`，108,222,401 B，SHA-256 `ca13521278d3516a099073b9db5dcc3b11739ccae475b14c9e735ec84c105d41`
- **systemd 状态**：MainPID `3001221`、NRestarts `0`、监听 `3009`
- **最新整库备份**：`/data/backups/xueyeceping/20261108_133127_20261108_teacher_tools_v1/database.sql.gz`，70,843,581 B，SHA-256 `0ef97f1bc66b32d3e669e61c9493e8a1a5e9035596232c2c9a2928f987e51b36`，`gzip -t` OK
- **数据事实**：`biz_question` 4792 行、`biz_county_exam_paper_question` 4187 行、`biz_platform_update` 158 条（最新 1.30.80 PUBLISHED）
- **磁盘**：`/data` 可用 331G，inode 3%
- **Nginx**：root 见上；除 `/profile/` 外 `NO_PUBLIC_MAPPING`（`question-images` 无公开映射）
- **接口探活**：`/` 与 `/prod-api/captchaImage` 均 200

### 6.2 图片目录状态（**硬阻断**）

```bash
ls -ld /data/upload-question-images
# ls: cannot access '/data/upload-question-images': No such file or directory
# 状态：NOT_EXISTS
```

- `/data` 为 `root:root 755`，`admin2` 不可写
- `/data/upload` 为 `admin2:admin2 775`（profile 可写）
- **`/data/upload-question-images` 不存在**（唯一技术硬阻断）

**上线前必须执行**（需 `sudo`）：

```bash
sudo mkdir -p /data/upload-question-images
sudo chown admin2:admin2 /data/upload-question-images
sudo chmod 750 /data/upload-question-images
# 复核
sudo -u admin2 test -w /data/upload-question-images && echo "OK" || echo "FAIL"
```

### 6.3 课堂空闲（已确认）

证据文件：`output/choice-media/production-idle-20261108.json`（2026-11-08 18:56:45）

- `lesson_status_1/2/3`：4（均 10-03/10-05 历史残留）
- `lesson_today`：0
- `answer_last_30min`：0
- `answer_today`：0
- 最近答卷：`2026-11-06 16:31:02`

**结论**：课堂空闲，无今日课或近 30 分钟答卷。

---

## 7. 备份计划

### 7.1 数据库

```bash
# 整库备份到新 release 同级 backups 目录
RELEASE_DIR=/data/apps/xueyeceping/releases/20261108_choice_media_a
BACKUP_DIR=${RELEASE_DIR}/backups/database
mkdir -p ${BACKUP_DIR}

mysqldump -h 127.0.0.1 -u root -p'<密码>' --single-transaction --routines --triggers \
  --set-gtid-purged=OFF ry-vue | gzip > ${BACKUP_DIR}/database.sql.gz

# 计算哈希
sha256sum ${BACKUP_DIR}/database.sql.gz | tee ${BACKUP_DIR}/database.sql.gz.sha256

# 验证 gzip 完整性
gzip -t ${BACKUP_DIR}/database.sql.gz && echo "Backup OK" || echo "Backup CORRUPTED"
```

### 7.2 旧制品与配置

```bash
# 备份旧 JAR
cp /data/apps/xueyeceping/releases/20261108_teacher_tools_v1/ruoyi-admin.jar \
   ${BACKUP_DIR}/ruoyi-admin.jar.old
sha256sum ${BACKUP_DIR}/ruoyi-admin.jar.old | tee ${BACKUP_DIR}/ruoyi-admin.jar.old.sha256

# 备份旧前端 root（仅记录路径，不复制整目录）
echo "/data/apps/xueyeceping/releases/20261108_teacher_tools_frontend_v1/frontend" \
  > ${BACKUP_DIR}/old-frontend-root.txt
sha256sum /data/apps/xueyeceping/releases/20261108_teacher_tools_frontend_v1/frontend/index.html \
  | tee ${BACKUP_DIR}/old-frontend-index.sha256

# 备份 systemd 配置与环境变量
systemctl cat xueyeceping > ${BACKUP_DIR}/xueyeceping.service.txt
sudo systemctl show xueyeceping --property=Environment > ${BACKUP_DIR}/xueyeceping.env.txt

# 备份 Nginx 站点配置
cp /etc/nginx/sites-available/xueyeceping ${BACKUP_DIR}/nginx-xueyeceping.conf
```

---

## 8. 发布顺序（前后端同窗口）

### 8.1 预建图片目录（sudo，需提前确认）

见 6.2 节。

### 8.2 数据库迁移

```bash
# 执行增量 SQL
mysql -h 127.0.0.1 -u root -p'<密码>' ry-vue < sql/choice_question_media_v1.sql

# 逐项后检（见 5.3 节）
# 必须确认两列四表索引存在、旧表行数 4792/4187 不变、新表 0 行
```

**迁移成功才启新后端**，否则新后端引用 `media_json` 列会报 Unknown column。

### 8.3 后端切换

```bash
RELEASE_DIR=/data/apps/xueyeceping/releases/20261108_choice_media_a

# 上传新 JAR 到新 release 目录
scp output/release-candidates/20261108_choice_media_a/artifacts/ruoyi-admin.jar \
  admin2@10.52.1.130:${RELEASE_DIR}/

# 校验 SHA-256
ssh admin2@10.52.1.130 "cd ${RELEASE_DIR} && sha256sum ruoyi-admin.jar"
# 期望：89E1BD93E30B470F420549FC0B91AFBF4DF933BFA9310F1E68C19F83B5F906AE

# 修改 systemd WorkingDirectory（需 sudo）
sudo systemctl edit xueyeceping --full
# 修改 WorkingDirectory=/data/apps/xueyeceping/releases/20261108_choice_media_a

# 重启后端
sudo systemctl restart xueyeceping

# 探活
sleep 10
systemctl status xueyeceping  # 检查 MainPID / NRestarts / Active
sudo ss -tlnp | grep 3009  # 确认监听
curl -I http://localhost:3009/prod-api/captchaImage  # 期望 200
```

### 8.4 前端切换（同窗口）

```bash
RELEASE_DIR=/data/apps/xueyeceping/releases/20261108_choice_media_a
FRONTEND_DIR=${RELEASE_DIR}/frontend

# 上传并解压前端
scp output/release-candidates/20261108_choice_media_a/artifacts/frontend.zip \
  admin2@10.52.1.130:${RELEASE_DIR}/
ssh admin2@10.52.1.130 "cd ${RELEASE_DIR} && unzip -q frontend.zip -d frontend"

# 校验包内 index.html 哈希（用 Python）
ssh admin2@10.52.1.130 "python3 -c \"
import zipfile, hashlib
with zipfile.ZipFile('${RELEASE_DIR}/frontend.zip') as z:
    data = z.read('index.html')
    print(hashlib.sha256(data).hexdigest().upper())
\""
# 期望：BF6C5B73EFF26BB3E284C11C106AB691ECA656FBF749EC114368311D64F1E278

# 修改 Nginx root（需 sudo）
sudo vim /etc/nginx/sites-available/xueyeceping
# 修改 root /data/apps/xueyeceping/releases/20261108_choice_media_a/frontend;

# Nginx reload（平滑切换）
sudo nginx -t && sudo systemctl reload nginx

# 等待 worker 切换窗口（最多 30 秒）
sleep 30

# 校验首页哈希（新连接）
curl -s http://10.52.1.130/index.html | sha256sum
# 期望：bf6c5b73eff26bb3e284c11c106ab691eca656fbf749ec114368311d64f1e278
```

---

## 9. 双向回切点

### 9.1 前端回切（独立）

```bash
# 改回旧 Nginx root
sudo vim /etc/nginx/sites-available/xueyeceping
# root /data/apps/xueyeceping/releases/20261108_teacher_tools_frontend_v1/frontend;

sudo nginx -t && sudo systemctl reload nginx
sleep 30

# 验证旧首页哈希
curl -s http://10.52.1.130/index.html | sha256sum
# 期望：0a2702000274026d4a0f82f7b6cd6fcf9b986f3642912c7a67514450d87c71e5
```

**前端可独立回切**，不影响后端；但旧前端无法显示图片，只能作为临时降级。

### 9.2 后端回切（需前端同步）

```bash
# 改回旧 systemd WorkingDirectory
sudo systemctl edit xueyeceping --full
# WorkingDirectory=/data/apps/xueyeceping/releases/20261108_teacher_tools_v1

sudo systemctl restart xueyeceping
sleep 10
systemctl status xueyeceping
curl -I http://localhost:3009/prod-api/captchaImage  # 期望 200
```

**后端回切时前端必须同步回切**，否则前端请求图片接口会 404（旧后端无图片 handler）。

### 9.3 数据库回切（仅保数据方案）

**按 `sql/choice_question_media_v1_rollback.md` 规则**：

1. **不直接删表列**，除非两列全 NULL 且无媒体记录（人工确认后执行）。
2. **优先回到能读媒体的兼容程序**，不能直接回不识图前端。
3. 已有真实图文题或答卷时，**不可长期回到不能呈现图片的旧前端**。
4. **禁止整库回灌**（会覆盖上线后的成绩）。

---

## 10. 验收清单（独立验收记录）

### 10.1 探活

- [ ] systemd 状态：MainPID 变化、NRestarts=0、Active=running
- [ ] 端口监听：`ss -tlnp | grep 3009` 有新 PID
- [ ] 首页哈希：新连接 `curl` 得到候选 `index.html` SHA-256 **`bf6c5b73...`**
- [ ] 验证码：`curl -I http://10.52.1.130/prod-api/captchaImage` 返回 200
- [ ] 业务接口：真实教师 Token `/business/question/list` 返回 200（不是 401）

### 10.2 真实角色功能（用独立验收记录，不动真实班级/学生/成绩）

- [ ] **教师建题**：新建一道图文选择题（题干 1 图 + 选项 B 有图），保存成功，题库可查看
- [ ] **Excel 导入**：上传一个含 3 题 5 图的 Excel，预览正确，确认导入，题库可查看三道新题
- [ ] **教师组卷**：创建独立验收课程，从题库选入刚建的图文题，预览显示图片
- [ ] **学生答题**：用独立验收学生账号，进入验收课程，看到图文题，选择答案并提交，刷新后锁定
- [ ] **评分与学情**：教师查看该学生成绩，学情页显示图文题与学生答案
- [ ] **历史回看**：学生查看错题/历史答案，图片正常显示
- [ ] **越权拒绝**：匿名访问 `/business/question-images/<imageId>/content` 返回 401，其他教师/学生访问不属于自己的图片返回 403

### 10.3 回归（不能破坏已上线功能）

- [ ] 旧纯文字题查看/编辑/保存不受影响
- [ ] 历史课程/抽测/成绩可正常查看
- [ ] 课堂 Python、教材资源、教师工具等其他功能正常

### 10.4 验收数据清理

验收完成后，**精确清理本轮测试数据**（验收课程、验收学生、验收题目、上传图片），不动真实班级/学生/成绩。

---

## 11. 已知限制与未验边界

### 11.1 P1 已知限制（必须进最终报告）

1. **线上 1.30.80 错位态**：前端有图文录题 UI、后端无图片接口，三条图片接口实测 404。
2. **清空最后一张图后磁盘不减**：`biz_question_image` 行保持 `RETAINED`，不自动清理 → 磁盘只增不减。
3. **同 `requestId` 并发上传可能看到空预览**。
4. **七天清理任务跨期运行未实测**。
5. **抽测阅卷只走操作题**（`CountyExamAnswerMapper.xml:181`、`:215` 硬过滤 `question_type='practical'`），选择题图文不经该页，不改 `readable()`。
6. **小疑点**：`examDesigner.vue` 对已持久题传 `{examId}`，若教师能进该页读图会 403；当前菜单仅教研员/管理员，未发现实际影响。
7. **前端为全量 dist 整目录替换**：所有 chunk 文件名变化，已打开页面懒加载会 404 直到刷新；必须保留旧 release 目录做回切。

### 11.2 未验边界（报告要如实标注）

- **真实接口组合**：陈旧指纹、防删、自动另存、回执重放、并发保存、含图旧题加图、移除最后一张图
- **导入中途失败整批回滚、并发确认只落一批**（代码已确认，未跑真实接口）
- **真实 Excel/WPS 多版本样本**（50 题 76 图、同格图文、纯图、跨格/合并、导出再导入）
- **慢网与集中读图并发**（仅隔离环境）
- **390/320px 窄屏、图片放大不误选**（本机已验，生产未验）

---

## 12. 证据文件索引

| 文件 | 内容 |
| :--- | :--- |
| `output/release-candidates/20261108_choice_media_a/overlay-{backend,frontend}.json` | 白名单清单（33+2） |
| `output/choice-media/dist-normalized-diff-20261108.json` | 前端归一化等价证据（2 个真实差异） |
| `output/choice-media/dist-logical-diff-20261108.json` | Vite 模块图对比（378 vs 378） |
| `output/choice-media/migration-rehearsal-20261108.json` | 迁移演练（REHEARSED） |
| `output/choice-media/production-idle-20261108.json` | 课堂空闲（0 今日课/0 近 30 分钟答卷） |
| `output/choice-media/production-preflight-20261108.json` | 生产前检（图片目录不存在） |
| `docs/choice-question-images-development-verification-20261106.md` | 本机验收（后端 933 项/媒体 22 项/前端 40 项/真实 50 题 76 图） |
| `contexts/choice-question-media/{requirements,design,tasks,ADR-001}.md` | 需求、设计、任务、架构决策 |
| `sql/choice_question_media_v1.sql` | 增量迁移 SQL（68 行，幂等） |
| `sql/choice_question_media_v1_rollback.md` | 回退规则（只能回能读媒体的兼容程序） |

---

## 13. 交付清单

- [x] 制品存在：2 个文件，哈希已核对
- [x] 白名单清单：前端 2 项 + 后端 33 项，逐项复验通过
- [x] 前端归一化等价：206 chunk → 2 个真实差异，模块图 0 增减
- [x] 迁移演练：首次+重复幂等、旧表行数不变、回退后数据完好
- [x] 课堂空闲：0 今日课、0 近 30 分钟答卷
- [x] 生产前检：基线未变、整库备份可用、磁盘充足
- [ ] **图片目录预建**（sudo，待执行）
- [ ] **生产时间窗**（待用户授权）

---

**下一步**：仅向用户索要**生产时间窗**（目标 130 与项目已确认）。授权后按本文 8～10 节执行备份、迁移、切换、探活、验收、清理、登记。
