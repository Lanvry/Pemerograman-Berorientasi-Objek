package test;

import model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Unit Test Komprehensif Arsitektur SOLID (Pertemuan 13)
 * Memverifikasi penerapan nyata 5 prinsip SOLID pada Smart Parking System:
 * 1. Single Responsibility Principle (SRP)
 * 2. Open-Closed Principle (OCP)
 * 3. Liskov Substitution Principle (LSP)
 * 4. Interface Segregation Principle (ISP)
 * 5. Dependency Inversion Principle (DIP)
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class SolidArchitectureTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   UNIT TEST SOLID ARCHITECTURE (MODUL 13): SMART PARKIR PENS             ");
        System.out.println("   Verifikasi SRP, OCP, LSP, ISP, & DIP secara Fungsional                 ");
        System.out.println("==========================================================================\n");

        testSRP_SeparationOfConcerns();
        testOCP_DynamicDiskonAndPaymentExtension();
        testLSP_StrategyAndProcessorSubstitution();
        testISP_SegregatedClientInterfaces();
        testDIP_RepositoryDependencyInversion();

        System.out.println("\n--------------------------------------------------------------------------");
        System.out.printf("HASIL PENGUJIAN SOLID: %d PASSED, %d FAILED (Total: %d Test Cases)%n",
                passed, failed, (passed + failed));
        System.out.println("--------------------------------------------------------------------------");

        if (failed == 0) {
            System.out.println(">> [VERIFIKASI SUKSES] 5 Prinsip SOLID Terbukti 100% Berfungsi Sempurna!");
        } else {
            System.err.println(">> [VERIFIKASI GAGAL] Terdapat test case SOLID yang belum lolos!");
            System.exit(1);
        }
    }

    /**
     * [TEST 1] Single Responsibility Principle (SRP)
     * Memverifikasi pemisahan tugas modular antara kalkulasi diskon, pemrosesan pembayaran,
     * persistensi repositori, dan kontrol gerbang.
     */
    private static void testSRP_SeparationOfConcerns() {
        System.out.print("[TEST 1 - SRP] Pemisahan tanggung jawab kalkulasi, pembayaran, persistensi & gerbang... ");
        try {
            GedungParkir gedung = new GedungParkir("Mall PENS SRP", 5000, 3000, 2000, 1000);
            SlotParkir slot1 = new SlotParkir("A01", 1, "MOBIL");
            gedung.tambahSlot(slot1);

            Kendaraan mobil = new Kendaraan("B 1001 SRP", "MOBIL", "Honda Civic");
            KarcisParkir tiket = gedung.prosesMasuk(mobil, 8);

            // 1. Tanggung Jawab Diskon terisolasi pada DiskonStrategy
            DiskonStrategy vipStrategy = new MemberVIPDiskonStrategy();
            double diskon = vipStrategy.hitungDiskon(mobil, 11000, 3);
            if (diskon != 3300.0) {
                throw new AssertionError("Kalkulasi diskon VIP salah!");
            }

            // 2. Tanggung Jawab Pembayaran terisolasi pada IPaymentProcessor
            IPaymentProcessor cashProcessor = new CashPaymentProcessor();
            if (!cashProcessor.getNamaMetode().equals("CASH")) {
                throw new AssertionError("Identitas prosesor pembayaran salah!");
            }

            // 3. Tanggung Jawab Gerbang terisolasi pada kontrol status gerbang
            gedung.setStatusGerbang(false, "TERTUTUP");
            if (gedung.isGerbangTerbuka()) {
                throw new AssertionError("Integritas status palang gerbang tidak sesuai!");
            }

            // 4. Tanggung Jawab Persistensi terisolasi pada IKendaraanRepository
            IKendaraanRepository repo = gedung.getKendaraanRepository();
            if (repo == null) {
                throw new AssertionError("Gedung tidak mengelola abstraksi repositori!");
            }

            System.out.println("PASSED (Tiap komponen memiliki tanggung jawab tunggal yang terdefinisi rapi)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }
    }

    /**
     * [TEST 2] Open-Closed Principle (OCP)
     * Menambahkan DiskonStrategy baru dan IPaymentProcessor baru tanpa mengubah kode kelas inti GedungParkir.
     */
    private static void testOCP_DynamicDiskonAndPaymentExtension() {
        System.out.print("[TEST 2 - OCP] Ekstensi Diskon & Payment Processor baru tanpa modifikasi engine... ");
        try {
            GedungParkir gedung = new GedungParkir("Mall PENS OCP", 5000, 3000, 2000, 1000);
            SlotParkir slot = new SlotParkir("B01", 1, "MOBIL");
            gedung.tambahSlot(slot);

            Kendaraan k = new Kendaraan("L 2026 OCP", "MOBIL", "Toyota Camry");
            KarcisParkir tiket = gedung.prosesMasuk(k, 10);

            // 1. Ekstensi Diskon Baru: "Promo Liburan Spesial (Diskon 50%)" tanpa mengubah GedungParkir!
            DiskonStrategy promoLiburanStrategy = new DiskonStrategy() {
                @Override
                public double hitungDiskon(Kendaraan kendaraan, double tarifAwal, int durasi) {
                    return tarifAwal * 0.50; // Potongan 50%
                }

                @Override
                public String getNamaDiskon() {
                    return "Promo Liburan Spesial (Diskon 50%)";
                }

                @Override
                public String getDeskripsi() {
                    return "Diskon separuh harga masa liburan";
                }
            };

            // Hitung tagihan untuk 3 jam: subtotal = 5000 + (2 * 3000) = 11000, diskon 50% = 5500
            double tagihanFinal = gedung.hitungTagihan(tiket, 13, promoLiburanStrategy);
            if (tagihanFinal != 5500.0) {
                throw new AssertionError("Kalkulasi strategi diskon baru tidak sesuai! Diharapkan 5500, dapat: " + tagihanFinal);
            }

            // 2. Ekstensi Payment Gateway Baru: "ShopeePay / Crypto Gateway" tanpa mengubah alur check-out!
            IPaymentProcessor customGateway = new IPaymentProcessor() {
                @Override
                public boolean prosesPembayaran(KarcisParkir t, double uangBayar) throws PembayaranKurangException {
                    if (uangBayar < t.getTotalBiaya()) {
                        throw new PembayaranKurangException(t.getTotalBiaya(), uangBayar);
                    }
                    t.prosesPembayaran("SHOPEEPAY_WALLET", uangBayar);
                    return true;
                }

                @Override
                public String getNamaMetode() {
                    return "SHOPEEPAY_WALLET";
                }
            };

            boolean checkoutSukses = gedung.prosesKeluar(tiket, 13, customGateway, 6000.0, promoLiburanStrategy);
            if (!checkoutSukses || !tiket.isSudahDibayar() || !tiket.getMetodePembayaran().equals("SHOPEEPAY_WALLET")) {
                throw new AssertionError("Check-out dengan payment processor ekstensi gagal!");
            }

            System.out.println("PASSED (Strategi diskon 50% & ShopeePay processor berjalan sukses tanpa merubah class GedungParkir)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }
    }

    /**
     * [TEST 3] Liskov Substitution Principle (LSP)
     * Menguji substitutabilitas seluruh turunan DiskonStrategy dan IPaymentProcessor.
     * Tidak boleh ada subclass yang melanggar kontrak invariant atau melempar exception yang tak terduga.
     */
    private static void testLSP_StrategyAndProcessorSubstitution() {
        System.out.print("[TEST 3 - LSP] Substitutabilitas strategi diskon dan prosesor pembayaran seragam... ");
        try {
            Kendaraan evCar = new Kendaraan("B 8888 EV", "MOBIL_LISTRIK", "Hyundai Ioniq 5");
            Kendaraan nonEv = new Kendaraan("B 7777 REG", "MOBIL", "Toyota Avanza");
            double tarifAwal = 10000.0;
            int durasi = 2;

            DiskonStrategy[] strategies = new DiskonStrategy[]{
                new TanpaDiskonStrategy(),
                new EVIncentiveDiskonStrategy(),
                new MemberVIPDiskonStrategy(),
                new PromoWeekendDiskonStrategy()
            };

            // Invariant LSP Diskon: Besar potongan harus 0 <= diskon <= tarifAwal
            for (DiskonStrategy s : strategies) {
                double dEV = s.hitungDiskon(evCar, tarifAwal, durasi);
                double dNonEV = s.hitungDiskon(nonEv, tarifAwal, durasi);

                if (dEV < 0 || dEV > tarifAwal || dNonEV < 0 || dNonEV > tarifAwal) {
                    throw new AssertionError("Strategi " + s.getNamaDiskon() + " melanggar batas kontrak diskon [0, tarifAwal]!");
                }
                if (s.getNamaDiskon() == null || s.getDeskripsi() == null) {
                    throw new AssertionError("Strategi " + s.getClass().getSimpleName() + " mengembalikan null metadata!");
                }
            }

            // Invariant LSP Payment: Semua prosesor melempar PembayaranKurangException saat nominal kurang
            KarcisParkir tiketTest = new KarcisParkir("TKT-LSP-01", nonEv, new SlotParkir("S01", 1, "MOBIL"), 8);
            tiketTest.hitungTarif(10, 5000, 3000); // 2 jam -> 8000

            IPaymentProcessor[] processors = new IPaymentProcessor[]{
                new CashPaymentProcessor(),
                new EMoneyPaymentProcessor(),
                new QRISPaymentProcessor(),
                new VirtualAccountPaymentProcessor("BCA")
            };

            for (IPaymentProcessor p : processors) {
                // Skenario Uang Kurang: Kontrak seragam melempar PembayaranKurangException
                boolean lemparException = false;
                try {
                    p.prosesPembayaran(tiketTest, 3000); // kurang dari 8000
                } catch (PembayaranKurangException expected) {
                    lemparException = true;
                }
                if (!lemparException) {
                    throw new AssertionError("Prosesor " + p.getNamaMetode() + " tidak konsisten melempar PembayaranKurangException!");
                }
            }

            System.out.println("PASSED (Seluruh subtipe diskon & payment mematuhi kontrak invariant tanpa exception liar)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }
    }

    /**
     * [TEST 4] Interface Segregation Principle (ISP)
     * Menguji bahwa client gerbang, client kasir/billing, dan client armada hanya berinteraksi
     * dengan interface spesifik yang dibutuhkannya tanpa dibebani method yang tidak relevan.
     */
    private static void testISP_SegregatedClientInterfaces() {
        System.out.print("[TEST 4 - ISP] Segregasi kontrak ramping IParkingGateController, IParkingBilling, IArmadaManager... ");
        try {
            GedungParkir gedung = new GedungParkir("Mall PENS ISP", 5000, 3000, 2000, 1000);
            SlotParkir slotA = new SlotParkir("M01", 1, "MOTOR");
            gedung.tambahSlot(slotA);

            Kendaraan motor = new Kendaraan("L 1234 ISP", "MOTOR", "Yamaha NMAX");

            // 1. Client Gate: Hanya membutuhkan kontrol akses & buka palang
            IParkingGateController gateClient = gedung;
            KarcisParkir tiket = gateClient.prosesMasuk(motor, 14);
            if (tiket == null || !gateClient.isGerbangTerbuka()) {
                throw new AssertionError("Gate client gagal memproses masuk atau mendeteksi palang terbuka!");
            }
            gateClient.setStatusGerbang(false, "TERTUTUP");
            if (gateClient.isGerbangTerbuka()) {
                throw new AssertionError("Gate status tidak sinkron!");
            }

            // 2. Client Billing: Hanya membutuhkan perhitungan tagihan dan proses checkout
            IParkingBilling billingClient = gedung;
            double tagihan = billingClient.hitungTagihan(tiket, 16, new MemberVIPDiskonStrategy());
            // Motor 2 jam: 2000 + 1000 = 3000, diskon VIP 30% (900) = 2100
            if (tagihan != 2100.0) {
                throw new AssertionError("Billing client menghitung tagihan tidak tepat! Diharapkan 2100, hasil: " + tagihan);
            }
            boolean suksesBayar = billingClient.prosesKeluar(tiket, 16, new EMoneyPaymentProcessor(), 2500, new MemberVIPDiskonStrategy());
            if (!suksesBayar || billingClient.getTotalPendapatan() <= 0) {
                throw new AssertionError("Billing client gagal menyelesaikan checkout!");
            }

            // 3. Client Armada: Hanya membutuhkan manajemen master kendaraan terdaftar
            IArmadaManager armadaClient = gedung;
            Kendaraan kBaru = new Kendaraan("B 5555 ISP", "MOBIL", "Suzuki Ertiga");
            armadaClient.tambahKendaraan(kBaru);
            if (armadaClient.cariKendaraan("B 5555 ISP") == null || armadaClient.getTotalKendaraanTerdaftar() < 1) {
                throw new AssertionError("Armada client gagal mengelola koleksi kendaraan!");
            }

            System.out.println("PASSED (Tiap client hanya terikat pada interface terspesialisasi tanpa fat interface)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }
    }

    /**
     * [TEST 5] Dependency Inversion Principle (DIP)
     * Menguji bahwa GedungParkir bergantung pada abstraksi IKendaraanRepository.
     * Menggunakan InMemoryKendaraanRepository (Fake) via Constructor & Setter Injection tanpa I/O disk.
     */
    private static void testDIP_RepositoryDependencyInversion() {
        System.out.print("[TEST 5 - DIP] Dependency Inversion via IKendaraanRepository (Constructor & Setter Injection)... ");
        try {
            // 1. Constructor Injection: Menggunakan implementasi InMemoryKendaraanRepository (Mock/Fake)
            IKendaraanRepository inMemoryRepo = new InMemoryKendaraanRepository();
            GedungParkir gedungInMem = new GedungParkir("Mall DIP Test", 5000, 3000, 2000, 1000, inMemoryRepo);

            Kendaraan k1 = new Kendaraan("B 9001 DIP", "MOBIL", "Hyundai Stargazer");
            Kendaraan k2 = new Kendaraan("L 9002 DIP", "MOTOR", "Honda Beat");
            gedungInMem.tambahKendaraan(k1);
            gedungInMem.tambahKendaraan(k2);

            // Verifikasi data tersimpan di abstraksi in-memory repository
            if (inMemoryRepo.cari("B 9001 DIP") == null || inMemoryRepo.getSemua().size() != 2) {
                throw new AssertionError("Constructor Injection IKendaraanRepository gagal!");
            }

            // 2. Setter Injection: Beralih ke KendaraanFileRepository
            Path tempFile = Paths.get("target_test_p13_dip", "armada_dip.txt");
            if (Files.exists(tempFile)) Files.deleteIfExists(tempFile);

            KendaraanFileRepository fileRepo = new KendaraanFileRepository(tempFile);
            gedungInMem.setKendaraanRepository(fileRepo);

            if (!(gedungInMem.getKendaraanRepository() instanceof KendaraanFileRepository)) {
                throw new AssertionError("Setter Injection IKendaraanRepository gagal memperbarui referensi!");
            }

            // Simpan ke file menggunakan repositori baru
            gedungInMem.simpanArmadaKeFile(tempFile);
            if (!Files.exists(tempFile) || Files.size(tempFile) == 0) {
                throw new AssertionError("File persistensi melalui repository terinjeksi gagal tersimpan!");
            }

            // Bersihkan file testing
            Files.deleteIfExists(tempFile);
            if (tempFile.getParent() != null) Files.deleteIfExists(tempFile.getParent());

            System.out.println("PASSED (GedungParkir terbukti decoupled dari disk I/O dan mudah diuji dengan fake repository)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }
    }
}
