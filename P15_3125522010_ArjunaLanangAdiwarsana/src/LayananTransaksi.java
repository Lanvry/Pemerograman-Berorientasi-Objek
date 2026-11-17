import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Layanan orkestrator yang mengoordinasikan alur checkout dan transaksi (Object Collaboration).
 * Memastikan Transactional Consistency (konsistensi transaksional ACID):
 * Jika proses pembayaran gagal, stok produk yang sempat dipotong akan secara otomatis
 * di-rollback (dikembalikan) ke keadaan semula tanpa inkonsistensi data.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class LayananTransaksi {
    private static final AtomicInteger COUNTER = new AtomicInteger(1000);
    private final List<Transaksi> logTransaksiGlobal;

    public LayananTransaksi() {
        this.logTransaksiGlobal = new ArrayList<>();
    }

    /**
     * Menghasilkan ID unik untuk transaksi.
     */
    private String generateIdTransaksi() {
        String tanggal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("TRX-%s-%04d", tanggal, COUNTER.incrementAndGet());
    }

    /**
     * Memproses checkout keranjang belanja pelanggan dengan metode pembayaran tertentu.
     * 
     * @param keranjang keranjang belanja milik pelanggan
     * @param metodePembayaran metode pembayaran yang dipilih
     * @return objek Transaksi yang berhasil diverifikasi
     * @throws StokTidakMencukupiException jika ada produk yang stoknya habis/kurang
     * @throws PembayaranGagalException jika otorisasi atau pembayaran gagal
     */
    public synchronized Transaksi prosesCheckout(Keranjang keranjang, MetodePembayaran metodePembayaran) 
            throws StokTidakMencukupiException, PembayaranGagalException {
        
        if (keranjang == null) {
            throw new IllegalArgumentException("Keranjang belanja tidak boleh null.");
        }
        if (keranjang.isKosong()) {
            throw new IllegalStateException("Tidak dapat checkout keranjang yang kosong!");
        }
        if (metodePembayaran == null) {
            throw new IllegalArgumentException("Metode pembayaran harus ditentukan.");
        }

        Pelanggan pelanggan = keranjang.getPemilik();
        List<ItemPesanan> daftarItem = keranjang.getDaftarItem();
        double totalTagihan = keranjang.hitungTotal();
        String idTransaksi = generateIdTransaksi();

        Transaksi transaksi = new Transaksi(idTransaksi, pelanggan, daftarItem, totalTagihan, metodePembayaran);

        // Tahap 1: Alokasi & Pengurangan Stok (dengan pelacak rollback)
        List<ItemPesanan> itemTerpotong = new ArrayList<>();
        try {
            for (ItemPesanan item : daftarItem) {
                Produk produk = item.getProduk();
                produk.kurangiStok(item.getKuantitas());
                itemTerpotong.add(item);
            }
        } catch (StokTidakMencukupiException e) {
            // Rollback stok yang sudah terlanjur dipotong sebelum exception terjadi
            for (ItemPesanan dipotong : itemTerpotong) {
                dipotong.getProduk().tambahStok(dipotong.getKuantitas());
            }
            transaksi.setStatusDibatalkan("Stok produk tidak mencukupi: " + e.getMessage());
            logTransaksiGlobal.add(transaksi);
            throw e;
        }

        // Tahap 2: Eksekusi Pembayaran
        try {
            String referensiBayar = metodePembayaran.prosesPembayaran(idTransaksi, totalTagihan);
            transaksi.setStatusBerhasil(referensiBayar);
            
            // Tahap 3: Penyelesaian Sukses
            pelanggan.tambahRiwayatTransaksi(transaksi);
            logTransaksiGlobal.add(transaksi);
            keranjang.kosongkan();
            
            return transaksi;
        } catch (PembayaranGagalException e) {
            // ROLLBACK STOK: Kembalikan seluruh stok produk yang telah dipotong karena pembayaran gagal
            for (ItemPesanan dipotong : itemTerpotong) {
                dipotong.getProduk().tambahStok(dipotong.getKuantitas());
            }
            transaksi.setStatusGagal(e.getAlasan());
            pelanggan.tambahRiwayatTransaksi(transaksi);
            logTransaksiGlobal.add(transaksi);
            
            // Lemparkan kembali agar caller dapat menangani respons kegagalan
            throw e;
        }
    }

    /**
     * Mendapatkan log seluruh transaksi toko secara aman (unmodifiable list).
     */
    public synchronized List<Transaksi> getLogTransaksiGlobal() {
        return Collections.unmodifiableList(logTransaksiGlobal);
    }
}
