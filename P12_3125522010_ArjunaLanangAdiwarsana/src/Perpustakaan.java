import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kelas Perpustakaan bertindak sebagai pengelola kumpulan objek (Collection Manager)
 * untuk entitas Buku menggunakan java.util.List dan java.util.ArrayList dengan generics.
 * Mendukung persistensi data ke file teks menggunakan java.nio.file dengan prinsip
 * Atomic Swap untuk menjamin konsistensi state.
 * 
 * Modul 12: Penyimpanan dan Pembacaan Data ke File
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

    /**
     * Menyimpan seluruh koleksi buku ke file teks pada lokasi Path tertentu.
     * Menggunakan separator pipe '|' dan encoding UTF-8.
     * Memvalidasi bahwa kode atau judul tidak mengandung karakter terlarang ('|', '\n', '\r').
     * 
     * @param lokasi path file tujuan
     * @throws IOException jika terjadi kegagalan penulisan file
     * @throws IllegalArgumentException jika data mengandung pemisah terlarang
     */
    public void simpan(Path lokasi) throws IOException {
        List<String> baris = new ArrayList<>();
        for (Buku buku : daftarBuku) {
            String kode = buku.getKode();
            String judul = buku.getJudul();
            for (String nilai : new String[]{kode, judul}) {
                if (nilai.contains("|")
                        || nilai.contains("\n")
                        || nilai.contains("\r")) {
                    throw new IllegalArgumentException(
                        "Kode/judul mengandung pemisah terlarang."
                    );
                }
            }
            baris.add(kode + "|" + judul);
        }
        Files.write(lokasi, baris, StandardCharsets.UTF_8);
    }

    /**
     * Membaca file teks dan membentuk kembali koleksi buku ke dalam daftarBuku.
     * Menerapkan prinsip Atomic Swap:
     * Seluruh data dibaca dan divalidasi ke dalam objek sementara terlebih dahulu.
     * Koleksi aktif hanya diperbarui jika seluruh baris berhasil divalidasi tanpa galat.
     * 
     * @param lokasi path file sumber
     * @throws IOException jika file tidak ditemukan, format salah, atau data tidak valid
     */
    public void muat(Path lokasi) throws IOException {
        List<String> baris = Files.readAllLines(
            lokasi, StandardCharsets.UTF_8
        );
        Perpustakaan sementara = new Perpustakaan();
        for (int i = 0; i < baris.size(); i++) {
            String[] bagian = baris.get(i).split("\\|", -1);
            if (bagian.length != 2) {
                throw new IOException(
                    "Format salah pada baris " + (i + 1)
                );
            }
            try {
                Buku buku = new Buku(bagian[0], bagian[1]);
                sementara.tambahBuku(buku);
            } catch (IllegalArgumentException e) {
                throw new IOException(
                    "Data tidak valid pada baris " + (i + 1)
                    + ": " + e.getMessage(),
                    e
                );
            }
        }
        // Ganti collection hanya setelah semua baris valid (Atomic Swap)
        daftarBuku.clear();
        daftarBuku.addAll(sementara.daftarBuku);
    }
}
