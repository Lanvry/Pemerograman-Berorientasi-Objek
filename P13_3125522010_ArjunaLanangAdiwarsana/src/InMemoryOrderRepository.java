import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementasi in-memory untuk IOrderRepository.
 * Sangat ideal untuk unit testing, mock testing, atau state non-persistent.
 * Membuktikan DIP: Dapat disubstitusikan ke CheckoutService tanpa mengubah satu baris pun kode service.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class InMemoryOrderRepository implements IOrderRepository {
    private final Map<String, Order> storage = new LinkedHashMap<>();

    @Override
    public void save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order tidak boleh null.");
        }
        storage.put(order.getOrderId(), order);
    }

    @Override
    public Order findById(String orderId) {
        if (orderId == null) return null;
        return storage.get(orderId);
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public int count() {
        return storage.size();
    }

    public void clear() {
        storage.clear();
    }
}
