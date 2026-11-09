import zipfile
import xml.etree.ElementTree as ET
import os

def escape_xml(text):
    return (text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace('"', "&quot;")
                .replace("'", "&apos;"))

def p_title(text):
    return f"""<w:p>
      <w:pPr>
        <w:pStyle w:val="Title"/>
        <w:spacing w:before="240" w:after="120"/>
        <w:jc w:val="center"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
          <w:b/>
          <w:sz w:val="36"/>
          <w:color w:val="000000"/>
        </w:rPr>
        <w:t>{escape_xml(text)}</w:t>
      </w:r>
    </w:p>"""

def p_subtitle(text):
    return f"""<w:p>
      <w:pPr>
        <w:spacing w:before="0" w:after="240"/>
        <w:jc w:val="center"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
          <w:sz w:val="24"/>
          <w:color w:val="000000"/>
          <w:b/>
        </w:rPr>
        <w:t>{escape_xml(text)}</w:t>
      </w:r>
    </w:p>"""

def p_h1(text):
    return f"""<w:p>
      <w:pPr>
        <w:pStyle w:val="Heading1"/>
        <w:spacing w:before="320" w:after="140"/>
        <w:pBdr>
          <w:bottom w:val="single" w:sz="8" w:space="4" w:color="000000"/>
        </w:pBdr>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
          <w:b/>
          <w:sz w:val="28"/>
          <w:color w:val="000000"/>
        </w:rPr>
        <w:t>{escape_xml(text)}</w:t>
      </w:r>
    </w:p>"""

def p_h2(text):
    return f"""<w:p>
      <w:pPr>
        <w:pStyle w:val="Heading2"/>
        <w:spacing w:before="220" w:after="80"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
          <w:b/>
          <w:sz w:val="24"/>
          <w:color w:val="000000"/>
        </w:rPr>
        <w:t>{escape_xml(text)}</w:t>
      </w:r>
    </w:p>"""

def p_body(text, bold_prefix=None, italic=False):
    bp_xml = ""
    if bold_prefix:
        bp_xml = f"""<w:r>
          <w:rPr>
            <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
            <w:b/>
            <w:sz w:val="22"/>
            <w:color w:val="000000"/>
          </w:rPr>
          <w:t xml:space="preserve">{escape_xml(bold_prefix)} </w:t>
        </w:r>"""
    
    rpr_italic = "<w:i/>" if italic else ""
    return f"""<w:p>
      <w:pPr>
        <w:spacing w:after="140" w:line="276" w:lineRule="auto"/>
        <w:jc w:val="both"/>
      </w:pPr>
      {bp_xml}
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
          {rpr_italic}
          <w:sz w:val="22"/>
          <w:color w:val="000000"/>
        </w:rPr>
        <w:t>{escape_xml(text)}</w:t>
      </w:r>
    </w:p>"""

def p_bullet(text, bold_prefix=None):
    bp_xml = ""
    if bold_prefix:
        bp_xml = f"""<w:r>
          <w:rPr>
            <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
            <w:b/>
            <w:sz w:val="22"/>
            <w:color w:val="000000"/>
          </w:rPr>
          <w:t xml:space="preserve">{escape_xml(bold_prefix)} </w:t>
        </w:r>"""
    return f"""<w:p>
      <w:pPr>
        <w:numPr>
          <w:ilvl w:val="0"/>
          <w:numId w:val="1"/>
        </w:numPr>
        <w:spacing w:after="100" w:line="260" w:lineRule="auto"/>
        <w:ind w:left="480" w:hanging="280"/>
        <w:jc w:val="both"/>
      </w:pPr>
      {bp_xml}
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
          <w:sz w:val="22"/>
          <w:color w:val="000000"/>
        </w:rPr>
        <w:t>{escape_xml(text)}</w:t>
      </w:r>
    </w:p>"""

def p_table_metadata():
    rows_data = [
        ("Mata Kuliah", "Praktikum Pemrograman Berorientasi Obyek (PBO) — Modul 9"),
        ("Topik Proyek", "Implementasi UML Class Diagram ke Program Java (SmartParkir)"),
        ("Nama Mahasiswa", "Arjuna Lanang Adiwarsana"),
        ("NRP", "3125522010"),
        ("Kelas / Prodi", "3D3 IT-PSDKU A / D3 Teknik Informatika"),
        ("Institusi", "Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep"),
        ("Dosen Pengampu", "Nirwana Haidar Hari, S.Pd., M.Kom.")
    ]
    xml = """<w:tbl>
      <w:tblPr>
        <w:tblW w:w="9600" w:type="dxa"/>
        <w:tblBorders>
          <w:top w:val="single" w:sz="8" w:space="0" w:color="000000"/>
          <w:left w:val="single" w:sz="8" w:space="0" w:color="000000"/>
          <w:bottom w:val="single" w:sz="8" w:space="0" w:color="000000"/>
          <w:right w:val="single" w:sz="8" w:space="0" w:color="000000"/>
          <w:insideH w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
          <w:insideV w:val="none"/>
        </w:tblBorders>
        <w:tblCellMar>
          <w:top w:w="120" w:type="dxa"/>
          <w:left w:w="160" w:type="dxa"/>
          <w:bottom w:w="120" w:type="dxa"/>
          <w:right w:w="160" w:type="dxa"/>
        </w:tblCellMar>
      </w:tblPr>"""
    for label, val in rows_data:
        xml += f"""
      <w:tr>
        <w:tc>
          <w:tcPr>
            <w:tcW w:w="2800" w:type="dxa"/>
            <w:shd w:val="clear" w:color="auto" w:fill="F5F5F5"/>
          </w:tcPr>
          <w:p>
            <w:pPr><w:spacing w:after="0"/></w:pPr>
            <w:r>
              <w:rPr>
                <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
                <w:b/>
                <w:sz w:val="20"/>
                <w:color w:val="000000"/>
              </w:rPr>
              <w:t>{escape_xml(label)}</w:t>
            </w:r>
          </w:p>
        </w:tc>
        <w:tc>
          <w:tcPr>
            <w:tcW w:w="6800" w:type="dxa"/>
            <w:shd w:val="clear" w:color="auto" w:fill="FFFFFF"/>
          </w:tcPr>
          <w:p>
            <w:pPr><w:spacing w:after="0"/></w:pPr>
            <w:r>
              <w:rPr>
                <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
                <w:sz w:val="20"/>
                <w:color w:val="000000"/>
              </w:rPr>
              <w:t>{escape_xml(val)}</w:t>
            </w:r>
          </w:p>
        </w:tc>
      </w:tr>"""
    xml += "\n    </w:tbl>"
    return xml

def p_table_class_overview():
    headers = ["Class Domain", "Tipe & Relasi UML", "Tanggung Jawab Utama (Responsibility)", "Proteksi Enkapsulasi & State"]
    rows = [
        ("Kendaraan", "Entitas Model\n(Agregasi target oleh KarcisParkir)", "Menyimpan identitas kendaraan (platNomor, tipe, merk) dan menyediakan penandaan jenis mobil listrik via method isMobilListrik().", "Atribut private, data bersifat immutable setelah instansiasi konstruktor."),
        ("SlotParkir", "Komponen Fisik\n(Komposisi dari GedungParkir 1..*)", "Merepresentasikan petak fisik per lantai, memverifikasi kesesuaian tipe kendaraan (cocokUntuk), serta mengunci/melepaskan status terisi.", "Mutasi status terkunci via isiSlot() dan kosongkanSlot(); dilarang ada setter liar tanpa objek kendaraan valid."),
        ("KarcisParkir", "Item Transaksi\n(Agregasi ke Kendaraan & Slot)", "Merekam siklus parkir, jam masuk/keluar, menghitung durasi, mengalkulasi tarif progresif, dan diskon 20% bagi kendaraan EV.", "Status pelunasan (sudahDibayar) dikunci via prosesPembayaran(); mencatat metode pembayaran dan total biaya riil."),
        ("GedungParkir", "Aggregate Root\n(Komposisi ke Slot & Transaksi)", "Mengendalikan seluruh alur bisnis: alokasi slot terdekat berbasis lantai, otorisasi gerbang masuk/keluar, dan akumulasi total kas pendapatan.", "Daftar slot dan transaksi berstatus private list; gerbang keluar memblokir kendaraan jika tiket belum lunas.")
    ]
    xml = """<w:tbl>
      <w:tblPr>
        <w:tblW w:w="9600" w:type="dxa"/>
        <w:tblBorders>
          <w:top w:val="single" w:sz="8" w:space="0" w:color="000000"/>
          <w:left w:val="single" w:sz="8" w:space="0" w:color="000000"/>
          <w:bottom w:val="single" w:sz="8" w:space="0" w:color="000000"/>
          <w:right w:val="single" w:sz="8" w:space="0" w:color="000000"/>
          <w:insideH w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
          <w:insideV w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
        </w:tblBorders>
        <w:tblCellMar>
          <w:top w:w="120" w:type="dxa"/>
          <w:left w:w="140" w:type="dxa"/>
          <w:bottom w:w="120" w:type="dxa"/>
          <w:right w:w="140" w:type="dxa"/>
        </w:tblCellMar>
      </w:tblPr>
      <w:tr>"""
    col_widths = [1600, 2200, 3200, 2600]
    for i, h in enumerate(headers):
        xml += f"""
        <w:tc>
          <w:tcPr>
            <w:tcW w:w="{col_widths[i]}" w:type="dxa"/>
            <w:shd w:val="clear" w:color="auto" w:fill="E5E5E5"/>
          </w:tcPr>
          <w:p>
            <w:pPr><w:jc w:val="center"/><w:spacing w:after="0"/></w:pPr>
            <w:r>
              <w:rPr>
                <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
                <w:b/>
                <w:sz w:val="18"/>
                <w:color w:val="000000"/>
              </w:rPr>
              <w:t>{escape_xml(h)}</w:t>
            </w:r>
          </w:p>
        </w:tc>"""
    xml += "\n      </w:tr>"
    for c_name, c_rel, c_resp, c_enc in rows:
        xml += f"""
      <w:tr>
        <w:tc>
          <w:tcPr><w:tcW w:w="{col_widths[0]}" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="FFFFFF"/></w:tcPr>
          <w:p><w:pPr><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="18"/><w:color w:val="000000"/></w:rPr><w:t>{escape_xml(c_name)}</w:t></w:r></w:p>
        </w:tc>
        <w:tc>
          <w:tcPr><w:tcW w:w="{col_widths[1]}" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="FFFFFF"/></w:tcPr>
          <w:p><w:pPr><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="000000"/></w:rPr><w:t>{escape_xml(c_rel)}</w:t></w:r></w:p>
        </w:tc>
        <w:tc>
          <w:tcPr><w:tcW w:w="{col_widths[2]}" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="FFFFFF"/></w:tcPr>
          <w:p><w:pPr><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="000000"/></w:rPr><w:t>{escape_xml(c_resp)}</w:t></w:r></w:p>
        </w:tc>
        <w:tc>
          <w:tcPr><w:tcW w:w="{col_widths[3]}" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="FFFFFF"/></w:tcPr>
          <w:p><w:pPr><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="000000"/></w:rPr><w:t>{escape_xml(c_enc)}</w:t></w:r></w:p>
        </w:tc>
      </w:tr>"""
    xml += "\n    </w:tbl>"
    return xml

def build_docx(filename="Laporan_SmartParkir_P9.docx"):
    content_types = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>
</Types>"""

    rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

    doc_rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml"/>
</Relationships>"""

    numbering = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:abstractNum w:abstractNumId="0">
    <w:lvl w:ilvl="0">
      <w:start w:val="1"/>
      <w:numFmt w:val="bullet"/>
      <w:lvlText w:val="•"/>
      <w:lvlJc w:val="left"/>
      <w:pPr>
        <w:ind w:left="480" w:hanging="280"/>
      </w:pPr>
      <w:rPr>
        <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
        <w:color w:val="000000"/>
        <w:b/>
      </w:rPr>
    </w:lvl>
  </w:abstractNum>
  <w:num w:numId="1">
    <w:abstractNumId w:val="0"/>
  </w:num>
</w:numbering>"""

    styles = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
        <w:sz w:val="22"/>
        <w:szCs w:val="22"/>
        <w:color w:val="000000"/>
        <w:lang w:val="id-ID"/>
      </w:rPr>
    </w:rPrDefault>
    <w:pPrDefault>
      <w:pPr>
        <w:spacing w:after="140" w:line="276" w:lineRule="auto"/>
      </w:pPr>
    </w:pPrDefault>
  </w:docDefaults>
  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:basedOn w:val="Normal"/>
    <w:rPr>
      <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
      <w:b/>
      <w:sz w:val="28"/>
      <w:color w:val="000000"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/>
    <w:basedOn w:val="Normal"/>
    <w:rPr>
      <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
      <w:b/>
      <w:sz w:val="24"/>
      <w:color w:val="000000"/>
    </w:rPr>
  </w:style>
</w:styles>"""

    # Assemble Document Body
    body_elements = []
    
    # Header & Meta
    body_elements.append(p_title("LAPORAN PRAKTIKUM MODUL 9"))
    body_elements.append(p_subtitle("IMPLEMENTASI UML CLASS DIAGRAM KE PROGRAM JAVA"))
    body_elements.append(p_table_metadata())
    body_elements.append(p_body(""))
    
    # 1. Judul
    body_elements.append(p_h1("1. Judul"))
    body_elements.append(p_body("Sistem Manajemen Parkir Gedung Bertingkat dan Alokasi Slot Otomatis Berbasis Pemrograman Berorientasi Objek (SmartParkir)."))
    
    # 2. Permasalahan
    body_elements.append(p_h1("2. Permasalahan"))
    body_elements.append(p_body("Pengelolaan fasilitas parkir gedung bertingkat seringkali menghadapi sejumlah kendala operasional yang berdampak langsung pada kelancaran sirkulasi kendaraan dan akurasi pendapatan fasilitas:"))
    
    body_elements.append(p_bullet(
        "Pada sistem manual, pengemudi memasuki area gedung tanpa mengetahui lantai dan blok mana yang memiliki kapasitas kosong. Hal ini memicu antrean panjang di jalur sirkulasi utama (aisle) serta penumpukan kendaraan di pintu gerbang masuk saat pengemudi berhenti untuk mencari informasi ketersediaan petak parkir.",
        bold_prefix="a. Ketidakteraturan Alokasi dan Penumpukan Antrean di Gedung Bertingkat:"
    ))
    body_elements.append(p_bullet(
        "Ketiadaan mekanisme validasi fisik otomatis menyebabkan pelanggaran alokasi, seperti slot khusus kendaraan listrik (EV) yang dilengkapi charging station ditempati oleh mobil berbahan bakar minyak, atau sepeda motor yang menggunakan petak mobil reguler sehingga mengurangi kapasitas efektif gedung secara drastis.",
        bold_prefix="b. Pelanggaran Peruntukan Kategori Slot (Misalokasi EV & Reguler):"
    ))
    body_elements.append(p_bullet(
        "Perhitungan durasi dan biaya parkir berbasis pencatatan kertas atau input kasir manual rentan terhadap salah hitung (human error), pemalsuan karcis, hilangnya tiket saat kendaraan berada di dalam gedung, serta ketiadaan skema penegakan tarif bertingkat (flat jam pertama + jam berikutnya) dan potongan insentif hijau secara akurat.",
        bold_prefix="c. Kerentanan Tarif dan Discrepancy Kas Pendapatan:"
    ))
    body_elements.append(p_bullet(
        "Pada sistem yang tidak menerapkan prinsip enkapsulasi kuat (anemic model), status slot sering tidak sinkron dengan kondisi fisik aktual. Kerentanan ini memicu terjadinya double booking slot atau kendaraan diizinkan keluar melalui portal tanpa proses verifikasi pelunasan tiket terlebih dahulu.",
        bold_prefix="d. Inkonsistensi Status Okupansi (Anemic State) dan Risiko Pelarian Gate:"
    ))
    
    # 3. Solusi
    body_elements.append(p_h1("3. Solusi"))
    body_elements.append(p_body("Untuk mengatasi persoalan di atas, proyek SmartParkir dirancang menggunakan paradigma Pemrograman Berorientasi Objek (PBO) murni di Java dengan pembagian tanggung jawab kelas yang terisolasi dan konsisten dengan diagram UML:"))
    
    body_elements.append(p_bullet(
        "Arsitektur sistem dibangun di atas 4 kelas domain utama: Kendaraan (entitas data kendaraan), SlotParkir (entitas fisik petak parkir), KarcisParkir (entitas transaksi dan tarif), dan GedungParkir yang bertindak sebagai Aggregate Root pengontrol seluruh gerbang dan daftar slot.",
        bold_prefix="a. Penerapan Arsitektur Domain Model Berorientasi Objek:"
    ))
    body_elements.append(p_bullet(
        "Metode alokasi cerdas pada GedungParkir.cariSlotTersedia() secara deterministik mencari slot kosong dari lantai terendah yang cocok dengan tipe kendaraan. Jika slot khusus EV pada lantai dasar terisi penuh, algoritma secara adaptif mengalokasikan slot mobil reguler yang masih tersedia tanpa menolak pengguna.",
        bold_prefix="b. Algoritma Alokasi Slot Deterministik Berbasis Kompatibilitas:"
    ))
    body_elements.append(p_bullet(
        "Seluruh atribut berstatus private. Perubahan status okupansi slot hanya dapat terjadi melalui pemanggilan metode isiSlot(Kendaraan k) dan kosongkanSlot(), menjamin tidak adanya slot yang bertanda terisi tanpa adanya objek kendaraan fisik nyata.",
        bold_prefix="c. Penegakan Enkapsulasi Ketat dan Validasi State:"
    ))
    body_elements.append(p_bullet(
        "Kelas KarcisParkir secara otomatis menghitung durasi parkir aktual dan mengalkulasi tarif bertingkat (jam pertama flat + tarif jam berikutnya). Kendaraan listrik (EV) secara otomatis memperoleh diskon insentif hijau sebesar 20% dari total biaya transaksi.",
        bold_prefix="d. Mekanisme Tarif Progresif Terotomatisasi dan Insentif Hijau (EV):"
    ))
    body_elements.append(p_bullet(
        "Metode prosesKeluar() pada GedungParkir mengunci gerbang palang: kendaraan dilarang keluar apabila transaksi karcis belum diverifikasi lunas (sudahDibayar == false). Setelah pembayaran valid, slot otomatis dikosongkan dan pendapatan kas bertambah.",
        bold_prefix="e. Validasi Siklus Transaksi Gate Masuk-Keluar:"
    ))
    body_elements.append(p_bullet(
        "Data operasional dipersistensikan ke dalam skema database relasional MySQL melalui DatabaseManager, divisualisasikan secara realtime melalui aplikasi desktop AdminGUI (Java Swing) dengan denah blueprint interaktif, serta didukung modul AIVisionService untuk deteksi plat nomor otomatis.",
        bold_prefix="f. Persistensi Data Relasional dan Antarmuka Monitoring Real-Time:"
    ))
    
    body_elements.append(p_h2("Ringkasan Pemetaan Class UML dan Relasi Proyek"))
    body_elements.append(p_table_class_overview())
    body_elements.append(p_body(""))
    
    # 4. Gambaran Singkat
    body_elements.append(p_h1("4. Gambaran Singkat"))
    body_elements.append(p_body("Secara operasional end-to-end, sistem SmartParkir menjalankan siklus pengelolaan parkir melalui tiga tahapan terintegrasi:"))
    
    body_elements.append(p_bullet(
        "Ketika kendaraan tiba di gerbang masuk, operator atau kamera cerdas mencatat plat nomor, tipe, dan merk kendaraan. Sistem menjalankan alokasi otomatis untuk mencocokkan slot terdekat yang kompatibel. Karcis parkir resmi diterbitkan berisi nomor tiket unik, slot yang dialokasikan, dan jam masuk. Seketika itu pula, status slot fisik dikunci menjadi 'terisi' dan data disimpan ke database.",
        bold_prefix="1. Tahap Check-In & Alokasi Slot Otomatis:"
    ))
    body_elements.append(p_bullet(
        "Petugas dan manajemen dapat memantau pergerakan okupansi secara langsung melalui denah blueprint interaktif (Java Swing) dan dashboard web. Tiap petak parkir menampilkan status ketersediaan secara transparan, mencegah terjadinya asimetri informasi antara petugas gerbang dan pengemudi.",
        bold_prefix="2. Tahap Monitoring Real-Time & Tata Kelola Okupansi:"
    ))
    body_elements.append(p_bullet(
        "Saat kendaraan menuju gerbang keluar, tiket diserahkan dan diverifikasi oleh sistem. Durasi parkir dihitung secara presisi sesuai selisih jam keluar dan jam masuk. Sistem menghitung total tagihan dengan formula progresif serta menerapkan pemotongan diskon 20% bagi unit EV. Pengemudi melakukan pelunasan biaya (tunai/non-tunai). Setelah pembayaran dinyatakan lunas, palang keluar dibuka, slot fisik dikembalikan ke status 'tersedia', dan total pendapatan kas gedung diperbarui.",
        bold_prefix="3. Tahap Check-Out, Audit Pembayaran & Pembebasan Slot:"
    ))
    
    # Wrap in w:document
    doc_xml = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    {"".join(body_elements)}
    <w:sectPr>
      <w:pgSz w:w="12240" w:h="15840"/>
      <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/>
    </w:sectPr>
  </w:body>
</w:document>"""

    # Validate XML
    ET.fromstring(doc_xml)
    ET.fromstring(styles)
    ET.fromstring(numbering)
    ET.fromstring(content_types)
    ET.fromstring(rels)
    ET.fromstring(doc_rels)
    
    with zipfile.ZipFile(filename, 'w', zipfile.ZIP_DEFLATED) as docx:
        docx.writestr('[Content_Types].xml', content_types)
        docx.writestr('_rels/.rels', rels)
        docx.writestr('word/_rels/document.xml.rels', doc_rels)
        docx.writestr('word/styles.xml', styles)
        docx.writestr('word/numbering.xml', numbering)
        docx.writestr('word/document.xml', doc_xml)
    
    print(f"File DOCX berhasil diperbarui: {filename} ({os.path.getsize(filename)} bytes)")

if __name__ == "__main__":
    build_docx()
