import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kelas Perpustakaan bertindak sebagai pengelola kumpulan objek (Collection Manager)
 * untuk entitas Buku menggunakan java.util.List dan java.util.ArrayList dengan generics.
 * 
 * Modul 11: Collection dan Pengelolaan Kumpulan Objek
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class Perpustakaan {
    // Enkapsulasi: collection bersifat private dan final agar referensi internal terlindungi
    private final List<Buku> daftarBuku = new ArrayList<>();

    /**
     * Menambahkan buku ke dalam koleksi setelah memvalidasi null dan keunikan kode.
     * @param buku objek Buku yang akan ditambahkan
     * @throws IllegalArgumentException jika objek null atau kode sudah terdaftar
     */
    public void tambahBuku(Buku buku) {
        if (buku == null) {
            throw new IllegalArgumentException("Objek buku tidak boleh null.");
        }
        if (cariBuku(buku.getKode()) != null) {
            throw new IllegalArgumentException("Kode buku sudah terdaftar.");
        }
        daftarBuku.add(buku);
    }

    /**
     * Mencari buku berdasarkan kode buku (case-insensitive).
     * @param kode kode unik buku
     * @return objek Buku jika ditemukan, null jika tidak ada
     */
    public Buku cariBuku(String kode) {
        if (kode == null) {
            return null;
        }
        for (Buku buku : daftarBuku) {
            if (buku.getKode().equalsIgnoreCase(kode.trim())) {
                return buku;
            }
        }
        return null;
    }

    /**
     * Menghapus buku dari koleksi berdasarkan kode buku.
     * @param kode kode unik buku
     * @return true jika berhasil dihapus, false jika buku tidak ditemukan
     */
    public boolean hapusBuku(String kode) {
        Buku buku = cariBuku(kode);
        if (buku == null) {
            return false;
        }
        return daftarBuku.remove(buku);
    }

    /**
     * Menampilkan seluruh isi koleksi buku beserta jumlah totalnya.
     */
    public void tampilkanBuku() {
        if (daftarBuku.isEmpty()) {
            System.out.println("Koleksi masih kosong.");
            return;
        }
        for (Buku buku : daftarBuku) {
            System.out.println(buku.getKode() + " - " + buku.getJudul());
        }
        System.out.println("Total buku: " + daftarBuku.size());
    }

    /**
     * Mengembalikan jumlah total buku dalam koleksi.
     */
    public int getTotalBuku() {
        return daftarBuku.size();
    }

    /**
     * Mengembalikan salinan read-only dari koleksi buku untuk melindungi enkapsulasi.
     */
    public List<Buku> getDaftarBuku() {
        return Collections.unmodifiableList(daftarBuku);
    }
}

