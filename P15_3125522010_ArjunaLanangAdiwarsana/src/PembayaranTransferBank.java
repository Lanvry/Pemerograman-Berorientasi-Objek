import java.util.UUID;

/**
 * Implementasi metode pembayaran Transfer Bank / Virtual Account.
 * Memvalidasi format nomor rekening dan limit transaksi harian.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class PembayaranTransferBank implements MetodePembayaran {
    private final String namaBank;
    private final String nomorRekeningVA;
    private final double saldoRekening;

    public PembayaranTransferBank(String namaBank, String nomorRekeningVA, double saldoRekening) {
        if (namaBank == null || namaBank.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama bank tidak boleh kosong.");
        }
        if (nomorRekeningVA == null || nomorRekeningVA.trim().length() < 5) {
            throw new IllegalArgumentException("Nomor rekening / Virtual Account tidak valid.");
        }
        if (saldoRekening < 0) {
            throw new IllegalArgumentException("Saldo rekening tidak boleh negatif.");
        }

        this.namaBank = namaBank.trim().toUpperCase();
        this.nomorRekeningVA = nomorRekeningVA.trim();
        this.saldoRekening = saldoRekening;
    }

    @Override
    public String getNamaMetode() {
        return "Transfer Bank (" + namaBank + ")";
    }

    public String getNamaBank() {
        return namaBank;
    }

    public String getNomorRekeningVA() {
        return nomorRekeningVA;
    }

    public double getSaldoRekening() {
        return saldoRekening;
    }

    @Override
    public String prosesPembayaran(String idTransaksi, double jumlah) throws PembayaranGagalException {
        if (saldoRekening < jumlah) {
            throw new PembayaranGagalException(idTransaksi, jumlah, 
                    String.format("Saldo rekening %s [%s] tidak mencukupi (Saldo: Rp %,.2f)", 
                            namaBank, nomorRekeningVA, saldoRekening));
        }

        String refCode = "TRF-" + namaBank + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return refCode;
    }
}
