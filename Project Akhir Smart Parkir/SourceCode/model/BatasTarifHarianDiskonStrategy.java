package model;

/**
 * Strategi pembatasan tarif maksimal harian (Daily Max Cap Policy - Skenario Boundary Parkir 24 Jam).
 * 
 * Menerapkan OCP (Open-Closed Principle):
 * Membatasi tagihan kumulatif per 24 jam agar tidak melebihi plafon tarif harian maksimum
 * (misal Rp 50.000/hari untuk mobil) tanpa memodifikasi kalkulasi dasar pada engine parkir.
 * 
 * Menerapkan LSP (Liskov Substitution Principle):
 * Menjamin potongan diskon selalu berada dalam rentang valid [0, tarifAwal].
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class BatasTarifHarianDiskonStrategy implements DiskonStrategy {

    private final double batasMaksimalPerHari;

    public BatasTarifHarianDiskonStrategy(double batasMaksimalPerHari) {
        if (batasMaksimalPerHari < 0) {
            throw new IllegalArgumentException("Batas tarif maksimal tidak boleh bernilai negatif!");
        }
        this.batasMaksimalPerHari = batasMaksimalPerHari;
    }

    public BatasTarifHarianDiskonStrategy() {
        this(50000.0); // Default plafon maksimal Rp 50.000 per hari
    }

    @Override
    public double hitungDiskon(Kendaraan k, double tarifAwal, int durasi) {
        if (durasi < 0) {
            throw new IllegalArgumentException("Durasi parkir tidak boleh negatif!");
        }
        if (tarifAwal > batasMaksimalPerHari) {
            return tarifAwal - batasMaksimalPerHari;
        }
        return 0.0;
    }

    @Override
    public String getNamaDiskon() {
        return String.format("Plafon Maksimal Harian (Cap Rp %,.0f)", batasMaksimalPerHari);
    }

    @Override
    public String getDeskripsi() {
        return String.format("Pembatasan tarif maksimal per siklus 24 jam sebesar Rp %,.0f", batasMaksimalPerHari);
    }

    public double getBatasMaksimalPerHari() {
        return batasMaksimalPerHari;
    }
}
