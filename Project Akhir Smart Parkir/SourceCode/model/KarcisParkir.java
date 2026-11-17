package model;

/**
 * Class KarcisParkir merepresentasikan bukti karcis parkir fisik/digital.
 * Mengimplementasikan relasi Agregasi dengan Kendaraan dan SlotParkir.
 * Bertanggung jawab menghitung durasi, tarif progresif, skema diskon dinamis (OCP/LSP), dan status bayar.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class KarcisParkir {
    private String nomorTiket;
    private Kendaraan kendaraan;
    private SlotParkir slot;
    private int jamMasuk;
    private int jamKeluar;
    private boolean sudahDibayar;
    private double totalBiaya;
    private double potonganDiskon;
    private String namaDiskon;
    private String metodePembayaran;

    public KarcisParkir(String nomorTiket, Kendaraan kendaraan, SlotParkir slot, int jamMasuk) {
        if (nomorTiket == null || nomorTiket.trim().isEmpty()) {
            throw new IllegalArgumentException("Nomor tiket tidak boleh kosong!");
        }
        if (kendaraan == null) {
            throw new IllegalArgumentException("Kendaraan tidak boleh null!");
        }
        if (slot == null) {
            throw new IllegalArgumentException("Slot parkir tidak boleh null!");
        }
        if (jamMasuk < 0 || jamMasuk > 23) {
            throw new IllegalArgumentException("Jam masuk harus berada dalam rentang 0-23!");
        }
        this.nomorTiket = nomorTiket.trim().toUpperCase();
        this.kendaraan = kendaraan;
        this.slot = slot;
        this.jamMasuk = jamMasuk;
        this.jamKeluar = jamMasuk;
        this.sudahDibayar = false;
        this.totalBiaya = 0.0;
        this.potonganDiskon = 0.0;
        this.namaDiskon = "-";
        this.metodePembayaran = "-";
    }

    public static void validasiWaktuOperasional(int jamMasuk, int jamKeluar) {
        if (jamMasuk < 0 || jamMasuk > 23) {
            throw new IllegalArgumentException("Jam masuk harus berada dalam rentang 0-23! Diberikan: " + jamMasuk);
        }
        if (jamKeluar < 0 || jamKeluar > 24) {
            throw new IllegalArgumentException("Jam keluar harus berada dalam rentang 0-24! Diberikan: " + jamKeluar);
        }
        if (jamKeluar < jamMasuk) {
            throw new IllegalArgumentException(String.format(
                "Anomali kronologis waktu: Jam keluar (%02d:00) tidak boleh lebih kecil dari jam masuk (%02d:00) pada hari yang sama!",
                jamKeluar, jamMasuk));
        }
    }

    public int hitungDurasi(int jamKeluarAktual) {
        if (jamKeluarAktual < 0 || jamKeluarAktual > 24) {
            throw new IllegalArgumentException("Jam keluar harus berada dalam rentang 0-24! Diberikan: " + jamKeluarAktual);
        }
        return DurasiParkir.of(jamMasuk, jamKeluarAktual).getTotalJam();
    }

    /**
     * Menghitung tarif parkir dengan skema DiskonStrategy dinamis (OCP & LSP)
     * serta memanfaatkan Value Object DurasiParkir & BiayaParkir (P14 Refactoring).
     */
    public double hitungTarif(int jamKeluarAktual, double tarifJamPertama, double tarifPerJamBerikutnya, DiskonStrategy diskonStrategy) {
        if (jamKeluarAktual < 0 || jamKeluarAktual > 24) {
            throw new IllegalArgumentException("Jam keluar harus berada dalam rentang 0-24! Diberikan: " + jamKeluarAktual);
        }
        if (tarifJamPertama < 0 || tarifPerJamBerikutnya < 0) {
            throw new IllegalArgumentException("Tarif parkir tidak boleh bernilai negatif!");
        }
        this.jamKeluar = jamKeluarAktual;
        DurasiParkir durasiParkir = DurasiParkir.of(jamMasuk, jamKeluarAktual);
        int durasi = durasiParkir.getTotalJam();

        double subtotal = tarifJamPertama;
        if (durasi > 1) {
            subtotal += (durasi - 1) * tarifPerJamBerikutnya;
        }

        DiskonStrategy activeStrategy = (diskonStrategy != null) ? diskonStrategy : tentukanDefaultDiskon();
        double diskon = activeStrategy.hitungDiskon(kendaraan, subtotal, durasi);

        BiayaParkir biaya = BiayaParkir.of(subtotal, diskon, activeStrategy.getNamaDiskon());
        this.potonganDiskon = biaya.getPotonganDiskon();
        this.namaDiskon = biaya.getNamaDiskon();
        this.totalBiaya = biaya.getTotalTagihan();
        return this.totalBiaya;
    }

    private DiskonStrategy tentukanDefaultDiskon() {
        if (kendaraan != null && kendaraan.isMobilListrik()) {
            return new EVIncentiveDiskonStrategy();
        }
        return new TanpaDiskonStrategy();
    }

    /**
     * Mengembalikan Value Object DurasiParkir terenkapsulasi (P14 Clean OOD).
     */
    public DurasiParkir getDurasiParkir() {
        return DurasiParkir.of(jamMasuk, jamKeluar);
    }

    /**
     * Mengembalikan Value Object BiayaParkir terenkapsulasi (P14 Clean OOD).
     */
    public BiayaParkir getRincianBiaya() {
        double subtotal = totalBiaya + potonganDiskon;
        return BiayaParkir.of(subtotal, potonganDiskon, namaDiskon);
    }

    /**
     * Menghitung tarif parkir menggunakan default EV Incentive (Backward Compatibility).
     */
    public double hitungTarif(int jamKeluarAktual, double tarifJamPertama, double tarifPerJamBerikutnya) {
        return hitungTarif(jamKeluarAktual, tarifJamPertama, tarifPerJamBerikutnya, new EVIncentiveDiskonStrategy());
    }

    public boolean prosesPembayaran(String metode, double uangBayar) throws PembayaranKurangException {
        if (this.sudahDibayar) {
            System.out.println("[PERINGATAN] Tiket " + nomorTiket + " sudah pernah dibayar sebelumnya.");
            return true;
        }
        if (uangBayar < this.totalBiaya) {
            System.out.printf("[PEMBAYARAN DITOLAK] Uang tidak mencukupi! Tagihan: Rp %,.0f, Dibayar: Rp %,.0f%n",
                    totalBiaya, uangBayar);
            throw new PembayaranKurangException(totalBiaya, uangBayar);
        }

        this.sudahDibayar = true;
        this.metodePembayaran = (metode != null && !metode.trim().isEmpty()) ? metode.toUpperCase() : "CASH";
        return true;
    }

    public String getNomorTiket() {
        return nomorTiket;
    }

    public Kendaraan getKendaraan() {
        return kendaraan;
    }

    public SlotParkir getSlot() {
        return slot;
    }

    public int getJamMasuk() {
        return jamMasuk;
    }

    public int getJamKeluar() {
        return jamKeluar;
    }

    public boolean isSudahDibayar() {
        return sudahDibayar;
    }

    public double getTotalBiaya() {
        return totalBiaya;
    }

    public double getPotonganDiskon() {
        return potonganDiskon;
    }

    public String getNamaDiskon() {
        return namaDiskon;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public void setPotonganDiskon(double potonganDiskon) {
        this.potonganDiskon = Math.max(0, potonganDiskon);
    }

    public void setNamaDiskon(String namaDiskon) {
        this.namaDiskon = (namaDiskon != null) ? namaDiskon : "-";
    }

    @Override
    public String toString() {
        return String.format("Tiket [%s] Plat: %s | Slot: %s | Masuk: %02d:00 | Tagihan: Rp %,.0f (%s) | Status: %s",
                nomorTiket, kendaraan.getPlatNomor(), slot.getIdSlot(), jamMasuk,
                totalBiaya, namaDiskon,
                (sudahDibayar ? "LUNAS" : "BELUM BAYAR"));
    }
}
