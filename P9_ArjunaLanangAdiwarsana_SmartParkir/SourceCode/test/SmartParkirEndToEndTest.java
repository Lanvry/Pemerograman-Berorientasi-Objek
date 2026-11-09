package test;

import model.*;
import model.QRISService.MidtransQRISResult;

public class SmartParkirEndToEndTest {
    public static void main(String[] args) throws Exception {
        System.out.println("==========================================================");
        System.out.println("   RUNNING SMARTPARKIR 2.0 END-TO-END INTEGRATION TEST    ");
        System.out.println("==========================================================");

        GedungParkir mall = new GedungParkir("Mall Grand PENS Plaza", 5000, 3000, 2000, 1000);
        mall.tambahSlot(new SlotParkir("MBL-L1-01", 1, "MOBIL"));
        mall.tambahSlot(new SlotParkir("MBL-L1-02", 1, "MOBIL"));
        mall.tambahSlot(new SlotParkir("MBL-L2-01", 2, "MOBIL"));
        mall.tambahSlot(new SlotParkir("EV-L1-01", 1, "MOBIL_LISTRIK"));
        mall.tambahSlot(new SlotParkir("MTR-L1-01", 1, "MOTOR"));

        // 1. Initial Quota Verification
        assert mall.getSisaKuota("MOBIL") == 3 : "Initial mobil quota must be 3";
        assert mall.getSisaKuota("MOBIL_LISTRIK") == 1 : "Initial EV quota must be 1";
        assert mall.getSisaKuota("MOTOR") == 1 : "Initial motor quota must be 1";
        assert mall.getSisaKuotaLantai(1, "MOBIL") == 2 : "Lantai 1 mobil must be 2";
        assert mall.getSisaKuotaLantai(2, "MOBIL") == 1 : "Lantai 2 mobil must be 1";

        // 2. Ticketless Check-In (ANPR Camera)
        Kendaraan k1 = new Kendaraan("B 1234 ABC", "MOBIL", "Toyota Avanza");
        KarcisParkir t1 = mall.prosesMasukTicketless(k1, 8);
        assert t1 != null : "Ticket 1 must be issued";
        assert t1.getSlot().getIdSlot().equals("MBL-L1-01") : "Waterfall: Floor 1 must be assigned first";
        assert mall.getSisaKuotaLantai(1, "MOBIL") == 1 : "Lantai 1 quota must be 1";

        // 3. Anti-Duplicate Check-In
        KarcisParkir tDuplicate = mall.prosesMasukTicketless(new Kendaraan("B 1234 ABC", "MOBIL", "Toyota Avanza"), 9);
        assert tDuplicate == null : "Duplicate plate entry must be blocked";

        // 4. EV Check-In & Green Incentive (20% Discount)
        Kendaraan ev = new Kendaraan("L 5555 EV", "MOBIL_LISTRIK", "Hyundai Ioniq 5");
        KarcisParkir tEv = mall.prosesMasukTicketless(ev, 9);
        assert tEv != null : "EV ticket must be issued";
        assert tEv.getSlot().getIdSlot().equals("EV-L1-01") : "EV must occupy dedicated EV slot";
        
        // Calculate 3-hour parking for EV: 5000 + 3000 + 3000 = 11000 * 0.80 = 8800
        double tagihanEv = tEv.hitungTarif(12, mall.getTarifJamPertamaMobil(), mall.getTarifPerJamMobil());
        assert tagihanEv == 8800.0 : "EV must receive exactly 20% discount (got " + tagihanEv + ")";

        // 5. Emergency QR Ticket Fallback (Unreadable Plate)
        KarcisParkir tEmergency = mall.prosesMasukEmergency("MOBIL", 10);
        assert tEmergency != null : "Emergency ticket must be generated";
        assert tEmergency.getNomorTiket().startsWith("EMG-") : "Ticket number must start with EMG-";

        // 6. Midtrans QRIS Charge & Settlement Simulation
        MidtransQRISResult midtrans = QRISService.createMidtransTransaction(tEv.getNomorTiket(), tagihanEv);
        assert midtrans != null : "Midtrans result must exist";
        assert midtrans.qrString != null && !midtrans.qrString.isEmpty() : "QR String must be generated";
        
        // Simulate customer scanned QR and paid via e-Wallet
        QRISService.simulasiBayar(tEv.getNomorTiket());
        String status = QRISService.checkTransactionStatus(tEv.getNomorTiket());
        assert "settlement".equalsIgnoreCase(status) : "Status must be settlement after payment";

        // 7. Check-Out & Slot Release
        boolean okExit = mall.prosesKeluar(tEv, 12, "QRIS_MIDTRANS", tagihanEv);
        assert okExit : "Checkout must succeed";
        assert tEv.isSudahDibayar() : "Ticket must be marked as paid";
        assert mall.getSisaKuota("MOBIL_LISTRIK") == 1 : "EV quota must return to 1 after vehicle exits";

        System.out.println("\n>>> ALL TESTS PASSED: SmartParkir 2.0 lifecycle verified successfully!");
    }
}
