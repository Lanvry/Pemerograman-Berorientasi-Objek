/**
 * Demonstrasi Kasus Sebelum Refactoring (BEFORE REFACTORING)
 * 
 * File ini mengompilasi representasi kode lama yang mengandung 7 Code Smell utama
 * serta 3 Studi Kasus Latihan Kelas Slide 17 Materi P14:
 * 
 * 1. Kasus A (Slide 17) & Code Smell (a, f, g):
 *    - Long Method: method prosesPesananLama() sepanjang 120 baris.
 *    - Long Parameter List: menerima 10 parameter tipe primitif.
 *    - Nested Conditionals: Arrow Anti-Pattern bersarang 5 tingkat.
 * 
 * 2. Kasus B (Slide 17) & Code Smell (b):
 *    - God Class: OldProdukGodClass memuat 35 method yang mencampurkan entity,
 *      koneksi basis data SQL, ekspor laporan/formatting, dan notifikasi email.
 * 
 * 3. Kasus C (Slide 17) & Code Smell (c):
 *    - Duplicated Code: Aturan validasi kode voucher diulang persis di 4 class berbeda.
 * 
 * 4. Code Smell (d):
 *    - Feature Envy: OldShippingCalculator terus-menerus mengakses data internal Customer.
 * 
 * 5. Code Smell (e):
 *    - Primitive Obsession: String mentah untuk email, double untuk uang, String untuk SKU.
 * 
 * Referensi Materi P14: Nirwana Haidar Hari, S.Pd., M.Kom.
 * D3 Teknik Informatika - PENS PSDKU Sumenep
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class BeforeRefactoring {

    // =========================================================================
    // 1. KASUS A (Slide 17): Long Method, Long Parameter List, Nested Conditionals
    // =========================================================================
    public static class OldOrderProcessor {
        /**
         * SEBELUM REFACTORING:
         * Method monolitik 120 baris dengan 10 parameter (Long Parameter List),
         * 5 tingkat nested if (Arrow Anti-Pattern), dan pelanggaran SRP masif.
         */
        public boolean prosesPesananLama(
                String customerName,
                String customerEmail,
                String customerPhone,
                String shippingAddress,
                String postalCode,
                String paymentType,
                String voucherCode,
                String productCode,
                int quantity,
                String customerNote) {

            System.out.println("[BEFORE] Memulai prosesPesananLama() monolitik...");

            // Level 1 Nesting: Validasi Nama & Email
            if (customerName != null && !customerName.isEmpty()) {
                if (customerEmail != null && customerEmail.contains("@")) { // Primitive Obsession: validasi email seadanya
                    // Level 2 Nesting: Validasi Alamat
                    if (shippingAddress != null && !shippingAddress.isEmpty()) {
                        // Level 3 Nesting: Validasi Kuantitas & Kode Produk
                        if (quantity > 0 && productCode != null) {
                            // Level 4 Nesting: Validasi Pembayaran
                            if (paymentType != null && (paymentType.equals("TRANSFER") || paymentType.equals("COD"))) {
                                
                                // Simulasi lookup harga produk (Hardcoded di dalam method)
                                double unitPrice = 0.0;
                                int stock = 0;
                                if (productCode.equals("PRD-LAPTOP01")) {
                                    unitPrice = 12000000.0;
                                    stock = 5;
                                } else if (productCode.equals("PRD-MOUSE01")) {
                                    unitPrice = 150000.0;
                                    stock = 20;
                                } else {
                                    System.out.println("[BEFORE ERROR] Produk tidak ditemukan!");
                                    return false;
                                }

                                // Level 5 Nesting: Pengecekan stok
                                if (stock >= quantity) {
                                    double subtotal = unitPrice * quantity; // Primitive double (floating point hazard)
                                    System.out.println("[BEFORE] Subtotal: Rp " + subtotal);

                                    // Kalkulasi diskon voucher (Duplikasi logika Kasus C)
                                    double discount = 0.0;
                                    if (voucherCode != null && !voucherCode.isEmpty()) {
                                        if (voucherCode.startsWith("PENS") || voucherCode.startsWith("DISKON")) {
                                            if (subtotal >= 100000.0) {
                                                discount = subtotal * 0.10; // Diskon 10%
                                                System.out.println("[BEFORE] Diskon voucher diterapkan: Rp " + discount);
                                            } else {
                                                System.out.println("[BEFORE] Belanja belum memenuhi syarat voucher!");
                                            }
                                        }
                                    }

                                    // Hitung Pajak PPN 11% secara inline
                                    double taxable = subtotal - discount;
                                    double tax = taxable * 0.11;
                                    double grandTotal = taxable + tax;

                                    // Simulasi Simpan ke Database via string SQL inline
                                    String sql = "INSERT INTO orders (cust_name, email, total, status) VALUES ('"
                                            + customerName + "', '" + customerEmail + "', " + grandTotal + ", 'PAID')";
                                    System.out.println("[BEFORE DB] Menjalankan query SQL: " + sql);

                                    // Simulasi Pengiriman Email Notifikasi inline
                                    System.out.println("[BEFORE SMTP] Mengirim email konfirmasi ke: " + customerEmail);
                                    System.out.println("[BEFORE SMTP] 'Terima kasih " + customerName + ", pesanan total: Rp " + grandTotal + "'");

                                    System.out.println("[BEFORE SUCCESS] Pesanan selesai diproses.");
                                    return true;
                                } else {
                                    System.out.println("[BEFORE ERROR] Stok barang tidak mencukupi!");
                                    return false;
                                }
                            } else {
                                System.out.println("[BEFORE ERROR] Metode pembayaran tidak valid!");
                                return false;
                            }
                        } else {
                            System.out.println("[BEFORE ERROR] Kuantitas atau kode produk invalid!");
                            return false;
                        }
                    } else {
                        System.out.println("[BEFORE ERROR] Alamat pengiriman kosong!");
                        return false;
                    }
                } else {
                    System.out.println("[BEFORE ERROR] Format email pelanggan tidak valid!");
                    return false;
                }
            } else {
                System.out.println("[BEFORE ERROR] Nama pelanggan kosong!");
                return false;
            }
        }
    }

    // =========================================================================
    // 2. KASUS B (Slide 17): God Class OldProdukGodClass (35 Method Campuran)
    // =========================================================================
    public static class OldProdukGodClass {
        // State produk
        public String id;
        public String kode;
        public String nama;
        public double harga;
        public int stok;
        public String deskripsi;
        public String kategori;

        // Kelompok 1: Method Getter/Setter Domain (10 method)
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getKode() { return kode; }
        public void setKode(String kode) { this.kode = kode; }
        public String getNama() { return nama; }
        public void setNama(String nama) { this.nama = nama; }
        public double getHarga() { return harga; }
        public void setHarga(double harga) { this.harga = harga; }
        public int getStok() { return stok; }
        public void setStok(int stok) { this.stok = stok; }

        // Kelompok 2: Method Database CRUD (10 method - Pelanggaran SRP)
        public void connectDB() { System.out.println("[DB] Connecting to MySQL..."); }
        public void disconnectDB() { System.out.println("[DB] Disconnected."); }
        public void saveToDatabase() { System.out.println("[DB] INSERT INTO produk VALUES ('" + kode + "', '" + nama + "')"); }
        public void updateDatabase() { System.out.println("[DB] UPDATE produk SET harga = " + harga + " WHERE kode = '" + kode + "'"); }
        public void deleteFromDatabase() { System.out.println("[DB] DELETE FROM produk WHERE kode = '" + kode + "'"); }
        public void findByKode(String k) { System.out.println("[DB] SELECT * FROM produk WHERE kode = '" + k + "'"); }
        public void executeRawQuery(String sql) { System.out.println("[DB] Executing raw SQL: " + sql); }
        public void beginTransaction() { System.out.println("[DB] START TRANSACTION;"); }
        public void commitTransaction() { System.out.println("[DB] COMMIT;"); }
        public void rollbackTransaction() { System.out.println("[DB] ROLLBACK;"); }

        // Kelompok 3: Method Laporan & Formatting (8 method - Pelanggaran SRP)
        public void exportToPDF() { System.out.println("[REPORT] Generating PDF report for: " + nama); }
        public void exportToExcel() { System.out.println("[REPORT] Exporting to .xlsx spreadsheet..."); }
        public void exportToCSV() { System.out.println("[REPORT] CSV: " + kode + "," + nama + "," + harga); }
        public void printBarcodeLabel() { System.out.println("[PRINT] Printing Barcode for SKU: " + kode); }
        public void printPriceTag() { System.out.println("[PRINT] Price Tag: Rp " + harga); }
        public String formatCatalogRow() { return kode + " | " + nama + " | Rp " + harga; }
        public void generateMonthlySalesReport() { System.out.println("[REPORT] Monthly sales calculated."); }
        public void calculateDepreciation() { System.out.println("[FINANCE] Asset depreciation calculated."); }

        // Kelompok 4: Method Notifikasi & Alert (7 method - Pelanggaran SRP)
        public void sendLowStockAlertEmail(String adminEmail) { System.out.println("[NOTIF] Sending email to " + adminEmail + ": Stok tinggal " + stok); }
        public void sendRestockSMS(String vendorPhone) { System.out.println("[SMS] Restock request sent to " + vendorPhone); }
        public void sendSlackNotification() { System.out.println("[SLACK] Webhook trigger low stock."); }
        public void notifyMarketingNewProduct() { System.out.println("[NOTIF] Marketing blast triggered."); }
        public void sendTelegramAlert() { System.out.println("[TELEGRAM] Bot alert low stock."); }
        public void logActivity(String action) { System.out.println("[AUDIT LOG] Action: " + action); }
        public void pingHealthCheck() { System.out.println("[HEALTH] Product service alive."); }
    }

    // =========================================================================
    // 3. KASUS C (Slide 17): Duplicated Code Aturan Validasi Voucher di 4 Class
    // =========================================================================

    // Class 1: CartService menduplikasi validasi voucher
    public static class OldCartService {
        public boolean checkVoucher(String vCode, double total) {
            // DUPLIKASI KASUS C (Blok 1)
            if (vCode == null || vCode.trim().isEmpty()) return false;
            String clean = vCode.trim().toUpperCase();
            if (!(clean.startsWith("PENS") || clean.startsWith("DISKON") || clean.startsWith("HEMAT") || clean.startsWith("PROMO"))) {
                return false;
            }
            if (clean.length() < 5 || clean.length() > 15) return false;
            if (total < 50000.0) return false;
            return true;
        }
    }

    // Class 2: CheckoutService menduplikasi validasi voucher yang sama persis
    public static class OldCheckoutService {
        public double hitungDiskon(String vCode, double total) {
            // DUPLIKASI KASUS C (Blok 2)
            if (vCode == null || vCode.trim().isEmpty()) return 0.0;
            String clean = vCode.trim().toUpperCase();
            if (!(clean.startsWith("PENS") || clean.startsWith("DISKON") || clean.startsWith("HEMAT") || clean.startsWith("PROMO"))) {
                return 0.0;
            }
            if (clean.length() < 5 || clean.length() > 15) return 0.0;
            if (total < 50000.0) return 0.0;

            if (clean.startsWith("PENS")) return total * 0.25;
            if (clean.startsWith("HEMAT")) return total * 0.20;
            return total * 0.10;
        }
    }

    // Class 3: PaymentService menduplikasi validasi voucher lagi
    public static class OldPaymentService {
        public boolean verifyVoucherEligibility(String vCode, double amount) {
            // DUPLIKASI KASUS C (Blok 3)
            if (vCode == null || vCode.trim().isEmpty()) return false;
            String clean = vCode.trim().toUpperCase();
            if (!(clean.startsWith("PENS") || clean.startsWith("DISKON") || clean.startsWith("HEMAT") || clean.startsWith("PROMO"))) {
                return false;
            }
            if (clean.length() < 5 || clean.length() > 15) return false;
            if (amount < 50000.0) return false;
            return true;
        }
    }

    // Class 4: AdminAuditService menduplikasi validasi voucher untuk audit
    public static class OldAdminAuditService {
        public boolean isValidVoucherPattern(String vCode) {
            // DUPLIKASI KASUS C (Blok 4)
            if (vCode == null || vCode.trim().isEmpty()) return false;
            String clean = vCode.trim().toUpperCase();
            if (!(clean.startsWith("PENS") || clean.startsWith("DISKON") || clean.startsWith("HEMAT") || clean.startsWith("PROMO"))) {
                return false;
            }
            if (clean.length() < 5 || clean.length() > 15) return false;
            return true;
        }
    }

    // =========================================================================
    // 4. CODE SMELL (d): Feature Envy pada Kalkulasi Ongkir
    // =========================================================================
    public static class OldCustomerEntity {
        public String name;
        public String tier; // "PLATINUM", "GOLD", "REGULAR"
        public int loyaltyYears;
        public String city;

        public OldCustomerEntity(String name, String tier, int loyaltyYears, String city) {
            this.name = name;
            this.tier = tier;
            this.loyaltyYears = loyaltyYears;
            this.city = city;
        }
    }

    public static class OldShippingCalculator {
        /**
         * FEATURE ENVY:
         * Method calculateShippingFee terlalu banyak menjamah field dan logika internal
         * milik CustomerEntity daripada datanya sendiri. Solusi: Move Method ke Customer!
         */
        public double calculateShippingFee(OldCustomerEntity c, double weightKg) {
            double fee = 10000.0 * weightKg;

            // Envy: membaca langsung field-field Customer
            if (c.tier.equals("PLATINUM")) {
                fee = 0.0; // Gratis ongkir
            } else if (c.tier.equals("GOLD")) {
                if (c.loyaltyYears > 2) {
                    fee = fee * 0.5; // Diskon ongkir 50%
                }
            } else if (c.city.equalsIgnoreCase("Sumenep")) {
                fee = fee - 2000.0;
            }
            return Math.max(0.0, fee);
        }
    }
}
