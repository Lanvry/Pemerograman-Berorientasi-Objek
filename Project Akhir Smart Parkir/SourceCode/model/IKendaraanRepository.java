package model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface IKendaraanRepository mendefinisikan abstraksi penyimpanan dan pengelolaan data kendaraan.
 * 
 * Prinsip SOLID yang diterapkan:
 * - DIP (Dependency Inversion Principle): Modul tingkat tinggi (GedungParkir) tidak boleh bergantung
 *   langsung pada modul tingkat rendah (KendaraanFileRepository / Database), melainkan keduanya
 *   bergantung pada abstraksi (IKendaraanRepository).
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public interface IKendaraanRepository {

    /**
     * Menyimpan seluruh koleksi kendaraan ke media penyimpanan (file/database).
     *
     * @param lokasi path tujuan (jika berbasis file) atau null untuk lokasi default
     * @throws IOException jika terjadi galat I/O
     */
    void simpan(Path lokasi) throws IOException;

    /**
     * Memuat daftar kendaraan dari media penyimpanan ke dalam repositori.
     *
     * @param lokasi path sumber (jika berbasis file) atau null untuk lokasi default
     * @return List kendaraan yang berhasil dimuat
     * @throws IOException jika file tidak ditemukan atau format data rusak
     */
    List<Kendaraan> muat(Path lokasi) throws IOException;

    /**
     * Menambahkan kendaraan baru ke repositori.
     *
     * @param k kendaraan yang akan ditambahkan
     * @throws FormatPlatTidakValidException jika format plat nomor salah
     * @throws IllegalArgumentException jika kendaraan null atau plat duplikat
     */
    void tambah(Kendaraan k) throws FormatPlatTidakValidException;

    /**
     * Menghapus kendaraan dari repositori berdasarkan plat nomor.
     *
     * @param plat nomor polisi kendaraan
     * @return true jika berhasil dihapus, false jika tidak ditemukan
     */
    boolean hapus(String plat);

    /**
     * Mencari kendaraan berdasarkan nomor polisi (case-insensitive & whitespace-tolerant).
     *
     * @param plat nomor polisi kendaraan
     * @return objek Kendaraan jika ditemukan, null jika tidak ada
     */
    Kendaraan cari(String plat);

    /**
     * Mengembalikan seluruh koleksi kendaraan terdaftar.
     *
     * @return List seluruh kendaraan
     */
    List<Kendaraan> getSemua();
}
