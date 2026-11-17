/**
 * Interface: NotificationService
 * 
 * Abstraksi untuk kanal pengiriman notifikasi/alert.
 * Memisahkan logika domain dari mekanisme detail pengiriman pesan (Email, SMS, Push Notif).
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public interface NotificationService {
    void sendNotification(CustomerEmail recipient, String subject, String message);
    void sendSystemAlert(String recipientEmail, String subject, String alertBody);
}
