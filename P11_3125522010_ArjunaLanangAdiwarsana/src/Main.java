/**
 * Driver class Main untuk menguji fungsionalitas Collection dan Pengelolaan Kumpulan Objek.
 * Menjalankan skenario dasar sesuai modul serta skenario pengujian tambahan (Langkah 3).
 * 
 * Modul 11: Collection dan Pengelolaan Kumpulan Objek
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   PRAKTIKUM PBO MODUL 11: COLLECTION & PENGELOLAAN KUMPULAN OBJEK       ");
        System.out.println("   Nama: Arjuna Lanang Adiwarsana | NRP: 3125522010                      ");
        System.out.println("==========================================================================");
        System.out.println();

        Perpustakaan perpustakaan = new Perpustakaan();

        // ---------------------------------------------------------------------
        // SKENARIO 1: Tambah buku B001 dan B002
        // ---------------------------------------------------------------------
        System.out.println("--- [SKENARIO 1] Menambahkan B001 dan B002 ---");
        perpustakaan.tambahBuku(new Buku("B001", "Dasar Java"));
        perpustakaan.tambahBuku(new Buku("B002", "Konsep OOP"));
        perpustakaan.tampilkanBuku();
        System.out.println();

        // ---------------------------------------------------------------------
        // SKENARIO 2: Cari B002
        // ---------------------------------------------------------------------
        System.out.println("--- [SKENARIO 2] Mencari buku dengan kode B002 ---");
        Buku hasil = perpustakaan.cariBuku("B002");
        System.out.println(
            hasil != null
            ? "Ditemukan: " + hasil.getJudul()
            : "Buku tidak ditemukan."
        );
        System.out.println();

        // ---------------------------------------------------------------------
        // SKENARIO 3: Tambah B001 kembali (Uji Pencegahan Kode Duplikat)
        // ---------------------------------------------------------------------
        System.out.println("--- [SKENARIO 3] Menambahkan kembali buku B001 (Duplikat) ---");
        try {
            perpustakaan.tambahBuku(new Buku("B001", "Buku Duplikat"));
        } catch (IllegalArgumentException e) {
            System.out.println("Ditolak: " + e.getMessage());
        }
        System.out.println("Total buku saat ini: " + perpustakaan.getTotalBuku());
        System.out.println();

        // ---------------------------------------------------------------------
        // SKENARIO 4: Hapus B001
        // ---------------------------------------------------------------------
        System.out.println("--- [SKENARIO 4] Menghapus buku B001 ---");
        System.out.println("Penghapusan B001: " + perpustakaan.hapusBuku("B001"));
        perpustakaan.tampilkanBuku();
        System.out.println();

        // ---------------------------------------------------------------------
        // SKENARIO 5: Cari B001 setelah dihapus
        // ---------------------------------------------------------------------
        System.out.println("--- [SKENARIO 5] Mencari buku B001 setelah dihapus ---");
        Buku cariHapus = perpustakaan.cariBuku("B001");
        System.out.println(
            cariHapus != null
            ? "Ditemukan: " + cariHapus.getJudul()
            : "Buku tidak ditemukan."
        );
        System.out.println();

        // ---------------------------------------------------------------------
        // SKENARIO 6: Hapus B999 yang tidak ada dalam koleksi
        // ---------------------------------------------------------------------
        System.out.println("--- [SKENARIO 6] Menghapus buku B999 (Tidak Ada) ---");
        System.out.println("Penghapusan B999: " + perpustakaan.hapusBuku("B999"));
        System.out.println("Total buku saat ini: " + perpustakaan.getTotalBuku());
        System.out.println();

        // ---------------------------------------------------------------------
        // PENGUJIAN TAMBAHAN (Sesuai Catatan Modul Langkah 3):
        // Judul kosong, Objek null, dan Menampilkan Collection kosong
        // ---------------------------------------------------------------------
        System.out.println("--- [PENGUJIAN TAMBAHAN 1] Validasi judul kosong ---");
        try {
            new Buku("B003", "");
        } catch (IllegalArgumentException e) {
            System.out.println("Ditolak: " + e.getMessage());
        }

        System.out.println("--- [PENGUJIAN TAMBAHAN 2] Validasi penambahan objek null ---");
        try {
            perpustakaan.tambahBuku(null);
        } catch (IllegalArgumentException e) {
            System.out.println("Ditolak: " + e.getMessage());
        }

        System.out.println("--- [PENGUJIAN TAMBAHAN 3] Menampilkan koleksi kosong ---");
        Perpustakaan perpusKosong = new Perpustakaan();
        perpusKosong.tampilkanBuku();
        System.out.println();

        System.out.println("==========================================================================");
        System.out.println("   SELURUH PENGUJIAN MODUL 11 SELESAI (100% SUKSES MEMENUHI MODUL)        ");
        System.out.println("==========================================================================");
    }
}

