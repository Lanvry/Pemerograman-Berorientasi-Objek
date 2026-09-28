public class Peminjaman {
    private String kodePinjam;
    private String tanggal;
    private Buku buku;
    private Anggota anggota;
    private int durasiHari;

    public Peminjaman(String kodePinjam, String tanggal, Buku buku, Anggota anggota, int durasiHari) {
        this.kodePinjam = kodePinjam;
        setTanggal(tanggal);
        setBuku(buku);
        setAnggota(anggota);
        setDurasiHari(durasiHari);
    }

    public String getKodePinjam() {
        return kodePinjam;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        if (tanggal != null && !tanggal.trim().isEmpty()) {
            this.tanggal = tanggal;
        } else {
            System.out.println("[Error] Tanggal peminjaman tidak boleh kosong.");
        }
    }

    public Buku getBuku() {
        return buku;
    }

    public void setBuku(Buku buku) {
        if (buku != null) {
            this.buku = buku;
        } else {
            System.out.println("[Error] Data buku tidak boleh kosong.");
        }
    }

    public Anggota getAnggota() {
        return anggota;
    }

    public void setAnggota(Anggota anggota) {
        if (anggota != null) {
            this.anggota = anggota;
        } else {
            System.out.println("[Error] Data anggota tidak boleh kosong.");
        }
    }

    public int getDurasiHari() {
        return durasiHari;
    }

    public void setDurasiHari(int durasiHari) {
        if (durasiHari > 0 && durasiHari <= 14) {
            this.durasiHari = durasiHari;
        } else {
            System.out.println("[Error] Durasi pinjam harus antara 1 sampai 14 hari.");
        }
    }

    public void tampilkanData() {
        System.out.println("Kode Pinjam    : " + kodePinjam);
        System.out.println("Tanggal Pinjam : " + tanggal);
        System.out.println("Durasi Pinjam  : " + durasiHari + " Hari");
        System.out.println("Peminjam       : " + (anggota != null ? anggota.getNama() : "-"));
        System.out.println("No. Kartu      : " + (anggota != null && anggota.getKartuAnggota() != null ? anggota.getKartuAnggota().getNomorKartu() : "-"));
        System.out.println("Buku Dipinjam  : " + (buku != null ? buku.getJudul() : "-"));
        System.out.println("Penulis Buku   : " + (buku != null ? buku.getPenulis() : "-"));
    }
}
