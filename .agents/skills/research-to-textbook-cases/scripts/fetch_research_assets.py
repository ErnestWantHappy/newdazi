"""下载指定帖子的私有附件与教育云盘资源到本次 run 的 staging，不写正式库。"""
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
import time
from pathlib import Path
from urllib.parse import urlparse

import requests

from common import (
    OUTPUT_ROOT, PipelineError, RESEARCH_PRIVATE_ROOT, cloud_api, cloud_session,
    ensure_run, mysql_readonly, parse_json_lines, read_json, remote_connect,
    safe_relative, sha256_file, trusted_download_url, utc_now, validate_cloud_url,
    write_json,
)


PRIVATE_SQL = r"""
SELECT JSON_OBJECT(
  'resourceId', r.resource_id,
  'postId', r.post_id,
  'name', r.original_file_name,
  'storedPath', r.stored_path,
  'size', r.file_size,
  'updatedAt', DATE_FORMAT(COALESCE(r.update_time,r.create_time), '%Y-%m-%d %H:%i:%s')
)
FROM biz_research_resource r
WHERE r.del_flag='0' AND r.resource_type='F' AND r.post_id IN (__POST_IDS__);
"""

CLOUD_SQL = r"""
SELECT JSON_OBJECT(
  'resourceId', r.resource_id,
  'postId', r.post_id,
  'linkUrl', r.link_url,
  'extractCode', r.extract_code,
  'updatedAt', DATE_FORMAT(COALESCE(r.update_time,r.create_time), '%Y-%m-%d %H:%i:%s')
)
FROM biz_research_resource r
WHERE r.del_flag='0' AND r.resource_type='L' AND r.post_id IN (__POST_IDS__);
"""


def relative_private_path(value: str) -> str:
    normalized = value.replace("\\", "/").lstrip("/")
    if not normalized.startswith("research-activity/"):
        normalized = "research-activity/" + normalized
    if ".." in normalized.split("/") or normalized.startswith("/"):
        raise PipelineError("私有资源路径不安全")
    return RESEARCH_PRIVATE_ROOT.rstrip("/") + "/" + normalized[len("research-activity/"):]


def download_private(client: Any, row: dict, run_path: Path, post_id: int) -> dict:
    remote_path = relative_private_path(row["storedPath"])
    name = Path(row["name"] or ("resource-%s.bin" % row["resourceId"])).name
    local = run_path / "source" / ("post-%d" % post_id) / "private" / name
    local.parent.mkdir(parents=True, exist_ok=True)
    expected = int(row["size"] or 0)
    if local.exists() and local.stat().st_size == expected:
        digest = sha256_file(local)
        state = "RESUMED"
    else:
        sftp = client.open_sftp()
        try:
            sftp.get(remote_path, str(local))
        finally:
            sftp.close()
        digest = sha256_file(local)
        state = "DOWNLOADED"
        if expected and local.stat().st_size != expected:
            raise PipelineError("私有附件大小与数据库不一致：resourceId=%s" % row["resourceId"])
    return {
        "postId": post_id,
        "origin": "PRIVATE",
        "resourceId": int(row["resourceId"]),
        "name": name,
        "bytes": local.stat().st_size,
        "sha256": digest,
        "stagingPath": safe_relative(local, run_path),
        "state": state,
        "updatedAt": row["updatedAt"],
    }


def cloud_tree(session: requests.Session, origin: str, link_id: str, extract_code: str,
               root: dict, prefix: str = "", depth: int = 0) -> list[dict]:
    if depth > 4:
        raise PipelineError("云盘目录层级超过安全上限")
    listing = cloud_api(session, origin, "listdir", {
        "link": link_id,
        "password": extract_code,
        "docid": root["docid"],
        "by": "name",
        "sort": "asc",
    })
    items = []
    for directory in listing.get("dirs", []):
        name = str(directory.get("name", ""))
        child_prefix = (prefix + "/" + name).strip("/")
        items.append({"name": child_prefix, "bytes": None, "docid": directory["docid"], "isDir": True})
        items.extend(cloud_tree(session, origin, link_id, extract_code, directory, child_prefix, depth + 1))
    for item in listing.get("files", []):
        name = str(item.get("name", ""))
        items.append({
            "name": (prefix + "/" + name).strip("/"),
            "bytes": int(item.get("size", 0)),
            "docid": item["docid"],
            "isDir": False,
        })
    return items


def download_cloud_file(session: requests.Session, origin: str, link_id: str, extract_code: str,
                        item: dict, run_path: Path, post_id: int) -> dict:
    relative = Path(item["name"])
    local = run_path / "source" / ("post-%d" % post_id) / "cloud" / relative
    local.parent.mkdir(parents=True, exist_ok=True)
    expected = int(item["bytes"] or 0)
    if local.exists() and local.stat().st_size == expected:
        return {"name": item["name"], "bytes": expected, "sha256": sha256_file(local),
                "stagingPath": safe_relative(local, run_path), "state": "RESUMED"}
    metadata = cloud_api(session, origin, "osdownload", {
        "link": link_id,
        "password": extract_code,
        "docid": item["docid"],
        "reqhost": urlparse(origin).hostname,
        "usehttps": True,
        "savename": relative.name,
    })
    authrequest = metadata.get("authrequest")
    if not isinstance(authrequest, list) or len(authrequest) < 2:
        raise PipelineError("云盘下载地址缺失")
    url = trusted_download_url(authrequest[1])
    temp = local.with_suffix(local.suffix + ".part")
    digest = hashlib.sha256()
    retried = False
    for attempt in range(3):
        try:
            with session.get(url, stream=True, timeout=(15, 120), allow_redirects=True) as response:
                response.raise_for_status()
                final_host = urlparse(response.url).hostname
                if final_host not in {"yun.xsedu.net.cn"}:
                    raise PipelineError("下载重定向到非受信域名")
                with temp.open("wb") as handle:
                    for chunk in response.iter_content(1024 * 1024):
                        if chunk:
                            handle.write(chunk)
                            digest.update(chunk)
            break
        except (requests.RequestException, PipelineError):
            if attempt == 2:
                temp.unlink(missing_ok=True)
                raise
            retried = True
            time.sleep(2 ** attempt)
    actual = temp.stat().st_size
    if expected and actual != expected:
        temp.unlink(missing_ok=True)
        raise PipelineError("云盘文件大小与目录清单不一致：%s" % item["name"])
    temp.replace(local)
    return {
        "name": item["name"],
        "bytes": actual,
        "sha256": digest.hexdigest(),
        "stagingPath": safe_relative(local, run_path),
        "state": "DOWNLOADED" if not retried else "RETRIED",
        "expectedBytes": expected,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--post-ids", required=True)
    args = parser.parse_args()
    post_ids = sorted({int(value) for value in args.post_ids.split(",") if value.strip()})
    placeholder = ",".join(str(value) for value in post_ids)
    run_path = ensure_run(args.run_id)
    scan = read_json(OUTPUT_ROOT / ("scan-%s.json" % args.run_id))
    fingerprint = {int(item["postId"]): item["postUpdatedAt"] for item in scan["posts"]}
    private_rows = parse_json_lines(mysql_readonly(PRIVATE_SQL.replace("__POST_IDS__", placeholder)))
    cloud_rows = parse_json_lines(mysql_readonly(CLOUD_SQL.replace("__POST_IDS__", placeholder)))
    assets: list[dict] = []
    jump, client, _ = remote_connect()
    try:
        for row in private_rows:
            assets.append({
                **download_private(client, row, run_path, int(row["postId"])),
                "postUpdatedAt": fingerprint.get(int(row["postId"])),
            })
    finally:
        client.close()
        jump.close()

    for row in cloud_rows:
        origin, link_id = validate_cloud_url(row["linkUrl"])
        session = cloud_session()
        session.get(row["linkUrl"], timeout=30)
        root = cloud_api(session, origin, "get", {"link": link_id, "password": row["extractCode"]})
        tree = cloud_tree(session, origin, link_id, row["extractCode"], root)
        for item in tree:
            if item["isDir"]:
                continue
            result = download_cloud_file(session, origin, link_id, row["extractCode"], item, run_path, int(row["postId"]))
            assets.append({
                **result,
                "postId": int(row["postId"]),
                "origin": "CLOUD",
                "resourceId": int(row["resourceId"]),
                "postUpdatedAt": fingerprint.get(int(row["postId"])),
            })
    output = run_path / "reports" / "assets.json"
    write_json(output, {"generatedAt": utc_now(), "postIds": post_ids, "assets": assets})
    print(json.dumps({"runId": args.run_id, "report": str(output), "assets": len(assets),
                      "bytes": sum(item["bytes"] or 0 for item in assets)}, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
