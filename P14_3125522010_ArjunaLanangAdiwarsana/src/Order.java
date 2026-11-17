import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain Entity: Order
 * 
 * Merepresentasikan transaksi pesanan lengkap dengan perincian item, subtotal,
 * diskon voucher, pajak pertambahan nilai (PPN), dan total bersih.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class Order {
    private final String orderId;
    private final String customerName;
    private final CustomerEmail customerEmail;
    private final String shippingAddress;
    private final List<OrderItem> items;
    private final VoucherCode voucherCode;
    private final PaymentType paymentType;
    private final String customerNote;
    private final LocalDateTime orderDate;

    private Money subtotal;
    private Money discountAmount;
    private Money taxAmount;
    private Money grandTotal;
    private OrderStatus status;

    public Order(String orderId,
                 String customerName,
                 CustomerEmail customerEmail,
                 String shippingAddress,
                 List<OrderItem> items,
                 VoucherCode voucherCode,
                 PaymentType paymentType,
                 String customerNote) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.shippingAddress = shippingAddress;
        this.items = new ArrayList<>(items);
        this.voucherCode = voucherCode;
        this.paymentType = paymentType;
        this.customerNote = customerNote;
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.CONFIRMED;

        this.subtotal = calculateSubtotal();
        this.discountAmount = Money.ZERO;
        this.taxAmount = Money.ZERO;
        this.grandTotal = this.subtotal;
    }

    public Money calculateSubtotal() {
        Money sum = Money.ZERO;
        for (OrderItem item : items) {
            sum = sum.add(item.calculateSubtotal());
        }
        return sum;
    }

    public void setCalculatedTotals(Money discount, Money tax, Money total) {
        this.discountAmount = Objects.requireNonNull(discount, "Diskon tidak boleh null.");
        this.taxAmount = Objects.requireNonNull(tax, "Pajak tidak boleh null.");
        this.grandTotal = Objects.requireNonNull(total, "Total akhir tidak boleh null.");
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "Status tidak boleh null.");
    }

    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public CustomerEmail getCustomerEmail() { return customerEmail; }
    public String getShippingAddress() { return shippingAddress; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public VoucherCode getVoucherCode() { return voucherCode; }
    public PaymentType getPaymentType() { return paymentType; }
    public String getCustomerNote() { return customerNote; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public Money getSubtotal() { return subtotal; }
    public Money getDiscountAmount() { return discountAmount; }
    public Money getTaxAmount() { return taxAmount; }
    public Money getGrandTotal() { return grandTotal; }
    public OrderStatus getStatus() { return status; }

    @Override
    public String toString() {
        return String.format("Order[%s] %s | Subtotal: %s | Diskon: %s | PPN: %s | Total: %s | Status: %s",
                orderId, customerName, subtotal, discountAmount, taxAmount, grandTotal, status);
    }
}
