package model;

/**
 * Factory class untuk menghasilkan instance IPaymentProcessor berdasarkan string metode.
 * 
 * Menerapkan OCP: Pemetaan string metode GUI/API ke abstraksi IPaymentProcessor yang sesuai.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class PaymentProcessorFactory {

    public static IPaymentProcessor getProcessor(String metode) {
        if (metode == null || metode.trim().isEmpty()) {
            return new CashPaymentProcessor();
        }
        String m = metode.trim().toUpperCase();
        if (m.contains("QRIS")) {
            return new QRISPaymentProcessor(m);
        } else if (m.contains("VA") || m.contains("VIRTUAL_ACCOUNT")) {
            return new VirtualAccountPaymentProcessor(m);
        } else if (m.contains("E_MONEY") || m.contains("EMONEY") || m.contains("E-MONEY") || m.contains("TAPCASH") || m.contains("FLAZZ") || m.contains("BRIZZI")) {
            return new EMoneyPaymentProcessor();
        } else {
            return new CashPaymentProcessor();
        }
    }
}
