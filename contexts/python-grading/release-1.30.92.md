# 1.30.92 Python批改教师确认制与运行查看验收

## 发布

- Release：`20261110_python_manual_confirm_v1`
- 后端JAR：108,301,913B，SHA-256 `a8162e4135b5ca9d153f190c6245e064b67e6481c166538e27793f237b9e0a8e`
- 前端：821文件，首页SHA-256 `d86ee446906b62db309733b9406fb420a1182cc78800052ab9f7ff5a3f49c45e`
- 服务：MainPID `3107625`，NRestarts 0，首页/验证码200，启动错误0
- 整库备份：`/data/backups/xueyeceping/20261110_124146_20261110_python_manual_confirm_v1/database.sql.gz`，53,410,495B，SHA-256 `0178408250904a8d6aaab98f9bc576b754246042eec5b58283f3623754562cc3`
- 产品更新：170 / 1.30.92 / PUBLISHED，真实教师接口可见

## 验收

- 后端定向25项通过，覆盖提交不写正式分、运行接口权限、Mapper契约。
- 前端批改页12项通过，生产构建和发布门禁通过。
- 真实教师1440/390：45人名单、Python代码、系统判题、运行查看和批量按钮正常；0横向溢出、0页面脚本异常、0个5xx。
- 运行查看输入后返回“三角形的周长是12厘米”，不产生新判题记录。
- 数据修复：`990607/4506` 43条、`662/4268` 74条系统0分转待批；教师已评分记录未动。

## 回退

恢复本次备份的 `service.before` 与 `nginx.before`，daemon-reload、重启 `xueyeceping-130`、`nginx -t && systemctl reload nginx`，将更新170转DRAFT。数据修复不整库回灌；如需撤销，按备份精确恢复两门课相关行。
