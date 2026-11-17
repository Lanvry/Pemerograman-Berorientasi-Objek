/**
 * Kontrak Open/Closed Principle (OCP) & Liskov Substitution Principle (LSP).
 * - OCP: Sistem terbuka untuk penambahan strategi diskon baru tanpa modifikasi kode eksisting.
 * - LSP: Setiap subtype implementasi diskon harus memenuhi invariant:
 *        0.0 <= diskon <= subtotal, tanpa melempar UnsupportedOperationException.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public interface IDiscountStrategy {
    /**
     * Menghitung besaran diskon berdasarkan subtotal belanja.
     * Kontrak Invariant: Nilai kembalian harus selalu berada di antara 0.0 dan subtotal.
     *
     * @param subtotal nilai total belanja sebelum diskon (harus >= 0)
     * @return nominal potongan harga (>= 0 dan <= subtotal)
     */
    double calculateDiscount(double subtotal);

    /**
     * Nama deskriptif dari strategi diskon.
     * @return nama strategi
     */
    String getStrategyName();
}
