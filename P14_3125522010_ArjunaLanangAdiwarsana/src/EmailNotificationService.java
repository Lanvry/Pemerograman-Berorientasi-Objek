/**
 * Service: EmailNotificationService
 * 
 * Implementasi pengiriman notifikasi berbasis Email.
 * Ekstraksi dari method notifikasi yang sebelumnya tertanam dalam God Class Produk dan Long Method.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class EmailNotificationService implements NotificationService {

    @Override
    public void sendNotification(CustomerEmail recipient, String subject, String message) {
        System.out.println("  [EMAIL DISPATCH] -> Mengirim email ke: " + recipient.getValue());
        System.out.println("  Subjek : " + subject);
        System.out.println("  Konten : " + message);
        System.out.println("  Status : [OK] Terkirim via SMTP Gateway.");
    }

    @Override
    public void sendSystemAlert(String recipientEmail, String subject, String alertBody) {
        System.out.println("  [SYSTEM ALERT] -> Dispatching ke Admin (" + recipientEmail + "):");
        System.out.println("  Subjek : [ALERT] " + subject);
        System.out.println("  Pesan  : " + alertBody);
    }
}
