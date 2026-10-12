# 登录双展厅本机验收

日期：2026-10-05；本文保留本机历史验收。后续正式1.30.28已上线，当前状态和回退见[正式验收](release-verification.md)。

## 2026-10-05 布局文案跟进

按用户追加要求，主登录框改为页面水平居中；桌面左上导语独占左栏，平板/手机纵排后登录框仍居中。删除“教研员登录查看完整成绩数据”入口及两处展厅说明文案，公开概览与原详情鉴权不变。本轮仅登录页布局/文案及验收记录调整，无后端、SQL或认证配置变更。

1920/1366/1024/820/390五种宽度定向浏览器验证通过：登录框水平中心偏差均0px，与导语无重叠、页面无横向溢出、三处文案均移除、登录框仍2输入/1按钮，页面异常0。截图和记录见`output/playwright/login-centered-20261005/`，构建日志`output/login-centered-build-20261005.log`；初轮26项记录见下文，其中监管提示入口已按本次要求删除，不再作为当前页面验收项。仅前端待发布，无需后端重启。

## 修改范围

- `RuoYi-Vue3/src/views/login.vue`：全景轮播、精简登录框、磨砂双展厅、验证码弹窗、概览加载/空状态/重试、桌面和窄屏布局。
- `RuoYi-Vue3/src/api/business/loginPreview.js`：独立、不携带令牌的公开概览请求。
- 后端`LoginPreviewController.java`、`LoginPreviewService.java`、`LoginPreviewMapper.java`、`mapper/business/LoginPreviewMapper.xml`：固定数量摘要、白名单、缓存及只读查询。
- `LoginPreviewServiceTest.java`：白名单、防正文/成绩/身份泄漏、数量上限、缓存、失败后重试、年级降级、空结果和既有详情鉴权。
- `PROJECT_CORE.md`、架构索引/系统图/业务边界及本专题需求、设计、任务、ADR与验收文档。保留工作区所有既有修改。

## 结果与证据

- Java6项：全部通过，失败/错误/跳过均0；命令`mvn -pl ruoyi-business -am test -Dtest=LoginPreviewServiceTest -Dsurefire.failIfNoSpecifiedTests=false -DforkCount=0`。日志`output/login-preview-tests-20261005.log`。
- 后端`mvn -pl ruoyi-admin -am clean package -DskipTests`成功（定向测试已先单独完成），日志`output/login-preview-package-20261005.log`。Vue3最终`npm run build:prod`成功，日志`output/login-preview-build-20261005.log`；未引入依赖。
- 本机库`xueyeceping_server_20260729`用只读事务执行真实Mapper SQL，活动1条、开课6条；109/188ms是该次本机查询结果。未写活动、课程、学生或成绩。证据`output/login-preview-sql-20261005.json`，复核脚本`output/check-login-preview-20261005.py`。
- 实际8080 API无Authorization：两个概览HTTP200/业务码200，返回字段均为摘要；原教研列表、主题详情、监管timeline、学生监管明细HTTP200/业务码401。不能把RuoYi业务码401误写为HTTP401。证据`output/login-preview-api-20261005.json`。
- Chromium26项通过，pageerror=0：主表单2输入/1按钮且无注册链接；三图/六标签/自动和手动轮播；公开请求不带令牌；摘要数量；桌面40:58并列、手机无全页横向溢出；活动/监管详情入口先登录弹窗；空数据、单厅失败、独立重试；完成/待批/无作业配色；开启验证码时先弹窗且不会立即发送登录请求，提交后保留原服务器登录验证。
- 真实教师`19157727791`进入`/teacher-dashboard/index`，教研员`laoda`进入`/platform/overview`，学生`2020710101`进入`/student/index`。本机验收按原认证完成，只访问核心页，未提交作品、改成绩或修改密码；成功验收会话通过logout清理。常用旧学生测试号`st99_363`在当前迁移库不存在，因此使用已记录且有效的0题考勤学生号；无签到操作。
- 浏览器证据`output/playwright/login-preview-20261005/results.json`，真实数据截图`desktop-real.png`（1920×1080）、`laptop-real.png`（1366×768）、`mobile-real.png`（390px全页）。脚本`output/verify-login-preview-20261005.cjs`；验证码开关及错误/空结果等边界使用受控模拟接口，不能当作正式环境开关状态证据。本机当前验证码开关关闭，原开关未修改。

## 发布、限制与下一步

无需SQL迁移、核心认证配置修改或新依赖；本机已启动新后端8080及Vite预览5173。没有访问/部署正式服务器或推送Git，正式仍1.30.26，不生成正式更新版本。前后端需配套发布并重启后端；回退仅恢复旧应用和前端，无需数据库回滚。

现有活动没有独立主讲字段，展示真实“发布学校/教师”，需要主讲人元数据时另行确认录入方案。当前本机仅1条活动，未虚构补足3条；正式数据及更大数据量性能需上线前复核。参与人数取窗口内真实活动学生，分母为当前有效班级人数，历史归属变动可能产生不一致，不把两者强制截断为相同。60秒缓存意味着概览有短时延迟。
