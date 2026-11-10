"""教研活动课例导入的公共工具：只处理公开元数据，过滤敏感信息并统一 staging 结构。"""
from __future__ import annotations

import base64
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import zipfile
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable
from urllib.parse import urlparse

import requests


ROOT = Path(__file__).resolve().parents[4]
SKILL_ROOT = Path(__file__).resolve().parents[1]
OUTPUT_ROOT = ROOT / "output" / "research-case-import"
SECRETS = ROOT / "contexts" / "secrets.local.md"
TRUSTED_CLOUD_HOSTS = {"yun.xsedu.net.cn"}
RESEARCH_PRIVATE_ROOT = "/data/upload-private/research-activity/"
TEXTBOOK_CASE_ROOT = "/data/apps/xueyeceping/shared/textbook/cases/"
ARCHIVE_SUFFIXES = {".zip", ".rar", ".7z", ".tar", ".gz", ".tgz", ".bz2", ".xz"}
EXECUTABLE_SUFFIXES = {".exe", ".bat", ".cmd", ".ps1", ".sh", ".py", ".url", ".com", ".scr", ".vbs", ".js", ".jar"}
PERSONAL_INFO_PATTERNS = (
    r"(?<!\d)(?:1[3-9]\d{9})(?!\d)",
    r"(?<!\d)(?:\d{17}[\dXx])(?!\d)",
    r"(?<!\d)(?:20\d{2}[-/.]?\d{2}[-/.]?\d{2})(?!\d)",
    r"学号[:：]\s*\d{4,}",
    r"身份证[:：]\s*\S{6,}",
)
SAFE_URL_SCHEMES = {"http", "https"}


class PipelineError(RuntimeError):
    """可预期的门禁或环境错误，报告给用户而不是吞掉。"""


def now_id() -> str:
    return datetime.now().strftime("%Y%m%d-%H%M%S")


def run_dir(run_id: str) -> Path:
    if not re.fullmatch(r"[A-Za-z0-9_.-]+", run_id or ""):
        raise PipelineError("run id 只能包含字母、数字、点、下划线和连字符")
    return OUTPUT_ROOT / run_id


def ensure_run(run_id: str) -> Path:
    path = run_dir(run_id)
    for name in ("source", "extracted", "previews", "reports"):
        (path / name).mkdir(parents=True, exist_ok=True)
    return path


def write_json(path: Path, value: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(safe_report(value), ensure_ascii=False, indent=2, sort_keys=False)
    redacted = redact_text(text)
    path.write_text(redacted + "\n", encoding="utf-8")


def read_json(path: Path) -> Any:
    return json.loads(path.read_text(encoding="utf-8"))


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def sha256_bytes(value: bytes) -> str:
    return hashlib.sha256(value).hexdigest()


def source_key(post_id: int) -> str:
    return "research-post-%d" % int(post_id)


def resource_key(post_id: int, sha256: str) -> str:
    return sha256_bytes(("research-post:%d:%s" % (int(post_id), sha256)).encode("utf-8"))


def redact_text(text: str) -> str:
    """清理报告中可能出现的完整私密链接、提取码与本机私密凭据。"""
    text = re.sub(r"(https?://yun\.xsedu\.net\.cn)\S+", r"\1/<redacted>", text, flags=re.I)
    text = re.sub(r"(extract_code|提取码|accesscode|password)\s*[:=]\s*[\"']?([^\s,}\"']+)",
                  r"\1=<redacted>", text, flags=re.I)
    text = re.sub(r"(?i)(YUELING_PASSWORD|PRACTICAL_AI_MASTER_KEY|COLLABORATION_KEY_SECRET|"
                  r"JUDGE0_AUTH_TOKEN|IOT_EMQX_API_SECRET)\s*=\s*\S+", r"\1=<redacted>", text)
    return text


def safe_report(value: Any) -> Any:
    if isinstance(value, dict):
        return {key: safe_report(item) for key, item in value.items()
                if key.lower() not in {"linkurl", "extractcode", "password", "token", "cookie", "authorization", "data"}}
    if isinstance(value, bytes):
        return "<bytes:%d>" % len(value)
    if isinstance(value, list):
        return [safe_report(item) for item in value]
    if isinstance(value, str):
        return redact_text(value)
    return value


def secret_value(env_name: str, label: str) -> str:
    value = os.environ.get(env_name)
    if value:
        return value
    if SECRETS.exists():
        match = re.search(r"^%s=(.+)$" % re.escape(env_name), SECRETS.read_text(encoding="utf-8"), re.M)
        if match:
            return match.group(1).strip().replace("\\@", "@")
    raise PipelineError("缺少 %s；只能从环境变量或 contexts/secrets.local.md 读取" % label)


def remote_connect():
    sys.path.insert(0, str(ROOT / "scripts" / "deploy"))
    import remote130
    return remote130.connect()


def remote_run(client: Any, script: str, timeout: int = 180) -> tuple[int, str, str]:
    import remote130
    encoded = base64.b64encode(script.encode("utf-8")).decode("ascii")
    return remote130.run(client, "echo %s | base64 -d | bash" % encoded, timeout=timeout)


def mysql_readonly(sql: str, timeout: int = 180) -> str:
    script = r"""set -e
cfg=/data/apps/xueyeceping/releases/20261008_seat_install_v1/config/application-druid.yml
u=$(awk '/username:/{print $2; exit}' "$cfg" | tr -d '"')
p=$(awk '/password:/{print $2; exit}' "$cfg" | tr -d '"')
umask 077
printf '[client]\nuser=%s\npassword=%s\nhost=127.0.0.1\n' "$u" "$p" >/tmp/.xyc-research-my.cnf
trap 'rm -f /tmp/.xyc-research-my.cnf' EXIT
mysql --defaults-extra-file=/tmp/.xyc-research-my.cnf --default-character-set=utf8mb4 -N -B ry-vue <<'SQL'
START TRANSACTION READ ONLY;
""" + sql + r"""
COMMIT;
SQL
"""
    jump, client, _ = remote_connect()
    try:
        code, out, err = remote_run(client, script, timeout=timeout)
        if code:
            raise PipelineError("正式库只读查询失败：" + redact_text(err.strip()))
        return out
    finally:
        client.close()
        jump.close()


def parse_tsv(text: str) -> list[list[str]]:
    return [line.split("\t") for line in text.splitlines() if line.strip()]


def parse_json_lines(text: str) -> list[dict[str, Any]]:
    return [json.loads(line) for line in text.splitlines() if line.strip()]


def cloud_session() -> requests.Session:
    session = requests.Session()
    session.headers.update({"User-Agent": "Mozilla/5.0 (compatible; research-case-import/1.0)"})
    return session


def cloud_api(session: requests.Session, origin: str, method: str, payload: dict[str, Any], timeout: int = 60) -> dict[str, Any]:
    response = session.post(origin + "/api/v1/link", params={"method": method}, json=payload,
                            timeout=timeout, allow_redirects=False)
    if response.status_code >= 400:
        try:
            error = response.json().get("errcode")
        except ValueError:
            error = None
        raise PipelineError("云盘接口 %s 返回 HTTP %d%s" % (method, response.status_code,
                                                          "，业务码 %s" % error if error else ""))
    try:
        return response.json()
    except ValueError as error:
        raise PipelineError("云盘接口 %s 未返回 JSON" % method) from error


def validate_cloud_url(url: str) -> tuple[str, str]:
    parsed = urlparse(url)
    if parsed.scheme != "https" or parsed.hostname not in TRUSTED_CLOUD_HOSTS:
        raise PipelineError("云盘链接不在受信域名列表")
    parts = [part for part in parsed.path.split("/") if part]
    link_id = next((part for part in reversed(parts) if len(part) == 32), None)
    if not link_id:
        raise PipelineError("云盘链接缺少可识别的分享标识")
    return "%s://%s" % (parsed.scheme, parsed.netloc), link_id


def trusted_download_url(url: str) -> str:
    parsed = urlparse(url)
    if parsed.scheme not in SAFE_URL_SCHEMES or parsed.hostname not in TRUSTED_CLOUD_HOSTS:
        raise PipelineError("下载地址不在教育云盘受信域名列表")
    return url


def safe_member_path(name: str) -> Path:
    normalized = name.replace("\\", "/")
    parts = [part for part in normalized.split("/") if part not in ("", ".")]
    if normalized.startswith("/") or re.match(r"^[A-Za-z]:", normalized) or ".." in parts:
        raise PipelineError("压缩包包含不安全路径")
    return Path(*parts)


def detect_mime(data: bytes, name: str) -> str:
    if data.startswith(b"PK\x03\x04"):
        if name.lower().endswith((".docx", ".pptx", ".xlsx")) or b"word/" in data[:4096] or b"ppt/" in data[:4096] or b"xl/" in data[:4096]:
            return "application/zip+ooxml"
        return "application/zip"
    if data.startswith(b"Rar!\x1A\x07"):
        return "application/vnd.rar"
    if data.startswith(b"7z\xBC\xAF\x27\x1C"):
        return "application/x-7z-compressed"
    if data.startswith(b"%PDF"):
        return "application/pdf"
    if data.startswith(b"\xD0\xCF\x11\xE0"):
        return "application/vnd.ms-office"
    if data.startswith(b"ID3") or data[:2] in (b"\xFF\xFB", b"\xFF\xF3", b"\xFF\xF2"):
        return "audio/mpeg"
    if data.startswith(b"RIFF") and data[8:12] == b"WAVE":
        return "audio/wav"
    if data.startswith(b"\xFF\xD8\xFF"):
        return "image/jpeg"
    if data.startswith(b"\x89PNG\r\n\x1A\n"):
        return "image/png"
    if data.startswith(b"GIF8"):
        return "image/gif"
    if data.startswith(b"\x1F\x8B"):
        return "application/gzip"
    if data[257:262] == b"ustar":
        return "application/x-tar"
    if data.lstrip().startswith(b"[InternetShortcut]"):
        return "text/internet-shortcut"
    return "application/octet-stream" if any(data.startswith(magic) for magic in (b"MZ", b"\x7fELF")) else "text/plain" if is_text(data) else "application/octet-stream"


def is_text(data: bytes) -> bool:
    if b"\x00" in data[:4096]:
        return False
    try:
        data[:4096].decode("utf-8")
        return True
    except UnicodeDecodeError:
        try:
            data[:4096].decode("gbk")
            return True
        except UnicodeDecodeError:
            return False


def decode_text(data: bytes) -> str:
    for encoding in ("utf-8-sig", "gbk"):
        try:
            return data.decode(encoding)
        except UnicodeDecodeError:
            continue
    return data.decode("utf-8", "replace")


def decode_zip_name(name: str, flag_bits: int = 0) -> str:
    """ZIP 未设置 UTF-8 标志时，国内压缩包常把 GBK 字节按 CP437 显示。"""
    if flag_bits & 0x800:
        return name
    try:
        return name.encode("cp437").decode("gbk")
    except (UnicodeEncodeError, UnicodeDecodeError):
        return name


def contains_personal_info(text: str) -> bool:
    return any(re.search(pattern, text) for pattern in PERSONAL_INFO_PATTERNS)


def url_shortcut_targets(data: bytes) -> list[str]:
    text = decode_text(data)
    targets = re.findall(r"(?im)^\s*URL\s*=\s*(\S+)", text)
    result = []
    for target in targets:
        parsed = urlparse(target)
        host = parsed.hostname or ""
        if parsed.scheme in SAFE_URL_SCHEMES and host and not (
            host in {"localhost", "127.0.0.1", "::1"} or host.startswith("10.")
            or host.startswith("192.168.") or re.fullmatch(r"172\.(?:1[6-9]|2\d|3[01])\..*", host)
            or re.fullmatch(r"[A-Za-z]", host)
        ):
            result.append(target)
        else:
            result.append("<blocked>")
    return result


def archive_tool() -> tuple[str, str]:
    for name in ("7z", "7za", "bsdtar", "tar"):
        path = shutil.which(name)
        if path:
            try:
                version = subprocess.run([path, "--version"], capture_output=True, timeout=10)
                detail = decode_text(version.stdout or version.stderr).splitlines()[0][:120]
            except (OSError, subprocess.SubprocessError):
                detail = "version unavailable"
            return path, "%s: %s" % (name, detail)
    raise PipelineError("未找到 7z/bsdtar/tar，无法安全检查该压缩包")


def external_tool_version(command: list[str]) -> str:
    try:
        result = subprocess.run(command, capture_output=True, timeout=15)
        return decode_text(result.stdout or result.stderr).splitlines()[0][:160] if (result.stdout or result.stderr) else "unknown"
    except (OSError, subprocess.SubprocessError):
        return "unavailable"


def category_for(name: str, mime: str) -> str:
    suffix = Path(name).suffix.lower()
    if suffix in ARCHIVE_SUFFIXES or mime in {"application/zip", "application/vnd.rar", "application/x-7z-compressed", "application/gzip", "application/x-tar"}:
        return "ARCHIVE"
    if suffix in {".doc", ".docx", ".pdf", ".txt"} and re.search(r"设计|反思|说课|教案|说明", name):
        return "DESIGN"
    if suffix in {".ppt", ".pptx"}:
        return "SLIDES"
    if re.search(r"任务单|学习单|练习|导学", name):
        return "WORKSHEET"
    if suffix in {".html", ".htm"}:
        return "TOOL"
    if mime.startswith(("image/", "audio/", "video/")) or suffix in {".mp3", ".wav", ".m4a", ".ogg", ".mp4", ".webm", ".jpg", ".jpeg", ".png", ".webp"}:
        return "MATERIAL"
    return "MATERIAL"


def display_title(name: str, course_title: str, lesson_no: int | None, category: str) -> str:
    path = Path(name)
    stem = path.stem
    if "教学设计" in stem or "反思" in stem:
        return ("第%s课《%s》教学设计" % (lesson_no, course_title)) if lesson_no else ("《%s》教学设计" % course_title)
    if "说课" in stem:
        return "《%s》说课稿" % course_title
    if category == "SLIDES":
        return stem if stem.startswith("《") else "%s（课件）" % stem
    if category == "WORKSHEET":
        return "《%s》学习任务单" % course_title
    if category == "ARCHIVE":
        return "%s（配套材料包）" % stem
    if category == "TOOL":
        return "%s（配套工具）" % stem
    return stem or ("《%s》配套素材" % course_title)


def stable_sort_no(category: str, index: int) -> int:
    base = {"DESIGN": 0, "SLIDES": 1000, "WORKSHEET": 2000, "TOOL": 3000, "MATERIAL": 4000, "ARCHIVE": 5000}[category]
    return base + index


def zip_entries(path: Path) -> list[dict[str, Any]]:
    with zipfile.ZipFile(path) as archive:
        entries = []
        for info in archive.infolist():
            data = archive.read(info) if not info.is_dir() and info.file_size <= 16 * 1024 * 1024 else b""
            decoded_name = decode_zip_name(info.filename, info.flag_bits)
            entries.append({
                "name": decoded_name,
                "rawName": info.filename,
                "bytes": info.file_size,
                "compressedBytes": info.compress_size,
                "modified": "%04d-%02d-%02d %02d:%02d:%02d" % info.date_time,
                "encrypted": bool(info.flag_bits & 0x1),
                "isDir": info.is_dir(),
                "mime": detect_mime(data, decoded_name) if data else "unknown",
                "data": data,
            })
        return entries


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def safe_relative(path: Path, root: Path) -> str:
    resolved = path.resolve()
    if not resolved.is_relative_to(root.resolve()):
        raise PipelineError("目标路径越过本次 run 目录")
    return resolved.relative_to(root.resolve()).as_posix()


def iter_files(root: Path) -> Iterable[Path]:
    return sorted(path for path in root.rglob("*") if path.is_file())
