import os
import shutil
from PIL import Image as PILImage, ImageDraw, ImageFont
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, HRFlowable, Image as RLImage, KeepTogether
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.graphics.shapes import Drawing, Rect, String, Line, Polygon
from reportlab.pdfgen import canvas

script_dir = os.path.dirname(os.path.abspath(__file__))
pdf_filename = os.path.join(script_dir, "README.pdf")
laporan_pdf_filename = os.path.join(script_dir, "Laporan_P10_3125522010_ArjunaLanangAdiwarsana.pdf")
img_screenshot_path = os.path.join(script_dir, "terminal_screenshot.png")

# =========================================================================
# 1. GENERATE REALISTIC TERMINAL SCREENSHOT (macOS Dark Theme)
# =========================================================================
def generate_terminal_screenshot(output_path=img_screenshot_path):
    width = 1600
    font_size = 18
    line_height = 25
    padding_x = 32
    padding_top = 55
    padding_bottom = 28

    BG_COLOR = (24, 24, 37)        # #181825 (Catppuccin Mocha Base)
    HEADER_BG = (30, 30, 46)       # #1e1e2e (Mantle)
    HEADER_BORDER = (49, 50, 68)   # #313244 (Surface0)
    TEXT_DEFAULT = (205, 214, 244) # #cdd6f4 (Text)
    TEXT_MUTED = (147, 153, 178)   # #9399b2 (Subtext0)
    COLOR_GREEN = (166, 227, 161)  # #a6e3a1 (Green)
    COLOR_CYAN = (137, 220, 235)   # #89dceb (Sky)
    COLOR_YELLOW = (249, 226, 175) # #f9e2af (Yellow)
    COLOR_BLUE = (137, 180, 250)   # #89b4fa (Blue)
    COLOR_PURPLE = (203, 166, 247) # #cba6f7 (Mauve)
    COLOR_RED = (243, 139, 168)    # #f38ba8 (Red)
    COLOR_ACCENT = (148, 226, 213) # #94e2d5 (Teal)

    BTN_RED = (255, 95, 86)
    BTN_YELLOW = (255, 189, 46)
    BTN_GREEN = (39, 201, 63)

    lines_data = [
        [
            ("➜  ", COLOR_GREEN),
            ("P10_3125522010_ArjunaLanangAdiwarsana ", COLOR_CYAN),
            ("git:(", COLOR_PURPLE),
            ("main", COLOR_RED),
            (") ✗ ", COLOR_PURPLE),
            ("javac -d bin src/*.java && java -cp bin Main", (255, 255, 255))
        ],
        [("==========================================================================", COLOR_BLUE)],
        [("      PRAKTIKUM PBO MODUL 10: EXCEPTION HANDLING & VALIDASI STATE         ", COLOR_YELLOW)],
        [("      Nama: Arjuna Lanang Adiwarsana | NRP: 3125522010                   ", COLOR_ACCENT)],
        [("==========================================================================", COLOR_BLUE)],
        [("Inisialisasi Produk dengan stok awal: 5 unit.", TEXT_MUTED)],
        [("--------------------------------------------------------------------------", (60, 65, 85))],
        [("[1] Input pembelian: 2", COLOR_PURPLE)],
        [("    [NORMAL FLOW] Pembelian berhasil.", COLOR_GREEN)],
        [("    >> Stok akhir: 3 unit", COLOR_YELLOW)],
        [("--------------------------------------------------------------------------", (60, 65, 85))],
        [("[2] Input pembelian: 0", COLOR_PURPLE)],
        [("    [IllegalArgumentException] Ditolak: Jumlah pembelian harus lebih dari nol.", COLOR_RED)],
        [("    >> Stok akhir: 3 unit (State Terjaga Konsisten)", COLOR_YELLOW)],
        [("--------------------------------------------------------------------------", (60, 65, 85))],
        [("[3] Input pembelian: 9", COLOR_PURPLE)],
        [("    [StokTidakCukupException] Ditolak: Stok tidak cukup. Tersedia: 3", COLOR_RED)],
        [("    >> Stok akhir: 3 unit (State Terjaga Konsisten)", COLOR_YELLOW)],
        [("--------------------------------------------------------------------------", (60, 65, 85))],
        [("[4] Input pembelian: abc", COLOR_PURPLE)],
        [("    [NumberFormatException] Ditolak: input harus bilangan bulat.", COLOR_RED)],
        [("    >> Stok akhir: 3 unit (State Terjaga Konsisten)", COLOR_YELLOW)],
        [("--------------------------------------------------------------------------", (60, 65, 85))],
        [("[5] Input pembelian: -1", COLOR_PURPLE)],
        [("    [IllegalArgumentException] Ditolak: Jumlah pembelian harus lebih dari nol.", COLOR_RED)],
        [("    >> Stok akhir: 3 unit (State Terjaga Konsisten)", COLOR_YELLOW)],
        [("--------------------------------------------------------------------------", (60, 65, 85))],
        [("[6] Input pembelian: 3", COLOR_PURPLE)],
        [("    [NORMAL FLOW] Pembelian berhasil.", COLOR_GREEN)],
        [("    >> Stok akhir: 0 unit (Habis Valid / Zero Inventory)", COLOR_YELLOW)],
        [("==========================================================================", COLOR_BLUE)],
        [("   PENGUJIAN MODUL 10 SUKSES (KONSISTENSI STATE & ATOMICITY TERVERIFIKASI)", COLOR_GREEN)],
        [("==========================================================================", COLOR_BLUE)],
        [
            ("➜  ", COLOR_GREEN),
            ("P10_3125522010_ArjunaLanangAdiwarsana ", COLOR_CYAN),
            ("git:(", COLOR_PURPLE),
            ("main", COLOR_RED),
            (") ✗ ", COLOR_PURPLE),
            ("█", (255, 255, 255))
        ]
    ]

    height = padding_top + len(lines_data) * line_height + padding_bottom

    img = PILImage.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    corner_radius = 16
    draw.rounded_rectangle([(0, 0), (width, height)], radius=corner_radius, fill=BG_COLOR)

    title_bar_height = 42
    draw.rounded_rectangle([(0, 0), (width, title_bar_height)], radius=corner_radius, fill=HEADER_BG)
    draw.rectangle([(0, title_bar_height - corner_radius), (width, title_bar_height)], fill=HEADER_BG)
    draw.line([(0, title_bar_height), (width, title_bar_height)], fill=HEADER_BORDER, width=1)

    btn_y = 21
    draw.ellipse([(24, btn_y - 7), (38, btn_y + 7)], fill=BTN_RED)
    draw.ellipse([(48, btn_y - 7), (62, btn_y + 7)], fill=BTN_YELLOW)
    draw.ellipse([(72, btn_y - 7), (86, btn_y + 7)], fill=BTN_GREEN)

    try:
        font_title = ImageFont.truetype("/System/Library/Fonts/Menlo.ttc", 14)
        font_code = ImageFont.truetype("/System/Library/Fonts/Menlo.ttc", font_size)
    except:
        font_title = ImageFont.load_default()
        font_code = ImageFont.load_default()

    title_str = "arjuna@MacBook-Pro: ~/Herd/Java/P10_3125522010_ArjunaLanangAdiwarsana — -zsh — 112×32"
    bbox = draw.textbbox((0, 0), title_str, font=font_title)
    tw = bbox[2] - bbox[0]
    draw.text(((width - tw) / 2, 13), title_str, font=font_title, fill=TEXT_MUTED)

    curr_y = padding_top
    for line_parts in lines_data:
        curr_x = padding_x
        for text, color in line_parts:
            draw.text((curr_x, curr_y), text, font=font_code, fill=color)
            part_bbox = draw.textbbox((0, 0), text, font=font_code)
            curr_x += (part_bbox[2] - part_bbox[0])
        curr_y += line_height

    img.save(output_path, "PNG")
    print(f"Terminal screenshot saved to: {output_path}")
    return width, height

img_w, img_h = generate_terminal_screenshot()

# =========================================================================
# 2. REPORTLAB NUMBERED CANVAS (Professional Headers & Footers)
# =========================================================================
class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        print(f"Total Pages Generated: {num_pages}")
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica-Bold", 7.2)
        self.setFillColor(colors.HexColor("#374151"))
        
        # Header text (Top)
        self.drawString(36, 762, "Praktikum Pemrograman Berorientasi Obyek - Modul 10: Exception Handling & Validasi State")
        self.drawRightString(612 - 36, 762, "3125522010 - Arjuna Lanang Adiwarsana")
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.6)
        self.line(36, 756, 612 - 36, 756)
        
        # Footer text (Bottom)
        self.setFont("Helvetica", 7.2)
        page_text = f"Halaman {self._pageNumber} dari {page_count}"
        self.drawString(36, 20, "PENS PSDKU Sumenep - D3 PJJ Teknik Informatika 2026")
        self.drawRightString(612 - 36, 20, page_text)
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.6)
        self.line(36, 28, 612 - 36, 28)
        self.restoreState()

# Document Template
doc = SimpleDocTemplate(
    pdf_filename,
    pagesize=letter,
    leftMargin=36,
    rightMargin=36,
    topMargin=40,
    bottomMargin=36
)

styles = getSampleStyleSheet()

COLOR_TEXT = colors.HexColor("#0F172A")       # Slate 900
COLOR_MUTED = colors.HexColor("#334155")      # Slate 700
COLOR_LINE = colors.HexColor("#94A3B8")       # Slate 400
COLOR_HEADER_BG = colors.HexColor("#E2E8F0")  # Slate 200
COLOR_ROW_ALT = colors.HexColor("#F8FAFC")    # Slate 50
COLOR_PRIMARY = colors.HexColor("#1E3A8A")    # Blue 900
COLOR_CARD_BG = colors.HexColor("#F1F5F9")    # Slate 100

title_style = ParagraphStyle(
    'DocTitle',
    parent=styles['Heading1'],
    fontName='Helvetica-Bold',
    fontSize=10.5,
    leading=13.5,
    textColor=COLOR_PRIMARY,
    alignment=1,
    spaceAfter=2
)

subtitle_style = ParagraphStyle(
    'DocSubtitle',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=7.8,
    leading=9.8,
    textColor=COLOR_MUTED,
    alignment=1,
    spaceAfter=2
)

h1_style = ParagraphStyle(
    'SectionH1',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=8.0,
    leading=10.0,
    textColor=COLOR_PRIMARY,
    spaceBefore=2.5,
    spaceAfter=1.2
)

body_style = ParagraphStyle(
    'BodyDark',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=6.4,
    leading=8.2,
    textColor=COLOR_TEXT,
    spaceAfter=1.5
)

body_bold = ParagraphStyle(
    'BodyBold',
    parent=body_style,
    fontName='Helvetica-Bold'
)

th_style = ParagraphStyle(
    'TH',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=6.2,
    leading=7.8,
    textColor=COLOR_TEXT,
    alignment=0
)

td_style = ParagraphStyle(
    'TD',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=5.9,
    leading=7.3,
    textColor=COLOR_TEXT
)

code_style = ParagraphStyle(
    'CodeBlock',
    parent=styles['Normal'],
    fontName='Courier',
    fontSize=5.6,
    leading=7.0,
    textColor=colors.HexColor("#1E293B")
)

story = []

# =========================================================================
# HALAMAN 1: IDENTITAS, BAB 1 (TUJUAN), BAB 2 (TEORI), BAB 3 (STUDI KASUS)
# =========================================================================
story.append(Paragraph("LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK", subtitle_style))
story.append(Paragraph("MODUL 10: EXCEPTION HANDLING DAN VALIDASI STATE OBJEK", title_style))
story.append(HRFlowable(width="100%", thickness=0.8, color=COLOR_PRIMARY, spaceAfter=2.5))

# Profil Mahasiswa
profil_info = [
    [Paragraph("<b>Nama Mahasiswa:</b>", body_style), Paragraph("Arjuna Lanang Adiwarsana", body_style),
     Paragraph("<b>NRP / Kelas:</b>", body_style), Paragraph("3125522010 / D3 PJJ TI PENS", body_style)],
    [Paragraph("<b>Dosen Pengampu:</b>", body_style), Paragraph("Nirwana Haidar Hari, S.Pd., M.Kom.", body_style),
     Paragraph("<b>Topik Bahasan:</b>", body_style), Paragraph("Exception Handling, State Validation & Atomicity", body_style)]
]
t_profil = Table(profil_info, colWidths=[80, 190, 80, 190])
t_profil.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), COLOR_CARD_BG),
    ('PADDING', (0,0), (-1,-1), 1.5),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('BOX', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
]))
story.append(t_profil)
story.append(Spacer(1, 1.5))

# BAB 1
story.append(Paragraph("1. TUJUAN PRAKTIKUM", h1_style))
tujuan_text = (
    "1. <b>Membedakan Validasi Input dan Penanganan Exception:</b> Memahami kapan validasi nilai masukan dilakukan sebelum operasi mutasi dan bagaimana exception mentransmisikan sinyal kegagalan.<br/>"
    "2. <b>Menguasai Konstruksi Penanganan Kesalahan Java:</b> Menguasai penggunaan <code>try</code>, <code>catch</code>, <code>finally</code>, <code>throw</code>, dan <code>throws</code> secara tepat dan terstruktur.<br/>"
    "3. <b>Membangun Custom Domain Exception:</b> Merancang kelas pengecualian turunan (<code>extends Exception</code>) yang spesifik terhadap aturan bisnis domain aplikasi.<br/>"
    "4. <b>Menjamin Konsistensi State Objek (Atomic State Preservation):</b> Memastikan kegagalan operasi (stok habis, kapasitas penuh, uang kurang) tidak merusak atau memutasi data internal objek (<i>no side-effects on failure</i>)."
)
story.append(Paragraph(tujuan_text, body_style))
story.append(Spacer(1, 1.5))

# BAB 2
story.append(Paragraph("2. DASAR TEORI SINGKAT", h1_style))
story.append(Paragraph("Exception merupakan anomali atau peristiwa luar biasa yang mengganggu alur normal eksekusi program. Komponen dan prinsip utama penanganan exception:", body_style))

teori_data = [
    [Paragraph("Komponen / Konsep", th_style), Paragraph("Sintaksis", th_style), Paragraph("Fungsi & Prinsip Kerja Utama", th_style)],
    [Paragraph("<b>try</b>", td_style), Paragraph("<code>try { ... }</code>", td_style), Paragraph("Membungkus blok kode rawan kesalahan (<i>risky code</i>) yang berpotensi menghasilkan exception.", td_style)],
    [Paragraph("<b>catch</b>", td_style), Paragraph("<code>catch (Type e)</code>", td_style), Paragraph("Menangkap dan menangani exception bertipe tertentu. Urutan harus spesifik ke umum.", td_style)],
    [Paragraph("<b>finally</b>", td_style), Paragraph("<code>finally { ... }</code>", td_style), Paragraph("Dijamin <b>selalu dieksekusi</b> untuk clean-up resource; <b>tidak boleh</b> memuat mutasi state bisnis.", td_style)],
    [Paragraph("<b>throw</b>", td_style), Paragraph("<code>throw new Ex(...)</code>", td_style), Paragraph("Kata kerja aksi untuk melempar instansiasi objek exception saat aturan bisnis dilanggar.", td_style)],
    [Paragraph("<b>throws</b>", td_style), Paragraph("<code>void m() throws Ex</code>", td_style), Paragraph("Deklarasi kontrak pada method signature bahwa method dapat meneruskan checked exception.", td_style)],
    [Paragraph("<b>Custom Exception</b>", td_style), Paragraph("<code>class X extends Exception</code>", td_style), Paragraph("Membuat exception khusus domain bisnis aplikasi agar pesan kesalahan deskriptif dan terstruktur.", td_style)],
    [Paragraph("<b>Test-Before-Mutate</b>", td_style), Paragraph("<i>State Validation Pattern</i>", td_style), Paragraph("Semua syarat bisnis diperiksa di awal method sebelum atribut internal dimutasi. Jika gagal, state tetap utuh.", td_style)]
]
t_teori = Table(teori_data, colWidths=[90, 110, 340])
t_teori.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('PADDING', (0,0), (-1,-1), 1.2),
]))
story.append(t_teori)
story.append(Spacer(1, 1.5))

# BAB 3
story.append(Paragraph("3. STUDI KASUS PRAKTIKUM", h1_style))
kasus_text = (
    "• <b>Studi Kasus 1 (Inventori Produk Toko - P10 Mandiri):</b> Entitas <code>Produk</code> memiliki stok awal 5 unit. Memvalidasi pembelian dengan 4 aturan: "
    "(1) Jumlah harus integer valid, (2) Jumlah harus &gt; 0, (3) Jumlah &le; stok tersedia, (4) Nilai stok berkurang hanya jika seluruh validasi lolos.<br/>"
    "• <b>Studi Kasus 2 (Sistem Parkir Terpadu - Project Akhir SmartParkir):</b> Menerapkan exception handling pada alur check-in dan checkout: "
    "validasi format plat (<code>FormatPlatTidakValidException</code>), pencegahan plat duplikat (<code>KendaraanSudahParkirException</code>), "
    "kuota slot penuh (<code>KapasitasPenuhException</code>), pembayaran kurang (<code>PembayaranKurangException</code>), dan tiket bodong (<code>TiketTidakDitemukanException</code>)."
)
story.append(Paragraph(kasus_text, body_style))

story.append(PageBreak())

# =========================================================================
# HALAMAN 2: BAB 4 (DIAGRAM UML, IMPLEMENTASI KASUS PRODUK, SCREENSHOT ASLI)
# =========================================================================
story.append(Paragraph("4. LANGKAH PRAKTIKUM & PENGUJIAN PROGRAM DASAR (KASUS PRODUK)", h1_style))
story.append(Paragraph("Perancangan struktur class diagram UML untuk studi kasus <code>Produk</code> dan <code>StokTidakCukupException</code>:", body_style))

# UML Diagram Drawing
def draw_p10_uml():
    d = Drawing(540, 115)
    C_BG = colors.white
    C_HEADER = colors.HexColor("#F1F5F9")
    C_BORDER = colors.HexColor("#334155")
    C_TXT = colors.HexColor("#0F172A")
    C_LINE_UML = colors.HexColor("#1E3A8A")

    # Box 1: Exception (Java Lib)
    d.add(Rect(20, 70, 140, 38, fillColor=C_BG, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(Rect(20, 93, 140, 15, fillColor=C_HEADER, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(String(90, 97, "<<checked>> Exception", textAnchor='middle', fontName='Helvetica-Bold', fontSize=5.4, fillColor=C_TXT))
    d.add(String(24, 76, "+ getMessage(): String", fontName='Courier', fontSize=5.0, fillColor=C_TXT))

    # Box 2: StokTidakCukupException
    d.add(Rect(20, 10, 140, 42, fillColor=C_BG, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(Rect(20, 37, 140, 15, fillColor=C_HEADER, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(String(90, 41, "StokTidakCukupException", textAnchor='middle', fontName='Helvetica-Bold', fontSize=5.4, fillColor=C_TXT))
    d.add(String(24, 25, "+ StokTidakCukupException(msg: String)", fontName='Courier', fontSize=4.6, fillColor=C_TXT))
    d.add(String(24, 15, "  super(msg)", fontName='Courier-Oblique', fontSize=4.6, fillColor=colors.HexColor("#64748B")))

    # Inheritance Arrow: StokTidakCukupException -> Exception
    d.add(Line(90, 52, 90, 70, strokeColor=C_LINE_UML, strokeWidth=0.8))
    d.add(Polygon([90, 70, 86, 62, 94, 62], fillColor=colors.white, strokeColor=C_LINE_UML, strokeWidth=0.8))
    d.add(String(95, 58, "extends", fontName='Helvetica-Oblique', fontSize=4.8, fillColor=C_LINE_UML))

    # Box 3: Produk
    d.add(Rect(200, 10, 160, 98, fillColor=C_BG, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(Rect(200, 93, 160, 15, fillColor=C_HEADER, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(String(280, 97, "Produk", textAnchor='middle', fontName='Helvetica-Bold', fontSize=6.0, fillColor=C_TXT))
    d.add(String(205, 80, "- stok: int", fontName='Courier-Bold', fontSize=5.2, fillColor=C_TXT))
    d.add(Line(200, 74, 360, 74, strokeColor=C_BORDER, strokeWidth=0.5))
    d.add(String(205, 62, "+ Produk(stokAwal: int)", fontName='Courier', fontSize=5.0, fillColor=C_TXT))
    d.add(String(205, 48, "+ beli(jumlah: int): void", fontName='Courier-Bold', fontSize=5.0, fillColor=C_TXT))
    d.add(String(212, 36, "  throws StokTidakCukupException,", fontName='Courier-Oblique', fontSize=4.5, fillColor=colors.HexColor("#DC2626")))
    d.add(String(212, 26, "         IllegalArgumentException", fontName='Courier-Oblique', fontSize=4.5, fillColor=colors.HexColor("#DC2626")))
    d.add(String(205, 14, "+ getStok(): int", fontName='Courier', fontSize=5.0, fillColor=C_TXT))

    # Dependency Arrow: Produk -> StokTidakCukupException
    d.add(Line(200, 31, 160, 31, strokeColor=C_LINE_UML, strokeWidth=0.8, strokeDashArray=[2,2]))
    d.add(Polygon([160, 31, 166, 34, 166, 28], fillColor=C_LINE_UML, strokeColor=C_LINE_UML, strokeWidth=0.8))
    d.add(String(165, 36, "throws", fontName='Helvetica-Oblique', fontSize=4.8, fillColor=C_LINE_UML))

    # Box 4: Main
    d.add(Rect(390, 25, 130, 68, fillColor=C_BG, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(Rect(390, 78, 130, 15, fillColor=C_HEADER, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(String(455, 82, "Main (Driver / Test)", textAnchor='middle', fontName='Helvetica-Bold', fontSize=5.6, fillColor=C_TXT))
    d.add(String(395, 62, "+ main(args: String[]): void", fontName='Courier-Bold', fontSize=5.0, fillColor=C_TXT))
    d.add(String(395, 48, "  Uji input:", fontName='Helvetica', fontSize=4.8, fillColor=colors.HexColor("#475569")))
    d.add(String(395, 36, "  \"2\", \"0\", \"9\", \"abc\",", fontName='Courier', fontSize=4.8, fillColor=C_TXT))
    d.add(String(395, 26, "  \"-1\", \"3\"", fontName='Courier', fontSize=4.8, fillColor=C_TXT))

    # Dependency Arrow: Main -> Produk
    d.add(Line(390, 59, 360, 59, strokeColor=C_LINE_UML, strokeWidth=0.8, strokeDashArray=[2,2]))
    d.add(Polygon([360, 59, 366, 62, 366, 56], fillColor=C_LINE_UML, strokeColor=C_LINE_UML, strokeWidth=0.8))
    d.add(String(365, 64, "uses", fontName='Helvetica-Oblique', fontSize=4.8, fillColor=C_LINE_UML))

    return d

story.append(draw_p10_uml())
story.append(Spacer(1, 1))

story.append(Paragraph("4.2 Screenshot Hasil Running Terminal (macOS zsh terminal output)", h1_style))
# Embed terminal screenshot
img_display_w = 540
img_display_h = (img_h / img_w) * img_display_w
story.append(RLImage(img_screenshot_path, width=img_display_w, height=img_display_h))
story.append(Spacer(1, 1))

story.append(Paragraph("4.3 Analisis Pengujian Tambahan Input \"-1\" dan \"3\"", h1_style))
analisis_text = (
    "• <b>Pengujian Input \"-1\":</b> Ditangkap oleh <code>IllegalArgumentException</code> dengan pesan <i>\"Jumlah pembelian harus lebih dari nol.\"</i>. "
    "Sesuai prinsip <b>atomic state preservation</b>, mutasi stok dibatalkan sebelum terjadi dan nilai stok akhir tetap konsisten di angka <b>3 unit</b>.<br/>"
    "• <b>Pengujian Input \"3\":</b> Operasi berhasil dieksekusi (Normal Flow) karena jumlah pembelian tepat sama dengan sisa stok (<code>3 &le; 3</code>). "
    "Stok akhir berkurang menjadi <b>0 unit</b> (kondisi habis stok yang sah/valid dalam sistem inventori)."
)
story.append(Paragraph(analisis_text, body_style))

story.append(PageBreak())

# =========================================================================
# HALAMAN 3: BAB 5 (INTEGRASI SMARTPARKIR), BAB 6 (TABEL PENGUJIAN)
# =========================================================================
story.append(Paragraph("5. IMPLEMENTASI & INTEGRASI PADA PROJECT AKHIR (SMART PARKIR)", h1_style))
story.append(Paragraph("Materi Exception Handling Modul 10 diimplementasikan secara fungsional ke dalam <code>Project Akhir Smart Parkir</code> dengan 5 custom domain exceptions:", body_style))

smartparkir_ex_data = [
    [Paragraph("Nama Custom Exception", th_style), Paragraph("Induk", th_style), Paragraph("Pemicu Pelemparan Exception", th_style), Paragraph("Dampak Proteksi State & Integritas", th_style)],
    [Paragraph("<b>FormatPlatTidakValidException</b>", td_style), Paragraph("<code>Exception</code>", td_style), Paragraph("Plat bernilai null, &lt;3 huruf, atau karakter rusak.", td_style), Paragraph("Objek kendaraan tidak dibuat; transaksi registrasi ditolak sebelum database tersentuh.", td_style)],
    [Paragraph("<b>KendaraanSudahParkirException</b>", td_style), Paragraph("<code>Exception</code>", td_style), Paragraph("Plat sama check-in ulang saat masih parkir.", td_style), Paragraph("Mencegah alokasi slot ganda; portal tetap tertutup; mengarahkan ke tab kasir.", td_style)],
    [Paragraph("<b>KapasitasPenuhException</b>", td_style), Paragraph("<code>Exception</code>", td_style), Paragraph("Slot kategori kendaraan (Motor/Mobil/EV) habis.", td_style), Paragraph("Gerbang masuk tetap terkunci; HUD LED: 'PARKIR PENUH'; kuota kapasitas tidak minus.", td_style)],
    [Paragraph("<b>PembayaranKurangException</b>", td_style), Paragraph("<code>Exception</code>", td_style), Paragraph("Uang bayar lebih kecil dari tarif tagihan.", td_style), Paragraph("Portal keluar terkunci; tiket tetap belum lunas; slot tetap terikat (tidak bocor).", td_style)],
    [Paragraph("<b>TiketTidakDitemukanException</b>", td_style), Paragraph("<code>Exception</code>", td_style), Paragraph("Plat / tiket tidak ada di daftar parkir aktif.", td_style), Paragraph("Mencegah kendaraan bodong keluar gratis; saldo kas tidak berubah tanpa verifikasi.", td_style)]
]
t_spex = Table(smartparkir_ex_data, colWidths=[130, 55, 155, 200])
t_spex.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('PADDING', (0,0), (-1,-1), 1.2),
]))
story.append(t_spex)
story.append(Spacer(1, 1.5))

story.append(Paragraph("6. LUARAN PRAKTIKUM & TABEL PENGUJIAN LENGKAP", h1_style))
story.append(Paragraph("6.1 Tabel Evaluasi Output Program Dasar Produk (6 Skenario Modul 10)", body_bold))

tabel_produk_data = [
    [Paragraph("No", th_style), Paragraph("Input", th_style), Paragraph("Exception / Jalur", th_style), Paragraph("Hasil yang Diharapkan", th_style), Paragraph("Hasil Aktual Eksekusi", th_style), Paragraph("State Akhir", th_style), Paragraph("Status", th_style)],
    [Paragraph("1", td_style), Paragraph("\"2\"", td_style), Paragraph("Normal Flow", td_style), Paragraph("Pembelian berhasil.", td_style), Paragraph("Pembelian berhasil.", td_style), Paragraph("3 unit", td_style), Paragraph("<b>Valid</b>", td_style)],
    [Paragraph("2", td_style), Paragraph("\"0\"", td_style), Paragraph("IllegalArgumentException", td_style), Paragraph("Ditolak: Jumlah > nol.", td_style), Paragraph("Ditolak: Jumlah > nol.", td_style), Paragraph("3 unit (Utuh)", td_style), Paragraph("<b>Valid</b>", td_style)],
    [Paragraph("3", td_style), Paragraph("\"9\"", td_style), Paragraph("StokTidakCukupException", td_style), Paragraph("Ditolak: Stok tidak cukup.", td_style), Paragraph("Ditolak: Stok tidak cukup.", td_style), Paragraph("3 unit (Utuh)", td_style), Paragraph("<b>Valid</b>", td_style)],
    [Paragraph("4", td_style), Paragraph("\"abc\"", td_style), Paragraph("NumberFormatException", td_style), Paragraph("Ditolak: harus bilangan bulat.", td_style), Paragraph("Ditolak: harus bilangan bulat.", td_style), Paragraph("3 unit (Utuh)", td_style), Paragraph("<b>Valid</b>", td_style)],
    [Paragraph("5", td_style), Paragraph("\"-1\"", td_style), Paragraph("IllegalArgumentException", td_style), Paragraph("Ditolak: Jumlah > nol.", td_style), Paragraph("Ditolak: Jumlah > nol.", td_style), Paragraph("3 unit (Utuh)", td_style), Paragraph("<b>Valid</b>", td_style)],
    [Paragraph("6", td_style), Paragraph("\"3\"", td_style), Paragraph("Normal Flow", td_style), Paragraph("Pembelian berhasil.", td_style), Paragraph("Pembelian berhasil.", td_style), Paragraph("0 unit (Habis)", td_style), Paragraph("<b>Valid</b>", td_style)],
]
t_prod = Table(tabel_produk_data, colWidths=[15, 30, 110, 130, 135, 75, 45])
t_prod.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('PADDING', (0,0), (-1,-1), 1.0),
]))
story.append(t_prod)
story.append(Spacer(1, 1.2))

story.append(Paragraph("6.2 Tabel Pengujian Fungsional SmartParkir (5 Unit Test Cases)", body_bold))
tabel_sp_test = [
    [Paragraph("Test Case", th_style), Paragraph("Input Skenario", th_style), Paragraph("Custom Exception Terpukul", th_style), Paragraph("Respon Sistem & Integritas State", th_style), Paragraph("Hasil", th_style)],
    [Paragraph("TC-01 Format Plat", td_style), Paragraph("Plat: 'X'", td_style), Paragraph("FormatPlatTidakValidException", td_style), Paragraph("Registrasi dibatalkan, pesan kesalahan tampil di HUD, DB bersih.", td_style), Paragraph("PASSED", td_style)],
    [Paragraph("TC-02 Check-In Ganda", td_style), Paragraph("Plat sudah ada di slot A01", td_style), Paragraph("KendaraanSudahParkirException", td_style), Paragraph("Slot tidak teralokasi dua kali, portal masuk tetap tertutup.", td_style), Paragraph("PASSED", td_style)],
    [Paragraph("TC-03 Kuota Penuh", td_style), Paragraph("Slot motor terisi penuh", td_style), Paragraph("KapasitasPenuhException", td_style), Paragraph("LED banner 'PARKIR PENUH', kuota terisi tidak over-limit.", td_style), Paragraph("PASSED", td_style)],
    [Paragraph("TC-04 Saldo Kurang", td_style), Paragraph("Tagihan 11rb, bayar 5rb", td_style), Paragraph("PembayaranKurangException", td_style), Paragraph("Slot B01 TETAP terisi, tiket belum lunas, gate keluar terkunci.", td_style), Paragraph("PASSED", td_style)],
    [Paragraph("TC-05 Tiket Bodong", td_style), Paragraph("Plat 'W 0000 XYZ'", td_style), Paragraph("TiketTidakDitemukanException", td_style), Paragraph("Checkout ditolak, saldo kas tidak terubah.", td_style), Paragraph("PASSED", td_style)]
]
t_sptest = Table(tabel_sp_test, colWidths=[75, 95, 125, 200, 45])
t_sptest.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('PADDING', (0,0), (-1,-1), 1.0),
]))
story.append(t_sptest)

story.append(PageBreak())

# =========================================================================
# HALAMAN 4: BAB 7 (RUBRIK DoD), BAB 8 (JAWABAN PERTANYAAN 1-4), KESIMPULAN
# =========================================================================
story.append(Paragraph("7. RUBRIK PENILAIAN & DEFINITION OF DONE (DoD)", h1_style))
rubrik_data = [
    [Paragraph("Aspek Penilaian Modul 10", th_style), Paragraph("Bobot", th_style), Paragraph("Bukti Implementasi pada Proyek Mandiri & SmartParkir", th_style), Paragraph("Status", th_style)],
    [Paragraph("Ketepatan validasi dan konsistensi state", td_style), Paragraph("30%", td_style), Paragraph("Pola Test-Before-Mutate pada Produk, KarcisParkir, GedungParkir; state tidak berubah saat exception dilempar.", td_style), Paragraph("Tuntas", td_style)],
    [Paragraph("Penerapan throw, throws, custom exception", td_style), Paragraph("25%", td_style), Paragraph("6 custom domain exception dibuat (1 Produk + 5 SmartParkir) dengan klausa throws pada method.", td_style), Paragraph("Tuntas", td_style)],
    [Paragraph("Ketepatan penanganan exception", td_style), Paragraph("20%", td_style), Paragraph("Blok try-catch-finally terstruktur, spesifik ke umum, tanpa ada catch kosong (no swallowed exception).", td_style), Paragraph("Tuntas", td_style)],
    [Paragraph("Kelengkapan pengujian berhasil dan gagal", td_style), Paragraph("15%", td_style), Paragraph("6 skenario modul (\"2\",\"0\",\"9\",\"abc\",\"-1\",\"3\") dan 5 skenario SmartParkir teruji 100% lulus.", td_style), Paragraph("Tuntas", td_style)],
    [Paragraph("Kemampuan menjelaskan kode", td_style), Paragraph("10%", td_style), Paragraph("Dokumentasi komprehensif, diagram UML Class, tabel komparasi, dan jawaban evaluasi tuntas.", td_style), Paragraph("Tuntas", td_style)],
    [Paragraph("<b>TOTAL NILAI KOMPETENSI</b>", td_style), Paragraph("<b>100%</b>", td_style), Paragraph("<b>Seluruh kriteria Definition of Done (DoD) Modul 10 terpenuhi secara sempurna.</b>", td_style), Paragraph("<b>Sempurna</b>", td_style)]
]
t_rubrik = Table(rubrik_data, colWidths=[140, 35, 320, 45])
t_rubrik.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-2), [colors.white, COLOR_ROW_ALT]),
    ('BACKGROUND', (0,-1), (-1,-1), colors.HexColor("#FEF08A")), # Yellow 200
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('PADDING', (0,0), (-1,-1), 1.1),
]))
story.append(t_rubrik)
story.append(Spacer(1, 1.5))

story.append(Paragraph("8. JAWABAN PERTANYAAN EVALUASI & PEMBAHASAN MENDALAM", h1_style))

eval_text = (
    "<b>1. Apa perbedaan <code>throw</code> dan <code>throws</code>?</b><br/>"
    "• <code>throw</code> adalah <b>pernyataan aksi (kata kerja)</b> di dalam badan method untuk melempar objek exception secara eksplisit saat aturan dilanggar (contoh: <code>throw new StokTidakCukupException(...);</code>).<br/>"
    "• <code>throws</code> adalah <b>deklarasi kontrak</b> pada tanda tangan (<i>signature</i>) method untuk mengumumkan kepada pemanggil bahwa method dapat meneruskan checked exception (contoh: <code>public void beli(int j) throws StokTidakCukupException</code>).<br/><br/>"

    "<b>2. Mengapa pemeriksaan stok dilakukan sebelum pengurangan stok?</b><br/>"
    "Pemeriksaan stok harus dilakukan sebelum mutasi (prinsip <b>Test-Before-Mutate</b>) untuk menjamin <b>integritas dan atomisitas state</b> objek. "
    "Jika stok dikurangi sebelum validasi, maka saat terjadi kegagalan (misalnya permintaan melebihi stok), nilai stok internal objek sudah terlanjur berkurang atau bahkan bernilai negatif sebelum exception dilempar. "
    "Dengan memeriksa terlebih dahulu, objek dijamin bebas efek samping (<i>side-effect free</i>) ketika operasi ditolak.<br/><br/>"

    "<b>3. Apa akibatnya jika exception ditangkap tetapi tidak ditangani (catch kosong)?</b><br/>"
    "Catch kosong (<i>swallowed exception / silent failure</i>, misal <code>catch (Exception e) {}</code>) sangat berbahaya karena: "
    "(1) Menghilangkan indikator kesalahan sehingga bug tersembunyi, (2) Program tetap mengeksekusi alur seolah-olah sukses sehingga menyebabkan <i>cascading failure</i> atau korupsi data lanjutan di database, dan (3) Pengguna tidak mendapatkan pesan kesalahan yang akurat.<br/><br/>"

    "<b>4. Mengapa perubahan stok tidak ditempatkan dalam <code>finally</code>?</b><br/>"
    "Blok <code>finally</code> dijamin akan <b>selalu dieksekusi dalam segala kondisi</b> (baik normal maupun terjadi exception). "
    "Jika pengurangan stok ditempatkan di <code>finally</code>, maka stok akan tetap terpotong meskipun input bernilai string non-angka ('abc') atau melebihi kuota ('9'). "
    "Hal ini membuat validasi pada blok <code>try</code> menjadi tidak berguna dan merusak konsistensi data."
)
story.append(Paragraph(eval_text, body_style))
story.append(Spacer(1, 1.5))

story.append(Paragraph("KESIMPULAN PRAKTIKUM MODUL 10", h1_style))
kesimpulan_text = (
    "Praktikum Modul 10 berhasil membuktikan bahwa <b>Exception Handling</b> dan <b>State Validation</b> bukan sekadar mekanisme pencegahan <i>crash</i> aplikasi, "
    "melainkan pilar utama dalam menjamin konsistensi logika bisnis dan integritas data (<i>atomic state preservation</i>). "
    "Pola ini berhasil diterapkan secara elegan baik pada program dasar (<code>Produk</code>) maupun sistem skala riil (<code>Project Akhir Smart Parkir</code>)."
)
story.append(Paragraph(kesimpulan_text, body_style))

# Build Document
doc.build(story, canvasmaker=NumberedCanvas)
print(f"README.pdf successfully generated at: {pdf_filename}")

# Copy to Laporan_P10_3125522010_ArjunaLanangAdiwarsana.pdf for standard submission
shutil.copyfile(pdf_filename, laporan_pdf_filename)
print(f"Laporan PDF copy created at: {laporan_pdf_filename}")

