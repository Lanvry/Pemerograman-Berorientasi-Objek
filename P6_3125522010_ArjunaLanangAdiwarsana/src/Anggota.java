public class Anggota {
    private String id;
    private String nama;
    private String alamat;
    private KartuAnggota kartuAnggota;

    public Anggota(String id, String nama, String alamat) {
        if (id != null && !id.trim().isEmpty()) {
            this.id = id;
        } else {
            System.out.println("[Error] ID anggota tidak boleh kosong.");
            this.id = "UNKNOWN";
        }
        setNama(nama);
        setAlamat(alamat);
        this.kartuAnggota = new KartuAnggota("KTA-" + this.id, "Aktif");
    }

    public String getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama != null && !nama.trim().isEmpty()) {
            this.nama = nama;
        } else {
            System.out.println("[Error] Nama anggota tidak boleh kosong.");
        }
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        if (alamat != null && !alamat.trim().isEmpty()) {
            this.alamat = alamat;
        } else {
            System.out.println("[Error] Alamat anggota tidak boleh kosong.");
        }
    }

    public KartuAnggota getKartuAnggota() {
        return kartuAnggota;
    }

    // Behavior dasar untuk Method Overriding (Polymorphic Behavior)
    public void tampilkanPeran() {
        System.out.println("Peran        : Anggota Umum Perpustakaan");
    }

    public int hitungDenda(int hariTerlambat) {
        if (hariTerlambat <= 0) return 0;
        return hariTerlambat * 1000; // Tarif dasar umum: Rp 1.000 / hari
    }

    // Method Overloading: hitungDenda dengan parameter tambahan diskon khusus
    public int hitungDenda(int hariTerlambat, double diskonPersen) {
        int dendaDasar = hitungDenda(hariTerlambat);
        if (diskonPersen <= 0.0) return dendaDasar;
        if (diskonPersen >= 100.0) return 0;
        return (int) (dendaDasar * (1.0 - (diskonPersen / 100.0)));
    }

    public int getMaksimalPinjam() {
        return 2; // Default maksimal pinjam umum: 2 buku
    }

    public void tampilkanData() {
        System.out.println("ID Anggota   : " + id);
        System.out.println("Nama         : " + nama);
        System.out.println("Alamat       : " + alamat);
        if (kartuAnggota != null) {
            System.out.println("Nomor Kartu  : " + kartuAnggota.getNomorKartu() + " (" + kartuAnggota.getStatus() + ")");
        }
        tampilkanPeran();
        System.out.println("Maks. Pinjam : " + getMaksimalPinjam() + " Buku");
    }
}
