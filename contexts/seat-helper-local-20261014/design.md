# 设计

2026-10-14。本轮复用Java8/RuoYi/MyBatis/Redis、Vue3和.NET Framework4，无新依赖。

## 本机协议

助手0.2.3只监听127.0.0.1:28123，提供GET /identity和OPTIONS预检。优先HttpListener，Windows URLACL拒绝访问时用.NET自带TcpListener保持相同地址，避免学生申请管理员权限。TCP响应校验Host、精确Origin，限制请求头8192字节和收发超时；HttpListener只注册回环前缀。两种模式只允许下载包公开platformUrl的精确Origin，不使用星号CORS；无来源及其他站点拒绝。响应no-store并支持Access-Control-Allow-Private-Network。

后台预取device/open的60秒单次凭证，本地缓存最多40秒，GET读取后销毁；请求中不向本机发送学生登录Token。未注册时使用原5分钟pair流程，密钥仍由独立proof轮询领取并DPAPI保存。identity返回installed/version/localIp/deviceId/token，不返回长期secret或proof；凭证未就绪时只返回installed/version。来源和版本检测不能构成硬件真实性证明，也不保证抵御主动伪造客户端或转发单次凭证。

## 身份和短时状态

学生布局按id/token代次重新探测，800ms超时；先检查学校开关，再用POST /student/claim-local消费助手凭证。后端复用associate验证，忽略网页自报IP/deviceId；school来自当前登录学生。请求固定使用该代次捕获的Authorization，迟到结果不写新身份。短期观测由Redis维护，device owner约束旧学生不能继续显示为该电脑当前使用者；旧版本无owner时保留迁移兼容。设备75秒、学生90秒TTL保持；已关联网页10秒轮询，未关联2秒重试，关闭学校30秒复核。认领失败在状态栏提示，silentError不影响真正401的统一登录过期处理。

新指纹加入物理网卡MAC以区分同传镜像，IP不进入持久指纹，避免DHCP变化使凭据失效；排除常见虚拟/VPN适配器，优先有网关的活动物理网卡。旧DPAPI配置只能由本机解密后持旧密钥调用device/upgrade-identity；锁学校、查目标指纹冲突，更新指纹并保持原deviceId/命名/基准。撤销记录也参加指纹查询，不能通过同指纹重配绕过撤销。未经配对的母机可同传程序，已配对凭据不得作为分发模板。

## 大屏与基准

每5秒从受授权课堂读取观测，切班/机房时清空旧行并隔离迟到结果，不允许重叠定时读请求。机房支持自由输入和历史选择，默认取该班级最近confirmed_at的机房。确认不再要求设备预先属于目标机房；学校锁、当前指派锁、基准锁和最新观测复核保留。前端携带studentId/deviceId/localIp，后端重新核对，IP已变则拒绝。

未人工命名的电脑按IPv4末段生成编号（.101对应101机），同机房原人工命名保留；记录学生ID、deviceId、local_ip和seat_name快照。学生学号/姓名沿用原学生表，避免复制第二套身份。缺席/未知/重复IP/保留占用冲突不覆盖，整班互换可同时保存。不同子网末段相同且机位名冲突时返回明确错误，管理员可调整名称；不声称自动识别物理桌面贴纸编号。

有IP快照且变化为IP_MISMATCH；旧基准未填IP时仍按deviceId比较CHANGED，待教师明确重存补齐IP。两者都显示“异机登录”。设备可变当前名称不会修改确认快照。当前值由助手心跳提供，网关连接来源不显示为机位IP。单人更新与整班保存走同一个确认接口。

## 发布及回退

先备份目标库并执行幂等 [SQL](../../sql/seat_helper_local_claim_v2.sql)，前置v1三表必须存在；新增两列可空，不猜填历史IP。再发布新后端、Vue3和助手下载制品，并重启后台；原学校开关保留，无新配置项。2026-10-14已在130完成备份、迁移复跑/后检和1.30.50配套发布，client-path切换新助手；111设备/42基准保持，真实教师接口通过，学生真实认领仍待有效正式账号。见[正式验收](release-verification.md)。本机库未迁移。

回退网页/后端时保留新列、基准、设备ID和已迁移指纹，已升级机器保留0.2.3助手，可用原快捷方式单次URL关联。若必须回退客户端，须独立设计指纹和DPAPI凭据反迁移，不能只替换旧EXE或整库回灌。已安装0.2.2须退出托盘再安装一次0.2.3，之后换学生无需再次下载；当前不自动更新客户端。
