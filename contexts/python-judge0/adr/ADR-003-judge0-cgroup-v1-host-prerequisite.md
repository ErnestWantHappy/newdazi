# ADR-003：Judge0 1.13.1 的宿主机 cgroup v1 前置条件

## 状态

已执行并验证。

## 事实

扩展服务器为 Ubuntu 24.04、Docker 29.1.3。初始宿主机仅挂载 cgroup v2，Judge0 CE 1.13.1 的 isolate 因无法创建 `/sys/fs/cgroup/memory/box-1` 返回 Internal Error。容器已使用 `privileged`，增加 `cgroupns: host` 或在容器内创建目录不能把 v2 转换为 v1。

## 决策

保持固定 Judge0 1.13.1 和现有资源限制，不关闭内存/进程隔离，也不替换镜像。已备份 `/etc/default/grub`，加入 `systemd.unified_cgroup_hierarchy=0` 并重启扩展服务器；启动后已核对 `/sys/fs/cgroup/memory` 为 cgroup v1，Judge0 已启动并完成真实 Python Accepted 验证。临时本机测试白名单已撤销，最终 `ALLOW_IP=10.52.1.123`。

## 回滚

若 CryptPad 或 Docker 异常，恢复 GRUB 备份并删除新增参数后重启；平台保留 `JUDGE0_MODE=disabled` 回退开关，提交和代码仍保留，不写零分。
