# 机位显示与助手安装正式发布验收

日期：2026-10-08（北京时间）。130正式版本 **1.30.37**，release `20261008_seat_install_v1`；上一版 `20261007_bug_race_v1`（1.30.36）。

## 范围与结果

- 基于1.30.36隔离源码，只叠加课堂大屏、学生机位组件、机位明细及共用显示工具4个前端文件。优先助手内网IP，降级时标注连接IP，姓名与IP分行，隐藏自动电脑指纹名。
- 助手升级0.2.1：优先D盘安装，明确回退位置；完整复制后从安装目录运行，保留关联文件。快捷方式与当前Windows用户登录自启独立执行，自启命令回读校验；安装结果窗口显示目录、状态和还原基线提示。
- 后端JAR与原正式字节一致，为切换下载配置复制至新release并重启。共享环境仅修改 `SEAT_HELPER_CLIENT_PATH`，其余键和值均核对不变；外置配置及隔离配置沿用旧版。无结构迁移，仅执行 `sql/seat_install_release_20261008.sql` 登记更新。

## 验证

- 客户端30项回归、.NET Framework4构建通过；前端Node3项、隔离Vue3生产构建及真实模板六列模拟数据检查通过。
- 独立systemd任务 `release-20261008_seat_install_v1` COMPLETED；后端、Nginx、aiot-lamp active，验证码代理HTTP200，首页与助手哈希匹配，候选env哈希匹配。
- 真实教师、学生、教研员登录及只读接口通过。教师当前可访问课堂45人姓名可见；学生首页机位提示可见；桌面1440/手机390登录页无横向溢出，浏览器脚本错误0。旧验收课程629本次接口业务500，已改为从当前教师仪表盘选择授权课程；未修改该课程或学生数据。
- 从正式学生鉴权接口实际下载34361 bytes，EXE主体逐字节等于0.2.1（34304 bytes），末尾为公开站点地址，下载文件在本机以 `--self-test` 运行通过（.NET4），没有执行设备注册/安装。
- 更新115／1.30.37为PUBLISHED，真实教师 `/business/platform-update/list` 验证可见。证据、截图、脚本在 `output/seat-install-release-20261008/`。
- 正式验收课堂没有在线助手设备，真实同机两端内网IP一致性仍需现场复核；本轮未在学生机实际安装、开机或还原。不能把下载/构建通过称为机房抗还原已验收。

## 备份与哈希

备份目录 `/data/backups/xueyeceping/20261008_seat_install_v1`，包含整库database.sql及service.before、nginx.before、env.before、config.before。整库 **339961705 bytes**，已核对dump完成标记，SHA-256 `1b38f378afc3a0aaf729b2ea319985b6fb04b4ce4e270cfffd428b3e73489018`。旧release保留。

| 制品 | SHA-256 |
| --- | --- |
| 后端JAR（保持原版） | `f975b202a80ecc296a0e96ec6cf9f461b37f805b61efb8b06e9716046132acc4` |
| 新首页 | `d22e4eac9c4fc651c6e9ba0908842487201c51ff66bc62d8530adb773f1efbc9` |
| 助手0.2.1原始EXE | `867f2c37dd6b8f37f70d9f700b67394310353292371221434ee1069fc559474e` |

## 回滚与下一步

先核对当前仍指向本release且无活动AI任务。恢复上述备份service.before到 `/etc/systemd/system/xueyeceping-130.service`、nginx.before到 `/etc/nginx/sites-available/xueyeceping-130`、env.before到 `/data/apps/xueyeceping/shared/xueyeceping-130.env`；daemon-reload、重启xueyeceping-130、nginx -t后reload Nginx。核对旧首页 `60c25b57e2a82a206cd01eed018e4af157590024445b538debb5b28af1f0197a`、验证码200及三服务active，助手下载回到0.2.0。仅将更新115／1.30.37撤回DRAFT；无业务/结构回滚SQL，不整库回灌覆盖新教学数据。已安装到学生机的0.2.1不会随服务器回滚自动降级，协议兼容旧后台。

下一步：实际学生机退出旧助手→下载运行新版→核对D盘/自启/关联→老师保存还原基线→重启还原后不再手动启动下载副本，核对托盘及教师端IP。不同Windows用户需要分别核对自启。AOCI未同步。
