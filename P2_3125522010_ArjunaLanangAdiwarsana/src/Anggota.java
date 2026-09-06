public class Anggota {
    String id;
    String nama;
    String alamat;

    // Constructor
    public Anggota(String id, String nama, String alamat) {
        this.id = id;
        this.nama = nama;
        this.alamat = alamat;
    }

    // Method tanpa parameter
    public void tampilkanData() {
        System.out.println("--- Detail Anggota ---");
        System.out.println("ID Anggota : " + id);
        System.out.println("Nama       : " + nama);
        System.out.println("Alamat     : " + alamat);
    }

    // Method dengan parameter
    public void ubahAlamat(String alamatBaru) {
        this.alamat = alamatBaru;
        System.out.println("[INFO] Alamat anggota " + nama + " (" + id + ") berhasil diubah menjadi: " + alamatBaru);
    }

    // Method dengan return value
    public String getNama() {
        return nama;
    }

    public String getId() {
        return id;
    }

    public String getAlamat() {
        return alamat;
    }
}
