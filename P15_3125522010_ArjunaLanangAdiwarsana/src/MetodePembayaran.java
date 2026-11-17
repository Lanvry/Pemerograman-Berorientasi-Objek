/**
 * Interface abstraksi untuk berbagai metode pembayaran (Strategy Pattern / DIP).
 * Memungkinkan penambahan metode pembayaran baru tanpa mengubah logika checkout (OCP).
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public interface MetodePembayaran {
    /**
     * Mendapatkan nama identitas metode pembayaran.
     */
    String getNamaMetode();

    /**
     * Memproses eksekusi pembayaran transaksi.
     * 
     * @param idTransaksi kode identifikasi transaksi
     * @param jumlah tagihan total yang harus dibayar
     * @return nomor/kode referensi transaksi jika berhasil
     * @throws PembayaranGagalException jika terjadi kegagalan otorisasi atau saldo tidak cukup
     */
    String prosesPembayaran(String idTransaksi, double jumlah) throws PembayaranGagalException;
}
