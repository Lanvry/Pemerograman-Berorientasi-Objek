package model;

/**
 * Parameter Object / Request Context untuk merangkum seluruh parameter
 * proses check-out dan billing parkir (P14 Clean Refactoring).
 * 
 * Mengeliminasi code smell "Long Parameter List" pada:
 * IParkingBilling.prosesKeluar(tiket, jamKeluar, paymentProcessor, uangBayar, diskonStrategy).
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class CheckOutRequest {
    private final KarcisParkir tiket;
    private final int jamKeluar;
    private final IPaymentProcessor paymentProcessor;
    private final double uangBayar;
    private final DiskonStrategy diskonStrategy;

    public CheckOutRequest(KarcisParkir tiket, int jamKeluar, IPaymentProcessor paymentProcessor, double uangBayar, DiskonStrategy diskonStrategy) {
        if (tiket == null) {
            throw new IllegalArgumentException("Tiket parkir tidak boleh null!");
        }
        if (jamKeluar < 0 || jamKeluar > 24) {
            throw new IllegalArgumentException("Jam keluar harus berada dalam rentang 0-24! Diterima: " + jamKeluar);
        }
        this.tiket = tiket;
        this.jamKeluar = jamKeluar;
        this.paymentProcessor = (paymentProcessor != null) ? paymentProcessor : new CashPaymentProcessor();
        this.uangBayar = Math.max(0.0, uangBayar);
        this.diskonStrategy = (diskonStrategy != null) ? diskonStrategy : tentukanDefaultDiskon(tiket);
    }

    public CheckOutRequest(KarcisParkir tiket, int jamKeluar, String namaMetode, double uangBayar, DiskonStrategy diskonStrategy) {
        this(tiket, jamKeluar, PaymentProcessorFactory.getProcessor(namaMetode), uangBayar, diskonStrategy);
    }

    public CheckOutRequest(KarcisParkir tiket, int jamKeluar, IPaymentProcessor paymentProcessor, double uangBayar) {
        this(tiket, jamKeluar, paymentProcessor, uangBayar, null);
    }

    public CheckOutRequest(KarcisParkir tiket, int jamKeluar, String namaMetode, double uangBayar) {
        this(tiket, jamKeluar, PaymentProcessorFactory.getProcessor(namaMetode), uangBayar, null);
    }

    private static DiskonStrategy tentukanDefaultDiskon(KarcisParkir tiket) {
        if (tiket != null && tiket.getKendaraan() != null && tiket.getKendaraan().isMobilListrik()) {
            return new EVIncentiveDiskonStrategy();
        }
        return new TanpaDiskonStrategy();
    }

    public KarcisParkir getTiket() {
        return tiket;
    }

    public int getJamKeluar() {
        return jamKeluar;
    }

    public IPaymentProcessor getPaymentProcessor() {
        return paymentProcessor;
    }

    public double getUangBayar() {
        return uangBayar;
    }

    public DiskonStrategy getDiskonStrategy() {
        return diskonStrategy;
    }
}
