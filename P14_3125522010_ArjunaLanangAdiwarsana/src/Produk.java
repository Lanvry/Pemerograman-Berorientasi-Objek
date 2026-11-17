import java.util.Objects;

/**
 * Domain Entity: Produk
 * 
 * Mengatasi Code Smell:
 * God Class (Solusi Eksplisit Kasus B Slide 17)
 * "Class Produk 35 method (laporan, database, notifikasi) -> Extract Class (SRP)"
 * 
 * SEBELUM:
 * Class Produk memegang 35 method yang mencampurkan:
 * - Data entity produk
 * - Operasi database (raw SQL, connection string, query CRUD)
 * - Pembuatan laporan (PDF export, CSV formatter, tabular printer)
 * - Pengiriman notifikasi email/SMS ketika stok menipis
 * 
 * SESUDAH (Setelah Refactoring):
 * Mengikuti Single Responsibility Principle (SRP):
 * Class Produk HANYA bertanggung jawab sebagai Domain Entity (state produk,
 * validasi mutasi stok, dan kalkulasi subtotal harga).
 * 
 * Tanggung jawab lain diekstrak ke class terpisah:
 * 1. ProdukRepository -> Tanggung jawab persistensi / database
 * 2. ProdukReportService -> Tanggung jawab pembuatan laporan / formatting
 * 3. StockNotificationService -> Tanggung jawab notifikasi stok
 * 
 * Referensi Materi P14: Nirwana Haidar Hari, S.Pd., M.Kom.
 * D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class Produk {
    private final ProductCode code;
    private final String name;
    private final Money price;
    private int stock;
    private final String category;

    public Produk(ProductCode code, String name, Money price, int stock, String category) {
        if (code == null) {
            throw new IllegalArgumentException("Kode produk tidak boleh null.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama produk tidak boleh kosong.");
        }
        if (price == null) {
            throw new IllegalArgumentException("Harga produk tidak boleh null.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stok awal tidak boleh bernilai negatif: " + stock);
        }

        this.code = code;
        this.name = name.trim();
        this.price = price;
        this.stock = stock;
        this.category = (category == null || category.trim().isEmpty()) ? "General" : category.trim();
    }

    public ProductCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getCategory() {
        return category;
    }

    public boolean isAvailable(int quantity) {
        return quantity > 0 && this.stock >= quantity;
    }

    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas pengurangan stok harus lebih dari 0: " + quantity);
        }
        if (this.stock < quantity) {
            throw new IllegalStateException(
                "Stok tidak mencukupi untuk produk " + name + " (" + code + "). Tersedia: " + stock + ", diminta: " + quantity
            );
        }
        this.stock -= quantity;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas penambahan stok harus lebih dari 0: " + quantity);
        }
        this.stock += quantity;
    }

    public Money calculateSubtotal(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas harus lebih dari 0: " + quantity);
        }
        return this.price.multiply(quantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Produk)) return false;
        Produk produk = (Produk) o;
        return Objects.equals(code, produk.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Kategori: %s | Harga: %s | Stok: %d unit",
                code, name, category, price, stock);
    }
}
