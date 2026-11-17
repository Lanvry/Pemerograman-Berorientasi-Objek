import java.util.Objects;

/**
 * Service: OrderProcessor
 * 
 * Mengatasi Code Smell:
 * 1. Long Method (Solusi Eksplisit Kasus A Slide 17: prosesPesanan() 120 baris diekstrak)
 * 2. Nested Conditionals (Arrow Anti-Pattern diganti Guard Clauses & Early Returns)
 * 3. Long Parameter List (Digantikan Parameter Object CheckoutRequest)
 * 4. God Responsibilities (Didelegasikan ke TaxCalculator, DiscountService, Repositories, Notification)
 * 
 * SEBELUM:
 * Method prosesPesanan() memiliki 120 baris kode monolithic dengan 5 tingkat nesting `if`:
 * - Cek validasi input berlapis
 * - Cek stok di loop bersarang
 * - Hitung diskon manual
 * - Hitung pajak manual
 * - Query insert database langsung
 * - Dispatch socket email inline
 * 
 * SESUDAH (Setelah Refactoring):
 * Menggunakan Clean OOD:
 * - Guard Clauses mengecek prekondisi di awal (fail-fast), menghilangkan nesting.
 * - Single Responsibility & Extract Method: tiap fase dipisahkan ke method/service tersendiri.
 * 
 * Referensi Materi P14: Nirwana Haidar Hari, S.Pd., M.Kom.
 * D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class OrderProcessor {
    private final ProdukRepository produkRepository;
    private final OrderRepository orderRepository;
    private final TaxCalculator taxCalculator;
    private final DiscountService discountService;
    private final NotificationService notificationService;

    public OrderProcessor(ProdukRepository produkRepository,
                          OrderRepository orderRepository,
                          TaxCalculator taxCalculator,
                          DiscountService discountService,
                          NotificationService notificationService) {
        this.produkRepository = Objects.requireNonNull(produkRepository, "ProdukRepository tidak boleh null.");
        this.orderRepository = Objects.requireNonNull(orderRepository, "OrderRepository tidak boleh null.");
        this.taxCalculator = Objects.requireNonNull(taxCalculator, "TaxCalculator tidak boleh null.");
        this.discountService = Objects.requireNonNull(discountService, "DiscountService tidak boleh null.");
        this.notificationService = Objects.requireNonNull(notificationService, "NotificationService tidak boleh null.");
    }

    /**
     * Method Utama Pemrosesan Pesanan (Hasil Refactoring Kasus A).
     * Berisi orkestrasi tingkat tinggi yang sangat ringkas, deklaratif, dan mudah dibaca.
     */
    public Order prosesPesanan(CheckoutRequest request) {
        // 1. Guard Clause: Validasi Request & Prekondisi (Early Return jika invalid)
        validateRequest(request);

        // 2. Guard Clause: Validasi Ketersediaan Stok Seluruh Item
        validateStockAvailability(request);

        // 3. Mutasi Domain: Kurangi Stok Produk
        deductProductStock(request);

        // 4. Kalkulasi Finansial (Subtotal, Diskon, Pajak, Grand Total)
        Order order = createAndCalculateOrder(request);

        // 5. Persistensi Data Pesanan
        orderRepository.save(order);

        // 6. Notifikasi Konfirmasi ke Pelanggan
        notifyCustomer(order);

        return order;
    }

    // =========================================================================
    // HASIL EXTRACT METHOD & GUARD CLAUSES (Memecah 120 baris menjadi fungsi kecil)
    // =========================================================================

    private void validateRequest(CheckoutRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CheckoutRequest tidak boleh null.");
        }
        if (request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Pesanan tidak dapat diproses: Keranjang belanja kosong.");
        }
        if (orderRepository.findById(request.getOrderId()).isPresent()) {
            throw new IllegalStateException("Pesanan dengan ID '" + request.getOrderId() + "' sudah pernah diproses.");
        }
    }

    private void validateStockAvailability(CheckoutRequest request) {
        for (OrderItem item : request.getItems()) {
            Produk liveProduct = produkRepository.findByCode(item.getProduct().getCode())
                    .orElseThrow(() -> new IllegalArgumentException("Produk tidak ditemukan di katalog: " + item.getProduct().getCode()));

            if (!liveProduct.isAvailable(item.getQuantity())) {
                throw new IllegalStateException(String.format(
                    "Stok produk '%s' tidak mencukupi! Tersedia: %d, diminta: %d.",
                    liveProduct.getName(), liveProduct.getStock(), item.getQuantity()
                ));
            }
        }
    }

    private void deductProductStock(CheckoutRequest request) {
        for (OrderItem item : request.getItems()) {
            Produk liveProduct = produkRepository.findByCode(item.getProduct().getCode()).get();
            liveProduct.reduceStock(item.getQuantity());
            produkRepository.save(liveProduct);
        }
    }

    private Order createAndCalculateOrder(CheckoutRequest request) {
        Order order = new Order(
            request.getOrderId(),
            request.getCustomerName(),
            request.getCustomerEmail(),
            request.getShippingAddress(),
            request.getItems(),
            request.getVoucherCode(),
            request.getPaymentType(),
            request.getCustomerNote()
        );

        Money subtotal = order.getSubtotal();
        Money discount = discountService.calculateDiscount(request.getVoucherCode(), subtotal);
        Money taxableAmount = subtotal.isGreaterThan(discount) ? subtotal.subtract(discount) : Money.ZERO;
        Money tax = taxCalculator.calculateTax(taxableAmount);
        Money grandTotal = taxableAmount.add(tax);

        order.setCalculatedTotals(discount, tax, grandTotal);
        return order;
    }

    private void notifyCustomer(Order order) {
        String subject = "Konfirmasi Pesanan Berhasil #" + order.getOrderId();
        String body = String.format(
            "Halo %s,\nTerima kasih atas pesanan Anda. Total tagihan: %s (Termasuk PPN: %s, Diskon: %s).\nMetode Pembayaran: %s.",
            order.getCustomerName(),
            order.getGrandTotal().formatRupiah(),
            order.getTaxAmount().formatRupiah(),
            order.getDiscountAmount().formatRupiah(),
            order.getPaymentType().getDescription()
        );
        notificationService.sendNotification(order.getCustomerEmail(), subject, body);
    }
}
