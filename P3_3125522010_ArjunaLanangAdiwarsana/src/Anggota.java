public class Anggota {
    private String id;
    private String nama;
    private String alamat;

    // Constructor dengan validasi terintegrasi
    public Anggota(String id, String nama, String alamat) {
        if (id != null && !id.trim().isEmpty()) {
            this.id = id;
        } else {
            System.out.println("[ERROR VALIDASI] ID anggota tidak boleh kosong/null!");
            this.id = "UNKNOWN";
        }
        setNama(nama);
        setAlamat(alamat);
    }

    // Read-only getter untuk 'id' (tidak ada setter agar tidak dapat diubah)
    public String getId() {
        return id;
    }

    // Getter & Setter dengan validasi untuk 'nama'
    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama != null && !nama.trim().isEmpty()) {
            this.nama = nama;
        } else {
            System.out.println("[ERROR VALIDASI] Nama anggota tidak boleh kosong/null!");
        }
    }

    // Getter & Setter dengan validasi untuk 'alamat'
    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        if (alamat != null && !alamat.trim().isEmpty()) {
            this.alamat = alamat;
        } else {
            System.out.println("[ERROR VALIDASI] Alamat anggota tidak boleh kosong/null!");
        }
    }

    // Method operasional tanpa parameter
    public void tampilkanData() {
        System.out.println("--- Detail Anggota (Encapsulated) ---");
        System.out.println("ID Anggota : " + (id != null ? id : "N/A"));
        System.out.println("Nama       : " + (nama != null ? nama : "N/A"));
        System.out.println("Alamat     : " + (alamat != null ? alamat : "N/A"));
    }
}
