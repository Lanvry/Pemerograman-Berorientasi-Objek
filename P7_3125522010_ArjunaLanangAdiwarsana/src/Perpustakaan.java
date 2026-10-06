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

    // Method Overloading 1: Cari buku berdasarkan judul
    public Buku cariBuku(String judul) {
        if (judul == null || judul.trim().isEmpty()) return null;
        for (Buku b : daftarBuku) {
            if (b.getJudul().equalsIgnoreCase(judul.trim())) {
                return b;
            }
        }
        return null;
    }

    // Method Overloading 2: Cari buku berdasarkan kode katalog dan cetak status
    public Buku cariBuku(String kode, boolean cetakDetail) {
        if (kode == null || kode.trim().isEmpty()) return null;
        for (Buku b : daftarBuku) {
            if (b.getKode().equalsIgnoreCase(kode.trim())) {
                if (cetakDetail) {
                    System.out.println("[Info Pencarian] Ditemukan buku: " + b.getJudul() + " (" + b.getPenulis() + ") di " + b.getLokasi());
                }
                return b;
            }
        }
        if (cetakDetail) {
            System.out.println("[Info Pencarian] Buku dengan kode " + kode + " tidak ditemukan.");
        }
        return null;
    }

    public void tampilkanDaftarBuku() {
        System.out.println("=== Koleksi Buku: " + nama + " ===");
        System.out.println("Lokasi: " + alamat);
        System.out.println("Jumlah Koleksi: " + daftarBuku.size() + " Buku");
        System.out.println("---------------------------------------------------------");
        if (daftarBuku.isEmpty()) {
            System.out.println("(Belum ada koleksi buku)");
        } else {
            for (int i = 0; i < daftarBuku.size(); i++) {
                Buku b = daftarBuku.get(i);
                System.out.println((i + 1) + ". [" + b.getKode() + "] " + b.getJudul() + " (" + b.getPenulis() + ", " + b.getTahunTerbit() + ") -> " + b.getLokasi());
            }
        }
        System.out.println("---------------------------------------------------------");
    }
}
