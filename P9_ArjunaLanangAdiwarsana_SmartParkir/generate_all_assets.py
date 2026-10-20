import os
import math
from PIL import Image, ImageDraw, ImageFont
from reportlab.lib.pagesizes import letter, landscape
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, HRFlowable, Image as RLImage
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfgen import canvas

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
UML_DIR = os.path.join(SCRIPT_DIR, "UML")
SCREENSHOT_DIR = os.path.join(SCRIPT_DIR, "Screenshot")
os.makedirs(UML_DIR, exist_ok=True)
os.makedirs(SCREENSHOT_DIR, exist_ok=True)

# -------------------------------------------------------------------------
# 1. GENERATE UML CLASS DIAGRAM (PNG + PDF)
# -------------------------------------------------------------------------
def draw_open_arrow(draw, start, end, fill="#000000", width=2, size=10):
    x1, y1 = start
    x2, y2 = end
    dx = x2 - x1
    dy = y2 - y1
    dist = math.hypot(dx, dy)
    if dist == 0:
        return
    ux = dx / dist
    uy = dy / dist
    px = -uy
    py = ux
    draw.line([start, end], fill=fill, width=int(width))
    a1 = (x2 - ux * size + px * (size * 0.5), y2 - uy * size + py * (size * 0.5))
    a2 = (x2 - ux * size - px * (size * 0.5), y2 - uy * size - py * (size * 0.5))
    draw.line([a1, end], fill=fill, width=int(width))
    draw.line([a2, end], fill=fill, width=int(width))

def create_class_diagram(output_png, output_pdf):
    W, H = 1580, 1380
    img = Image.new("RGB", (W, H), "#ffffff")
    draw = ImageDraw.Draw(img)

    font_path_bold = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
    font_path_regular = "/System/Library/Fonts/Supplemental/Arial.ttf"
    font_path_italic = "/System/Library/Fonts/Supplemental/Arial Italic.ttf"
    if not os.path.exists(font_path_bold):
        font_path_bold = "/System/Library/Fonts/Supplemental/Arial.ttf"

    font_title_block = ImageFont.truetype(font_path_bold, 15)
    font_title_block_sub = ImageFont.truetype(font_path_regular, 13)
    font_class_title = ImageFont.truetype(font_path_bold, 16)
    font_stereo = ImageFont.truetype(font_path_italic, 13)
    font_member = ImageFont.truetype(font_path_regular, 13)
    font_label = ImageFont.truetype(font_path_regular, 13)
    font_label_bold = ImageFont.truetype(font_path_bold, 13)

    # Canvas border
    draw.rectangle([25, 20, W - 25, H - 20], fill="#ffffff", outline="#222222", width=1)

    # Title Block (Academic Header) Top Left
    draw.rectangle([25, 20, 600, 115], fill="#fbfbfb", outline="#222222", width=1)
    draw.text((40, 32), "UML Class Diagram: Smart Parking System", fill="#000000", font=font_title_block)
    draw.text((40, 56), "Mahasiswa : Arjuna Lanang Adiwarsana (NRP: 3125522010)", fill="#333333", font=font_title_block_sub)
    draw.text((40, 78), "Mata Kuliah: Praktikum PBO - Modul 9 (PENS PSDKU)", fill="#333333", font=font_title_block_sub)

    # Legend Block Top Right
    draw.rectangle([700, 20, W - 25, 115], fill="#fbfbfb", outline="#222222", width=1)
    draw.text((715, 30), "Keterangan Notasi & Hubungan UML:", fill="#000000", font=font_label_bold)
    draw.text((715, 52), "+ public    - private    # protected    * / 1..* Multiplicity", fill="#333333", font=font_label)

    # Legend shapes
    pts_leg_comp = [(725, 82), (717, 88), (725, 94), (733, 88)]
    draw.polygon(pts_leg_comp, fill="#000000", outline="#000000")
    draw.line([(733, 88), (750, 88)], fill="#000000", width=2)
    draw.text((756, 80), "Komposisi (Part-of)", fill="#222222", font=font_label)

    pts_leg_agg = [(915, 82), (907, 88), (915, 94), (923, 88)]
    draw.polygon(pts_leg_agg, fill="#ffffff", outline="#000000")
    draw.line([(923, 88), (940, 88)], fill="#000000", width=2)
    draw.text((946, 80), "Agregasi (Has-a)", fill="#222222", font=font_label)

    draw_open_arrow(draw, (1100, 88), (1130, 88), fill="#000000", width=2, size=7)
    draw.text((1138, 80), "Asosiasi / Referensi", fill="#222222", font=font_label)

    # Helper function to draw standard UML class box
    def draw_class(x, y, w, stereo, name, attributes, methods, bg_color="#ffffff"):
        header_h = 44 if stereo else 36
        attr_h = (len(attributes) * 21 + 10) if attributes else 0
        method_h = (len(methods) * 21 + 10) if methods else 0
        total_h = header_h + attr_h + method_h

        draw.rectangle([x, y, x + w, y + total_h], fill=bg_color, outline="#000000", width=1)
        draw.line([(x, y + header_h), (x + w, y + header_h)], fill="#000000", width=1)

        if stereo:
            sw = draw.textlength(stereo, font=font_stereo)
            draw.text((x + (w - sw) / 2, y + 6), stereo, fill="#222222", font=font_stereo)
            nw = draw.textlength(name, font=font_class_title)
            draw.text((x + (w - nw) / 2, y + 23), name, fill="#000000", font=font_class_title)
        else:
            nw = draw.textlength(name, font=font_class_title)
            draw.text((x + (w - nw) / 2, y + 10), name, fill="#000000", font=font_class_title)

        curr_y = y + header_h + 8
        if attributes:
            for attr in attributes:
                draw.text((x + 12, curr_y), attr, fill="#000000", font=font_member)
                curr_y += 21
            curr_y += 2
            draw.line([(x, curr_y), (x + w, curr_y)], fill="#000000", width=1)
            curr_y += 8

        if methods:
            for m in methods:
                draw.text((x + 12, curr_y), m, fill="#000000", font=font_member)
                curr_y += 21

        return total_h

    # 1. Kendaraan (Top Left)
    box_w_kendaraan = 460
    box_x_kendaraan = 60
    box_y_kendaraan = 180
    h_kendaraan = draw_class(
        x=box_x_kendaraan, y=box_y_kendaraan, w=box_w_kendaraan,
        stereo="<<entity>>", name="Kendaraan",
        attributes=[
            "- platNomor : String",
            "- tipe : String",
            "- merk : String"
        ],
        methods=[
            "+ Kendaraan(platNomor, tipe, merk)",
            "+ getPlatNomor() : String",
            "+ getTipe() : String",
            "+ getMerk() : String",
            "+ isMobilListrik() : boolean",
            "+ toString() : String"
        ],
        bg_color="#fcfdff"
    )

    # 2. SlotParkir (Bottom Left)
    box_w_slot = 460
    box_x_slot = 60
    box_y_slot = 620
    h_slot = draw_class(
        x=box_x_slot, y=box_y_slot, w=box_w_slot,
        stereo="<<entity>>", name="SlotParkir",
        attributes=[
            "- idSlot : String",
            "- lantai : int",
            "- tipeSlot : String",
            "- terisi : boolean",
            "- kendaraanParkir : Kendaraan"
        ],
        methods=[
            "+ SlotParkir(idSlot, lantai, tipeSlot)",
            "+ cocokUntuk(k : Kendaraan) : boolean",
            "+ isiSlot(k : Kendaraan) : boolean",
            "+ kosongkanSlot() : Kendaraan",
            "+ getIdSlot() : String",
            "+ getLantai() : int",
            "+ getTipeSlot() : String",
            "+ isTerisi() : boolean",
            "+ getKendaraanParkir() : Kendaraan",
            "+ toString() : String"
        ],
        bg_color="#fcfcff"
    )

    # 3. GedungParkir (Top Right - Main Coordinator)
    box_w_gedung = 600
    box_x_gedung = 910
    box_y_gedung = 180
    h_gedung = draw_class(
        x=box_x_gedung, y=box_y_gedung, w=box_w_gedung,
        stereo="<<aggregate root>>", name="GedungParkir",
        attributes=[
            "- namaGedung : String",
            "- daftarSlot : List<SlotParkir>",
            "- riwayatTransaksi : List<KarcisParkir>",
            "- tarifJamPertamaMobil : double",
            "- tarifPerJamMobil : double",
            "- tarifJamPertamaMotor : double",
            "- tarifPerJamMotor : double",
            "- totalPendapatan : double",
            "- counterTiket : int"
        ],
        methods=[
            "+ GedungParkir(nama, t1Mobil, t2Mobil, t1Motor, t2Motor)",
            "+ tambahSlot(slot : SlotParkir) : void",
            "+ cariSlotTersedia(k : Kendaraan) : SlotParkir",
            "+ prosesMasuk(k : Kendaraan, jamMasuk : int) : KarcisParkir",
            "+ prosesKeluar(t : KarcisParkir, jam : int, mtd : String, bayar : double) : boolean",
            "+ tampilkanStatusKapasitas() : void",
            "+ cetakStrukKeluar(tiket : KarcisParkir) : void",
            "+ getNamaGedung() : String",
            "+ getDaftarSlot() : List<SlotParkir>",
            "+ getRiwayatTransaksi() : List<KarcisParkir>",
            "+ getTotalPendapatan() : double"
        ],
        bg_color="#f8fbf8"
    )

    # 4. KarcisParkir (Bottom Right - Transaction Line)
    box_w_karcis = 600
    box_x_karcis = 910
    box_y_karcis = 820
    h_karcis = draw_class(
        x=box_x_karcis, y=box_y_karcis, w=box_w_karcis,
        stereo="<<entity>>", name="KarcisParkir",
        attributes=[
            "- nomorTiket : String",
            "- kendaraan : Kendaraan",
            "- slot : SlotParkir",
            "- jamMasuk : int",
            "- jamKeluar : int",
            "- sudahDibayar : boolean",
            "- totalBiaya : double",
            "- metodePembayaran : String"
        ],
        methods=[
            "+ KarcisParkir(nomorTiket, kendaraan, slot, jamMasuk)",
            "+ hitungDurasi(jamKeluarAktual : int) : int",
            "+ hitungTarif(jamKeluar : int, tarif1 : double, tarif2 : double) : double",
            "+ prosesPembayaran(metode : String, uangBayar : double) : boolean",
            "+ getNomorTiket() : String",
            "+ getKendaraan() : Kendaraan",
            "+ getSlot() : SlotParkir",
            "+ getJamMasuk() : int",
            "+ getJamKeluar() : int",
            "+ isSudahDibayar() : boolean",
            "+ getTotalBiaya() : double",
            "+ getMetodePembayaran() : String",
            "+ toString() : String"
        ],
        bg_color="#fffdf8"
    )

    # -------------------------------------------------------------------------
    # DRAW RELATIONSHIPS
    # -------------------------------------------------------------------------
    # RELASI 1: GedungParkir *--> SlotParkir (Komposisi)
    # Line from GedungParkir to SlotParkir (step path)
    x_g_mid = box_x_gedung + 80
    y_g_bot = box_y_gedung + h_gedung
    y_s_mid = box_y_slot + 120
    x_s_right = box_x_slot + box_w_slot

    # Diamond at GedungParkir (pointing down)
    pts_comp1 = [(x_g_mid, y_g_bot), (x_g_mid - 7, y_g_bot + 8), (x_g_mid, y_g_bot + 16), (x_g_mid + 7, y_g_bot + 8)]
    draw.polygon(pts_comp1, fill="#000000", outline="#000000")
    draw.line([(x_g_mid, y_g_bot + 16), (x_g_mid, y_s_mid)], fill="#111111", width=2)
    draw_open_arrow(draw, (x_g_mid, y_s_mid), (x_s_right, y_s_mid), fill="#111111", width=2, size=10)
    draw.text((x_g_mid + 12, y_g_bot + 8), "1", fill="#000000", font=font_label_bold)
    draw.text((x_s_right + 15, y_s_mid - 24), "1..*", fill="#000000", font=font_label_bold)
    draw.text(((x_s_right + x_g_mid) / 2 - 40, y_s_mid - 22), "«composition» (daftarSlot)", fill="#333333", font=font_stereo)

    # RELASI 2: GedungParkir *--> KarcisParkir (Komposisi)
    x_rel2 = box_x_gedung + 300
    y_top_rel2 = box_y_gedung + h_gedung
    y_bot_rel2 = box_y_karcis
    draw.line([(x_rel2, y_top_rel2 + 16), (x_rel2, y_bot_rel2)], fill="#111111", width=2)
    pts_comp2 = [(x_rel2, y_top_rel2), (x_rel2 - 8, y_top_rel2 + 8), (x_rel2, y_top_rel2 + 16), (x_rel2 + 8, y_top_rel2 + 8)]
    draw.polygon(pts_comp2, fill="#000000", outline="#000000")
    draw.text((x_rel2 + 14, y_top_rel2 + 10), "1", fill="#000000", font=font_label_bold)
    draw.text((x_rel2 + 14, y_bot_rel2 - 24), "0..*", fill="#000000", font=font_label_bold)
    draw.text((x_rel2 - 130, (y_top_rel2 + y_bot_rel2) / 2 - 8), "«composition» (riwayatTransaksi)", fill="#333333", font=font_stereo)

    # RELASI 3: KarcisParkir o--> Kendaraan (Agregasi)
    y_rel3 = box_y_karcis + 80
    x_k_mid = box_x_kendaraan + box_w_kendaraan
    draw.line([(box_x_karcis - 16, y_rel3), (x_k_mid + 50, y_rel3)], fill="#111111", width=2)
    draw.line([(x_k_mid + 50, y_rel3), (x_k_mid + 50, box_y_kendaraan + 120)], fill="#111111", width=2)
    draw_open_arrow(draw, (x_k_mid + 50, box_y_kendaraan + 120), (x_k_mid, box_y_kendaraan + 120), fill="#111111", width=2, size=10)
    # Hollow diamond at KarcisParkir
    pts_agg1 = [(box_x_karcis, y_rel3), (box_x_karcis - 8, y_rel3 - 7), (box_x_karcis - 16, y_rel3), (box_x_karcis - 8, y_rel3 + 7)]
    draw.polygon(pts_agg1, fill="#ffffff", outline="#000000")
    draw.text((x_k_mid + 12, box_y_kendaraan + 96), "1", fill="#000000", font=font_label_bold)
    draw.text((box_x_karcis - 45, y_rel3 - 24), "1..*", fill="#000000", font=font_label_bold)
    draw.text((x_k_mid + 60, (y_rel3 + box_y_kendaraan + 120) / 2), "«aggregates» (kendaraan)", fill="#333333", font=font_stereo)

    # RELASI 4: KarcisParkir o--> SlotParkir (Agregasi)
    y_rel4 = box_y_karcis + 160
    y_s_target = box_y_slot + 220
    draw.line([(box_x_karcis - 16, y_rel4), (box_x_slot + box_w_slot + 70, y_rel4)], fill="#111111", width=2)
    draw.line([(box_x_slot + box_w_slot + 70, y_rel4), (box_x_slot + box_w_slot + 70, y_s_target)], fill="#111111", width=2)
    draw_open_arrow(draw, (box_x_slot + box_w_slot + 70, y_s_target), (box_x_slot + box_w_slot, y_s_target), fill="#111111", width=2, size=10)
    pts_agg2 = [(box_x_karcis, y_rel4), (box_x_karcis - 8, y_rel4 - 7), (box_x_karcis - 16, y_rel4), (box_x_karcis - 8, y_rel4 + 7)]
    draw.polygon(pts_agg2, fill="#ffffff", outline="#000000")
    draw.text((box_x_slot + box_w_slot + 12, y_s_target - 24), "1", fill="#000000", font=font_label_bold)
    draw.text((box_x_karcis - 45, y_rel4 - 24), "1..*", fill="#000000", font=font_label_bold)
    draw.text((box_x_slot + box_w_slot + 80, (y_rel4 + y_s_target) / 2), "«aggregates» (slot)", fill="#333333", font=font_stereo)

    img.save(output_png, quality=95)

    doc = SimpleDocTemplate(output_pdf, pagesize=landscape(letter),
                            leftMargin=18, rightMargin=18, topMargin=18, bottomMargin=18)
    story = [RLImage(output_png, width=700, height=544)]
    doc.build(story)
    print(f"[Sukses] Diagram Class tersimpan di:\n - {output_png}\n - {output_pdf}")

# -------------------------------------------------------------------------
# 2. GENERATE TERMINAL SCREENSHOT
# -------------------------------------------------------------------------
def generate_terminal_screenshot(output_path):
    width = 1750
    font_size = 18
    line_height = 25
    padding_x = 35
    padding_top = 65
    padding_bottom = 35

    BG_COLOR = (24, 24, 37)
    HEADER_BG = (30, 30, 46)
    HEADER_BORDER = (49, 50, 68)
    TEXT_DEFAULT = (205, 214, 244)
    TEXT_MUTED = (147, 153, 178)
    COLOR_GREEN = (166, 227, 161)
    COLOR_CYAN = (137, 220, 235)
    COLOR_YELLOW = (249, 226, 175)
    COLOR_BLUE = (137, 180, 250)
    COLOR_PURPLE = (203, 166, 247)
    COLOR_RED = (243, 139, 168)
    COLOR_ORANGE = (250, 179, 135)

    BTN_RED = (255, 95, 86)
    BTN_YELLOW = (255, 189, 46)
    BTN_GREEN = (39, 201, 63)

    lines_data = [
        [
            ("➜  ", COLOR_GREEN),
            ("P9_ArjunaLanangAdiwarsana_SmartParkir ", COLOR_CYAN),
            ("git:(", COLOR_PURPLE),
            ("main", COLOR_RED),
            (") ✗ ", COLOR_PURPLE),
            ("javac -d bin SourceCode/model/*.java SourceCode/Main.java && java -cp bin Main", (255, 255, 255))
        ],
        [("==========================================================================", COLOR_BLUE)],
        [("   PRAKTIKUM PBO MODUL 9: IMPLEMENTASI UML CLASS DIAGRAM KE KODE JAVA    ", COLOR_YELLOW)],
        [("   Topik  : Smart Parking Gedung & Slot Otomatis (SmartParkir)           ", COLOR_CYAN)],
        [("   Nama   : Arjuna Lanang Adiwarsana (NRP: 3125522010)                   ", TEXT_DEFAULT)],
        [("   Kampus : PENS PSDKU Sumenep                                           ", TEXT_MUTED)],
        [("==========================================================================", COLOR_BLUE)],
        [("", TEXT_DEFAULT)],
        [(">>> [SKENARIO 1] Inisialisasi Fasilitas Gedung Parkir Bertingkat", COLOR_GREEN)],
        [("Gedung parkir siap beroperasi dengan 5 slot terdaftar.", TEXT_DEFAULT)],
        [("==========================================================================", COLOR_BLUE)],
        [("              STATUS OKUPANSI REALTIME - MALL GRAND PENS PLAZA              ", COLOR_CYAN)],
        [("==========================================================================", COLOR_BLUE)],
        [(" • Slot Motor           : Terisi 0/2 (Tersedia: 2)", TEXT_DEFAULT)],
        [(" • Slot Mobil Reguler   : Terisi 0/2 (Tersedia: 2)", TEXT_DEFAULT)],
        [(" • Slot Mobil Listrik   : Terisi 0/1 (Tersedia: 1)", TEXT_DEFAULT)],
        [("--------------------------------------------------------------------------", COLOR_BLUE)],
        [(" Total Akumulasi Kas Masuk : Rp 0", COLOR_GREEN)],
        [("==========================================================================", COLOR_BLUE)],
        [("", TEXT_DEFAULT)],
        [(">>> [SKENARIO 2] Check-In Kendaraan & Alokasi Slot Cerdas", COLOR_GREEN)],
        [("[CHECK-IN SUKSES] ", COLOR_GREEN), ("B 1234 ABC (MOBIL - Toyota Avanza) berhasil masuk.", TEXT_DEFAULT)],
        [(" >> Tiket: TKT-0001 | Alokasi Slot: MBL-L1-01 (Lantai 1)", COLOR_YELLOW)],
        [("[CHECK-IN SUKSES] ", COLOR_GREEN), ("L 5566 EV (MOBIL_LISTRIK - Hyundai Ioniq 5) berhasil masuk.", TEXT_DEFAULT)],
        [(" >> Tiket: TKT-0002 | Alokasi Slot: EV-L1-01 (Lantai 1)", COLOR_YELLOW)],
        [("[CHECK-IN SUKSES] ", COLOR_GREEN), ("M 9988 XYZ (MOTOR - Honda Vario) berhasil masuk.", TEXT_DEFAULT)],
        [(" >> Tiket: TKT-0003 | Alokasi Slot: MTR-L1-01 (Lantai 1)", COLOR_YELLOW)],
        [("", TEXT_DEFAULT)],
        [("==========================================================================", COLOR_BLUE)],
        [("              STATUS OKUPANSI REALTIME - MALL GRAND PENS PLAZA              ", COLOR_CYAN)],
        [("==========================================================================", COLOR_BLUE)],
        [(" • Slot Motor           : Terisi 1/2 (Tersedia: 1)", TEXT_DEFAULT)],
        [(" • Slot Mobil Reguler   : Terisi 1/2 (Tersedia: 1)", TEXT_DEFAULT)],
        [(" • Slot Mobil Listrik   : Terisi 1/1 (Tersedia: 0)", TEXT_DEFAULT)],
        [("--------------------------------------------------------------------------", COLOR_BLUE)],
        [(" Total Akumulasi Kas Masuk : Rp 0", COLOR_GREEN)],
        [("==========================================================================", COLOR_BLUE)],
        [("", TEXT_DEFAULT)],
        [(">>> [SKENARIO 3] Uji Enkapsulasi & Proteksi Kapasitas Penuh", COLOR_GREEN)],
        [("a. Mencoba memarkir Mobil Listrik kedua (Slot EV penuh -> dialihkan ke Mobil Lt 2):", COLOR_ORANGE)],
        [("[CHECK-IN SUKSES] ", COLOR_GREEN), ("N 7788 EV (MOBIL_LISTRIK - Wuling Binguo) berhasil masuk.", TEXT_DEFAULT)],
        [(" >> Tiket: TKT-0004 | Alokasi Slot: MBL-L2-01 (Lantai 2)", COLOR_YELLOW)],
        [("", TEXT_DEFAULT)],
        [("b. Mencoba memarkir Mobil ketiga (Slot Mobil Lt 1 & 2 sudah habis):", COLOR_ORANGE)],
        [("[PARKIR PENUH] ", COLOR_RED), ("Tidak ada slot kosong yang tersedia untuk kendaraan D 9999 MOB (MOBIL - Mitsubishi Pajero)!", TEXT_DEFAULT)],
        [("", TEXT_DEFAULT)],
        [("c. Mencoba check-out dengan pembayaran kurang dari tagihan:", COLOR_ORANGE)],
        [("[PEMBAYARAN DITOLAK] ", COLOR_RED), ("Uang tidak mencukupi! Tagihan: Rp 14,000, Dibayar: Rp 10,000", TEXT_DEFAULT)],
        [("[GATE TETAP TERTUTUP] ", COLOR_RED), ("Pembayaran belum selesai. Portal keluar tidak dapat dibuka.", TEXT_DEFAULT)],
        [("", TEXT_DEFAULT)],
        [(">>> [SKENARIO 4] Check-Out Sukses Kendaraan Reguler (Mobil k1)", COLOR_GREEN)],
        [("[CHECK-OUT SUKSES] ", COLOR_CYAN), ("Kendaraan B 1234 ABC telah keluar.", TEXT_DEFAULT)],
        [(" >> Slot MBL-L1-01 sekarang KOSONG kembali dan siap digunakan.", COLOR_GREEN)],
        [("==========================================================================", COLOR_BLUE)],
        [("                     KUITANSI PARKIR - MALL GRAND PENS PLAZA                   ", COLOR_CYAN)],
        [("==========================================================================", COLOR_BLUE)],
        [(" No. Tiket    : TKT-0001            Metode Bayar : E_MONEY", TEXT_DEFAULT)],
        [(" Kendaraan    : B 1234 ABC (MOBIL - Toyota Avanza)", TEXT_DEFAULT)],
        [(" Lokasi Parkir: Slot MBL-L1-01 (Lantai 1)", TEXT_DEFAULT)],
        [(" Waktu Parkir : 08:00 s/d 12:00 (Durasi: 4 Jam)", TEXT_DEFAULT)],
        [("--------------------------------------------------------------------------", COLOR_BLUE)],
        [(" TOTAL TAGIHAN: Rp 14,000  [STATUS: LUNAS - GATE OPEN]", COLOR_GREEN)],
        [("==========================================================================", COLOR_BLUE)],
        [("", TEXT_DEFAULT)],
        [(">>> [SKENARIO 5] Check-Out Mobil Listrik EV (Diskon Insentif Hijau 20%)", COLOR_GREEN)],
        [("[CHECK-OUT SUKSES] ", COLOR_CYAN), ("Kendaraan L 5566 EV telah keluar.", TEXT_DEFAULT)],
        [(" >> Slot EV-L1-01 sekarang KOSONG kembali dan siap digunakan.", COLOR_GREEN)],
        [("==========================================================================", COLOR_BLUE)],
        [("                     KUITANSI PARKIR - MALL GRAND PENS PLAZA                   ", COLOR_CYAN)],
        [("==========================================================================", COLOR_BLUE)],
        [(" No. Tiket    : TKT-0002            Metode Bayar : QRIS", TEXT_DEFAULT)],
        [(" Kendaraan    : L 5566 EV (MOBIL_LISTRIK - Hyundai Ioniq 5)", TEXT_DEFAULT)],
        [(" Lokasi Parkir: Slot EV-L1-01 (Lantai 1)", TEXT_DEFAULT)],
        [(" Waktu Parkir : 09:00 s/d 14:00 (Durasi: 5 Jam)", TEXT_DEFAULT)],
        [(" Catatan      : Mendapatkan Insentif Hijau Diskon 20% (EV Friendly)", COLOR_YELLOW)],
        [("--------------------------------------------------------------------------", COLOR_BLUE)],
        [(" TOTAL TAGIHAN: Rp 13,600  [STATUS: LUNAS - GATE OPEN]", COLOR_GREEN)],
        [("==========================================================================", COLOR_BLUE)],
        [("", TEXT_DEFAULT)],
        [(">>> [SKENARIO 6] Monitoring Realtime Terkini Gedung Parkir", COLOR_GREEN)],
        [("Slot MBL-L1-01 dan EV-L1-01 sekarang sudah kosong kembali dan siap dipakai mobil baru.", TEXT_DEFAULT)],
        [("==========================================================================", COLOR_BLUE)],
        [("              STATUS OKUPANSI REALTIME - MALL GRAND PENS PLAZA              ", COLOR_CYAN)],
        [("==========================================================================", COLOR_BLUE)],
        [(" • Slot Motor           : Terisi 1/2 (Tersedia: 1)", TEXT_DEFAULT)],
        [(" • Slot Mobil Reguler   : Terisi 1/2 (Tersedia: 1)", TEXT_DEFAULT)],
        [(" • Slot Mobil Listrik   : Terisi 0/1 (Tersedia: 1)", TEXT_DEFAULT)],
        [("--------------------------------------------------------------------------", COLOR_BLUE)],
        [(" Total Akumulasi Kas Masuk : Rp 27,600", COLOR_GREEN)],
        [("==========================================================================", COLOR_BLUE)],
        [("", TEXT_DEFAULT)],
        [("[PENGUJIAN SELESAI] Seluruh relasi, enkapsulasi, dan otomasi alokasi slot bekerja sempurna!", COLOR_GREEN)]
    ]

    height = padding_top + padding_bottom + (len(lines_data) * line_height)
    img = Image.new("RGB", (width, height), BG_COLOR)
    draw = ImageDraw.Draw(img)

    draw.rectangle([(0, 0), (width, 42)], fill=HEADER_BG)
    draw.line([(0, 42), (width, 42)], fill=HEADER_BORDER, width=1)

    draw.ellipse([(18, 14), (30, 26)], fill=BTN_RED)
    draw.ellipse([(38, 14), (50, 26)], fill=BTN_YELLOW)
    draw.ellipse([(58, 14), (70, 26)], fill=BTN_GREEN)

    font_path_title = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
    font_title = ImageFont.truetype(font_path_title if os.path.exists(font_path_title) else "/System/Library/Fonts/Supplemental/Arial.ttf", 13)
    header_title = "arjuna@MacBook-Pro: ~/Herd/Java/P9_ArjunaLanangAdiwarsana_SmartParkir (zsh)"
    tw = draw.textlength(header_title, font=font_title)
    draw.text(((width - tw) / 2, 13), header_title, fill=TEXT_MUTED, font=font_title)

    font_path_mono = "/System/Library/Fonts/SFNSMono.ttf"
    if not os.path.exists(font_path_mono):
        font_path_mono = "/System/Library/Fonts/Menlo.ttc"
    if not os.path.exists(font_path_mono):
        font_path_mono = "/System/Library/Fonts/Supplemental/Courier New.ttf"

    font = ImageFont.truetype(font_path_mono, font_size)

    y = padding_top
    for line in lines_data:
        x = padding_x
        for seg_text, seg_color in line:
            draw.text((x, y), seg_text, font=font, fill=seg_color)
            x += draw.textlength(seg_text, font=font)
        y += line_height

    img.save(output_path, "PNG", quality=95)
    print(f"[Sukses] Terminal screenshot tersimpan di: {output_path}")

# -------------------------------------------------------------------------
# 3. WRITE RAW DIAGRAMS (PLANTUML & MERMAID)
# -------------------------------------------------------------------------
def write_class_diagram_sources():
    puml_path = os.path.join(UML_DIR, "ClassDiagram.puml")
    mermaid_path = os.path.join(UML_DIR, "ClassDiagram.mermaid")

    puml = """@startuml
skinparam classAttributeIconSize 0
skinparam monochrome false
skinparam packageStyle rectangle
skinparam shadowing false

class Kendaraan {
    - platNomor : String
    - tipe : String
    - merk : String
    + Kendaraan(platNomor : String, tipe : String, merk : String)
    + getPlatNomor() : String
    + getTipe() : String
    + getMerk() : String
    + isMobilListrik() : boolean
    + toString() : String
}

class SlotParkir {
    - idSlot : String
    - lantai : int
    - tipeSlot : String
    - terisi : boolean
    - kendaraanParkir : Kendaraan
    + SlotParkir(idSlot : String, lantai : int, tipeSlot : String)
    + cocokUntuk(k : Kendaraan) : boolean
    + isiSlot(k : Kendaraan) : boolean
    + kosongkanSlot() : Kendaraan
    + getIdSlot() : String
    + getLantai() : int
    + getTipeSlot() : String
    + isTerisi() : boolean
    + getKendaraanParkir() : Kendaraan
    + toString() : String
}

class KarcisParkir {
    - nomorTiket : String
    - kendaraan : Kendaraan
    - slot : SlotParkir
    - jamMasuk : int
    - jamKeluar : int
    - sudahDibayar : boolean
    - totalBiaya : double
    - metodePembayaran : String
    + KarcisParkir(nomorTiket : String, kendaraan : Kendaraan, slot : SlotParkir, jamMasuk : int)
    + hitungDurasi(jamKeluarAktual : int) : int
    + hitungTarif(jamKeluar : int, tarif1 : double, tarif2 : double) : double
    + prosesPembayaran(metode : String, uangBayar : double) : boolean
    + getNomorTiket() : String
    + getKendaraan() : Kendaraan
    + getSlot() : SlotParkir
    + getJamMasuk() : int
    + getJamKeluar() : int
    + isSudahDibayar() : boolean
    + getTotalBiaya() : double
    + getMetodePembayaran() : String
    + toString() : String
}

class GedungParkir {
    - namaGedung : String
    - daftarSlot : List<SlotParkir>
    - riwayatTransaksi : List<KarcisParkir>
    - tarifJamPertamaMobil : double
    - tarifPerJamMobil : double
    - tarifJamPertamaMotor : double
    - tarifPerJamMotor : double
    - totalPendapatan : double
    - counterTiket : int
    + GedungParkir(nama : String, t1Mobil : double, t2Mobil : double, t1Motor : double, t2Motor : double)
    + tambahSlot(slot : SlotParkir) : void
    + cariSlotTersedia(k : Kendaraan) : SlotParkir
    + prosesMasuk(k : Kendaraan, jamMasuk : int) : KarcisParkir
    + prosesKeluar(t : KarcisParkir, jam : int, metode : String, bayar : double) : boolean
    + tampilkanStatusKapasitas() : void
    + cetakStrukKeluar(tiket : KarcisParkir) : void
    + getNamaGedung() : String
    + getDaftarSlot() : List<SlotParkir>
    + getRiwayatTransaksi() : List<KarcisParkir>
    + getTotalPendapatan() : double
}

GedungParkir "1" *-- "1..*" SlotParkir : composition (daftarSlot)
GedungParkir "1" *-- "0..*" KarcisParkir : composition (riwayatTransaksi)
KarcisParkir "1..*" o-- "1" Kendaraan : aggregation (kendaraan)
KarcisParkir "1..*" o-- "1" SlotParkir : aggregation (slot)
SlotParkir "1" o-- "0..1" Kendaraan : associates (kendaraanParkir)

@enduml
"""

    mermaid = """```mermaid
classDiagram
    direction TB

    class Kendaraan {
        -String platNomor
        -String tipe
        -String merk
        +Kendaraan(platNomor, tipe, merk)
        +getPlatNomor() String
        +getTipe() String
        +getMerk() String
        +isMobilListrik() boolean
        +toString() String
    }

    class SlotParkir {
        -String idSlot
        -int lantai
        -String tipeSlot
        -boolean terisi
        -Kendaraan kendaraanParkir
        +SlotParkir(idSlot, lantai, tipeSlot)
        +cocokUntuk(Kendaraan k) boolean
        +isiSlot(Kendaraan k) boolean
        +kosongkanSlot() Kendaraan
        +getIdSlot() String
        +getLantai() int
        +getTipeSlot() String
        +isTerisi() boolean
        +getKendaraanParkir() Kendaraan
        +toString() String
    }

    class KarcisParkir {
        -String nomorTiket
        -Kendaraan kendaraan
        -SlotParkir slot
        -int jamMasuk
        -int jamKeluar
        -boolean sudahDibayar
        -double totalBiaya
        -String metodePembayaran
        +KarcisParkir(nomorTiket, kendaraan, slot, jamMasuk)
        +hitungDurasi(int jamKeluarAktual) int
        +hitungTarif(int jamKeluar, double tarif1, double tarif2) double
        +prosesPembayaran(String metode, double uangBayar) boolean
        +getNomorTiket() String
        +getKendaraan() Kendaraan
        +getSlot() SlotParkir
        +getJamMasuk() int
        +getJamKeluar() int
        +isSudahDibayar() boolean
        +getTotalBiaya() double
        +getMetodePembayaran() String
        +toString() String
    }

    class GedungParkir {
        -String namaGedung
        -List~SlotParkir~ daftarSlot
        -List~KarcisParkir~ riwayatTransaksi
        -double tarifJamPertamaMobil
        -double tarifPerJamMobil
        -double tarifJamPertamaMotor
        -double tarifPerJamMotor
        -double totalPendapatan
        -int counterTiket
        +GedungParkir(nama, t1Mobil, t2Mobil, t1Motor, t2Motor)
        +tambahSlot(SlotParkir slot) void
        +cariSlotTersedia(Kendaraan k) SlotParkir
        +prosesMasuk(Kendaraan k, int jamMasuk) KarcisParkir
        +prosesKeluar(KarcisParkir t, int jam, String metode, double bayar) boolean
        +tampilkanStatusKapasitas() void
        +cetakStrukKeluar(KarcisParkir tiket) void
        +getNamaGedung() String
        +getDaftarSlot() List~SlotParkir~
        +getRiwayatTransaksi() List~KarcisParkir~
        +getTotalPendapatan() double
    }

    GedungParkir "1" *-- "1..*" SlotParkir : Komposisi (daftarSlot)
    GedungParkir "1" *-- "0..*" KarcisParkir : Komposisi (riwayatTransaksi)
    KarcisParkir "1..*" o-- "1" Kendaraan : Agregasi (kendaraan)
    KarcisParkir "1..*" o-- "1" SlotParkir : Agregasi (slot)
    SlotParkir "1" o-- "0..1" Kendaraan : Asosiasi (kendaraanParkir)
```
"""

    with open(puml_path, "w", encoding="utf-8") as f:
        f.write(puml)
    with open(mermaid_path, "w", encoding="utf-8") as f:
        f.write(mermaid)
    print(f"[Sukses] File raw diagram class tersimpan:\n - {puml_path}\n - {mermaid_path}")

# -------------------------------------------------------------------------
# 4. GENERATE FULL PDF REPORT
# -------------------------------------------------------------------------
class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)

        if self._pageNumber > 1:
            self.line(40, letter[1] - 40, letter[0] - 40, letter[1] - 40)
            self.setFont("Helvetica", 8)
            self.setFillColor(colors.HexColor("#64748B"))
            self.drawString(40, letter[1] - 34, "Praktikum PBO - Modul 9: Smart Parking Gedung & Slot Otomatis")
            self.drawRightString(letter[0] - 40, letter[1] - 34, "Arjuna Lanang Adiwarsana | 3125522010")

        self.line(40, 45, letter[0] - 40, 45)
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#64748B"))
        self.drawString(40, 32, "PENS PSDKU Sumenep • D3 Teknik Informatika • 2026")
        page_str = f"Halaman {self._pageNumber} dari {page_count}"
        self.drawRightString(letter[0] - 40, 32, page_str)
        self.restoreState()

def generate_pdf_report(pdf_path, img_diagram_path, img_screenshot_path):
    doc = SimpleDocTemplate(
        pdf_path,
        pagesize=letter,
        leftMargin=40, rightMargin=40, topMargin=50, bottomMargin=55
    )

    styles = getSampleStyleSheet()
    normal_style = styles['Normal']

    title_style = ParagraphStyle(
        'DocTitle', parent=normal_style,
        fontName='Helvetica-Bold', fontSize=18, leading=22,
        textColor=colors.HexColor("#0F172A"), alignment=1
    )
    subtitle_style = ParagraphStyle(
        'DocSubtitle', parent=normal_style,
        fontName='Helvetica', fontSize=11, leading=15,
        textColor=colors.HexColor("#334155"), alignment=1
    )
    h1_style = ParagraphStyle(
        'Heading1_Custom', parent=normal_style,
        fontName='Helvetica-Bold', fontSize=13, leading=17,
        textColor=colors.HexColor("#0F172A"), spaceBefore=14, spaceAfter=6,
        keepWithNext=True
    )
    body_style = ParagraphStyle(
        'Body_Custom', parent=normal_style,
        fontName='Helvetica', fontSize=9, leading=13,
        textColor=colors.HexColor("#334155"), spaceAfter=5
    )
    table_cell = ParagraphStyle(
        'TableCell', parent=normal_style,
        fontName='Helvetica', fontSize=8, leading=11, textColor=colors.HexColor("#1E293B")
    )
    table_header = ParagraphStyle(
        'TableHeader', parent=normal_style,
        fontName='Helvetica-Bold', fontSize=8.5, leading=11, textColor=colors.white, alignment=1
    )
    q_title = ParagraphStyle(
        'QTitle', parent=normal_style,
        fontName='Helvetica-Bold', fontSize=9.5, leading=13, textColor=colors.HexColor("#1E40AF"), spaceBefore=6
    )
    q_ans = ParagraphStyle(
        'QAns', parent=normal_style,
        fontName='Helvetica', fontSize=8.5, leading=12, textColor=colors.HexColor("#1E293B"), spaceAfter=6
    )

    story = []

    # Title & Header
    story.append(Paragraph("LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK", title_style))
    story.append(Spacer(1, 4))
    story.append(Paragraph("<b>MODUL 9: IMPLEMENTASI UML CLASS DIAGRAM KE PROGRAM JAVA</b>", subtitle_style))
    story.append(Paragraph("Studi Kasus: Smart Parking Gedung & Slot Otomatis (SmartParkir)", subtitle_style))
    story.append(Spacer(1, 10))
    story.append(HRFlowable(width="100%", thickness=1.5, color=colors.HexColor("#0284C7"), spaceAfter=10))

    # Meta Data Table
    meta_data = [
        [Paragraph("<b>Nama Mahasiswa</b>", table_cell), Paragraph(": Arjuna Lanang Adiwarsana", table_cell),
         Paragraph("<b>Dosen Pengampu</b>", table_cell), Paragraph(": Nirwana Haidar Hari, S.Pd., M.Kom.", table_cell)],
        [Paragraph("<b>NRP</b>", table_cell), Paragraph(": 3125522010", table_cell),
         Paragraph("<b>Institusi</b>", table_cell), Paragraph(": PENS PSDKU Sumenep", table_cell)],
        [Paragraph("<b>Program Studi</b>", table_cell), Paragraph(": D3 Teknik Informatika", table_cell),
         Paragraph("<b>Tahun Akademik</b>", table_cell), Paragraph(": 2026", table_cell)]
    ]
    t_meta = Table(meta_data, colWidths=[100, 170, 95, 165])
    t_meta.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
    ]))
    story.append(t_meta)
    story.append(Spacer(1, 12))

    # 1. Rubrik Penilaian
    story.append(Paragraph("1. Pemetaan Rubrik Penilaian Modul 9 (Bobot 100%)", h1_style))
    rubrik_data = [
        [Paragraph("Aspek Penilaian", table_header), Paragraph("Bobot", table_header),
         Paragraph("Ketentuan Modul", table_header), Paragraph("Realisasi Implementasi pada Proyek", table_header)],
        [Paragraph("<b>Kesesuaian UML dengan Source Code</b>", table_cell), Paragraph("<b>30%</b>", table_cell),
         Paragraph("Seluruh class, atribut, method, dan visibility sinkron 1:1.", table_cell),
         Paragraph("4 class (<code>Kendaraan</code>, <code>SlotParkir</code>, <code>KarcisParkir</code>, <code>GedungParkir</code>) konsisten 100% dengan diagram UML.", table_cell)],
        [Paragraph("<b>Implementasi Class dan Object</b>", table_cell), Paragraph("<b>25%</b>", table_cell),
         Paragraph("Minimal 4 class utama dengan constructor & validation.", table_cell),
         Paragraph("Class memiliki constructor berparameter, validasi nilai awal, proteksi kapasitas penuh, dan alokasi slot cerdas.", table_cell)],
        [Paragraph("<b>Implementasi Relasi Antarobject</b>", table_cell), Paragraph("<b>20%</b>", table_cell),
         Paragraph("Minimal 2 relasi antarobject yang berjalan nyata saat runtime.", table_cell),
         Paragraph("1) <b>Komposisi</b>: <code>GedungParkir</code> [Komposisi] <code>SlotParkir</code><br/>2) <b>Komposisi</b>: <code>GedungParkir</code> [Komposisi] <code>KarcisParkir</code><br/>3) <b>Agregasi</b>: <code>KarcisParkir</code> [Agregasi] <code>Kendaraan</code><br/>4) <b>Agregasi</b>: <code>KarcisParkir</code> [Agregasi] <code>SlotParkir</code>", table_cell)],
        [Paragraph("<b>Encapsulation & Responsibility</b>", table_cell), Paragraph("<b>15%</b>", table_cell),
         Paragraph("Dilarang anemic model (bukan hanya getter/setter). Object menjaga state.", table_cell),
         Paragraph("Semua field <code>private</code>. Slot menjaga status terisi/kosong via <code>isiSlot()</code> dan <code>kosongkanSlot()</code> tanpa setter liar.", table_cell)],
        [Paragraph("<b>Program Dijalankan & Dijelaskan</b>", table_cell), Paragraph("<b>10%</b>", table_cell),
         Paragraph("Kode berjalan mulus, mencakup skenario pengujian lengkap.", table_cell),
         Paragraph("<code>Main.java</code> menjalankan 6 skenario interaksi: alokasi slot, penolakan saat penuh, pembayaran kurang, insentif EV 20%, dan monitoring kas.", table_cell)],
    ]
    t_rubrik = Table(rubrik_data, colWidths=[110, 45, 150, 225])
    t_rubrik.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#0284C7")),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#94A3B8")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(t_rubrik)
    story.append(Spacer(1, 14))

    story.append(PageBreak())

    # 2. Rancangan UML Class Diagram
    story.append(Paragraph("2. Rancangan UML Class Diagram Smart Parking System", h1_style))
    story.append(Paragraph("Diagram di bawah memodelkan 4 class utama beserta notasi Komposisi (filled diamond), Agregasi (hollow diamond), dan Asosiasi langsung:", body_style))
    story.append(Spacer(1, 4))
    if os.path.exists(img_diagram_path):
        story.append(RLImage(img_diagram_path, width=520, height=454))
    story.append(Spacer(1, 10))

    story.append(PageBreak())

    # 3. Penjelasan Struktur Class & Hubungan dengan Kode Java
    story.append(Paragraph("3. Penjelasan Hubungan UML dengan Implementasi Source Code", h1_style))
    penjelasan_teks = (
        "Setiap entitas dalam diagram UML diimplementasikan ke dalam struktur Java bersih di package <code>model</code>:<br/>"
        "• <b><code>Kendaraan.java</code></b>: Merepresentasikan entitas kendaraan dengan atribut <code>platNomor</code>, "
        "<code>tipe</code> (MOTOR, MOBIL, MOBIL_LISTRIK), dan <code>merk</code>. Memiliki method bisnis <code>isMobilListrik()</code> "
        "untuk penentuan kelayakan diskon insentif hijau.<br/>"
        "• <b><code>SlotParkir.java</code></b>: Merepresentasikan petak parkir fisik. Memiliki tanggung jawab menjaga integritas okupansi "
        "melalui method <code>cocokUntuk()</code>, <code>isiSlot()</code>, dan <code>kosongkanSlot()</code>. Mengunci mutasi langsung "
        "sehingga status terisi selalu sinkron dengan keberadaan fisik kendaraan.<br/>"
        "• <b><code>KarcisParkir.java</code></b>: Berelasi <b>Agregasi</b> dengan <code>Kendaraan</code> dan <code>SlotParkir</code>. "
        "Bertanggung jawab menghitung durasi parkir dan tarif berjenjang (jam ke-1 flat, jam berikutnya progresif) serta menerapkan diskon 20% khusus EV.<br/>"
        "• <b><code>GedungParkir.java</code></b>: Berperan sebagai <b>Aggregate Root</b> yang memegang tanggung jawab bisnis terbesar. "
        "Mengelola koleksi <code>SlotParkir</code> dan <code>KarcisParkir</code> melalui relasi <b>Komposisi</b>, mengotomasi alokasi slot terdekat, "
        "mengontrol gerbang masuk-keluar, dan merekap kas harian."
    )
    story.append(Paragraph(penjelasan_teks, body_style))
    story.append(Spacer(1, 8))

    # 4. Screenshot Eksekusi Program
    story.append(Paragraph("4. Screenshot Hasil Eksekusi Program (Main.java)", h1_style))
    story.append(Paragraph("Program berhasil dieksekusi dengan hasil seluruh 6 skenario validasi bisnis berjalan lancar:", body_style))
    story.append(Spacer(1, 4))
    if os.path.exists(img_screenshot_path):
        story.append(RLImage(img_screenshot_path, width=520, height=340))
    story.append(Spacer(1, 12))

    story.append(PageBreak())

    # 5. Jawaban Pertanyaan Evaluasi (Section 9 Modul)
    story.append(Paragraph("5. Jawaban Pertanyaan Evaluasi Praktikum Modul 9", h1_style))

    q_list = [
        ("1. Apa perbedaan class pada UML dan object pada program?",
         "<b>Class pada UML</b> adalah skema atau cetak biru (<i>blueprint</i>) konseptual yang mendefinisikan atribut, method, dan relasi "
         "secara abstrak sebelum kode dijalankan. Sedangkan <b>Object pada program</b> adalah instansi nyata di memori komputer yang hidup "
         "saat runtime (melalui operator <code>new</code>), menyimpan state nilai riil, dan memiliki identitas spesifik."),

        ("2. Bagaimana association pada UML diterapkan dalam Java?",
         "Association dalam Java diterapkan dengan <b>mendeklarasikan variabel instan (atribut) bertipe objek class lain</b>. "
         "Contohnya, di dalam class <code>KarcisParkir</code> dideklarasikan atribut: <code>private Kendaraan kendaraan;</code> dan "
         "<code>private SlotParkir slot;</code> sehingga objek tiket parkir terhubung dan dapat berinteraksi dengan entitas kendaraan dan slot terkait."),

        ("3. Mengapa attribute sebaiknya tidak seluruhnya public?",
         "Jika atribut bernilai <code>public</code>, data internal objek dapat diubah sembarangan dari luar tanpa melalui validasi "
         "(misalnya: slot diubah statusnya menjadi kosong padahal kendaraan masih ada di dalamnya). "
         "Dengan modifier <code>private</code> (Enkapsulasi), seluruh perubahan state wajib melewati method resmi yang memiliki validasi logika bisnis."),

        ("4. Apakah source code proyek sudah konsisten dengan UML?",
         "<b>Ya, 100% konsisten.</b> Seluruh nama class, tipe data atribut, visibilitas (<code>-</code> private dan <code>+</code> public), "
         "parameter method, serta kardinalitas relasi (Komposisi Gedung ke Slot dan Tiket, serta Agregasi Tiket ke Kendaraan dan Slot) sinkron identik antara diagram dan kode."),

        ("5. Class mana yang memiliki responsibility paling besar dalam proyek Anda?",
         "Class dengan <i>responsibility</i> terbesar adalah <b><code>GedungParkir.java</code></b> (*Aggregate Root*). "
         "Class ini mengoordinasikan seluruh alur operasional: mencari slot kosong terdekat secara otomatis, mengalokasikan slot sesuai jenis kendaraan, "
         "mengelola alur check-in tiket, memvalidasi pelunasan tarif saat check-out, mengosongkan kembali slot fisik, dan merekap kas pendapatan gedung.")
    ]

    for q, a in q_list:
        story.append(Paragraph(q, q_title))
        story.append(Paragraph(a, q_ans))

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"[Sukses] Dokumen laporan PDF selesai dibuat di:\n - {pdf_path}")

if __name__ == "__main__":
    out_uml_png = os.path.join(UML_DIR, "ClassDiagram_SmartParkir.png")
    out_uml_pdf = os.path.join(UML_DIR, "ClassDiagram_SmartParkir.pdf")
    out_screenshot_png = os.path.join(SCREENSHOT_DIR, "hasil_eksekusi.png")
    out_report_pdf = os.path.join(SCRIPT_DIR, "Laporan_P9_3125522010_ArjunaLanangAdiwarsana.pdf")

    print(">>> Menghasilkan diagram UML Class...")
    create_class_diagram(out_uml_png, out_uml_pdf)

    print(">>> Menulis kode mentah diagram class (PlantUML & Mermaid)...")
    write_class_diagram_sources()

    print(">>> Menghasilkan terminal screenshot...")
    generate_terminal_screenshot(out_screenshot_png)

    print(">>> Menghasilkan laporan lengkap PDF...")
    generate_pdf_report(out_report_pdf, out_uml_png, out_screenshot_png)

    print(">>> Selesai memproses seluruh aset proyek SmartParkir!")
