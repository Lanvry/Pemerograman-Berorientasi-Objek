import java.util.Objects;

/**
 * Value Object / Entitas yang merepresentasikan item spesifik dalam keranjang atau transaksi.
 * Menyimpan snapshot harga saat pembelian agar tidak terpengaruh fluktuasi harga produk di masa depan.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class ItemPesanan {
    private final Produk produk;
    private final int kuantitas;
    private final double hargaSaatBeli;

    public ItemPesanan(Produk produk, int kuantitas) {
        if (produk == null) {
            throw new IllegalArgumentException("Produk referensi tidak boleh null.");
        }
        if (kuantitas <= 0) {
            throw new IllegalArgumentException("Kuantitas pesanan harus lebih besar dari nol.");
        }
        this.produk = produk;
        this.kuantitas = kuantitas;
        this.hargaSaatBeli = produk.getHarga();
    }

    public ItemPesanan(Produk produk, int kuantitas, double hargaSaatBeli) {
        if (produk == null) {
            throw new IllegalArgumentException("Produk referensi tidak boleh null.");
        }
        if (kuantitas <= 0) {
            throw new IllegalArgumentException("Kuantitas pesanan harus lebih besar dari nol.");
        }
        if (hargaSaatBeli < 0) {
            throw new IllegalArgumentException("Harga saat beli tidak boleh bernilai negatif.");
        }
        this.produk = produk;
        this.kuantitas = kuantitas;
        this.hargaSaatBeli = hargaSaatBeli;
    }

    public Produk getProduk() {
        return produk;
    }

    public int getKuantitas() {
        return kuantitas;
    }

    public double getHargaSaatBeli() {
        return hargaSaatBeli;
    }

    /**
     * Menghitung subtotal dari item pesanan.
     */
    public double getSubtotal() {
        return hargaSaatBeli * kuantitas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPesanan that = (ItemPesanan) o;
        return kuantitas == that.kuantitas &&
                Double.compare(that.hargaSaatBeli, hargaSaatBeli) == 0 &&
                Objects.equals(produk, that.produk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produk, kuantitas, hargaSaatBeli);
    }

    @Override
    public String toString() {
        return String.format("%s x %d @ Rp %,.2f = Rp %,.2f", 
                produk.getNamaProduk(), kuantitas, hargaSaatBeli, getSubtotal());
    }
}
