import java.io.File;

/**
 * Driver Utama Demonstrasi Penerapan SOLID Principles.
 * Menyajikan visualisasi interaktif dan eksekusi skenario nyata dari S, O, L, I, dan D.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class Main {
    public static void main(String[] args) {
        printBanner();

        System.out.println("\n[1] SETUP DEPENDENCIES VIA CONSTRUCTOR INJECTION (DIP)");
        System.out.println("----------------------------------------------------------------------");
        String dataFilePath = "orders_data.txt";
        IOrderRepository fileRepo = new FileOrderRepository(dataFilePath);
        IReceiptPrinter printer = new ConsoleReceiptPrinter();
        EmailNotifier emailNotifier = new EmailNotifier();
        IDiscountStrategy initialDiscount = new PercentageDiscount(10.0); // 10% diskon

        CheckoutService checkoutService = new CheckoutService(fileRepo, initialDiscount, printer, emailNotifier);
        System.out.println("✓ CheckoutService berhasil dikonfigurasi melalui Constructor Injection.");
        System.out.println("  - Repository : FileOrderRepository (" + dataFilePath + ")");
        System.out.println("  - Diskon     : " + initialDiscount.getStrategyName());
        System.out.println("  - Printer    : ConsoleReceiptPrinter");
        System.out.println("  - Notifier   : EmailNotifier");

        System.out.println("\n[2] DEMONSTRASI S - SINGLE RESPONSIBILITY PRINCIPLE (SRP)");
        System.out.println("----------------------------------------------------------------------");
        System.out.println("Order murni merepresentasikan state pesanan tanpa logic file I/O atau kirim email.");
        Order order1 = new Order("ORD-202611-001", "Budi Santoso", "budi.santoso@pens.ac.id");
        order1.addItem(new Item("ITM-01", "Buku Pemrograman Java", 85_000, 2));
        order1.addItem(new Item("ITM-02", "Mouse Wireless Ergonomis", 150_000, 1));
        order1.addItem(new Item("ITM-03", "Flashdisk 64GB USB-C", 95_000, 1));
        System.out.printf("Order #%s dibuat dengan %d item, Subtotal: Rp %,.0f, Status: %s\n",
                order1.getOrderId(), order1.getItems().size(), order1.calculateSubtotal(), order1.getStatus());

        System.out.println("\n[3] DEMONSTRASI D & O - CHECKOUT WITH PERCENTAGE DISCOUNT (OCP & DIP)");
        System.out.println("----------------------------------------------------------------------");
        Receipt receipt1 = checkoutService.processCheckout(order1);
        System.out.println("✓ Transaksi berhasil diproses.");
        System.out.printf("  Net Total dibayar: Rp %,.0f (Hemat: Rp %,.0f)\n",
                receipt1.getNetTotal(), receipt1.getDiscountAmount());
        System.out.println("  Status Order sekarang: " + order1.getStatus());

        System.out.println("\n[4] DEMONSTRASI O - OPEN/CLOSED PRINCIPLE (OCP)");
        System.out.println("----------------------------------------------------------------------");
        System.out.println("Menambahkan diskon baru (VipDiscount) tanpa mengubah baris kode CheckoutService!");
        checkoutService.setDiscountStrategy(new VipDiscount());
        System.out.println("Strategi diskon diganti ke: " + checkoutService.getDiscountStrategy().getStrategyName());

        Order order2 = new Order("ORD-202611-002", "Siti Aminah (VIP)", "siti.vip@pens.ac.id");
        order2.addItem(new Item("ITM-10", "Mechanical Keyboard RGB", 650_000, 1));
        order2.addItem(new Item("ITM-11", "Keycaps PBT Custom", 180_000, 1));
        
        System.out.println("\nMemproses checkout pesanan VIP (Subtotal > Rp 500.000 memicu bonus 20%):");
        Receipt receipt2 = checkoutService.processCheckout(order2);
        System.out.printf("✓ Pesanan VIP selesai. Diskon didapatkan: Rp %,.0f (Total Net: Rp %,.0f)\n",
                receipt2.getDiscountAmount(), receipt2.getNetTotal());

        System.out.println("\n[5] DEMONSTRASI L - LISKOV SUBSTITUTION PRINCIPLE (LSP)");
        System.out.println("----------------------------------------------------------------------");
        System.out.println("Uji substitutability: FixedDiscount voucher Rp 100.000 pada belanja kecil Rp 45.000.");
        System.out.println("Ekspektasi LSP: Sistem tidak menghasilkan total minus dan tidak melempar Exception tak terduga.");
        checkoutService.setDiscountStrategy(new FixedDiscount(100_000.0));
        
        Order order3 = new Order("ORD-202611-003", "Andi Pratama", "andi@pens.ac.id");
        order3.addItem(new Item("ITM-20", "Notebook Spiral A5", 45_000, 1));
        Receipt receipt3 = checkoutService.processCheckout(order3);
        System.out.printf("Subtotal: Rp %,.0f | Voucher: Rp 100.000 | Diskon Diterapkan: Rp %,.0f | Net Total: Rp %,.0f\n",
                receipt3.getSubtotal(), receipt3.getDiscountAmount(), receipt3.getNetTotal());
        System.out.println("✓ Invariant LSP terpenuhi: Net Total >= 0, diskon dibatasi maksimal sebesar subtotal!");

        System.out.println("\n[6] DEMONSTRASI I - INTERFACE SEGREGATION PRINCIPLE (ISP)");
        System.out.println("----------------------------------------------------------------------");
        System.out.println("Memeriksa segregated interfaces Printable dan Auditable pada Order:");
        Printable printableDoc = order1;
        System.out.println(">> Client Printer hanya melihat Printable.getPrintContent():");
        System.out.println("   [Preview Baris Pertama]: " + printableDoc.getPrintContent().split("\n")[0]);

        Auditable auditableDoc = order1;
        System.out.println(">> Sub-sistem Audit hanya melihat Auditable interface:");
        System.out.println("   Audit ID       : " + auditableDoc.getAuditId());
        System.out.println("   Audit Timestamp: " + auditableDoc.getAuditTimestamp());
        System.out.println("   Audit Metadata : " + auditableDoc.getAuditMetadata());

        System.out.println("\n[7] VERIFIKASI PERSISTENSI FILE REPOSITORY (DIP & SRP)");
        System.out.println("----------------------------------------------------------------------");
        System.out.printf("Jumlah total transaksi tersimpan di %s: %d order\n", dataFilePath, fileRepo.count());
        Order found = fileRepo.findById("ORD-202611-001");
        if (found != null) {
            System.out.printf("✓ Sukses mencari kembali order via ID '%s' milik '%s' (%s)\n",
                    found.getOrderId(), found.getCustomerName(), found.getStatus());
        }

        System.out.println("\n======================================================================");
        System.out.println("         SEMUA PRINSIP SOLID BERHASIL DIDEMONSTRASIKAN SECARA ELEGAN! ");
        System.out.println("======================================================================\n");
    }

    private static void printBanner() {
        System.out.println("======================================================================");
        System.out.println("           PEMROGRAMAN BERORIENTASI OBYEK (PBO) - PERTEMUAN 13         ");
        System.out.println("               IMPLEMENTASI PENUH SOLID DESIGN PRINCIPLES             ");
        System.out.println("======================================================================");
        System.out.println("  Nama   : Arjuna Lanang Adiwarsana");
        System.out.println("  NRP    : 3125522010");
        System.out.println("  Kelas  : D3 PJJ Teknik Informatika - PENS PSDKU Sumenep");
        System.out.println("  Dosen  : Nirwana Haidar Hari, S.Pd., M.Kom.");
        System.out.println("======================================================================");
    }
}
