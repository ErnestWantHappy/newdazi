"""准备本机文档样板与隔离Office预览，实验程序留待逐包审核。"""
import json
import subprocess
import tempfile
from pathlib import Path
import fitz

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"


def main():
    data = json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))
    source = Path(data["sourceRoot"])
    samples = []
    for case in data["cases"]:
        directory = case["sourceDirectory"]
        if "岔路小学-第11课" in directory:
            author, school, book, section = "田甜", "岔路小学", "xxkj-5-0-2026", "lesson-11"
        elif "金阳小学-第11课" in directory:
            author, school, book, section = "杨晶茹", "金阳小学", "xxkj-5-0-2026", "lesson-11"
        elif "象山县-第20课《人工智能中的神经网络》" in directory:
            author, school, book, section = "朱屹", "", "xxkj-7-1-2026", "lesson-20"
        else:
            continue
        resources = []
        for resource in case["resources"]:
            path = source / resource["source"]
            if path.parent != source / directory or path.suffix.lower() not in {".docx", ".pptx", ".pdf"}:
                continue
            name = path.stem
            category = "SLIDES" if path.suffix.lower() == ".pptx" or any(word in name.upper() for word in ["PPT", "课件"]) else "DESIGN" if "设计" in name or "依托数据建模" in name else "WORKSHEET"
            # 作者同时提供同名PDF和Word时仅列Word一项，避免同一教学设计重复出现。
            if path.suffix.lower() == ".pdf" and path.with_suffix(".docx").exists():
                continue
            resource.update(category=category, title={"DESIGN": "教学设计", "SLIDES": "教学课件", "WORKSHEET": "任务单"}[category])
            if path.suffix.lower() == ".pdf":
                preview = path
            else:
                destination = OUT / "previews" / resource["sha256"]
                destination.mkdir(parents=True, exist_ok=True)
                preview = destination / (path.stem + ".pdf")
                if not preview.exists():
                    # 独立用户目录防止复用正在运行的办公软件进程和写入用户配置。
                    with tempfile.TemporaryDirectory(prefix="case-office-") as profile:
                        result = subprocess.run([r"C:/Program Files/LibreOffice/program/soffice.exe",
                            "-env:UserInstallation=" + Path(profile).as_uri(), "--headless", "--convert-to", "pdf",
                            "--outdir", str(destination), str(path)], stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=120)
                        if result.returncode or not preview.exists():
                            raise RuntimeError("预览生成失败：" + path.name)
            with fitz.open(preview) as document:
                if not len(document):
                    raise RuntimeError("预览为空")
                print(f"{author} {resource['title']}：{len(document)}页")
                # 供人工核对第一页版面，尚不代表逐页校对通过。
                thumbnail = OUT / "review" / (resource["sha256"] + ".png")
                thumbnail.parent.mkdir(parents=True, exist_ok=True)
                document[0].get_pixmap(matrix=fitz.Matrix(0.8, 0.8)).save(thumbnail)
            resource["preview"] = str(preview)
            resources.append(resource)
        case.update(bookCode=book, sectionKey=section, author=author, school=school,
                    title="认识决策树" if section == "lesson-11" else "人工智能中的神经网络",
                    resources=resources, reviewed=False, status="DRAFT")
        samples.append(case)
    (OUT / "samples.json").write_text(json.dumps({"sourceRoot": str(source), "cases": samples}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"已准备{len(samples)}份样板，仍为待审核草稿，未执行入库")


if __name__ == "__main__":
    main()
