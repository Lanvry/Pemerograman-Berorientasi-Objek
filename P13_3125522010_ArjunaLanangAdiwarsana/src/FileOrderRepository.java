import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementasi File-based persistence untuk IOrderRepository.
 * Menyimpan data Order beserta daftar Item ke dalam flat file teks.
 *
 * Membuktikan SRP & DIP:
 * - SRP: Tanggung jawab membaca dan menulis file terisolasi di kelas ini.
 * - DIP: CheckoutService tidak mengetahui rincian file I/O ini.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class FileOrderRepository implements IOrderRepository {
    private final String filePath;

    public FileOrderRepository(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path tidak boleh kosong.");
        }
        this.filePath = filePath.trim();
        initFile();
    }

    private void initFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Gagal menginisialisasi file: " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order tidak boleh null.");
        }
        List<Order> existingOrders = findAll();
        // Hapus jika sudah ada (update inplace)
        existingOrders.removeIf(o -> o.getOrderId().equalsIgnoreCase(order.getOrderId()));
        existingOrders.add(order);

        writeAll(existingOrders);
    }

    @Override
    public synchronized Order findById(String orderId) {
        if (orderId == null) return null;
        for (Order o : findAll()) {
            if (o.getOrderId().equalsIgnoreCase(orderId.trim())) {
                return o;
            }
        }
        return null;
    }

    @Override
    public synchronized List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return orders;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Format: orderId;customerName;customerEmail;status;createdAt;itemsData
                // itemsData: id:name:price:qty#id:name:price:qty
                String[] parts = line.split(";", -1);
                if (parts.length >= 5) {
                    String orderId = parts[0];
                    String customerName = parts[1];
                    String customerEmail = parts[2];
                    Order.Status status = Order.Status.valueOf(parts[3]);
                    LocalDateTime createdAt = LocalDateTime.parse(parts[4]);

                    Order order = new Order(orderId, customerName, customerEmail, status, createdAt);

                    if (parts.length >= 6 && !parts[5].isEmpty()) {
                        String[] itemTokens = parts[5].split("#");
                        for (String itemToken : itemTokens) {
                            String[] it = itemToken.split(":", -1);
                            if (it.length == 4) {
                                String itemId = it[0];
                                String itemName = it[1];
                                double itemPrice = Double.parseDouble(it[2]);
                                int itemQty = Integer.parseInt(it[3]);
                                order.rehydrateItem(new Item(itemId, itemName, itemPrice, itemQty));
                            }
                        }
                    }
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            System.err.println("Error saat membaca repository file: " + e.getMessage());
        }
        return orders;
    }

    @Override
    public synchronized int count() {
        return findAll().size();
    }

    private void writeAll(List<Order> orders) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.write("# Database Flat File Order Repository - P13 SOLID\n");
            for (Order o : orders) {
                StringBuilder itemsSb = new StringBuilder();
                List<Item> items = o.getItems();
                for (int i = 0; i < items.size(); i++) {
                    Item it = items.get(i);
                    itemsSb.append(it.getId()).append(":")
                           .append(it.getName()).append(":")
                           .append(it.getPrice()).append(":")
                           .append(it.getQuantity());
                    if (i < items.size() - 1) {
                        itemsSb.append("#");
                    }
                }
                String line = String.format("%s;%s;%s;%s;%s;%s\n",
                        o.getOrderId(),
                        o.getCustomerName(),
                        o.getCustomerEmail(),
                        o.getStatus().name(),
                        o.getCreatedAt().toString(),
                        itemsSb.toString());
                writer.write(line);
            }
        } catch (IOException e) {
            System.err.println("Gagal menulis ke file repository: " + e.getMessage());
        }
    }
}
