import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keranjang belanja milik pelanggan.
 * Mengelola koleksi ItemPesanan menggunakan Map untuk efisiensi lookup dan pembaruan kuantitas.
 * Menerapkan batas kapasitas dan enkapsulasi kuat (unmodifiable list/defensive copying).
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class Keranjang {
    public static final int DEFAULT_KAPASITAS_MAKSIMAL = 10;

    private final Pelanggan pemilik;
    private final int kapasitasMaksimal;
    private final Map<String, ItemPesanan> itemMap;

    public Keranjang(Pelanggan pemilik) {
        this(pemilik, DEFAULT_KAPASITAS_MAKSIMAL);
    }

    public Keranjang(Pelanggan pemilik, int kapasitasMaksimal) {
        if (pemilik == null) {
            throw new IllegalArgumentException("Pemilik keranjang tidak boleh null.");
        }
        if (kapasitasMaksimal <= 0) {
            throw new IllegalArgumentException("Kapasitas maksimal harus lebih besar dari nol.");
        }
        this.pemilik = pemilik;
        this.kapasitasMaksimal = kapasitasMaksimal;
        this.itemMap = new LinkedHashMap<>();
    }

    public Pelanggan getPemilik() {
        return pemilik;
    }

    public int getKapasitasMaksimal() {
        return kapasitasMaksimal;
    }

    /**
     * Menambahkan produk ke keranjang belanja dengan kuantitas tertentu.
     * Jika produk sudah ada di keranjang, kuantitasnya akan diakumulasikan.
     * 
     * @param produk barang yang dibeli
     * @param kuantitas jumlah barang
     * @throws KapasitasPenuhException jika jumlah variasi produk melebihi kapasitas
     */
    public synchronized void tambahItem(Produk produk, int kuantitas) throws KapasitasPenuhException {
        if (produk == null) {
            throw new IllegalArgumentException("Produk yang ditambahkan tidak boleh null.");
        }
        if (kuantitas <= 0) {
            throw new IllegalArgumentException("Kuantitas yang ditambahkan harus lebih dari nol.");
        }

        String kode = produk.getKodeProduk();
        if (!itemMap.containsKey(kode) && itemMap.size() >= kapasitasMaksimal) {
            throw new KapasitasPenuhException(kapasitasMaksimal, itemMap.size());
        }

        if (itemMap.containsKey(kode)) {
            ItemPesanan existing = itemMap.get(kode);
            int kuantitasTotal = existing.getKuantitas() + kuantitas;
            itemMap.put(kode, new ItemPesanan(produk, kuantitasTotal));
        } else {
            itemMap.put(kode, new ItemPesanan(produk, kuantitas));
        }
    }

    /**
     * Menghapus item dari keranjang berdasarkan kode produk.
     */
    public synchronized boolean hapusItem(String kodeProduk) {
        if (kodeProduk == null) return false;
        return itemMap.remove(kodeProduk) != null;
    }

    /**
     * Memperbarui kuantitas item yang ada dalam keranjang.
     */
    public synchronized void perbaruiKuantitas(String kodeProduk, int kuantitasBaru) {
        if (kodeProduk == null || !itemMap.containsKey(kodeProduk)) {
            throw new IllegalArgumentException("Produk tidak ditemukan di dalam keranjang.");
        }
        if (kuantitasBaru <= 0) {
            hapusItem(kodeProduk);
        } else {
            ItemPesanan existing = itemMap.get(kodeProduk);
            itemMap.put(kodeProduk, new ItemPesanan(existing.getProduk(), kuantitasBaru));
        }
    }

    /**
     * Menghitung total biaya seluruh barang di dalam keranjang belanja.
     */
    public synchronized double hitungTotal() {
        double total = 0.0;
        for (ItemPesanan item : itemMap.values()) {
            total += item.getSubtotal();
        }
        return total;
    }

    /**
     * Mengembalikan daftar item dalam keranjang secara terenkapsulasi (unmodifiable list).
     */
    public synchronized List<ItemPesanan> getDaftarItem() {
        return Collections.unmodifiableList(new ArrayList<>(itemMap.values()));
    }

    /**
     * Mengosongkan keranjang belanja (setelah checkout berhasil atau dibatalkan).
     */
    public synchronized void kosongkan() {
        itemMap.clear();
    }

    public synchronized int getJumlahVariasiItem() {
        return itemMap.size();
    }

    public synchronized boolean isKosong() {
        return itemMap.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Keranjang [Pemilik: %s, Total Variasi: %d/%d, Total Harga: Rp %,.2f]", 
                pemilik.getNama(), itemMap.size(), kapasitasMaksimal, hitungTotal());
    }
}
