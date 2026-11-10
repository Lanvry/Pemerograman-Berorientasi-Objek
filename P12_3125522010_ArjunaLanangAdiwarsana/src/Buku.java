/**
 * Kelas Buku merepresentasikan entitas domain buku perpustakaan.
 * Mengenkapsulasi data kode dan judul dengan validasi ketat (immutability).
 * 
 * Modul 12: Penyimpanan dan Pembacaan Data ke File
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class Buku {
    private final String kode;
    private final String judul;

    public Buku(String kode, String judul) {
        if (kode == null || kode.trim().isEmpty()
                || judul == null || judul.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode dan judul wajib diisi.");
        }
        this.kode = kode.trim();
        this.judul = judul.trim();
    }

    public String getKode() {
        return kode;
    }

    public String getJudul() {
        return judul;
    }

    @Override
    public String toString() {
        return kode + " - " + judul;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Buku other = (Buku) obj;
        return kode.equalsIgnoreCase(other.kode);
    }

    @Override
    public int hashCode() {
        return kode.toLowerCase().hashCode();
    }
}
