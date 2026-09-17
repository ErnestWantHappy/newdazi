#!/usr/bin/env bash
# hwprobe.sh —— 129 扩展机硬件探针（配合 123 后端 ExtensionHealthController SSH 探针）
# 用法：执行后输出单行 JSON，字段契约见 contexts/site-ops-and-info/design.md
# 建议部署：/usr/local/bin/hwprobe.sh，chmod +x，并以免密 SSH 授权给 123 后端账号
set -e

HOST=$(hostname)
IP=$(hostname -I 2>/dev/null | awk '{print $1}')
OS=$(. /etc/os-release 2>/dev/null && echo "$PRETTY_NAME" || uname -s)
KERNEL=$(uname -r)
CORES=$(nproc 2>/dev/null || echo 0)
SOCKETS=$(lscpu 2>/dev/null | awk -F: '/^Socket/{gsub(/ /,"",$2);print $2;exit}' || echo 1)
CPU_MODEL=$(lscpu 2>/dev/null | awk -F: '/Model name/{sub(/^ +/,"",$2);print $2;exit}' || echo '')

# 内存（字节）
if [ -r /proc/meminfo ]; then
  TOTAL_KB=$(awk '/MemTotal/{print $2}' /proc/meminfo)
  AVAIL_KB=$(awk '/MemAvailable/{print $2}' /proc/meminfo)
  MEM_TOTAL=$((TOTAL_KB * 1024))
  MEM_AVAIL=$((AVAIL_KB * 1024))
else
  MEM_TOTAL=0; MEM_AVAIL=0
fi

# 磁盘（字节）
DISKS_JSON=""
while IFS= read -r line; do
  [ -z "$line" ] && continue
  mount=$(echo "$line" | awk '{print $NF}')
  fs=$(echo "$line" | awk '{print $1}')
  total=$(echo "$line" | awk '{print $2}')
  free=$(echo "$line" | awk '{print $4}')
  used_pct=0
  if [ "$total" -gt 0 ] 2>/dev/null; then
    used_pct=$(( (total - free) * 100 / total ))
  fi
  entry=$(printf '{"mount":"%s","fs":"%s","totalBytes":%s,"freeBytes":%s,"usedPercent":%s}' "$mount" "$fs" "$total" "$free" "$used_pct")
  if [ -z "$DISKS_JSON" ]; then DISKS_JSON="$entry"; else DISKS_JSON="$DISKS_JSON,$entry"; fi
done < <(df -B1 2>/dev/null | tail -n +2)

# Node 版本（宿主 + CryptPad 容器若有）
NODE_V=$(node -v 2>/dev/null || echo '')
CRYPTPAD_NODE_V=$(docker exec cryptpad node -v 2>/dev/null || docker inspect --format '{{.Config.Image}}' cryptpad 2>/dev/null || echo '')

# Java 信息：查第一个 java 进程的命令行与版本
JAVA_JSON="null"
JAVA_PID=$(pgrep -f 'java' 2>/dev/null | head -n1 || true)
if [ -n "$JAVA_PID" ]; then
  JAVAHOME=$(readlink -f /proc/$JAVA_PID/exe 2>/dev/null | sed 's#/bin/java##' || echo '')
  JAVA_VERSION=$(ps -p $JAVA_PID -o args= 2>/dev/null | head -c 300 || echo '')
  JAVATIME=$(ps -p $JAVA_PID -o lstart= 2>/dev/null | head -c 200 || echo '')
  JAVAPARGS=$(tr '\0' ' ' < /proc/$JAVA_PID/cmdline 2>/dev/null | head -c 400 || echo '')
  JAVA_JSON=$(printf '{"version":"%s","startTime":"%s","home":"%s","arguments":"%s"}' "$JAVA_VERSION" "$JAVATIME" "$JAVAHOME" "$JAVAPARGS")
fi

cat <<EOF
{"hostname":"$HOST","ip":"$IP","os":"$OS","kernel":"$KERNEL","cpu":{"model":"$CPU_MODEL","cores":$CORES,"sockets":$SOCKETS},"memory":{"totalBytes":$MEM_TOTAL,"availableBytes":$MEM_AVAIL},"disks":[$DISKS_JSON],"nodeVersion":"$NODE_V","cryptpadNodeVersion":"$CRYPTPAD_NODE_V","java":$JAVA_JSON,"gpu":""}
EOF