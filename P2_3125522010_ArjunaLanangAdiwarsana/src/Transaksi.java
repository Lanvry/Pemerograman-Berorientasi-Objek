public class Transaksi {
    String kodeTransaksi;
    Produk produk;
    Pelanggan pelanggan;
    int jumlah;

    // Constructor
    public Transaksi(String kodeTransaksi, Produk produk, Pelanggan pelanggan, int jumlah) {
        this.kodeTransaksi = kodeTransaksi;
        this.produk = produk;
        this.pelanggan = pelanggan;
        this.jumlah = jumlah;
    }

    // Method tanpa parameter
    public void tampilkanDetailTransaksi() {
        System.out.println("========================================");
        System.out.println("        DETAIL TRANSAKSI TOKO           ");
        System.out.println("========================================");
        System.out.println("Kode Transaksi : " + kodeTransaksi);
        System.out.println("Pelanggan      : " + pelanggan.getNama());
        System.out.println("Produk Dibeli  : " + produk.getNama());
        System.out.println("Harga Satuan   : Rp " + String.format("%,.2f", produk.getHarga()));
        System.out.println("Jumlah Beli    : " + jumlah);
        System.out.println("Total Bayar    : Rp " + String.format("%,.2f", hitungTotalBayar()));
        System.out.println("========================================");
    }

    // Method dengan parameter
    public void ubahJumlah(int jumlahBaru) {
        if (jumlahBaru > 0) {
            this.jumlah = jumlahBaru;
            System.out.println("[INFO] Jumlah pembelian transaksi " + kodeTransaksi + " diubah menjadi " + jumlahBaru);
        } else {
            System.out.println("[WARNING] Jumlah pembelian minimal 1.");
        }
    }

    // Method dengan return value
    public double hitungTotalBayar() {
        return produk.hitungTotalHarga(this.jumlah);
    }

    // Process Transaction (Deducts stock and customer balance)
    public boolean prosesTransaksi() {
        double totalBayar = hitungTotalBayar();
        System.out.println("\n[PROSES TRANSAKSI " + kodeTransaksi + "]");

        if (produk.getStok() < jumlah) {
            System.out.println("[GAGAL] Stok produk " + produk.getNama() + " tidak mencukupi. (Stok: " + produk.getStok() + ", Diminta: " + jumlah + ")");
            return false;
        }

        if (!pelanggan.kelayakanTransaksi(totalBayar)) {
            System.out.println("[GAGAL] Saldo pelanggan " + pelanggan.getNama() + " tidak mencukupi. (Saldo: Rp " + String.format("%,.2f", pelanggan.getSaldo()) + ", Total: Rp " + String.format("%,.2f", totalBayar) + ")");
            return false;
        }

        produk.kurangiStok(jumlah);
        pelanggan.potongSaldo(totalBayar);
        System.out.println("[BERHASIL] Transaksi " + kodeTransaksi + " berhasil diproses!");
        System.out.println("Sisa stok " + produk.getNama() + ": " + produk.getStok());
        System.out.println("Sisa saldo " + pelanggan.getNama() + ": Rp " + String.format("%,.2f", pelanggan.getSaldo()));
        return true;
    }
}
