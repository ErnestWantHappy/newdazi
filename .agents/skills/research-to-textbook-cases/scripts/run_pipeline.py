"""教研活动课例导入统一入口；默认 dry-run，apply 才允许正式写入。"""
from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path

from common import OUTPUT_ROOT, PipelineError, now_id, read_json, run_dir, write_json


SCRIPTS = Path(__file__).resolve().parent


def call(script: str, *args: str) -> None:
    command = [sys.executable, "-X", "utf8", str(SCRIPTS / script), *args]
    completed = subprocess.run(command, cwd=str(SCRIPTS.parents[3]))
    if completed.returncode:
        raise PipelineError("%s 执行失败，退出码 %d" % (script, completed.returncode))


def main() -> int:
    parser = argparse.ArgumentParser(description="教研活动资源整理为电子教材与优秀课例")
    parser.add_argument("command", choices=["scan", "plan", "stage", "apply", "verify"])
    parser.add_argument("--run-id", default="")
    parser.add_argument("--post-ids", default="")
    parser.add_argument("--after-post-id", type=int, default=0)
    parser.add_argument("--manifest", default="")
    parser.add_argument("--confirm-low-peak-window", action="store_true")
    parser.add_argument("--publish", action="store_true")
    args = parser.parse_args()
    run_id = args.run_id or now_id()

    if args.command == "scan":
        call("scan_research_resources.py", "--run-id", run_id,
             "--after-post-id", str(args.after_post_id), "--post-ids", args.post_ids)
    elif args.command == "plan":
        if not (run_dir(run_id) / "reports" / "assets.json").exists():
            call("scan_research_resources.py", "--run-id", run_id, "--post-ids", args.post_ids)
        call("resolve_textbook_section.py", "--run-id", run_id)
    elif args.command == "stage":
        post_ids = args.post_ids or "15,60,70"
        call("scan_research_resources.py", "--run-id", run_id, "--post-ids", post_ids)
        call("resolve_textbook_section.py", "--run-id", run_id)
        call("fetch_research_assets.py", "--run-id", run_id, "--post-ids", post_ids)
        call("inspect_archive.py", "--run-id", run_id)
        call("build_resources.py", "--run-id", run_id)
        call("convert_previews.py", "--run-id", run_id)
        call("build_archive_summary.py", "--run-id", run_id)
        call("build_import_manifest.py", "--run-id", run_id)
    elif args.command == "apply":
        manifest = args.manifest or str(run_dir(run_id) / "manifest.json")
        extra = ["--confirm-low-peak-window"] if args.confirm_low_peak_window else []
        if args.publish:
            extra.append("--publish")
        call("apply_import.py", "--manifest", manifest, *extra)
    elif args.command == "verify":
        manifest = args.manifest or str(run_dir(run_id) / "manifest.json")
        call("verify_import.py", "--manifest", manifest)
    cursor = OUTPUT_ROOT / "last-success.json"
    if args.command in {"apply", "verify"}:
        write_json(cursor, {"runId": run_id, "command": args.command})
    print("RUN_ID=%s COMMAND=%s" % (run_id, args.command))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
