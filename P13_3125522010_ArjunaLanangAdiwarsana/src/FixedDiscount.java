/**
 * Strategi diskon nominal tetap (misal voucher Rp 20.000).
 * Memenuhi Liskov Substitution Principle (LSP):
 * Jika nominal diskon melebihi subtotal, diskon dibatasi maksimal sebesar subtotal.
 * Dengan demikian, net total tidak akan pernah menjadi negatif (menjaga invariant kontrak).
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class FixedDiscount implements IDiscountStrategy {
    private final double amount;

    public FixedDiscount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Nominal diskon tetap tidak boleh negatif.");
        }
        this.amount = amount;
    }

    @Override
    public double calculateDiscount(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal tidak boleh negatif.");
        }
        // Invariant LSP: Diskon tidak boleh melebihi subtotal
        return Math.min(amount, subtotal);
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String getStrategyName() {
        return String.format("Diskon Tetap (Voucher Rp %,.0f)", amount);
    }
}
