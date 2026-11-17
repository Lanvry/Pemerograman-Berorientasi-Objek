package model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class GedungParkir bertindak sebagai Aggregate Root / Central Coordinator.
 * 
 * Prinsip SOLID yang diterapkan:
 * - SRP (Single Responsibility Principle): Mengorkestrasikan alur bisnis parkir, delegasi penyimpanan
 *   ke IKendaraanRepository, kalkulasi diskon ke DiskonStrategy, dan pembayaran ke IPaymentProcessor.
 * - OCP (Open-Closed Principle): Terbuka terhadap penambahan skema diskon baru atau gateway pembayaran
 *   baru tanpa memodifikasi method prosesKeluar() maupun alur operasional utama.
 * - LSP (Liskov Substitution Principle): Subtipe DiskonStrategy dan IPaymentProcessor dapat saling
 *   menggantikan dengan aman.
 * - ISP (Interface Segregation Principle): Mengimplementasikan interface terpisah dan ramping:
 *   - IParkingGateController (kontrol buka/tutup palang gerbang dan alur masuk)
 *   - IParkingBilling (kalkulasi tagihan, skema diskon, dan check-out pembayaran)
 *   - IArmadaManager (pendaftaran dan manajemen koleksi kendaraan terdaftar)
 * - DIP (Dependency Inversion Principle): Bergantung pada abstraksi IKendaraanRepository melalui
 *   Constructor Injection & Setter Injection, bukan implementasi file/database fisik secara langsung.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class GedungParkir implements IParkingGateController, IParkingBilling, IArmadaManager {
    private String namaGedung;
    private List<SlotParkir> daftarSlot;
    private List<Kendaraan> daftarKendaraan;
    private List<KarcisParkir> riwayatTransaksi;
    private double tarifJamPertamaMobil;
    private double tarifPerJamMobil;
    private double tarifJamPertamaMotor;
    private double tarifPerJamMotor;
    private double totalPendapatan;
    private int counterTiket;

    // DIP: Dependensi pada Abstraksi IKendaraanRepository
    private IKendaraanRepository kendaraanRepository;

    // ISP: State Kontrol Palang Gerbang
    private boolean gerbangTerbuka;
    private String statusGerbang;

    public GedungParkir(String namaGedung,
                        double tarifJamPertamaMobil, double tarifPerJamMobil,
                        double tarifJamPertamaMotor, double tarifPerJamMotor) {
        this(namaGedung, tarifJamPertamaMobil, tarifPerJamMobil, tarifJamPertamaMotor, tarifPerJamMotor, new KendaraanFileRepository());
    }

    /**
     * Constructor Injection untuk DIP: Menerima abstraksi IKendaraanRepository.
     */
    public GedungParkir(String namaGedung,
                        double tarifJamPertamaMobil, double tarifPerJamMobil,
                        double tarifJamPertamaMotor, double tarifPerJamMotor,
                        IKendaraanRepository repository) {
        if (namaGedung == null || namaGedung.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama gedung tidak boleh kosong!");
        }
        this.namaGedung = namaGedung.trim();
        this.daftarSlot = new ArrayList<>();
        this.daftarKendaraan = new ArrayList<>();
        this.riwayatTransaksi = new ArrayList<>();
        this.tarifJamPertamaMobil = tarifJamPertamaMobil;
        this.tarifPerJamMobil = tarifPerJamMobil;
        this.tarifJamPertamaMotor = tarifJamPertamaMotor;
        this.tarifPerJamMotor = tarifPerJamMotor;
        this.totalPendapatan = 0.0;
        this.kendaraanRepository = (repository != null) ? repository : new KendaraanFileRepository();
        this.gerbangTerbuka = false;
        this.statusGerbang = "TERTUTUP / STANDBY";
        DatabaseManager.inisialisasiDatabase();
        this.counterTiket = DatabaseManager.getNextTiketCounter();
        this.totalPendapatan = DatabaseManager.hitungTotalPendapatanDB();
    }

    public void tambahSlot(SlotParkir slot) {
        if (slot == null) {
            throw new IllegalArgumentException("Objek slot tidak boleh null.");
        }
        if (cariSlot(slot.getIdSlot()) != null) {
            throw new IllegalArgumentException("Slot dengan ID " + slot.getIdSlot() + " sudah terdaftar.");
        }
        daftarSlot.add(slot);
        DatabaseManager.updateStatusSlot(slot);
    }

    public void tambahDaftarSlot(List<SlotParkir> slots) {
        if (slots != null && !slots.isEmpty()) {
            for (SlotParkir s : slots) {
                if (s != null && cariSlot(s.getIdSlot()) == null) {
                    daftarSlot.add(s);
                }
            }
            DatabaseManager.simpanBatchSlot(slots);
        }
    }

    public SlotParkir cariSlot(String idSlot) {
        if (idSlot == null || idSlot.trim().isEmpty()) return null;
        String idTrim = idSlot.trim();
        for (SlotParkir s : daftarSlot) {
            if (s.getIdSlot().equalsIgnoreCase(idTrim)) {
                return s;
            }
        }
        return null;
    }

    public boolean hapusSlot(String idSlot) {
        SlotParkir slot = cariSlot(idSlot);
        if (slot == null) {
            return false;
        }
        if (slot.isTerisi()) {
            throw new IllegalStateException("Slot " + idSlot + " sedang terisi kendaraan "
                    + slot.getKendaraanParkir().getPlatNomor() + ". Tidak dapat dihapus!");
        }
        return daftarSlot.remove(slot);
    }

    public void tampilkanSemuaSlot() {
        if (daftarSlot.isEmpty()) {
            System.out.println("Daftar slot parkir masih kosong.");
            return;
        }
        for (SlotParkir s : daftarSlot) {
            System.out.printf("Slot %s (Lt.%d, %s) : %s%n",
                    s.getIdSlot(), s.getLantai(), s.getTipeSlot(),
                    s.isTerisi() ? "TERISI [" + s.getKendaraanParkir().getPlatNomor() + "]" : "KOSONG");
        }
        System.out.println("Total slot parkir: " + daftarSlot.size());
    }

    // =========================================================================
    // IMPLEMENTASI IArmadaManager (ISP) & DIP
    // =========================================================================

    @Override
    public void tambahKendaraan(Kendaraan k) throws FormatPlatTidakValidException {
        if (k == null) {
            throw new IllegalArgumentException("Objek kendaraan tidak boleh null.");
        }
        Kendaraan.validasiFormatPlat(k.getPlatNomor());
        if (cariKendaraan(k.getPlatNomor()) != null) {
            throw new IllegalArgumentException("Kendaraan dengan plat '" + k.getPlatNomor() + "' sudah terdaftar.");
        }
        daftarKendaraan.add(k);
        if (kendaraanRepository != null && kendaraanRepository.cari(k.getPlatNomor()) == null) {
            try {
                kendaraanRepository.tambah(k);
            } catch (Exception ignored) {}
        }
        DatabaseManager.simpanKendaraan(k);
    }

    @Override
    public Kendaraan cariKendaraan(String platNomor) {
        if (platNomor == null || platNomor.trim().isEmpty()) {
            return null;
        }
        String pTarget = platNomor.replaceAll("\\s+", "").toUpperCase();
        for (Kendaraan k : daftarKendaraan) {
            String pCur = k.getPlatNomor().replaceAll("\\s+", "").toUpperCase();
            if (pCur.equals(pTarget)) {
                return k;
            }
        }
        return null;
    }

    @Override
    public boolean hapusKendaraan(String platNomor) {
        Kendaraan k = cariKendaraan(platNomor);
        if (k == null) {
            return false;
        }
        SlotParkir slotAktif = cariSlotDenganPlat(platNomor);
        if (slotAktif != null) {
            throw new IllegalStateException("Kendaraan plat " + platNomor + " sedang aktif parkir di slot "
                    + slotAktif.getIdSlot() + ". Checkout terlebih dahulu sebelum menghapus data!");
        }
        boolean removed = daftarKendaraan.remove(k);
        if (kendaraanRepository != null) {
            kendaraanRepository.hapus(platNomor);
        }
        return removed;
    }

    public void tampilkanSemuaKendaraan() {
        if (daftarKendaraan.isEmpty()) {
            System.out.println("Daftar kendaraan terdaftar masih kosong.");
            return;
        }
        for (Kendaraan k : daftarKendaraan) {
            System.out.println(" • " + k);
        }
        System.out.println("Total kendaraan terdaftar: " + daftarKendaraan.size());
    }

    @Override
    public List<Kendaraan> getDaftarKendaraan() {
        return Collections.unmodifiableList(daftarKendaraan);
    }

    @Override
    public int getTotalKendaraanTerdaftar() {
        return daftarKendaraan.size();
    }

    // Setter & Getter Injection untuk IKendaraanRepository (DIP)
    public void setKendaraanRepository(IKendaraanRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository tidak boleh null.");
        }
        this.kendaraanRepository = repository;
    }

    public IKendaraanRepository getKendaraanRepository() {
        return this.kendaraanRepository;
    }

    // =========================================================================
    // PERSISTENSI FILE ARMADA (DIP & NIO.2)
    // =========================================================================

    public void simpanArmadaKeFile(Path lokasi) throws IOException {
        if (lokasi == null) {
            throw new IllegalArgumentException("Path lokasi berkas tidak boleh null.");
        }
        if (this.kendaraanRepository instanceof KendaraanFileRepository) {
            KendaraanFileRepository.simpan(lokasi, this.daftarKendaraan);
        } else if (this.kendaraanRepository != null) {
            this.kendaraanRepository.simpan(lokasi);
        } else {
            KendaraanFileRepository.simpan(lokasi, this.daftarKendaraan);
        }
    }

    public void simpanArmadaKeFile() throws IOException {
        simpanArmadaKeFile(KendaraanFileRepository.DEFAULT_PATH);
    }

    public int muatArmadaDariFile(Path lokasi) throws IOException {
        if (lokasi == null) {
            throw new IllegalArgumentException("Path lokasi berkas tidak boleh null.");
        }
        List<Kendaraan> armadaBaru;
        if (this.kendaraanRepository != null) {
            armadaBaru = this.kendaraanRepository.muat(lokasi);
        } else {
            armadaBaru = KendaraanFileRepository.muatFile(lokasi);
        }

        // Atomic Swap: Seluruh baris valid sebelum commit ke state aktif
        this.daftarKendaraan.clear();
        this.daftarKendaraan.addAll(armadaBaru);

        for (Kendaraan k : armadaBaru) {
            DatabaseManager.simpanKendaraan(k);
        }

        return armadaBaru.size();
    }

    public int muatArmadaDariFile() throws IOException {
        return muatArmadaDariFile(KendaraanFileRepository.DEFAULT_PATH);
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

    // =========================================================================
    // IMPLEMENTASI IParkingGateController (ISP)
    // =========================================================================

    @Override
    public boolean isGerbangTerbuka() {
        return gerbangTerbuka;
    }

    @Override
    public void setStatusGerbang(boolean terbuka, String status) {
        this.gerbangTerbuka = terbuka;
        this.statusGerbang = (status != null) ? status : (terbuka ? "TERBUKA" : "TERTUTUP");
    }

    @Override
    public String getStatusGerbang() {
        return statusGerbang;
    }

    public void tutupGerbang() {
        setStatusGerbang(false, "TERTUTUP / SIAP");
    }

    @Override
    public KarcisParkir prosesMasukTicketless(Kendaraan k, int jamMasuk) {
        if (k == null) {
            throw new IllegalArgumentException("Kendaraan tidak boleh null!");
        }
        if (jamMasuk < 0 || jamMasuk > 23) {
            throw new IllegalArgumentException("Jam masuk harus berada dalam rentang 0-23! Diberikan: " + jamMasuk);
        }
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
        setStatusGerbang(true, "TERBUKA MASUK (TICKETLESS)");
        return tiket;
    }

    @Override
    public KarcisParkir prosesMasukEmergency(String tipeKendaraan, int jamMasuk) throws KapasitasPenuhException {
        if (jamMasuk < 0 || jamMasuk > 23) {
            throw new IllegalArgumentException("Jam masuk harus berada dalam rentang 0-23! Diberikan: " + jamMasuk);
        }
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
        setStatusGerbang(true, "TERBUKA MASUK (EMERGENCY)");
        return tiket;
    }

    @Override
    public KarcisParkir prosesMasuk(Kendaraan k, int jamMasuk) throws KapasitasPenuhException, KendaraanSudahParkirException {
        if (k == null) {
            throw new IllegalArgumentException("Data kendaraan tidak boleh null!");
        }
        if (jamMasuk < 0 || jamMasuk > 23) {
            throw new IllegalArgumentException("Jam masuk harus berada dalam rentang 0-23! Diberikan: " + jamMasuk);
        }

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

        boolean berhasilIsi = slotTersedia.isiSlot(k);
        if (!berhasilIsi) {
            System.out.println("[CHECK-IN GAGAL] Gagal menempati slot parkir.");
            return null;
        }

        String noTiket = String.format("TKT-%04d", counterTiket++);
        KarcisParkir tiket = new KarcisParkir(noTiket, k, slotTersedia, jamMasuk);
        riwayatTransaksi.add(tiket);

        if (cariKendaraan(k.getPlatNomor()) == null) {
            daftarKendaraan.add(k);
        }

        DatabaseManager.simpanKendaraan(k);
        DatabaseManager.updateStatusSlot(slotTersedia);
        DatabaseManager.simpanKarcisMasuk(tiket);
        setStatusGerbang(true, "TERBUKA MASUK");

        System.out.printf("[CHECK-IN SUKSES] %s berhasil masuk.%n", k);
        System.out.printf(" >> Tiket: %s | Alokasi Slot: %s (Lantai %d)%n",
                tiket.getNomorTiket(), slotTersedia.getIdSlot(), slotTersedia.getLantai());
        return tiket;
    }

    public SlotParkir cariSlotTersedia(Kendaraan k) {
        if (k == null) {
            return null;
        }
        if (k.isMobilListrik()) {
            SlotParkir slotEV = cariSlotKhususEV();
            if (slotEV != null) {
                return slotEV;
            }
        }
        return cariSlotRegulerTerendah(k);
    }

    private SlotParkir cariSlotKhususEV() {
        for (SlotParkir slot : daftarSlot) {
            if (!slot.isTerisi() && "MOBIL_LISTRIK".equals(slot.getTipeSlot())) {
                return slot;
            }
        }
        return null;
    }

    private SlotParkir cariSlotRegulerTerendah(Kendaraan k) {
        SlotParkir kandidat = null;
        for (SlotParkir slot : daftarSlot) {
            if (slot.isTerisi() || !slot.cocokUntuk(k)) {
                continue;
            }
            if (kandidat == null || slot.getLantai() < kandidat.getLantai()) {
                kandidat = slot;
                if (kandidat.getLantai() == 1) {
                    break;
                }
            }
        }
        return kandidat;
    }

    public SlotParkir cariSlotDenganPlat(String plat) {
        if (plat == null || plat.trim().isEmpty()) return null;
        String p = plat.replaceAll("\\s+", "").toUpperCase();
        for (SlotParkir s : daftarSlot) {
            if (!s.isTerisi() || s.getKendaraanParkir() == null) continue;
            String platParkir = s.getKendaraanParkir().getPlatNomor().replaceAll("\\s+", "").toUpperCase();
            if (platParkir.equals(p)) {
                return s;
            }
        }
        return null;
    }

    public KarcisParkir cariTiketAktifDenganPlat(String plat) {
        if (plat == null || plat.trim().isEmpty()) return null;
        String p = plat.replaceAll("\\s+", "").toUpperCase();
        for (int i = riwayatTransaksi.size() - 1; i >= 0; i--) {
            KarcisParkir t = riwayatTransaksi.get(i);
            if (t.isSudahDibayar() || t.getKendaraan() == null) continue;
            String platTiket = t.getKendaraan().getPlatNomor().replaceAll("\\s+", "").toUpperCase();
            if (platTiket.equals(p)) {
                return t;
            }
        }
        return null;
    }

    public KarcisParkir cariTiketAktifDenganSlot(String idSlot) {
        if (idSlot == null || idSlot.trim().isEmpty()) return null;
        String s = idSlot.trim().toUpperCase();
        for (int i = riwayatTransaksi.size() - 1; i >= 0; i--) {
            KarcisParkir t = riwayatTransaksi.get(i);
            if (t.isSudahDibayar() || t.getSlot() == null) continue;
            if (t.getSlot().getIdSlot().equalsIgnoreCase(s)) {
                return t;
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

    // =========================================================================
    // IMPLEMENTASI IParkingBilling (ISP, OCP, LSP, P14 Clean Refactoring)
    // =========================================================================

    private double getTarifJamPertama(Kendaraan k) {
        if (k != null && "MOTOR".equalsIgnoreCase(k.getTipe())) {
            return tarifJamPertamaMotor;
        }
        return tarifJamPertamaMobil;
    }

    private double getTarifPerJamBerikutnya(Kendaraan k) {
        if (k != null && "MOTOR".equalsIgnoreCase(k.getTipe())) {
            return tarifPerJamMotor;
        }
        return tarifPerJamMobil;
    }

    @Override
    public double hitungTagihan(KarcisParkir tiket, int jamKeluar, DiskonStrategy diskonStrategy) {
        if (tiket == null) return 0.0;
        if (jamKeluar < 0 || jamKeluar > 24) {
            throw new IllegalArgumentException("Jam keluar harus berada dalam rentang 0-24! Diberikan: " + jamKeluar);
        }
        double tarifPertama = getTarifJamPertama(tiket.getKendaraan());
        double tarifBerikutnya = getTarifPerJamBerikutnya(tiket.getKendaraan());
        return tiket.hitungTarif(jamKeluar, tarifPertama, tarifBerikutnya, diskonStrategy);
    }

    @Override
    public double hitungTagihan(KarcisParkir tiket, int jamKeluar) {
        DiskonStrategy defaultStrategy = (tiket != null && tiket.getKendaraan() != null && tiket.getKendaraan().isMobilListrik())
                ? new EVIncentiveDiskonStrategy()
                : new TanpaDiskonStrategy();
        return hitungTagihan(tiket, jamKeluar, defaultStrategy);
    }

    @Override
    public double hitungTagihan(BillingContext context) {
        if (context == null || context.getTiket() == null) return 0.0;
        return hitungTagihan(context.getTiket(), context.getJamKeluar(), context.getDiskonStrategy());
    }

    /**
     * Memproses alur check-out dan pembayaran menggunakan Parameter Object CheckOutRequest (P14 Clean Refactoring).
     * Menerapkan Guard Clauses untuk alur validasi yang datar tanpa nesting berlebih.
     */
    @Override
    public boolean prosesKeluar(CheckOutRequest request)
            throws TiketTidakDitemukanException, PembayaranKurangException {
        if (request == null) {
            throw new IllegalArgumentException("CheckOutRequest tidak boleh null!");
        }

        KarcisParkir tiket = request.getTiket();
        validasiKelaikanCheckOut(tiket);

        int jamKeluar = request.getJamKeluar();
        double tarifPertama = getTarifJamPertama(tiket.getKendaraan());
        double tarifBerikutnya = getTarifPerJamBerikutnya(tiket.getKendaraan());

        tiket.hitungTarif(jamKeluar, tarifPertama, tarifBerikutnya, request.getDiskonStrategy());

        IPaymentProcessor processor = request.getPaymentProcessor();
        boolean lunas = processor.prosesPembayaran(tiket, request.getUangBayar());
        if (!lunas) {
            System.out.println("[GATE TETAP TERTUTUP] Pembayaran belum selesai. Portal keluar tidak dapat dibuka.");
            return false;
        }

        selesaikanPelepasanSlotDanKas(tiket, processor);
        return true;
    }

    @Override
    public boolean prosesKeluar(KarcisParkir tiket, int jamKeluar, IPaymentProcessor paymentProcessor, double uangBayar, DiskonStrategy diskonStrategy)
            throws TiketTidakDitemukanException, PembayaranKurangException {
        if (uangBayar < 0) {
            throw new IllegalArgumentException("Nominal pembayaran tidak boleh bernilai negatif!");
        }
        CheckOutRequest request = new CheckOutRequest(tiket, jamKeluar, paymentProcessor, uangBayar, diskonStrategy);
        return prosesKeluar(request);
    }

    @Override
    public boolean prosesKeluar(KarcisParkir tiket, int jamKeluar, String metode, double uangBayar)
            throws TiketTidakDitemukanException, PembayaranKurangException {
        if (tiket == null) {
            System.out.println("[CHECK-OUT GAGAL] Tiket parkir tidak ditemukan.");
            throw new TiketTidakDitemukanException("Tiket null");
        }
        if (uangBayar < 0) {
            throw new IllegalArgumentException("Nominal pembayaran tidak boleh bernilai negatif!");
        }
        IPaymentProcessor processor = PaymentProcessorFactory.getProcessor(metode);
        DiskonStrategy diskon = (tiket != null && tiket.getKendaraan() != null && tiket.getKendaraan().isMobilListrik())
                ? new EVIncentiveDiskonStrategy()
                : new TanpaDiskonStrategy();
        CheckOutRequest request = new CheckOutRequest(tiket, jamKeluar, processor, uangBayar, diskon);
        return prosesKeluar(request);
    }

    private void validasiKelaikanCheckOut(KarcisParkir tiket) throws TiketTidakDitemukanException {
        if (tiket == null) {
            System.out.println("[CHECK-OUT GAGAL] Tiket parkir tidak ditemukan.");
            throw new TiketTidakDitemukanException("Tiket null");
        }
        if (tiket.isSudahDibayar()) {
            System.out.printf("[CHECK-OUT DITOLAK] Tiket %s sudah lunas/keluar sebelumnya.%n", tiket.getNomorTiket());
            throw new TiketTidakDitemukanException("Tiket " + tiket.getNomorTiket() + " sudah diselesaikan sebelumnya.");
        }
        SlotParkir slot = tiket.getSlot();
        if (slot != null && (!slot.isTerisi() || (slot.getKendaraanParkir() != null && !slot.getKendaraanParkir().equals(tiket.getKendaraan())))) {
            System.out.printf("[CHECK-OUT DITOLAK] Kendaraan %s sudah tidak terparkir di slot %s (mungkin sudah keluar sebelumnya).%n",
                    tiket.getKendaraan() != null ? tiket.getKendaraan().getPlatNomor() : "-", slot.getIdSlot());
            throw new TiketTidakDitemukanException(tiket.getKendaraan() != null ? tiket.getKendaraan().getPlatNomor() : tiket.getNomorTiket());
        }
    }

    private void selesaikanPelepasanSlotDanKas(KarcisParkir tiket, IPaymentProcessor processor) {
        SlotParkir slot = tiket.getSlot();
        if (slot != null && slot.isTerisi() && tiket.getKendaraan() != null && tiket.getKendaraan().equals(slot.getKendaraanParkir())) {
            slot.kosongkanSlot();
            DatabaseManager.updateStatusSlot(slot);
        }

        this.totalPendapatan += tiket.getTotalBiaya();
        DatabaseManager.updateKarcisKeluar(tiket);
        setStatusGerbang(true, "TERBUKA KELUAR (" + processor.getNamaMetode() + ")");

        System.out.printf("[CHECK-OUT SUKSES] Kendaraan %s telah keluar (%s).%n",
                tiket.getKendaraan().getPlatNomor(), processor.getNamaMetode());
        if (slot != null) {
            System.out.printf(" >> Slot %s sekarang KOSONG kembali dan siap digunakan.%n", slot.getIdSlot());
        }
    }

    public boolean prosesKeluarDenganPlat(String plat, int jamKeluar, String metode, double uangBayar)
            throws TiketTidakDitemukanException, PembayaranKurangException {
        KarcisParkir tiket = cariTiketAktifDenganPlat(plat);
        if (tiket == null) {
            throw new TiketTidakDitemukanException(plat);
        }
        return prosesKeluar(tiket, jamKeluar, metode, uangBayar);
    }

    public boolean prosesKeluarDenganPlat(String plat, int jamKeluar, IPaymentProcessor processor, double uangBayar, DiskonStrategy diskon)
            throws TiketTidakDitemukanException, PembayaranKurangException {
        KarcisParkir tiket = cariTiketAktifDenganPlat(plat);
        if (tiket == null) {
            throw new TiketTidakDitemukanException(plat);
        }
        return prosesKeluar(tiket, jamKeluar, processor, uangBayar, diskon);
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
        DurasiParkir durasi = tiket.getDurasiParkir();
        BiayaParkir biaya = tiket.getRincianBiaya();
        System.out.println("==========================================================================");
        System.out.printf("                     KUITANSI PARKIR - %s                   %n", namaGedung.toUpperCase());
        System.out.println("==========================================================================");
        System.out.printf(" No. Tiket    : %-18s  Metode Bayar : %s%n", tiket.getNomorTiket(), tiket.getMetodePembayaran());
        System.out.printf(" Kendaraan    : %s (%s - %s)%n",
                tiket.getKendaraan().getPlatNomor(), tiket.getKendaraan().getTipe(), tiket.getKendaraan().getMerk());
        System.out.printf(" Lokasi Parkir: Slot %s (Lantai %d)%n", tiket.getSlot().getIdSlot(), tiket.getSlot().getLantai());
        System.out.printf(" Waktu Parkir : %s (Durasi: %s)%n", durasi.getRentangWaktu(), durasi.toDisplayString());
        if (biaya.isMendapatDiskon()) {
            System.out.printf(" Program      : %s (Hemat %s)%n", biaya.getNamaDiskon(), biaya.formatDiskon());
        } else if (tiket.getKendaraan().isMobilListrik()) {
            System.out.println(" Catatan      : Mendapatkan Insentif Hijau Diskon 20% (EV Friendly)");
        }
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf(" TOTAL TAGIHAN: %s  [STATUS: LUNAS - GATE OPEN]%n", biaya.formatTotalTagihan());
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

    @Override
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
