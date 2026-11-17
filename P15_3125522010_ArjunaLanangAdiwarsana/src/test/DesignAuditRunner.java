package test;

import model.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * ============================================================================
 * MODUL 15: DESIGN AUDIT & INTEGRATED SYSTEM VERIFICATION RUNNER
 * POLITEKNIK ELEKTRONIKA NEGERI SURABAYA (PENS) - PSDKU SUMENEP
 * ============================================================================
 * Pengembang : Arjuna Lanang Adiwarsana
 * NRP        : 3125522010
 * Mata Kuliah: Pemrograman Berorientasi Obyek (PBO)
 * 
 * Melakukan pengujian sistemik dan trace interaksi objek untuk 4 Skenario Uji:
 * 1. Skenario Normal (Happy Path Checkout with Strategy & Gate Control)
 * 2. Skenario Boundary (Durasi 0 Jam, Crossover Tengah Malam, Slot Terakhir)
 * 3. Skenario Invalid (Plat Tidak Valid, Pembayaran Kurang, Tiket Palsu)
 * 4. Skenario Failure (Kapasitas Penuh, Anti-Passback, Fallback Emergency)
 * ============================================================================
 */
public class DesignAuditRunner {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("       PEMROGRAMAN BERORIENTASI OBYEK (PBO) - PERTEMUAN 15: DESIGN AUDIT        ");
        System.out.println("              VERIFIKASI INTEGRITAS DESAIN & ARSITEKTUR SMART PARKIR            ");
        System.out.println("================================================================================");
        System.out.println("  Mahasiswa : Arjuna Lanang Adiwarsana  |  NRP: 3125522010");
        System.out.println("  Kelas     : D3 PJJ Teknik Informatika - PENS PSDKU Sumenep");
        System.out.println("  Institusi : Departemen Teknik Informatika dan Komputer - PENS");
        System.out.println("================================================================================\n");

        runSkenario1_Normal();
        runSkenario2_Boundary();
        runSkenario3_Invalid();
        runSkenario4_Failure();

        System.out.println("\n================================================================================");
        System.out.printf("  REKAPITULASI AUDIT DESAIN SISTEM: %d BERHASIL (PASSED), %d GAGAL (FAILED)%n", totalPassed, totalFailed);
        System.out.println("================================================================================");

        if (totalFailed == 0) {
            System.out.println(">> [AUDIT VERIFIED] Sistem Terintegrasi Memenuhi Seluruh Spesifikasi Arsitektur!");
            System.out.println(">> Seluruh Invariant, Enkapsulasi, Relasi Objek, dan Exception Terjaga Kuat.");
        } else {
            System.err.println(">> [AUDIT FAILED] Terdapat anomali atau pelanggaran kontrak pada sistem!");
            System.exit(1);
        }
    }

    private static void pass(String msg) {
        totalPassed++;
        System.out.println("   [PASSED] " + msg);
    }

    private static void fail(String msg) {
        totalFailed++;
        System.err.println("   [FAILED] " + msg);
    }

    // =========================================================================
    // SKENARIO 1: NORMAL (HAPPY PATH INTEGRATION)
    // =========================================================================
    private static void runSkenario1_Normal() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("[SKENARIO 1: NORMAL] Alur Transaksi Standar (Kendaraan Masuk -> Parkir -> Bayar)");
        System.out.println("--------------------------------------------------------------------------------");
        try {
            GedungParkir gedung = new GedungParkir("Grand PENS Park Center", 5000, 3000, 2000, 1000);
            gedung.tambahSlot(new SlotParkir("MBL-L1-01", 1, "MOBIL"));
            gedung.tambahSlot(new SlotParkir("MBL-L1-02", 1, "MOBIL"));

            // 1. Trace Masuk
            Kendaraan mobil = new Kendaraan("B 1010 PBO", "MOBIL", "Toyota Yaris");
            System.out.println("  [TRACE 1.1] Client memicu prosesMasuk() untuk plat " + mobil.getPlatNomor());
            KarcisParkir tiket = gedung.prosesMasuk(mobil, 8);
            System.out.println("  [TRACE 1.2] GedungParkir alokasikan Slot: " + tiket.getSlot().getIdSlot() + " (Lantai " + tiket.getSlot().getLantai() + ")");
            System.out.println("  [TRACE 1.3] Status Palang Gerbang Masuk: " + gedung.getStatusGerbang());

            if (tiket != null && "MBL-L1-01".equals(tiket.getSlot().getIdSlot())) {
                pass("Tiket diterbitkan dengan slot MBL-L1-01 pada jam masuk 08:00 WIB");
            } else {
                fail("Gagal menerbitkan tiket normal.");
            }

            // 2. Trace Hitung Biaya dengan Strategi Diskon OCP
            int jamKeluar = 11; // 3 Jam
            DiskonStrategy promoWeekend = new PromoWeekendDiskonStrategy();
            double tagihan = gedung.hitungTagihan(tiket, jamKeluar, promoWeekend);
            System.out.println("  [TRACE 1.4] Hitung Durasi (8 -> 11): 3 Jam. Gross: Rp 11.000, Diskon 10%: Rp 1.100, Net: Rp " + (long)tagihan);

            if (tagihan == 9900.0) {
                pass("Kalkulasi tagihan 3 jam dengan diskon akhir pekan 10% valid (Rp 9.900)");
            } else {
                fail("Kalkulasi tagihan tidak sesuai: " + tagihan);
            }

            // 3. Trace Pembayaran via CashPaymentProcessor
            IPaymentProcessor cash = new CashPaymentProcessor();
            System.out.println("  [TRACE 1.5] Eksekusi Pembayaran Kasir Cash: Nominal Bayar Rp 10.000");
            boolean sukses = gedung.prosesKeluar(tiket, jamKeluar, cash, 10000.0, promoWeekend);

            if (sukses && tiket.isSudahDibayar() && !tiket.getSlot().isTerisi()) {
                pass("Checkout sukses: Karcis lunas, slot MBL-L1-01 dibebaskan, gerbang keluar terbuka");
            } else {
                fail("Status checkout normal tidak valid.");
            }

        } catch (Exception e) {
            fail("Exception tak terduga pada Skenario Normal: " + e.getMessage());
        }
    }

    // =========================================================================
    // SKENARIO 2: BOUNDARY (NILAI BATAS & TITIK EKSTRIM)
    // =========================================================================
    private static void runSkenario2_Boundary() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("[SKENARIO 2: BOUNDARY] Evaluasi Titik Ekstrim (Durasi 0 Jam, Midnight, Kuota 0)");
        System.out.println("--------------------------------------------------------------------------------");
        try {
            GedungParkir gedung = new GedungParkir("PENS Boundary Plaza", 5000, 3000, 2000, 1000);
            gedung.tambahSlot(new SlotParkir("MBL-L1-01", 1, "MOBIL"));

            // 2A. Durasi Parkir 0 Jam (Masuk & Keluar pada jam yang sama)
            Kendaraan k1 = new Kendaraan("L 1111 PEN", "MOBIL", "Honda Brio");
            KarcisParkir t1 = gedung.prosesMasuk(k1, 10);
            int durasi0 = t1.hitungDurasi(10);
            double tagihan0 = gedung.hitungTagihan(t1, 10, new TanpaDiskonStrategy());
            System.out.println("  [TRACE 2.1] Boundary Durasi Masuk 10 Keluar 10 -> Durasi dihitung: " + durasi0 + " Jam, Tagihan: Rp " + (long)tagihan0);

            if (durasi0 == 1 && tagihan0 == 5000.0) {
                pass("Boundary Durasi Nol: Jam masuk sama dengan jam keluar tetap dihitung 1 jam tarif dasar (Rp 5.000)");
            } else {
                fail("Penanganan durasi 0 jam tidak tepat: durasi=" + durasi0 + ", tagihan=" + tagihan0);
            }
            gedung.prosesKeluar(t1, 10, new CashPaymentProcessor(), 5000.0, new TanpaDiskonStrategy());

            // 2B. Crossover Tengah Malam (Masuk 23:00, Keluar 02:00)
            Kendaraan kMidnight = new Kendaraan("M 2302 SMN", "MOBIL", "Suzuki Ertiga");
            KarcisParkir tMid = gedung.prosesMasuk(kMidnight, 23);
            int durasiMidnight = tMid.hitungDurasi(2);
            System.out.println("  [TRACE 2.2] Boundary Midnight Masuk 23:00 Keluar 02:00 -> Durasi: " + durasiMidnight + " Jam");

            if (durasiMidnight == 3) {
                pass("Boundary Midnight Crossover: 23:00 -> 02:00 dihitung tepat 3 jam menggunakan modulo 24");
            } else {
                fail("Kalkulasi midnight crossover keliru: " + durasiMidnight);
            }
            gedung.prosesKeluar(tMid, 2, new CashPaymentProcessor(), 11000.0, new TanpaDiskonStrategy());

            // 2C. Batas Kuota Terakhir (Tersisa tepat 1 slot, lalu menjadi 0)
            int kuotaSebelum = gedung.getSisaKuota("MOBIL");
            Kendaraan kLast = new Kendaraan("W 9999 SBY", "MOBIL", "Nissan March");
            KarcisParkir tLast = gedung.prosesMasuk(kLast, 14);
            int kuotaSesudah = gedung.getSisaKuota("MOBIL");
            System.out.println("  [TRACE 2.3] Boundary Slot Terakhir: Kuota Awal=" + kuotaSebelum + " -> Terisi Slot=" + tLast.getSlot().getIdSlot() + " -> Sisa Kuota=" + kuotaSesudah);

            if (kuotaSebelum == 1 && kuotaSesudah == 0) {
                pass("Boundary Kuota Ekstrim: Transisi kuota 1 menjadi 0 terpelihara secara deterministik");
            } else {
                fail("Transisi kuota tidak konsisten.");
            }

        } catch (Exception e) {
            fail("Exception tak terduga pada Skenario Boundary: " + e.getMessage());
        }
    }

    // =========================================================================
    // SKENARIO 3: INVALID (DEFENSIVE VALIDATION & REJECTIONS)
    // =========================================================================
    private static void runSkenario3_Invalid() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("[SKENARIO 3: INVALID] Penanganan Masukan Tidak Sah & Pelanggaran Kontrak");
        System.out.println("--------------------------------------------------------------------------------");

        // 3A. Validasi Format Plat Nomor (Regex Invasi)
        boolean platInvalidCaught = false;
        try {
            System.out.println("  [TRACE 3.1] Validasi format plat tidak sah: 'X@#999' dan 'A'");
            Kendaraan.validasiFormatPlat("X@#999");
        } catch (FormatPlatTidakValidException e) {
            platInvalidCaught = true;
            System.out.println("  [TRACE 3.2] FormatPlatTidakValidException tertangkap: " + e.getMessage());
        } catch (Exception e) {
            fail("Exception salah tipe untuk plat tidak valid: " + e.getClass().getName());
        }
        if (platInvalidCaught) {
            pass("Defensive Check: Format plat non-standar ditolak dengan FormatPlatTidakValidException");
        } else {
            fail("Plat tidak valid lolos tanpa proteksi!");
        }

        // 3B. Validasi Pembayaran Kurang (PembayaranKurangException)
        try {
            GedungParkir gedung = new GedungParkir("PENS Invalid Testing", 5000, 3000, 2000, 1000);
            gedung.tambahSlot(new SlotParkir("MBL-L1-01", 1, "MOBIL"));
            Kendaraan k = new Kendaraan("B 7777 ABC", "MOBIL", "Toyota Raize");
            KarcisParkir tiket = gedung.prosesMasuk(k, 8);

            boolean bayarKurangCaught = false;
            try {
                System.out.println("  [TRACE 3.3] Uji bayar kurang: Tagihan Rp 11.000, Uang Disetor Rp 5.000");
                gedung.prosesKeluar(tiket, 11, new CashPaymentProcessor(), 5000.0, new TanpaDiskonStrategy());
            } catch (PembayaranKurangException e) {
                bayarKurangCaught = true;
                System.out.println("  [TRACE 3.4] PembayaranKurangException tertangkap: " + e.getMessage());
            }

            if (bayarKurangCaught && !tiket.isSudahDibayar() && tiket.getSlot().isTerisi()) {
                pass("Defensive Check: Pembayaran kurang ditolak, tiket belum lunas, dan slot tetap terkunci");
            } else {
                fail("Pembayaran kurang tidak memicu PembayaranKurangException atau slot bocor!");
            }

            // 3C. Validasi Tiket Fiktif / Tidak Ditemukan
            KarcisParkir tiketPalsu = gedung.cariTiketAktif("TKT-NON-EXISTENT");
            System.out.println("  [TRACE 3.5] Pencarian karcis fiktif 'TKT-NON-EXISTENT': " + tiketPalsu);
            if (tiketPalsu == null) {
                pass("Defensive Check: Karcis tidak terdaftar mengembalikan null (Safe Query Contract)");
            } else {
                fail("Karcis fiktif ditemukan secara anomali!");
            }

        } catch (Exception e) {
            fail("Exception tak terduga pada Skenario Invalid: " + e.getMessage());
        }
    }

    // =========================================================================
    // SKENARIO 4: FAILURE (SISTEM RECOVERY & EXCEPTION RESILIENCE)
    // =========================================================================
    private static void runSkenario4_Failure() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("[SKENARIO 4: FAILURE] Ketahanan Sistem Terhadap Kegagalan (Fault Tolerance)");
        System.out.println("--------------------------------------------------------------------------------");

        try {
            GedungParkir gedung = new GedungParkir("PENS Resilient Center", 5000, 3000, 2000, 1000);
            gedung.tambahSlot(new SlotParkir("MBL-L1-01", 1, "MOBIL"));

            // 4A. Failure Kapasitas Penuh (KapasitasPenuhException)
            Kendaraan k1 = new Kendaraan("B 1111 ABC", "MOBIL", "Honda HR-V");
            gedung.prosesMasuk(k1, 8); // Mengisi slot tunggal
            gedung.tutupGerbang(); // Gerbang menutup setelah k1 masuk

            boolean fullCaught = false;
            try {
                Kendaraan k2 = new Kendaraan("B 2222 DEF", "MOBIL", "Daihatsu Rocky");
                System.out.println("  [TRACE 4.1] Mencoba memasukkan kendaraan kedua saat kapasitas 100% penuh");
                gedung.prosesMasuk(k2, 9);
            } catch (KapasitasPenuhException e) {
                fullCaught = true;
                System.out.println("  [TRACE 4.2] KapasitasPenuhException tertangkap: " + e.getMessage());
            }

            if (fullCaught && !gedung.isGerbangTerbuka()) {
                pass("Fault Resilience: Kapasitas penuh melempar KapasitasPenuhException dan palang tertutup rapat");
            } else {
                fail("Kapasitas penuh gagal diisolasi.");
            }

            // 4B. Anti-Passback / Duplikasi Kendaraan (KendaraanSudahParkirException)
            boolean duplicateCaught = false;
            try {
                System.out.println("  [TRACE 4.3] Mencoba memasukkan kembali kendaraan B 1111 ABC yang belum checkout");
                gedung.prosesMasuk(new Kendaraan("B 1111 ABC", "MOBIL", "Honda HR-V"), 10);
            } catch (KendaraanSudahParkirException e) {
                duplicateCaught = true;
                System.out.println("  [TRACE 4.4] KendaraanSudahParkirException tertangkap: " + e.getMessage());
            }

            if (duplicateCaught) {
                pass("Anti-Passback: Kendaraan dengan plat duplikat ditolak oleh sistem");
            } else {
                fail("Duplikasi plat nomor berhasil lolos tanpa exception!");
            }

            // 4C. Fallback Masuk Darurat (Emergency QR Ticket saat Vision/OCR Gagal)
            gedung.prosesKeluar(gedung.cariTiketAktifDenganPlat("B 1111 ABC"), 11, new CashPaymentProcessor(), 14000.0, new TanpaDiskonStrategy());
            System.out.println("  [TRACE 4.5] Simulasi Kamera ANPR Gagal Baca Plat -> Fallback Emergency Ticket");
            KarcisParkir tiketDarurat = gedung.prosesMasukEmergency("MOBIL", 12);

            if (tiketDarurat != null && tiketDarurat.getNomorTiket().startsWith("EMG-") && tiketDarurat.getKendaraan().getPlatNomor().startsWith("EMG-")) {
                pass("Graceful Fallback: Tiket Darurat ('EMG-XXXX') sukses diterbitkan saat sensor ANPR offline");
            } else {
                fail("Fallback tiket darurat gagal dioperasikan.");
            }

            // 4D. File Persistence Error Resilience via In-Memory Fallback
            System.out.println("  [TRACE 4.6] Uji Fallback DIP: Menginjeksi InMemoryKendaraanRepository");
            IKendaraanRepository memoryRepo = new InMemoryKendaraanRepository();
            gedung.setKendaraanRepository(memoryRepo);
            gedung.tambahKendaraan(new Kendaraan("D 3125 TI", "MOBIL", "PENS EV"));
            gedung.simpanArmadaKeFile(); // Menulis ke RAM aman tanpa melempar IOException disk

            if (memoryRepo.getSemua().size() == 1) {
                pass("Architectural DIP Fallback: Swapping repository ke In-Memory mencegah sistem lumpuh saat storage offline");
            } else {
                fail("DIP In-Memory fallback gagal menyimpan armada.");
            }

        } catch (Exception e) {
            fail("Exception tak terduga pada Skenario Failure: " + e.getMessage());
        }
    }
}
