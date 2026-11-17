/**
 * Domain Service: TaxCalculator
 * 
 * Hasil ekstraksi dari Long Method prosesPesanan() (Kasus A).
 * Bertanggung jawab khusus untuk kalkulasi pajak pertambahan nilai (PPN)
 * berdasarkan regulasi perpajakan yang berlaku (standar PPN 11%).
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class TaxCalculator {
    public static final double DEFAULT_TAX_RATE = 11.0; // PPN 11%

    private final double taxRate;

    public TaxCalculator() {
        this(DEFAULT_TAX_RATE);
    }

    public TaxCalculator(double taxRate) {
        if (taxRate < 0) {
            throw new IllegalArgumentException("Tarif pajak tidak boleh negatif: " + taxRate);
        }
        this.taxRate = taxRate;
    }

    public Money calculateTax(Money taxableAmount) {
        if (taxableAmount == null || taxableAmount.isZero()) {
            return Money.ZERO;
        }
        return taxableAmount.percentage(taxRate);
    }

    public double getTaxRate() {
        return taxRate;
    }
}
