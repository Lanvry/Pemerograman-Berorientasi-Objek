import java.util.ArrayList;
import java.util.List;

/**
 * Driver Utama Praktikum P14: Refactoring & Clean Object-Oriented Design (OOD)
 * 
 * Mengimplementasikan dan mendemonstrasikan secara komprehensif:
 * 1. Solusi Eksplisit Studi Kasus Latihan Kelas Slide 17:
 *    - Kasus A: Method prosesPesanan() 120 baris -> Extract Method, Guard Clauses, SRP.
 *    - Kasus B: Class Produk 35 method -> Extract Class (SRP).
 *    - Kasus C: Aturan validasi kode voucher berulang di 4 class -> Reusable Behavior.
 * 2. Tujuh (7) Teknik Refactoring Kunci (Before & After):
 *    a. Long Method -> Extract Method
 *    b. God Class -> Extract Class (SRP)
 *    c. Duplicated Code -> Extract Reusable Behavior
 *    d. Feature Envy -> Move Method
 *    e. Primitive Obsession -> Value Object (CustomerEmail, Money, ProductCode, VoucherCode)
 *    f. Long Parameter List -> Parameter Object (CheckoutRequest)
 *    g. Nested Conditionals -> Guard Clauses & Early Returns
 * 
 * Dosen Pengampu: Nirwana Haidar Hari, S.Pd., M.Kom.
 * Institusi: D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0) {
            String flag = args[0].trim().toLowerCase();
            if (flag.equals("--interactive") || flag.equals("-i")) {
                runInteractiveMenu();
                return;
            } else if (flag.equals("--test") || flag.equals("-t")) {
                TestRunner.main(args);
                return;
            }
        }

        runFullDemonstration();
    }

    public static void runFullDemonstration() {
        printHeader();

        // Demonstrasi Komparasi Before & After 7 Teknik Kunci
        demoSevenRefactoringTechniques();

        // Demonstrasi Pemecahan 3 Kasus Slide 17 secara detail
        demoKasusASlide17();
        demoKasusBSlide17();
        demoKasusCSlide17();

        printFooter();
    }

    private static void runInteractiveMenu() {
        printHeader();
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n==========================================================================================");
            System.out.println("              MENU NAVIGASI INTERAKTIF DEMO REFACTORING P14");
            System.out.println("==========================================================================================");
            System.out.println(" [1] Demonstrasi 7 Teknik Refactoring Kunci (Before vs After)");
            System.out.println(" [2] Studi Kasus A Slide 17 (Refactoring Method prosesPesanan 120 baris)");
            System.out.println(" [3] Studi Kasus B Slide 17 (Refactoring God Class Produk 35 method)");
            System.out.println(" [4] Studi Kasus C Slide 17 (Eliminasi Duplikasi Validasi Voucher di 4 class)");
            System.out.println(" [5] Eksekusi Seluruh Skenario (Full Walkthrough Demonstration)");
            System.out.println(" [6] Jalankan Automated Test Suite (TestRunner - 16 Skenario Uji 100% Hijau)");
            System.out.println(" [0] Keluar");
            System.out.print(" Pilih opsi menu [0-6]: ");

            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";
            System.out.println();

            switch (input) {
                case "1":
                    demoSevenRefactoringTechniques();
                    break;
                case "2":
                    demoKasusASlide17();
                    break;
                case "3":
                    demoKasusBSlide17();
                    break;
                case "4":
                    demoKasusCSlide17();
                    break;
                case "5":
                    runFullDemonstration();
                    break;
                case "6":
                    TestRunner.main(new String[]{});
                    break;
                case "0":
                    running = false;
                    System.out.println("Keluar dari program demonstrasi. Terima kasih.");
                    break;
                default:
                    System.out.println("[!] Pilihan tidak valid. Silakan masukkan angka 0 - 6.");
            }
        }
    }

    private static void printHeader() {
        System.out.println("==========================================================================================");
        System.out.println("   PRAKTIKUM P14: REFACTORING CODE & CLEAN OBJECT-ORIENTED DESIGN (OOD)");
        System.out.println("   Dosen Pengampu : Nirwana Haidar Hari, S.Pd., M.Kom.");
        System.out.println("   Institusi      : D3 Teknik Informatika - PENS PSDKU Sumenep");
        System.out.println("   Mahasiswa      : Arjuna Lanang Adiwarsana (NRP: 3125522010)");
        System.out.println("==========================================================================================\n");
    }

    /**
     * Demonstrasi Matriks 7 Teknik Refactoring Kunci (Before & After)
     */
    private static void demoSevenRefactoringTechniques() {
        System.out.println("==========================================================================================");
        System.out.println(">>> BAGIAN 1: DEMONSTRASI 7 TEKNIK REFACTORING KUNCI (BEFORE vs AFTER) <<<");
        System.out.println("==========================================================================================");

        // a & g. Nested Conditionals -> Guard Clauses & Long Method -> Extract Method
        System.out.println("\n[A & G] NESTED CONDITIONALS -> GUARD CLAUSES & LONG METHOD -> EXTRACT METHOD");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("BEFORE : 120 baris monolitik, 5 tingkat nested 'if' (Arrow Anti-Pattern).");
        System.out.println("AFTER  : Guard Clauses fail-fast di awal, diekstrak menjadi method terfokus (< 15 baris).");

        // b. God Class -> Extract Class (SRP)
        System.out.println("\n[B] GOD CLASS -> EXTRACT CLASS (SINGLE RESPONSIBILITY PRINCIPLE)");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("BEFORE : OldProdukGodClass memegang 35 method (Domain + Database SQL + Laporan + Notif).");
        System.out.println("AFTER  : Dipecah menjadi 4 kelas kohesif:");
        System.out.println("         1. Produk (Domain Entity)");
        System.out.println("         2. ProdukRepository (Persistensi / Database)");
        System.out.println("         3. ProdukReportService (Reporting / Analitik)");
        System.out.println("         4. StockNotificationService (Notifikasi / Alert)");

        // c. Duplicated Code -> Extract Reusable Behavior
        System.out.println("\n[C] DUPLICATED CODE -> EXTRACT REUSABLE BEHAVIOR (KASUS C)");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("BEFORE : Logika cek voucher diulang persis di Cart, Checkout, Payment, dan Admin.");
        System.out.println("AFTER  : Disatukan dalam class VoucherValidator & Value Object VoucherCode.");

        // d. Feature Envy -> Move Method
        System.out.println("\n[D] FEATURE ENVY -> MOVE METHOD");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("BEFORE : OldShippingCalculator terus menginspeksi getter milik Customer (tier, city, loyalty).");
        System.out.println("AFTER  : Logika dipindahkan ke Customer.calculateEffectiveLoyaltyDiscountRate() & isEligibleForFreeShipping().");
        Customer customerDemo = new Customer(
            "CUST-001",
            "Arjuna Lanang",
            new CustomerEmail("arjuna@pens.ac.id"),
            "Sumenep",
            Customer.Tier.GOLD,
            3
        );
        System.out.println("  -> Evaluasi Langsung via Customer:");
        System.out.println("     Customer        : " + customerDemo);
        System.out.println("     Effective Diskon: " + customerDemo.calculateEffectiveLoyaltyDiscountRate() + "%");
        System.out.println("     Gratis Ongkir   : " + customerDemo.isEligibleForFreeShipping(Money.of(250_000)));

        // e. Primitive Obsession -> Value Object
        System.out.println("\n[E] PRIMITIVE OBSESSION -> VALUE OBJECT");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("BEFORE : String mentah untuk email, double untuk uang, String sembarang untuk SKU & Voucher.");
        System.out.println("AFTER  : Value Objects immutable bervalidasi kuat (CustomerEmail, Money, ProductCode, VoucherCode).");
        CustomerEmail email = new CustomerEmail("wardilanang46@gmail.com");
        Money moneyA = Money.of(150_000);
        Money moneyB = Money.of(50_000);
        Money totalMoney = moneyA.add(moneyB);
        ProductCode pCode = new ProductCode("PRD-LAPTOP01");
        VoucherCode vCode = new VoucherCode("PENSJUARA");
        System.out.println("  -> Contoh Instansiasi Value Objects:");
        System.out.println("     CustomerEmail : " + email.getValue() + " (Domain: " + email.getDomain() + ")");
        System.out.println("     Money Operasi : " + moneyA + " + " + moneyB + " = " + totalMoney);
        System.out.println("     ProductCode   : " + pCode.getValue());
        System.out.println("     VoucherCode   : " + vCode.getCode() + " (Prefix: " + vCode.getPrefix() + ")");

        // f. Long Parameter List -> Parameter Object
        System.out.println("\n[F] LONG PARAMETER LIST -> PARAMETER OBJECT");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("BEFORE : method(name, email, phone, addr, postal, payType, voucher, sku, qty, note) -> 10 param!");
        System.out.println("AFTER  : method(CheckoutRequest request) -> Parameter Object bersih, aman, mudah di-evolusikan.");
        System.out.println("==========================================================================================\n");
    }

    /**
     * Demonstrasi Pemecahan Kasus A Slide 17
     */
    private static void demoKasusASlide17() {
        System.out.println("==========================================================================================");
        System.out.println(">>> LATIHAN KELAS SLIDE 17 - KASUS A: REFACTORING METHOD prosesPesanan() 120 BARIS <<<");
        System.out.println("==========================================================================================");

        // Inisialisasi dependensi terinjeksi (Clean Architecture)
        ProdukRepository produkRepo = new InMemoryProdukRepository();
        OrderRepository orderRepo = new InMemoryOrderRepository();
        TaxCalculator taxCalc = new TaxCalculator(11.0); // PPN 11%
        DiscountService discountService = new DiscountService();
        NotificationService notificationService = new EmailNotificationService();

        // Siapkan Master Produk di Katalog
        Produk laptop = new Produk(new ProductCode("PRD-LAPTOP01"), "ThinkPad T14 Gen 4", Money.of(15_000_000), 5, "Elektronik");
        Produk mouse = new Produk(new ProductCode("PRD-MOUSE01"), "Logitech MX Master 3S", Money.of(1_500_000), 12, "Aksesoris");
        produkRepo.save(laptop);
        produkRepo.save(mouse);

        OrderProcessor processor = new OrderProcessor(
            produkRepo,
            orderRepo,
            taxCalc,
            discountService,
            notificationService
        );

        // Buat Parameter Object CheckoutRequest
        List<OrderItem> cartItems = new ArrayList<>();
        cartItems.add(new OrderItem(laptop, 1));
        cartItems.add(new OrderItem(mouse, 2));

        CheckoutRequest request = new CheckoutRequest(
            "ORD-2026-PENS-001",
            "Arjuna Lanang Adiwarsana",
            new CustomerEmail("arjuna@pens.ac.id"),
            "Jl. Raya Lenteng No. 12, Sumenep, Madura",
            cartItems,
            new VoucherCode("PENSJUARA"), // Diskon 25% untuk PENS
            PaymentType.TRANSFER_BANK,
            "Mohon dipacking kayu dan bubble wrap tebal."
        );

        System.out.println("Eksekusi prosesPesanan() hasil refactoring...");
        Order processedOrder = processor.prosesPesanan(request);

        System.out.println("\n[HASIL REFACTORING KASUS A - ORDER BERHASIL DIPROSES]");
        System.out.println("Order ID        : " + processedOrder.getOrderId());
        System.out.println("Pelanggan       : " + processedOrder.getCustomerName() + " (" + processedOrder.getCustomerEmail() + ")");
        System.out.println("Alamat Kirim    : " + processedOrder.getShippingAddress());
        System.out.println("Metode Bayar    : " + processedOrder.getPaymentType().getDescription());
        System.out.println("Catatan         : " + processedOrder.getCustomerNote());
        System.out.println("Rincian Belanja :");
        for (OrderItem item : processedOrder.getItems()) {
            System.out.println("  - " + item);
        }
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("Subtotal Bruto  : " + processedOrder.getSubtotal());
        System.out.println("Diskon Voucher  : -" + processedOrder.getDiscountAmount() + " (Voucher: " + processedOrder.getVoucherCode() + ")");
        System.out.println("Pajak PPN (11%) : +" + processedOrder.getTaxAmount());
        System.out.println("Grand Total Net : " + processedOrder.getGrandTotal());
        System.out.println("Status Order    : " + processedOrder.getStatus());
        System.out.println("Sisa Stok Laptop: " + produkRepo.findByCode(laptop.getCode()).get().getStock() + " unit (berkurang dari 5)");
        System.out.println("Sisa Stok Mouse : " + produkRepo.findByCode(mouse.getCode()).get().getStock() + " unit (berkurang dari 12)");
        System.out.println("==========================================================================================\n");
    }

    /**
     * Demonstrasi Pemecahan Kasus B Slide 17
     */
    private static void demoKasusBSlide17() {
        System.out.println("==========================================================================================");
        System.out.println(">>> LATIHAN KELAS SLIDE 17 - KASUS B: REFACTORING GOD CLASS PRODUK (35 METHOD) <<<");
        System.out.println("==========================================================================================");

        // 1. Domain Entity Produk murni (SRP)
        Produk p1 = new Produk(new ProductCode("PRD-SSD01"), "Samsung NVMe 980 Pro 1TB", Money.of(1_850_000), 2, "Storage");
        Produk p2 = new Produk(new ProductCode("PRD-RAM01"), "Corsair Vengeance 32GB DDR5", Money.of(2_100_000), 1, "Memory");
        Produk p3 = new Produk(new ProductCode("PRD-MON01"), "LG UltraFine 27 Inch 4K", Money.of(6_500_000), 8, "Display");

        // 2. Class Hasil Ekstraksi: Repository
        ProdukRepository repo = new InMemoryProdukRepository();
        repo.save(p1);
        repo.save(p2);
        repo.save(p3);

        // 3. Class Hasil Ekstraksi: Reporting Service
        ProdukReportService reportService = new ProdukReportService();
        System.out.println("\n[OUTPUT DARI PRODUK REPORT SERVICE - SEPARATED RESPONSIBILITY]");
        System.out.println(reportService.generateCatalogSummary(repo.findAll()));

        // 4. Class Hasil Ekstraksi: Stock Notification Service
        NotificationService notif = new EmailNotificationService();
        StockNotificationService stockAlertService = new StockNotificationService(notif, "admin.gudang@pens.ac.id");

        System.out.println("[MONITORING STOK KRITIS (<= 2 UNIT) DARI STOCK NOTIFICATION SERVICE]");
        List<Produk> lowStock = repo.findLowStock(2);
        System.out.println(reportService.generateLowStockAlert(lowStock, 2));
        stockAlertService.checkAndAlertLowStock(lowStock, 2);
        System.out.println("==========================================================================================\n");
    }

    /**
     * Demonstrasi Pemecahan Kasus C Slide 17
     */
    private static void demoKasusCSlide17() {
        System.out.println("==========================================================================================");
        System.out.println(">>> LATIHAN KELAS SLIDE 17 - KASUS C: VALIDASI VOUCHER BERULANG DI 4 CLASS <<<");
        System.out.println("==========================================================================================");

        System.out.println("Pengujian Reusable Validator (VoucherValidator & VoucherCode):");
        Money belanja = Money.of(500_000);
        String[] testVouchers = {"PENSJUARA", "HEMAT20", "DISKON10", "PROMO50", "KODE_PALSU"};

        for (String raw : testVouchers) {
            try {
                VoucherCode vc = new VoucherCode(raw);
                Money diskon = VoucherValidator.validateAndCalculateDiscount(vc, belanja);
                VoucherValidator.VoucherRule rule = VoucherValidator.getVoucherRule(vc);
                System.out.printf("  [VALID] Voucher: %-10s | Keterangan: %-30s | Diskon: %s\n",
                        vc.getCode(), rule.getDescription(), diskon);
            } catch (IllegalArgumentException e) {
                System.out.printf("  [INVALID] Kode: %-10s | Alasan: %s\n", raw, e.getMessage());
            }
        }

        System.out.println("\nKesimpulan Kasus C:");
        System.out.println("Seluruh logika validasi regex, kuota, masa aktif, dan rate diskon kini terpusat");
        System.out.println("100% pada VoucherValidator & VoucherCode. Duplikasi kode di 4 class berhasil dieliminasi.");
        System.out.println("==========================================================================================");
    }

    private static void printFooter() {
        System.out.println("\nRefactoring & Clean OOD Selesai & Berhasil.");
        System.out.println("Seluruh code smell telah dibersihkan sesuai prinsip-prinsip Clean Code & SOLID.");
        System.out.println("Arjuna Lanang Adiwarsana (3125522010) - PENS PSDKU Sumenep\n");
    }
}
