package model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kelas KendaraanFileRepository bertindak sebagai pengelola persistensi berkas teks (File Persistence)
 * untuk entitas Kendaraan menggunakan pustaka java.nio.file (Files, Path, Paths) dan UTF-8 charset.
 * 
 * Sesuai kaidah Modul 12: Penyimpanan dan Pembacaan Data ke File
 * - Format serialisasi terdelimitasi pipe '|' (Format: PLAT_NOMOR|TIPE|MERK).
 * - Validasi karakter terlarang ('|', '\n', '\r') saat penyimpanan agar integritas delimiter terjaga.
 * - Validasi integritas domain lengkap saat pembacaan (FormatPlatTidakValidException & IllegalArgumentException).
 * - Deteksi plat nomor duplikat di dalam berkas.
 * - Menerapkan prinsip Atomic State Preservation: seluruh parsing dan validasi dilakukan pada koleksi
 *   sementara (buffer), sehingga file yang korup atau tidak valid tidak akan merusak status data yang sedang aktif.
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class KendaraanFileRepository {

    public static final Path DEFAULT_PATH = Paths.get("armada_kendaraan.txt");
    public static final String DELIMITER = "|";

    /**
     * Menyimpan seluruh koleksi kendaraan ke berkas teks pada lokasi Path tertentu.
     * Menggunakan separator pipe '|' dan encoding StandardCharsets.UTF_8.
     * Memvalidasi bahwa setiap atribut tidak mengandung karakter pemisah terlarang.
     *
     * @param lokasi path berkas tujuan
     * @param daftarKendaraan kumpulan kendaraan yang akan disimpan
     * @throws IOException jika terjadi galat saat penulisan ke disk
     * @throws IllegalArgumentException jika parameter null atau terdapat karakter terlarang
     */
    public static void simpan(Path lokasi, List<Kendaraan> daftarKendaraan) throws IOException {
        if (lokasi == null) {
            throw new IllegalArgumentException("Path lokasi berkas tidak boleh null.");
        }
        if (daftarKendaraan == null) {
            throw new IllegalArgumentException("Daftar kendaraan tidak boleh null.");
        }

        List<String> barisList = new ArrayList<>();
        // Metadata header sebagai dokumentasi schema file
        barisList.add("# ==========================================================================");
        barisList.add("# DATA REPOSITORI ARMADA KENDARAAN SMART PARKIR (MODUL 12)");
        barisList.add("# FORMAT: PLAT_NOMOR|TIPE|MERK");
        barisList.add("# CATATAN: Karakter '|', '\\n', '\\r' dilarang dalam field");
        barisList.add("# ==========================================================================");

        for (Kendaraan k : daftarKendaraan) {
            if (k == null) continue;
            String plat = k.getPlatNomor();
            String tipe = k.getTipe();
            String merk = k.getMerk();

            // Validasi karakter terlarang yang dapat merusak struktur delimiter
            for (String field : new String[]{plat, tipe, merk}) {
                if (field != null && (field.contains("|") || field.contains("\n") || field.contains("\r"))) {
                    throw new IllegalArgumentException("Data kendaraan mengandung pemisah terlarang ('|', newline): " + field);
                }
            }

            barisList.add(plat + DELIMITER + tipe + DELIMITER + (merk != null ? merk : "Umum"));
        }

        // Buat folder induk jika belum eksis
        if (lokasi.getParent() != null && !Files.exists(lokasi.getParent())) {
            Files.createDirectories(lokasi.getParent());
        }

        // Tulis baris secara atomik/keseluruhan dengan UTF-8
        Files.write(lokasi, barisList, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    /**
     * Membaca berkas teks dan membentuk kembali koleksi List<Kendaraan>.
     * Menerapkan prinsip Atomic State Preservation:
     * Seluruh baris di-parse dan divalidasi ke koleksi sementara terlebih dahulu.
     * Jika terjadi kegagalan format atau domain pada baris manapun, metode melempar IOException
     * dengan informasi baris yang presisi.
     *
     * @param lokasi path berkas sumber
     * @return List<Kendaraan> yang valid
     * @throws IOException jika file tidak ditemukan, format salah, atau data domain tidak valid
     */
    public static List<Kendaraan> muat(Path lokasi) throws IOException {
        if (lokasi == null) {
            throw new IllegalArgumentException("Path lokasi berkas tidak boleh null.");
        }
        if (!Files.exists(lokasi)) {
            throw new IOException("Berkas tidak ditemukan pada lokasi: " + lokasi.toAbsolutePath());
        }

        List<String> semuaBaris = Files.readAllLines(lokasi, StandardCharsets.UTF_8);
        List<Kendaraan> koleksiSementara = new ArrayList<>();
        Set<String> platTerdaftar = new HashSet<>();

        for (int i = 0; i < semuaBaris.size(); i++) {
            String baris = semuaBaris.get(i);
            int nomorBaris = i + 1;

            // Abaikan baris kosong dan baris komentar diawali '#'
            if (baris.trim().isEmpty() || baris.trim().startsWith("#")) {
                continue;
            }

            String[] bagian = baris.split("\\|", -1);
            if (bagian.length != 3) {
                throw new IOException(String.format(
                    "Format salah pada baris %d: Diharapkan 3 kolom (PLAT|TIPE|MERK), ditemukan %d kolom. Baris: '%s'",
                    nomorBaris, bagian.length, baris
                ));
            }

            String plat = bagian[0].trim();
            String tipe = bagian[1].trim();
            String merk = bagian[2].trim();

            if (plat.isEmpty()) {
                throw new IOException("Data tidak valid pada baris " + nomorBaris + ": Plat nomor kosong.");
            }

            try {
                // Validasi format plat nomor sesuai aturan domain P10
                Kendaraan.validasiFormatPlat(plat);

                // Validasi keunikan plat nomor dalam file
                String platClean = plat.replaceAll("\\s+", "").toUpperCase();
                if (platTerdaftar.contains(platClean)) {
                    throw new IllegalArgumentException("Plat nomor duplikat terdeteksi dalam berkas: " + plat);
                }
                platTerdaftar.add(platClean);

                // Instansiasi objek Kendaraan (validasi tipe dan inisialisasi state)
                Kendaraan k = new Kendaraan(plat, tipe, merk);
                koleksiSementara.add(k);
            } catch (FormatPlatTidakValidException | IllegalArgumentException e) {
                throw new IOException("Data tidak valid pada baris " + nomorBaris + ": " + e.getMessage(), e);
            }
        }

        return koleksiSementara;
    }
}
