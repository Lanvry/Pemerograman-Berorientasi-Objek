package test;

import model.*;

public class GedungParkirTest {
    public static void main(String[] args) throws Exception {
        GedungParkir g = new GedungParkir("Test Mall", 5000, 3000, 2000, 1000);
        g.tambahSlot(new SlotParkir("MBL-L1-01", 1, "MOBIL"));
        g.tambahSlot(new SlotParkir("MBL-L2-01", 2, "MOBIL"));
        g.tambahSlot(new SlotParkir("EV-L1-01", 1, "MOBIL_LISTRIK"));

        // Check initial quota
        if (g.getSisaKuota("MOBIL") != 2) {
            throw new AssertionError("Initial quota mobil must be 2, got " + g.getSisaKuota("MOBIL"));
        }
        if (g.getSisaKuota("MOBIL_LISTRIK") != 1) {
            throw new AssertionError("Initial quota EV must be 1, got " + g.getSisaKuota("MOBIL_LISTRIK"));
        }

        // Test ticketless check-in mobil reguler
        Kendaraan k1 = new Kendaraan("B 1111 AAA", "MOBIL", "Toyota Avanza");
        KarcisParkir t1 = g.prosesMasukTicketless(k1, 8);
        if (t1 == null) throw new AssertionError("Ticket must be issued");
        if (g.getSisaKuota("MOBIL") != 1) {
            throw new AssertionError("Quota mobil must decrease to 1, got " + g.getSisaKuota("MOBIL"));
        }
        if (g.getSisaKuotaLantai(1, "MOBIL") != 0) {
            throw new AssertionError("Lantai 1 must be filled first (waterfall), got " + g.getSisaKuotaLantai(1, "MOBIL"));
        }
        if (g.getSisaKuotaLantai(2, "MOBIL") != 1) {
            throw new AssertionError("Lantai 2 must still have 1, got " + g.getSisaKuotaLantai(2, "MOBIL"));
        }

        // Test search active ticket by plate
        KarcisParkir found = g.cariTiketAktif("B 1111 AAA");
        if (found == null || !found.getNomorTiket().equals(t1.getNomorTiket())) {
            throw new AssertionError("Must be able to find active ticket by plate");
        }

        // Test emergency QR check-in
        KarcisParkir tEmergency = g.prosesMasukEmergency("MOBIL", 9);
        if (tEmergency == null || !tEmergency.getNomorTiket().startsWith("EMG-")) {
            throw new AssertionError("Emergency ticket must be generated with EMG- prefix");
        }
        if (g.getSisaKuota("MOBIL") != 0) {
            throw new AssertionError("Quota mobil must be 0 after emergency entry");
        }

        System.out.println("GedungParkirTest: PASS");
    }
}
