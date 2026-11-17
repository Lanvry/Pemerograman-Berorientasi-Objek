import java.time.LocalDateTime;

/**
 * Kontrak Interface Segregation Principle (ISP) untuk obyek yang dapat diaudit.
 * Sub-sistem audit dan logging hanya bergantung pada interface ini,
 * tanpa memaksa implementor menyediakan method cetak atau notifikasi.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public interface Auditable {
    /**
     * Mendapatkan identifier entitas untuk jejak audit.
     * @return audit ID
     */
    String getAuditId();

    /**
     * Mendapatkan waktu pencatatan audit.
     * @return timestamp
     */
    LocalDateTime getAuditTimestamp();

    /**
     * Mendapatkan ringkasan metadata audit (misal status transaksi dan identitas pembuat).
     * @return ringkasan metadata
     */
    String getAuditMetadata();
}
