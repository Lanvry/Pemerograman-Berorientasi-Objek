public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("   PRAKTIKUM MODUL 6 - POLYMORPHISM, OVERRIDING & DYNAMIC BINDING        ");
        System.out.println("   SISTEM MANAJEMEN PERPUSTAKAAN                                         ");
        System.out.println("=========================================================================\n");

        // ---------------------------------------------------------------------
        // 1. UJI UPCASTING & POLYMORPHIC REFERENCE (Bagian D)
        // ---------------------------------------------------------------------
        System.out.println("=== 1. UJI UPCASTING & POLYMORPHIC REFERENCE ===");
        // Reference bertipe superclass (Anggota), tetapi menampung objek subclass aktual
        Anggota refMhs = new Mahasiswa("A001", "Arjuna Lanang Adiwarsana", "Jl. Trunojoyo No. 45, Sumenep", "3125522010", "D3 PJJ Teknik Informatika");
        Anggota refDsn = new Dosen("A002", "Nirwana Haidar Hari, S.Pd., M.Kom.", "Jl. Raya Lenteng No. 88, Sumenep", "198801232024011001", "Teknik Informatika");
        Anggota refTdk = new Tendik("A003", "Ahmad Fauzi, S.Kom.", "Jl. Jokotole No. 12, Sumenep", "199205152020121002", "Bagian Administrasi & Akademik");

        System.out.println("[Sukses] Tiga objek subclass berhasil di-upcast ke tipe reference superclass 'Anggota'.");
        System.out.println("refMhs (Tipe Reference: Anggota) -> Objek Aktual: " + refMhs.getClass().getSimpleName());
        System.out.println("refDsn (Tipe Reference: Anggota) -> Objek Aktual: " + refDsn.getClass().getSimpleName());
        System.out.println("refTdk (Tipe Reference: Anggota) -> Objek Aktual: " + refTdk.getClass().getSimpleName() + "\n");

        // ---------------------------------------------------------------------
        // 2. UJI METHOD OVERRIDING & DYNAMIC BINDING (Bagian B & F)
        // ---------------------------------------------------------------------
        System.out.println("=== 2. UJI METHOD OVERRIDING & DYNAMIC BINDING ===");
        System.out.println("--- Pemanggilan method tampilkanPeran() melalui Superclass Reference ---");
        System.out.print("refMhs.tampilkanPeran() -> ");
        refMhs.tampilkanPeran(); // Mengeksekusi versi Mahasiswa secara dinamis saat runtime
        System.out.print("refDsn.tampilkanPeran() -> ");
        refDsn.tampilkanPeran(); // Mengeksekusi versi Dosen secara dinamis saat runtime
        System.out.print("refTdk.tampilkanPeran() -> ");
        refTdk.tampilkanPeran(); // Mengeksekusi versi Tendik secara dinamis saat runtime
        System.out.println();

        System.out.println("--- Pemanggilan method hitungDenda(3 Hari Terlambat) via Dynamic Binding ---");
        System.out.println("Denda refMhs (Mahasiswa) : Rp " + refMhs.hitungDenda(3) + " (Tarif Mhs: Rp 500/hari)");
        System.out.println("Denda refDsn (Dosen)     : Rp " + refDsn.hitungDenda(3) + " (Privilege Bebas Denda)");
        System.out.println("Denda refTdk (Tendik)    : Rp " + refTdk.hitungDenda(3) + " (Tarif Tendik: Rp 750/hari)\n");

        // ---------------------------------------------------------------------
        // 3. UJI METHOD OVERLOADING (Bagian C)
        // ---------------------------------------------------------------------
        System.out.println("=== 3. UJI METHOD OVERLOADING (COMPILE-TIME POLYMORPHISM) ===");
        System.out.println("--- Overloading pada Anggota: hitungDenda() ---");
        int dendaStandarMhs = refMhs.hitungDenda(4); // Parameter (int)
        int dendaDiskonMhs = refMhs.hitungDenda(4, 50.0); // Parameter (int, double)
        System.out.println("1. hitungDenda(4 hari)           -> Rp " + dendaStandarMhs);
        System.out.println("2. hitungDenda(4 hari, diskon 50%) -> Rp " + dendaDiskonMhs);

        Perpustakaan perpus = new Perpustakaan("Perpustakaan Terpadu PENS Sumenep", "Gedung A Kampus PSDKU");
        Buku b1 = new Buku("B001", "Pemrograman Berorientasi Objek", "Nirwana Haidar", 2024);
        Buku b2 = new Buku("B002", "Struktur Data & Algoritma", "Budi Raharjo", 2022);
        perpus.tambahBuku(b1);
        perpus.tambahBuku(b2);

        System.out.println("\n--- Overloading pada Perpustakaan: cariBuku() ---");
        Buku hasil1 = perpus.cariBuku("Pemrograman Berorientasi Objek"); // cariBuku(String judul)
        System.out.println("1. cariBuku(String judul)     -> Ditemukan: " + (hasil1 != null ? hasil1.getJudul() : "Tidak"));
        System.out.print("2. cariBuku(String, boolean)  -> ");
        Buku hasil2 = perpus.cariBuku("B002", true); // cariBuku(String kode, boolean cetakDetail)
        System.out.println();

        // ---------------------------------------------------------------------
        // 4. UJI POLYMORPHIC COLLECTION (Bagian E)
        // ---------------------------------------------------------------------
        System.out.println("=== 4. UJI POLYMORPHIC COLLECTION (ARRAY OF SUPERCLASS) ===");
        // Menyimpan berbagai variasi subclass ke dalam array ber-tipe superclass Anggota
        Anggota[] koleksiAnggota = {
            refMhs,
            refDsn,
            refTdk,
            new Mahasiswa("A004", "Siti Nurhaliza", "Jl. Panglegur No. 05", "3125522011", "D3 PJJ Teknik Informatika")
        };

        System.out.println("Melakukan iterasi polimorfik pada " + koleksiAnggota.length + " elemen array Anggota[]:\n");
        int index = 1;
        for (Anggota a : koleksiAnggota) {
            System.out.println("---------------------------------------------------------");
            System.out.println("Elemen ke-" + index + " [Tipe Objek: " + a.getClass().getSimpleName() + "]");
            System.out.println("---------------------------------------------------------");
            a.tampilkanData(); // Dynamic binding memanggil method implementasi masing-masing subclass
            System.out.println("Simulasi Denda Terlambat 5 Hari : Rp " + a.hitungDenda(5));
            System.out.println();
            index++;
        }

        // ---------------------------------------------------------------------
        // 5. UJI INTEGRASI TRANSAKSI PEMINJAMAN DENGAN RELASI POLYMORPHIC (P4 + P6)
        // ---------------------------------------------------------------------
        System.out.println("=== 5. INTEGRASI TRANSAKSI PEMINJAMAN (RELASI POLYMORPHIC) ===");
        Peminjaman pinjam1 = new Peminjaman("PJ-001", "2026-10-04", b1, refMhs, 7);
        Peminjaman pinjam2 = new Peminjaman("PJ-002", "2026-10-04", b2, refDsn, 14);

        System.out.println("--- Data Transaksi Peminjaman 1 ---");
        pinjam1.tampilkanData();
        System.out.println("Denda Keterlambatan (jika terlambat 2 hari): Rp " + pinjam1.hitungTotalDenda(2));
        System.out.println();

        System.out.println("--- Data Transaksi Peminjaman 2 ---");
        pinjam2.tampilkanData();
        System.out.println("Denda Keterlambatan (jika terlambat 2 hari): Rp " + pinjam2.hitungTotalDenda(2));
        System.out.println();

        System.out.println("=========================================================================");
        System.out.println("   PENGUJIAN MODUL 6 SELESAI (SELURUH REQUIREMENT BERHASIL)              ");
        System.out.println("=========================================================================");
    }
}
