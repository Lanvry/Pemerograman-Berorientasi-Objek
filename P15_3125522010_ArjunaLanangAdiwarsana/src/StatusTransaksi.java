/**
 * Enumerasi status transaksi dalam sistem e-commerce/retail.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public enum StatusTransaksi {
    PENDING("Menunggu Pembayaran"),
    BERHASIL("Pembayaran Berhasil"),
    GAGAL("Transaksi Gagal"),
    DIBATALKAN("Transaksi Dibatalkan");

    private final String deskripsi;

    StatusTransaksi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
}
