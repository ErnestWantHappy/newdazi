# ADR-006：用Windows连接所属进程确认默认浏览器优先级

2026-10-17已按用户重新授权合并发布130正式1.30.52，双端`20261017_seat_score_v1`、助手0.2.4；真实教师/教研员验证及更新130/PUBLISHED通过，学生与机房仍待验。见[正式验收与回退](../seat-score-release-20261017.md)。

2026-10-16，已获用户确认。

选择本机进程识别、服务端短期票据及学校锁仲裁，不新增浏览器扩展。网页UA不能可靠区分Chromium浏览器，也不能让网页自己决定优先级。单纯“最后登录者覆盖”会在多浏览器轮询时反复抢占；“最先登录者固定”不符合默认浏览器优先需求。

使用Windows公开API：[TCP所属PID表](https://learn.microsoft.com/en-us/windows/win32/api/tcpmib/ns-tcpmib-mib_tcptable_owner_pid)、[协议关联查询](https://learn.microsoft.com/windows/win32/api/shlwapi/nf-shlwapi-assocquerystringa)。API结果与真实Chrome/Edge回环连接联合核对，不读取浏览器存储，不用前端声明当可信证据。无法识别不伪称默认优先。

以75秒短期占用兼容网页10秒轮询和短暂网络故障；不增加注销时跨域发学生凭据的通道。代价是停止检测后接替需要等待，后台节流需现场验证。HTTP默认处理程序作为统一默认浏览器；同程序多档案/多个账号不在本次区分范围。

跨域访问采用安装包明确的公开别名清单，拒绝任意Origin或仅因DNS解析到相同IP就放行。
