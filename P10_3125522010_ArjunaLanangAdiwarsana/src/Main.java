public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("      PRAKTIKUM PBO MODUL 10: EXCEPTION HANDLING & VALIDASI STATE         ");
        System.out.println("      Nama: Arjuna Lanang Adiwarsana | NRP: 3125522010                   ");
        System.out.println("==========================================================================\n");

        Produk produk = new Produk(5);
        System.out.println("Inisialisasi Produk dengan stok awal: " + produk.getStok() + " unit.\n");

        // Daftar input pengujian sesuai modul langkah 3 dan penugasan tambahan ("-1" dan "3")
        String[] daftarInput = {"2", "0", "9", "abc", "-1", "3"};

        for (String input : daftarInput) {
            System.out.println("Input pembelian: " + input);
            try {
                int jumlah = Integer.parseInt(input);
                produk.beli(jumlah);
                System.out.println("Pembelian berhasil.");
            } catch (NumberFormatException e) {
                System.out.println("Ditolak: input harus bilangan bulat.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ditolak: " + e.getMessage());
            } catch (StokTidakCukupException e) {
                System.out.println("Ditolak: " + e.getMessage());
            } finally {
                System.out.println("Stok akhir: " + produk.getStok());
                System.out.println("--------------------------------------------------");
            }
        }
    }
}
