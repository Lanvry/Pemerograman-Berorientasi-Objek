import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementasi InMemoryOrderRepository
 * Mengisolasi penyimpanan order in-memory untuk efisiensi eksekusi dan pengujian.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> orders = new LinkedHashMap<>();

    @Override
    public void save(Order order) {
        if (order == null) throw new IllegalArgumentException("Order tidak boleh null.");
        orders.put(order.getOrderId(), order);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        if (orderId == null) return Optional.empty();
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public List<Order> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(orders.values()));
    }

    @Override
    public int count() {
        return orders.size();
    }
}
