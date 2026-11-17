import java.util.List;

/**
 * Service: StockNotificationService
 * 
 * Hasil ekstraksi SRP dari God Class Produk (Kasus B).
 * Bertanggung jawab khusus untuk mengecek batas minimum stok produk dan
 * memicu pengiriman notifikasi/peringatan kepada manajer gudang/admin logistik.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class StockNotificationService {
    private final NotificationService notificationService;
    private final String adminEmail;

    public StockNotificationService(NotificationService notificationService, String adminEmail) {
        this.notificationService = notificationService;
        this.adminEmail = adminEmail;
    }

    public void checkAndAlertLowStock(List<Produk> lowStockProducts, int threshold) {
        if (lowStockProducts == null || lowStockProducts.isEmpty()) {
            return;
        }

        StringBuilder alertMsg = new StringBuilder();
        alertMsg.append("Peringatan stok gudang: Ditemukan ").append(lowStockProducts.size())
                .append(" produk berada di bawah ambang batas (<= ").append(threshold).append(" unit):\n");

        for (Produk p : lowStockProducts) {
            alertMsg.append(" - ").append(p.getName()).append(" [").append(p.getCode()).append("] : Sisa ")
                    .append(p.getStock()).append(" unit\n");
        }
        alertMsg.append("Mohon segera jadwalkan Purchase Order (PO) ke pemasok.");

        notificationService.sendSystemAlert(adminEmail, "Peringatan Stok Kritis Gudang", alertMsg.toString());
    }
}
