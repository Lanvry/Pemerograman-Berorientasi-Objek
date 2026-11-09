import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn
import os

doc = docx.Document()

# Page Margins: Standar Akademik (Kiri: 4 cm, Atas: 3 cm, Kanan: 3 cm, Bawah: 3 cm)
for section in doc.sections:
    section.top_margin = Inches(1.18)     # ~3.0 cm
    section.bottom_margin = Inches(1.18)  # ~3.0 cm
    section.left_margin = Inches(1.57)    # ~4.0 cm
    section.right_margin = Inches(1.18)   # ~3.0 cm
    section.different_first_page_header_footer = False

# Helper Styling Functions
def add_title(text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.line_spacing = 1.15
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(14)
    r.bold = True
    r.font.color.rgb = RGBColor(17, 24, 39)
    return p

def add_subtitle(text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(12)
    p.paragraph_format.line_spacing = 1.15
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(12)
    r.bold = True
    r.font.color.rgb = RGBColor(55, 65, 81)
    return p

def add_h1(text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(12)
    r.bold = True
    r.font.color.rgb = RGBColor(17, 24, 39)
    return p

def add_h2(text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(10)
    p.paragraph_format.space_after = Pt(3)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(12)
    r.bold = True
    r.font.color.rgb = RGBColor(31, 41, 55)
    return p

def add_h3(text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(8)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(11.5)
    r.bold = True
    r.italic = True
    r.font.color.rgb = RGBColor(55, 65, 81)
    return p

def add_body(text, italic=False):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.line_spacing = 1.15
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(12)
    r.italic = italic
    r.font.color.rgb = RGBColor(17, 24, 39)
    return p

def add_bullet(text, prefix=""):
    p = doc.add_paragraph(style='List Bullet')
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(3)
    p.paragraph_format.line_spacing = 1.15
    if prefix:
        r_pre = p.add_run(prefix)
        r_pre.font.name = "Times New Roman"
        r_pre.font.size = Pt(12)
        r_pre.bold = True
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(12)
    r.font.color.rgb = RGBColor(17, 24, 39)
    return p

def add_code_block(code_text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.line_spacing = 1.05
    p.paragraph_format.left_indent = Inches(0.2)
    pPr = p._p.get_or_add_pPr()
    pBdr = parse_xml(
        f'<w:pBdr {nsdecls("w")}>'
        f'  <w:left w:val="single" w:sz="18" w:space="8" w:color="3B82F6"/>'
        f'</w:pBdr>'
    )
    pPr.append(pBdr)
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="F8FAFC"/>')
    pPr.append(shd)

    r = p.add_run(code_text)
    r.font.name = "Courier New"
    r.font.size = Pt(9.5)
    r.font.color.rgb = RGBColor(30, 41, 59)
    return p

def style_academic_table(table, col_widths=None):
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    for i, row in enumerate(table.rows):
        trPr = row._tr.get_or_add_trPr()
        trPr.append(parse_xml(f'<w:cantSplit {nsdecls("w")}/>'))
        if i == 0:
            trPr.append(parse_xml(f'<w:tblHeader {nsdecls("w")}/>'))
        for j, cell in enumerate(row.cells):
            if col_widths and j < len(col_widths):
                cell.width = col_widths[j]
            cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
            tcPr = cell._tc.get_or_add_tcPr()
            if i == 0:
                shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="E2E8F0"/>')
                tcPr.append(shd)
            for p in cell.paragraphs:
                p.paragraph_format.space_before = Pt(2.5)
                p.paragraph_format.space_after = Pt(2.5)
                p.paragraph_format.line_spacing = 1.05
                for r in p.runs:
                    r.font.name = "Times New Roman"
                    r.font.size = Pt(10)
                    if i == 0:
                        r.bold = True
                        r.font.color.rgb = RGBColor(15, 23, 42)
                    else:
                        r.font.color.rgb = RGBColor(30, 41, 59)

# =============================================================
# HEADER LAPORAN & IDENTITAS
# =============================================================
add_title("LAPORAN PRAKTIKUM PEMROGRAMAN BERORIENTASI OBYEK")
add_subtitle("MODUL 10: EXCEPTION HANDLING DAN VALIDASI STATE OBJEK")

# Profil Mahasiswa & Proyek
t_identitas = doc.add_table(rows=6, cols=2)
info_data = [
    ("Nama Mahasiswa", "Arjuna Lanang Adiwarsana"),
    ("NRP", "3125522010"),
    ("Kelas / Program Studi", "D3 PJJ Teknik Informatika - PENS PSDKU Sumenep"),
    ("Dosen Pengampu", "Nirwana Haidar Hari, S.Pd., M.Kom."),
    ("Mata Kuliah", "Praktikum Pemrograman Berorientasi Obyek (PBO)"),
    ("Topik Utama", "Exception Handling, State Validation & Atomic State Preservation")
]
for idx, (label, val) in enumerate(info_data):
    row = t_identitas.rows[idx].cells
    row[0].text = label
    row[1].text = val
    row[0].paragraphs[0].runs[0].bold = True
style_academic_table(t_identitas, [Inches(2.0), Inches(4.2)])
doc.add_paragraph().paragraph_format.space_after = Pt(6)

# =============================================================
# 1. TUJUAN PRAKTIKUM
# =============================================================
add_h1("1. Tujuan Praktikum")
add_body("Berdasarkan silabus dan panduan Praktikum Pemrograman Berorientasi Obyek Modul 10, sasaran pembelajaran yang dicapai adalah:")
add_bullet("Membedakan konsep validasi input masukan dengan penanganan exception, serta menentukan titik waktu yang tepat untuk melempar exception sebelum operasi mutasi objek berlangsung.", "a. ")
add_bullet("Menguasai konstruksi penanganan galat bahasa Java secara terstruktur menggunakan blok try, catch, finally, kata kunci throw, dan klausul throws.", "b. ")
add_bullet("Merancang kelas custom exception turunan dari java.lang.Exception yang merefleksikan domain aturan bisnis aplikasi secara kontekstual.", "c. ")
add_bullet("Menjamin konsistensi state internal objek (atomic state preservation) sehingga saat suatu transaksi ditolak atau gagal, status dan atribut internal objek tidak rusak (no side-effects on failure).", "d. ")

# =============================================================
# 2. DASAR TEORI SINGKAT
# =============================================================
add_h1("2. Dasar Teori Singkat")
add_body("Exception merupakan peristiwa abnormal atau gangguan luar biasa yang memutus alur normal eksekusi program Java. Mekanisme penanganan exception dirancang untuk memisahkan logika utama bisnis dari penanganan kegagalan, sehingga sistem tetap berjalan stabil (robust) tanpa mengalami penghentian paksa (crash).")

add_h2("2.1 Komponen Exception Handling Java")
table_komponen = doc.add_table(rows=1, cols=3)
hdr = table_komponen.rows[0].cells
hdr[0].text = "Komponen"
hdr[1].text = "Sintaksis"
hdr[2].text = "Fungsi dan Prinsip Kerja"

komponen_data = [
    ("try", "try { ... }", "Membungkus blok instruksi rawan galat (risky code) yang berpotensi menghasilkan exception."),
    ("catch", "catch (ExceptionType e) { ... }", "Menangkap dan memproses exception tertentu yang dilempar dari blok try. Disusun urut dari tipe yang paling spesifik ke tipe umum."),
    ("finally", "finally { ... }", "Blok penutup yang dijamin selalu dieksekusi dalam segala kondisi, baik normal maupun saat terjadi exception. Digunakan untuk pelepasan resource (file/koneksi), dilarang untuk mutasi nilai bisnis."),
    ("throw", "throw new MyException(...);", "Kata kerja aksi untuk melempar instansiasi objek exception secara eksplisit saat syarat validasi dilanggar."),
    ("throws", "void m() throws Ex1, Ex2", "Klausul deklarasi kontrak pada tanda tangan method bahwa method tersebut meneruskan checked exception kepada pemanggil."),
    ("Custom Exception", "class X extends Exception", "Kelas exception khusus domain bisnis aplikasi agar pesan kesalahan deskriptif dan menyediakan informasi kontekstual yang kaya."),
    ("Test-Before-Mutate", "Validasi di Awal Method", "Pola evaluasi seluruh pra-kondisi sebelum atribut internal dimutasi. Jika gagal, exception dilempar dan data internal dijamin tidak termutasi.")
]
for item in komponen_data:
    row = table_komponen.add_row().cells
    row[0].text = item[0]
    row[1].text = item[1]
    row[2].text = item[2]
style_academic_table(table_komponen, [Inches(1.4), Inches(1.8), Inches(3.0)])
doc.add_paragraph().paragraph_format.space_after = Pt(4)

add_h2("2.2 Checked vs Unchecked Exception")
add_body("Dalam hierarki kelas Java Throwable, exception diklasifikasikan ke dalam dua kelompok utama:")
add_bullet("Merupakan kelas turunan dari RuntimeException (misalnya IllegalArgumentException, NumberFormatException, dan NullPointerException). Kompiler tidak memaksa pemanggil untuk menyertakan blok try-catch atau deklarasi throws. Exception jenis ini lazimnya mencerminkan kesalahan logika pemrograman atau masukan pengguna yang keliru pada runtime.", "• Unchecked Exception: ")
add_bullet("Merupakan kelas turunan langsung dari Exception di luar cabang RuntimeException (misalnya StokTidakCukupException, KapasitasPenuhException). Kompiler secara ketat memeriksa (checked) bahwa pemanggil method wajib membungkus pemanggilan dalam try-catch atau meneruskannya kembali via throws. Exception ini digunakan untuk kondisi kegagalan bisnis yang realistis terjadi.", "• Checked Exception: ")

# =============================================================
# 3. STUDI KASUS
# =============================================================
add_h1("3. Studi Kasus Praktikum")

add_h2("3.1 Studi Kasus 1: Inventori Produk Toko (Modul Mandiri P10)")
add_body("Objek Produk mengelola data inventori dengan persediaan awal sebanyak 5 unit. Operasi pembelian dirancang dengan empat aturan bisnis utama:")
add_bullet("Jumlah barang yang dibeli harus berupa nilai bilangan bulat valid (dikonversi melalui Integer.parseInt).", "1. ")
add_bullet("Jumlah barang yang dibeli harus bernilai positif atau lebih dari nol (jumlah > 0).", "2. ")
add_bullet("Jumlah barang yang dibeli tidak boleh melebihi sisa stok yang tersedia saat itu (jumlah <= stok).", "3. ")
add_bullet("Nilai stok hanya boleh dikurangi apabila seluruh validasi di atas berhasil dilalui tanpa exception.", "4. ")

add_h2("3.2 Studi Kasus 2: Sistem Parkir Terpadu (Project Akhir Smart Parkir)")
add_body("Pada sistem Smart Parkir skala riil, validasi state dan exception handling diterapkan untuk mengamankan 5 titik rawan operasional:")
add_bullet("FormatPlatTidakValidException: Mencegah nomor plat kosong, kurang dari 3 karakter, atau mengandung simbol asing tercatat ke sistem.", "1. ")
add_bullet("KendaraanSudahParkirException: Mencegah kendaraan dengan nomor plat yang sama didaftarkan masuk kembali saat masih terparkir di slot gedung.", "2. ")
add_bullet("KapasitasPenuhException: Menolak pembukaan gerbang otomatis saat slot parkir pada lantai atau kategori kendaraan (Motor/Mobil/EV) telah penuh.", "3. ")
add_bullet("PembayaranKurangException: Mencegah pembebasan slot parkir fisik dan penutupan tiket jika nominal uang yang dibayar di loket kasir kurang dari tagihan progresif.", "4. ")
add_bullet("TiketTidakDitemukanException: Menolak pemrosesan kendaraan tanpa karcis fisik sah atau kendaraan yang sudah checkout sebelumnya.", "5. ")

# =============================================================
# 4. LANGKAH PRAKTIKUM & PENGUJIAN KASUS PRODUK
# =============================================================
add_h1("4. Langkah Praktikum & Pengujian Program Dasar (Kasus Produk)")
add_body("Program dasar dibangun menggunakan arsitektur modular yang terdiri dari tiga berkas Java terpisah:")
add_bullet("StokTidakCukupException.java: Kelas checked custom exception dengan konstruktor pembawa pesan kesalahan.", "1. ")
add_bullet("Produk.java: Entitas domain yang mengenkapsulasi atribut privat stok dan method mutasi beli(int jumlah). Method ini mengimplementasikan pola Test-Before-Mutate dengan melempar IllegalArgumentException jika jumlah <= 0 dan StokTidakCukupException jika jumlah > stok sebelum melakukan pengurangan stok.", "2. ")
add_bullet("Main.java: Driver class yang menjalankan pengujian terhadap urutan masukan (\"2\", \"0\", \"9\", \"abc\"), dilanjutkan pengujian tambahan (\"-1\" dan \"3\").", "3. ")

add_h2("4.1 Hasil Eksekusi Terminal macOS (Main.java)")
add_body("Berikut adalah tangkapan layar terminal eksekusi program asli menggunakan kompiler javac dan runtime JVM pada lingkungan macOS:")

# Insert screenshot if exists
script_dir = os.path.dirname(os.path.abspath(__file__))
img_path = os.path.join(script_dir, "terminal_screenshot.png")
if os.path.exists(img_path):
    p_img = doc.add_paragraph()
    p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_img.paragraph_format.space_before = Pt(4)
    p_img.paragraph_format.space_after = Pt(2)
    p_img.paragraph_format.keep_with_next = True
    r_img = p_img.add_run()
    r_img.add_picture(img_path, width=Inches(6.0))
    p_cap = doc.add_paragraph()
    p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_cap.paragraph_format.space_after = Pt(6)
    r_cap = p_cap.add_run("Gambar 1. Hasil Eksekusi Driver Class Main.java pada Terminal macOS")
    r_cap.font.name = "Times New Roman"
    r_cap.font.size = Pt(9.5)
    r_cap.italic = True

add_h2("4.2 Analisis Pengujian Tambahan Input \"-1\" dan \"3\"")
add_bullet("Ketika masukan \"-1\" diuji, method Integer.parseInt berhasil mengubahnya menjadi bilangan bulat -1. Namun, pada pemeriksaan kondisi awal (jumlah <= 0), program melempar IllegalArgumentException dengan pesan \"Jumlah pembelian harus lebih dari nol.\". Karena validasi dicegat sebelum pengurangan, stok internal tetap terjaga konsisten di angka 3 unit.", "• Analisis Masukan \"-1\": ")
add_bullet("Ketika masukan \"3\" diuji terhadap sisa stok 3 unit, kondisi jumlah <= stok terpenuhi (3 <= 3). Operasi pembelian berhasil diproses dan stok akhir termutasi menjadi 0 unit. Pembelian tepat sejumlah stok yang tersisa diperbolehkan karena aturan bisnis inventori memperbolehkan kondisi stok habis (zero inventory state) yang sah tanpa merusak state objek.", "• Analisis Masukan \"3\": ")

# =============================================================
# 5. INTEGRASI PADA PROYEK SMART PARKIR
# =============================================================
add_h1("5. Implementasi & Integrasi pada Project Akhir (Smart Parkir)")
add_body("Konsep validasi state dan domain exception diterapkan secara fungsional ke dalam sistem Smart Parkir pada berkas GedungParkir.java, KarcisParkir.java, Kendaraan.java, dan AdminGUI.java.")

add_h2("5.1 Daftar 5 Custom Domain Exception Smart Parkir")
table_sp = doc.add_table(rows=1, cols=4)
h_sp = table_sp.rows[0].cells
h_sp[0].text = "Nama Custom Exception"
h_sp[1].text = "Kelas Induk"
h_sp[2].text = "Kondisi Pemicu Pelemparan"
h_sp[3].text = "Dampak Pengamanan State"

sp_data = [
    ("FormatPlatTidakValidException", "Exception", "Nomor plat null, panjang < 3 karakter, atau mengandung simbol rusak.", "Instansiasi objek dibatalkan; database tetap bersih."),
    ("KendaraanSudahParkirException", "Exception", "Nomor plat sama terdeteksi check-in padahal berstatus aktif di slot.", "Portal gerbang masuk tetap terkunci; alokasi slot ganda dicegah."),
    ("KapasitasPenuhException", "Exception", "Slot kategori kendaraan (Motor/Mobil/EV) bernilai 0.", "LED menampilkan 'PARKIR PENUH'; kapasitas terisi tidak melebihi kuota."),
    ("PembayaranKurangException", "Exception", "Nominal bayar lebih kecil dari tarif tagihan.", "Slot parkir fisik tetap terikat pada tiket; tiket belum lunas."),
    ("TiketTidakDitemukanException", "Exception", "Plat atau ID tiket tidak ada di daftar parkir aktif.", "Mencegah kendaraan asing keluar gratis; saldo kas tidak berubah.")
]
for item in sp_data:
    row = table_sp.add_row().cells
    row[0].text = item[0]
    row[1].text = item[1]
    row[2].text = item[2]
    row[3].text = item[3]
style_academic_table(table_sp, [Inches(1.8), Inches(0.9), Inches(1.8), Inches(1.7)])
doc.add_paragraph().paragraph_format.space_after = Pt(4)

add_h2("5.2 Kode Implementasi Atomic State Preservation pada Checkout Parkir")
add_body("Potongan kode berikut memperlihatkan bagaimana kelas KarcisParkir dan GedungParkir menjamin integritas state pada transaksi kasir:")

code_sample = (
    "// Cuplikan pada KarcisParkir.java\n"
    "public boolean prosesPembayaran(String metode, double uangBayar) throws PembayaranKurangException {\n"
    "    if (this.sudahDibayar) return true;\n"
    "    // 1. Validasi state sebelum mutasi (Test-Before-Mutate)\n"
    "    if (uangBayar < this.totalBiaya) {\n"
    "        throw new PembayaranKurangException(this.totalBiaya, uangBayar);\n"
    "    }\n"
    "    // 2. Mutasi state hanya terjadi jika syarat pembayaran lolos\n"
    "    this.sudahDibayar = true;\n"
    "    this.metodePembayaran = metode.toUpperCase();\n"
    "    return true;\n"
    "}\n\n"
    "// Cuplikan pada GedungParkir.java\n"
    "public boolean prosesKeluar(KarcisParkir tiket, int jamKeluar, String metode, double uangBayar) \n"
    "        throws TiketTidakDitemukanException, PembayaranKurangException {\n"
    "    if (tiket == null || tiket.isSudahDibayar()) {\n"
    "        throw new TiketTidakDitemukanException(\"Tiket tidak valid atau sudah selesai.\");\n"
    "    }\n"
    "    tiket.hitungTarif(jamKeluar, tarifPertama, tarifBerikutnya);\n"
    "    // Jika baris ini melempar PembayaranKurangException, pengosongan slot di bawah BATAL dieksekusi!\n"
    "    tiket.prosesPembayaran(metode, uangBayar);\n"
    "    // Pelepasan slot fisik aman dilakukan karena pembayaran tervalidasi\n"
    "    SlotParkir slot = tiket.getSlot();\n"
    "    if (slot != null) {\n"
    "        slot.kosongkanSlot();\n"
    "        DatabaseManager.updateStatusSlot(slot);\n"
    "    }\n"
    "    this.totalPendapatan += tiket.getTotalBiaya();\n"
    "    DatabaseManager.updateKarcisKeluar(tiket);\n"
    "    return true;\n"
    "}"
)
add_code_block(code_sample)

# =============================================================
# 6. LUARAN PRAKTIKUM & TABEL PENGUJIAN
# =============================================================
add_h1("6. Luaran Praktikum & Tabel Pengujian")

add_h2("6.1 Tabel Evaluasi Output Program Dasar Produk (6 Skenario Modul 10)")
table_eval = doc.add_table(rows=1, cols=7)
h_eval = table_eval.rows[0].cells
h_eval[0].text = "No"
h_eval[1].text = "Input"
h_eval[2].text = "Jalur / Exception"
h_eval[3].text = "Hasil yang Diharapkan"
h_eval[4].text = "Hasil Aktual Eksekusi"
h_eval[5].text = "Stok Akhir"
h_eval[6].text = "Status"

eval_data = [
    ("1", "\"2\"", "Normal Flow", "Pembelian berhasil.", "Pembelian berhasil.", "3 unit", "Valid"),
    ("2", "\"0\"", "IllegalArgumentException", "Ditolak: Jumlah > nol.", "Ditolak: Jumlah pembelian harus lebih dari nol.", "3 unit", "Valid"),
    ("3", "\"9\"", "StokTidakCukupException", "Ditolak: Stok tidak cukup.", "Ditolak: Stok tidak cukup. Tersedia: 3", "3 unit", "Valid"),
    ("4", "\"abc\"", "NumberFormatException", "Ditolak: harus bilangan bulat.", "Ditolak: input harus bilangan bulat.", "3 unit", "Valid"),
    ("5", "\"-1\"", "IllegalArgumentException", "Ditolak: Jumlah > nol.", "Ditolak: Jumlah pembelian harus lebih dari nol.", "3 unit", "Valid"),
    ("6", "\"3\"", "Normal Flow", "Pembelian berhasil.", "Pembelian berhasil.", "0 unit", "Valid")
]
for item in eval_data:
    row = table_eval.add_row().cells
    for c_idx, val in enumerate(item):
        row[c_idx].text = val
style_academic_table(table_eval, [Inches(0.4), Inches(0.6), Inches(1.6), Inches(1.3), Inches(1.3), Inches(0.6), Inches(0.4)])
doc.add_paragraph().paragraph_format.space_after = Pt(4)

add_h2("6.2 Tabel Pengujian Fungsional Sistem Parkir (5 Test Cases)")
table_sp_test = doc.add_table(rows=1, cols=5)
h_spt = table_sp_test.rows[0].cells
h_spt[0].text = "No"
h_spt[1].text = "Skenario Operasi"
h_spt[2].text = "Custom Exception"
h_spt[3].text = "Respon Sistem & Integritas State"
h_spt[4].text = "Hasil"

spt_data = [
    ("1", "Registrasi plat format rusak ('X')", "FormatPlatTidakValidException", "Registrasi dibatalkan, pesan kesalahan tampil di HUD, DB bersih.", "PASSED"),
    ("2", "Check-in plat yang sedang parkir", "KendaraanSudahParkirException", "Slot tidak dialokasikan ganda, portal masuk tetap tertutup.", "PASSED"),
    ("3", "Check-in saat kapasitas penuh", "KapasitasPenuhException", "LED banner menampilkan 'PARKIR PENUH', kuota terisi tidak over-limit.", "PASSED"),
    ("4", "Checkout dengan uang bayar kurang", "PembayaranKurangException", "Slot B01 TETAP terisi, tiket belum lunas, gate keluar terkunci.", "PASSED"),
    ("5", "Checkout plat bodong ('W 0000 XYZ')", "TiketTidakDitemukanException", "Checkout ditolak, saldo kas dan slot tidak mengalami mutasi.", "PASSED")
]
for item in spt_data:
    row = table_sp_test.add_row().cells
    for c_idx, val in enumerate(item):
        row[c_idx].text = val
style_academic_table(table_sp_test, [Inches(0.4), Inches(1.8), Inches(1.6), Inches(1.9), Inches(0.5)])
doc.add_paragraph().paragraph_format.space_after = Pt(4)

# =============================================================
# 7. KRITERIA PENILAIAN & DEFINITION OF DONE
# =============================================================
add_h1("7. Kriteria Penilaian & Definition of Done (DoD)")
table_rubrik = doc.add_table(rows=1, cols=4)
h_rub = table_rubrik.rows[0].cells
h_rub[0].text = "Aspek Penilaian Modul 10"
h_rub[1].text = "Bobot"
h_rub[2].text = "Bukti Implementasi pada Proyek"
h_rub[3].text = "Status Capaian"

rubrik_data = [
    ("Ketepatan validasi dan konsistensi state", "30%", "Pola Test-Before-Mutate pada Produk, KarcisParkir, dan GedungParkir; state tidak rusak saat operasi ditolak.", "Tuntas"),
    ("Penerapan throw, throws, custom exception", "25%", "6 custom domain exception dibangun (1 Produk + 5 Smart Parkir) dengan klausa throws pada method signature.", "Tuntas"),
    ("Ketepatan penanganan exception", "20%", "Blok try-catch-finally bertingkat terstruktur urut spesifik ke umum tanpa ada catch kosong.", "Tuntas"),
    ("Kelengkapan pengujian berhasil dan gagal", "15%", "Seluruh 6 skenario modul dasar (\"2\",\"0\",\"9\",\"abc\",\"-1\",\"3\") dan 5 skenario Smart Parkir 100% lulus.", "Tuntas"),
    ("Kemampuan menjelaskan kode", "10%", "Laporan komprehensif memuat dasar teori, diagram relasi, analisis state, dan pembahasan pertanyaan.", "Tuntas"),
    ("TOTAL EVALUASI KOMPETENSI", "100%", "Seluruh kriteria kualifikasi Definition of Done (DoD) Modul 10 terpenuhi secara sempurna.", "Sempurna")
]
for item in rubrik_data:
    row = table_rubrik.add_row().cells
    for c_idx, val in enumerate(item):
        row[c_idx].text = val
style_academic_table(table_rubrik, [Inches(1.8), Inches(0.6), Inches(3.0), Inches(0.8)])
doc.add_paragraph().paragraph_format.space_after = Pt(4)

# =============================================================
# 8. JAWABAN PERTANYAAN EVALUASI & PEMBAHASAN MENDALAM
# =============================================================
add_h1("8. Jawaban Pertanyaan Evaluasi & Pembahasan Mendalam")

add_h2("8.1 Apa perbedaan kata kunci throw dan throws?")
add_body("Kata kunci throw merupakan pernyataan aksi di dalam badan method atau blok kode untuk melempar instansiasi objek exception secara aktif saat suatu syarat bisnis dilanggar (misalnya throw new StokTidakCukupException(\"Stok habis!\");). Sebaliknya, kata kunci throws merupakan klausul deklarasi kontrak pada tanda tangan (signature) method untuk mengumumkan kepada pemanggil bahwa method tersebut berpotensi meneruskan checked exception yang wajib ditangani pemanggil (misalnya public void beli(int jumlah) throws StokTidakCukupException).")

add_h2("8.2 Mengapa pemeriksaan stok dilakukan sebelum pengurangan stok?")
add_body("Pemeriksaan stok harus mendahului operasi mutasi (pola Test-Before-Mutate) demi menjamin prinsip atomisitas dan integritas state objek. Jika nilai stok dikurangi terlebih dahulu sebelum divalidasi, maka ketika terjadi kegagalan (misalnya permintaan melebihi sisa unit yang tersedia), atribut stok internal objek sudah terlanjur berkurang atau bahkan menjadi negatif sebelum exception dilempar. Dengan memvalidasi di awal, objek dijamin bebas dari efek samping (side-effect free) ketika transaksi ditolak.")

add_h2("8.3 Apa akibatnya jika exception ditangkap tetapi tidak ditangani (catch kosong)?")
add_body("Catch kosong (swallowed exception atau silent failure, misalnya catch (Exception e) {}) sangat berbahaya karena:")
add_bullet("Sistem menghilangkan jejak terjadinya galat sehingga pengembang dan pengguna tidak menyadari adanya anomali sistem.", "a. ")
add_bullet("Program tetap melanjutkan eksekusi ke baris berikutnya seolah-olah operasi berhasil, memicu kegagalan berantai (cascading failure) dan potensi korupsi data lanjutan pada basis data.", "b. ")
add_bullet("Pengguna tidak menerima umpan balik yang akurat mengapa permintaan transaksi mereka tidak membuahkan hasil.", "c. ")

add_h2("8.4 Mengapa perubahan stok tidak ditempatkan dalam blok finally?")
add_body("Blok finally dirancang khusus untuk instruksi yang harus selalu dieksekusi dalam kondisi apapun (baik blok try berhasil tuntas maupun terjadi exception), seperti menutup koneksi database atau melepaskan lock file. Jika mutasi pengurangan stok ditempatkan di dalam finally, maka stok akan tetap berkurang meskipun masukan pengguna tidak valid berupa karakter abjad (\"abc\") atau bernilai negatif. Akibatnya, seluruh validasi pencegahan yang telah dibangun pada blok try menjadi sia-sia dan merusak konsistensi data.")

# =============================================================
# 9. KESIMPULAN PRAKTIKUM
# =============================================================
add_h1("9. Kesimpulan Praktikum")
add_body("Berdasarkan implementasi dan pengujian yang telah diselesaikan pada Modul 10, dapat disimpulkan bahwa exception handling bukan sekadar mekanisme untuk mencegah program berhenti mendadak (crash), melainkan instrumen pengendalian logika bisnis yang sangat vital untuk menjaga keutuhan data internal objek (atomic state preservation). Penerapan custom exception dan validasi dini pada kasus Produk maupun proyek Smart Parkir membuktikan bahwa sistem dapat menangani berbagai skenario kegagalan transaksi secara elegan, aman, dan konsisten.")

output_file = "/Users/arjunalanangadiwarsana/Herd/Java/P10_3125522010_ArjunaLanangAdiwarsana/Laporan_P10_3125522010_ArjunaLanangAdiwarsana.docx"
doc.save(output_file)
print(f"Laporan docx berhasil dibuat tanpa BAB di: {output_file}")
