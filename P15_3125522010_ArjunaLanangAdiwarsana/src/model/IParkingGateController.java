package model;

/**
 * Interface IParkingGateController mengatur operasi kontrol palang gerbang dan alur check-in kendaraan.
 * 
 * Sesuai prinsip ISP (Interface Segregation Principle):
 * - Memisahkan kontrak kontrol gerbang fisik / ANPR camera / ticketless entry dari modul billing
 *   dan repositori data armada.
 * - Client pemanggil (seperti gerbang otomatis, sensor deteksi kendaraan, atau CCTV loop) hanya
 *   perlu bergantung pada interface ini.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public interface IParkingGateController {

    /**
     * Memproses alur check-in kendaraan standar dengan validasi kuota dan anti-duplikasi.
     */
    KarcisParkir prosesMasuk(Kendaraan k, int jamMasuk) throws KapasitasPenuhException, KendaraanSudahParkirException;

    /**
     * Memproses alur masuk ticketless otomatis berbasis kamera ANPR.
     */
    KarcisParkir prosesMasukTicketless(Kendaraan k, int jamMasuk);

    /**
     * Memproses penerbitan tiket darurat saat plat nomor tidak terbaca.
     */
    KarcisParkir prosesMasukEmergency(String tipeKendaraan, int jamMasuk) throws KapasitasPenuhException;

    /**
     * Memeriksa apakah palang gerbang sedang dalam kondisi terbuka.
     */
    boolean isGerbangTerbuka();

    /**
     * Mengatur status fisik palang gerbang (terbuka/tertutup beserta keterangannya).
     */
    void setStatusGerbang(boolean terbuka, String status);

    /**
     * Mengembalikan status tekstual gerbang saat ini.
     */
    String getStatusGerbang();
}
