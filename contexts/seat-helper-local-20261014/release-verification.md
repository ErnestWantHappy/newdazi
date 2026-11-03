# 130正式1.30.50发布验收

2026-10-14（北京时间）。双端release `20261014_seat_local_v1`、助手0.2.3。切换 **17:27:01**，**17:27:24** 探活完成，约23秒；独立systemd任务状态COMPLETED，后端PID2346831/NRestarts=0，后端、Nginx、aiot-lamp均active。

## 制品隔离

后端以现场1.30.49 JAR为基线，仅替换SeatHelperController及三个DTO、SeatHelperMapper、SeatHelperService共6个class。外层只有ruoyi-business嵌套JAR不同；其它类、Mapper、资源和依赖逐条字节保持，正式PracticalPageRenderer及page-jpeg-v4保持，未混入工作区后续页图改动。

前端从当前Vue3源码制作新隔离快照并执行build:prod，保留已上线登录WebP/延后请求/VForm按需和批改布局，唯一排除当前尚未上线的PracticalTextPreview样式差异（该文件取HEAD已发布基线）。未使用历史output/build发布。原工作区修改保持；旧哈希静态资源复制到新release，避免已打开页面请求旧资源404。

- JAR SHA-256：`d501cc74d881c1f8396d9d303e13792d12fc83b0e0d57bb1ee5a53c504b16d00`。
- 前端ZIP：43,346,190 bytes，SHA-256 `466cc5f792973af207599b2870efc5630813d5c9c9f961ea0b3b1eaf01e6b3db`。
- 首页SHA-256：`9bfd15a41f91a2419e4770e3878ce1c6010abacc8817b11d4bf3b0509e8b438f`。
- 助手主体SHA-256：`9980b2b0bacbd9f12c01356e4c271a74903f600fbaa268b5fec23fead4d6407b`，线上文件哈希一致；学生鉴权下载主体仍待有效正式学生账号验证。

## 备份与迁移

固定SSH双端主机指纹，经123跳板连接130，未调用针对123 Windows的deploy.py。备份目录 `/data/backups/xueyeceping/20261014_seat_local_v1`，整库 **404,586,706 bytes**，SHA-256 **`064fd77565b5442727ed00f1889f205848a822dc4e5ec50f465c8368e59d1fef`**，Dump completed尾标和大小核对通过，附旧unit、Nginx、共享env、实际外置config及EnvironmentFile副本。

执行仓库`sql/seat_helper_local_claim_v2.sql`并复跑，新增local_ip/seat_name两列可空，旧111设备/42基准保持，原绑定8字段全量排序摘要保持`021d975cffd40fc53ea76c27d29035752baa3d067b20fec80e84102408e1caba`；历史新快照字段均空，不猜填IP。切换前再查5分钟交卷与两类AI待执行/运行均0。共享env键集合保持，只有SEAT_HELPER_CLIENT_PATH切到本release；现有学校开关、外置配置、隔离env保持，旧20261008依赖release不可删除。

## 已完成正式验收

- 真实教师登录、选校、工作台、当前授权课堂机位接口通过，课程361/2021级7班返回45人；匿名和教师调用学生claim-local被拒绝。
- 真实教师Chromium进入完整大屏，一键保存按钮和灰色“未连助手”可见，机位明细入口已消失；只读验收，未点击保存真实机位或更改作品/成绩。
- 正式教研员凭据登录/getInfo/默认首页通过。两角色浏览器pageerror/HTTP5xx均0。
- `sql/seat_helper_local_release_20261014.sql`登记 **128 / 1.30.50 / PUBLISHED**，真实教师`/business/platform-update/list`确认可见；并非只有草稿。

正式凭据文件内学生验收账号登录失败，已向用户索取有效账号，没有猜测密码。因此学生真实登录、下载主体、实际Windows助手对正式新接口的认领/换账号仍未验，不能用本机Mock/API夹具冒充正式通过。新浏览器LNA/HTTP权限、实体机房两机换人/异机、250台同传/还原后唯一性、自启与高峰负载继续待现场。IP变化告警仅提示核实，不能单独认定盗号。

## 回退

上一后端/助手目录`20261014_seat_logo_overflow_v2`，上一前端`20261014_login_input_v1`均保留。确认当前仍为本release且任务/交卷空闲后，从本次备份恢复service.before、nginx.before、env.before到对应原路径，daemon-reload、重启后端、nginx -t/reload并核对首页恢复`c59bce0b...`和旧JAR`f1f59ddf...`；将128撤DRAFT。若已发生学生助手升级，保留机器端0.2.3和已升级fingerprint，旧网页可通过助手单次URL关联，不直接替换旧EXE。新增两列/基准保留，无结构回滚SQL，不整库回灌覆盖新教学数据。

执行和非敏感机器证据在`output/seat-local-release-20261014/`（baseline/package-evidence/manifest/migration/prepared/status/api-verification/browser-verification）。AOCI同步状态由后续真实工具结果登记，当前不宣称aligned。
