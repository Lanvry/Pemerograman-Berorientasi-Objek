package model;

/**
 * Exception yang dilempar ketika nominal pembayaran yang diberikan
 * lebih kecil dari total tarif tagihan parkir yang harus dilunasi.
 */
public class PembayaranKurangException extends Exception {
    private final double totalTagihan;
    private final double nominalBayar;
    private final double kekurangan;

    public PembayaranKurangException(double totalTagihan, double nominalBayar) {
        super(String.format("Pembayaran ditolak! Tagihan: Rp %,.0f, Nominal bayar: Rp %,.0f (Kurang: Rp %,.0f)",
                totalTagihan, nominalBayar, (totalTagihan - nominalBayar)));
        this.totalTagihan = totalTagihan;
        this.nominalBayar = nominalBayar;
        this.kekurangan = Math.max(0, totalTagihan - nominalBayar);
    }

    public double getTotalTagihan() {
        return totalTagihan;
    }

    public double getNominalBayar() {
        return nominalBayar;
    }

    public double getKekurangan() {
        return kekurangan;
    }
}
