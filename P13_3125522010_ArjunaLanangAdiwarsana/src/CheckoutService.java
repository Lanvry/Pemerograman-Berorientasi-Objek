/**
 * High-Level Service yang mengorkestrasi alur checkout transaksi.
 * Memenuhi DIP (Dependency Inversion Principle) secara utuh:
 * - Tidak membuat obyek konkret di dalam kelasnya.
 * - Menerima dependensi interface abstrak melalui Constructor Injection:
 *   1. IOrderRepository (SRP & DIP)
 *   2. IDiscountStrategy (OCP & LSP)
 *   3. IReceiptPrinter (SRP & ISP)
 *   4. INotifier (SRP & DIP)
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class CheckoutService {
    private final IOrderRepository repository;
    private IDiscountStrategy discountStrategy;
    private final IReceiptPrinter receiptPrinter;
    private final INotifier notifier;

    public CheckoutService(
            IOrderRepository repository,
            IDiscountStrategy discountStrategy,
            IReceiptPrinter receiptPrinter,
            INotifier notifier
    ) {
        if (repository == null) {
            throw new IllegalArgumentException("IOrderRepository tidak boleh null.");
        }
        if (discountStrategy == null) {
            throw new IllegalArgumentException("IDiscountStrategy tidak boleh null.");
        }
        if (receiptPrinter == null) {
            throw new IllegalArgumentException("IReceiptPrinter tidak boleh null.");
        }
        if (notifier == null) {
            throw new IllegalArgumentException("INotifier tidak boleh null.");
        }
        this.repository = repository;
        this.discountStrategy = discountStrategy;
        this.receiptPrinter = receiptPrinter;
        this.notifier = notifier;
    }

    /**
     * Memungkinkan pergantian strategi diskon saat runtime (OCP & Polimorfisme).
     * @param discountStrategy strategi baru
     */
    public void setDiscountStrategy(IDiscountStrategy discountStrategy) {
        if (discountStrategy == null) {
            throw new IllegalArgumentException("Strategi diskon tidak boleh null.");
        }
        this.discountStrategy = discountStrategy;
    }

    public IDiscountStrategy getDiscountStrategy() {
        return discountStrategy;
    }

    public IOrderRepository getRepository() {
        return repository;
    }

    public IReceiptPrinter getReceiptPrinter() {
        return receiptPrinter;
    }

    public INotifier getNotifier() {
        return notifier;
    }

    /**
     * Memproses checkout pesanan dari validasi, kalkulasi diskon,
     * transisi status, persistensi, notifikasi, hingga pencetakan struk.
     *
     * @param order pesanan yang akan diproses
     * @return obyek Receipt tanda bukti transaksi
     */
    public Receipt processCheckout(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Pesanan tidak boleh null.");
        }
        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("Pesanan tidak memiliki item belanja (keranjang kosong).");
        }
        if (order.getStatus() != Order.Status.PENDING) {
            throw new IllegalStateException("Hanya pesanan berstatus PENDING yang dapat dicheckout. Status saat ini: " + order.getStatus());
        }

        double subtotal = order.calculateSubtotal();
        double discountAmount = discountStrategy.calculateDiscount(subtotal);

        // Pertahankan LSP Invariant: Diskon tidak boleh melebihi subtotal
        if (discountAmount > subtotal) {
            discountAmount = subtotal;
        }
        if (discountAmount < 0) {
            discountAmount = 0.0;
        }

        double netTotal = subtotal - discountAmount;

        // 1. Ubah state domain
        order.markAsPaid();

        // 2. Persistensi melalui Repository (DIP)
        repository.save(order);

        // 3. Buat struk transaksi (SRP & ISP)
        Receipt receipt = new Receipt(
                order,
                subtotal,
                discountAmount,
                discountStrategy.getStrategyName(),
                netTotal
        );

        // 4. Cetak Struk via Printer Abstraksi (ISP)
        receiptPrinter.printReceipt(receipt);

        // 5. Kirim Notifikasi via Notifier Abstraksi (DIP)
        String emailMsg = String.format("Halo %s, transaksi #%s berhasil dibayar sebesar Rp %,.0f.",
                order.getCustomerName(), order.getOrderId(), netTotal);
        notifier.sendNotification(order.getCustomerEmail(), "Konfirmasi Pembayaran Order #" + order.getOrderId(), emailMsg);

        return receipt;
    }
}
