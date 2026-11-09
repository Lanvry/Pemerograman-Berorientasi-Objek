package test;

import model.*;

public class DatabaseManagerTest {
    public static void main(String[] args) {
        DatabaseManager.inisialisasiDatabase();

        Kendaraan k = new Kendaraan("D 9999 EV", "MOBIL_LISTRIK", "Hyundai Ioniq");
        SlotParkir s = new SlotParkir("EV-L1-01", 1, "MOBIL_LISTRIK");
        KarcisParkir t = new KarcisParkir("TKT-TEST-99", k, s, 10);

        // 1. Simpan transaksi masuk
        boolean saved = DatabaseManager.simpanTransaksiMasuk(t, "ANPR_TICKETLESS");
        if (!saved) {
            throw new AssertionError("Ticket should save to database");
        }

        // 2. Cari tiket aktif by plat
        KarcisParkir found = DatabaseManager.cariTiketAktifByPlat("D 9999 EV");
        if (found == null || !found.getNomorTiket().equals("TKT-TEST-99")) {
            throw new AssertionError("Should find active ticket by plate");
        }

        // 3. Update pelunasan
        boolean settled = DatabaseManager.updatePelunasanTiket("TKT-TEST-99", 12, 8000.0, "QRIS_MIDTRANS");
        if (!settled) {
            throw new AssertionError("Ticket settlement should succeed");
        }

        // 4. Setelah lunas, tiket tidak boleh muncul sebagai tiket aktif
        KarcisParkir activeAfterPaid = DatabaseManager.cariTiketAktifByPlat("D 9999 EV");
        if (activeAfterPaid != null) {
            throw new AssertionError("Settled ticket must no longer appear as active");
        }

        System.out.println("DatabaseManagerTest: PASS");
    }
}
