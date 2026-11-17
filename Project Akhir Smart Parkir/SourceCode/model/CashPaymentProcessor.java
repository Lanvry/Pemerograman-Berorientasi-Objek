package model;

/**
 * Pemroses pembayaran tunai fisik (Cash).
 * 
 * Memverifikasi kecukupan uang tunai dan menandai status lunas tiket.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class CashPaymentProcessor implements IPaymentProcessor {

    @Override
    public boolean prosesPembayaran(KarcisParkir tiket, double uangBayar) throws PembayaranKurangException {
        if (tiket == null) {
            throw new IllegalArgumentException("Tiket parkir tidak boleh null.");
        }
        return tiket.prosesPembayaran("CASH", uangBayar);
    }

    @Override
    public String getNamaMetode() {
        return "CASH";
    }
}
