package model;

/**
 * Interface IPaymentProcessor mendefinisikan kontrak terstandardisasi untuk eksekusi pembayaran (Strategy Pattern).
 * 
 * Prinsip SOLID yang diterapkan:
 * - OCP: Penambahan gateway pembayaran baru (misal ShopeePay, Gopay, Flazz, dsb) dapat dilakukan
 *   dengan membuat implementor baru tanpa mengubah method prosesKeluar() di GedungParkir.
 * - LSP: Seluruh prosesor pembayaran menjamin kontrak transaksi seragam (sukses/gagal/kurang).
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public interface IPaymentProcessor {

    /**
     * Memproses penyelesaian transaksi pembayaran karcis parkir.
     *
     * @param tiket tiket parkir aktif yang akan dilunasi
     * @param uangBayar nominal pembayaran yang diserahkan/diotorisasi
     * @return true jika pembayaran sukses dan lunas
     * @throws PembayaranKurangException jika nominal pembayaran kurang dari total tagihan
     */
    boolean prosesPembayaran(KarcisParkir tiket, double uangBayar) throws PembayaranKurangException;

    /**
     * Mengembalikan nama kode resmi metode pembayaran (misal: "CASH", "E_MONEY", "QRIS", "VIRTUAL_ACCOUNT").
     *
     * @return nama metode pembayaran
     */
    String getNamaMetode();
}
