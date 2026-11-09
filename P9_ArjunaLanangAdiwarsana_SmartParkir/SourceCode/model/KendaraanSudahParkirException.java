package model;

/**
 * Exception yang dilempar ketika kendaraan dengan plat yang sama
 * terdeteksi mencoba check-in padahal masih aktif terparkir di dalam gedung.
 */
public class KendaraanSudahParkirException extends Exception {
    private final String platNomor;
    private final String idSlot;

    public KendaraanSudahParkirException(String platNomor, String idSlot) {
        super(String.format("Kendaraan plat '%s' sudah terparkir di slot %s. Duplikasi check-in dicegah!", platNomor, idSlot));
        this.platNomor = platNomor;
        this.idSlot = idSlot;
    }

    public String getPlatNomor() {
        return platNomor;
    }

    public String getIdSlot() {
        return idSlot;
    }
}
