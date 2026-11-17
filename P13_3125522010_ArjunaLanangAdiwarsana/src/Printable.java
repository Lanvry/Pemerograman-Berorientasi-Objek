/**
 * Kontrak Interface Segregation Principle (ISP) untuk obyek yang dapat dicetak.
 * Client pencetak struk hanya bergantung pada interface ini.
 *
 * @author Arjuna Lanang Adiwarsana (3125522010)
 */
public interface Printable {
    /**
     * Menghasilkan representasi string terformat untuk kebutuhan cetak.
     * @return representasi teks terformat
     */
    String getPrintContent();
}
