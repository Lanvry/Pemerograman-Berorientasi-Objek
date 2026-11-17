import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object: ProductCode
 * 
 * Mengatasi Code Smell: Primitive Obsession
 * Menggantikan String SKU sembarang dengan validasi pola standar internal sistem
 * (contoh: PRD-XXXX, min 3 karakter alfanumerik setelah prefix).
 * 
 * Referensi Materi P14: Nirwana Haidar Hari, S.Pd., M.Kom.
 * D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public final class ProductCode {
    private static final Pattern CODE_PATTERN = Pattern.compile("^PRD-[A-Za-z0-9]{3,10}$");

    private final String value;

    public ProductCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode produk (SKU) tidak boleh kosong atau null.");
        }
        String normalized = code.trim().toUpperCase();
        if (!CODE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Format kode produk tidak valid: '" + code + "'. Pola wajib: PRD-XXX (contoh: PRD-LAPTOP01)"
            );
        }
        this.value = normalized;
    }

    public String getValue() {
        return value;
    }

    public String getCode() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCode)) return false;
        ProductCode that = (ProductCode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
