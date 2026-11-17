/**
 * Domain Service: DiscountService
 * 
 * Hasil ekstraksi dari Long Method prosesPesanan() (Kasus A).
 * Mengorkestrasi aturan diskon (voucher promo, diskon loyalitas, dll)
 * berkolaborasi dengan VoucherValidator tanpa menimbulkan duplikasi kode.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class DiscountService {

    public Money calculateDiscount(VoucherCode voucher, Money subtotal) {
        if (voucher == null || subtotal == null || subtotal.isZero()) {
            return Money.ZERO;
        }
        return VoucherValidator.validateAndCalculateDiscount(voucher, subtotal);
    }
}
