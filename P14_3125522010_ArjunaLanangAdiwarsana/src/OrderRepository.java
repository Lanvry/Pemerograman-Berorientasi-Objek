import java.util.List;
import java.util.Optional;

/**
 * Interface: OrderRepository
 * Abstraksi persistensi untuk entitas Order.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String orderId);
    List<Order> findAll();
    int count();
}
