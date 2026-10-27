package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Manager koneksi dan operasi CRUD Database MySQL untuk SmartParkir.
 * Mendukung persistensi data kendaraan, status slot parkir, dan transaksi karcis parkir.
 */
public class DatabaseManager {

    public static final String CONFIG_FILE = "db_config.properties";
    private static Properties config = null;
    private static Boolean connectionAvailable = null;

    // Load konfigurasi dari db_config.properties
    public static synchronized Properties getConfig() {
        if (config == null) {
            config = new Properties();
            // Default konfigurasi MySQL
            config.setProperty("db.url", "jdbc:mysql://localhost:3306/smart_parkir_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta");
            config.setProperty("db.user", "root");
            config.setProperty("db.password", "");

            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileInputStream in = new FileInputStream(f)) {
                    config.load(in);
                } catch (Exception e) {
                    System.err.println("[DB CONFIG] Gagal membaca " + CONFIG_FILE + ": " + e.getMessage());
                }
            } else {
                try (FileOutputStream out = new FileOutputStream(f)) {
                    config.store(out, "Konfigurasi Database MySQL SmartParkir");
                } catch (Exception ignored) {}
            }
        }
        return config;
    }

    /**
     * Membuka koneksi baru ke MySQL database.
     */
    public static Connection getConnection() throws SQLException {
        Properties cfg = getConfig();
        String url = cfg.getProperty("db.url");
        String user = cfg.getProperty("db.user");
        String pass = cfg.getProperty("db.password");
        return DriverManager.getConnection(url, user, pass);
    }

    /**
     * Cek apakah koneksi database aktif dan berfungsi.
     */
    public static boolean isConnected() {
        try (Connection conn = getConnection()) {
            connectionAvailable = (conn != null && !conn.isClosed());
            return connectionAvailable;
        } catch (Exception e) {
            connectionAvailable = false;
            return false;
        }
    }

    /**
     * Inisialisasi otomatis tabel database jika belum ada.
     */
    public static void inisialisasiDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Tabel Master Kendaraan
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS kendaraan (" +
                "  plat_nomor VARCHAR(20) PRIMARY KEY," +
                "  tipe VARCHAR(20) NOT NULL," +
                "  merk VARCHAR(50) NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // 2. Tabel Master Slot Parkir
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS slot_parkir (" +
                "  kode_slot VARCHAR(20) PRIMARY KEY," +
                "  lantai INT NOT NULL," +
                "  tipe_peruntukan VARCHAR(20) NOT NULL," +
                "  terisi BOOLEAN DEFAULT FALSE," +
                "  plat_nomor_parkir VARCHAR(20) NULL," +
                "  FOREIGN KEY (plat_nomor_parkir) REFERENCES kendaraan(plat_nomor) ON DELETE SET NULL" +
                ")"
            );

            // 3. Tabel Transaksi Karcis Parkir
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS karcis_parkir (" +
                "  nomor_tiket VARCHAR(30) PRIMARY KEY," +
                "  plat_nomor VARCHAR(20) NOT NULL," +
                "  kode_slot VARCHAR(20) NOT NULL," +
                "  jam_masuk INT NOT NULL," +
                "  jam_keluar INT NULL," +
                "  durasi_jam INT NULL," +
                "  total_biaya DOUBLE DEFAULT 0," +
                "  metode_pembayaran VARCHAR(30) DEFAULT '-'," +
                "  sudah_dibayar BOOLEAN DEFAULT FALSE," +
                "  waktu_masuk_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  waktu_keluar_ts TIMESTAMP NULL," +
                "  FOREIGN KEY (plat_nomor) REFERENCES kendaraan(plat_nomor)," +
                "  FOREIGN KEY (kode_slot) REFERENCES slot_parkir(kode_slot)" +
                ")"
            );

            System.out.println("[DATABASE] Inisialisasi skema tabel MySQL berhasil diverifikasi.");
        } catch (SQLException e) {
            System.err.println("[DATABASE WARNING] Gagal inisialisasi database: " + e.getMessage());
        }
    }

    /**
     * Menyimpan atau memperbarui data kendaraan ke database.
     */
    public static void simpanKendaraan(Kendaraan k) {
        if (k == null) return;
        String sql = "INSERT INTO kendaraan (plat_nomor, tipe, merk) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE tipe = VALUES(tipe), merk = VALUES(merk)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k.getPlatNomor());
            ps.setString(2, k.getTipe());
            ps.setString(3, k.getMerk());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DATABASE] Gagal menyimpan kendaraan " + k.getPlatNomor() + ": " + e.getMessage());
        }
    }

    /**
     * Memperbarui status slot parkir (terisi/kosong & plat kendaraan parkir).
     */
    public static void updateStatusSlot(SlotParkir slot) {
        if (slot == null) return;
        String sql = "INSERT INTO slot_parkir (kode_slot, lantai, tipe_peruntukan, terisi, plat_nomor_parkir) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE lantai = VALUES(lantai), tipe_peruntukan = VALUES(tipe_peruntukan), " +
                     "terisi = VALUES(terisi), plat_nomor_parkir = VALUES(plat_nomor_parkir)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slot.getIdSlot());
            ps.setInt(2, slot.getLantai());
            ps.setString(3, slot.getTipeSlot());
            ps.setBoolean(4, slot.isTerisi());
            if (slot.isTerisi() && slot.getKendaraanParkir() != null) {
                ps.setString(5, slot.getKendaraanParkir().getPlatNomor());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DATABASE] Gagal memperbarui slot " + slot.getIdSlot() + ": " + e.getMessage());
        }
    }

    /**
     * Menyimpan transaksi karcis masuk ke database.
     */
    public static void simpanKarcisMasuk(KarcisParkir tiket) {
        if (tiket == null) return;
        String sql = "INSERT INTO karcis_parkir (nomor_tiket, plat_nomor, kode_slot, jam_masuk, sudah_dibayar) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE jam_masuk = VALUES(jam_masuk)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tiket.getNomorTiket());
            ps.setString(2, tiket.getKendaraan().getPlatNomor());
            ps.setString(3, tiket.getSlot().getIdSlot());
            ps.setInt(4, tiket.getJamMasuk());
            ps.setBoolean(5, tiket.isSudahDibayar());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DATABASE] Gagal menyimpan karcis masuk " + tiket.getNomorTiket() + ": " + e.getMessage());
        }
    }

    /**
     * Memperbarui transaksi karcis saat check-out dan lunas.
     */
    public static void updateKarcisKeluar(KarcisParkir tiket) {
        if (tiket == null) return;
        String sql = "UPDATE karcis_parkir SET jam_keluar = ?, durasi_jam = ?, total_biaya = ?, " +
                     "metode_pembayaran = ?, sudah_dibayar = ?, waktu_keluar_ts = CURRENT_TIMESTAMP " +
                     "WHERE nomor_tiket = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tiket.getJamKeluar());
            ps.setInt(2, tiket.hitungDurasi(tiket.getJamKeluar()));
            ps.setDouble(3, tiket.getTotalBiaya());
            ps.setString(4, tiket.getMetodePembayaran());
            ps.setBoolean(5, tiket.isSudahDibayar());
            ps.setString(6, tiket.getNomorTiket());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DATABASE] Gagal mengupdate karcis keluar " + tiket.getNomorTiket() + ": " + e.getMessage());
        }
    }

    /**
     * Menghitung total pendapatan langsung dari database.
     */
    public static double hitungTotalPendapatanDB() {
        String sql = "SELECT COALESCE(SUM(total_biaya), 0) AS total FROM karcis_parkir WHERE sudah_dibayar = TRUE";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE] Gagal menghitung total pendapatan: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Mengambil jumlah riwayat transaksi yang tersimpan di database.
     */
    public static int getJumlahTransaksiDB() {
        String sql = "SELECT COUNT(*) AS total FROM karcis_parkir";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE] Gagal menghitung riwayat transaksi: " + e.getMessage());
        }
        return 0;
    }
}
