"""只读复核 1.30.90 线上状态、启动日志和产品更新登记。"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts" / "deploy"))
import remote130  # noqa: E402


def main() -> int:
    jump, client, password = remote130.connect()
    try:
        code, out, err = remote130.run(
            client,
            "systemctl show xueyeceping-130 -p ActiveState -p SubState -p MainPID -p NRestarts; "
            "journalctl -u xueyeceping-130 --since '10 minutes ago' --no-pager "
            "| grep -Ei 'ERROR|Exception|Failed to start|Started|Application run failed' "
            "| tail -80 || true",
            timeout=120,
        )
        if code:
            raise RuntimeError((err or out).strip())
        lines = [line.strip() for line in out.splitlines() if line.strip()]
        service = {}
        logs = []
        for line in lines:
            if "=" in line and line.split("=", 1)[0] in {
                "ActiveState", "SubState", "MainPID", "NRestarts"
            }:
                key, value = line.split("=", 1)
                service[key] = value
            else:
                logs.append(line)
        errors = [
            line for line in logs
            if re.search(r"\bERROR\b|Exception|Failed to start|Application run failed", line)
        ]
        print("SERVICE=" + json.dumps(service, ensure_ascii=False))
        print("LOG_HAS_ERROR=" + ("1" if errors else "0"))
        if errors:
            print("LOG_ERRORS=" + json.dumps(errors[-20:], ensure_ascii=False))
        print("LOG_STARTED=" + json.dumps(
            [line for line in logs if "Started" in line][-5:], ensure_ascii=False))
        return 0 if not errors else 1
    finally:
        client.close()
        jump.close()


if __name__ == "__main__":
    raise SystemExit(main())
