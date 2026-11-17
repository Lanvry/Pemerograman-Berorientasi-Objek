package model;

/**
 * Pemroses pembayaran digital QRIS (Quick Response Code Indonesian Standard).
 * 
 * Terintegrasi dengan payment gateway (Midtrans / QRIS Dinamis).
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class QRISPaymentProcessor implements IPaymentProcessor {

    private final String kodeMetode;

    public QRISPaymentProcessor() {
        this("QRIS");
    }

    public QRISPaymentProcessor(String kodeMetode) {
        this.kodeMetode = (kodeMetode != null && !kodeMetode.trim().isEmpty()) ? kodeMetode.trim().toUpperCase() : "QRIS";
    }

    @Override
    public boolean prosesPembayaran(KarcisParkir tiket, double uangBayar) throws PembayaranKurangException {
        if (tiket == null) {
            throw new IllegalArgumentException("Tiket parkir tidak boleh null.");
        }
        return tiket.prosesPembayaran(kodeMetode, uangBayar);
    }

    @Override
    public String getNamaMetode() {
        return kodeMetode;
    }
}
