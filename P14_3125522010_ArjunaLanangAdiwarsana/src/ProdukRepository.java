import java.util.List;
import java.util.Optional;

/**
 * Interface ProdukRepository
 * 
 * Hasil ekstraksi SRP dari God Class Produk (Kasus B).
 * Bertanggung jawab khusus untuk kontrak abstraksi akses data & persistensi produk.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public interface ProdukRepository {
    void save(Produk produk);
    Optional<Produk> findByCode(ProductCode code);
    List<Produk> findAll();
    List<Produk> findLowStock(int threshold);
    boolean existsByCode(ProductCode code);
    int count();
    void delete(ProductCode code);
    void deleteByCode(ProductCode code);
}
