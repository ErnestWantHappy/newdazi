# 登录双展厅正式验收与回退

日期：2026-10-05；130正式版本 **1.30.28**，更新107为PUBLISHED，真实教师更新列表可见。

## 发布范围与构建

以正式1.30.27 `20261005_seat_helper_auto_v2` 的隔离源码为基线，只叠加登录页、公开请求模块、三个后端源码、Mapper XML和测试，共7个文件。构建前后逐文件比对通过；后端制品只新增三个LoginPreview class和对应Mapper XML，其他模块解压内容保持。前端新增登录组件使共享导出及压缩别名重新生成，以源码逐文件差异限定范围，未混入其他工作区修改。原静态资源及机位助手0.2.0保留。

独立构建命令：`mvn -pl ruoyi-admin -am clean package -Dtest=LoginPreviewServiceTest -Dsurefire.failIfNoSpecifiedTests=false -DforkCount=0`；6项通过，失败/错误/跳过0。Vue3 `npm run build:prod`通过。清单、日志及脚本见 `output/login-release-20261005/`。

## 服务器与验收

- 新双端release `/data/apps/xueyeceping/releases/20261005_login_showrooms_v1`，独立systemd部署任务 `release-20261005_login_showrooms_v1` 状态COMPLETED；后台、Nginx、aiot-lamp均active。已重启后台，Nginx平滑reload。
- JAR SHA-256 `ed0d35a824e3bc1cf2eecbdd98393499ca7748e34605baceda9b9d473ad2176e`；首页SHA-256 `8821e4c032f2cdfee946fbdd247d1ce98bf4823a7da75dac76adec65a0b74c5f`，远端制品及实际HTTP首页匹配。
- 正式配置、AI主密钥、migration-isolation.env保持；共享env哈希完整不变，机位助手仍使用原已验证release中的0.2.0下载路径；未调整学校配置。保留原mjs MIME和旧static资源，未修改资源访问边界。
- 公开概览真实3条活动、6条课程班级，HTTP200/业务200，响应字段严格匹配白名单。原教研列表/主题详情、监管timeline/学生明细无令牌均HTTP200/业务401；概览请求不带Authorization。未修改作品、成绩、课程或账号。
- 正式开课查询首次约5.2秒，60秒缓存命中约14毫秒；只读SQL现场复核约5.4秒。测量只代表该次请求，不等于并发容量验收；登录与两个展厅独立加载，首次列表可能等待数秒。
- 1920/1366/1024/820/390五种宽度：登录框中心偏差0px，与导语无重叠、全页无横向溢出、指定三处文案消失，2输入/1登录按钮、3背景/6标签保留。活动先登录弹窗、手动/5秒自动轮播通过，页面异常/HTTP500均0。
- 正式教师首页及教师登录表单、学生登录表单与核心页通过，只读访问并logout；教研员本地测试凭据在正式返回用户名/密码错误，原账号状态有效，未重置密码或改变权限。正式教研员登录冒烟未完成，本机同角色冒烟已通过，不把两者混同。
- 正式验证码开关关闭；开启时弹窗流程已在本机受控场景验证，未改变正式开关。
- 截图 `output/playwright/login-release-20261005/`；API、布局、登录与权限证据 `output/login-release-20261005/verification.json`，状态、清单、SQL复核、更新登记及教师可见性分别为同目录JSON。

## 备份、SQL与回退

完整备份 `/data/backups/xueyeceping/20261005_login_showrooms_v1/database.sql`，313994316 bytes，SHA-256 `1bec3cab2e18adb41ef73ff711c9475b51ba7f4229c09d7b0e7b0c6e3489429e`，dump完成标记已确认。同目录保存service.before/nginx.before/env.before/config.before。

无表结构迁移、新依赖或业务数据修正；仅执行 `sql/login_showrooms_release_20261005.sql` 登记产品更新。1.30.28/update_id107为PUBLISHED，真实教师 `/business/platform-update/list` 返回可见。

回退前核对当前仍指向本release且无活动AI任务。将本次备份service.before/nginx.before/env.before分别恢复到原systemd unit、Nginx站点和shared env路径，daemon-reload、restart xueyeceping-130、nginx -t、reload nginx；验证首页回到1.30.27哈希 `03a471b810e194ea067eb13fe6d2cadf238147bbd291477c4bda7f24181c36fa`、后端/prod-api探活和三服务active，再仅把更新107/1.30.28撤为DRAFT。无业务回滚SQL，不整库回灌覆盖发布期间新作品或成绩，不删除旧release或备份。

## 剩余事项

活动无独立主讲字段，继续如实展示发布方。冷查询约5秒、大规模匿名并发及正式教研员有效凭据验证尚待跟进。AOCI曾被并发恢复阻止，本轮恢复读取时Host输出截断，未完成完整交付校验；不声称完整索引认知或治理对齐。发布验收以当前源码和服务器证据为准。
