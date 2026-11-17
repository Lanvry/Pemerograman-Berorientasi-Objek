import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository Implementasi: InMemoryProdukRepository
 * 
 * Mengisolasi penyimpanan data produk in-memory, membebaskan domain entity
 * dari beban koneksi basis data atau SQL queries.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class InMemoryProdukRepository implements ProdukRepository {
    private final Map<ProductCode, Produk> storage = new LinkedHashMap<>();

    @Override
    public void save(Produk produk) {
        if (produk == null) {
            throw new IllegalArgumentException("Produk yang akan disimpan tidak boleh null.");
        }
        storage.put(produk.getCode(), produk);
    }

    @Override
    public Optional<Produk> findByCode(ProductCode code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(storage.get(code));
    }

    @Override
    public List<Produk> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }

    @Override
    public List<Produk> findLowStock(int threshold) {
        List<Produk> lowStock = new ArrayList<>();
        for (Produk p : storage.values()) {
            if (p.getStock() <= threshold) {
                lowStock.add(p);
            }
        }
        return lowStock;
    }

    @Override
    public boolean existsByCode(ProductCode code) {
        return code != null && storage.containsKey(code);
    }

    @Override
    public int count() {
        return storage.size();
    }

    @Override
    public void delete(ProductCode code) {
        deleteByCode(code);
    }

    @Override
    public void deleteByCode(ProductCode code) {
        if (code != null) {
            storage.remove(code);
        }
    }
}
