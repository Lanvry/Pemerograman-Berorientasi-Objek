/**
 * Abstraksi Notifikasi untuk pengiriman pesan ke pelanggan.
 * Memenuhi DIP (Dependency Inversion Principle) & SRP:
 * Menghilangkan ketergantungan langsung (hardcoded) pada client email, WhatsApp, atau SMS konkret.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public interface INotifier {
    /**
     * Mengirimkan notifikasi ke penerima tertentu.
     * @param recipient alamat email, nomor telepon, atau ID pelanggan
     * @param subject judul notifikasi
     * @param message isi pesan notifikasi
     */
    void sendNotification(String recipient, String subject, String message);
}
