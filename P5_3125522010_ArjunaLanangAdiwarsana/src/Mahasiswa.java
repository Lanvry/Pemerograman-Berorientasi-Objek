public class Mahasiswa extends Anggota {
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

    @Override
    public void tampilkanData() {
        super.tampilkanData();
        System.out.println("NRP          : " + nrp);
        System.out.println("Program Studi: " + prodi);
        System.out.println("Tipe Anggota : Mahasiswa");
    }
}
