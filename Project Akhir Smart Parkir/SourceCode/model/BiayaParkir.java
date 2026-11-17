package model;

import java.util.Locale;
import java.util.Objects;

/**
 * Value Object BiayaParkir merepresentasikan komponen finansial tagihan parkir (P14 Clean Refactoring).
 * 
 * Karakteristik Value Object:
 * - Immutable (atribut final, tanpa setter)
 * - Mengenkapsulasi kalkulasi subtotal, diskon, dan tagihan netto
 * - Menyediakan formatting mata uang Rupiah terstandarisasi untuk UI/Kuitansi
 * - Mencegah "Primitive Obsession" terkait double nominal uang
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public final class BiayaParkir {
    private final double subtotal;
    private final double potonganDiskon;
    private final double totalTagihan;
    private final String namaDiskon;

    private BiayaParkir(double subtotal, double potonganDiskon, String namaDiskon) {
        this.subtotal = Math.max(0.0, subtotal);
        double diskonValid = Math.max(0.0, potonganDiskon);
        // Potongan diskon tidak boleh melebihi subtotal
        if (diskonValid > this.subtotal) {
            diskonValid = this.subtotal;
        }
        this.potonganDiskon = diskonValid;
        this.totalTagihan = this.subtotal - this.potonganDiskon;
        this.namaDiskon = (namaDiskon != null && !namaDiskon.trim().isEmpty()) ? namaDiskon.trim() : "Tanpa Diskon";
    }

    public static BiayaParkir of(double subtotal, double potonganDiskon, String namaDiskon) {
        return new BiayaParkir(subtotal, potonganDiskon, namaDiskon);
    }

    public static BiayaParkir of(double subtotal) {
        return new BiayaParkir(subtotal, 0.0, "Tanpa Diskon");
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getPotonganDiskon() {
        return potonganDiskon;
    }

    public double getTotalTagihan() {
        return totalTagihan;
    }

    public String getNamaDiskon() {
        return namaDiskon;
    }

    public boolean isMendapatDiskon() {
        return potonganDiskon > 0.0;
    }

    public boolean isLunas(double nominalBayar) {
        return nominalBayar >= totalTagihan;
    }

    public double hitungKembalian(double nominalBayar) {
        if (!isLunas(nominalBayar)) {
            return 0.0;
        }
        return nominalBayar - totalTagihan;
    }

    public String formatSubtotal() {
        return formatRupiah(subtotal);
    }

    public String formatDiskon() {
        return formatRupiah(potonganDiskon);
    }

    public String formatTotalTagihan() {
        return formatRupiah(totalTagihan);
    }

    public String formatKembalian(double nominalBayar) {
        return formatRupiah(hitungKembalian(nominalBayar));
    }

    public static String formatRupiah(double nominal) {
        return String.format(Locale.US, "Rp %,.0f", nominal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BiayaParkir)) return false;
        BiayaParkir that = (BiayaParkir) o;
        return Double.compare(that.subtotal, subtotal) == 0 &&
               Double.compare(that.potonganDiskon, potonganDiskon) == 0 &&
               Double.compare(that.totalTagihan, totalTagihan) == 0 &&
               Objects.equals(namaDiskon, that.namaDiskon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subtotal, potonganDiskon, totalTagihan, namaDiskon);
    }

    @Override
    public String toString() {
        return String.format("BiayaParkir{tagihan=%s, subtotal=%s, diskon=%s (%s)}",
                formatTotalTagihan(), formatSubtotal(), formatDiskon(), namaDiskon);
    }
}
