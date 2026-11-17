import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entitas Domain Order yang mengimplementasikan SRP, ISP.
 * - SRP: Hanya mengelola status, lifecycle, dan komposisi item pesanan.
 * - ISP: Mengimplementasikan Printable dan Auditable secara terpisah.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class Order implements Printable, Auditable {
    public enum Status {
        PENDING,
        PAID,
        CANCELLED
    }

    private final String orderId;
    private final String customerName;
    private final String customerEmail;
    private final List<Item> items;
    private Status status;
    private final LocalDateTime createdAt;

    public Order(String orderId, String customerName, String customerEmail) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID tidak boleh kosong.");
        }
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama pelanggan tidak boleh kosong.");
        }
        if (customerEmail == null || customerEmail.trim().isEmpty() || !customerEmail.contains("@")) {
            throw new IllegalArgumentException("Email pelanggan tidak valid.");
        }
        this.orderId = orderId.trim();
        this.customerName = customerName.trim();
        this.customerEmail = customerEmail.trim();
        this.items = new ArrayList<>();
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor untuk rehidrasi data dari persistensi
    public Order(String orderId, String customerName, String customerEmail, Status status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.items = new ArrayList<>();
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * Menambahkan item saat rehidrasi persistensi tanpa memicu validasi status bisnis.
     * @param item item yang direhidrasi
     */
    public void rehydrateItem(Item item) {
        if (item != null) {
            this.items.add(item);
        }
    }

    public void addItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item tidak boleh null.");
        }
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("Tidak dapat menambah item pada pesanan dengan status " + status);
        }
        this.items.add(item);
    }

    public boolean removeItem(String itemId) {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("Tidak dapat menghapus item pada pesanan dengan status " + status);
        }
        return this.items.removeIf(it -> it.getId().equalsIgnoreCase(itemId));
    }

    public double calculateSubtotal() {
        double sum = 0.0;
        for (Item item : items) {
            sum += item.getSubtotal();
        }
        return sum;
    }

    public int getItemCount() {
        int totalQty = 0;
        for (Item item : items) {
            totalQty += item.getQuantity();
        }
        return totalQty;
    }

    public void markAsPaid() {
        if (this.status == Status.CANCELLED) {
            throw new IllegalStateException("Pesanan yang sudah dibatalkan tidak dapat dibayar.");
        }
        this.status = Status.PAID;
    }

    public void cancel() {
        if (this.status == Status.PAID) {
            throw new IllegalStateException("Pesanan yang sudah dibayar tidak dapat dibatalkan.");
        }
        this.status = Status.CANCELLED;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // =========================================================================
    // IMPLEMENTASI INTERFACE SEGREGATION PRINCIPLE (ISP)
    // =========================================================================

    @Override
    public String getPrintContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ORDER #%s | Pelanggan: %s\n", orderId, customerName));
        sb.append(String.format("Status: %s | Tanggal: %s\n", status, createdAt.toLocalDate()));
        sb.append("--------------------------------------------------\n");
        for (Item item : items) {
            sb.append(String.format("  - %-20s x%2d  Rp %,10.0f\n", 
                item.getName(), item.getQuantity(), item.getSubtotal()));
        }
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("Subtotal: Rp %,.0f", calculateSubtotal()));
        return sb.toString();
    }

    @Override
    public String getAuditId() {
        return "AUDIT-" + orderId;
    }

    @Override
    public LocalDateTime getAuditTimestamp() {
        return createdAt;
    }

    @Override
    public String getAuditMetadata() {
        return String.format("[ORDER_AUDIT] ID=%s, Customer=%s <%s>, Items=%d, Subtotal=Rp%,.0f, Status=%s",
                orderId, customerName, customerEmail, items.size(), calculateSubtotal(), status);
    }
}
