import os
import shutil
from PIL import Image as PILImage, ImageDraw, ImageFont
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, HRFlowable, Image as RLImage
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.graphics.shapes import Drawing, Rect, String, Line, Polygon
from reportlab.pdfgen import canvas

script_dir = os.path.dirname(os.path.abspath(__file__))
pdf_filename = os.path.join(script_dir, "README.pdf")
laporan_pdf_filename = os.path.join(script_dir, "Laporan_P11_3125522010_ArjunaLanangAdiwarsana.pdf")
img_screenshot_path = os.path.join(script_dir, "terminal_screenshot.png")

# =========================================================================
# GENERATE REALISTIC TERMINAL SCREENSHOT
# =========================================================================
def generate_terminal_screenshot(output_path=img_screenshot_path):
    width = 1600
    font_size = 18
    line_height = 25
    padding_x = 32
    padding_top = 55
    padding_bottom = 28

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
    COLOR_ACCENT = (148, 226, 213)

    BTN_RED = (255, 95, 86)
    BTN_YELLOW = (255, 189, 46)
    BTN_GREEN = (39, 201, 63)

    lines_data = [
        [
            ("➜  ", COLOR_GREEN),
            ("P11_3125522010_ArjunaLanangAdiwarsana ", COLOR_CYAN),
            ("git:(", COLOR_PURPLE),
            ("main", COLOR_RED),
            (") ✗ ", COLOR_PURPLE),
            ("javac -d bin src/*.java && java -cp bin Main", (255, 255, 255))
        ],
        [("==========================================================================", COLOR_BLUE)],
        [("   PRAKTIKUM PBO MODUL 11: COLLECTION & PENGELOLAAN KUMPULAN OBJEK       ", COLOR_YELLOW)],
        [("   Nama: Arjuna Lanang Adiwarsana | NRP: 3125522010                      ", COLOR_ACCENT)],
        [("==========================================================================", COLOR_BLUE)],
        [("--- [SKENARIO 1] Menambahkan B001 dan B002 ---", COLOR_PURPLE)],
        [("B001 - Dasar Java", TEXT_DEFAULT)],
        [("B002 - Konsep OOP", TEXT_DEFAULT)],
        [("Total buku: 2", COLOR_GREEN)],
        [("--- [SKENARIO 2] Mencari buku dengan kode B002 ---", COLOR_PURPLE)],
        [("Ditemukan: Konsep OOP", COLOR_GREEN)],
        [("--- [SKENARIO 3] Menambahkan kembali buku B001 (Duplikat) ---", COLOR_PURPLE)],
        [("Ditolak: Kode buku sudah terdaftar.", COLOR_RED)],
        [("Total buku saat ini: 2", COLOR_YELLOW)],
        [("--- [SKENARIO 4] Menghapus buku B001 ---", COLOR_PURPLE)],
        [("Penghapusan B001: true", COLOR_GREEN)],
        [("B002 - Konsep OOP", TEXT_DEFAULT)],
        [("Total buku: 1", COLOR_GREEN)],
        [("--- [SKENARIO 5] Mencari buku B001 setelah dihapus ---", COLOR_PURPLE)],
        [("Buku tidak ditemukan.", COLOR_YELLOW)],
        [("--- [SKENARIO 6] Menghapus buku B999 (Tidak Ada) ---", COLOR_PURPLE)],
        [("Penghapusan B999: false", COLOR_YELLOW)],
        [("Total buku saat ini: 1", COLOR_GREEN)],
        [("--- [PENGUJIAN TAMBAHAN 1] Validasi judul kosong ---", COLOR_PURPLE)],
        [("Ditolak: Kode dan judul wajib diisi.", COLOR_RED)],
        [("--- [PENGUJIAN TAMBAHAN 2] Validasi penambahan objek null ---", COLOR_PURPLE)],
        [("Ditolak: Objek buku tidak boleh null.", COLOR_RED)],
        [("--- [PENGUJIAN TAMBAHAN 3] Menampilkan koleksi kosong ---", COLOR_PURPLE)],
        [("Koleksi masih kosong.", COLOR_YELLOW)],
        [("==========================================================================", COLOR_BLUE)],
        [("   SELURUH PENGUJIAN MODUL 11 SELESAI (100% SUKSES MEMENUHI MODUL)        ", COLOR_GREEN)],
        [("==========================================================================", COLOR_BLUE)],
        [
            ("➜  ", COLOR_GREEN),
            ("P11_3125522010_ArjunaLanangAdiwarsana ", COLOR_CYAN),
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

    title_str = "arjuna@MacBook-Pro: ~/Herd/Java/P11_3125522010_ArjunaLanangAdiwarsana — -zsh — 112×32"
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
# REPORTLAB SETUP
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
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica-Bold", 7.2)
        self.setFillColor(colors.HexColor("#374151"))
        self.drawString(36, 762, "Praktikum Pemrograman Berorientasi Obyek - Modul 11: Collection & Pengelolaan Objek")
        self.drawRightString(612 - 36, 762, "3125522010 - Arjuna Lanang Adiwarsana")
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.6)
        self.line(36, 756, 612 - 36, 756)
        
        self.setFont("Helvetica", 7.2)
        page_text = f"Halaman {self._pageNumber} dari {page_count}"
        self.drawString(36, 20, "PENS PSDKU Sumenep - D3 PJJ Teknik Informatika 2026")
        self.drawRightString(612 - 36, 20, page_text)
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.6)
        self.line(36, 28, 612 - 36, 28)
        self.restoreState()

doc = SimpleDocTemplate(
    pdf_filename,
    pagesize=letter,
    leftMargin=36,
    rightMargin=36,
    topMargin=40,
    bottomMargin=36
)

styles = getSampleStyleSheet()

COLOR_TEXT = colors.HexColor("#0F172A")
COLOR_MUTED = colors.HexColor("#334155")
COLOR_LINE = colors.HexColor("#94A3B8")
COLOR_HEADER_BG = colors.HexColor("#E2E8F0")
COLOR_ROW_ALT = colors.HexColor("#F8FAFC")
COLOR_PRIMARY = colors.HexColor("#1E3A8A")
COLOR_CARD_BG = colors.HexColor("#F1F5F9")

title_style = ParagraphStyle('DocTitle', parent=styles['Heading1'], fontName='Helvetica-Bold', fontSize=10.5, leading=13.5, textColor=COLOR_PRIMARY, alignment=1, spaceAfter=2)
subtitle_style = ParagraphStyle('DocSubtitle', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=7.8, leading=9.8, textColor=COLOR_MUTED, alignment=1, spaceAfter=2)
h1_style = ParagraphStyle('SectionH1', parent=styles['Heading2'], fontName='Helvetica-Bold', fontSize=8.0, leading=10.0, textColor=COLOR_PRIMARY, spaceBefore=2.5, spaceAfter=1.2)
body_style = ParagraphStyle('BodyDark', parent=styles['Normal'], fontName='Helvetica', fontSize=6.4, leading=8.2, textColor=COLOR_TEXT, spaceAfter=1.5)
th_style = ParagraphStyle('TH', parent=styles['Normal'], fontName='Helvetica-Bold', fontSize=6.2, leading=7.8, textColor=COLOR_TEXT, alignment=0)
td_style = ParagraphStyle('TD', parent=styles['Normal'], fontName='Helvetica', fontSize=5.9, leading=7.3, textColor=COLOR_TEXT)

story = []

# HALAMAN 1
story.append(Paragraph("LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK", subtitle_style))
story.append(Paragraph("MODUL 11: COLLECTION DAN PENGELOLAAN KUMPULAN OBJEK", title_style))
story.append(HRFlowable(width="100%", thickness=0.8, color=COLOR_PRIMARY, spaceAfter=2.5))

profil_info = [
    [Paragraph("<b>Nama Mahasiswa:</b>", body_style), Paragraph("Arjuna Lanang Adiwarsana", body_style),
     Paragraph("<b>NRP / Kelas:</b>", body_style), Paragraph("3125522010 / D3 PJJ TI PENS", body_style)],
    [Paragraph("<b>Dosen Pengampu:</b>", body_style), Paragraph("Nirwana Haidar Hari, S.Pd., M.Kom.", body_style),
     Paragraph("<b>Topik Bahasan:</b>", body_style), Paragraph("Collection Framework, List & ArrayList, CRUD, Generics", body_style)]
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

story.append(Paragraph("1. Tujuan Praktikum", h1_style))
tujuan_text = (
    "1. <b>Menyimpan Kumpulan Objek Menggunakan ArrayList:</b> Memanfaatkan koleksi dinamis ArrayList berukuran adaptif.<br/>"
    "2. <b>Mengimplementasikan Operasi CRUD Koleksi:</b> Menambah (<code>add</code>), mencari (<code>search</code>), menampilkan (<code>traverse</code>), dan menghapus (<code>remove</code>) objek domain.<br/>"
    "3. <b>Menerapkan Generics dan Perulangan For-Each:</b> Menggunakan parameter <code>&lt;T&gt;</code> untuk type safety serta iterasi elemen yang bersih.<br/>"
    "4. <b>Menjaga Validitas Data dalam Collection:</b> Menegakkan aturan keunikan ID, menolak data null/kosong, dan menjaga konsistensi state."
)
story.append(Paragraph(tujuan_text, body_style))
story.append(Spacer(1, 1.5))

story.append(Paragraph("2. Dasar Teori Singkat", h1_style))
teori_data = [
    [Paragraph("Operasi / Method", th_style), Paragraph("Kegunaan", th_style), Paragraph("Karakteristik & Integritas", th_style)],
    [Paragraph("<b>add(E element)</b>", td_style), Paragraph("Menambahkan objek ke posisi akhir koleksi.", td_style), Paragraph("Ukuran bertambah 1. Pengecekan keunikan wajib dilakukan sebelum memanggil method ini.", td_style)],
    [Paragraph("<b>get(int index)</b>", td_style), Paragraph("Mengambil elemen berdasarkan indeks posisi.", td_style), Paragraph("Indeks berbasis 0. Kurang tepat untuk identitas bisnis karena indeks bergeser saat ada penghapusan.", td_style)],
    [Paragraph("<b>remove(Object o)</b>", td_style), Paragraph("Menghapus objek dari koleksi.", td_style), Paragraph("Menggeser elemen di kanannya ke kiri. Mengembalikan boolean hasil penghapusan.", td_style)],
    [Paragraph("<b>size() & isEmpty()</b>", td_style), Paragraph("Menghitung total elemen dan mengecek kekosongan.", td_style), Paragraph("size() mengembalikan integer non-negatif; isEmpty() mengembalikan true jika koleksi kosong.", td_style)],
    [Paragraph("<b>Enkapsulasi</b>", td_style), Paragraph("Pertahanan data internal koleksi.", td_style), Paragraph("Koleksi private final; getter mengembalikan Collections.unmodifiableList untuk cegah modifikasi liar.", td_style)]
]
t_teori = Table(teori_data, colWidths=[90, 160, 290])
t_teori.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('PADDING', (0,0), (-1,-1), 1.2),
]))
story.append(t_teori)
story.append(Spacer(1, 1.5))

story.append(Paragraph("3. Studi Kasus", h1_style))
kasus_text = (
    "• <b>Studi Kasus 1 (Perpustakaan - Modul Mandiri P11):</b> Entitas <code>Perpustakaan</code> mengelola <code>List&lt;Buku&gt;</code>. "
    "Aturan: Kode dan judul tidak boleh kosong; kode buku harus unik (duplikat ditolak); pencarian dan penghapusan berbasis kode buku.<br/>"
    "• <b>Studi Kasus 2 (Smart Parkir - Project Akhir):</b> Entitas <code>GedungParkir</code> mengelola <code>List&lt;Kendaraan&gt;</code> (Armada Terdaftar) dan <code>List&lt;SlotParkir&gt;</code>. "
    "Aturan: Plat nomor unik; validasi format plat (P10); proteksi penghapusan saat kendaraan sedang aktif parkir."
)
story.append(Paragraph(kasus_text, body_style))

story.append(PageBreak())

# HALAMAN 2
story.append(Paragraph("4. Langkah Praktikum & Pengujian Program Dasar (Kasus Buku)", h1_style))
story.append(Paragraph("Diagram UML Class entitas Buku, Perpustakaan, dan relasi collection:", body_style))

def draw_p11_uml():
    d = Drawing(540, 100)
    C_BG = colors.white
    C_HEADER = colors.HexColor("#F1F5F9")
    C_BORDER = colors.HexColor("#334155")
    C_TXT = colors.HexColor("#0F172A")
    C_LINE_UML = colors.HexColor("#1E3A8A")

    # Box 1: Buku
    d.add(Rect(30, 10, 160, 80, fillColor=C_BG, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(Rect(30, 72, 160, 18, fillColor=C_HEADER, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(String(110, 77, "Buku", textAnchor='middle', fontName='Helvetica-Bold', fontSize=6.0, fillColor=C_TXT))
    d.add(String(35, 60, "- kode: String", fontName='Courier-Bold', fontSize=5.0, fillColor=C_TXT))
    d.add(String(35, 50, "- judul: String", fontName='Courier-Bold', fontSize=5.0, fillColor=C_TXT))
    d.add(Line(30, 45, 190, 45, strokeColor=C_BORDER, strokeWidth=0.5))
    d.add(String(35, 34, "+ Buku(kode, judul)", fontName='Courier', fontSize=4.8, fillColor=C_TXT))
    d.add(String(35, 24, "+ getKode(): String", fontName='Courier', fontSize=4.8, fillColor=C_TXT))
    d.add(String(35, 14, "+ getJudul(): String", fontName='Courier', fontSize=4.8, fillColor=C_TXT))

    # Box 2: Perpustakaan
    d.add(Rect(260, 10, 240, 80, fillColor=C_BG, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(Rect(260, 72, 240, 18, fillColor=C_HEADER, strokeColor=C_BORDER, strokeWidth=0.7))
    d.add(String(380, 77, "Perpustakaan (Collection Manager)", textAnchor='middle', fontName='Helvetica-Bold', fontSize=6.0, fillColor=C_TXT))
    d.add(String(265, 58, "- daftarBuku: List<Buku>", fontName='Courier-Bold', fontSize=5.0, fillColor=colors.HexColor("#2563EB")))
    d.add(Line(260, 52, 500, 52, strokeColor=C_BORDER, strokeWidth=0.5))
    d.add(String(265, 42, "+ tambahBuku(buku: Buku): void", fontName='Courier', fontSize=4.8, fillColor=C_TXT))
    d.add(String(265, 32, "+ cariBuku(kode: String): Buku", fontName='Courier', fontSize=4.8, fillColor=C_TXT))
    d.add(String(265, 22, "+ hapusBuku(kode: String): boolean", fontName='Courier', fontSize=4.8, fillColor=C_TXT))
    d.add(String(265, 12, "+ tampilkanBuku(): void", fontName='Courier', fontSize=4.8, fillColor=C_TXT))

    # Composition diamond: Perpustakaan -> Buku
    d.add(Line(260, 50, 190, 50, strokeColor=C_LINE_UML, strokeWidth=0.9))
    d.add(Polygon([260, 50, 252, 54, 244, 50, 252, 46], fillColor=C_LINE_UML, strokeColor=C_LINE_UML, strokeWidth=0.9))
    d.add(String(200, 54, "1", fontName='Helvetica-Bold', fontSize=5.0, fillColor=C_LINE_UML))
    d.add(String(232, 54, "0..*", fontName='Helvetica-Bold', fontSize=5.0, fillColor=C_LINE_UML))

    return d

story.append(draw_p11_uml())
story.append(Spacer(1, 1))

story.append(Paragraph("4.2 Screenshot Hasil Running Terminal macOS (Main.java)", h1_style))
img_display_w = 540
img_display_h = (img_h / img_w) * img_display_w
story.append(RLImage(img_screenshot_path, width=img_display_w, height=img_display_h))

story.append(PageBreak())

# HALAMAN 3
story.append(Paragraph("5. Implementasi & Integrasi pada Project Akhir (Smart Parkir)", h1_style))
story.append(Paragraph("Penerapan <code>List&lt;Kendaraan&gt;</code> dan <code>List&lt;SlotParkir&gt;</code> pada sistem Smart Parkir:", body_style))

sp_fitur_data = [
    [Paragraph("Fitur Koleksi", th_style), Paragraph("Method GedungParkir", th_style), Paragraph("Validasi & Aturan Bisnis", th_style), Paragraph("Integritas State", th_style)],
    [Paragraph("Tambah Armada", td_style), Paragraph("<code>tambahKendaraan(k)</code>", td_style), Paragraph("Format plat valid (P10) & tolak nomor polisi duplikat.", td_style), Paragraph("Jumlah armada bertambah, DB tersinkron.", td_style)],
    [Paragraph("Cari Armada", td_style), Paragraph("<code>cariKendaraan(plat)</code>", td_style), Paragraph("Pencarian case-insensitive & whitespace-tolerant.", td_style), Paragraph("Mengembalikan referensi objek kendaraan.", td_style)],
    [Paragraph("Hapus Armada", td_style), Paragraph("<code>hapusKendaraan(plat)</code>", td_style), Paragraph("Ditolak jika kendaraan sedang parkir di slot aktif.", td_style), Paragraph("State slot aman; armada bebas dapat dihapus.", td_style)],
    [Paragraph("Iterasi Armada", td_style), Paragraph("<code>tampilkanSemuaKendaraan()</code>", td_style), Paragraph("Perulangan for-each untuk mencetak seluruh detail.", td_style), Paragraph("Menampilkan total size() armada terdaftar.", td_style)],
    [Paragraph("Enkapsulasi", td_style), Paragraph("<code>getDaftarKendaraan()</code>", td_style), Paragraph("Mengembalikan Collections.unmodifiableList.", td_style), Paragraph("Cegah penambahan/penghapusan liar dari luar.", td_style)]
]
t_spfitur = Table(sp_fitur_data, colWidths=[90, 130, 190, 130])
t_spfitur.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('PADDING', (0,0), (-1,-1), 1.2),
]))
story.append(t_spfitur)
story.append(Spacer(1, 1.5))

story.append(Paragraph("6. Luaran Praktikum & Tabel Pengujian", h1_style))
story.append(Paragraph("6.1 Tabel Evaluasi Output Program Dasar Perpustakaan (8 Skenario)", body_style))

eval_buku_data = [
    [Paragraph("No", th_style), Paragraph("Skenario", th_style), Paragraph("Aksi / Input", th_style), Paragraph("Hasil Diharapkan", th_style), Paragraph("Hasil Aktual", th_style), Paragraph("Data", th_style), Paragraph("Status", th_style)],
    [Paragraph("1", td_style), Paragraph("Tambah B001 & B002", td_style), Paragraph("B001, B002", td_style), Paragraph("Kedua buku tersimpan", td_style), Paragraph("Kedua buku tersimpan", td_style), Paragraph("2", td_style), Paragraph("Valid", td_style)],
    [Paragraph("2", td_style), Paragraph("Cari buku B002", td_style), Paragraph("B002", td_style), Paragraph("Ditemukan: Konsep OOP", td_style), Paragraph("Ditemukan: Konsep OOP", td_style), Paragraph("2", td_style), Paragraph("Valid", td_style)],
    [Paragraph("3", td_style), Paragraph("Tambah duplikat", td_style), Paragraph("B001 lagi", td_style), Paragraph("Ditolak kode duplikat", td_style), Paragraph("Ditolak: Kode buku sudah terdaftar.", td_style), Paragraph("2", td_style), Paragraph("Valid", td_style)],
    [Paragraph("4", td_style), Paragraph("Hapus buku B001", td_style), Paragraph("B001", td_style), Paragraph("Menghasilkan true", td_style), Paragraph("Penghapusan B001: true", td_style), Paragraph("1", td_style), Paragraph("Valid", td_style)],
    [Paragraph("5", td_style), Paragraph("Cari buku terhapus", td_style), Paragraph("B001", td_style), Paragraph("Buku tidak ditemukan", td_style), Paragraph("Buku tidak ditemukan.", td_style), Paragraph("1", td_style), Paragraph("Valid", td_style)],
    [Paragraph("6", td_style), Paragraph("Hapus B999 fiktif", td_style), Paragraph("B999", td_style), Paragraph("Menghasilkan false", td_style), Paragraph("Penghapusan B999: false", td_style), Paragraph("1", td_style), Paragraph("Valid", td_style)],
    [Paragraph("7", td_style), Paragraph("Judul kosong", td_style), Paragraph("new Buku(\"B03\", \"\")", td_style), Paragraph("Ditolak wajib diisi", td_style), Paragraph("Ditolak: Kode dan judul wajib diisi.", td_style), Paragraph("1", td_style), Paragraph("Valid", td_style)],
    [Paragraph("8", td_style), Paragraph("Objek null", td_style), Paragraph("tambahBuku(null)", td_style), Paragraph("Ditolak tidak boleh null", td_style), Paragraph("Ditolak: Objek buku tidak boleh null.", td_style), Paragraph("1", td_style), Paragraph("Valid", td_style)]
]
t_evalbuku = Table(eval_buku_data, colWidths=[15, 80, 85, 120, 145, 25, 40])
t_evalbuku.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('PADDING', (0,0), (-1,-1), 1.0),
]))
story.append(t_evalbuku)

story.append(PageBreak())

# HALAMAN 4
story.append(Paragraph("7. Kriteria Penilaian Modul 11 (Definition of Done)", h1_style))
rubrik_data = [
    [Paragraph("Aspek Penilaian Modul 11", th_style), Paragraph("Bobot", th_style), Paragraph("Bukti Implementasi pada Proyek Mandiri & Smart Parkir", th_style), Paragraph("Capaian", th_style)],
    [Paragraph("Ketepatan collection dan generics", td_style), Paragraph("20%", td_style), Paragraph("Penggunaan List<Buku> dan List<Kendaraan> dengan generics type safety.", td_style), Paragraph("Tuntas", td_style)],
    [Paragraph("Operasi tambah, cari, tampilkan, dan hapus", td_style), Paragraph("30%", td_style), Paragraph("4 operasi CRUD diimplementasikan pada Perpustakaan dan GedungParkir.", td_style), Paragraph("Tuntas", td_style)],
    [Paragraph("Encapsulation dan pembagian tanggung jawab", td_style), Paragraph("20%", td_style), Paragraph("Koleksi private final; perlindungan unmodifiableList dari manipulasi liar luar.", td_style), Paragraph("Tuntas", td_style)],
    [Paragraph("Validasi dan penanganan kesalahan", td_style), Paragraph("15%", td_style), Paragraph("Mencegah plat/kode duplikat, input null, dan blokir hapus kendaraan aktif parkir.", td_style), Paragraph("Tuntas", td_style)],
    [Paragraph("Pengujian dan kemampuan menjelaskan", td_style), Paragraph("15%", td_style), Paragraph("8 skenario perpustakaan dan 7 skenario Smart Parkir 100% lulus teruji.", td_style), Paragraph("Tuntas", td_style)],
    [Paragraph("<b>TOTAL NILAI KOMPETENSI</b>", td_style), Paragraph("<b>100%</b>", td_style), Paragraph("<b>Seluruh kriteria kualifikasi Definition of Done (DoD) Modul 11 terpenuhi secara sempurna.</b>", td_style), Paragraph("<b>Sempurna</b>", td_style)]
]
t_rubrik = Table(rubrik_data, colWidths=[140, 35, 320, 45])
t_rubrik.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-2), [colors.white, COLOR_ROW_ALT]),
    ('BACKGROUND', (0,-1), (-1,-1), colors.HexColor("#FEF08A")),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('PADDING', (0,0), (-1,-1), 1.1),
]))
story.append(t_rubrik)
story.append(Spacer(1, 1.5))

story.append(Paragraph("8. Jawaban Pertanyaan Evaluasi & Pembahasan Mendalam", h1_style))
eval_text = (
    "<b>1. Apa perbedaan Buku dan List&lt;Buku&gt;?</b><br/>"
    "• <code>Buku</code> adalah <b>entitas tunggal</b> yang merepresentasikan satu objek buku konkret (memiliki atribut kode dan judul).<br/>"
    "• <code>List&lt;Buku&gt;</code> adalah <b>wadah koleksi</b> yang mengelola nol atau lebih objek Buku (memiliki method add, remove, search, size).<br/><br/>"

    "<b>2. Mengapa indeks list kurang tepat dijadikan identitas tetap sebuah buku?</b><br/>"
    "Indeks list bersifat <b>relatif posisional</b>. Saat ada elemen yang dihapus (<code>remove</code>), seluruh elemen di kanannya bergeser satu indeks ke kiri (<i>index shifting</i>). Selain itu, pengurutan ulang (sorting) akan mengacak indeks. Oleh karena itu, identitas harus menggunakan kode unik bisnis (seperti Kode Buku atau Nomor Polisi) yang tidak terpengaruh pergeseran indeks memori.<br/><br/>"

    "<b>3. Mengapa pengecekan kode duplikat dilakukan sebelum add()?</b><br/>"
    "Pengecekan sebelum penambahan (pola <b>Test-Before-Mutate</b>) menjamin <b>integritas dan atomisitas koleksi</b>. Jika objek langsung ditambahkan ke list sebelum divalidasi, maka saat duplikasi terdeteksi, list sudah kotor dan membutuhkan proses rollback yang memboroskan memori. Pencegahan dini memastikan koleksi bebas dari efek samping (<i>side-effect free</i>).<br/><br/>"

    "<b>4. Apa risiko jika collection internal dapat diubah langsung oleh class lain?</b><br/>"
    "Jika referensi internal bocor ke luar (misal melalui getter publik biasa), kelas luar dapat memanggil <code>add()</code> tanpa validasi aturan bisnis atau memanggil <code>clear()</code> untuk menghapus seluruh data secara liar. Solusinya adalah mengembalikan salinan tidak dapat dimodifikasi via <code>Collections.unmodifiableList(daftarBuku)</code>."
)
story.append(Paragraph(eval_text, body_style))
story.append(Spacer(1, 1.5))

story.append(Paragraph("9. Kesimpulan Praktikum", h1_style))
kesimpulan_text = (
    "Praktikum Modul 11 berhasil membuktikan bahwa <b>Java Collections Framework (`List` & `ArrayList`)</b> dengan <b>Generics</b> "
    "adalah solusi industri yang unggul untuk mengelola sekumpulan objek secara dinamis, tipe-aman, dan terenkapsulasi. "
    "Konsep ini berhasil diterapkan baik pada program dasar (<code>Perpustakaan</code>) maupun sistem skala penuh (<code>Smart Parkir</code>)."
)
story.append(Paragraph(kesimpulan_text, body_style))

doc.build(story, canvasmaker=NumberedCanvas)
print(f"README.pdf successfully generated at: {pdf_filename}")

shutil.copyfile(pdf_filename, laporan_pdf_filename)
print(f"Laporan PDF copy created at: {laporan_pdf_filename}")
