package model;

/**
 * Class KarcisParkir merepresentasikan bukti karcis parkir fisik/digital.
 * Mengimplementasikan relasi Agregasi dengan Kendaraan dan SlotParkir.
 * Bertanggung jawab menghitung durasi, tarif progresif, diskon EV, dan status bayar.
 */
public class KarcisParkir {
    private String nomorTiket;
    private Kendaraan kendaraan;
    private SlotParkir slot;
    private int jamMasuk;
    private int jamKeluar;
    private boolean sudahDibayar;
    private double totalBiaya;
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

    public double hitungTarif(int jamKeluarAktual, double tarifJamPertama, double tarifPerJamBerikutnya) {
        this.jamKeluar = jamKeluarAktual;
        int durasi = hitungDurasi(jamKeluarAktual);

        double subtotal = tarifJamPertama;
        if (durasi > 1) {
            subtotal += (durasi - 1) * tarifPerJamBerikutnya;
        }

        // Insentif Hijau: Mobil Listrik mendapatkan diskon 20%
        if (kendaraan.isMobilListrik()) {
            subtotal = subtotal * 0.80;
        }

        this.totalBiaya = subtotal;
        return this.totalBiaya;
    }

    public boolean prosesPembayaran(String metode, double uangBayar) {
        if (this.sudahDibayar) {
            System.out.println("[PERINGATAN] Tiket " + nomorTiket + " sudah pernah dibayar sebelumnya.");
            return true;
        }
        if (uangBayar < this.totalBiaya) {
            System.out.printf("[PEMBAYARAN DITOLAK] Uang tidak mencukupi! Tagihan: Rp %,.0f, Dibayar: Rp %,.0f%n",
                    totalBiaya, uangBayar);
            return false;
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

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    @Override
    public String toString() {
        return String.format("Tiket [%s] Plat: %s | Slot: %s | Masuk: %02d:00 | Status: %s",
                nomorTiket, kendaraan.getPlatNomor(), slot.getIdSlot(), jamMasuk,
                (sudahDibayar ? "LUNAS" : "BELUM BAYAR"));
    }
}
