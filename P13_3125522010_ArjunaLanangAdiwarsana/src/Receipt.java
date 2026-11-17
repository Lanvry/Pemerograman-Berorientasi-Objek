import java.time.LocalDateTime;

/**
 * Entitas Value Object yang merepresentasikan tanda bukti pembayaran transaksi.
 * Memenuhi SRP & ISP (mengimplementasikan Printable).
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class Receipt implements Printable {
    private final Order order;
    private final double subtotal;
    private final double discountAmount;
    private final String discountStrategyName;
    private final double netTotal;
    private final LocalDateTime transactionTime;

    public Receipt(Order order, double subtotal, double discountAmount, String discountStrategyName, double netTotal) {
        this.order = order;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.discountStrategyName = discountStrategyName;
        this.netTotal = netTotal;
        this.transactionTime = LocalDateTime.now();
    }

    public Order getOrder() {
        return order;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public String getDiscountStrategyName() {
        return discountStrategyName;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    @Override
    public String getPrintContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("            STRUK RESMI PEMBAYARAN POS           \n");
        sb.append("         P13 SOLID PRINCIPLES ARCHITECTURE        \n");
        sb.append("==================================================\n");
        sb.append(String.format("No. Transaksi : %s\n", order.getOrderId()));
        sb.append(String.format("Pelanggan     : %s\n", order.getCustomerName()));
        sb.append(String.format("Email         : %s\n", order.getCustomerEmail()));
        sb.append(String.format("Waktu         : %s\n", transactionTime.toString()));
        sb.append(String.format("Status Order  : %s\n", order.getStatus()));
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("%-22s %4s %10s %10s\n", "Item", "Qty", "Harga", "Subtotal"));
        sb.append("--------------------------------------------------\n");
        for (Item it : order.getItems()) {
            sb.append(String.format("%-22s %4d Rp%,9.0f Rp%,9.0f\n",
                    it.getName(), it.getQuantity(), it.getPrice(), it.getSubtotal()));
        }
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("Total Belanja (Gross) : Rp %,14.0f\n", subtotal));
        sb.append(String.format("Diskon (%-14s) : -Rp %,13.0f\n", discountStrategyName, discountAmount));
        sb.append("==================================================\n");
        sb.append(String.format("TOTAL DIBAYAR (NET)   : Rp %,14.0f\n", netTotal));
        sb.append("==================================================\n");
        sb.append("      Terima kasih telah berbelanja bersama kami! \n");
        sb.append("==================================================\n");
        return sb.toString();
    }
}
