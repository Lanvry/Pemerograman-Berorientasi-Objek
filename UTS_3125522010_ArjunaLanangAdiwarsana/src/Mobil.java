public class Mobil extends Kendaraan {
    private int kapasitasPenumpang;
    private String jenisTransmisi;
    private boolean denganSupir;
    private double biayaSupirPerHari;
    private double biayaAsuransiHarian;

    public Mobil(String platNomor, String merk, String model, int tahunProduksi, double tarifPerHari,
                 int kapasitasPenumpang, String jenisTransmisi) {
        super(platNomor, merk, model, tahunProduksi, tarifPerHari);
        if (kapasitasPenumpang <= 0) {
            throw new IllegalArgumentException("Kapasitas penumpang mobil minimal 1 orang.");
        }
        this.kapasitasPenumpang = kapasitasPenumpang;
        this.jenisTransmisi = jenisTransmisi;
        this.denganSupir = false;
        this.biayaSupirPerHari = 150_000.0;
        this.biayaAsuransiHarian = 25_000.0;
    }

    public int getKapasitasPenumpang() {
        return kapasitasPenumpang;
    }

    public String getJenisTransmisi() {
        return jenisTransmisi;
    }

    public boolean isDenganSupir() {
        return denganSupir;
    }

    public void setDenganSupir(boolean denganSupir) {
        this.denganSupir = denganSupir;
    }

    public double getBiayaSupirPerHari() {
        return biayaSupirPerHari;
    }

    public void setBiayaSupirPerHari(double biayaSupirPerHari) {
        if (biayaSupirPerHari < 0) {
            throw new IllegalArgumentException("Biaya supir tidak boleh bernilai negatif.");
        }
        this.biayaSupirPerHari = biayaSupirPerHari;
    }

    public double getBiayaAsuransiHarian() {
        return biayaAsuransiHarian;
    }

    @Override
    public double hitungBiayaSewa(int durasiHari) {
        if (durasiHari < 1) {
            throw new IllegalArgumentException("Durasi sewa mobil minimal 1 hari.");
        }

        double totalDasar = getTarifPerHari() * durasiHari;
        double totalAsuransi = biayaAsuransiHarian * durasiHari;
        double totalSupir = denganSupir ? (biayaSupirPerHari * durasiHari) : 0.0;

        return totalDasar + totalAsuransi + totalSupir;
    }

    @Override
    public void tampilkanSpesifikasi() {
        System.out.printf("  [Kategori: Mobil] Plat: %s | Merk: %s %s | Transmisi: %s | Kapasitas: %d Orang | Supir: %s%n",
                getPlatNomor(), getMerk(), getModel(), jenisTransmisi, kapasitasPenumpang,
                (denganSupir ? "Ya (Rp " + String.format("%,.0f", biayaSupirPerHari) + "/hari)" : "Lepas Kunci"));
    }
}
