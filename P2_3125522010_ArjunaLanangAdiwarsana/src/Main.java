public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   P2 - IMPLEMENTASI CLASS, OBJECT, ATTRIBUTE,  ");
        System.out.println("           METHOD, DAN CONSTRUCTOR               ");
        System.out.println("             SISTEM PERPUSTAKAAN                 ");
        System.out.println("=================================================\n");

        // 1. Inisialisasi Objek Menggunakan Constructor (Minimal 2 Objek per Class)
        System.out.println("=== 1. PEMBUATAN OBJEK (CONSTRUCTOR) ===");
        Buku buku1 = new Buku("B001", "Pemrograman Java Dasar", "Nirwana Haidar");
        Buku buku2 = new Buku("B002", "Struktur Data & Algoritma", "Budi Raharjo");

        Anggota anggota1 = new Anggota("A001", "Arjuna Lanang", "Jl. Sumenep No. 10");
        Anggota anggota2 = new Anggota("A002", "Siti Aminah", "Jl. Pemuda No. 45");

        Peminjaman peminjaman1 = new Peminjaman("2026-09-01", buku1, anggota1);
        Peminjaman peminjaman2 = new Peminjaman("2026-09-05", buku2, anggota2);

        System.out.println("[SUCCESS] Minimal 2 Objek per class berhasil dibuat.\n");

        // 2. Pengujian Method Tanpa Parameter (Display Data)
        System.out.println("=== 2. PENGUJIAN METHOD TANPA PARAMETER ===");
        buku1.tampilkanData();
        System.out.println();
        buku2.tampilkanData();
        System.out.println();
        anggota1.tampilkanData();
        System.out.println();
        anggota2.tampilkanData();
        System.out.println();
        peminjaman1.tampilkanData();
        System.out.println();

        // 3. Pengujian Method dengan Parameter
        System.out.println("=== 3. PENGUJIAN METHOD DENGAN PARAMETER ===");
        buku1.ubahJudul("Pemrograman Java Lanjut & Framework");
        anggota2.ubahAlamat("Jl. Merdeka No. 88, Surabaya");
        peminjaman1.ubahTanggal("2026-09-02");
        System.out.println();

        // 4. Pengujian Method dengan Return Value
        System.out.println("=== 4. PENGUJIAN METHOD DENGAN RETURN VALUE ===");
        String judulBuku1 = buku1.getJudul();
        System.out.println("Judul Buku 1 (via getJudul())    : " + judulBuku1);

        String namaAnggota2 = anggota2.getNama();
        System.out.println("Nama Anggota 2 (via getNama())   : " + namaAnggota2);

        String tglPinjam1 = peminjaman1.getTanggal();
        System.out.println("Tanggal Pinjam 1 (via getTanggal()): " + tglPinjam1);
        System.out.println();

        // 5. Ringkasan Data Akhir Setelah Perubahan
        System.out.println("=== 5. RINGKASAN DATA AKHIR PEMINJAMAN ===");
        peminjaman1.tampilkanData();
        System.out.println();
        peminjaman2.tampilkanData();
        System.out.println();

        System.out.println("=================================================");
        System.out.println("       PENGUJIAN MODUL 2 SELESAI DENGAN SUKSES!  ");
        System.out.println("=================================================");
    }
}
