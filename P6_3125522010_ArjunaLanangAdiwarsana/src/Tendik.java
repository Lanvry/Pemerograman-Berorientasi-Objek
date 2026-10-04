public class Tendik extends Anggota {
    private String nip;
    private String unitKerja;

    public Tendik(String id, String nama, String alamat, String nip, String unitKerja) {
        super(id, nama, alamat);
        setNip(nip);
        setUnitKerja(unitKerja);
    }

    public Tendik(String id, String nama, String alamat, String nip) {
        this(id, nama, alamat, nip, "Bagian Administrasi & Akademik");
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        if (nip != null && !nip.trim().isEmpty()) {
            this.nip = nip;
        } else {
            System.out.println("[Error] NIP tendik tidak boleh kosong.");
        }
    }

    public String getUnitKerja() {
        return unitKerja;
    }

    public void setUnitKerja(String unitKerja) {
        if (unitKerja != null && !unitKerja.trim().isEmpty()) {
            this.unitKerja = unitKerja;
        } else {
            System.out.println("[Error] Unit kerja tidak boleh kosong.");
        }
    }

    // Method Overriding 1: Kustomisasi Peran
    @Override
    public void tampilkanPeran() {
        System.out.println("Peran        : Tenaga Kependidikan / Staf (Akses Operasional & Umum)");
    }

    // Method Overriding 2: Tarif Denda Khusus Tendik (Rp 750 / hari)
    @Override
    public int hitungDenda(int hariTerlambat) {
        if (hariTerlambat <= 0) return 0;
        return hariTerlambat * 750;
    }

    // Method Overriding 3: Kuota Maksimal Peminjaman
    @Override
    public int getMaksimalPinjam() {
        return 4; // Tendik dapat meminjam maksimal 4 buku
    }

    // Method Overriding 4: Tampilkan Data Spesifik Tendik
    @Override
    public void tampilkanData() {
        System.out.println("ID Anggota   : " + getId());
        System.out.println("Nama         : " + getNama());
        System.out.println("Alamat       : " + getAlamat());
        if (getKartuAnggota() != null) {
            System.out.println("Nomor Kartu  : " + getKartuAnggota().getNomorKartu() + " (" + getKartuAnggota().getStatus() + ")");
        }
        System.out.println("NIP          : " + nip);
        System.out.println("Unit Kerja   : " + unitKerja);
        tampilkanPeran();
        System.out.println("Maks. Pinjam : " + getMaksimalPinjam() + " Buku");
    }
}
