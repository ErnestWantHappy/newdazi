"""把压缩后的预览PDF同步到本机运行目录并更新对应资源行，可重复执行。

只允许本机开发库与本机 TEXTBOOK_ROOT；旧预览在无任何资源引用时才删除。
"""
import argparse
import datetime
import json
import os
import re
import shutil
from pathlib import Path

import pymysql
import yaml

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--report", type=Path, default=OUT / "preview-compression.json")
    args = parser.parse_args()

    report = json.loads(args.report.read_text(encoding="utf-8"))
    changed = [item for item in report["items"] if item.get("compressed")]
    if not changed:
        print("没有需要同步的压缩结果")
        return

    root = os.environ.get("TEXTBOOK_ROOT")
    if not root:
        raise RuntimeError("必须设置 TEXTBOOK_ROOT 指向本机教材根目录")
    cases_root = Path(root).resolve() / "cases"
    for item in changed:
        old_path = cases_root / "previews" / (item["beforeSha256"] + ".pdf")
        new_path = cases_root / "previews" / (item["afterSha256"] + ".pdf")
        # 旧文件已清理且新文件就位，说明上一轮已同步完成。
        if not old_path.is_file() and not new_path.is_file():
            raise RuntimeError("运行目录缺少预览文件：" + str(old_path))

    master = yaml.safe_load((ROOT / "RuoYi-Vue/ruoyi-admin/src/main/resources/application-druid.yml").read_text(encoding="utf-8"))["spring"]["datasource"]["druid"]["master"]
    match = re.match(r"jdbc:mysql://([^:/]+):(\d+)/([^?]+)", master["url"])
    if not match or match[1] not in {"localhost", "127.0.0.1"} or match[3] != "xueyeceping_server_20260729":
        raise RuntimeError("拒绝非本机开发基线")
    connection = pymysql.connect(host=match[1], port=int(match[2]), user=master["username"],
                                 password=str(master["password"]), database=match[3], charset="utf8mb4")
    backup_rows = []
    try:
        with connection.cursor() as cursor:
            for item in changed:
                old_relative = "previews/" + item["beforeSha256"] + ".pdf"
                new_relative = "previews/" + item["afterSha256"] + ".pdf"
                cursor.execute("SELECT resource_id,case_id,filename,preview_path,bytes FROM biz_textbook_case_resource WHERE preview_path=%s", (old_relative,))
                rows = cursor.fetchall()
                if not rows:
                    # 已经同步过（例如重新导入后再次执行）就跳过，保持脚本可重复运行。
                    cursor.execute("SELECT COUNT(*) FROM biz_textbook_case_resource WHERE preview_path=%s", (new_relative,))
                    if cursor.fetchone()[0]:
                        item["runtimePreview"] = new_relative
                        item["updatedRows"] = 0
                        print(f"{item['case']} 已是压缩后预览，跳过")
                        continue
                    raise RuntimeError("库中没有引用该预览的资源：" + old_relative)
                target = cases_root / new_relative
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(Path(item["preview"]), target)
                if target.stat().st_size != item["afterBytes"]:
                    raise RuntimeError("复制后的预览大小不一致")
                for row in rows:
                    backup_rows.append({"resource_id": row[0], "case_id": row[1], "filename": row[2],
                                        "preview_path": row[3], "bytes": row[4]})
                cursor.execute("UPDATE biz_textbook_case_resource SET preview_path=%s WHERE preview_path=%s",
                               (new_relative, old_relative))
                item["runtimePreview"] = new_relative
                item["updatedRows"] = len(rows)
                print(f"{item['case']} {item['preview'].split(os.sep)[-1]} 更新{len(rows)}行 → {new_relative}")
            connection.commit()

            # 旧预览只有确认没有资源再引用才删除，避免误伤共享文件。
            for item in changed:
                old_relative = "previews/" + item["beforeSha256"] + ".pdf"
                cursor.execute("SELECT COUNT(*) FROM biz_textbook_case_resource WHERE preview_path=%s", (old_relative,))
                if cursor.fetchone()[0] == 0:
                    (cases_root / old_relative).unlink(missing_ok=True)
    except Exception:
        connection.rollback()
        raise
    finally:
        connection.close()

    backup = OUT / "backups" / ("preview-compress-" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S") + ".json")
    backup.parent.mkdir(parents=True, exist_ok=True)
    backup.write_text(json.dumps(backup_rows, ensure_ascii=False, indent=2), encoding="utf-8")
    args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"已同步{len(changed)}个预览，旧行备份 {backup.name}；未连接正式环境")


if __name__ == "__main__":
    main()
