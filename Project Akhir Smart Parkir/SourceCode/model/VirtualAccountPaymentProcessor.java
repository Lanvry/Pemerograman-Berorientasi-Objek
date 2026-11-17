package model;

/**
 * Pemroses pembayaran digital perbankan Virtual Account (BCA, Mandiri, BNI, BRI, Permata).
 * 
 * Sesuai LSP: Menyediakan substitusi transparan untuk kanal transfer antar-bank.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class VirtualAccountPaymentProcessor implements IPaymentProcessor {

    private final String namaBank;

    public VirtualAccountPaymentProcessor() {
        this("VA");
    }

    public VirtualAccountPaymentProcessor(String namaBank) {
        this.namaBank = (namaBank != null && !namaBank.trim().isEmpty()) ? namaBank.trim().toUpperCase() : "VA";
    }

    @Override
    public boolean prosesPembayaran(KarcisParkir tiket, double uangBayar) throws PembayaranKurangException {
        if (tiket == null) {
            throw new IllegalArgumentException("Tiket parkir tidak boleh null.");
        }
        String metodeFinal = namaBank.startsWith("VA") ? namaBank : ("VA_" + namaBank);
        return tiket.prosesPembayaran(metodeFinal, uangBayar);
    }

    @Override
    public String getNamaMetode() {
        return namaBank;
    }
}
