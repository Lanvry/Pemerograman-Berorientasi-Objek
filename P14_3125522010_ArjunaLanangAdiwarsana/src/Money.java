import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Value Object: Money
 * 
 * Mengatasi Code Smell: Primitive Obsession
 * Menggantikan tipe data primitif `double` / `float` untuk uang yang rentan
 * kesalahan floating point precision, nilai negatif tak terduga, dan hilangnya
 * domain behavior (penjumlahan, pemotongan diskon, formatting mata uang).
 * Bersifat immutable dan thread-safe.
 * 
 * Referensi Materi P14: Nirwana Haidar Hari, S.Pd., M.Kom.
 * D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public final class Money implements Comparable<Money> {
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    public Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Nilai nominal uang tidak boleh null.");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Nominal uang tidak boleh bernilai negatif: " + amount);
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money(double amount) {
        this(BigDecimal.valueOf(amount));
    }

    public Money(long amount) {
        this(BigDecimal.valueOf(amount));
    }

    public static Money of(double amount) {
        return new Money(amount);
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "Nilai penambah tidak boleh null.");
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        Objects.requireNonNull(other, "Nilai pengurang tidak boleh null.");
        if (this.amount.compareTo(other.amount) < 0) {
            throw new IllegalArgumentException("Pengurangan menghasilkan saldo negatif (" + this.amount + " - " + other.amount + ")");
        }
        return new Money(this.amount.subtract(other.amount));
    }

    public Money multiply(double factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Faktor perkalian tidak boleh negatif: " + factor);
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)));
    }

    public Money percentage(double percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Persentase harus berada di rentang 0 - 100: " + percent);
        }
        BigDecimal fraction = BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return new Money(this.amount.multiply(fraction));
    }

    public boolean isGreaterThan(Money other) {
        return this.compareTo(other) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return this.compareTo(other) >= 0;
    }

    public boolean isLessThan(Money other) {
        return this.compareTo(other) < 0;
    }

    public boolean isLessThanOrEqual(Money other) {
        return this.compareTo(other) <= 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public int compareTo(Money o) {
        return this.amount.compareTo(o.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money money = (Money) o;
        return this.amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    public String formatRupiah() {
        Locale indonesia = Locale.forLanguageTag("id-ID");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(indonesia);
        return formatter.format(amount);
    }

    @Override
    public String toString() {
        return formatRupiah();
    }
}
