public class Buku {
    String kode;
    String judul;
    String penulis;

    // Constructor
    public Buku(String kode, String judul, String penulis) {
        this.kode = kode;
        this.judul = judul;
        this.penulis = penulis;
    }

    // Method tanpa parameter
    public void tampilkanData() {
        System.out.println("--- Detail Buku ---");
        System.out.println("Kode Buku : " + kode);
        System.out.println("Judul Buku: " + judul);
        System.out.println("Penulis   : " + penulis);
    }

    // Method dengan parameter
    public void ubahJudul(String judulBaru) {
        this.judul = judulBaru;
        System.out.println("[INFO] Judul buku dengan kode " + kode + " berhasil diubah menjadi: \"" + judulBaru + "\"");
    }

    // Method dengan return value
    public String getJudul() {
        return judul;
    }

    public String getKode() {
        return kode;
    }

    public String getPenulis() {
        return penulis;
    }
}
