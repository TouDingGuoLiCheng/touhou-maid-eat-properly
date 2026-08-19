from PIL import Image, ImageDraw
from pathlib import Path

gui = Path(__file__).resolve().parents[1] / (
    "src/main/resources/assets/touhou_maid_eat_properly/textures/gui"
)
src = Image.open(gui / "snack_raid_allowed.png").convert("RGBA")
print("size", src.size, "mode", src.mode)

black = 0
trans = 0
for px in src.getdata():
    if px[3] == 0:
        trans += 1
    elif px[0] < 8 and px[1] < 8 and px[2] < 8:
        black += 1
print("transparent", trans, "near-black opaque", black)

cleaned = []
for r, g, b, a in src.getdata():
    if a == 0 or (r < 12 and g < 12 and b < 12 and a > 0):
        cleaned.append((0, 0, 0, 0))
    else:
        cleaned.append((r, g, b, a))
allowed = Image.new("RGBA", src.size)
allowed.putdata(cleaned)
allowed.save(gui / "snack_raid_allowed.png")

dis = allowed.copy()
dpix = []
for r, g, b, a in dis.getdata():
    if a == 0:
        dpix.append((0, 0, 0, 0))
    else:
        dpix.append((int(r * 0.55), int(g * 0.55), int(b * 0.55), a))
dis.putdata(dpix)

w, h = dis.size
margin = max(6, w // 8)

outline = Image.new("RGBA", dis.size, (0, 0, 0, 0))
od = ImageDraw.Draw(outline)
for t in range(-3, 4):
    od.line([(margin + t, margin), (w - 1 - margin + t, h - 1 - margin)], fill=(20, 10, 10, 200), width=5)
    od.line([(w - 1 - margin + t, margin), (margin + t, h - 1 - margin)], fill=(20, 10, 10, 200), width=5)

overlay = Image.new("RGBA", dis.size, (0, 0, 0, 0))
draw = ImageDraw.Draw(overlay)
for t in range(-2, 3):
    draw.line([(margin + t, margin), (w - 1 - margin + t, h - 1 - margin)], fill=(220, 40, 40, 255), width=3)
    draw.line([(w - 1 - margin + t, margin), (margin + t, h - 1 - margin)], fill=(220, 40, 40, 255), width=3)

out = Image.alpha_composite(dis, outline)
out = Image.alpha_composite(out, overlay)
out.save(gui / "snack_raid_disallowed.png")
print("wrote", gui / "snack_raid_allowed.png")
print("wrote", gui / "snack_raid_disallowed.png")
