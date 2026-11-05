"""生成神经网络实验包发布副本，移除旧机地址，保留原档并记录差异。"""
import hashlib
import json
import re
import shutil
import zipfile
from pathlib import Path
from import_cases import digest

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"


def main():
    inventory = json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))
    source = next(Path(inventory["sourceRoot"]).rglob("人工智能中的神经网络实验工具集.zip"))
    target = OUT / "release-materials/神经网络实验工具集-课前配置版.zip"
    target.parent.mkdir(parents=True, exist_ok=True)
    temporary = target.with_suffix(".part")
    removed = []
    with zipfile.ZipFile(source) as original, zipfile.ZipFile(temporary, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=4) as release:
        for info in original.infolist():
            name = info.filename.replace("\\", "/")
            if ".." in Path(name).parts or name.startswith("/"):
                raise ValueError("压缩包包含越界路径")
            if name.endswith("/teacher.txt"):
                removed.append(name)
                continue
            if not info.is_dir() and info.file_size < 2 * 1024 * 1024 and Path(name).suffix.lower() in {".pem", ".json", ".yaml", ".yml", ".env"}:
                content = original.read(info)
                if b"PRIVATE KEY" in content or re.search(rb"sk-[a-zA-Z0-9_-]{24,}", content):
                    raise ValueError("检测到需人工复核的凭据，未生成发布包")
            with original.open(info) as incoming, release.open(info.filename, "w", force_zip64=True) as outgoing:
                shutil.copyfileobj(incoming, outgoing, length=1024 * 1024)
    temporary.replace(target)
    record = {"originalSha256": digest(source), "releaseSha256": digest(target), "bytes": target.stat().st_size,
        "removed": removed, "change": "仅移除两处旧教师机地址文件；程序代码和运行时未修改", "runtimeVerified": False}
    (OUT / "neural-tool-review.json").write_text(json.dumps(record, ensure_ascii=False, indent=2), encoding="utf-8")
    # 发布清单使用新副本根目录，避免修改原始来源树。
    manifests = json.loads((OUT / "samples-local-reviewed.json").read_text(encoding="utf-8"))
    neural = next(case for case in manifests["cases"] if case["sectionKey"] == "lesson-20")
    tool_case = dict(neural)
    tool_case["resources"] = [{"source": target.name, "sha256": record["releaseSha256"], "category": "TOOL",
        "title": "神经网络实验工具集", "preview": "", "description": "配合本课进行图像分类与神经网络实验。包含教师机启动器、学生端程序、训练图片、自带Python运行时和环境安装脚本。已移除旧教师机地址文件，首次启动需按说明配置本机网络。",
        "requirements": "Windows教师机与局域网学生机；首次需联网准备训练依赖，教师机服务使用3222端口。具体操作以包内README及迁移指南为准。本发布副本尚未在学校机房运行验证。"}]
    # 单独资源不能直接覆盖全课资源；供后续合并现有清单时取用。
    (OUT / "neural-tool-resource.json").write_text(json.dumps({"sourceRoot": str(target.parent), "case": tool_case}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"发布副本完成，移除{len(removed)}处旧地址；{target.stat().st_size}字节；SHA256={record['releaseSha256']}")


if __name__ == "__main__":
    main()
