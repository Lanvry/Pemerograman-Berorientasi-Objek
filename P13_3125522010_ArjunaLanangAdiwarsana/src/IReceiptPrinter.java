/**
 * Kontrak Printer Penerima Struk (IReceiptPrinter).
 * Memenuhi Interface Segregation Principle (ISP) & SRP:
 * Bergantung secara murni pada role-interface `Printable`.
 * Mesin printer tidak perlu mengetahui detail database, diskon, atau email.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public interface IReceiptPrinter {
    /**
     * Mencetak dokumen struk yang mengimplementasikan Printable.
     * @param printable dokumen yang akan dicetak
     */
    void printReceipt(Printable printable);
}
