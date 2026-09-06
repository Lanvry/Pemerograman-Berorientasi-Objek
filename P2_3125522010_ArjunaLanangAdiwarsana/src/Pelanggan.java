public class Pelanggan {
    String idPelanggan;
    String nama;
    String email;
    double saldo;

    // Constructor
    public Pelanggan(String idPelanggan, String nama, String email, double saldo) {
        this.idPelanggan = idPelanggan;
        this.nama = nama;
        this.email = email;
        this.saldo = saldo;
    }

    // Method tanpa parameter
    public void tampilkanData() {
        System.out.println("--- Detail Pelanggan ---");
        System.out.println("ID Pelanggan : " + idPelanggan);
        System.out.println("Nama         : " + nama);
        System.out.println("Email        : " + email);
        System.out.println("Saldo        : Rp " + String.format("%,.2f", saldo));
    }

    // Method dengan parameter
    public void topUpSaldo(double jumlah) {
        if (jumlah > 0) {
            this.saldo += jumlah;
            System.out.println("[INFO] Top up saldo untuk " + nama + " sebesar Rp " + String.format("%,.2f", jumlah) + " berhasil. Saldo baru: Rp " + String.format("%,.2f", this.saldo));
        } else {
            System.out.println("[WARNING] Nominal top up harus lebih besar dari 0.");
        }
    }

    // Method dengan return value
    public boolean kelayakanTransaksi(double total) {
        return this.saldo >= total;
    }

    // Getter & Helper Methods
    public String getNama() {
        return nama;
    }

    public double getSaldo() {
        return saldo;
    }

    public void potongSaldo(double jumlah) {
        if (this.saldo >= jumlah) {
            this.saldo -= jumlah;
        } else {
            System.out.println("[WARNING] Saldo tidak mencukupi.");
        }
    }
}
