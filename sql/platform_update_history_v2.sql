-- =============================================================
-- 平台更新历史补录 v2（2026-08-22）
-- 用途：在 v1（1.0.0~1.7.0 共 8 条）基础上，把已开发上线但未记录进平台更新的功能补齐。
-- 数据来源：contexts/context.md 历史发布记录 + contexts/PROJECT_CORE.md 已上线功能清单。
-- 说明：全部幂等（按 version_no 防重复）；描述面向教师/教研员，纯文本多行。
-- 目标库：本机开发库 / 正式库。执行前先备份目标库。
-- =============================================================

INSERT INTO `biz_platform_update`
  (`version_no`, `title`, `content`, `published_at`, `status`, `create_by`, `create_time`)
SELECT s.version_no, s.title, s.content, s.published_at, 'PUBLISHED', 'AI 发布记录', s.published_at
FROM (
  SELECT '1.8.0' AS version_no, '教师工具与帮助中心上线' AS title,
    CONCAT('新增“教师工具”单页导航，按年级和学科整理常用教学工具，支持搜索与快速定位。\n新增“帮助中心”，为备课、课程设计、批改与学情提供图文分步教程。\n优化教师首页，按课程开设年级分栏，历史课程可折叠，页面加载更快。') AS content,
    '2026-08-10 15:30:00' AS published_at
  UNION ALL SELECT '1.9.0' AS version_no, '教师工具入口修复与课堂服务自动启动' AS title,
    CONCAT('修复打字平台、网络仿真、图像识别、小学实验等课堂工具的失效入口。\n新增三个课堂工具服务的开机自启动，地址更加稳定。') AS content,
    '2026-08-10 17:00:00' AS published_at
  UNION ALL SELECT '1.10.0' AS version_no, '高并发访问性能大幅提升' AS title,
    CONCAT('对成绩查询、历史成绩、班级人数等常用页面进行查询优化，新增索引。\n修复高并发批量交卷中的超时与失败问题，课堂整班同时提交更稳定。\n访问权限校验进一步完善，越权请求统一返回无权限提示。') AS content,
    '2026-08-11 12:00:00' AS published_at
  UNION ALL SELECT '1.11.0' AS version_no, '初中物联网课堂接入准备' AS title,
    CONCAT('部署县域 MQTT 消息服务，为掌控板等物联网设备接入课堂做准备。\n支持课堂中的数据接收、保存与查看流程开发。') AS content,
    '2026-08-11 16:30:00' AS published_at
  UNION ALL SELECT '1.12.0' AS version_no, '在线协作能力探索' AS title,
    CONCAT('探索 Word/Excel/PPT 在线协作方案，完成教室权限、版本保存等基础协议对接。\n后续经实测评估后切换为自托管协作方案。') AS content,
    '2026-08-14 20:30:00' AS published_at
  UNION ALL SELECT '1.13.0' AS version_no, '登录页与下载体验优化' AS title,
    CONCAT('登录页升级为三图轮播展示，支持键盘切换，观感更现代。\n全平台文件下载统一命名规则，下载文件更易辨认。') AS content,
    '2026-08-14 22:10:00' AS published_at
  UNION ALL SELECT '1.14.0' AS version_no, '教师帮助中心升级' AS title,
    CONCAT('帮助中心补充常见问题与课堂实操教程，按年级细化使用指引。\n优化了帮助内容对不同教师角色的可见范围。') AS content,
    '2026-08-16 15:40:00' AS published_at
  UNION ALL SELECT '1.15.0' AS version_no, 'Python 在线编程与在线协作上线' AS title,
    CONCAT('课程内 Python 操作题支持在线编程作答，保存草稿、运行示例、提交判题与历史记录。\n在线协作切换为自托管方案，同班学生可共同编辑同一份文档，支持版本历史。') AS content,
    '2026-08-17 20:00:00' AS published_at
  UNION ALL SELECT '1.16.0' AS version_no, '课堂考勤全面升级' AS title,
    CONCAT('教师可在一页查看各班的签到汇总与名单明细，并支持为学生添加备注。\n教师保存课程后首页课程数据即时刷新，无需手动重进。') AS content,
    '2026-08-18 13:00:00' AS published_at
  UNION ALL SELECT '1.17.0' AS version_no, 'Python 与文件操作题混合课程支持' AS title,
    CONCAT('一门课程可同时包含文件作品题与 Python 编程题，均归入操作题计分。\n修复学生端编程题显示与草稿保存的问题，作答体验更稳定。') AS content,
    '2026-08-18 18:00:00' AS published_at
  UNION ALL SELECT '1.18.0' AS version_no, '平台更新日志上线' AS title,
    CONCAT('新增“平台更新”栏目，集中展示每个版本的更新亮点与修复内容。\n教师和教研员可随时查看平台做了什么改进。') AS content,
    '2026-08-19 12:00:00' AS published_at
  UNION ALL SELECT '1.19.0' AS version_no, 'Python 刷题练习上线' AS title,
    CONCAT('新增独立“Python 刷题”与“Python 练习”，不占课程作业分，作为课后练习。\n内置 80 道系统编程题，教师可按年级与班级配置题单。\n学生可查看题目、运行示例、提交判题并查看历史。') AS content,
    '2026-08-19 17:00:00' AS published_at
  UNION ALL SELECT '1.20.0' AS version_no, '初中物联网实验上线' AS title,
    CONCAT('正式接入标准 MQTT 消息服务，掌控板可连接课堂物联网实验。\n教师可创建实验、自动分组、生成课堂口令与投屏配置。\n学生端新增物联网看板，按班级共享实验数据。') AS content,
    '2026-08-20 18:00:00' AS published_at
  UNION ALL SELECT '1.21.0' AS version_no, '在线协作体验优化' AS title,
    CONCAT('协作编辑者身份显示优化，学生显示“学号 姓名”。\n修复老版本浏览器进入协作或诊断报错的问题，提示更明确。\n修复学生进入协作编辑后被错误跳转的问题。') AS content,
    '2026-08-20 22:00:00' AS published_at
  UNION ALL SELECT '1.22.0' AS version_no, '课程物联网开关与协作稳定性提升' AS title,
    CONCAT('物联网入口改为课程级开关，只有教师在课程设计中开启，学生端才显示。\n修复协作文档“版本已变化”的频繁提示，多人编辑保存更稳定。\n新增在线成员列表与版本历史查看。\n教师物联页新增“学生数据收集”统计卡。') AS content,
    '2026-08-21 19:00:00' AS published_at
  UNION ALL SELECT '1.23.0' AS version_no, '全区上线前稳定性加固' AS title,
    CONCAT('打字题重复提交保留历史最高分，学生更公平。\n判题队列容量可调，整班集中提交编程题更不易失败。\n新增“扩展服务监控”页面，集中查看判题、协作、物联网与消息服务状态。\n完善页面防误关提示，作答与打字过程中提醒学生不要误关页面。') AS content,
    '2026-08-21 23:00:00' AS published_at
  UNION ALL SELECT '1.24.0' AS version_no, 'Python 统一题库与练习题单' AS title,
    CONCAT('Python 刷题升级为“练习题单”模型，可关联一个或多个负责班级，同班可有多份题单。\n新增三步出题、双表导入、参考代码测试点验证与统一学情。\n学生端升级为可拖动的三窗格在线编程界面，支持自定义输入与样例运行。\n内置 120 道系统编程题，覆盖 720 个测试点。') AS content,
    '2026-08-22 17:00:00' AS published_at
) AS s
WHERE NOT EXISTS (
  SELECT 1 FROM `biz_platform_update` existing
  WHERE existing.`version_no` = s.version_no
);

-- 后检：新增后总记录数与重复版本数
SELECT
  (SELECT COUNT(*) FROM `biz_platform_update` WHERE `status` = 'PUBLISHED') AS published_total,
  (SELECT COUNT(*) FROM (SELECT version_no FROM `biz_platform_update` GROUP BY version_no HAVING COUNT(*)>1) t) AS duplicate_versions;