public class KartuAnggota {
    private String nomorKartu;
    private String status;

    public KartuAnggota(String nomorKartu, String status) {
        this.nomorKartu = (nomorKartu != null && !nomorKartu.trim().isEmpty()) ? nomorKartu : "KTA-000";
        this.status = (status != null && !status.trim().isEmpty()) ? status : "Aktif";
    }

    public String getNomorKartu() {
        return nomorKartu;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status != null && !status.trim().isEmpty()) {
            this.status = status;
        } else {
            System.out.println("[Error] Status kartu tidak boleh kosong.");
        }
    }

    public void tampilkanData() {
        System.out.println("Nomor Kartu  : " + nomorKartu);
        System.out.println("Status Kartu : " + status);
    }
}
