public class KartuAnggota {
    private String nomorKartu;
    private String status;

    public KartuAnggota(String nomorKartu, String status) {
        setNomorKartu(nomorKartu);
        setStatus(status);
    }

    public String getNomorKartu() {
        return nomorKartu;
    }

    public void setNomorKartu(String nomorKartu) {
        if (nomorKartu != null && !nomorKartu.trim().isEmpty()) {
            this.nomorKartu = nomorKartu;
        } else {
            this.nomorKartu = "KTA-DEFAULT";
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status != null && !status.trim().isEmpty()) {
            this.status = status;
        } else {
            this.status = "Tidak Aktif";
        }
    }

    public void tampilkanKartu() {
        System.out.println("Nomor Kartu : " + nomorKartu + " | Status: " + status);
    }
}
