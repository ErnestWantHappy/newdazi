# 130 外置配置模板（放 `/data/apps/xueyeceping/shared/config/`，无密码）

密码与密钥一律用环境变量或 130 本机私密记录，不写进本文件。
`profile` 指向 `/data/upload`，库内 `/profile/...` 相对路径零修改即可沿用。

```yaml
# application.yml（130 外置，覆盖打包内同名配置）
ruoyi:
  profile: /data/upload
  libre-office:
    home: /usr/lib/libreoffice
    instance-count: 3
    process-warn-threshold: 17
    max-tasks-per-process: 30
    task-execution-timeout: 300000
    task-queue-timeout: 120000
```

```yaml
# application-druid.yml（130 外置）
spring:
  datasource:
    druid:
      master:
        url: "jdbc:mysql://127.0.0.1:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true"
        username: root
        # password: 经环境变量注入，不落盘
```

```properties
# redis（130 本机 127.0.0.1:6379，密码见私密记录；持久目录已迁 /data/redis）
# 后端 spring.redis 配置同样以外置 yml + 环境变量提供密码。
```

129 依赖（130 后端环境变量，与 123 同值、只读复用，不改 129 配置）：
`JUDGE0_BASE_URL=http://10.52.1.129:2358`、
`CRYPTPAD_*=http://10.52.1.129(:80)`（主/沙箱双入口兼容方案待定，见 design §5）、
`EMQX_*=10.52.1.129:1883`（演练用独立 ClientID，禁用生产订阅）。

前端构建（130）：`VITE_APP_BASE_API=/prod-api`（经 130 Nginx 反代）；
`VITE_DEVELOPER_SITE_URL` 保持默认（123 开发者站点，130 不自建）。
Nginx（130）：80/443 对外，后端内部 3009；CryptPad 来源隔离规则单独设计。
