# 130服务器旧发布目录清理验收

日期：2026-10-15。用户授权清理130服务器无用目录；仅做文件维护，不是产品发布。

## 清理范围与前检

- 经123跳板连接130，校验固定双端SSH主机指纹。删除对象限定 `/data/apps/xueyeceping/releases/` 下20个2026年9月旧release。
- 核对systemd/Nginx/cron/shared配置、进程启动参数/工作目录/打开文件及release间符号链接；所选目录未发现引用。路径真实解析、挂载边界及上传/数据库目录排除检查通过。
- 当前前后端均在 `20261014_seat_local_v1`；实际外置config和隔离env仍引用 `20261008_seat_install_v1`，上述目录保留。10月release和既有数据库/发布备份均保留。
- 未另行归档或生成恢复备份，直接删除指定旧目录；只保存本机清单和核验记录。

## 清理结果

- 已删除 **20个目录**，文件系统可用空间增加 **3,939,569,664 bytes**（约3.67 GiB）。
- 删除清单：

- `/data/apps/xueyeceping/releases/20260909_isolation_v1`
- `/data/apps/xueyeceping/releases/20260909_migration_resume_v1`
- `/data/apps/xueyeceping/releases/20260909_migration_resume_v2`
- `/data/apps/xueyeceping/releases/20260910_full_local_v1`
- `/data/apps/xueyeceping/releases/20260910_research_class_v1`
- `/data/apps/xueyeceping/releases/20260910_teacher_help_menu_v1`
- `/data/apps/xueyeceping/releases/20260914_lesson_fixes_v1`
- `/data/apps/xueyeceping/releases/20260916_iot_subscribe_fix_v1`
- `/data/apps/xueyeceping/releases/20260917_ai_apply_home_fix_v1`
- `/data/apps/xueyeceping/releases/20260917_paper_ai_v1`
- `/data/apps/xueyeceping/releases/20260917_paper_ai_v2`
- `/data/apps/xueyeceping/releases/20260918_classroom_gate_fix_v1`
- `/data/apps/xueyeceping/releases/20260919_full_insight_fixes_v1`
- `/data/apps/xueyeceping/releases/20260920_insight_batch_pdf_v1`
- `/data/apps/xueyeceping/releases/20260920_insight_batch_pdf_v2`
- `/data/apps/xueyeceping/releases/20260920_redo_export_fixes_v1`
- `/data/apps/xueyeceping/releases/20260921_full_datalab_classroom_v1`
- `/data/apps/xueyeceping/releases/20260921_monitor129_v1`
- `/data/apps/xueyeceping/releases/20260921_research_ai_full_v1`
- `/data/apps/xueyeceping/releases/20260921_share_grading_v1`

## 验收与边界

- 后端、Nginx、aiot-lamp均active，MainPID分别2346831、2356475、1722195；前后完全一致，NRestarts均为0，无服务重启。
- 当前JAR与首页文件SHA-256前后保持；带真实Host的首页响应哈希与当前首页文件一致。
- `/`：HTTP 200。
- `/prod-api/captchaImage`：HTTP 200，业务码200。
- `/favicon.ico?v=2`：HTTP 200。
- `/loader.css`：HTTP 200。
- `/static/js/index-CRciMTa7.js`：HTTP 200。
- `/static/css/index-CjKE6SIU.css`：HTTP 200。

当前JAR SHA-256：`d501cc74d881c1f8396d9d303e13792d12fc83b0e0d57bb1ee5a53c504b16d00`；首页SHA-256：`9bfd15a41f91a2419e4770e3878ce1c6010abacc8817b11d4bf3b0509e8b438f`。

- 未操作数据库、学生作品、业务配置、其他应用目录、系统日志或数据库备份；未执行登录/成绩写入或完整业务链回归。
- 9月旧release已不存在，历史验收中的“直接切回9月目录”步骤不再可执行。必要时须从既有备份独立核验、恢复制品后再规划切换，不因备份存在而假定历史回退已可用。
- 当前与近期回退仍按1.30.50验收及现场配置执行；本轮不改变正式版本，无SQL、构建、重启或产品更新登记。
- 原始清单/前检/后检：`output/server-cleanup-20261015/{inventory,plan,result}.json`。本地安全清理不等于已经验证所有剩余目录均必要。
