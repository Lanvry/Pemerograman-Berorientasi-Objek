import java.util.Objects;

/**
 * Domain Entity: Customer
 * 
 * Mengatasi Code Smell:
 * Feature Envy (Solusi Teknik D Refactoring Materi P14: Move Method)
 * 
 * SEBELUM:
 * Class eksternal seperti `ShippingCalculator` atau `DiscountPolicy` terus-menerus
 * memanggil banyak getter milik Customer (`getTier()`, `getLoyaltyYears()`, `getCity()`)
 * untuk menghitung diskon loyalitas dan kelayakan gratis ongkir.
 * Ini menunjukkan "Feature Envy" (Class lain lebih tertarik pada data Customer dibanding datanya sendiri).
 * 
 * SESUDAH:
 * Melalui teknik Move Method, logika evaluasi hak istimewa (diskon loyalitas & gratis ongkir)
 * dipindahkan ke dalam class Customer karena Customer lah pemegang data utama (Information Expert).
 * 
 * Referensi Materi P14: Nirwana Haidar Hari, S.Pd., M.Kom.
 * D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class Customer {
    public enum Tier {
        REGULAR(0.0),
        SILVER(5.0),
        GOLD(10.0),
        PLATINUM(15.0);

        private final double discountRate;

        Tier(double discountRate) {
            this.discountRate = discountRate;
        }

        public double getDiscountRate() {
            return discountRate;
        }
    }

    private final String id;
    private final String name;
    private final CustomerEmail email;
    private final String city;
    private final Tier tier;
    private final int loyaltyYears;

    public Customer(String id, String name, CustomerEmail email, String city, Tier tier, int loyaltyYears) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID customer tidak boleh kosong.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Nama customer tidak boleh kosong.");
        this.id = id.trim();
        this.name = name.trim();
        this.email = Objects.requireNonNull(email, "Email tidak boleh null.");
        this.city = (city == null || city.trim().isEmpty()) ? "Sumenep" : city.trim();
        this.tier = (tier == null) ? Tier.REGULAR : tier;
        this.loyaltyYears = Math.max(0, loyaltyYears);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public CustomerEmail getEmail() { return email; }
    public String getCity() { return city; }
    public Tier getTier() { return tier; }
    public int getLoyaltyYears() { return loyaltyYears; }

    // =========================================================================
    // HASIL MOVE METHOD (Menyembuhkan Feature Envy dari External Calculator)
    // =========================================================================

    /**
     * Dihitung langsung oleh Customer sebagai Information Expert.
     * Memberi diskon tambahan 1% per tahun loyalitas, ditambah bonus tier.
     */
    public double calculateEffectiveLoyaltyDiscountRate() {
        double baseRate = tier.getDiscountRate();
        double tenureBonus = Math.min(loyaltyYears * 1.0, 5.0); // Maks 5% bonus tahunan
        return baseRate + tenureBonus;
    }

    /**
     * Memeriksa apakah customer berhak atas subsidi ongkir gratis.
     */
    public boolean isEligibleForFreeShipping(Money orderTotal) {
        if (tier == Tier.PLATINUM) return true;
        if (tier == Tier.GOLD && orderTotal.isGreaterThan(Money.of(200_000))) return true;
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) | Tier: %s | Loyalitas: %d thn | Diskon: %.1f%%",
                name, email, tier, loyaltyYears, calculateEffectiveLoyaltyDiscountRate());
    }
}
