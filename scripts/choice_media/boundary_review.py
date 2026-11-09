"""本机真实数据库边界回归；所有新批次立即登记，不在生产运行。"""
import io
import json
import sys
import uuid
from pathlib import Path

import requests
from openpyxl import Workbook
from openpyxl.drawing.image import Image as SheetImage
from openpyxl.drawing.spreadsheet_drawing import AnchorMarker, TwoCellAnchor
from PIL import Image

from migrate_local import ROOT, connect

BASE = "http://127.0.0.1:18080"
OUT = ROOT / "output/choice-media"


def main():
    database, _ = connect()
    database.close()
    session = requests.Session()
    session.headers["Authorization"] = "Bearer " + json.loads(
        (OUT / "teacher-session.local.json").read_text(encoding="utf-8")
    )["token"]
    manifest = json.loads((OUT / "review-data.json").read_text(encoding="utf-8"))
    checks = []

    def register(batch):
        manifest["batchIds"] = sorted(set(manifest["batchIds"] + [int(batch["batchId"])]))
        for item in batch["items"]:
            media = item["draft"]["question"]["media"]
            references = media["stem"] + sum(media["options"].values(), [])
            references += item["draft"]["pendingImages"]
            manifest["imageIds"] += [int(image["imageId"]) for image in references]
        manifest["imageIds"] = sorted(set(manifest["imageIds"]))
        (OUT / "review-data.json").write_text(json.dumps(manifest), encoding="utf-8")

    def call(method, path, expected=200, **kwargs):
        response = session.request(method, BASE + path, timeout=90, **kwargs)
        data = response.json()
        assert data.get("code") == expected, (path, data)
        return data.get("data")

    orphan = call("GET", "/business/question/import-batches/16")
    register(orphan)
    checks.append("补登记遗留批次16及其全部图片")
    book = Workbook()
    sheet = book.active
    sheet.append(["题目类型", "题目内容", "选项A", "选项B", "选项C", "选项D", "标准答案"])
    for index in range(3):
        sheet.append(["选择题", f"【图片边界验收】题{index}", "甲", "乙", "丙", "丁", "A"])
    for offset in [0, 10000]:
        pixels = io.BytesIO()
        Image.new("RGB", (80, 60), "#337ecc").save(pixels, format="PNG")
        pixels.seek(0)
        image = SheetImage(pixels)
        image.anchor = TwoCellAnchor(
            _from=AnchorMarker(col=1, row=1, colOff=offset),
            to=AnchorMarker(col=3, row=2),
        )
        sheet.add_image(image)
    source = io.BytesIO()
    book.save(source)
    fixture = OUT / "fixtures/ambiguous-repeated-images.xlsx"
    fixture.write_bytes(source.getvalue())

    def upload():
        batch = call(
            "POST", "/business/question/import-batches",
            files={"file": ("ambiguous.xlsx", source.getvalue())},
            data={"requestId": uuid.uuid4().hex, "allowDuplicate": "true"},
        )
        register(batch)
        return batch

    batch = upload()
    origin, target = batch["items"][:2]
    pending = origin["draft"]["pendingImages"]
    assert len(pending) == 2
    assert pending[0]["imageId"] == pending[1]["imageId"]
    assert pending[0]["pendingId"] != pending[1]["pendingId"]
    checks.append("真实跨格同图重复实例生成不同pendingId")
    endpoint = f'/business/question/import-batches/{batch["batchId"]}/items/{origin["itemId"]}'
    placements = [
        {**image, "slot": slot, "targetItemId": int(target["itemId"])}
        for image, slot in zip(pending, ["A", "B"])
    ]
    call("PATCH", endpoint, expected=500, json={
        "expectedRevision": batch["revision"],
        "placements": [{**placements[0], "imageId": "999999999"}],
    })
    unchanged = call("GET", f'/business/question/import-batches/{batch["batchId"]}')
    assert unchanged == batch
    checks.append("pendingId与imageId不匹配拒绝且草稿不变")
    updated = call("PATCH", endpoint, json={
        "expectedRevision": batch["revision"], "placements": placements,
    })
    media = updated["items"][1]["draft"]["question"]["media"]
    assert media["options"]["A"][0]["imageId"] == pending[0]["imageId"]
    assert media["options"]["B"][0]["imageId"] == pending[1]["imageId"]
    assert not updated["items"][0]["draft"]["pendingImages"]
    checks.append("同一请求两图跨题移动至同一目标题A/B均保留")
    call("PATCH", endpoint, expected=500, json={
        "expectedRevision": batch["revision"], "included": False,
    })
    assert call("GET", f'/business/question/import-batches/{batch["batchId"]}') == updated
    checks.append("陈旧revision拒绝且不覆盖较新草稿")
    batch = upload()
    origin, target = batch["items"][:2]
    pending = origin["draft"]["pendingImages"]
    endpoint = f'/business/question/import-batches/{batch["batchId"]}/items/{origin["itemId"]}'
    placements = [
        {**image, "slot": "A", "targetItemId": int(target["itemId"])}
        for image in pending
    ]
    call("PATCH", endpoint, expected=500, json={
        "expectedRevision": batch["revision"], "placements": placements,
    })
    assert call("GET", f'/business/question/import-batches/{batch["batchId"]}') == batch
    checks.append("第二图目标容量满时第一图移动与pending移除一起回滚")
    placements[1]["discard"] = True
    updated = call("PATCH", endpoint, json={
        "expectedRevision": batch["revision"], "placements": placements,
    })
    assert not updated["items"][0]["draft"]["pendingImages"]
    assert len(updated["items"][1]["draft"]["question"]["media"]["options"]["A"]) == 1
    checks.append("同一原题重复图片分别移动和丢弃互不干扰")
    result = {"passed": len(checks), "checks": checks, "scope": "本机18080/本机MySQL；合成普通嵌入图，非真实WPS兼容证明"}
    (OUT / "boundary-result-20261108.json").write_text(
        json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()
