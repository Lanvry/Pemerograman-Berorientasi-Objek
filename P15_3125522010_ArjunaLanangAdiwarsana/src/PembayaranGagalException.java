/**
 * Exception yang dilempar ketika proses eksekusi pembayaran transaksi mengalami kegagalan.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class PembayaranGagalException extends Exception {
    private final String idTransaksi;
    private final double jumlahTagihan;
    private final String alasan;

    public PembayaranGagalException(String idTransaksi, double jumlahTagihan, String alasan) {
        super(String.format("Pembayaran gagal untuk transaksi [%s] sebesar Rp %,.2f: %s", 
                idTransaksi, jumlahTagihan, alasan));
        this.idTransaksi = idTransaksi;
        this.jumlahTagihan = jumlahTagihan;
        this.alasan = alasan;
    }

    public String getIdTransaksi() {
        return idTransaksi;
    }

    public double getJumlahTagihan() {
        return jumlahTagihan;
    }

    public String getAlasan() {
        return alasan;
    }
}
