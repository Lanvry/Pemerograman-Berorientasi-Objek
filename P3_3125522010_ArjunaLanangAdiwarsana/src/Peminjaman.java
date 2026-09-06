public class Peminjaman {
    private String tanggal;
    private Buku buku;
    private Anggota anggota;
    private int durasiHari;

    // Constructor dengan validasi terintegrasi
    public Peminjaman(String tanggal, Buku buku, Anggota anggota, int durasiHari) {
        setTanggal(tanggal);
        setBuku(buku);
        setAnggota(anggota);
        setDurasiHari(durasiHari);
    }

    // ==================== GETTER & SETTER WITH VALIDATION ====================
    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        if (tanggal != null && !tanggal.trim().isEmpty()) {
            this.tanggal = tanggal;
        } else {
            System.out.println("[ERROR VALIDASI] Tanggal peminjaman tidak boleh kosong/null!");
        }
    }

    public Buku getBuku() {
        return buku;
    }

    public void setBuku(Buku buku) {
        if (buku != null) {
            this.buku = buku;
        } else {
            System.out.println("[ERROR VALIDASI] Objek buku tidak boleh null pada peminjaman!");
        }
    }

    public Anggota getAnggota() {
        return anggota;
    }

    public void setAnggota(Anggota anggota) {
        if (anggota != null) {
            this.anggota = anggota;
        } else {
            System.out.println("[ERROR VALIDASI] Objek anggota tidak boleh null pada peminjaman!");
        }
    }

    public int getDurasiHari() {
        return durasiHari;
    }

    public void setDurasiHari(int durasiHari) {
        if (durasiHari > 0 && durasiHari <= 14) {
            this.durasiHari = durasiHari;
        } else {
            System.out.println("[ERROR VALIDASI] Durasi hari harus antara 1 - 14 hari! (Diinput: " + durasiHari + " hari)");
        }
    }

    // Method operasional tanpa parameter
    public void tampilkanData() {
        System.out.println("========================================");
        System.out.println("   DETAIL PEMINJAMAN (ENCAPSULATED)     ");
        System.out.println("========================================");
        System.out.println("Tanggal Pinjam : " + (tanggal != null ? tanggal : "N/A"));
        System.out.println("Durasi Pinjam  : " + (durasiHari > 0 ? durasiHari + " Hari" : "N/A"));
        System.out.println("Peminjam       : " + (anggota != null ? anggota.getNama() : "N/A"));
        System.out.println("Buku Dipinjam  : " + (buku != null ? buku.getJudul() : "N/A"));
        System.out.println("========================================");
    }
}
