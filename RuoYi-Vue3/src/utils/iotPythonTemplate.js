/**
 * 生成小学信息科技实验板（N17 / MicroPython）可直接粘贴运行的 MQTT 示例。
 * WiFi 不由平台保存，避免把学校无线密码写入数据库或接口响应。
 */
export function buildPrimaryIotPythonCode(config = {}) {
  const value = key => pythonString(config[key] ?? '')
  return `# -*- coding: utf_8 -*-
# 导入 MYBIT 程序库
from npython import *
# 导入 MQTT 程序库
from umqtt.simple import MQTTClient
import time

# 在下面开始写你自己的代码
# 只在实验板本地填写 WiFi，平台不会保存无线密码
WIFI_NAME = "请填写2.4G_WiFi名称"
WIFI_PASSWORD = "请填写WiFi密码"

# 以下参数由学业测评平台生成，请不要改动
MQTT_SERVER = ${value('brokerUrl')}
MQTT_PORT = ${Number(config.brokerPort || 1883)}
MQTT_CLIENT_ID = ${value('clientId')}
MQTT_USERNAME = ${value('username')}
MQTT_PASSWORD = ${value('password')}
MQTT_TOPIC = ${value('topic')}

client = None

def connect_wifi():
    # 屏幕显示格式：oled.print(列, 行, 显示内容, 字号)
    oled.print(1,1,"正在连接WiFi",1)
    print("正在连接 WiFi...")
    ip = wifi.connect(WIFI_NAME, WIFI_PASSWORD)
    oled.print(1,1,"WiFi连接成功",1)
    print("WiFi 已连接:", ip)

def connect_mqtt():
    global client
    oled.print(1,1,"正在连接MQTT",1)
    print("正在连接 MQTT...")
    client = MQTTClient(
        MQTT_CLIENT_ID.encode(),
        MQTT_SERVER,
        port=MQTT_PORT,
        user=MQTT_USERNAME.encode(),
        password=MQTT_PASSWORD.encode(),
        keepalive=60
    )
    client.connect()
    oled.print(1,1,"MQTT连接成功",1)
    print("MQTT 已连接")

def publish_value(value):
    global client
    payload = '{"source":"primary-board","value":' + str(value) + '}'
    try:
        client.publish(MQTT_TOPIC.encode(), payload.encode())
        oled.print(1,1,"数据发送成功",1)
        print("发送成功:", payload)
    except OSError as error:
        oled.print(1,1,"连接断开，重连中",1)
        print("发送失败，正在重连:", error)
        time.sleep(2)
        connect_mqtt()
        client.publish(MQTT_TOPIC.encode(), payload.encode())

connect_wifi()
connect_mqtt()

while True:
    # 把 123 换成传感器读数，例如光照、温度或湿度
    publish_value(123)
    time.sleep(5)
`
}

function pythonString(value) {
  // JSON 双引号字符串与 Python 字符串语法兼容，并能安全转义反斜杠和引号。
  return JSON.stringify(String(value))
}
