import java.util.ArrayList;
import java.util.List;

/**
 * Implementasi Notifikasi via Email.
 * Memenuhi SRP: Hanya bertanggung jawab menyusun dan mengirimkan payload notifikasi email.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class EmailNotifier implements INotifier {
    private final List<String> sentLogs = new ArrayList<>();

    @Override
    public void sendNotification(String recipient, String subject, String message) {
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email tidak boleh kosong.");
        }
        String logEntry = String.format("[EMAIL SENT] To: %s | Subject: %s | Preview: %s",
                recipient, subject, message.replace("\n", " ").substring(0, Math.min(message.length(), 60)));
        sentLogs.add(logEntry);
        System.out.println(">> Notifikasi Terkirim: " + logEntry);
    }

    public List<String> getSentLogs() {
        return new ArrayList<>(sentLogs);
    }

    public int getSentCount() {
        return sentLogs.size();
    }
}
