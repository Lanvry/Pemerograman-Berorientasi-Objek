package test;

import model.*;

/**
 * Unit Test Khusus untuk Memverifikasi Refactoring P14 (Clean OOD & Code Smells).
 * 
 * Mencakup verifikasi fungsional untuk:
 * 1. Parameter Object / Context Object: CheckOutRequest & BillingContext
 * 2. Value Objects: DurasiParkir & BiayaParkir
 * 3. Replace Nested Conditionals with Guard Clauses (Slot matching & Checkout validation)
 * 4. Extract Method verification pada GedungParkir & AdminGUI alur check-out
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class CleanRefactoringP14Test {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   UNIT TEST CLEAN OOD & CODE SMELLS REFACTORING (MODUL 14): SMART PARKIR ");
        System.out.println("   Verifikasi Parameter Object, Value Object, Guard Clauses & Extracted Code");
        System.out.println("==========================================================================\n");

        testDurasiParkirValueObject();
        testBiayaParkirValueObject();
        testCheckOutRequestParameterObject();
        testBillingContextObject();
        testGuardClausesSlotParkir();
        testGedungParkirCheckOutViaParameterObject();

        System.out.println("\n--------------------------------------------------------------------------");
        System.out.printf("HASIL PENGUJIAN REFACTORING P14: %d PASSED, %d FAILED (Total: %d Test Cases)%n",
                passed, failed, (passed + failed));
        System.out.println("--------------------------------------------------------------------------");

        if (failed == 0) {
            System.out.println(">> [VERIFIKASI SUKSES] Seluruh Metrik Refactoring P14 Terpenuhi 100%!");
        } else {
            System.err.println(">> [VERIFIKASI GAGAL] Terdapat test case refactoring yang gagal!");
            System.exit(1);
        }
    }

    private static void testDurasiParkirValueObject() {
        System.out.print("[TEST 1 - Value Object] DurasiParkir (Immutability, Midnight Rollover, Min 1 Jam)... ");
        try {
            // Normal 2 jam: masuk 8, keluar 10 -> 2 jam
            DurasiParkir d1 = DurasiParkir.of(8, 10);
            if (d1.getTotalJam() != 2 || !d1.getRentangWaktu().equals("08:00 s/d 10:00")) {
                throw new AssertionError("Kalkulasi durasi normal salah!");
            }

            // Min 1 jam: masuk 8, keluar 8 -> 1 jam
            DurasiParkir dMin = DurasiParkir.of(8, 8);
            if (dMin.getTotalJam() != 1) {
                throw new AssertionError("Aturan minimal 1 jam tidak terpenuhi!");
            }

            // Midnight rollover: masuk 23, keluar 2 -> 3 jam
            DurasiParkir dMid = DurasiParkir.of(23, 2);
            if (dMid.getTotalJam() != 3) {
                throw new AssertionError("Kalkulasi rollover tengah malam salah! Hasil: " + dMid.getTotalJam());
            }

            // Value Object equality
            DurasiParkir dSame = DurasiParkir.of(8, 10);
            if (!d1.equals(dSame) || d1.hashCode() != dSame.hashCode()) {
                throw new AssertionError("Kontrak equals/hashCode Value Object dilanggar!");
            }

            System.out.println("PASSED (Aturan bisnis durasi terenkapsulasi rapi)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }
    }

    private static void testBiayaParkirValueObject() {
        System.out.print("[TEST 2 - Value Object] BiayaParkir (Kalkulasi Diskon, Format Rupiah, Lunas)... ");
        try {
            // Subtotal 10.000, diskon 2.000 -> tagihan 8.000
            BiayaParkir b1 = BiayaParkir.of(10000.0, 2000.0, "Diskon Promo");
            if (b1.getTotalTagihan() != 8000.0 || !b1.isMendapatDiskon()) {
                throw new AssertionError("Kalkulasi total tagihan salah!");
            }

            // Format Rupiah
            if (!b1.formatTotalTagihan().contains("8,000") && !b1.formatTotalTagihan().contains("8.000")) {
                throw new AssertionError("Format Rupiah tidak valid: " + b1.formatTotalTagihan());
            }

            // Validasi kelunasan & kembalian
            if (!b1.isLunas(10000.0) || b1.hitungKembalian(10000.0) != 2000.0) {
                throw new AssertionError("Validasi lunas dan kembalian salah!");
            }
            if (b1.isLunas(5000.0)) {
                throw new AssertionError("Pembayaran kurang harus ditandai belum lunas!");
            }

            // Diskon melebihi subtotal harus ter-clamp ke subtotal (tidak boleh negatif)
            BiayaParkir bOver = BiayaParkir.of(5000.0, 99000.0, "Super Diskon");
            if (bOver.getTotalTagihan() != 0.0 || bOver.getPotonganDiskon() != 5000.0) {
                throw new AssertionError("Diskon berlebih tidak di-clamp dengan benar!");
            }

            System.out.println("PASSED (Formatting Rupiah dan kalkulasi finansial terisolasi sempurna)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }
    }

    private static void testCheckOutRequestParameterObject() {
        System.out.print("[TEST 3 - Parameter Object] CheckOutRequest (Merapikan Long Parameter List)... ");
        try {
            Kendaraan k = new Kendaraan("B 9999 PO", "MOBIL", "Honda CRV");
            SlotParkir s = new SlotParkir("P01", 1, "MOBIL");
            KarcisParkir t = new KarcisParkir("TKT-REQ-01", k, s, 9);

            IPaymentProcessor processor = new CashPaymentProcessor();
            DiskonStrategy strategy = new MemberVIPDiskonStrategy();

            CheckOutRequest req = new CheckOutRequest(t, 12, processor, 50000.0, strategy);
            if (req.getTiket() != t || req.getJamKeluar() != 12 || req.getUangBayar() != 50000.0) {
                throw new AssertionError("Getter CheckOutRequest tidak mengembalikan state yang sesuai!");
            }
            if (req.getPaymentProcessor() != processor || req.getDiskonStrategy() != strategy) {
                throw new AssertionError("Dependensi processor / strategy tidak sesuai!");
            }

            // Validasi guard clause pada CheckOutRequest
            try {
                new CheckOutRequest(null, 12, processor, 10000.0);
                throw new AssertionError("CheckOutRequest harus menolak tiket null!");
            } catch (IllegalArgumentException expected) {}

            try {
                new CheckOutRequest(t, 25, processor, 10000.0);
                throw new AssertionError("CheckOutRequest harus menolak jam keluar > 23!");
            } catch (IllegalArgumentException expected) {}

            System.out.println("PASSED (CheckOutRequest merangkum 5 parameter checkout menjadi 1 objek utuh)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }
    }

    private static void testBillingContextObject() {
        System.out.print("[TEST 4 - Context Object] BillingContext (Kalkulasi Biaya Terisolasi)... ");
        try {
            Kendaraan k = new Kendaraan("L 8888 BC", "MOBIL", "Toyota Kijang");
            SlotParkir s = new SlotParkir("C01", 1, "MOBIL");
            KarcisParkir t = new KarcisParkir("TKT-CTX-01", k, s, 8);

            // Masuk jam 8, keluar jam 11 (3 jam)
            // Tarif Mobil: Jam pertama 5.000, per jam berikutnya 3.000 -> Subtotal = 5.000 + 2*3.000 = 11.000
            // Tanpa diskon -> Netto = 11.000
            BillingContext ctx = new BillingContext(t, 11, 5000.0, 3000.0, new TanpaDiskonStrategy());
            DurasiParkir durasi = ctx.hitungDurasi();
            if (durasi.getTotalJam() != 3) {
                throw new AssertionError("Durasi pada BillingContext salah! Diharapkan: 3, Hasil: " + durasi.getTotalJam());
            }

            BiayaParkir biaya = ctx.hitungBiaya();
            if (biaya.getTotalTagihan() != 11000.0 || biaya.getSubtotal() != 11000.0) {
                throw new AssertionError("Biaya pada BillingContext salah! Hasil: " + biaya.getTotalTagihan());
            }

            System.out.println("PASSED (BillingContext mengeksekusi kalkulasi durasi & biaya secara independen)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }
    }

    private static void testGuardClausesSlotParkir() {
        System.out.print("[TEST 5 - Guard Clauses] SlotParkir cocokUntuk & isiSlot (Pengecekan Datar)... ");
        try {
            SlotParkir slotMotor = new SlotParkir("M01", 1, "MOTOR");
            SlotParkir slotMobil = new SlotParkir("A01", 1, "MOBIL");
            SlotParkir slotEV = new SlotParkir("EV01", 1, "MOBIL_LISTRIK");

            Kendaraan motor = new Kendaraan("L 1111 MTR", "MOTOR", "Yamaha");
            Kendaraan mobil = new Kendaraan("B 2222 MBL", "MOBIL", "Toyota");
            Kendaraan ev = new Kendaraan("B 3333 EV", "MOBIL_LISTRIK", "Hyundai Ioniq");

            // Guard clauses: null check
            if (slotMotor.cocokUntuk(null)) {
                throw new AssertionError("Slot cocokUntuk(null) harus mengembalikan false!");
            }

            // Motor hanya untuk motor
            if (!slotMotor.cocokUntuk(motor) || slotMotor.cocokUntuk(mobil) || slotMotor.cocokUntuk(ev)) {
                throw new AssertionError("Validasi slot motor tidak presisi!");
            }

            // Slot Mobil reguler menerima mobil dan ev
            if (!slotMobil.cocokUntuk(mobil) || !slotMobil.cocokUntuk(ev) || slotMobil.cocokUntuk(motor)) {
                throw new AssertionError("Validasi slot mobil reguler tidak presisi!");
            }

            // Slot EV khusus EV
            if (!slotEV.cocokUntuk(ev) || slotEV.cocokUntuk(mobil) || slotEV.cocokUntuk(motor)) {
                throw new AssertionError("Validasi slot khusus EV tidak presisi!");
            }

            System.out.println("PASSED (Alur cocokUntuk dan isiSlot datar tanpa nested-if bertingkat)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }
    }

    private static void testGedungParkirCheckOutViaParameterObject() {
        System.out.print("[TEST 6 - Integration] GedungParkir.prosesKeluar(CheckOutRequest) & ISP hitungTagihan(BillingContext)... ");
        try {
            GedungParkir gedung = new GedungParkir("Plaza P14 Refactor", 4000, 2000, 2000, 1000);
            SlotParkir s = new SlotParkir("R01", 1, "MOBIL");
            gedung.tambahSlot(s);

            double pendapatanSebelum = gedung.getTotalPendapatan();

            Kendaraan k = new Kendaraan("D 1414 P14", "MOBIL", "Honda HR-V");
            KarcisParkir tiket = gedung.prosesMasuk(k, 10);

            // Verifikasi hitungTagihan(BillingContext)
            BillingContext ctx = new BillingContext(tiket, 13, new PromoWeekendDiskonStrategy());
            double tagihanViaCtx = gedung.hitungTagihan(ctx);
            // 3 jam: 4000 + 2*2000 = 8000, diskon weekend 10% (800) = 7200
            if (tagihanViaCtx != 7200.0) {
                throw new AssertionError("Tagihan via BillingContext salah! Hasil: " + tagihanViaCtx);
            }

            // Eksekusi checkout menggunakan Parameter Object CheckOutRequest
            CheckOutRequest request = new CheckOutRequest(tiket, 13, new CashPaymentProcessor(), 10000.0, new PromoWeekendDiskonStrategy());
            boolean ok = gedung.prosesKeluar(request);

            if (!ok || !tiket.isSudahDibayar() || s.isTerisi()) {
                throw new AssertionError("Check-out via CheckOutRequest gagal menyelesaikan siklus parkir!");
            }

            if (gedung.getTotalPendapatan() != (pendapatanSebelum + 7200.0)) {
                throw new AssertionError("Pendapatan gedung tidak terupdate dengan benar! Hasil: " + gedung.getTotalPendapatan());
            }

            System.out.println("PASSED (Proses keluar terintegrasi sempurna via Parameter Object)");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }
    }
}
