public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   P3 - ENCAPSULATION, ACCESS MODIFIER,        ");
        System.out.println("       GETTER-SETTER, DAN VALIDASI DATA        ");
        System.out.println("             SISTEM PERPUSTAKAAN                 ");
        System.out.println("=================================================\n");

        // 1. TEST VALID: PEMBUATAN OBJEK DENGAN CONSTRUCTOR BER-VALIDASI
        System.out.println("=== 1. TEST VALID: PEMBUATAN OBJEK (CONSTRUCTOR) ===");
        Buku buku1 = new Buku("B001", "Pemrograman Java Dasar", "Nirwana Haidar", 2024);
        Buku buku2 = new Buku("B002", "Struktur Data & Algoritma", "Budi Raharjo", 2022);

        Anggota anggota1 = new Anggota("A001", "Arjuna Lanang", "Jl. Sumenep No. 10");
        Anggota anggota2 = new Anggota("A002", "Siti Aminah", "Jl. Pemuda No. 45");

        Peminjaman peminjaman1 = new Peminjaman("2026-09-01", buku1, anggota1, 7);
        Peminjaman peminjaman2 = new Peminjaman("2026-09-05", buku2, anggota2, 14);

        System.out.println("[SUCCESS] Objek valid berhasil dibuat.");
        buku1.tampilkanData();
        System.out.println();
        anggota1.tampilkanData();
        System.out.println();
        peminjaman1.tampilkanData();
        System.out.println();

        // 2. TEST VALID: PERUBAHAN DATA HANYA MELALUI SETTER VALID
        System.out.println("=== 2. TEST VALID: PERUBAHAN DATA VIA SETTER ===");
        System.out.println("Mengubah judul buku1...");
        buku1.setJudul("Pemrograman Java Lanjut & Framework");
        System.out.println("Getter -> Judul Baru: " + buku1.getJudul());

        System.out.println("Mengubah alamat anggota2...");
        anggota2.setAlamat("Jl. Merdeka No. 88, Surabaya");
        System.out.println("Getter -> Alamat Baru: " + anggota2.getAlamat());

        System.out.println("Mengubah durasi peminjaman1...");
        peminjaman1.setDurasiHari(10);
        System.out.println("Getter -> Durasi Baru: " + peminjaman1.getDurasiHari() + " Hari\n");

        // 3. TEST INVALID: PERCOBAAN INPUT DATA INVALID (VALIDASI MEMBENTENGI DATA)
        System.out.println("=== 3. TEST INVALID: PERCOBAAN INPUT DATA INVALID ===");
        
        System.out.println("[UJI 1] Uji tahun terbit invalid (buku1.setTahunTerbit(900)):");
        buku1.setTahunTerbit(900);
        System.out.println("Getter -> Tahun Terbit Setelah Uji: " + buku1.getTahunTerbit() + " (Data Tetap Aman)\n");

        System.out.println("[UJI 2] Uji judul kosong (buku1.setJudul(\"\")):");
        buku1.setJudul("");
        System.out.println("Getter -> Judul Setelah Uji: \"" + buku1.getJudul() + "\" (Data Tetap Aman)\n");

        System.out.println("[UJI 3] Uji nama anggota kosong (anggota1.setNama(\"   \")):");
        anggota1.setNama("   ");
        System.out.println("Getter -> Nama Setelah Uji: \"" + anggota1.getNama() + "\" (Data Tetap Aman)\n");

        System.out.println("[UJI 4] Uji durasi pinjam melebihi batas 14 hari (peminjaman1.setDurasiHari(20)):");
        peminjaman1.setDurasiHari(20);
        System.out.println("Getter -> Durasi Setelah Uji: " + peminjaman1.getDurasiHari() + " Hari (Data Tetap Aman)\n");

        System.out.println("[UJI 5] Uji durasi pinjam negatif (peminjaman1.setDurasiHari(-5)):");
        peminjaman1.setDurasiHari(-5);
        System.out.println("Getter -> Durasi Setelah Uji: " + peminjaman1.getDurasiHari() + " Hari (Data Tetap Aman)\n");

        System.out.println("[UJI 6] Uji pembuatan objek peminjaman invalid via Constructor:");
        Peminjaman peminjamanInvalid = new Peminjaman("", null, null, 30);

        // 4. RINGKASAN AKHIR PENGUJIAN P3
        System.out.println("\n=== 4. RINGKASAN DATA AKHIR PEMINJAMAN ===");
        peminjaman1.tampilkanData();
        System.out.println();
        peminjaman2.tampilkanData();
        System.out.println();

        System.out.println("=================================================");
        System.out.println("       PENGUJIAN MODUL 3 SELESAI DENGAN SUKSES!  ");
        System.out.println("=================================================");
    }
}
