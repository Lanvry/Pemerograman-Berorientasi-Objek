public class Buku implements DapatDilacak {
    private String kode;
    private String judul;
    private String penulis;
    private int tahunTerbit;
    private String rakLokasi;

    public Buku(String kode, String judul, String penulis, int tahunTerbit, String rakLokasi) {
        setKode(kode);
        setJudul(judul);
        setPenulis(penulis);
        setTahunTerbit(tahunTerbit);
        setRakLokasi(rakLokasi);
    }

    public Buku(String kode, String judul, String penulis, int tahunTerbit) {
        this(kode, judul, penulis, tahunTerbit, "Rak A-01 (Umum)");
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        if (kode != null && !kode.trim().isEmpty()) {
            this.kode = kode;
        } else {
            System.out.println("[Error] Kode buku tidak boleh kosong.");
            this.kode = "B-UNKNOWN";
        }
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        if (judul != null && !judul.trim().isEmpty()) {
            this.judul = judul;
        } else {
            System.out.println("[Error] Judul buku tidak boleh kosong.");
        }
    }

    public String getPenulis() {
        return penulis;
    }

    public void setPenulis(String penulis) {
        if (penulis != null && !penulis.trim().isEmpty()) {
            this.penulis = penulis;
        } else {
            System.out.println("[Error] Penulis buku tidak boleh kosong.");
        }
    }

    public int getTahunTerbit() {
        return tahunTerbit;
    }

    public void setTahunTerbit(int tahunTerbit) {
        if (tahunTerbit > 0 && tahunTerbit <= 2026) {
            this.tahunTerbit = tahunTerbit;
        } else {
            System.out.println("[Error] Tahun terbit tidak valid.");
            this.tahunTerbit = 2026;
        }
    }

    public String getRakLokasi() {
        return rakLokasi;
    }

    public void setRakLokasi(String rakLokasi) {
        if (rakLokasi != null && !rakLokasi.trim().isEmpty()) {
            this.rakLokasi = rakLokasi;
        } else {
            this.rakLokasi = "Rak Penyimpanan Umum";
        }
    }

    // Implementasi Interface DapatDilacak (Berbeda hierarki dengan Anggota)
    @Override
    public String getLokasi() {
        return "Koleksi Fisik di " + rakLokasi;
    }

    public void tampilkanData() {
        System.out.println("[" + kode + "] " + judul + " - Penulis: " + penulis + " (" + tahunTerbit + ")");
        System.out.println("Lokasi Fisik : " + getLokasi());
    }
}
