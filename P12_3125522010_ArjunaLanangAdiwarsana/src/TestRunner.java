import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Kelas TestRunner mengotomatiskan seluruh skenario pengujian Modul 12.
 * Mencakup 8 skenario utama sesuai spesifikasi modul dan pengujian konsistensi atomik (Atomic Swap),
 * serta skenario tambahan validasi karakter terlarang saat persistensi.
 * 
 * Modul 12: Penyimpanan dan Pembacaan Data ke File
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class TestRunner {
    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   PRAKTIKUM PBO MODUL 12: TEST RUNNER PERSISTENSI FILE TEKS              ");
        System.out.println("   Nama: Arjuna Lanang Adiwarsana | NRP: 3125522010                      ");
        System.out.println("==========================================================================");
        System.out.println();

        Path tempDir = Paths.get("temp_test_data");
        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            testSkenario1_SimpanDuaBukuValid(tempDir);
            testSkenario2_MuatDuaBukuValid(tempDir);
            testSkenario3_MuatFileTidakAda(tempDir);
            testSkenario4_FormatBarisSalah(tempDir);
            testSkenario5_FieldKosong(tempDir);
            testSkenario6_KodeDuplikat(tempDir);
            testSkenario7_MuatFileKosong(tempDir);
            testSkenario8_KonsistensiStateAtomicSwap(tempDir);
            testSkenario9_PencegahanKarakterTerlarang(tempDir);

        } catch (Exception e) {
            System.err.println("Terjadi kesalahan fatal selama eksekusi pengujian: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanupTempDir(tempDir);
        }

        System.out.println();
        System.out.println("==========================================================================");
        System.out.printf("   HASIL AKHIR: %d / %d PENGUJIAN LULUS (%.1f%% SUCCESS RATE)\n",
                passedTests, totalTests, ((double) passedTests / totalTests) * 100);
        if (passedTests == totalTests) {
            System.out.println("   STATUS: 100% SUKSES MEMENUHI SELURUH DEFINITION OF DONE MODUL 12      ");
        } else {
            System.out.println("   STATUS: TERDAPAT PENGUJIAN YANG GAGAL                                 ");
        }
        System.out.println("==========================================================================");
    }

    private static void recordResult(String skenario, boolean success, String detail) {
        totalTests++;
        if (success) {
            passedTests++;
            System.out.printf("[PASSED] %-38s | %s\n", skenario, detail);
        } else {
            System.out.printf("[FAILED] %-38s | %s\n", skenario, detail);
        }
    }

    // 1. Simpan dua buku valid
    private static void testSkenario1_SimpanDuaBukuValid(Path dir) throws IOException {
        Path file = dir.resolve("skenario1_valid.txt");
        Perpustakaan perpus = new Perpustakaan();
        perpus.tambahBuku(new Buku("B001", "Dasar Java"));
        perpus.tambahBuku(new Buku("B002", "Konsep OOP"));

        perpus.simpan(file);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        boolean ok = lines.size() == 2
                && lines.get(0).equals("B001|Dasar Java")
                && lines.get(1).equals("B002|Konsep OOP");
        recordResult("Skenario 1: Simpan 2 Buku Valid", ok,
                "File berisi " + lines.size() + " baris data terformat benar.");
    }

    // 2. Muat dua buku valid
    private static void testSkenario2_MuatDuaBukuValid(Path dir) throws IOException {
        Path file = dir.resolve("skenario1_valid.txt");
        Perpustakaan perpus = new Perpustakaan();
        perpus.muat(file);

        boolean ok = perpus.getTotalBuku() == 2
                && perpus.cariBuku("B001") != null
                && perpus.cariBuku("B002") != null
                && perpus.cariBuku("B001").getJudul().equals("Dasar Java")
                && perpus.cariBuku("B002").getJudul().equals("Konsep OOP");
        recordResult("Skenario 2: Muat 2 Buku Valid", ok,
                "Kedua objek Buku berhasil dibentuk kembali ke memori.");
    }

    // 3. Muat file yang tidak ada
    private static void testSkenario3_MuatFileTidakAda(Path dir) {
        Path nonExistent = dir.resolve("file_yang_sama_sekali_tidak_ada.txt");
        Perpustakaan perpus = new Perpustakaan();
        boolean caught = false;
        String errMsg = "";
        try {
            perpus.muat(nonExistent);
        } catch (IOException e) {
            caught = true;
            errMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        recordResult("Skenario 3: Muat File Tidak Ada", caught,
                "IOException tertangkap sesuai ekspektasi (" + errMsg + ").");
    }

    // 4. Ubah satu baris menjadi B003 (format salah tanpa separator '|')
    private static void testSkenario4_FormatBarisSalah(Path dir) throws IOException {
        Path file = dir.resolve("skenario4_format_salah.txt");
        Files.write(file, Arrays.asList("B001|Dasar Java", "B003"), StandardCharsets.UTF_8);

        Perpustakaan perpus = new Perpustakaan();
        boolean caught = false;
        String errMsg = "";
        try {
            perpus.muat(file);
        } catch (IOException e) {
            caught = true;
            errMsg = e.getMessage();
        }
        boolean ok = caught && errMsg.contains("Format salah pada baris 2");
        recordResult("Skenario 4: Format Baris Salah", ok,
                "Ditolak dengan pesan: \"" + errMsg + "\"");
    }

    // 5. Ubah satu baris menjadi B003| (judul kosong / hanya spasi)
    private static void testSkenario5_FieldKosong(Path dir) throws IOException {
        Path file = dir.resolve("skenario5_judul_kosong.txt");
        Files.write(file, Arrays.asList("B001|Dasar Java", "B003|   "), StandardCharsets.UTF_8);

        Perpustakaan perpus = new Perpustakaan();
        boolean caught = false;
        String errMsg = "";
        try {
            perpus.muat(file);
        } catch (IOException e) {
            caught = true;
            errMsg = e.getMessage();
        }
        boolean ok = caught && errMsg.contains("Data tidak valid pada baris 2")
                && errMsg.contains("Kode dan judul wajib diisi");
        recordResult("Skenario 5: Judul Kosong / Whitespace", ok,
                "Ditolak dengan pesan: \"" + errMsg + "\"");
    }

    // 6. Isi file dengan kode duplikat
    private static void testSkenario6_KodeDuplikat(Path dir) throws IOException {
        Path file = dir.resolve("skenario6_duplikat.txt");
        Files.write(file, Arrays.asList("B001|Dasar Java", "B001|Java Lanjutan"), StandardCharsets.UTF_8);

        Perpustakaan perpus = new Perpustakaan();
        boolean caught = false;
        String errMsg = "";
        try {
            perpus.muat(file);
        } catch (IOException e) {
            caught = true;
            errMsg = e.getMessage();
        }
        boolean ok = caught && errMsg.contains("Data tidak valid pada baris 2")
                && errMsg.contains("Kode buku sudah terdaftar");
        recordResult("Skenario 6: Kode Duplikat dalam File", ok,
                "Ditolak dengan pesan: \"" + errMsg + "\"");
    }

    // 7. Muat file kosong
    private static void testSkenario7_MuatFileKosong(Path dir) throws IOException {
        Path file = dir.resolve("skenario7_kosong.txt");
        Files.write(file, Arrays.asList(), StandardCharsets.UTF_8);

        Perpustakaan perpus = new Perpustakaan();
        perpus.tambahBuku(new Buku("B999", "Buku Pre-Existing"));
        perpus.muat(file);

        boolean ok = perpus.getTotalBuku() == 0 && perpus.getDaftarBuku().isEmpty();
        recordResult("Skenario 7: Muat File Kosong", ok,
                "Koleksi berhasil dikosongkan secara valid tanpa galat.");
    }

    // 8. Uji Konsistensi State (Atomic Swap)
    private static void testSkenario8_KonsistensiStateAtomicSwap(Path dir) throws IOException {
        Path fileRusak = dir.resolve("skenario8_corrupt.txt");
        // Baris 1 valid, baris 2 corrupt
        Files.write(fileRusak, Arrays.asList(
                "B100|Buku Baru Valid",
                "BARIS_KORUP_TANPA_SEPARATOR"
        ), StandardCharsets.UTF_8);

        Perpustakaan perpus = new Perpustakaan();
        perpus.tambahBuku(new Buku("B000", "Buku Master Eksisting Tetap Aman"));

        boolean caught = false;
        try {
            perpus.muat(fileRusak);
        } catch (IOException e) {
            caught = true;
        }

        // State harus tetap utuh: B000 ada, size == 1, B100 tidak masuk
        boolean ok = caught
                && perpus.getTotalBuku() == 1
                && perpus.cariBuku("B000") != null
                && perpus.cariBuku("B100") == null;

        recordResult("Skenario 8: Konsistensi State (Atomic Swap)", ok,
                "State awal tetap utuh (1 buku) saat file rusak dibaca; tidak ada partial update.");
    }

    // 9. Skenario Tambahan: Pencegahan Karakter Terlarang saat Simpan
    private static void testSkenario9_PencegahanKarakterTerlarang(Path dir) throws IOException {
        Path file = dir.resolve("skenario9_terlarang.txt");
        Perpustakaan perpus = new Perpustakaan();
        // Coba masukkan buku yang mengandung pipa atau newline
        perpus.tambahBuku(new Buku("B101", "Buku|Dengan|Pipa"));

        boolean caught = false;
        String errMsg = "";
        try {
            perpus.simpan(file);
        } catch (IllegalArgumentException e) {
            caught = true;
            errMsg = e.getMessage();
        }

        boolean ok = caught && errMsg.contains("pemisah terlarang");
        recordResult("Skenario 9: Proteksi Karakter Terlarang", ok,
                "Karakter pipa/newline ditolak sebelum file ditulis.");
    }

    private static void cleanupTempDir(Path dir) {
        try {
            if (Files.exists(dir)) {
                Files.walk(dir)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {}
                        });
            }
        } catch (Exception ignored) {}
    }
}
