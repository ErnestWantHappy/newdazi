# 130正式发布验收：机位与成绩合并1.30.52

2026-10-17，用户重新明确授权“更新部署服务器，另一个AI对话的修改也要部署上线”。已核对对话“排查成绩分析与待批改显示”，将其成绩/打字修复与机位助手0.2.4一起发布。此前10月16日停止指示和旧暂存仅为历史，未把旧暂存切为正式。

## 已上线范围

- 产品1.30.52，前后端release `/data/apps/xueyeceping/releases/20261017_seat_score_v1`，下载助手0.2.4。
- 助手普通/开机/重复运行静默，正式域名`http://xxkj.xsedu.net.cn`及`http://10.52.1.130`精确允许。Windows默认HTTP浏览器优先；其他浏览器等待，停止认领约75秒加轮询后接替。同机换学生自动认领，不改作品/成绩权限。
- 成绩图表保留全体学生和独立状态，操作待批先累计已有评分，页面/导出同步。学生自主重打保留本轮最高及对应指标，教师打字重做才开启清分新轮。
- 无表结构/数据迁移，不批量重算或修改学生成绩。唯一SQL为`sql/seat_score_release_20261017.sql`发布登记，复跑保持单条。原124设备/42常用机位基准未改。

## 隔离与验证

以现场拉取、SHA核对的正式1.30.51 JAR为基线，只替换12个业务条目：SeatHelperController及3内部DTO、SeatHelperService、ScoreQueryController及2内部类、StudentHomeController及2内部DTO、BizStudentAnswerMapper.xml。外层只有业务JAR变化，其余依赖、资源、业务条目逐字节保持；页图v4、期限/PPTX修复保留。

前端以正式1.30.51源码快照（原dist首页哈希与服务器一致）仅覆盖7文件：studentSession.js、seatHelper.js、成绩index.vue、RankChart.vue、rankData.js、CourseComparisonChart.vue、学生index.vue。不混入未发布的PracticalTextPreview样式/页图裁剪尝试。

本轮重新通过Java75、Node29、客户端52、完整后端clean package、隔离前端生产构建和登录资源门禁。真实Vue组件/模拟API：成绩5组、机位11组；并行测试曾共享Vite缓存出现504，隔离各自cacheDir后机位11组重跑通过。独立原生Chrome/Edge两组：真实TCP所属进程、默认Edge优先及各自正式域名/IP四种入口可达；未读Cookie、未修改默认浏览器。后台device/open为临时接口，不是正式学生认领。

## 备份与切换

- 备份 `/data/backups/xueyeceping/20261017_seat_score_v1`，完整库427,255,471B，SHA-256 `35dcdd97edc78918845b7b302119622c8c541c36493ffa1ed5ba575e830ca934`；含原service/nginx/env/config及环境文件快照。未执行结构迁移。
- 切换前批改任务/报告任务/近5分钟提交均0，校验当前unit/site/env及制品SHA、Nginx候选语法通过。环境变量只更新`SEAT_HELPER_CLIENT_PATH`；旧外置config/隔离env仍引用20261008目录，不可删除。
- `09:23:22`开始，`09:23:45`完成，23秒恢复。systemd后台发布job完成，三服务xueyeceping-130/nginx/aiot-lamp均active；PID2452324、NRestarts=0。Nginx平滑reload并等待首页SHA生效，无回滚。
- 保留旧静态资源，未删除旧release、备份、上传或学生作品。10月16日旧暂存也保留，不作为当前正式版本。

| 制品 | SHA-256 |
| --- | --- |
| ruoyi-admin.jar | `8fe0c7571ed24bba4b7ea39d6484962bedaab845f392eda260b7bc74378441d8` |
| frontend/index.html | `27b1dbcfb1685c0fb4cee82b4120fbf44bdfacd613a5dbfed8429588064c9575` |
| frontend.zip | `1b6cb4137f84a2659baf7b05e02913e158353170425c9d363595adaa2a4aa178` |
| SeatHelper.exe 0.2.4 | `f0fe6f6083d95196c623f4e99ceab5af84e8d3be753961dc8208d976933e8d42` |

## 正式接口、页面与发布登记

真实教师登录、首页、课堂机位API、成绩API通过；所授权班级43学生与机位名单一致，校验156条有效课程评分均符合已有分+表现分及0～100限制，其中37条操作待批仍有数值总分。匿名和教师调用学生认领接口均拒绝。域名与IP的首页SHA都匹配候选，服务器JAR/助手下载路径主体SHA匹配。

真实教师浏览器完整课堂大屏及成绩页（全部课程43人/37条待批数值总分）、教研员登录/默认首页通过，pageerror0/HTTP5xx0。初次临时脚本用了不存在的图表选择器，修正验收定位后通过；未因此修改产品代码。已查看正式成绩页面截图。没有点击改分、清分、机位保存或写答案，测试成绩写入0。

更新130/1.30.52已设PUBLISHED，SQL复跑仍唯一，真实教师`/business/platform-update/list`确认可见，标题“机位静默关联与成绩统计优化”。未推送Git。

证据位于`output/seat-score-release-20261017/`：baseline/backup/manifest/prepared/status/api-verification/browser-verification/published.json、teacher-score.png、teacher-seat-header.png。复测日志`output/seat-score-20261017-{tests,node,client,package,seat-ui,score-ui,native}.log`。

## 回退与剩余边界

回退前核对当前运行release与课堂空闲情况，恢复本次备份`service.before`、`nginx.before`、`env.before`到对应systemd/Nginx/shared env位置，systemctl daemon-reload后重启后台，nginx -t后reload并探活，应用回到1.30.51；撤更新130为DRAFT。不回灌整库、不撤销学生新成绩、设备凭据或常用机位。若已安装0.2.4，旧1.30.51网页只识别0.2.3，需配套处理本机助手或保留网页版本识别兼容，不能承诺旧网页自动识别0.2.4。正常网页登录/作答仍不依赖助手。

正式学生凭据上轮已确认失效，本轮未重复猜试：学生真实鉴权下载、换账号/多浏览器认领、实际重打提交仍待有效正式账号。原生浏览器自动化不代替机房开机、浏览器权限、后台节流、还原卡、250机同传与整班性能；同浏览器多个档案/隐身窗口不区分。旧助手必须退出托盘后下载安装0.2.4一次，已安装EXE不会自动升级。其他历史期限、PPTX预览及固定A4裁剪问题不在本轮修复范围。
