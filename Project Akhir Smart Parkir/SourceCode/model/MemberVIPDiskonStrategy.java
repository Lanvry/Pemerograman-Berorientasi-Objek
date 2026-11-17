package model;

/**
 * Strategi potongan harga untuk pengguna langganan / armada terdaftar Member VIP (Diskon 30%).
 * 
 * Sesuai OCP: Menambah program loyalitas VIP tanpa mengubah logika inti GedungParkir.
 * Sesuai LSP: Konsisten membatasi diskon dalam rentang sah [0, tarifAwal].
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class MemberVIPDiskonStrategy implements DiskonStrategy {

    private static final double PERSENTASE_DISKON = 0.30;

    @Override
    public double hitungDiskon(Kendaraan k, double tarifAwal, int durasi) {
        if (durasi < 0) {
            throw new IllegalArgumentException("Durasi parkir tidak boleh bernilai negatif!");
        }
        if (tarifAwal > 0) {
            return tarifAwal * PERSENTASE_DISKON;
        }
        return 0.0;
    }

    @Override
    public String getNamaDiskon() {
        return "Member VIP (Diskon 30%)";
    }

    @Override
    public String getDeskripsi() {
        return "Potongan 30% khusus pelanggan prioritas & member armada VIP";
    }
}
