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
        DatabaseManager.inisialisasiDatabase();
        this.counterTiket = DatabaseManager.getNextTiketCounter();
        this.totalPendapatan = DatabaseManager.hitungTotalPendapatanDB();
    }

    public void tambahSlot(SlotParkir slot) {
        if (slot != null) {
            daftarSlot.add(slot);
            DatabaseManager.updateStatusSlot(slot);
        }
    }

    public void tambahDaftarSlot(List<SlotParkir> slots) {
        if (slots != null && !slots.isEmpty()) {
            daftarSlot.addAll(slots);
            DatabaseManager.simpanBatchSlot(slots);
        }
    }

    public int getSisaKuota(String tipeKendaraan) {
        if (tipeKendaraan == null) return 0;
        String t = tipeKendaraan.trim().toUpperCase();
        int count = 0;
        for (SlotParkir s : daftarSlot) {
            if (!s.isTerisi() && s.getTipeSlot().equals(t)) {
                count++;
            }
        }
        return count;
    }

    public int getSisaKuotaLantai(int lantai, String tipeKendaraan) {
        if (tipeKendaraan == null) return 0;
        String t = tipeKendaraan.trim().toUpperCase();
        int count = 0;
        for (SlotParkir s : daftarSlot) {
            if (!s.isTerisi() && s.getLantai() == lantai && s.getTipeSlot().equals(t)) {
                count++;
            }
        }
        return count;
    }

    public KarcisParkir cariTiketAktif(String query) {
        if (query == null || query.trim().isEmpty()) return null;
        String q = query.trim().toUpperCase();
        KarcisParkir tPlat = cariTiketAktifDenganPlat(q);
        if (tPlat != null) return tPlat;
        for (int i = riwayatTransaksi.size() - 1; i >= 0; i--) {
            KarcisParkir t = riwayatTransaksi.get(i);
            if (!t.isSudahDibayar() && t.getNomorTiket().equalsIgnoreCase(q)) {
                return t;
            }
        }
        return null;
    }

    public KarcisParkir prosesMasukTicketless(Kendaraan k, int jamMasuk) {
        if (k == null) {
            throw new IllegalArgumentException("Kendaraan tidak boleh null!");
        }
        // Validasi anti double check-in
        if (cariTiketAktifDenganPlat(k.getPlatNomor()) != null) {
            System.out.println("[GATE REJECTED] Kendaraan plat " + k.getPlatNomor() + " sudah terdaftar aktif di dalam gedung!");
            return null;
        }

        SlotParkir slotTersedia = cariSlotTersedia(k);
        if (slotTersedia == null) {
            System.out.println("[KAPASITAS PENUH] Tidak ada slot tersedia untuk tipe: " + k.getTipe());
            return null;
        }

        slotTersedia.isiSlot(k);
        counterTiket++;
        String nomorTiket = String.format("TKT-%04d", counterTiket);
        KarcisParkir tiket = new KarcisParkir(nomorTiket, k, slotTersedia, jamMasuk);
        riwayatTransaksi.add(tiket);

        DatabaseManager.simpanKendaraan(k);
        DatabaseManager.updateStatusSlot(slotTersedia);
        DatabaseManager.simpanTransaksiMasuk(tiket, "ANPR_TICKETLESS");
        return tiket;
    }

    public KarcisParkir prosesMasukEmergency(String tipeKendaraan, int jamMasuk) throws KapasitasPenuhException {
        String tipe = (tipeKendaraan != null && !tipeKendaraan.trim().isEmpty()) ? tipeKendaraan.toUpperCase() : "MOBIL";
        counterTiket++;
        String emgPlate = String.format("EMG-%04d", counterTiket);
        Kendaraan kEmg = new Kendaraan(emgPlate, tipe, "MANUAL_QR");
        
        SlotParkir slotTersedia = cariSlotTersedia(kEmg);
        if (slotTersedia == null) {
            System.out.println("[KAPASITAS PENUH] Tidak ada slot tersedia untuk tiket darurat!");
            throw new KapasitasPenuhException(tipe);
        }

        slotTersedia.isiSlot(kEmg);
        String nomorTiket = String.format("EMG-%04d", counterTiket);
        KarcisParkir tiket = new KarcisParkir(nomorTiket, kEmg, slotTersedia, jamMasuk);
        riwayatTransaksi.add(tiket);

        DatabaseManager.simpanKendaraan(kEmg);
        DatabaseManager.updateStatusSlot(slotTersedia);
        DatabaseManager.simpanTransaksiMasuk(tiket, "EMERGENCY_QR");
        return tiket;
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
                    if (kandidat.getLantai() == 1) {
                        break; // Lantai 1 adalah lantai terendah, optimal berhenti di sini
                    }
                }
            }
        }
        return kandidat;
    }

    public SlotParkir cariSlotDenganPlat(String plat) {
        if (plat == null || plat.trim().isEmpty()) return null;
        String p = plat.replaceAll("\\s+", "").toUpperCase();
        for (SlotParkir s : daftarSlot) {
            if (s.isTerisi() && s.getKendaraanParkir() != null) {
                String platParkir = s.getKendaraanParkir().getPlatNomor().replaceAll("\\s+", "").toUpperCase();
                if (platParkir.equals(p)) {
                    return s;
                }
            }
        }
        return null;
    }

    public KarcisParkir cariTiketAktifDenganPlat(String plat) {
        if (plat == null || plat.trim().isEmpty()) return null;
        String p = plat.replaceAll("\\s+", "").toUpperCase();
        for (int i = riwayatTransaksi.size() - 1; i >= 0; i--) {
            KarcisParkir t = riwayatTransaksi.get(i);
            if (!t.isSudahDibayar() && t.getKendaraan() != null) {
                String platTiket = t.getKendaraan().getPlatNomor().replaceAll("\\s+", "").toUpperCase();
                if (platTiket.equals(p)) {
                    return t;
                }
            }
        }
        return null;
    }

    public KarcisParkir cariTiketAktifDenganSlot(String idSlot) {
        if (idSlot == null || idSlot.trim().isEmpty()) return null;
        String s = idSlot.trim().toUpperCase();
        for (int i = riwayatTransaksi.size() - 1; i >= 0; i--) {
            KarcisParkir t = riwayatTransaksi.get(i);
            if (!t.isSudahDibayar() && t.getSlot() != null) {
                if (t.getSlot().getIdSlot().equalsIgnoreCase(s)) {
                    return t;
                }
            }
        }
        return null;
    }

    public List<KarcisParkir> getDaftarTiketAktif() {
        List<KarcisParkir> aktif = new ArrayList<>();
        for (KarcisParkir t : riwayatTransaksi) {
            if (!t.isSudahDibayar()) {
                aktif.add(t);
            }
        }
        return aktif;
    }

    public KarcisParkir prosesMasuk(Kendaraan k, int jamMasuk) throws KapasitasPenuhException, KendaraanSudahParkirException {
        if (k == null) {
            throw new IllegalArgumentException("Data kendaraan tidak boleh null!");
        }

        // Cegah duplikasi: Cek apakah kendaraan dengan plat yang sama sudah ada di dalam gedung
        SlotParkir slotSudahAda = cariSlotDenganPlat(k.getPlatNomor());
        if (slotSudahAda != null) {
            System.out.printf("[CHECK-IN DITOLAK] Kendaraan plat %s sudah terparkir di slot %s!%n",
                k.getPlatNomor(), slotSudahAda.getIdSlot());
            throw new KendaraanSudahParkirException(k.getPlatNomor(), slotSudahAda.getIdSlot());
        }

        SlotParkir slotTersedia = cariSlotTersedia(k);
        if (slotTersedia == null) {
            System.out.printf("[PARKIR PENUH] Tidak ada slot kosong yang tersedia untuk kendaraan %s!%n", k);
            throw new KapasitasPenuhException(k.getTipe());
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

        // Persistensi ke Database MySQL
        DatabaseManager.simpanKendaraan(k);
        DatabaseManager.updateStatusSlot(slotTersedia);
        DatabaseManager.simpanKarcisMasuk(tiket);

        System.out.printf("[CHECK-IN SUKSES] %s berhasil masuk.%n", k);
        System.out.printf(" >> Tiket: %s | Alokasi Slot: %s (Lantai %d)%n",
                tiket.getNomorTiket(), slotTersedia.getIdSlot(), slotTersedia.getLantai());
        return tiket;
    }

    public boolean prosesKeluar(KarcisParkir tiket, int jamKeluar, String metode, double uangBayar) throws TiketTidakDitemukanException, PembayaranKurangException {
        if (tiket == null) {
            System.out.println("[CHECK-OUT GAGAL] Tiket parkir tidak ditemukan.");
            throw new TiketTidakDitemukanException("Tiket null");
        }

        if (tiket.isSudahDibayar()) {
            System.out.printf("[CHECK-OUT DITOLAK] Tiket %s sudah lunas/keluar sebelumnya.%n", tiket.getNomorTiket());
            throw new TiketTidakDitemukanException("Tiket " + tiket.getNomorTiket() + " sudah diselesaikan sebelumnya.");
        }

        // Tentukan tarif dasar berdasarkan jenis kendaraan
        double tarifPertama = "MOTOR".equals(tiket.getKendaraan().getTipe()) ? tarifJamPertamaMotor : tarifJamPertamaMobil;
        double tarifBerikutnya = "MOTOR".equals(tiket.getKendaraan().getTipe()) ? tarifPerJamMotor : tarifPerJamMobil;

        // Hitung total tagihan
        tiket.hitungTarif(jamKeluar, tarifPertama, tarifBerikutnya);

        // Proses pembayaran (melempar PembayaranKurangException jika nominal kurang)
        boolean lunas = tiket.prosesPembayaran(metode, uangBayar);
        if (!lunas) {
            System.out.println("[GATE TETAP TERTUTUP] Pembayaran belum selesai. Portal keluar tidak dapat dibuka.");
            return false;
        }

        // Kosongkan slot fisik tempat kendaraan tadi parkir
        SlotParkir slot = tiket.getSlot();
        if (slot != null) {
            slot.kosongkanSlot();
            DatabaseManager.updateStatusSlot(slot);
        }

        // Akumulasi kas pendapatan
        this.totalPendapatan += tiket.getTotalBiaya();

        // Persistensi ke Database MySQL
        DatabaseManager.updateKarcisKeluar(tiket);

        System.out.printf("[CHECK-OUT SUKSES] Kendaraan %s telah keluar.%n", tiket.getKendaraan().getPlatNomor());
        if (slot != null) {
            System.out.printf(" >> Slot %s sekarang KOSONG kembali dan siap digunakan.%n", slot.getIdSlot());
        }
        return true;
    }

    public boolean prosesKeluarDenganPlat(String plat, int jamKeluar, String metode, double uangBayar) throws TiketTidakDitemukanException, PembayaranKurangException {
        KarcisParkir tiket = cariTiketAktifDenganPlat(plat);
        if (tiket == null) {
            throw new TiketTidakDitemukanException(plat);
        }
        return prosesKeluar(tiket, jamKeluar, metode, uangBayar);
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
