package model;

/**
 * Class SlotParkir merepresentasikan satu petak tempat parkir fisik dalam gedung.
 * Bertanggung jawab menjaga status ketersediaan dan validitas kendaraan yang diparkir.
 */
public class SlotParkir {
    private String idSlot;       // Contoh: "L1-A01", "L1-EV01", "L2-B02"
    private int lantai;
    private String tipeSlot;     // "MOTOR", "MOBIL", "MOBIL_LISTRIK"
    private boolean terisi;
    private Kendaraan kendaraanParkir;

    public SlotParkir(String idSlot, int lantai, String tipeSlot) {
        if (idSlot == null || idSlot.trim().isEmpty()) {
            throw new IllegalArgumentException("ID Slot tidak boleh kosong!");
        }
        if (lantai < 1) {
            throw new IllegalArgumentException("Lantai parkir minimal lantai 1!");
        }
        if (tipeSlot == null || tipeSlot.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipe slot tidak boleh kosong!");
        }
        String tipeUpper = tipeSlot.trim().toUpperCase();
        if (!tipeUpper.equals("MOTOR") && !tipeUpper.equals("MOBIL") && !tipeUpper.equals("MOBIL_LISTRIK")) {
            throw new IllegalArgumentException("Tipe slot tidak valid! Pilih: MOTOR, MOBIL, atau MOBIL_LISTRIK");
        }
        this.idSlot = idSlot.trim().toUpperCase();
        this.lantai = lantai;
        this.tipeSlot = tipeUpper;
        this.terisi = false;
        this.kendaraanParkir = null;
    }

    public boolean cocokUntuk(Kendaraan k) {
        if (k == null) {
            return false;
        }
        // Slot MOBIL_LISTRIK hanya untuk MOBIL_LISTRIK
        // Slot MOBIL untuk MOBIL atau MOBIL_LISTRIK
        // Slot MOTOR hanya untuk MOTOR
        if ("MOTOR".equals(tipeSlot)) {
            return "MOTOR".equals(k.getTipe());
        } else if ("MOBIL_LISTRIK".equals(tipeSlot)) {
            return k.isMobilListrik();
        } else if ("MOBIL".equals(tipeSlot)) {
            return "MOBIL".equals(k.getTipe()) || k.isMobilListrik();
        }
        return false;
    }

    public boolean isiSlot(Kendaraan k) {
        if (this.terisi) {
            return false; // Slot sudah terisi
        }
        if (!cocokUntuk(k)) {
            return false; // Jenis kendaraan tidak sesuai dengan peruntukan slot
        }
        this.kendaraanParkir = k;
        this.terisi = true;
        return true;
    }

    public Kendaraan kosongkanSlot() {
        if (!this.terisi) {
            return null;
        }
        Kendaraan kKeluar = this.kendaraanParkir;
        this.kendaraanParkir = null;
        this.terisi = false;
        return kKeluar;
    }

    public String getIdSlot() {
        return idSlot;
    }

    public int getLantai() {
        return lantai;
    }

    public String getTipeSlot() {
        return tipeSlot;
    }

    public boolean isTerisi() {
        return terisi;
    }

    public Kendaraan getKendaraanParkir() {
        return kendaraanParkir;
    }

    @Override
    public String toString() {
        String infoKendaraan = terisi ? ("TERISI [" + kendaraanParkir.getPlatNomor() + "]") : "KOSONG";
        return String.format("[%s] Lt.%d (%s) -> %s", idSlot, lantai, tipeSlot, infoKendaraan);
    }
}
