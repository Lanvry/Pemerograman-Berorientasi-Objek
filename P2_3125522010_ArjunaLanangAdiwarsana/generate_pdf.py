import os
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
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
    fontSize=18,
    leading=22,
    textColor=PRIMARY,
    alignment=1, # Center
    spaceAfter=10
)

subtitle_style = ParagraphStyle(
    'DocSubtitle',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=11,
    leading=14,
    textColor=SECONDARY,
    alignment=1,
    spaceAfter=15
)

h1_style = ParagraphStyle(
    'SectionH1',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=13,
    leading=16,
    textColor=PRIMARY,
    spaceBefore=10,
    spaceAfter=6
)

body_style = ParagraphStyle(
    'BodyDark',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=9.5,
    leading=13,
    textColor=TEXT_DARK,
    spaceAfter=6
)

code_style = ParagraphStyle(
    'CodeBlock',
    parent=styles['Normal'],
    fontName='Courier-Bold',
    fontSize=8,
    leading=10,
    textColor=colors.HexColor("#0F172A")
)

terminal_style = ParagraphStyle(
    'TerminalBlock',
    parent=styles['Normal'],
    fontName='Courier',
    fontSize=7.5,
    leading=9.5,
    textColor=colors.HexColor("#0F172A")
)

th_style = ParagraphStyle(
    'TH',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=8.5,
    leading=11,
    textColor=colors.white
)

td_style = ParagraphStyle(
    'TD',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=8,
    leading=10.5,
    textColor=TEXT_DARK
)

story = []

# ==========================================
# PAGE 1: Project Overview & Sprint Backlog
# ==========================================
story.append(Paragraph("LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK", subtitle_style))
story.append(Paragraph("MODUL 2: IMPLEMENTASI CLASS, OBJECT, ATTRIBUTE, METHOD, DAN CONSTRUCTOR", title_style))
story.append(HRFlowable(width="100%", thickness=1.5, color=PRIMARY, spaceAfter=12))

story.append(Paragraph("1. Profil Proyek", h1_style))
project_info = [
    [Paragraph("<b>Nama Proyek:</b>", body_style), Paragraph("Sistem Toko Online (E-Commerce Management)", body_style)],
    [Paragraph("<b>Pengembang:</b>", body_style), Paragraph("Arjuna Lanang Ading Warsana", body_style)],
    [Paragraph("<b>Mata Kuliah:</b>", body_style), Paragraph("Workshop Pemrograman Framework (PBO)", body_style)],
    [Paragraph("<b>Basis Proyek (P1):</b>", body_style), Paragraph("Sistem Manajemen Produk & Transaksi Toko", body_style)]
]
t_proj = Table(project_info, colWidths=[120, 420])
t_proj.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), BG_LIGHT),
    ('PADDING', (0,0), (-1,-1), 5),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('BOX', (0,0), (-1,-1), 0.5, BORDER_COLOR),
]))
story.append(t_proj)
story.append(Spacer(1, 10))

story.append(Paragraph("2. Product Goal", h1_style))
story.append(Paragraph("Membangun sistem toko online yang terstruktur dan modular untuk mengelola katalog produk, profil pelanggan, dan alur transaksi secara otomatis dengan mengimplementasikan prinsip-prinsip Pemrograman Berorientasi Obyek (PBO).", body_style))
story.append(Spacer(1, 8))

story.append(Paragraph("3. Sprint Goal (Sprint P2)", h1_style))
story.append(Paragraph("Mengimplementasikan class utama proyek (Produk, Pelanggan, Transaksi) sehingga objek dapat dibuat, diberi data awal melalui constructor, serta menjalankan operasi dasar method (tanpa parameter, dengan parameter, dan return value).", body_style))
story.append(Spacer(1, 10))

story.append(Paragraph("4. Sprint Backlog (P2)", h1_style))
backlog_data = [
    [Paragraph("ID", th_style), Paragraph("Sprint Backlog Item", th_style), Paragraph("Status", th_style)],
    [Paragraph("SB-01", td_style), Paragraph("Membuat class Produk beserta atribut (kodeProduk, nama, harga, stok)", td_style), Paragraph("DONE", td_style)],
    [Paragraph("SB-02", td_style), Paragraph("Membuat class Pelanggan beserta atribut (idPelanggan, nama, email, saldo)", td_style), Paragraph("DONE", td_style)],
    [Paragraph("SB-03", td_style), Paragraph("Membuat class Transaksi beserta atribut (kodeTransaksi, produk, pelanggan, jumlah)", td_style), Paragraph("DONE", td_style)],
    [Paragraph("SB-04", td_style), Paragraph("Membuat constructor pada setiap class untuk inisialisasi objek", td_style), Paragraph("DONE", td_style)],
    [Paragraph("SB-05", td_style), Paragraph("Membuat method utama (tanpa param, dengan param, return value)", td_style), Paragraph("DONE", td_style)],
    [Paragraph("SB-06", td_style), Paragraph("Membuat minimal 2 objek per class dan skenario pengujian pada Main.java", td_style), Paragraph("DONE", td_style)]
]
t_backlog = Table(backlog_data, colWidths=[45, 425, 70])
t_backlog.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), PRIMARY),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
    ('PADDING', (0,0), (-1,-1), 5),
]))
story.append(t_backlog)

story.append(PageBreak())

# ==========================================
# PAGE 2: Object Refinement & Class Diagram
# ==========================================
story.append(Paragraph("5. Refinement Objek dan Spesifikasi Class", h1_style))
story.append(Paragraph("Tabel berikut menyajikan pemetaan kandidat objek P1 menjadi class Java beserta spesifikasi atribut, method, dan constructor:", body_style))
story.append(Spacer(1, 6))

refinement_data = [
    [Paragraph("Class", th_style), Paragraph("Attribute", th_style), Paragraph("Method", th_style), Paragraph("Constructor", th_style)],
    [
        Paragraph("<b>Produk</b>", td_style),
        Paragraph("• kodeProduk (String)<br/>• nama (String)<br/>• harga (double)<br/>• stok (int)", td_style),
        Paragraph("• tampilkanData()<br/>• tambahStok(int)<br/>• hitungTotalHarga(int)<br/>• getNama(), getHarga()", td_style),
        Paragraph("Produk(kodeProduk, nama, harga, stok)", td_style)
    ],
    [
        Paragraph("<b>Pelanggan</b>", td_style),
        Paragraph("• idPelanggan (String)<br/>• nama (String)<br/>• email (String)<br/>• saldo (double)", td_style),
        Paragraph("• tampilkanData()<br/>• topUpSaldo(double)<br/>• kelayakanTransaksi(double)<br/>• getNama(), getSaldo()", td_style),
        Paragraph("Pelanggan(idPelanggan, nama, email, saldo)", td_style)
    ],
    [
        Paragraph("<b>Transaksi</b>", td_style),
        Paragraph("• kodeTransaksi (String)<br/>• produk (Produk)<br/>• pelanggan (Pelanggan)<br/>• jumlah (int)", td_style),
        Paragraph("• tampilkanDetailTransaksi()<br/>• ubahJumlah(int)<br/>• hitungTotalBayar()<br/>• prosesTransaksi()", td_style),
        Paragraph("Transaksi(kodeTransaksi, produk, pelanggan, jumlah)", td_style)
    ]
]
t_ref = Table(refinement_data, colWidths=[70, 140, 190, 140])
t_ref.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), SECONDARY),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
    ('PADDING', (0,0), (-1,-1), 5),
]))
story.append(t_ref)
story.append(Spacer(1, 12))

story.append(Paragraph("6. Diagram Sederhana Class (UML Diagram)", h1_style))
story.append(Paragraph("Relasi dan hubungan antar class dalam proyek Sistem Toko Online:", body_style))
story.append(Spacer(1, 4))

def build_uml_diagram():
    d = Drawing(540, 325)
    
    COLOR_PRIMARY = colors.HexColor("#1A365D")   # Deep Navy Header
    COLOR_SECONDARY = colors.HexColor("#2B6CB0") # Slate Blue Header
    COLOR_BG = colors.HexColor("#F8FAFC")        # Soft Light BG
    COLOR_BORDER = colors.HexColor("#475569")    # Dark slate border
    COLOR_TEXT = colors.HexColor("#0F172A")      # Main text
    COLOR_LINE = colors.HexColor("#2563EB")      # Blue line connector

    def draw_class_card(x, y, w, h, title, attributes, methods, header_color):
        d.add(Rect(x, y, w, h, fillColor=COLOR_BG, strokeColor=COLOR_BORDER, strokeWidth=1.2, rx=4, ry=4))
        
        th = 22
        d.add(Rect(x, y + h - th, w, th, fillColor=header_color, strokeColor=COLOR_BORDER, strokeWidth=1.2, rx=4, ry=4))
        d.add(Rect(x, y + h - th, w, 5, fillColor=header_color, strokeColor=colors.transparent, strokeWidth=0))
        d.add(String(x + w/2.0, y + h - 15, title, textAnchor='middle', fontName='Helvetica-Bold', fontSize=9.5, fillColor=colors.white))
        
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

    # Box 1: Produk (Top Left)
    produks_attrs = [
        "- kodeProduk : String",
        "- nama : String",
        "- harga : double",
        "- stok : int"
    ]
    produks_mths = [
        "+ Produk(kode, nama, harga, stok)",
        "+ tampilkanData() : void",
        "+ tambahStok(jumlah: int) : void",
        "+ hitungTotalHarga(qty: int) : double"
    ]
    draw_class_card(10, 175, 250, 130, "Class: Produk", produks_attrs, produks_mths, COLOR_PRIMARY)

    # Box 2: Pelanggan (Top Right)
    pelanggan_attrs = [
        "- idPelanggan : String",
        "- nama : String",
        "- email : String",
        "- saldo : double"
    ]
    pelanggan_mths = [
        "+ Pelanggan(id, nama, email, saldo)",
        "+ tampilkanData() : void",
        "+ topUpSaldo(jumlah: double) : void",
        "+ kelayakanTransaksi(nom: double) : boolean"
    ]
    draw_class_card(280, 175, 250, 130, "Class: Pelanggan", pelanggan_attrs, pelanggan_mths, COLOR_PRIMARY)

    # Box 3: Transaksi (Bottom Center)
    transaksi_attrs = [
        "- kodeTransaksi : String",
        "- produk : Produk",
        "- pelanggan : Pelanggan",
        "- jumlah : int"
    ]
    transaksi_mths = [
        "+ Transaksi(kode, produk, pelanggan, jumlah)",
        "+ tampilkanDetailTransaksi() : void",
        "+ ubahJumlah(jumlah: int) : void",
        "+ hitungTotalBayar() : double",
        "+ prosesTransaksi() : boolean"
    ]
    draw_class_card(145, 0, 250, 140, "Class: Transaksi", transaksi_attrs, transaksi_mths, COLOR_SECONDARY)

    # Association Connectors (y = 140 to 175 gap)
    # Left Line: Produk (x=135, y=175) down to Transaksi (x=195, y=140)
    d.add(Line(135, 175, 135, 157.5, strokeColor=COLOR_LINE, strokeWidth=1.5))
    d.add(Line(135, 157.5, 195, 157.5, strokeColor=COLOR_LINE, strokeWidth=1.5))
    d.add(Line(195, 157.5, 195, 140, strokeColor=COLOR_LINE, strokeWidth=1.5))
    
    # Arrow head pointing to Produk (bottom of card)
    d.add(Polygon([135, 175, 131, 167, 139, 167], fillColor=COLOR_LINE, strokeColor=COLOR_LINE))
    
    # Multiplicity Text Left
    d.add(String(142, 165, "1", fontName='Helvetica-Bold', fontSize=8.5, fillColor=COLOR_PRIMARY))
    d.add(String(202, 143, "* (1..N)", fontName='Helvetica-Bold', fontSize=7.5, fillColor=COLOR_PRIMARY))

    # Right Line: Pelanggan (x=405, y=175) down to Transaksi (x=345, y=140)
    d.add(Line(405, 175, 405, 157.5, strokeColor=COLOR_LINE, strokeWidth=1.5))
    d.add(Line(405, 157.5, 345, 157.5, strokeColor=COLOR_LINE, strokeWidth=1.5))
    d.add(Line(345, 157.5, 345, 140, strokeColor=COLOR_LINE, strokeWidth=1.5))
    
    # Arrow head pointing to Pelanggan (bottom of card)
    d.add(Polygon([405, 175, 401, 167, 409, 167], fillColor=COLOR_LINE, strokeColor=COLOR_LINE))

    # Multiplicity Text Right
    d.add(String(394, 165, "1", fontName='Helvetica-Bold', fontSize=8.5, fillColor=COLOR_PRIMARY))
    d.add(String(325, 143, "* (1..N)", fontName='Helvetica-Bold', fontSize=7.5, fillColor=COLOR_PRIMARY))

    return d

story.append(build_uml_diagram())

story.append(PageBreak())

# ==========================================
# PAGE 3: Output Screenshot, Review, & Retro
# ==========================================
story.append(Paragraph("7. Hasil Executing / Running Program (Main.java)", h1_style))

running_log = """=== 1. PEMBUATAN OBJEK (CONSTRUCTOR) ===
[SUCCESS] Objek produk1, produk2, pelanggan1, pelanggan2 berhasil dibuat.

=== 2. PENGUJIAN METHOD TANPA PARAMETER ===
--- Detail Produk ---
Kode Produk : P001 | Nama : Laptop Gaming Asus | Harga : Rp 15.000.000,00 | Stok : 10
--- Detail Pelanggan ---
ID : C001 | Nama : Arjuna Lanang | Saldo : Rp 20.000.000,00

=== 3. PENGUJIAN METHOD DENGAN PARAMETER ===
[INFO] Stok Laptop Gaming Asus berhasil ditambahkan sebanyak 5. Stok sekarang: 15
[INFO] Top up saldo untuk Budi Santoso sebesar Rp 2.000.000,00 berhasil. Saldo baru: Rp 6.000.000,00

=== 4. PENGUJIAN METHOD DENGAN RETURN VALUE ===
Kalkulasi total harga 2 unit Laptop Gaming Asus: Rp 30.000.000,00
Apakah pelanggan Budi Santoso layak belanja Rp 5.000.000? YA

=== 5. PEMBUATAN OBJEK TRANSAKSI & OPERASI BERSAMA ===
[PROSES TRANSAKSI TRX001]
[BERHASIL] Transaksi TRX001 berhasil diproses!
Sisa stok Laptop Gaming Asus: 14 | Sisa saldo Arjuna Lanang: Rp 5.000.000,00

--- Transaksi 2 (Mengubah Jumlah dengan Parameter) ---
[INFO] Jumlah pembelian transaksi TRX002 diubah menjadi 2
[PROSES TRANSAKSI TRX002]
[GAGAL] Saldo pelanggan Budi Santoso tidak mencukupi. (Saldo: Rp 6.000.000, Total: Rp 10.000.000)"""

t_log = Table([[Paragraph(running_log.replace('\n', '<br/>'), terminal_style)]], colWidths=[540])
t_log.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
    ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#94A3B8")),
    ('PADDING', (0,0), (-1,-1), 6),
]))
story.append(t_log)
story.append(Spacer(1, 8))

story.append(Paragraph("8. Sprint Review", h1_style))
review_data = [
    [Paragraph("Item Kriteria", th_style), Paragraph("Hasil Implementasi", th_style)],
    [Paragraph("Class berhasil dibuat", td_style), Paragraph("Berhasil dibuat 3 class: Produk, Pelanggan, Transaksi", td_style)],
    [Paragraph("Object berhasil dibuat", td_style), Paragraph("Berhasil dibuat minimal 2 objek per class pada Main.java", td_style)],
    [Paragraph("Constructor berjalan", td_style), Paragraph("Berjalan sempurna menginisialisasi seluruh atribut objek", td_style)],
    [Paragraph("Method berjalan", td_style), Paragraph("Berhasil mengeksekusi method tanpa param, param, & return value", td_style)],
    [Paragraph("Program dapat dijalankan", td_style), Paragraph("Berhasil dikompilasi dan dijalankan tanpa error/exception", td_style)],
    [Paragraph("Kendala", td_style), Paragraph("Tidak ada kendala teknis utama selama pengerjaan Modul 2", td_style)]
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
story.append(Spacer(1, 8))

story.append(Paragraph("9. Sprint Retrospective", h1_style))
retro_text = """
<b>What Went Well?</b> Seluruh struktur class, atribut, constructor, dan method berhasil dibangun dengan bersih dan rapi. Seluruh skenario pengujian pada Main.java berjalan sesuai ekspektasi.<br/>
<b>What Went Wrong?</b> Diperlukan perhatian khusus pada validasi kondisi stok dan saldo sebelum transaksi diproses agar tidak terjadi nilai minus.<br/>
<b>Improvement:</b> Pada Sprint P3 berikutnya, atribut class akan dienkapsulasi menggunakan access modifier <i>private</i> serta ditambahkan getter/setter dan validasi ketat.
"""
story.append(Paragraph(retro_text, body_style))

# Build Document
doc.build(story)
print(f"PDF successfully updated at: {pdf_filename}")

import shutil
if os.path.exists(os.path.dirname(alt_pdf_dir)):
    os.makedirs(alt_pdf_dir, exist_ok=True)
    shutil.copy(pdf_filename, alt_pdf_filename)
    print(f"PDF copy saved at: {alt_pdf_filename}")

