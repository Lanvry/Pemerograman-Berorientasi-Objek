import os
import shutil
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.graphics.shapes import Drawing, Rect, String, Line, Polygon

script_dir = os.path.dirname(os.path.abspath(__file__))
pdf_filename = os.path.join(script_dir, "README.pdf")

alt_pdf_dir = "/Users/arjunalanangadiwarsana/Herd/Workshop Pemerograman Framework/Pertemuan Kedua"
alt_pdf_filename = os.path.join(alt_pdf_dir, "README.pdf")

doc = SimpleDocTemplate(
    pdf_filename,
    pagesize=letter,
    leftMargin=36,
    rightMargin=36,
    topMargin=36,
    bottomMargin=36
)

styles = getSampleStyleSheet()

# Custom Palette
PRIMARY = colors.HexColor("#1A365D")   # Deep Navy
SECONDARY = colors.HexColor("#2B6CB0") # Slate Blue
ACCENT = colors.HexColor("#319795")    # Teal accent
BG_LIGHT = colors.HexColor("#F8FAFC")  # Light gray background
TEXT_DARK = colors.HexColor("#1E293B")
BORDER_COLOR = colors.HexColor("#CBD5E1")

# Custom Typography Styles
title_style = ParagraphStyle(
    'DocTitle',
    parent=styles['Heading1'],
    fontName='Helvetica-Bold',
    fontSize=16,
    leading=20,
    textColor=PRIMARY,
    alignment=1, # Center
    spaceAfter=8
)

subtitle_style = ParagraphStyle(
    'DocSubtitle',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=10.5,
    leading=13,
    textColor=SECONDARY,
    alignment=1,
    spaceAfter=10
)

h1_style = ParagraphStyle(
    'SectionH1',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=11.5,
    leading=14,
    textColor=PRIMARY,
    spaceBefore=8,
    spaceAfter=4
)

body_style = ParagraphStyle(
    'BodyDark',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=8.5,
    leading=11.5,
    textColor=TEXT_DARK,
    spaceAfter=4
)

terminal_style = ParagraphStyle(
    'TerminalBlock',
    parent=styles['Normal'],
    fontName='Courier',
    fontSize=7,
    leading=8.5,
    textColor=colors.HexColor("#0F172A")
)

th_style = ParagraphStyle(
    'TH',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=8,
    leading=10,
    textColor=colors.white,
    alignment=0
)

td_style = ParagraphStyle(
    'TD',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=7.5,
    leading=9.5,
    textColor=TEXT_DARK
)

story = []

# ==========================================
# HALAMAN 1: Project Overview & Sprint Backlog
# ==========================================
story.append(Paragraph("LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK", subtitle_style))
story.append(Paragraph("MODUL 2: IMPLEMENTASI CLASS, OBJECT, ATTRIBUTE, METHOD, DAN CONSTRUCTOR", title_style))
story.append(HRFlowable(width="100%", thickness=1.5, color=PRIMARY, spaceAfter=8))

story.append(Paragraph("1. Profil Proyek", h1_style))
project_info = [
    [Paragraph("<b>Nama Proyek:</b>", body_style), Paragraph("Sistem Perpustakaan", body_style)],
    [Paragraph("<b>Pengembang / NRP:</b>", body_style), Paragraph("Arjuna Lanang Adiwarsana / 3125522010", body_style)],
    [Paragraph("<b>Mata Kuliah:</b>", body_style), Paragraph("Workshop Pemrograman Framework (PBO)", body_style)],
    [Paragraph("<b>Basis Proyek (P2):</b>", body_style), Paragraph("Sistem Manajemen Buku, Anggota & Peminjaman Perpustakaan", body_style)]
]
t_proj = Table(project_info, colWidths=[120, 420])
t_proj.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), BG_LIGHT),
    ('PADDING', (0,0), (-1,-1), 4),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('BOX', (0,0), (-1,-1), 0.5, BORDER_COLOR),
]))
story.append(t_proj)
story.append(Spacer(1, 6))

story.append(Paragraph("2. Product Goal", h1_style))
story.append(Paragraph("Membangun sistem pencatatan peminjaman buku perpustakaan yang terstruktur.", body_style))
story.append(Spacer(1, 6))

story.append(Paragraph("3. Sprint Goal (Sprint P2)", h1_style))
story.append(Paragraph("Mengimplementasikan class utama proyek (Buku, Anggota, Peminjaman) sehingga object dapat dibuat, diberi data awal melalui constructor, serta menjalankan operasi dasar method (tanpa parameter, dengan parameter, dan return value).", body_style))
story.append(Spacer(1, 6))

story.append(Paragraph("4. Sprint Backlog (P2)", h1_style))
backlog_data = [
    [Paragraph("ID", th_style), Paragraph("Sprint Backlog Item", th_style), Paragraph("Status", th_style)],
    [Paragraph("SB-01", td_style), Paragraph("Membuat class Buku beserta atribut (kode, judul, penulis)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-02", td_style), Paragraph("Membuat class Anggota beserta atribut (id, nama, alamat)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-03", td_style), Paragraph("Membuat class Peminjaman beserta atribut (tanggal, buku, anggota)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-04", td_style), Paragraph("Membuat constructor pada setiap class untuk inisialisasi objek", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-05", td_style), Paragraph("Membuat method utama (tanpa param, dengan param, return value)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-06", td_style), Paragraph("Membuat minimal 2 objek per class dan skenario pengujian pada Main.java", td_style), Paragraph("Done", td_style)]
]
t_backlog = Table(backlog_data, colWidths=[45, 425, 70])
t_backlog.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), PRIMARY),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
    ('PADDING', (0,0), (-1,-1), 4),
]))
story.append(t_backlog)

story.append(PageBreak())

# ==========================================
# HALAMAN 2: Refinement Object & UML Diagram
# ==========================================
story.append(Paragraph("5. Refinement Object dan Spesifikasi Class", h1_style))
story.append(Paragraph("Tabel berikut menyajikan pemetaan kandidat objek Sistem Perpustakaan menjadi class Java beserta spesifikasi atribut, method, dan constructor:", body_style))
story.append(Spacer(1, 4))

refinement_data = [
    [Paragraph("Class", th_style), Paragraph("Attribute", th_style), Paragraph("Method", th_style), Paragraph("Constructor", th_style)],
    [
        Paragraph("<b>Buku</b>", td_style),
        Paragraph("• kode (String)<br/>• judul (String)<br/>• penulis (String)", td_style),
        Paragraph("• tampilkanData()<br/>• ubahJudul(String)<br/>• getJudul() : String", td_style),
        Paragraph("Buku(kode, judul, penulis)", td_style)
    ],
    [
        Paragraph("<b>Anggota</b>", td_style),
        Paragraph("• id (String)<br/>• nama (String)<br/>• alamat (String)", td_style),
        Paragraph("• tampilkanData()<br/>• ubahAlamat(String)<br/>• getNama() : String", td_style),
        Paragraph("Anggota(id, nama, alamat)", td_style)
    ],
    [
        Paragraph("<b>Peminjaman</b>", td_style),
        Paragraph("• tanggal (String)<br/>• buku (Buku)<br/>• anggota (Anggota)", td_style),
        Paragraph("• tampilkanData()<br/>• ubahTanggal(String)<br/>• getTanggal() : String", td_style),
        Paragraph("Peminjaman(tanggal, buku, anggota)", td_style)
    ]
]
t_ref = Table(refinement_data, colWidths=[75, 135, 185, 145])
t_ref.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), SECONDARY),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
    ('PADDING', (0,0), (-1,-1), 4),
]))
story.append(t_ref)
story.append(Spacer(1, 8))

story.append(Paragraph("6. Diagram Class Sederhana (UML Diagram)", h1_style))
story.append(Paragraph("Relasi dan hubungan antar class dalam proyek Sistem Perpustakaan:", body_style))
story.append(Spacer(1, 4))

def build_uml_diagram():
    d = Drawing(540, 260)
    
    COLOR_PRIMARY = colors.HexColor("#1A365D")   # Deep Navy Header
    COLOR_SECONDARY = colors.HexColor("#2B6CB0") # Slate Blue Header
    COLOR_BG = colors.HexColor("#F8FAFC")        # Soft Light BG
    COLOR_BORDER = colors.HexColor("#475569")    # Dark slate border
    COLOR_TEXT = colors.HexColor("#0F172A")      # Main text
    COLOR_LINE = colors.HexColor("#2563EB")      # Blue line connector

    def draw_class_card(x, y, w, h, title, attributes, methods, header_color):
        d.add(Rect(x, y, w, h, fillColor=COLOR_BG, strokeColor=COLOR_BORDER, strokeWidth=1.2, rx=4, ry=4))
        
        th = 20
        d.add(Rect(x, y + h - th, w, th, fillColor=header_color, strokeColor=COLOR_BORDER, strokeWidth=1.2, rx=4, ry=4))
        d.add(Rect(x, y + h - th, w, 5, fillColor=header_color, strokeColor=colors.transparent, strokeWidth=0))
        d.add(String(x + w/2.0, y + h - 14, title, textAnchor='middle', fontName='Helvetica-Bold', fontSize=9.5, fillColor=colors.white))
        
        cy = y + h - th - 13
        for attr in attributes:
            d.add(String(x + 8, cy, attr, fontName='Courier', fontSize=7.5, fillColor=COLOR_TEXT))
            cy -= 11
            
        div_y = cy + 4
        d.add(Line(x, div_y, x + w, div_y, strokeColor=COLOR_BORDER, strokeWidth=0.8))
        
        cy = div_y - 12
        for mth in methods:
            d.add(String(x + 8, cy, mth, fontName='Courier', fontSize=7.5, fillColor=COLOR_TEXT))
            cy -= 11

    # Box 1: Buku (Top Left)
    buku_attrs = [
        "kode : String",
        "judul : String",
        "penulis : String"
    ]
    buku_mths = [
        "Buku(kode, judul, penulis)",
        "void tampilkanData()",
        "void ubahJudul(judulBaru)",
        "String getJudul()"
    ]
    draw_class_card(10, 140, 250, 115, "Class: Buku", buku_attrs, buku_mths, COLOR_PRIMARY)

    # Box 2: Anggota (Top Right)
    anggota_attrs = [
        "id : String",
        "nama : String",
        "alamat : String"
    ]
    anggota_mths = [
        "Anggota(id, nama, alamat)",
        "void tampilkanData()",
        "void ubahAlamat(alamatBaru)",
        "String getNama()"
    ]
    draw_class_card(280, 140, 250, 115, "Class: Anggota", anggota_attrs, anggota_mths, COLOR_PRIMARY)

    # Box 3: Peminjaman (Bottom Center)
    peminjaman_attrs = [
        "tanggal : String",
        "buku : Buku",
        "anggota : Anggota"
    ]
    peminjaman_mths = [
        "Peminjaman(tgl, buku, anggota)",
        "void tampilkanData()",
        "void ubahTanggal(tglBaru)",
        "String getTanggal()"
    ]
    draw_class_card(145, 0, 250, 115, "Class: Peminjaman", peminjaman_attrs, peminjaman_mths, COLOR_SECONDARY)

    # Association Connectors (y = 115 to 140 gap)
    # Left Line: Buku (x=135, y=140) down to Peminjaman (x=195, y=115)
    d.add(Line(135, 140, 135, 127.5, strokeColor=COLOR_LINE, strokeWidth=1.5))
    d.add(Line(135, 127.5, 195, 127.5, strokeColor=COLOR_LINE, strokeWidth=1.5))
    d.add(Line(195, 127.5, 195, 115, strokeColor=COLOR_LINE, strokeWidth=1.5))
    
    # Arrow head pointing to Buku
    d.add(Polygon([135, 140, 131, 132, 139, 132], fillColor=COLOR_LINE, strokeColor=COLOR_LINE))
    
    # Multiplicity Text Left
    d.add(String(142, 132, "1", fontName='Helvetica-Bold', fontSize=8.5, fillColor=COLOR_PRIMARY))
    d.add(String(202, 118, "*", fontName='Helvetica-Bold', fontSize=8.5, fillColor=COLOR_PRIMARY))

    # Right Line: Anggota (x=405, y=140) down to Peminjaman (x=345, y=115)
    d.add(Line(405, 140, 405, 127.5, strokeColor=COLOR_LINE, strokeWidth=1.5))
    d.add(Line(405, 127.5, 345, 127.5, strokeColor=COLOR_LINE, strokeWidth=1.5))
    d.add(Line(345, 127.5, 345, 115, strokeColor=COLOR_LINE, strokeWidth=1.5))
    
    # Arrow head pointing to Anggota
    d.add(Polygon([405, 140, 401, 132, 409, 132], fillColor=COLOR_LINE, strokeColor=COLOR_LINE))

    # Multiplicity Text Right
    d.add(String(394, 132, "1", fontName='Helvetica-Bold', fontSize=8.5, fillColor=COLOR_PRIMARY))
    d.add(String(330, 118, "*", fontName='Helvetica-Bold', fontSize=8.5, fillColor=COLOR_PRIMARY))

    return d

story.append(build_uml_diagram())

story.append(PageBreak())

# ==========================================
# HALAMAN 3: Running Console, Review & Retro
# ==========================================
story.append(Paragraph("7. Hasil Executing / Running Console (Main.java)", h1_style))

running_log = """=== 1. PEMBUATAN OBJEK (CONSTRUCTOR) ===
[SUCCESS] Minimal 2 Objek per class berhasil dibuat.

=== 2. PENGUJIAN METHOD TANPA PARAMETER ===
--- Detail Buku ---
Kode Buku : B001 | Judul: Pemrograman Java Dasar | Penulis: Nirwana Haidar
--- Detail Anggota ---
ID Anggota : A001 | Nama: Arjuna Lanang | Alamat: Jl. Sumenep No. 10
========================================
      DETAIL PEMINJAMAN PERPUSTAKAAN    
========================================
Tanggal Pinjam : 2026-09-01
Peminjam       : Arjuna Lanang | Buku: Pemrograman Java Dasar

=== 3. PENGUJIAN METHOD DENGAN PARAMETER ===
[INFO] Judul buku B001 diubah: "Pemrograman Java Lanjut & Framework"
[INFO] Alamat anggota A002 diubah: Jl. Merdeka No. 88, Surabaya
[INFO] Tanggal peminjaman A001 diubah: 2026-09-02

=== 4. PENGUJIAN METHOD DENGAN RETURN VALUE ===
Judul Buku 1 (via getJudul())    : Pemrograman Java Lanjut & Framework
Nama Anggota 2 (via getNama())   : Siti Aminah
Tanggal Pinjam 1 (via getTanggal()): 2026-09-02

=== 5. RINGKASAN DATA AKHIR PEMINJAMAN ===
[PEMINJAMAN 1] Tgl: 2026-09-02 | Peminjam: Arjuna Lanang | Buku: Pemrograman Java Lanjut
[PEMINJAMAN 2] Tgl: 2026-09-05 | Peminjam: Siti Aminah   | Buku: Struktur Data & Algoritma"""

t_log = Table([[Paragraph(running_log.replace('\n', '<br/>'), terminal_style)]], colWidths=[540])
t_log.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
    ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#94A3B8")),
    ('PADDING', (0,0), (-1,-1), 5),
]))
story.append(t_log)
story.append(Spacer(1, 6))

story.append(Paragraph("8. Sprint Review", h1_style))
review_data = [
    [Paragraph("Item Kriteria", th_style), Paragraph("Hasil Implementasi", th_style)],
    [Paragraph("Class berhasil dibuat", td_style), Paragraph("Berhasil dibuat 3 class: Buku, Anggota, Peminjaman", td_style)],
    [Paragraph("Object berhasil dibuat", td_style), Paragraph("Berhasil dibuat minimal 2 objek per class pada Main.java", td_style)],
    [Paragraph("Constructor berjalan", td_style), Paragraph("Berjalan sempurna menginisialisasi seluruh atribut objek", td_style)],
    [Paragraph("Method berjalan", td_style), Paragraph("Berhasil mengeksekusi method tanpa param, param, & return value", td_style)],
    [Paragraph("Program dapat dijalankan", td_style), Paragraph("Berhasil dikompilasi dan dijalankan tanpa error/exception", td_style)]
]
t_rev = Table(review_data, colWidths=[160, 380])
t_rev.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), PRIMARY),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
    ('PADDING', (0,0), (-1,-1), 4),
]))
story.append(t_rev)
story.append(Spacer(1, 6))

story.append(Paragraph("9. Sprint Retrospective", h1_style))
retro_text = """
<b>What Went Well?</b> Seluruh struktur class (Buku, Anggota, Peminjaman), atribut, constructor, dan ketiga jenis method berhasil dibangun dengan rapi serta dijalankan tanpa error.<br/>
<b>What Went Wrong?</b> Atribut class masih menggunakan tingkat akses default/package-private sehingga nilainya masih dapat diubah secara langsung dari luar class tanpa validasi.<br/>
<b>Improvement:</b> Pada Sprint P3 berikutnya, seluruh atribut class akan dienkapsulasi menggunakan access modifier <i>private</i> serta ditambahkan getter, setter, dan validasi data terkontrol.
"""
story.append(Paragraph(retro_text, body_style))

# Build Document
doc.build(story)
print(f"PDF successfully updated at: {pdf_filename}")

if os.path.exists(os.path.dirname(alt_pdf_dir)):
    os.makedirs(alt_pdf_dir, exist_ok=True)
    shutil.copy(pdf_filename, alt_pdf_filename)
    print(f"PDF copy saved at: {alt_pdf_filename}")
