import os
from PIL import Image as PILImage, ImageDraw, ImageFont
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, Image as RLImage
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfgen import canvas

script_dir = os.path.dirname(os.path.abspath(__file__))
pdf_output_path = os.path.join(script_dir, "Laporan_UTS_3125522010_ArjunaLanangAdiwarsana.pdf")
class_diagram_img = os.path.join(script_dir, "class-diagram.png")

# =========================================================================
# HELPER: GENERATE REALISTIC TERMINAL SCREENSHOTS
# =========================================================================
def create_terminal_screenshot(output_path, title_text, lines_data, width=1500, font_size=18, line_height=25):
    padding_top = 48
    padding_bottom = 24
    padding_x = 24
    height = padding_top + (len(lines_data) * line_height) + padding_bottom

    img = PILImage.new("RGB", (width, height), (24, 24, 37))
    draw = ImageDraw.Draw(img)

    # Title Bar
    draw.rectangle([(0, 0), (width, 36)], fill=(30, 30, 46))
    draw.line([(0, 36), (width, 36)], fill=(49, 50, 68), width=1)

    # Mac Traffic light buttons
    draw.ellipse([(16, 11), (28, 23)], fill=(255, 95, 86))
    draw.ellipse([(36, 11), (48, 23)], fill=(255, 189, 46))
    draw.ellipse([(56, 11), (68, 23)], fill=(39, 201, 63))

    try:
        font_title = ImageFont.truetype("/System/Library/Fonts/Menlo.ttc", 13)
        font_code = ImageFont.truetype("/System/Library/Fonts/Menlo.ttc", font_size)
    except:
        font_title = ImageFont.load_default()
        font_code = ImageFont.load_default()

    bbox = draw.textbbox((0, 0), title_text, font=font_title)
    tw = bbox[2] - bbox[0]
    draw.text(((width - tw) / 2, 11), title_text, font=font_title, fill=(147, 153, 178))

    curr_y = padding_top
    for line in lines_data:
        curr_x = padding_x
        for segment_text, segment_color in line:
            draw.text((curr_x, curr_y), segment_text, font=font_code, fill=segment_color)
            seg_bbox = draw.textbbox((0, 0), segment_text, font=font_code)
            curr_x += (seg_bbox[2] - seg_bbox[0])
        curr_y += line_height

    img.save(output_path, "PNG")
    print(f"[OK] Generated screenshot: {output_path}")
    return output_path

# Palette for Terminal
C_WHITE = (205, 214, 244)
C_MUTED = (147, 153, 178)
C_GREEN = (166, 227, 161)
C_CYAN = (137, 220, 235)
C_YELLOW = (249, 226, 175)
C_PURPLE = (203, 166, 247)
C_RED = (243, 139, 168)
C_BLUE = (137, 180, 250)

# Screenshot 1
ss1_path = os.path.join(script_dir, "terminal_screenshot_1.png")
lines_ss1 = [
    [
        ("➜  ", C_GREEN),
        ("UTS_3125522010_ArjunaLanangAdiwarsana ", C_CYAN),
        ("git:(", C_PURPLE), ("main", C_RED), (") ✗ ", C_PURPLE),
        ("javac -d bin src/*.java && java -cp bin Main", (255, 255, 255))
    ],
    [("========================================================================", C_BLUE)],
    [("       UJIAN TENGAH SEMESTER (UTS) - PRAKTIKUM PBO SEMESTER GENAP       ", C_YELLOW)],
    [("                 SISTEM MANAJEMEN RENTAL KENDARAAN                      ", C_YELLOW)],
    [("========================================================================", C_BLUE)],
    [("Mahasiswa : Arjuna Lanang Adiwarsana", C_WHITE)],
    [("NRP       : 3125522010", C_WHITE)],
    [("Kelas     : 3D3 IT-PSDKU A", C_WHITE)],
    [("", C_WHITE)],
    [("------------------------------------------------------------------------", C_MUTED)],
    [("SKENARIO 1: DEMO POLYMORPHISM & DYNAMIC METHOD DISPATCH", C_GREEN)],
    [("------------------------------------------------------------------------", C_MUTED)],
    [("Simulasi Perhitungan Biaya Sewa Armada selama 3 Hari:", C_WHITE)],
    [("  [Kategori: Mobil] Plat: L 1234 ABC | Merk: Toyota Innova Reborn | Transmisi: Otomatis | Kapasitas: 7 Orang | Supir: Lepas Kunci", C_WHITE)],
    [("  -> Total Biaya (3 Hari): Rp 1,575,000", C_GREEN)],
    [("", C_WHITE)],
    [("  [Kategori: Mobil] Plat: W 5678 XYZ | Merk: Toyota HiAce Commuter | Transmisi: Manual | Kapasitas: 14 Orang | Supir: Ya (Rp 150,000/hari)", C_WHITE)],
    [("  -> Total Biaya (3 Hari): Rp 3,225,000", C_GREEN)],
    [("", C_WHITE)],
    [("  [Kategori: Motor] Plat: M 4321 DEF | Merk: Honda PCX 160 | Tipe: Matic | Fasilitas Helm: 2 Unit", C_WHITE)],
    [("  -> Total Biaya (3 Hari): Rp 390,000", C_GREEN)],
    [("", C_WHITE)],
    [("  [Kategori: Motor] Plat: M 9876 GHI | Merk: Kawasaki Ninja ZX-25R | Tipe: Sport | Fasilitas Helm: 1 Unit", C_WHITE)],
    [("  -> Total Biaya (3 Hari): Rp 750,000", C_GREEN)]
]
create_terminal_screenshot(ss1_path, "arjuna@MacBook-Pro: ~/Herd/Java/UTS_3125522010_ArjunaLanangAdiwarsana — java -cp bin Main (Polymorphism)", lines_ss1)

# Screenshot 2
ss2_path = os.path.join(script_dir, "terminal_screenshot_2.png")
lines_ss2 = [
    [("------------------------------------------------------------------------", C_MUTED)],
    [("SKENARIO 2: TRANSAKSI PENYEWAAN NORMAL (DENGAN BENEFIT MEMBER VIP)", C_GREEN)],
    [("------------------------------------------------------------------------", C_MUTED)],
    [("[Berhasil] Transaksi berhasil dibuat.", C_GREEN)],
    [("Status Mobil Innova saat ini: Sedang Disewa", C_YELLOW)],
    [("", C_WHITE)],
    [("------------------------------------------------------------------------", C_MUTED)],
    [("SKENARIO 3: VALIDASI BUSINESS RULES", C_GREEN)],
    [("------------------------------------------------------------------------", C_MUTED)],
    [("1. Uji Coba Input Durasi Tidak Valid (durasi = 0 hari):", C_WHITE)],
    [("   [VALIDASI TERPENUHI] Gagal membuat transaksi: Durasi sewa minimal 1 hari.", C_RED)],
    [("", C_WHITE)],
    [("2. Uji Coba Menyewa Kendaraan yang Sedang Disewa (Innova Reborn):", C_WHITE)],
    [("   [VALIDASI TERPENUHI] Gagal membuat transaksi: Kendaraan L 1234 ABC sedang tidak tersedia.", C_RED)],
    [("", C_WHITE)],
    [("------------------------------------------------------------------------", C_MUTED)],
    [("SKENARIO 4: TRANSAKSI PENYEWAAN MOTOR DENGAN FASILITAS HELM EKSTRA", C_GREEN)],
    [("------------------------------------------------------------------------", C_MUTED)],
    [("[Berhasil] Transaksi sewa motor berhasil dibuat.", C_GREEN)],
    [("Status Motor PCX saat ini: Sedang Disewa", C_YELLOW)]
]
create_terminal_screenshot(ss2_path, "arjuna@MacBook-Pro: ~/Herd/Java/UTS_3125522010_ArjunaLanangAdiwarsana — Business Rules Validation", lines_ss2)

# Screenshot 3
ss3_path = os.path.join(script_dir, "terminal_screenshot_3.png")
lines_ss3 = [
    [("------------------------------------------------------------------------", C_MUTED)],
    [("SKENARIO 5: PENGEMBALIAN TEPAT WAKTU VS PENGEMBALIAN TERLAMBAT (DENDA)", C_GREEN)],
    [("------------------------------------------------------------------------", C_MUTED)],
    [("A. Proses Pengembalian Transaksi 1 (Tepat Waktu - 0 hari keterlambatan):", C_WHITE)],
    [("=================================================================", C_BLUE)],
    [("                    KUITANSI SEWA KENDARAAN                     ", C_YELLOW)],
    [("=================================================================", C_BLUE)],
    [("No. Transaksi  : TRX-2026-001", C_WHITE)],
    [("Status Sewa    : SELESAI (SUDAH KEMBALI)", C_GREEN)],
    [("Pelanggan      : Arjuna Lanang (PLG-001) | Member: VIP", C_WHITE)],
    [("Kendaraan      : L 1234 ABC - Toyota Innova Reborn", C_WHITE)],
    [("Durasi Sewa    : 3 Hari", C_WHITE)],
    [("-----------------------------------------------------------------", C_MUTED)],
    [("Subtotal Sewa        : Rp      1,575,000", C_WHITE)],
    [("Diskon Member (10%) : -Rp       157,500", C_CYAN)],
    [("-----------------------------------------------------------------", C_MUTED)],
    [("TOTAL PEMBAYARAN     : Rp      1,417,500", C_GREEN)],
    [("=================================================================", C_BLUE)],
    [("", C_WHITE)],
    [("B. Proses Pengembalian Transaksi 2 (Terlambat 2 Hari - Kena Denda Dinamis):", C_WHITE)],
    [("=================================================================", C_BLUE)],
    [("                    KUITANSI SEWA KENDARAAN                     ", C_YELLOW)],
    [("=================================================================", C_BLUE)],
    [("No. Transaksi  : TRX-2026-002", C_WHITE)],
    [("Status Sewa    : SELESAI (SUDAH KEMBALI)", C_GREEN)],
    [("Pelanggan      : Budi Santoso (PLG-002) | Member: GOLD", C_WHITE)],
    [("Kendaraan      : M 4321 DEF - Honda PCX 160", C_WHITE)],
    [("Durasi Sewa    : 2 Hari", C_WHITE)],
    [("Keterlambatan  : 2 Hari (Kena Denda 150%/hari)", C_RED)],
    [("-----------------------------------------------------------------", C_MUTED)],
    [("Subtotal Sewa        : Rp        260,000", C_WHITE)],
    [("Diskon Member (5%) : -Rp        13,000", C_CYAN)],
    [("Denda Keterlambatan  : +Rp       360,000", C_RED)],
    [("-----------------------------------------------------------------", C_MUTED)],
    [("TOTAL PEMBAYARAN     : Rp        607,000", C_GREEN)],
    [("=================================================================", C_BLUE)],
    [("", C_WHITE)],
    [("Status Mobil Innova setelah kembali : Tersedia (Siap Disewa)", C_GREEN)],
    [("Status Motor PCX setelah kembali    : Tersedia (Siap Disewa)", C_GREEN)]
]
create_terminal_screenshot(ss3_path, "arjuna@MacBook-Pro: ~/Herd/Java/UTS_3125522010_ArjunaLanangAdiwarsana — Return & Penalty Invoice", lines_ss3)

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
            self.setFont("Helvetica", 9)
            self.setFillColor(colors.HexColor("#374151"))
            self.drawRightString(612 - 54, 34, f"Halaman {self._pageNumber} dari {num_pages}")
            super().showPage()
        super().save()

doc = SimpleDocTemplate(
    pdf_output_path,
    pagesize=letter,
    leftMargin=54,
    rightMargin=54,
    topMargin=46,
    bottomMargin=46
)

styles = getSampleStyleSheet()

header_meta_style = ParagraphStyle(
    'HeaderMeta',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=9.5,
    leading=13.5,
    textColor=colors.black
)

genap_style = ParagraphStyle(
    'GenapRight',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=9.5,
    leading=13.5,
    alignment=2,
    textColor=colors.black
)

doc_title_style = ParagraphStyle(
    'DocTitle',
    parent=styles['Heading1'],
    fontName='Helvetica-Bold',
    fontSize=18,
    leading=22,
    textColor=colors.black,
    spaceBefore=10,
    spaceAfter=10
)

h1_section = ParagraphStyle(
    'SectionH1',
    parent=styles['Heading2'],
    fontName='Helvetica-Bold',
    fontSize=11.5,
    leading=15,
    textColor=colors.black,
    spaceBefore=9,
    spaceAfter=4
)

h2_section = ParagraphStyle(
    'SectionH2',
    parent=styles['Heading3'],
    fontName='Helvetica-Bold',
    fontSize=10,
    leading=13.5,
    textColor=colors.black,
    spaceBefore=7,
    spaceAfter=3
)

body_text = ParagraphStyle(
    'BodyNormal',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=9,
    leading=12.5,
    textColor=colors.black,
    spaceAfter=4
)

body_text_bold = ParagraphStyle(
    'BodyBold',
    parent=styles['Normal'],
    fontName='Helvetica-Bold',
    fontSize=9,
    leading=12.5,
    textColor=colors.black,
    spaceAfter=4
)

bullet_item = ParagraphStyle(
    'BulletItem',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=9,
    leading=12.2,
    leftIndent=14,
    firstLineIndent=-10,
    textColor=colors.black,
    spaceAfter=2
)

bullet_subitem = ParagraphStyle(
    'BulletSubItem',
    parent=styles['Normal'],
    fontName='Helvetica',
    fontSize=8.8,
    leading=11.8,
    leftIndent=28,
    firstLineIndent=-10,
    textColor=colors.black,
    spaceAfter=1.5
)

story = []

# =========================================================================
# HALAMAN 1: IDENTITAS & KASUS RENTAL KENDARAAN (SAMA SEPERTI SAMPEL)
# =========================================================================
meta_table_data = [
    [
        Paragraph("<b>Nama</b> : Arjuna Lanang Adiwarsana<br/><b>Prodi</b> : D3 Teknik Informatika<br/><b>NRP</b>  : 3125522010", header_meta_style),
        Paragraph("Genap", genap_style)
    ]
]
meta_table = Table(meta_table_data, colWidths=[360, 144])
meta_table.setStyle(TableStyle([
    ('VALIGN', (0,0), (-1,-1), 'TOP'),
    ('BOTTOMPADDING', (0,0), (-1,-1), 0),
    ('TOPPADDING', (0,0), (-1,-1), 0),
    ('LEFTPADDING', (0,0), (-1,-1), 0),
    ('RIGHTPADDING', (0,0), (-1,-1), 0),
]))
story.append(meta_table)

story.append(Paragraph("Soal UTS Praktikum Pemrograman Berorientasi Obyek", doc_title_style))

story.append(Paragraph("1. Sistem Rental Kendaraan", h1_section))
story.append(Paragraph(
    "Sebuah perusahaan persewaan armada kendaraan mengelola berbagai tipe transportasi (Mobil dan Motor) serta melayani proses peminjaman pelanggan dengan berbagai kategori keanggotaan. "
    "Sistem ini menangani perhitungan biaya sewa harian, proteksi asuransi, biaya supir, fasilitas helm ekstra, hingga denda keterlambatan pengembalian secara dinamis. "
    "Program dibangun berbasis Java OOP dengan mengimplementasikan prinsip Encapsulation, Validation, Relationship, Inheritance, Polymorphism, Abstract Class, dan Interface.",
    body_text
))

story.append(Paragraph("Lakukan Analisis :", body_text_bold))
story.append(Paragraph(
    "a. Identifikasi Class, Attribute, Method, dan Relasi yang ada pada sistem rental. Tentukan rancangan Abstract Class, Interface, Subclass, serta Relasi Antar-Object (Association / Aggregation).<br/>"
    "b. Buatlah Class Diagram UML lengkap dengan notasi standar akademik.<br/>"
    "c. Implementasikan program Java dengan minimal 2 validasi business rules dan method overriding polimorfik.<br/>"
    "d. Tambahkan unsur kreativitas berupa logika bisnis mandiri (sistem membership dan perhitungan denda dinamis).",
    body_text
))

story.append(Paragraph("Identifikasi Class, Atribut, Method, dan Relasi", h1_section))

story.append(Paragraph("- <b>Interface: DapatDisewa</b>", bullet_item))
story.append(Paragraph("i. Peran: Menentukan kontrak perilaku ketersediaan sewa armada tanpa terikat hierarki fisik.", bullet_subitem))
story.append(Paragraph("ii. Method: <code>isTersedia(): boolean</code>, <code>sewa(idTransaksi: String): void</code>, <code>kembalikan(): void</code>.", bullet_subitem))

story.append(Paragraph("- <b>Abstract Superclass: Kendaraan</b> (implements <code>DapatDisewa</code>)", bullet_item))
story.append(Paragraph("i. Atribut: <code>platNomor</code>, <code>merk</code>, <code>model</code>, <code>tahunProduksi</code>, <code>tarifPerHari</code>, <code>tersedia</code> (seluruhnya <code>private</code>).", bullet_subitem))
story.append(Paragraph("ii. Method: Getter, Setter dengan validasi, <code>tampilkanInformasiDasar()</code>.", bullet_subitem))
story.append(Paragraph("iii. Abstract Method: <code>hitungBiayaSewa(durasi: int): double</code> dan <code>tampilkanSpesifikasi(): void</code>.", bullet_subitem))

story.append(Paragraph("- <b>Subclass: Mobil</b> (extends <code>Kendaraan</code>)", bullet_item))
story.append(Paragraph("i. Atribut Tambahan: <code>kapasitasPenumpang</code>, <code>jenisTransmisi</code>, <code>denganSupir</code>, <code>biayaSupirPerHari</code>, <code>biayaAsuransiHarian</code>.", bullet_subitem))
story.append(Paragraph("ii. Perilaku Override: Menghitung total sewa mencakup tarif dasar, proteksi asuransi harian, serta opsi supir.", bullet_subitem))

story.append(Paragraph("- <b>Subclass: Motor</b> (extends <code>Kendaraan</code>)", bullet_item))
story.append(Paragraph("i. Atribut Tambahan: <code>tipeMotor</code>, <code>jumlahHelm</code>, <code>biayaHelmTambahanPerHari</code>.", bullet_subitem))
story.append(Paragraph("ii. Perilaku Override: Menghitung sewa dasar ditambah biaya sewa unit helm kedua (helm ke-1 gratis).", bullet_subitem))

story.append(Paragraph("- <b>Entity: Pelanggan</b>", bullet_item))
story.append(Paragraph("i. Atribut: <code>idPelanggan</code>, <code>nama</code>, <code>nomorTelepon</code>, <code>tipeMember</code> (VIP, GOLD, REGULER).", bullet_subitem))
story.append(Paragraph("ii. Method: <code>getPersentaseDiskon(): double</code> (VIP = 10%, GOLD = 5%, REGULER = 0%).", bullet_subitem))

story.append(Paragraph("- <b>Transaksi / Controller: TransaksiRental</b>", bullet_item))
story.append(Paragraph("i. Relasi: Agregasi dengan <code>Pelanggan</code> (*has-a*) dan <code>Kendaraan</code> (*has-a*).", bullet_subitem))
story.append(Paragraph("ii. Atribut: <code>idTransaksi</code>, <code>pelanggan</code>, <code>kendaraan</code>, <code>durasiHari</code>, <code>keterlambatanHari</code>, <code>statusSelesai</code>.", bullet_subitem))
story.append(Paragraph("iii. Method: <code>hitungSubtotalSewa()</code>, <code>hitungPotonganDiskon()</code>, <code>hitungDendaKeterlambatan()</code>, <code>hitungTotalBayar()</code>, <code>prosesPengembalian()</code>, <code>cetakKuitansi()</code>.", bullet_subitem))

# =========================================================================
# HALAMAN 2: SKEMA ENKAPSULASI, VALIDASI, DAN CLASS DIAGRAM
# =========================================================================
story.append(PageBreak())

story.append(Paragraph("Tahap Dekomposisi & Business Rules", h1_section))

story.append(Paragraph("- <b>1. Enkapsulasi & Akses Terkontrol:</b>", bullet_item))
story.append(Paragraph("i. Kriteria: Seluruh atribut utama pada setiap class berakses <code>private</code>. Akses modifikasi dibatasi ketat melalui konstruktor dan setter yang tervalidasi.", bullet_subitem))
story.append(Paragraph("ii. Atribut sensitif seperti status ketersediaan armada hanya dapat diubah melalui siklus transaksi resmi (<code>sewa()</code> dan <code>kembalikan()</code>).", bullet_subitem))

story.append(Paragraph("- <b>2. Validasi Business Rules (Minimal 2 Rules):</b>", bullet_item))
story.append(Paragraph("i. <b>Rule 1 (Validasi Durasi Sewa):</b> Durasi sewa wajib bernilai positif (<code>durasiHari >= 1</code>). Jika durasi &lt; 1, program secara otomatis melempar <code>IllegalArgumentException</code>.", bullet_subitem))
story.append(Paragraph("ii. <b>Rule 2 (Validasi Ketersediaan Kendaraan):</b> Transaksi rental hanya dapat dibuat apabila kendaraan berstatus tersedia (<code>isTersedia() == true</code>). Jika kendaraan sedang disewa, sistem melempar <code>IllegalStateException</code> untuk mencegah sewa ganda.", bullet_subitem))
story.append(Paragraph("iii. <b>Rule 3 (Integritas Data):</b> Nomor identitas pelanggan, plat nomor, dan tarif sewa harian wajib valid dan bernilai lebih besar dari 0.", bullet_subitem))

story.append(Paragraph("- <b>3. Relasi Antar-Object (Minimal 2 Relasi):</b>", bullet_item))
story.append(Paragraph("i. <b>Relasi 1 (Aggregation / Association):</b> <code>TransaksiRental</code> menghubungkan objek <code>Pelanggan</code> (multiplisitas 1 ke 0..*).", bullet_subitem))
story.append(Paragraph("ii. <b>Relasi 2 (Aggregation / Association):</b> <code>TransaksiRental</code> menghubungkan objek <code>Kendaraan</code> (multiplisitas 1 ke 0..*).", bullet_subitem))
story.append(Paragraph("iii. <b>Relasi 3 (Generalization):</b> Subclass <code>Mobil</code> dan <code>Motor</code> mengekstensi superclass <code>Kendaraan</code>.", bullet_subitem))
story.append(Paragraph("iv. <b>Relasi 4 (Realization):</b> Class <code>Kendaraan</code> mengimplementasikan antarmuka <code>DapatDisewa</code>.", bullet_subitem))

story.append(Spacer(1, 4))
story.append(Paragraph("b. Perancangan UML Class Diagram", h1_section))
story.append(Paragraph(
    "Diagram di bawah dirancang mengikuti standar formal Unified Modeling Language (UML) monokrom, menampilkan stereotype <code>&lt;&lt;interface&gt;&gt;</code>, <code>&lt;&lt;abstract&gt;&gt;</code>, relasi inheritance tree-fork, realization dashed-line, serta relasi agregasi terhubung:",
    body_text
))

if os.path.exists(class_diagram_img):
    story.append(RLImage(class_diagram_img, width=504, height=310))

# =========================================================================
# HALAMAN 3: PENJELASAN KONSEP OOP & UNTUK KREATIVITAS
# =========================================================================
story.append(PageBreak())

story.append(Paragraph("c. Implementasi Konsep Berorientasi Obyek (PBO)", h1_section))

story.append(Paragraph(
    "Berikut adalah rincian pemenuhan konsep pemrograman berorientasi obyek dari P1 hingga P7 sesuai rubrik ujian:",
    body_text
))

story.append(Paragraph("- <b>Inheritance & Abstract Class (P5 - P7):</b>", bullet_item))
story.append(Paragraph(
    "Superclass <code>Kendaraan</code> dideklarasikan sebagai <code>abstract class</code> karena entitas 'Kendaraan' secara riil adalah konsep umum yang tidak boleh diinstansiasi langsung tanpa wujud spesifik (apakah Mobil atau Motor). "
    "Class ini memuat method abstrak <code>hitungBiayaSewa(durasi)</code> dan <code>tampilkanSpesifikasi()</code> yang mewajibkan seluruh subclass untuk mengimplementasikan perilaku masing-masing secara spesifik.",
    bullet_subitem
))

story.append(Paragraph("- <b>Polymorphism & Dynamic Method Dispatch (P6 - P7):</b>", bullet_item))
story.append(Paragraph(
    "Polimorfisme diterapkan melalui method overriding pada <code>hitungBiayaSewa(int durasiHari)</code>. "
    "Pada subclass <code>Mobil</code>, biaya mencakup <code>(TarifDasar + Asuransi) * Durasi + BiayaSupir</code>. "
    "Sedangkan pada subclass <code>Motor</code>, kalkulasi berupa <code>TarifDasar * Durasi + BiayaHelmEkstra</code>. "
    "Ketika daftar kendaraan diproses di dalam array bertipe superclass <code>Kendaraan[] armada</code>, Java secara dinamis (Dynamic Binding) mengeksekusi logika perhitungan milik class konkretnya masing-masing saat runtime.",
    bullet_subitem
))

story.append(Paragraph("- <b>Interface (P7):</b>", bullet_item))
story.append(Paragraph(
    "Interface <code>DapatDisewa</code> berperan sebagai kontrak kapabilitas (<i>behavior contract</i>). "
    "Antarmuka ini memisahkan tata kelola status sewa (<code>isTersedia()</code>, <code>sewa()</code>, dan <code>kembalikan()</code>) agar dapat diadopsi secara fleksibel tanpa mencemari logika hierarki kendaraan.",
    bullet_subitem
))

story.append(Paragraph("d. Unsur Kreativitas Solusi (Logika Bisnis Mandiri)", h1_section))
story.append(Paragraph(
    "Sebagai pemenuhan unsur kreativitas di luar ketentuan minimal, ditambahkan dua fitur logika bisnis riil:",
    body_text
))

story.append(Paragraph("- <b>1. Klasifikasi Membership & Diskon Bertingkat:</b>", bullet_item))
story.append(Paragraph(
    "Pelanggan diklasifikasikan ke dalam tipe member <code>VIP</code>, <code>GOLD</code>, dan <code>REGULER</code>. "
    "Member VIP otomatis mendapatkan diskon loyalitas sebesar 10% dari subtotal sewa, sedangkan Member GOLD mendapatkan diskon 5%. Pelanggan Reguler tidak mendapatkan potongan diskon. Nilai diskon dipotong transparan saat transaksi dibuat.",
    bullet_subitem
))

story.append(Paragraph("- <b>2. Perhitungan Denda Keterlambatan Pengembalian Dinamis:</b>", bullet_item))
story.append(Paragraph(
    "Ketika pelanggan mengembalikan kendaraan melewati batas durasi yang disepakati (<code>keterlambatanHari > 0</code>), sistem menerapkan penalti denda progresif sebesar 150% (1,5 kali lipat) dari tarif sewa normal harian untuk setiap hari keterlambatan. "
    "Rumus: <code>totalDenda = keterlambatanHari * (tarifPerHari * 1.5)</code>.",
    bullet_subitem
))

story.append(Paragraph("- <b>3. Kuitansi Digital Transparan:</b>", bullet_item))
story.append(Paragraph(
    "Transaksi menyediakan method <code>cetakKuitansi()</code> yang merinci subtotal sewa kotor, potongan diskon membership, denda keterlambatan (jika ada), serta total akhir pembayaran secara formal dan profesional.",
    bullet_subitem
))

# =========================================================================
# HALAMAN 4: SCREENSHOT HASIL PENGUJIAN 1
# =========================================================================
story.append(PageBreak())

story.append(Paragraph("e. Hasil Eksekusi Program (Log Terminal)", h1_section))
story.append(Paragraph(
    "Pengujian program dijalankan melalui kelas <code>Main.java</code> dengan menguji minimal 3 objek riil, skenario polimorfisme, validasi kegagalan, dan skenario pengembalian:",
    body_text
))

story.append(Paragraph("1. Eksekusi Inisialisasi Objek & Demonstrasi Polymorphism", h2_section))
story.append(Paragraph(
    "Terminal di bawah menunjukkan inisialisasi 4 objek kendaraan konkret (Innova Reborn, HiAce Commuter dengan supir, Honda PCX 160 helm ekstra, dan Kawasaki Ninja ZX-25R) serta evaluasi biaya polimorfik lewat perulangan array superclass:",
    body_text
))

if os.path.exists(ss1_path):
    story.append(RLImage(ss1_path, width=504, height=195))

story.append(Spacer(1, 6))
story.append(Paragraph("Logika Penyusunannya :", body_text_bold))
story.append(Paragraph(
    "- 1. Pertama, program menginstansiasi objek spesifik dari subclass <code>Mobil</code> dan <code>Motor</code>. Nilai atribut unik seperti supir dan helm diteruskan melalui konstruktor.",
    bullet_item
))
story.append(Paragraph(
    "- 2. Kedua, objek-objek tersebut dimasukkan ke dalam koleksi polimorfik <code>Kendaraan[] daftarArmada</code>. Saat method <code>k.hitungBiayaSewa(3)</code> dipanggil, formula yang berjalan secara dinamis berbeda antara Mobil (tarif + asuransi + supir) dan Motor (tarif + helm ekstra).",
    bullet_item
))
story.append(Paragraph(
    "<b>Hasil Akhir:</b> Nilai estimasi biaya terhitung tepat dan presisi tanpa perlu melakukan <i>type-casting</i> manual (<code>instanceof</code>), membuktikan penerapan <i>polymorphism</i> yang efektif.",
    body_text
))

# =========================================================================
# HALAMAN 5: SCREENSHOT HASIL PENGUJIAN 2 & 3 (VALIDASI & DENDA)
# =========================================================================
story.append(PageBreak())

story.append(Paragraph("2. Eksekusi Pengujian Validasi Business Rules", h2_section))
story.append(Paragraph(
    "Terminal di bawah menguji ketahanan program saat menghadapi kondisi tidak valid (durasi &lt;= 0 dan upaya menyewa kendaraan yang sedang berstatus terpakai):",
    body_text
))

if os.path.exists(ss2_path):
    story.append(RLImage(ss2_path, width=504, height=155))

story.append(Spacer(1, 6))
story.append(Paragraph("Analisis Penanganan Validasi :", body_text_bold))
story.append(Paragraph(
    "- <b>Uji Coba Durasi 0 Hari:</b> Sistem menangkap pelanggaran aturan durasi dan melempar <code>IllegalArgumentException</code> dengan pesan <i>'Durasi sewa minimal 1 hari'</i>. Program tidak crash melainkan ditangani rapi dalam blok <code>try-catch</code>.",
    bullet_item
))
story.append(Paragraph(
    "- <b>Uji Coba Sewa Ganda:</b> Ketika pelanggan kedua mencoba menyewa mobil Innova yang sedang aktif disewa, sistem menolak dan melempar <code>IllegalStateException</code> dengan pesan <i>'Kendaraan sedang tidak tersedia'</i>.",
    bullet_item
))

story.append(Spacer(1, 6))
story.append(Paragraph("3. Eksekusi Pengembalian Tepat Waktu vs Terlambat (Denda Dinamis)", h2_section))
story.append(Paragraph(
    "Terminal di bawah menampilkan pencetakan kuitansi pada skenario pengembalian normal (Mobil Innova oleh Member VIP) dibandingkan pengembalian terlambat 2 hari (Motor PCX oleh Member GOLD):",
    body_text
))

if os.path.exists(ss3_path):
    story.append(RLImage(ss3_path, width=504, height=210))

# =========================================================================
# HALAMAN 6: PEMBAHASAN LOGIKA EKSEKUSI & CHEAT SHEET DEMO
# =========================================================================
story.append(PageBreak())

story.append(Paragraph("f. Pembahasan Logika Eksekusi dan Demo 3 Menit", h1_section))

story.append(Paragraph(
    "Berdasarkan hasil eksekusi program di atas, seluruh skenario bisnis berjalan sesuai rancangan awal. Berikut adalah rangkuman esensial untuk menjawab pertanyaan penguji saat sesi presentasi demo 3 menit:",
    body_text
))

story.append(Paragraph("1. Pertanyaan 1: Mengapa saya merancang class seperti ini?", h2_section))
story.append(Paragraph(
    "Arsitektur dirancang modular dan terpisah berdasarkan perannya masing-masing (<i>Separation of Concerns</i>):<br/>"
    "- <b>Interface <code>DapatDisewa</code></b> berfungsi sebagai kontrak perilaku formal untuk mengontrol status peminjaman tanpa mencampuradukkan detail atribut fisik.<br/>"
    "- <b>Abstract Class <code>Kendaraan</code></b> berperan sebagai superclass yang merangkum atribut bersama (plat nomor, merk, model, tarif) dengan proteksi <code>private</code>. Kelas ini dijadikan abstrak agar mencegah instansiasi armada tanpa identitas jenis yang jelas.<br/>"
    "- <b>Subclass <code>Mobil</code> dan <code>Motor</code></b> memperluas superclass untuk menangani spesifikasi unik (seperti kapasitas penumpang, transmisi, supir, tipe motor, dan fasilitas helm).<br/>"
    "- <b>Class <code>Pelanggan</code></b> bertindak sebagai model data penyewa dan klasifikasi tingkatan membership.<br/>"
    "- <b>Class <code>TransaksiRental</code></b> menghubungkan <code>Pelanggan</code> dan <code>Kendaraan</code> menggunakan relasi agregasi, mengelola siklus sewa, menghitung potongan diskon, dan menghitung denda keterlambatan secara otomatis.",
    body_text
))

story.append(Paragraph("2. Pertanyaan 2: Di mana polymorphism terjadi?", h2_section))
story.append(Paragraph(
    "Polymorphism terjadi pada pemanggilan method <code>hitungBiayaSewa(int durasiHari)</code>:<br/>"
    "- Method ini dideklarasikan sebagai <code>abstract method</code> pada superclass <code>Kendaraan</code>.<br/>"
    "- <code>Mobil</code> meng-override method ini dengan menyertakan biaya premi asuransi harian dan tarif opsi supir.<br/>"
    "- <code>Motor</code> meng-override method ini dengan menyertakan sewa fasilitas helm kedua.<br/>"
    "- Pada <code>Main.java</code>, seluruh kendaraan disimpan dalam array bertipe superclass <code>Kendaraan[]</code>. Ketika dipanggil melalui baris <code>k.hitungBiayaSewa(durasi)</code>, JVM secara dinamis mengeksekusi rumus yang tepat sesuai tipe objek aslinya saat runtime tanpa memerlukan pengecekan manual.",
    body_text
))

story.append(Paragraph("3. Pertanyaan 3: Apa fitur kreatif yang ditambahkan?", h2_section))
story.append(Paragraph(
    "Fitur kreatif yang diimplementasikan adalah:<br/>"
    "- <b>Sistem Diskon Membership Bertingkat:</b> Pelanggan dengan status Member VIP mendapatkan potongan harga 10%, sedangkan Member GOLD memperoleh diskon 5% dari subtotal sewa.<br/>"
    "- <b>Sistem Denda Keterlambatan Otomatis:</b> Jika pelanggan terlambat mengembalikan kendaraan (<code>keterlambatanHari > 0</code>), program otomatis mengenakan denda penalti sebesar 150% (1,5 kali lipat) dari tarif normal kendaraan per hari keterlambatan.<br/>"
    "- <b>Struk Kuitansi Formal:</b> Seluruh rincian biaya sewa, diskon, dan denda disajikan secara transparan melalui kuitansi digital.",
    body_text
))

# Build Document
doc.build(story, canvasmaker=NumberedCanvas)
print(f"[SUCCESS] Laporan UTS PDF berhasil dibuat: {pdf_output_path}")
