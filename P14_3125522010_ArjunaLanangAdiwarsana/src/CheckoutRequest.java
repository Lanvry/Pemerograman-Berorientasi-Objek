import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Parameter Object: CheckoutRequest
 * 
 * Mengatasi Code Smell:
 * Long Parameter List (Solusi Teknik F Refactoring Materi P14)
 * 
 * SEBELUM:
 * Method checkout menerima daftar parameter sangat panjang (8-10 parameter primitif):
 * `prosesPesanan(String name, String email, String addr, String phone, String payType, String voucher, String prodCode, int qty, String note)`
 * yang menyulitkan pembacaan, rawan tertukar urutan argumen tipe sama, dan rapuh terhadap perubahan.
 * 
 * SESUDAH:
 * Seluruh payload dikelompokkan ke dalam satu objek parameter bervolume tinggi,
 * bervalidasi kuat, dan kaya makna semantik.
 * 
 * Referensi Materi P14: Nirwana Haidar Hari, S.Pd., M.Kom.
 * D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class CheckoutRequest {
    private final String orderId;
    private final String customerName;
    private final CustomerEmail customerEmail;
    private final String shippingAddress;
    private final List<OrderItem> items;
    private final VoucherCode voucherCode; // Nullable jika tidak memakai voucher
    private final PaymentType paymentType;
    private final String customerNote;

    public CheckoutRequest(String orderId,
                           String customerName,
                           CustomerEmail customerEmail,
                           String shippingAddress,
                           List<OrderItem> items,
                           VoucherCode voucherCode,
                           PaymentType paymentType,
                           String customerNote) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID tidak boleh kosong.");
        }
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama pelanggan tidak boleh kosong.");
        }
        this.customerEmail = Objects.requireNonNull(customerEmail, "Email pelanggan tidak boleh null.");
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Alamat pengiriman tidak boleh kosong.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Daftar item belanja tidak boleh kosong.");
        }
        this.paymentType = Objects.requireNonNull(paymentType, "Metode pembayaran tidak boleh null.");

        this.orderId = orderId.trim();
        this.customerName = customerName.trim();
        this.shippingAddress = shippingAddress.trim();
        this.items = new ArrayList<>(items);
        this.voucherCode = voucherCode;
        this.customerNote = (customerNote == null) ? "" : customerNote.trim();
    }

    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public CustomerEmail getCustomerEmail() { return customerEmail; }
    public String getShippingAddress() { return shippingAddress; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public VoucherCode getVoucherCode() { return voucherCode; }
    public PaymentType getPaymentType() { return paymentType; }
    public String getCustomerNote() { return customerNote; }

    public boolean hasVoucher() {
        return voucherCode != null;
    }
}
