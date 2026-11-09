public class Produk {
    private int stok;

    public Produk(int stokAwal) {
        if (stokAwal < 0) {
            throw new IllegalArgumentException("Stok awal tidak boleh negatif.");
        }
        this.stok = stokAwal;
    }

    public void beli(int jumlah) throws StokTidakCukupException {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah pembelian harus lebih dari nol.");
        }
        if (jumlah > stok) {
            throw new StokTidakCukupException("Stok tidak cukup. Tersedia: " + stok);
        }
        // State berubah setelah seluruh validasi berhasil
        this.stok -= jumlah;
    }

    public int getStok() {
        return stok;
    }
}
