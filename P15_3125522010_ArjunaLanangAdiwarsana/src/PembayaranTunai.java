/**
 * Implementasi metode pembayaran Tunai (Cash).
 * Memvalidasi apakah jumlah uang tunai yang diserahkan cukup untuk melunasi tagihan.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class PembayaranTunai implements MetodePembayaran {
    private final double uangDiterima;

    public PembayaranTunai(double uangDiterima) {
        if (uangDiterima < 0) {
            throw new IllegalArgumentException("Uang yang diterima tidak boleh negatif.");
        }
        this.uangDiterima = uangDiterima;
    }

    @Override
    public String getNamaMetode() {
        return "Tunai (Cash)";
    }

    public double getUangDiterima() {
        return uangDiterima;
    }

    public double hitungKembalian(double tagihan) {
        return Math.max(0, uangDiterima - tagihan);
    }

    @Override
    public String prosesPembayaran(String idTransaksi, double jumlah) throws PembayaranGagalException {
        if (uangDiterima < jumlah) {
            double kekurangan = jumlah - uangDiterima;
            throw new PembayaranGagalException(idTransaksi, jumlah, 
                    String.format("Uang tunai tidak cukup. Diterima: Rp %,.2f, Kurang: Rp %,.2f", 
                            uangDiterima, kekurangan));
        }

        double kembalian = uangDiterima - jumlah;
        return String.format("CASH-%s-KEMBALIAN-Rp%,.2f", idTransaksi, kembalian);
    }
}
