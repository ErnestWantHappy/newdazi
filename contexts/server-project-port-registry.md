# 10.52.1.123 项目与端口台账

> 最近核验：2026-08-15。维护人：郑东旭。  
> 规则：部署新项目或调整端口前，先更新本表；“历史目录”不等于当前正在运行，不得仅凭文件夹名占用端口。

## 当前对外项目

| 项目 | 用途概述 | 对外地址 | 实际端口 / 路由 | 发布或运行目录 | 状态 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 信息科技学业测评平台 | 教学、题库、课程、学生作答、批改与学情分析 | `http://xxkj.xsedu.net.cn/`、`http://10.52.1.123:3010/` | Nginx 3010；API 3009 | `D:\program\3009dazipingtai\releases\` | 运行中 |
| 郑东旭个人主页 | 教师简介、工具作品、讲座荣誉与联系方式 | `http://10.52.1.123:3012/` | Nginx 3012 | `D:\program\zhengdongxu-portfolio\releases\` | 运行中 |
| 班主任工作台 | 名单、考勤、积分、座位、值日与成绩分析；纯静态、本地存储 | `http://10.52.1.123:3012/tools/banzhuren/` | 复用 Nginx 3012 子路径 | 随个人主页 release 发布 | 运行中 |
| 小型网络搭建仿真网页 | 信息科技网络设备拖拽、连线和实验 | `http://10.52.1.123:3020/` | NSSM 3020 | `D:\program\3000xiaxingwangluodajian_7shang\` | 运行中 |
| AI 错题刷题平台 | Excel 题库、刷题、错题沉淀和统计 | `http://10.52.1.123/aicuoti/` | 80 路由反代 5003 | `D:\program\5003aishuati\` | 运行中 |
| AI 学业评价系统 | 多学科学情分析、班级画像和教学建议 | `http://10.52.1.123/aiquanxuekexiangshan/` | 80 子路径；API 3017 | `D:\program\3017aiquanxueke\` | 运行中 |
| 信息科技基础检测 | 信息科技基础知识检测 | `http://10.52.1.123/pcjichu/` | 80 路由反代 3011；后端进程 3000 | `D:\program\3011xinxikejijiance\` | 运行中 |
| 郑老师工具导航 | 教育工具统一导航首页 | `http://10.52.1.123/` | Nginx 80 根站 | `D:\program\80zhenglaoshidaohang\` | 运行中 |
| 图像识别工具 | Python 图像识别服务 | `http://10.52.1.123:3001/` | NSSM 3001 | `D:\program\3001tuxiangshibie\` | 运行中 |
| 课堂实验工具 | Node 课堂/学校实验服务 | `http://10.52.1.123:3003/` | NSSM 3003 | `D:\program\3003zhangxiao\` | 运行中 |
| SIoT 教学物联网 PoC | MQTT 与 Web 管理实验服务 | `mqtt://10.52.1.123:1883`、`http://10.52.1.123:1888/` | 1883、1888 | `D:\program\siot-poc\releases\` | 运行中 |

## 端口占用规则

| 端口 | 当前用途 | 新项目能否使用 |
| :---: | :--- | :---: |
| 80 | 工具导航、域名反代、`/aicuoti/`、`/aiquanxuekexiangshan/`、`/pcjichu/` | 否 |
| 1883 / 1888 | SIoT MQTT / Web | 否 |
| 3000 | 信息科技基础检测后端 | 否 |
| 3001 | 图像识别工具 | 否 |
| 3002 | Windows 系统监听；对应历史邮件目录需再核实 | 否 |
| 3003 | 课堂实验工具 | 否 |
| 3005 | Windows HTTP.sys 监听；旧导航目录存在 | 否 |
| 3009 | 学业测评平台后端 | 否 |
| 3010 | 学业测评平台前端 | 否 |
| 3011 | 信息科技基础检测对外服务 | 否 |
| 3012 | 郑东旭个人主页及静态子工具 | 否；新静态工具优先挂子路径 |
| 3017 | AI 学业评价系统 API | 否 |
| 3020 | 小型网络搭建仿真网页 | 否 |
| 5003 | AI 错题刷题平台 | 否 |

## 历史目录与待复核项

以下目录在服务器存在，但当前台账未把它们认定为独立对外服务。再次启用前必须先查监听、服务、入口和数据依赖：

- `D:\program\3002youjian\`：历史邮件工具目录。
- `D:\program\3004onlyoffice\`：历史 OnlyOffice 目录。
- `D:\program\3005wangzhidaohang\`：历史导航资源目录，3005 当前由 HTTP.sys 占用。
- `D:\program\3006backend-shujuhuoqu1\`：历史数据获取后端目录。
- `D:\program\3007shujuqingqiu\`：历史数据请求工具目录。
- `D:\program\3010daziqianduan\`：旧测评平台前端目录；正式版本以 release 和 Nginx root 为准。
- `D:\program\5005dmwdianming\`：历史大目湾点名工具目录。
- `D:\program\80zhenglaoshidaohang_*backup*\`：80 导航站备份，不是独立项目。
- `D:\program\_startup_logs\`：服务启动脚本与日志，不分配端口。

## 变更登记要求

1. 新服务先选未占用端口；静态工具优先挂在 `3012/tools/<slug>/`，避免新增防火墙规则。
2. 发布目录使用 `releases/<时间戳_版本>/`，不覆盖旧 release。
3. 修改 Nginx 前备份配置并记录 SHA-256；先执行候选配置检查，再切换和 reload。
4. 表中“运行中”必须同时有监听、HTTP 探活或服务状态证据；只存在目录只能写“待复核”。
