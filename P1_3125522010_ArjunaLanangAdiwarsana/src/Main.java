public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   SISTEM RENTAL KENDARAAN (AUTORENT) - PBO P1    ");
        System.out.println("==================================================");

        // Membuat objek 1 dari NamaClass (Kendaraan)
        NamaClass mobil1 = new NamaClass();
        mobil1.platNomor = "N 1234 ABC";
        mobil1.merk = "Toyota Avanza";
        mobil1.tarifPerHari = 350000;
        mobil1.isTersedia = true;

        // Membuat objek 2 dari NamaClass (Kendaraan)
        NamaClass mobil2 = new NamaClass();
        mobil2.platNomor = "L 5678 DEF";
        mobil2.merk = "Honda CR-V";
        mobil2.tarifPerHari = 600000;
        mobil2.isTersedia = false;

        // Menampilkan data objek
        System.out.println("\n[1] Menampilkan Data Kendaraan Pertama:");
        mobil1.tampilkanData();

        System.out.println("\n[2] Menampilkan Data Kendaraan Kedua:");
        mobil2.tampilkanData();

        // Mengubah status sewa kendaraan 1
        System.out.println("\n[3] Memproses Peminjaman Kendaraan 1...");
        mobil1.setStatusSewa(false);

        System.out.println("\n[4] Data Kendaraan Pertama Setelah Dipinjam:");
        mobil1.tampilkanData();

        System.out.println("==================================================");
        System.out.println("Program Praktikum P1 Berhasil Dijalankan!");
        System.out.println("==================================================");
    }
}
