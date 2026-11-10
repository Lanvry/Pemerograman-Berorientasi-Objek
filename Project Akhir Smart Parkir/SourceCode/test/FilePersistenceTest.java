package test;

import model.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit Test Modul 12: File Persistence (Penyimpanan dan Pembacaan Data ke File)
 * Menguji persistensi entitas Kendaraan menggunakan java.nio.file (Files, Path, Paths),
 * format terdelimitasi pipe '|', validasi domain, deteksi duplikasi, dan prinsip
 * Atomic State Preservation (Atomic Rollback).
 * 
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class FilePersistenceTest {

    private static final Path DIR_TEST = Paths.get("target_test_p12");

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   UNIT TEST MODUL 12: FILE PERSISTENCE SMART PARKIR                      ");
        System.out.println("   Pengujian java.nio.file, Pipe Delimited & Atomic State Preservation   ");
        System.out.println("==========================================================================");
        System.out.println();

        int passed = 0;
        int total = 8;

        try {
            if (!Files.exists(DIR_TEST)) {
                Files.createDirectories(DIR_TEST);
            }
        } catch (IOException e) {
            System.err.println("Gagal membuat direktori uji: " + e.getMessage());
            return;
        }

        Path fileValid = DIR_TEST.resolve("armada_valid.txt");
        Path fileCorruptKolom = DIR_TEST.resolve("armada_corrupt_kolom.txt");
        Path fileCorruptPlat = DIR_TEST.resolve("armada_corrupt_plat.txt");
        Path fileCorruptDup = DIR_TEST.resolve("armada_corrupt_duplikat.txt");

        // TEST 1: Menyimpan Armada Valid ke File Teks (UTF-8, Pipe Delimited)
        System.out.print("[TEST 1] Operasi simpanArmadaKeFile (Serialisasi Pipe Delimited UTF-8)... ");
        try {
            GedungParkir gedung1 = new GedungParkir("Test Gedung", 5000, 3000, 2000, 1000);
            gedung1.tambahKendaraan(new Kendaraan("B 1234 ABC", "MOBIL", "Toyota Avanza"));
            gedung1.tambahKendaraan(new Kendaraan("L 5678 XYZ", "MOTOR", "Honda Vario"));
            gedung1.tambahKendaraan(new Kendaraan("B 9999 EV", "MOBIL_LISTRIK", "Hyundai Ioniq 5"));

            gedung1.simpanArmadaKeFile(fileValid);

            if (Files.exists(fileValid) && Files.size(fileValid) > 0) {
                List<String> lines = Files.readAllLines(fileValid, StandardCharsets.UTF_8);
                boolean hasAvanza = lines.stream().anyMatch(l -> l.equals("B 1234 ABC|MOBIL|Toyota Avanza"));
                boolean hasVario = lines.stream().anyMatch(l -> l.equals("L 5678 XYZ|MOTOR|Honda Vario"));
                boolean hasEV = lines.stream().anyMatch(l -> l.equals("B 9999 EV|MOBIL_LISTRIK|Hyundai Ioniq 5"));

                if (hasAvanza && hasVario && hasEV) {
                    System.out.println("PASSED (3 kendaraan berhasil ditulis dengan format pipe)");
                    passed++;
                } else {
                    System.out.println("FAILED: Format baris tidak sesuai spesifikasi.");
                }
            } else {
                System.out.println("FAILED: Berkas tidak tercipta atau kosong.");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // TEST 2: Memuat Armada dari File ke Memori
        System.out.print("[TEST 2] Operasi muatArmadaDariFile (Deserialisasi & Rekonstruksi Object)... ");
        try {
            GedungParkir gedung2 = new GedungParkir("Test Gedung Reload", 5000, 3000, 2000, 1000);
            int totalDimuat = gedung2.muatArmadaDariFile(fileValid);

            Kendaraan k1 = gedung2.cariKendaraan("B 1234 ABC");
            Kendaraan k2 = gedung2.cariKendaraan("L 5678 XYZ");
            Kendaraan k3 = gedung2.cariKendaraan("B 9999 EV");

            if (totalDimuat == 3 && k1 != null && k2 != null && k3 != null
                    && k3.isMobilListrik() && k2.getTipe().equals("MOTOR")) {
                System.out.println("PASSED (3 kendaraan berhasil direkonstruksi lengkap dengan tipenya)");
                passed++;
            } else {
                System.out.println("FAILED: Objek yang dimuat tidak lengkap atau tidak cocok.");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // TEST 3: Pencegahan Karakter Terlarang Saat Simpan (Karakter '|', newline)
        System.out.print("[TEST 3] Proteksi karakter terlarang (Pencegahan delimiter injection)... ");
        try {
            GedungParkir gedung3 = new GedungParkir("Test Gedung Injection", 5000, 3000, 2000, 1000);
            // Karakter '|' di dalam merk
            Kendaraan kIllegal = new Kendaraan("B 1111 AAA", "MOBIL", "Toyota|Corrupt|Hack");
            gedung3.tambahKendaraan(kIllegal);
            gedung3.simpanArmadaKeFile(DIR_TEST.resolve("armada_illegal.txt"));
            System.out.println("FAILED: Seharusnya melempar IllegalArgumentException saat mengandung '|'!");
        } catch (IllegalArgumentException e) {
            System.out.println("PASSED (Injeksi terlarang ditolak: " + e.getMessage() + ")");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: Tipe exception salah: " + e.getMessage());
        }

        // TEST 4: Penolakan File Korup (Jumlah Kolom Tidak Sesuai)
        System.out.print("[TEST 4] Penolakan format baris salah (Kolom kurang/lebih)... ");
        try {
            List<String> badLines = new ArrayList<>();
            badLines.add("# Header");
            badLines.add("B 1234 ABC|MOBIL|Avanza");
            badLines.add("L 5555 BAD|MOTOR"); // Hanya 2 kolom, diharapkan 3
            Files.write(fileCorruptKolom, badLines, StandardCharsets.UTF_8);

            GedungParkir gedung4 = new GedungParkir("Test Corrupt Kolom", 5000, 3000, 2000, 1000);
            gedung4.muatArmadaDariFile(fileCorruptKolom);
            System.out.println("FAILED: Seharusnya melempar IOException akibat format baris salah!");
        } catch (IOException e) {
            System.out.println("PASSED (Ditolak dengan pesan informatif: " + e.getMessage() + ")");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: Tipe exception salah: " + e.getMessage());
        }

        // TEST 5: Penolakan File dengan Format Plat Tidak Valid (Domain Exception P10)
        System.out.print("[TEST 5] Penolakan plat tidak valid (Integrasi FormatPlatTidakValidException)... ");
        try {
            List<String> badPlatLines = new ArrayList<>();
            badPlatLines.add("# File dengan plat cacat");
            badPlatLines.add("B 1234 ABC|MOBIL|Avanza");
            badPlatLines.add("12345|MOBIL|Mobil Bodong"); // Format plat angka doang tidak valid
            Files.write(fileCorruptPlat, badPlatLines, StandardCharsets.UTF_8);

            GedungParkir gedung5 = new GedungParkir("Test Corrupt Plat", 5000, 3000, 2000, 1000);
            gedung5.muatArmadaDariFile(fileCorruptPlat);
            System.out.println("FAILED: Seharusnya melempar IOException akibat plat tidak valid!");
        } catch (IOException e) {
            System.out.println("PASSED (Ditolak: " + e.getMessage() + ")");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: Tipe exception salah: " + e.getMessage());
        }

        // TEST 6: Penolakan Plat Nomor Duplikat di dalam Berkas
        System.out.print("[TEST 6] Penolakan plat nomor duplikat di dalam berkas file... ");
        try {
            List<String> dupLines = new ArrayList<>();
            dupLines.add("B 1234 ABC|MOBIL|Avanza Silver");
            dupLines.add("B 1234 ABC|MOBIL|Avanza Hitam Duplikat");
            Files.write(fileCorruptDup, dupLines, StandardCharsets.UTF_8);

            GedungParkir gedung6 = new GedungParkir("Test Corrupt Dup", 5000, 3000, 2000, 1000);
            gedung6.muatArmadaDariFile(fileCorruptDup);
            System.out.println("FAILED: Seharusnya melempar IOException akibat plat duplikat!");
        } catch (IOException e) {
            System.out.println("PASSED (Ditolak: " + e.getMessage() + ")");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: Tipe exception salah: " + e.getMessage());
        }

        // TEST 7: Penolakan Berkas yang Tidak Eksis
        System.out.print("[TEST 7] Penanganan berkas tidak ditemukan (Non-existent path)... ");
        try {
            GedungParkir gedung7 = new GedungParkir("Test Non Existent", 5000, 3000, 2000, 1000);
            gedung7.muatArmadaDariFile(DIR_TEST.resolve("berkas_ghaib_404.txt"));
            System.out.println("FAILED: Seharusnya melempar IOException jika file tidak ditemukan!");
        } catch (IOException e) {
            System.out.println("PASSED (FileNotFound ditangani: " + e.getMessage() + ")");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: Tipe exception salah: " + e.getMessage());
        }

        // TEST 8: ATOMIC STATE PRESERVATION (Atomic Rollback)
        System.out.print("[TEST 8] Atomic State Preservation (Data aktif tidak rusak saat file corrupt)... ");
        try {
            GedungParkir gedungAktif = new GedungParkir("Gedung Active Preservation", 5000, 3000, 2000, 1000);
            // State awal: 2 kendaraan terdaftar aman
            Kendaraan kA = new Kendaraan("D 1111 AAA", "MOBIL", "Honda Jazz");
            Kendaraan kB = new Kendaraan("N 2222 BBB", "MOTOR", "Yamaha Aerox");
            gedungAktif.tambahKendaraan(kA);
            gedungAktif.tambahKendaraan(kB);

            int jumlahSebelum = gedungAktif.getTotalKendaraanTerdaftar();

            // Coba muat berkas yang korup (memiliki baris cacat di baris ke-2)
            try {
                gedungAktif.muatArmadaDariFile(fileCorruptPlat);
            } catch (IOException ex) {
                // Harapannya melempar IOException
            }

            // Verifikasi State Aktif: Harus tetap utuh 2 unit, tidak kosong, tidak setengah jalan
            int jumlahSesudah = gedungAktif.getTotalKendaraanTerdaftar();
            Kendaraan cekA = gedungAktif.cariKendaraan("D 1111 AAA");
            Kendaraan cekB = gedungAktif.cariKendaraan("N 2222 BBB");

            if (jumlahSebelum == 2 && jumlahSesudah == 2 && cekA != null && cekB != null) {
                System.out.println("PASSED (State aktif tetap utuh 2 unit kendaraan, tidak terhapus atau rusak)");
                passed++;
            } else {
                System.out.println("FAILED: State koleksi armada rusak setelah kegagalan membaca file!");
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // Cleanup temporary test files
        try {
            Files.deleteIfExists(fileValid);
            Files.deleteIfExists(fileCorruptKolom);
            Files.deleteIfExists(fileCorruptPlat);
            Files.deleteIfExists(fileCorruptDup);
            Files.deleteIfExists(DIR_TEST.resolve("armada_illegal.txt"));
            Files.deleteIfExists(DIR_TEST);
        } catch (IOException ignored) {}

        System.out.println();
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("HASIL PENGUJIAN FILE PERSISTENCE: %d PASSED, %d FAILED (Total: %d Test Cases)%n",
                passed, (total - passed), total);
        System.out.println("--------------------------------------------------------------------------");
        if (passed == total) {
            System.out.println(">> [VERIFIKASI SUKSES] Seluruh syarat Modul 12 terpenuhi secara sempurna!");
        } else {
            System.err.println(">> [VERIFIKASI GAGAL] Ada pengujian yang belum lolos!");
            System.exit(1);
        }
    }
}
