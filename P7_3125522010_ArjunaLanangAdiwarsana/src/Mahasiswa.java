public class Mahasiswa extends Anggota implements DapatDilacak {
    private String nrp;
    private String prodi;

    public Mahasiswa(String id, String nama, String alamat, String nrp, String prodi) {
        super(id, nama, alamat);
        setNrp(nrp);
        setProdi(prodi);
    }

    public Mahasiswa(String id, String nama, String alamat, String nrp) {
        this(id, nama, alamat, nrp, "D3 PJJ Teknik Informatika");
    }

    public String getNrp() {
        return nrp;
    }

    public void setNrp(String nrp) {
        if (nrp != null && !nrp.trim().isEmpty()) {
            this.nrp = nrp;
        } else {
            System.out.println("[Error] NRP tidak boleh kosong.");
        }
    }

    public String getProdi() {
        return prodi;
    }

    public void setProdi(String prodi) {
        if (prodi != null && !prodi.trim().isEmpty()) {
            this.prodi = prodi;
        } else {
            System.out.println("[Error] Program Studi tidak boleh kosong.");
        }
    }

    // Implementasi Abstract Method 1: Kustomisasi Peran
    @Override
    public void tampilkanPeran() {
        System.out.println("Peran        : Mahasiswa (Akses Koleksi Skripsi & Akademik)");
    }

    // Implementasi Abstract Method 2: Tarif Denda Khusus Mahasiswa (Rp 500 / hari)
    @Override
    public int hitungDenda(int hariTerlambat) {
        if (hariTerlambat <= 0) return 0;
        return hariTerlambat * 500;
    }

    // Implementasi Abstract Method 3: Kuota Maksimal Peminjaman
    @Override
    public int getMaksimalPinjam() {
        return 3; // Mahasiswa dapat meminjam maksimal 3 buku
    }

    // Implementasi Interface DapatDilacak
    @Override
    public String getLokasi() {
        return "Area Baca Lantai 2 / Ruang Koleksi Skripsi";
    }

    // Override Method tampilkanData untuk informasi detail Mahasiswa
    @Override
    public void tampilkanData() {
        System.out.println("ID Anggota   : " + getId());
        System.out.println("Nama         : " + getNama());
        System.out.println("Alamat       : " + getAlamat());
        if (getKartuAnggota() != null) {
            System.out.println("Nomor Kartu  : " + getKartuAnggota().getNomorKartu() + " (" + getKartuAnggota().getStatus() + ")");
        }
        System.out.println("NRP          : " + nrp);
        System.out.println("Program Studi: " + prodi);
        tampilkanPeran();
        System.out.println("Maks. Pinjam : " + getMaksimalPinjam() + " Buku");
        System.out.println("Lokasi Akses : " + getLokasi());
    }
}
