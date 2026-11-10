import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Driver class Main untuk menguji fungsionalitas penyimpanan dan pembacaan data ke file.
 * Mendukung argument command-line "simpan" dan "muat" sesuai spesifikasi Modul 12.
 * 
 * Modul 12: Penyimpanan dan Pembacaan Data ke File
 * Penulis: Arjuna Lanang Adiwarsana (3125522010)
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 1
                || !(args[0].equals("simpan")
                || args[0].equals("muat")
                || args[0].equalsIgnoreCase("test"))) {
            System.out.println(
                "Gunakan: java Main simpan atau java Main muat"
            );
            return;
        }

        if (args[0].equalsIgnoreCase("test")) {
            TestRunner.main(new String[]{});
            return;
        }

        Perpustakaan perpustakaan = new Perpustakaan();
        Path lokasi = Paths.get("buku.txt");

        try {
            if (args[0].equals("simpan")) {
                perpustakaan.tambahBuku(
                    new Buku("B001", "Dasar Java")
                );
                perpustakaan.tambahBuku(
                    new Buku("B002", "Konsep OOP")
                );
                perpustakaan.simpan(lokasi);
                System.out.println("Data berhasil disimpan.");
            } else {
                perpustakaan.muat(lokasi);
                perpustakaan.tampilkanBuku();
            }
        } catch (IOException e) {
            System.out.println(
                "Operasi file gagal: " + e.getMessage()
            );
        } catch (IllegalArgumentException e) {
            System.out.println(
                "Data ditolak: " + e.getMessage()
            );
        }
    }
}
