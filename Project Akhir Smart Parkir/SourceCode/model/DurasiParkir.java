package model;

import java.util.Objects;

/**
 * Value Object DurasiParkir merepresentasikan durasi waktu parkir kendaraan (P14 Clean Refactoring).
 * 
 * Karakteristik Value Object:
 * - Immutable (atribut final, tidak memiliki setter)
 * - Self-validating (validasi jam masuk & jam keluar pada konstruktor)
 * - Mengenkapsulasi aturan bisnis durasi (rollover tengah malam & minimal 1 jam)
 * - Implementasi equals(), hashCode(), dan toString() representatif
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public final class DurasiParkir {
    private final int jamMasuk;
    private final int jamKeluar;
    private final int totalJam;

    private DurasiParkir(int jamMasuk, int jamKeluar) {
        if (jamMasuk < 0 || jamMasuk > 23) {
            throw new IllegalArgumentException("Jam masuk harus berada dalam rentang 0-23! Diterima: " + jamMasuk);
        }
        if (jamKeluar < 0 || jamKeluar > 24) {
            throw new IllegalArgumentException("Jam keluar harus berada dalam rentang 0-24! Diterima: " + jamKeluar);
        }
        this.jamMasuk = jamMasuk;
        this.jamKeluar = jamKeluar;
        this.totalJam = hitungTotalJamInternal(jamMasuk, jamKeluar);
    }

    public static DurasiParkir of(int jamMasuk, int jamKeluar) {
        return new DurasiParkir(jamMasuk, jamKeluar);
    }

    private static int hitungTotalJamInternal(int masuk, int keluar) {
        if (keluar < masuk) {
            // Simulasi melewati tengah malam (misal masuk 23, keluar 2 -> 3 jam)
            return (24 - masuk) + keluar;
        }
        int selisih = keluar - masuk;
        return Math.max(1, selisih); // Minimal dihitung 1 jam
    }

    public int getJamMasuk() {
        return jamMasuk;
    }

    public int getJamKeluar() {
        return jamKeluar;
    }

    public int getTotalJam() {
        return totalJam;
    }

    public String getRentangWaktu() {
        return String.format("%02d:00 s/d %02d:00", jamMasuk, jamKeluar);
    }

    public String toDisplayString() {
        return totalJam + " Jam";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DurasiParkir)) return false;
        DurasiParkir that = (DurasiParkir) o;
        return jamMasuk == that.jamMasuk && jamKeluar == that.jamKeluar && totalJam == that.totalJam;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jamMasuk, jamKeluar, totalJam);
    }

    @Override
    public String toString() {
        return String.format("%s (%d Jam)", getRentangWaktu(), totalJam);
    }
}
