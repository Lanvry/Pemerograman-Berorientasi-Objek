public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("   PRAKTIKUM MODUL 7 - ABSTRACT CLASS, ABSTRACT METHOD & INTERFACE       ");
        System.out.println("   SISTEM MANAJEMEN PERPUSTAKAAN (PROJECT BASED LEARNING + AGILE)        ");
        System.out.println("=========================================================================\n");

        // ---------------------------------------------------------------------
        // SKENARIO 1: MEMBUAT OBJECT SUBCLASS (Bagian F - Skenario 1)
        // ---------------------------------------------------------------------
        System.out.println("=== SKENARIO 1: MEMBUAT OBJECT SUBCLASS DARI ABSTRACT CLASS & INTERFACE ===");
        Mahasiswa mhs1 = new Mahasiswa("A001", "Arjuna Lanang Adiwarsana", "Jl. Trunojoyo No. 45, Sumenep", "3125522010", "D3 PJJ Teknik Informatika");
        Dosen dsn1 = new Dosen("A002", "Nirwana Haidar Hari, S.Pd., M.Kom.", "Jl. Raya Lenteng No. 88, Sumenep", "198801232024011001", "Teknik Informatika");
        Tendik tdk1 = new Tendik("A003", "Ahmad Fauzi, S.Kom.", "Jl. Jokotole No. 12, Sumenep", "199205152020121002", "Bagian Administrasi & Akademik");
        Buku b1 = new Buku("B001", "Pemrograman Berorientasi Objek", "Nirwana Haidar", 2024, "Rak A-03 (Informatika)");
        Buku b2 = new Buku("B002", "Struktur Data & Algoritma", "Budi Raharjo", 2022, "Rak B-01 (Algoritma)");

        System.out.println("[Sukses] Seluruh object subclass berhasil diinstansiasi:");
        System.out.println("1. Objek Mahasiswa : " + mhs1.getNama() + " (NRP: " + mhs1.getNrp() + ")");
        System.out.println("2. Objek Dosen     : " + dsn1.getNama() + " (NIP: " + dsn1.getNip() + ")");
        System.out.println("3. Objek Tendik    : " + tdk1.getNama() + " (Unit: " + tdk1.getUnitKerja() + ")");
        System.out.println("4. Objek Buku      : " + b1.getJudul() + " [" + b1.getKode() + "]\n");

        // Catatan: Pembuktian bahwa abstract class Anggota tidak dapat diinstansiasi langsung:
        // Anggota test = new Anggota("A000", "Anonim", "Sumenep"); // [COMPILE ERROR]: Anggota is abstract; cannot be instantiated

        // ---------------------------------------------------------------------
        // SKENARIO 2: MEMANGGIL ABSTRACT METHOD MELALUI REFERENCE SUPERCLASS (Bagian F - Skenario 2)
        // ---------------------------------------------------------------------
        System.out.println("=== SKENARIO 2: MEMANGGIL ABSTRACT METHOD VIA REFERENCE SUPERCLASS (DYNAMIC BINDING) ===");
        // Upcasting ke abstract class reference
        Anggota refAnggota1 = mhs1;
        Anggota refAnggota2 = dsn1;
        Anggota refAnggota3 = tdk1;

        System.out.println("--- Pemanggilan Abstract Method: tampilkanPeran() ---");
        System.out.print("refAnggota1 (Mahasiswa) -> ");
        refAnggota1.tampilkanPeran(); // Dynamic binding memanggil method Mahasiswa
        System.out.print("refAnggota2 (Dosen)     -> ");
        refAnggota2.tampilkanPeran(); // Dynamic binding memanggil method Dosen
        System.out.print("refAnggota3 (Tendik)    -> ");
        refAnggota3.tampilkanPeran(); // Dynamic binding memanggil method Tendik

        System.out.println("\n--- Pemanggilan Abstract Method: hitungDenda(3 Hari) & getMaksimalPinjam() ---");
        System.out.println("refAnggota1 -> Denda: Rp " + refAnggota1.hitungDenda(3) + " | Kuota Pinjam: " + refAnggota1.getMaksimalPinjam() + " Buku (Tarif Mahasiswa)");
        System.out.println("refAnggota2 -> Denda: Rp " + refAnggota2.hitungDenda(3) + " | Kuota Pinjam: " + refAnggota2.getMaksimalPinjam() + " Buku (Privilege Dosen)");
        System.out.println("refAnggota3 -> Denda: Rp " + refAnggota3.hitungDenda(3) + " | Kuota Pinjam: " + refAnggota3.getMaksimalPinjam() + " Buku (Tarif Tendik)\n");

        // ---------------------------------------------------------------------
        // SKENARIO 3: MEMANGGIL METHOD MELALUI REFERENCE INTERFACE (Bagian F - Skenario 3)
        // ---------------------------------------------------------------------
        System.out.println("=== SKENARIO 3: MEMANGGIL METHOD MELALUI REFERENCE INTERFACE (CROSS-HIERARCHY) ===");
        // Reference bertipe interface DapatDilacak menampung instance dari class dengan hierarki berbeda
        DapatDilacak pelacakMhs = mhs1; // Dari hierarki Anggota -> Mahasiswa
        DapatDilacak pelacakDsn = dsn1; // Dari hierarki Anggota -> Dosen
        DapatDilacak pelacakBuku = b1;  // Dari hierarki independen Buku

        System.out.println("[Sukses] Pemanggilan method getLokasi() melalui Reference Interface DapatDilacak:");
        System.out.println("1. pelacakMhs.getLokasi()  -> " + pelacakMhs.getLokasi());
        System.out.println("2. pelacakDsn.getLokasi()  -> " + pelacakDsn.getLokasi());
        System.out.println("3. pelacakBuku.getLokasi() -> " + pelacakBuku.getLokasi() + "\n");

        // ---------------------------------------------------------------------
        // SKENARIO 4: ARRAY/COLLECTION BERTIPE SUPERCLASS & INTERFACE (Bagian F - Skenario 4)
        // ---------------------------------------------------------------------
        System.out.println("=== SKENARIO 4: POLYMORPHIC COLLECTION (ARRAY OF ABSTRACT CLASS & INTERFACE) ===");
        
        System.out.println("--- Bagian 4A: Iterasi Polimorfik Array Anggota[] (Abstract Superclass) ---");
        Anggota[] daftarAnggota = {
            mhs1,
            dsn1,
            tdk1,
            new Mahasiswa("A004", "Siti Nurhaliza", "Jl. Panglegur No. 05", "3125522011", "D3 PJJ Teknik Informatika")
        };

        for (int i = 0; i < daftarAnggota.length; i++) {
            System.out.println(">> Anggota ke-" + (i + 1) + " [" + daftarAnggota[i].getClass().getSimpleName() + "]:");
            daftarAnggota[i].tampilkanData();
            System.out.println("   Simulasi Denda Terlambat 4 Hari: Rp " + daftarAnggota[i].hitungDenda(4));
            System.out.println();
        }

        System.out.println("--- Bagian 4B: Iterasi Polimorfik Array DapatDilacak[] (Interface Reference) ---");
        DapatDilacak[] daftarPelacakan = {
            mhs1,
            dsn1,
            tdk1,
            b1,
            b2
        };

        for (int i = 0; i < daftarPelacakan.length; i++) {
            System.out.println("Pelacak ke-" + (i + 1) + " [" + daftarPelacakan[i].getClass().getSimpleName() + "] -> Lokasi: " + daftarPelacakan[i].getLokasi());
        }
        System.out.println();

        // ---------------------------------------------------------------------
        // SKENARIO 5: INTEGRASI SISTEM PERPUSTAKAAN & VERIFIKASI FITUR P1-P6
        // ---------------------------------------------------------------------
        System.out.println("=== SKENARIO 5: INTEGRASI SISTEM & VERIFIKASI FITUR P1-P6 ===");
        // Method Overloading pada Anggota
        System.out.println("1. Uji Method Overloading Anggota.hitungDenda():");
        System.out.println("   - Standar 5 hari (Mahasiswa) : Rp " + mhs1.hitungDenda(5));
        System.out.println("   - Diskon 50% 5 hari (Mahasiswa): Rp " + mhs1.hitungDenda(5, 50.0));

        // Pengelolaan Agregasi Buku di Perpustakaan & Method Overloading cariBuku()
        Perpustakaan perpus = new Perpustakaan("Perpustakaan Terpadu PENS Sumenep", "Gedung A Kampus PSDKU");
        perpus.tambahBuku(b1);
        perpus.tambahBuku(b2);
        System.out.println("\n2. Uji Method Overloading Perpustakaan.cariBuku():");
        Buku cari1 = perpus.cariBuku("Pemrograman Berorientasi Objek");
        System.out.println("   - cariBuku(judul)    -> Ditemukan: " + (cari1 != null ? cari1.getJudul() : "Tidak"));
        System.out.print("   - cariBuku(kode, cetakDetail) -> ");
        perpus.cariBuku("B002", true);

        // Transaksi Peminjaman dengan Polimorfisme
        System.out.println("\n3. Uji Transaksi Peminjaman (Relasi Asosiasi Polimorfik):");
        Peminjaman pinjam1 = new Peminjaman("PJ-001", "2026-10-06", b1, mhs1, 7);
        Peminjaman pinjam2 = new Peminjaman("PJ-002", "2026-10-06", b2, dsn1, 14);

        System.out.println("--- Detail Transaksi 1 ---");
        pinjam1.tampilkanData();
        System.out.println("Total Denda (Keterlambatan 3 Hari): Rp " + pinjam1.hitungTotalDenda(3));
        System.out.println();

        System.out.println("--- Detail Transaksi 2 ---");
        pinjam2.tampilkanData();
        System.out.println("Total Denda (Keterlambatan 3 Hari): Rp " + pinjam2.hitungTotalDenda(3));

        System.out.println("\n=========================================================================");
        System.out.println("   PENGUJIAN MODUL 7 SELESAI (SELURUH DEFINITION OF DONE TERPENUHI)       ");
        System.out.println("=========================================================================");
    }
}
