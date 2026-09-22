public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   PRAKTIKUM MODUL 4 - RELASI ANTAR OBJEK        ");
        System.out.println("   SISTEM MANAJEMEN PERPUSTAKAAN                 ");
        System.out.println("=================================================\n");

        // 1. Uji Pembuatan Objek (Test 1)
        System.out.println("=== 1. UJI PEMBUATAN OBJEK (TEST 1) ===");
        Buku buku1 = new Buku("B001", "Pemrograman Java", "Nirwana Haidar", 2024);
        Buku buku2 = new Buku("B002", "Struktur Data & Algoritma", "Budi Raharjo", 2022);
        Buku buku3 = new Buku("B003", "Rekayasa Perangkat Lunak", "Ian Sommerville", 2023);

        Anggota anggota1 = new Anggota("A001", "Arjuna Lanang Adiwarsana", "Jl. Trunojoyo No. 45, Sumenep");
        Anggota anggota2 = new Anggota("A002", "Siti Aminah", "Jl. KH. Agus Salim No. 12, Sumenep");

        Perpustakaan perpus = new Perpustakaan("Perpustakaan PENS Sumenep", "Gedung A Lantai 2 Kampus PSDKU");
        System.out.println("[Sukses] Seluruh objek berhasil diinisialisasi.\n");

        // 2. Uji Interaksi Antar Objek (Test 2)
        System.out.println("=== 2. UJI INTERAKSI ANTAR OBJEK (TEST 2) ===");
        
        // A. Pengujian Komposisi (KartuAnggota di dalam Anggota)
        System.out.println("--- Cek Data Anggota & Kartu (Komposisi) ---");
        anggota1.tampilkanData();
        System.out.println();
        anggota2.tampilkanData();
        System.out.println();

        // B. Pengujian Agregasi (Buku dimasukkan ke Perpustakaan)
        System.out.println("--- Menambahkan Buku ke Perpustakaan (Agregasi) ---");
        perpus.tambahBuku(buku1);
        perpus.tambahBuku(buku2);
        perpus.tambahBuku(buku3);
        System.out.println();

        // C. Pengujian Asosiasi (Peminjaman menghubungkan Anggota dan Buku)
        System.out.println("--- Membuat Transaksi Peminjaman (Asosiasi) ---");
        Peminjaman pinjam1 = new Peminjaman("PJ-001", "2026-09-22", buku1, anggota1, 7);
        Peminjaman pinjam2 = new Peminjaman("PJ-002", "2026-09-23", buku2, anggota2, 14);
        System.out.println("[Sukses] Transaksi peminjaman berhasil dibuat.\n");

        // 3. Uji Penggunaan Data Objek Lain via Method (Test 3)
        System.out.println("=== 3. UJI PENGGUNAAN DATA OBJEK LAIN (TEST 3) ===");
        perpus.tampilkanDaftarBuku();
        System.out.println();

        System.out.println("--- Detail Transaksi Peminjaman 1 ---");
        pinjam1.tampilkanData();
        System.out.println();

        System.out.println("--- Detail Transaksi Peminjaman 2 ---");
        pinjam2.tampilkanData();
        System.out.println();

        // Verifikasi bahwa objek Buku tetap berdiri sendiri di luar Perpustakaan (Agregasi)
        System.out.println("--- Bukti Objek Buku Tetap Berdiri Sendiri (Agregasi) ---");
        System.out.println("Judul buku 1 via objek asli : " + buku1.getJudul());
        System.out.println("Penulis buku 1              : " + buku1.getPenulis());
        System.out.println("(Objek Buku tetap dapat diakses mandiri meskipun terdaftar di Perpustakaan)\n");

        System.out.println("=================================================");
        System.out.println("   PENGUJIAN MODUL 4 SELESAI                     ");
        System.out.println("=================================================");
    }
}
