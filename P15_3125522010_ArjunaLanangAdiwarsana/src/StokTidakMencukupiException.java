/**
 * Exception yang dilempar ketika permintaan kuantitas produk melebihi stok yang tersedia.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class StokTidakMencukupiException extends Exception {
    private final String kodeProduk;
    private final int stokTersedia;
    private final int kuantitasDiminta;

    public StokTidakMencukupiException(String kodeProduk, int stokTersedia, int kuantitasDiminta) {
        super(String.format("Stok produk [%s] tidak mencukupi! Tersedia: %d, Diminta: %d", 
                kodeProduk, stokTersedia, kuantitasDiminta));
        this.kodeProduk = kodeProduk;
        this.stokTersedia = stokTersedia;
        this.kuantitasDiminta = kuantitasDiminta;
    }

    public String getKodeProduk() {
        return kodeProduk;
    }

    public int getStokTersedia() {
        return stokTersedia;
    }

    public int getKuantitasDiminta() {
        return kuantitasDiminta;
    }
}
