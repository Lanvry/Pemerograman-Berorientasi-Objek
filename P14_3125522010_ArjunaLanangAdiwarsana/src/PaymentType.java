/**
 * Enum PaymentType
 * Merepresentasikan kanal pembayaran yang didukung sistem.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public enum PaymentType {
    TRANSFER_BANK("Transfer Virtual Account Bank"),
    E_WALLET("Dompet Digital (QRIS / GoPay / OVO)"),
    KARTU_KREDIT("Kartu Kredit / Debit Online"),
    COD("Bayar di Tempat (Cash on Delivery)");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
