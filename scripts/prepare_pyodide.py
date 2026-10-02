"""下载固定版本及所需依赖，供学校内网直接从本站加载。"""
import hashlib
import json
from pathlib import Path
from urllib.request import urlopen

BASE = 'https://cdn.jsdelivr.net/pyodide/v0.26.4/full/'
DEST = Path(__file__).resolve().parents[1] / 'RuoYi-Vue3/public/pyodide'


def download(name, expected=None):
    target = DEST / name
    if not target.exists() or (expected and hashlib.sha256(target.read_bytes()).hexdigest() != expected):
        with urlopen(BASE + name, timeout=120) as response:
            content = response.read()
        if expected and hashlib.sha256(content).hexdigest() != expected:
            raise RuntimeError('下载校验失败: ' + name)
        target.write_bytes(content)
    return hashlib.sha256(target.read_bytes()).hexdigest()


if __name__ == '__main__':
    DEST.mkdir(parents=True, exist_ok=True)
    hashes = {name: download(name) for name in [
        'pyodide.mjs', 'pyodide.asm.js', 'pyodide.asm.wasm', 'python_stdlib.zip', 'pyodide-lock.json'
    ]}
    packages = json.loads((DEST / 'pyodide-lock.json').read_text())['packages']
    seen = set()

    def package(name):
        if name in seen:
            return
        seen.add(name)
        info = packages[name]
        for dep in info['depends']:
            package(dep)
        hashes[info['file_name']] = download(info['file_name'], info['sha256'])
        print('已校验 ' + name, flush=True)

    for name in ['pandas', 'matplotlib', 'numpy', 'micropip']:
        package(name)
    (DEST / 'asset-manifest.json').write_text(json.dumps({'version': '0.26.4', 'sha256': hashes}, indent=2))
