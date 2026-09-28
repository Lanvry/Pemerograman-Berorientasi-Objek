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

    @Override
    public void tampilkanData() {
        super.tampilkanData();
        System.out.println("NIP          : " + nip);
        System.out.println("Departemen   : " + departemen);
        System.out.println("Tipe Anggota : Dosen");
    }
}
