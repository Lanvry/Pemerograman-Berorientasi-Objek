import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object: VoucherCode
 * 
 * Mengatasi Code Smell:
 * 1. Primitive Obsession (String mentah diubah menjadi objek bervalidasi kuat)
 * 2. Duplicated Code (Solusi Kasus C Slide 17: Validasi format voucher di satu tempat)
 * 
 * Format standar: Prefix valid (PENS, DISKON, HEMAT, PROMO) diikuti kombinasi angka/huruf.
 * Panjang 5 - 15 karakter alfanumerik kapital.
 * 
 * Referensi Materi P14: Nirwana Haidar Hari, S.Pd., M.Kom.
 * D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public final class VoucherCode {
    private static final Pattern VOUCHER_PATTERN = Pattern.compile("^(PENS|DISKON|HEMAT|PROMO)[A-Z0-9]{2,10}$");

    private final String code;

    public VoucherCode(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode voucher tidak boleh null atau kosong.");
        }
        String clean = rawCode.trim().toUpperCase();
        if (!VOUCHER_PATTERN.matcher(clean).matches()) {
            throw new IllegalArgumentException(
                "Kode voucher '" + rawCode + "' tidak valid. Wajib diawali PENS/DISKON/HEMAT/PROMO (5-15 karakter)."
            );
        }
        this.code = clean;
    }

    public String getCode() {
        return code;
    }

    public String getPrefix() {
        if (code.startsWith("PENS")) return "PENS";
        if (code.startsWith("DISKON")) return "DISKON";
        if (code.startsWith("HEMAT")) return "HEMAT";
        if (code.startsWith("PROMO")) return "PROMO";
        return "UNKNOWN";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoucherCode)) return false;
        VoucherCode that = (VoucherCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
