package model;

/**
 * Interface DiskonStrategy mendefinisikan kontrak kalkulasi skema diskon tarif parkir (Strategy Pattern).
 * 
 * Prinsip SOLID yang diterapkan:
 * - OCP (Open-Closed Principle): Terbuka untuk penambahan skema diskon baru (misal Promo Holiday,
 *   Diskon Karyawan, dsb) tanpa mengubah logika perhitungan inti di GedungParkir maupun KarcisParkir.
 * - LSP (Liskov Substitution Principle): Seluruh kelas turunan DiskonStrategy dapat saling
 *   menggantikan tanpa merusak invariant atau menghasilkan galat tak terduga.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public interface DiskonStrategy {

    /**
     * Menghitung nilai nominal potongan biaya parkir dalam Rupiah.
     *
     * @param k objek Kendaraan yang parkir (dapat null jika darurat)
     * @param tarifAwal nominal tagihan kotor sebelum potongan
     * @param durasi durasi parkir dalam satuan jam
     * @return besar nominal potongan diskon (>= 0 dan <= tarifAwal)
     */
    double hitungDiskon(Kendaraan k, double tarifAwal, int durasi);

    /**
     * Mengembalikan nama identitas resmi program diskon.
     *
     * @return label program diskon untuk ditampilkan pada GUI dan kuitansi
     */
    String getNamaDiskon();

    /**
     * Mengembalikan ringkasan deskripsi ketentuan diskon.
     *
     * @return deskripsi singkat diskon
     */
    String getDeskripsi();
}
