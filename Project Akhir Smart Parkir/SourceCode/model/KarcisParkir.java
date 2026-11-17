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

    public int hitungDurasi(int jamKeluarAktual) {
        if (jamKeluarAktual < jamMasuk) {
            // Simulasi melewati tengah malam (misal masuk 23, keluar 2 -> 3 jam)
            return (24 - jamMasuk) + jamKeluarAktual;
        }
        int durasi = jamKeluarAktual - jamMasuk;
        return Math.max(1, durasi); // Minimal dihitung 1 jam
    }

    /**
     * Menghitung tarif parkir dengan skema DiskonStrategy dinamis (OCP & LSP).
     */
    public double hitungTarif(int jamKeluarAktual, double tarifJamPertama, double tarifPerJamBerikutnya, DiskonStrategy diskonStrategy) {
        this.jamKeluar = jamKeluarAktual;
        int durasi = hitungDurasi(jamKeluarAktual);

        double subtotal = tarifJamPertama;
        if (durasi > 1) {
            subtotal += (durasi - 1) * tarifPerJamBerikutnya;
        }

        if (diskonStrategy != null) {
            this.potonganDiskon = diskonStrategy.hitungDiskon(kendaraan, subtotal, durasi);
            this.namaDiskon = diskonStrategy.getNamaDiskon();
        } else {
            // Fallback default insentif EV jika strategy tidak disuplai
            if (kendaraan.isMobilListrik()) {
                this.potonganDiskon = subtotal * 0.20;
                this.namaDiskon = "Insentif Hijau EV (Diskon 20%)";
            } else {
                this.potonganDiskon = 0.0;
                this.namaDiskon = "Reguler (Tanpa Diskon)";
            }
        }

        // Batasi potongan diskon agar tidak negatif dan tidak melebihi subtotal
        if (this.potonganDiskon > subtotal) {
            this.potonganDiskon = subtotal;
        }
        if (this.potonganDiskon < 0) {
            this.potonganDiskon = 0;
        }

        this.totalBiaya = subtotal - this.potonganDiskon;
        return this.totalBiaya;
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
