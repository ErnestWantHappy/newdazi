# 本机验收记录

> 正式状态已更新：2026-10-14 130发布1.30.50/助手0.2.3，迁移两列、真实教师/教研员及发布更新登记通过；学生凭据失效、下载/实际认领与机房仍待验。下面保留本机阶段证据，当前正式状态以[正式验收](release-verification.md)为准。

2026-10-14。仅本机代码候选，正式仍1.30.49/助手0.2.2，未发布本轮变更。保留工作区既有登录、批改和页图改动；本机制品不是隔离正式发布包，不得直接混入正式release。

## 修改范围

- 客户端：`clients/seat-helper/SeatHelper.cs`、`build.ps1`、`tests/SeatHelperRegression.cs`、`README.md`。
- 学生：`StudentSeatHelper.vue`、`studentSession.js`、`utils/seatHelper.js`；`utils/request.js`增加可选silentError，原业务默认行为及401保持。
- 教师：`ClassroomSeatPanel.vue`、`views/business/classroomDesktop/index.vue`。
- 后端：`SeatHelperController.java`、`SeatHelperService.java`、`SeatHelperMapper.java`，新增`SeatHelperLocalClaimTest.java`。
- 验收：`seatHelper.test.js`、`seatHelperRequest.test.js`、`scripts/verify_seat_helper_local.mjs`；迁移`sql/seat_helper_local_claim_v2.sql`。
- 文档：本专题需求/设计/任务/ADR、原课堂专题、PROJECT_CORE、架构索引/系统/数据/集成/业务边界与客户端说明。

## 实测

| 层级 | 结果 | 证据 |
| --- | --- | --- |
| Java定向 | 29/29，0失败/跳过；含13项新认领/基准/指纹升级 | `output/seat-helper-java-final-20261014.log` |
| 后端打包 | `mvn -pl ruoyi-admin -am clean package -DskipTests`成功；测试已独立执行 | `output/seat-helper-backend-package-20261014.log` |
| 客户端 | .NET4编译成功，41/41；实际HttpListener/TcpListener请求、Origin拒绝、单次token及预检 | `output/seat-helper-client-final-20261014.log` |
| Node | 14/14；IP不回退、800ms超时、凭证消费、静默请求与原会话过期处理 | `output/seat-helper-node-20261014.log` |
| Vue3生产构建 | 成功，含原登录19项和入口/图片检查 | `output/seat-helper-frontend-build-final-20261014.log` |
| Chromium组件 | 10组通过、页面异常0、真实业务写入0；挂载真实学生会话/状态及教师机位面板，模拟本机探测/业务API | `output/playwright/seat-local-20261014/result.json`、`uninstalled.png`、`associated-alert.png` |

助手候选0.2.3 EXE SHA-256 `9980B2B0BACBD9F12C01356E4C271A74903F600FBAA268B5FEC23FEAD4D6407B`。客户端测试真实操作仅限测试目录、测试注册表项和临时监听，不启动用户浏览器、不配对真实设备、不改真实Windows自启。

最终补充同传克隆并发的锁内旧凭据重验，13项新服务测试及全部29项机位测试通过；最终后台package再次成功，见`output/seat-helper-backend-package-final-20261014.log`。前端最终生产构建和浏览器10组在最新学生状态代码下通过，未使用旧构建冒充本轮结果。

## 验收边界

浏览器十组覆盖未安装下载、运行后变绿、换学生、一键保存、异机IP/单人更新、机房恢复、迟到响应隔离、静默失败、学校关闭和助手消失；卡片标签辅助展示用于夹具，完整大屏已编译，未进行真实教师/学生/教研员登录或完整大屏现场验收。Java使用Mock Redis/Mapper/身份上下文，TTL过期以删除观测模拟，不等于真实Redis/数据库联调。未监听原80/8080服务时不假称真实API通过。

SQL是新增两个可空字段，未在任何目标库执行，幂等性及映射还需真实目标库验证。部署先备份迁移，再配套发布/重启后台和安装新助手。正式产品更新待真正部署成功再登记，不生成虚假已发布版本。

新浏览器可能要求LNA权限或安全上下文，HTTP内网不能由CORS保证可访问回环；须用机房浏览器验证或由管理员在母机按学校策略配置。不允许声称所有浏览器绝不弹权限框。MAC是辅助机器区分，无硬件签名；IP/DHCP变化告警须教师核实。未运行助手的服务端观测最多在既有75/90秒TTL及5秒校准内过期，不能把轮询称为实时推送。不同子网末段相同可能有机位名冲突，缺席保留机位可能使单人确认0人，均不自动覆盖。

母机不要携带已配对凭据同传；真实250台唯一性、极域/还原保护、自启、浏览器许可和整班负载尚未验。首次安装及旧配置指纹升级是不同路径，真实旧版本迁移和回退客户端还需专项验证。回退边界见[design.md](design.md)。AOCI未同步，索引需后续复核。
