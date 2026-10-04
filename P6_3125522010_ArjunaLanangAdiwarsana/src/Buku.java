public class Buku {
    private String kode;
    private String judul;
    private String penulis;
    private int tahunTerbit;

    public Buku(String kode, String judul, String penulis, int tahunTerbit) {
        setKode(kode);
        setJudul(judul);
        setPenulis(penulis);
        setTahunTerbit(tahunTerbit);
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

    public void tampilkanData() {
        System.out.println("[" + kode + "] " + judul + " - Penulis: " + penulis + " (" + tahunTerbit + ")");
    }
}
