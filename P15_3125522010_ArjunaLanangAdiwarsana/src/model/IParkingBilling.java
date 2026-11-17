package model;

/**
 * Interface IParkingBilling mengatur kalkulasi tarif, penerapan diskon, dan eksekusi pembayaran check-out.
 * 
 * Sesuai prinsip ISP (Interface Segregation Principle):
 * - Memisahkan kontrak finansial dan kasir dari kontrol gerbang fisik dan manajemen data master armada.
 * - Client pemanggil (seperti kasir, self-payment kiosk, modul audit keuangan) hanya
 *   perlu bergantung pada interface ini.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public interface IParkingBilling {

    /**
     * Menghitung total tagihan akhir dengan skema diskon dinamis (Strategy Pattern).
     */
    double hitungTagihan(KarcisParkir tiket, int jamKeluar, DiskonStrategy diskonStrategy);

    /**
     * Menghitung total tagihan akhir dengan skema default.
     */
    double hitungTagihan(KarcisParkir tiket, int jamKeluar);

    /**
     * Memproses alur check-out dan pembayaran dengan parameter nama metode (kompatibilitas backward).
     */
    boolean prosesKeluar(KarcisParkir tiket, int jamKeluar, String metode, double uangBayar)
            throws TiketTidakDitemukanException, PembayaranKurangException;

    /**
     * Memproses alur check-out menggunakan abstraksi IPaymentProcessor dan DiskonStrategy (OCP & LSP).
     */
    boolean prosesKeluar(KarcisParkir tiket, int jamKeluar, IPaymentProcessor paymentProcessor, double uangBayar, DiskonStrategy diskonStrategy)
            throws TiketTidakDitemukanException, PembayaranKurangException;

    /**
     * Mengembalikan total akumulasi pendapatan kas parkir gedung.
     */
    double getTotalPendapatan();
}
