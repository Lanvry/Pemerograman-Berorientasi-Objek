package model;

/**
 * Strategi insentif ramah lingkungan khusus kendaraan listrik (Diskon 20%).
 * 
 * Sesuai OCP: Memisahkan aturan bisnis kendaraan listrik ke dalam kelas mandiri.
 * Sesuai LSP: Jika kendaraan bukan mobil listrik, diskon bernilai 0.0 dengan aman
 * tanpa melempar exception atau membatalkan invariant kalkulasi.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class EVIncentiveDiskonStrategy implements DiskonStrategy {

    private static final double PERSENTASE_DISKON = 0.20;

    @Override
    public double hitungDiskon(Kendaraan k, double tarifAwal, int durasi) {
        if (durasi < 0) {
            throw new IllegalArgumentException("Durasi parkir tidak boleh bernilai negatif!");
        }
        if (k != null && k.isMobilListrik() && tarifAwal > 0) {
            return tarifAwal * PERSENTASE_DISKON;
        }
        return 0.0;
    }

    @Override
    public String getNamaDiskon() {
        return "Insentif Hijau EV (Diskon 20%)";
    }

    @Override
    public String getDeskripsi() {
        return "Potongan 20% khusus kendaraan bertenaga listrik ramah lingkungan";
    }
}
