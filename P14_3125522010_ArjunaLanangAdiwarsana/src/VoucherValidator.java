import java.util.HashMap;
import java.util.Map;

/**
 * Domain Service: VoucherValidator
 * 
 * Mengatasi Code Smell:
 * Duplicated Code (Solusi Eksplisit Kasus C Slide 17)
 * "Aturan validasi kode voucher berulang di 4 class berbeda"
 * 
 * Class ini mengekstrak seluruh aturan bisnis voucher (eligibilitas, kuota aktif,
 * persentase diskon, batas minimal belanja) menjadi Single Source of Truth
 * sehingga dapat digunakan kembali oleh CartService, CheckoutService, PaymentService,
 * dan AdminService tanpa duplikasi baris kode.
 * 
 * Referensi Materi P14: Nirwana Haidar Hari, S.Pd., M.Kom.
 * D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class VoucherValidator {

    // Mock basis data voucher aktif beserta diskon persentase dan minimum belanja
    private static final Map<String, VoucherRule> ACTIVE_VOUCHERS = new HashMap<>();

    static {
        ACTIVE_VOUCHERS.put("PENSJUARA", new VoucherRule("PENSJUARA", 25.0, Money.of(100_000), "Diskon Dies Natalis PENS 25%"));
        ACTIVE_VOUCHERS.put("DISKON10", new VoucherRule("DISKON10", 10.0, Money.of(50_000), "Diskon Pengguna Reguler 10%"));
        ACTIVE_VOUCHERS.put("HEMAT20", new VoucherRule("HEMAT20", 20.0, Money.of(150_000), "Voucher Super Hemat 20%"));
        ACTIVE_VOUCHERS.put("PROMO50", new VoucherRule("PROMO50", 50.0, Money.of(300_000), "Flash Sale Diskon 50%"));
    }

    public static class VoucherRule {
        private final String code;
        private final double discountPercentage;
        private final Money minPurchase;
        private final String description;

        public VoucherRule(String code, double discountPercentage, Money minPurchase, String description) {
            this.code = code;
            this.discountPercentage = discountPercentage;
            this.minPurchase = minPurchase;
            this.description = description;
        }

        public String getCode() { return code; }
        public double getDiscountPercentage() { return discountPercentage; }
        public Money getMinPurchase() { return minPurchase; }
        public String getDescription() { return description; }
    }

    /**
     * Memvalidasi apakah kode voucher terdaftar, aktif, dan memenuhi syarat minimal belanja.
     * Mengembalikan hasil kalkulasi nominal diskon yang sah.
     */
    public static Money validateAndCalculateDiscount(VoucherCode voucher, Money orderSubtotal) {
        if (voucher == null) {
            return Money.ZERO;
        }

        VoucherRule rule = ACTIVE_VOUCHERS.get(voucher.getCode());
        if (rule == null) {
            throw new IllegalArgumentException(
                "Voucher '" + voucher.getCode() + "' tidak terdaftar atau sudah kadaluwarsa dalam sistem."
            );
        }

        if (orderSubtotal.isLessThan(rule.getMinPurchase())) {
            throw new IllegalArgumentException(
                "Subtotal belanja " + orderSubtotal + " belum memenuhi syarat minimum voucher '" 
                + voucher.getCode() + "' (" + rule.getMinPurchase() + ")."
            );
        }

        return orderSubtotal.percentage(rule.getDiscountPercentage());
    }

    public static boolean isVoucherActive(String rawCode) {
        try {
            VoucherCode vc = new VoucherCode(rawCode);
            return ACTIVE_VOUCHERS.containsKey(vc.getCode());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static VoucherRule getVoucherRule(VoucherCode voucher) {
        if (voucher == null) return null;
        return ACTIVE_VOUCHERS.get(voucher.getCode());
    }
}
