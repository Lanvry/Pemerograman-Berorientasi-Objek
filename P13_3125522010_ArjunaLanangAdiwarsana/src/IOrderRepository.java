import java.util.List;

/**
 * Abstraksi Repository untuk persistensi Order.
 * Memenuhi DIP (Dependency Inversion Principle) dan SRP:
 * Modul bisnis (CheckoutService) hanya bergantung pada interface ini,
 * bukan pada MySQL, File I/O, atau mekanisme penyimpanan konkret lainnya.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public interface IOrderRepository {
    /**
     * Menyimpan data order ke media penyimpanan.
     * @param order obyek pesanan yang akan disimpan
     */
    void save(Order order);

    /**
     * Mencari data order berdasarkan order ID.
     * @param orderId ID pesanan
     * @return order jika ditemukan, atau null jika tidak ada
     */
    Order findById(String orderId);

    /**
     * Mengambil seluruh data pesanan yang tersimpan.
     * @return daftar seluruh order
     */
    List<Order> findAll();

    /**
     * Menghitung total pesanan tersimpan.
     * @return jumlah record pesanan
     */
    int count();
}
