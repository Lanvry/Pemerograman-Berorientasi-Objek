import java.util.Objects;

/**
 * Entitas Produk yang merepresentasikan barang dalam sistem katalog inventaris.
 * Mematuhi prinsip Information Expert: bertanggung jawab penuh atas integritas stoknya sendiri.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class Produk {
    private final String kodeProduk;
    private final String namaProduk;
    private final double harga;
    private final String kategori;
    private int stok;

    public Produk(String kodeProduk, String namaProduk, double harga, int stokAwal, String kategori) {
        if (kodeProduk == null || kodeProduk.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode produk tidak boleh kosong.");
        }
        if (namaProduk == null || namaProduk.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama produk tidak boleh kosong.");
        }
        if (harga < 0) {
            throw new IllegalArgumentException("Harga produk tidak boleh negatif.");
        }
        if (stokAwal < 0) {
            throw new IllegalArgumentException("Stok awal tidak boleh negatif.");
        }

        this.kodeProduk = kodeProduk.trim();
        this.namaProduk = namaProduk.trim();
        this.harga = harga;
        this.stok = stokAwal;
        this.kategori = (kategori == null || kategori.trim().isEmpty()) ? "Umum" : kategori.trim();
    }

    public String getKodeProduk() {
        return kodeProduk;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public double getHarga() {
        return harga;
    }

    public String getKategori() {
        return kategori;
    }

    public synchronized int getStok() {
        return stok;
    }

    /**
     * Memeriksa ketersediaan kuantitas stok produk.
     */
    public synchronized boolean cekKetersediaan(int kuantitas) {
        return kuantitas > 0 && this.stok >= kuantitas;
    }

    /**
     * Mengurangi stok produk saat dipesan/dibeli.
     * 
     * @param kuantitas jumlah yang ingin dikurangi
     * @throws StokTidakMencukupiException jika permintaan melebihi sisa stok
     */
    public synchronized void kurangiStok(int kuantitas) throws StokTidakMencukupiException {
        if (kuantitas <= 0) {
            throw new IllegalArgumentException("Kuantitas pengurangan stok harus lebih besar dari nol.");
        }
        if (this.stok < kuantitas) {
            throw new StokTidakMencukupiException(this.kodeProduk, this.stok, kuantitas);
        }
        this.stok -= kuantitas;
    }

    /**
     * Menambahkan stok kembali (restock atau rollback transaksi gagal).
     * 
     * @param kuantitas jumlah yang dikembalikan ke stok
     */
    public synchronized void tambahStok(int kuantitas) {
        if (kuantitas <= 0) {
            throw new IllegalArgumentException("Kuantitas penambahan stok harus lebih besar dari nol.");
        }
        this.stok += kuantitas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produk produk = (Produk) o;
        return Objects.equals(kodeProduk, produk.kodeProduk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kodeProduk);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Kategori: %s | Harga: Rp %,.2f | Stok: %d", 
                kodeProduk, namaProduk, kategori, harga, stok);
    }
}
