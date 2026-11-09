package model;

/**
 * Exception yang dilempar ketika format plat nomor kendaraan tidak valid
 * (kosong, kurang dari 3 karakter, atau mengandung simbol ilegal).
 */
public class FormatPlatTidakValidException extends Exception {
    private final String inputPlat;

    public FormatPlatTidakValidException(String inputPlat) {
        super(String.format("Format plat nomor '%s' tidak valid! Contoh format valid: 'B 1234 ABC' atau 'L 9988 XYZ'.", inputPlat));
        this.inputPlat = inputPlat;
    }

    public String getInputPlat() {
        return inputPlat;
    }
}
