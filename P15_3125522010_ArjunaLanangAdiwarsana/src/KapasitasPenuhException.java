/**
 * Exception yang dilempar saat jumlah atau variasi item pada keranjang belanja
 * melebihi batas kapasitas maksimal yang diizinkan sistem.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class KapasitasPenuhException extends Exception {
    private final int kapasitasMaksimal;
    private final int jumlahSaatIni;

    public KapasitasPenuhException(int kapasitasMaksimal, int jumlahSaatIni) {
        super(String.format("Kapasitas keranjang belanja penuh! Maksimal: %d jenis item, Saat ini: %d", 
                kapasitasMaksimal, jumlahSaatIni));
        this.kapasitasMaksimal = kapasitasMaksimal;
        this.jumlahSaatIni = jumlahSaatIni;
    }

    public int getKapasitasMaksimal() {
        return kapasitasMaksimal;
    }

    public int getJumlahSaatIni() {
        return jumlahSaatIni;
    }
}
