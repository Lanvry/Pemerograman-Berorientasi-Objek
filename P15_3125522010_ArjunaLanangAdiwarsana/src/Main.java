/**
 * ============================================================================
 * PERTEMUAN 15: INTEGRASI & EVALUASI PROYEK BERORIENTASI OBYEK
 * DRIVER UTAMA (MAIN APPLICATION & DEMONSTRATION RUNNER)
 * ============================================================================
 * Mahasiswa : Arjuna Lanang Adiwarsana
 * NRP       : 3125522010
 * Program   : D3 PJJ Teknik Informatika - PENS PSDKU Sumenep
 * Dosen     : Nirwana Haidar Hari, S.Pd., M.Kom.
 * ============================================================================
 * Mengintegrasikan seluruh domain model (Produk, Keranjang, Transaksi, Pembayaran, Pelanggan)
 * dan memvalidasi 4 skenario evaluasi (Normal, Boundary, Invalid, Failure) sesuai Slide 12 P15.
 * ============================================================================
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     SISTEM INFORMASI TRANSAKSI RITEL & EVALUASI DESAIN OBJEK (PBO P15)         ");
        System.out.println("                 POLITEKNIK ELEKTRONIKA NEGERI SURABAYA                         ");
        System.out.println("================================================================================");
        System.out.println("  Author : Arjuna Lanang Adiwarsana (NRP: 3125522010)");
        System.out.println("  Kelas  : D3 PJJ Teknik Informatika - PENS PSDKU Sumenep");
        System.out.println("  Materi : Integrasi & Evaluasi Proyek Berorientasi Obyek (Slide 1-20)");
        System.out.println("================================================================================\n");

        System.out.println(">> Menjalankan Suite Pengujian 4 Skenario Evaluasi Sistem (TestRunner)...\n");
        TestRunner.main(args);

        System.out.println("\n>> Menjalankan Demonstrasi Interaksi Objek End-to-End...");
        demoLiveTransaction();
    }

    private static void demoLiveTransaction() {
        try {
            System.out.println("\n--- [DEMO LIVE TRANSACTION: COLLABORATION OBJECT FLOW] ---");
            KatalogProduk katalog = new KatalogProduk();
            Produk p1 = new Produk("PRD-101", "Kopi Robusta Sumenep 250g", 35000.0, 15, "Minuman");
            Produk p2 = new Produk("PRD-102", "Gula Aren Asli Madura 500g", 25000.0, 20, "Sembako");
            katalog.tambahProduk(p1);
            katalog.tambahProduk(p2);

            Pelanggan pembeli = new Pelanggan("CUST-LIVE", "Arjuna Lanang Adiwarsana", "arjuna@pens.ac.id", "081234567890");
            Keranjang keranjang = new Keranjang(pembeli);

            System.out.printf("1. Menambahkan item ke keranjang belanja milik %s...%n", pembeli.getNama());
            keranjang.tambahItem(p1, 2); // 70.000
            keranjang.tambahItem(p2, 1); // 25.000
            System.out.printf("   Total Item: %d jenis, Total Tagihan: Rp %,.2f%n", keranjang.getJumlahVariasiItem(), keranjang.hitungTotal());

            System.out.println("2. Menyiapkan metode pembayaran Tunai (Cash Rp 100.000)...");
            PembayaranTunai cash = new PembayaranTunai(100000.0);

            System.out.println("3. Memproses checkout melalui LayananTransaksi...");
            LayananTransaksi layanan = new LayananTransaksi();
            Transaksi transaksi = layanan.prosesCheckout(keranjang, cash);

            System.out.println("4. Transaksi Sukses! Mencetak struk resmi:\n");
            System.out.println(transaksi.cetakStruk());

            System.out.printf("5. Audit Mutasi State Setelah Transaksi:%n");
            System.out.printf("   - Sisa Stok '%s': %d (Awal: 15)%n", p1.getNamaProduk(), p1.getStok());
            System.out.printf("   - Sisa Stok '%s': %d (Awal: 20)%n", p2.getNamaProduk(), p2.getStok());
            System.out.printf("   - Keranjang Kosong: %b%n", keranjang.isKosong());
            System.out.printf("   - Riwayat Transaksi Pelanggan: %d transaksi tercatat%n", pembeli.getRiwayatTransaksi().size());
            System.out.println("================================================================================");
            System.out.println(">> [DEMO SELESAI] Seluruh komponen arsitektur PBO berfungsi sempurna!");
            System.out.println("================================================================================");

        } catch (Exception e) {
            System.err.println("Demo gagal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
