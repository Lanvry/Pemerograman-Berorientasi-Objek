public class Dosen extends Anggota {
    private String nip;
    private String departemen;

    public Dosen(String id, String nama, String alamat, String nip, String departemen) {
        super(id, nama, alamat);
        setNip(nip);
        setDepartemen(departemen);
    }

    public Dosen(String id, String nama, String alamat, String nip) {
        this(id, nama, alamat, nip, "Teknik Informatika");
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        if (nip != null && !nip.trim().isEmpty()) {
            this.nip = nip;
        } else {
            System.out.println("[Error] NIP tidak boleh kosong.");
        }
    }

    public String getDepartemen() {
        return departemen;
    }

    public void setDepartemen(String departemen) {
        if (departemen != null && !departemen.trim().isEmpty()) {
            this.departemen = departemen;
        } else {
            System.out.println("[Error] Departemen tidak boleh kosong.");
        }
    }

    // Method Overriding 1: Kustomisasi Peran
    @Override
    public void tampilkanPeran() {
        System.out.println("Peran        : Dosen Pengajar & Peneliti (Akses Koleksi Jurnal & Referensi Khusus)");
    }

    // Method Overriding 2: Privilege Bebas Denda untuk Dosen
    @Override
    public int hitungDenda(int hariTerlambat) {
        // Dosen mendapatkan hak istimewa bebas denda keterlambatan (Rp 0)
        return 0;
    }

    // Method Overriding 3: Kuota Maksimal Peminjaman
    @Override
    public int getMaksimalPinjam() {
        return 5; // Dosen dapat meminjam maksimal 5 buku untuk riset/pengajaran
    }

    // Method Overriding 4: Tampilkan Data Spesifik Dosen
    @Override
    public void tampilkanData() {
        System.out.println("ID Anggota   : " + getId());
        System.out.println("Nama         : " + getNama());
        System.out.println("Alamat       : " + getAlamat());
        if (getKartuAnggota() != null) {
            System.out.println("Nomor Kartu  : " + getKartuAnggota().getNomorKartu() + " (" + getKartuAnggota().getStatus() + ")");
        }
        System.out.println("NIP          : " + nip);
        System.out.println("Departemen   : " + departemen);
        tampilkanPeran();
        System.out.println("Maks. Pinjam : " + getMaksimalPinjam() + " Buku");
    }
}
