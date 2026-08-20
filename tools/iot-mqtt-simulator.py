"""物联网课堂 MQTT 模拟器。

只从环境变量读取凭据，支持数字、文本和 JSON，便于在不接触学生密码的情况下验收平台接收、限流和 Topic 映射。
"""
import argparse
import json
import os
import random
import sys
import time

try:
    import paho.mqtt.client as mqtt
except ImportError as exc:
    raise SystemExit("请先安装 paho-mqtt，再运行本模拟器") from exc


def required(name: str) -> str:
    value = os.environ.get(name, "").strip()
    if not value:
        raise SystemExit(f"缺少环境变量 {name}，不会从命令行或代码接收密码")
    return value


def main() -> int:
    parser = argparse.ArgumentParser(description="发布课堂物联网数字/文本/JSON消息")
    parser.add_argument("--payload-type", choices=("number", "text", "json"), default="number")
    parser.add_argument("--payload", default="42")
    parser.add_argument("--count", type=int, default=5)
    parser.add_argument("--interval", type=float, default=1.0)
    parser.add_argument("--qos", type=int, choices=(0, 1), default=0)
    args = parser.parse_args()
    host = os.environ.get("IOT_SIMULATOR_HOST", "10.52.1.123")
    port = int(os.environ.get("IOT_SIMULATOR_PORT", "1883"))
    username = required("IOT_SIMULATOR_USERNAME")
    password = required("IOT_SIMULATOR_PASSWORD")
    topic = required("IOT_SIMULATOR_TOPIC")
    client_id = os.environ.get("IOT_SIMULATOR_CLIENT_ID", f"iot-simulator-{random.randint(1000, 9999)}")
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=client_id)
    client.username_pw_set(username, password)
    client.connect(host, port, keepalive=30)
    client.loop_start()
    try:
        for index in range(max(1, args.count)):
            payload = args.payload
            if args.payload_type == "number":
                payload = str(float(args.payload) + index)
                if payload.endswith(".0"):
                    payload = payload[:-2]
            elif args.payload_type == "json":
                payload = json.dumps({"value": float(args.payload) + index, "source": "simulator", "index": index}, ensure_ascii=False)
            result = client.publish(topic, payload, qos=args.qos)
            result.wait_for_publish()
            print(json.dumps({"index": index + 1, "topic": topic, "payloadType": args.payload_type, "bytes": len(payload.encode("utf-8"))}, ensure_ascii=False))
            if index + 1 < args.count:
                time.sleep(max(0, args.interval))
    finally:
        client.loop_stop()
        client.disconnect()
    return 0


if __name__ == "__main__":
    sys.exit(main())
