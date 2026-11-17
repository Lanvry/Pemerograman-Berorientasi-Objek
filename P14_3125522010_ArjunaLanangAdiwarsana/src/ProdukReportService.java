import java.util.List;

/**
 * Service: ProdukReportService
 * 
 * Hasil ekstraksi SRP dari God Class Produk (Kasus B).
 * Bertanggung jawab khusus untuk memformat, menganalisis, dan mencetak laporan katalog produk,
 * inventaris stok, serta ringkasan nilai aset.
 * 
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public class ProdukReportService {

    public Money calculateTotalInventoryValuation(List<Produk> produkList) {
        Money totalAssetValue = Money.ZERO;
        if (produkList == null) return totalAssetValue;
        for (Produk p : produkList) {
            totalAssetValue = totalAssetValue.add(p.getPrice().multiply(p.getStock()));
        }
        return totalAssetValue;
    }

    public String generateCatalogSummary(List<Produk> produkList) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append(String.format(" %-12s | %-28s | %-14s | %-16s | %-6s\n", 
                "KODE SKU", "NAMA PRODUK", "KATEGORI", "HARGA SATUAN", "STOK"));
        sb.append("========================================================================================\n");

        Money totalAssetValue = calculateTotalInventoryValuation(produkList);
        int totalUnits = 0;

        for (Produk p : produkList) {
            sb.append(String.format(" %-12s | %-28s | %-14s | %-16s | %-6d\n",
                    p.getCode().getValue(),
                    truncate(p.getName(), 28),
                    p.getCategory(),
                    p.getPrice().formatRupiah(),
                    p.getStock()));
            totalUnits += p.getStock();
        }

        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format(" Total Variasi Produk: %d SKU | Total Fisik: %d unit\n", produkList.size(), totalUnits));
        sb.append(String.format(" Estimasi Total Nilai Inventaris: %s\n", totalAssetValue.formatRupiah()));
        sb.append("========================================================================================\n");
        return sb.toString();
    }

    public String generateLowStockAlert(List<Produk> lowStockList, int threshold) {
        StringBuilder sb = new StringBuilder();
        sb.append(">>> PERINGATAN INVENTARIS: PRODUK DI BAWAH THRESHOLD (<= ").append(threshold).append(" UNIT) <<<\n");
        if (lowStockList.isEmpty()) {
            sb.append("Semua stok produk berada di level aman.\n");
            return sb.toString();
        }
        for (Produk p : lowStockList) {
            sb.append(String.format(" [!] Kritis: %s (%s) - Sisa hanya %d unit!\n",
                    p.getName(), p.getCode(), p.getStock()));
        }
        return sb.toString();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 3) + "...";
    }
}
