# 2026-11-04 实施与验证

- 已实现 PY 配置选项、上传MIME/文本/大小校验、TEXT 源码预览及教师隔离运行接口。
- 已实现输入、运行中、输出/错误、重复点击禁用、换作品/卸载丢弃迟到响应。
- 已通过后端格式、服务、入口角色定向测试及前端生产构建。
- 已用隔离接口模拟实操源码预览、stdin请求、输出、换到TXT隐藏运行，1440/390/320无横向溢出、无页面JS错误。证据 output/codebuddy-monitor-2025/py-ui-result.json。
- 130近期功能只读复核22项通过，包括课例/教材/页图/视频Range/HTML沙箱/下载/匿名拒绝/教师兼教研员/发布记录。证据 output/codebuddy-monitor-2025/online-audit.json。
- 尚未实测真实上传PY→提交→教师运行Judge0完整链路；未发布本功能，正式仍为1.30.71。
- 已从130连接真实Judge0验证中文输入输出成功及ZeroDivisionError回显，未生成平台提交或成绩。现场服务进程上限8，已修正新运行请求参数并加入回归断言。证据 output/codebuddy-monitor-2025/judge-live-result.json。
- 已知旧问题仍需治理，见 output/codebuddy-monitor-2025/复核返工.md。未宣称其已修复。
- 已随 1.30.72（2026-11-05，release `20261105_review_py_workbench_v2`）正式上线；真实 PY 上传→教师端运行完整链路仍待在正式环境补验。
