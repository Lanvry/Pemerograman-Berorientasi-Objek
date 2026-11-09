package model;

/**
 * Exception yang dilempar ketika gedung parkir tidak memiliki slot kosong
 * yang sesuai dengan tipe kendaraan yang hendak masuk (Motor, Mobil, atau EV).
 */
public class KapasitasPenuhException extends Exception {
    private final String tipeKendaraan;

    public KapasitasPenuhException(String tipeKendaraan) {
        super(String.format("Kapasitas parkir untuk kategori '%s' sudah PENUH!", tipeKendaraan));
        this.tipeKendaraan = tipeKendaraan;
    }

    public KapasitasPenuhException(String tipeKendaraan, String pesan) {
        super(pesan);
        this.tipeKendaraan = tipeKendaraan;
    }

    public String getTipeKendaraan() {
        return tipeKendaraan;
    }
}
