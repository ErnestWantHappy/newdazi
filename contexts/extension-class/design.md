# 实现设计

## 数据及权限

新增 `biz_extension_class`、`biz_extension_class_member`、`biz_extension_lesson_member`、`biz_extension_plan_member`。课时冻结名单，题单记录发布及后续成员加入时的投放名单，后者补充解决“移出成员导致旧题单学情分母缩水”的要求；不是第二套行政班。

`biz_lesson` 增 `lesson_scope/extension_class_id/lesson_date`，题单增 `plan_scope/extension_class_id`；学生增 `space_pref/space_selected`，默认regular不代表已亲自选过。迁移统一 `sql/extension_class_v1.sql`，不包含任何生产数据迁移或账号冻结。

建班锁学校行串行分配载体，名称及载体唯一索引兜底；成员唯一索引保证单一活跃归属。成员修改、建签到课及签到共享班级锁，签到读当前成员及指派使用当前读；成员写入还锁学生行。移出/归档释放活跃关系，快照不动。

教师服务双重检查角色、当前学校及负责人；学生身份复用studentSs的档案兜底，按登录账号找本人学生档案。拓展题单只允许一个拓展班，创建后不跨空间/换拓展班，避免既有发布版本跟随题单主行变更而越权。

## 接口和页面

- `/business/extension/teacher`：班级、教师候选、成员、学生候选、签到课、dashboard、历史及逐课roster；所有子对象校验归属。
- `/business/extension/student`：context、space、home；签到沿用student-home/checkin。
- Python overview、question、draft、submit携带space，服务端独立验证，不把客户端传入当授权。
- Vue3复用StudentSpaceSwitch与studentSpace store。登录仅在学生跳转时请求context，首次弹窗等待选择；后续按账号偏好跳转。
- ExtensionClassPanel嵌入教师首页和班级管理拓展页签；学生独立extension页，纯Python通过query进入原练习页。
- 常规查询增加scope过滤；不向biz_teacher_class写新拓展班。普通课程设计入口拒绝专用和历史课程。

## 验证环境与回退

仅本机开发。原8080后端JAR占用时使用隔离源码目录打包并另启18080，避免中断原服务。旧本机库缺DATA_LAB快照表，真实Python集成使用隔离验证库，不伪造旧实验快照、不将隔离建表视作旧数据迁移完成。

回退应用保留新增表列及快照；不要整库回灌覆盖开发后产生的数据。本机备份与实际验证记录维护在tasks.md；130无变更。
