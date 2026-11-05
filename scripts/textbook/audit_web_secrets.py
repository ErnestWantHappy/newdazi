"""只读扫描网页/工具工程里的疑似凭据，只输出位置与键名，不输出取值。

用于打包前的凭据核对；占位符（示例、你的、xxx、<...>、空串）单独标注。
"""
import json
import re
from pathlib import Path

ROOT = Path(r"d:/dmwprogram/newdazipingtai/newdazi")
OUT = ROOT / "output/textbook-cases"
SOURCE = Path(json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))["sourceRoot"])
SKIP_DIRS = {"node_modules", ".git", "__pycache__", "venv", ".venv", "dist", ".idea", ".vscode", "runtime", ".workbuddy", ".wpp_backup"}
TEXT = {".py", ".js", ".mjs", ".cjs", ".ts", ".html", ".json", ".txt", ".md", ".bat", ".cmd", ".yml", ".yaml", ".ini", ".env", ".example", ".toml"}
PATTERN = re.compile(r"(?P<key>[A-Za-z0-9_\-]*(?:api[_-]?key|secret|token|password|passwd|access[_-]?key)[A-Za-z0-9_\-]*)\s*[:=]\s*[\"']?(?P<value>[^\"'\s,;]{6,})", re.I)
PLACEHOLDER = re.compile(r"^(你的|这里|示例|xxx|\*{3,}|<.*>|your|example|placeholder|\$\{|changeme)", re.I)


def scan(paths, label):
    findings = []
    for base in paths:
        for path in base.rglob("*"):
            if not path.is_file() or path.suffix.lower() not in TEXT:
                continue
            if any(part in SKIP_DIRS for part in path.parts):
                continue
            if path.name.endswith((".min.js", ".map")):
                continue
            try:
                text = path.read_text(encoding="utf-8", errors="ignore")
            except OSError:
                continue
            for match in PATTERN.finditer(text):
                value = match.group("value")
                findings.append({
                    "file": str(path),
                    "key": match.group("key"),
                    "length": len(value),
                    "placeholder": bool(PLACEHOLDER.match(value)),
                })
    (OUT / f"web-secrets-{label}.json").write_text(json.dumps(findings, ensure_ascii=False, indent=2), encoding="utf-8")
    real = [item for item in findings if not item["placeholder"]]
    print(f"[{label}] 命中 {len(findings)} 处，非占位 {len(real)} 处")
    for item in real:
        print(f"   {item['file'].split('优秀课例展示')[-1]} :: {item['key']} (len={item['length']})")


if __name__ == "__main__":
    projects = json.loads((OUT / "web-projects.json").read_text(encoding="utf-8"))
    scan([SOURCE / item["project"] for item in projects], "web-projects")
    scan([Path(r"d:/dmwprogram/newdazipingtai/newdazi/output/textbook-cases/rar-work")], "rar-work")
