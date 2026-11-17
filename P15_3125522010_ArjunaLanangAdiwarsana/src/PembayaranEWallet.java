import java.util.UUID;

/**
 * Implementasi metode pembayaran Dompet Digital (E-Wallet).
 * Menyimpan saldo dompet dan memverifikasi autentikasi PIN sebelum debet saldo.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class PembayaranEWallet implements MetodePembayaran {
    private final String provider;
    private final String nomorAkun;
    private double saldoEWallet;
    private final String pin;

    public PembayaranEWallet(String provider, String nomorAkun, double saldoAwal, String pin) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider E-Wallet tidak boleh kosong.");
        }
        if (nomorAkun == null || nomorAkun.trim().length() < 9) {
            throw new IllegalArgumentException("Nomor akun E-Wallet tidak valid.");
        }
        if (saldoAwal < 0) {
            throw new IllegalArgumentException("Saldo awal E-Wallet tidak boleh negatif.");
        }
        if (pin == null || pin.trim().length() != 6) {
            throw new IllegalArgumentException("PIN E-Wallet harus berupa 6 digit.");
        }

        this.provider = provider.trim();
        this.nomorAkun = nomorAkun.trim();
        this.saldoEWallet = saldoAwal;
        this.pin = pin.trim();
    }

    @Override
    public String getNamaMetode() {
        return "E-Wallet (" + provider + ")";
    }

    public String getProvider() {
        return provider;
    }

    public String getNomorAkun() {
        return nomorAkun;
    }

    public synchronized double getSaldoEWallet() {
        return saldoEWallet;
    }

    /**
     * Memverifikasi PIN dan mendebet saldo E-Wallet jika mencukupi.
     */
    public synchronized boolean otorisasiDanDebet(String inputPin, double nominal) {
        if (!this.pin.equals(inputPin)) {
            return false;
        }
        if (this.saldoEWallet < nominal) {
            return false;
        }
        this.saldoEWallet -= nominal;
        return true;
    }

    @Override
    public synchronized String prosesPembayaran(String idTransaksi, double jumlah) throws PembayaranGagalException {
        if (saldoEWallet < jumlah) {
            throw new PembayaranGagalException(idTransaksi, jumlah, 
                    String.format("Saldo %s [%s] tidak mencukupi. Saldo saat ini: Rp %,.2f", 
                            provider, nomorAkun, saldoEWallet));
        }

        this.saldoEWallet -= jumlah;
        return "EWL-" + provider.toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
