public class TransaksiRental {
    private String idTransaksi;
    private Pelanggan pelanggan;
    private Kendaraan kendaraan;
    private int durasiHari;
    private int keterlambatanHari;
    private boolean statusSelesai;

    public TransaksiRental(String idTransaksi, Pelanggan pelanggan, Kendaraan kendaraan, int durasiHari) {
        if (idTransaksi == null || idTransaksi.trim().isEmpty()) {
            throw new IllegalArgumentException("ID Transaksi tidak boleh kosong.");
        }
        if (pelanggan == null) {
            throw new IllegalArgumentException("Objek pelanggan tidak boleh bernilai null.");
        }
        if (kendaraan == null) {
            throw new IllegalArgumentException("Objek kendaraan tidak boleh bernilai null.");
        }
        if (durasiHari < 1) {
            throw new IllegalArgumentException("Durasi sewa minimal 1 hari.");
        }
        if (!kendaraan.isTersedia()) {
            throw new IllegalStateException("Kendaraan " + kendaraan.getPlatNomor() + " sedang tidak tersedia.");
        }

        this.idTransaksi = idTransaksi.trim().toUpperCase();
        this.pelanggan = pelanggan;
        this.kendaraan = kendaraan;
        this.durasiHari = durasiHari;
        this.keterlambatanHari = 0;
        this.statusSelesai = false;

        // Kunci ketersediaan kendaraan saat kontrak sewa dibuat
        this.kendaraan.sewa(this.idTransaksi);
    }

    public String getIdTransaksi() {
        return idTransaksi;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public Kendaraan getKendaraan() {
        return kendaraan;
    }

    public int getDurasiHari() {
        return durasiHari;
    }

    public int getKeterlambatanHari() {
        return keterlambatanHari;
    }

    public boolean isStatusSelesai() {
        return statusSelesai;
    }

    // Biaya sewa sebelum diskon dan denda, dievaluasi secara polimorfik
    public double hitungSubtotalSewa() {
        return kendaraan.hitungBiayaSewa(durasiHari);
    }

    public double hitungPotonganDiskon() {
        return hitungSubtotalSewa() * pelanggan.getPersentaseDiskon();
    }

    // Denda 150% dari tarif harian normal untuk setiap hari keterlambatan
    public double hitungDendaKeterlambatan() {
        if (keterlambatanHari <= 0) {
            return 0.0;
        }
        double tarifDendaPerHari = kendaraan.getTarifPerHari() * 1.5;
        return keterlambatanHari * tarifDendaPerHari;
    }

    public double hitungTotalBayar() {
        double subtotal = hitungSubtotalSewa();
        double diskon = hitungPotonganDiskon();
        double denda = hitungDendaKeterlambatan();
        return (subtotal - diskon) + denda;
    }

    public void prosesPengembalian(int keterlambatan) {
        if (statusSelesai) {
            throw new IllegalStateException("Transaksi [" + idTransaksi + "] sudah selesai sebelumnya.");
        }
        if (keterlambatan < 0) {
            throw new IllegalArgumentException("Keterlambatan tidak boleh bernilai negatif.");
        }

        this.keterlambatanHari = keterlambatan;
        this.kendaraan.kembalikan();
        this.statusSelesai = true;
    }

    public void cetakKuitansi() {
        double subtotal = hitungSubtotalSewa();
        double diskon = hitungPotonganDiskon();
        double denda = hitungDendaKeterlambatan();
        double total = hitungTotalBayar();

        System.out.println("=================================================================");
        System.out.printf("                    KUITANSI SEWA KENDARAAN                     %n");
        System.out.println("=================================================================");
        System.out.printf("No. Transaksi  : %s%n", idTransaksi);
        System.out.printf("Status Sewa    : %s%n", (statusSelesai ? "SELESAI (SUDAH KEMBALI)" : "AKTIF (SEDANG DISEWA)"));
        System.out.printf("Pelanggan      : %s (%s) | Member: %s%n",
                pelanggan.getNama(), pelanggan.getIdPelanggan(), pelanggan.getTipeMember());
        System.out.printf("Kendaraan      : %s - %s %s%n",
                kendaraan.getPlatNomor(), kendaraan.getMerk(), kendaraan.getModel());
        System.out.printf("Durasi Sewa    : %d Hari%n", durasiHari);
        if (keterlambatanHari > 0) {
            System.out.printf("Keterlambatan  : %d Hari (Kena Denda 150%%/hari)%n", keterlambatanHari);
        }
        System.out.println("-----------------------------------------------------------------");
        System.out.printf("Subtotal Sewa        : Rp %,14.0f%n", subtotal);
        if (diskon > 0) {
            System.out.printf("Diskon Member (%.0f%%) : -Rp %,13.0f%n",
                    pelanggan.getPersentaseDiskon() * 100, diskon);
        }
        if (denda > 0) {
            System.out.printf("Denda Keterlambatan  : +Rp %,13.0f%n", denda);
        }
        System.out.println("-----------------------------------------------------------------");
        System.out.printf("TOTAL PEMBAYARAN     : Rp %,14.0f%n", total);
        System.out.println("=================================================================");
    }
}
