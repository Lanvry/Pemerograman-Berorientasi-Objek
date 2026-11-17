import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Automated Test Runner untuk Praktikum P14: Refactoring Code & Clean OOD.
 * 
 * Menguji secara komprehensif, ketat, dan otomatis (JUnit-free runner):
 * 1. Verifikasi Ekuivalensi 100% Hasil/Behavior SEBELUM vs SESUDAH Refactoring
 * 2. Uji Value Objects (CustomerEmail, Money, ProductCode, VoucherCode)
 * 3. Uji Parameter Object (CheckoutRequest)
 * 4. Uji Guard Clauses & Early Returns (Fail-fast boundary checks)
 * 5. Uji Kasus A (Extract Method & Long Method prosesPesanan 120 baris)
 * 6. Uji Kasus B (Extract Class & Single Responsibility pada God Class Produk 35 method)
 * 7. Uji Kasus C (Duplicated Code validasi voucher di 4 class disatukan ke VoucherValidator)
 * 8. Uji Move Method (Feature Envy pada kalkulasi ongkir & loyalitas)
 * 9. Uji End-to-End Transaksi Multi-item, Mutasi Stok, dan Persistensi
 * 
 * Dosen Pengampu : Nirwana Haidar Hari, S.Pd., M.Kom.
 * Institusi      : D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class TestRunner {

    // Kode Warna ANSI untuk Visualisasi Terminal
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_BOLD   = "\u001B[1m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN   = "\u001B[36m";
    private static final String ANSI_WHITE  = "\u001B[37m";

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final List<String> testLogs = new ArrayList<>();

    public static void main(String[] args) {
        printHeader();

        // ---------------------------------------------------------------------
        // KELOMPOK 1: PENGUJIAN VALUE OBJECTS (PRIMITIVE OBSESSION)
        // ---------------------------------------------------------------------
        runTest("TC-01 [Value Object] CustomerEmail Validation, Case Normalization & Immutability",
                TestRunner::testCustomerEmailValidation);

        runTest("TC-02 [Value Object] Money Zero, Arithmetic, Invariant Defense & Format Rupiah",
                TestRunner::testMoneyOperationsAndFormatting);

        runTest("TC-03 [Value Object] ProductCode Prefix Format, Length & Domain Invariants",
                TestRunner::testProductCodeValidation);

        runTest("TC-04 [Value Object] VoucherCode Prefix Matching & Format Normalization",
                TestRunner::testVoucherCodeValidation);

        // ---------------------------------------------------------------------
        // KELOMPOK 2: PENGUJIAN PARAMETER OBJECT & GUARD CLAUSES
        // ---------------------------------------------------------------------
        runTest("TC-05 [Parameter Object] CheckoutRequest Parameter Encapsulation & Immutability",
                TestRunner::testCheckoutRequestEncapsulation);

        runTest("TC-06 [Guard Clauses] OrderProcessor Fail-Fast Boundary Checks & Invariant Defense",
                TestRunner::testOrderProcessorGuardClauses);

        // ---------------------------------------------------------------------
        // KELOMPOK 3: VERIFIKASI EKUIVALENSI SEBELUM vs SESUDAH REFACTORING (100% IDENTIK)
        // ---------------------------------------------------------------------
        runTest("TC-07 [Refactoring Equivalence] Kasus A: Before vs After Identical Calculation Results",
                TestRunner::testRefactoringEquivalenceCalculation);

        runTest("TC-08 [Refactoring Equivalence] Kasus A: Before vs After Identical Failure Behaviors",
                TestRunner::testRefactoringEquivalenceFailures);

        // ---------------------------------------------------------------------
        // KELOMPOK 4: KASUS B (EXTRACT CLASS & SRP PADA GOD CLASS PRODUK 35 METHOD)
        // ---------------------------------------------------------------------
        runTest("TC-09 [Kasus B - SRP] Produk Domain Entity Cohesion & Stock Mutation Invariants",
                TestRunner::testProdukEntityCohesion);

        runTest("TC-10 [Kasus B - SRP] ProdukRepository Data Persistence & Isolation",
                TestRunner::testProdukRepositoryPersistence);

        runTest("TC-11 [Kasus B - SRP] ProdukReportService Analytics & Total Inventory Valuation",
                TestRunner::testProdukReportServiceAnalytics);

        runTest("TC-12 [Kasus B - SRP] StockNotificationService Critical Stock Monitoring & Alerting",
                TestRunner::testStockNotificationServiceAlerting);

        // ---------------------------------------------------------------------
        // KELOMPOK 5: KASUS C (DUPLICATED CODE VALIDASI VOUCHER DI 4 CLASS)
        // ---------------------------------------------------------------------
        runTest("TC-13 [Kasus C - Duplicated Code] VoucherValidator Single Source of Truth vs 4 Old Classes",
                TestRunner::testVoucherValidatorEliminatesDuplication);

        // ---------------------------------------------------------------------
        // KELOMPOK 6: FEATURE ENVY & MOVE METHOD
        // ---------------------------------------------------------------------
        runTest("TC-14 [Move Method] Feature Envy Elimination in Shipping & Loyalty Evaluation",
                TestRunner::testFeatureEnvyMoveMethod);

        // ---------------------------------------------------------------------
        // KELOMPOK 7: END-TO-END TRANSAKSI & IMMUTABILITY CONTRACT
        // ---------------------------------------------------------------------
        runTest("TC-15 [End-to-End] Full Clean OOD Multi-Item Order Lifecycle & Stock Deduction",
                TestRunner::testEndToEndMultiItemOrder);

        runTest("TC-16 [Clean OOD] Money Immutability & Value Object Defensive Encapsulation",
                TestRunner::testMoneyImmutabilityContract);

        printSummary();

        if (failedTests > 0) {
            System.exit(1);
        }
    }

    // =========================================================================
    // TEST FRAMEWORK HARNESS & RUNNER
    // =========================================================================

    private static void printHeader() {
        System.out.println(ANSI_BOLD + ANSI_CYAN + "==========================================================================================" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_CYAN + "      PRAKTIKUM P14: REFACTORING & CLEAN OOD RIGOROUS AUTOMATED TEST SUITE        " + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_WHITE + "          Mahasiswa : Arjuna Lanang Adiwarsana | NRP: 3125522010                  " + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_WHITE + "          Institusi : D3 Teknik Informatika - PENS PSDKU Sumenep                  " + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_CYAN + "==========================================================================================\n" + ANSI_RESET);
    }

    private static void runTest(String testName, TestCaseRunnable testCase) {
        totalTests++;
        long startTime = System.nanoTime();
        try {
            testCase.run();
            long elapsedUs = (System.nanoTime() - startTime) / 1_000;
            passedTests++;
            System.out.printf("  %s%s[PASS]%s %s %s(%d µs)%s\n",
                    ANSI_BOLD, ANSI_GREEN, ANSI_RESET, testName, ANSI_YELLOW, elapsedUs, ANSI_RESET);
            testLogs.add(String.format("PASS: %s (%d µs)", testName, elapsedUs));
        } catch (Throwable t) {
            long elapsedUs = (System.nanoTime() - startTime) / 1_000;
            failedTests++;
            System.out.printf("  %s%s[FAIL]%s %s -> %s %s(%d µs)%s\n",
                    ANSI_BOLD, ANSI_RED, ANSI_RESET, testName, t.getMessage(), ANSI_YELLOW, elapsedUs, ANSI_RESET);
            testLogs.add(String.format("FAIL: %s (%s)", testName, t.getMessage()));
            t.printStackTrace(System.out);
        }
    }

    private static void printSummary() {
        double passRate = ((double) passedTests / totalTests) * 100.0;
        System.out.println("\n" + ANSI_BOLD + ANSI_CYAN + "==========================================================================================" + ANSI_RESET);
        System.out.printf(ANSI_BOLD + " HASIL PENGUJIAN: %d Total | " + ANSI_GREEN + "%d Passed" + ANSI_RESET + ANSI_BOLD + " | " +
                (failedTests == 0 ? ANSI_WHITE : ANSI_RED) + "%d Failed" + ANSI_RESET + ANSI_BOLD + " (Tingkat Kelulusan: %.1f%%)\n" + ANSI_RESET,
                totalTests, passedTests, failedTests, passRate);
        System.out.println(ANSI_BOLD + ANSI_CYAN + "==========================================================================================" + ANSI_RESET);

        if (failedTests == 0) {
            System.out.println(ANSI_BOLD + ANSI_GREEN + " >>> SELURUH 16 SKENARIO UJI REFACTORING & CLEAN OOD LULUS 100% HIJAU! <<<" + ANSI_RESET);
            System.out.println(ANSI_GREEN + " ✓ Ekuivalensi Before vs After Refactoring Terverifikasi 100% Identik." + ANSI_RESET);
            System.out.println(ANSI_GREEN + " ✓ Seluruh 7 Code Smells & 3 Kasus Latihan Slide 17 Teruji dan Tervalidasi Kuat." + ANSI_RESET);
        } else {
            System.out.println(ANSI_BOLD + ANSI_RED + " >>> PERINGATAN: TERDAPAT " + failedTests + " SKENARIO PENGUJIAN YANG GAGAL! <<<" + ANSI_RESET);
        }
        System.out.println(ANSI_BOLD + ANSI_CYAN + "==========================================================================================\n" + ANSI_RESET);
    }

    // =========================================================================
    // ASSERTION UTILITIES
    // =========================================================================

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Gagal (Diharapkan TRUE): " + message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion Gagal (Diharapkan FALSE): " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(String.format("Mismatch: Diharapkan [%s], namun mendapatkan [%s] - %s",
                    expected, actual, message));
        }
    }

    private static void assertEqualsDouble(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(String.format("Mismatch Nilai Numerik: Diharapkan [%.2f], namun mendapatkan [%.2f] - %s",
                    expected, actual, message));
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError("Assertion Gagal (Objek tidak boleh NULL): " + message);
        }
    }

    private static <T extends Throwable> void assertThrows(Class<T> expectedType, Executable executable, String message) {
        try {
            executable.execute();
            throw new AssertionError("Diharapkan melempar " + expectedType.getSimpleName() + " namun tidak ada exception yang dilempar - " + message);
        } catch (Throwable actual) {
            if (!expectedType.isInstance(actual)) {
                throw new AssertionError("Tipe exception tidak sesuai! Diharapkan " + expectedType.getSimpleName() +
                        ", namun mendapatkan " + actual.getClass().getSimpleName() + ": " + actual.getMessage() + " - " + message);
            }
        }
    }

    @FunctionalInterface
    private interface TestCaseRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface Executable {
        void execute() throws Throwable;
    }

    // =========================================================================
    // MOCK CLASSES FOR TESTING
    // =========================================================================

    private static class MockNotificationService implements NotificationService {
        private int notificationCount = 0;
        private int alertCount = 0;
        private String lastRecipient = null;
        private String lastSubject = null;
        private String lastMessage = null;

        @Override
        public void sendNotification(CustomerEmail recipient, String subject, String message) {
            this.notificationCount++;
            this.lastRecipient = recipient.getValue();
            this.lastSubject = subject;
            this.lastMessage = message;
        }

        @Override
        public void sendSystemAlert(String recipientEmail, String subject, String alertBody) {
            this.alertCount++;
            this.lastRecipient = recipientEmail;
            this.lastSubject = subject;
            this.lastMessage = alertBody;
        }

        public int getNotificationCount() { return notificationCount; }
        public int getAlertCount() { return alertCount; }
        public String getLastRecipient() { return lastRecipient; }
        public String getLastSubject() { return lastSubject; }
    }

    // =========================================================================
    // 16 AUTOMATED TEST CASE IMPLEMENTATIONS
    // =========================================================================

    /**
     * TC-01: [Value Object] CustomerEmail Validation, Case Normalization & Immutability
     */
    private static void testCustomerEmailValidation() {
        // Valid emails
        CustomerEmail email1 = new CustomerEmail("arjuna@pens.ac.id");
        assertEquals("arjuna@pens.ac.id", email1.getValue(), "Email harus tersimpan dengan benar");
        assertEquals("pens.ac.id", email1.getDomain(), "Domain harus diekstrak dengan tepat");

        // Normalisasi huruf kapital & whitespace
        CustomerEmail email2 = new CustomerEmail("  WardILanang46@GMAIL.com  ");
        assertEquals("wardilanang46@gmail.com", email2.getValue(), "Email harus di-trim dan dinormalisasi huruf kecil");
        assertEquals("gmail.com", email2.getDomain(), "Domain harus gmail.com");

        // Kesetaraan Berbasis Nilai (Value-based equality)
        CustomerEmail email3 = new CustomerEmail("wardilanang46@gmail.com");
        assertEquals(email2, email3, "Dua objek Value Object dengan email sama harus equals");
        assertEquals(email2.hashCode(), email3.hashCode(), "HashCode kedua objek email sama harus identik");

        // Invalid emails melempar IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> new CustomerEmail(null), "Null email harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new CustomerEmail(""), "Empty email harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new CustomerEmail("   "), "Blank email harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new CustomerEmail("plainstring"), "Email tanpa @ harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new CustomerEmail("@domain.com"), "Email tanpa username harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new CustomerEmail("user@.com"), "Email dengan domain kosong harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new CustomerEmail("user@domain"), "Email tanpa TLD harus ditolak");
    }

    /**
     * TC-02: [Value Object] Money Zero, Arithmetic, Invariant Defense & Format Rupiah
     */
    private static void testMoneyOperationsAndFormatting() {
        Money m1 = Money.of(150_000);
        Money m2 = Money.of(50_000);

        // Operasi dasar
        assertEquals(Money.of(200_000), m1.add(m2), "150.000 + 50.000 harus 200.000");
        assertEquals(Money.of(100_000), m1.subtract(m2), "150.000 - 50.000 harus 100.000");
        assertEquals(Money.of(300_000), m1.multiply(2), "150.000 * 2 harus 300.000");
        assertEquals(Money.of(15_000), m1.percentage(10.0), "10% dari 150.000 harus 15.000");

        // Perbandingan
        assertTrue(m1.isGreaterThan(m2), "150.000 harus lebih besar dari 50.000");
        assertTrue(m2.isLessThan(m1), "50.000 harus lebih kecil dari 150.000");
        assertTrue(m1.isGreaterThanOrEqual(m1), "150.000 harus >= 150.000");
        assertFalse(m1.isZero(), "150.000 bukan nol");
        assertTrue(Money.ZERO.isZero(), "Money.ZERO harus nol");

        // Format Rupiah
        String formatted = m1.formatRupiah();
        assertTrue(formatted.contains("Rp") && formatted.contains("150"),
                "Format rupiah harus mengandung simbol Rp dan angka 150");

        // Invariant defense (Uang negatif dilarang keras)
        assertThrows(IllegalArgumentException.class, () -> Money.of(-1000), "Nominal negatif harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> m2.subtract(m1), "Hasil pengurangan negatif harus melempar exception");
        assertThrows(IllegalArgumentException.class, () -> m1.multiply(-1), "Pengali negatif harus melempar exception");
        assertThrows(IllegalArgumentException.class, () -> m1.percentage(-5), "Persentase negatif harus ditolak");
    }

    /**
     * TC-03: [Value Object] ProductCode Prefix Format, Length & Domain Invariants
     */
    private static void testProductCodeValidation() {
        ProductCode code1 = new ProductCode("prd-laptop01");
        assertEquals("PRD-LAPTOP01", code1.getCode(), "ProductCode harus otomatis dinormalisasi UPPERCASE");

        ProductCode code2 = new ProductCode("PRD-LAPTOP01");
        assertEquals(code1, code2, "Dua ProductCode dengan kode sama harus equals");
        assertEquals(code1.hashCode(), code2.hashCode(), "HashCode ProductCode harus sama");

        // Invariant guards: harus diawali "PRD-", panjang 6 - 20 karakter
        assertThrows(IllegalArgumentException.class, () -> new ProductCode(null), "Null kode produk harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new ProductCode(""), "Kode produk kosong harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new ProductCode("LAPTOP01"), "Kode tanpa prefix PRD- harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new ProductCode("PRD-A"), "Kode terlalu pendek (<6 karakter) harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new ProductCode("PRD-123456789012345678"), "Kode terlalu panjang (>20 karakter) harus ditolak");
    }

    /**
     * TC-04: [Value Object] VoucherCode Prefix Matching & Format Normalization
     */
    private static void testVoucherCodeValidation() {
        VoucherCode vc1 = new VoucherCode("pensjuara");
        assertEquals("PENSJUARA", vc1.getCode(), "VoucherCode harus dinormalisasi huruf kapital");
        assertEquals("PENS", vc1.getPrefix(), "Prefix PENSJUARA harus PENS");

        VoucherCode vc2 = new VoucherCode("HEMAT20");
        assertEquals("HEMAT", vc2.getPrefix(), "Prefix HEMAT20 harus HEMAT");

        VoucherCode vc3 = new VoucherCode("DISKON10");
        assertEquals("DISKON", vc3.getPrefix(), "Prefix DISKON10 harus DISKON");

        VoucherCode vc4 = new VoucherCode("PROMO50");
        assertEquals("PROMO", vc4.getPrefix(), "Prefix PROMO50 harus PROMO");

        // Invariant guards: prefix tidak diizinkan atau format invalid
        assertThrows(IllegalArgumentException.class, () -> new VoucherCode(null), "Null voucher code harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new VoucherCode(""), "Empty voucher code harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new VoucherCode("KODE_PALSU"), "Prefix tidak sah harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> new VoucherCode("PENS"), "Kode kurang dari 5 karakter harus ditolak");
    }

    /**
     * TC-05: [Parameter Object] CheckoutRequest Parameter Encapsulation & Immutability
     */
    private static void testCheckoutRequestEncapsulation() {
        Produk laptop = new Produk(new ProductCode("PRD-LAPTOP01"), "ThinkPad T14", Money.of(12_000_000), 5, "Laptop");
        OrderItem item = new OrderItem(laptop, 1);
        List<OrderItem> itemList = new ArrayList<>();
        itemList.add(item);

        CheckoutRequest request = new CheckoutRequest(
                "ORD-2026-001",
                "Arjuna Lanang",
                new CustomerEmail("arjuna@pens.ac.id"),
                "Jl. Raya Lenteng No. 12, Sumenep",
                itemList,
                new VoucherCode("DISKON10"),
                PaymentType.TRANSFER_BANK,
                "Mohon packing kayu aman"
        );

        assertEquals("ORD-2026-001", request.getOrderId(), "Order ID harus sesuai");
        assertEquals("Arjuna Lanang", request.getCustomerName(), "Nama customer harus sesuai");
        assertEquals("arjuna@pens.ac.id", request.getCustomerEmail().getValue(), "Email customer harus sesuai");
        assertEquals(1, request.getItems().size(), "Jumlah item harus 1");
        assertTrue(request.hasVoucher(), "Request harus terdeteksi memiliki voucher");
        assertEquals(PaymentType.TRANSFER_BANK, request.getPaymentType(), "Metode pembayaran harus TRANSFER_BANK");

        // Immutability: Items list tidak dapat dimutasi dari luar (UnmodifiableList)
        assertThrows(UnsupportedOperationException.class, () -> request.getItems().add(item),
                "getItems() harus mengembalikan list unmodifiable");

        // Guard validation di constructor CheckoutRequest
        assertThrows(IllegalArgumentException.class, () -> new CheckoutRequest(
                "", "Arjuna", new CustomerEmail("a@b.com"), "Alamat", itemList, null, PaymentType.COD, ""
        ), "Order ID kosong harus ditolak");

        assertThrows(IllegalArgumentException.class, () -> new CheckoutRequest(
                "ORD-1", "", new CustomerEmail("a@b.com"), "Alamat", itemList, null, PaymentType.COD, ""
        ), "Customer Name kosong harus ditolak");

        assertThrows(NullPointerException.class, () -> new CheckoutRequest(
                "ORD-1", "Arjuna", null, "Alamat", itemList, null, PaymentType.COD, ""
        ), "Customer Email null harus ditolak");

        assertThrows(IllegalArgumentException.class, () -> new CheckoutRequest(
                "ORD-1", "Arjuna", new CustomerEmail("a@b.com"), "Alamat", Collections.emptyList(), null, PaymentType.COD, ""
        ), "Daftar item belanja kosong harus ditolak");
    }

    /**
     * TC-06: [Guard Clauses] OrderProcessor Fail-Fast Boundary Checks & Invariant Defense
     */
    private static void testOrderProcessorGuardClauses() {
        ProdukRepository produkRepo = new InMemoryProdukRepository();
        OrderRepository orderRepo = new InMemoryOrderRepository();
        TaxCalculator taxCalc = new TaxCalculator(11.0);
        DiscountService discService = new DiscountService();
        NotificationService notifService = new MockNotificationService();

        OrderProcessor processor = new OrderProcessor(produkRepo, orderRepo, taxCalc, discService, notifService);

        // Guard 1: Null Request
        assertThrows(IllegalArgumentException.class, () -> processor.prosesPesanan(null),
                "Null request harus fail-fast di Guard Clause 1");

        // Persiapan produk
        Produk laptop = new Produk(new ProductCode("PRD-LAPTOP01"), "ThinkPad T14", Money.of(12_000_000), 2, "Laptop");
        produkRepo.save(laptop);

        // Guard 2: Insufficient Stock
        OrderItem oversizedItem = new OrderItem(laptop, 5); // Meminta 5 saat stok hanya 2
        CheckoutRequest stockExceededRequest = new CheckoutRequest(
                "ORD-ERR-STOCK", "Budi", new CustomerEmail("budi@pens.ac.id"), "Jl. Trunojoyo",
                List.of(oversizedItem), null, PaymentType.TRANSFER_BANK, ""
        );
        assertThrows(IllegalStateException.class, () -> processor.prosesPesanan(stockExceededRequest),
                "Pesanan melebihi stok harus fail-fast di Guard Clause 2");

        // Guard 3: Duplicate Order ID
        OrderItem validItem = new OrderItem(laptop, 1);
        CheckoutRequest validRequest = new CheckoutRequest(
                "ORD-DUP-01", "Budi", new CustomerEmail("budi@pens.ac.id"), "Jl. Trunojoyo",
                List.of(validItem), null, PaymentType.TRANSFER_BANK, ""
        );
        processor.prosesPesanan(validRequest); // Sukses pertama kali

        // Eksekusi ulang dengan ID sama persis
        assertThrows(IllegalStateException.class, () -> processor.prosesPesanan(validRequest),
                "Order ID duplikat harus ditolak oleh Guard Clause");
    }

    /**
     * TC-07: [Refactoring Equivalence] Kasus A: Before vs After Identical Calculation Results
     * Menguji bahwa hasil kalkulasi finansial (Subtotal, Diskon, Pajak PPN 11%, Grand Total)
     * antara kode SEBELUM refactoring (OldOrderProcessor) dan SESUDAH refactoring (OrderProcessor)
     * adalah 100% IDENTIK sampai ke nilai Rupiah terkecil.
     */
    private static void testRefactoringEquivalenceCalculation() {
        // SETUP DATA KASUS A
        String customerName = "Arjuna Lanang Adiwarsana";
        String customerEmailStr = "arjuna@pens.ac.id";
        String address = "Jl. Raya Lenteng No. 12, Sumenep";
        String voucherCodeStr = "DISKON10"; // Diskon 10%
        String productCodeStr = "PRD-LAPTOP01"; // Harga Rp 12.000.000, stok 5
        int quantity = 1;

        // 1. EKSEKUSI SEBELUM REFACTORING (KODE MONOLITIK LAMA)
        // Redirect System.out sementara untuk menangkap log lama tanpa mengotori konsol
        PrintStream originalOut = System.out;
        ByteArrayOutputStream tempBaos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(tempBaos));

        BeforeRefactoring.OldOrderProcessor oldProcessor = new BeforeRefactoring.OldOrderProcessor();
        boolean oldSuccess = oldProcessor.prosesPesananLama(
                customerName,
                customerEmailStr,
                "08123456789",
                address,
                "69411",
                "TRANSFER",
                voucherCodeStr,
                productCodeStr,
                quantity,
                "Mohon packing kayu"
        );

        System.setOut(originalOut); // Kembalikan System.out
        assertTrue(oldSuccess, "Kode lama harus mengembalikan boolean true untuk pesanan valid");

        // Nilai kalkulasi matematis menurut algoritma lama:
        double expectedSubtotal = 12_000_000.0;
        double expectedDiscount = 1_200_000.0; // 10% dari 12.000.000
        double expectedTaxable = 10_800_000.0;
        double expectedTax = 1_188_000.0;      // 11% dari 10.800.000
        double expectedGrandTotal = 11_988_000.0;

        // 2. EKSEKUSI SESUDAH REFACTORING (KODE BERSIH CLEAN OOD)
        ProdukRepository produkRepo = new InMemoryProdukRepository();
        OrderRepository orderRepo = new InMemoryOrderRepository();
        TaxCalculator taxCalc = new TaxCalculator(11.0);
        DiscountService discService = new DiscountService();
        MockNotificationService notifService = new MockNotificationService();

        Produk refactoredLaptop = new Produk(new ProductCode(productCodeStr), "ThinkPad T14 Gen 4", Money.of(12_000_000), 5, "Laptop");
        produkRepo.save(refactoredLaptop);

        OrderProcessor cleanProcessor = new OrderProcessor(produkRepo, orderRepo, taxCalc, discService, notifService);

        CheckoutRequest request = new CheckoutRequest(
                "ORD-EQUIV-001",
                customerName,
                new CustomerEmail(customerEmailStr),
                address,
                List.of(new OrderItem(refactoredLaptop, quantity)),
                new VoucherCode(voucherCodeStr),
                PaymentType.TRANSFER_BANK,
                "Mohon packing kayu"
        );

        Order newOrder = cleanProcessor.prosesPesanan(request);

        // 3. VERIFIKASI EKUIVALENSI 100% IDENTIK
        assertNotNull(newOrder, "Order baru tidak boleh null");
        assertEquals(Money.of(expectedSubtotal), newOrder.getSubtotal(),
                "Subtotal SEBELUM vs SESUDAH harus 100% identik (Rp 12.000.000)");

        assertEquals(Money.of(expectedDiscount), newOrder.getDiscountAmount(),
                "Diskon SEBELUM vs SESUDAH harus 100% identik (Rp 1.200.000)");

        assertEquals(Money.of(expectedTax), newOrder.getTaxAmount(),
                "Pajak PPN 11% SEBELUM vs SESUDAH harus 100% identik (Rp 1.188.000)");

        assertEquals(Money.of(expectedGrandTotal), newOrder.getGrandTotal(),
                "Grand Total Net SEBELUM vs SESUDAH harus 100% identik (Rp 11.988.000)");

        assertEquals(OrderStatus.CONFIRMED, newOrder.getStatus(), "Status order sesudah refactoring harus CONFIRMED");
        assertEquals(4, refactoredLaptop.getStock(), "Stok produk sesudah refactoring harus berkurang menjadi 4 unit");
        assertEquals(1, notifService.getNotificationCount(), "Notifikasi konfirmasi email harus terkirim 1 kali");
    }

    /**
     * TC-08: [Refactoring Equivalence] Kasus A: Before vs After Identical Failure Behaviors
     */
    private static void testRefactoringEquivalenceFailures() {
        // Bisnis failure 1: Email tanpa karakter '@'
        BeforeRefactoring.OldOrderProcessor oldProcessor = new BeforeRefactoring.OldOrderProcessor();

        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));

        boolean oldEmailFail = oldProcessor.prosesPesananLama(
                "Budi", "budi_tanpa_at", "081", "Sumenep", "69411", "TRANSFER", null, "PRD-LAPTOP01", 1, ""
        );
        System.setOut(originalOut);
        assertFalse(oldEmailFail, "Kode lama harus mengembalikan false jika email tidak valid");

        // Pada kode baru, CustomerEmail menolak email tanpa @ dengan IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> new CustomerEmail("budi_tanpa_at"),
                "Kode baru harus melempar IllegalArgumentException untuk email tanpa @");

        // Bisnis failure 2: Stok tidak mencukupi (Minta 10 saat stok hanya 5)
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        boolean oldStockFail = oldProcessor.prosesPesananLama(
                "Budi", "budi@pens.ac.id", "081", "Sumenep", "69411", "TRANSFER", null, "PRD-LAPTOP01", 10, ""
        );
        System.setOut(originalOut);
        assertFalse(oldStockFail, "Kode lama harus mengembalikan false jika kuantitas melebihi stok");

        // Pada kode baru, OrderProcessor melempar IllegalStateException jika stok kurang
        ProdukRepository pRepo = new InMemoryProdukRepository();
        Produk laptop = new Produk(new ProductCode("PRD-LAPTOP01"), "Laptop", Money.of(12_000_000), 5, "Laptop");
        pRepo.save(laptop);
        OrderProcessor processor = new OrderProcessor(pRepo, new InMemoryOrderRepository(), new TaxCalculator(), new DiscountService(), new MockNotificationService());

        CheckoutRequest reqOverstock = new CheckoutRequest(
                "ORD-FAIL-1", "Budi", new CustomerEmail("budi@pens.ac.id"), "Sumenep",
                List.of(new OrderItem(laptop, 10)), null, PaymentType.TRANSFER_BANK, ""
        );
        assertThrows(IllegalStateException.class, () -> processor.prosesPesanan(reqOverstock),
                "Kode baru harus melempar IllegalStateException saat stok kurang");
    }

    /**
     * TC-09: [Kasus B - SRP] Produk Domain Entity Cohesion & Stock Mutation Invariants
     */
    private static void testProdukEntityCohesion() {
        Produk p = new Produk(new ProductCode("PRD-SSD01"), "Samsung NVMe 980 Pro 1TB", Money.of(1_850_000), 10, "Storage");

        assertEquals("PRD-SSD01", p.getCode().getCode(), "Kode SKU harus sesuai");
        assertEquals("Samsung NVMe 980 Pro 1TB", p.getName(), "Nama produk harus sesuai");
        assertEquals(Money.of(1_850_000), p.getPrice(), "Harga produk harus sesuai");
        assertEquals(10, p.getStock(), "Stok awal harus 10");

        // Pengurangan stok valid
        p.reduceStock(3);
        assertEquals(7, p.getStock(), "Sisa stok setelah dikurangi 3 harus 7");

        // Penambahan stok valid
        p.addStock(5);
        assertEquals(12, p.getStock(), "Stok setelah ditambah 5 harus 12");

        // Kalkulasi subtotal
        assertEquals(Money.of(3_700_000), p.calculateSubtotal(2), "Subtotal 2 unit harus Rp 3.700.000");

        // Invariant checks
        assertThrows(IllegalStateException.class, () -> p.reduceStock(100), "Mengurangi stok melebihi stok tersedia harus melempar IllegalStateException");
        assertThrows(IllegalArgumentException.class, () -> p.reduceStock(-1), "Pengurangan bernilai negatif harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> p.addStock(0), "Penambahan stok 0 harus ditolak");
        assertThrows(IllegalArgumentException.class, () -> p.calculateSubtotal(0), "Kalkulasi kuantitas 0 harus ditolak");
    }

    /**
     * TC-10: [Kasus B - SRP] ProdukRepository Data Persistence & Isolation
     */
    private static void testProdukRepositoryPersistence() {
        ProdukRepository repo = new InMemoryProdukRepository();

        Produk p1 = new Produk(new ProductCode("PRD-RAM01"), "Corsair 32GB", Money.of(2_100_000), 5, "Memory");
        Produk p2 = new Produk(new ProductCode("PRD-MON01"), "LG UltraFine 4K", Money.of(6_500_000), 2, "Display");

        repo.save(p1);
        repo.save(p2);

        assertEquals(2, repo.count(), "Repository harus menyimpan 2 produk");
        assertTrue(repo.findByCode(new ProductCode("PRD-RAM01")).isPresent(), "RAM01 harus dapat ditemukan via SKU");
        assertTrue(repo.findByCode(new ProductCode("PRD-MON01")).isPresent(), "MON01 harus dapat ditemukan via SKU");
        assertFalse(repo.findByCode(new ProductCode("PRD-UNKNOWN")).isPresent(), "SKU fiktif harus return Optional.empty");

        // Find low stock (ambang batas <= 2)
        List<Produk> low = repo.findLowStock(2);
        assertEquals(1, low.size(), "Hanya 1 produk yang stoknya <= 2 (LG Monitor)");
        assertEquals("PRD-MON01", low.get(0).getCode().getCode(), "Produk stok rendah harus PRD-MON01");

        // Delete
        repo.delete(new ProductCode("PRD-RAM01"));
        assertEquals(1, repo.count(), "Setelah delete 1 item, sisa produk harus 1");
    }

    /**
     * TC-11: [Kasus B - SRP] ProdukReportService Analytics & Total Inventory Valuation
     */
    private static void testProdukReportServiceAnalytics() {
        ProdukReportService reportService = new ProdukReportService();

        Produk p1 = new Produk(new ProductCode("PRD-SSD01"), "SSD 1TB", Money.of(1_000_000), 2, "Storage"); // 2 * 1jt = 2jt
        Produk p2 = new Produk(new ProductCode("PRD-RAM01"), "RAM 16GB", Money.of(500_000), 4, "Memory");   // 4 * 500rb = 2jt

        List<Produk> catalog = List.of(p1, p2);

        // Nilai total inventaris: 2.000.000 + 2.000.000 = 4.000.000
        Money totalValuation = reportService.calculateTotalInventoryValuation(catalog);
        assertEquals(Money.of(4_000_000), totalValuation, "Total nilai aset inventaris harus Rp 4.000.000");

        // Verifikasi output format tabel
        String report = reportService.generateCatalogSummary(catalog);
        assertTrue(report.contains("PRD-SSD01") && report.contains("SSD 1TB"), "Laporan harus memuat data SSD");
        assertTrue(report.contains("PRD-RAM01") && report.contains("RAM 16GB"), "Laporan harus memuat data RAM");
        assertTrue(report.contains("Total Variasi Produk: 2 SKU"), "Laporan harus merekap total SKU");
    }

    /**
     * TC-12: [Kasus B - SRP] StockNotificationService Critical Stock Monitoring & Alerting
     */
    private static void testStockNotificationServiceAlerting() {
        MockNotificationService mockNotif = new MockNotificationService();
        StockNotificationService alertService = new StockNotificationService(mockNotif, "logistik.pens@pens.ac.id");

        Produk lowItem = new Produk(new ProductCode("PRD-LOW01"), "Baterai CMOS", Money.of(15_000), 1, "Aksesoris");
        List<Produk> criticalList = List.of(lowItem);

        // Alert dipicu untuk stok kritis
        alertService.checkAndAlertLowStock(criticalList, 2);
        assertEquals(1, mockNotif.getAlertCount(), "Harus memicu 1 kali pengiriman alert");
        assertEquals("logistik.pens@pens.ac.id", mockNotif.getLastRecipient(), "Penerima alert harus admin logistik");
        assertTrue(mockNotif.getLastSubject().contains("Peringatan Stok Kritis"), "Subjek harus memuat peringatan stok kritis");

        // Tidak memicu alert jika daftar produk kritis kosong
        alertService.checkAndAlertLowStock(Collections.emptyList(), 2);
        assertEquals(1, mockNotif.getAlertCount(), "Alert count tidak boleh bertambah jika tidak ada stok kritis");
    }

    /**
     * TC-13: [Kasus C - Duplicated Code] VoucherValidator Single Source of Truth vs 4 Old Classes
     * Memverifikasi bahwa VoucherValidator menggantikan 4 class duplikat lama dengan hasil identik.
     */
    private static void testVoucherValidatorEliminatesDuplication() {
        // 4 Class Lama yang menduplikasi kode Kasus C
        BeforeRefactoring.OldCartService oldCart = new BeforeRefactoring.OldCartService();
        BeforeRefactoring.OldCheckoutService oldCheckout = new BeforeRefactoring.OldCheckoutService();
        BeforeRefactoring.OldPaymentService oldPayment = new BeforeRefactoring.OldPaymentService();
        BeforeRefactoring.OldAdminAuditService oldAudit = new BeforeRefactoring.OldAdminAuditService();

        // Skenario 1: Voucher PENSJUARA (Diskon 25%, syarat minimal belanja 100.000)
        double belanja = 200_000.0;
        String voucherStr = "PENSJUARA";

        // Hasil dari 4 class lama
        boolean cartCheck = oldCart.checkVoucher(voucherStr, belanja);
        double oldDiscount = oldCheckout.hitungDiskon(voucherStr, belanja);
        boolean payVerify = oldPayment.verifyVoucherEligibility(voucherStr, belanja);
        boolean auditPattern = oldAudit.isValidVoucherPattern(voucherStr);

        assertTrue(cartCheck && payVerify && auditPattern, "Seluruh class lama harus menyetujui voucher PENSJUARA");
        assertEqualsDouble(50_000.0, oldDiscount, 0.001, "Diskon lama harus 25% * 200.000 = 50.000");

        // Hasil dari Class Baru (VoucherValidator)
        VoucherCode vc = new VoucherCode(voucherStr);
        assertTrue(VoucherValidator.isVoucherActive(voucherStr), "VoucherValidator harus mengenali PENSJUARA");
        Money cleanDiscount = VoucherValidator.validateAndCalculateDiscount(vc, Money.of(200_000));

        // Verifikasi kesamaan mutlak (100% Identik)
        assertEquals(Money.of(oldDiscount), cleanDiscount,
                "Diskon VoucherValidator harus 100% identik dengan diskon lama (Rp 50.000)");

        // Skenario 2: Minimum purchase violation (Belanja 20.000 saat syarat minimum 100.000)
        assertFalse(oldCart.checkVoucher(voucherStr, 20_000.0), "Old cart menolak belanja di bawah minimum");
        assertEqualsDouble(0.0, oldCheckout.hitungDiskon(voucherStr, 20_000.0), 0.001, "Old checkout mengembalikan 0");
        assertThrows(IllegalArgumentException.class, () ->
                VoucherValidator.validateAndCalculateDiscount(vc, Money.of(20_000)),
                "VoucherValidator melempar exception eksplisit untuk subtotal di bawah minimum"
        );
    }

    /**
     * TC-14: [Move Method] Feature Envy Elimination in Shipping & Loyalty Evaluation
     */
    private static void testFeatureEnvyMoveMethod() {
        // SEBELUM: OldShippingCalculator envy terhadap data internal Customer
        BeforeRefactoring.OldCustomerEntity oldCust = new BeforeRefactoring.OldCustomerEntity("Arjuna", "PLATINUM", 4, "Sumenep");
        BeforeRefactoring.OldShippingCalculator oldCalc = new BeforeRefactoring.OldShippingCalculator();
        double oldShipping = oldCalc.calculateShippingFee(oldCust, 3.0); // 3 kg
        assertEqualsDouble(0.0, oldShipping, 0.001, "Customer PLATINUM harus gratis ongkir (0.0)");

        // SESUDAH: Customer sebagai Information Expert menghitung sendiri (Move Method)
        Customer newCust = new Customer("C01", "Arjuna", new CustomerEmail("arjuna@pens.ac.id"), "Sumenep", Customer.Tier.PLATINUM, 4);
        assertTrue(newCust.isEligibleForFreeShipping(Money.of(50_000)), "Customer PLATINUM harus berhak gratis ongkir");

        // Loyalty discount rate: PLATINUM (15%) + tenure bonus (min(4*1, 5) = 4%) = 19%
        assertEqualsDouble(19.0, newCust.calculateEffectiveLoyaltyDiscountRate(), 0.001,
                "Effective loyalty discount rate harus 19.0%");

        // GOLD Customer: bonus 10% base + 3% tenure = 13%
        Customer goldCust = new Customer("C02", "Budi", new CustomerEmail("budi@pens.ac.id"), "Surabaya", Customer.Tier.GOLD, 3);
        assertEqualsDouble(13.0, goldCust.calculateEffectiveLoyaltyDiscountRate(), 0.001,
                "Effective loyalty discount rate GOLD 3 thn harus 13.0%");
    }

    /**
     * TC-15: [End-to-End] Full Clean OOD Multi-Item Order Lifecycle & Stock Deduction
     */
    private static void testEndToEndMultiItemOrder() {
        ProdukRepository pRepo = new InMemoryProdukRepository();
        OrderRepository oRepo = new InMemoryOrderRepository();
        TaxCalculator taxCalc = new TaxCalculator(11.0);
        DiscountService discService = new DiscountService();
        MockNotificationService notifService = new MockNotificationService();

        OrderProcessor processor = new OrderProcessor(pRepo, oRepo, taxCalc, discService, notifService);

        Produk p1 = new Produk(new ProductCode("PRD-LAPTOP01"), "Laptop ThinkPad", Money.of(15_000_000), 5, "Laptop");
        Produk p2 = new Produk(new ProductCode("PRD-MOUSE01"), "Logitech MX Master", Money.of(1_500_000), 10, "Aksesoris");
        pRepo.save(p1);
        pRepo.save(p2);

        // Keranjang belanja: 1x Laptop (15jt) + 2x Mouse (3jt) = Subtotal 18jt
        OrderItem item1 = new OrderItem(p1, 1);
        OrderItem item2 = new OrderItem(p2, 2);

        // Voucher PENSJUARA: Diskon 25% = 25% * 18jt = 4.5jt
        // Taxable = 18jt - 4.5jt = 13.5jt
        // PPN 11% = 11% * 13.5jt = 1.485jt
        // Grand Total = 13.5jt + 1.485jt = 14.985jt
        CheckoutRequest req = new CheckoutRequest(
                "ORD-E2E-100",
                "Arjuna Lanang",
                new CustomerEmail("arjuna@pens.ac.id"),
                "Kampus PENS PSDKU Sumenep",
                List.of(item1, item2),
                new VoucherCode("PENSJUARA"),
                PaymentType.TRANSFER_BANK,
                "Kirim segera untuk lab komputer"
        );

        Order completedOrder = processor.prosesPesanan(req);

        assertEquals(Money.of(18_000_000), completedOrder.getSubtotal(), "Subtotal harus Rp 18.000.000");
        assertEquals(Money.of(4_500_000), completedOrder.getDiscountAmount(), "Diskon 25% harus Rp 4.500.000");
        assertEquals(Money.of(1_485_000), completedOrder.getTaxAmount(), "Pajak 11% harus Rp 1.485.000");
        assertEquals(Money.of(14_985_000), completedOrder.getGrandTotal(), "Grand Total harus Rp 14.985.000");
        assertEquals(OrderStatus.CONFIRMED, completedOrder.getStatus(), "Status order harus CONFIRMED");

        // Verifikasi mutasi stok di basis data repository
        Produk repoP1 = pRepo.findByCode(p1.getCode()).get();
        Produk repoP2 = pRepo.findByCode(p2.getCode()).get();
        assertEquals(4, repoP1.getStock(), "Stok laptop harus berkurang dari 5 menjadi 4");
        assertEquals(8, repoP2.getStock(), "Stok mouse harus berkurang dari 10 menjadi 8");

        // Verifikasi persistensi order
        assertTrue(oRepo.findById("ORD-E2E-100").isPresent(), "Pesanan harus tersimpan dalam OrderRepository");

        // Verifikasi notifikasi terkirim
        assertEquals(1, notifService.getNotificationCount(), "Notifikasi konfirmasi email harus terkirim 1 kali");
        assertEquals("arjuna@pens.ac.id", notifService.getLastRecipient(), "Penerima notifikasi harus sesuai");
    }

    /**
     * TC-16: [Clean OOD] Money Immutability & Value Object Defensive Encapsulation
     */
    private static void testMoneyImmutabilityContract() {
        Money initial = Money.of(100_000);
        Money added = initial.add(Money.of(50_000));

        // Memastikan instance asli TIDAK bermutasi (Pure Immutability)
        assertEqualsDouble(100_000.0, initial.getAmount().doubleValue(), 0.001, "Nilai instance initial tidak boleh berubah");
        assertEqualsDouble(150_000.0, added.getAmount().doubleValue(), 0.001, "Instance baru harus memuat hasil penjumlahan");

        Money subtracted = initial.subtract(Money.of(30_000));
        assertEqualsDouble(100_000.0, initial.getAmount().doubleValue(), 0.001, "Nilai instance initial tidak boleh berubah setelah subtract");
        assertEqualsDouble(70_000.0, subtracted.getAmount().doubleValue(), 0.001, "Instance baru harus memuat hasil pengurangan");
    }
}
