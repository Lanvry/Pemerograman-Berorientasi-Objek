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
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
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
        self.drawString(36, 762, "Praktikum Pemrograman Berorientasi Obyek - Modul 6: Polymorphism & Dynamic Binding")
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
    fontSize=11.5,
    leading=14.5,
    textColor=COLOR_TEXT,
    alignment=1,
    spaceAfter=2
)

subtitle_style = ParagraphStyle(
    'DocSubtitle',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=8,
    leading=10.5,
    textColor=COLOR_MUTED,
    alignment=1,
    spaceAfter=2
)

h1_style = ParagraphStyle(
    'SectionH1',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=8.5,
    leading=11,
    textColor=COLOR_TEXT,
    spaceBefore=3,
    spaceAfter=2
)

body_style = ParagraphStyle(
    'BodyDark',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=6.8,
    leading=8.8,
    textColor=COLOR_TEXT,
    spaceAfter=2
)

terminal_style = ParagraphStyle(
    'TerminalBlock',
    parent=styles['Normal'],
    fontName='Courier',
    fontSize=5.4,
    leading=6.8,
    textColor=COLOR_TEXT
)

th_style = ParagraphStyle(
    'TH',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=6.5,
    leading=8.2,
    textColor=COLOR_TEXT,
    alignment=0
)

td_style = ParagraphStyle(
    'TD',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=6.3,
    leading=7.8,
    textColor=COLOR_TEXT
)

story = []

# =========================================================================
# HALAMAN 1: IDENTITAS, SPRINT GOAL, SPRINT BACKLOG, HIERARCHY & AUDIT BEHAVIOR
# =========================================================================
story.append(Paragraph("LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK", subtitle_style))
story.append(Paragraph("MODUL 6: POLYMORPHISM, METHOD OVERRIDING, OVERLOADING & DYNAMIC BINDING", title_style))
story.append(HRFlowable(width="100%", thickness=0.8, color=COLOR_TEXT, spaceAfter=3))

story.append(Paragraph("1. Profil Proyek", h1_style))
project_info = [
    [Paragraph("<b>Nama Proyek:</b>", body_style), Paragraph("Sistem Manajemen Perpustakaan (Polymorphic Behavior & Dynamic Dispatch)", body_style)],
    [Paragraph("<b>Nama Mahasiswa / NRP:</b>", body_style), Paragraph("Arjuna Lanang Adiwarsana / 3125522010", body_style)],
    [Paragraph("<b>Program Studi / Kampus:</b>", body_style), Paragraph("D3 PJJ Teknik Informatika - PENS PSDKU Sumenep", body_style)],
    [Paragraph("<b>Fokus Pertemuan (P6):</b>", body_style), Paragraph("Runtime Polymorphism (Overriding), Compile-time (Overloading), Upcasting, Polymorphic Collection & Dynamic Binding", body_style)]
]
t_proj = Table(project_info, colWidths=[120, 420])
t_proj.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), colors.white),
    ('PADDING', (0,0), (-1,-1), 1.8),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('BOX', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#E5E7EB")),
]))
story.append(t_proj)
story.append(Spacer(1, 2))

story.append(Paragraph("2. Sprint Goal (P6)", h1_style))
story.append(Paragraph("Mengembangkan behavior objek pada proyek Sistem Manajemen Perpustakaan hasil P5 melalui method overriding, overloading, upcasting, dan polymorphic collection sehingga subclass (<b>Mahasiswa</b>, <b>Dosen</b>, <b>Tendik</b>) dapat merespons pemanggilan method yang sama (<code>tampilkanPeran()</code>, <code>hitungDenda()</code>, <code>getMaksimalPinjam()</code>) dengan perilaku spesifik secara dinamis saat runtime (Dynamic Binding).", body_style))
story.append(Spacer(1, 2))

story.append(Paragraph("3. Sprint Backlog (P6)", h1_style))
backlog_data = [
    [Paragraph("ID", th_style), Paragraph("Sprint Backlog Item", th_style), Paragraph("Status", th_style)],
    [Paragraph("SB-01", td_style), Paragraph("Audit superclass (Anggota) dan subclass P5 (Mahasiswa, Dosen)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-02", td_style), Paragraph("Menentukan behavior polymorphic yang dioverride (tampilkanPeran, hitungDenda, getMaksimalPinjam)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-03", td_style), Paragraph("Implementasi overriding pada subclass dengan anotasi @Override (termasuk subclass Tendik)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-04", td_style), Paragraph("Implementasi method overloading pada Anggota (hitungDenda) dan Perpustakaan (cariBuku)", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-05", td_style), Paragraph("Implementasi upcasting reference superclass ke instance subclass", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-06", td_style), Paragraph("Membuat polymorphic collection (Anggota[]) dan iterasi polimorfik", td_style), Paragraph("Done", td_style)],
    [Paragraph("SB-07", td_style), Paragraph("Melakukan pengujian pembuktian dynamic method dispatch / dynamic binding", td_style), Paragraph("Done", td_style)]
]
t_backlog = Table(backlog_data, colWidths=[38, 442, 60])
t_backlog.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('PADDING', (0,0), (-1,-1), 1.8),
]))
story.append(t_backlog)
story.append(Spacer(1, 2))

story.append(Paragraph("4. Bagian A: Hierarchy Class P5 & Behavior yang Dipilih untuk Polymorphism", h1_style))
story.append(Paragraph("Struktur hierarki P5 dikembangkan dengan menambahkan subclass <code>Tendik</code> untuk memperkaya variasi polimorfisme:", body_style))

audit_data = [
    [Paragraph("Superclass", th_style), Paragraph("Subclass", th_style), Paragraph("Method yang Di-override", th_style), Paragraph("Perilaku Spesifik (Polymorphic Behavior)", th_style)],
    [
        Paragraph("<b>Anggota</b>", td_style),
        Paragraph("<b>Mahasiswa</b>", td_style),
        Paragraph("<code>tampilkanPeran()</code><br/><code>hitungDenda(int)</code><br/><code>getMaksimalPinjam()</code>", td_style),
        Paragraph("• Peran: Akses koleksi skripsi & akademik<br/>• Denda terdiskon mahasiswa: Rp 500 / hari<br/>• Maksimal peminjaman: 3 buku", td_style)
    ],
    [
        Paragraph("<b>Anggota</b>", td_style),
        Paragraph("<b>Dosen</b>", td_style),
        Paragraph("<code>tampilkanPeran()</code><br/><code>hitungDenda(int)</code><br/><code>getMaksimalPinjam()</code>", td_style),
        Paragraph("• Peran: Akses jurnal & referensi riset khusus<br/>• Hak istimewa akademik: Bebas denda (Rp 0)<br/>• Maksimal peminjaman: 5 buku", td_style)
    ],
    [
        Paragraph("<b>Anggota</b>", td_style),
        Paragraph("<b>Tendik</b>", td_style),
        Paragraph("<code>tampilkanPeran()</code><br/><code>hitungDenda(int)</code><br/><code>getMaksimalPinjam()</code>", td_style),
        Paragraph("• Peran: Akses operasional kampus & administrasi<br/>• Denda tarif staf: Rp 750 / hari<br/>• Maksimal peminjaman: 4 buku", td_style)
    ]
]
t_audit = Table(audit_data, colWidths=[65, 75, 130, 270])
t_audit.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('PADDING', (0,0), (-1,-1), 2.5),
]))
story.append(t_audit)

story.append(PageBreak())

# =========================================================================
# HALAMAN 2: CLASS DIAGRAM TERBARU, OVERRIDING, OVERLOADING, PENJELASAN UPCASTING
# =========================================================================
story.append(Paragraph("5. Bagian B: Class Diagram Terbaru (P6 - Polymorphism)", h1_style))
story.append(Paragraph("Diagram kelas menampilkan superclass, 3 subclass dengan method yang dioverride, overloading, dan relasi P4:", body_style))

def draw_p6_uml():
    d = Drawing(540, 240)
    
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
        d.add(String(x + w/2.0, y + h - 9.5, full_title, textAnchor='middle', fontName='Helvetica-Bold', fontSize=5.8, fillColor=C_TEXT))
        
        cy = y + h - th - 7
        for a in attrs:
            d.add(String(x + 3, cy, a, fontName='Courier', fontSize=4.8, fillColor=C_TEXT))
            cy -= 6.2
            
        div_y = cy + 2
        d.add(Line(x, div_y, x + w, div_y, strokeColor=C_BORDER, strokeWidth=0.4))
        
        cy = div_y - 6.5
        for m in mths:
            d.add(String(x + 3, cy, m, fontName='Courier', fontSize=4.8, fillColor=C_TEXT))
            cy -= 6.2

    # --- TOP ROW ---
    # 1. Perpustakaan (Top Left)
    card(0, 150, 125, 85, "Perpustakaan", "",
         ["- nama, alamat : String", "- daftarBuku : List"],
         ["+ tambahBuku(Buku)", "+ cariBuku(judul) : Buku", "+ cariBuku(kode, bool)", "+ tampilkanDaftar()"])

    # 2. Buku (Top Center-Left)
    card(140, 150, 115, 85, "Buku", "",
         ["- kode, judul : String", "- penulis : String", "- tahunTerbit : int"],
         ["+ getKode(), getJudul()", "+ getPenulis()", "+ tampilkanData()"])

    # 3. Superclass: Anggota (Top Right)
    card(275, 135, 185, 100, "Anggota", "Superclass",
         ["- id, nama, alamat : String", "- kartuAnggota : KartuAnggota"],
         ["+ tampilkanPeran() void", "+ hitungDenda(int hari) : int", "+ hitungDenda(int, double) : int", "+ getMaksimalPinjam() : int", "+ tampilkanData() void"])

    # 4. KartuAnggota (Far Right)
    card(475, 155, 65, 80, "KartuAnggota", "",
         ["- nomorKartu : String", "- status : String"],
         ["+ getNomorKartu()", "+ tampilkanKartu()"])

    # --- BOTTOM ROW ---
    # 5. Peminjaman (Bottom Left)
    card(10, 15, 150, 95, "Peminjaman", "",
         ["- kodePinjam, tgl : String", "- buku : Buku", "- anggota : Anggota", "- durasiHari : int"],
         ["+ hitungTotalDenda(hari) : int", "+ tampilkanData() void"])

    # 6. Subclass: Mahasiswa (Bottom Middle-Left)
    card(180, 15, 110, 85, "Mahasiswa", "Subclass",
         ["- nrp, prodi : String"],
         ["+ @Override tampilkanPeran()", "+ @Override hitungDenda(int)", "+ @Override getMaxPinjam()", "+ @Override tampilkanData()"])

    # 7. Subclass: Dosen (Bottom Middle-Right)
    card(305, 15, 110, 85, "Dosen", "Subclass",
         ["- nip, dept : String"],
         ["+ @Override tampilkanPeran()", "+ @Override hitungDenda(int)", "+ @Override getMaxPinjam()", "+ @Override tampilkanData()"])

    # 8. Subclass: Tendik (Bottom Right)
    card(430, 15, 110, 85, "Tendik", "Subclass",
         ["- nip, unitKerja : String"],
         ["+ @Override tampilkanPeran()", "+ @Override hitungDenda(int)", "+ @Override getMaxPinjam()", "+ @Override tampilkanData()"])

    # --- CONNECTORS ---
    # Perpustakaan ◇-- Buku
    d.add(Line(125, 192, 140, 192, strokeColor=C_LINE, strokeWidth=0.8))
    d.add(Polygon([125, 192, 128, 195, 131, 192, 128, 189], fillColor=colors.white, strokeColor=C_LINE, strokeWidth=0.8))
    
    # Anggota ◆-- KartuAnggota
    d.add(Line(460, 192, 475, 192, strokeColor=C_LINE, strokeWidth=0.8))
    d.add(Polygon([460, 192, 463, 195, 466, 192, 463, 189], fillColor=C_LINE, strokeColor=C_LINE, strokeWidth=0.8))

    # Peminjaman -- Buku
    d.add(Line(85, 110, 85, 130, strokeColor=C_LINE, strokeWidth=0.6))
    d.add(Line(85, 130, 195, 130, strokeColor=C_LINE, strokeWidth=0.6))
    d.add(Line(195, 130, 195, 150, strokeColor=C_LINE, strokeWidth=0.6))

    # Peminjaman -- Anggota (Polymorphic Association)
    d.add(Line(160, 60, 172, 60, strokeColor=C_LINE, strokeWidth=0.6))
    d.add(Line(172, 60, 172, 115, strokeColor=C_LINE, strokeWidth=0.6))
    d.add(Line(172, 115, 367, 115, strokeColor=C_LINE, strokeWidth=0.6))
    d.add(Line(367, 115, 367, 135, strokeColor=C_LINE, strokeWidth=0.6))

    # Inheritance Trunk & Triangle
    d.add(Line(367, 135, 367, 118, strokeColor=C_LINE, strokeWidth=0.9))
    d.add(Polygon([367, 135, 363, 128, 371, 128], fillColor=colors.white, strokeColor=C_LINE, strokeWidth=0.9))
    
    # Fork to 3 Subclasses
    d.add(Line(235, 118, 485, 118, strokeColor=C_LINE, strokeWidth=0.9))
    d.add(Line(235, 118, 235, 100, strokeColor=C_LINE, strokeWidth=0.9))
    d.add(Line(360, 118, 360, 100, strokeColor=C_LINE, strokeWidth=0.9))
    d.add(Line(485, 118, 485, 100, strokeColor=C_LINE, strokeWidth=0.9))
    d.add(String(240, 121, "Generalization (extends & @Override)", fontName='Helvetica-BoldOblique', fontSize=4.8, fillColor=C_TEXT))

    return d

story.append(draw_p6_uml())
story.append(Spacer(1, 1))

story.append(Paragraph("6. Contoh Overriding, Overloading, dan Penjelasan Upcasting", h1_style))

p_oop_concepts = [
    [
        Paragraph("<b>A. Contoh Method Overriding (@Override)</b>", body_style),
        Paragraph("<b>B. Contoh Method Overloading & Alasan</b>", body_style)
    ],
    [
        Paragraph("<code>// Di Subclass Mahasiswa:<br/>@Override<br/>public int hitungDenda(int hari) {<br/>&nbsp;&nbsp;return hari * 500; // Tarif Mahasiswa<br/>}<br/>// Di Subclass Dosen:<br/>@Override<br/>public int hitungDenda(int hari) {<br/>&nbsp;&nbsp;return 0; // Bebas Denda<br/>}</code>", td_style),
        Paragraph("<code>// Di Class Anggota (Nama sama, parameter beda):<br/>public int hitungDenda(int hari) { ... }<br/>public int hitungDenda(int hari, double diskon) { ... }<br/>// Di Class Perpustakaan:<br/>public Buku cariBuku(String judul) { ... }<br/>public Buku cariBuku(String kode, boolean cetak) { ... }</code><br/><i>Alasan:</i> Memudahkan pemanggil memilih variasi pencarian/kalkulasi tanpa menambah nama method baru.", td_style)
    ],
    [
        Paragraph("<b>C. Penjelasan Upcasting & Polymorphic Reference</b>", body_style),
        Paragraph("<b>D. Manfaat Arsitektural Polymorphism</b>", body_style)
    ],
    [
        Paragraph("<code>Anggota a1 = new Mahasiswa(...);<br/>Anggota a2 = new Dosen(...);<br/>Anggota a3 = new Tendik(...);</code><br/>Upcasting menyimpan referensi objek subclass ke variabel bertipe superclass. Kode pemanggil cukup berinteraksi dengan tipe umum <code>Anggota</code>.", td_style),
        Paragraph("1. <b>Kopling Rendah:</b> <code>Peminjaman</code> dan <code>Perpustakaan</code> tidak bergantung pada subclass konkret.<br/>2. <b>Open/Closed Principle:</b> Menambah subclass baru (misal: AnggotaLuar) tidak mengubah logika peminjaman.<br/>3. <b>Polymorphic Collection:</b> Objek beragam dapat diproses dalam satu array loop.", td_style)
    ]
]
t_oop = Table(p_oop_concepts, colWidths=[270, 270])
t_oop.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), colors.white),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ('PADDING', (0,0), (-1,-1), 2.2),
]))
story.append(t_oop)

story.append(PageBreak())

# =========================================================================
# HALAMAN 3: SCREENSHOT HASIL RUNNING, TABEL PENGUJIAN DYNAMIC BINDING, REVIEW & RETRO
# =========================================================================
story.append(Paragraph("7. Bukti Eksekusi Program (Console Output Main.java)", h1_style))

console_log = """=== 1. UJI UPCASTING & POLYMORPHIC REFERENCE ===
[Sukses] Tiga objek subclass berhasil di-upcast ke tipe reference superclass 'Anggota'.
refMhs (Reference: Anggota) -> Objek: Mahasiswa | refDsn -> Dosen | refTdk -> Tendik

=== 2. UJI METHOD OVERRIDING & DYNAMIC BINDING ===
refMhs.tampilkanPeran() -> Peran : Mahasiswa (Akses Koleksi Skripsi & Akademik)
refDsn.tampilkanPeran() -> Peran : Dosen Pengajar & Peneliti (Akses Koleksi Jurnal)
refTdk.tampilkanPeran() -> Peran : Tenaga Kependidikan / Staf (Akses Operasional)
Denda (3 Hari Terlambat): Mahasiswa = Rp 1500 | Dosen = Rp 0 | Tendik = Rp 2250

=== 3. UJI METHOD OVERLOADING (COMPILE-TIME) ===
1. hitungDenda(4 hari) -> Rp 2000 | 2. hitungDenda(4 hari, diskon 50%) -> Rp 1000
1. cariBuku(judul) -> Ditemukan: Pemrograman Berorientasi Objek
2. cariBuku(kode, bool) -> [Info] Ditemukan buku: Struktur Data & Algoritma

=== 4. UJI POLYMORPHIC COLLECTION (ARRAY OF SUPERCLASS) ===
Iterasi pada 4 elemen array Anggota[] berhasil memanggil method spesifik tiap subclass secara dinamis.
=== 5. INTEGRASI TRANSAKSI PEMINJAMAN ===
PJ-001 (Mahasiswa) - Buku: PBO | Denda: Rp 1000  | PJ-002 (Dosen) - Buku: SDA | Denda: Rp 0"""

t_console = Table([[Paragraph(console_log.replace('\n', '<br/>'), terminal_style)]], colWidths=[540])
t_console.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,-1), COLOR_ROW_ALT),
    ('BOX', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('PADDING', (0,0), (-1,-1), 2.2),
]))
story.append(t_console)
story.append(Spacer(1, 1))

story.append(Paragraph("8. Tabel Pengujian Dynamic Binding & Penjelasan", h1_style))
dyn_data = [
    [Paragraph("Reference", th_style), Paragraph("Object Aktual", th_style), Paragraph("Method Dipanggil", th_style), Paragraph("Output yang Dihasilkan", th_style)],
    [Paragraph("Anggota", td_style), Paragraph("Mahasiswa", td_style), Paragraph("tampilkanPeran()", td_style), Paragraph("Peran : Mahasiswa (Akses Koleksi Skripsi & Akademik)", td_style)],
    [Paragraph("Anggota", td_style), Paragraph("Dosen", td_style), Paragraph("tampilkanPeran()", td_style), Paragraph("Peran : Dosen Pengajar & Peneliti (Akses Koleksi Jurnal)", td_style)],
    [Paragraph("Anggota", td_style), Paragraph("Tendik", td_style), Paragraph("tampilkanPeran()", td_style), Paragraph("Peran : Tenaga Kependidikan / Staf (Akses Operasional)", td_style)],
    [Paragraph("Anggota", td_style), Paragraph("Mahasiswa", td_style), Paragraph("hitungDenda(3)", td_style), Paragraph("Rp 1.500 (Tarif Mahasiswa: Rp 500 / hari)", td_style)],
    [Paragraph("Anggota", td_style), Paragraph("Dosen", td_style), Paragraph("hitungDenda(3)", td_style), Paragraph("Rp 0 (Hak Istimewa Bebas Denda)", td_style)],
    [Paragraph("Anggota", td_style), Paragraph("Tendik", td_style), Paragraph("hitungDenda(3)", td_style), Paragraph("Rp 2.250 (Tarif Tendik: Rp 750 / hari)", td_style)]
]
t_dyn = Table(dyn_data, colWidths=[60, 75, 110, 295])
t_dyn.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('PADDING', (0,0), (-1,-1), 1.5),
]))
story.append(t_dyn)
story.append(Spacer(1, 1))

story.append(Paragraph("<b>Mengapa method berbeda dieksekusi walau referensi sama?</b> Pada Java, pemanggilan method non-static ditentukan saat runtime melalui <b>Dynamic Method Dispatch (Dynamic Binding)</b>. JVM menelusuri tabel metode virtual (vtable) milik objek aktual di heap memory (bukan tipe referensinya), sehingga implementasi subclass yang bersangkutan yang dieksekusi.", body_style))
story.append(Spacer(1, 1))

story.append(Paragraph("9. Sprint Review", h1_style))
review_data = [
    [Paragraph("Item Evaluasi", th_style), Paragraph("Hasil Evaluasi", th_style)],
    [Paragraph("Superclass & Subclass tersedia", td_style), Paragraph("Superclass Anggota serta Subclass Mahasiswa, Dosen, Tendik berfungsi optimal", td_style)],
    [Paragraph("Method overriding berhasil", td_style), Paragraph("Overriding tampilkanPeran(), hitungDenda(), getMaksimalPinjam(), tampilkanData() berhasil", td_style)],
    [Paragraph("Minimal 2 subclass memiliki behavior berbeda", td_style), Paragraph("3 subclass (Mahasiswa, Dosen, Tendik) memiliki logika denda & peran yang berbeda", td_style)],
    [Paragraph("Method overloading tersedia", td_style), Paragraph("Overloading pada Anggota.hitungDenda() dan Perpustakaan.cariBuku() berhasil", td_style)],
    [Paragraph("Upcasting & Polymorphic reference", td_style), Paragraph("Reference Anggota sukses menampung instance Mahasiswa, Dosen, dan Tendik", td_style)],
    [Paragraph("Polymorphic collection berhasil", td_style), Paragraph("Array Anggota[] menampung 4 objek subclass dan diiterasi secara polimorfik", td_style)],
    [Paragraph("Dynamic binding dapat dibuktikan", td_style), Paragraph("Terbukti pemanggilan method via reference superclass mengeksekusi method subclass aktual", td_style)],
    [Paragraph("Program berjalan & Kendala", td_style), Paragraph("Program terkompilasi dan berjalan 100% sukses tanpa kendala teknis", td_style)]
]
t_review = Table(review_data, colWidths=[160, 380])
t_review.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), COLOR_HEADER_BG),
    ('GRID', (0,0), (-1,-1), 0.5, COLOR_LINE),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, COLOR_ROW_ALT]),
    ('PADDING', (0,0), (-1,-1), 1.2),
]))
story.append(t_review)
story.append(Spacer(1, 1))

story.append(Paragraph("10. Sprint Retrospective", h1_style))
retro_text = """
<b>What Went Well?</b> Polimorfisme dan dynamic binding membuat kode modular dan extensible. Relasi <code>Peminjaman</code> dapat menerima objek anggota apa pun tanpa perlu logika seleksi percabangan.<br/>
<b>What Went Wrong?</b> Perlu ketelitian membedakan <i>compile-time polymorphism</i> (overloading) dan <i>runtime polymorphism</i> (overriding).<br/>
<b>Improvement:</b> Pada modul P7 (Abstraction & Interface), superclass <code>Anggota</code> dapat didefinisikan sebagai <code>abstract class</code> dengan <code>abstract method</code> untuk memperjelas kontrak desain sistem.
"""
story.append(Paragraph(retro_text, body_style))

doc.build(story, canvasmaker=NumberedCanvas)
print(f"README.pdf successfully generated at: {pdf_filename}")
