public class Main {
    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("       UJIAN TENGAH SEMESTER (UTS) - PRAKTIKUM PBO SEMESTER GENAP       ");
        System.out.println("                 SISTEM MANAJEMEN RENTAL KENDARAAN                      ");
        System.out.println("========================================================================");
        System.out.println("Mahasiswa : Arjuna Lanang Adiwarsana");
        System.out.println("NRP       : 3125522010");
        System.out.println("Kelas     : 3D3 IT-PSDKU A\n");

        // 1. Instansiasi Objek Kendaraan (Minimal 3 objek dari turunan abstract class)
        Mobil mobilInnova = new Mobil("L 1234 ABC", "Toyota", "Innova Reborn", 2022, 500_000, 7, "Otomatis");
        Mobil mobilHiAce = new Mobil("W 5678 XYZ", "Toyota", "HiAce Commuter", 2021, 900_000, 14, "Manual");
        mobilHiAce.setDenganSupir(true); // Memanfaatkan opsi layanan supir

        Motor motorPcx = new Motor("M 4321 DEF", "Honda", "PCX 160", 2023, 120_000, "Matic", 2);
        Motor motorNinja = new Motor("M 9876 GHI", "Kawasaki", "Ninja ZX-25R", 2024, 250_000, "Sport", 1);

        // Instansiasi Objek Pelanggan
        Pelanggan p1 = new Pelanggan("PLG-001", "Arjuna Lanang", "081234567890", "VIP");
        Pelanggan p2 = new Pelanggan("PLG-002", "Budi Santoso", "082233445566", "GOLD");
        Pelanggan p3 = new Pelanggan("PLG-003", "Citra Lestari", "085566778899", "REGULER");

        // 2. Demonstrasi Polymorphism & Dynamic Method Dispatch
        System.out.println("------------------------------------------------------------------------");
        System.out.println("SKENARIO 1: DEMO POLYMORPHISM & DYNAMIC METHOD DISPATCH");
        System.out.println("------------------------------------------------------------------------");
        Kendaraan[] daftarArmada = { mobilInnova, mobilHiAce, motorPcx, motorNinja };

        int estimasiHari = 3;
        System.out.printf("Simulasi Perhitungan Biaya Sewa Armada selama %d Hari:%n", estimasiHari);
        for (Kendaraan k : daftarArmada) {
            k.tampilkanSpesifikasi();
            // Pemanggilan hitungBiayaSewa dievaluasi secara dinamis sesuai tipe objek riil
            double estimasiBiaya = k.hitungBiayaSewa(estimasiHari);
            System.out.printf("  -> Total Biaya (%d Hari): Rp %,.0f%n%n", estimasiHari, estimasiBiaya);
        }

        // 3. Demonstrasi Relasi Antar-Objek (TransaksiRental menghubungkan Pelanggan & Kendaraan)
        System.out.println("------------------------------------------------------------------------");
        System.out.println("SKENARIO 2: TRANSAKSI PENYEWAAN NORMAL (DENGAN BENEFIT MEMBER VIP)");
        System.out.println("------------------------------------------------------------------------");
        TransaksiRental tx1 = new TransaksiRental("TRX-2026-001", p1, mobilInnova, 3);
        System.out.println("[Berhasil] Transaksi berhasil dibuat.");
        System.out.printf("Status Mobil Innova saat ini: %s%n%n", (mobilInnova.isTersedia() ? "Tersedia" : "Sedang Disewa"));

        // 4. Demonstrasi Validasi Business Rule 1 & 2
        System.out.println("------------------------------------------------------------------------");
        System.out.println("SKENARIO 3: VALIDASI BUSINESS RULES");
        System.out.println("------------------------------------------------------------------------");

        // Validasi Rule 1: Durasi sewa tidak valid (< 1 hari)
        System.out.println("1. Uji Coba Input Durasi Tidak Valid (durasi = 0 hari):");
        try {
            TransaksiRental txGagalDurasi = new TransaksiRental("TRX-FAIL-01", p3, motorNinja, 0);
        } catch (IllegalArgumentException e) {
            System.out.printf("   [VALIDASI TERPENUHI] Gagal membuat transaksi: %s%n%n", e.getMessage());
        }

        // Validasi Rule 2: Kendaraan sedang disewa tidak boleh disewa ulang
        System.out.println("2. Uji Coba Menyewa Kendaraan yang Sedang Disewa (Innova Reborn):");
        try {
            TransaksiRental txGagalKetersediaan = new TransaksiRental("TRX-FAIL-02", p2, mobilInnova, 2);
        } catch (IllegalStateException e) {
            System.out.printf("   [VALIDASI TERPENUHI] Gagal membuat transaksi: %s%n%n", e.getMessage());
        }

        // 5. Demonstrasi Transaksi Kedua untuk Motor
        System.out.println("------------------------------------------------------------------------");
        System.out.println("SKENARIO 4: TRANSAKSI PENYEWAAN MOTOR DENGAN FASILITAS HELM EKSTRA");
        System.out.println("------------------------------------------------------------------------");
        TransaksiRental tx2 = new TransaksiRental("TRX-2026-002", p2, motorPcx, 2);
        System.out.println("[Berhasil] Transaksi sewa motor berhasil dibuat.");
        System.out.printf("Status Motor PCX saat ini: %s%n%n", (motorPcx.isTersedia() ? "Tersedia" : "Sedang Disewa"));

        // 6. Demonstrasi Unsur Kreativitas (Pengembalian, Denda Keterlambatan, & Cetak Kuitansi)
        System.out.println("------------------------------------------------------------------------");
        System.out.println("SKENARIO 5: PENGEMBALIAN TEPAT WAKTU VS PENGEMBALIAN TERLAMBAT (DENDA)");
        System.out.println("------------------------------------------------------------------------");

        // Pengembalian Tepat Waktu (Mobil Innova)
        System.out.println("A. Proses Pengembalian Transaksi 1 (Tepat Waktu - 0 hari keterlambatan):");
        tx1.prosesPengembalian(0);
        tx1.cetakKuitansi();
        System.out.println();

        // Pengembalian Terlambat 2 Hari (Motor PCX)
        System.out.println("B. Proses Pengembalian Transaksi 2 (Terlambat 2 Hari - Kena Denda Dinamis):");
        tx2.prosesPengembalian(2);
        tx2.cetakKuitansi();
        System.out.println();

        // 7. Verifikasi Status Akhir Kendaraan
        System.out.println("------------------------------------------------------------------------");
        System.out.println("SKENARIO 6: VERIFIKASI RE-INTEGRASI STATUS ARMADA PASCA PENGEMBALIAN");
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("Status Mobil Innova setelah kembali : %s%n", (mobilInnova.isTersedia() ? "Tersedia (Siap Disewa)" : "Sedang Disewa"));
        System.out.printf("Status Motor PCX setelah kembali    : %s%n", (motorPcx.isTersedia() ? "Tersedia (Siap Disewa)" : "Sedang Disewa"));
        System.out.println("========================================================================");
    }
}
