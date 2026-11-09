package test;

import model.*;
import java.util.List;

/**
 * Unit Test Modul 11: Collection dan Pengelolaan Kumpulan Objek
 * Memverifikasi implementasi operasi tambah, cari, tampilkan, dan hapus
 * pada objek domain Kendaraan dan SlotParkir dalam GedungParkir,
 * serta memastikan validasi keunikan ID dan pertahanan enkapsulasi.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class CollectionManagementTest {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   UNIT TEST MODUL 11: COLLECTION MANAGEMENT SMART PARKIR                 ");
        System.out.println("   Pengujian List<Kendaraan> & List<SlotParkir> dengan Generics           ");
        System.out.println("==========================================================================");
        System.out.println();

        int passed = 0;
        int total = 7;

        GedungParkir gedung = new GedungParkir("SmartParkir Mal Galaksi", 5000, 3000, 2000, 1000);
        SlotParkir sA1 = new SlotParkir("A01", 1, "MOBIL");
        SlotParkir sM1 = new SlotParkir("M01", 1, "MOTOR");
        gedung.tambahSlot(sA1);
        gedung.tambahSlot(sM1);

        // TEST 1: Tambah Kendaraan Normal
        System.out.print("[TEST 1] Operasi tambahKendaraan (Generics & Collection add)... ");
        try {
            Kendaraan k1 = new Kendaraan("B 1234 ABC", "MOBIL", "Toyota Yaris");
            Kendaraan k2 = new Kendaraan("L 5678 XYZ", "MOTOR", "Honda Vario");
            gedung.tambahKendaraan(k1);
            gedung.tambahKendaraan(k2);
            if (gedung.getTotalKendaraanTerdaftar() == 2) {
                System.out.println("PASSED (2 kendaraan tersimpan dalam List<Kendaraan>)");
                passed++;
            } else {
                System.out.println("FAILED: Total kendaraan tidak sesuai.");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // TEST 2: Cari Kendaraan Berdasarkan Plat Nomor
        System.out.print("[TEST 2] Operasi cariKendaraan (Pencarian berbasis kode unik)... ");
        Kendaraan cari = gedung.cariKendaraan("B 1234 ABC");
        if (cari != null && cari.getMerk().equals("Toyota Yaris")) {
            System.out.println("PASSED (Kendaraan ditemukan: " + cari + ")");
            passed++;
        } else {
            System.out.println("FAILED: Kendaraan tidak ditemukan.");
        }

        // TEST 3: Tolak Kendaraan Duplikat
        System.out.print("[TEST 3] Pencegahan kode unik duplikat (Plat sama ditolak)... ");
        try {
            Kendaraan kDup = new Kendaraan("B 1234 ABC", "MOBIL", "Toyota Yaris Palsu");
            gedung.tambahKendaraan(kDup);
            System.out.println("FAILED: Seharusnya melempar exception plat duplikat!");
        } catch (IllegalArgumentException e) {
            System.out.println("PASSED (Ditolak: " + e.getMessage() + ")");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: Exception salah tipe: " + e.getMessage());
        }

        // TEST 4: Tampilkan Koleksi Kendaraan
        System.out.println("[TEST 4] Operasi tampilkanSemuaKendaraan (Iterasi for-each):");
        gedung.tampilkanSemuaKendaraan();
        System.out.println(" >> [TEST 4 PASSED] Koleksi berhasil diiterasi.");
        passed++;

        // TEST 5: Proteksi Hapus Kendaraan yang Sedang Parkir Aktif
        System.out.print("[TEST 5] Proteksi hapusKendaraan saat kendaraan aktif parkir... ");
        try {
            Kendaraan kAktif = gedung.cariKendaraan("B 1234 ABC");
            gedung.prosesMasuk(kAktif, 8); // Parkir di A01
            try {
                gedung.hapusKendaraan("B 1234 ABC");
                System.out.println("FAILED: Kendaraan aktif parkir seharusnya tidak boleh dihapus!");
            } catch (IllegalStateException e) {
                System.out.println("PASSED (Proteksi Integritas: " + e.getMessage() + ")");
                passed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // TEST 6: Hapus Kendaraan yang Tidak Sedang Parkir
        System.out.print("[TEST 6] Operasi hapusKendaraan pada kendaraan bebas... ");
        boolean removed = gedung.hapusKendaraan("L 5678 XYZ");
        boolean notFound = !gedung.hapusKendaraan("W 9999 NON"); // Tidak ada
        if (removed && notFound && gedung.getTotalKendaraanTerdaftar() == 1) {
            System.out.println("PASSED (L 5678 XYZ berhasil dihapus, total sisa: 1)");
            passed++;
        } else {
            System.out.println("FAILED: Penghapusan tidak sesuai kondisi.");
        }

        // TEST 7: Pertahanan Enkapsulasi Koleksi Internal (Unmodifiable List)
        System.out.print("[TEST 7] Pertahanan enkapsulasi (Cegah modifikasi liar dari luar)... ");
        try {
            List<Kendaraan> listLuar = gedung.getDaftarKendaraan();
            listLuar.add(new Kendaraan("D 9999 HCK", "MOBIL", "Hacker Car"));
            System.out.println("FAILED: Koleksi internal berhasil dimodifikasi dari luar!");
        } catch (UnsupportedOperationException e) {
            System.out.println("PASSED (Koleksi internal terlindungi via UnmodifiableList)");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        System.out.println();
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("HASIL PENGUJIAN: %d PASSED, %d FAILED (Total: %d Test Cases)%n", passed, total - passed, total);
        System.out.println("--------------------------------------------------------------------------");
        if (passed == total) {
            System.out.println(">> [VERIFIKASI SUKSES] Seluruh syarat Modul 11 terpenuhi secara sempurna!");
        }
    }
}
