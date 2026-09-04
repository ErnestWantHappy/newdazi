# AI 发布登记单（Agent Release Log）

> 规则（AGENTS.md 硬性要求）：**任何代码修改部署上线到 10.52.1.123 前，必须新增一行登记，并同步写入平台更新记录（biz_platform_update）。**
> 登记时机：发布探活成功后立即登记；平台更新先写草稿，管理员确认后置为 PUBLISHED（或按已有授权直接发布）。

| 日期 | 版本号 | release 目录 / 描述 | 本次改动摘要 | 平台更新已写入 |
| --- | --- | --- | --- | --- |
|（示例）2026-08-22 | 1.25.0 | releases/20260822_xxx_v1 | 学生实验工具上线 | 是 |
| 2026-08-23 | 1.25.0 | releases/20260823_student_tool_v1 | 学生实验工具、题目开放开关、129监控增强、帮助中心推荐环境、版权精简、Agent词典（正式发布+SQL执行） | 是（正式库 PUBLISHED） |
| 2026-08-23 | 1.25.1 | releases/20260823_student_tool_hotfix_v1 | 热修 4 个线上缺陷：课程设计器保存失败（iot_enabled 插入列值不匹配）、成绩查询点课报错（历史课优雅降级）、教师成绩页题目开放开关卡片、学生端静默轮询不打断打字（正式发布，前端+后端） | 是（正式库 PUBLISHED，update_id=48） |
| 2026-08-23 | 1.25.2 | releases/20260823_iot_frontend_v1 | 物联网修复与重构：消息表外键移除+接收异常隔离与断线重连（教师端收不到数据根因修复）、教师端页面重构为小组数据总览大表+小组历史明细、学生端新增本组历史数据列表、学生端样式美化与复制按钮修复（正式发布，前端+后端，无增量SQL——外键删除已直接在正式库执行） | 是（正式库 PUBLISHED，update_id=49） |
| 2026-08-23 | 1.26.0 | releases/20260823_iot_frontend_v1（后端已合入判题扩容）+ 129 `/srv/judge0-python` | Python 判题方案 A：400 份同时提交排队、10 路真实判题；123 执行器 10/10/1000、课程并发 60，129 worker 10/队列 512；50/100/200/400 正式阶梯压测全部通过，验收数据清零 | 是（正式库 PUBLISHED，update_id=50） |
| 2026-08-24 | 1.26.1 | releases/20260824_python_iot_ux_v1 | Python 刷题县级机构降级、OJ 切题与判题状态、课程题型初始开放、首次保存在线协作、物联班级入口与数据弹窗、帮助和监控文案、隐藏若依官网菜单 | 是（正式库已发布，update_id=51） |
| 2026-08-24 | 1.26.2 | releases/20260824_course_designer_python_input_v1 | 课程设计器紧凑布局、导学单与说明精简、固定已选题操作列、协作文件题前置提示、课程 Python stdin 自定义运行 | 是（正式库已发布，update_id=52） |
| 2026-08-26 | 1.26.3 | releases/20260826_operation_ai_usage_archive_v1 | 修复课程旧题答案污染“已批改”统计；删题前事务归档；AI 模型参考价、任务 token/理论费用与百炼错误中文提示 | 是（正式库已发布，update_id=53） |
| 2026-08-27 | 1.26.4 | releases/20260827_162145_1247099_shared_course_sticky_v1 | 教师首页标识共享课程与创建教师，服务端下发设计/删除能力并修正删除校验顺序；学生端顶部导航滚动吸顶 | 是（正式库已发布，update_id=54） |
| 2026-08-29 | 1.26.5 | releases/20260829_course_designer_default_open_v1（前端） | 课程设计器现有“学生开放”理论题、操作题双开关默认开启；不覆盖存量班级状态，不改成绩查询与课程推进 | 是（正式库已发布，update_id=55） |
| 2026-08-31 | 1.27.0 | releases/20260831_084148_c0aa4f8_flowchart_v1 | 画程流程图操作题首期正式上线：教师标准答案/基础图、学生受限编辑与草稿提交、结构检查建议与教师确认；补齐节点单击/拖拽、节点/连线删除、开始/结束独立、输入输出文字与四边中点连接点 | 是（正式库 PUBLISHED，update_id=56） |
| 2026-08-31 | 1.27.1 | 服务器配置批次 `20260831_094758_teacher_tool_gateway_v1` | 教师工具统一改为 D 盘 Nginx 80 端口路径；3006 注册 NSSM 自动服务并配置异常重启；停用 C 盘 OpenResty 与旧 Nginx 计划任务，确保 80/3010/3012 只有 UnifiedNginx 进程树 | 是（正式库 PUBLISHED，update_id=57） |
| 2026-08-31 | 1.27.2 | releases/20260831_primary_iot_v1 | 小学实验板标准 MQTT 正式接入：8 个班级账号与精确 Topic ACL 同步、同步状态与失败重试、教师/学生 Python 代码入口；删除 class_* 宽权限与临时探针账号，完成本班允许/跨班拒绝/订阅拒绝验收 | 是（正式库 PUBLISHED，update_id=58） |
| 2026-08-31 | 1.27.3 | releases/20260831_primary_iot_student_python_v2（仅前端） | 学生物联页面默认打开小学实验板 Python 页签，将一键复制本人小组 Python 代码置于首要入口；初中 Mind+ 页签继续保留，后端和数据库不变；Nginx 精确重启后生效 | 是（正式库 PUBLISHED，update_id=59） |
| 2026-08-31 | 1.27.4 | releases/20260831_primary_iot_python_template_v1（仅前端） | 小学一键复制代码改为 N17 示例格式，补齐 utf_8 文件头、npython 导入说明和 oled.print 屏幕状态显示；MQTT 参数与初中 Mind+ 不变 | 是（正式库 PUBLISHED，update_id=60） |
| 2026-08-31 | 1.27.5 | releases/20260831_student_class_99_v1 | 学生新增、Excel 导入及上传前校验统一允许班级号 01～99，支持 11 班及以上；00、100、601/602 等非法格式继续拦截；前后端发布并完成正式探活 | 是（正式库 PUBLISHED，update_id=61） |
| 2026-09-01 | 1.27.6 | releases/20260901_student_add_validation_hotfix_v4/frontend（仅前端） | 修复单个新增学生时 `10～99` 学号被错误前端正则拦截的问题；后端、数据库结构不变，Nginx 重启后 3009/3010/验证码/静态脚本均 200 | 是（正式库 PUBLISHED，update_id=62） |
| 2026-09-01 | 1.27.7 | releases/20260901_operation_attachment_preview_cache_hotfix_v2（后端） | 修复操作题 Office 命中页图缓存时漏写当前附件 PDF 预览状态；启动对账已有 PDF、无 PDF 重新入队，消除 pending/队列为 0 的状态误报；正式库无结构 SQL | 是（正式库 PUBLISHED，update_id=63） |
| 2026-09-01 | 1.27.8 | releases/20260901_172047_teacher_feedback_governance_v1/frontend_v4（前端；后端沿用 1.27.7 已合入制品） | 修复课堂表现按列导出，课程选题显示出题人，教师课程增删即时反馈，批改未交提示，题库操作列固定和学生导出；学生导入改批量事务、同校锁和结构化耗时，诊断区分业务提示/慢接口/系统异常；正式库新增账号查重联合索引 | 是（正式库 PUBLISHED，update_id=64） |
| 2026-09-01 | 1.28.0 | releases/20260901_scheme2_score_numeric_v1 | 方案二安全处理正式发布：课程归档字段、学生停用/纠错与删除保护；成绩查询及班级/导学单/Python 列表改自然数值排序；后端与 Vue3 前端同步切换 | 是（正式库 PUBLISHED，update_id=65） |
| 2026-09-01 | 1.28.1 | releases/20260901_score_numeric_sort_hotfix_v1（仅前端） | 修复 Element Plus `sort-method` 参数误用导致的学号字典序；成绩主表、课堂表现、排行榜、理论详情改为正确自然数值排序回调，课程成绩列改用 `sort-by` | 是（正式库 PUBLISHED，update_id=66） |
| 2026-09-02 | 1.28.2 | releases/20260902_student_correction_upload_hotfix_v1（仅前端） | 修复学生管理批量纠错上传 Excel 未按 multipart/form-data 发送、后端收到空文件并提示系统繁忙；保留旧前端 release，未修改后端、业务数据或上传大小限制 | 是（正式库 PUBLISHED，update_id=67） |
| 2026-09-02 | 1.28.3 | releases/20260902_student_entry_year_grade_v1（仅前端） | 学生管理按当前校区学部自动在入学年份后显示当前年级：小学 x年级、初中 初x、高中 高x；筛选和保存仍提交纯年份 | 是（正式库 PUBLISHED，update_id=68） |
| 2026-09-02 | 1.28.4 | releases/20260902_research_public_notice_share_v1 | 教研活动 NOTICE 通知支持随机令牌公开分享，匿名查看正文及受控图片；支持 7 天、30 天、永久有效期与撤销；后端+Vue3+增量 SQL | 是（正式库 PUBLISHED，update_id=69） |
| 2026-09-02 | 1.28.5 | releases/20260902_research_public_notice_share_hotfix_v1/frontend（仅前端） | 修复公开分享链接页面空白（匿名接口返回对象直接赋值），并为 HTTP 正式地址增加 `document.execCommand('copy')` 复制降级；后端、数据库结构不变 | 是（正式库 PUBLISHED，update_id=70） |
| 2026-09-02 | 1.28.6 | releases/20260902_flowchart_frontend_restore_v1/frontend（仅前端） | 修复 1.28.5 前端热修覆盖画程流程图资源的问题，恢复题库流程图操作题新增、编辑和学生作答入口；后端、数据库结构和题目数据不变 | 是（正式库 PUBLISHED） |
| 2026-09-02 | 1.29.0 | releases/20260902_135546_full_restore_v1 | 紧急全量恢复：后端+前端同步部署，修复今天多次热修导致线上后端 JAR 缺失 FlowchartController（流程图 404）和 TeacherGradingController（批改 404）；从包含所有最新代码的本地源码重新编译；外置 config/ 从 scheme2_v1 复制，nginx root 已切换；无增量 SQL | 是（正式库 PUBLISHED，update_id=71） |

