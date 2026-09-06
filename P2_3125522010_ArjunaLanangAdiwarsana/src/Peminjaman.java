public class Peminjaman {
    String tanggal;
    Buku buku;
    Anggota anggota;

    // Constructor
    public Peminjaman(String tanggal, Buku buku, Anggota anggota) {
        this.tanggal = tanggal;
        this.buku = buku;
        this.anggota = anggota;
    }

    // Method tanpa parameter
    public void tampilkanData() {
        System.out.println("========================================");
        System.out.println("      DETAIL PEMINJAMAN PERPUSTAKAAN    ");
        System.out.println("========================================");
        System.out.println("Tanggal Pinjam : " + tanggal);
        System.out.println("Peminjam       : " + (anggota != null ? anggota.getNama() : "N/A"));
        System.out.println("Buku Dipinjam  : " + (buku != null ? buku.getJudul() : "N/A"));
        System.out.println("========================================");
    }

    // Method dengan parameter
    public void ubahTanggal(String tanggalBaru) {
        this.tanggal = tanggalBaru;
        System.out.println("[INFO] Tanggal peminjaman untuk anggota " + (anggota != null ? anggota.getNama() : "") + " berhasil diubah menjadi: " + tanggalBaru);
    }

    // Method dengan return value
    public String getTanggal() {
        return tanggal;
    }

    public Buku getBuku() {
        return buku;
    }

    public Anggota getAnggota() {
        return anggota;
    }
}
