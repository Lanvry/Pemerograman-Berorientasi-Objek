import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Automated Test Suite Runner untuk Pengujian SOLID Principles.
 * Menguji secara ketat aspek SRP, OCP, LSP, ISP, dan DIP tanpa dependensi eksternal (JUnit-free runner).
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class TestRunner {
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final List<String> testLogs = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          P13 SOLID PRINCIPLES RIGOROUS AUTOMATED TEST SUITE          ");
        System.out.println("                  Arjuna Lanang Adiwarsana - 3125522010               ");
        System.out.println("======================================================================\n");

        runTest("TC-01 [SRP] Order Domain State & Subtotal Calculation", () -> testSRP_OrderDomainState());
        runTest("TC-02 [SRP] Item Input Validation & Invariant Guard", () -> testSRP_ItemValidation());
        runTest("TC-03 [OCP] Strategy Pattern Pluggability Across All Diskon", () -> testOCP_PluggableDiscounts());
        runTest("TC-04 [OCP] Custom Strategy Extension Without Service Modification", () -> testOCP_CustomDiscountExtension());
        runTest("TC-05 [LSP] Discount Subtypes Invariant Contract (0 <= d <= subtotal)", () -> testLSP_ContractInvariant());
        runTest("TC-06 [LSP] FixedDiscount Capping (Voucher > Subtotal -> Diskon = Subtotal)", () -> testLSP_FixedDiscountCapping());
        runTest("TC-07 [ISP] Printable Segregated Interface Access", () -> testISP_PrintableRoleInterface());
        runTest("TC-08 [ISP] Auditable Segregated Interface Audit Subsystem", () -> testISP_AuditableRoleInterface());
        runTest("TC-09 [DIP] CheckoutService via InMemoryRepository Mocking", () -> testDIP_InMemoryRepositoryInjection());
        runTest("TC-10 [DIP] CheckoutService via FileOrderRepository Persistence", () -> testDIP_FileRepositoryPersistence());
        runTest("TC-11 [DIP] Pluggable Mock Notifier Injection & Verification", () -> testDIP_PluggableNotifier());
        runTest("TC-12 [E2E] End-to-End Transaction Lifecycle & Receipt Integrity", () -> testEndToEndLifecycle());

        printSummary();
    }

    private static void runTest(String testName, Runnable testCase) {
        totalTests++;
        try {
            testCase.run();
            passedTests++;
            System.out.printf("  [PASS] %s\n", testName);
            testLogs.add(String.format("PASS: %s", testName));
        } catch (Throwable t) {
            failedTests++;
            System.out.printf("  [FAIL] %s -> %s\n", testName, t.getMessage());
            testLogs.add(String.format("FAIL: %s (%s)", testName, t.getMessage()));
            t.printStackTrace(System.out);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(String.format("Mismatch: expected [%s] but got [%s] - %s", expected, actual, message));
        }
    }

    private static void assertEqualsDouble(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(String.format("Mismatch: expected [%.2f] but got [%.2f] - %s", expected, actual, message));
        }
    }

    // =========================================================================
    // TEST CASES IMPLEMENTATION
    // =========================================================================

    private static void testSRP_OrderDomainState() {
        Order order = new Order("ORD-TEST-01", "Pelanggan A", "pelanggan@test.com");
        assertEquals(Order.Status.PENDING, order.getStatus(), "Status awal harus PENDING");
        
        order.addItem(new Item("ITM-1", "Barang 1", 10_000, 2));
        order.addItem(new Item("ITM-2", "Barang 2", 25_000, 1));
        
        assertEqualsDouble(45_000.0, order.calculateSubtotal(), 0.001, "Subtotal harus 45.000");
        assertEquals(3, order.getItemCount(), "Total quantity item harus 3");

        boolean removed = order.removeItem("ITM-1");
        assertTrue(removed, "Item 1 harus berhasil dihapus");
        assertEqualsDouble(25_000.0, order.calculateSubtotal(), 0.001, "Subtotal setelah hapus item harus 25.000");
    }

    private static void testSRP_ItemValidation() {
        Item item = new Item("I01", "Kopi", 15_000, 3);
        assertEqualsDouble(45_000.0, item.getSubtotal(), 0.001, "Subtotal item harus 45.000");

        boolean caught = false;
        try {
            new Item("", "Kopi", 1000, 1);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Harus melempar exception untuk ID item kosong");

        caught = false;
        try {
            new Item("I02", "Kopi", -500, 1);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Harus melempar exception untuk harga negatif");
    }

    private static void testOCP_PluggableDiscounts() {
        double subtotal = 100_000.0;
        IDiscountStrategy noDisc = new NoDiscount();
        IDiscountStrategy pctDisc = new PercentageDiscount(15.0); // 15% -> 15.000
        IDiscountStrategy fixDisc = new FixedDiscount(25_000.0);  // 25.000
        IDiscountStrategy vipDisc = new VipDiscount();            // 15% (karena < 500k) -> 15.000

        assertEqualsDouble(0.0, noDisc.calculateDiscount(subtotal), 0.001, "NoDiscount harus 0");
        assertEqualsDouble(15_000.0, pctDisc.calculateDiscount(subtotal), 0.001, "Percentage 15% harus 15.000");
        assertEqualsDouble(25_000.0, fixDisc.calculateDiscount(subtotal), 0.001, "Fixed discount harus 25.000");
        assertEqualsDouble(15_000.0, vipDisc.calculateDiscount(subtotal), 0.001, "VipDiscount < 500k harus 15%");
    }

    private static void testOCP_CustomDiscountExtension() {
        // Ekstensi strategi diskon baru tanpa modifikasi source code lainnya
        IDiscountStrategy flashSale = new IDiscountStrategy() {
            @Override
            public double calculateDiscount(double subtotal) {
                return subtotal * 0.50; // 50% flash sale
            }

            @Override
            public String getStrategyName() {
                return "Flash Sale 50%";
            }
        };

        assertEqualsDouble(50_000.0, flashSale.calculateDiscount(100_000.0), 0.001, "Diskon Flash Sale harus 50.000");
        assertEquals("Flash Sale 50%", flashSale.getStrategyName(), "Nama strategi harus sesuai");
    }

    private static void testLSP_ContractInvariant() {
        IDiscountStrategy[] strategies = new IDiscountStrategy[] {
            new NoDiscount(),
            new PercentageDiscount(20.0),
            new FixedDiscount(30_000.0),
            new VipDiscount()
        };

        double[] subtotals = {0.0, 10_000.0, 100_000.0, 1_000_000.0};
        for (IDiscountStrategy strategy : strategies) {
            for (double subtotal : subtotals) {
                double discount = strategy.calculateDiscount(subtotal);
                assertTrue(discount >= 0.0, strategy.getStrategyName() + " diskon tidak boleh negatif");
                assertTrue(discount <= subtotal, strategy.getStrategyName() + " diskon tidak boleh melebihi subtotal");
            }
        }
    }

    private static void testLSP_FixedDiscountCapping() {
        // Kasus voucher Rp 75.000 untuk belanjaan kecil Rp 30.000
        FixedDiscount voucher = new FixedDiscount(75_000.0);
        double discount = voucher.calculateDiscount(30_000.0);
        
        assertEqualsDouble(30_000.0, discount, 0.001, 
                "Diskon voucher harus dipatok maksimal sebesar subtotal belanjaan");
    }

    private static void testISP_PrintableRoleInterface() {
        Order order = new Order("ORD-P01", "Budi", "budi@email.com");
        order.addItem(new Item("ITM-P", "Buku Java", 50_000, 1));
        
        // Client hanya mengacu pada interface Printable
        Printable printable = order;
        String content = printable.getPrintContent();
        assertTrue(content.contains("ORD-P01"), "Printable content harus memuat nomor order");
        assertTrue(content.contains("Buku Java"), "Printable content harus memuat nama item");
    }

    private static void testISP_AuditableRoleInterface() {
        Order order = new Order("ORD-A01", "Siti", "siti@email.com");
        order.addItem(new Item("ITM-A", "Mouse", 80_000, 1));

        // Sub-sistem audit hanya mengacu pada Auditable
        Auditable auditable = order;
        assertEquals("AUDIT-ORD-A01", auditable.getAuditId(), "Audit ID harus sesuai");
        assertTrue(auditable.getAuditMetadata().contains("Siti"), "Audit metadata harus memuat pelanggan");
        assertTrue(auditable.getAuditTimestamp() != null, "Audit timestamp tidak boleh null");
    }

    private static void testDIP_InMemoryRepositoryInjection() {
        IOrderRepository inMemoryRepo = new InMemoryOrderRepository();
        CheckoutService service = new CheckoutService(
                inMemoryRepo,
                new NoDiscount(),
                new ConsoleReceiptPrinter(),
                new EmailNotifier()
        );

        Order order = new Order("ORD-MEM-01", "Rudi", "rudi@mail.com");
        order.addItem(new Item("IT-1", "Kabel LAN", 20_000, 2));

        Receipt receipt = service.processCheckout(order);
        assertEqualsDouble(40_000.0, receipt.getNetTotal(), 0.001, "Net total harus 40.000");
        assertEquals(1, inMemoryRepo.count(), "Repository in-memory harus menyimpan 1 record");
        assertEquals(order, inMemoryRepo.findById("ORD-MEM-01"), "Harus menemukan kembali order dari memory");
    }

    private static void testDIP_FileRepositoryPersistence() {
        String testFile = "test_orders_temp.txt";
        new File(testFile).delete(); // Bersihkan jika ada file lama

        IOrderRepository fileRepo = new FileOrderRepository(testFile);
        CheckoutService service = new CheckoutService(
                fileRepo,
                new PercentageDiscount(10.0),
                new ConsoleReceiptPrinter(),
                new EmailNotifier()
        );

        Order order = new Order("ORD-FILE-01", "Dewi", "dewi@mail.com");
        order.addItem(new Item("IT-F1", "Headset", 100_000, 1));

        Receipt receipt = service.processCheckout(order);
        assertEqualsDouble(90_000.0, receipt.getNetTotal(), 0.001, "Net total diskon 10% harus 90.000");

        // Verifikasi baca ulang dari file baru
        IOrderRepository reloadRepo = new FileOrderRepository(testFile);
        assertEquals(1, reloadRepo.count(), "Repository file harus membaca 1 pesanan");
        Order loaded = reloadRepo.findById("ORD-FILE-01");
        assertTrue(loaded != null, "Order harus berhasil di-load kembali dari file");
        assertEquals("Dewi", loaded.getCustomerName(), "Nama pelanggan harus konsisten");

        // Cleanup
        new File(testFile).delete();
    }

    private static void testDIP_PluggableNotifier() {
        // Mock Notifier untuk membuktikan DIP dan testability
        class MockNotifier implements INotifier {
            boolean wasCalled = false;
            String lastRecipient;

            @Override
            public void sendNotification(String recipient, String subject, String message) {
                this.wasCalled = true;
                this.lastRecipient = recipient;
            }
        }

        MockNotifier mockNotifier = new MockNotifier();
        CheckoutService service = new CheckoutService(
                new InMemoryOrderRepository(),
                new NoDiscount(),
                new ConsoleReceiptPrinter(),
                mockNotifier
        );

        Order order = new Order("ORD-NOTIF-01", "Doni", "doni@test.com");
        order.addItem(new Item("IT-N", "Speaker", 200_000, 1));
        service.processCheckout(order);

        assertTrue(mockNotifier.wasCalled, "Mock Notifier harus dipanggil saat checkout");
        assertEquals("doni@test.com", mockNotifier.lastRecipient, "Penerima email harus cocok");
    }

    private static void testEndToEndLifecycle() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        VipDiscount vipDiscount = new VipDiscount(); // Subtotal 600.000 >= 500.000 -> 20%
        EmailNotifier notifier = new EmailNotifier();

        CheckoutService service = new CheckoutService(
                repo,
                vipDiscount,
                new ConsoleReceiptPrinter(),
                notifier
        );

        Order order = new Order("ORD-E2E-99", "Prof. Bambang", "bambang@pens.ac.id");
        order.addItem(new Item("PROD-1", "Monitor 24 Inch IPS", 600_000, 1));

        assertEquals(Order.Status.PENDING, order.getStatus(), "Status awal pesanan");
        Receipt receipt = service.processCheckout(order);

        assertEquals(Order.Status.PAID, order.getStatus(), "Status setelah checkout harus PAID");
        assertEqualsDouble(600_000.0, receipt.getSubtotal(), 0.001, "Gross subtotal harus 600.000");
        assertEqualsDouble(120_000.0, receipt.getDiscountAmount(), 0.001, "Diskon VIP 20% harus 120.000");
        assertEqualsDouble(480_000.0, receipt.getNetTotal(), 0.001, "Net total harus 480.000");
        assertEquals(1, repo.count(), "Repository harus menyimpan order");
        assertEquals(1, notifier.getSentCount(), "Notifikasi harus terkirim 1 kali");
    }

    private static void printSummary() {
        System.out.println("\n======================================================================");
        System.out.printf(" HASIL PENGUJIAN: %d Total | %d Passed | %d Failed (Success Rate: %.1f%%)\n",
                totalTests, passedTests, failedTests, ((double) passedTests / totalTests) * 100.0);
        System.out.println("======================================================================");
        if (failedTests == 0) {
            System.out.println(" >>> SELURUH 12 SKENARIO UJI SOLID BERHASIL DENGAN STATUS 100% HIJAU! <<<");
        } else {
            System.out.println(" >>> PERINGATAN: TERDAPAT SKENARIO UJI YANG GAGAL! <<<");
        }
        System.out.println("======================================================================\n");
    }
}
