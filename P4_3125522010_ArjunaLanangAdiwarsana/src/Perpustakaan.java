import java.util.ArrayList;
import java.util.List;

public class Perpustakaan {
    private String nama;
    private String alamat;
    private List<Buku> daftarBuku;

    public Perpustakaan(String nama, String alamat) {
        setNama(nama);
        setAlamat(alamat);
        this.daftarBuku = new ArrayList<>();
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama != null && !nama.trim().isEmpty()) {
            this.nama = nama;
        } else {
            System.out.println("[Error] Nama perpustakaan tidak boleh kosong.");
        }
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        if (alamat != null && !alamat.trim().isEmpty()) {
            this.alamat = alamat;
        } else {
            System.out.println("[Error] Alamat perpustakaan tidak boleh kosong.");
        }
    }

    public List<Buku> getDaftarBuku() {
        return daftarBuku;
    }

    public void tambahBuku(Buku buku) {
        if (buku != null) {
            daftarBuku.add(buku);
            System.out.println("[Sukses] Buku \"" + buku.getJudul() + "\" ditambahkan ke koleksi " + this.nama);
        } else {
            System.out.println("[Error] Objek buku tidak valid.");
        }
    }

    public void tampilkanDaftarBuku() {
        System.out.println("=== Koleksi Buku: " + nama + " ===");
        System.out.println("Lokasi: " + alamat);
        System.out.println("Jumlah Koleksi: " + daftarBuku.size() + " Buku");
        System.out.println("----------------------------------------");
        if (daftarBuku.isEmpty()) {
            System.out.println("(Belum ada koleksi buku)");
        } else {
            for (int i = 0; i < daftarBuku.size(); i++) {
                Buku b = daftarBuku.get(i);
                System.out.println((i + 1) + ". [" + b.getKode() + "] " + b.getJudul() + " (" + b.getPenulis() + ", " + b.getTahunTerbit() + ")");
            }
        }
        System.out.println("----------------------------------------");
    }
}
