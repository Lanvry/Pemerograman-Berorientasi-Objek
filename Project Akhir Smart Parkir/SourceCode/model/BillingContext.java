package model;

/**
 * Context Object untuk mengkapsulasi seluruh konteks yang diperlukan
 * saat melakukan kalkulasi tagihan parkir (P14 Clean Refactoring).
 * 
 * Memadukan KarcisParkir, jam keluar aktual, tarif per jam, dan strategi diskon.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class BillingContext {
    private final KarcisParkir tiket;
    private final int jamKeluar;
    private final double tarifJamPertama;
    private final double tarifPerJamBerikutnya;
    private final DiskonStrategy diskonStrategy;

    public BillingContext(KarcisParkir tiket, int jamKeluar, double tarifJamPertama, double tarifPerJamBerikutnya, DiskonStrategy diskonStrategy) {
        if (tiket == null) {
            throw new IllegalArgumentException("Tiket tidak boleh null!");
        }
        if (jamKeluar < 0 || jamKeluar > 24) {
            throw new IllegalArgumentException("Jam keluar harus berada dalam rentang 0-24! Diterima: " + jamKeluar);
        }
        this.tiket = tiket;
        this.jamKeluar = jamKeluar;
        this.tarifJamPertama = tarifJamPertama;
        this.tarifPerJamBerikutnya = tarifPerJamBerikutnya;
        this.diskonStrategy = (diskonStrategy != null) ? diskonStrategy : tentukanDefaultDiskon(tiket);
    }

    public BillingContext(KarcisParkir tiket, int jamKeluar, DiskonStrategy diskonStrategy) {
        this(tiket, jamKeluar, 0.0, 0.0, diskonStrategy);
    }

    public BillingContext(KarcisParkir tiket, int jamKeluar) {
        this(tiket, jamKeluar, 0.0, 0.0, null);
    }

    private static DiskonStrategy tentukanDefaultDiskon(KarcisParkir tiket) {
        if (tiket != null && tiket.getKendaraan() != null && tiket.getKendaraan().isMobilListrik()) {
            return new EVIncentiveDiskonStrategy();
        }
        return new TanpaDiskonStrategy();
    }

    public KarcisParkir getTiket() {
        return tiket;
    }

    public int getJamKeluar() {
        return jamKeluar;
    }

    public double getTarifJamPertama() {
        return tarifJamPertama;
    }

    public double getTarifPerJamBerikutnya() {
        return tarifPerJamBerikutnya;
    }

    public DiskonStrategy getDiskonStrategy() {
        return diskonStrategy;
    }

    public DurasiParkir hitungDurasi() {
        return DurasiParkir.of(tiket.getJamMasuk(), jamKeluar);
    }

    public BiayaParkir hitungBiaya() {
        int durasi = hitungDurasi().getTotalJam();
        double subtotal = tarifJamPertama;
        if (durasi > 1) {
            subtotal += (durasi - 1) * tarifPerJamBerikutnya;
        }

        double potongan = diskonStrategy.hitungDiskon(tiket.getKendaraan(), subtotal, durasi);
        return BiayaParkir.of(subtotal, potongan, diskonStrategy.getNamaDiskon());
    }
}
