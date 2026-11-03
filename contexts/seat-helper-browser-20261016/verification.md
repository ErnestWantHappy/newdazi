# 本机验收

2026-10-17已按用户重新授权合并发布130正式1.30.52，双端`20261017_seat_score_v1`、助手0.2.4；真实教师/教研员验证及更新130/PUBLISHED通过，学生与机房仍待验。见[正式验收与回退](../seat-score-release-20261017.md)。下文为10月16日本机验证记录；旧备份/暂存未启用，由10月17日新release替代。

| 验证 | 结果与范围 |
| --- | --- |
| Java | SeatHelperLocalClaimTest17、PairService13、Policy3、Download1，共34通过；包含默认接管/反抢拒绝、超时接替、同浏览器换学生、旧URL不得抢占、设备认证票据、下载主体/域名/IP尾部及非法配置不返回半包。Redis/Mapper为模拟，非正式学生联调。 |
| 客户端 | 52项通过，真实HttpListener/TcpListener、TCP端口字节序/PID/程序路径、别名精确匹配、各浏览器票据隔离与单次消费、默认变化废弃旧票。 |
| 原生浏览器 | 独立测试助手与本机真实Chrome/Edge两组通过，TCP识别路径与浏览器实际EXE一致；系统默认Edge优先、Chrome非优先。各自打开正式IP/域名登录页后请求本地助手，四种组合均可达。使用独立headless进程，不代表机房权限/所有浏览器已验。未改默认浏览器、不安装、不写注册表、不读Cookie；device/open为临时接口。 |
| Node | 15通过，包括新旧助手识别、NAT不回退、800ms探测、401及迟到身份隔离。 |
| 组件 | 真实Vue学生组件/教师机位面板、Chromium与模拟API11组通过，pageerror0；优先等待隐藏下载/旧IP，无业务弹窗。 |
| 构建 | 后端clean package、前端生产构建、客户端0.2.4构建通过；下载保护/预取续期修改又通过34项Java。隔离前端仅覆盖正式1.30.51的2个机位文件，构建发布门禁通过。 |

本地日志`output/seat-browser-{client,maven,package,frontend,ui}.log`；原生报告`output/seat-browser-native/result.json`；组件报告及截图`output/playwright/seat-local-20261016/`。原生测试编译SeatHelperBrowserProbe.cs为独立exe，再运行`scripts/verify_seat_helper_browsers.mjs`，无需安装实际助手。

真实业务写入0，无SQL或运行配置变更，不启动本机业务服务。真实正式学生凭据仍待提供，机房重启、后台节流、还原卡/同传/整班性能仍待验。新客户端安装须先退出旧托盘，旧安装包不自动更新。
