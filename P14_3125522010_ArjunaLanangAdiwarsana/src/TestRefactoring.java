import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Automated Test Runner untuk Praktikum P14: Refactoring & Clean OOD
 * 
 * Menguji seluruh komponen refactoring, edge cases, invariansi Value Objects,
 * Guard Clauses, dan solusi Kasus A, B, dan C tanpa dependensi eksternal (pure Java).
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class TestRefactoring {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("         RUNNING AUTOMATED UNIT TESTS FOR P14 REFACTORING");
        System.out.println("==========================================================================");

        testCustomerEmailValueObject();
        testMoneyValueObject();
        testProductCodeValueObject();
        testVoucherCodeAndValidatorKasusC();
        testProdukSRPKasusB();
        testFeatureEnvyMoveMethod();
        testOrderProcessorAndGuardClausesKasusA();

        printSummary();

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %s\n", testName);
            testsPassed++;
        } else {
            System.err.printf("  [FAIL] %s\n", testName);
            testsFailed++;
        }
    }

    private static void testCustomerEmailValueObject() {
        System.out.println("\n--- [1] Menguji Value Object: CustomerEmail (Primitive Obsession) ---");
        try {
            CustomerEmail validEmail = new CustomerEmail("arjuna@pens.ac.id");
            assertTrue("Email valid berhasil dibuat", validEmail.getValue().equals("arjuna@pens.ac.id"));
            assertTrue("Domain email diekstrak benar", validEmail.getDomain().equals("pens.ac.id"));

            CustomerEmail sameEmail = new CustomerEmail("ARJUNA@PENS.AC.ID");
            assertTrue("Email case-insensitive equality", validEmail.equals(sameEmail));
        } catch (Exception e) {
            assertTrue("CustomerEmail valid gagal: " + e.getMessage(), false);
        }

        try {
            new CustomerEmail("invalid-email-format");
            assertTrue("Email tanpa @ seharusnya gagal", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Guard clause email invalid melempar IllegalArgumentException", true);
        }

        try {
            new CustomerEmail(null);
            assertTrue("Email null seharusnya gagal", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Guard clause email null melempar IllegalArgumentException", true);
        }
    }

    private static void testMoneyValueObject() {
        System.out.println("\n--- [2] Menguji Value Object: Money (Primitive Obsession) ---");
        Money m1 = Money.of(100_000);
        Money m2 = Money.of(50_000);

        assertTrue("Penjumlahan Money: 100k + 50k = 150k", m1.add(m2).equals(Money.of(150_000)));
        assertTrue("Pengurangan Money: 100k - 50k = 50k", m1.subtract(m2).equals(Money.of(50_000)));
        assertTrue("Perkalian Money: 100k * 2 = 200k", m1.multiply(2).equals(Money.of(200_000)));
        assertTrue("Persentase Money: 10% dari 100k = 10k", m1.percentage(10).equals(Money.of(10_000)));
        assertTrue("Format Rupiah mengandung 'Rp'", m1.formatRupiah().contains("Rp"));

        try {
            m2.subtract(m1); // 50k - 100k -> negatif
            assertTrue("Pengurangan saldo negatif seharusnya melempar Exception", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Guard clause saldo negatif berhasil mencegah mutasi ilegal", true);
        }

        try {
            new Money(-1000);
            assertTrue("Instansiasi Money negatif seharusnya melempar Exception", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Guard clause nominal negatif berhasil", true);
        }
    }

    private static void testProductCodeValueObject() {
        System.out.println("\n--- [3] Menguji Value Object: ProductCode (Primitive Obsession) ---");
        try {
            ProductCode sku = new ProductCode("PRD-SSD01");
            assertTrue("ProductCode valid berhasil", sku.getValue().equals("PRD-SSD01"));
        } catch (Exception e) {
            assertTrue("ProductCode valid gagal: " + e.getMessage(), false);
        }

        try {
            new ProductCode("BARANG_123");
            assertTrue("ProductCode tanpa prefix PRD- seharusnya gagal", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Guard clause pattern ProductCode berhasil", true);
        }
    }

    private static void testVoucherCodeAndValidatorKasusC() {
        System.out.println("\n--- [4] Menguji Kasus C: VoucherCode & VoucherValidator (Duplicated Code) ---");
        VoucherCode v1 = new VoucherCode("PENSJUARA");
        assertTrue("Prefix voucher PENS berhasil diidentifikasi", v1.getPrefix().equals("PENS"));

        Money belanja500k = Money.of(500_000);
        Money diskonPens = VoucherValidator.validateAndCalculateDiscount(v1, belanja500k);
        // PENSJUARA = 25% dari 500k = 125k
        assertTrue("Diskon PENSJUARA 25% dari 500k = 125k", diskonPens.equals(Money.of(125_000)));

        try {
            Money belanjaKecil = Money.of(30_000);
            VoucherValidator.validateAndCalculateDiscount(v1, belanjaKecil); // Min purchase 100k
            assertTrue("Belanja di bawah minimum belanja voucher harus gagal", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Validasi batas minimum belanja voucher berhasil melempar Exception", true);
        }

        try {
            new VoucherCode("INVALIDVOUCHER");
            assertTrue("Voucher tanpa prefix sah harus gagal", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Guard clause pola voucher gagal untuk kode tidak sah", true);
        }
    }

    private static void testProdukSRPKasusB() {
        System.out.println("\n--- [5] Menguji Kasus B: God Class Produk -> Extract Class (SRP) ---");
        Produk produk = new Produk(new ProductCode("PRD-MON01"), "Monitor 24 Inch", Money.of(2_000_000), 10, "Display");

        // 1. Domain behavior
        assertTrue("Produk tersedia untuk 5 unit", produk.isAvailable(5));
        produk.reduceStock(3);
        assertTrue("Stok produk berkurang menjadi 7 unit", produk.getStock() == 7);
        produk.addStock(5);
        assertTrue("Stok produk bertambah menjadi 12 unit", produk.getStock() == 12);

        // 2. Repository behavior (Extracted)
        ProdukRepository repo = new InMemoryProdukRepository();
        repo.save(produk);
        assertTrue("Repository berhasil menyimpan dan menemukan produk", repo.findByCode(produk.getCode()).isPresent());
        assertTrue("Repository mendeteksi ketiadaan produk acak", !repo.findByCode(new ProductCode("PRD-FAKE99")).isPresent());

        // 3. Reporting behavior (Extracted)
        ProdukReportService reportService = new ProdukReportService();
        String summary = reportService.generateCatalogSummary(repo.findAll());
        assertTrue("Report service menghasilkan ringkasan berisi nama produk", summary.contains("Monitor 24 Inch"));
    }

    private static void testFeatureEnvyMoveMethod() {
        System.out.println("\n--- [6] Menguji Feature Envy -> Move Method ---");
        Customer customerGold = new Customer(
            "C-01", "Budi Santoso", new CustomerEmail("budi@gmail.com"), "Surabaya", Customer.Tier.GOLD, 4
        );
        // GOLD = 10% base + 4% tenure = 14%
        double effectiveRate = customerGold.calculateEffectiveLoyaltyDiscountRate();
        assertTrue("Diskon loyalitas dihitung internal oleh Customer (Move Method)", effectiveRate == 14.0);
        assertTrue("Customer GOLD belanja > 200k berhak gratis ongkir", customerGold.isEligibleForFreeShipping(Money.of(250_000)));

        Customer customerReg = new Customer(
            "C-02", "Siti Aminah", new CustomerEmail("siti@gmail.com"), "Sumenep", Customer.Tier.REGULAR, 0
        );
        assertTrue("Customer REGULAR diskon loyalitas = 0%", customerReg.calculateEffectiveLoyaltyDiscountRate() == 0.0);
        assertTrue("Customer REGULAR tidak berhak gratis ongkir", !customerReg.isEligibleForFreeShipping(Money.of(500_000)));
    }

    private static void testOrderProcessorAndGuardClausesKasusA() {
        System.out.println("\n--- [7] Menguji Kasus A: Long Method prosesPesanan() & Guard Clauses ---");
        ProdukRepository produkRepo = new InMemoryProdukRepository();
        OrderRepository orderRepo = new InMemoryOrderRepository();
        TaxCalculator taxCalc = new TaxCalculator(11.0);
        DiscountService discountService = new DiscountService();
        NotificationService notificationService = new EmailNotificationService();

        Produk p = new Produk(new ProductCode("PRD-RAM01"), "RAM 16GB DDR4", Money.of(800_000), 5, "Memory");
        produkRepo.save(p);

        OrderProcessor processor = new OrderProcessor(
            produkRepo, orderRepo, taxCalc, discountService, notificationService
        );

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(p, 2)); // Subtotal: 1.600.000

        CheckoutRequest request = new CheckoutRequest(
            "ORD-TEST-001",
            "Arjuna",
            new CustomerEmail("arjuna@pens.ac.id"),
            "Sumenep",
            items,
            new VoucherCode("DISKON10"), // Diskon 10% = 160.000
            PaymentType.TRANSFER_BANK,
            "Urgent"
        );

        Order order = processor.prosesPesanan(request);

        // Subtotal = 1.600.000
        // Diskon 10% = 160.000
        // Taxable = 1.440.000
        // PPN 11% = 158.400
        // Grand Total = 1.598.400
        assertTrue("Order berhasil diproses", order != null);
        assertTrue("Subtotal bruto tepat 1.600.000", order.getSubtotal().equals(Money.of(1_600_000)));
        assertTrue("Diskon tepat 160.000", order.getDiscountAmount().equals(Money.of(160_000)));
        assertTrue("PPN 11% tepat 158.400", order.getTaxAmount().equals(Money.of(158_400)));
        assertTrue("Grand Total tepat 1.598.400", order.getGrandTotal().equals(Money.of(1_598_400)));
        assertTrue("Stok produk berkurang dari 5 menjadi 3", produkRepo.findByCode(p.getCode()).get().getStock() == 3);

        // Uji Guard Clause: Duplicate Order ID
        try {
            processor.prosesPesanan(request); // Menggunakan Order ID yang sama
            assertTrue("Duplicate order ID seharusnya melempar Exception", false);
        } catch (IllegalStateException e) {
            assertTrue("Guard clause duplicate order ID berhasil mencegah double processing", true);
        }

        // Uji Guard Clause: Stok Kurang
        try {
            List<OrderItem> overItems = new ArrayList<>();
            overItems.add(new OrderItem(p, 10)); // Minta 10, stok tersisa 3
            CheckoutRequest overRequest = new CheckoutRequest(
                "ORD-TEST-002",
                "Arjuna",
                new CustomerEmail("arjuna@pens.ac.id"),
                "Sumenep",
                overItems,
                null,
                PaymentType.COD,
                ""
            );
            processor.prosesPesanan(overRequest);
            assertTrue("Pesanan melebihi stok seharusnya melempar Exception", false);
        } catch (IllegalStateException e) {
            assertTrue("Guard clause stok tidak mencukupi berhasil menangkap error", true);
        }
    }

    private static void printSummary() {
        System.out.println("\n==========================================================================");
        System.out.printf("  HASIL PENGUJIAN OTOMATIS: %d LULUS (PASS), %d GAGAL (FAIL)\n", testsPassed, testsFailed);
        System.out.println("==========================================================================");
    }
}
