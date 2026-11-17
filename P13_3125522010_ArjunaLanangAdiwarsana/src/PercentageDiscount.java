/**
 * Strategi diskon persentase (misal 10%, 25%).
 * Memenuhi LSP: Persentase dibatasi antara 0% hingga 100%, menjamin diskon <= subtotal.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class PercentageDiscount implements IDiscountStrategy {
    private final double percentage;

    public PercentageDiscount(double percentage) {
        if (percentage < 0.0 || percentage > 100.0) {
            throw new IllegalArgumentException("Persentase diskon harus antara 0.0 dan 100.0%");
        }
        this.percentage = percentage;
    }

    @Override
    public double calculateDiscount(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal tidak boleh negatif.");
        }
        return subtotal * (percentage / 100.0);
    }

    public double getPercentage() {
        return percentage;
    }

    @Override
    public String getStrategyName() {
        return String.format("Diskon Persentase (%.1f%%)", percentage);
    }
}
