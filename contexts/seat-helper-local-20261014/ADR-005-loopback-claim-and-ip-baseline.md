# ADR-005 回环单次认领与IP确认快照

日期：2026-10-14；用户确认本地回环方案，130已正式发布1.30.50/助手0.2.3，学生真实认领与机房仍待验，见[正式验收](release-verification.md)。

背景：不关机换班使新登录会话丢失原机位关联；连接来源IP经NAT不能表示学生物理机。逐台命名与机位明细弹窗增加教师课堂负担。

备选：继续依赖特殊URL；按浏览器存储绑定设备；回环接口重新认领。前两者不能证明当前本机助手，浏览器数据还可能被同传复制。选择回环接口，并保留原URL作为兼容入口。

对已确认方案的认证细化：不把deviceSecret暴露给浏览器，不允许直接凭自报deviceId/localIp认领；返回已认证助手取得的短期单次token。浏览器经当前学生鉴权消费，后端复核学校/撤销/TTL。此机制不构成硬件签名，不保证阻止凭证转发或主动伪造助手。

基准新增确认时IP和名称快照；IP不做设备主键、不进入MAC指纹。旧基准不追溯编造IP。教师明确保存才能更改，告警用于核实，不直接裁定盗号/抄袭。

HttpListener的URLACL拒绝时使用同一回环端口的TcpListener兼容路径，无新依赖或管理员安装步骤。浏览器LNA权限、安全上下文、机房策略仍需实测，不能承诺所有浏览器零权限弹窗；官方说明：[Chrome](https://developer.chrome.com/blog/local-network-access)、[Edge](https://learn.microsoft.com/en-us/deployedge/ms-edge-local-network-access)。

代价：配套SQL/前后端/助手发布、首次升级与浏览器策略现场验证；5秒REST校准不等于推送即时到达。已有指纹迁移需持旧密钥，回退客户端要专门处理DPAPI与指纹，见[设计](design.md)。验证见[验收](verification.md)。
