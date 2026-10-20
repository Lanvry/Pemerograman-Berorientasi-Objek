import os
import math
from PIL import Image, ImageDraw, ImageFont
from reportlab.lib.pagesizes import letter, landscape
from reportlab.platypus import SimpleDocTemplate, Image as RLImage

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_PNG = os.path.join(SCRIPT_DIR, "UseCaseDiagram_SmartParkir.png")
OUTPUT_PDF = os.path.join(SCRIPT_DIR, "UseCaseDiagram_SmartParkir.pdf")

def draw_stick_figure(draw, cx, cy, label, font, line_color="#000000", line_width=2):
    # cx, cy is the top anchor (head center)
    head_r = 16
    # 1. Head
    draw.ellipse([(cx - head_r, cy - head_r), (cx + head_r, cy + head_r)], fill="#ffffff", outline=line_color, width=line_width)

    # 2. Body
    body_top = cy + head_r
    body_bot = body_top + 45
    draw.line([(cx, body_top), (cx, body_bot)], fill=line_color, width=line_width)

    # 3. Arms
    arm_y = body_top + 16
    draw.line([(cx - 28, arm_y + 16), (cx, arm_y)], fill=line_color, width=line_width)
    draw.line([(cx + 28, arm_y + 16), (cx, arm_y)], fill=line_color, width=line_width)

    # 4. Legs
    leg_len = 45
    draw.line([(cx, body_bot), (cx - 24, body_bot + leg_len)], fill=line_color, width=line_width)
    draw.line([(cx, body_bot), (cx + 24, body_bot + leg_len)], fill=line_color, width=line_width)

    # 5. Label below feet
    label_y = body_bot + leg_len + 8
    lines = label.split("\n")
    curr_y = label_y
    for l in lines:
        tw = draw.textlength(l, font=font)
        draw.text((cx - tw / 2, curr_y), l, fill="#000000", font=font)
        curr_y += 18

    # Return center of figure for connecting lines
    return (cx, cy + 30)

def draw_usecase_oval(draw, cx, cy, w, h, text, font, font_bold=None, bg_color="#ffffff", outline_color="#000000", width=2):
    # Oval bounding box
    bbox = [cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2]
    draw.ellipse(bbox, fill=bg_color, outline=outline_color, width=width)

    lines = text.split("\n")
    total_h = len(lines) * 20
    curr_y = cy - total_h / 2 + 2

    for line in lines:
        use_font = font_bold if font_bold else font
        tw = draw.textlength(line, font=use_font)
        draw.text((cx - tw / 2, curr_y), line, fill="#000000", font=use_font)
        curr_y += 20

    return (cx, cy, w, h)

def get_oval_edge_point(cx, cy, w, h, target_x, target_y):
    # Calculates the point on the perimeter of the ellipse towards (target_x, target_y)
    dx = target_x - cx
    dy = target_y - cy
    if dx == 0 and dy == 0:
        return (cx, cy)
    angle = math.atan2(dy, dx)
    # Parametric ellipse point
    a = w / 2.0
    b = h / 2.0
    px = cx + a * math.cos(angle)
    py = cy + b * math.sin(angle)
    return (px, py)

def create_use_case_diagram():
    W, H = 1350, 1050
    img = Image.new("RGB", (W, H), "#ffffff")
    draw = ImageDraw.Draw(img)

    font_path_bold = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
    font_path_regular = "/System/Library/Fonts/Supplemental/Arial.ttf"
    font_path_italic = "/System/Library/Fonts/Supplemental/Arial Italic.ttf"
    if not os.path.exists(font_path_bold):
        font_path_bold = "/System/Library/Fonts/Supplemental/Arial.ttf"

    font_title = ImageFont.truetype(font_path_bold, 18)
    font_sub = ImageFont.truetype(font_path_regular, 13)
    font_actor = ImageFont.truetype(font_path_bold, 14)
    font_actor_sub = ImageFont.truetype(font_path_regular, 12)
    font_subsystem = ImageFont.truetype(font_path_bold, 16)
    font_stereo = ImageFont.truetype(font_path_regular, 13)
    font_uc = ImageFont.truetype(font_path_bold, 14)
    font_uc_reg = ImageFont.truetype(font_path_regular, 13)

    # Subsystem boundary box (Center)
    sub_x1 = 340
    sub_y1 = 80
    sub_x2 = 980
    sub_y2 = 970
    draw.rectangle([sub_x1, sub_y1, sub_x2, sub_y2], fill="#ffffff", outline="#000000", width=2)

    # Subsystem label top left inside box
    draw.text((sub_x1 + 18, sub_y1 + 14), "«subsystem»", fill="#000000", font=font_stereo)
    draw.text((sub_x1 + 18, sub_y1 + 34), "Smart Parking System", fill="#000000", font=font_subsystem)

    # Left Actors
    # Actor 1: Pengendara (Customer / Driver)
    pt_pengendara = draw_stick_figure(draw, cx=170, cy=240, label="Pengendara\n(Customer)", font=font_actor)

    # Actor 2: Teknisi / Petugas Lapangan (Technician)
    pt_teknisi = draw_stick_figure(draw, cx=170, cy=740, label="Teknisi Parkir\n(Technician)", font=font_actor)

    # Right Actors
    # Actor 3: Bank / Payment Gateway
    pt_bank = draw_stick_figure(draw, cx=1160, cy=440, label="Bank / E-Money\nGateway", font=font_actor)

    # Actor 4: Operator / Admin Parkir
    pt_operator = draw_stick_figure(draw, cx=1160, cy=780, label="Operator Parkir\n(Admin)", font=font_actor)

    # Use Cases in Subsystem (Vertical Stack of Ovals)
    uc_center_x = (sub_x1 + sub_x2) / 2
    uc_w = 260
    uc_h = 75

    use_cases = [
        # (id, cy, text, w, h)
        ("uc1", 160, "Check-In &\nAmbil Karcis Masuk", 240, 70),
        ("uc2", 280, "Alokasi Slot Otomatis\n(Cari Slot Terdekat)", 250, 72),
        ("uc3", 400, "Cek Lokasi &\nKetersediaan Slot", 240, 70),
        ("uc4", 530, "Hitung Tarif &\nPembayaran Parkir", 240, 72),
        ("uc5", 660, "Validasi Tiket Keluar\n(Check-Out Gate)", 240, 70),
        ("uc6", 780, "Monitoring Status\n& Kuota Slot", 240, 70),
        ("uc7", 895, "Maintenance Barrier\nGate & Sensor Slot", 250, 72)
    ]

    uc_nodes = {}
    for uc_id, cy, text, w, h in use_cases:
        draw_usecase_oval(draw, uc_center_x, cy, w, h, text, font=font_uc)
        uc_nodes[uc_id] = (uc_center_x, cy, w, h)

    # Draw Relationship Lines (Actor to Use Case)
    def connect_actor_to_uc(actor_pt, uc_id):
        uc_cx, uc_cy, uc_w, uc_h = uc_nodes[uc_id]
        ax, ay = actor_pt
        edge_x, edge_y = get_oval_edge_point(uc_cx, uc_cy, uc_w, uc_h, ax, ay)
        draw.line([actor_pt, (edge_x, edge_y)], fill="#000000", width=2)

    # 1. Pengendara connects to:
    # - uc1: Check-In & Ambil Karcis Masuk
    # - uc2: Alokasi Slot Otomatis
    # - uc3: Cek Lokasi & Ketersediaan Slot
    # - uc4: Hitung Tarif & Pembayaran Parkir
    # - uc5: Validasi Tiket Keluar
    connect_actor_to_uc(pt_pengendara, "uc1")
    connect_actor_to_uc(pt_pengendara, "uc2")
    connect_actor_to_uc(pt_pengendara, "uc3")
    connect_actor_to_uc(pt_pengendara, "uc4")
    connect_actor_to_uc(pt_pengendara, "uc5")

    # 2. Teknisi Parkir connects to:
    # - uc6: Monitoring Status & Kuota Slot
    # - uc7: Maintenance Barrier Gate & Sensor Slot
    connect_actor_to_uc(pt_teknisi, "uc6")
    connect_actor_to_uc(pt_teknisi, "uc7")

    # 3. Bank / E-Money Gateway connects to:
    # - uc4: Hitung Tarif & Pembayaran Parkir
    connect_actor_to_uc(pt_bank, "uc4")

    # 4. Operator Parkir connects to:
    # - uc4: Hitung Tarif & Pembayaran Parkir (alternatif manual cash)
    # - uc5: Validasi Tiket Keluar (lost ticket / emergency)
    # - uc6: Monitoring Status & Kuota Slot
    connect_actor_to_uc(pt_operator, "uc4")
    connect_actor_to_uc(pt_operator, "uc5")
    connect_actor_to_uc(pt_operator, "uc6")

    # Document Header / Title
    draw.text((40, 25), "UML Use Case Diagram: Smart Parking System", fill="#000000", font=font_title)
    draw.text((40, 48), "Proyek: Smart Parking Gedung & Slot Otomatis (SmartParkir) | Mahasiswa: Arjuna Lanang Adiwarsana", fill="#555555", font=font_sub)

    img.save(OUTPUT_PNG, quality=95)

    # Save to PDF
    doc = SimpleDocTemplate(OUTPUT_PDF, pagesize=landscape(letter),
                            leftMargin=18, rightMargin=18, topMargin=18, bottomMargin=18)
    story = [RLImage(OUTPUT_PNG, width=700, height=544)]
    doc.build(story)
    print(f"[Sukses] Use Case Diagram tersimpan di:\n - {OUTPUT_PNG}\n - {OUTPUT_PDF}")

if __name__ == "__main__":
    create_use_case_diagram()
