public class NamaClass {
    String platNomor;
    String merk;
    double tarifPerHari;
    boolean isTersedia;

    void tampilkanData() {
        System.out.println("=== Detail Data Kendaraan ===");
        System.out.println("Plat Nomor    : " + platNomor);
        System.out.println("Merk Kendaraan: " + merk);
        System.out.println("Tarif / Hari  : Rp " + String.format("%,.0f", tarifPerHari));
        System.out.println("Status        : " + (isTersedia ? "Tersedia" : "Tersewa"));
        System.out.println("-----------------------------");
    }

    void setStatusSewa(boolean status) {
        this.isTersedia = status;
    }
}
