from pathlib import Path

import pypdfium2 as pdfium
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(r"G:\教师工作资料\郑东旭教师荣誉")
OUTPUT = Path(__file__).parent / "honor-contact-sheets"
OUTPUT.mkdir(parents=True, exist_ok=True)

FONT_PATH = r"C:\Windows\Fonts\msyh.ttc"
font = ImageFont.truetype(FONT_PATH, 18)
small_font = ImageFont.truetype(FONT_PATH, 15)
files = sorted(ROOT.rglob("*.pdf"))


def fit_image(image: Image.Image, width: int, height: int) -> Image.Image:
    result = Image.new("RGB", (width, height), "white")
    image.thumbnail((width, height), Image.Resampling.LANCZOS)
    x = (width - image.width) // 2
    y = (height - image.height) // 2
    result.paste(image, (x, y))
    return result


for sheet_index in range((len(files) + 7) // 8):
    sheet = Image.new("RGB", (1600, 1200), "#e9eef5")
    draw = ImageDraw.Draw(sheet)
    for slot, path in enumerate(files[sheet_index * 8: sheet_index * 8 + 8]):
        document = pdfium.PdfDocument(str(path))
        page = document[0]
        bitmap = page.render(scale=1.3)
        rendered = bitmap.to_pil().convert("RGB")
        preview = fit_image(rendered, 350, 470)
        column = slot % 4
        row = slot // 4
        x = 25 + column * 395
        y = 20 + row * 590
        sheet.paste(preview, (x, y))
        relative = str(path.relative_to(ROOT))
        category = relative.split("\\", 1)[0]
        name = path.stem
        if len(name) > 28:
            name = name[:28] + "…"
        draw.text((x, y + 482), category, fill="#235a86", font=small_font)
        draw.text((x, y + 512), name, fill="#172333", font=font)
        draw.text((x, y + 547), f"PDF 第 1 页 / 共 {len(document)} 页", fill="#66788a", font=small_font)
        page.close()
        document.close()
    sheet.save(OUTPUT / f"honors-{sheet_index + 1}.png")

print(f"generated={len(list(OUTPUT.glob('*.png')))} files={len(files)} output={OUTPUT}")
