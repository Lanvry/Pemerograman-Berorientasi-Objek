public class Buku {
    private String kode;
    private String judul;
    private String penulis;
    private int tahunTerbit;

    public Buku(String kode, String judul, String penulis, int tahunTerbit) {
        if (kode != null && !kode.trim().isEmpty()) {
            this.kode = kode;
        } else {
            System.out.println("[Error] Kode buku tidak boleh kosong.");
            this.kode = "UNKNOWN";
        }
        setJudul(judul);
        setPenulis(penulis);
        setTahunTerbit(tahunTerbit);
    }

    public String getKode() {
        return kode;
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
            System.out.println("[Error] Penulis tidak boleh kosong.");
        }
    }

    public int getTahunTerbit() {
        return tahunTerbit;
    }

    public void setTahunTerbit(int tahunTerbit) {
        if (tahunTerbit >= 1000 && tahunTerbit <= 2100) {
            this.tahunTerbit = tahunTerbit;
        } else {
            System.out.println("[Error] Tahun terbit harus antara 1000 sampai 2100.");
        }
    }

    public void tampilkanData() {
        System.out.println("Kode Buku    : " + kode);
        System.out.println("Judul        : " + judul);
        System.out.println("Penulis      : " + penulis);
        System.out.println("Tahun Terbit : " + tahunTerbit);
    }
}
