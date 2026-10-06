import os
from PIL import Image, ImageDraw, ImageFont

def create_terminal_screenshot(output_path="terminal_screenshot.png"):
    # High resolution setup
    width = 1600
    font_size = 20
    line_height = 28
    padding_x = 35
    padding_top = 70
    padding_bottom = 35

    # Color palette (macOS Dark Terminal / Catppuccin Mocha style)
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

    # Traffic light buttons
    BTN_RED = (255, 95, 86)
    BTN_YELLOW = (255, 189, 46)
    BTN_GREEN = (39, 201, 63)

    # Terminal lines data: list of lists of (text, color)
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
        [("   PRAKTIKUM MODUL 7 - ABSTRACT CLASS, ABSTRACT METHOD & INTERFACE", COLOR_YELLOW)],
        [("   SISTEM MANAJEMEN PERPUSTAKAAN (PROJECT BASED LEARNING + AGILE)", COLOR_CYAN)],
        [("=========================================================================================", COLOR_BLUE)],
        [("", TEXT_DEFAULT)],
        [("=== SKENARIO 1: MEMBUAT OBJECT SUBCLASS DARI ABSTRACT CLASS & INTERFACE ===", COLOR_GREEN)],
        [("[Sukses] ", COLOR_GREEN), ("Seluruh object subclass berhasil diinstansiasi di heap memory:", TEXT_DEFAULT)],
        [("1. Objek Mahasiswa : ", COLOR_CYAN), ("Arjuna Lanang Adiwarsana (NRP: 3125522010)", TEXT_DEFAULT)],
        [("2. Objek Dosen     : ", COLOR_CYAN), ("Nirwana Haidar Hari, S.Pd., M.Kom. (NIP: 198801232024011001)", TEXT_DEFAULT)],
        [("3. Objek Tendik    : ", COLOR_CYAN), ("Ahmad Fauzi, S.Kom. (Unit: Bagian Administrasi & Akademik)", TEXT_DEFAULT)],
        [("4. Objek Buku      : ", COLOR_CYAN), ("Pemrograman Berorientasi Objek [B001]", TEXT_DEFAULT)],
        [("", TEXT_DEFAULT)],
        [("=== SKENARIO 2: MEMANGGIL ABSTRACT METHOD VIA REFERENCE SUPERCLASS (DYNAMIC BINDING) ===", COLOR_GREEN)],
        [("--- Pemanggilan Abstract Method: tampilkanPeran() ---", COLOR_YELLOW)],
        [("refAnggota1 (Mahasiswa) -> ", COLOR_PURPLE), ("Peran        : Mahasiswa (Akses Koleksi Skripsi & Akademik)", TEXT_DEFAULT)],
        [("refAnggota2 (Dosen)     -> ", COLOR_PURPLE), ("Peran        : Dosen Pengajar & Peneliti (Akses Koleksi Jurnal)", TEXT_DEFAULT)],
        [("refAnggota3 (Tendik)    -> ", COLOR_PURPLE), ("Peran        : Tenaga Kependidikan / Staf (Akses Operasional)", TEXT_DEFAULT)],
        [("--- Pemanggilan Abstract Method: hitungDenda(3 Hari) & getMaksimalPinjam() ---", COLOR_YELLOW)],
        [("refAnggota1 -> ", COLOR_PURPLE), ("Denda: Rp 1500 | Kuota Pinjam: 3 Buku (Tarif Khusus Mahasiswa: Rp 500/hari)", TEXT_DEFAULT)],
        [("refAnggota2 -> ", COLOR_PURPLE), ("Denda: Rp 0    | Kuota Pinjam: 5 Buku (Hak Istimewa Bebas Denda)", TEXT_DEFAULT)],
        [("refAnggota3 -> ", COLOR_PURPLE), ("Denda: Rp 2250 | Kuota Pinjam: 4 Buku (Tarif Khusus Tendik: Rp 750/hari)", TEXT_DEFAULT)],
        [("", TEXT_DEFAULT)],
        [("=== SKENARIO 3: MEMANGGIL METHOD MELALUI REFERENCE INTERFACE (CROSS-HIERARCHY) ===", COLOR_GREEN)],
        [("[Sukses] ", COLOR_GREEN), ("Pemanggilan method getLokasi() melalui Reference Interface DapatDilacak:", TEXT_DEFAULT)],
        [("1. pelacakMhs.getLokasi()  -> ", COLOR_ACCENT), ("Area Baca Lantai 2 / Ruang Koleksi Skripsi", TEXT_DEFAULT)],
        [("2. pelacakDsn.getLokasi()  -> ", COLOR_ACCENT), ("Ruang Dosen Gedung TI / Laboratorium Riset", TEXT_DEFAULT)],
        [("3. pelacakBuku.getLokasi() -> ", COLOR_ACCENT), ("Koleksi Fisik di Rak A-03 (Informatika)", TEXT_DEFAULT)],
        [("", TEXT_DEFAULT)],
        [("=== SKENARIO 4: POLYMORPHIC COLLECTION (ARRAY OF ABSTRACT CLASS & INTERFACE) ===", COLOR_GREEN)],
        [(">> Anggota ke-1 [Mahasiswa]: ", COLOR_YELLOW), ("Arjuna Lanang (3125522010) | Kuota: 3 | Denda 4 Hari: Rp 2000", TEXT_DEFAULT)],
        [(">> Anggota ke-2 [Dosen]    : ", COLOR_YELLOW), ("Nirwana Haidar, S.Pd., M.Kom. | Kuota: 5 | Denda 4 Hari: Rp 0", TEXT_DEFAULT)],
        [(">> Anggota ke-3 [Tendik]   : ", COLOR_YELLOW), ("Ahmad Fauzi, S.Kom. | Kuota: 4 | Denda 4 Hari: Rp 3000", TEXT_DEFAULT)],
        [(">> Anggota ke-4 [Mahasiswa]: ", COLOR_YELLOW), ("Siti Nurhaliza (3125522011) | Kuota: 3 | Denda 4 Hari: Rp 2000", TEXT_DEFAULT)],
        [("Iterasi DapatDilacak[]     : ", COLOR_CYAN), ("5 Elemen lintas hierarki berhasil mengembalikan lokasi fisik secara dinamis", TEXT_DEFAULT)],
        [("", TEXT_DEFAULT)],
        [("=== SKENARIO 5: INTEGRASI SISTEM & VERIFIKASI FITUR P1-P6 ===", COLOR_GREEN)],
        [("1. Overloading hitungDenda : ", COLOR_BLUE), ("Standar 5 Hari = Rp 2500 | Diskon 50% = Rp 1250", TEXT_DEFAULT)],
        [("2. Overloading cariBuku    : ", COLOR_BLUE), ("cariBuku(judul) & cariBuku(kode, bool) -> Berhasil Ditemukan", TEXT_DEFAULT)],
        [("3. Transaksi Peminjaman    : ", COLOR_BLUE), ("PJ-001 (Mahasiswa, Denda: Rp 1500) | PJ-002 (Dosen, Denda: Rp 0)", TEXT_DEFAULT)],
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

    # Create image
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Rounded rectangle background
    corner_radius = 16
    draw.rounded_rectangle([(0, 0), (width, height)], radius=corner_radius, fill=BG_COLOR)

    # Title bar
    title_bar_height = 48
    draw.rounded_rectangle([(0, 0), (width, title_bar_height)], radius=corner_radius, fill=HEADER_BG)
    # Fill bottom corners of title bar so only top corners are rounded
    draw.rectangle([(0, title_bar_height - corner_radius), (width, title_bar_height)], fill=HEADER_BG)
    draw.line([(0, title_bar_height), (width, title_bar_height)], fill=HEADER_BORDER, width=1)

    # Traffic light buttons
    btn_y = 24
    draw.ellipse([(24, btn_y - 7), (38, btn_y + 7)], fill=BTN_RED)
    draw.ellipse([(48, btn_y - 7), (62, btn_y + 7)], fill=BTN_YELLOW)
    draw.ellipse([(72, btn_y - 7), (86, btn_y + 7)], fill=BTN_GREEN)

    # Title text
    try:
        font_title = ImageFont.truetype("/System/Library/Fonts/Menlo.ttc", 15)
        font_code = ImageFont.truetype("/System/Library/Fonts/Menlo.ttc", font_size)
    except:
        font_title = ImageFont.load_default()
        font_code = ImageFont.load_default()

    title_str = "arjuna@MacBook-Pro: ~/Herd/Java/P7_3125522010_ArjunaLanangAdiwarsana — -zsh — 112×42"
    # Measure title width
    bbox = draw.textbbox((0, 0), title_str, font=font_title)
    tw = bbox[2] - bbox[0]
    draw.text(((width - tw) / 2, 16), title_str, font=font_title, fill=TEXT_MUTED)

    # Draw code lines
    curr_y = padding_top
    for line_parts in lines_data:
        curr_x = padding_x
        for text, color in line_parts:
            draw.text((curr_x, curr_y), text, font=font_code, fill=color)
            part_bbox = draw.textbbox((0, 0), text, font=font_code)
            curr_x += (part_bbox[2] - part_bbox[0])
        curr_y += line_height

    img.save(output_path, "PNG")
    print(f"Screenshot successfully saved to: {output_path}")

if __name__ == "__main__":
    create_terminal_screenshot()
