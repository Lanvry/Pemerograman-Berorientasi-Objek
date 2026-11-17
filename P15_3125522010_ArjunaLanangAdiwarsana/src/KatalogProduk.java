import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Katalog dan Repositori Produk yang mengelola inventaris toko.
 * Menerapkan enkapsulasi koleksi untuk mencegah modifikasi state internal secara ilegal.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class KatalogProduk {
    private final Map<String, Produk> produkMap;

    public KatalogProduk() {
        this.produkMap = new LinkedHashMap<>();
    }

    /**
     * Mendaftarkan produk baru ke dalam katalog inventaris.
     */
    public synchronized void tambahProduk(Produk produk) {
        if (produk == null) {
            throw new IllegalArgumentException("Produk tidak boleh null.");
        }
        produkMap.put(produk.getKodeProduk(), produk);
    }

    /**
     * Mencari produk berdasarkan kode identifikasi unik.
     */
    public synchronized Produk cariProduk(String kodeProduk) {
        if (kodeProduk == null) return null;
        return produkMap.get(kodeProduk);
    }

    /**
     * Mengembalikan seluruh daftar produk dalam katalog secara aman (unmodifiable list).
     */
    public synchronized List<Produk> getSemuaProduk() {
        return Collections.unmodifiableList(new ArrayList<>(produkMap.values()));
    }

    /**
     * Menyaring produk berdasarkan kategori tertentu.
     */
    public synchronized List<Produk> cariBerdasarkanKategori(String kategori) {
        if (kategori == null) return Collections.emptyList();
        List<Produk> hasil = new ArrayList<>();
        for (Produk p : produkMap.values()) {
            if (p.getKategori().equalsIgnoreCase(kategori.trim())) {
                hasil.add(p);
            }
        }
        return Collections.unmodifiableList(hasil);
    }

    public synchronized int getTotalProduk() {
        return produkMap.size();
    }
}
