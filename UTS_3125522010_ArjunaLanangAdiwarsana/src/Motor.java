public class Motor extends Kendaraan {
    private String tipeMotor;
    private int jumlahHelm;
    private double biayaHelmTambahanPerHari;

    public Motor(String platNomor, String merk, String model, int tahunProduksi, double tarifPerHari,
                 String tipeMotor, int jumlahHelm) {
        super(platNomor, merk, model, tahunProduksi, tarifPerHari);
        if (jumlahHelm < 1 || jumlahHelm > 2) {
            throw new IllegalArgumentException("Jumlah helm yang disediakan maksimal 2 unit dan minimal 1 unit.");
        }
        this.tipeMotor = tipeMotor;
        this.jumlahHelm = jumlahHelm;
        this.biayaHelmTambahanPerHari = 10_000.0;
    }

    public String getTipeMotor() {
        return tipeMotor;
    }

    public int getJumlahHelm() {
        return jumlahHelm;
    }

    public void setJumlahHelm(int jumlahHelm) {
        if (jumlahHelm < 1 || jumlahHelm > 2) {
            throw new IllegalArgumentException("Jumlah helm harus 1 atau 2 unit.");
        }
        this.jumlahHelm = jumlahHelm;
    }

    public double getBiayaHelmTambahanPerHari() {
        return biayaHelmTambahanPerHari;
    }

    @Override
    public double hitungBiayaSewa(int durasiHari) {
        if (durasiHari < 1) {
            throw new IllegalArgumentException("Durasi sewa motor minimal 1 hari.");
        }

        double totalDasar = getTarifPerHari() * durasiHari;
        // Helm pertama gratis, helm kedua dikenakan sewa tambahan per hari
        double totalHelmTambahan = (jumlahHelm > 1) ? (biayaHelmTambahanPerHari * durasiHari) : 0.0;

        return totalDasar + totalHelmTambahan;
    }

    @Override
    public void tampilkanSpesifikasi() {
        System.out.printf("  [Kategori: Motor] Plat: %s | Merk: %s %s | Tipe: %s | Fasilitas Helm: %d Unit%n",
                getPlatNomor(), getMerk(), getModel(), tipeMotor, jumlahHelm);
    }
}
