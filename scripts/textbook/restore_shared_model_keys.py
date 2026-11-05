"""按用户确认恢复三份资源的自愿共享模型Key，仅更新发布副本，不输出Key。"""
import json
import re
import shutil
import zipfile
from pathlib import Path
from build_web_packages import OUT, SOURCE, PACKAGES, PACKAGES_SPEC, digest, normalize_shared_agent_config, write_readme


def main():
    targets = {"w5-internet-ai-lab", "w19-campus-agent", "s11-animal-stamp"}
    manifest_path = OUT / "catalog-local-reviewed.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    records = []
    for spec in PACKAGES_SPEC:
        slug = spec["slug"]
        if slug not in targets:
            continue
        root = OUT / "shared-key-review" / slug
        root.mkdir(parents=True, exist_ok=True)
        replacements = {}
        if slug == "w5-internet-ai-lab":
            original = (SOURCE / spec["source"] / "index.html").read_text(encoding="utf-8")
            match = re.search(r"const DEEPSEEK_API_KEY = '[^']*';", original)
            if not match:
                raise RuntimeError("来源缺少共享Key声明")
            with zipfile.ZipFile(PACKAGES / (slug + ".zip")) as archive:
                text = archive.read("index.html").decode("utf-8")
            text = re.sub(r"const DEEPSEEK_API_KEY = '[^']*';[^\n]*", lambda _: match[0], text, count=1)
            replacements["index.html"] = text.encode("utf-8")
        elif slug == "s11-animal-stamp":
            original = SOURCE / spec["source"] / "src/.env"
            replacements["src/.env"] = original.read_bytes()
        else:
            originals = sorted((OUT / "staging").glob("*-w19-campus-agent/智能体/backend/config.json"))
            if not originals:
                raise RuntimeError("缺少已解包的原共享模型配置")
            (root / "backend").mkdir(exist_ok=True)
            shutil.copyfile(originals[0], root / "backend/config.json")
            with zipfile.ZipFile(PACKAGES / (slug + ".zip")) as archive:
                (root / "backend/config.example.json").write_bytes(archive.read("backend/config.example.json"))
            normalize_shared_agent_config(root)
            replacements["backend/config.json"] = (root / "backend/config.json").read_bytes()
        write_readme(root, spec)
        replacements["使用说明_平台版.txt"] = (root / "使用说明_平台版.txt").read_bytes()
        target = PACKAGES / (slug + ".zip")
        backup = OUT / "backups" / (slug + "-before-shared-key.zip")
        if not backup.exists():
            shutil.copyfile(target, backup)
        temporary = target.with_suffix(".part")
        with zipfile.ZipFile(target) as original, zipfile.ZipFile(temporary, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as release:
            for info in original.infolist():
                if info.filename in replacements:
                    continue
                with original.open(info) as incoming, release.open(info.filename, "w", force_zip64=True) as outgoing:
                    shutil.copyfileobj(incoming, outgoing, length=1024 * 1024)
            for name, content in replacements.items():
                release.writestr(name, content)
        temporary.replace(target)
        key = "packages/" + target.name
        resource = next(resource for case in manifest["cases"] for resource in case["resources"] if resource["source"] == key)
        resource.update(sha256=digest(target), requirements=spec["requirements"])
        records.append({"target": key, "bytes": target.stat().st_size, "sha256": resource["sha256"], "sharedKeyRestored": True})
        print(f"已恢复共享模型配置并更新说明：{slug}（未调用外部模型）")
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    web_path = OUT / "web-packages.json"
    web = json.loads(web_path.read_text(encoding="utf-8"))
    for item in web:
        record = next((record for record in records if record["target"] == item["target"]), None)
        if record:
            item.update(sha256=record["sha256"], bytes=record["bytes"], sharedKeyRestored=True)
    web_path.write_text(json.dumps(web, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUT / "shared-key-policy.json").write_text(json.dumps(records, ensure_ascii=False, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
