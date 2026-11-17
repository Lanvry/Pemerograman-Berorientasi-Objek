/**
 * Implementasi Printer berbasis Konsol Terminal (System.out).
 * Memenuhi SRP: Hanya bertanggung jawab mencetak teks struk ke standar output.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class ConsoleReceiptPrinter implements IReceiptPrinter {
    @Override
    public void printReceipt(Printable printable) {
        if (printable == null) {
            throw new IllegalArgumentException("Dokumen printable tidak boleh null.");
        }
        System.out.println(printable.getPrintContent());
    }
}
