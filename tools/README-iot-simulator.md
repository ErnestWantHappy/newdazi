# 物联网模拟器

模拟器不会把账号、密码或 Token 写入仓库。运行前在当前终端设置以下环境变量：

```powershell
$env:IOT_SIMULATOR_USERNAME = "平台生成的设备账号"
$env:IOT_SIMULATOR_PASSWORD = "一次性设备密码"
$env:IOT_SIMULATOR_TOPIC = "平台生成的 Topic"
python tools/iot-mqtt-simulator.py --payload-type json --payload 42 --count 5 --interval 1
```

可选环境变量：`IOT_SIMULATOR_HOST`（默认 `10.52.1.123`）、`IOT_SIMULATOR_PORT`（默认 `1883`）、`IOT_SIMULATOR_CLIENT_ID`。模拟器支持 `number`、`text`、`json` 三种消息格式；输出只包含 Topic、格式和字节数，不输出密码。
