package model;

/**
 * Strategi promosi akhir pekan / promo terbatas (Diskon 10%).
 * 
 * Sesuai OCP: Memungkinkan promo musiman diaktifkan secara instan.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class PromoWeekendDiskonStrategy implements DiskonStrategy {

    private static final double PERSENTASE_DISKON = 0.10;

    @Override
    public double hitungDiskon(Kendaraan k, double tarifAwal, int durasi) {
        if (tarifAwal > 0) {
            return tarifAwal * PERSENTASE_DISKON;
        }
        return 0.0;
    }

    @Override
    public String getNamaDiskon() {
        return "Promo Spesial (Diskon 10%)";
    }

    @Override
    public String getDeskripsi() {
        return "Potongan 10% promo akhir pekan dan event khusus";
    }
}
