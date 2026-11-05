"""把已核对署名的文档整理成可入本机清单，保留所有来源目录的处理去向。"""
import json
import re
import shutil
from collections import Counter
from pathlib import Path
from import_cases import digest
from review_case_catalog import text

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output/textbook-cases"
# 对应本次已保存的53目录清单，仍需用文件内署名验证，序号本身不构成审核依据。
AUTHORS = ["王纪宇", "张莹洁", "", "史习阳", "鲍鑫扬", "周旺琼", "夏吕丹", "郑瑾", "叶晓静", "黄芳芳", "朱屹", "郑东旭", "罗洁", "黄意", "卢旭浩", "岑姝锌", "张楚人", "王诗莹", "陈凯", "郑琦", "吴康乐", "田甜", "杨晶茹", "仇蓓蓓", "张红波", "姚新淼", "傅迪", "袁立玺", "史家乐", "臧天赐", "黄素媛", "黄素媛", "郭璐文", "沈倩", "章晓伟", "林珠", "周健", "张维春", "杨滨", "王素军", "侯娜飞", "黄乐帅", "姚静", "李炯荣", "史敏敏", "薛心怡", "郑榴红", "章敬洋", "金辉", "童妮娜", "曾瑞恒", "黄钡钡", "徐静莹"]
SCHOOLS = {"王纪宇": "宁波大学青藤书院", "张莹洁": "宁波大学青藤书院", "史习阳": "宁波市洪塘中学",
    "鲍鑫扬": "宁波市江北区外国语艺术学校", "周旺琼": "宁波福山正达外国语学校", "夏吕丹": "慈溪市文锦书院",
    "郑瑾": "象山县丹城中学", "叶晓静": "象山县丹山书院", "黄芳芳": "象山县文峰学校", "朱屹": "塔山中学",
    "郑东旭": "象山县大目湾实验学校", "罗洁": "宁波高新区信懋小学", "黄意": "求精书院",
    "卢旭浩": "宁波前湾新区世纪城实验小学", "岑姝锌": "宁波前湾新区海智小学", "张楚人": "宁波前湾新区海韵小学",
    "王诗莹": "宁波前湾新区滨海学校", "陈凯": "慈溪市庵东镇中心学校", "郑琦": "象山县丹城中学",
    "吴康乐": "宁海县大佳何镇中心小学", "田甜": "宁海县岔路镇中心小学", "杨晶茹": "金阳小学",
    "仇蓓蓓": "宁波市奉化区江口街道中心小学", "张红波": "上海师范大学附属宁波实验学校", "姚新淼": "宁波市奉化区实验小学",
    "袁立玺": "宁波市奉化区江口街道南浦小学", "史家乐": "宁波市北仑区大碶中学", "臧天赐": "应麟书院",
    "郭璐文": "鄞州区姜山镇中心初级中学", "沈倩": "慈溪市凤湖初级中学", "章晓伟": "宁波市镇海区实验小学",
    "林珠": "宁波市镇海区崇正书院", "周健": "镇海区静远小学", "张维春": "象山县实验小学",
    "杨滨": "宁波市镇海区中心学校", "王素军": "宁波市镇海区蛟川中心学校", "侯娜飞": "余姚东风小学教育集团东江校区",
    "黄乐帅": "余姚市泗门镇中心小学", "姚静": "余姚市兰江小学", "李炯荣": "浙江省余姚市梨洲小学",
    "史敏敏": "宁波市北仑区滨海新城实验学校", "薛心怡": "顾国和中学", "郑榴红": "宁波市北仑区柴桥实验小学",
    "章敬洋": "北仑区泰河学校", "金辉": "宁波市北仑区蔚斗小学", "童妮娜": "象山县文峰学校",
    "曾瑞恒": "慈溪市周巷镇精忠小学", "黄钡钡": "慈溪市文谷外国语小学", "徐静莹": "慈溪市明月书院"}


def main():
    if not (OUT / "preview-failures.json").exists():
        raise RuntimeError("全量预览任务尚未完成")
    data = json.loads((OUT / "catalog-prepared.json").read_text(encoding="utf-8"))
    if len(data["cases"]) != len(AUTHORS):
        raise RuntimeError("来源清单变化，必须重新核对署名映射")
    source = Path(data["sourceRoot"])
    inventory = json.loads((OUT / "inventory.json").read_text(encoding="utf-8"))
    original_resources = {case["sourceKey"]: case["resources"] for case in inventory["cases"]}
    stage_root = OUT / "release-materials/combined"
    signatures = {}
    ledger = []
    for case, author in zip(data["cases"], AUTHORS):
        evidence = re.sub(r"\s+", "", case["sourceDirectory"] + " ".join(resource.get("reviewExcerpt", "") + text(source / resource["source"])[:2500] for resource in case["resources"]))
        verified = bool(author and author in evidence)
        # 用户2026-11-03明确选择以真实来源说明收录该未署名课例。
        if case["sourceDirectory"] == "七年级/七年级上册/江北区-第18课《生成式人工智能》":
            author = "江北区展示课例（未署名）"
            verified = True
        ready = [resource for resource in case["resources"] if resource.get("previewStatus") == "READY"]
        assets = list(original_resources[case["sourceKey"]])
        for path in (source / case["sourceDirectory"]).iterdir():
            if path.is_file() and path.suffix.lower() in {".jpg", ".jpeg", ".png", ".webp", ".webm"}:
                assets.append({"source": path.relative_to(source).as_posix(), "sha256": digest(path), "bytes": path.stat().st_size})
        for asset in assets:
            path = source / asset["source"]
            extension = path.suffix.lower()
            depth = len(path.relative_to(source / case["sourceDirectory"]).parts)
            is_video = extension in {".mp4", ".webm"} and depth <= 3
            is_image = extension in {".jpg", ".jpeg", ".png", ".webp"} and depth == 1
            if not (is_video or is_image):
                continue
            if is_video:
                with path.open("rb") as stream:
                    header = stream.read(32)
                previewable = header[4:8] == b"ftyp" if extension == ".mp4" else header.startswith(b"\x1aE\xdf\xa3")
            else:
                from PIL import Image
                with Image.open(path) as image:
                    image.verify()
                previewable = True
            asset.update(category="MATERIAL", title=path.stem, preview=str(path) if previewable else "", previewStatus="READY" if previewable else "DOWNLOAD_ONLY",
                         description="本课配套演示视频" if is_video else "本课配套图片")
            ready.append(asset)
        school = SCHOOLS.get(author, "")
        # 署名来自真实目录或文档，学校还需在当前证据中逐字出现才写入。
        if school not in evidence:
            school = ""
        case.update(author=author, school=school, reviewed=verified and bool(ready), status="PUBLISHED" if verified and ready else "DRAFT")
        category_counts = Counter(resource["category"] for resource in ready)
        for resource in ready:
            if category_counts[resource["category"]] > 1 or "板书" in Path(resource["source"]).stem:
                resource["title"] = Path(resource["source"]).stem
        signature = (case["bookCode"], case["sectionKey"], author, tuple(sorted(resource["sha256"] for resource in ready)))
        if ready and signature in signatures:
            case.update(reviewed=False, status="DRAFT")
            disposition = "合并至 " + signatures[signature]
        elif not verified:
            disposition = "待核对署名"
        elif not ready:
            disposition = "待修复预览"
        else:
            signatures[signature] = case["sourceKey"]
            disposition = "本机文档可入库；实验/视频等材料另行审核"
        case["resources"] = ready
        if case["reviewed"]:
            for resource in ready:
                target = stage_root / resource["source"]
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(source / resource["source"], target)
        ledger.append({"source": case["sourceDirectory"], "sourceKey": case["sourceKey"], "author": author,
                       "bookCode": case["bookCode"], "sectionKey": case["sectionKey"], "result": disposition, "documents": len(ready)})
    # 合并已审查的神经网络发布包，沿用该课既有文档资源。
    tool = json.loads((OUT / "neural-tool-resource.json").read_text(encoding="utf-8"))
    neural = next(case for case in data["cases"] if case["sourceKey"] == tool["case"]["sourceKey"])
    resource = tool["case"]["resources"][0]
    tool_source = Path(tool["sourceRoot"]) / resource["source"]
    resource["source"] = "prepared/" + tool_source.name
    target = stage_root / resource["source"]
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(tool_source, target)
    neural["resources"].append(resource)
    data["sourceRoot"] = str(stage_root)
    (OUT / "catalog-local-reviewed.json").write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUT / "source-ledger.json").write_text(json.dumps(ledger, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"可入本机{sum(c['reviewed'] for c in data['cases'])}份；全部{len(ledger)}目录已有去向；未写库")


if __name__ == "__main__":
    main()
