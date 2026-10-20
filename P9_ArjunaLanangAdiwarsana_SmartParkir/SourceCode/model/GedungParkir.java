package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class GedungParkir bertindak sebagai Aggregate Root / Central Coordinator.
 * Memiliki responsibility terbesar dalam domain:
 * - Mengelola koleksi SlotParkir (Komposisi)
 * - Mengelola riwayat KarcisParkir (Komposisi)
 * - Mencarikan slot kosong terdekat secara cerdas (Automated Slot Allocation)
 * - Mengatur alur Check-In dan Check-Out kendaraan
 * - Mengakumulasi total pendapatan gedung
 */
public class GedungParkir {
    private String namaGedung;
    private List<SlotParkir> daftarSlot;
    private List<KarcisParkir> riwayatTransaksi;
    private double tarifJamPertamaMobil;
    private double tarifPerJamMobil;
    private double tarifJamPertamaMotor;
    private double tarifPerJamMotor;
    private double totalPendapatan;
    private int counterTiket;

    public GedungParkir(String namaGedung,
                        double tarifJamPertamaMobil, double tarifPerJamMobil,
                        double tarifJamPertamaMotor, double tarifPerJamMotor) {
        if (namaGedung == null || namaGedung.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama gedung tidak boleh kosong!");
        }
        this.namaGedung = namaGedung.trim();
        this.daftarSlot = new ArrayList<>();
        this.riwayatTransaksi = new ArrayList<>();
        this.tarifJamPertamaMobil = tarifJamPertamaMobil;
        this.tarifPerJamMobil = tarifPerJamMobil;
        this.tarifJamPertamaMotor = tarifJamPertamaMotor;
        this.tarifPerJamMotor = tarifPerJamMotor;
        this.totalPendapatan = 0.0;
        this.counterTiket = 1;
    }

    public void tambahSlot(SlotParkir slot) {
        if (slot != null) {
            daftarSlot.add(slot);
        }
    }

    public SlotParkir cariSlotTersedia(Kendaraan k) {
        if (k == null) {
            return null;
        }

        // Jika Mobil Listrik, coba prioritaskan slot khusus MOBIL_LISTRIK (EV Charger) terlebih dahulu
        if (k.isMobilListrik()) {
            for (SlotParkir slot : daftarSlot) {
                if (!slot.isTerisi() && "MOBIL_LISTRIK".equals(slot.getTipeSlot())) {
                    return slot;
                }
            }
        }

        // Prioritaskan mencari slot dari lantai terendah yang masih KOSONG dan COCOK
        SlotParkir kandidat = null;
        for (SlotParkir slot : daftarSlot) {
            if (!slot.isTerisi() && slot.cocokUntuk(k)) {
                if (kandidat == null || slot.getLantai() < kandidat.getLantai()) {
                    kandidat = slot;
                    break; // Ambil slot kosong pertama yang cocok
                }
            }
        }
        return kandidat;
    }

    public KarcisParkir prosesMasuk(Kendaraan k, int jamMasuk) {
        if (k == null) {
            System.out.println("[CHECK-IN GAGAL] Data kendaraan tidak valid.");
            return null;
        }

        SlotParkir slotTersedia = cariSlotTersedia(k);
        if (slotTersedia == null) {
            System.out.printf("[PARKIR PENUH] Tidak ada slot kosong yang tersedia untuk kendaraan %s!%n", k);
            return null;
        }

        // Isi slot secara fisik
        boolean berhasilIsi = slotTersedia.isiSlot(k);
        if (!berhasilIsi) {
            System.out.println("[CHECK-IN GAGAL] Gagal menempati slot parkir.");
            return null;
        }

        // Terbitkan tiket
        String noTiket = String.format("TKT-%04d", counterTiket++);
        KarcisParkir tiket = new KarcisParkir(noTiket, k, slotTersedia, jamMasuk);
        riwayatTransaksi.add(tiket);

        System.out.printf("[CHECK-IN SUKSES] %s berhasil masuk.%n", k);
        System.out.printf(" >> Tiket: %s | Alokasi Slot: %s (Lantai %d)%n",
                tiket.getNomorTiket(), slotTersedia.getIdSlot(), slotTersedia.getLantai());
        return tiket;
    }

    public boolean prosesKeluar(KarcisParkir tiket, int jamKeluar, String metode, double uangBayar) {
        if (tiket == null) {
            System.out.println("[CHECK-OUT GAGAL] Tiket parkir tidak ditemukan.");
            return false;
        }

        // Tentukan tarif dasar berdasarkan jenis kendaraan
        double tarifPertama = "MOTOR".equals(tiket.getKendaraan().getTipe()) ? tarifJamPertamaMotor : tarifJamPertamaMobil;
        double tarifBerikutnya = "MOTOR".equals(tiket.getKendaraan().getTipe()) ? tarifPerJamMotor : tarifPerJamMobil;

        // Hitung total tagihan
        tiket.hitungTarif(jamKeluar, tarifPertama, tarifBerikutnya);

        // Proses pembayaran
        boolean lunas = tiket.prosesPembayaran(metode, uangBayar);
        if (!lunas) {
            System.out.println("[GATE TETAP TERTUTUP] Pembayaran belum selesai. Portal keluar tidak dapat dibuka.");
            return false;
        }

        // Kosongkan slot fisik tempat kendaraan tadi parkir
        SlotParkir slot = tiket.getSlot();
        slot.kosongkanSlot();

        // Akumulasi kas pendapatan
        this.totalPendapatan += tiket.getTotalBiaya();

        System.out.printf("[CHECK-OUT SUKSES] Kendaraan %s telah keluar.%n", tiket.getKendaraan().getPlatNomor());
        System.out.printf(" >> Slot %s sekarang KOSONG kembali dan siap digunakan.%n", slot.getIdSlot());
        return true;
    }

    public void tampilkanStatusKapasitas() {
        int totalMotor = 0, terisiMotor = 0;
        int totalMobil = 0, terisiMobil = 0;
        int totalEV = 0, terisiEV = 0;

        for (SlotParkir s : daftarSlot) {
            if ("MOTOR".equals(s.getTipeSlot())) {
                totalMotor++;
                if (s.isTerisi()) terisiMotor++;
            } else if ("MOBIL_LISTRIK".equals(s.getTipeSlot())) {
                totalEV++;
                if (s.isTerisi()) terisiEV++;
            } else if ("MOBIL".equals(s.getTipeSlot())) {
                totalMobil++;
                if (s.isTerisi()) terisiMobil++;
            }
        }

        System.out.println("==========================================================================");
        System.out.printf("              STATUS OKUPANSI REALTIME - %s              %n", namaGedung.toUpperCase());
        System.out.println("==========================================================================");
        System.out.printf(" • Slot Motor           : Terisi %d/%d (Tersedia: %d)%n",
                terisiMotor, totalMotor, (totalMotor - terisiMotor));
        System.out.printf(" • Slot Mobil Reguler   : Terisi %d/%d (Tersedia: %d)%n",
                terisiMobil, totalMobil, (totalMobil - terisiMobil));
        System.out.printf(" • Slot Mobil Listrik   : Terisi %d/%d (Tersedia: %d)%n",
                terisiEV, totalEV, (totalEV - terisiEV));
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf(" Total Akumulasi Kas Masuk : Rp %,.0f%n", totalPendapatan);
        System.out.println("==========================================================================\n");
    }

    public void cetakStrukKeluar(KarcisParkir tiket) {
        if (tiket == null) return;
        int durasi = tiket.hitungDurasi(tiket.getJamKeluar());
        System.out.println("==========================================================================");
        System.out.printf("                     KUITANSI PARKIR - %s                   %n", namaGedung.toUpperCase());
        System.out.println("==========================================================================");
        System.out.printf(" No. Tiket    : %-18s  Metode Bayar : %s%n", tiket.getNomorTiket(), tiket.getMetodePembayaran());
        System.out.printf(" Kendaraan    : %s (%s - %s)%n",
                tiket.getKendaraan().getPlatNomor(), tiket.getKendaraan().getTipe(), tiket.getKendaraan().getMerk());
        System.out.printf(" Lokasi Parkir: Slot %s (Lantai %d)%n", tiket.getSlot().getIdSlot(), tiket.getSlot().getLantai());
        System.out.printf(" Waktu Parkir : %02d:00 s/d %02d:00 (Durasi: %d Jam)%n",
                tiket.getJamMasuk(), tiket.getJamKeluar(), durasi);
        if (tiket.getKendaraan().isMobilListrik()) {
            System.out.println(" Catatan      : Mendapatkan Insentif Hijau Diskon 20% (EV Friendly)");
        }
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf(" TOTAL TAGIHAN: Rp %,.0f  [STATUS: LUNAS - GATE OPEN]%n", tiket.getTotalBiaya());
        System.out.println("==========================================================================\n");
    }

    public String getNamaGedung() {
        return namaGedung;
    }

    public List<SlotParkir> getDaftarSlot() {
        return Collections.unmodifiableList(daftarSlot);
    }

    public List<KarcisParkir> getRiwayatTransaksi() {
        return Collections.unmodifiableList(riwayatTransaksi);
    }

    public double getTotalPendapatan() {
        return totalPendapatan;
    }

    public double getTarifJamPertamaMobil() {
        return tarifJamPertamaMobil;
    }

    public double getTarifPerJamMobil() {
        return tarifPerJamMobil;
    }

    public double getTarifJamPertamaMotor() {
        return tarifJamPertamaMotor;
    }

    public double getTarifPerJamMotor() {
        return tarifPerJamMotor;
    }
}
