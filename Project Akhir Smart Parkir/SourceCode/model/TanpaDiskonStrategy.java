package model;

/**
 * Strategi tarif reguler tanpa potongan harga (Diskon 0%).
 * 
 * Menerapkan LSP & OCP sebagai skema baseline standar sistem parkir.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class TanpaDiskonStrategy implements DiskonStrategy {

    @Override
    public double hitungDiskon(Kendaraan k, double tarifAwal, int durasi) {
        if (durasi < 0) {
            throw new IllegalArgumentException("Durasi parkir tidak boleh bernilai negatif!");
        }
        return 0.0;
    }

    @Override
    public String getNamaDiskon() {
        return "Reguler (Tanpa Diskon)";
    }

    @Override
    public String getDeskripsi() {
        return "Tarif parkir standar penuh tanpa potongan harga";
    }
}
