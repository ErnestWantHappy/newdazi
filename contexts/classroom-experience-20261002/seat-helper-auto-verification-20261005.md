# 2026-10-05 机位助手直接运行：正式验收

用户选择方案1。130正式1.30.27已切换 `20261005_seat_helper_auto_v2`，客户端0.2.0。更新106为PUBLISHED，现有真实教师list确认可见。后台、Nginx、aiot-lamp均active，首页与代理API均HTTP200。

## 实现与范围

学生鉴权下载EXE仅附带公开平台根地址，普通运行不打开管理员配置表单。客户端自动获取物理网卡内网IPv4和机器指纹，以原学生网页登录决定学校；客户端独立轮询证明领取设备密钥，数据库提交后发布结果，DPAPI保存后确认领取。同校有效电脑重复配对复用ID及教师命名，被撤销电脑不能自动恢复。教师当前授权课堂可设置机房/机位并显式确认基准，学生无命名权限。

主要文件：后端SeatHelperController、SeatHelperService、SeatHelperPairService、SeatHelperMapper；前端studentSession、seatHelper工具、StudentSeatHelper、ClassroomSeatPanel和管理页；客户端SeatHelper.cs及构建/回归。未引入依赖、调整原权限模型或修改数据库结构；30秒心跳、75/90秒TTL与历史课禁写保留。

本机JAR被运行中的开发服务占用，采用独立源码目录clean package，不启停该服务。同时发现另一任务登录展厅改动，独立制品恢复原登录页并排除LoginPreview相关代码，保留工作区原改动；JAR实际业务class变化均限SeatHelper，前端五个编译模块变化，旧静态资产保持。原教师课堂42/42人数与成绩入口、课堂跳转正式复核通过。

## 已验证

- Java 708项，0失败/错误，3跳过；Node1项、客户端18项回归及三端构建通过。
- 正式API4组：独立4账号登录、鉴权下载公开尾部与主体哈希、无助手真实作品提交、轮询凭据隔离。
- 正式浏览器7组：实际下载Windows EXE获取IP与网页登录关联、DPAPI保存及链接重放拒绝；教师设置名称和确认；同一已登录标签页重复关联保留ID和名称；学生命名接口拒绝；Python停留超过98秒正常、网页续期4次；撤销后禁止自动注册但课程可用；未安装或学校关闭仍正常课程。pageerror/HTTP500均0。
- 验后4账号/学校停用、临时学校列表权限撤除、专用设备撤销、指派/教师班级关联清0。测试作品及1条长期基准保留审计。真实61校仍启用，未修改真实学生作品、成绩、密码或课程指派。

实际Windows EXE通过自动配对验收模式连接正式站，验证真实IP、注册、DPAPI和心跳；验收模式没有写开发机桌面/自启，也未验证真实Windows10机房、极域、还原卡或镜像克隆。普通启动无表单、安装和快捷方式逻辑已实现，但不能把这轮接口验证称为机房现场安装验收。

## 发布、备份与回退

首次候选v1在正式下载验收中发现公开尾部长度不一致，未登记产品版本；修正客户端17字节标识和21字节尾部解析，补三项真实文件回归，另建v2而未覆盖旧release。最终仅v2通过验收并登记1.30.27。无业务迁移，仅执行 `sql/seat_helper_auto_release_20261005.sql` 发布记录；临时验收写入限专用学校。

初始完整备份 `/data/backups/xueyeceping/20261005_seat_helper_auto_v1/database.sql`，313986710 bytes，SHA-256 `96bb5a487976da96a6b49a70b1d552bba0540c8ef451f6a5399de627b84dc954`。v2前完整备份 `/data/backups/xueyeceping/20261005_seat_helper_auto_v2/database.sql`，313989350 bytes，SHA-256 `bc3bbe1981059f4566cc67e5e3c2f384a5e02d7e2b692bd55c5be77776deb77d`。

JAR SHA-256 `6c7affea3e379912a9e4e691680c6b39924aa416f446e8265488e754db0dd276`；首页 `03a471b810e194ea067eb13fe6d2cadf238147bbd291477c4bda7f24181c36fa`；原始EXE `c2c8aac53b38edf6398125d0a038888de2b1587866d87ddeac12b01982c3126a`，27648 bytes。下载追加公开JSON和21字节尾部，整体哈希不同，已核对主体一致。共享env其他行保持，只切换客户端路径；无HTTPS或无关服务调整。

回退到已验收1.30.26时，恢复**v1初始备份**中的 `service.before/nginx.before/env.before` 到原unit、Nginx和共享env，daemon-reload后重启后台、nginx -t并reload，探活并核对旧首页哈希；更新106撤DRAFT。v2备份指向未验收的v1，不能把它当最终旧版回退。保留新设备记录与教师基准，无结构回滚SQL，不整库回灌覆盖后续教学数据。学生客户端需退出旧实例再重新下载。

证据：`output/seat-helper-auto-20261005/` 的manifest、java-isolated.log、frontend-isolated.log、client-final.log、node.log、api-results、browser-results、existing-teacher、cleanup、status-result、final-audit、teacher-version；截图 `output/playwright/seat-helper-auto-20261005/`。私密配置/令牌文件仅本机保留，不入公开文档。

## 剩余与下一步

学生关闭旧配置窗口/旧托盘实例，刷新平台后重新下载并直接运行。教师随后统一命名、确认常用机位。真实机房自启/还原仍待小范围现场验证；历史118条空作品状态未治理。没有推送Git，另一任务的本机登录展厅仍未发布。
