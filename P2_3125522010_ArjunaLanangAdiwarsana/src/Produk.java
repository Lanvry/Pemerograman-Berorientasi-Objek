public class Produk {
    String kodeProduk;
    String nama;
    double harga;
    int stok;

    // Constructor
    public Produk(String kodeProduk, String nama, double harga, int stok) {
        this.kodeProduk = kodeProduk;
        this.nama = nama;
        this.harga = harga;
        this.stok = stok;
    }

    // Method tanpa parameter
    public void tampilkanData() {
        System.out.println("--- Detail Produk ---");
        System.out.println("Kode Produk : " + kodeProduk);
        System.out.println("Nama Produk : " + nama);
        System.out.println("Harga       : Rp " + String.format("%,.2f", harga));
        System.out.println("Stok        : " + stok);
    }

    // Method dengan parameter
    public void tambahStok(int jumlah) {
        if (jumlah > 0) {
            this.stok += jumlah;
            System.out.println("[INFO] Stok " + nama + " berhasil ditambahkan sebanyak " + jumlah + ". Stok sekarang: " + this.stok);
        } else {
            System.out.println("[WARNING] Jumlah penambahan stok harus lebih dari 0.");
        }
    }

    // Method dengan return value
    public double hitungTotalHarga(int jumlah) {
        return this.harga * jumlah;
    }

    // Getter methods
    public String getNama() {
        return nama;
    }

    public int getStok() {
        return stok;
    }

    public double getHarga() {
        return harga;
    }

    public void kurangiStok(int jumlah) {
        if (jumlah <= stok) {
            this.stok -= jumlah;
        } else {
            System.out.println("[WARNING] Stok tidak mencukupi untuk pengurangan.");
        }
    }
}
