package model;

import java.util.List;

/**
 * Interface IArmadaManager mengatur pendaftaran, pencarian, dan pengelolaan master koleksi kendaraan.
 * 
 * Sesuai prinsip ISP (Interface Segregation Principle):
 * - Memisahkan operasi master data kendaraan dari gerbang dan billing.
 * - Client pemanggil (seperti form pendaftaran kendaraan, modul langganan VIP, atau audit armada)
 *   hanya perlu bergantung pada interface ini.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public interface IArmadaManager {

    /**
     * Mendaftarkan kendaraan baru ke sistem master armada.
     */
    void tambahKendaraan(Kendaraan k) throws FormatPlatTidakValidException;

    /**
     * Menghapus kendaraan dari daftar terdaftar.
     */
    boolean hapusKendaraan(String platNomor);

    /**
     * Mencari kendaraan terdaftar berdasarkan nomor polisi.
     */
    Kendaraan cariKendaraan(String platNomor);

    /**
     * Mengembalikan seluruh koleksi kendaraan terdaftar dalam format unmodifiable.
     */
    List<Kendaraan> getDaftarKendaraan();

    /**
     * Mengembalikan total jumlah kendaraan terdaftar.
     */
    int getTotalKendaraanTerdaftar();
}
