import java.util.Objects;

/**
 * Item merepresentasikan entitas produk/layanan dalam pesanan.
 * Memenuhi SRP (Single Responsibility Principle):
 * Hanya bertanggung jawab menjaga data item dan kalkulasi subtotal item.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class Item {
    private final String id;
    private final String name;
    private final double price;
    private final int quantity;

    public Item(String id, String name, double price, int quantity) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID Item tidak boleh kosong.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Item tidak boleh kosong.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Harga tidak boleh negatif.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas harus lebih dari 0.");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.price = price;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return price * quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s - %s x%d @Rp %,.0f = Rp %,.0f", id, name, quantity, price, getSubtotal());
    }
}
