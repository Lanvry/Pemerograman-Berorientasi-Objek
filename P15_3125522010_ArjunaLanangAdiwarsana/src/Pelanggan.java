import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entitas Pelanggan yang memiliki identitas, informasi kontak, dan riwayat transaksi.
 * Menerapkan enkapsulasi kuat pada riwayat transaksi menggunakan unmodifiable list.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class Pelanggan {
    private final String idPelanggan;
    private final String nama;
    private final String email;
    private final String nomorTelepon;
    private final List<Transaksi> riwayatTransaksi;

    public Pelanggan(String idPelanggan, String nama, String email, String nomorTelepon) {
        if (idPelanggan == null || idPelanggan.trim().isEmpty()) {
            throw new IllegalArgumentException("ID Pelanggan tidak boleh kosong.");
        }
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama pelanggan tidak boleh kosong.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Format email pelanggan tidak valid.");
        }

        this.idPelanggan = idPelanggan.trim();
        this.nama = nama.trim();
        this.email = email.trim();
        this.nomorTelepon = (nomorTelepon == null) ? "-" : nomorTelepon.trim();
        this.riwayatTransaksi = new ArrayList<>();
    }

    public String getIdPelanggan() {
        return idPelanggan;
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    /**
     * Mengembalikan riwayat transaksi pelanggan secara aman (unmodifiable list).
     */
    public List<Transaksi> getRiwayatTransaksi() {
        return Collections.unmodifiableList(riwayatTransaksi);
    }

    /**
     * Menambahkan transaksi yang telah diproses ke dalam riwayat transaksi pelanggan.
     */
    public void tambahRiwayatTransaksi(Transaksi transaksi) {
        if (transaksi != null) {
            this.riwayatTransaksi.add(transaksi);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pelanggan pelanggan = (Pelanggan) o;
        return Objects.equals(idPelanggan, pelanggan.idPelanggan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPelanggan);
    }

    @Override
    public String toString() {
        return String.format("Pelanggan[%s - %s (%s)]", idPelanggan, nama, email);
    }
}
