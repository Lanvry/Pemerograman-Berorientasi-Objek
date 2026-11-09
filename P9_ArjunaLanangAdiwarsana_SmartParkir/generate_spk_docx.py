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
        <w:spacing w:before="240" w:after="100"/>
        <w:jc w:val="center"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
          <w:b/>
          <w:sz w:val="34"/>
          <w:color w:val="000000"/>
        </w:rPr>
        <w:t>{escape_xml(text)}</w:t>
      </w:r>
    </w:p>"""

def p_subtitle(text):
    return f"""<w:p>
      <w:pPr>
        <w:spacing w:before="0" w:after="220"/>
        <w:jc w:val="center"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
          <w:sz w:val="22"/>
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
        <w:spacing w:before="300" w:after="140"/>
        <w:pBdr>
          <w:bottom w:val="single" w:sz="8" w:space="4" w:color="000000"/>
        </w:pBdr>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
          <w:b/>
          <w:sz w:val="26"/>
          <w:color w:val="000000"/>
        </w:rPr>
        <w:t>{escape_xml(text)}</w:t>
      </w:r>
    </w:p>"""

def p_h2(text):
    return f"""<w:p>
      <w:pPr>
        <w:pStyle w:val="Heading2"/>
        <w:spacing w:before="200" w:after="80"/>
      </w:pPr>
      <w:r>
        <w:rPr>
          <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
          <w:b/>
          <w:sz w:val="22"/>
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
        ("Mata Kuliah", "Sistem Pendukung Keputusan (SPK)"),
        ("Tugas", "Tugas 6 : Rencana Project"),
        ("Pendekatan / Tema", "Project-Based Learning (PBL)"),
        ("Studi Kasus", "Optimasi Manajemen Alokasi Ruang Fasilitas Parkir Gedung Bertingkat"),
        ("Nama Mahasiswa", "Arjuna Lanang Adiwarsana"),
        ("NRP", "3125522010"),
        ("Kelas / Prodi", "3 D3PSDKU-SM IT Kelas A / D3 Teknik Informatika"),
        ("Institusi", "Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep")
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

def p_table_criteria():
    headers = ["Kode", "Kriteria Keputusan", "Karakter", "Bobot", "Parameter Pengukuran Lapangan"]
    rows = [
        ("C1", "Jarak ke Titik Akses Utama (Lift / Pintu)", "Cost", "30%", "Jarak tempuh berjalan kaki (meter) dari petak ke akses keluar gedung."),
        ("C2", "Posisi Elevasi Lantai Parkir", "Cost", "25%", "Level lantai (Lantai 1, Lantai 2, dst) untuk efisiensi waktu manuver rampa."),
        ("C3", "Kesesuaian Tipe & Fasilitas Kendaraan", "Benefit", "25%", "Prioritas alokasi fasilitas khusus (stasiun pengisian daya EV / zonasi motor)."),
        ("C4", "Tingkat Kepadatan Sirkulasi Zona/Lorong", "Cost", "20%", "Persentase keterisian petak pada lorong terkait untuk menghindari kemacetan.")
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
    col_widths = [1000, 2700, 1400, 1200, 3300]
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
    for c_code, c_name, c_type, c_w, c_desc in rows:
        xml += f"""
      <w:tr>
        <w:tc>
          <w:tcPr><w:tcW w:w="{col_widths[0]}" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="FFFFFF"/></w:tcPr>
          <w:p><w:pPr><w:jc w:val="center"/><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="18"/><w:color w:val="000000"/></w:rPr><w:t>{escape_xml(c_code)}</w:t></w:r></w:p>
        </w:tc>
        <w:tc>
          <w:tcPr><w:tcW w:w="{col_widths[1]}" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="FFFFFF"/></w:tcPr>
          <w:p><w:pPr><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:b/><w:sz w:val="18"/><w:color w:val="000000"/></w:rPr><w:t>{escape_xml(c_name)}</w:t></w:r></w:p>
        </w:tc>
        <w:tc>
          <w:tcPr><w:tcW w:w="{col_widths[2]}" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="FFFFFF"/></w:tcPr>
          <w:p><w:pPr><w:jc w:val="center"/><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="000000"/></w:rPr><w:t>{escape_xml(c_type)}</w:t></w:r></w:p>
        </w:tc>
        <w:tc>
          <w:tcPr><w:tcW w:w="{col_widths[3]}" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="FFFFFF"/></w:tcPr>
          <w:p><w:pPr><w:jc w:val="center"/><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="000000"/></w:rPr><w:t>{escape_xml(c_w)}</w:t></w:r></w:p>
        </w:tc>
        <w:tc>
          <w:tcPr><w:tcW w:w="{col_widths[4]}" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="FFFFFF"/></w:tcPr>
          <w:p><w:pPr><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:sz w:val="18"/><w:color w:val="000000"/></w:rPr><w:t>{escape_xml(c_desc)}</w:t></w:r></w:p>
        </w:tc>
      </w:tr>"""
    xml += "\n    </w:tbl>"
    return xml

def build_docx(filename="Tugas_6_Rencana_Project_SPK.docx"):
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
      <w:sz w:val="26"/>
      <w:color w:val="000000"/>
    </w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/>
    <w:basedOn w:val="Normal"/>
    <w:rPr>
      <w:rFonts w:ascii="Arial" w:hAnsi="Arial"/>
      <w:b/>
      <w:sz w:val="22"/>
      <w:color w:val="000000"/>
    </w:rPr>
  </w:style>
</w:styles>"""

    body_elements = []
    
    # Title & Metadata
    body_elements.append(p_title("TUGAS 6 : RENCANA PROJECT"))
    body_elements.append(p_subtitle("SISTEM PENDUKUNG KEPUTUSAN (SPK) BERBASIS PROJECT-BASED LEARNING (PBL)"))
    body_elements.append(p_table_metadata())
    body_elements.append(p_body(""))
    
    # 1. Judul
    body_elements.append(p_h1("- Judul:"))
    body_elements.append(p_body(
        "Rancang Bangun Sistem Pendukung Keputusan Alokasi dan Rekomendasi Slot Parkir Optimal pada Gedung Bertingkat Menggunakan Metode Multi-Attribute Decision Making (MADM) Berbasis Project-Based Learning",
        bold_prefix="Judul Rencana Project (PBL):"
    ))
    
    # 2. Permasalahan
    body_elements.append(p_h1("- Permasalahan:"))
    body_elements.append(p_body(
        "Dalam kerangka Project-Based Learning (PBL), proyek ini berangkat dari permasalahan autentik pada fasilitas parkir gedung bertingkat di area publik dan perkantoran, di mana alur penentuan tempat parkir saat ini masih mengandalkan pencarian acak oleh pengemudi atau arahan manual juru parkir:"))
    
    body_elements.append(p_bullet(
        "Pengemudi dan pengelola menghadapi dilema multi-kriteria yang saling bertolak belakang: meminimalkan jarak tempuh ke pintu lift, memilih lantai terendah untuk menghemat waktu manuver rampa, memastikan ketersediaan stasiun pengisian daya bagi mobil listrik (EV), serta menghindari kemacetan di lorong padat. Tanpa model matematis yang terintegrasi, keputusan alokasi slot berlangsung secara spekulatif dan subjektif.",
        bold_prefix="a. Kompleksitas Trade-Off Multi-Kriteria Riil di Lapangan:"
    ))
    body_elements.append(p_bullet(
        "Ketiadaan sistem pembuat keputusan menyebabkan ketimpangan okupansi: lantai dasar sering mengalami penumpukan kendaraan (bottleneck sirkulasi) sementara kapasitas kosong di lantai atas atau zona samping tidak termanfaatkan secara efektif oleh pengguna.",
        bold_prefix="b. Ketidakefisienan Utilisasi Ruang dan Bottle-Neck Sirkulasi Gedung:"
    ))
    body_elements.append(p_bullet(
        "Sering terjadi misalokasi fasilitas khusus di lapangan, seperti slot parkir yang memiliki charging station EV ditempati oleh mobil konvensional berbahan bakar minyak, karena tidak adanya evaluasi kesesuaian atribut kendaraan terhadap spesifikasi slot saat gerbang masuk.",
        bold_prefix="c. Pelanggaran Peruntukan Fasilitas Khusus (EV Charging Station):"
    ))
    body_elements.append(p_bullet(
        "Terdapat kesenjangan antara teori metode SPK di perkuliahan dengan implementasi nyata pada sistem perangkat lunak yang beroperasi secara real-time. Pengelola gedung membutuhkan instrumen terukur yang mampu merangking alternatif petak kosong dalam hitungan detik saat tiket masuk dicetak.",
        bold_prefix="d. Ketiadaan Implementasi Prototipe Keputusan Terkomputasi Real-Time:"
    ))
    
    # 3. Solusi
    body_elements.append(p_h1("- Solusi:"))
    body_elements.append(p_body(
        "Sebagai luaran utama dari pendekatan Project-Based Learning (PBL), dirancang dan dibangun sebuah produk perangkat lunak Sistem Pendukung Keputusan (SPK) yang menghubungkan pemodelan matematis MADM/MCDM dengan sistem operasional parkir terintegrasi:"))
    
    body_elements.append(p_bullet(
        "Mendefinisikan setiap slot kosong aktual sebagai alternatif keputusan (A1, A2, ..., An) yang dinilai berdasarkan 4 kriteria terukur lapangan: Jarak ke akses pintu/lift (C1 - Cost), Level lantai parkir (C2 - Cost), Kesesuaian tipe kendaraan & fasilitas charger EV (C3 - Benefit), serta Kepadatan lalu lintas lorong (C4 - Cost).",
        bold_prefix="a. Formulasi Model Kriteria dan Alternatif Keputusan Nyata:"
    ))
    body_elements.append(p_bullet(
        "Menerapkan metode perankingan multi-kriteria (seperti SMART / MOORA atau kombinasi AHP-TOPSIS / SAW) untuk menormalisasi matriks keputusan, mengalikan nilai ternormalisasi dengan vektor bobot prioritas, dan menghasilkan skor preferensi akhir guna menentukan ranking alternatif slot terbaik secara objektif.",
        bold_prefix="b. Implementasi Mesin Komputasi Algoritma MADM:"
    ))
    body_elements.append(p_bullet(
        "Menyediakan mekanisme adaptasi bobot secara otomatis sesuai profil kendaraan yang tiba di gate (misal: kendaraan listrik otomatis menaikkan bobot kriteria fasilitas C3 agar diarahkan ke slot charging station).",
        bold_prefix="c. Pembobotan Kriteria Adaptif Berdasarkan Profil Kendaraan:"
    ))
    body_elements.append(p_bullet(
        "Membangun luaran produk PBL berupa prototipe perangkat lunak fungsional yang mencakup modul pengambilan keputusan (decision engine), database relasional status slot, visualisasi monitoring denah blueprint multi-lantai bagi operator, serta integrasi pencetakan kode slot rekomendasi pada karcis parkir masuk.",
        bold_prefix="d. Pembangunan Prototipe Aplikasi Terintegrasi (Luaran Nyata PBL):"
    ))
    
    body_elements.append(p_h2("Tabel Kriteria Penilaian Keputusan Alokasi Slot (PBL)"))
    body_elements.append(p_table_criteria())
    body_elements.append(p_body(""))
    
    # 4. Gambaran Singkat
    body_elements.append(p_h1("- Gambaran Singkat:"))
    body_elements.append(p_body(
        "Pelaksanaan project PBL ini terbagi ke dalam tahapan pengembangan dan operasional sistem sebagai berikut:"))
    
    body_elements.append(p_bullet(
        "Mahasiswa mengidentifikasi alur parkir gedung bertingkat, menentukan parameter kriteria (jarak, lantai, tipe slot, kepadatan), serta memodelkannya ke dalam struktur data matriks keputusan SPK.",
        bold_prefix="1. Tahap Observasi Masalah & Perancangan Model Matematis SPK:"
    ))
    body_elements.append(p_bullet(
        "Saat kendaraan tiba di gerbang masuk, data kendaraan diidentifikasi. Sistem memfilter seluruh slot yang sedang berstatus 'kosong' sebagai daftar alternatif, menormalisasi data matriks keputusan, dan menghitung perankingan menggunakan algoritma MADM (seperti SMART/MOORA).",
        bold_prefix="2. Tahap Eksekusi Komputasi Rekomendasi Real-Time di Gate:"
    ))
    body_elements.append(p_bullet(
        "Slot dengan nilai preferensi tertinggi (Ranking 1) ditetapkan sebagai rekomendasi keputusan terbaik. Kode slot langsung dicetak pada karcis parkir pengemudi dan status slot fisik dikunci menjadi 'terisi' pada database serta denah pemantauan.",
        bold_prefix="3. Tahap Penerbitan Tiket Rekomendasi & Sinkronisasi Denah:"
    ))
    body_elements.append(p_bullet(
        "Mengukur efektivitas prototipe SPK dalam meratakan distribusi okupansi antar lantai, memangkas waktu pencarian petak parkir bagi pengemudi, serta memastikan 100% kepatuhan peruntukan fasilitas khusus (seperti slot EV).",
        bold_prefix="4. Tahap Evaluasi Kinerja & Validasi Luaran PBL:"
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
    
    print(f"File DOCX SPK bertema PBL berhasil dibuat: {filename} ({os.path.getsize(filename)} bytes)")

if __name__ == "__main__":
    build_docx("Tugas_6_Rencana_Project_SPK.docx")
    build_docx("Rencana_Project_SPK_SmartParkir.docx")
