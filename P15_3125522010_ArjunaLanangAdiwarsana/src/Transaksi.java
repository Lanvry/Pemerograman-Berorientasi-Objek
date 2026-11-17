import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entitas Transaksi yang merepresentasikan order final yang telah dibuat dan diproses.
 * Memiliki status siklus hidup (PENDING, BERHASIL, GAGAL, DIBATALKAN),
 * daftar item pesanan tidak dapat diubah (immutable snapshot), dan catatan audit pembayaran.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 * Politeknik Elektronika Negeri Surabaya (PENS) PSDKU Sumenep
 */
public class Transaksi {
    private final String idTransaksi;
    private final LocalDateTime waktuTransaksi;
    private final Pelanggan pelanggan;
    private final List<ItemPesanan> daftarItem;
    private final double totalBiaya;
    private final MetodePembayaran metodePembayaran;
    private StatusTransaksi status;
    private String catatan;

    public Transaksi(String idTransaksi, Pelanggan pelanggan, List<ItemPesanan> daftarItem, 
                     double totalBiaya, MetodePembayaran metodePembayaran) {
        if (idTransaksi == null || idTransaksi.trim().isEmpty()) {
            throw new IllegalArgumentException("ID Transaksi tidak boleh kosong.");
        }
        if (pelanggan == null) {
            throw new IllegalArgumentException("Pelanggan tidak boleh null.");
        }
        if (daftarItem == null || daftarItem.isEmpty()) {
            throw new IllegalArgumentException("Daftar item pesanan tidak boleh kosong.");
        }
        if (totalBiaya < 0) {
            throw new IllegalArgumentException("Total biaya tidak boleh bernilai negatif.");
        }
        if (metodePembayaran == null) {
            throw new IllegalArgumentException("Metode pembayaran harus ditentukan.");
        }

        this.idTransaksi = idTransaksi.trim();
        this.waktuTransaksi = LocalDateTime.now();
        this.pelanggan = pelanggan;
        // Defensive copying & unmodifiable list
        this.daftarItem = Collections.unmodifiableList(new ArrayList<>(daftarItem));
        this.totalBiaya = totalBiaya;
        this.metodePembayaran = metodePembayaran;
        this.status = StatusTransaksi.PENDING;
        this.catatan = "Menunggu konfirmasi pembayaran";
    }

    public String getIdTransaksi() {
        return idTransaksi;
    }

    public LocalDateTime getWaktuTransaksi() {
        return waktuTransaksi;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public List<ItemPesanan> getDaftarItem() {
        return daftarItem;
    }

    public double getTotalBiaya() {
        return totalBiaya;
    }

    public MetodePembayaran getMetodePembayaran() {
        return metodePembayaran;
    }

    public StatusTransaksi getStatus() {
        return status;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setStatusBerhasil(String referensi) {
        this.status = StatusTransaksi.BERHASIL;
        this.catatan = "Pembayaran diverifikasi. Referensi: " + referensi;
    }

    public void setStatusGagal(String alasan) {
        this.status = StatusTransaksi.GAGAL;
        this.catatan = "Gagal diproses: " + alasan;
    }

    public void setStatusDibatalkan(String alasan) {
        this.status = StatusTransaksi.DIBATALKAN;
        this.catatan = "Dibatalkan: " + alasan;
    }

    /**
     * Mencetak struk bukti transaksi secara terformat.
     */
    public String cetakStruk() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================\n");
        sb.append("                BUKTI TRANSAKSI PEMBAYARAN              \n");
        sb.append("        Toko Kelontong & Ritel PENS PSDKU Sumenep       \n");
        sb.append("========================================================\n");
        sb.append(String.format("ID Transaksi    : %s\n", idTransaksi));
        sb.append(String.format("Waktu           : %s\n", waktuTransaksi.format(dtf)));
        sb.append(String.format("Pelanggan       : %s (%s)\n", pelanggan.getNama(), pelanggan.getIdPelanggan()));
        sb.append(String.format("Metode Bayar    : %s\n", metodePembayaran.getNamaMetode()));
        sb.append(String.format("Status          : %s (%s)\n", status.name(), status.getDeskripsi()));
        sb.append(String.format("Keterangan      : %s\n", catatan));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("%-28s %-6s %-12s %-10s\n", "Nama Barang", "Qty", "Harga", "Subtotal"));
        sb.append("--------------------------------------------------------\n");
        for (ItemPesanan item : daftarItem) {
            sb.append(String.format("%-28s %-6d Rp %,9.2f Rp %,10.2f\n", 
                    item.getProduk().getNamaProduk(),
                    item.getKuantitas(),
                    item.getHargaSaatBeli(),
                    item.getSubtotal()));
        }
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format("TOTAL TAGIHAN   : Rp %,.2f\n", totalBiaya));
        sb.append("========================================================\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaksi transaksi = (Transaksi) o;
        return Objects.equals(idTransaksi, transaksi.idTransaksi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTransaksi);
    }

    @Override
    public String toString() {
        return String.format("Transaksi[%s | %s | Rp %,.2f | %s]", 
                idTransaksi, pelanggan.getNama(), totalBiaya, status);
    }
}
