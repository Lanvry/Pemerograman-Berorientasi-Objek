import os
from PIL import Image, ImageDraw, ImageFont
from reportlab.lib.pagesizes import letter, landscape
from reportlab.platypus import SimpleDocTemplate, Image as RLImage

def draw_hollow_triangle(draw, tip, base1, base2, fill="#ffffff", outline="#000000", width=1):
    draw.polygon([tip, base1, base2], fill=fill, outline=outline)

def draw_dashed_line(draw, x1, y1, x2, y2, fill="#000000", width=1, dash_len=6, gap_len=5):
    import math
    dx = x2 - x1
    dy = y2 - y1
    dist = math.hypot(dx, dy)
    if dist == 0:
        return
    vx = dx / dist
    vy = dy / dist
    curr = 0
    while curr < dist:
        end = min(curr + dash_len, dist)
        draw.line([(x1 + vx * curr, y1 + vy * curr), (x1 + vx * end, y1 + vy * end)], fill=fill, width=width)
        curr += dash_len + gap_len

def create_uml_diagram(output_png, output_pdf):
    W, H = 2250, 1380
    img = Image.new("RGB", (W, H), "#ffffff")
    draw = ImageDraw.Draw(img)

    # Fonts - Clean standard academic sans-serif
    font_path_bold = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
    font_path_regular = "/System/Library/Fonts/Supplemental/Arial.ttf"
    font_path_italic = "/System/Library/Fonts/Supplemental/Arial Italic.ttf"
    if not os.path.exists(font_path_bold):
        font_path_bold = "/System/Library/Fonts/Supplemental/Arial.ttf"
    if not os.path.exists(font_path_italic):
        font_path_italic = "/System/Library/Fonts/Supplemental/Arial.ttf"

    font_title_block = ImageFont.truetype(font_path_bold, 14)
    font_title_block_sub = ImageFont.truetype(font_path_regular, 13)
    
    font_class_title = ImageFont.truetype(font_path_bold, 16)
    font_class_italic = ImageFont.truetype(font_path_italic, 16)
    font_stereo = ImageFont.truetype(font_path_regular, 13)
    font_member = ImageFont.truetype(font_path_regular, 13)
    font_label = ImageFont.truetype(font_path_regular, 12)
    font_label_bold = ImageFont.truetype(font_path_bold, 12)

    # Outer border / frame (StarUML style canvas border)
    draw.rectangle([30, 20, W - 30, H - 20], fill="#ffffff", outline="#000000", width=1)

    # Title Block (Academic engineering block)
    draw.rectangle([30, 20, 480, 95], fill="#ffffff", outline="#000000", width=1)
    draw.text((45, 30), "UML Class Diagram: Sistem Rental Kendaraan", fill="#000000", font=font_title_block)
    draw.text((45, 52), "Author  : Arjuna Lanang Adiwarsana (NRP: 3125522010)", fill="#000000", font=font_title_block_sub)
    draw.text((45, 72), "Project : UTS Praktikum PBO - D3 IT PSDKU PENS", fill="#000000", font=font_title_block_sub)

    # Helper function: draw monochrome UML Class box
    def draw_class(x, y, w, stereo, name, attributes, methods, is_abstract=False):
        header_h = 44 if stereo else 36
        attr_h = (len(attributes) * 20 + 10) if attributes else 0
        method_h = (len(methods) * 20 + 10) if methods else 0
        total_h = header_h + attr_h + method_h

        # Main box
        draw.rectangle([x, y, x + w, y + total_h], fill="#ffffff", outline="#000000", width=1)

        # Header divider
        draw.line([(x, y + header_h), (x + w, y + header_h)], fill="#000000", width=1)

        # Title text
        if stereo:
            sw = draw.textlength(stereo, font=font_stereo)
            draw.text((x + (w - sw) / 2, y + 6), stereo, fill="#000000", font=font_stereo)
            tfont = font_class_italic if is_abstract else font_class_title
            nw = draw.textlength(name, font=tfont)
            draw.text((x + (w - nw) / 2, y + 22), name, fill="#000000", font=tfont)
        else:
            tfont = font_class_italic if is_abstract else font_class_title
            nw = draw.textlength(name, font=tfont)
            draw.text((x + (w - nw) / 2, y + 10), name, fill="#000000", font=tfont)

        curr_y = y + header_h + 8

        # Attributes
        if attributes:
            for attr in attributes:
                draw.text((x + 12, curr_y), attr, fill="#000000", font=font_member)
                curr_y += 20
            curr_y += 2
            draw.line([(x, curr_y), (x + w, curr_y)], fill="#000000", width=1)
            curr_y += 8

        # Methods
        if methods:
            for m in methods:
                draw.text((x + 12, curr_y), m, fill="#000000", font=font_member)
                curr_y += 20

        return total_h

    # 1. Interface: DapatDisewa (Top Left)
    h_dapatdisewa = draw_class(
        x=80, y=140, w=340,
        stereo="<<interface>>", name="DapatDisewa",
        attributes=[],
        methods=[
            "+ isTersedia() : boolean",
            "+ sewa(idTransaksi : String) : void",
            "+ kembalikan() : void"
        ]
    )

    # 2. Abstract Superclass: Kendaraan (Top Center)
    h_kendaraan = draw_class(
        x=520, y=140, w=590,
        stereo="<<abstract>>", name="Kendaraan",
        attributes=[
            "- platNomor : String",
            "- merk : String",
            "- model : String",
            "- tahunProduksi : int",
            "- tarifPerHari : double",
            "- tersedia : boolean"
        ],
        methods=[
            "+ Kendaraan(platNomor, merk, model, thn, tarif)",
            "+ getPlatNomor() : String",
            "+ getMerk() : String",
            "+ getModel() : String",
            "+ getTahunProduksi() : int",
            "+ getTarifPerHari() : double",
            "+ setTarifPerHari(tarifPerHari : double) : void",
            "+ isTersedia() : boolean",
            "+ sewa(idTransaksi : String) : void",
            "+ kembalikan() : void",
            "+ {abstract} hitungBiayaSewa(durasiHari : int) : double",
            "+ {abstract} tampilkanSpesifikasi() : void",
            "+ tampilkanInformasiDasar() : void"
        ],
        is_abstract=True
    )

    # 3. Subclass: Mobil (Bottom Center-Left)
    h_mobil = draw_class(
        x=350, y=740, w=510,
        stereo="", name="Mobil",
        attributes=[
            "- kapasitasPenumpang : int",
            "- jenisTransmisi : String",
            "- denganSupir : boolean",
            "- biayaSupirPerHari : double",
            "- biayaAsuransiHarian : double"
        ],
        methods=[
            "+ Mobil(plat, merk, model, thn, tarif, kap, transmisi)",
            "+ getKapasitasPenumpang() : int",
            "+ getJenisTransmisi() : String",
            "+ isDenganSupir() : boolean",
            "+ setDenganSupir(denganSupir : boolean) : void",
            "+ getBiayaSupirPerHari() : double",
            "+ setBiayaSupirPerHari(biayaSupirPerHari : double) : void",
            "+ hitungBiayaSewa(durasiHari : int) : double",
            "+ tampilkanSpesifikasi() : void"
        ]
    )

    # 4. Subclass: Motor (Bottom Center-Right)
    h_motor = draw_class(
        x=900, y=740, w=480,
        stereo="", name="Motor",
        attributes=[
            "- tipeMotor : String",
            "- jumlahHelm : int",
            "- biayaHelmTambahanPerHari : double"
        ],
        methods=[
            "+ Motor(plat, merk, model, thn, tarif, tipe, helm)",
            "+ getTipeMotor() : String",
            "+ getJumlahHelm() : int",
            "+ setJumlahHelm(jumlahHelm : int) : void",
            "+ getBiayaHelmTambahanPerHari() : double",
            "+ hitungBiayaSewa(durasiHari : int) : double",
            "+ tampilkanSpesifikasi() : void"
        ]
    )

    # 5. Entity: Pelanggan (Top Right)
    h_pelanggan = draw_class(
        x=1470, y=140, w=530,
        stereo="", name="Pelanggan",
        attributes=[
            "- idPelanggan : String",
            "- nama : String",
            "- nomorTelepon : String",
            "- tipeMember : String"
        ],
        methods=[
            "+ Pelanggan(idPelanggan, nama, nomorTelepon, tipeMember)",
            "+ getIdPelanggan() : String",
            "+ getNama() : String",
            "+ setNama(nama : String) : void",
            "+ getNomorTelepon() : String",
            "+ setNomorTelepon(nomorTelepon : String) : void",
            "+ getTipeMember() : String",
            "+ setTipeMember(tipeMember : String) : void",
            "+ getPersentaseDiskon() : double",
            "+ tampilkanProfil() : void"
        ]
    )

    # 6. Transaction: TransaksiRental (Bottom Right)
    h_transaksi = draw_class(
        x=1450, y=660, w=570,
        stereo="", name="TransaksiRental",
        attributes=[
            "- idTransaksi : String",
            "- pelanggan : Pelanggan",
            "- kendaraan : Kendaraan",
            "- durasiHari : int",
            "- keterlambatanHari : int",
            "- statusSelesai : boolean"
        ],
        methods=[
            "+ TransaksiRental(idTransaksi, pelanggan, kendaraan, durasi)",
            "+ getIdTransaksi() : String",
            "+ getPelanggan() : Pelanggan",
            "+ getKendaraan() : Kendaraan",
            "+ getDurasiHari() : int",
            "+ getKeterlambatanHari() : int",
            "+ isStatusSelesai() : boolean",
            "+ hitungSubtotalSewa() : double",
            "+ hitungPotonganDiskon() : double",
            "+ hitungDendaKeterlambatan() : double",
            "+ hitungTotalBayar() : double",
            "+ prosesPengembalian(keterlambatan : int) : void",
            "+ cetakKuitansi() : void"
        ]
    )

    # ================= RELATIONSHIPS & CONNECTORS =================

    # 1. Realization: Kendaraan ..|> DapatDisewa
    draw_dashed_line(draw, 520, 190, 435, 190, fill="#000000", width=1)
    draw_hollow_triangle(draw, (420, 190), (435, 183), (435, 197), fill="#ffffff", outline="#000000", width=1)

    # 2. Generalization (Inheritance Tree): Mobil & Motor --|> Kendaraan
    # Mobil top center: (x=605, y=740) -> up to y=675
    draw.line([(605, 740), (605, 675)], fill="#000000", width=1)
    # Motor top center: (x=1140, y=740) -> up to y=675
    draw.line([(1140, 740), (1140, 675)], fill="#000000", width=1)
    # Horizontal junction bar
    draw.line([(605, 675), (1140, 675)], fill="#000000", width=1)
    # Central stem up to Kendaraan bottom center (x=815)
    kendaraan_bottom = 140 + h_kendaraan
    draw.line([(815, 675), (815, kendaraan_bottom + 15)], fill="#000000", width=1)
    draw_hollow_triangle(draw, (815, kendaraan_bottom), (807, kendaraan_bottom + 15), (823, kendaraan_bottom + 15), fill="#ffffff", outline="#000000", width=1)

    # 3. Association: TransaksiRental --> Pelanggan
    pelanggan_bottom = 140 + h_pelanggan
    draw.line([(1735, 660), (1735, pelanggan_bottom)], fill="#000000", width=1)
    draw.line([(1735, pelanggan_bottom), (1729, pelanggan_bottom + 10)], fill="#000000", width=1)
    draw.line([(1735, pelanggan_bottom), (1741, pelanggan_bottom + 10)], fill="#000000", width=1)
    # Multiplicities
    draw.text((1745, pelanggan_bottom + 8), "1", fill="#000000", font=font_label_bold)
    draw.text((1745, 638), "0..*", fill="#000000", font=font_label_bold)

    # 4. Association: TransaksiRental --> Kendaraan
    draw.line([(1450, 780), (1395, 780)], fill="#000000", width=1)
    draw.line([(1395, 780), (1395, 360)], fill="#000000", width=1)
    draw.line([(1395, 360), (1110, 360)], fill="#000000", width=1)
    draw.line([(1110, 360), (1120, 354)], fill="#000000", width=1)
    draw.line([(1110, 360), (1120, 366)], fill="#000000", width=1)
    # Multiplicities
    draw.text((1125, 342), "1", fill="#000000", font=font_label_bold)
    draw.text((1415, 762), "0..*", fill="#000000", font=font_label_bold)

    # Standard Bottom Legend (Minimalist monochrome)
    draw.rectangle([80, 1190, 420, 1330], fill="#ffffff", outline="#000000", width=1)
    draw.text((95, 1200), "Keterangan Relasi UML:", fill="#000000", font=font_label_bold)

    # Realization
    draw_dashed_line(draw, 95, 1235, 140, 1235, fill="#000000", width=1)
    draw_hollow_triangle(draw, (140, 1235), (132, 1230), (132, 1240), fill="#ffffff", outline="#000000", width=1)
    draw.text((155, 1228), "Realization (implements)", fill="#000000", font=font_label)

    # Generalization
    draw.line([(95, 1270), (140, 1270)], fill="#000000", width=1)
    draw_hollow_triangle(draw, (140, 1270), (132, 1265), (132, 1275), fill="#ffffff", outline="#000000", width=1)
    draw.text((155, 1263), "Generalization (extends)", fill="#000000", font=font_label)

    # Association
    draw.line([(95, 1305), (140, 1305)], fill="#000000", width=1)
    draw.line([(140, 1305), (133, 1300)], fill="#000000", width=1)
    draw.line([(140, 1305), (133, 1310)], fill="#000000", width=1)
    draw.text((155, 1298), "Association (has-a)", fill="#000000", font=font_label)

    # Save PNG
    img.save(output_png, "PNG", dpi=(300, 300))
    print(f"[OK] Saved clean monochrome PNG diagram to: {output_png}")

    # Generate PDF
    doc = SimpleDocTemplate(
        output_pdf,
        pagesize=landscape(letter),
        leftMargin=10, rightMargin=10,
        topMargin=10, bottomMargin=10
    )
    story = []
    story.append(RLImage(output_png, width=772, height=474))
    doc.build(story)
    print(f"[OK] Saved clean monochrome PDF diagram to: {output_pdf}")

if __name__ == "__main__":
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_png = os.path.join(script_dir, "class-diagram.png")
    output_pdf = os.path.join(script_dir, "class-diagram.pdf")
    create_uml_diagram(output_png, output_pdf)
