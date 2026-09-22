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

doc = SimpleDocTemplate(
    pdf_filename,
    pagesize=letter,
    leftMargin=36,
    rightMargin=36,
    topMargin=32,
    bottomMargin=32
)

styles = getSampleStyleSheet()

PRIMARY = colors.HexColor("#1A365D")   # Navy
SECONDARY = colors.HexColor("#2B6CB0") # Blue
BG_LIGHT = colors.HexColor("#F8FAFC")
TEXT_DARK = colors.HexColor("#1E293B")
BORDER_COLOR = colors.HexColor("#CBD5E1")

title_style = ParagraphStyle(
    'DocTitle',
    parent=styles['Heading1'],
    fontName='Helvetica-Bold',
    fontSize=14,
    leading=17,
    textColor=PRIMARY,
    alignment=1,
    spaceAfter=4
)

subtitle_style = ParagraphStyle(
    'DocSubtitle',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=9.5,
    leading=12,
    textColor=SECONDARY,
    alignment=1,
    spaceAfter=6
)

h1_style = ParagraphStyle(
    'SectionH1',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=10,
    leading=13,
    textColor=PRIMARY,
    spaceBefore=6,
    spaceAfter=3
)

body_style = ParagraphStyle(
    'BodyDark',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=8,
    leading=10.5,
    textColor=TEXT_DARK,
    spaceAfter=3
)

terminal_style = ParagraphStyle(
    'TerminalBlock',
    parent=styles['Normal'],
    fontName='Courier',
    fontSize=6.5,
    leading=8,
    textColor=colors.HexColor("#0F172A")
)

th_style = ParagraphStyle(
    'TH',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=7.5,
    leading=9.5,
    textColor=colors.white,
    alignment=0
)

td_style = ParagraphStyle(
    'TD',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=7.2,
    leading=9,
    textColor=TEXT_DARK
)

story = []

# =========================================================================
# HALAMAN 1: IDENTITAS, SPRINT GOAL, SPRINT BACKLOG, TABEL IDENTIFIKASI RELASI
# =========================================================================
story.append(Paragraph("LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK", subtitle_style))
story.append(Paragraph("MODUL 4: RELASI ANTAROBJECT (ASSOCIATION, AGGREGATION, DAN COMPOSITION)", title_style))
story.append(HRFlowable(width="100%", thickness=1.5, color=PRIMARY, spaceAfter=6))

story.append(Paragraph("1. Profil Proyek", h1_style))
project_info = [
    [Paragraph("<b>Nama Proyek:</b>", body_style), Paragraph("Sistem Manajemen Perpustakaan", body_style)],
    [Paragraph("<b>Nama Mahasiswa / NRP:</b>", body_style), Paragraph("Arjuna Lanang Adiwarsana / 3125522010", body_style)],
    [Paragraph("<b>Program Studi / Kampus:</b>", body_style), Paragraph("D3 PJJ Teknik Informatika - PENS PSDKU Sumenep", body_style)],
    [Paragraph("<b>Fokus Pertemuan (P4):</b>", body_style), Paragraph("Menghubungkan class proyek menggunakan relasi Association, Aggregation, dan Composition", body_style)]
]
t_proj = Table(project_info, colWidths=[130, 410])
t_proj.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), BG_LIGHT),
    ('PADDING', (0,0), (-1,-1), 3),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('BOX', (0,0), (-1,-1), 0.5, BORDER_COLOR),
]))
story.append(t_proj)
story.append(Spacer(1, 4))

story.append(Paragraph("2. Sprint Goal (P4)", h1_style))
story.append(Paragraph("Menghubungkan class utama sistem perpustakaan (Buku, Anggota, KartuAnggota, Peminjaman, dan Perpustakaan) agar objek dapat saling bertukar data dan berinteraksi secara modular sesuai konsep relasi objek.", body_style))
story.append(Spacer(1, 4))

story.append(Paragraph("3. Sprint Backlog (P4)", h1_style))
backlog_data = [
    [Paragraph("ID", th_style), Paragraph("Sprint Backlog Item", th_style), Paragraph("Status", th_style)],
    [Paragraph("SB-01", td_style), Paragraph("Mengidentifikasi kebutuhan relasi antarclass dalam sistem", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-02", td_style), Paragraph("Membuat pembaruan class diagram UML beserta cardinalities", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-03", td_style), Paragraph("Implementasi relasi association pada class Peminjaman", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-04", td_style), Paragraph("Implementasi relasi aggregation pada class Perpustakaan", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-05", td_style), Paragraph("Implementasi relasi composition pada class Anggota dan KartuAnggota", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-06", td_style), Paragraph("Membuat skenario pengujian interaksi objek di Main.java", td_style), Paragraph("Done", td_style)]
]
t_backlog = Table(backlog_data, colWidths=[40, 435, 65])
t_backlog.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), PRIMARY),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
    ('PADDING', (0,0), (-1,-1), 3),
]))
story.append(t_backlog)
story.append(Spacer(1, 4))

story.append(Paragraph("4. Bagian A: Tabel Identifikasi Relasi Antarclass", h1_style))
story.append(Paragraph("Identifikasi relasi antar class yang digunakan pada proyek Sistem Perpustakaan:", body_style))
story.append(Spacer(1, 2))

relasi_data = [
    [Paragraph("Class A", th_style), Paragraph("Class B", th_style), Paragraph("Relasi", th_style), Paragraph("Alasan Pemilihan Relasi", th_style)],
    [
        Paragraph("<b>Peminjaman</b>", td_style),
        Paragraph("<b>Anggota</b>", td_style),
        Paragraph("Association", td_style),
        Paragraph("Anggota melakukan peminjaman buku. Objek Anggota berdiri sendiri dan hanya direferensikan saat ada transaksi peminjaman.", td_style)
    ],
    [
        Paragraph("<b>Peminjaman</b>", td_style),
        Paragraph("<b>Buku</b>", td_style),
        Paragraph("Association", td_style),
        Paragraph("Buku dipinjam dalam transaksi peminjaman. Peminjaman hanya meminjam data referensi Buku tanpa memiliki daur hidup buku tersebut.", td_style)
    ],
    [
        Paragraph("<b>Perpustakaan</b>", td_style),
        Paragraph("<b>Buku</b>", td_style),
        Paragraph("Aggregation", td_style),
        Paragraph("Perpustakaan memiliki koleksi daftar buku. Objek Buku dibuat di luar dan tetap ada meskipun objek Perpustakaan ditiadakan.", td_style)
    ],
    [
        Paragraph("<b>Anggota</b>", td_style),
        Paragraph("<b>KartuAnggota</b>", td_style),
        Paragraph("Composition", td_style),
        Paragraph("Kartu anggota dibuat langsung di dalam constructor Anggota. Kartu tidak dapat berdiri sendiri jika objek Anggota tidak ada.", td_style)
    ]
]
t_relasi = Table(relasi_data, colWidths=[75, 75, 75, 315])
t_relasi.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), SECONDARY),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
    ('PADDING', (0,0), (-1,-1), 3.5),
]))
story.append(t_relasi)

story.append(PageBreak())

# =========================================================================
# HALAMAN 2: CLASS DIAGRAM & PENJELASAN RELASI
# =========================================================================
story.append(Paragraph("5. Bagian B: Pembaruan Class Diagram (UML)", h1_style))
story.append(Paragraph("Class diagram sistem perpustakaan yang menunjukkan atribut, method, tipe relasi, dan multiplicity:", body_style))
story.append(Spacer(1, 2))

def draw_p4_uml():
    d = Drawing(540, 240)
    
    C_HEADER_PR = colors.HexColor("#1A365D")
    C_HEADER_SEC = colors.HexColor("#2B6CB0")
    C_HEADER_ACC = colors.HexColor("#0D9488")
    C_BG = colors.HexColor("#F8FAFC")
    C_BORDER = colors.HexColor("#475569")
    C_TEXT = colors.HexColor("#0F172A")
    C_LINE = colors.HexColor("#1E293B")
    
    def card(x, y, w, h, title, attrs, mths, header_c):
        d.add(Rect(x, y, w, h, fillColor=C_BG, strokeColor=C_BORDER, strokeWidth=1, rx=3, ry=3))
        th = 15
        d.add(Rect(x, y + h - th, w, th, fillColor=header_c, strokeColor=C_BORDER, strokeWidth=1, rx=3, ry=3))
        d.add(Rect(x, y + h - th, w, 4, fillColor=header_c, strokeColor=colors.transparent, strokeWidth=0))
        d.add(String(x + w/2.0, y + h - 11, title, textAnchor='middle', fontName='Helvetica-Bold', fontSize=8, fillColor=colors.white))
        
        cy = y + h - th - 10
        for a in attrs:
            d.add(String(x + 5, cy, a, fontName='Courier', fontSize=6.2, fillColor=C_TEXT))
            cy -= 8.5
            
        div_y = cy + 2
        d.add(Line(x, div_y, x + w, div_y, strokeColor=C_BORDER, strokeWidth=0.6))
        
        cy = div_y - 9
        for m in mths:
            d.add(String(x + 5, cy, m, fontName='Courier', fontSize=6.2, fillColor=C_TEXT))
            cy -= 8.5

    # 1. Buku (Top Center)
    card(190, 140, 160, 95, "Buku", 
         ["- kode : String", "- judul : String", "- penulis : String", "- tahunTerbit : int"],
         ["+ getKode() : String", "+ getJudul() : String", "+ getPenulis() : String", "+ tampilkanData() : void"],
         C_HEADER_PR)

    # 2. Perpustakaan (Top Left)
    card(0, 140, 150, 95, "Perpustakaan",
         ["- nama : String", "- alamat : String", "- daftarBuku : List<Buku>"],
         ["+ tambahBuku(Buku) : void", "+ getDaftarBuku() : List", "+ tampilkanDaftarBuku() : void"],
         C_HEADER_SEC)

    # 3. Anggota (Bottom Right)
    card(280, 0, 160, 105, "Anggota",
         ["- id : String", "- nama : String", "- alamat : String", "- kartuAnggota : KartuAnggota"],
         ["+ getId() : String", "+ getNama() : String", "+ getKartuAnggota() : Kartu", "+ tampilkanData() : void"],
         C_HEADER_PR)

    # 4. KartuAnggota (Bottom Far Right)
    card(465, 10, 75, 85, "KartuAnggota",
         ["- nomor : String", "- status : String"],
         ["+ getNomor() : String", "+ getStatus() : String", "+ tampilkanData()"],
         C_HEADER_ACC)

    # 5. Peminjaman (Bottom Left)
    card(30, 0, 170, 105, "Peminjaman",
         ["- kodePinjam : String", "- tanggal : String", "- buku : Buku", "- anggota : Anggota", "- durasiHari : int"],
         ["+ getKodePinjam() : String", "+ getBuku() : Buku", "+ getAnggota() : Anggota", "+ tampilkanData() : void"],
         C_HEADER_SEC)

    # Connectors:
    # 1. Aggregation: Perpustakaan ◇---- Buku
    d.add(Line(150, 187, 190, 187, strokeColor=C_LINE, strokeWidth=1.2))
    d.add(Polygon([150, 187, 155, 190, 160, 187, 155, 184], fillColor=colors.white, strokeColor=C_LINE, strokeWidth=1.2))
    d.add(String(163, 191, "1", fontName='Helvetica-Bold', fontSize=7, fillColor=C_HEADER_PR))
    d.add(String(180, 191, "*", fontName='Helvetica-Bold', fontSize=7, fillColor=C_HEADER_PR))
    d.add(String(155, 175, "Aggregation", fontName='Helvetica-Oblique', fontSize=5.5, fillColor=SECONDARY))

    # 2. Composition: Anggota ◆---- KartuAnggota
    d.add(Line(440, 50, 465, 50, strokeColor=C_LINE, strokeWidth=1.2))
    d.add(Polygon([440, 50, 445, 53, 450, 50, 445, 47], fillColor=C_LINE, strokeColor=C_LINE, strokeWidth=1.2))
    d.add(String(452, 54, "1", fontName='Helvetica-Bold', fontSize=7, fillColor=C_HEADER_PR))
    d.add(String(458, 54, "1", fontName='Helvetica-Bold', fontSize=7, fillColor=C_HEADER_PR))
    d.add(String(435, 38, "Composition", fontName='Helvetica-Oblique', fontSize=5.5, fillColor=C_HEADER_ACC))

    # 3. Association: Peminjaman ---- Buku
    d.add(Line(115, 105, 115, 125, strokeColor=C_LINE, strokeWidth=1))
    d.add(Line(115, 125, 240, 125, strokeColor=C_LINE, strokeWidth=1))
    d.add(Line(240, 125, 240, 140, strokeColor=C_LINE, strokeWidth=1))
    d.add(String(120, 110, "*", fontName='Helvetica-Bold', fontSize=7, fillColor=C_HEADER_PR))
    d.add(String(245, 132, "1", fontName='Helvetica-Bold', fontSize=7, fillColor=C_HEADER_PR))
    d.add(String(145, 128, "Association", fontName='Helvetica-Oblique', fontSize=5.5, fillColor=PRIMARY))

    # 4. Association: Peminjaman ---- Anggota
    d.add(Line(200, 50, 280, 50, strokeColor=C_LINE, strokeWidth=1))
    d.add(String(205, 54, "*", fontName='Helvetica-Bold', fontSize=7, fillColor=C_HEADER_PR))
    d.add(String(270, 54, "1", fontName='Helvetica-Bold', fontSize=7, fillColor=C_HEADER_PR))
    d.add(String(215, 42, "Association", fontName='Helvetica-Oblique', fontSize=5.5, fillColor=PRIMARY))

    return d

story.append(draw_p4_uml())
story.append(Spacer(1, 4))

story.append(Paragraph("6. Penjelasan Implementasi Relasi (Bagian C, D, E)", h1_style))

penjelasan_relasi = [
    [
        Paragraph("<b>C. Association</b><br/>(Peminjaman - Anggota / Buku)", body_style),
        Paragraph("Class <code>Peminjaman</code> memiliki atribut <code>private Anggota anggota;</code> dan <code>private Buku buku;</code>. Keduanya berinteraksi lewat parameter constructor dan method. Daur hidup objek mandiri; menghapus transaksi peminjaman tidak akan menghapus data anggota atau buku.", td_style)
    ],
    [
        Paragraph("<b>D. Aggregation</b><br/>(Perpustakaan - Buku)", body_style),
        Paragraph("Class <code>Perpustakaan</code> memiliki atribut <code>private List&lt;Buku&gt; daftarBuku;</code>. Objek buku dibuat terlebih dahulu di luar perpustakaan lalu dimasukkan lewat method <code>tambahBuku(buku)</code>. Jika objek perpustakaan dihapus, buku tetap ada di memori.", td_style)
    ],
    [
        Paragraph("<b>E. Composition</b><br/>(Anggota - KartuAnggota)", body_style),
        Paragraph("Class <code>Anggota</code> memiliki atribut <code>private KartuAnggota kartuAnggota;</code> yang langsung diinisialisasi di dalam constructor Anggota (<code>this.kartuAnggota = new KartuAnggota(...)</code>). Kartu anggota tidak dibuat terpisah di luar class Anggota.", td_style)
    ]
]
t_penjelasan = Table(penjelasan_relasi, colWidths=[150, 390])
t_penjelasan.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), BG_LIGHT),
    ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ('PADDING', (0,0), (-1,-1), 4),
]))
story.append(t_penjelasan)

story.append(PageBreak())

# =========================================================================
# HALAMAN 3: SCREENSHOT HASIL RUNNING, SKENARIO TEST, REVIEW & RETROSPECTIVE
# =========================================================================
story.append(Paragraph("7. Hasil Eksekusi Program (Console Output Main.java)", h1_style))

console_log = """=== 1. UJI PEMBUATAN OBJEK (TEST 1) ===
[Sukses] Seluruh objek berhasil diinisialisasi.

=== 2. UJI INTERAKSI ANTAR OBJEK (TEST 2) ===
--- Cek Data Anggota & Kartu (Komposisi) ---
ID Anggota   : A001 | Nama: Arjuna Lanang Adiwarsana | Nomor Kartu: KTA-A001 (Aktif)
ID Anggota   : A002 | Nama: Siti Aminah             | Nomor Kartu: KTA-A002 (Aktif)

--- Menambahkan Buku ke Perpustakaan (Agregasi) ---
[Sukses] Buku "Pemrograman Java" ditambahkan ke koleksi Perpustakaan PENS Sumenep
[Sukses] Buku "Struktur Data & Algoritma" ditambahkan ke koleksi Perpustakaan PENS Sumenep
[Sukses] Buku "Rekayasa Perangkat Lunak" ditambahkan ke koleksi Perpustakaan PENS Sumenep

--- Membuat Transaksi Peminjaman (Asosiasi) ---
[Sukses] Transaksi peminjaman berhasil dibuat.

=== 3. UJI PENGGUNAAN DATA OBJEK LAIN (TEST 3) ===
=== Koleksi Buku: Perpustakaan PENS Sumenep ===
Lokasi: Gedung A Lantai 2 Kampus PSDKU | Jumlah Koleksi: 3 Buku
1. [B001] Pemrograman Java (Nirwana Haidar, 2024)
2. [B002] Struktur Data & Algoritma (Budi Raharjo, 2022)
3. [B003] Rekayasa Perangkat Lunak (Ian Sommerville, 2023)

--- Detail Transaksi Peminjaman 1 ---
Kode Pinjam    : PJ-001 | Tanggal: 2026-09-22 | Durasi: 7 Hari
Peminjam       : Arjuna Lanang Adiwarsana (No. Kartu: KTA-A001)
Buku Dipinjam  : Pemrograman Java (Penulis: Nirwana Haidar)

--- Bukti Objek Buku Tetap Berdiri Sendiri (Agregasi) ---
Judul buku 1 via objek asli : Pemrograman Java
(Objek Buku tetap dapat diakses mandiri meskipun terdaftar di Perpustakaan)"""

t_console = Table([[Paragraph(console_log.replace('\n', '<br/>'), terminal_style)]], colWidths=[540])
t_console.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#F8FAFC")),
    ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#94A3B8")),
    ('PADDING', (0,0), (-1,-1), 3.5),
]))
story.append(t_console)
story.append(Spacer(1, 4))

story.append(Paragraph("8. Skenario Pengujian (Bagian F)", h1_style))
test_matrix = [
    [Paragraph("Skenario", th_style), Paragraph("Keterangan Pengujian", th_style), Paragraph("Status", th_style)],
    [Paragraph("<b>Test 1</b>", td_style), Paragraph("Pembuatan objek Buku, Perpustakaan, serta Anggota yang otomatis membuat KartuAnggota.", td_style), Paragraph("<b>Berhasil</b>", td_style)],
    [Paragraph("<b>Test 2</b>", td_style), Paragraph("Menambahkan buku ke perpustakaan (agregasi) dan membuat transaksi peminjaman (asosiasi).", td_style), Paragraph("<b>Berhasil</b>", td_style)],
    [Paragraph("<b>Test 3</b>", td_style), Paragraph("Mengakses data judul buku dan nama anggota dari method pada class Peminjaman dan Perpustakaan.", td_style), Paragraph("<b>Berhasil</b>", td_style)]
]
t_matrix = Table(test_matrix, colWidths=[50, 420, 70])
t_matrix.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), SECONDARY),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
    ('PADDING', (0,0), (-1,-1), 3),
]))
story.append(t_matrix)
story.append(Spacer(1, 4))

story.append(Paragraph("9. Sprint Review", h1_style))
review_data = [
    [Paragraph("Item Evaluasi", th_style), Paragraph("Hasil Evaluasi", th_style)],
    [Paragraph("Class diagram diperbarui", td_style), Paragraph("Sudah diperbarui dengan 5 class dan notasi relasi UML", td_style)],
    [Paragraph("Association berhasil", td_style), Paragraph("Class Peminjaman berhasil menghubungkan Anggota dan Buku", td_style)],
    [Paragraph("Aggregation berhasil", td_style), Paragraph("Class Perpustakaan berhasil menampung list Buku dari luar", td_style)],
    [Paragraph("Composition berhasil", td_style), Paragraph("Class Anggota berhasil membuat objek KartuAnggota di constructor", td_style)],
    [Paragraph("Object dapat berinteraksi", td_style), Paragraph("Semua objek dapat bertukar data via method getter/setter", td_style)],
    [Paragraph("Program berjalan", td_style), Paragraph("Program berhasil dikompilasi dan dijalankan tanpa error", td_style)],
    [Paragraph("Kendala", td_style), Paragraph("Tidak ada kendala, alur logika program berjalan sesuai rancangan", td_style)]
]
t_review = Table(review_data, colWidths=[160, 380])
t_review.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), PRIMARY),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
    ('PADDING', (0,0), (-1,-1), 2.5),
]))
story.append(t_review)
story.append(Spacer(1, 4))

story.append(Paragraph("10. Sprint Retrospective", h1_style))
retro_text = """
<b>What Went Well?</b> Seluruh relasi berhasil diterapkan dengan baik. Peminjaman dapat membaca data Anggota dan Buku, Perpustakaan dapat mengelola daftar buku, serta KartuAnggota terikat langsung dengan Anggota.<br/>
<b>What Went Wrong?</b> Sempat perlu penyesuaian saat membedakan cara inisialisasi objek pada agregasi (dibuat di luar) dengan komposisi (dibuat di dalam constructor).<br/>
<b>Improvement:</b> Pada praktikum berikutnya (P5 Inheritance), class dapat dirapikan lagi menggunakan superclass untuk class yang memiliki atribut serupa.
"""
story.append(Paragraph(retro_text, body_style))

# Render Dokumen
doc.build(story)
print(f"README.pdf successfully generated at: {pdf_filename}")
