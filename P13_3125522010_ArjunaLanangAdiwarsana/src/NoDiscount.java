/**
 * Implementasi strategi tanpa diskon (Null Object Pattern).
 * Memenuhi LSP: Selalu mengembalikan 0.0, aman disubstitusikan di mana pun.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class NoDiscount implements IDiscountStrategy {
    @Override
    public double calculateDiscount(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal tidak boleh negatif.");
        }
        return 0.0;
    }

    @Override
    public String getStrategyName() {
        return "Tanpa Diskon (Reguler)";
    }
}
