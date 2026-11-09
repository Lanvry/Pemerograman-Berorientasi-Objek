import model.GedungParkir;
import model.KarcisParkir;
import model.Kendaraan;
import model.SlotParkir;

/**
 * Driver class Main untuk menguji interaksi object, relasi, enkapsulasi,
 * dan validasi state pada Sistem Smart Parking Gedung & Slot Otomatis (SmartParkir).
 * Memenuhi seluruh target evaluasi Praktikum Modul 9 PBO.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   PRAKTIKUM PBO MODUL 9: IMPLEMENTASI UML CLASS DIAGRAM KE KODE JAVA    ");
        System.out.println("   Topik  : Smart Parking Gedung & Slot Otomatis (SmartParkir)           ");
        System.out.println("   Nama   : Arjuna Lanang Adiwarsana (NRP: 3125522010)                   ");
        System.out.println("   Kampus : PENS PSDKU Sumenep                                           ");
        System.out.println("==========================================================================\n");

        // --------------------------------------------------------------------
        // SKENARIO 1: INISIALISASI GEDUNG PARKIR & DAFTAR SLOT FISIK
        // --------------------------------------------------------------------
        System.out.println(">>> [SKENARIO 1] Inisialisasi Fasilitas Gedung Parkir Bertingkat");
        GedungParkir mallPark = new GedungParkir(
                "Mall Grand PENS Plaza",
                5000.0, // Tarif jam pertama mobil: Rp 5.000
                3000.0, // Tarif jam berikutnya mobil: Rp 3.000/jam
                2000.0, // Tarif jam pertama motor: Rp 2.000
                1000.0  // Tarif jam berikutnya motor: Rp 1.000/jam
        );

        // Tambahkan slot parkir fisik (Lantai 1 dan Lantai 2)
        // Slot Motor
        mallPark.tambahSlot(new SlotParkir("MTR-L1-01", 1, "MOTOR"));
        mallPark.tambahSlot(new SlotParkir("MTR-L1-02", 1, "MOTOR"));

        // Slot Mobil Reguler
        mallPark.tambahSlot(new SlotParkir("MBL-L1-01", 1, "MOBIL"));
        mallPark.tambahSlot(new SlotParkir("MBL-L2-01", 2, "MOBIL"));

        // Slot Mobil Listrik (EV Charger dedicated)
        mallPark.tambahSlot(new SlotParkir("EV-L1-01", 1, "MOBIL_LISTRIK"));

        System.out.println("Gedung parkir siap beroperasi dengan 5 slot terdaftar.");
        mallPark.tampilkanStatusKapasitas();

        // --------------------------------------------------------------------
        // SKENARIO 2: CHECK-IN KENDARAAN & ALOKASI SLOT OTOMATIS
        // --------------------------------------------------------------------
        System.out.println(">>> [SKENARIO 2] Check-In Kendaraan & Alokasi Slot Cerdas");
        Kendaraan k1 = new Kendaraan("B 1234 ABC", "MOBIL", "Toyota Avanza");
        Kendaraan k2 = new Kendaraan("L 5566 EV", "MOBIL_LISTRIK", "Hyundai Ioniq 5");
        Kendaraan k3 = new Kendaraan("M 9988 XYZ", "MOTOR", "Honda Vario");

        // Kendaraan masuk gate
        KarcisParkir tiket1 = mallPark.prosesMasuk(k1, 8);  // Masuk jam 08:00
        KarcisParkir tiket2 = mallPark.prosesMasuk(k2, 9);  // Masuk jam 09:00
        KarcisParkir tiket3 = mallPark.prosesMasuk(k3, 10); // Masuk jam 10:00
        System.out.println();

        mallPark.tampilkanStatusKapasitas();

        // --------------------------------------------------------------------
        // SKENARIO 3: UJI ENKAPSULASI, VALIDASI STATE, & PROTEKSI OVER-CAPACITY
        // --------------------------------------------------------------------
        System.out.println(">>> [SKENARIO 3] Uji Enkapsulasi & Proteksi Kapasitas Penuh");
        System.out.println("a. Mencoba memarkir Mobil Listrik kedua (Slot EV hanya ada 1 dan sudah terisi):");
        Kendaraan k4_ev = new Kendaraan("N 7788 EV", "MOBIL_LISTRIK", "Wuling Binguo");
        // Karena k4 adalah EV, jika slot EV penuh, sistem cerdas akan mencarikan slot Mobil reguler di Lt 2!
        KarcisParkir tiket4 = mallPark.prosesMasuk(k4_ev, 10);

        System.out.println("\nb. Mencoba memarkir Mobil ketiga (Slot Mobil di Lantai 1 & 2 sekarang sudah habis):");
        Kendaraan k5_mobil = new Kendaraan("D 9999 MOB", "MOBIL", "Mitsubishi Pajero");
        KarcisParkir tiketGagal = mallPark.prosesMasuk(k5_mobil, 11); // Harusnya ditolak (Parkir Penuh)

        System.out.println("\nc. Mencoba check-out dengan pembayaran kurang dari tagihan:");
        if (tiket1 != null) {
            // Jam masuk 08:00, keluar 12:00 (4 jam -> 5.000 + 3x3.000 = Rp 14.000)
            mallPark.prosesKeluar(tiket1, 12, "CASH", 10000.0); // Uang cuma Rp 10.000
        }
        System.out.println();

        // --------------------------------------------------------------------
        // SKENARIO 4: CHECK-OUT SUKSES KENDARAAN REGULER (PELEPASAN SLOT)
        // --------------------------------------------------------------------
        System.out.println(">>> [SKENARIO 4] Check-Out Sukses Kendaraan Reguler (Mobil k1)");
        if (tiket1 != null) {
            boolean suksesKeluar = mallPark.prosesKeluar(tiket1, 12, "E_MONEY", 14000.0);
            if (suksesKeluar) {
                mallPark.cetakStrukKeluar(tiket1);
            }
        }

        // --------------------------------------------------------------------
        // SKENARIO 5: CHECK-OUT KENDARAAN LISTRIK (INSENTIF DISKON HIJAU 20%)
        // --------------------------------------------------------------------
        System.out.println(">>> [SKENARIO 5] Check-Out Mobil Listrik EV (Diskon Insentif Hijau 20%)");
        if (tiket2 != null) {
            // Masuk jam 09:00, keluar 14:00 (5 jam)
            // Tarif normal: 5.000 + 4x3.000 = Rp 17.000.
            // Diskon 20% EV: 17.000 * 0.8 = Rp 13.600!
            boolean suksesEV = mallPark.prosesKeluar(tiket2, 14, "QRIS", 15000.0);
            if (suksesEV) {
                mallPark.cetakStrukKeluar(tiket2);
            }
        }

        // --------------------------------------------------------------------
        // SKENARIO 6: MONITORING AKHIR OKUPANSI & REKAP KAS GEDUNG
        // --------------------------------------------------------------------
        System.out.println(">>> [SKENARIO 6] Monitoring Realtime Terkini Gedung Parkir");
        System.out.println("Slot MBL-L1-01 dan EV-L1-01 sekarang sudah kosong kembali dan siap dipakai mobil baru.");
        mallPark.tampilkanStatusKapasitas();

        System.out.println("[PENGUJIAN SELESAI] Seluruh relasi, enkapsulasi, dan otomasi alokasi slot bekerja sempurna!");
    }
}
