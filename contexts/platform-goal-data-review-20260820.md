# 本机数据治理候选复核（2026-08-20）

本记录只包含只读查询结果，不是数据变更授权。

## 课程 1

- `lesson_id=1`，标题为 `55`。
- `dept_id` 为空，`create_by` 为空，`create_time=2025-08-14 09:43:54`，`update_time` 为空。
- 没有课程指派记录，也没有可用于反推学校的创建者或指派人。
- 结论：证据不足，不能猜测学校或批量回填；保持冻结，等待业务负责人依据原始教学资料确认。

## 区域抽测“33”

- 实际记录为 `exam_id=3`、`exam_name=33`。
- `status=1`（开启态），`grading_enabled=0`，`open_time=2026-07-22 15:34:08`，`close_time` 为空。
- 关联 7 个班、270 名学生。
- 结论：虽然名称像测试数据，但仍有真实教学关联，不能自动关闭、归档或删除。需先取得负责人确认，再生成逐条影响清单、备份和可回滚下线脚本。

## 账号重复候选

- 登录名重复组为 0；`biz_student.user_id` 保持唯一。
- `student_no` 重复组属于班级序号复用候选，不能直接视为重复账号或删除合并对象。

## 定时任务名称

- `sys_job.job_id=104` 的调用目标为 `sysPerfEventCleanupTask.cleanupExpiredPerfEvents`，代码组件和任务用途均可对应。
- 原名称为问号乱码；已在备份 `backups/20260820_goal_job104_pre/sys_job_104.sql`（SHA-256 `F29109EEAAEADF7C5F069E5A395494880199CA79325996646CB6DFCD93E30878`）后按调用目标条件修复为“系统性能事件清理”。
- 仅修改显示名称，不改变任务状态、Cron 或调用目标；回滚脚本为 `sql/platform_data_governance_v1_fix_job104_name_rollback.sql`。

## 正式库复核与执行

- 正式 `ry-vue` 的任务和导学单状态已用只读前检复核。公开测试导学单为 `sheet_id=1`、标题“测试722”，有 2 条答案；在备份后仅将父导学单改为软归档，答案未删除。
- 正式备份目录为 `D:\program\3009dazipingtai\backups\20260820_goal_confirmed_audit_fixes`；精确回滚脚本为 `sql/platform_data_governance_v1_rollback_confirmed_audit_fixes.sql`。

## 正式课程 34

- 正式课程 34 原学校为初中部（169），但 4 条指派、指派人和创建者账号均指向小学部（139）。
- 已在备份 `D:\program\3009dazipingtai\backups\20260820_goal_lesson34_scope` 后，将课程学校精确改为 139；后检 4 条指派全部一致，跨校指派为 0。
- 课程 1 仍缺少创建者、指派和可信学校线索，继续保持冻结。
