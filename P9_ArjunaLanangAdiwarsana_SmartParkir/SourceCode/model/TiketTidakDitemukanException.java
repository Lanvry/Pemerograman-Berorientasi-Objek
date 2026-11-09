package model;

/**
 * Exception yang dilempar ketika proses check-out atau pencarian karcis gagal
 * karena nomor tiket atau plat kendaraan tidak memiliki sesi parkir aktif.
 */
public class TiketTidakDitemukanException extends Exception {
    private final String pencarian;

    public TiketTidakDitemukanException(String pencarian) {
        super(String.format("Tiket/Plat '%s' tidak ditemukan dalam daftar parkir aktif atau sudah selesai check-out.", pencarian));
        this.pencarian = pencarian;
    }

    public String getPencarian() {
        return pencarian;
    }
}
