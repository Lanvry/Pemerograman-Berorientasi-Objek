package model;

/**
 * Class Kendaraan merepresentasikan objek kendaraan yang masuk ke gedung parkir.
 * Menerapkan prinsip enkapsulasi ketat dengan atribut private dan validasi state.
 */
public class Kendaraan {
    private String platNomor;
    private String tipe; // "MOTOR", "MOBIL", "MOBIL_LISTRIK"
    private String merk;

    public Kendaraan(String platNomor, String tipe, String merk) {
        if (platNomor == null || platNomor.trim().isEmpty()) {
            throw new IllegalArgumentException("Plat nomor tidak boleh kosong!");
        }
        if (tipe == null || tipe.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipe kendaraan tidak boleh kosong!");
        }
        String tipeUpper = tipe.trim().toUpperCase();
        if (!tipeUpper.equals("MOTOR") && !tipeUpper.equals("MOBIL") && !tipeUpper.equals("MOBIL_LISTRIK")) {
            throw new IllegalArgumentException("Tipe kendaraan tidak valid! Pilih: MOTOR, MOBIL, atau MOBIL_LISTRIK");
        }
        this.platNomor = platNomor.trim().toUpperCase();
        this.tipe = tipeUpper;
        this.merk = (merk != null && !merk.trim().isEmpty()) ? merk.trim() : "Umum";
    }

    public String getPlatNomor() {
        return platNomor;
    }

    public String getTipe() {
        return tipe;
    }

    public String getMerk() {
        return merk;
    }

    public boolean isMobilListrik() {
        return "MOBIL_LISTRIK".equals(this.tipe);
    }

    @Override
    public String toString() {
        return String.format("%s (%s - %s)", platNomor, tipe, merk);
    }
}
