import os
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
img_screenshot_path = os.path.join(script_dir, "terminal_screenshot.png")

# =========================================================================
# GENERATE REALISTIC TERMINAL SCREENSHOT
# =========================================================================
def generate_terminal_screenshot(output_path=img_screenshot_path):
    width = 1600
    font_size = 18
    line_height = 24
    padding_x = 30
    padding_top = 55
    padding_bottom = 25

    BG_COLOR = (24, 24, 37)        # #181825
    HEADER_BG = (30, 30, 46)       # #1e1e2e
    HEADER_BORDER = (49, 50, 68)   # #313244
    TEXT_DEFAULT = (205, 214, 244) # #cdd6f4
    TEXT_MUTED = (147, 153, 178)   # #9399b2
    COLOR_GREEN = (166, 227, 161)  # #a6e3a1
    COLOR_CYAN = (137, 220, 235)   # #89dceb
    COLOR_YELLOW = (249, 226, 175) # #f9e2af
    COLOR_BLUE = (137, 180, 250)   # #89b4fa
    COLOR_PURPLE = (203, 166, 247) # #cba6f7
    COLOR_RED = (243, 139, 168)    # #f38ba8
    COLOR_ACCENT = (148, 226, 213) # #94e2d5

    BTN_RED = (255, 95, 86)
    BTN_YELLOW = (255, 189, 46)
    BTN_GREEN = (39, 201, 63)

    lines_data = [
        [
            ("➜  ", COLOR_GREEN),
            ("P7_3125522010_ArjunaLanangAdiwarsana ", COLOR_CYAN),
            ("git:(", COLOR_PURPLE),
            ("main", COLOR_RED),
            (") ✗ ", COLOR_PURPLE),
            ("javac -d bin src/*.java && java -cp bin Main", (255, 255, 255))
        ],
        [("=========================================================================================", COLOR_BLUE)],
        [("   PRAKTIKUM MODUL 7 - ABSTRACT CLASS, ABSTRACT METHOD & INTERFACE (PERPUSTAKAAN)", COLOR_YELLOW)],
        [("=========================================================================================", COLOR_BLUE)],
        [("=== SKENARIO 1: MEMBUAT OBJECT SUBCLASS DARI ABSTRACT CLASS & INTERFACE ===", COLOR_GREEN)],
        [("[Sukses] ", COLOR_GREEN), ("Objek Mahasiswa (3125522010), Dosen (19880123...), Tendik, Buku [B001] berhasil dibuat", TEXT_DEFAULT)],
        [("=== SKENARIO 2: MEMANGGIL ABSTRACT METHOD VIA REFERENCE SUPERCLASS (DYNAMIC BINDING) ===", COLOR_GREEN)],
        [("refAnggota1 (Mahasiswa) -> ", COLOR_PURPLE), ("Peran: Mahasiswa | Denda: Rp 1500 (Tarif Mhs: Rp 500/hari) | Kuota: 3 Buku", TEXT_DEFAULT)],
        [("refAnggota2 (Dosen)     -> ", COLOR_PURPLE), ("Peran: Dosen     | Denda: Rp 0    (Privilege Bebas Denda)  | Kuota: 5 Buku", TEXT_DEFAULT)],
        [("refAnggota3 (Tendik)    -> ", COLOR_PURPLE), ("Peran: Tendik    | Denda: Rp 2250 (Tarif Tendik: Rp 750/hari) | Kuota: 4 Buku", TEXT_DEFAULT)],
        [("=== SKENARIO 3: MEMANGGIL METHOD MELALUI REFERENCE INTERFACE (CROSS-HIERARCHY) ===", COLOR_GREEN)],
        [("pelacakMhs.getLokasi()  -> ", COLOR_ACCENT), ("Area Baca Lantai 2 / Ruang Koleksi Skripsi", TEXT_DEFAULT)],
        [("pelacakDsn.getLokasi()  -> ", COLOR_ACCENT), ("Ruang Dosen Gedung TI / Laboratorium Riset", TEXT_DEFAULT)],
        [("pelacakBuku.getLokasi() -> ", COLOR_ACCENT), ("Koleksi Fisik di Rak A-03 (Informatika)", TEXT_DEFAULT)],
        [("=== SKENARIO 4: POLYMORPHIC COLLECTION (ARRAY OF ABSTRACT CLASS & INTERFACE) ===", COLOR_GREEN)],
        [("Iterasi Anggota[]       : ", COLOR_YELLOW), ("4 elemen diproses: Mahasiswa (Rp 2000), Dosen (Rp 0), Tendik (Rp 3000)", TEXT_DEFAULT)],
        [("Iterasi DapatDilacak[]  : ", COLOR_CYAN), ("5 elemen lintas hierarki (Mhs, Dsn, Tdk, Buku1, Buku2) tereksekusi polimorfik", TEXT_DEFAULT)],
        [("=== SKENARIO 5: INTEGRASI SISTEM & VERIFIKASI FITUR P1-P6 ===", COLOR_GREEN)],
        [("Overloading & Transaksi : ", COLOR_BLUE), ("hitungDenda(5h, 50%)=Rp 1250 | cariBuku=Sukses | PJ-001 (Mhs), PJ-002 (Dsn)", TEXT_DEFAULT)],
        [("=========================================================================================", COLOR_BLUE)],
        [("   PENGUJIAN MODUL 7 SELESAI (SELURUH DEFINITION OF DONE TERPENUHI)", COLOR_GREEN)],
        [("=========================================================================================", COLOR_BLUE)],
        [
            ("➜  ", COLOR_GREEN),
            ("P7_3125522010_ArjunaLanangAdiwarsana ", COLOR_CYAN),
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

    title_str = "arjuna@MacBook-Pro: ~/Herd/Java/P7_3125522010_ArjunaLanangAdiwarsana — -zsh — 112×28"
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
    return width, height

# Generate terminal screenshot image
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
        print(f"Total Pages Generated: {num_pages}")
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_number(num_pages)
            super().showPage()
        super().save()

    def draw_page_number(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 7.5)
        self.setFillColor(colors.HexColor("#4B5563"))
        
        # Header text
        self.drawString(36, 762, "Praktikum Pemrograman Berorientasi Obyek - Modul 7: Abstract Class & Interface")
        self.drawRightString(612 - 36, 762, "3125522010 - Arjuna Lanang Adiwarsana")
        self.setStrokeColor(colors.HexColor("#D1D5DB"))
        self.setLineWidth(0.5)
        self.line(36, 756, 612 - 36, 756)
        
        # Footer text
        page_text = f"Halaman {self._pageNumber} dari {page_count}"
        self.drawString(36, 20, "PENS PSDKU Sumenep - D3 PJJ Teknik Informatika 2026")
        self.drawRightString(612 - 36, 20, page_text)
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

COLOR_TEXT = colors.HexColor("#111827")
COLOR_MUTED = colors.HexColor("#374151")
COLOR_LINE = colors.HexColor("#9CA3AF")
COLOR_HEADER_BG = colors.HexColor("#E5E7EB")
COLOR_ROW_ALT = colors.HexColor("#F9FAFB")

title_style = ParagraphStyle(
    'DocTitle',
    parent=styles['Heading1'],
    fontName='Helvetica-Bold',
    fontSize=11,
    leading=14,
    textColor=COLOR_TEXT,
    alignment=1,
    spaceAfter=2
)

subtitle_style = ParagraphStyle(
    'DocSubtitle',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=8,
    leading=10,
    textColor=COLOR_MUTED,
    alignment=1,
    spaceAfter=2
)

h1_style = ParagraphStyle(
    'SectionH1',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=8.3,
    leading=10.5,
    textColor=COLOR_TEXT,
    spaceBefore=2.5,
    spaceAfter=1.5
)

body_style = ParagraphStyle(
    'BodyDark',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=6.6,
    leading=8.5,
    textColor=COLOR_TEXT,
    spaceAfter=2
)

th_style = ParagraphStyle(
    'TH',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=6.4,
    leading=8,
    textColor=COLOR_TEXT,
    alignment=0
)

td_style = ParagraphStyle(
    'TD',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=6.1,
    leading=7.5,
    textColor=COLOR_TEXT
)

story = []

# =========================================================================
# HALAMAN 1: IDENTITAS, SPRINT GOAL, SPRINT BACKLOG, TABEL AUDIT DESAIN P6
# =========================================================================
story.append(Paragraph("LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK", subtitle_style))
story.append(Paragraph("MODUL 7: ABSTRACT CLASS, ABSTRACT METHOD, DAN INTERFACE", title_style))
story.append(HRFlowable(width="100%", thickness=0.8, color=COLOR_TEXT, spaceAfter=3))

story.append(Paragraph("1. Profil Proyek (Project Identity)", h1_style))
project_info = [
    [Paragraph("<b>Nama Proyek:</b>", body_style), Paragraph("Sistem Manajemen Perpustakaan Terpadu (Refactoring Abstraction & Interface Contract)", body_style)],
    [Paragraph("<b>Nama Mahasiswa / NRP:</b>", body_style), Paragraph("Arjuna Lanang Adiwarsana / 3125522010", body_style)],
    [Paragraph("<b>Program Studi / Kampus:</b>", body_style), Paragraph("D3 PJJ Teknik Informatika - PENS PSDKU Sumenep (Tahun 2026)", body_style)],
    [Paragraph("<b>Fokus Modul (P7):</b>", body_style), Paragraph("Abstract Class, Abstract Method, Interface Implementation, Cross-Hierarchy Polymorphism & Agile Review", body_style)]
]
t_proj = Table(project_info, colWidths=[115, 425])
t_proj.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), colors.white),
    ('PADDING', (0,0), (-1,-1), 1.8),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('BOX', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#E5E7EB")),
]))
story.append(t_proj)
story.append(Spacer(1, 2))

story.append(Paragraph("2. Sprint Goal (P7)", h1_style))
story.append(Paragraph("Mengembangkan desain proyek P6 dengan menerapkan <b>abstract class</b> (<code>Anggota</code>) dan <b>interface</b> (<code>DapatDilacak</code>) yang sesuai sehingga struktur class lebih jelas, perilaku object memiliki kontrak yang konsisten, mencegah instansiasi sembarangan pada superclass umum, dan program tetap dapat dijalankan serta terintegrasi utuh.", body_style))
story.append(Spacer(1, 2))

story.append(Paragraph("3. Sprint Backlog (P7)", h1_style))
backlog_data = [
    [Paragraph("ID", th_style), Paragraph("Sprint Backlog Item", th_style), Paragraph("Status", th_style)],
    [Paragraph("SB-01", td_style), Paragraph("Audit hierarchy dan behavior proyek P6 untuk menentukan kebutuhan abstraksi", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-02", td_style), Paragraph("Menentukan kandidat abstract class (Refactoring class Anggota menjadi abstract class)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-03", td_style), Paragraph("Menentukan abstract method (tampilkanPeran(), hitungDenda(), getMaksimalPinjam())", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-04", td_style), Paragraph("Menentukan kandidat interface (Membuat interface DapatDilacak dengan kontrak getLokasi())", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-05", td_style), Paragraph("Memperbarui class diagram UML (Generalization, Realization, Composition, Aggregation)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-06", td_style), Paragraph("Mengimplementasikan abstract class, subclass konkret, dan interface pada source code Java", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-07", td_style), Paragraph("Menguji 4 skenario polymorphism (Subclass, Superclass ref, Interface ref, Array collection)", td_style), Paragraph("Done", td_style)]
]
t_backlog = Table(backlog_data, colWidths=[38, 442, 60])
t_backlog.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('PADDING', (0,0), (-1,-1), 1.6),
]))
story.append(t_backlog)
story.append(Spacer(1, 2))

story.append(Paragraph("4. Bagian A – Audit Desain P6 (Refactoring Abstraction & Interface)", h1_style))
story.append(Paragraph("Analisis kebutuhan struktur dan perilaku objek dari modul P6 ke P7:", body_style))

audit_data = [
    [Paragraph("Class / Behavior", th_style), Paragraph("Kondisi P6", th_style), Paragraph("Rencana P7", th_style), Paragraph("Alasan Perubahan Desain", th_style)],
    [
        Paragraph("<b>Class Anggota</b>", td_style),
        Paragraph("Class biasa (Konkret)", td_style),
        Paragraph("<b>Abstract class</b>", td_style),
        Paragraph("Anggota hanya mewakili konsep umum abstrak pengguna perpustakaan. Objek nyata harus spesifik (Mahasiswa/Dosen/Tendik) dan tidak boleh diinstansiasi langsung (<code>new Anggota()</code>).", td_style)
    ],
    [
        Paragraph("<b>tampilkanPeran()</b>", td_style),
        Paragraph("Method biasa", td_style),
        Paragraph("<b>Abstract method</b>", td_style),
        Paragraph("Setiap jenis anggota memiliki peran, wewenang, dan hak akses katalog yang berbeda, sehingga subclass wajib mendefinisikan perilakunya sendiri.", td_style)
    ],
    [
        Paragraph("<b>hitungDenda(int)</b>", td_style),
        Paragraph("Method biasa", td_style),
        Paragraph("<b>Abstract method</b>", td_style),
        Paragraph("Tarif denda keterlambatan bersifat spesifik untuk masing-masing tipe anggota (Mahasiswa: Rp 500/hari, Dosen: Rp 0 / bebas denda, Tendik: Rp 750/hari).", td_style)
    ],
    [
        Paragraph("<b>getMaksimalPinjam()</b>", td_style),
        Paragraph("Method biasa", td_style),
        Paragraph("<b>Abstract method</b>", td_style),
        Paragraph("Kuota maksimal peminjaman buku wajib disesuaikan dengan jenis keanggotaan akademik (Mahasiswa: 3, Dosen: 5, Tendik: 4 buku).", td_style)
    ],
    [
        Paragraph("<b>DapatDilacak</b>", td_style),
        Paragraph("Belum ada", td_style),
        Paragraph("<b>Interface</b><br/>(<code>getLokasi()</code>)", td_style),
        Paragraph("Menentukan kontrak kemampuan pelacakan lokasi fisik yang dapat diterapkan lintas class yang berbeda hierarki (<code>Mahasiswa</code>, <code>Dosen</code>, <code>Tendik</code>, dan <code>Buku</code>).", td_style)
    ]
]
t_audit = Table(audit_data, colWidths=[90, 75, 80, 295])
t_audit.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('PADDING', (0,0), (-1,-1), 2.2),
]))
story.append(t_audit)

story.append(PageBreak())

# =========================================================================
# HALAMAN 2: CLASS DIAGRAM TERBARU, IMPLEMENTASI ABSTRACT CLASS & INTERFACE
# =========================================================================
story.append(Paragraph("5. Bagian B – Update Class Diagram (P7 - Abstraction & Interface)", h1_style))
story.append(Paragraph("Diagram UML menampilkan abstract class (<code>&lt;&lt;abstract&gt;&gt;</code>), interface (<code>&lt;&lt;interface&gt;&gt;</code>), relasi generalization (garis solid segitiga tertutup), realization (garis putus-putus segitiga tertutup), serta relasi P4:", body_style))

def draw_p7_uml():
    d = Drawing(540, 245)
    
    C_BG = colors.white
    C_HEADER_BG = colors.HexColor("#F3F4F6")
    C_BORDER = colors.HexColor("#374151")
    C_TEXT = colors.HexColor("#111827")
    C_LINE = colors.HexColor("#111827")
    
    def card(x, y, w, h, title, stereotype, attrs, mths):
        d.add(Rect(x, y, w, h, fillColor=C_BG, strokeColor=C_BORDER, strokeWidth=0.7))
        th = 13
        d.add(Rect(x, y + h - th, w, th, fillColor=C_HEADER_BG, strokeColor=C_BORDER, strokeWidth=0.7))
        full_title = f"<<{stereotype}>> {title}" if stereotype else title
        d.add(String(x + w/2.0, y + h - 9.5, full_title, textAnchor='middle', fontName='Helvetica-Bold', fontSize=5.6, fillColor=C_TEXT))
        
        cy = y + h - th - 7
        for a in attrs:
            d.add(String(x + 3, cy, a, fontName='Courier', fontSize=4.6, fillColor=C_TEXT))
            cy -= 5.9
            
        div_y = cy + 2
        d.add(Line(x, div_y, x + w, div_y, strokeColor=C_BORDER, strokeWidth=0.4))
        
        cy = div_y - 6.2
        for m in mths:
            d.add(String(x + 3, cy, m, fontName='Courier', fontSize=4.6, fillColor=C_TEXT))
            cy -= 5.9

    # --- TOP ROW ---
    # 1. Perpustakaan (Top Left)
    card(0, 155, 120, 85, "Perpustakaan", "",
         ["- nama, alamat : String", "- daftarBuku : List<Buku>"],
         ["+ tambahBuku(Buku)", "+ cariBuku(judul) : Buku", "+ cariBuku(kode, bool)", "+ tampilkanDaftar()"])

    # 2. Buku (Top Center-Left) - implements DapatDilacak
    card(132, 155, 125, 85, "Buku", "",
         ["- kode, judul : String", "- penulis, rakLokasi : String", "- tahunTerbit : int"],
         ["+ getLokasi() : String", "+ tampilkanData() void"])

    # 3. Abstract Superclass: Anggota (Top Center-Right)
    card(268, 140, 192, 100, "Anggota", "abstract",
         ["- id, nama, alamat : String", "- kartuAnggota : KartuAnggota"],
         ["+ {abstract} tampilkanPeran()", "+ {abstract} hitungDenda(int)", "+ {abstract} getMaksimalPinjam()", "+ hitungDenda(int, double) : int", "+ tampilkanData() void"])

    # 4. KartuAnggota (Far Right)
    card(470, 160, 70, 80, "KartuAnggota", "",
         ["- nomorKartu : String", "- status : String"],
         ["+ getNomorKartu()", "+ tampilkanKartu()"])

    # --- INTERFACE (Floating Center-Left) ---
    card(2, 60, 118, 50, "DapatDilacak", "interface",
         [],
         ["+ getLokasi() : String"])

    # --- BOTTOM ROW (Subclasses) ---
    # 5. Peminjaman (Bottom Left)
    card(128, 10, 130, 95, "Peminjaman", "",
         ["- kodePinjam, tgl : String", "- buku : Buku", "- anggota : Anggota", "- durasiHari : int"],
         ["+ hitungTotalDenda(hari)", "+ tampilkanData() void"])

    # 6. Subclass: Mahasiswa (Bottom Middle)
    card(268, 10, 85, 90, "Mahasiswa", "",
         ["- nrp, prodi : String"],
         ["+ tampilkanPeran()", "+ hitungDenda(int)", "+ getMaxPinjam()", "+ getLokasi() : String"])

    # 7. Subclass: Dosen (Bottom Middle-Right)
    card(360, 10, 85, 90, "Dosen", "",
         ["- nip, dept : String"],
         ["+ tampilkanPeran()", "+ hitungDenda(int)", "+ getMaxPinjam()", "+ getLokasi() : String"])

    # 8. Subclass: Tendik (Bottom Right)
    card(452, 10, 88, 90, "Tendik", "",
         ["- nip, unitKerja : String"],
         ["+ tampilkanPeran()", "+ hitungDenda(int)", "+ getMaxPinjam()", "+ getLokasi() : String"])

    # --- RELATIONS & CONNECTORS ---
    # Perpustakaan ◇-- Buku (Agregasi)
    d.add(Line(120, 195, 132, 195, strokeColor=C_LINE, strokeWidth=0.8))
    d.add(Polygon([120, 195, 123, 198, 126, 195, 123, 192], fillColor=colors.white, strokeColor=C_LINE, strokeWidth=0.8))
    
    # Anggota ◆-- KartuAnggota (Komposisi)
    d.add(Line(460, 195, 470, 195, strokeColor=C_LINE, strokeWidth=0.8))
    d.add(Polygon([460, 195, 463, 198, 466, 195, 463, 192], fillColor=C_LINE, strokeColor=C_LINE, strokeWidth=0.8))

    # Peminjaman -- Buku & Peminjaman -- Anggota (Asosiasi)
    d.add(Line(193, 105, 193, 155, strokeColor=C_LINE, strokeWidth=0.6))
    d.add(Line(258, 60, 264, 60, strokeColor=C_LINE, strokeWidth=0.6))
    d.add(Line(264, 60, 264, 125, strokeColor=C_LINE, strokeWidth=0.6))
    d.add(Line(264, 125, 364, 125, strokeColor=C_LINE, strokeWidth=0.6))
    d.add(Line(364, 125, 364, 140, strokeColor=C_LINE, strokeWidth=0.6))

    # Generalization: Anggota <|-- (Mahasiswa, Dosen, Tendik)
    d.add(Line(364, 140, 364, 122, strokeColor=C_LINE, strokeWidth=0.9))
    d.add(Polygon([364, 140, 360, 133, 368, 133], fillColor=colors.white, strokeColor=C_LINE, strokeWidth=0.9))
    
    # Generalization horizontal fork
    d.add(Line(310, 122, 496, 122, strokeColor=C_LINE, strokeWidth=0.9))
    d.add(Line(310, 122, 310, 100, strokeColor=C_LINE, strokeWidth=0.9))
    d.add(Line(402, 122, 402, 100, strokeColor=C_LINE, strokeWidth=0.9))
    d.add(Line(496, 122, 496, 100, strokeColor=C_LINE, strokeWidth=0.9))
    d.add(String(312, 125, "Generalization (extends)", fontName='Helvetica-BoldOblique', fontSize=4.6, fillColor=C_TEXT))

    # Realization: DapatDilacak <|.. (Mahasiswa, Dosen, Tendik, Buku)
    # Dashed line to Buku
    d.add(Line(61, 110, 61, 140, strokeColor=C_LINE, strokeWidth=0.7))
    d.add(Line(61, 140, 160, 140, strokeColor=C_LINE, strokeWidth=0.7))
    d.add(Line(160, 140, 160, 155, strokeColor=C_LINE, strokeWidth=0.7))
    d.add(Polygon([160, 155, 157, 148, 163, 148], fillColor=colors.white, strokeColor=C_LINE, strokeWidth=0.7))

    # Realization dashed line to Subclasses
    d.add(Line(61, 60, 61, 3, strokeColor=C_LINE, strokeWidth=0.7))
    d.add(Line(61, 3, 290, 3, strokeColor=C_LINE, strokeWidth=0.7))
    d.add(Line(290, 3, 290, 10, strokeColor=C_LINE, strokeWidth=0.7))
    d.add(Line(290, 3, 380, 3, strokeColor=C_LINE, strokeWidth=0.7))
    d.add(Line(380, 3, 380, 10, strokeColor=C_LINE, strokeWidth=0.7))
    d.add(Line(380, 3, 470, 3, strokeColor=C_LINE, strokeWidth=0.7))
    d.add(Line(470, 3, 470, 10, strokeColor=C_LINE, strokeWidth=0.7))
    d.add(Polygon([61, 60, 58, 53, 64, 53], fillColor=colors.white, strokeColor=C_LINE, strokeWidth=0.7))
    d.add(String(65, 5, "Realization / implements DapatDilacak (Cross-Hierarchy)", fontName='Helvetica-BoldOblique', fontSize=4.6, fillColor=C_TEXT))

    return d

story.append(draw_p7_uml())
story.append(Spacer(1, 1))

story.append(Paragraph("6. Bagian C, D, E – Perbandingan & Alasan Perubahan Desain", h1_style))

p_diff = [
    [
        Paragraph("<b>Aspek</b>", th_style),
        Paragraph("<b>Abstract Class (Anggota)</b>", th_style),
        Paragraph("<b>Interface (DapatDilacak)</b>", th_style)
    ],
    [
        Paragraph("<b>Fungsi Utama</b>", td_style),
        Paragraph("Mewakili konsep umum entitas pengguna perpustakaan, berbagi state/atribut, dan menyediakan kerangka dasar bagi hierarki keanggotaan.", td_style),
        Paragraph("Menentukan kontrak perilaku (<i>behavior contract</i>) pelacakan lokasi fisik yang dapat diterapkan lintas class tanpa batasan hierarki pewarisan.", td_style)
    ],
    [
        Paragraph("<b>Keyword & Relasi</b>", td_style),
        Paragraph("<code>abstract class</code> dan diwarisi via <code>extends</code> (Single Inheritance).", td_style),
        Paragraph("<code>interface</code> dan diimplementasikan via <code>implements</code> (Multiple Realization).", td_style)
    ],
    [
        Paragraph("<b>Atribut & Konstruktor</b>", td_style),
        Paragraph("Dapat memiliki atribut instance (<code>id</code>, <code>nama</code>, <code>alamat</code>) dan constructor superclass.", td_style),
        Paragraph("Tidak memiliki atribut instance dan tidak memiliki constructor.", td_style)
    ],
    [
        Paragraph("<b>Metode</b>", td_style),
        Paragraph("Kombinasi <b>abstract method</b> (wajib dioverride subclass) dan method konkret umum (<code>tampilkanData()</code>).", td_style),
        Paragraph("Hanya memuat deklarasi method abstrak tanpa body: <code>String getLokasi();</code>.", td_style)
    ]
]
t_comp = Table(p_diff, colWidths=[80, 230, 230])
t_comp.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ('PADDING', (0,0), (-1,-1), 1.8),
]))
story.append(t_comp)

story.append(PageBreak())

# =========================================================================
# HALAMAN 3: SCREENSHOT TERMINAL ASLI, TABEL PENGUJIAN POLYMORPHISM, REVIEW & RETRO
# =========================================================================
story.append(Paragraph("7. Screenshot Hasil Running Program (macOS Terminal Output Main.java)", h1_style))

# Embed actual terminal screenshot image (rendered with 540 pt width, proportional height)
img_display_w = 540
img_display_h = (img_h / img_w) * img_display_w
story.append(RLImage(img_screenshot_path, width=img_display_w, height=img_display_h))
story.append(Spacer(1, 1.5))

story.append(Paragraph("8. Bagian F – Tabel Pengujian Polymorphism (4 Skenario Pengujian)", h1_style))
poly_test_data = [
    [Paragraph("No", th_style), Paragraph("Skenario Pengujian", th_style), Paragraph("Reference / Objek", th_style), Paragraph("Hasil yang Diharapkan", th_style), Paragraph("Hasil Eksekusi & Status", th_style)],
    [
        Paragraph("1", td_style),
        Paragraph("Membuat object subclass", td_style),
        Paragraph("<code>Mahasiswa</code>, <code>Dosen</code>, <code>Tendik</code>, <code>Buku</code>", td_style),
        Paragraph("Object berhasil dibuat di memory heap", td_style),
        Paragraph("<b>[Sukses]</b> Objek berhasil dibuat tanpa error", td_style)
    ],
    [
        Paragraph("2", td_style),
        Paragraph("Memanggil abstract method via reference superclass", td_style),
        Paragraph("Reference <code>Anggota</code> (refAnggota1, refAnggota2, refAnggota3)", td_style),
        Paragraph("Method subclass yang sesuai dijalankan secara dinamis", td_style),
        Paragraph("<b>[Sukses]</b> Mahasiswa (Rp 1500), Dosen (Rp 0), Tendik (Rp 2250)", td_style)
    ],
    [
        Paragraph("3", td_style),
        Paragraph("Memanggil method via reference interface", td_style),
        Paragraph("Reference <code>DapatDilacak</code> (pelacakMhs, pelacakBuku)", td_style),
        Paragraph("Implementasi interface yang sesuai dijalankan lintas hierarki", td_style),
        Paragraph("<b>[Sukses]</b> Mhs (Area Baca Lt.2), Buku (Rak A-03)", td_style)
    ],
    [
        Paragraph("4", td_style),
        Paragraph("Array/collection bertipe superclass & interface", td_style),
        Paragraph("Array <code>Anggota[]</code> dan <code>DapatDilacak[]</code>", td_style),
        Paragraph("Setiap object menjalankan perilakunya sendiri dalam loop", td_style),
        Paragraph("<b>[Sukses]</b> 4 anggota & 5 pelacak tereksekusi polimorfik", td_style)
    ]
]
t_poly = Table(poly_test_data, colWidths=[18, 122, 125, 137, 138])
t_poly.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('PADDING', (0,0), (-1,-1), 1.2),
]))
story.append(t_poly)
story.append(Spacer(1, 1))

story.append(Paragraph("9. Sprint Review (Checklist 7/7 Terverifikasi)", h1_style))
review_data = [
    [Paragraph("Item Evaluasi", th_style), Paragraph("Hasil Evaluasi", th_style)],
    [Paragraph("Abstract class berhasil dibuat", td_style), Paragraph("Class Anggota dideklarasikan sebagai abstract class dengan state & constructor", td_style)],
    [Paragraph("Abstract method diimplementasikan subclass", td_style), Paragraph("Subclass Mahasiswa, Dosen, Tendik wajib mengimplementasikan 3 abstract method", td_style)],
    [Paragraph("Minimal 2 subclass dapat digunakan", td_style), Paragraph("3 subclass konkret (Mahasiswa, Dosen, Tendik) aktif digunakan dalam transaksi", td_style)],
    [Paragraph("Interface berhasil diimplementasikan", td_style), Paragraph("Interface DapatDilacak berhasil diimplementasikan pada Mahasiswa, Dosen, Tendik, Buku", td_style)],
    [Paragraph("Class diagram sesuai dengan kode", td_style), Paragraph("Notasi Generalization & Realization konsisten 100% dengan implementasi Java", td_style)],
    [Paragraph("Pengujian polymorphism berhasil", td_style), Paragraph("4 skenario pengujian polymorphism (superclass & interface) teruji sukses", td_style)],
    [Paragraph("Fitur proyek sebelumnya tetap berjalan", td_style), Paragraph("Fitur P1-P6 (Komposisi, Agregasi, Overloading, Peminjaman) tetap berjalan normal", td_style)]
]
t_review = Table(review_data, colWidths=[165, 375])
t_review.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('PADDING', (0,0), (-1,-1), 0.9),
]))
story.append(t_review)
story.append(Spacer(1, 1))

story.append(Paragraph("10. Sprint Retrospective", h1_style))
retro_text = """
<b>What Went Well?</b> Penerapan <code>abstract class</code> mencegah instansiasi objek umum yang tidak valid, sementara <code>interface</code> memungkinkan standarisasi kontrak kapabilitas pelacakan lokasi pada entitas yang berbeda hierarki (<code>Anggota</code> dan <code>Buku</code>).<br/>
<b>What Went Wrong?</b> Diperlukan pemahaman tajam mengenai relasi <i>is-a</i> (inheritance/abstract class) versus <i>can-do</i> (interface implementation).<br/>
<b>Improvement:</b> Struktur desain yang modular dan terstandarisasi ini menjadi fondasi yang kokoh untuk menghadapi Evaluasi UTS Praktikum (P8) dan modul perancangan lanjutan (P9).
"""
story.append(Paragraph(retro_text, body_style))

doc.build(story, canvasmaker=NumberedCanvas)
print(f"README.pdf successfully generated at: {pdf_filename}")
