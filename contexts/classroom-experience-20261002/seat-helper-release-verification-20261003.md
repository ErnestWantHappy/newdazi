# 机位助手正式HTTP启用验收（2026-10-03）

用户明确要求没有HTTPS也能正式使用并开启助手，且不影响正常教学。正式版本1.30.26，更新105已PUBLISHED，真实教师list可见。前后端release `/data/apps/xueyeceping/releases/20261003_seat_helper_http_v1`，客户端0.1.2。当前61所有效学校已开启，独立验收学校已关闭/隐藏；真实机房还需学校管理员逐机安装和注册。

## 修改与发布

`SeatHelperService.java`、`SeatHelperPolicyTest.java`、`clients/seat-helper/SeatHelper.cs`及测试/build、Vue3`views/business/seatHelper/index.vue`和README同步支持HTTP/HTTPS。原本必须HTTPS是此功能的地址校验限制，并非浏览器教学必需条件。根地址/端口/协议仍校验，禁止账号、路径、参数及片段的学校配置；原哈希、DPAPI、单次凭证及显式基准保持。

完整备份目录 `/data/backups/xueyeceping/20261003_seat_helper_http_v1`，313975585 bytes，SHA-256 `9ed2bbdeac148985b54e85835df3f812f73f5273a5c5f7d4d8071c0d7e772f84`，完整mysqldump结束标记已核验；service.before/nginx.before/env.before/config.before保留。无表结构或真实学生/成绩/作品迁移，仅执行仓库启用SQL和发布登记SQL。共享env仅增加`SEAT_HELPER_ENABLED=true`和`SEAT_HELPER_CLIENT_PATH=/data/apps/xueyeceping/releases/20261003_seat_helper_http_v1/clients/SeatHelper.exe`，其他行逐项一致。systemd独立任务COMPLETED，后台/Nginx/aiot-lamp均active，代理探活成功。

JAR SHA-256 `92d40a8cd586688f5388b7f5fb62e2a5dddd0417498e3572b2fff7ca1a7c0eef`，首页 `96c62c298e0cddc18a080afc526bec1b6499b76908850ff3e690e24d4ab409dc`，EXE `3d2daa5b4c7f251c6797ad5ca0e3cba25b79681e6bb4e8bb08f9b61f8eeb7f5e`。嵌套依赖内容核验后台仅SeatHelperService.class变化；排除编译文件名与作用域标识后前端仅助手管理提示模块变化，现有教师课程629/12班42/42与成绩查询一致。旧静态资源保留，未改其他服务配置或HTTPS网络。

## 验证

- Java定向3项零失败/跳过，clean package通过；C#15项及EXE构建；Node1项；Vue3生产构建。没有将此前676项全量作为本轮重跑。
- 正式API7组：临时管理员/教师/两学生真实登录；HTTP设置和非网页拒绝；学生鉴权下载PE且哈希一致；未安装/未关联学生获取当前课、上传并真实提交；下载EXE使用正式HTTP注册和DPAPI保存、注册码重放拒绝；EXE打开凭证/心跳IP/学生关联/单次重放拒绝；教师看到UNBOUND并显式确认NORMAL，另一学生保持UNIDENTIFIED。
- 正式浏览器6组：管理学校配置和HTTP回显、注册码入口；真实客户端URL在新标签页关联并清片段；教师显示机位和基准；同标签页点击Python练习超过98秒，网页续期4次且仍NORMAL，设备心跳由实际EXE定时调用维持；撤销/未安装仍可读当前课程；关闭独立学校助手后入口隐藏但课程正常。pageerror/HTTP500均0。
- 实际下载EXE的地址选择方法在设备撤销后返回`http://10.52.1.130`。客户端15项另覆盖接口故障、损坏配置等异常。没有将“返回入口”冒充真实机房系统默认浏览器/快捷方式/自启现场验收。
- 独立验收学校990300、专用账号和新临时学校列表角色权限已停用/撤除，教师关联与指派清0，设备撤销，专用作品和1条基准保留审计；真实61校仍启用。所有业务写入限定本次独立验收范围，未改真实班级分数、作品或指派。
- `output/seat-helper-release-20261003/`保存baseline、audit、manifest、deploy/status、api/browser/fallback/cleanup、existing-teacher、teacher-version等非敏感报告；凭据、配置及单次URL仅在忽略的private文件。截图在`output/playwright/seat-helper-release-20261003/`。

## 使用与限制

普通网页为`http://10.52.1.130`，接口为`http://10.52.1.130/prod-api`。管理员进入机位助手按机房/机位生成一次性注册码；学生可从首页下载客户端，管理员在每台电脑配置，教师确认常用机位。没有客户端仍正常上课、答题；关联失败不会强制退出或锁账号。现有学校已开，新学校需后台设置。

HTTP没有传输加密，仅在学校可信网络使用；HTTPS仍兼容，后续可按实际证书入口升级。客户端未签名，Windows10学生机、极域分发、还原卡、安装快捷方式/自启/DPAPI基线未现场验证。开发Windows电脑和正式链路已验，不能称全校电脑已安装。历史118条空作品状态未治理。

## 回退

仅在当前仍为本次release且确认期间配置未漂移时，恢复本次备份的service.before、nginx.before、env.before到原unit/Nginx/shared env路径；daemon-reload后重启本站后台、nginx语法检查并reload、核对旧前端index和API探活。前端回1.30.25，后台回1.30.24，助手全局关闭。将更新105/1.30.26撤为DRAFT。新增设备、长期基准和提交作品保留，不恢复整库覆盖期间写入；无必需回滚SQL。
