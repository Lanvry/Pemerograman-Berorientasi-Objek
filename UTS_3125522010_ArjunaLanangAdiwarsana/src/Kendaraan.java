public abstract class Kendaraan implements DapatDisewa {
    private String platNomor;
    private String merk;
    private String model;
    private int tahunProduksi;
    private double tarifPerHari;
    private boolean tersedia;

    public Kendaraan(String platNomor, String merk, String model, int tahunProduksi, double tarifPerHari) {
        if (platNomor == null || platNomor.trim().isEmpty()) {
            throw new IllegalArgumentException("Plat nomor tidak boleh kosong.");
        }
        if (tarifPerHari <= 0) {
            throw new IllegalArgumentException("Tarif sewa per hari harus lebih besar dari 0.");
        }
        if (tahunProduksi < 1900) {
            throw new IllegalArgumentException("Tahun produksi tidak valid.");
        }

        this.platNomor = platNomor.trim().toUpperCase();
        this.merk = merk;
        this.model = model;
        this.tahunProduksi = tahunProduksi;
        this.tarifPerHari = tarifPerHari;
        this.tersedia = true;
    }

    public String getPlatNomor() {
        return platNomor;
    }

    public String getMerk() {
        return merk;
    }

    public String getModel() {
        return model;
    }

    public int getTahunProduksi() {
        return tahunProduksi;
    }

    public double getTarifPerHari() {
        return tarifPerHari;
    }

    public void setTarifPerHari(double tarifPerHari) {
        if (tarifPerHari <= 0) {
            throw new IllegalArgumentException("Tarif sewa per hari harus lebih besar dari 0.");
        }
        this.tarifPerHari = tarifPerHari;
    }

    @Override
    public boolean isTersedia() {
        return tersedia;
    }

    @Override
    public void sewa(String idTransaksi) {
        if (!tersedia) {
            throw new IllegalStateException("Kendaraan [" + platNomor + "] saat ini sedang tidak tersedia untuk disewa.");
        }
        this.tersedia = false;
    }

    @Override
    public void kembalikan() {
        this.tersedia = true;
    }

    // Method abstract untuk perhitungan sewa spesifik tiap jenis kendaraan
    public abstract double hitungBiayaSewa(int durasiHari);

    // Method abstract untuk menampilkan detail spesifikasi teknis
    public abstract void tampilkanSpesifikasi();

    public void tampilkanInformasiDasar() {
        System.out.printf("[%s] %s %s (%d) | Tarif: Rp %,.0f/hari | Status: %s%n",
                platNomor, merk, model, tahunProduksi, tarifPerHari,
                (tersedia ? "Tersedia" : "Sedang Disewa"));
    }
}
