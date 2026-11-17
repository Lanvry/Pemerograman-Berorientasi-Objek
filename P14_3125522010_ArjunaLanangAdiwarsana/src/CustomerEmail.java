import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object: CustomerEmail
 * 
 * Mengatasi Code Smell: Primitive Obsession
 * Menggantikan representasi email berbasis String mentah dengan objek yang
 * memiliki validasi otomatis, sifat immutable (tak dapat diubah), dan kesetaraan nilai.
 * 
 * Referensi Materi P14: Nirwana Haidar Hari, S.Pd., M.Kom.
 * D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public final class CustomerEmail {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final String value;

    public CustomerEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email pelanggan tidak boleh kosong atau null.");
        }
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Format email tidak valid: '" + email + "'. Contoh: user@domain.com");
        }
        this.value = normalized;
    }

    public String getValue() {
        return value;
    }

    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerEmail)) return false;
        CustomerEmail that = (CustomerEmail) o;
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
