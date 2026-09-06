public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   P2 - IMPLEMENTASI CLASS, OBJECT, ATTRIBUTE,  ");
        System.out.println("           METHOD, DAN CONSTRUCTOR               ");
        System.out.println("=================================================\n");

        // 1. Inisialisasi Objek Menggunakan Constructor (Minimal 2 Objek per Class)
        System.out.println("=== 1. PEMBUATAN OBJEK (CONSTRUCTOR) ===");
        Produk produk1 = new Produk("P001", "Laptop Gaming Asus", 15000000.0, 10);
        Produk produk2 = new Produk("P002", "Smartphone 5G Samsung", 5000000.0, 25);

        Pelanggan pelanggan1 = new Pelanggan("C001", "Arjuna Lanang", "arjuna@email.com", 20000000.0);
        Pelanggan pelanggan2 = new Pelanggan("C002", "Budi Santoso", "budi@email.com", 4000000.0);

        System.out.println("[SUCCESS] Objek produk1, produk2, pelanggan1, pelanggan2 berhasil dibuat.\n");

        // 2. Pengujian Method Tanpa Parameter (Display Data)
        System.out.println("=== 2. PENGUJIAN METHOD TANPA PARAMETER ===");
        produk1.tampilkanData();
        System.out.println();
        produk2.tampilkanData();
        System.out.println();
        pelanggan1.tampilkanData();
        System.out.println();
        pelanggan2.tampilkanData();
        System.out.println();

        // 3. Pengujian Method dengan Parameter
        System.out.println("=== 3. PENGUJIAN METHOD DENGAN PARAMETER ===");
        produk1.tambahStok(5);
        pelanggan2.topUpSaldo(2000000.0);
        System.out.println();

        // 4. Pengujian Method dengan Return Value
        System.out.println("=== 4. PENGUJIAN METHOD DENGAN RETURN VALUE ===");
        double totalHarga1 = produk1.hitungTotalHarga(2);
        System.out.println("Kalkulasi total harga 2 unit " + produk1.getNama() + ": Rp " + String.format("%,.2f", totalHarga1));

        boolean layak = pelanggan2.kelayakanTransaksi(5000000.0);
        System.out.println("Apakah pelanggan " + pelanggan2.getNama() + " layak belanja Rp 5.000.000? " + (layak ? "YA" : "TIDAK"));
        System.out.println();

        // 5. Inisialisasi Objek Transaksi & Proses Transaksi
        System.out.println("=== 5. PEMBUATAN OBJEK TRANSAKSI & OPERASI BERSAMA ===");
        Transaksi transaksi1 = new Transaksi("TRX001", produk1, pelanggan1, 1);
        Transaksi transaksi2 = new Transaksi("TRX002", produk2, pelanggan2, 1);

        System.out.println("--- Transaksi 1 ---");
        transaksi1.tampilkanDetailTransaksi();
        transaksi1.prosesTransaksi();

        System.out.println("\n--- Transaksi 2 (Mengubah Jumlah dengan Parameter) ---");
        transaksi2.ubahJumlah(2);
        transaksi2.tampilkanDetailTransaksi();
        transaksi2.prosesTransaksi();

        // 6. Ringkasan Data Akhir setelah Transaksi
        System.out.println("\n=== 6. RINGKASAN DATA AKHIR PRODUK & PELANGGAN ===");
        produk1.tampilkanData();
        System.out.println();
        produk2.tampilkanData();
        System.out.println();
        pelanggan1.tampilkanData();
        System.out.println();
        pelanggan2.tampilkanData();
        System.out.println();

        System.out.println("=================================================");
        System.out.println("       PENGUJIAN MODUL 2 SELESAI DENGAN SUKSES!  ");
        System.out.println("=================================================");
    }
}
