package test;

import model.*;

/**
 * Unit Test Komprehensif untuk menguji materi Modul 10:
 * - 5 Custom Exceptions Domain SmartParkir
 * - Validasi input & state sebelum mutasi
 * - Atomic State Preservation (state tidak rusak ketika operasi ditolak)
 */
public class ExceptionHandlingTest {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   UNIT TEST MODUL 10: EXCEPTION HANDLING & VALIDASI STATE SMARTPARKIR    ");
        System.out.println("==========================================================================\n");

        int passed = 0;
        int failed = 0;

        // Test 1: Validasi Format Plat Nomor
        try {
            System.out.print("[TEST 1] FormatPlatTidakValidException pada plat kosong/rusak... ");
            try {
                Kendaraan.validasiFormatPlat("X");
                System.out.println("FAILED (Tidak melempar exception)");
                failed++;
            } catch (FormatPlatTidakValidException e) {
                System.out.println("PASSED (Tertangkap: " + e.getMessage() + ")");
                passed++;
            }
        } catch (Exception e) { failed++; }

        // Test 2: KendaraanSudahParkirException
        try {
            System.out.print("[TEST 2] KendaraanSudahParkirException pada duplikasi check-in... ");
            GedungParkir g = new GedungParkir("Test Parkir", 5000, 3000, 2000, 1000);
            g.tambahSlot(new SlotParkir("A01", 1, "MOBIL"));
            g.tambahSlot(new SlotParkir("A02", 1, "MOBIL"));

            Kendaraan k = new Kendaraan("B 1111 TST", "MOBIL", "Sedan");
            g.prosesMasuk(k, 8);

            try {
                g.prosesMasuk(k, 9); // Duplikasi
                System.out.println("FAILED (Tidak melempar exception)");
                failed++;
            } catch (KendaraanSudahParkirException e) {
                System.out.println("PASSED (Tertangkap: " + e.getMessage() + ")");
                passed++;
            }
        } catch (Exception e) { failed++; }

        // Test 3: KapasitasPenuhException
        try {
            System.out.print("[TEST 3] KapasitasPenuhException saat seluruh slot terisi penuh... ");
            GedungParkir g = new GedungParkir("Test Parkir Penuh", 5000, 3000, 2000, 1000);
            g.tambahSlot(new SlotParkir("MTR1", 1, "MOTOR")); // Hanya 1 slot

            Kendaraan m1 = new Kendaraan("B 2222 MTR", "MOTOR", "Vario");
            Kendaraan m2 = new Kendaraan("B 3333 MTR", "MOTOR", "Beat");

            g.prosesMasuk(m1, 8);
            try {
                g.prosesMasuk(m2, 9);
                System.out.println("FAILED (Tidak melempar exception)");
                failed++;
            } catch (KapasitasPenuhException e) {
                System.out.println("PASSED (Tertangkap: " + e.getMessage() + ")");
                passed++;
            }
        } catch (Exception e) { failed++; }

        // Test 4: PembayaranKurangException & State Preservation
        try {
            System.out.print("[TEST 4] PembayaranKurangException & Integritas State Slot... ");
            GedungParkir g = new GedungParkir("Test Bayar", 5000, 3000, 2000, 1000);
            SlotParkir s = new SlotParkir("B01", 1, "MOBIL");
            g.tambahSlot(s);

            Kendaraan k = new Kendaraan("B 4444 CAS", "MOBIL", "Avanza");
            KarcisParkir tiket = g.prosesMasuk(k, 8);

            double saldoSebelum = g.getTotalPendapatan();

            try {
                // Jam masuk 8, jam keluar 11 (3 jam -> Rp 5000 + 2x3000 = Rp 11.000). Bayar Rp 5.000 (Kurang)
                g.prosesKeluar(tiket, 11, "CASH", 5000.0);
                System.out.println("FAILED (Tidak melempar PembayaranKurangException)");
                failed++;
            } catch (PembayaranKurangException e) {
                boolean slotTetapTerisi = s.isTerisi();
                boolean tiketBelumLunas = !tiket.isSudahDibayar();
                boolean saldoTetap = (g.getTotalPendapatan() == saldoSebelum);

                if (slotTetapTerisi && tiketBelumLunas && saldoTetap) {
                    System.out.println("PASSED (State Terjaga: Slot tetap terisi, tiket belum lunas, saldo tidak berubah)");
                    passed++;
                } else {
                    System.out.println("FAILED (State rusak: slot/saldo berubah secara prematur)");
                    failed++;
                }
            }
        } catch (Exception e) { failed++; }

        // Test 5: TiketTidakDitemukanException
        try {
            System.out.print("[TEST 5] TiketTidakDitemukanException saat checkout plat bodong... ");
            GedungParkir g = new GedungParkir("Test Bodong", 5000, 3000, 2000, 1000);
            try {
                g.prosesKeluarDenganPlat("W 0000 XYZ", 12, "CASH", 20000.0);
                System.out.println("FAILED (Tidak melempar exception)");
                failed++;
            } catch (TiketTidakDitemukanException e) {
                System.out.println("PASSED (Tertangkap: " + e.getMessage() + ")");
                passed++;
            }
        } catch (Exception e) { failed++; }

        System.out.println("\n--------------------------------------------------------------------------");
        System.out.printf("HASIL PENGUJIAN: %d PASSED, %d FAILED (Total: %d Test Cases)%n", passed, failed, (passed + failed));
        System.out.println("--------------------------------------------------------------------------\n");

        if (failed == 0) {
            System.out.println(">> [VERIFIKASI SUKSES] Seluruh aturan Modul 10 terpenuhi secara sempurna!");
        } else {
            System.err.println(">> [VERIFIKASI GAGAL] Masih ada kegagalan pada skenario exception handling!");
            System.exit(1);
        }
    }
}
