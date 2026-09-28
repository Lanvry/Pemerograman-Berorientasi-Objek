public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   PRAKTIKUM MODUL 5 - INHERITANCE & SUBCLASS    ");
        System.out.println("   SISTEM MANAJEMEN PERPUSTAKAAN                 ");
        System.out.println("=================================================\n");

        // Inisialisasi objek subclass dan eksekusi constructor superclass
        System.out.println("=== 1. UJI INSTANSIASI SUBCLASS & SUPER CONSTRUCTOR ===");
        Mahasiswa mhs = new Mahasiswa(
            "A001",
            "Arjuna Lanang Adiwarsana",
            "Jl. Trunojoyo No. 45, Sumenep",
            "3125522010",
            "D3 PJJ Teknik Informatika"
        );

        Dosen dosen = new Dosen(
            "A002",
            "Nirwana Haidar Hari, S.Pd., M.Kom.",
            "Jl. Raya Lenteng No. 88, Sumenep",
            "198801232024011001",
            "Teknik Informatika"
        );
        System.out.println("[Sukses] Objek Mahasiswa dan Dosen berhasil diinstansiasi via super().\n");

        // Akses member superclass yang diwarisi subclass
        System.out.println("=== 2. UJI AKSES METHOD SUPERCLASS (INHERITANCE) ===");
        System.out.println("--- Akses Data Mahasiswa via Method Superclass ---");
        System.out.println("Nama (Superclass)   : " + mhs.getNama());
        System.out.println("Alamat (Superclass) : " + mhs.getAlamat());
        System.out.println("No. Kartu (Super)   : " + mhs.getKartuAnggota().getNomorKartu());
        System.out.println("NRP (Subclass)      : " + mhs.getNrp());
        System.out.println("Prodi (Subclass)    : " + mhs.getProdi());
        System.out.println();

        System.out.println("--- Akses Data Dosen via Method Superclass ---");
        System.out.println("Nama (Superclass)   : " + dosen.getNama());
        System.out.println("Alamat (Superclass) : " + dosen.getAlamat());
        System.out.println("No. Kartu (Super)   : " + dosen.getKartuAnggota().getNomorKartu());
        System.out.println("NIP (Subclass)      : " + dosen.getNip());
        System.out.println("Departemen (Sub)    : " + dosen.getDepartemen());
        System.out.println();

        // Tampilkan seluruh field melalui method overriding
        System.out.println("=== 3. DETAIL LENGKAP OBJEK SUBCLASS ===");
        System.out.println("--- Data Mahasiswa ---");
        mhs.tampilkanData();
        System.out.println();

        System.out.println("--- Data Dosen ---");
        dosen.tampilkanData();
        System.out.println();

        // Verifikasi integrasi relasi P4 dengan objek subclass
        System.out.println("=== 4. INTEGRASI RELASI P4 DENGAN SUBCLASS ANGGOTA ===");
        Buku buku1 = new Buku("B001", "Pemrograman Berorientasi Objek", "Nirwana Haidar", 2024);
        Buku buku2 = new Buku("B002", "Struktur Data & Algoritma", "Budi Raharjo", 2022);
        
        Perpustakaan perpus = new Perpustakaan("Perpustakaan PENS Sumenep", "Gedung A Kampus PSDKU");
        perpus.tambahBuku(buku1);
        perpus.tambahBuku(buku2);
        System.out.println();

        // Mahasiswa dan Dosen diterima parameter Anggota karena memenuhi relasi is-a
        Peminjaman pinjam1 = new Peminjaman("PJ-001", "2026-09-28", buku1, mhs, 7);
        Peminjaman pinjam2 = new Peminjaman("PJ-002", "2026-09-28", buku2, dosen, 14);

        System.out.println("--- Transaksi Peminjaman 1 (Mahasiswa is-a Anggota) ---");
        pinjam1.tampilkanData();
        System.out.println();

        System.out.println("--- Transaksi Peminjaman 2 (Dosen is-a Anggota) ---");
        pinjam2.tampilkanData();
        System.out.println();

        System.out.println("=================================================");
        System.out.println("   PENGUJIAN MODUL 5 SELESAI (SUKSES)           ");
        System.out.println("=================================================");
    }
}
