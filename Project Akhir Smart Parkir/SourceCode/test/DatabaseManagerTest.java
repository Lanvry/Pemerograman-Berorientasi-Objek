package test;

import model.*;

public class DatabaseManagerTest {
    public static void main(String[] args) {
        DatabaseManager.inisialisasiDatabase();

        String plate = "D " + (1000 + (int)(Math.random() * 8999)) + " EV";
        String ticketId = "TKT-TEST-" + System.currentTimeMillis();

        Kendaraan k = new Kendaraan(plate, "MOBIL_LISTRIK", "Hyundai Ioniq");
        SlotParkir s = new SlotParkir("EV-L1-01", 1, "MOBIL_LISTRIK");
        KarcisParkir t = new KarcisParkir(ticketId, k, s, 10);

        // 1. Simpan transaksi masuk
        boolean saved = DatabaseManager.simpanTransaksiMasuk(t, "ANPR_TICKETLESS");
        if (!saved) {
            throw new AssertionError("Ticket should save to database");
        }

        // 2. Cari tiket aktif by plat
        KarcisParkir found = DatabaseManager.cariTiketAktifByPlat(plate);
        if (found == null || !found.getNomorTiket().equals(ticketId)) {
            throw new AssertionError("Should find active ticket by plate");
        }

        // 3. Update pelunasan
        boolean settled = DatabaseManager.updatePelunasanTiket(ticketId, 12, 8000.0, "QRIS_MIDTRANS");
        if (!settled) {
            throw new AssertionError("Ticket settlement should succeed");
        }

        // 4. Setelah lunas, tiket tidak boleh muncul sebagai tiket aktif
        KarcisParkir activeAfterPaid = DatabaseManager.cariTiketAktifByPlat(plate);
        if (activeAfterPaid != null) {
            throw new AssertionError("Settled ticket must no longer appear as active");
        }

        System.out.println("DatabaseManagerTest: PASS");
    }
}
