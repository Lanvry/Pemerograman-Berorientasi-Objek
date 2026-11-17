/**
 * Strategi diskon khusus member VIP bertingkat (Tiered VIP Discount).
 * Memenuhi OCP (ekstensi strategi baru tanpa ubah kode lama) dan LSP:
 * Memberikan diskon 15% untuk semua belanja, dan tambahan bonus 5% (total 20%)
 * jika subtotal mencapai atau melebihi threshold Rp 500.000.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class VipDiscount implements IDiscountStrategy {
    private static final double BASE_RATE = 15.0; // 15%
    private static final double BONUS_RATE = 5.0; // ekstra 5%
    private static final double THRESHOLD = 500_000.0; // Rp 500.000

    @Override
    public double calculateDiscount(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal tidak boleh negatif.");
        }
        double rate = BASE_RATE;
        if (subtotal >= THRESHOLD) {
            rate += BONUS_RATE;
        }
        double discount = subtotal * (rate / 100.0);
        return Math.min(discount, subtotal);
    }

    @Override
    public String getStrategyName() {
        return "Diskon Eksklusif Member VIP (15% - 20%)";
    }
}
