package test;

import model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * AUTOMATED INTEGRATION TEST SUITE: EVALUASI SISTEM SMART PARKIR (MODUL P15)
 * ============================================================================
 * Penulis      : Arjuna Lanang Adiwarsana
 * NRP          : 3125522010
 * Institusi    : PENS PSDKU Sumenep - D3 PJJ Teknik Informatika
 * Mata Kuliah  : Pemrograman Berorientasi Obyek (PBO)
 * 
 * Verifikasi 4 Skenario Evaluasi P15 pada Sistem Smart Parking:
 * 1. Skenario 1 (Normal)   : Siklus penuh kendaraan masuk (Mobil/Motor/EV), alokasi slot otomatis,
 *                            durasi parkir reguler, pemilihan strategi diskon dan metode bayar
 *                            (Cash/E-Money/QRIS), checkout sukses, pembebasan slot kembali kosong,
 *                            struk terbit, kas bertambah.
 * 2. Skenario 2 (Boundary) : Parkir tepat 1 jam (biaya minimum), parkir 24 jam (pembatasan max per hari),
 *                            alokasi kapasitas batas terakhir (slot A09 untuk motor, B12/C12 untuk mobil,
 *                            EV2 untuk EV), pembayaran dengan nominal pas (Rp 0 kembalian).
 * 3. Skenario 3 (Invalid)  : Plat nomor tidak sesuai regex (12345 / kosong), jam keluar < jam masuk,
 *                            checkout nomor tiket yang tidak pernah terdaftar.
 * 4. Skenario 4 (Failure & Recovery / Robustness):
 *                            Pembayaran kurang (PembayaranKurangException) -> verifikasi state konsistensi:
 *                            slot parkir tetap terisi, kendaraan tetap berstatus parkir, kas parkir tidak
 *                            bertambah liar, sistem pulih tanpa corrupt data.
 * ============================================================================
 */
public class SystemEvaluationP15Test {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     INTEGRATION TEST SUITE: EVALUASI SISTEM SMART PARKIR (MODUL P15)           ");
        System.out.println("     Otomasi Pengujian 4 Skenario: Normal, Boundary, Invalid, Failure & Recovery");
        System.out.println("================================================================================");
        System.out.println(" Pengembang : Arjuna Lanang Adiwarsana (NRP: 3125522010)");
        System.out.println(" Kelas      : D3 PJJ Teknik Informatika - PENS PSDKU Sumenep");
        System.out.println("================================================================================\n");

        runSkenario1_Normal();
        runSkenario2_Boundary();
        runSkenario3_Invalid();
        runSkenario4_FailureAndRecovery();

        System.out.println("\n================================================================================");
        System.out.printf(" HASIL PENGUJIAN EVALUASI P15: %d PASSED, %d FAILED (Total: %d Test Cases)%n",
                totalPassed, totalFailed, (totalPassed + totalFailed));
        System.out.println("================================================================================");

        if (totalFailed == 0) {
            System.out.println(">> [PASS] Seluruh Skenario Evaluasi P15 Terpenuhi 100% Sempurna!");
            System.out.println(">> [PASS] Integritas Alokasi Slot, Strategi Diskon, Gateway Pembayaran,");
            System.out.println(">>        Validasi Defensive, dan Robustness Atomic State Terbukti Tangguh.");
        } else {
            System.err.println(">> [FAIL] Terdapat test case pada Skenario Evaluasi P15 yang gagal!");
            System.exit(1);
        }
    }

    private static void pass(String kodeTest, String pesan) {
        totalPassed++;
        System.out.printf("   [PASS] [%s] %s%n", kodeTest, pesan);
    }

    private static void fail(String kodeTest, String pesan) {
        totalFailed++;
        System.err.printf("   [FAIL] [%s] %s%n", kodeTest, pesan);
    }

    // =========================================================================
    // SKENARIO 1: NORMAL (HAPPY PATH SIKLUS PENUH KENDARAAN MASUK S/D CHECKOUT)
    // =========================================================================
    private static void runSkenario1_Normal() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(" [SKENARIO 1: NORMAL] SIKLUS PENUH KENDARAAN MASUK, DISKON, BAYAR, KAS BERTAMBAH");
        System.out.println("--------------------------------------------------------------------------------");

        try {
            // 1. Inisialisasi Fasilitas Gedung Parkir Terisolasi
            IKendaraanRepository repo = new InMemoryKendaraanRepository();
            GedungParkir gedung = new GedungParkir("Mall Grand PENS Plaza", 5000, 3000, 2000, 1000, repo);

            SlotParkir slotMobil = new SlotParkir("B01", 1, "MOBIL");
            SlotParkir slotMotor = new SlotParkir("A01", 1, "MOTOR");
            SlotParkir slotEV    = new SlotParkir("EV1", 1, "MOBIL_LISTRIK");

            gedung.tambahSlot(slotMobil);
            gedung.tambahSlot(slotMotor);
            gedung.tambahSlot(slotEV);

            double saldoKasAwal = gedung.getTotalPendapatan();

            // 2. Check-In & Alokasi Slot Otomatis (Mobil, Motor, EV)
            Kendaraan mobil = new Kendaraan("B 1234 NRM", "MOBIL", "Toyota Avanza");
            Kendaraan motor = new Kendaraan("L 5678 MTR", "MOTOR", "Honda Vario");
            Kendaraan ev    = new Kendaraan("W 9999 ELE", "MOBIL_LISTRIK", "Hyundai Ioniq 5");

            KarcisParkir tMobil = gedung.prosesMasuk(mobil, 8);  // Masuk jam 08:00
            KarcisParkir tMotor = gedung.prosesMasuk(motor, 9);  // Masuk jam 09:00
            KarcisParkir tEV    = gedung.prosesMasuk(ev, 10);   // Masuk jam 10:00

            // Verifikasi Alokasi Slot Otomatis
            if (tMobil != null && "B01".equals(tMobil.getSlot().getIdSlot()) && slotMobil.isTerisi() &&
                tMotor != null && "A01".equals(tMotor.getSlot().getIdSlot()) && slotMotor.isTerisi() &&
                tEV != null && "EV1".equals(tEV.getSlot().getIdSlot()) && slotEV.isTerisi()) {
                pass("TEST-1.1", "Alokasi slot otomatis berhasil: Mobil -> B01, Motor -> A01, EV -> EV1");
            } else {
                fail("TEST-1.1", "Alokasi slot otomatis untuk salah satu jenis kendaraan tidak valid.");
            }

            // 3. Durasi Parkir Reguler & Pemilihan Strategi Diskon Dinamis (OCP/LSP)
            // Mobil: 3 Jam (08:00 -> 11:00), Promo Spesial Weekend (Diskon 10%)
            // Subtotal Mobil: 5000 + 2*3000 = 11000, Diskon 10% = 1100, Net = 9900
            DiskonStrategy promoWeekend = new PromoWeekendDiskonStrategy();
            double tagihanMobil = gedung.hitungTagihan(tMobil, 11, promoWeekend);

            // Motor: 2 Jam (09:00 -> 11:00), Member VIP (Diskon 30%)
            // Subtotal Motor: 2000 + 1*1000 = 3000, Diskon 30% = 900, Net = 2100
            DiskonStrategy memberVIP = new MemberVIPDiskonStrategy();
            double tagihanMotor = gedung.hitungTagihan(tMotor, 11, memberVIP);

            // EV: 4 Jam (10:00 -> 14:00), Insentif Hijau EV (Diskon 20%)
            // Subtotal EV: 5000 + 3*3000 = 14000, Diskon 20% = 2800, Net = 11200
            DiskonStrategy evIncentive = new EVIncentiveDiskonStrategy();
            double tagihanEV = gedung.hitungTagihan(tEV, 14, evIncentive);

            if (tagihanMobil == 9900.0 && tagihanMotor == 2100.0 && tagihanEV == 11200.0) {
                pass("TEST-1.2", "Kalkulasi durasi reguler & strategi diskon (Weekend 10%, VIP 30%, EV 20%) tepat");
            } else {
                fail("TEST-1.2", String.format("Tagihan keliru: Mobil=%.0f, Motor=%.0f, EV=%.0f", tagihanMobil, tagihanMotor, tagihanEV));
            }

            // 4. Pembayaran Beragam Metode: CASH, E-MONEY, QRIS
            IPaymentProcessor cashProc   = new CashPaymentProcessor();
            IPaymentProcessor emoneyProc = new EMoneyPaymentProcessor();
            IPaymentProcessor qrisProc   = new QRISPaymentProcessor("QRIS_GOPAY");

            // Mobil bayar CASH Rp 10.000 (Kembalian Rp 100)
            boolean checkoutMobil = gedung.prosesKeluar(tMobil, 11, cashProc, 10000.0, promoWeekend);
            // Motor bayar E-MONEY Rp 2.100 (Pas)
            boolean checkoutMotor = gedung.prosesKeluar(tMotor, 11, emoneyProc, 2100.0, memberVIP);
            // EV bayar QRIS Rp 11.200 (Pas)
            boolean checkoutEV    = gedung.prosesKeluar(tEV, 14, qrisProc, 11200.0, evIncentive);

            if (checkoutMobil && checkoutMotor && checkoutEV &&
                tMobil.isSudahDibayar() && tMotor.isSudahDibayar() && tEV.isSudahDibayar()) {
                pass("TEST-1.3", "Checkout sukses untuk 3 metode: CASH (Rp 10.000), E-MONEY (Rp 2.100), QRIS (Rp 11.200)");
            } else {
                fail("TEST-1.3", "Salah satu proses checkout dengan payment processor gagal.");
            }

            // 5. Pembebasan Slot Kembali Kosong
            boolean semuaSlotKosong = (!slotMobil.isTerisi() && slotMobil.getKendaraanParkir() == null) &&
                                      (!slotMotor.isTerisi() && slotMotor.getKendaraanParkir() == null) &&
                                      (!slotEV.isTerisi() && slotEV.getKendaraanParkir() == null);

            int kuotaMobil = gedung.getSisaKuota("MOBIL");
            int kuotaMotor = gedung.getSisaKuota("MOTOR");
            int kuotaEV    = gedung.getSisaKuota("MOBIL_LISTRIK");

            if (semuaSlotKosong && kuotaMobil == 1 && kuotaMotor == 1 && kuotaEV == 1) {
                pass("TEST-1.4", "Pembebasan slot berhasil: Slot B01, A01, EV1 kembali KOSONG dan kuota pulih penuh");
            } else {
                fail("TEST-1.4", "Slot tidak terbebas sempurna setelah kendaraan keluar.");
            }

            // 6. Penerbitan Struk Pembayaran
            System.out.println("   [STRUK PREVIEW]");
            gedung.cetakStrukKeluar(tMobil);
            gedung.cetakStrukKeluar(tMotor);
            gedung.cetakStrukKeluar(tEV);
            pass("TEST-1.5", "Struk kuitansi keluar resmi berhasil diterbitkan untuk seluruh transaksi checkout");

            // 7. Kas Parkir Bertambah Akurat
            double totalSetoranNet = tagihanMobil + tagihanMotor + tagihanEV; // 9900 + 2100 + 11200 = 23200
            double saldoKasAkhir = gedung.getTotalPendapatan();

            if (saldoKasAkhir == (saldoKasAwal + totalSetoranNet)) {
                pass("TEST-1.6", String.format("Kas parkir bertambah akurat: Rp %,.0f -> Rp %,.0f (+Rp %,.0f)",
                        saldoKasAwal, saldoKasAkhir, totalSetoranNet));
            } else {
                fail("TEST-1.6", String.format("Inkonsistensi saldo kas: Harapan Rp %,.0f, Realita Rp %,.0f",
                        (saldoKasAwal + totalSetoranNet), saldoKasAkhir));
            }

        } catch (Exception e) {
            fail("TEST-1.X", "Exception tidak terduga pada Skenario Normal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // SKENARIO 2: BOUNDARY (NILAI BATAS, KAPASITAS BATAS TERAKHIR, NOMINAL PAS)
    // =========================================================================
    private static void runSkenario2_Boundary() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(" [SKENARIO 2: BOUNDARY] PARKIR 1 JAM, 24 JAM, BATAS TERAKHIR A09/B12/EV2, PAS");
        System.out.println("--------------------------------------------------------------------------------");

        try {
            IKendaraanRepository repo = new InMemoryKendaraanRepository();
            GedungParkir gedung = new GedungParkir("Mall Grand PENS Boundary", 5000, 3000, 2000, 1000, repo);

            // -----------------------------------------------------------------
            // 2.1 Parkir Tepat 1 Jam (Biaya Minimum)
            // -----------------------------------------------------------------
            SlotParkir slotM1 = new SlotParkir("B01", 1, "MOBIL");
            gedung.tambahSlot(slotM1);

            Kendaraan mobil1Jam = new Kendaraan("B 1001 MIN", "MOBIL", "Honda Brio");
            KarcisParkir t1Jam = gedung.prosesMasuk(mobil1Jam, 8);

            // Masuk 08:00, Keluar 09:00 -> Durasi tepat 1 Jam
            double tagihan1Jam = gedung.hitungTagihan(t1Jam, 9, new TanpaDiskonStrategy());
            // Masuk 08:00, Keluar 08:00 -> Durasi 0 Jam tetap minimal 1 Jam (Biaya Minimum Rp 5.000)
            int durasiNol = t1Jam.hitungDurasi(8);
            double tagihanNol = gedung.hitungTagihan(t1Jam, 8, new TanpaDiskonStrategy());

            if (tagihan1Jam == 5000.0 && durasiNol == 1 && tagihanNol == 5000.0) {
                pass("TEST-2.1", "Boundary Parkir Tepat 1 Jam: Biaya minimum Rp 5.000 (tanpa charge tambahan jam berikutnya)");
            } else {
                fail("TEST-2.1", String.format("Kalkulasi biaya minimum keliru: 1jam=%.0f, 0jam=%.0f", tagihan1Jam, tagihanNol));
            }
            gedung.prosesKeluar(t1Jam, 9, new CashPaymentProcessor(), 5000.0, new TanpaDiskonStrategy());

            // -----------------------------------------------------------------
            // 2.2 Parkir 24 Jam Penuh & Pembatasan Maksimal Harian (Daily Cap)
            // -----------------------------------------------------------------
            Kendaraan mobil24Jam = new Kendaraan("L 2424 CAP", "MOBIL", "Pajero Sport");
            KarcisParkir t24Jam = gedung.prosesMasuk(mobil24Jam, 0); // Masuk 00:00

            // Keluar 24:00 -> Durasi 24 Jam
            // Tanpa batasan: 5000 + (23 * 3000) = Rp 74.000
            // Dengan Plafon Maksimal Harian: Dibatasi maksimal Rp 50.000 / hari
            BatasTarifHarianDiskonStrategy capHarian = new BatasTarifHarianDiskonStrategy(50000.0);
            double tagihan24Jam = gedung.hitungTagihan(t24Jam, 24, capHarian);
            double diskonPlafon = t24Jam.getPotonganDiskon();

            // Uji Crossover Tengah Malam: Masuk 23:00, Keluar 02:00 -> Durasi 3 Jam
            Kendaraan mobilMidnight = new Kendaraan("M 2302 MID", "MOBIL", "Ertiga");
            SlotParkir slotMid = new SlotParkir("B02", 1, "MOBIL");
            gedung.tambahSlot(slotMid);
            KarcisParkir tMid = gedung.prosesMasuk(mobilMidnight, 23);
            int durasiMidnight = tMid.hitungDurasi(2);

            if (tagihan24Jam == 50000.0 && diskonPlafon == 24000.0 && durasiMidnight == 3) {
                pass("TEST-2.2", "Boundary Parkir 24 Jam: Plafon tarif harian Rp 50.000 sukses membatasi tagihan bruto Rp 74.000");
            } else {
                fail("TEST-2.2", String.format("Plafon 24 jam gagal: Tagihan=%.0f (Diskon=%.0f), DurasiMidnight=%d",
                        tagihan24Jam, diskonPlafon, durasiMidnight));
            }
            gedung.prosesKeluar(t24Jam, 24, new CashPaymentProcessor(), 50000.0, capHarian);
            gedung.prosesKeluar(tMid, 2, new CashPaymentProcessor(), 11000.0, new TanpaDiskonStrategy());

            // -----------------------------------------------------------------
            // 2.3 Alokasi Kapasitas Batas Terakhir (A09 Motor, B12/C12 Mobil, EV2 EV)
            // -----------------------------------------------------------------
            GedungParkir gedungBlueprint = new GedungParkir("Grand PENS Blueprint", 5000, 3000, 2000, 1000, repo);

            // A. Motor: Slot A01 s/d A09 (9 Slot)
            List<SlotParkir> slotsMotor = new ArrayList<>();
            for (int i = 1; i <= 9; i++) {
                slotsMotor.add(new SlotParkir(String.format("A%02d", i), 1, "MOTOR"));
            }
            gedungBlueprint.tambahDaftarSlot(slotsMotor);

            // Isi slot A01 s/d A08 (8 motor masuk)
            for (int i = 1; i <= 8; i++) {
                gedungBlueprint.prosesMasuk(new Kendaraan(String.format("L 100%d MTR", i), "MOTOR", "Motor " + i), 8);
            }
            // Sisa slot motor tepat 1 (yaitu A09)
            int sisaMotorSebelum = gedungBlueprint.getSisaKuota("MOTOR");
            Kendaraan motorTerakhir = new Kendaraan("L 9909 MTR", "MOTOR", "Yamaha Aerox");
            KarcisParkir tMotorBatas = gedungBlueprint.prosesMasuk(motorTerakhir, 9);
            int sisaMotorSesudah = gedungBlueprint.getSisaKuota("MOTOR");

            boolean motorBatasValid = (sisaMotorSebelum == 1) &&
                                      (tMotorBatas != null && "A09".equals(tMotorBatas.getSlot().getIdSlot())) &&
                                      (sisaMotorSesudah == 0);

            // B. Mobil: Slot B01 s/d B12 (tanpa B05 tangga = 11 slot)
            List<SlotParkir> slotsMobilB = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                if (i == 5) continue; // Tangga
                slotsMobilB.add(new SlotParkir(String.format("B%02d", i), 1, "MOBIL"));
            }
            gedungBlueprint.tambahDaftarSlot(slotsMobilB);

            // Isi 10 slot mobil (B01..B04, B06..B11) sehingga tersisa B12
            int hitungIsi = 0;
            for (int i = 1; i <= 12; i++) {
                if (i == 5 || i == 12) continue;
                gedungBlueprint.prosesMasuk(new Kendaraan(String.format("B 20%02d MBL", i), "MOBIL", "Mobil " + i), 8);
                hitungIsi++;
            }
            int sisaMobilBSebelum = gedungBlueprint.getSisaKuota("MOBIL");
            Kendaraan mobilBatasB12 = new Kendaraan("B 9912 MBL", "MOBIL", "Honda CR-V");
            KarcisParkir tMobilB12 = gedungBlueprint.prosesMasuk(mobilBatasB12, 10);

            boolean mobilB12Valid = (sisaMobilBSebelum == 1) &&
                                    (tMobilB12 != null && "B12".equals(tMobilB12.getSlot().getIdSlot()));

            // Tambahkan baris C (C01 s/d C12 tanpa C05)
            List<SlotParkir> slotsMobilC = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                if (i == 5) continue;
                slotsMobilC.add(new SlotParkir(String.format("C%02d", i), 1, "MOBIL"));
            }
            gedungBlueprint.tambahDaftarSlot(slotsMobilC);

            // Isi C01 s/d C11 sehingga menyisakan C12
            for (int i = 1; i <= 11; i++) {
                if (i == 5) continue;
                gedungBlueprint.prosesMasuk(new Kendaraan(String.format("L 30%02d MOB", i), "MOBIL", "Mobil C" + i), 8);
            }
            Kendaraan mobilBatasC12 = new Kendaraan("L 9912 MOB", "MOBIL", "Toyota Camry");
            KarcisParkir tMobilC12 = gedungBlueprint.prosesMasuk(mobilBatasC12, 11);

            boolean mobilC12Valid = (tMobilC12 != null && "C12".equals(tMobilC12.getSlot().getIdSlot()));

            // C. Mobil Listrik (EV): Slot EV1 dan EV2 (2 Slot)
            gedungBlueprint.tambahSlot(new SlotParkir("EV1", 1, "MOBIL_LISTRIK"));
            gedungBlueprint.tambahSlot(new SlotParkir("EV2", 1, "MOBIL_LISTRIK"));

            // Isi EV1
            gedungBlueprint.prosesMasuk(new Kendaraan("B 1001 ELE", "MOBIL_LISTRIK", "Wuling Air EV"), 8);
            int sisaEVSebelum = gedungBlueprint.getSisaKuota("MOBIL_LISTRIK");
            Kendaraan evBatasTerakhir = new Kendaraan("W 9902 ELE", "MOBIL_LISTRIK", "Hyundai Ioniq 6");
            KarcisParkir tEVBatas = gedungBlueprint.prosesMasuk(evBatasTerakhir, 9);
            int sisaEVSesudah = gedungBlueprint.getSisaKuota("MOBIL_LISTRIK");

            boolean evBatasValid = (sisaEVSebelum == 1) &&
                                   (tEVBatas != null && "EV2".equals(tEVBatas.getSlot().getIdSlot())) &&
                                   (sisaEVSesudah == 0);

            if (motorBatasValid && mobilB12Valid && mobilC12Valid && evBatasValid) {
                pass("TEST-2.3", "Alokasi kapasitas batas terakhir presisi: Motor -> A09, Mobil -> B12 & C12, EV -> EV2");
            } else {
                fail("TEST-2.3", String.format("Alokasi batas terakhir gagal (MotorA09=%b, MobilB12=%b, MobilC12=%b, EV2=%b)",
                        motorBatasValid, mobilB12Valid, mobilC12Valid, evBatasValid));
            }

            // -----------------------------------------------------------------
            // 2.4 Pembayaran dengan Nominal Pas (Rp 0 Kembalian)
            // -----------------------------------------------------------------
            double tagihanPas = gedungBlueprint.hitungTagihan(tMotorBatas, 11, new TanpaDiskonStrategy()); // 2 Jam: 2000 + 1000 = 3000
            double uangPas = 3000.0;
            BiayaParkir rincianBiaya = BiayaParkir.of(tagihanPas, 0.0, "Reguler");
            double kembalian = rincianBiaya.hitungKembalian(uangPas);

            boolean checkoutPas = gedungBlueprint.prosesKeluar(tMotorBatas, 11, new CashPaymentProcessor(), uangPas, new TanpaDiskonStrategy());

            if (checkoutPas && kembalian == 0.0 && tMotorBatas.isSudahDibayar()) {
                pass("TEST-2.4", "Boundary Pembayaran Pas: Tagihan Rp 3.000 dibayar Rp 3.000 menghasilkan tepat Rp 0 kembalian");
            } else {
                fail("TEST-2.4", "Pembayaran nominal pas gagal atau kembalian tidak nol.");
            }

        } catch (Exception e) {
            fail("TEST-2.X", "Exception tidak terduga pada Skenario Boundary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // SKENARIO 3: INVALID (DEFENSIVE VALIDATION, REGEX, WAKTU ANOMALI, TIKET BODONG)
    // =========================================================================
    private static void runSkenario3_Invalid() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(" [SKENARIO 3: INVALID] PLAT NON-REGEX (12345/KOSONG), JAM KELUAR < MASUK, BODONG");
        System.out.println("--------------------------------------------------------------------------------");

        try {
            // -----------------------------------------------------------------
            // 3.1 Plat Nomor Tidak Sesuai Regex ('12345', Kosong, Simbol Liar)
            // -----------------------------------------------------------------
            boolean regexAngkaTertangkap = false;
            try {
                Kendaraan.validasiFormatPlat("12345");
            } catch (FormatPlatTidakValidException ex) {
                regexAngkaTertangkap = true;
            }

            boolean regexKosongTertangkap = false;
            try {
                Kendaraan.validasiFormatPlat("");
            } catch (FormatPlatTidakValidException ex) {
                regexKosongTertangkap = true;
            }

            boolean regexSpasiTertangkap = false;
            try {
                Kendaraan.validasiFormatPlat("    ");
            } catch (FormatPlatTidakValidException ex) {
                regexSpasiTertangkap = true;
            }

            boolean regexSimbolTertangkap = false;
            try {
                Kendaraan.validasiFormatPlat("$%#@!");
            } catch (FormatPlatTidakValidException ex) {
                regexSimbolTertangkap = true;
            }

            if (regexAngkaTertangkap && regexKosongTertangkap && regexSpasiTertangkap && regexSimbolTertangkap) {
                pass("TEST-3.1", "Defensive Regex: Plat angka murni '12345', string kosong, dan simbol liar ditolak dengan aman");
            } else {
                fail("TEST-3.1", String.format("Validasi regex plat bocor: Angka=%b, Kosong=%b, Spasi=%b, Simbol=%b",
                        regexAngkaTertangkap, regexKosongTertangkap, regexSpasiTertangkap, regexSimbolTertangkap));
            }

            // -----------------------------------------------------------------
            // 3.2 Jam Keluar < Jam Masuk (Anomali Kronologis Waktu Parkir)
            // -----------------------------------------------------------------
            boolean anomaliWaktuTertangkap = false;
            try {
                // Skenario: Masuk jam 14:00, diklaim keluar jam 10:00 pada hari operasional yang sama
                KarcisParkir.validasiWaktuOperasional(14, 10);
            } catch (IllegalArgumentException ex) {
                anomaliWaktuTertangkap = ex.getMessage().contains("tidak boleh lebih kecil dari jam masuk");
            }

            boolean jamKeluarNegatifTertolak = false;
            try {
                KarcisParkir.validasiWaktuOperasional(8, -1);
            } catch (IllegalArgumentException ex) {
                jamKeluarNegatifTertolak = true;
            }

            boolean jamKeluarLebih23Tertolak = false;
            try {
                KarcisParkir.validasiWaktuOperasional(8, 25);
            } catch (IllegalArgumentException ex) {
                jamKeluarLebih23Tertolak = true;
            }

            if (anomaliWaktuTertangkap && jamKeluarNegatifTertolak && jamKeluarLebih23Tertolak) {
                pass("TEST-3.2", "Defensive Time Validation: Jam keluar < jam masuk dan jam di luar 0-23 ditolak tegas");
            } else {
                fail("TEST-3.2", String.format("Validasi jam tidak valid bocor: Anomali=%b, Negatif=%b, >23=%b",
                        anomaliWaktuTertangkap, jamKeluarNegatifTertolak, jamKeluarLebih23Tertolak));
            }

            // -----------------------------------------------------------------
            // 3.3 Checkout Nomor Tiket Tidak Pernah Terdaftar (Bodong / Fiktif)
            // -----------------------------------------------------------------
            IKendaraanRepository repo = new InMemoryKendaraanRepository();
            GedungParkir gedung = new GedungParkir("Mall Grand PENS Validasi", 5000, 3000, 2000, 1000, repo);

            boolean tiketNullTertangkap = false;
            try {
                gedung.prosesKeluar(null, 12, "CASH", 50000.0);
            } catch (TiketTidakDitemukanException ex) {
                tiketNullTertangkap = true;
            }

            boolean platBodongTertangkap = false;
            try {
                gedung.prosesKeluarDenganPlat("W 0000 XYZ", 12, "CASH", 50000.0);
            } catch (TiketTidakDitemukanException ex) {
                platBodongTertangkap = ex.getMessage().contains("W 0000 XYZ");
            }

            KarcisParkir tiketFiktif = gedung.cariTiketAktif("TKT-99999");
            boolean pencarianAman = (tiketFiktif == null);

            if (tiketNullTertangkap && platBodongTertangkap && pencarianAman) {
                pass("TEST-3.3", "Defensive Ticket Lookup: Tiket null & plat bodong 'W 0000 XYZ' memicu TiketTidakDitemukanException");
            } else {
                fail("TEST-3.3", String.format("Tiket bodong tidak tertangani aman: Null=%b, Bodong=%b, CariAman=%b",
                        tiketNullTertangkap, platBodongTertangkap, pencarianAman));
            }

        } catch (Exception e) {
            fail("TEST-3.X", "Exception tidak terduga pada Skenario Invalid: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // SKENARIO 4: FAILURE & RECOVERY / ROBUSTNESS (ATOMIC STATE PRESERVATION)
    // =========================================================================
    private static void runSkenario4_FailureAndRecovery() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(" [SKENARIO 4: ROBUSTNESS] PEMBAYARAN KURANG -> ATOMIC STATE & SYSTEM RECOVERY   ");
        System.out.println("--------------------------------------------------------------------------------");

        try {
            IKendaraanRepository repo = new InMemoryKendaraanRepository();
            GedungParkir gedung = new GedungParkir("Mall Grand PENS Robust", 5000, 3000, 2000, 1000, repo);

            SlotParkir slotB1 = new SlotParkir("B01", 1, "MOBIL");
            gedung.tambahSlot(slotB1);

            Kendaraan mobil = new Kendaraan("B 4040 ROB", "MOBIL", "Toyota Fortuner");
            KarcisParkir tiket = gedung.prosesMasuk(mobil, 8); // Masuk jam 08:00
            gedung.tutupGerbang(); // Gerbang otomatis menutup setelah kendaraan parkir di slot

            double saldoKasSebelum = gedung.getTotalPendapatan();

            // -----------------------------------------------------------------
            // 4.1 Kegagalan Pembayaran: Uang Bayar Kurang dari Tagihan
            // -----------------------------------------------------------------
            // Jam masuk 08:00, jam keluar 12:00 (Durasi 4 Jam)
            // Tagihan: 5000 + (3 * 3000) = Rp 14.000. Uang disetor: Rp 5.000 (Kurang Rp 9.000)
            boolean bayarKurangTertangkap = false;
            double kekuranganTercatat = 0.0;

            try {
                gedung.prosesKeluar(tiket, 12, new CashPaymentProcessor(), 5000.0, new TanpaDiskonStrategy());
            } catch (PembayaranKurangException ex) {
                bayarKurangTertangkap = true;
                kekuranganTercatat = ex.getKekurangan();
            }

            if (bayarKurangTertangkap && kekuranganTercatat == 9000.0) {
                pass("TEST-4.1", "Robustness Fault: Pembayaran kurang Rp 9.000 berhasil dicegat oleh PembayaranKurangException");
            } else {
                fail("TEST-4.1", "PembayaranKurangException tidak dilempar atau nilai kekurangan keliru: " + kekuranganTercatat);
            }

            // -----------------------------------------------------------------
            // 4.2 Verifikasi Atomic State Preservation (State Tidak Boleh Corrupt)
            // -----------------------------------------------------------------
            boolean slotTetapTerisi       = slotB1.isTerisi();
            boolean kendaraanMasihDiSlot  = mobil.equals(slotB1.getKendaraanParkir());
            boolean tiketBelumLunas       = !tiket.isSudahDibayar();
            boolean kasTidakBerubahLiar   = (gedung.getTotalPendapatan() == saldoKasSebelum);
            boolean palangTetapTertutup   = !gedung.isGerbangTerbuka();
            boolean tiketMasihAktif       = (gedung.cariTiketAktifDenganPlat(mobil.getPlatNomor()) != null);

            if (slotTetapTerisi && kendaraanMasihDiSlot && tiketBelumLunas &&
                kasTidakBerubahLiar && palangTetapTertutup && tiketMasihAktif) {
                pass("TEST-4.2", "Atomic State Preservation: Slot tetap terisi, kendaraan tetap parkir, kas tidak bertambah liar, tiket belum lunas");
            } else {
                fail("TEST-4.2", String.format("State rusak pasca kegagalan bayar (Slot=%b, Kend=%b, BelumLunas=%b, KasAman=%b, Palang=%b, Aktif=%b)",
                        slotTetapTerisi, kendaraanMasihDiSlot, tiketBelumLunas, kasTidakBerubahLiar, palangTetapTertutup, tiketMasihAktif));
            }

            // -----------------------------------------------------------------
            // 4.3 Pemulihan Sistem (Graceful System Recovery Tanpa Corrupt Data)
            // -----------------------------------------------------------------
            // Customer melunasi dengan uang pas penuh: Rp 14.000
            boolean recoverySukses = gedung.prosesKeluar(tiket, 12, new CashPaymentProcessor(), 14000.0, new TanpaDiskonStrategy());

            boolean slotTerbebas        = !slotB1.isTerisi() && slotB1.getKendaraanParkir() == null;
            boolean tiketKiniLunas      = tiket.isSudahDibayar();
            boolean kasBertambahValid   = (gedung.getTotalPendapatan() == (saldoKasSebelum + 14000.0));
            boolean tiketTidakLagiAktif = (gedung.cariTiketAktifDenganPlat(mobil.getPlatNomor()) == null);

            if (recoverySukses && slotTerbebas && tiketKiniLunas && kasBertambahValid && tiketTidakLagiAktif) {
                pass("TEST-4.3", "Graceful Recovery: Pelunasan susulan sukses, slot terbebas, tiket lunas, kas bertambah sah tanpa duplikasi data");
            } else {
                fail("TEST-4.3", "Sistem gagal pulih dari kondisi kegagalan pembayaran.");
            }

        } catch (Exception e) {
            fail("TEST-4.X", "Exception tidak terduga pada Skenario Failure & Recovery: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
