import os
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.graphics.shapes import Drawing, Rect, String, Line, Polygon
from reportlab.pdfgen import canvas

script_dir = os.path.dirname(os.path.abspath(__file__))
pdf_filename = os.path.join(script_dir, "README.pdf")

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        canvas.Canvas.__init__(self, *args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_number(num_pages)
            canvas.Canvas.showPage(self)
        canvas.Canvas.save(self)

    def draw_page_number(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 7.5)
        self.setFillColor(colors.HexColor("#4B5563"))
        
        # Header text
        self.drawString(36, 762, "Praktikum Pemrograman Berorientasi Obyek - Modul 5: Inheritance")
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

# Skema warna akademis formal (Monochrome / Netral minimalis)
COLOR_TEXT = colors.HexColor("#111827")      # Hitam / Charcoal teks utama
COLOR_MUTED = colors.HexColor("#374151")     # Teks sekunder
COLOR_LINE = colors.HexColor("#9CA3AF")      # Garis tabel & pembatas
COLOR_HEADER_BG = colors.HexColor("#E5E7EB") # Latar abu-abu header tabel formal
COLOR_ROW_ALT = colors.HexColor("#F9FAFB")   # Zebra striping sangat lembut

title_style = ParagraphStyle(
    'DocTitle',
    parent=styles['Heading1'],
    fontName='Helvetica-Bold',
    fontSize=12,
    leading=15,
    textColor=COLOR_TEXT,
    alignment=1,
    spaceAfter=3
)

subtitle_style = ParagraphStyle(
    'DocSubtitle',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=8.5,
    leading=11,
    textColor=COLOR_MUTED,
    alignment=1,
    spaceAfter=3
)

h1_style = ParagraphStyle(
    'SectionH1',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=9,
    leading=11.5,
    textColor=COLOR_TEXT,
    spaceBefore=4,
    spaceAfter=2
)

body_style = ParagraphStyle(
    'BodyDark',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=7.2,
    leading=9.2,
    textColor=COLOR_TEXT,
    spaceAfter=2
)

terminal_style = ParagraphStyle(
    'TerminalBlock',
    parent=styles['Normal'],
    fontName='Courier',
    fontSize=5.8,
    leading=7.2,
    textColor=COLOR_TEXT
)

th_style = ParagraphStyle(
    'TH',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=6.8,
    leading=8.5,
    textColor=COLOR_TEXT,
    alignment=0
)

td_style = ParagraphStyle(
    'TD',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=6.6,
    leading=8.2,
    textColor=COLOR_TEXT
)

story = []

# =========================================================================
# HALAMAN 1: IDENTITAS, SPRINT GOAL, SPRINT BACKLOG, AUDIT CLASS & GENERALIZATION
# =========================================================================
story.append(Paragraph("LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK", subtitle_style))
story.append(Paragraph("MODUL 5: INHERITANCE, GENERALIZATION, SUPERCLASS, DAN SUBCLASS", title_style))
story.append(HRFlowable(width="100%", thickness=0.8, color=COLOR_TEXT, spaceAfter=4))

story.append(Paragraph("1. Profil Proyek", h1_style))
project_info = [
    [Paragraph("<b>Nama Proyek:</b>", body_style), Paragraph("Sistem Manajemen Perpustakaan (Refactoring Inheritance)", body_style)],
    [Paragraph("<b>Nama Mahasiswa / NRP:</b>", body_style), Paragraph("Arjuna Lanang Adiwarsana / 3125522010", body_style)],
    [Paragraph("<b>Program Studi / Kampus:</b>", body_style), Paragraph("D3 PJJ Teknik Informatika - PENS PSDKU Sumenep", body_style)],
    [Paragraph("<b>Fokus Pertemuan (P5):</b>", body_style), Paragraph("Generalization, Superclass (Anggota), Subclass (Mahasiswa & Dosen), extends, dan super", body_style)]
]
t_proj = Table(project_info, colWidths=[120, 420])
t_proj.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), colors.white),
    ('PADDING', (0,0), (-1,-1), 2.2),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('BOX', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#E5E7EB")),
]))
story.append(t_proj)
story.append(Spacer(1, 3))

story.append(Paragraph("2. Sprint Goal (P5)", h1_style))
story.append(Paragraph("Memperbaiki desain proyek P4 dengan mengidentifikasi class yang memiliki karakteristik serupa dan melakukan generalization menggunakan inheritance, menghasilkan Superclass <b>Anggota</b> serta Subclass <b>Mahasiswa</b> dan <b>Dosen</b> tanpa merusak relasi yang telah dibangun pada P4.", body_style))
story.append(Spacer(1, 3))

story.append(Paragraph("3. Sprint Backlog (P5)", h1_style))
backlog_data = [
    [Paragraph("ID", th_style), Paragraph("Sprint Backlog Item", th_style), Paragraph("Status", th_style)],
    [Paragraph("SB-01", td_style), Paragraph("Mencari class/entitas yang memiliki kesamaan atribut (duplikasi)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-02", td_style), Paragraph("Menentukan superclass (Anggota) untuk menampung atribut umum", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-03", td_style), Paragraph("Menentukan subclass (Mahasiswa & Dosen) untuk atribut khusus", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-04", td_style), Paragraph("Memperbarui class diagram UML sebelum dan sesudah refactoring", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-05", td_style), Paragraph("Implementasi pewarisan class menggunakan keyword extends", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-06", td_style), Paragraph("Implementasi pemanggilan constructor superclass menggunakan super(...)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-07", td_style), Paragraph("Melakukan pengujian inheritance dan integrasi relasi pada Main.java", td_style), Paragraph("Done", td_style)]
]
t_backlog = Table(backlog_data, colWidths=[38, 442, 60])
t_backlog.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('PADDING', (0,0), (-1,-1), 2.2),
]))
story.append(t_backlog)
story.append(Spacer(1, 3))

story.append(Paragraph("4. Bagian A: Audit Class & Identifikasi Generalization", h1_style))
story.append(Paragraph("Analisis dilakukan terhadap entitas pengguna sistem perpustakaan yang memiliki redundansi data personal:", body_style))

audit_data = [
    [Paragraph("Class Entitas", th_style), Paragraph("Attribute Umum (Superclass)", th_style), Paragraph("Attribute Khusus (Subclass)", th_style), Paragraph("Behavior Khusus", th_style)],
    [
        Paragraph("<b>Mahasiswa</b>", td_style),
        Paragraph("id, nama, alamat, kartuAnggota", td_style),
        Paragraph("nrp, prodi", td_style),
        Paragraph("getNrp(), getProdi(), tampilkanData()", td_style)
    ],
    [
        Paragraph("<b>Dosen</b>", td_style),
        Paragraph("id, nama, alamat, kartuAnggota", td_style),
        Paragraph("nip, departemen", td_style),
        Paragraph("getNip(), getDepartemen(), tampilkanData()", td_style)
    ]
]
t_audit = Table(audit_data, colWidths=[80, 150, 140, 170])
t_audit.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('PADDING', (0,0), (-1,-1), 3),
]))
story.append(t_audit)
story.append(Spacer(1, 3))

kandidat_box = [
    [
        Paragraph("<b>Kandidat Superclass:</b><br/>• <b>Anggota</b> (id, nama, alamat, kartuAnggota, tampilkanData())", td_style),
        Paragraph("<b>Kandidat Subclass:</b><br/>• <b>Mahasiswa</b> (extends Anggota + nrp, prodi)<br/>• <b>Dosen</b> (extends Anggota + nip, departemen)", td_style)
    ]
]
t_kandidat = Table(kandidat_box, colWidths=[270, 270])
t_kandidat.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), COLOR_ROW_ALT),
    ('BOX', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('INNERGRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('PADDING', (0,0), (-1,-1), 3.5),
]))
story.append(t_kandidat)

story.append(PageBreak())

# =========================================================================
# HALAMAN 2: CLASS DIAGRAM SEBELUM & SESUDAH, PENJELASAN HUBUNGAN IS-A
# =========================================================================
story.append(Paragraph("5. Bagian B: Pembaruan Class Diagram (UML)", h1_style))
story.append(Paragraph("Perbandingan struktur diagram kelas sebelum refactoring (P4) dan setelah penerapan inheritance (P5):", body_style))
story.append(Spacer(1, 1))

def draw_p5_uml():
    d = Drawing(540, 255)
    
    C_BG = colors.white
    C_HEADER_BG = colors.HexColor("#F3F4F6")
    C_BORDER = colors.HexColor("#374151")
    C_TEXT = colors.HexColor("#111827")
    C_LINE = colors.HexColor("#111827")
    
    def card(x, y, w, h, title, stereotype, attrs, mths):
        # Card body
        d.add(Rect(x, y, w, h, fillColor=C_BG, strokeColor=C_BORDER, strokeWidth=0.8))
        th = 15
        # Card header (subtle neutral grey)
        d.add(Rect(x, y + h - th, w, th, fillColor=C_HEADER_BG, strokeColor=C_BORDER, strokeWidth=0.8))
        
        full_title = f"<<{stereotype}>> {title}" if stereotype else title
        d.add(String(x + w/2.0, y + h - 11, full_title, textAnchor='middle', fontName='Helvetica-Bold', fontSize=6.5, fillColor=C_TEXT))
        
        cy = y + h - th - 8
        for a in attrs:
            d.add(String(x + 4, cy, a, fontName='Courier', fontSize=5.3, fillColor=C_TEXT))
            cy -= 7.2
            
        div_y = cy + 2
        d.add(Line(x, div_y, x + w, div_y, strokeColor=C_BORDER, strokeWidth=0.5))
        
        cy = div_y - 7.5
        for m in mths:
            d.add(String(x + 4, cy, m, fontName='Courier', fontSize=5.3, fillColor=C_TEXT))
            cy -= 7.2

    # --- TOP ROW: Aggregation & Superclass ---
    # 1. Perpustakaan (Top Left)
    card(0, 165, 130, 85, "Perpustakaan", "",
         ["- nama : String", "- alamat : String", "- daftarBuku : List"],
         ["+ tambahBuku(Buku)", "+ tampilkanDaftarBuku()"])

    # 2. Buku (Top Center-Left)
    card(155, 165, 125, 85, "Buku", "",
         ["- kode, judul : String", "- penulis : String", "- tahunTerbit : int"],
         ["+ getKode() : String", "+ getJudul() : String"])

    # 3. Superclass: Anggota (Top Right)
    card(310, 145, 145, 105, "Anggota", "Superclass",
         ["- id, nama : String", "- alamat : String", "- kartu : KartuAnggota"],
         ["+ getId(), getNama()", "+ getAlamat()", "+ getKartuAnggota()", "+ tampilkanData()"])

    # 4. KartuAnggota (Far Right)
    card(475, 165, 65, 80, "KartuAnggota", "",
         ["- nomor : String", "- status : String"],
         ["+ getNomor()", "+ tampilkan()"])

    # --- BOTTOM ROW: Peminjaman & Subclasses ---
    # 5. Peminjaman (Bottom Left)
    card(40, 15, 150, 95, "Peminjaman", "",
         ["- kodePinjam : String", "- tanggal : String", "- buku : Buku", "- anggota : Anggota", "- durasiHari : int"],
         ["+ getKodePinjam()", "+ getBuku() : Buku", "+ getAnggota() : Anggota", "+ tampilkanData()"])

    # 6. Subclass: Mahasiswa (Bottom Middle-Right)
    card(255, 15, 130, 85, "Mahasiswa", "Subclass",
         ["- nrp : String", "- prodi : String"],
         ["+ getNrp() : String", "+ getProdi() : String", "+ tampilkanData()"])

    # 7. Subclass: Dosen (Bottom Right)
    card(400, 15, 135, 85, "Dosen", "Subclass",
         ["- nip : String", "- departemen : String"],
         ["+ getNip() : String", "+ getDepartemen() : String", "+ tampilkanData()"])

    # --- CONNECTORS ---
    # 1. Aggregation: Perpustakaan ◇---- Buku
    d.add(Line(130, 207, 155, 207, strokeColor=C_LINE, strokeWidth=1))
    d.add(Polygon([130, 207, 134, 210, 138, 207, 134, 204], fillColor=colors.white, strokeColor=C_LINE, strokeWidth=1))
    d.add(String(140, 210, "1", fontName='Helvetica-Bold', fontSize=6, fillColor=C_TEXT))
    d.add(String(149, 210, "*", fontName='Helvetica-Bold', fontSize=6, fillColor=C_TEXT))
    d.add(String(133, 196, "Aggregation", fontName='Helvetica-Oblique', fontSize=4.8, fillColor=COLOR_MUTED))

    # 2. Composition: Anggota ◆---- KartuAnggota
    d.add(Line(455, 207, 475, 207, strokeColor=C_LINE, strokeWidth=1))
    d.add(Polygon([455, 207, 459, 210, 463, 207, 459, 204], fillColor=C_LINE, strokeColor=C_LINE, strokeWidth=1))
    d.add(String(465, 210, "1", fontName='Helvetica-Bold', fontSize=6, fillColor=C_TEXT))
    d.add(String(453, 196, "Composition", fontName='Helvetica-Oblique', fontSize=4.8, fillColor=COLOR_MUTED))

    # 3. Association: Peminjaman ---- Buku
    d.add(Line(115, 110, 115, 135, strokeColor=C_LINE, strokeWidth=0.8))
    d.add(Line(115, 135, 215, 135, strokeColor=C_LINE, strokeWidth=0.8))
    d.add(Line(215, 135, 215, 165, strokeColor=C_LINE, strokeWidth=0.8))
    d.add(String(120, 115, "*", fontName='Helvetica-Bold', fontSize=6, fillColor=C_TEXT))
    d.add(String(220, 155, "1", fontName='Helvetica-Bold', fontSize=6, fillColor=C_TEXT))
    d.add(String(145, 138, "Association", fontName='Helvetica-Oblique', fontSize=4.8, fillColor=COLOR_MUTED))

    # 4. Association: Peminjaman ---- Anggota
    d.add(Line(190, 60, 230, 60, strokeColor=C_LINE, strokeWidth=0.8))
    d.add(Line(230, 60, 230, 115, strokeColor=C_LINE, strokeWidth=0.8))
    d.add(Line(230, 115, 335, 115, strokeColor=C_LINE, strokeWidth=0.8))
    d.add(Line(335, 115, 335, 145, strokeColor=C_LINE, strokeWidth=0.8))
    d.add(String(195, 64, "*", fontName='Helvetica-Bold', fontSize=6, fillColor=C_TEXT))
    d.add(String(340, 135, "1", fontName='Helvetica-Bold', fontSize=6, fillColor=C_TEXT))
    d.add(String(240, 118, "Association (Polymorphic)", fontName='Helvetica-Oblique', fontSize=4.8, fillColor=COLOR_MUTED))

    # 5. Inheritance: Mahasiswa & Dosen extends Anggota (Generalization Arrow ▲)
    # Trunk from Anggota bottom:
    d.add(Line(382, 145, 382, 125, strokeColor=C_LINE, strokeWidth=1))
    # Closed empty triangle at superclass
    d.add(Polygon([382, 145, 377, 137, 387, 137], fillColor=colors.white, strokeColor=C_LINE, strokeWidth=1))
    
    # Fork to Mahasiswa & Dosen:
    d.add(Line(320, 125, 467, 125, strokeColor=C_LINE, strokeWidth=1))
    d.add(Line(320, 125, 320, 100, strokeColor=C_LINE, strokeWidth=1))
    d.add(Line(467, 125, 467, 100, strokeColor=C_LINE, strokeWidth=1))
    d.add(String(340, 128, "Generalization (extends)", fontName='Helvetica-BoldOblique', fontSize=5.2, fillColor=C_TEXT))

    return d

story.append(draw_p5_uml())
story.append(Spacer(1, 3))

story.append(Paragraph("6. Penjelasan Hubungan is-a & Manfaat Refactoring", h1_style))

penjelasan_is_a = [
    [
        Paragraph("<b>Hubungan is-a</b><br/>(Prinsip Inheritance)", body_style),
        Paragraph("• <b>Mahasiswa is-a Anggota:</b> Objek Mahasiswa mewarisi seluruh karakteristik dasar anggota perpustakaan (id, nama, alamat, kartu) serta memiliki atribut spesifik akademik (NRP, Program Studi).<br/>• <b>Dosen is-a Anggota:</b> Objek Dosen adalah anggota resmi perpustakaan dengan data kepegawaian (NIP, Departemen). Keduanya dapat diperlakukan sebagai tipe <code>Anggota</code> dalam transaksi peminjaman.", td_style)
    ],
    [
        Paragraph("<b>Perbedaan is-a vs has-a</b>", body_style),
        Paragraph("• <b>is-a (Inheritance):</b> Mahasiswa adalah Anggota (<code>Mahasiswa extends Anggota</code>).<br/>• <b>has-a (Composition/Aggregation):</b> Anggota memiliki Kartu (<code>Anggota *-- KartuAnggota</code>) dan Perpustakaan memiliki Buku (<code>Perpustakaan o-- Buku</code>).", td_style)
    ],
    [
        Paragraph("<b>Manfaat Refactoring</b>", body_style),
        Paragraph("1. <b>Mengurangi Duplikasi:</b> Logika dasar identitas dan kartu anggota dikelola terpusat di superclass.<br/>2. <b>Struktur Lebih Jelas:</b> Hirarki kelas menggambarkan domain akademik secara tepat.<br/>3. <b>Ekstensibilitas & Maintenance:</b> Penambahan jenis anggota baru (misal: Tendik/Umum) cukup membuat subclass baru.", td_style)
    ]
]
t_penjelasan = Table(penjelasan_is_a, colWidths=[125, 415])
t_penjelasan.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), colors.white),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ('PADDING', (0,0), (-1,-1), 2.8),
]))
story.append(t_penjelasan)

story.append(PageBreak())

# =========================================================================
# HALAMAN 3: SCREENSHOT HASIL RUNNING, BUKTI EXTENDS/SUPER, SPRINT REVIEW & RETRO
# =========================================================================
story.append(Paragraph("7. Hasil Eksekusi Program (Console Output Main.java)", h1_style))

console_log = """=================================================
   PRAKTIKUM MODUL 5 - INHERITANCE & SUBCLASS    
   SISTEM MANAJEMEN PERPUSTAKAAN                 
=================================================

=== 1. UJI INSTANSIASI SUBCLASS & SUPER CONSTRUCTOR ===
[Sukses] Objek Mahasiswa dan Dosen berhasil diinstansiasi via super().

=== 2. UJI AKSES METHOD SUPERCLASS (INHERITANCE) ===
--- Akses Data Mahasiswa via Method Superclass ---
Nama (Superclass)   : Arjuna Lanang Adiwarsana | Alamat: Jl. Trunojoyo No. 45, Sumenep
No. Kartu (Super)   : KTA-A001 | NRP: 3125522010 | Prodi: D3 PJJ Teknik Informatika

--- Akses Data Dosen via Method Superclass ---
Nama (Superclass)   : Nirwana Haidar Hari, S.Pd., M.Kom. | Alamat: Jl. Raya Lenteng No. 88
No. Kartu (Super)   : KTA-A002 | NIP: 198801232024011001 | Departemen: Teknik Informatika

=== 3. DETAIL LENGKAP OBJEK SUBCLASS ===
--- Data Mahasiswa ---: ID: A001 | Nama: Arjuna Lanang Adiwarsana | No. KTA: KTA-A001 (Aktif)
                       NRP: 3125522010 | Prodi: D3 PJJ Teknik Informatika | Tipe: Mahasiswa
--- Data Dosen ---    : ID: A002 | Nama: Nirwana Haidar Hari, S.Pd., M.Kom. | No. KTA: KTA-A002
                       NIP: 198801232024011001 | Departemen: Teknik Informatika | Tipe: Dosen

=== 4. INTEGRASI RELASI P4 DENGAN SUBCLASS ANGGOTA ===
[Sukses] Buku "Pemrograman Berorientasi Objek" & "Struktur Data & Algoritma" ditambahkan ke Perpustakaan.
--- Transaksi 1 (Mahasiswa is-a Anggota): PJ-001 | Durasi: 7 Hari | Peminjam: Arjuna Lanang (KTA-A001) | Buku: Pemrograman Berorientasi Objek
--- Transaksi 2 (Dosen is-a Anggota)    : PJ-002 | Durasi: 14 Hari | Peminjam: Nirwana Haidar (KTA-A002) | Buku: Struktur Data & Algoritma
=================================================
   PENGUJIAN MODUL 5 SELESAI (SUKSES)           
================================================="""

t_console = Table([[Paragraph(console_log.replace('\n', '<br/>'), terminal_style)]], colWidths=[540])
t_console.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), COLOR_ROW_ALT),
    ('BOX', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('PADDING', (0,0), (-1,-1), 2.5),
]))
story.append(t_console)
story.append(Spacer(1, 2))

story.append(Paragraph("8. Bukti Penggunaan extends dan super", h1_style))
code_proof = [
    [
        Paragraph("<b>Bukti extends (Inheritance):</b><br/><code>public class Mahasiswa extends Anggota {<br/>&nbsp;&nbsp;private String nrp, prodi;<br/>&nbsp;&nbsp;// mewarisi atribut & method Anggota<br/>}</code>", td_style),
        Paragraph("<b>Bukti super (Constructor Chaining):</b><br/><code>public Mahasiswa(String id, String nm, String almt, String nrp, String prd) {<br/>&nbsp;&nbsp;super(id, nm, almt); // panggil constructor superclass<br/>&nbsp;&nbsp;this.nrp = nrp; this.prodi = prd;<br/>}</code>", td_style)
    ]
]
t_proof = Table(code_proof, colWidths=[270, 270])
t_proof.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), colors.white),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('PADDING', (0,0), (-1,-1), 2.5),
]))
story.append(t_proof)
story.append(Spacer(1, 2))

story.append(Paragraph("9. Sprint Review", h1_style))
review_data = [
    [Paragraph("Item Evaluasi", th_style), Paragraph("Hasil Evaluasi", th_style)],
    [Paragraph("Kandidat inheritance ditemukan", td_style), Paragraph("Teridentifikasi kesamaan data personal pada Mahasiswa & Dosen", td_style)],
    [Paragraph("Superclass berhasil dibuat", td_style), Paragraph("Class Anggota berhasil menampung atribut umum & komposisi Kartu", td_style)],
    [Paragraph("Minimal 2 subclass dibuat", td_style), Paragraph("Subclass Mahasiswa dan Dosen berhasil diimplementasikan", td_style)],
    [Paragraph("extends diterapkan", td_style), Paragraph("Mahasiswa extends Anggota dan Dosen extends Anggota", td_style)],
    [Paragraph("super diterapkan", td_style), Paragraph("Constructor superclass dipanggil dengan super(id, nama, alamat)", td_style)],
    [Paragraph("Class diagram diperbarui", td_style), Paragraph("Diperbarui dengan notasi Generalization UML & relasi P4 tetap utuh", td_style)],
    [Paragraph("Program berhasil dijalankan", td_style), Paragraph("Berhasil dikompilasi & dijalankan tanpa error / warning", td_style)],
    [Paragraph("Kendala", td_style), Paragraph("Tidak ada kendala, alur logika inheritance dan relasi berjalan baik", td_style)]
]
t_review = Table(review_data, colWidths=[150, 390])
t_review.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('PADDING', (0,0), (-1,-1), 1.6),
]))
story.append(t_review)
story.append(Spacer(1, 2))

story.append(Paragraph("10. Sprint Retrospective", h1_style))
retro_text = """
<b>What Went Well?</b> Refactoring inheritance berhasil mengeliminasi duplikasi kode. Superclass <code>Anggota</code> memusatkan pengelolaan identitas dasar dan kartu anggota, sedangkan <code>Mahasiswa</code> dan <code>Dosen</code> fokus pada atribut spesifik masing-masing. Relasi asosiasi dengan <code>Peminjaman</code> dan agregasi dengan <code>Perpustakaan</code> dari P4 tetap bekerja secara polimorfis tanpa hambatan.<br/>
<b>What Went Wrong?</b> Diperlukan ketelitian dalam penempatan pemanggilan <code>super(...)</code> sebagai pernyataan pertama pada constructor subclass agar chaining berjalan sesuai aturan Java compiler.<br/>
<b>Improvement:</b> Menyiapkan subclass untuk penerapan method overriding dinamis dan polimorfisme tingkat lanjut pada modul berikutnya (P6).
"""
story.append(Paragraph(retro_text, body_style))

# Render Dokumen
doc.build(story, canvasmaker=NumberedCanvas)
print(f"README.pdf successfully generated at: {pdf_filename}")
