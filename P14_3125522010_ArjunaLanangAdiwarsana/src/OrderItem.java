import java.util.Objects;

/**
 * Domain Entity: OrderItem
 * 
 * Merepresentasikan item dalam transaksi pesanan yang mengikat objek Produk,
 * kuantitas pembelian, dan snapshot harga satuan (Money).
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class OrderItem {
    private final Produk product;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(Produk product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Produk dalam item pesanan tidak boleh null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas pesanan harus lebih besar dari 0: " + quantity);
        }
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice(); // Snapshot harga saat dipesan
    }

    public Produk getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money calculateSubtotal() {
        return unitPrice.multiply(quantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem orderItem = (OrderItem) o;
        return quantity == orderItem.quantity &&
               Objects.equals(product, orderItem.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, quantity);
    }

    @Override
    public String toString() {
        return String.format("%s (x%d) = %s", product.getName(), quantity, calculateSubtotal());
    }
}
