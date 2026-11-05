"""2025宁波课例批次：只读读取两份汇总表（仅取署名/课名/学校/奖项字段）。"""
import json
import sys
from pathlib import Path

import openpyxl

SOURCE = Path(r"E:\2025年宁波市义务段“新课程背景下优秀课堂教学案例”（信息科技）")


def dump(path):
    book = openpyxl.load_workbook(path, data_only=True, read_only=True)
    result = {}
    for sheet in book.worksheets:
        rows = []
        for row in sheet.iter_rows(values_only=True):
            values = ["" if cell is None else str(cell).strip() for cell in row]
            if any(values):
                rows.append(values)
        result[sheet.title] = rows
    book.close()
    return result


def main():
    data = {}
    for relative in ["小学/小学案例汇总表.xlsx", "初中/初中优秀课堂教学案例汇总.xlsx"]:
        data[relative] = dump(SOURCE / relative)
    Path(sys.argv[1]).write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    for name, sheets in data.items():
        print("==", name)
        for sheet, rows in sheets.items():
            print("  sheet", sheet, "rows", len(rows))


if __name__ == "__main__":
    main()
