# 2026-10-16历史暂存：当日未发布

2026-10-17已按用户重新授权合并发布130正式1.30.52，双端`20261017_seat_score_v1`、助手0.2.4；真实教师/教研员验证及更新130/PUBLISHED通过，学生与机房仍待验。见[正式验收与回退](../seat-score-release-20261017.md)。本文件只保留10月16日停止指示及旧暂存的历史事实；后续实际发布以10月17日新记录为准。

2026-10-16，用户最后指示“不要部署服务器，本地修改就可以了”。后续只做本地修改；不得继续运行发布、切换或发布更新登记脚本，除非用户重新明确授权。

## 已发生的操作

停止指示前按此前上线授权只读核对130、整库备份并上传了隔离候选`20261016_seat_browser_v1`。未切换systemd/Nginx/env、未重启、未执行业务SQL或发布登记。最后只读复核：正式unit/site/env哈希保持，PID2408758/NRestarts=0，xueyeceping-130/nginx/aiot-lamp均active；候选三端SHA匹配，最近5分钟276次提交，未通过空闲门禁。1.30.52更新记录为0，不生成RELEASE_LOG正式条目。

正式仍产品1.30.51、前后端`20261015_practical_pptx_v1`、助手0.2.3（下载路径仍指向`20261014_seat_local_v1`）。新0.2.4仅本机及未启用的服务器暂存副本，不可向老师宣称已上线。

## 隔离证据

- 后端以现场拉取的1.30.51 JAR仅替换5个SeatHelperService/SeatHelperController及内部DTO条目；其它JAR/业务条目逐字节保持，页图v4及期限/PPTX修复保留。不混入成绩/打字候选。
- 前端以正式1.30.51源码快照（dist首页哈希匹配线上）仅覆盖`studentSession.js`、`seatHelper.js`，独立构建发布门禁通过。候选Nginx语法验证通过，旧静态资源保留。
- 暂存位置：`/data/apps/xueyeceping/releases/20261016_seat_browser_v1`，没有成为运行release。保留暂存副本，不擅自删除。
- 备份位置：`/data/backups/xueyeceping/20261016_seat_browser_v1`，整库425,995,126B，SHA-256 `a6ee08d99a787e354fe6640d742842a380908a4e481495f876a153619b0329e1`；含service/nginx/env/config快照。备份时124设备/42基准，未改基准。

| 候选文件 | SHA-256 |
| --- | --- |
| ruoyi-admin.jar | `915c857a2bf9cfd3e9a1d3b6004948bc17b5c4222d6294e099601f3f9f8f1b0a` |
| frontend/index.html | `ad3aa113d0eb9a35567c79805379488e2cff40718d318cc464f05125829d787a` |
| frontend.zip | `d1fa9a442403924c95ec64b5e20837bc1973b8130c521786d8a9ec8c8cbff036` |
| SeatHelper.exe 0.2.4 | `14ba194daa17cd7ff55b2efcb4cef1111ceb45a038098b657ea0f31087d5fd48` |

本机过程与状态位于`output/seat-browser-release-20261016/`。这些脚本不是自动任务；没有派发systemd发布job，没有循环等待发布。`sql/seat_helper_browser_release_20261016.sql`仅为未执行的未来发布登记候选。

## 后续边界

本轮无需回滚运行环境，因为未切换。不得因本记录自行修改服务器或清理暂存/备份。将来用户重新授权时须重新核对当时正式基线、候选SHA、服务/课堂状态及凭据；不能把本次暂存直接当作当前发布许可或最新基线。无表结构迁移，实际发布才需配套三端与后端重启；不得整库回灌。真实学生联调、机房自启/浏览器权限/后台节流/还原与同传仍未验。
