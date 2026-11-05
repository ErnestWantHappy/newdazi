"""为全量课例生成PDF预览及异常清单，可中断后重跑，原始文件保持只读。"""
import json
import subprocess
import tempfile
from pathlib import Path
import fitz
from compress_case_previews import rasterize
from review_case_catalog import text

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
# 与后端预览上限一致：超过就重采样，避免生成完再被接口拒绝。
PREVIEW_LIMIT = 100 * 1024 * 1024


def main():
    data = json.loads((OUT / "catalog-candidates.json").read_text(encoding="utf-8"))
    source = Path(data["sourceRoot"])
    failures = []
    completed = 0
    for case in data["cases"]:
        resources = []
        hashes = set()
        for resource in case["resources"]:
            path = source / resource["source"]
            if path.suffix.lower() not in {".doc", ".docx", ".ppt", ".pptx", ".pdf"}:
                continue
            if len(path.relative_to(source / case["sourceDirectory"]).parts) > 3:
                continue
            if resource["sha256"] in hashes:
                continue
            hashes.add(resource["sha256"])
            if path.suffix.lower() == ".pdf" and (path.with_suffix(".docx").exists() or path.with_suffix(".doc").exists()):
                continue
            try:
                if path.suffix.lower() == ".pdf":
                    preview = path
                else:
                    destination = OUT / "previews" / resource["sha256"]
                    destination.mkdir(parents=True, exist_ok=True)
                    preview = destination / (path.stem + ".pdf")
                    if not preview.exists():
                        with tempfile.TemporaryDirectory(prefix="case-office-") as profile:
                            result = subprocess.run([r"C:/Program Files/LibreOffice/program/soffice.exe",
                                "-env:UserInstallation=" + Path(profile).as_uri(), "--headless", "--convert-to", "pdf",
                                "--outdir", str(destination), str(path)], stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=120)
                            if result.returncode or not preview.exists():
                                raise RuntimeError("Office转换失败")
                with fitz.open(preview) as document:
                    if not len(document):
                        raise RuntimeError("PDF为空")
                    resource["pages"] = len(document)
                    content = " ".join(document[i].get_text() for i in range(min(2, len(document))))
                    resource["reviewExcerpt"] = content[:1200]
                    thumbnail = OUT / "review" / (resource["sha256"] + ".png")
                    if not thumbnail.exists():
                        thumbnail.parent.mkdir(parents=True, exist_ok=True)
                        document[0].get_pixmap(matrix=fitz.Matrix(0.7, 0.7)).save(thumbnail)
                # 摘要和缩略图取完再压缩；只处理工作副本，来源PDF不动。
                if preview.is_relative_to(OUT / "previews") and preview.stat().st_size > PREVIEW_LIMIT:
                    rasterize(preview, preview, 1920, 85)
                name = path.stem
                if path.suffix.lower() in {".pptx", ".ppt"} or "课件" in name or "PPT" in name.upper():
                    category = "SLIDES"
                elif "教学设计" in content[:1200] or "教学设计" in name or "课标内容" in content[:1200]:
                    category = "DESIGN"
                elif any(word in name for word in ["任务", "学单", "导学", "学习单", "评价"]):
                    category = "WORKSHEET"
                else:
                    category = "MATERIAL"
                resource.update(category=category, title={"DESIGN": "教学设计", "SLIDES": "教学课件", "WORKSHEET": path.stem, "MATERIAL": path.stem}[category],
                                preview=str(preview), previewStatus="READY")
                completed += 1
                print(f"完成 {completed} {case['title']} {path.name} {resource['pages']}页", flush=True)
            except Exception as error:
                resource.update(previewStatus="FAILED", preview="")
                failures.append({"source": resource["source"], "reason": type(error).__name__})
                print(f"待修复 {path.name} {type(error).__name__}", flush=True)
            resources.append(resource)
        case["resources"] = resources
        # 每完成一个目录就保存当前事实，长任务中断也能接管。
        (OUT / "catalog-prepared.json").write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUT / "preview-failures.json").write_text(json.dumps(failures, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"预览准备完成{completed}件，失败{len(failures)}件；仍待署名和内容审核，未入库", flush=True)


if __name__ == "__main__":
    main()
