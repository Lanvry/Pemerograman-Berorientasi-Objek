public class Buku {
    private String kode;
    private String judul;
    private String penulis;
    private int tahunTerbit;

    // Constructor dengan validasi terintegrasi
    public Buku(String kode, String judul, String penulis, int tahunTerbit) {
        if (kode != null && !kode.trim().isEmpty()) {
            this.kode = kode;
        } else {
            System.out.println("[ERROR VALIDASI] Kode buku tidak boleh kosong/null!");
            this.kode = "UNKNOWN";
        }
        setJudul(judul);
        setPenulis(penulis);
        setTahunTerbit(tahunTerbit);
    }

    // Read-only getter untuk 'kode' (tidak ada setter agar tidak bisa diubah)
    public String getKode() {
        return kode;
    }

    // Getter & Setter dengan validasi untuk 'judul'
    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        if (judul != null && !judul.trim().isEmpty()) {
            this.judul = judul;
        } else {
            System.out.println("[ERROR VALIDASI] Judul buku tidak boleh kosong/null!");
        }
    }

    // Getter & Setter dengan validasi untuk 'penulis'
    public String getPenulis() {
        return penulis;
    }

    public void setPenulis(String penulis) {
        if (penulis != null && !penulis.trim().isEmpty()) {
            this.penulis = penulis;
        } else {
            System.out.println("[ERROR VALIDASI] Penulis buku tidak boleh kosong/null!");
        }
    }

    // Getter & Setter dengan validasi untuk 'tahunTerbit'
    public int getTahunTerbit() {
        return tahunTerbit;
    }

    public void setTahunTerbit(int tahunTerbit) {
        if (tahunTerbit >= 1000 && tahunTerbit <= 2100) {
            this.tahunTerbit = tahunTerbit;
        } else {
            System.out.println("[ERROR VALIDASI] Tahun terbit tidak valid! Harus antara 1000 - 2100. (Diinput: " + tahunTerbit + ")");
        }
    }

    // Method operasional tanpa parameter
    public void tampilkanData() {
        System.out.println("--- Detail Buku (Encapsulated) ---");
        System.out.println("Kode Buku    : " + (kode != null ? kode : "N/A"));
        System.out.println("Judul Buku   : " + (judul != null ? judul : "N/A"));
        System.out.println("Penulis      : " + (penulis != null ? penulis : "N/A"));
        System.out.println("Tahun Terbit : " + (tahunTerbit > 0 ? tahunTerbit : "N/A"));
    }
}
