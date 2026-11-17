package test;

import model.*;
import java.util.List;

/**
 * Unit & Integration Test Modul P15: Object Integration & Robustness Audit
 * 
 * Memverifikasi:
 * 1. Interaksi terintegrasi antara GedungParkir, KarcisParkir, Kendaraan, SlotParkir,
 *    DiskonStrategy, IPaymentProcessor, dan IKendaraanRepository (Bebas Circular Dependency).
 * 2. Robustness & Error Recovery (Atomic State Preservation saat pembayaran gagal).
 * 3. Boundary & Failure State Handling:
 *    - Re-checkout prevention (kendaraan sudah keluar tidak bisa dicheckout ulang).
 *    - Kapasitas penuh dengan pesan kategori informatif.
 *    - Penolakan jam masuk/keluar di luar 0-23 dan durasi negatif.
 *    - Penolakan nominal pembayaran negatif.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010) - Agent 8 Integrator
 */
public class ObjectIntegrationTest {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   AUDIT INTEGRASI OBJEK & ROBUSTNESS P15: SMART PARKIR PENS             ");
        System.out.println("==========================================================================");

        int passed = 0;
        int failed = 0;

        // TEST 1: Seamless Object Interaction & Clean Inversion of Control
        try {
            System.out.print("[TEST 1 - INTEGRASI OBJEK] Interaksi mulus 7 komponen inti P15... ");
            IKendaraanRepository repo = new InMemoryKendaraanRepository();
            GedungParkir gedung = new GedungParkir("Mall PENS Integrasi", 5000, 3000, 2000, 1000, repo);

            SlotParkir slotMobil = new SlotParkir("A-01", 1, "MOBIL");
            SlotParkir slotEV = new SlotParkir("EV-01", 1, "MOBIL_LISTRIK");
            gedung.tambahSlot(slotMobil);
            gedung.tambahSlot(slotEV);

            Kendaraan mobil = new Kendaraan("L 1111 INT", "MOBIL", "Innova");
            Kendaraan ev = new Kendaraan("B 2222 EV", "MOBIL_LISTRIK", "Hyundai Ioniq");

            gedung.tambahKendaraan(mobil);
            gedung.tambahKendaraan(ev);

            // Verifikasi IKendaraanRepository
            boolean repoContains = (repo.cari("L 1111 INT") != null && repo.cari("B 2222 EV") != null);

            // Check-in
            KarcisParkir tMobil = gedung.prosesMasuk(mobil, 8);
            KarcisParkir tEV = gedung.prosesMasuk(ev, 9);

            // Verifikasi relasi objek
            boolean relasiValid = (tMobil.getSlot() == slotMobil) &&
                                  (slotMobil.getKendaraanParkir() == mobil) &&
                                  (tEV.getSlot() == slotEV) &&
                                  (slotEV.getKendaraanParkir() == ev);

            // Billing & Strategy
            DiskonStrategy vipStrategy = new MemberVIPDiskonStrategy();
            double tagihanMobil = gedung.hitungTagihan(tMobil, 11, vipStrategy); // durasi 3 jam -> (5000 + 2*3000) = 11000, diskon 30% = 7700
            
            // Payment Processor
            IPaymentProcessor emoneyProc = new EMoneyPaymentProcessor();
            boolean checkoutMobil = gedung.prosesKeluar(tMobil, 11, emoneyProc, 10000, vipStrategy);

            if (repoContains && relasiValid && checkoutMobil && (tagihanMobil == 7700.0) && !slotMobil.isTerisi()) {
                System.out.println("PASSED (GedungParkir, Karcis, Kendaraan, Slot, Diskon, Payment & Repo terhubung mulus)");
                passed++;
            } else {
                System.out.println("FAILED (Inkonsistensi relasi antar objek)");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // TEST 2: Robustness & Error Recovery (Atomic State Preservation on Payment Failure)
        try {
            System.out.print("[TEST 2 - ATOMIC STATE] State slot, tiket, & kas tetap utuh saat bayar ditolak... ");
            GedungParkir gedung = new GedungParkir("Mall PENS Robustness", 5000, 3000, 2000, 1000, new InMemoryKendaraanRepository());
            SlotParkir slot = new SlotParkir("B-01", 1, "MOBIL");
            gedung.tambahSlot(slot);

            Kendaraan k = new Kendaraan("B 3333 ATOM", "MOBIL", "Civic");
            KarcisParkir tiket = gedung.prosesMasuk(k, 10);

            double saldoAwal = gedung.getTotalPendapatan();

            // Checkout jam 12 (durasi 2 jam: 5000 + 3000 = Rp 8000), tapi bayar Rp 4000 (Kurang)
            try {
                gedung.prosesKeluar(tiket, 12, new CashPaymentProcessor(), 4000.0, new TanpaDiskonStrategy());
                System.out.println("FAILED (PembayaranKurangException tidak dilempar)");
                failed++;
            } catch (PembayaranKurangException ex) {
                // Verifikasi integritas state (Atomic state)
                boolean slotMasihTerisi = slot.isTerisi();
                boolean kendaraanMasihSama = k.equals(slot.getKendaraanParkir());
                boolean tiketBelumLunas = !tiket.isSudahDibayar();
                boolean saldoTidakBerubah = (gedung.getTotalPendapatan() == saldoAwal);

                if (slotMasihTerisi && kendaraanMasihSama && tiketBelumLunas && saldoTidakBerubah) {
                    // Coba Error Recovery: Bayar dengan uang yang cukup
                    boolean recoveryOk = gedung.prosesKeluar(tiket, 12, new CashPaymentProcessor(), 10000.0, new TanpaDiskonStrategy());
                    boolean slotKosongPascaRecovery = !slot.isTerisi();
                    boolean tiketLunasPascaRecovery = tiket.isSudahDibayar();
                    boolean saldoBertambah = (gedung.getTotalPendapatan() == saldoAwal + 8000.0);

                    if (recoveryOk && slotKosongPascaRecovery && tiketLunasPascaRecovery && saldoBertambah) {
                        System.out.println("PASSED (Atomic State terbukti terjaga & Error Recovery sukses dilunasi)");
                        passed++;
                    } else {
                        System.out.println("FAILED (Error recovery gagal menyelesaikan transaksi)");
                        failed++;
                    }
                } else {
                    System.out.println("FAILED (State rusak: slot, tiket, atau saldo berubah sebelum transaksi sah)");
                    failed++;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // TEST 3: Re-checkout Prevention (Kendaraan sudah keluar tidak bisa dicheckout ulang)
        try {
            System.out.print("[TEST 3 - PREVENSI RE-CHECKOUT] Penolakan checkout ulang tiket yang sudah lunas... ");
            GedungParkir gedung = new GedungParkir("Mall PENS ReCheck", 4000, 2000, 2000, 1000, new InMemoryKendaraanRepository());
            SlotParkir slot = new SlotParkir("C-01", 1, "MOBIL");
            gedung.tambahSlot(slot);

            Kendaraan k = new Kendaraan("D 4444 RE", "MOBIL", "Fortuner");
            KarcisParkir tiket = gedung.prosesMasuk(k, 8);

            // Checkout pertama (sukses)
            gedung.prosesKeluar(tiket, 9, "CASH", 5000.0);

            // Percobaan checkout kedua dengan tiket yang sama (harus ditolak)
            try {
                gedung.prosesKeluar(tiket, 10, "CASH", 5000.0);
                System.out.println("FAILED (Tiket sudah lunas dapat dicheckout ulang)");
                failed++;
            } catch (TiketTidakDitemukanException ex) {
                // Percobaan checkout via plat yang sudah keluar
                try {
                    gedung.prosesKeluarDenganPlat("D 4444 RE", 10, "CASH", 5000.0);
                    System.out.println("FAILED (Plat yang sudah keluar masih bisa dicheckout)");
                    failed++;
                } catch (TiketTidakDitemukanException exPlat) {
                    System.out.println("PASSED (Tiket/plat sudah keluar ditolak via TiketTidakDitemukanException)");
                    passed++;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // TEST 4: Boundary & Validation (Jam Negatif, Jam > 23, Uang Negatif)
        try {
            System.out.print("[TEST 4 - BOUNDARY VALIDATION] Penolakan jam tidak valid (<0 atau >23) & uang negatif... ");
            GedungParkir gedung = new GedungParkir("Mall PENS Boundary", 5000, 3000, 2000, 1000, new InMemoryKendaraanRepository());
            SlotParkir slot = new SlotParkir("D-01", 1, "MOBIL");
            gedung.tambahSlot(slot);

            Kendaraan k = new Kendaraan("N 5555 BD", "MOBIL", "Yaris");

            boolean jamMasukNegatifDitolak = false;
            try {
                gedung.prosesMasuk(k, -1);
            } catch (IllegalArgumentException ex) {
                jamMasukNegatifDitolak = true;
            }

            boolean jamMasukLebihDitolak = false;
            try {
                gedung.prosesMasuk(k, 25);
            } catch (IllegalArgumentException ex) {
                jamMasukLebihDitolak = true;
            }

            KarcisParkir tiket = gedung.prosesMasuk(k, 8);

            boolean jamKeluarNegatifDitolak = false;
            try {
                gedung.prosesKeluar(tiket, -2, "CASH", 10000.0);
            } catch (IllegalArgumentException ex) {
                jamKeluarNegatifDitolak = true;
            }

            boolean uangNegatifDitolak = false;
            try {
                gedung.prosesKeluar(tiket, 10, "CASH", -5000.0);
            } catch (IllegalArgumentException ex) {
                uangNegatifDitolak = true;
            }

            // Durasi negatif pada strategi diskon
            boolean durasiNegatifStrategyDitolak = false;
            try {
                new MemberVIPDiskonStrategy().hitungDiskon(k, 10000.0, -1);
            } catch (IllegalArgumentException ex) {
                durasiNegatifStrategyDitolak = true;
            }

            if (jamMasukNegatifDitolak && jamMasukLebihDitolak && jamKeluarNegatifDitolak && uangNegatifDitolak && durasiNegatifStrategyDitolak) {
                System.out.println("PASSED (Seluruh boundary jam negatif, jam >23, uang negatif, & durasi negatif tertolak aman)");
                passed++;
            } else {
                System.out.printf("FAILED (jMasukNeg: %b, jMasukLeb: %b, jKeluarNeg: %b, uangNeg: %b, durasiNeg: %b)%n",
                        jamMasukNegatifDitolak, jamMasukLebihDitolak, jamKeluarNegatifDitolak, uangNegatifDitolak, durasiNegatifStrategyDitolak);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // TEST 5: Kapasitas Penuh dengan Pesan Kategori Informatif
        try {
            System.out.print("[TEST 5 - KAPASITAS PENUH] Verifikasi pesan error kapasitas informatif... ");
            GedungParkir gedung = new GedungParkir("Mall PENS Full", 5000, 3000, 2000, 1000, new InMemoryKendaraanRepository());
            SlotParkir slotMotor = new SlotParkir("M-01", 1, "MOTOR");
            gedung.tambahSlot(slotMotor);

            Kendaraan motor1 = new Kendaraan("L 1111 MTR", "MOTOR", "Vario");
            Kendaraan motor2 = new Kendaraan("L 2222 MTR", "MOTOR", "NMAX");

            gedung.prosesMasuk(motor1, 8);

            try {
                gedung.prosesMasuk(motor2, 9);
                System.out.println("FAILED (KapasitasPenuhException tidak dilempar saat slot motor penuh)");
                failed++;
            } catch (KapasitasPenuhException ex) {
                boolean kategoriCocok = "MOTOR".equalsIgnoreCase(ex.getTipeKendaraan());
                boolean pesanInformatif = ex.getMessage().contains("MOTOR") && ex.getMessage().contains("PENUH");

                if (kategoriCocok && pesanInformatif) {
                    System.out.println("PASSED (Pesan informatif: '" + ex.getMessage() + "')");
                    passed++;
                } else {
                    System.out.println("FAILED (Format pesan tidak informatif: " + ex.getMessage() + ")");
                    failed++;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("\n--------------------------------------------------------------------------");
        System.out.printf("HASIL AUDIT P15: %d PASSED, %d FAILED (Total: %d Test Cases)%n", passed, failed, (passed + failed));
        System.out.println("--------------------------------------------------------------------------\n");

        if (failed == 0) {
            System.out.println(">> [VERIFIKASI SUKSES] Integrasi Objek, Robustness & Exception Handling P15 Terbukti Sempurna!");
        } else {
            System.err.println(">> [VERIFIKASI GAGAL] Terdapat kegagalan pada audit integrasi P15!");
            System.exit(1);
        }
    }
}
